package totetmatt.gephi.twitter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import totetmatt.gephi.twitter.networklogic.Networklogic;
import totetmatt.gephi.twitter.utils.UserIdResolver;
import twitter4j.FilterQuery;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

/**
 *
 * @author totetmatt
 */
@ServiceProvider(service = TwitterStreamer.class)
public class TwitterStreamer {

    TwitterStream twitter;
    CredentialProperty credentialProperty = new CredentialProperty();
    List<String> wordTracking = new ArrayList<String>();
    Map<String, Long> userTracking = new HashMap<String, Long>();

    public void addUser(String screenName) {
        try {
            long id = UserIdResolver.resolve(screenName);
            userTracking.put(screenName, id);
        } catch (IOException ex) {
            Logger.getLogger(MainTwitterWindows.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addWordTracking(String word) {
        if (!wordTracking.contains(word)) {
            wordTracking.add(word);
        }
    }

    public Map<String, Long> getUserTracking() {
        return userTracking;
    }

    public List<String> getWordTracking() {
        return wordTracking;
    }

    public CredentialProperty getCredentialProperty() {
        return credentialProperty;
    }

    public TwitterStreamer() {

    }

    /* Start a new stream with new query parameter */
    public void start(Networklogic networkLogic) {
        AccessToken accessToken = new AccessToken(credentialProperty.getToken(), credentialProperty.getTokenSecret());
        twitter = new TwitterStreamFactory().getInstance();
        twitter.setOAuthConsumer(credentialProperty.getConsumerKey(), credentialProperty.getConsumerSecret());
        twitter.setOAuthAccessToken(accessToken);
        FilterQuery fq = new FilterQuery();

        Collection<String> tmpWordTrack = new ArrayList<String>();

        Collection<Long> tmpUserTrack = new ArrayList<Long>();

        if (!wordTracking.isEmpty()) {
            tmpWordTrack.addAll(wordTracking);
        }

        if (!userTracking.isEmpty()) {
            tmpWordTrack.addAll(userTracking.keySet());
            tmpUserTrack = userTracking.values();
            tmpUserTrack.remove(Long.parseLong("0"));

        }

        String[] track = new String[tmpWordTrack.size()];
        tmpWordTrack.toArray(track);

        if (!tmpUserTrack.isEmpty()) {
            Long[] userTrack = new Long[tmpUserTrack.size()];
            tmpUserTrack.toArray(userTrack);
            fq.follow(ArrayUtils.toPrimitive(userTrack));
        }

        fq.track(track);

        networkLogic.setTrack(track);

        twitter.addListener(networkLogic);
        twitter.filter(fq);
    }

    /* Stop the running stream*/
    public void stop() {
        twitter.shutdown();
    }

    /* Save all tracking parameters in a file*/
    public void saveTracking(File saveFile) throws JSONException, IOException {
        // Using JSONObject from Twitter
        // + Avoid to load another library
        // - Very limited
        JSONObject o = new JSONObject();

        o.put("wordTracking", wordTracking);
        o.put("userTracking", userTracking);
        Files.write(saveFile.toPath(), o.toString().getBytes());

    }

    /* Load all query parameters from a file */
    public void loadTracking(File loadFile) throws IOException, JSONException {
        // Using JSONObject from Twitter
        // + Avoid to laod another library
        // - Very limited
        JSONObject o = new JSONObject(new String(Files.readAllBytes(loadFile.toPath())));

        wordTracking.clear();
        userTracking.clear();

        for (int i = 0; i < o.getJSONArray("wordTracking").length(); i++) {
            wordTracking.add(o.getJSONArray("wordTracking").getString(i));
        }
        Iterator userIt = o.getJSONObject("userTracking").keys();
        while (userIt.hasNext()) {
            String username = (String) userIt.next();
            long id = o.getJSONObject("userTracking").getLong(username);
            userTracking.put(username, id);
        }
    }
}
