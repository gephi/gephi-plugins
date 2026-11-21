package cz.cvut.fit.gephi.multimode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.ProjectController;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Lookup;

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
    private JCheckBox removeNodes;
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
        removeNodes = field("removeNodes");
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

    @Test(timeout = 60000)
    public void matricesAreListedAgainOnceARunHasRemovedNodes() throws Exception {
        loadAndSelectKind();
        firstMatrix.setSelectedItem(combination(firstMatrix, "A", "B"));
        secondMatrix.setSelectedItem(combination(secondMatrix, "B", "A"));
        removeNodes.setSelected(true);

        start.doClick(0);

        // the B nodes are removed by the run, and only A is left to pick from
        waitUntil(() -> graph.getNodeCount() == 3);
        waitUntil(() -> listed(firstMatrix).equals(Arrays.asList("A - A")));
        assertEquals(Arrays.asList("A - A"), listed(firstMatrix));
        assertEquals(Arrays.asList("A - A"), listed(secondMatrix));
        assertTrue(start.isEnabled());
    }

    @Test(timeout = 60000)
    public void runsAreExecutedOneAfterTheOther() throws Exception {
        loadAndSelectKind();
        firstMatrix.setSelectedItem(combination(firstMatrix, "A", "B"));
        secondMatrix.setSelectedItem(combination(secondMatrix, "B", "A"));

        graph.writeLock(); // the runs have to wait for the graph
        try {
            start.doClick(0);
            start.doClick(0);
            waitUntil(() -> threadsRunningTheTransformation() > 0);
            Thread.sleep(200); // time for the second run to start, if nothing holds it back
            assertEquals("the second run has to wait for the first one", 1, threadsRunningTheTransformation());
        } finally {
            graph.writeUnlock();
        }

        waitUntil(() -> graph.getEdgeCount() == 9);
        assertEquals(9, graph.getEdgeCount());
    }

    @Test(timeout = 60000)
    public void aRunWaitingForAnotherOneStillWorksOnTheWorkspaceItWasStartedIn() throws Exception {
        loadAndSelectKind();
        firstMatrix.setSelectedItem(combination(firstMatrix, "A", "B"));
        secondMatrix.setSelectedItem(combination(secondMatrix, "B", "A"));

        graph.writeLock(); // the first run waits for the graph
        try {
            start.doClick(0);
            // The second run, which removes the B nodes, waits for the first one: the user moves on to another
            // workspace in the meantime
            removeNodes.setSelected(true);
            start.doClick(0);
            Lookup.getDefault().lookup(ProjectController.class).openNewWorkspace();
            flushEventQueue();
        } finally {
            graph.writeUnlock();
        }

        waitUntil(() -> graph.getNodeCount() == 3);
        assertEquals("the B nodes are removed from the graph the run was started on", 3, graph.getNodeCount());
    }

    @Test
    public void matricesAreClearedWhenAnotherWorkspaceIsOpened() throws Exception {
        loadAndSelectKind();
        assertTrue(start.isEnabled());

        Lookup.getDefault().lookup(ProjectController.class).openNewWorkspace();
        flushEventQueue();

        assertEquals("the columns belong to the graph of the other workspace", 0, attributes.getItemCount());
        assertEquals(0, firstMatrix.getItemCount());
        assertEquals(0, secondMatrix.getItemCount());
        assertFalse(start.isEnabled());
        assertTrue("the columns of the new workspace can be loaded", load.isEnabled());
    }

    @Test
    public void matricesAreClearedWhenTheProjectIsClosed() throws Exception {
        loadAndSelectKind();
        assertTrue(start.isEnabled());

        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
        flushEventQueue();

        assertEquals(0, attributes.getItemCount());
        assertEquals(0, firstMatrix.getItemCount());
        assertEquals(0, secondMatrix.getItemCount());
        assertFalse(start.isEnabled());
        assertFalse(load.isEnabled());
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

    private static long threadsRunningTheTransformation() {
        return Thread.getAllStackTraces().values().stream()
                .filter(stack -> Arrays.stream(stack)
                        .anyMatch(frame -> frame.getClassName().equals(LongTaskTransformation.class.getName())))
                .count();
    }

    private static void flushEventQueue() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
        });
    }

    /**
     * The items of the combo box, as read on the event thread where the panel updates it.
     */
    private static List<String> listed(JComboBox<?> box) {
        List<String> items = new ArrayList<String>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                for (int i = 0; i < box.getItemCount(); i++) {
                    items.add(String.valueOf(box.getItemAt(i)));
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return items;
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
