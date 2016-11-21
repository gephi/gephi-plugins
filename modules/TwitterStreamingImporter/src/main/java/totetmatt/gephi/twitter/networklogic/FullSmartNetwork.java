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

    private int HAS_MENTION;
    private int RETWEETS;
    private int RETWEETS_FROM;
    private int HAS_HASHTAG;
    private int HAS_MEDIA;
    private int HAS_LINK;
    private int HAS_SYMBOL;
    private int TWEETS;

    public FullSmartNetwork() {

    }

    @Override
    public void refreshGraphModel() {
        super.refreshGraphModel();
        HAS_MENTION = graphModel.addEdgeType("Has_mention");
        RETWEETS = graphModel.addEdgeType("Retweets");
        RETWEETS_FROM = graphModel.addEdgeType("Retweets_from");
        HAS_HASHTAG = graphModel.addEdgeType("Has_hashtag");
        HAS_MEDIA = graphModel.addEdgeType("Has_media");
        HAS_LINK = graphModel.addEdgeType("Has_link");
        HAS_SYMBOL = graphModel.addEdgeType("Has_symbol");
        TWEETS = graphModel.addEdgeType("Tweets");
    }

    @Override
    public void processStatus(Status status) {
        processStatus(status, null);
    }

    public void processStatus(Status status, Node retweetUser) {
        Node tweet = createTweet(status);

        Node user = createUser(status.getUser().getScreenName());
        
        createLink(user, tweet, TWEETS);
        // Retweet are handled later
        if (!status.isRetweet()) {
            for (UserMentionEntity mention : status.getUserMentionEntities()) {
                Node mentionNode = createUser(mention.getScreenName());
                createLink(tweet, mentionNode, HAS_MENTION);
            }
        }
        
        for (HashtagEntity hashtag : status.getHashtagEntities()) {
            if (!Arrays.asList(track).contains(hashtag.getText().toLowerCase())) {
                Node hashtagNode = createHashtag(hashtag.getText());
                createLink(tweet, hashtagNode, HAS_HASHTAG);
            }
        }
        
        for (URLEntity link : status.getURLEntities()) {
            if(!link.getExpandedURL().isEmpty()){
                Node linkNode = createUrl(link.getExpandedURL());
                createLink(tweet, linkNode, HAS_LINK);
            }
        }
        
        for (SymbolEntity symbol : status.getSymbolEntities()) {
            Node symbolNode = createSymbol(symbol.getText());
            createLink(tweet, symbolNode, HAS_SYMBOL);
        }
        
        for (MediaEntity media : status.getMediaEntities()) {
            Node mediaNode = createMedia(media.getMediaURL());
            createLink(tweet, mediaNode, HAS_MEDIA);
        }
        
        if (status.getRetweetedStatus() != null) {
            processStatus(status.getRetweetedStatus(), user);
        }
        
        // We link to the original content to give more "weight"
        if (retweetUser != null) {
            createLink(retweetUser, user, RETWEETS_FROM);
            createLink(retweetUser, tweet, RETWEETS);
        }
    }
    
    private void createLink(Node origin, Node target, int type) {
        Edge link = graphModel.getGraph().getEdge(origin, target, type);
        if (link == null) {
            link = graphModel.factory().newEdge(origin, target, type, true);
            link.setWeight(1.0);
            link.setColor(Color.GRAY);
            graphModel.getGraph().addEdge(link);
        }
    }

    @Override
    public String getName() {
        return "Full Twitter Network";
    }

}
