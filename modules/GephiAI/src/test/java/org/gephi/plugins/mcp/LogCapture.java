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

package org.gephi.plugins.mcp;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Collects what one class logs while a test runs. Gephi's crash reporter sends every logged
 * exception, at any level, as a bug report, so the log lines for a failure that is not a fault in
 * Gephi must carry no exception, and a test can say so with {@link #assertNoException()}.
 */
public final class LogCapture extends Handler implements AutoCloseable {

    private final Logger logger;
    private final List<LogRecord> records = Collections.synchronizedList(new ArrayList<>());

    /** Starts collecting what {@code owner} logs; close it to stop. */
    public LogCapture(Class<?> owner) {
        logger = Logger.getLogger(owner.getName());
        logger.addHandler(this);
    }

    public List<LogRecord> records() {
        synchronized (records) {
            return new ArrayList<>(records);
        }
    }

    /** Fails when anything logged carries an exception, which Gephi's crash reporter would send. */
    public void assertNoException() {
        for (LogRecord r : records()) {
            assertNull(r.getThrown(), "a logged exception reaches Gephi's crash reporter: " + r.getLevel()
                + " " + r.getMessage());
        }
    }

    @Override
    public void publish(LogRecord record) {
        records.add(record);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        logger.removeHandler(this);
    }
}
