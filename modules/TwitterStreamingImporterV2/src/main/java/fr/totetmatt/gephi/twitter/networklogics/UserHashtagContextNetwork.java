package fr.totetmatt.gephi.twitter.networklogics;

import com.twitter.clientlib.model.FilteredStreamingTweetResponse;
import com.twitter.clientlib.model.FilteredStreamingTweetResponseMatchingRules;
import fr.totetmatt.gephi.twitter.networklogics.utils.ExpansionParser;

import com.twitter.clientlib.model.FullTextEntities;
import com.twitter.clientlib.model.HashtagEntity;
import com.twitter.clientlib.model.MentionEntity;
import com.twitter.clientlib.model.Rule;
import com.twitter.clientlib.model.Tweet;
import com.twitter.clientlib.model.TweetReferencedTweets;
import com.twitter.clientlib.model.User;
import static fr.totetmatt.gephi.twitter.networklogics.utils.TwitterEdgeColumn.EDGE_HASHTAG;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.openide.util.lookup.ServiceProvider;

/**
 * Create a network from tweet based only on User -> User and create a link per
 * hashtag shared by users. Original Projection from @Bernardamus :
 * https://twitter.com/Bernardamus/status/1131334028043411456
 *
 * @author totetmatt
 */
@ServiceProvider(service = Networklogic.class)
public class UserHashtagContextNetwork extends Networklogic {

    final private Map<String, Integer> edgeTypes = new HashMap<>();

    public UserHashtagContextNetwork() {

    }

    @Override
    public void refreshGraphModel(List<Rule> rules) {
        super.refreshGraphModel(rules);
        if (graphModel.getEdgeTable().getColumn(EDGE_HASHTAG.label) == null) {
            graphModel.getEdgeTable().addColumn(EDGE_HASHTAG.label, EDGE_HASHTAG.classType, Origin.DATA);
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

    private void createProjection(Tweet tweetData, ExpansionParser.ExpansionData expansionData, List<FilteredStreamingTweetResponseMatchingRules> rules) {

        if (tweetData != null) {
            FullTextEntities entities = tweetData.getEntities();
            if (entities != null) {
                var hashtags = entities.getHashtags();
                var mentions = entities.getMentions();
                if (hashtags != null && mentions != null) {
                    Map<Integer, String> hashtagEdges = new HashMap<>();
                    Set<Node> mentionNodes = new HashSet<>();
                    for (HashtagEntity hashtagEntity : hashtags) {
                        String hashtag = "#" + hashtagEntity.getTag().toLowerCase();
                        int edgeType = getOrCreateType(hashtag);
                        hashtagEdges.put(edgeType, hashtag);
                    }

                    User authorUser = expansionData.getUserIdToUser().get(tweetData.getAuthorId());
                    if (authorUser != null) {
                        Node authorNode = createUser(authorUser, rules);
                        authorNode.addTimestamp(expansionData.getCurrentMillis());
                        for (MentionEntity mention : mentions) {

                            var user = expansionData.getUserIdToUser().get(mention.getId());
                            Node mentionNode;
                            if (user != null) {
                                mentionNode = createUser(user, rules);
                            } else {
                                mentionNode = createUser(mention.getId(), mention.getUsername(), rules);
                            }
                            mentionNode.addTimestamp(expansionData.getCurrentMillis());
                            mentionNodes.add(mentionNode);
                        }

                        for (Node mentionNode : mentionNodes) {
                            if (mentionNode.getId() != authorNode.getId()) {
                                for (var edgeType : hashtagEdges.entrySet()) {
                                    createLink(authorNode, mentionNode, edgeType.getKey(), edgeType.getValue(), expansionData.getCurrentMillis());
                                }
                            }
                        }

                        // Ref
                        List<TweetReferencedTweets> referencedTweets = tweetData.getReferencedTweets();
                        if (referencedTweets != null) {
                            for (TweetReferencedTweets referencedTweet : referencedTweets) {
                                Tweet refTweet = expansionData.getTweetIdToTweet().get(referencedTweet.getId());
                                if (refTweet != null) {

                                    User refAuthor = expansionData.getUserIdToUser().get(refTweet.getAuthorId());
                                    if (refAuthor != null) { // Could be redondant, but emphasis the Retweet / Reply
                                        Node refAuthorNode = createUser(refAuthor, rules);
                                        if (refAuthorNode.getId() != authorNode.getId()) {
                                            for (var edgeType : hashtagEdges.entrySet()) {
                                                createLink(authorNode, refAuthorNode, edgeType.getKey(), edgeType.getValue(), expansionData.getCurrentMillis());
                                            }
                                        }
                                    }
                                    createProjection(refTweet, expansionData, rules);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStatus(FilteredStreamingTweetResponse streamingTweet) {
        ExpansionParser.ExpansionData expansionData = ExpansionParser.parse(streamingTweet.getIncludes());
        List<FilteredStreamingTweetResponseMatchingRules> rules = streamingTweet.getMatchingRules();

        Tweet tweetData = streamingTweet.getData();
        createProjection(tweetData, expansionData, rules);

    }

    private void createLink(Node origin, Node target, int type, String hashtag, long currentMillis) {
        Edge edge = graphModel.getGraph().getEdge(origin, target, type);
        if (edge == null) { // If no, create it
            edge = graphModel
                    .factory()
                    .newEdge(origin, target, type, true);
            edge.setWeight(1.0);
            edge.setColor(Color.GRAY);
            edge.setLabel(hashtag);
            edge.setAttribute(EDGE_HASHTAG.label, hashtag);
            graphModel.getGraph().addEdge(edge);
        } else { // If yes, increment the weight
            edge.setWeight(edge.getWeight() + 1);
        }
        edge.addTimestamp(currentMillis);
    }

    @Override
    public String getName() {
        return "Bernardamus Projection";
    }

    @Override
    public int index() {
        return 10;
    }

}
