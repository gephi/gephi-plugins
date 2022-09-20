/*
 * author: Clément Levallois
 */
package net.clementlevallois.web.publish.plugin.controller;

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
import static net.clementlevallois.web.publish.plugin.controller.GlobalConfigParams.*;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author LEVALLOIS
 */
public class PublishingActions {

    private static final ResourceBundle bundle = NbBundle.getBundle(GephiPluginDesktopLogic.class);

    private static final int BYTES_IN_A_MEGABYTE = 1_048_576;

    public static JsonObject getGexfAsString() {
        JsonObject jsonObject = new JsonObject();
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentProject() == null) {
            jsonObject.addProperty(ERROR_CODE_NO_OPEN_PROJECT, bundle.getString("general.message.error.no_open_project"));
            return jsonObject;
        }
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph graph = graphModel.getGraph();
        if (graph.getNodeCount() == 0) {
            jsonObject.addProperty(ERROR_CODE_EMPTY_NETWORK, bundle.getString("general.message.error.empty_network"));
            return jsonObject;
        }
        Workspace workspace = pc.getCurrentWorkspace();
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        Exporter exporterGexf = ec.getExporter("gexf");
        exporterGexf.setWorkspace(workspace);
        StringWriter stringWriter = new StringWriter();
        ec.exportWriter(stringWriter, (CharacterExporter) exporterGexf);
        String gexfToSendAsString = stringWriter.toString();

        int sizeGexfInBytes = gexfToSendAsString.getBytes(StandardCharsets.UTF_8).length; //

        // Files pushed to a github shouldn't be larger than 100Mb
        // There is still a doubt: is this limit the same for uploads to a gist?
        if (sizeGexfInBytes >= (BYTES_IN_A_MEGABYTE * MAX_MB_FOR_GITHUB_PUSH)) {
            jsonObject.addProperty(ERROR_CODE_GEXF_TOO_BIG, bundle.getString("general.message.error.network_too_big"));
            return jsonObject;
        }

        if (graph.getNodeCount() > 10_000 || graph.getEdgeCount() > 20_000) {
            jsonObject.addProperty(SUCCESS_BUT_WITH_WARNING, gexfToSendAsString);
        } else {
            jsonObject.addProperty(SUCCESS_CODE, gexfToSendAsString);
        }
        return jsonObject;
    }

    public static JsonObject connectToGithub() {
        JsonObject responseCOnnectGithubAsJO = null;
        try {
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
            responseCOnnectGithubAsJO = responseAsJsonElement.getAsJsonObject();

        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return responseCOnnectGithubAsJO;
    }

    public static JsonObject postGexfToGist(String gexfFile, String access_token, String fileName) {
        JsonObject responsePostGexfToGist = new JsonObject();
        JsonObject bodyPostGexfToGist = new JsonObject();
        bodyPostGexfToGist.addProperty("description", bundle.getString("general.message.file_sent_from_gephi"));
        bodyPostGexfToGist.addProperty("public", "true");
        JsonObject fileItself = new JsonObject();
        JsonObject contentsFile = new JsonObject();
        contentsFile.addProperty("content", gexfFile);
        fileItself.add(fileName, contentsFile);
        bodyPostGexfToGist.add("files", fileItself);
        String bodyToString = bodyPostGexfToGist.toString();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "https://api.github.com/gists";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer " + access_token)
                    .POST(HttpRequest.BodyPublishers.ofString(bodyToString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() > 299) {
                responsePostGexfToGist.addProperty(String.valueOf(response.statusCode()), response.body());
            } else {
                JsonElement responseAsJsonElement = JsonParser.parseString(response.body());
                JsonObject responseBodyAsSJsonObject = responseAsJsonElement.getAsJsonObject();
                responsePostGexfToGist.add(String.valueOf(response.statusCode()), responseBodyAsSJsonObject);
            }

        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return responsePostGexfToGist;
    }

}
