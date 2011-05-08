/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.neo4j;

import org.gephi.neo4j.plugin.api.ClassNotFulfillRequirementsException;
import org.gephi.neo4j.plugin.api.FileSystemClassLoader;
import org.gephi.neo4j.plugin.api.MutableNeo4jDelegateNodeDebugger;
import org.gephi.neo4j.plugin.api.Neo4jDelegateNodeDebugger;
import org.gephi.neo4j.plugin.api.Neo4jExporter;
import org.gephi.neo4j.plugin.api.Neo4jImporter;
import org.gephi.neo4j.plugin.api.Neo4jVisualDebugger;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.desktop.neo4j.ui.DebugFileChooserComponent;
import org.gephi.desktop.neo4j.ui.DebugPanel;
import org.gephi.desktop.neo4j.ui.ExportOptionsPanel;
import org.gephi.desktop.neo4j.ui.TraversalFilterPanel;
import org.gephi.desktop.neo4j.ui.TraversalImportPanel;
import org.gephi.desktop.neo4j.ui.util.Neo4jUtils;
import org.gephi.desktop.neo4j.ui.util.ObsoleneVersionOfNeo4jStoreException;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.visualization.VizController;
import org.neo4j.graphdb.GraphDatabaseService;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Martin Škurla
 */
public class Neo4jMenuAction extends CallableSystemAction {

    private static final String IMPORT_LAST_PATH = "Neo4jMenuAction_Import_Last_Path";
    private static final String EXPORT_LAST_PATH = "Neo4jMenuAction_Export_Last_Path";

    private static boolean previousEdgeHasUniColor;

    private JMenuItem exportMenuItem;
    private JMenuItem debugMenuItem;
    private JMenu menu;

    public Neo4jMenuAction() {
        initializeMenu();

        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
                exportMenuItem.setEnabled(true);
                debugMenuItem.setEnabled(true);
            }

            @Override
            public void disable() {
                exportMenuItem.setEnabled(false);
                debugMenuItem.setEnabled(false);
            }

            @Override
            public void select(Workspace workspace) {
            }

            @Override
            public void unselect(Workspace workspace) {
            }

