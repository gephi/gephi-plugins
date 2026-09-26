package fr.totetmatt.blueskygephi.atproto.response;

/**
 * A cursor-paginated XRPC response. Implemented by every response type that the
 * generic pagination helper in {@code AtClient} can walk.
 *
 * @author totetmatt
 */
public interface Paged {

    /**
     * @return the cursor for the next page, or {@code null} when there are no
     * more pages.
     */
    String getCursor();
}
