package fr.totetmatt.blueskygephi.atproto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AtIdentifierTest {

    // Invisible characters that come along when a handle is copied from a page or an app.
    private static final String LRE = String.valueOf((char) 0x202A);
    private static final String PDF = String.valueOf((char) 0x202C);
    private static final String NBSP = String.valueOf((char) 0x00A0);
    private static final String ZWSP = String.valueOf((char) 0x200B);

    /** Whether what the user typed can be sent once it is cleaned. */
    private static void assertAccepted(String typed) {
        assertTrue("should be accepted: " + typed, AtIdentifier.isValid(AtIdentifier.clean(typed)));
    }

    private static void assertRejected(String typed) {
        assertFalse("should be rejected: " + typed, AtIdentifier.isValid(AtIdentifier.clean(typed)));
    }

    @Test
    public void bareWordsAreNotIdentifiers() {
        assertRejected("keyword");
        assertRejected("KeyWord");
        assertRejected("@keyword");
        assertRejected("localhost");
        assertRejected("");
        assertRejected("   ");
    }

    @Test
    public void handles() {
        assertAccepted("alice.bsky.social");
        assertAccepted("Alice.Bsky.Social");
        assertAccepted("bsky.app");
        assertAccepted("a-b.example.com");
        assertAccepted("xn--nxasmq6b.example.com");
        assertAccepted("8.cn");
        assertAccepted("example.t");
        assertAccepted("name.t--t");
        assertAccepted("XX.LCS.MIT.EDU");
        assertRejected("alice.");
        assertRejected(".alice.com");
        assertRejected("-a.com");
        assertRejected("a-.com");
        assertRejected("a..com");
        assertRejected("john..test");
        assertRejected("xn--bcher-.tld");
        assertRejected("_underscore.com");
        assertRejected("name.org.");
        assertRejected("alice.123");
        assertRejected("cn.8");
        assertRejected("alice smith.com");
        assertRejected("alice bsky social");
        assertRejected("alice@bsky.social");
        assertRejected("alice_smith.bsky.social");
        assertRejected("https://bsky.app/profile/alice.bsky.social");
        assertRejected("a".repeat(64) + ".com");
        assertRejected("a.".repeat(130) + "com");
    }

    @Test
    public void handlesAreAscii() {
        assertRejected("m" + (char) 0x00FC + "nchen.example.com");
    }

    @Test
    public void dids() {
        assertAccepted("did:plc:z72i7hdynmk6r22z27h6tvur");
        assertAccepted("did:web:example.com");
        assertAccepted("did:web:localhost%3A3000");
        assertAccepted("did:method:val:two");
        assertAccepted("did:method::::val");
        assertAccepted("did:method:-:_:.:%ab");
        assertRejected("did:plc:");
        assertRejected("did:plc");
        assertRejected("did:PLC:abc");
        assertRejected("did:plc:abc:");
        assertRejected("did:plc:abc%");
        assertRejected("did:m123:val");
        assertRejected("DID:plc:abc");
        assertRejected("did:method:val/two");
        assertRejected("did:plc:a b");
    }

    @Test
    public void listUrisAreNotActors() {
        assertRejected("at://did:plc:z72i7hdynmk6r22z27h6tvur/app.bsky.graph.list/3kabc");
    }

    @Test
    public void cleanTidiesPastedText() {
        assertEquals("alice.bsky.social", AtIdentifier.clean("  @Alice.Bsky.Social \t"));
        assertEquals("alice.bsky.social", AtIdentifier.clean("alice.bsky.social"));
        // Direction marks and no-break spaces come along when copying from a page.
        assertEquals("alice.bsky.social", AtIdentifier.clean(LRE + "@alice.bsky.social" + PDF));
        assertEquals("alice.bsky.social", AtIdentifier.clean(NBSP + "alice.bsky.social" + NBSP));
        assertEquals("alice.bsky.social", AtIdentifier.clean("alice" + ZWSP + ".bsky.social"));
        // A DID is case-sensitive and is kept as typed.
        assertEquals("did:plc:AbC", AtIdentifier.clean(" did:plc:AbC "));
        assertEquals("", AtIdentifier.clean(null));
        assertEquals("", AtIdentifier.clean(" \t "));
    }

    @Test
    public void cleanLeavesListUrisAlone() {
        String list = "at://did:plc:z72i7hdynmk6r22z27h6tvur/app.bsky.graph.list/3kAbc";
        assertEquals(list, AtIdentifier.clean(" " + list + " "));
    }
}
