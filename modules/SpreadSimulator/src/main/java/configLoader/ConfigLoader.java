package configLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    public static final String folderSimulationTmp = getProperty("folder.simulation.tmp");
    public static final String folderSimulationTmpFilename = getProperty("folder.simulation.tmp.filename");
    public static final String componentNameSimulationComponent = getProperty("component.name.simulationComponent");
    public static final String modelBuilderToolInfoStatusCreate = getProperty("modelBuilderTool.infoStatus.create");
    public static final String modelBuilderToolInfoStatusLink = getProperty("modelBuilderTool.infoStatus.link");
    public static final String colNameModelBuilderNodeState = getProperty("colName.modelBuilder.nodeState");
    public static final String colNameModelBuilderProbability = getProperty("colName.modelBuilder.probability");
    public static final String colNameModelBuilderTransitionType = getProperty("colName.modelBuilder.transitionType");
    public static final String buttonLabelRunSimulation = getProperty("button.label.runSimulation");
    public static final String buttonLabelRunSimulationSeries = getProperty("button.label.runSimulationSeries");
    public static final String colNameModelBuilderProvocativeNeighbours = getProperty("colName.modelBuilder.provocativeNeighbours");
    public static final String colNameNewNodeState = getProperty("colName.newNodeState");
    public static final String colNameRootState = getProperty("colName.rootState");
    public static final String colNameNodeState = getProperty("colName.nodeState");
    public static final String messageErrorUnknowTransitionType = getProperty("message.error.unknowTransitionType");
    public static final String colNameNodeRole = getProperty("colName.nodeRole");
    public static final String colNameModelBuilderDescription = getProperty("colName.modelBuilder.description");
    public static final String modelBuilderLabelTransition = getProperty("modelBuilder.label.transition");
    public static final String folderSimulationBuilderModels = getProperty("folder.simulationBuilder.models");
    public static final String modelBuilderLabelState = getProperty("modelBuilder.label.state");
    public static final String folderReports = getProperty("folder.reports");
    public static final String folderSimulationBuilderSimulations = getProperty("folder.simulationBuilder.simulations");

    static String getProperty(String name) {
        Properties prop = new Properties();
        try (FileInputStream config = new FileInputStream("simulation.properties")) {
            prop.load(config);
            return prop.getProperty(name);
        } catch (IOException e) {
            File currentDir = new File(System.getProperty("user.dir"));
            File targetFile = new File(currentDir.getParentFile().getParentFile(), "simulationWindows.properties");
            try (FileInputStream config = new FileInputStream(targetFile.getAbsolutePath())) {
                prop.load(config);
                return prop.getProperty(name);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }


}
