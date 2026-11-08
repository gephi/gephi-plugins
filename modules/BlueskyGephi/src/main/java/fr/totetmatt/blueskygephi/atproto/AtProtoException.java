package fr.totetmatt.blueskygephi.atproto;

/**
 * Uniform error type for all XRPC calls, so callers get one consistent failure
 * mode instead of a mix of thrown {@link RuntimeException}s and {@code null}
 * returns.
 *
 * @author totetmatt
 */
public class AtProtoException extends RuntimeException {

    /** HTTP status when the failure came from a response, otherwise -1. */
    private final int statusCode;
    /** XRPC method that failed, when known. */
    private final String lexicon;
    /** Raw response body the server returned, when the failure came from a response. */
    private final String body;

    public AtProtoException(String message) {
        super(message);
        this.statusCode = -1;
        this.lexicon = null;
        this.body = null;
    }

    public AtProtoException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.lexicon = null;
        this.body = null;
    }

    public AtProtoException(int statusCode, String lexicon, String body) {
        super("XRPC " + lexicon + " failed (HTTP " + statusCode + "): " + body);
        this.statusCode = statusCode;
        this.lexicon = lexicon;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /** @return the XRPC method that failed, or {@code null} if not applicable. */
    public String getLexicon() {
        return lexicon;
    }

    /** @return the raw response body the server returned, or {@code null} if none. */
    public String getResponseBody() {
        return body;
    }

    /**
     * A short, human-readable explanation suitable for showing to the user,
     * derived from the HTTP status when there is one. The raw technical message
     * (with the lexicon and response body) stays available via
     * {@link #getMessage()} for logging.
     */
    public String getUserMessage() {
        switch (statusCode) {
            case 400:
                return "Bluesky rejected the request (HTTP 400). The handle or list id may be invalid.";
            case 401:
                return "Bluesky login expired or was rejected (HTTP 401). Reconnect with your handle and an app password.";
            case 403:
                return "Bluesky refused access (HTTP 403). This account may not be allowed to view that data.";
            case 404:
                return "Not found on Bluesky (HTTP 404). The handle or list may not exist.";
            case 429:
                return "Bluesky rate limit reached (HTTP 429). Please wait a while before trying again.";
            default:
                if (statusCode >= 500) {
                    return "Bluesky is having server issues (HTTP " + statusCode + "). Please try again later.";
                }
                if (statusCode > 0) {
                    return "Bluesky request failed (HTTP " + statusCode + ").";
                }
                // No HTTP status: authentication, network, timeout or cancellation.
                return getMessage();
        }
    }

    /**
     * {@link #getUserMessage()} plus the raw resource content (the server's
     * response body) when the failure carried one, so a report shows the exact
     * reason the server gave (e.g. {@code {"error":"ExpiredToken", ...}}) and
     * not just the generic summary.
     */
    public String getUserMessageWithContent() {
        String base = getUserMessage();
        if (body != null && !body.isBlank()) {
            String content = body.strip();
            // Keep the report readable if a server ever returns a large body.
            if (content.length() > MAX_CONTENT_CHARS) {
                content = content.substring(0, MAX_CONTENT_CHARS) + "…";
            }
            // The message already is the body when there's no HTTP status; avoid printing it twice.
            if (!content.equals(base)) {
                return base + "\n\nServer response: " + content;
            }
        }
        return base;
    }

    private static final int MAX_CONTENT_CHARS = 500;
}
