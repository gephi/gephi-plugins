package fr.totetmatt.blueskygephi.atproto;

/**
 *
 * @author totetmatt
 */
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.totetmatt.blueskygephi.atproto.response.AppBskyGraphGetFollowers;
import fr.totetmatt.blueskygephi.atproto.response.AppBskyGraphGetFollows;
import fr.totetmatt.blueskygephi.atproto.response.AppBskyGraphGetList;
import fr.totetmatt.blueskygephi.atproto.response.ComAtprotoServerCreateSession;
import fr.totetmatt.blueskygephi.atproto.response.Paged;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.logging.Logger;

public class AtClient {

    private static final Logger logger = Logger.getLogger(AtClient.class.getName());

    private static final int MAX_RETRIES = 5;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    // Cap on how long a single backoff can sleep. Rate-limit windows are up to
    // 5 minutes, so honor a reported reset that far out but no further.
    private static final long MAX_BACKOFF_MS = 300_000L;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final AtContext context;
    private final HttpClient client;

    // Mutated by refreshSession() from any worker thread, read everywhere.
    private volatile ComAtprotoServerCreateSession session = null;

    public AtClient(String host) {
        this(host, HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
    }

    // Package-private: lets tests inject a mocked HttpClient without hitting the network.
    AtClient(String host, HttpClient client) {
        this.context = new AtContext(host);
        this.client = client;
    }

    public boolean comAtprotoServerCreateSession(String identifier, String password) {
        try {
            // Build the JSON body with the mapper so a password containing
            // quotes/backslashes can't break or inject into the request.
            String body = objectMapper.writeValueAsString(Map.of(
                    "identifier", identifier,
                    "password", password));
            HttpRequest request = HttpRequest.newBuilder(context.getURIForLexicon("com.atproto.server.createSession"))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.warning("Bluesky createSession failed (HTTP " + response.statusCode() + "): " + response.body());
                this.session = null;
                return false;
            }
            this.session = objectMapper.readValue(response.body(), ComAtprotoServerCreateSession.class);
            return this.session != null && this.session.getAccessJwt() != null;
        } catch (IOException e) {
            logger.warning("Bluesky createSession error: " + e.getMessage());
            this.session = null;
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.session = null;
            return false;
        }
    }

