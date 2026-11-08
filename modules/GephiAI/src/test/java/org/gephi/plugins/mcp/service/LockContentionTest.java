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

        CountDownLatch readerHolds = new CountDownLatch(1);
        CountDownLatch releaseReader = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            g.readLock();
            try {
                readerHolds.countDown();
                releaseReader.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                g.readUnlock();
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

        CountDownLatch writerHolds = new CountDownLatch(1);
        CountDownLatch releaseWriter = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            g.writeLock();
            try {
                writerHolds.countDown();
                releaseWriter.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                g.writeUnlock();
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

    /**
     * A long reader (a running statistic, in Gephi) must not let a waiting plugin write freeze
     * every other reader, and the write must give up with an error that does not blame the
     * renderer. A blocking writeLock() here parks at the head of the queue and new readers
     * (the renderer, layouts, the interface thread) wait behind it until the long reader ends.
     */
    @Test
    void lockWriteGivesUpBehindALongReaderWithoutBlockingOtherReaders() throws Exception {
        Graph g = newGraph();

        CountDownLatch readerHolds = new CountDownLatch(1);
        CountDownLatch releaseReader = new CountDownLatch(1);
        Thread longReader = new Thread(() -> {
            g.readLock();
            try {
                readerHolds.countDown();
                releaseReader.await(60, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                g.readUnlock();
            }
        }, "long-reader");
        longReader.setDaemon(true);
        longReader.start();
        assertTrue(readerHolds.await(5, TimeUnit.SECONDS), "reader thread failed to start");

        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch writerDone = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            try {
                GephiControlService.lockWrite(g);
                GephiControlService.unlockWrite(g);
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                writerDone.countDown();
            }
        }, "waiting-writer");
        writer.setDaemon(true);
        writer.start();

        try {
            // While the write waits, fresh readers must still get in, as the renderer does
            // every frame. Each gets well under a second; a parked writer would hold them
            // until the long reader ended.
            for (int i = 0; i < 5; i++) {
                Thread.sleep(500);
                CountDownLatch got = new CountDownLatch(1);
                Thread probe = new Thread(() -> {
                    g.readLock();
                    g.readUnlock();
                    got.countDown();
                }, "probe-reader");
                probe.setDaemon(true);
                probe.start();
                assertTrue(got.await(1, TimeUnit.SECONDS),
                    "a new reader waited behind the pending write");
            }

            assertTrue(writerDone.await(30, TimeUnit.SECONDS), "lockWrite never gave up");
            assertNotNull(failure.get(), "lockWrite acquired over a live read hold");
            String message = String.valueOf(failure.get().getMessage());
            assertTrue(message.contains("Graph is busy"), message);
            assertFalse(message.contains("renderer"), "the busy message blames the renderer: " + message);
            assertTrue(message.contains("1 read holds"), "the busy message must report the read hold: " + message);
            assertTrue(message.startsWith("Graph is busy"), "the busy message is wrapped: " + message);
        } finally {
            releaseReader.countDown();
            longReader.join(5_000);
            writer.join(5_000);
        }
    }

    /** A request thread that arrives interrupted gets an error and holds nothing. */
    @Test
    void lockReadOnAnInterruptedThreadThrowsAndHoldsNothing() {
        Graph g = newGraph();
        Thread.currentThread().interrupt();
        try {
            RuntimeException e = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class, () -> GephiControlService.lockRead(g));
            assertTrue(e.getMessage().contains("Interrupted"), e.getMessage());
            assertTrue(Thread.interrupted(), "the interrupt flag must be restored");
            assertEquals(0, g.getLock().getReadHoldCount(), "an interrupted lockRead left a read hold");
        } finally {
            Thread.interrupted();
        }
    }

    /**
     * A thread that already reads must get a nested read at once, even with a writer queued
     * behind it: Gephi calls back into code that reads while it holds a read itself.
     */
    @Test
    void lockReadReentersUnderAQueuedWriter() throws Exception {
        Graph g = newGraph();
        CountDownLatch readerHolds = new CountDownLatch(1);
        CountDownLatch writerQueued = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicInteger holds = new AtomicInteger(-1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread reader = new Thread(() -> {
            g.readLock();
            try {
                readerHolds.countDown();
                writerQueued.await(10, TimeUnit.SECONDS);
                GephiControlService.lockRead(g);
                holds.set(g.getLock().getReadHoldCount());
                g.readUnlock();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                g.readUnlock();
                done.countDown();
            }
        }, "reentrant-reader");
        reader.setDaemon(true);
        reader.start();
        assertTrue(readerHolds.await(5, TimeUnit.SECONDS), "reader thread failed to start");

        Thread writer = new Thread(() -> {
            g.writeLock();
            g.writeUnlock();
        }, "queued-writer");
        writer.setDaemon(true);
        writer.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (g.getLock().getQueueLength() < 1 && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
        assertEquals(1, g.getLock().getQueueLength(), "the writer never queued");
        writerQueued.countDown();

        assertTrue(done.await(2, TimeUnit.SECONDS), "a nested read waited behind the queued writer");
        assertNull(failure.get(), () -> "lockRead threw: " + failure.get());
        assertEquals(2, holds.get(), "the nested read did not hold");
        writer.join(5_000);
        assertFalse(writer.isAlive(), "writer thread leaked");
    }

    /** Inside the plugin's own write, a read (Gephi-internal calls make one) is granted at once. */
    @Test
    void lockReadInsideLockWriteAcquiresAtOnce() {
        Graph g = newGraph();
        GephiControlService.lockWrite(g);
        try {
            GephiControlService.lockRead(g);
            try {
                assertEquals(1, g.getLock().getReadHoldCount());
            } finally {
                g.readUnlock();
            }
        } finally {
            GephiControlService.unlockWrite(g);
        }
    }

    /**
     * A read that times out says what holds the lock, so a person can tell a writer (a long
     * operation) from a leaked read hold.
     */
    @Test
    void lockReadBusyMessageReportsTheLockState() throws Exception {
        Graph g = newGraph();
        CountDownLatch writerHolds = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            g.writeLock();
            try {
                writerHolds.countDown();
                release.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                g.writeUnlock();
            }
        }, "holding-writer");
        writer.setDaemon(true);
        writer.start();
        assertTrue(writerHolds.await(5, TimeUnit.SECONDS), "writer thread failed to start");
        try {
            RuntimeException e = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class, () -> GephiControlService.lockRead(g, 500_000_000L));
            String message = e.getMessage();
            assertTrue(message.contains("Graph is busy"), message);
            assertTrue(message.contains("write lock held"), message);
            assertTrue(message.contains("0 read holds"), message);
        } finally {
            release.countDown();
            writer.join(5_000);
        }
    }
}
