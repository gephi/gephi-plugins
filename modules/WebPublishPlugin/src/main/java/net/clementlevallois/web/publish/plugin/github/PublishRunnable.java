/*
 * author: Clément Levallois
 */
package net.clementlevallois.web.publish.plugin.github;

import net.clementlevallois.web.publish.plugin.exceptions.NoOpenProjectException;
import net.clementlevallois.web.publish.plugin.exceptions.EmptyGraphException;
import net.clementlevallois.web.publish.plugin.exceptions.FileAboveMaxGithubSizeException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.UUID;
import net.clementlevallois.web.publish.plugin.controller.WebPublishExporterUI;
import static net.clementlevallois.web.publish.plugin.controller.GlobalConfigParams.*;
import net.clementlevallois.web.publish.plugin.exceptions.PublishToGistException;
import net.clementlevallois.web.publish.plugin.model.GitHubModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.plugin.ExporterGEXF;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class PublishRunnable implements LongTask, Runnable {

    private static final ResourceBundle bundle = NbBundle.getBundle(WebPublishExporterUI.class);

    private ProgressTicket progressTicket;
    private final GitHubModel gitHubModel;

    private String retinaUrl;
    private String gistUrl;

    public PublishRunnable(GitHubModel gitHubModel) {
        this.gitHubModel = gitHubModel;
    }

    @Override
    public void run() {
        Progress.start(progressTicket);
        try {
            Progress.setDisplayName(progressTicket, bundle.getString("general.message.exporting_gexf"));
            String gexfAsString = getGexfAsString();

            Progress.setDisplayName(progressTicket, bundle.getString("general.message.publishing_to_gist"));
            String fileName = createNameForGistFile();

            JsonObject jsonResponse = postGexfToGist(gexfAsString, fileName, gitHubModel.getAccessToken());
            retinaUrl = getRetinaLink(jsonResponse, fileName);
            gistUrl = getGistHtmlLink(jsonResponse);

            Progress.finish(progressTicket, bundle.getString("general.message.export_finished"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getRetinaUrl() {
        return retinaUrl;
    }

    public String getGistUrl() {
        return gistUrl;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    private static String createNameForGistFile() {
        return "network-" + UUID.randomUUID().toString().substring(0, 12) + ".gexf";
    }

    private static String getRetinaLink(JsonObject metadataOnGist, String fileName) {
        JsonObject metadataOnFiles = metadataOnGist.get("files").getAsJsonObject();
        JsonObject metadataOnOneFile = metadataOnFiles.get(fileName).getAsJsonObject();
        String rawUrl = metadataOnOneFile.get("raw_url").getAsString();
        String retinaFullURl = RETINA_BASE_URL + "?url=" + rawUrl;

        return retinaFullURl;

    }

    private static String getGistHtmlLink(JsonObject metadataOnGist) {
        String htmlUrl = metadataOnGist.get("html_url").getAsString();
        return htmlUrl;

    }

    protected static String getGexfAsString() throws IOException {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentProject() == null) {
            throw new NoOpenProjectException();
        }

        Workspace workspace = pc.getCurrentWorkspace();

        return getGexfAsStringFromWorkspace(workspace);
    }

    protected static String getGexfAsStringFromWorkspace(Workspace workspace) throws IOException {

        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        Graph graph = graphModel.getGraph();
        if (graph.getNodeCount() == 0) {
            throw new EmptyGraphException();
        }

        ExportController ec = Lookup.getDefault().lookup(ExportController.class);

        ExporterGEXF exporterGexf = (ExporterGEXF) ec.getExporter("gexf");
        exporterGexf.setWorkspace(workspace);
        exporterGexf.setExportDynamic(false);

        StringWriter stringWriter = new StringWriter();
        ec.exportWriter(stringWriter, exporterGexf);
        stringWriter.close();
        String gexfToSendAsString = stringWriter.toString();

        int sizeGexfInBytes = gexfToSendAsString.getBytes(StandardCharsets.UTF_8).length;

        // Files pushed to a github shouldn't be larger than 100Mb
        // There is still a doubt: is this limit the same for uploads to a gist?
        if (sizeGexfInBytes >= (1_048_576 * MAX_MB_FOR_GITHUB_PUSH)) {
            throw new FileAboveMaxGithubSizeException();
        }

        return gexfToSendAsString;
    }

    public static Boolean isGraphVeryLarge() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = pc.getCurrentWorkspace();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        Graph graph = graphModel.getGraph();

        return graph.getNodeCount() > WARNING_NODE_COUNT || graph.getEdgeCount() > WARNING_EDGE_COUNT;
    }

    public static JsonObject connectToGithub() throws IOException, InterruptedException {
        JsonObject responseConnectGithubAsJO;
        HttpClient client = HttpClient.newHttpClient();
        String inputParams = "client_id=" + GEPHI_APP_CLIENT_ID + "&scope=gist";
        String url = "https://github.com/login/device/code";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(inputParams))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonElement responseAsJsonElement = JsonParser.parseString(response.body());
        responseConnectGithubAsJO = responseAsJsonElement.getAsJsonObject();

        return responseConnectGithubAsJO;
    }

    protected static JsonObject postGexfToGist(String gexfFile, String fileName, String access_token) throws PublishToGistException {
        JsonObject bodyPostGexfToGist = new JsonObject();
        bodyPostGexfToGist.addProperty("description", bundle.getString("general.message.file_sent_from_gephi"));
        bodyPostGexfToGist.addProperty("public", "true");
        JsonObject fileItself = new JsonObject();
        JsonObject contentsFile = new JsonObject();
        contentsFile.addProperty("content", gexfFile);
        fileItself.add(fileName, contentsFile);
        bodyPostGexfToGist.add("files", fileItself);
        String bodyToString = bodyPostGexfToGist.toString();

        HttpClient client = HttpClient.newHttpClient();
        String url = "https://api.github.com/gists";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + access_token)
                .POST(HttpRequest.BodyPublishers.ofString(bodyToString))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() > 299) {
                throw new PublishToGistException(String.valueOf(response.statusCode()), response.body());
            }
        } catch (IOException | InterruptedException ex) {
            throw new PublishToGistException(ex);
        }
        JsonElement responseAsJsonElement = JsonParser.parseString(response.body());
        JsonObject metadataOnGist = responseAsJsonElement.getAsJsonObject();
        return metadataOnGist;
    }
}
