package fr.totetmatt.gephi.twitter.networklogics;

import fr.totetmatt.gephi.twitter.networklogics.utils.ExpansionParser;
import com.twitter.clientlib.model.CashtagEntity;
import com.twitter.clientlib.model.FilteredStreamingTweetResponse;
import com.twitter.clientlib.model.FilteredStreamingTweetResponseMatchingRules;
import com.twitter.clientlib.model.FullTextEntities;
import com.twitter.clientlib.model.HashtagEntity;
import com.twitter.clientlib.model.MentionEntity;
import com.twitter.clientlib.model.Rule;
import com.twitter.clientlib.model.Tweet;
import com.twitter.clientlib.model.UrlEntity;
import com.twitter.clientlib.model.User;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author totetmatt
 */
@ServiceProvider(service = Networklogic.class)
public class FullSmartNetwork extends Networklogic {

    private int MENTION;
    private int HAS_HASHTAG;
    private int HAS_LINK;
    private int HAS_SYMBOL;
    private int TWEETS;
    private int IN_REPLY_TO;

    final private Map<String, Integer> edgeTypes = new HashMap<>();

    public FullSmartNetwork() {

    }

    @Override
    public void refreshGraphModel(List<Rule> rules) {
        super.refreshGraphModel(rules);
        MENTION = graphModel.addEdgeType("mention");
        HAS_HASHTAG = graphModel.addEdgeType("has_hashtag");
        HAS_LINK = graphModel.addEdgeType("has_link");
        HAS_SYMBOL = graphModel.addEdgeType("has_symbol");
        TWEETS = graphModel.addEdgeType("tweets");
        IN_REPLY_TO = graphModel.addEdgeType("in_reply_to");
    }

    private void generateHashtags(FullTextEntities entities, Node tweet, ExpansionParser.ExpansionData expansionData, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        if (entities.getHashtags() != null) {
            List<HashtagEntity> hashtags = entities.getHashtags();
            if (hashtags != null) {
                for (HashtagEntity hashtagEntity : hashtags) {
                    if (this.rulesTokenized.contains(hashtagEntity.getTag().toLowerCase())) {
                        continue;
                    }
                    Node hashtag = createHashtag(hashtagEntity.getTag(), rules);
                    hashtag.addTimestamp(expansionData.getCurrentMillis());
                    createLink(tweet, hashtag, HAS_HASHTAG, expansionData.getCurrentMillis());
                }
            }
        }
    }

    private void generateCashtags(FullTextEntities entities, Node tweet, ExpansionParser.ExpansionData expansionData, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        if (entities.getCashtags() != null) {
            List<CashtagEntity> cashtags = entities.getCashtags();
            if (cashtags != null) {
                for (CashtagEntity cashtagEntity : cashtags) {
                    Node symbolNode = createSymbol(cashtagEntity.getTag(), rules);
                    symbolNode.addTimestamp(expansionData.getCurrentMillis());
                    createLink(tweet, symbolNode, HAS_SYMBOL, expansionData.getCurrentMillis());
                }
            }
        }
    }

    private void generateUrls(FullTextEntities entities, Node tweet, ExpansionParser.ExpansionData expansionData, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        var urls = entities.getUrls();
        if (urls != null) {
            for (UrlEntity link : urls) {
                Node linkNode = createUrl(link.getExpandedUrl().toString(), rules);
                linkNode.addTimestamp(expansionData.getCurrentMillis());
                createLink(tweet, linkNode, HAS_LINK, expansionData.getCurrentMillis());
            }
        }
    }

    private void generateMentions(FullTextEntities entities, Node tweet, ExpansionParser.ExpansionData expansionData, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        List<MentionEntity> mentions = entities.getMentions();
        if (mentions != null) {
            for (MentionEntity mention : mentions) {
                User user = expansionData.getUserIdToUser().get(mention.getId());
                Node userNode;
                if (user == null) {
                    userNode = createUser(mention.getId(), mention.getUsername(), rules);
                } else {
                    userNode = createUser(user, rules);
                }
                userNode.addTimestamp(expansionData.getCurrentMillis());
                createLink(tweet, userNode, MENTION, expansionData.getCurrentMillis());
            }
        }
    }

    private int getOrCreateType(String hashtag) {
        if (edgeTypes.containsKey(hashtag)) {
            return edgeTypes.get(hashtag);
        } else {
            int newType = graphModel.addEdgeType(hashtag);
            edgeTypes.put(hashtag, newType);
            return newType;
        }
    }

    private void createTweet(Tweet tweetData, ExpansionParser.ExpansionData expansionData, List<FilteredStreamingTweetResponseMatchingRules> rules) {

        Node tweet = createTweet(tweetData, rules);
        tweet.addTimestamp(expansionData.getCurrentMillis());

        FullTextEntities entities = tweetData.getEntities();
        if (entities != null) {
            // Hashtags
            generateHashtags(entities, tweet, expansionData, rules);
            // Cashtags
            generateCashtags(entities, tweet, expansionData, rules);
            //Url
            generateUrls(entities, tweet, expansionData, rules);
            //Mentions 
            generateMentions(entities, tweet, expansionData, rules);

        }
        User authorUser = expansionData.getUserIdToUser().get(tweetData.getAuthorId());
        Node authorNode;
        if (authorUser != null) {
            authorNode = createUser(authorUser, rules);
        } else {
            authorNode = createUser(tweetData.getAuthorId(), tweetData.getAuthorId(), rules);
        }
        authorNode.addTimestamp(expansionData.getCurrentMillis());
        createLink(authorNode, tweet, TWEETS, expansionData.getCurrentMillis());
        String replyToUserId = tweetData.getInReplyToUserId();
        if (replyToUserId != null) {
            User replyToUser = expansionData.getUserIdToUser().get(replyToUserId);
            if (replyToUser != null) {
                Node replyToUserNode = createUser(replyToUser, rules);
                replyToUserNode.addTimestamp(expansionData.getCurrentMillis());
                createLink(tweet, replyToUserNode, IN_REPLY_TO, expansionData.getCurrentMillis());
            }
        }
        var refTweets = tweetData.getReferencedTweets();
        if (refTweets != null) {
            for (var refTweet : refTweets) {
                Tweet refTweetObj = expansionData.getTweetIdToTweet().get(refTweet.getId());
                if (refTweetObj != null) {
                    User refAuthor = expansionData.getUserIdToUser().get(refTweetObj.getAuthorId());
                    if (refAuthor != null) {
                        Node refAuthorNode = createUser(refAuthor, rules);
                        refAuthorNode.addTimestamp(expansionData.getCurrentMillis());
                        if (authorNode.getId() != refAuthorNode.getId()) {
                            int edgeType = getOrCreateType(refTweet.getType().getValue());
                            createLink(authorNode, refAuthorNode, edgeType, expansionData.getCurrentMillis());
                        }
                    }
                    createTweet(refTweetObj, expansionData, rules);
                }
            }
        }
    }

    @Override
    public void onStatus(FilteredStreamingTweetResponse streamingTweet) {

        List<FilteredStreamingTweetResponseMatchingRules> rules = streamingTweet.getMatchingRules();
        ExpansionParser.ExpansionData expansionData = ExpansionParser.parse(streamingTweet.getIncludes());

        Tweet tweetData = streamingTweet.getData();
        createTweet(tweetData, expansionData, rules);

    }

    private void createLink(Node origin, Node target, int type, double timestamp) {
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
