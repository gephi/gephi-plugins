package fr.totetmatt.blueskygephi.atproto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class AtClientTest {

    private static final String FOLLOWS = "app.bsky.graph.getFollows";
    private static final String FOLLOWERS = "app.bsky.graph.getFollowers";
    private static final String DID = "did:plc:abcdefghijklmnopqrstuvwx";
    private static final String SESSION = "{\"did\":\"" + DID + "\",\"handle\":\"me.example.com\","
            + "\"accessJwt\":\"access\",\"refreshJwt\":\"refresh\"}";
    private static final String EMPTY_PAGE = "{\"subject\":{\"did\":\"" + DID + "\",\"handle\":\"alice.example.com\"},"
            + "\"follows\":[],\"followers\":[]}";

    private final StubHttpClient http = new StubHttpClient();
    private AtClient client;

    @Before
    public void logIn() {
        http.respond("com.atproto.server.createSession", 200, SESSION);
        client = new AtClient("pds.example.invalid", http);
        assertTrue(client.comAtprotoServerCreateSession("me.example.com", "app-password"));
        http.requests.clear();
    }

    /** What the Bluesky API answers when the actor is neither a handle nor a DID. */
    private static String invalidIdentifier(String method, String actor) {
        return "{\"error\":\"InvalidRequest\",\"message\":\"Invalid " + method
                + " params: Invalid AT identifier (got \\\"" + actor + "\\\")\"}";
    }

    private AtProtoException followsFailure(String actor) {
        try {
            client.appBskyGraphGetFollows(actor, Optional.empty(), page -> {
            }, null);
        } catch (AtProtoException e) {
            return e;
        }
        throw new AssertionError("expected an AtProtoException for \"" + actor + "\"");
    }

    private AtProtoException followersFailure(String actor) {
        try {
            client.appBskyGraphGetFollowers(actor, Optional.empty(), page -> {
            }, null);
        } catch (AtProtoException e) {
            return e;
        }
        throw new AssertionError("expected an AtProtoException for \"" + actor + "\"");
    }

    @Test
    public void followsOfABareWordAreNotRequested() {
        http.respond(FOLLOWS, 400, invalidIdentifier(FOLLOWS, "keyword"));

        AtProtoException e = followsFailure("keyword");

        // The API only knows handles and DIDs: a word that is neither must be
        // reported to the user without sending it, instead of coming back as HTTP 400.
        assertEquals("the request must not be sent, but failed with: " + e.getMessage(), -1, e.getStatusCode());
        assertEquals(List.of(), http.requests);
        assertTrue(e.getUserMessage(), e.getUserMessage().contains("keyword"));
        assertTrue(e.getUserMessage(), e.getUserMessage().contains("handle"));
    }

    @Test
    public void followersOfABareWordAreNotRequested() {
        http.respond(FOLLOWERS, 400, invalidIdentifier(FOLLOWERS, "keyword"));

        AtProtoException e = followersFailure("keyword");

        assertEquals("the request must not be sent, but failed with: " + e.getMessage(), -1, e.getStatusCode());
        assertEquals(List.of(), http.requests);
        assertTrue(e.getUserMessage(), e.getUserMessage().contains("keyword"));
    }

    @Test
    public void textThatIsNotAHandleOrDidIsNotRequested() {
        http.respond(FOLLOWS, 400, invalidIdentifier(FOLLOWS, "x"));
        List<String> notIdentifiers = List.of(
                "",
                "keyword",
                "@alice.bsky.social",
                "https://bsky.app/profile/alice.bsky.social",
                "alice bsky social",
                "alice.bsky.social/",
                "at://" + DID + "/app.bsky.graph.list/3kabc");

        for (String actor : notIdentifiers) {
            AtProtoException e = followsFailure(actor);
            assertEquals("\"" + actor + "\" must not be sent, but failed with: " + e.getMessage(), -1, e.getStatusCode());
        }

        assertEquals(List.of(), http.requests);
    }

    @Test
    public void handleAndDidAreSentAsGiven() {
        http.respond(FOLLOWS, 200, EMPTY_PAGE);
        http.respond(FOLLOWERS, 200, EMPTY_PAGE);

        client.appBskyGraphGetFollows("alice.example.com", Optional.empty(), page -> {
        }, null);
        client.appBskyGraphGetFollows(DID, Optional.empty(), page -> {
        }, null);
        client.appBskyGraphGetFollowers("alice.example.com", Optional.empty(), page -> {
        }, null);
        client.appBskyGraphGetFollowers(DID, Optional.empty(), page -> {
        }, null);

        assertEquals(4, http.requests.size());
        assertEquals("alice.example.com", http.param(0, "actor"));
        assertEquals(DID, http.param(1, "actor"));
        assertEquals("alice.example.com", http.param(2, "actor"));
        assertEquals(DID, http.param(3, "actor"));
    }

    @Test
    public void apiRejectionOfAWellFormedActorStillSurfacesAsAtProtoException() {
        http.respond(FOLLOWS, 400, "{\"error\":\"InvalidRequest\",\"message\":\"Profile not found\"}");

        AtProtoException e = followsFailure("nobody.example.com");

        // Only the server knows whether an account exists: its answer must still reach the user.
        assertEquals(400, e.getStatusCode());
        assertEquals(FOLLOWS, e.getLexicon());
        assertTrue(e.getUserMessageWithContent(), e.getUserMessageWithContent().contains("Profile not found"));
        assertEquals(1, http.requests.size());
    }
}
