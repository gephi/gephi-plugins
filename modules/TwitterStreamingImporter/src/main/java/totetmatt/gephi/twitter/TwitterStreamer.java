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
import twitter4j.FilterQuery;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.util.function.Consumer;

/**
 *
 * @author totetmatt
 */
@ServiceProvider(service = TwitterStreamer.class)
public class TwitterStreamer {

    private TwitterStream twitterStream;
    private Twitter twitter;
    private CredentialProperty credentialProperty = new CredentialProperty();
    private final List<String> wordTracking = new ArrayList<>();
    private final Map<String, Long> userTracking = new HashMap<>();

    private boolean running = false;
    public void addFromList(String screenName,String listName){
        twitter = new TwitterFactory().getInstance();
        AccessToken accessToken = new AccessToken(credentialProperty.getToken(), credentialProperty.getTokenSecret());
        twitter.setOAuthConsumer(credentialProperty.getConsumerKey(), credentialProperty.getConsumerSecret());
        twitter.setOAuthAccessToken(accessToken);
        long cursor = -1;
            PagableResponseList<User> users;
            try {
                do {
                    users = twitter.getUserListMembers(screenName,listName, cursor);
                    for (User u : users) {
                        Logger.getLogger(MainTwitterWindows.class.getName()).log(Level.INFO, u.getScreenName().toLowerCase());
                        userTracking.put(u.getScreenName().toLowerCase(), u.getId());
                    }
                } while ((cursor = users.getNextCursor()) != 0);
            } catch (TwitterException ex) {
                Exceptions.printStackTrace(ex);
            }
    }
    public void addUser(String screenName) {
        twitter = new TwitterFactory().getInstance();
        AccessToken accessToken = new AccessToken(credentialProperty.getToken(), credentialProperty.getTokenSecret());
        twitter.setOAuthConsumer(credentialProperty.getConsumerKey(), credentialProperty.getConsumerSecret());
        twitter.setOAuthAccessToken(accessToken);
        try {
            ResponseList<User> response = twitter.users().lookupUsers(new String[]{screenName});
            for (User u : response) {
                userTracking.put(u.getScreenName().toLowerCase(), u.getId());
            }
        } catch (TwitterException ex) {
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

    public void setCredentialProperty(CredentialProperty credentialProperty) {
        this.credentialProperty = credentialProperty;
    }

    public TwitterStreamer() {
    }

    /* Start a new stream with new query parameter */
    public void start(Networklogic networkLogic) {
        AccessToken accessToken = new AccessToken(credentialProperty.getToken(), credentialProperty.getTokenSecret());
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.setOAuthConsumer(credentialProperty.getConsumerKey(), credentialProperty.getConsumerSecret());
        twitterStream.setOAuthAccessToken(accessToken);
        FilterQuery fq = new FilterQuery();

        Collection<String> tmpWordTrack = new ArrayList<>();

        Collection<Long> tmpUserTrack = new ArrayList<>();

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

        networkLogic.refreshGraphModel();
        twitterStream.addListener(networkLogic);
        running = true;
        twitterStream.onException(new Consumer<Exception> (){
            @Override
            public void accept(Exception t)  {
                stop();
                throw new UnsupportedOperationException(t);
            }
        });
        twitterStream.filter(fq);
    }

    /* Stop the running stream*/
    public void stop() {
        if (running) {
            twitterStream.shutdown();
        }
        running = false;
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
