package fr.totetmatt.blueskygephi.atproto;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Syntax checks for the {@code at-identifier} format that Bluesky applies to
 * every {@code actor} parameter: a handle ({@code alice.bsky.social}) or a DID
 * ({@code did:plc:...}). A bare word such as a keyword or a display name is
 * neither, and the API answers it with HTTP 400 {@code InvalidRequest}.
 */
public final class AtIdentifier {

    // https://atproto.com/specs/handle: at least two labels, the last one starts
    // with a letter, 253 characters at most.
    private static final Pattern HANDLE = Pattern.compile(
            "([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?");
    // https://atproto.com/specs/did: did:<lower-case method>:<method specific id>,
    // 2048 characters at most.
    private static final Pattern DID = Pattern.compile("did:[a-z]+:[a-zA-Z0-9._:%-]*[a-zA-Z0-9._-]");
    // Invisible direction marks and zero-width characters that copying a handle
    // out of a web page or an app can bring along.
    private static final Pattern INVISIBLE = Pattern.compile("[\\u200B-\\u200F\\u202A-\\u202E\\u2066-\\u2069\\uFEFF]");
    private static final char NO_BREAK_SPACE = 0xA0;

    private AtIdentifier() {
    }

    public static boolean isHandle(String s) {
        return s != null && s.length() <= 253 && HANDLE.matcher(s).matches();
    }

    public static boolean isDid(String s) {
        return s != null && s.length() <= 2048 && DID.matcher(s).matches();
    }

    /** @return true if {@code s} can be sent as an {@code actor}, exactly as it is. */
    public static boolean isValid(String s) {
        return isHandle(s) || isDid(s);
    }

    /**
     * Tidies what a user typed or pasted: drops surrounding white space, invisible
     * direction marks and a leading {@code @}, and lower-cases a handle (handles
     * are case-insensitive). Anything that still is not a handle or a DID is
     * returned as it is, for {@link #isValid(String)} to reject.
     */
    public static String clean(String raw) {
        if (raw == null) {
            return "";
        }
        String s = INVISIBLE.matcher(raw).replaceAll("").replace(NO_BREAK_SPACE, ' ').strip();
        if (s.startsWith("@")) {
            s = s.substring(1).strip();
        }
        return isHandle(s) ? s.toLowerCase(Locale.ROOT) : s;
    }
}
