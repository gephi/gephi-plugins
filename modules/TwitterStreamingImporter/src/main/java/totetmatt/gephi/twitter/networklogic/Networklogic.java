package totetmatt.gephi.twitter.networklogic;

import java.awt.Color;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.openide.util.Lookup;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

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

    // Type 
    public static final String TWITTER_TYPE_COLUMN_ID = "twitter_type";
    public static final String TWITTER_TYPE_USER = "User";
    public static final String TWITTER_TYPE_TWEET = "Tweet";
    public static final String TWITTER_TYPE_HASHTAG = "Hashtag";
    public static final String TWITTER_TYPE_MEDIA = "Media";
    public static final String TWITTER_TYPE_URL = "URL";
    public static final String TWITTER_TYPE_SYMBOL = "Symbol";
    
    // Column for Nodes
    public static final String NODE_TWEET_GEO_LATITUDE = "lat";
    public static final String NODE_TWEET_GEO_LONGITUDE = "lng";
    public static final String NODE_TWEET_CREATED_AT = "created_at";
    public static final String NODE_TWEET_LANG = "lang";

    // Basic Color code used since the beginning.
    // Not real standard but ensure same color code for all node type
    public final static Color STANDARD_COLOR_USER = new Color(0.5f, 0, 0);
    public final static Color STANDARD_COLOR_TWEET = new Color(0.5f, 0.5f, 0);
    public final static Color STANDARD_COLOR_HASHTAG = new Color(0, 0.5f, 0);
    public final static Color STANDARD_COLOR_MEDIA = new Color(0, 0.5f, 0.5f);
    public final static Color STANDARD_COLOR_URL = new Color(0, 0, 0.5f);
    public final static Color STANDARD_COLOR_SYMBOL = new Color(0.5f, 0, 0.5f);

    protected GraphModel graphModel;

    public Networklogic() {

    }

    public void setTrack(String[] track) {
        this.track = track;
    }

    // Used to keep reference to the "current" workspace
    // Should be called before a new stream
    public void refreshGraphModel() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        if(graphModel.getGraph().getEdgeCount() == 0 && graphModel.getGraph().getNodeCount() == 0){
            Configuration conf = new Configuration();
            conf.setTimeRepresentation(TimeRepresentation.TIMESTAMP);
            graphModel.setConfiguration(conf);
        }

        if(!graphModel.getNodeTable().hasColumn(TWITTER_TYPE_COLUMN_ID)){
            graphModel.getNodeTable().addColumn(TWITTER_TYPE_COLUMN_ID, "Twitter Type", String.class, null);
            
            graphModel.getNodeTable().addColumn(NODE_TWEET_GEO_LATITUDE, Double.class,null);
            graphModel.getNodeTable().addColumn(NODE_TWEET_GEO_LONGITUDE, Double.class,null);
            graphModel.getNodeTable().addColumn(NODE_TWEET_CREATED_AT, String.class);
            graphModel.getNodeTable().addColumn(NODE_TWEET_LANG,String.class);
            
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
            node.setAttribute(TWITTER_TYPE_COLUMN_ID, typeText);

            node.setSize(10);
            node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
            node.setY((float) ((0.01 + Math.random()) * 1000) - 500);

            graphModel.getGraph().addNode(node);
        }

        return node;
    }

    protected Node createTweet(Status status) {
        return createNode(String.valueOf(status.getId()), status.getText(), NodeType.TWEET);
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

    protected Node createUser(String screenName) {
        screenName = "@" + screenName.toLowerCase();

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
