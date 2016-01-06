/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.semantic.statistics;

import fr.inria.edelweiss.semantic.statistics.gui.TypeTreeModel;
import fr.inria.edelweiss.semantic.tests.Utils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author edemairy
 */
public class SemanticWebStatisticsTest {

    final static private String[][] humansTypeTree = {
        {"Female", "Animal"},
        {"Person", "Animal"},
        {"Researcher", "Person"},
        {"Lecturer", "Person"},
        {"Man", "Person"},
        {"Woman", "Person"},
        {"Male", "Animal"},
        {"Man", "Male"},
        {"Woman", "Female"}
    };

    public SemanticWebStatisticsTest() {
    }
    private Utils utils;

    @Before
    public void setUp() {
        utils = new Utils();
        utils.initGephi();
    }

    @Test
    public void testCountTypes() throws IOException, InterruptedException {
        SemanticWebStatistics statistics = new SemanticWebStatistics();
        String[][] expectedResult = {
            {}
        };
        Utils utils = new Utils();
        utils.initGephi();
        utils.whenUsingConfiguration("Humans");
        utils.getDriver().sparqlQuery("construct { ?x ?r ?y } where {?x ?r ?y}");
        // Workspace workspaces[] = utils.whenCreatingGraphs(topComponent);
        String[][] countTypesResult = utils.getSparqlRequester().selectOnGraph(SemanticWebStatistics.COUNT_TYPES);
        Map<String, Integer> model = statistics.countTypes(countTypesResult);

    }

    @Test
    public void testBuildTreeModel() throws IOException, InterruptedException {
        SemanticWebStatistics statistics = new SemanticWebStatistics();
        TypeTreeModel model = statistics.buildTreeModel(humansTypeTree);

        assertEquals(1, model.getRoot().getChildCount());
        assertEquals(3, model.findNode("Animal").getChildCount());
        assertEquals(4, model.findNode("Person").getChildCount());
        assertEquals(1, model.findNode("Male").getChildCount());
        assertEquals(1, model.findNode("Female").getChildCount());
        assertEquals(0, model.findNode("Woman").getChildCount());
        assertEquals(0, model.findNode("Man").getChildCount());
        statistics.displayTypeTree(model);
    }

    @Test
    public void testUpdateModelCount() throws IOException, InterruptedException {
        SemanticWebStatistics statistics = new SemanticWebStatistics();
        TypeTreeModel model = statistics.buildTreeModel(humansTypeTree);
        Map<String, Integer> count = new HashMap<String, Integer>();
        count.put("Animal", 1);
        count.put("Person", 1);
        statistics.updateModelCount(model, count);
    }
}
