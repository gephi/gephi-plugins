package ec.loxa.sna.gephi.websiteexport;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import ec.loxa.sna.gephi.websiteexport.util.statistics.GraphStatistic;
import ec.loxa.sna.gephi.websiteexport.util.statistics.Metrics;
import ec.loxa.sna.gephi.websiteexport.util.statistics.StatisticsJSON;
import ec.loxa.sna.gephi.websiteexport.utilities.Util;
import java.io.*;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.plugin.ExporterCSV;
import org.gephi.io.exporter.plugin.ExporterGEXF;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.project.api.*;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author jorgaf
 */
public class WebSiteExporter implements Exporter, LongTask {

    public static final String PATH_JAR = "/ec/loxa/sna/gephi/websiteexport/files/";
    private Workspace currentWorkSpace;
    private ProgressTicket progress;
    private boolean cancel = false;
    private File path;
    private String projectName;
    private Workspace[] allWorkspaces;
    //ExporterGEXF properties
    private boolean exportAttributes;
    private boolean exportColors;
    private boolean exportDynamic;
    private boolean exportPosition;
    private boolean exportSize;
    private final StatisticsJSON statistics = new StatisticsJSON();
    private File projectPath;
    private String[] selectedWorkspaces;
    private String description;
    private String keywords;
    private String title;
    private String graphType;
    private boolean append;
    private int numAppend;
    private String theme;

