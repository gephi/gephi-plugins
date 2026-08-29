/*
 * Copyright 2026 Matt Artz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gephi.plugins.mcp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.junit.jupiter.api.Test;

/**
 * Contention tests for the deadlock-safe lock helpers, against the real
 * ReentrantReadWriteLock behind a standalone GraphModel (two threads, no running
 * Gephi). lockWrite and lockRead poll a timed tryLock instead of parking in the
 * lock's wait queue (see the comments on those helpers); what matters under
 * contention is that they wait through a held lock and then genuinely acquire,
 * rather than failing fast, wedging forever, or reporting a hold they do not have.
 *
 * <p>Contention is established by a latch the test itself controls, never by a
 * sleep. The holder keeps the lock until this test releases it, so "the contender
 * is still blocked" is a fact about the lock rather than a guess about scheduling.
 * The only wall-clock value here is the probe below, and it can fail in one
 * direction only: if the contender acquires while the holder provably still holds,
 * which is the defect these tests exist to catch.
 */
class LockContentionTest {

    /** How long to watch a contender that must not succeed yet. */
    private static final long BLOCKED_PROBE_MS = 300;

    /** Generous ceiling for an acquisition that should follow release almost at once. */
    private static final long ACQUIRE_TIMEOUT_S = 15;

    private static Graph newGraph() {
        return GraphModel.Factory.newInstance().getGraph();
    }

    @Test
    void lockWriteWaitsOutAContendingReaderAndActuallyAcquires() throws Exception {
        Graph g = newGraph();
        ReentrantReadWriteLock.ReadLock rl = GephiControlService.readLockHandle(g);
        assertNotNull(rl, "read lock handle must be reachable (lockWrite depends on it)");

        CountDownLatch readerHolds = new CountDownLatch(1);
        CountDownLatch releaseReader = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            rl.lock();
            try {
                readerHolds.countDown();
                releaseReader.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                rl.unlock();
            }
        }, "contending-reader");
        reader.setDaemon(true);
        reader.start();
        assertTrue(readerHolds.await(5, TimeUnit.SECONDS), "reader thread failed to start");

        // lockWrite runs on its own thread because a write hold count is per-thread,
        // and because the main thread must stay free to release the reader.
        CountDownLatch acquired = new CountDownLatch(1);
        AtomicInteger holdCountWhileHeld = new AtomicInteger(-1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread writer = new Thread(() -> {
            try {
                GephiControlService.lockWrite(g);
                try {
                    holdCountWhileHeld.set(g.getLock().getWriteHoldCount());
                } finally {
                    GephiControlService.unlockWrite(g);
                }
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                acquired.countDown();
            }
        }, "contending-writer");
        writer.setDaemon(true);
        writer.start();

        // The reader still holds, and only this thread can release it, so a writer
        // that finishes here acquired a write lock over a live read hold.
        assertFalse(acquired.await(BLOCKED_PROBE_MS, TimeUnit.MILLISECONDS),
            "lockWrite acquired while a reader still held the lock");

        releaseReader.countDown();

        assertTrue(acquired.await(ACQUIRE_TIMEOUT_S, TimeUnit.SECONDS),
            "lockWrite never acquired after the reader released");
        assertNull(failure.get(), () -> "lockWrite threw: " + failure.get());
        assertEquals(1, holdCountWhileHeld.get(),
            "lockWrite returned without actually holding the write lock");

        reader.join(5_000);
        writer.join(5_000);
        assertFalse(reader.isAlive(), "reader thread leaked");
        assertFalse(writer.isAlive(), "writer thread leaked");
    }

    @Test
    void lockReadWaitsOutAHeldWriterAndActuallyAcquires() throws Exception {
        Graph g = newGraph();
        ReentrantReadWriteLock.WriteLock wl = GephiControlService.writeLockHandle(g);
        assertNotNull(wl, "write lock handle must be reachable (lockRead's counterpart)");

        CountDownLatch writerHolds = new CountDownLatch(1);
        CountDownLatch releaseWriter = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            wl.lock();
            try {
                writerHolds.countDown();
                releaseWriter.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                wl.unlock();
            }
        }, "holding-writer");
        writer.setDaemon(true);
        writer.start();
        assertTrue(writerHolds.await(5, TimeUnit.SECONDS), "writer thread failed to start");

        CountDownLatch acquired = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread readerThread = new Thread(() -> {
            try {
                GephiControlService.lockRead(g);
                g.readUnlock();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                acquired.countDown();
            }
        }, "contending-reader");
        readerThread.setDaemon(true);
        readerThread.start();

        assertFalse(acquired.await(BLOCKED_PROBE_MS, TimeUnit.MILLISECONDS),
            "lockRead acquired while a writer still held the lock");

        releaseWriter.countDown();

        assertTrue(acquired.await(ACQUIRE_TIMEOUT_S, TimeUnit.SECONDS),
            "lockRead never acquired after the writer released");
        assertNull(failure.get(), () -> "lockRead threw: " + failure.get());

        writer.join(5_000);
        readerThread.join(5_000);
        assertFalse(writer.isAlive(), "writer thread leaked");
        assertFalse(readerThread.isAlive(), "reader thread leaked");
    }
}
