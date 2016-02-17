package totetmatt.gephi.twitter.networklogic;

import java.awt.Color;
import java.util.Arrays;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 *
 * @author totetmatt
 */
@ServiceProvider(service = Networklogic.class)
public class FullSmartNetwork extends Networklogic {

    public FullSmartNetwork() {

    }

    @Override
    public void onStatus(Status status) {
        processStatus(status, null);
    }

    public void processStatus(Status status, Node retweetUser) {
        Node tweet = graphController.getGraphModel().getGraph().getNode("" + status.getId());
        if (tweet == null) {
            tweet = graphController.getGraphModel()
                    .factory()
                    .newNode("" + status.getId());
            tweet.setLabel(status.getText());
            tweet.setColor(STANDARD_COLOR_TWEET);
            tweet.setX((float) Math.random());
            tweet.setY((float) Math.random());
            graphController.getGraphModel().getGraph().addNode(tweet);
        }
        /**/

        Node user = createUser(status.getUser().getScreenName());
        createLink(user, tweet);
        // Retweet are handled later
        if (!status.isRetweet()) {
            for (UserMentionEntity mention : status.getUserMentionEntities()) {
                Node mentionNode = createUser(mention.getScreenName());
                createLink(tweet, mentionNode);
            }
        }
        for (HashtagEntity hashtag : status.getHashtagEntities()) {
            if (!Arrays.asList(track).contains(hashtag.getText().toLowerCase())) {
                Node hashtagNode = createHashtag(hashtag.getText());
                createLink(tweet, hashtagNode);
            }
        }
        for (URLEntity link : status.getURLEntities()) {
            Node linkNode = createLink(link.getExpandedURL());
            createLink(tweet, linkNode);
        }
        for (SymbolEntity symbol : status.getSymbolEntities()) {
            Node symbolNode = createSymbol(symbol.getText());
            createLink(tweet, symbolNode);
        }
        for (MediaEntity media : status.getMediaEntities()) {
            Node mediaNode = createMedia(media.getMediaURL());
            createLink(tweet, mediaNode);
        }
        if (status.getRetweetedStatus() != null) {
            processStatus(status.getRetweetedStatus(), user);
        }
        // We link to the original content to give more "weight"
        if (retweetUser != null) {
            createLink(retweetUser, user);
            createLink(retweetUser, tweet);

        }

    }

    private Node createMedia(String media) {
        Node mediaNode = graphController.getGraphModel().getGraph().getNode(media);
        if (mediaNode == null) {
            mediaNode = graphController.getGraphModel()
                    .factory()
                    .newNode(media);
            mediaNode.setLabel(media);
            mediaNode.setColor(STANDARD_COLOR_MEDIA);
            mediaNode.setX((float) Math.random());
            mediaNode.setY((float) Math.random());
            graphController.getGraphModel().getGraph().addNode(mediaNode);
        }
        return mediaNode;
    }

    private Node createSymbol(String symbol) {
        Node symbolNode = graphController.getGraphModel().getGraph().getNode("$" + symbol);
        if (symbolNode == null) {
            symbolNode = graphController.getGraphModel()
                    .factory()
                    .newNode("$" + symbol);
            symbolNode.setLabel("$" + symbol);
            symbolNode.setColor(STANDARD_COLOR_SYMBOL);
            symbolNode.setX((float) Math.random());
            symbolNode.setY((float) Math.random());
            graphController.getGraphModel().getGraph().addNode(symbolNode);
        }
        return symbolNode;
    }

    private Node createLink(String url) {

        Node linkNode = graphController.getGraphModel().getGraph().getNode(url);
        if (linkNode == null) {
            linkNode = graphController.getGraphModel()
                    .factory()
                    .newNode(url);
            linkNode.setLabel(url);
            linkNode.setColor(STANDARD_COLOR_HASHTAG);
            linkNode.setX((float) Math.random());
            linkNode.setY((float) Math.random());
            graphController.getGraphModel().getGraph().addNode(linkNode);
        }
        return linkNode;
    }

    private Node createHashtag(String hashtag) {
        hashtag = hashtag.toLowerCase();
        Node hashtagNode = graphController.getGraphModel().getGraph().getNode("#" + hashtag);
        if (hashtagNode == null) {
            hashtagNode = graphController.getGraphModel()
                    .factory()
                    .newNode("#" + hashtag);
            hashtagNode.setLabel("#" + hashtag);
            hashtagNode.setColor(STANDARD_COLOR_HASHTAG);
            hashtagNode.setX((float) Math.random());
            hashtagNode.setY((float) Math.random());
            graphController.getGraphModel().getGraph().addNode(hashtagNode);
        }
        return hashtagNode;
    }

    private Node createUser(String screenName) {
        screenName = screenName.toLowerCase();
        Node user = graphController.getGraphModel().getGraph().getNode("@" + screenName);
        if (user == null) {
            user = graphController.getGraphModel()
                    .factory()
                    .newNode("@" + screenName);
            user.setLabel("@" + screenName);
            user.setColor(STANDARD_COLOR_USER);
            user.setX((float) Math.random());
            user.setY((float) Math.random());
            graphController.getGraphModel().getGraph().addNode(user);
        }
        return user;
    }

    private void createLink(Node origin, Node target) {
        Edge link = graphController.getGraphModel()
                .getGraph()
                .getEdge(origin, target);
        if (link == null) {
            link = graphController.getGraphModel()
                    .factory()
                    .newEdge(origin, target);
            link.setWeight(1.0);
            link.setColor(Color.GRAY);
            graphController.getGraphModel().getGraph().addEdge(link);
        }
    }

    @Override
    public String getName() {
        return "Full Twitter Network";
    }

}
