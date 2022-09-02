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
import java.util.ResourceBundle;
import java.util.UUID;
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

    public static JsonObject getGexfAsString() {
        JsonObject jsonObject = new JsonObject();
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentProject() == null) {
            jsonObject.addProperty("95", bundle.getString("general.message.error.no_open_project"));
            return jsonObject;
        }
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        if (graphModel.getGraph().getNodeCount() == 0) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("96", bundle.getString("general.message.error.empty_network"));
            return jsonObject;
        }
        Workspace workspace = pc.getCurrentWorkspace();
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        Exporter exporterGexf = ec.getExporter("gexf");
        exporterGexf.setWorkspace(workspace);
        StringWriter stringWriter = new StringWriter();
        ec.exportWriter(stringWriter, (CharacterExporter) exporterGexf);
        String gexfToSendAsString = stringWriter.toString();

        jsonObject.addProperty("200", gexfToSendAsString);

        return jsonObject;
    }

    public static JsonObject pollGithubToCheckForUserAuth(String deviceCode) {
        JsonObject jsonObject = new JsonObject();
        String clientId = "Iv1.936245ffcd310336";
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "https://github.com/login/oauth/access_token";

            String inputParams = "client_id="
                    + clientId
                    + "&"
                    + "device_code="
                    + deviceCode
                    + "&"
                    + "grant_type=urn:ietf:params:oauth:grant-type:device_code";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(inputParams))
                    .build();

            boolean success = false;
            long startTime = System.currentTimeMillis();
            long maxDuration = 900_000;
            float currDuration = 0;
            while (!success && currDuration < maxDuration) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonElement responseAsJsonElement = JsonParser.parseString(response.body());
                jsonObject = responseAsJsonElement.getAsJsonObject();
                if (jsonObject.has("access_token")) {
                    System.out.println(jsonObject.toString());
                    break;
                }
                currDuration = (float) (System.currentTimeMillis() - startTime) / (float) 1000;
                Thread.sleep(5200);
            }
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return jsonObject;
    }

    public static JsonObject connectToGithub() {
        JsonObject responseCOnnectGithubAsJO = null;
        try {
            HttpClient client = HttpClient.newHttpClient();
            String clientId = "Iv1.936245ffcd310336";
            String inputParams = "client_id=" + clientId + "&scope=gist";
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
                JsonObject error = new JsonObject();
                error.addProperty(String.valueOf(response.statusCode()), response.body());
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
