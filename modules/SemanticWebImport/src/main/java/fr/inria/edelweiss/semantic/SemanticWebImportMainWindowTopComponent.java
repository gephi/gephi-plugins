/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic;

import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.inria.edelweiss.semantic.configurationmanager.ConfigurationManager;
import fr.inria.edelweiss.semantic.importer.SemanticWebImportParser;
import fr.inria.edelweiss.semantic.utils.FilesUtils;
import fr.inria.edelweiss.semantic.utils.StopWatch;
import fr.inria.edelweiss.sparql.DriverParametersPanel;
import fr.inria.edelweiss.sparql.SparqlDriver;
import fr.inria.edelweiss.sparql.SparqlDriverFactory;
import fr.inria.edelweiss.sparql.SparqlDriverParameters;
import fr.inria.edelweiss.sparql.SparqlRequester;
import fr.inria.edelweiss.sparql.corese.CoreseDriver;

import static fr.inria.edelweiss.semantic.PluginProperties.*;
//import org.gephi.scripting.api.ScriptingController;
//import org.python.util.PythonInterpreter;

/**
 * Top component responsible for the main window of SemanticWebImport.
 */
@ConvertAsProperties(dtd = "-//fr.inria.edelweiss.semantic//SemanticWebImportMainWindow//EN", autostore = false)
@TopComponent.Description(preferredID = "SemanticWebImportMainWindowTopComponent",
        iconBase = "fr/inria/edelweiss/resources/semantic_web_icon_16.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@ActionID(category = "Window", id = "fr.inria.edelweiss.SemanticWebImportMainWindowTopComponent")
@ActionReference(path = "Menu/Window")
@TopComponent.OpenActionRegistration(displayName = "#CTL_SemanticWebImportMainWindowAction",
        preferredID = "SemanticWebImportMainWindowTopComponent")
@ServiceProvider(service = SparqlRequester.class)
@TopComponent.Registration(mode = "editor", openAtStartup = true, roles = {"overview"})
public final class SemanticWebImportMainWindowTopComponent extends TopComponent implements SparqlRequester, LongTaskListener {

    public final static String DEFAULT_CONFIGURATION = "Humans";
    private static final Logger logger = Logger.getLogger(SemanticWebImportMainWindowTopComponent.class.getName());
    private static final String PREFERRED_ID = "SemanticWebImportMainWindowTopComponent";
    private final static String ICON_PATH = "fr/inria/edelweiss/resources/semantic_web_icon_16.png";
    private static final String DEFAULT_SPARQL_REQUEST = "construct {?x ?r ?y}\nwhere {?x ?r ?y}\nlimit 100";
    static File lastLoadConfigurationDirectory = new File(getHome());
    private static SemanticWebImportMainWindowTopComponent instance;
    private static SemanticWebImportParser rdfParser;
    private final ConfigurationManager configurationManager = new ConfigurationManager(this);
    private boolean sparqlDriverSelectorInitialized;
    private final ArrayList<SparqlDriver> driverHandlers = new ArrayList<SparqlDriver>();
    private SparqlDriver<SparqlDriverParameters> sparqlDriver;
    private StopWatch watch;
    private String lastPythonUsedDirectory;
    // This method is called whenever the user or program changes the selected item.
    // Note: The new item may be the same as the previous item.
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoLayout;
    private javax.swing.JPanel configurationPanel;
    private javax.swing.JComboBox configurationSelector;
    private javax.swing.JPanel confiugrationManagementPanel;
    private javax.swing.JPanel driverSelectorPanel;
    private javax.swing.JLabel fynLabel;
    private javax.swing.JSpinner fynSpinner;
    private javax.swing.JTabbedPane graphBuilderTab;
    private javax.swing.JCheckBox ignoreBlankNode;

    /*
     *
     */
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton loadConfiguration;
    private javax.swing.JPanel logPanel;
    private javax.swing.JTextArea logWindow;
    private javax.swing.JPanel parametersPanel;
    private javax.swing.JPanel pythonPanel;
    private javax.swing.JTextField pythonPostProcessingFileName;
    private javax.swing.JLabel pythonPostProcessingLabel;
    private javax.swing.JTextField pythonPreProcessingFileName;
    private javax.swing.JLabel pythonPreProcessingLabel;
    private javax.swing.JCheckBox resetWorkspace;
    private javax.swing.JButton saveConfiguration;
    private javax.swing.JTextField saveConfigurationNameTextField;
    private javax.swing.JButton setConfiguration;
    private javax.swing.JButton setPythonPostProcessingFileName;
    private javax.swing.JButton setPythonPreProcessingFileName;
    private javax.swing.JLabel sparqlDriverLabel;
    private javax.swing.JComboBox sparqlDriverSelector;
    private javax.swing.JPanel sparqlEditorPanel;
    private javax.swing.JButton sparqlQueryResultButton;
    private javax.swing.JTextField sparqlQueryResultFileName;
    private javax.swing.JLabel sparqlQueryResultLabel;
    private fr.inria.corese.gui.query.SparqlQueryEditor sparqlRequestEditor;
    private javax.swing.JLabel sparqlRequestLabel;
    private javax.swing.JButton start;
    public SemanticWebImportMainWindowTopComponent() throws IOException {
        initComponents();
        setName(
                NbBundle.getMessage(
                        SemanticWebImportMainWindowTopComponent.class,
                        "CTL_SemanticWebImportMainWindowTopComponent"));
        setToolTipText(
                NbBundle.getMessage(
                        SemanticWebImportMainWindowTopComponent.class,
                        "HINT_SemanticWebImportMainWindowTopComponent"));

        initConfigurations();
        Logger.getLogger("").addHandler(new LogWindowHandler());

        configurationManager.setCurrentProperties(DEFAULT_CONFIGURATION);

        sparqlRequestEditor.setQueryText(DEFAULT_SPARQL_REQUEST);

        setIcon(ImageUtilities.icon2Image(loadIcon()));

        fillSparqlDriverSelector();
        refreshActiveSparqlDriver();

    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings
     * files only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized SemanticWebImportMainWindowTopComponent getDefault() throws IOException {
        if (instance == null) {
            instance = new SemanticWebImportMainWindowTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the SemanticWebImportMainWindowTopComponent instance. Never
     * call {@link #getDefault} directly!
     */
    public static synchronized SemanticWebImportMainWindowTopComponent findInstance() throws IOException {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);

        if (win == null) {
            Logger.getLogger(SemanticWebImportMainWindowTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof SemanticWebImportMainWindowTopComponent) {
            return (SemanticWebImportMainWindowTopComponent) win;

        }
        Logger.getLogger(SemanticWebImportMainWindowTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                        + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    public static Icon loadIcon() {
        Image image = ImageUtilities.loadImage(ICON_PATH, true);
        return ImageUtilities.image2Icon(image);
    }

    public static SparqlRequester getSparqlRequester() {
        return SemanticWebImportMainWindowTopComponent.instance;
    }

    private static String getHome() {
        String result = System.getProperty("user.home");
        if (result == null) {
            logger.severe("user.home was not defined.");
            result = ".";
        }
        return result;
    }

    private void initConfigurations() throws IOException {
        resetConfigurations();
        addConfigurations(configurationManager.loadResourceConfigurations("/fr/inria/edelweiss/semantic/default_configuration/"));
        addConfigurations(loadFileConfigurations(getHome() + "/.semanticwebimport/"));
        refreshConfigurationSelector();
    }

    private void resetConfigurations() {
        resetConfigurationSelector();
        configurationManager.setListProperties(new HashMap<String, Properties>());
    }

    private void resetConfigurationSelector() {
        configurationSelector.removeAllItems();
    }

    private void refreshConfigurationSelector() {
        for (String nameConfiguration : configurationManager.getListProperties().keySet()) {
            configurationSelector.addItem(nameConfiguration);
        }
    }

    public void setConfigurationAction(String configurationName) {
        configurationManager.setCurrentProperties(configurationName);
        Properties currentProperties = configurationManager.getCurrentProperties();
        if ((configurationName != null) && !configurationName.isEmpty()) {
            logger.log(Level.INFO, "Loading configuration \"{0}\"", configurationName);
            // Obtain the driver name and set it.
            String driverName = currentProperties.getProperty(ConfigurationManager.DRIVER_NAME);
            int driverIndex = findDriver(driverName);
            if (driverIndex != -1) {
                logger.log(Level.INFO, "Selecting the driver \"{0}\"", driverName);
                sparqlDriverSelector.setSelectedIndex(driverIndex);
            } else {
                logger.log(Level.INFO, "No SPARQL driver found with name: \"{0}\"", driverName);
            }
            sparqlDriver.getParameters().readProperties(currentProperties);
            refreshActiveSparqlDriver();

            // Set the sparql request.
            String sparqlRequest = configurationManager.getCurrentProperties().getProperty(ConfigurationManager.SPARQL_REQUEST);
            sparqlRequestEditor.setQueryText(sparqlRequest);
        } else {
            logger.info("No configuration to load.");
        }
    }

    private Set<Properties> loadFileConfigurations(final String directoryPath) throws IOException {
        Set<Properties> result = new HashSet<Properties>();
        File fileProperties = new File(directoryPath);
        if (fileProperties.exists()) {
            if (fileProperties.isFile()) {
                Properties newProperties = new Properties();
                newProperties.loadFromXML(new FileInputStream(fileProperties));
                result.add(newProperties);
            } else if (fileProperties.isDirectory()) {
                File[] files = fileProperties.listFiles();
                for (File file : files) {
                    try {
                        Properties newProperties = new Properties();
                        FileInputStream is = new FileInputStream(file);
                        newProperties.loadFromXML(is);
                        is.close();
                        result.add(newProperties);
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "An exception occured when trying to parse the file {0}: {1}", new Object[]{file.getName(), ex.getMessage()});
                    }
                }
            }
        }
        return result;
    }

    private void fillSparqlDriverSelector() {
        Collection<? extends SparqlDriver> sparqlDriverList = Lookup.getDefault().lookupAll(SparqlDriver.class);
        sparqlDriverSelector.removeAllItems();
        for (SparqlDriver driver : sparqlDriverList) {
            addDriver(driver);
        }
        sparqlDriverSelectorInitialized = true;
    }

    private void addDriver(final SparqlDriver driver) {
        sparqlDriverSelector.addItem(driver.getDisplayName());
        driverHandlers.add(driver);
    }

    private int findDriver(final String className) {
        for (int i = 0; i < driverHandlers.size(); ++i) {
            if (driverHandlers.get(i).getClass().getName().equals(className)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        graphBuilderTab = new javax.swing.JTabbedPane();
        driverSelectorPanel = new javax.swing.JPanel();
        sparqlDriverSelector = new javax.swing.JComboBox();
        sparqlDriverLabel = new javax.swing.JLabel();
        parametersPanel = new javax.swing.JPanel();
        sparqlEditorPanel = new javax.swing.JPanel();
        sparqlRequestLabel = new javax.swing.JLabel();
        sparqlRequestEditor = new fr.inria.corese.gui.query.SparqlQueryEditor();
        logPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        logWindow = new javax.swing.JTextArea();
        configurationPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        pythonPanel = new javax.swing.JPanel();
        pythonPostProcessingFileName = new javax.swing.JTextField();
        pythonPostProcessingLabel = new javax.swing.JLabel();
        setPythonPostProcessingFileName = new javax.swing.JButton();
        pythonPreProcessingLabel = new javax.swing.JLabel();
        pythonPreProcessingFileName = new javax.swing.JTextField();
        setPythonPreProcessingFileName = new javax.swing.JButton();
        confiugrationManagementPanel = new javax.swing.JPanel();
        loadConfiguration = new javax.swing.JButton();
        saveConfiguration = new javax.swing.JButton();
        configurationSelector = new javax.swing.JComboBox();
        saveConfigurationNameTextField = new javax.swing.JTextField();
        setConfiguration = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        ignoreBlankNode = new javax.swing.JCheckBox();
        sparqlQueryResultButton = new javax.swing.JButton();
        fynSpinner = new javax.swing.JSpinner();
        sparqlQueryResultLabel = new javax.swing.JLabel();
        autoLayout = new javax.swing.JCheckBox();
        sparqlQueryResultFileName = new javax.swing.JTextField();
        fynLabel = new javax.swing.JLabel();
        resetWorkspace = new javax.swing.JCheckBox();
        start = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(640, 480));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(600, 460));

        graphBuilderTab.setBackground(new java.awt.Color(240, 242, 240));
        graphBuilderTab.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        graphBuilderTab.setToolTipText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.graphBuilderTab.toolTipText")); // NOI18N
        graphBuilderTab.setPreferredSize(new java.awt.Dimension(600, 450));

        driverSelectorPanel.setPreferredSize(new java.awt.Dimension(600, 400));

        sparqlDriverSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        sparqlDriverSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sparqlDriverSelectorActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(sparqlDriverLabel, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.sparqlDriverLabel.text")); // NOI18N

        parametersPanel.setBackground(new java.awt.Color(237, 235, 236));
        parametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        parametersPanel.setForeground(new java.awt.Color(255, 0, 0));
        parametersPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout driverSelectorPanelLayout = new javax.swing.GroupLayout(driverSelectorPanel);
        driverSelectorPanel.setLayout(driverSelectorPanelLayout);
        driverSelectorPanelLayout.setHorizontalGroup(
                driverSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(driverSelectorPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(driverSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(parametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                                        .addGroup(driverSelectorPanelLayout.createSequentialGroup()
                                                .addComponent(sparqlDriverSelector, 0, 499, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(sparqlDriverLabel)))
                                .addContainerGap())
        );
        driverSelectorPanelLayout.setVerticalGroup(
                driverSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(driverSelectorPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(driverSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(sparqlDriverLabel)
                                        .addComponent(sparqlDriverSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(parametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                                .addContainerGap())
        );

        graphBuilderTab.addTab(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.driverSelectorPanel.TabConstraints.tabTitle"), driverSelectorPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sparqlRequestLabel, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.sparqlRequestLabel.text")); // NOI18N

        javax.swing.GroupLayout sparqlEditorPanelLayout = new javax.swing.GroupLayout(sparqlEditorPanel);
        sparqlEditorPanel.setLayout(sparqlEditorPanelLayout);
        sparqlEditorPanelLayout.setHorizontalGroup(
                sparqlEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(sparqlEditorPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(sparqlEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(sparqlRequestEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                                        .addComponent(sparqlRequestLabel))
                                .addContainerGap())
        );
        sparqlEditorPanelLayout.setVerticalGroup(
                sparqlEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(sparqlEditorPanelLayout.createSequentialGroup()
                                .addComponent(sparqlRequestLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sparqlRequestEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                                .addContainerGap())
        );

        graphBuilderTab.addTab(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.sparqlEditorPanel.TabConstraints.tabTitle"), sparqlEditorPanel); // NOI18N

        logWindow.setColumns(20);
        logWindow.setRows(5);
        jScrollPane2.setViewportView(logWindow);

        javax.swing.GroupLayout logPanelLayout = new javax.swing.GroupLayout(logPanel);
        logPanel.setLayout(logPanelLayout);
        logPanelLayout.setHorizontalGroup(
                logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 641, Short.MAX_VALUE)
                        .addGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(logPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                                        .addContainerGap()))
        );
        logPanelLayout.setVerticalGroup(
                logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 425, Short.MAX_VALUE)
                        .addGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(logPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                                        .addContainerGap()))
        );

        graphBuilderTab.addTab(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.logPanel.TabConstraints.tabTitle"), logPanel); // NOI18N

        configurationPanel.setPreferredSize(new java.awt.Dimension(640, 200));

        pythonPostProcessingFileName.setText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.pythonPostProcessingFileName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(pythonPostProcessingLabel, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.pythonPostProcessingLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(setPythonPostProcessingFileName, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.setPythonPostProcessingFileName.text")); // NOI18N
        setPythonPostProcessingFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setPythonPostProcessingFileNameActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(pythonPreProcessingLabel, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.pythonPreProcessingLabel.text")); // NOI18N

        pythonPreProcessingFileName.setText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.pythonPreProcessingFileName.text")); // NOI18N
        pythonPreProcessingFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pythonPreProcessingFileNameActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(setPythonPreProcessingFileName, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.setPythonPreProcessingFileName.text")); // NOI18N
        setPythonPreProcessingFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setPythonPreProcessingFileNameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pythonPanelLayout = new javax.swing.GroupLayout(pythonPanel);
        pythonPanel.setLayout(pythonPanelLayout);
        pythonPanelLayout.setHorizontalGroup(
                pythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pythonPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(pythonPreProcessingLabel)
                                        .addComponent(pythonPostProcessingLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(pythonPreProcessingFileName)
                                        .addComponent(pythonPostProcessingFileName))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(setPythonPreProcessingFileName, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(setPythonPostProcessingFileName, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addContainerGap())
        );

        pythonPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, setPythonPostProcessingFileName, setPythonPreProcessingFileName);

        pythonPanelLayout.setVerticalGroup(
                pythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pythonPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(pythonPreProcessingLabel)
                                        .addComponent(pythonPreProcessingFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(setPythonPreProcessingFileName))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(pythonPostProcessingLabel)
                                        .addComponent(pythonPostProcessingFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(setPythonPostProcessingFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pythonPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, pythonPostProcessingFileName, pythonPostProcessingLabel, pythonPreProcessingFileName, pythonPreProcessingLabel, setPythonPostProcessingFileName, setPythonPreProcessingFileName);

        org.openide.awt.Mnemonics.setLocalizedText(loadConfiguration, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.loadConfiguration.text")); // NOI18N
        loadConfiguration.setToolTipText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.loadConfiguration.toolTipText")); // NOI18N
        loadConfiguration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadConfigurationActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveConfiguration, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.saveConfiguration.text")); // NOI18N
        saveConfiguration.setToolTipText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.saveConfiguration.toolTipText")); // NOI18N
        saveConfiguration.setActionCommand(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.saveConfiguration.actionCommand")); // NOI18N
        saveConfiguration.setPreferredSize(new java.awt.Dimension(0, 0));
        saveConfiguration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveConfigurationActionPerformed(evt);
            }
        });

        configurationSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        configurationSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationSelectorActionPerformed(evt);
            }
        });

        saveConfigurationNameTextField.setText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.saveConfigurationNameTextField.text")); // NOI18N
        saveConfigurationNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveConfigurationNameTextFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(setConfiguration, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.setConfiguration.text")); // NOI18N
        setConfiguration.setToolTipText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.setConfiguration.toolTipText")); // NOI18N
        setConfiguration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setConfigurationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout confiugrationManagementPanelLayout = new javax.swing.GroupLayout(confiugrationManagementPanel);
        confiugrationManagementPanel.setLayout(confiugrationManagementPanelLayout);
        confiugrationManagementPanelLayout.setHorizontalGroup(
                confiugrationManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(confiugrationManagementPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(confiugrationManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(saveConfigurationNameTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(confiugrationManagementPanelLayout.createSequentialGroup()
                                                .addComponent(setConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(configurationSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(confiugrationManagementPanelLayout.createSequentialGroup()
                                                .addComponent(loadConfiguration)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(saveConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        confiugrationManagementPanelLayout.setVerticalGroup(
                confiugrationManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(confiugrationManagementPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(confiugrationManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(setConfiguration)
                                        .addComponent(configurationSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(saveConfigurationNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(confiugrationManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(saveConfiguration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(loadConfiguration))
                                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(ignoreBlankNode, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.ignoreBlankNode.text")); // NOI18N
        ignoreBlankNode.setToolTipText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.ignoreBlankNode.toolTipText")); // NOI18N
        ignoreBlankNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreBlankNodeActionPerformed(evt);
            }
        });

        sparqlQueryResultButton.setLabel(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.label")); // NOI18N
        sparqlQueryResultButton.setName(""); // NOI18N
        sparqlQueryResultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sparqlQueryResultButtonActionPerformed(evt);
            }
        });

        fynSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        org.openide.awt.Mnemonics.setLocalizedText(sparqlQueryResultLabel, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.sparqlQueryResultLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(autoLayout, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.autoLayout.text")); // NOI18N
        autoLayout.setPreferredSize(new java.awt.Dimension(0, 0));
        autoLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoLayoutActionPerformed(evt);
            }
        });

        sparqlQueryResultFileName.setText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.sparqlQueryResultFileName.text")); // NOI18N
        sparqlQueryResultFileName.setToolTipText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.sparqlQueryResultFileName.toolTipText")); // NOI18N
        sparqlQueryResultFileName.setPreferredSize(new java.awt.Dimension(4, 10));
        sparqlQueryResultFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sparqlQueryResultFileNameActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(fynLabel, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.fynLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resetWorkspace, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.resetWorkspace.text")); // NOI18N
        resetWorkspace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetWorkspaceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(sparqlQueryResultLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(sparqlQueryResultFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(sparqlQueryResultButton, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(resetWorkspace)
                                                        .addComponent(autoLayout, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(ignoreBlankNode)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(fynSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(fynLabel)))))
                                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(sparqlQueryResultLabel)
                                        .addComponent(sparqlQueryResultFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sparqlQueryResultButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(resetWorkspace)
                                        .addComponent(ignoreBlankNode))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(autoLayout, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fynSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fynLabel))
                                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, sparqlQueryResultButton, sparqlQueryResultFileName, sparqlQueryResultLabel);

        javax.swing.GroupLayout configurationPanelLayout = new javax.swing.GroupLayout(configurationPanel);
        configurationPanel.setLayout(configurationPanelLayout);
        configurationPanelLayout.setHorizontalGroup(
                configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(configurationPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(pythonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jSeparator1)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jSeparator2)
                                        .addComponent(confiugrationManagementPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        configurationPanelLayout.setVerticalGroup(
                configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(configurationPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(4, 4, 4)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(pythonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(confiugrationManagementPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(64, Short.MAX_VALUE))
        );

        graphBuilderTab.addTab(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.configurationPanel.TabConstraints.tabTitle"), configurationPanel); // NOI18N

        jScrollPane1.setViewportView(graphBuilderTab);
        graphBuilderTab.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        start.setBackground(new java.awt.Color(255, 0, 51));
        org.openide.awt.Mnemonics.setLocalizedText(start, org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.start.text")); // NOI18N
        start.setToolTipText(org.openide.util.NbBundle.getMessage(SemanticWebImportMainWindowTopComponent.class, "SemanticWebImportMainWindowTopComponent.start.toolTipText")); // NOI18N
        start.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startCreateGraphsAction(evt);
            }
        });
        add(start, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void sparqlDriverSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sparqlDriverSelectorActionPerformed

        if (sparqlDriverSelectorInitialized) {
            if ("comboBoxChanged".equals(evt.getActionCommand())) {
                refreshActiveSparqlDriver();
            }
        }
    }//GEN-LAST:event_sparqlDriverSelectorActionPerformed

    private void sparqlQueryResultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sparqlQueryResultButtonActionPerformed
    }//GEN-LAST:event_sparqlQueryResultButtonActionPerformed

    private void sparqlQueryResultFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sparqlQueryResultFileNameActionPerformed
    }//GEN-LAST:event_sparqlQueryResultFileNameActionPerformed

    private void saveConfigurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveConfigurationActionPerformed
        configurationManager.saveConfigurationActionPerformed(sparqlDriver, saveConfigurationNameTextField.getText(), sparqlRequestEditor.getQueryText(), pythonPreProcessingFileName.getText(), pythonPostProcessingFileName.getText());
    }//GEN-LAST:event_saveConfigurationActionPerformed

    private void setConfigurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setConfigurationActionPerformed
        String configurationName = (String) configurationSelector.getSelectedItem();
        setConfigurationAction(configurationName);
    }//GEN-LAST:event_setConfigurationActionPerformed

    private void configurationSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationSelectorActionPerformed
    }//GEN-LAST:event_configurationSelectorActionPerformed

    private void autoLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoLayoutActionPerformed
    }//GEN-LAST:event_autoLayoutActionPerformed

    private void resetWorkspaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetWorkspaceActionPerformed
    }//GEN-LAST:event_resetWorkspaceActionPerformed

    private void ignoreBlankNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreBlankNodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ignoreBlankNodeActionPerformed

    private void loadConfigurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadConfigurationActionPerformed
        Properties properties = configurationManager.loadConfigurationActionPerformed(evt);
        addConfiguration(properties);
        resetConfigurationSelector();
        refreshConfigurationSelector();
        String configurationName = properties.getProperty(ConfigurationManager.CONFIGURATION_NAME);
        pythonPreProcessingFileName.setText(properties.getProperty(ConfigurationManager.PYTHON_PRE));
        pythonPostProcessingFileName.setText(properties.getProperty(ConfigurationManager.PYTHON_POST));
        setConfigurationAction(configurationName);
        saveConfigurationNameTextField.setText(configurationName);
    }//GEN-LAST:event_loadConfigurationActionPerformed

    private void saveConfigurationNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveConfigurationNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveConfigurationNameTextFieldActionPerformed

    private void setPythonPostProcessingFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setPythonPostProcessingFileNameActionPerformed
        setPythonProcessingFileName("File to use for python post-processsing", pythonPostProcessingFileName);
    }//GEN-LAST:event_setPythonPostProcessingFileNameActionPerformed

    private void pythonPreProcessingFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pythonPreProcessingFileNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pythonPreProcessingFileNameActionPerformed

    private void setPythonPreProcessingFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setPythonPreProcessingFileNameActionPerformed
        setPythonProcessingFileName("File to use for python pre-processsing", pythonPreProcessingFileName);
    }//GEN-LAST:event_setPythonPreProcessingFileNameActionPerformed

    private void setPythonProcessingFileName(final String message, JTextField field) {
        File newFile = FilesUtils.selectFile(JFileChooser.OPEN_DIALOG, message, "Load", lastPythonUsedDirectory, this, ".py", "Python script");
        if (newFile == null) {
            return;
        }
        field.setText(newFile.getAbsolutePath());
        lastPythonUsedDirectory = newFile.getParent();
    }

    private void startCreateGraphsAction(java.awt.event.ActionEvent evt) {
        startCreateGraphs();
    }

    /*
     * This is launching the asynchronous build of the graph.
     * Since the task is asynchronous. Processing done when it is finished must be placed in terminate().
     * \sa waitCreateGraphs to wait for the completion of the graph.
     * \sa terminate to
     */
    public void startCreateGraphs() {
        applyPythonScript(pythonPreProcessingFileName.getText());

        logger.info("Entering startCreateGraphs");
        final SemanticWebImportParser.RequestParameters requestParameters = new SemanticWebImportParser.RequestParameters(sparqlRequestEditor.getQueryText());
        rdfParser = new SemanticWebImportParser(requestParameters);

        logger.info("Starting the RDF importer for Gephi");
        watch = new StopWatch();
        refreshActiveSparqlDriver();
        refreshCurrentConfiguration();
        rdfParser.populateRDFGraph(getSparqlDriver(), configurationManager.getCurrentProperties(), this);
        //
    }

    @Override
    public void taskFinished(LongTask lt) {
        applyPythonScript(pythonPostProcessingFileName.getText());

        logger.log(Level.INFO, "Finished startCreateGraphs. Time elapsed = {0} milliseconds", watch.elapsedMillis());
    }
    // End of variables declaration//GEN-END:variables

    /**
     * @param fileName
     * @TODO restore the python script functionality
     */
    private void applyPythonScript(String fileName) {
        // Apply python script after import.
//        try {
//            ScriptingController scripting = Lookup.getDefault().lookup(ScriptingController.class);
//            PythonInterpreter interpreter = scripting.getPythonInterpreter();
//            InputStream pythonStream = FilesUtils.getResourceOrFile(fileName);
//            interpreter.execfile(pythonStream);
//        } catch (IllegalArgumentException ex) {
//
//        }
    }

    private void refreshActiveSparqlDriver() {
        String newDriverName = getCurrentSelectedDriverName();

        // The panel and the driver needs to be built iff (i) there is none; (ii) the driver selected is different from the current one.
        if (sparqlDriver == null || (!newDriverName.equals(sparqlDriver.getClass().getName()))) {
            try {
                // Build the new driver.
                sparqlDriver = SparqlDriverFactory.getDriver(newDriverName);
                SparqlDriverParameters parameters = sparqlDriver.getParameters();

                parametersPanel.removeAll();
                // Fill the new parameters panel.
                String panelClassName = sparqlDriver.getParameters().getPanelClassName();
                ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                Constructor<?> constructor = loader.loadClass(panelClassName).getDeclaredConstructor(sparqlDriver.getParameters().getClass());
                DriverParametersPanel<SparqlDriverParameters> newPanel = (DriverParametersPanel<SparqlDriverParameters>) constructor.newInstance(sparqlDriver.getParameters());
                if (newPanel != null) {
                    newPanel.setParameters(parameters);
                    parametersPanel.add(newPanel);
                    parametersPanel.setVisible(true);
                    parametersPanel.validate();
                }

                parameters.addObserver(newPanel);
//                parameters.addObserver(sparqlDriver);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InstantiationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private String getCurrentSelectedDriverName() {
        int driverIndex = sparqlDriverSelector.getSelectedIndex();
        SparqlDriver newDriver = driverHandlers.get(driverIndex);
        return newDriver.getClass().getName();
    }

    /**
     * Set the values of the properties by reading the GUI state elements.
     * \todo Should be in the configuration manager, with a listener
     * mechanism to update the properties.
     */
    private void refreshCurrentConfiguration() {
        configurationManager.getCurrentProperties().setProperty(String.valueOf(IGNORE_BLANK_PROPERTIES), Boolean.toString(ignoreBlankNode.isSelected()));
        configurationManager.getCurrentProperties().setProperty(String.valueOf(RESET_WORKSPACE), Boolean.toString(resetWorkspace.isSelected()));
        configurationManager.getCurrentProperties().setProperty(String.valueOf(POST_PROCESSING), Boolean.toString(autoLayout.isSelected()));
        configurationManager.getCurrentProperties().setProperty(String.valueOf(SAVE_SPARQL_RESULT), sparqlQueryResultFileName.getText());
        configurationManager.getCurrentProperties().setProperty(String.valueOf(FYN_LEVEL), Integer.toString((Integer) fynSpinner.getValue()));
    }

    /*
     * Block until the thread building the graph terminates.
     */
    public void waitCreateGraphs() throws InterruptedException {
        rdfParser.waitEndpopulateRDFGraph();
    }

    public JPanel getDriverSelectorPanel() {
        return driverSelectorPanel;
    }

    public String getSparqlRequest() {
        return sparqlRequestEditor.getQueryText();
    }

    private SparqlDriver getSparqlDriver() {
        return this.sparqlDriver;
    }

    private void addConfigurations(Set<Properties> configurations) {
        for (Properties p : configurations) {
            addConfiguration(p);
        }
    }

    private void addConfiguration(Properties configuration) {
        configurationManager.getListProperties().put(configuration.getProperty(ConfigurationManager.CONFIGURATION_NAME), configuration);
    }

    @Override
    public String sparqlQuery(String request) {
        CoreseDriver driver = new CoreseDriver();
        try {
            driver.getParameters().addResource(createTempLastRdfFile().getAbsolutePath());
            driver.init();
            return driver.sparqlQuery(request);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return "";
    }

    /**
     * Apply a SPARQL request on the RDF used to display the graph. For
     * example, a "select ?x ?y ?z where { ?x ?y ?z} limit 10" query will
     * return a String[3][] array of size at most 10. The first column will
     * contain the results for ?x, the second for ?y, etc.
     *
     * @param request
     * @return The URI of the bindings, each column.
     */
    @Override
    public String[][] selectOnGraph(final String request) {
        CoreseDriver driver = new CoreseDriver();
        try {
            driver.getParameters().addResource(createTempLastRdfFile().getAbsolutePath());
            driver.init();
            return driver.selectOnGraph(request);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new String[0][0];
    }

    private File createTempLastRdfFile() throws IOException {
        File tempFile = File.createTempFile("tempLastRdf", ".rdf");
        tempFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.append(rdfParser.getLastRdfResult());
        }
        return tempFile;
    }

    ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        p.setProperty(IGNORE_BLANK_PROPERTIES.getValue(),
                Boolean.toString(ignoreBlankNode.isSelected()));
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        ignoreBlankNode.setSelected(
                Boolean.getBoolean(p.getProperty(IGNORE_BLANK_PROPERTIES.getValue())));
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public SparqlRequester getDriver() {
        return sparqlDriver;
    }

    public void addTab(final String title, JPanel panel) {
        int tabIndex = findTab(title);
        if (tabIndex == -1) {
            tabIndex = graphBuilderTab.getTabCount();
        } else {
            graphBuilderTab.removeTabAt(tabIndex);
        }
        graphBuilderTab.insertTab(title, SemanticWebImportMainWindowTopComponent.loadIcon(), panel, title, tabIndex);
    }

    private int findTab(String title) {
        for (int numTab = 0; numTab < graphBuilderTab.getTabCount(); ++numTab) {
            if (graphBuilderTab.getTitleAt(numTab).equals(title)) {
                return numTab;
            }
        }
        return -1;
    }

    class LogWindowHandler extends Handler {

        public LogWindowHandler() {
            setFormatter(new SimpleFormatter());
        }

        @Override
        public void publish(LogRecord record) {
            logWindow.append(getFormatter().format(record).replace("\n", " -- ") + '\n');
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
