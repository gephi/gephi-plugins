package fr.inria.edelweiss.sparql.corese;

import fr.inria.edelweiss.sparql.corese.CoreseDriverParametersPanel;
import abbot.finder.ComponentNotFoundException;
import abbot.finder.MultipleComponentsFoundException;
import abbot.finder.matchers.ClassMatcher;
import abbot.finder.matchers.NameMatcher;
import abbot.tester.ComponentTester;
import abbot.tester.JFileChooserTester;
import abbot.tester.JListTester;
import abbot.tester.ListTester;
import fr.inria.edelweiss.sparql.corese.CoreseDriverParameters;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import junit.extensions.abbot.ComponentTestFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author edemairy
 */
public class CoreseDriverParametersPanelTest extends ComponentTestFixture {

    private Logger LOGGER = Logger.getLogger(CoreseDriverParametersPanelTest.class.getName());
    private ComponentTester tester;

    @Before
    protected void setUp() throws Exception {
        tester = new ComponentTester();
    }

    @After
    protected void tearDown() throws Exception {
        tester = null;
    }
    private String clickType;

    @Test
    public void testSimpleRun() throws ComponentNotFoundException, MultipleComponentsFoundException, IOException {
        CoreseDriverParametersPanel.main(new String[0]);
    }

    @Test
    public void testAddResource() throws ComponentNotFoundException, MultipleComponentsFoundException, IOException {
        final int FILES_NUMBER = 2;
        ArrayList<File> files = whenCreateTempFiles(FILES_NUMBER, "test", ".rdf");
        CoreseDriverParametersPanel panel = whenInitPanel();


        whenAddFiles(panel, files);

        JList listResources = (JList) getFinder().find(panel, new NameMatcher("listResources"));
        assertEquals(FILES_NUMBER, listResources.getModel().getSize());
    }

    @Test
    public void testRemoveResourceWithSingleClick() throws ComponentNotFoundException, MultipleComponentsFoundException, IOException {
        final int FILES_NUMBER = 5;
        final int[] FILES_INDICES_TO_REMOVE = new int[]{1, 3, 4};
        ArrayList<File> files = whenCreateTempFiles(FILES_NUMBER, "test", ".rdf");
        CoreseDriverParametersPanel panel = whenInitPanel();
        whenAddFiles(panel, files);
        whenRemoveFilesWithSingleClick(panel, FILES_INDICES_TO_REMOVE);

        JList listResources = (JList) getFinder().find(panel, new NameMatcher("listResources"));
        LOGGER.log(Level.INFO, "before test: listResources size = {0}", listResources.getModel().getSize());
        assertEquals(FILES_NUMBER - FILES_INDICES_TO_REMOVE.length, listResources.getModel().getSize());
    }


    private ArrayList<File> whenCreateTempFiles(final int nbFiles, final String prefix, final String suffix) throws IOException {
        ArrayList<File> result = new ArrayList<File>();
        for (int i = 0; i < nbFiles; ++i) {
            File newFile = File.createTempFile(prefix, suffix);
            newFile.deleteOnExit();
            result.add(newFile);
        }
        return result;
    }

    private CoreseDriverParametersPanel whenInitPanel() throws ComponentNotFoundException, MultipleComponentsFoundException {
        CoreseDriverParametersPanel.main(new String[0]);
        CoreseDriverParametersPanel panel = (CoreseDriverParametersPanel) getFinder().find(new ClassMatcher(CoreseDriverParametersPanel.class));
        return panel;
    }

    private void whenAddFiles(CoreseDriverParametersPanel panel, ArrayList<File> files) throws ComponentNotFoundException, MultipleComponentsFoundException {
        JButton addButton = (JButton) getFinder().find(panel, new NameMatcher("+"));
        for (int i = 0; i < files.size(); ++i) {
            tester.actionClick(addButton);
            tester.waitForIdle();
            String fileName = files.get(i).getAbsolutePath();
            JFileChooserTester fileChooserTester = new JFileChooserTester();
            JFileChooser fileChooser = (JFileChooser) getFinder().find(new ClassMatcher(JFileChooser.class));
            fileChooserTester.actionSetFilename(fileChooser, fileName);
            fileChooserTester.actionApprove(fileChooser);
        }
    }

    private void whenRemoveFilesWithSingleClick(CoreseDriverParametersPanel panel, int[] indices) throws ComponentNotFoundException, MultipleComponentsFoundException {
        Arrays.sort(indices);
        JButton removeButton = (JButton) getFinder().find(panel, new NameMatcher("-"));
        JList listResources = (JList) getFinder().find(panel, new NameMatcher("listResources"));
        JListTester listTester = new JListTester();
        for (int i = indices.length - 1; i >= 0; --i) {
            listTester.actionSelectIndex(listResources, indices[i]);
            listTester.waitForIdle();
            tester.actionClick(removeButton);
            tester.waitForIdle();
            LOGGER.log(Level.INFO, "listResources size = " + listResources.getModel().getSize());
        }
    }
}
