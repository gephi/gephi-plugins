package fr.totetmatt.gephi.twitter.networklogics;

import com.twitter.clientlib.model.FilteredStreamingTweetResponse;
import fr.totetmatt.gephi.twitter.networklogics.utils.ExpansionParser;
import com.twitter.clientlib.model.FilteredStreamingTweetResponseMatchingRules;
import com.twitter.clientlib.model.FullTextEntities;
import com.twitter.clientlib.model.MentionEntity;
import com.twitter.clientlib.model.Rule;
import com.twitter.clientlib.model.Tweet;
import com.twitter.clientlib.model.TweetReferencedTweets;
import com.twitter.clientlib.model.User;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 * Create a network from tweet based only on User -> User If there is multiple
 * time the same link between users, the weight of the edge is incremented
 *
 * @author totetmatt
 */
@ServiceProvider(service = Networklogic.class)
public class UserNetwork extends Networklogic {

    private int MENTION;
    final private Map<String, Integer> edgeTypes = new HashMap<>();

    private int getOrCreateType(String hashtag) {
        if (edgeTypes.containsKey(hashtag)) {
            return edgeTypes.get(hashtag);
        } else {
            int newType = graphModel.addEdgeType(hashtag);
            edgeTypes.put(hashtag, newType);
            return newType;
        }
    }

    public UserNetwork() {

    }

    @Override
    public void refreshGraphModel(List<Rule> rules) {
        super.refreshGraphModel(rules);
        MENTION = graphModel.addEdgeType("mention");
    }

    private void generateUsers(Tweet tweetData, ExpansionParser.ExpansionData expansionData, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        long currentMillis = System.currentTimeMillis();
        if (tweetData != null) {
            var authorId = tweetData.getAuthorId();
            User author = expansionData.getUserIdToUser().get(authorId);
            if (author != null) {
                Node authorNode = createUser(author, rules);
                authorNode.addTimestamp(currentMillis);
                FullTextEntities entities = tweetData.getEntities();
                if (entities != null) {
                    List<MentionEntity> mentions = entities.getMentions();
                    if (mentions != null) {
                        for (MentionEntity mention : mentions) {
                            User mentionUser = expansionData.getUserIdToUser().get(mention.getId());
                            Node mentionNode;
                            if (mentionUser != null) {
                                mentionNode = createUser(mentionUser, rules);
                            } else {
                                mentionNode = createUser(mention.getId(), mention.getUsername(), rules);
                            }
                            mentionNode.addTimestamp(currentMillis);
                            if (authorNode.getId() != mentionNode.getId()) {
                                createLink(authorNode, mentionNode, MENTION, currentMillis);
                            }
                        }
                    }
                }
                List<TweetReferencedTweets> referenceTweets = tweetData.getReferencedTweets();
                if (referenceTweets != null) {
                    for (var refTweet : referenceTweets) {
                        
                        Tweet tweet = expansionData.getTweetIdToTweet().get(refTweet.getId());
                        if(tweet == null) continue; // I guess expansion doesn't include a whole conversation. Therefore sometime it will be null
                        User refUser = expansionData.getUserIdToUser().get(tweet.getAuthorId());
                        Node refUserNode;
                        if (refUser != null) {
                            refUserNode = createUser(refUser, rules);
                            refUserNode.addTimestamp(currentMillis);
                            if (authorNode.getId() != refUserNode.getId()) {
                                int edgeType = getOrCreateType(refTweet.getType().getValue());
                                createLink(authorNode, refUserNode, edgeType, currentMillis);
                            }
                            generateUsers(tweet, expansionData, rules);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStatus(FilteredStreamingTweetResponse streamingTweet) {

        List<FilteredStreamingTweetResponseMatchingRules> rules = streamingTweet.getMatchingRules();
        ExpansionParser.ExpansionData expansionData = ExpansionParser.parse(streamingTweet.getIncludes());

        Tweet tweetData = streamingTweet.getData();
        generateUsers(tweetData, expansionData, rules);

    }

    private void createLink(Node origin, Node target, int type, long currentMillis) {
        Edge edge = graphModel.getGraph().getEdge(origin, target, type);
        if (edge == null) { // If no, create it
            edge = graphModel
                    .factory()
                    .newEdge(origin, target, type, true);
            edge.setWeight(1.0);
            edge.setColor(Color.GRAY);
            graphModel.getGraph().addEdge(edge);
        } else { // If yes, increment the weight
            edge.setWeight(edge.getWeight() + 1);
        }
        edge.addTimestamp(currentMillis);
    }

    @Override
    public String getName() {
        return "User Network";
    }

    @Override
    public int index() {
        return 1;
    }

}
