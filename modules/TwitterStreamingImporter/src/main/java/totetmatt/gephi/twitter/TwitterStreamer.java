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
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import totetmatt.gephi.twitter.networklogic.Networklogic;
import totetmatt.gephi.twitter.networklogic.utils.Language;
import totetmatt.gephi.twitter.networklogic.utils.TrackLocation;
import twitter4j.FilterQuery;
import twitter4j.JSONArray;
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
    private final Map<String,TrackLocation> locationTracking = new HashMap<>();
    private final List<Language> languageFilter = new ArrayList<>();

    private boolean running = false;

    private boolean randomSample = false;
    
    public void setRandomSample(boolean randomSample) {
        this.randomSample = randomSample;
    }
    
    public boolean getRandomSample() {
        return this.randomSample;
    }
    
    public Map<String,TrackLocation> getLocationTracking() {
        return locationTracking;
    }
    
    public void addLocation(TrackLocation location) {
        if(location.isValid()) {
            locationTracking.put(location.getName(),location);
        } else {
            throw new IllegalArgumentException("-90 <= Latitude <= 90 and -180 <= Longitude <= 180");
        }
    }
    
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

    public void addLanguage(Language l){
        if(!languageFilter.contains(l)) {
            languageFilter.add(l);
        }
    }
    public Map<String, Long> getUserTracking() {
        return userTracking;
    }

    public List<String> getWordTracking() {
        return wordTracking;
    }

    public List<Language> getLanguageFilter(){
        return languageFilter;
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
        
        if (!locationTracking.isEmpty()) {
            List<double[]> tmpLocationTrack = new ArrayList<>();
            for(TrackLocation l : locationTracking.values()) {
                 tmpLocationTrack.add(new double[]{l.getSwLongitude(),l.getSwLatitude()});
                 tmpLocationTrack.add(new double[]{l.getNeLongitude(),l.getNeLatitude()});
            }
            double[][] locationTrack = new double[tmpLocationTrack.size()][];
            tmpLocationTrack.toArray(locationTrack);
            fq.locations(locationTrack);
        }

        String[] track = new String[tmpWordTrack.size()];
        tmpWordTrack.toArray(track);

        if (!tmpUserTrack.isEmpty()) {
            Long[] userTrack = new Long[tmpUserTrack.size()];
            tmpUserTrack.toArray(userTrack);
            fq.follow(ArrayUtils.toPrimitive(userTrack));
        }

        String langStringFilter = "";
        if(!languageFilter.isEmpty()) {
            ArrayList<String> lang = new ArrayList<>();
       
            for(Language l : languageFilter) {
                lang.add(l.getCode());
            }
            langStringFilter = String.join(",",lang);
            
        }
       // Initalize the networklogic 
        networkLogic.setTrack(track);
        networkLogic.refreshGraphModel();
        
        // Error handling for the twitter streamer
        twitterStream.addListener(networkLogic);
            twitterStream.onException(new Consumer<Exception> (){
            @Override
            public void accept(Exception t)  {
                stop();
                throw new UnsupportedOperationException(t);
            }
        });
            
        // If sample stream api has been selected
        if(this.randomSample) {
            if("".equals(langStringFilter)){
                twitterStream.sample();
            } else {
                twitterStream.sample(langStringFilter);
            }
        } else {  // if track streaming api has been selected
            fq.language(langStringFilter);
            fq.track(track);
            twitterStream.filter(fq);
        }
        running = true;
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
        JSONArray words = new JSONArray(wordTracking);
        o.put("wordTracking", words);
        
        JSONObject users = new JSONObject(userTracking);
        o.put("userTracking", users);
        
        JSONArray locations = new JSONArray();
        for(Entry<String,TrackLocation> l: locationTracking.entrySet()) {
            JSONObject jsonLocation = new JSONObject();
            jsonLocation.put("id", l.getKey());
            jsonLocation.put("name", l.getValue().getName());
            jsonLocation.put("neLat", l.getValue().getNeLatitude());
            jsonLocation.put("neLong", l.getValue().getNeLongitude());
            jsonLocation.put("swLat", l.getValue().getSwLatitude());
            jsonLocation.put("swLong", l.getValue().getSwLongitude());
   
            locations.put(jsonLocation);
        }
        o.put("locationsTracking", locations);
        
        JSONArray languages = new JSONArray();
        for(Language l: languageFilter) {
            JSONObject jsonLang = new JSONObject();
            jsonLang.put("code", l.getCode());
            jsonLang.put("label", l.getLabel());
            languages.put(jsonLang);
        }
        o.put("languageFilter",languages);
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
        locationTracking.clear();

        for (int i = 0; i < o.getJSONArray("wordTracking").length(); i++) {
            wordTracking.add(o.getJSONArray("wordTracking").getString(i));
        }
        Iterator userIt = o.getJSONObject("userTracking").keys();
        while (userIt.hasNext()) {
            String username = (String) userIt.next();
            long id = o.getJSONObject("userTracking").getLong(username);
            userTracking.put(username, id);
        }
        
         for (int i = 0; i < o.getJSONArray("locationsTracking").length(); i++) {
            JSONObject jsonLocation = o.getJSONArray("locationsTracking").getJSONObject(i);
            locationTracking.put(jsonLocation.getString("id"),new TrackLocation(
                        Double.parseDouble(jsonLocation.getString("swLat") ),
                        Double.parseDouble(jsonLocation.getString("swLong") ),
                        Double.parseDouble(jsonLocation.getString("neLat") ),
                        Double.parseDouble(jsonLocation.getString("neLong") ),
                        jsonLocation.getString("name")
                   
            ));
        }
         
        for (int i = 0; i < o.getJSONArray("languageFilter").length(); i++) {
            JSONObject jsonLanguage = o.getJSONArray("languageFilter").getJSONObject(i);
            languageFilter.add(
                    new Language(jsonLanguage.getString("code")
                            ,jsonLanguage.getString("label")
                    )
            );
        }
    
    }
}
