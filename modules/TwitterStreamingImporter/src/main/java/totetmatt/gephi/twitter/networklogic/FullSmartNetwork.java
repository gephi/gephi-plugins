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
    private int QUOTES;
    private int QUOTES_FROM;

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
        QUOTES = graphModel.addEdgeType("Quotes");
        QUOTES_FROM = graphModel.addEdgeType("Quotes_from");
    }

    @Override
    public void processStatus(Status status) {
        processStatus(status, null,-1);
    }

    public void processStatus(Status status, Node retweetUser,int link_kind) {
        long currentMillis = System.currentTimeMillis();
        
        Node tweet = createTweet(status);
        tweet.addTimestamp(currentMillis);
        
        Node user = createUser(status.getUser());
        user.addTimestamp(currentMillis);
        createLink(user, tweet, TWEETS,currentMillis);
   
        // Retweet are handled later
        if (!status.isRetweet()) {
            for (UserMentionEntity mention : status.getUserMentionEntities()) {
                Node mentionNode = createUser(mention);
                mentionNode.addTimestamp(currentMillis);
                createLink(tweet, mentionNode, HAS_MENTION,currentMillis);
            }
        }
        
        for (HashtagEntity hashtag : status.getHashtagEntities()) {
            if (!Arrays.asList(track).contains(hashtag.getText().toLowerCase())) {
                Node hashtagNode = createHashtag(hashtag.getText());
                hashtagNode.addTimestamp(currentMillis);
                createLink(tweet, hashtagNode, HAS_HASHTAG,currentMillis);
            }
        }
        
        for (URLEntity link : status.getURLEntities()) {
            if(!link.getExpandedURL().isEmpty()){
                Node linkNode = createUrl(link.getExpandedURL());
                linkNode.addTimestamp(currentMillis);
                createLink(tweet, linkNode, HAS_LINK,currentMillis);
            }
        }
        
        for (SymbolEntity symbol : status.getSymbolEntities()) {
            Node symbolNode = createSymbol(symbol.getText());
            symbolNode.addTimestamp(currentMillis);
            createLink(tweet, symbolNode, HAS_SYMBOL,currentMillis);
        }
    
        for (MediaEntity media : status.getMediaEntities()) {
            Node mediaNode = createMedia(media.getMediaURL());
            mediaNode.addTimestamp(currentMillis);
            createLink(tweet, mediaNode, HAS_MEDIA,currentMillis);
        }
        
        if (status.getRetweetedStatus() != null) {
            processStatus(status.getRetweetedStatus(), user,RETWEETS);
        }
        
        if(status.getQuotedStatus() != null) {
            processStatus(status.getQuotedStatus(), user,QUOTES);
        }
        
        // The idea here is to bring the retweet / quote link to the original content
        if (retweetUser != null && retweetUser != user) {
            if(link_kind == RETWEETS) {
                createLink(retweetUser, user, RETWEETS_FROM,currentMillis);
                createLink(retweetUser, tweet, RETWEETS,currentMillis);
            } else if (link_kind == QUOTES) {
                createLink(retweetUser, user, QUOTES_FROM,currentMillis);
                createLink(retweetUser, tweet, QUOTES,currentMillis);
            }
        }
    }
    
    private void createLink(Node origin, Node target, int type,double timestamp) {
        Edge link = graphModel.getGraph().getEdge(origin, target, type);
        if (link == null) {
            link = graphModel.factory().newEdge(origin, target, type, true);
            link.setWeight(1.0);
            link.setColor(Color.GRAY);
            graphModel.getGraph().addEdge(link);
        }
        link.addTimestamp(timestamp);
    }

    @Override
    public String getName() {
        return "Full Twitter Network";
    }

    @Override
    public int index() {
        return 0;
    }

}
