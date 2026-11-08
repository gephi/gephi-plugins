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
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 * A request that fails with an exception logs it with its stack trace. Before this, a crash inside
 * Gephi (a null pointer in PNG export, for one) reached the caller as a one-line message and left
 * nothing in Gephi's log to report.
 */
class FailureLogTest {

    @Test
    void failureIsLoggedWithItsExceptionAndReturnedAsAnError() {
        Logger logger = Logger.getLogger(GephiControlService.class.getName());
        List<LogRecord> records = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord r) {
                records.add(r);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(handler);
        try {
            NullPointerException npe = new NullPointerException("item is null");
            JsonObject r = GephiControlService.failure("Export failed: ", npe);
            assertEquals(false, r.get("success").getAsBoolean());
            assertEquals("Export failed: item is null", r.get("error").getAsString());
            assertEquals(1, records.size());
            assertSame(npe, records.get(0).getThrown(), "the stack trace was not logged");
        } finally {
            logger.removeHandler(handler);
        }
    }
}
