/*
 * author: Clément Levallois
 */
package net.clementlevallois.web.exports.plugin.controller;

import com.google.gson.JsonObject;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author LEVALLOIS
 */
public class Tests {

    /**
     * @param args the command line arguments
     */
    private static JsonObject responseGithubConnectAction;
    private static String accessToken;

    public static void main(String[] args) {
        responseGithubConnectAction = PublishingActions.connectToGithub();
        if (!responseGithubConnectAction.has("user_code")) {
            System.out.println("error retrieving your user code");
        } else {
            String userCode = responseGithubConnectAction.get("user_code").getAsString();
            System.out.println("USER CODE TO ENTER: " + userCode);
            System.out.println("URL: https://github.com/login/device");
            String deviceCode = responseGithubConnectAction.get("device_code").getAsString();
            JsonObject resultGithubPolling = PublishingActions.pollGithubToCheckForUserAuth(deviceCode);
            if (resultGithubPolling.has("access_token")) {
                accessToken = resultGithubPolling.get("access_token").getAsString();
            } else {
                System.out.println("error - the user code was not entered on Github");
                return;
            }
            JsonObject jsonObjectOfGexfAsStringRetrieval = PublishingActions.getGexfAsString();
            if (!jsonObjectOfGexfAsStringRetrieval.has("200")) {
                if (!jsonObjectOfGexfAsStringRetrieval.keySet().isEmpty()) {
                    String errorKey = jsonObjectOfGexfAsStringRetrieval.keySet().iterator().next();
                    System.out.println(jsonObjectOfGexfAsStringRetrieval.get(errorKey).getAsString());
                } else {
                    System.out.println("unspecified error when retrieving gexf of current network");
                }
            } else {
                String gexf = jsonObjectOfGexfAsStringRetrieval.getAsJsonArray("200").getAsString();
                if (accessToken.isBlank()) {
                    System.out.println("error retrieving access token from user preferences");
                } else {
                    PublishingActions.postGexfToGist(gexf, accessToken, "test.gexf");
                }
            }
        }
    }

}
