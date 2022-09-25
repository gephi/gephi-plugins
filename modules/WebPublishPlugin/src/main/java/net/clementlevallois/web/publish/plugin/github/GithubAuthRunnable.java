/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package net.clementlevallois.web.publish.plugin.github;

import static net.clementlevallois.web.publish.plugin.controller.GlobalConfigParams.ACCESS_TOKEN_KEY_IN_USER_PREFS;
import static net.clementlevallois.web.publish.plugin.controller.GlobalConfigParams.GEPHI_APP_CLIENT_ID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import net.clementlevallois.web.publish.plugin.controller.WebPublishExporterUI;
import net.clementlevallois.web.publish.plugin.model.GitHubModel;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author LEVALLOIS
 */
public class GithubAuthRunnable implements LongTask, Runnable {

    private static final ResourceBundle bundle = NbBundle.getBundle(WebPublishExporterUI.class);

    private ProgressTicket progressTicket;
    private final GitHubModel gitHubModel;

    private boolean cancel = false;

    public GithubAuthRunnable(GitHubModel gitHubModel) {
        this.gitHubModel = gitHubModel;
    }

    @Override
    public void run() {
        Progress.start(progressTicket);
        Progress.setDisplayName(progressTicket, bundle.getString("general.message.authenticating_with_github"));

        JsonObject responseAsJsonObject = new JsonObject();
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://github.com/login/oauth/access_token";

        String inputParams = "client_id="
            + GEPHI_APP_CLIENT_ID
            + "&"
            + "device_code="
            + gitHubModel.getDeviceCode()
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
        while (!success && currDuration < maxDuration && !cancel) {
            HttpResponse<String> response = null;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            JsonElement responseAsJsonElement = JsonParser.parseString(response.body());
            responseAsJsonObject = responseAsJsonElement.getAsJsonObject();
            if (responseAsJsonObject.has("access_token")) {
                break;
            }
            currDuration = (float) (System.currentTimeMillis() - startTime) / (float) 1000;
            // the min duration recommended by Github between two polls is 5 seconds
            // -> https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps#device-flow
            try {
                Thread.sleep(5200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (responseAsJsonObject.has("access_token")) {
            String accessToken = responseAsJsonObject.get("access_token").getAsString();
            gitHubModel.setAccessToken(accessToken);
            Preferences preferences = NbPreferences.forModule(this.getClass());
            preferences.put(ACCESS_TOKEN_KEY_IN_USER_PREFS, accessToken);
        }

        Progress.finish(progressTicket);
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