            @Override
            public void close(Workspace workspace) {
            }
        });
    }

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return "importNeo4jDB";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return menu;
    }

    private void initializeMenu() {
        menu = new JMenu(NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_MenuLabel"));

        String fullImportMenuLabel = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_FullImportMenuLabel");
        JMenuItem fullImport = new JMenuItem(new FullImportMenuAction(fullImportMenuLabel));
        fullImport.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/neo4j/resources/import.png", false));

        String traversalImportMenuLabel = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_TraversalImportMenuLabel");
        JMenuItem traversalImport = new JMenuItem(new TraversalImportMenuAction(traversalImportMenuLabel));

        String exportMenuLabel = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_ExportMenuLabel");
        exportMenuItem = new JMenuItem(new ExportMenuAction(exportMenuLabel));
        exportMenuItem.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/neo4j/resources/export.png", false));

        String debugMenuLabel = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_DebugMenuLabel");
        debugMenuItem = new JMenuItem(new DebugMenuAction(debugMenuLabel));


        menu.add(fullImport);
        menu.add(traversalImport);
        menu.addSeparator();

        exportMenuItem.setEnabled(false);
        menu.add(exportMenuItem);
        menu.addSeparator();

        debugMenuItem.setEnabled(false);
        menu.add(debugMenuItem);
    }

    private static void showWarningMessage() {
        NotifyDescriptor notifyDescriptor =
                new NotifyDescriptor.Message("Selected file is not valid Neo4j debug file.",
                JOptionPane.WARNING_MESSAGE);

        DialogDisplayer.getDefault().notify(notifyDescriptor);
    }

    private static void initProject() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        if (projectController.getCurrentProject() == null) {
            ProjectControllerUI projectControllerUI = Lookup.getDefault().lookup(ProjectControllerUI.class);
            projectControllerUI.newProject();
        }
    }

    private static class FullImportMenuAction extends AbstractAction {
        private TraversalFilterPanel filterPanel;


        FullImportMenuAction(String menuLabel) {
            super(menuLabel);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int dialogResult = chooseDirectory(fileChooser);

            if (dialogResult == JFileChooser.CANCEL_OPTION) {
                return;
            }

            final File neo4jDirectory = fileChooser.getSelectedFile();
            if (neo4jDirectory != null && neo4jDirectory.exists()) {
                NbPreferences.forModule(Neo4jMenuAction.class).put(IMPORT_LAST_PATH, neo4jDirectory.getParentFile().getAbsolutePath());
            }

            GraphDatabaseService tempGraphDB;
            try {
                tempGraphDB = Neo4jUtils.localDatabase(neo4jDirectory);
            }
            // older incompatible version of Neo4j was used during creating database
            catch (ObsoleneVersionOfNeo4jStoreException obsoleneVersionException) {
                String errorMessage = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_InvalidStorageDatabaseVersion");

                NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(errorMessage, JOptionPane.WARNING_MESSAGE);

                DialogDisplayer.getDefault().notify(notifyDescriptor);

                return;
            }

            final GraphDatabaseService graphDB = tempGraphDB;

            Object result = showFilterDialog();

            if (result == NotifyDescriptor.OK_OPTION) {
                final Neo4jImporter neo4jImporter = Lookup.getDefault().lookup(Neo4jImporter.class);

                LongTaskExecutor executor = new LongTaskExecutor(true);
                executor.execute((LongTask) neo4jImporter, new Runnable() {

                    @Override
                    public void run() {
                        initProject();
                        neo4jImporter.importDatabase(graphDB,
                                filterPanel.getFilterDescriptions(),
                                filterPanel.isRestrictModeEnabled(),
                                filterPanel.isMatchCaseEnabled());
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_ImportTaskFinished", neo4jDirectory));
                    }
                });
            }
        }

        private int chooseDirectory(JFileChooser fileChooser) {
            String lastDirectory = NbPreferences.forModule(Neo4jMenuAction.class).get(IMPORT_LAST_PATH, "");
            fileChooser.setCurrentDirectory(new File(lastDirectory));

            String localImportDialogTitle = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_ImportDialogTitle");
            fileChooser.setDialogTitle(localImportDialogTitle);

            Neo4jCustomDirectoryProvider.setEnabled(true);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int dialogResult = fileChooser.showOpenDialog(null);

            Neo4jCustomDirectoryProvider.setEnabled(false);

            return dialogResult;
        }

        private Object showFilterDialog() {
            String traversalDialogTitle = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_TraversalDialogTitle");
            filterPanel = new TraversalFilterPanel();

            ValidationPanel validationPanel = filterPanel.createValidationPanel();
            final DialogDescriptor dd = new DialogDescriptor(validationPanel, traversalDialogTitle);

            validationPanel.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                }
            });

            return DialogDisplayer.getDefault().notify(dd);
        }
    }

    private static class TraversalImportMenuAction extends AbstractAction {
        private TraversalImportPanel traversalPanel;


        TraversalImportMenuAction(String menuLabel) {
            super(menuLabel);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int dialogResult = chooseDirectory(fileChooser);

            if (dialogResult == JFileChooser.CANCEL_OPTION) {
                return;
            }

            final File neo4jDirectory = fileChooser.getSelectedFile();
            if (neo4jDirectory != null && neo4jDirectory.exists()) {
                NbPreferences.forModule(Neo4jMenuAction.class).put(IMPORT_LAST_PATH, neo4jDirectory.getParentFile().getAbsolutePath());
            }

            GraphDatabaseService tempGraphDB;
            try {
                tempGraphDB = Neo4jUtils.localDatabase(neo4jDirectory);
            }
            // older incompatible version of Neo4j was used during creating database
            catch (ObsoleneVersionOfNeo4jStoreException obsoleneVersionException) {
                String errorMessage = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_InvalidStorageDatabaseVersion");

                NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(errorMessage, JOptionPane.WARNING_MESSAGE);

                DialogDisplayer.getDefault().notify(notifyDescriptor);

                return;
            }

            final GraphDatabaseService graphDB = tempGraphDB;

            Object result = showTraversalDialog(graphDB);
            if (result == NotifyDescriptor.OK_OPTION) {
                final Neo4jImporter neo4jImporter = Lookup.getDefault().lookup(Neo4jImporter.class);

                LongTaskExecutor executor = new LongTaskExecutor(true);
                executor.execute((LongTask) neo4jImporter, new Runnable() {

                    @Override
                    public void run() {
                        initProject();
                        neo4jImporter.importDatabase(graphDB,
                                traversalPanel.getStartNodeId(),
                                traversalPanel.getOrder(),
                                traversalPanel.getMaxDepth(),
                                traversalPanel.getRelationshipDescriptions(),
                                traversalPanel.getFilterDescriptions(),
                                traversalPanel.isRestrictModeEnabled(),
                                traversalPanel.isMatchCaseEnabled());
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_ImportTaskFinished", neo4jDirectory));
                    }
                });
            }
        }

        private int chooseDirectory(JFileChooser fileChooser) {
            String lastDirectory = NbPreferences.forModule(Neo4jMenuAction.class).get(IMPORT_LAST_PATH, "");
            fileChooser.setCurrentDirectory(new File(lastDirectory));

            String importDialogTitle = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_ImportDialogTitle");
            fileChooser.setDialogTitle(importDialogTitle);

            Neo4jCustomDirectoryProvider.setEnabled(true);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int dialogResult = fileChooser.showOpenDialog(null);

            Neo4jCustomDirectoryProvider.setEnabled(false);

            return dialogResult;
        }

        private Object showTraversalDialog(GraphDatabaseService graphDB) {
            String traversalDialogTitle = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_TraversalDialogTitle");
            traversalPanel = new TraversalImportPanel(graphDB);

            ValidationPanel validationPanel = traversalPanel.createValidationPanel();
            final DialogDescriptor dd = new DialogDescriptor(validationPanel, traversalDialogTitle);

            validationPanel.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                }
            });

            return DialogDisplayer.getDefault().notify(dd);
        }
    }

    private static class ExportMenuAction extends AbstractAction {
        private ExportOptionsPanel exportOptionsPanel;


        ExportMenuAction(String menuLabel) {
            super(menuLabel);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            Object result = showExportOptionsDialog();
            if (result == NotifyDescriptor.OK_OPTION) {
                JFileChooser fileChooser = new JFileChooser();
                int dialogResult = chooseDirectory(fileChooser);

                if (dialogResult == JFileChooser.APPROVE_OPTION) {
                    final File neo4jDirectory = fileChooser.getSelectedFile();
                    if (neo4jDirectory != null && neo4jDirectory.exists()) {
                        NbPreferences.forModule(Neo4jMenuAction.class).put(EXPORT_LAST_PATH, neo4jDirectory.getParentFile().getAbsolutePath());
                    }
                    final Neo4jExporter neo4jExporter = Lookup.getDefault().lookup(Neo4jExporter.class);

                    LongTaskExecutor executor = new LongTaskExecutor(true);
                    executor.execute((LongTask) neo4jExporter, new Runnable() {

                        @Override
                        public void run() {
                            GraphDatabaseService tempGraphDB;
                            try {
                                tempGraphDB = Neo4jUtils.localDatabase(neo4jDirectory);
                            }
                            // older incompatible version of Neo4j was used during creating database
                            catch (ObsoleneVersionOfNeo4jStoreException obsoleneVersionException) {
                                String errorMessage = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_InvalidStorageDatabaseVersion");

                                NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(errorMessage, JOptionPane.WARNING_MESSAGE);

                                DialogDisplayer.getDefault().notify(notifyDescriptor);

                                return;
                            }

                            final GraphDatabaseService graphDB = tempGraphDB;

                            neo4jExporter.exportDatabase(graphDB,
                                    exportOptionsPanel.getFromColumn(),
                                    exportOptionsPanel.getDefaultValue(),
                                    exportOptionsPanel.getExportEdgeColumnNames(),
                                    exportOptionsPanel.getExportNodeColumnNames());

                            graphDB.shutdown();
                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_ExportTaskFinished", neo4jDirectory));
                        }
                    });
                }
            }
        }

        private Object showExportOptionsDialog() {
            String exportOptionsDialogTitle = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_ExportOptionsDialogTitle");
            exportOptionsPanel = new ExportOptionsPanel();

            ValidationPanel validationPanel = exportOptionsPanel.createValidationPanel();
            final DialogDescriptor dd = new DialogDescriptor(validationPanel, exportOptionsDialogTitle);

            validationPanel.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                }
            });

            return DialogDisplayer.getDefault().notify(dd);
        }

        private int chooseDirectory(JFileChooser fileChooser) {
            String lastDirectory = NbPreferences.forModule(Neo4jMenuAction.class).get(EXPORT_LAST_PATH, "");
            fileChooser.setCurrentDirectory(new File(lastDirectory));

            String localExportDialogTitle = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_ExportDialogTitle");
            fileChooser.setDialogTitle(localExportDialogTitle);

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            return fileChooser.showOpenDialog(null);
        }
    }

    private static class DebugMenuAction extends AbstractAction {

        DebugMenuAction(String menuLabel) {
            super(menuLabel);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int dialogResult = chooseDebugFile(fileChooser);

            if (dialogResult == JFileChooser.CANCEL_OPTION) {
                return;
            }

            File neo4jDebugFile = fileChooser.getSelectedFile();
            FileSystemClassLoader classLoader = Lookup.getDefault().lookup(FileSystemClassLoader.class);

            Class<?> loadedClass = null;

            try {
                loadedClass = classLoader.loadClass(neo4jDebugFile,
                        enabled,
                        Neo4jDelegateNodeDebugger.class);
            } catch (ClassNotFoundException cnfe) {
                showWarningMessage();
                return;
            } catch (NoClassDefFoundError ncdfe) {
                showWarningMessage();
                return;
            } catch (ClassNotFulfillRequirementsException cnfre) {
                showWarningMessage();
                return;
            } catch (IllegalArgumentException iae) {
                showWarningMessage();
                return;
            }

            Neo4jDelegateNodeDebugger neo4jDebugger = null;
            try {
                neo4jDebugger = (Neo4jDelegateNodeDebugger) loadedClass.newInstance();
            } catch (IllegalAccessException iae) {
                throw new AssertionError();
            } catch (InstantiationException ie) {
                throw new AssertionError();
            }

            // see GraphRestoration JavaDoc
            GraphRestoration graphRestoration = new GraphRestoration();

            previousEdgeHasUniColor =
                    VizController.getInstance().getVizModel().isEdgeHasUniColor();
            VizController.getInstance().getVizModel().setEdgeHasUniColor(true);

            String debugDialogTitle = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_DebugOptionsDialogTitle");
            DialogDescriptor dialog = new DialogDescriptor(new DebugPanel(new MutableNeo4jDelegateNodeDebugger(neo4jDebugger)),
                    debugDialogTitle,
                    false,
                    graphRestoration);
            dialog.addPropertyChangeListener(graphRestoration);

            DialogDisplayer.getDefault().notify(dialog);
        }

        private int chooseDebugFile(JFileChooser fileChooser) {
            String chooseDebugFileDialogTitle =
                    NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_ChooseDebugFileDialogTitle");
            fileChooser.setDialogTitle(chooseDebugFileDialogTitle);

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new Neo4jDebugFileFilter());
            fileChooser.setFileView(new Neo4jDebugFileView());
            fileChooser.setAccessory(new DebugFileChooserComponent(fileChooser));

            return fileChooser.showOpenDialog(null);
        }
    }

    /*
     * This class is needed because of NetBeans platform. Action listener is used for
     * cases when user clicks OK/Cancel button. PropertyChangeListener is used for cases
     * when user clicks Cancel/Close button.
     *
     * We want to restore graph in all 3 cases, so we need to implement and register both
     * interfaces.
     */
    private static class GraphRestoration implements PropertyChangeListener, ActionListener {

        private final Neo4jVisualDebugger neo4jVisualDebugger =
                Lookup.getDefault().lookup(Neo4jVisualDebugger.class);

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            neo4jVisualDebugger.restore();
            VizController.getInstance().getVizModel().setEdgeHasUniColor(previousEdgeHasUniColor);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            neo4jVisualDebugger.restore();
            VizController.getInstance().getVizModel().setEdgeHasUniColor(previousEdgeHasUniColor);
        }
    }
}