    @Override
    public boolean execute() {
        Progress.start(progress);
        Progress.setDisplayName(progress, getMessage("message_Export_Web_Site"));
        try {
            export();
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
        Progress.finish(progress);

        //Si el proceso se canceló se borra la carpeta raíz
        if (cancel) {
            try {
                if (!append) {
                    delete(projectPath);
                }
            } catch (IOException ex) {
            }
        }

        return !cancel;
    }

    private void export() throws Exception {
        Project project = Lookup.getDefault().lookup(ProjectController.class).
                getCurrentProject();

        ProjectInformation projectInformation
                = project.getLookup().lookup(ProjectInformation.class);

        setProjectName(projectInformation.getName());
        if (!isAppend()) {

            File root = new File(getPath(), getProjectName());
            //TODO: Debe ejecutarse si y solo si no se agrega un proyecto. Verificar la estructura del index.html
            delete(root);
            root.mkdir();
            projectPath = root;

            //To get de Project Properties
            ProjectMetaData pi = project.getLookup().lookup(ProjectMetaData.class);
            description = pi.getDescription();
            keywords = pi.getKeywords();
            title = pi.getTitle();
        } else {
            projectPath = getPath();
        }

        //To get workspaces in the project
        WorkspaceProvider workspaceProvider
                = project.getLookup().lookup(WorkspaceProvider.class);

        setAllWorkspaces(workspaceProvider.getWorkspaces());

        //To get workspace information
        WorkspaceInformation workspaceInfortion;
        String workspaceName;

        GraphModel graphModel;
        //AttributeModel attModel;

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        boolean hasPartitionImage;

        if (append) {
            List<GraphStatistic> grph;
            File statisticPath = new File(projectPath.getAbsolutePath() + File.separator + "estadisticas.json");
            JsonReader reder = new JsonReader(new FileReader(statisticPath.getAbsolutePath()));
            Gson gson = new Gson();
            try {
                StatisticsJSON resp = gson.fromJson(reder, StatisticsJSON.class);
                grph = resp.getGraphs();
                setNumAppend(grph.size());
                for (int i = 0; i < grph.size(); i++) {
                    statistics.addGraph(grph.get(i));
                }
            } catch (Exception e) {
                System.out.println("Fail: " + e.getMessage());
            }
        }

        for (int i = 0; i < allWorkspaces.length && !cancel; i++) {
            setWorkspace(allWorkspaces[i]);
            workspaceInfortion
                    = getWorkspace().getLookup().lookup(WorkspaceInformation.class);
            workspaceName = workspaceInfortion.getName().replace(" ", "");

            //Verificar si se ha seleccionado
            if (isSelected(workspaceName)) {
                try {
                    pc.openWorkspace(getWorkspace());
                } catch (Exception e) {
                    System.out.println("MESSAGE: " + e.getMessage());
                }

                Progress.setDisplayName(progress, getMessage("message_Export")
                        + " " + workspaceName);

                if (append) {
                    String pathAux = projectPath + File.separator + workspaceName;
                    File ws = new File(pathAux);
                    while (ws.exists()) {
                        workspaceName = workspaceName + "_1";
                        pathAux = projectPath + File.separator + workspaceName;
                        ws = new File(pathAux);
                    }
                }
                createDirectoryToWorkspace(projectPath, workspaceName);
                saveGEXF(workspaceName, getWorkspace());

                //Se verifica si no se canceló el proceso
                if (!cancel) {
//                    PartitionModel partitionModel
//                            = getWorkspace().getLookup().lookup(PartitionModel.class);

                    graphModel = getWorkspace().getLookup().lookup(GraphModel.class);
                    //attModel = getWorkspace().getLookup().lookup(AttributeModel.class);

                    try {
                        if (graphModel.isDirected()) {
                            graphType = "Directed";
                        } else {
                            graphType = "Undirected";
                        }
                    } catch (Exception e) {
                        System.out.println("ERROR: " + e.getMessage());
                    }

                    saveGraphCSV(workspaceName, getWorkspace());
                    saveGraphPDF(workspaceName, getWorkspace());
//                    hasPartitionImage = generatePartitionImage(partitionModel, workspaceName);
//                    getStatistics(workspaceName, graphModel.getGraphVisible(), hasPartitionImage);
                    getStatistics(workspaceName, graphModel.getGraphVisible(), false);
                } else {
                    Progress.setDisplayName(progress, getMessage("message_Cancel"));
                }
            }
        }
        //Si el proceso no fue cancelado se genera la información
        if (!cancel) {
            saveStatistics();
            if (!isAppend()) {
                copyWebSiteFiles();
            }
            builIndexPage();
            if (!append) {
                builInfoPage();
                builAboutPage();
            }
            delete(new File(projectPath.getAbsolutePath() + File.separator + "FilesWebSite.zip"));
            delete(new File(projectPath.getAbsolutePath() + File.separator + "__MACOSX"));
        }
    }

    private void getStatistics(String wsName, Graph currentGraph, boolean hasPartitionImage) throws IOException {

        GraphStatistic graphData = new GraphStatistic();
        Metrics metricsData;

        graphData.setName(wsName);
        graphData.setTitle("Title to " + wsName);
        graphData.setEdges(String.valueOf(currentGraph.getEdgeCount()));
        graphData.setNodes(String.valueOf(currentGraph.getNodeCount()));
        graphData.setDescription(description);
        graphData.setType(graphType);

        metricsData = new Metrics();
        metricsData.setName("Metric 1");
        metricsData.setValue("0,00");
        metricsData.setDescription("Description for metric 1");
        graphData.addMetric(metricsData);

        metricsData = new Metrics();
        metricsData.setName("Metric 2");
        metricsData.setValue("0,00");
        metricsData.setDescription("Description for metric 2");
        graphData.addMetric(metricsData);

        metricsData = new Metrics();
        metricsData.setName("Metric 3");
        metricsData.setValue("0,00");
        metricsData.setDescription("Description for metric 3");
        graphData.addMetric(metricsData);

        metricsData = new Metrics();
        metricsData.setName("Metric 4");
        metricsData.setValue("0,00");
        metricsData.setDescription("Description for metric 4");
        graphData.addMetric(metricsData);

        metricsData = new Metrics();
        metricsData.setName("Metric 5");
        metricsData.setValue("0,00");
        metricsData.setDescription("Description for metric 5");
        graphData.addMetric(metricsData);

        metricsData = new Metrics();
        metricsData.setName("Metric 6");
        metricsData.setValue("0,00");
        metricsData.setDescription("Description for metric 6");
        graphData.addMetric(metricsData);

        metricsData = new Metrics();
        metricsData.setName("Metric 7");
        metricsData.setValue("0,00");
        metricsData.setDescription("Description for metric 7");
        graphData.addMetric(metricsData);

        metricsData = new Metrics();
        metricsData.setName("Metric 8");
        metricsData.setValue("0,00");
        metricsData.setDescription("Description for metric 8");
        graphData.addMetric(metricsData);

        graphData.setGraphfile(wsName + "/" + wsName + ".csv");
        graphData.setPdffile(wsName + "/" + wsName + ".pdf");
        graphData.setBrowsegraph(wsName + "/" + wsName + ".gexf");

        if (hasPartitionImage) {
            graphData.setImgColorDescription(wsName + "/" + "imgDescriptor.png");
        }

        statistics.addGraph(graphData);
    }

    private void copyWebSiteFiles() throws Exception {
        Util util = new Util();
        util.setDirectoryToExtract(projectPath);        

        util.copyFromJar(PATH_JAR, "FilesWebSite.zip");
        util.unZip(projectPath.getAbsolutePath() + File.separator + "FilesWebSite.zip");
    }

    private void builIndexPage() throws IOException {
        File index = new File(projectPath.getAbsolutePath() + File.separator + "index.html");
        StringBuilder options = new StringBuilder();
        Document doc = Jsoup.parse(index, "UTF-8");
        List<GraphStatistic> gStatistics = statistics.getGraphs();
        int i = 0;
        Element comboGrafos = doc.select("#grafos").first();

        Element valueTheme = doc.select("#valTheme").first();
        valueTheme.empty();
        String val = "<input id='theme' type='text' value='" + theme + "' />";
        valueTheme.append(val);

        Element desc = doc.select("meta[name=keywords]").first();
        String words = desc.attr("content");
        if (keywords != null && keywords.length() > 0) {
            words += ", " + keywords;
        }
        desc.attr("content", words);

        String ttl = doc.title();
        if (title != null && title.length() > 0) {
            doc.title(ttl + " " + title);
        }

        if (!append) {
            //comboGrafos.empty();
            //}
            options.append("<optgroup label='").append(projectName).append("'>");
            for (GraphStatistic gs : gStatistics) {
                if (i == 0) {
                    Element body = doc.body();
                    body.attr("onload", "start('" + gs.getName() + "');");
                    options.append("<option value = '").append(gs.getName()).append("' selected='true'>").append(gs.getName()).append("</option>");
                } else {
                    options.append("<option value = '").append(gs.getName()).append("'>").append(gs.getName()).append("</option>");
                }
                i++;
            }
            options.append("</optgroup>");
        } else {
            Elements labels = doc.select("optgroup");
            String pName = "";
            boolean eProject = false;
            for (Element opt : labels) {
                pName = opt.attr("label");
                if (pName.equals(projectName)) {
                    eProject = true;
                    break;
                } else {
                    eProject = false;
                }
            }
            if (eProject) {
                Element labelProj = doc.select("optgroup[label=" + pName + "]").first();
                StringBuilder sbOpt = new StringBuilder();
                for (int j = numAppend; j < gStatistics.size(); j++) {
                    sbOpt.append("<option value = '").append(gStatistics.get(j).getName()).append("'>").append(gStatistics.get(j).getName()).append("</option>");
                }
                labelProj.append(sbOpt.toString());
            } else {
                options.append("<optgroup label='").append(projectName).append("'>");
                for (int j = numAppend; j < gStatistics.size(); j++) {
                    options.append("<option value = '").append(gStatistics.get(j).getName()).append("'>").append(gStatistics.get(j).getName()).append("</option>");
                }
            }
        }

        comboGrafos.append(options.toString());

        String pathFile = projectPath.getAbsolutePath() + File.separator + "index.html";
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(pathFile), "UTF-8");

        out.write(doc.toString());
        out.close();
    }

