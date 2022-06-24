/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.totetmatt.gephi.twitter.networklogics.utils;

import com.twitter.clientlib.model.Expansions;
import com.twitter.clientlib.model.Tweet;
import com.twitter.clientlib.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author totetmatt
 */
public class ExpansionParser {

    static public class ExpansionData {

        final private Map<String, User> userIdToUser;
        final private Map<String, Tweet> tweetIdToTweet;
        final long currentMillis;

        public ExpansionData(Map<String, User> userIdToUser, Map<String, Tweet> tweetIdToTweet) {
            this.userIdToUser = userIdToUser;
            this.tweetIdToTweet = tweetIdToTweet;
            this.currentMillis = System.currentTimeMillis();
        }

        public Map<String, User> getUserIdToUser() {
            return userIdToUser;
        }

        public Map<String, Tweet> getTweetIdToTweet() {
            return tweetIdToTweet;
        }

        public long getCurrentMillis() {
            return currentMillis;
        }

    }

    static public ExpansionData parse(Expansions expansions) {
        Map<String, User> userIdToUser = new HashMap<>();
        Map<String, Tweet> tweetIdToTweet = new HashMap<>();
        if (expansions != null) {
            List<User> users = expansions.getUsers();
            if (users != null) {
                for (User user : users) {
                    userIdToUser.put(user.getId(), user);
                }
            }
            var tweets = expansions.getTweets();
            if (tweets != null) {
                for (var tweet : tweets) {
                    tweetIdToTweet.put(tweet.getId(), tweet);
                }
            }
        }
        return new ExpansionData(userIdToUser, tweetIdToTweet);
    }

}
