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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * The service survives being shut down and used again.
 *
 * <p>Stopping the server from Tools, Gephi AI Server calls {@code shutdown()}, which shuts
 * the layout executor down for good. The service is a singleton, so starting the server again
 * hands back the same instance. While the executor field was final, every layout after a
 * stop-and-start failed with {@code RejectedExecutionException} for the rest of the session,
 * and the only sign of it was the "Layout already running" message that follows.
 *
 * <p>The plugin's own smoke test used to step around this by calling NanoHTTPD's {@code stop()}
 * directly rather than {@code stopServer()}, which is what pointed at the defect: when a test
 * avoids a code path to stay green, the path is worth looking at.
 */
class ServiceRestartTest {

    private static ExecutorService executorOf(GephiControlService service) throws Exception {
        Field f = GephiControlService.class.getDeclaredField("layoutExecutor");
        f.setAccessible(true);
        return (ExecutorService) f.get(service);
    }

    /** Calls the private accessor the layout path uses, which is where the repair happens. */
    private static ExecutorService liveExecutorOf(GephiControlService service) throws Exception {
        java.lang.reflect.Method m = GephiControlService.class.getDeclaredMethod("layoutExecutor");
        m.setAccessible(true);
        return (ExecutorService) m.invoke(service);
    }

    @Test
    void theLayoutExecutorIsUsableAgainAfterShutdown() throws Exception {
        GephiControlService service = GephiControlService.getInstance();

        ExecutorService before = liveExecutorOf(service);
        assertNotNull(before, "a layout executor must exist before shutdown");
        assertFalse(before.isShutdown(), "precondition: the executor starts alive");

        service.shutdown();
        assertTrue(executorOf(service).isShutdown(), "shutdown() must actually stop the executor");

        ExecutorService after = liveExecutorOf(service);
        assertNotNull(after);
        assertFalse(after.isShutdown(),
            "after a stop and start, the layout executor must be usable again");
        assertNotSame(before, after, "a shut-down executor cannot be revived; expect a new one");

        // Prove it, rather than trusting isShutdown(): the executor must accept work.
        assertTrue(after.submit(() -> "ran").get(5, TimeUnit.SECONDS).equals("ran"),
            "the recreated executor must accept and run a task");

        // Leave the singleton in a working state for whatever runs next.
        assertFalse(liveExecutorOf(service).isShutdown());
    }

    @Test
    void repeatedShutdownsKeepRecovering() throws Exception {
        GephiControlService service = GephiControlService.getInstance();
        for (int i = 0; i < 3; i++) {
            service.shutdown();
            ExecutorService e = liveExecutorOf(service);
            assertFalse(e.isShutdown(), "cycle " + i + ": executor must recover");
            assertTrue(e.submit(() -> true).get(5, TimeUnit.SECONDS), "cycle " + i + ": must run work");
        }
    }
}