    private void builInfoPage() throws IOException {
        File info = new File(projectPath.getAbsolutePath() + File.separator + "info.html");
        StringBuilder desc = new StringBuilder();
        Document doc = Jsoup.parse(info, "UTF-8");
        Elements e = doc.select("#tabProject");

        String ttl = doc.title();
        doc.title(ttl + " " + title);

        Element parrafo = doc.select("#tabProject").first();
        if (description != null /*|| !description.isEmpty()*/) {
            e = doc.select("div#tabProject > h4").remove();
            e = doc.select("div#tabProject > p").remove();
            desc.append("<h4>").append(projectName).append("</h4>");
            desc.append("<p style=\"text-align: justify\">").append(description).append("</p>");
        }
        parrafo.append(desc.toString());

        String pathFile = projectPath.getAbsolutePath() + File.separator + "info.html";
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(pathFile), "UTF-8");

        out.write(doc.toString());
        out.close();
    }

    private void builAboutPage() throws IOException {
        File about = new File(projectPath.getAbsolutePath() + File.separator + "about.html");
        Document doc = Jsoup.parse(about, "UTF-8");

        String ttl = doc.title();
        doc.title(ttl + " " + title);

        String pathFile = projectPath.getAbsolutePath() + File.separator + "about.html";
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(pathFile), "UTF-8");

        out.write(doc.toString());
        out.close();
    }

    private void saveStatistics() throws Exception {
        String pathFile = projectPath.getAbsolutePath() + File.separator + "estadisticas.json";
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(pathFile), "UTF-8");
        out.write(statistics.toJSON());
        out.close();
    }

    private void saveGraphCSV(String workspaceName, Workspace currentWorkspace) throws Exception {
        String pathFile = projectPath.getAbsolutePath() + File.separator + workspaceName + File.separator + workspaceName + ".csv";
        Writer out = new OutputStreamWriter(new FileOutputStream(pathFile), "UTF-8");
        ExporterCSV exporterCSV = new ExporterCSV();
        exporterCSV.setWorkspace(currentWorkspace);
        exporterCSV.setWriter(out);
        exporterCSV.setExportVisible(true);

        exporterCSV.execute();

        out.flush();
        out.close();
    }

    private void saveGraphPDF(String workspaceName, Workspace currentWs) throws Exception {
        String pathFile = projectPath.getAbsolutePath() + File.separator + workspaceName + File.separator + workspaceName + ".pdf";
        File file = new File(pathFile);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        PDFExporter exportPDF = new PDFExporter();

        exportPDF.setMarginTop(5f);
        exportPDF.setMarginLeft(5f);
        exportPDF.setMarginRight(5f);
        exportPDF.setMarginBottom(5f);

        exportPDF.setWorkspace(currentWs);
        exportPDF.setOutputStream(out);

        exportPDF.execute();

        out.flush();
        out.close();
    }

    private void saveGEXF(String workspaceName, Workspace currentWs) throws Exception {
        String pathFile = projectPath.getAbsolutePath() + File.separator + workspaceName + File.separator + workspaceName + ".gexf";
        File file = new File(pathFile);
        ExporterGEXF gexfExporter = new ExporterGEXF();
        Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

        gexfExporter.setExportAttributes(isExportAttributes());
        gexfExporter.setExportColors(isExportColors());
        gexfExporter.setExportDynamic(isExportDynamic());
        gexfExporter.setExportPosition(isExportPosition());
        gexfExporter.setExportSize(isExportSize());

        gexfExporter.setExportVisible(true);

        gexfExporter.setWorkspace(currentWs);
        gexfExporter.setWriter(out);

        gexfExporter.execute();

        out.flush();
        out.close();

    }

    private void createDirectoryToWorkspace(File root, String workspaceName) throws IOException {
        //TODO: Verificar si el directorio existe. Si existe generar otro nombre
        File directory = new File(root, workspaceName);
        delete(directory);
        directory.mkdir();
    }

    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (f.exists() && !f.delete()) {
            throw new IOException(getMessage("exception_Failed_to_Delete") + f);
        }
    }

    private boolean isSelected(String wsName) {
        for (String name : getSelectedWorkspaces()) {
            if (wsName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

//    private boolean generatePartitionImage(PartitionModel pm, String wsName) throws IOException {
//        String[] text;
//        Color[] colors;
//        Partition partition = pm.getSelectedPartition();
//
//        if (partition != null) {
//            text = new String[partition.getParts().length];
//            colors = new Color[partition.getParts().length];
//            for (int i = 0; i < colors.length; i++) {
//                text[i] = partition.getParts()[i].getDisplayName();
//                colors[i] = partition.getParts()[i].getColor();
//            }
//
//            String max = getMaxText(text);
//
//            BufferedImage bInformation = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
//
//            Font font = new Font("Verdana", Font.BOLD, 12);
//            FontMetrics fMetrics = bInformation.getGraphics().getFontMetrics();
//            int fontWidht = fMetrics.stringWidth(max);
//            int fontHeight = fMetrics.getHeight();
//
//            int ancho = 3 + 17 + 2 + (int) (fontWidht * 1.2) + 3;
//            int alto = 3 + (text.length * (fontHeight + 3)) + 3;
//
//            BufferedImage bi = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
//
//            Graphics2D g2d = bi.createGraphics();
//            g2d.setFont(font);
//            g2d.setBackground(Color.WHITE);
//
//            int x = 3, y = 3;
//
//            for (int i = 0; i < colors.length; i++) {
//                g2d.setColor(colors[i]);
//                g2d.fillOval(x, y, 17, 17);
//                g2d.setColor(Color.BLACK);
//                g2d.drawString(text[i], (x + 19), y + 14);
//                y += fontHeight + 3;
//            }
//
//            File f = new File(projectPath.getAbsoluteFile() + File.separator
//                    + wsName + File.separator + "imgDescriptor.png");
//
//            ImageIO.write(bi, "PNG", f);
//            return true;
//        } else {
//            return false;
//        }
//    }

    private String getMaxText(String[] messages) {
        String max;

        max = messages[0];

        for (int i = 1; i < messages.length; i++) {
            if (messages[i].length() > max.length()) {
                max = messages[i];
            }
        }

        return max;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        currentWorkSpace = workspace;
    }

    @Override
    public Workspace getWorkspace() {
        return currentWorkSpace;
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName.replace(" ", "");
    }

    public Workspace[] getAllWorkspaces() {
        return allWorkspaces;
    }

    public void setAllWorkspaces(Workspace[] workspaces) {
        this.allWorkspaces = workspaces;
    }

    /**
     * @return the path
     */
    public File getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(File path) {
        this.path = path;
    }

    public String getMessage(String key) {
        return NbBundle.getMessage(WebSiteExporter.class, key);
    }

    /**
     * @return the selectedWorkspaces
     */
    public String[] getSelectedWorkspaces() {
        return selectedWorkspaces;
    }

    /**
     * @param selectedWorkspaces the selectedWorkspaces to set
     */
    public void setSelectedWorkspaces(String[] selectedWorkspaces) {
        this.selectedWorkspaces = selectedWorkspaces;
    }

    /**
     * @return the exportAttributes
     */
    public boolean isExportAttributes() {
        return exportAttributes;
    }

    /**
     * @param exportAttributes the exportAttributes to set
     */
    public void setExportAttributes(boolean exportAttributes) {
        this.exportAttributes = exportAttributes;
    }

    /**
     * @return the exportColors
     */
    public boolean isExportColors() {
        return exportColors;
    }

    /**
     * @param exportColors the exportColors to set
     */
    public void setExportColors(boolean exportColors) {
        this.exportColors = exportColors;
    }

    /**
     * @return the exportDynamic
     */
    public boolean isExportDynamic() {
        return exportDynamic;
    }

    /**
     * @param exportDynamic the exportDynamic to set
     */
    public void setExportDynamic(boolean exportDynamic) {
        this.exportDynamic = exportDynamic;
    }

    /**
     * @return the exportPosition
     */
    public boolean isExportPosition() {
        return exportPosition;
    }

    /**
     * @param exportPosition the exportPosition to set
     */
    public void setExportPosition(boolean exportPosition) {
        this.exportPosition = exportPosition;
    }

    /**
     * @return the exportSize
     */
    public boolean isExportSize() {
        return exportSize;
    }

    /**
     * @param exportSize the exportSize to set
     */
    public void setExportSize(boolean exportSize) {
        this.exportSize = exportSize;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public boolean isAppend() {
        return append;
    }

    public int getNumAppend() {
        return numAppend;
    }

    public void setNumAppend(int numAppend) {
        this.numAppend = numAppend;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
