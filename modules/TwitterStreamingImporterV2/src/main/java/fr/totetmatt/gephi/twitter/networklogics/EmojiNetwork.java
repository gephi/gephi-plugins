package fr.totetmatt.gephi.twitter.networklogics;

import com.twitter.clientlib.model.FilteredStreamingTweetResponse;
import com.twitter.clientlib.model.FilteredStreamingTweetResponseMatchingRules;
import com.twitter.clientlib.model.Rule;
import com.twitter.clientlib.model.Tweet;
import com.vdurmont.emoji.EmojiParser;
import java.awt.Color;
import java.util.List;
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
public class EmojiNetwork extends Networklogic {

    private int TWEET;

    public EmojiNetwork() {

    }

    @Override
    public void refreshGraphModel(List<Rule> rules) {
        super.refreshGraphModel(rules);
        TWEET = graphModel.addEdgeType("Emoji");
    }

    @Override
    public void onStatus(FilteredStreamingTweetResponse streamingTweet) {
        long currentMillis = System.currentTimeMillis();
        Tweet tweetData = streamingTweet.getData();
        List<FilteredStreamingTweetResponseMatchingRules> rules = streamingTweet.getMatchingRules();
        if (tweetData != null) {
            List<String> emojis = EmojiParser.extractEmojis(tweetData.getText());
            for (String s : emojis) {
                for (String t : emojis) {
                    if (!s.equals(t)) {
                        Node source = createEmoji(s, rules);
                        Node target = createEmoji(t, rules);
                        createLink(source, target, TWEET, currentMillis);
                    }
                }
            }
        }
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
        return "Emoji Network";
    }

    @Override
    public int index() {
        return 3;
    }
}
