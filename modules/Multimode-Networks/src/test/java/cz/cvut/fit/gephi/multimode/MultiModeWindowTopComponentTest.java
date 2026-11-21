package cz.cvut.fit.gephi.multimode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Drives the real panel the way the user does: load the columns, pick the column, the values to
 * project and click Run.
 */
public class MultiModeWindowTopComponentTest {

    private final List<Node> kindB = new ArrayList<Node>();
    private UndirectedGraph graph;
    private MultiModeWindowTopComponent panel;
    private JButton load;
    private JButton start;
    private JComboBox<?> attributes;
    private JComboBox<?> firstMatrix;
    private JComboBox<?> secondMatrix;

    @BeforeClass
    public static void useHeadlessSwing() {
        System.setProperty("java.awt.headless", "true");
    }

    @Before
    public void openThePanelOnABipartiteGraph() throws Exception {
        GraphModel model = TestGraphs.newProjectModel();
        graph = model.getUndirectedGraph();
        Node[] a = new Node[3];
        Node[] b = new Node[3];
        for (int i = 0; i < 3; i++) {
            a[i] = TestGraphs.addNode(model, "a" + i, "A");
            b[i] = TestGraphs.addNode(model, "b" + i, "B");
            kindB.add(b[i]);
        }
        for (int i = 0; i < 3; i++) {
            TestGraphs.addEdge(model, a[i], b[i], 1.0);
            TestGraphs.addEdge(model, a[i], b[(i + 1) % 3], 1.0);
        }

        panel = new MultiModeWindowTopComponent();
        load = field("load");
        start = field("start");
        attributes = field("attributes");
        firstMatrix = field("firstMatrix");
        secondMatrix = field("secondMatrix");
    }

    @Test
    public void runIsDisabledWhenTheValuesToProjectAreNoLongerInTheGraph() {
        loadAndSelectKind();
        assertTrue("a complete selection can be run", start.isEnabled());

        graph.removeAllNodes(kindB);
        firstMatrix.setSelectedItem(combination(firstMatrix, "A", "B"));

        assertEquals("no combination starts from a value which is gone", 0, secondMatrix.getItemCount());
        assertFalse("there is nothing to run without a second matrix", start.isEnabled());
    }

    @Test
    public void runDoesNothingWhenNoSecondMatrixIsSelected() {
        loadAndSelectKind();
        graph.removeAllNodes(kindB);
        firstMatrix.setSelectedItem(combination(firstMatrix, "A", "B"));
        int nodes = graph.getNodeCount();
        int edges = graph.getEdgeCount();

        clickRun();

        assertEquals(nodes, graph.getNodeCount());
        assertEquals(edges, graph.getEdgeCount());
        assertFalse(start.isEnabled());
    }

    @Test
    public void runIsDisabledWhenTheGraphWasCleared() {
        loadAndSelectKind();

        graph.clear();
        firstMatrix.setSelectedItem(combination(firstMatrix, "B", "A"));

        assertEquals(0, secondMatrix.getItemCount());
        assertFalse(start.isEnabled());
        clickRun();
        assertEquals(0, graph.getNodeCount());
    }

    @Test(timeout = 60000)
    public void runProjectsTheValuesSelected() throws Exception {
        loadAndSelectKind();
        firstMatrix.setSelectedItem(combination(firstMatrix, "A", "B"));
        secondMatrix.setSelectedItem(combination(secondMatrix, "B", "A"));
        assertTrue(start.isEnabled());

        start.doClick(0);

        // every A node shares a B node with each of the two others
        waitUntil(() -> graph.getEdgeCount() == 9);
        assertEquals(9, graph.getEdgeCount());
        assertEquals(6, graph.getNodeCount());
    }

    /**
     * What clicking Run does, whether the button looks enabled or not.
     */
    private void clickRun() {
        ActionEvent click = new ActionEvent(start, ActionEvent.ACTION_PERFORMED, start.getActionCommand());
        for (ActionListener listener : start.getActionListeners()) {
            listener.actionPerformed(click);
        }
    }

    private void loadAndSelectKind() {
        load.doClick(0);
        for (int i = 0; i < attributes.getItemCount(); i++) {
            Object item = attributes.getItemAt(i);
            if (item instanceof Column && TestGraphs.KIND.equals(((Column) item).getId())) {
                attributes.setSelectedIndex(i);
                return;
            }
        }
        fail("the kind column is not listed");
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
    }

    private static ValueCombination combination(JComboBox<?> box, String first, String second) {
        List<String> listed = new ArrayList<String>();
        for (int i = 0; i < box.getItemCount(); i++) {
            ValueCombination combination = (ValueCombination) box.getItemAt(i);
            if (combination.getFirst().equals(first) && combination.getSecond().equals(second)) {
                return combination;
            }
            listed.add(combination.toString());
        }
        throw new AssertionError(first + " - " + second + " is not listed in " + listed);
    }

    @SuppressWarnings("unchecked")
    private <T> T field(String name) throws Exception {
        Field field = MultiModeWindowTopComponent.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(panel);
    }
}
