package fr.totetmatt.gephi.twitter.networklogics;


import com.twitter.clientlib.model.FilteredStreamingTweetResponse;
import com.twitter.clientlib.model.FilteredStreamingTweetResponseMatchingRules;
import com.twitter.clientlib.model.Media;
import com.twitter.clientlib.model.Rule;
import com.twitter.clientlib.model.Tweet;
import com.twitter.clientlib.model.User;
import com.vdurmont.emoji.EmojiParser;
import fr.totetmatt.gephi.twitter.networklogics.utils.TwitterNodeColumn;
import fr.totetmatt.gephi.twitter.utils.listener.TweetsStreamListener;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author totetmatt
 */
public abstract class Networklogic implements Comparable, TweetsStreamListener<FilteredStreamingTweetResponse> {

    protected static final Logger logger = Logger.getLogger(Networklogic.class.getName());

    abstract public void onStatus(FilteredStreamingTweetResponse newTweet);

    protected GraphModel graphModel;

    protected Set<String> rulesTokenized = new HashSet<>();

    @Override
    final synchronized public void actionOnTweetsStream(FilteredStreamingTweetResponse newTweet) {

        if (newTweet == null) {
            logger.log(Level.SEVERE, "Error: actionOnTweetsStream - streamingTweet is null ");
            return;
        }
        if (newTweet.getErrors() != null) {
            newTweet.getErrors().forEach(error -> {
                logger.log(Level.SEVERE, error.toString());
            });
            return;
        }

        try {

            onStatus(newTweet);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        } finally {

        }

    }

    @Override
    public int compareTo(Object o) {
        Integer p1 = this.index();
        Integer p2 = ((Networklogic) o).index();

        if (p1 > p2) {
            return 1;
        } else if (p1 < p2) {
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
        EMOJI("Emoji", new Color(1.0f, 1.0f, 1.0f)),
        CONVERSATION("Conversation", new Color(0.1f, 0.2f, 0.3f));

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

    public Networklogic() {

    }

    final public void refreshRulesNodeColumn(List<Rule> rules){
        graphModel.getGraph().writeLock();
        for (Rule r : rules) {
            String nodeTagAttributeName = "twitter_tag_" + r.getTag();
            if (!graphModel.getNodeTable().hasColumn(nodeTagAttributeName)) {
                graphModel.getNodeTable().addColumn(nodeTagAttributeName, Boolean.class, Origin.DATA);
            }
            for (String token : r.getValue().split(" ")) {
                rulesTokenized.add(token.toLowerCase());
            }
        }
        graphModel.getGraph().writeUnlock();
    }
    // Used to keep reference to the "current" workspace
    // Should be called before a new stream
    public void refreshGraphModel(List<Rule> rules) {

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
        // Flag if multiple rules
        refreshRulesNodeColumn(rules);
        for (TwitterNodeColumn c : TwitterNodeColumn.values()) {
            if (!graphModel.getNodeTable().hasColumn(c.label)) {
                graphModel.getNodeTable().addColumn(c.label, c.classType, Origin.DATA);
            }
        }

    }

    final protected Node createNode(String id, String label, NodeType type, List<FilteredStreamingTweetResponseMatchingRules> rules) {
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
            if (rules != null) {
                for (FilteredStreamingTweetResponseMatchingRules r : rules) {
                    node.setAttribute("twitter_tag_" + r.getTag(), true);
                }
            }
            graphModel.getGraph().addNode(node);
        }

        return node;
    }

    final protected Node createTweet(Tweet status, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        Node tweet = createNode(status.getId(), status.getText(), NodeType.TWEET, rules);
        tweet.setAttribute(TwitterNodeColumn.NODE_CREATED_AT.label, status.getCreatedAt().toString());
        tweet.setAttribute(TwitterNodeColumn.NODE_LANG.label, status.getLang());
        tweet.setAttribute(TwitterNodeColumn.NODE_CONVERSATION_ID.label, status.getConversationId());
        tweet.setAttribute(TwitterNodeColumn.NODE_POSSIBLY_SENSITIVE.label, status.getPossiblySensitive());
        return tweet;
    }

    final protected Node createMedia(Media media, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        Node mediaNode = createNode(media.getMediaKey(), media.getMediaKey(), NodeType.MEDIA, rules);
        mediaNode.setAttribute(TwitterNodeColumn.NODE_LANG.label, media.getType());
        return mediaNode;
    }

    final protected Node createSymbol(String symbol, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        symbol = "$" + symbol;
        return createNode(symbol, symbol, NodeType.SYMBOL, rules);
    }

    final protected Node createUrl(String url, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        return createNode(url, url, NodeType.URL, rules);
    }

    final protected Node createHashtag(String hashtag, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        hashtag = "#" + hashtag.toLowerCase();
        return createNode(hashtag, hashtag, NodeType.HASHTAG, rules);
    }

    final protected Node createUser(User u, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        String screenName = "@" + u.getUsername().toLowerCase();
        Node user = createNode(u.getId(), screenName, NodeType.USER, rules);
        user.setAttribute(TwitterNodeColumn.NODE_USER_DESCRIPTION.label, u.getDescription());
        user.setAttribute(TwitterNodeColumn.NODE_USER_PROFILE_IMAGE.label, u.getProfileImageUrl().toString());
        user.setAttribute(TwitterNodeColumn.NODE_USER_REAL_NAME.label, u.getName());
        user.setAttribute(TwitterNodeColumn.NODE_CREATED_AT.label, u.getCreatedAt().toString());
        user.setAttribute(TwitterNodeColumn.NODE_USER_LOCATION.label, u.getLocation());
        return user;
    }

    final protected Node createEmoji(String emoji_utf8, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        Node emoji = createNode(EmojiParser.parseToHtmlDecimal(emoji_utf8), emoji_utf8, NodeType.EMOJI, rules);
        emoji.setAttribute(TwitterNodeColumn.NODE_EMOJI_UTF8.label, emoji_utf8);
        emoji.setAttribute(TwitterNodeColumn.NODE_EMOJI_ALIAS.label, EmojiParser.parseToAliases(emoji_utf8));
        emoji.setAttribute(TwitterNodeColumn.NODE_EMOJI_HTML_DECIMAL.label, EmojiParser.parseToHtmlDecimal(emoji_utf8));
        return emoji;
    }

    final protected Node createUser(String id, String username, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        String screenName = "@" + username.toLowerCase();
        Node user = createNode(id, screenName, NodeType.USER, rules);
        return user;
    }

    // This is mainly for the name in the UI.
    public abstract String getName();

    //  Index place in the networklogic list.
    public abstract int index();

    // Other method can be overidden for dedicated usage.
    @Override
    public String toString() {
        return this.getName();
    }

    public GraphModel getGraphModel() {
        return graphModel;
    }

}
