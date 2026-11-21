package fr.totetmatt.blueskygephi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import fr.totetmatt.blueskygephi.atproto.AtProtoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;

public class FailureLoggingTest {

    private final List<LogRecord> records = new ArrayList<>();
    private Logger logger;

    @Before
    public void captureLog() {
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
    }

    @Test
    public void apiFailureIsLoggedWithoutThrowable() {
        // Gephi's crash reporter sends every logged record that carries a Throwable,
        // so an error the user is already told about must be logged as a plain message.
        AtProtoException rejected = new AtProtoException(400, "app.bsky.graph.getFollows",
                "{\"error\":\"InvalidRequest\",\"message\":\"Invalid AT identifier\"}");

        BlueskyGephi.logFailure(logger, "Bluesky: could not fetch \"x\"", rejected.getUserMessageWithContent(), rejected);

        assertEquals(1, records.size());
        assertEquals(Level.WARNING, records.get(0).getLevel());
        assertNull(records.get(0).getThrown());
        assertTrue(records.get(0).getMessage(), records.get(0).getMessage().contains("Invalid AT identifier"));
    }

    @Test
    public void causeOfAnApiFailureStaysInTheLogMessage() {
        AtProtoException unreachable = new AtProtoException("XRPC app.bsky.graph.getFollows failed after retries",
                new IOException("connection reset"));

        BlueskyGephi.logFailure(logger, "Bluesky: could not fetch \"x\"", unreachable.getUserMessageWithContent(), unreachable);

        assertEquals(1, records.size());
        assertNull(records.get(0).getThrown());
        assertTrue(records.get(0).getMessage(), records.get(0).getMessage().contains("connection reset"));
    }

    @Test
    public void unexpectedFailureKeepsItsStackTrace() {
        IllegalStateException bug = new IllegalStateException("boom");

        BlueskyGephi.logFailure(logger, "Bluesky: could not fetch \"x\"", "Unexpected error", bug);

        assertEquals(1, records.size());
        assertEquals(Level.WARNING, records.get(0).getLevel());
        assertSame(bug, records.get(0).getThrown());
    }
}
