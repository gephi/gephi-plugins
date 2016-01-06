/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.semanticweb.filter;

import fr.inria.wimmics.semanticweb.filter.type.TypeFilter;
import fr.inria.edelweiss.sparql.GephiUtils;
import fr.inria.edelweiss.sparql.corese.CoreseDriver;
import org.gephi.graph.api.Graph;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class SemanticWebFilterTest {

    public SemanticWebFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Ignore(value="Lack a simple way to load and execute a configuration.")
    @Test
    public void testFilter() {
        String[] expectedResult = {
            "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Alice",
            "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Catherine",
            "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Flora",
            "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Jennifer"
        };
        Graph graph = buildHumansGraph();
        TypeFilter filter = new TypeFilter();
        filter.setType("#Woman");
        Graph result = filter.filter(graph);
        assertEquals(expectedResult, result.getNodes());
    }

    private Graph buildHumansGraph() {

        return null;
//        CoreseDriver driver = new CoreseDriver();
//        driver.sparqlQuery(null);
    }
}
