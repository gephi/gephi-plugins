/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.totetmatt.gephi.twitter.networklogics;


import com.twitter.clientlib.model.FilteredStreamingTweetResponse;
import com.twitter.clientlib.model.FilteredStreamingTweetResponseMatchingRules;
import com.twitter.clientlib.model.FullTextEntities;
import com.twitter.clientlib.model.HashtagEntity;
import com.twitter.clientlib.model.Tweet;
import java.awt.Color;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author totetmatt
 */
@ServiceProvider(service = Networklogic.class)
public class HashtagNetwork extends Networklogic {

    @Override
    public String getName() {
        return "Hashtag Network";
    }

    @Override
    public int index() {
        return 2;
    }

    private void generateHashtags(Tweet tweetData, List<FilteredStreamingTweetResponseMatchingRules> rules) {
        long currentMillis = System.currentTimeMillis();
        if (tweetData != null) {
            FullTextEntities entities = tweetData.getEntities();
            if (entities != null) {
                List<HashtagEntity> hashtags = entities.getHashtags();
                if (hashtags != null) {
                    for (HashtagEntity hashtag1 : hashtags) {
                        String strHashtag1 = hashtag1.getTag().toLowerCase();
                        if (this.rulesTokenized.contains(strHashtag1)) {
                            continue;
                        }
                        for (HashtagEntity hashtag2 : hashtags) {
                            String strHashtag2 = hashtag2.getTag().toLowerCase();
                            if (this.rulesTokenized.contains(strHashtag2)) {
                                continue;
                            }
                            if (!strHashtag2.equals(strHashtag1)) {
                                Node nodeHashtag1 = this.createHashtag(strHashtag1, rules);
                                Node nodeHashtag2 = this.createHashtag(strHashtag2, rules);
                                nodeHashtag1.addTimestamp(currentMillis);
                                nodeHashtag2.addTimestamp(currentMillis);
                                Edge link = graphModel.getGraph().getEdge(nodeHashtag1, nodeHashtag2);
                                if (link == null) {
                                    link = graphModel.factory().newEdge(nodeHashtag1, nodeHashtag2, false);
                                    link.setWeight(1.0);
                                    link.setColor(Color.GRAY);
                                    graphModel.getGraph().addEdge(link);
                                } else {
                                    link.setWeight(link.getWeight() + 1);
                                }
                                link.addTimestamp(currentMillis);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStatus(FilteredStreamingTweetResponse streamingTweet) {
        Tweet tweetData = streamingTweet.getData();
        List<FilteredStreamingTweetResponseMatchingRules> rules = streamingTweet.getMatchingRules();
        generateHashtags(tweetData, rules);
        var expansions = streamingTweet.getIncludes();
        if (expansions != null) {
            var tweets = expansions.getTweets();
            if (tweets != null) {
                for (Tweet tweet : tweets) {
                    generateHashtags(tweet, rules);
                }
            }
        }
    }
}
