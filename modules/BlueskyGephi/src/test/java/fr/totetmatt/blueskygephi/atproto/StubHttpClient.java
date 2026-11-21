package fr.totetmatt.blueskygephi.atproto;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

/**
 * In-memory stand-in for the Bluesky API, so that tests never open a socket.
 * It records every request it receives and answers with canned responses.
 * A request that no response was registered for fails the test at once instead
 * of being retried like a network error.
 */
final class StubHttpClient extends HttpClient {

    /** "METHOD /path?query" of every request received, in order. */
    final List<String> requests = new ArrayList<>();
    private final Map<String, Canned> canned = new HashMap<>();

    private record Canned(int status, String body) {
    }

    /** Answers every request whose path ends with {@code nsid}. */
    void respond(String nsid, int status, String body) {
        canned.put(nsid, new Canned(status, body));
    }

    /** The decoded value of query parameter {@code name} of the n-th request, or null. */
    String param(int request, String name) {
        String line = requests.get(request);
        int q = line.indexOf('?');
        if (q < 0) {
            return null;
        }
        for (String pair : line.substring(q + 1).split("&")) {
            if (pair.startsWith(name + "=")) {
                return URLDecoder.decode(pair.substring(name.length() + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
        URI uri = request.uri();
        String path = uri.getPath();
        requests.add(request.method() + " " + path + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery()));
        for (Map.Entry<String, Canned> e : canned.entrySet()) {
            if (path.endsWith(e.getKey())) {
                return (HttpResponse<T>) new Response(e.getValue().status(), e.getValue().body(), uri);
            }
        }
        throw new AssertionError("StubHttpClient: no response registered for " + path);
    }

    private static final class Response implements HttpResponse<String> {

        private final int status;
        private final String body;
        private final URI uri;

        Response(int status, String body, URI uri) {
            this.status = status;
            this.body = body;
            this.uri = uri;
        }

        @Override
        public int statusCode() {
            return status;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (name, value) -> true);
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return uri;
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> handler,
            HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return Redirect.NEVER;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        return null;
    }

    @Override
    public SSLParameters sslParameters() {
        return null;
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public HttpClient.Version version() {
        return HttpClient.Version.HTTP_1_1;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }
}
