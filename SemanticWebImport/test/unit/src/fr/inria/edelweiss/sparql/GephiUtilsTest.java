package fr.inria.edelweiss.sparql;

import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import static org.junit.Assert.assertEquals;
import org.junit.*;

/**
 *
 * @author edemairy
 */
public class GephiUtilsTest {

    /**
     * Test of splitName method, of class GephiUtils.
     */
    @Test
    public void testSplitName() {
        final String expectedNamespace = "http://www.inria.fr/2007/09/11/humans.rdfs";
        final String expectedId = "age";

        GephiUtils.SplittedName splittedName = GephiUtils.splitName(expectedNamespace + '#' + expectedId);
        assertEquals(expectedId, splittedName.getId());
        assertEquals(expectedNamespace, splittedName.getNamespace());
        splittedName = GephiUtils.splitName(expectedNamespace + '/' + expectedId);
        assertEquals(expectedId, splittedName.getId());
        assertEquals(expectedNamespace, splittedName.getNamespace());
        splittedName = GephiUtils.splitName(expectedId);
        assertEquals(expectedId, splittedName.getId());
        assertEquals("", splittedName.getNamespace());
    }

    /**
     * Test of shortenName method, of class GephiUtils.
     */
    @Test
    public void testShortenName() {
        assertEquals("Empty strings must return an empty string", "", GephiUtils.shortenName(""));
        assertEquals("\"prefix#suffix\" must be shortened in \"suffix\"", "suffix",
                GephiUtils.shortenName("prefix#suffix"));
        assertEquals("\"prefix#suffix/\" must be shortened in \"suffix\"", "suffix",
                GephiUtils.shortenName("prefix#suffix/"));
        assertEquals("\"prefix1/prefix2#suffix1/suffix2\" must be shortened in \"suffix2\"", "suffix2",
                GephiUtils.shortenName("prefix1/prefix2#suffix1/suffix2"));
        assertEquals("csoulign", GephiUtils.shortenName("http://ns.inria.fr/isicil/id/person/csoulign"));

    }
}
    