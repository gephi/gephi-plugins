package totetmatt.gephi.twitter;

import org.openide.util.NbPreferences;

/**
 *
 * @author totetmatt
 */
public class CredentialProperty {

    String consumerKey = "";
    String consumerSecret = "";
    String token = "";
    String tokenSecret = "";

    public CredentialProperty() {
        load();
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getToken() {
        return token;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public void load() {
        consumerKey = NbPreferences.forModule(TwitterStreamer.class).get("twitter.consumerKey", "");
        consumerSecret = NbPreferences.forModule(TwitterStreamer.class).get("twitter.consumerSecret", "");
        token = NbPreferences.forModule(TwitterStreamer.class).get("twitter.token", "");
        tokenSecret = NbPreferences.forModule(TwitterStreamer.class).get("twitter.tokenSecret", "");

    }

    public void save() {
        NbPreferences.forModule(TwitterStreamer.class).put("twitter.consumerKey", consumerKey);
        NbPreferences.forModule(TwitterStreamer.class).put("twitter.consumerSecret", consumerSecret);
        NbPreferences.forModule(TwitterStreamer.class).put("twitter.token", token);
        NbPreferences.forModule(TwitterStreamer.class).put("twitter.tokenSecret", tokenSecret);
    }
}
