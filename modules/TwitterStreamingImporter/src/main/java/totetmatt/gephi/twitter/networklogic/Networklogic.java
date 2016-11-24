package totetmatt.gephi.twitter.networklogic;

import java.awt.Color;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.openide.util.Lookup;
import totetmatt.gephi.twitter.networklogic.utils.TwitterNodeColumn;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 *
 * @author totetmatt
 */
public abstract class Networklogic implements StatusListener {

    // Track word passed on the Stream FilterQuery
    protected String[] track;

    public enum NodeType {
        USER("User", new Color(0.5f, 0, 0)),
        TWEET("Tweet", new Color(0.5f, 0.5f, 0)),
        HASHTAG("Hashtag", new Color(0, 0.5f, 0)),
        MEDIA("Media", new Color(0, 0.5f, 0.5f)),
        URL("Link", new Color(0, 0, 0.5f)),
        SYMBOL("Symbol", new Color(0.5f, 0, 0.5f));

        private final String type;
        private final Color color;

        private NodeType(String type, Color color) {
            this.type = type;
            this.color = color;
        }

        public String getType() {
            return type;
        }

        public Color getColor() {
            return color;
        }
    }

    protected GraphModel graphModel;

    public Networklogic() {

    }

    public void setTrack(String[] track) {
        this.track = track;
    }

    // Used to keep reference to the "current" workspace
    // Should be called before a new stream
    public void refreshGraphModel() {
        for(NodeType v :NodeType.values()){
            System.out.println(v);
        }
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        if(graphModel.getGraph().getEdgeCount() == 0 && graphModel.getGraph().getNodeCount() == 0){
            Configuration conf = new Configuration();
            conf.setTimeRepresentation(TimeRepresentation.TIMESTAMP);
            graphModel.setConfiguration(conf);
        }
        
        for(TwitterNodeColumn c :TwitterNodeColumn.values()){
            if(!graphModel.getNodeTable().hasColumn(c.label)){
                graphModel.getNodeTable().addColumn(c.label, c.classType,null);
            }
        }
    }

    // This is call for each tweet received, it *needs* to be defined afterward.
    @Override
    public final void onStatus(Status status) {
        try {
            graphModel.getGraph().writeLock();
            processStatus(status);
        } catch (Exception e) {
            Logger.getLogger(Networklogic.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graphModel.getGraph().writeUnlock();
        }
    }

    protected Node createNode(String id, String label, NodeType type) {
        Node node = graphModel.getGraph().getNode(id);

        if (node == null) {
            Color color = type.getColor();
            String typeText = type.getType();
            node = graphModel.factory().newNode(id);
            node.setLabel(label);
            node.setColor(color);
            node.setAttribute(TwitterNodeColumn.NODE_TYPE.label, typeText);

            node.setSize(10);
            node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
            node.setY((float) ((0.01 + Math.random()) * 1000) - 500);

            graphModel.getGraph().addNode(node);
        }

        return node;
    }

    protected Node createTweet(Status status) {
        Node tweet = createNode(String.valueOf(status.getId()), status.getText(), NodeType.TWEET);
        
        tweet.setAttribute(TwitterNodeColumn.NODE_CREATED_AT.label, status.getCreatedAt().toString());
        tweet.setAttribute(TwitterNodeColumn.NODE_LANG.label,status.getLang());
       
        
        if(status.getGeoLocation() != null){
            tweet.setAttribute(TwitterNodeColumn.NODE_TWEET_GEO_LATITUDE.label, status.getGeoLocation().getLatitude());
            tweet.setAttribute(TwitterNodeColumn.NODE_TWEET_GEO_LONGITUDE.label, status.getGeoLocation().getLongitude());
        }
        return tweet;
    }

    protected Node createMedia(String media) {
        return createNode(media, media, NodeType.MEDIA);
    }

    protected Node createSymbol(String symbol) {
        symbol = "$" + symbol;
        return createNode(symbol, symbol, NodeType.SYMBOL);
    }

    protected Node createUrl(String url) {
        return createNode(url, url, NodeType.URL);
    }

    protected Node createHashtag(String hashtag) {
        hashtag = "#" + hashtag.toLowerCase();

        return createNode(hashtag, hashtag, NodeType.HASHTAG);
    }

    protected Node createUser(User u) {
        String screenName = "@" + u.getScreenName().toLowerCase();
        Node user = createNode(screenName, screenName, NodeType.USER);
        user.setAttribute(TwitterNodeColumn.NODE_LANG.label, u.getLang());
        user.setAttribute(TwitterNodeColumn.NODE_USER_DESCRIPTION.label,u.getDescription());
        user.setAttribute(TwitterNodeColumn.NODE_USER_EMAIL.label,u.getEmail());
        user.setAttribute(TwitterNodeColumn.NODE_USER_PROFILE_IMAGE.label, u.getBiggerProfileImageURL());
        user.setAttribute(TwitterNodeColumn.NODE_USER_FRIENDS_COUNT.label,  u.getFriendsCount());
        user.setAttribute(TwitterNodeColumn.NODE_USER_FOLLOWERS_COUNT.label,u.getFollowersCount());
        user.setAttribute(TwitterNodeColumn.NODE_USER_REAL_NAME.label,u.getName());
        user.setAttribute(TwitterNodeColumn.NODE_CREATED_AT.label,u.getCreatedAt().toString());
        user.setAttribute(TwitterNodeColumn.NODE_USER_LOCATION.label,u.getLocation());           
        return user;
    }
    protected Node createUser(UserMentionEntity u) {
        String screenName = "@" + u.getScreenName().toLowerCase();
        return createNode(screenName, screenName, NodeType.USER);
    }

    public abstract void processStatus(Status status);

    // This is mainly for the name in the UI.
    public abstract String getName();

    // Other method can be overidden for dedicated usage.
    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
    }

    @Override
    public void onStallWarning(StallWarning warning) {
    }

    @Override
    public void onException(Exception ex) {
    }

    @Override
    public String toString() {
        return this.getName();
    }

}
