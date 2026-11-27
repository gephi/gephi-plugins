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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import org.junit.jupiter.api.Test;

/**
 * A port that another program holds is for the user to fix, and the message the failed start
 * returns says so: Gephi shows it in a dialog at startup and in the server panel. The log line
 * must not carry the BindException as well, because Gephi's crash reporter sends every logged
 * exception, at any level, as a bug report.
 */
class InstallerBindFailureTest {

    @Test
    void portInUseIsExplainedAndLogsNoException() throws Exception {
        // Port 0 lets the system choose a free port, which this socket then holds.
        try (ServerSocket holder = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
             LogCapture log = new LogCapture(Installer.class)) {
            int port = holder.getLocalPort();

            String message = new Installer().startNow(port);

            assertNotNull(message, "a failed start must say why");
            assertTrue(message.contains(Integer.toString(port)), message);
            assertFalse(log.records().isEmpty(), "the failure must still leave a line in the log");
            log.assertNoException();
            assertTrue(log.records().stream().anyMatch(r -> r.getLevel().intValue() >= Level.WARNING.intValue()),
                "a server that could not start belongs in the log at WARNING or above");
        }
    }
}
