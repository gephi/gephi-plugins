/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.clementlevallois.web.publish.plugin.github;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import static net.clementlevallois.web.publish.plugin.controller.GlobalConfigParams.ACCESS_TOKEN_KEY_IN_USER_PREFS;
import static net.clementlevallois.web.publish.plugin.controller.GlobalConfigParams.GEPHI_APP_CLIENT_ID;
import static net.clementlevallois.web.publish.plugin.controller.JPanelWebExport.COLOR_SUCCESS;
import net.clementlevallois.web.publish.plugin.controller.WebPublishExporterUI;
import net.clementlevallois.web.publish.plugin.model.GitHubModel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author LEVALLOIS
 */
public class GithubPollerCreator {

    private static final ResourceBundle bundle = NbBundle.getBundle(WebPublishExporterUI.class);

    public SwingWorker createPoller(GitHubModel gitHubModel, final JTextField jTextFieldGithubErrorMsg) throws IOException, InterruptedException {
        return new SwingWorker<JsonObject, Integer>() {

            @Override
            protected JsonObject doInBackground() throws IOException, InterruptedException {
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
                while (!success && currDuration < maxDuration) {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    JsonElement responseAsJsonElement = JsonParser.parseString(response.body());
                    responseAsJsonObject = responseAsJsonElement.getAsJsonObject();
                    if (responseAsJsonObject.has("access_token")) {
                        break;
                    }
                    currDuration = (float) (System.currentTimeMillis() - startTime) / (float) 1000;
                    // the min duration recommended by Github between two polls is 5 seconds
                    // -> https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps#device-flow
                    Thread.sleep(5200);
                }
                return responseAsJsonObject;
            }

            @Override
            public void done() {
                JsonObject response;
                try {
                    response = get();
                } catch (InterruptedException | ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                    jTextFieldGithubErrorMsg.setText("Internet connection error");
                    jTextFieldGithubErrorMsg.setCaretPosition(0);
                    return;
                }
                if (response.has("access_token")) {
                    String accessToken = response.get("access_token").getAsString();
                    gitHubModel.setAccessToken(accessToken);
                    Preferences preferences = NbPreferences.forModule(this.getClass());
                    preferences.put(ACCESS_TOKEN_KEY_IN_USER_PREFS, accessToken);
                    jTextFieldGithubErrorMsg.setForeground(Color.decode(COLOR_SUCCESS));
                    jTextFieldGithubErrorMsg.setText(bundle.getString("general.message.success_switch_to_publish"));
                    jTextFieldGithubErrorMsg.setCaretPosition(0);
                } else {
                    jTextFieldGithubErrorMsg.setText(bundle.getString("general.message.error.no_user_code"));
                    jTextFieldGithubErrorMsg.setCaretPosition(0);
                }
            }

        };
    }

}
