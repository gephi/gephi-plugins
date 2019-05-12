package totetmatt.gephi.twitter.networklogic;

import com.vdurmont.emoji.EmojiParser;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.TimeFormat;
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
public abstract class Networklogic implements StatusListener, Comparable  {

    // Track word passed on the Stream FilterQuery
    protected String[] track;

    @Override
    public int compareTo(Object o) {
       Integer p1 = this.index();
       Integer p2 = ((Networklogic) o).index();

       if (p1 > p2) {
           return 1;
       } else if (p1 < p2){
           return -1;
       } else {
           return 0;
       }
    }

    public enum NodeType {
        USER("User", new Color(0.5f, 0, 0)),
        TWEET("Tweet", new Color(0.5f, 0.5f, 0)),
        HASHTAG("Hashtag", new Color(0, 0.5f, 0)),
        MEDIA("Media", new Color(0, 0.5f, 0.5f)),
        URL("Link", new Color(0, 0, 0.5f)),
        SYMBOL("Symbol", new Color(0.5f, 0, 0.5f)),
        EMOJI("Emoji", new Color(1.0f, 1.0f, 1.0f));
        
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
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        graphModel.setTimeFormat(TimeFormat.DATETIME);
        if (graphModel.getConfiguration().getTimeRepresentation() != TimeRepresentation.TIMESTAMP) {
            Configuration conf = new Configuration();
            conf.setTimeRepresentation(TimeRepresentation.TIMESTAMP);
            try {
                graphModel.setConfiguration(conf);
            } catch (IllegalStateException e) {
                throw new RuntimeException("Timestamp time representation is needed but the current workspace uses "
                        + graphModel.getConfiguration().getTimeRepresentation()
                        + " and it can't be automatically changed when the graph is not in its initial status. Please create an empty workspace for use with twitter streaming.",
                        e);
            }
        }

        for (TwitterNodeColumn c : TwitterNodeColumn.values()) {
            if (!graphModel.getNodeTable().hasColumn(c.label)) {
                graphModel.getNodeTable().addColumn(c.label, c.classType, Origin.DATA);
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
        tweet.setAttribute(TwitterNodeColumn.NODE_LANG.label, status.getLang());
        tweet.setAttribute(TwitterNodeColumn.NODE_POSSIBLY_SENSITIVE.label, status.isPossiblySensitive());
        
        if(status.getQuotedStatusPermalink() != null) {
            tweet.setAttribute(TwitterNodeColumn.NODE_QUOTED_STATUS_PERMALINK.label, status.getQuotedStatusPermalink().getExpandedURL());
        }
        
        if (status.getPlace() != null) {
            tweet.setAttribute(TwitterNodeColumn.NODE_TWEET_PLACE_COUNTRY.label, status.getPlace().getCountry());
            tweet.setAttribute(TwitterNodeColumn.NODE_TWEET_PLACE_TYPE.label, status.getPlace().getPlaceType());
            tweet.setAttribute(TwitterNodeColumn.NODE_TWEET_PLACE_FULLNAME.label, status.getPlace().getFullName());
            tweet.setAttribute(TwitterNodeColumn.NODE_TWEET_PLACE_NAME.label, status.getPlace().getName());
        }
        if (status.getGeoLocation() != null) {
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
        user.setAttribute(TwitterNodeColumn.NODE_USER_DESCRIPTION.label, u.getDescription());
        user.setAttribute(TwitterNodeColumn.NODE_USER_EMAIL.label, u.getEmail());
        user.setAttribute(TwitterNodeColumn.NODE_USER_PROFILE_IMAGE.label, u.getBiggerProfileImageURL());
        user.setAttribute(TwitterNodeColumn.NODE_USER_FRIENDS_COUNT.label, u.getFriendsCount());
        user.setAttribute(TwitterNodeColumn.NODE_USER_FOLLOWERS_COUNT.label, u.getFollowersCount());
        user.setAttribute(TwitterNodeColumn.NODE_USER_REAL_NAME.label, u.getName());
        user.setAttribute(TwitterNodeColumn.NODE_CREATED_AT.label, u.getCreatedAt().toString());
        user.setAttribute(TwitterNodeColumn.NODE_USER_LOCATION.label, u.getLocation());
        return user;
    }
    protected Node createEmoji(String emoji_utf8) {
        Node emoji = createNode(EmojiParser.parseToHtmlDecimal(emoji_utf8), emoji_utf8, NodeType.EMOJI);
        emoji.setAttribute(TwitterNodeColumn.NODE_EMOJI_UTF8.label, emoji_utf8);
        emoji.setAttribute(TwitterNodeColumn.NODE_EMOJI_ALIAS.label, EmojiParser.parseToAliases(emoji_utf8));
        emoji.setAttribute(TwitterNodeColumn.NODE_EMOJI_HTML_DECIMAL.label, EmojiParser.parseToHtmlDecimal(emoji_utf8));
        return emoji;
    }
    protected Node createUser(UserMentionEntity u) {
        String screenName = "@" + u.getScreenName().toLowerCase();
        return createNode(screenName, screenName, NodeType.USER);
    }

    public abstract void processStatus(Status status);

    // This is mainly for the name in the UI.
    public abstract String getName();
    
    //  Index place in the networklogic list.
    public abstract int index();

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