    /**
     * Exchanges the (short-lived) access token for a fresh one using the refresh
     * token. Synchronized so two worker threads that both hit a 401 at the same
     * time don't each burn a refresh.
     *
     * @param staleAccessJwt the access token that just got a 401; if the current
     * session no longer uses it, another thread already refreshed and we're done.
     */
    private synchronized boolean refreshSession(String staleAccessJwt) {
        ComAtprotoServerCreateSession current = session;
        if (current == null) {
            return false;
        }
        if (staleAccessJwt != null && !staleAccessJwt.equals(current.getAccessJwt())) {
            return true;
        }
        if (current.getRefreshJwt() == null) {
            return false;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(context.getURIForLexicon("com.atproto.server.refreshSession"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + current.getRefreshJwt())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.warning("Bluesky refreshSession failed (HTTP " + response.statusCode() + ")");
                return false;
            }
            ComAtprotoServerCreateSession refreshed = objectMapper.readValue(response.body(), ComAtprotoServerCreateSession.class);
            if (refreshed == null || refreshed.getAccessJwt() == null) {
                return false;
            }
            this.session = refreshed;
            return true;
        } catch (IOException e) {
            logger.warning("Bluesky refreshSession error: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Single entry point for authenticated GET calls: attaches the bearer token,
     * refreshes once on 401 and retries, backs off and retries on 429/5xx and
     * transient network errors, and deserializes a 200 body into {@code type}.
     */
    private <T> T get(String lexicon, Map<String, String> params, Class<T> type, LongConsumer onWaiting) {
        boolean refreshed = false;
        int attempt = 0;
        while (true) {
            ComAtprotoServerCreateSession current = session;
            if (current == null || current.getAccessJwt() == null) {
                throw new AtProtoException("Not authenticated with Bluesky. Please connect first.");
            }
            String usedAccessJwt = current.getAccessJwt();
            try {
                HttpRequest request = HttpRequest.newBuilder(context.getURIForLexicon(lexicon, params))
                        .GET()
                        .timeout(REQUEST_TIMEOUT)
                        .header("Authorization", "Bearer " + usedAccessJwt)
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                if (statusCode == 200) {
                    return objectMapper.readValue(response.body(), type);
                }
                if (statusCode == 401 && !refreshed) {
                    refreshed = true;
                    if (refreshSession(usedAccessJwt)) {
                        continue; // retry immediately with the new token (not counted as a retry)
                    }
                    throw new AtProtoException(statusCode, lexicon, response.body());
                }
                if (statusCode == 429 && attempt < MAX_RETRIES) {
                    // Rate limited: wait it out and surface the countdown to the caller.
                    backoff(response, attempt++, onWaiting);
                    continue;
                }
                if (statusCode >= 500 && attempt < MAX_RETRIES) {
                    backoff(response, attempt++, null);
                    continue;
                }
                throw new AtProtoException(statusCode, lexicon, response.body());
            } catch (IOException e) {
                if (attempt < MAX_RETRIES) {
                    backoff(null, attempt++, null);
                    continue;
                }
                throw new AtProtoException("XRPC " + lexicon + " failed after retries", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AtProtoException("XRPC " + lexicon + " interrupted", e);
            }
        }
    }

    /**
     * Follows an XRPC cursor, handing each page to {@code onPage} as it arrives
     * so callers can write results incrementally (and be cancelled between
     * pages) instead of buffering an entire crawl in memory.
     */
    private <T extends Paged> void paginate(String lexicon, Map<String, String> baseParams,
            Class<T> type, Optional<Integer> maxPages, Consumer<T> onPage, LongConsumer onWaiting) {
        Map<String, String> params = new HashMap<>(baseParams);
        int page = 0;
        while (maxPages.isEmpty() || page < maxPages.get()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            T response = get(lexicon, params, type, onWaiting);
            onPage.accept(response);
            if (response.getCursor() == null) {
                return;
            }
            params.put("cursor", response.getCursor());
            page++;
        }
    }

    /**
     * Sleeps before a retry. On a 429 it honors the {@code RateLimit-Reset}
     * (epoch seconds) or {@code Retry-After} header; otherwise it uses
     * randomized exponential backoff. Responds to interruption for cancellation.
     */
    private void backoff(HttpResponse<?> response, int attempt, LongConsumer onWaiting) {
        long waitMs = Math.min(computeWaitMs(response, attempt) + ThreadLocalRandom.current().nextLong(250), MAX_BACKOFF_MS);
        long deadline = System.currentTimeMillis() + waitMs;
        try {
            long remaining;
            while ((remaining = deadline - System.currentTimeMillis()) > 0) {
                if (onWaiting != null) {
                    onWaiting.accept(remaining);
                }
                // Tick ~once per second so the caller can render a live countdown,
                // and so cancellation (interrupt) is picked up promptly.
                Thread.sleep(Math.min(remaining, 1000L));
            }
            if (onWaiting != null) {
                onWaiting.accept(0L);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AtProtoException("Interrupted during backoff", e);
        }
    }

    private long computeWaitMs(HttpResponse<?> response, int attempt) {
        long waitMs = (long) (Math.pow(2, attempt) * 500L); // 0.5s, 1s, 2s, 4s, 8s, ...
        if (response != null) {
            Optional<String> reset = response.headers().firstValue("ratelimit-reset");
            Optional<String> retryAfter = response.headers().firstValue("retry-after");
            if (reset.isPresent()) {
                try {
                    long resetMs = Long.parseLong(reset.get().trim()) * 1000L - System.currentTimeMillis();
                    if (resetMs > 0) {
                        waitMs = resetMs;
                    }
                } catch (NumberFormatException ignored) {
                    // fall back to exponential backoff
                }
            } else if (retryAfter.isPresent()) {
                try {
                    long retryMs = Long.parseLong(retryAfter.get().trim()) * 1000L;
                    if (retryMs > 0) {
                        waitMs = retryMs;
                    }
                } catch (NumberFormatException ignored) {
                    // fall back to exponential backoff
                }
            }
        }
        return waitMs;
    }

    public void appBskyGraphGetFollowers(String actor, Optional<Integer> maxPages, Consumer<AppBskyGraphGetFollowers> onPage, LongConsumer onWaiting) {
        paginate("app.bsky.graph.getFollowers", Map.of("actor", actor, "limit", "100"),
                AppBskyGraphGetFollowers.class, maxPages, onPage, onWaiting);
    }

    public void appBskyGraphGetFollows(String actor, Optional<Integer> maxPages, Consumer<AppBskyGraphGetFollows> onPage, LongConsumer onWaiting) {
        paginate("app.bsky.graph.getFollows", Map.of("actor", actor, "limit", "100"),
                AppBskyGraphGetFollows.class, maxPages, onPage, onWaiting);
    }

    public void appBskyGraphGetList(String list, Consumer<AppBskyGraphGetList> onPage, LongConsumer onWaiting) {
        paginate("app.bsky.graph.getList", Map.of("list", list, "limit", "100"),
                AppBskyGraphGetList.class, Optional.empty(), onPage, onWaiting);
    }
}
