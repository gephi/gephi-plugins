/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.totetmatt.gephi.twitter.utils;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author totetmatt
 */
public class TwitterApiFields {

    static public final Set<String> tweetFields = new HashSet<>();
    static public final Set<String> mediaFields = new HashSet<>();
    static public final Set<String> userFields = new HashSet<>();
    static public final Set<String> expansionsFields = new HashSet<>();

    static {
        tweetFields.add("author_id");
        tweetFields.add("id");
        tweetFields.add("created_at");
        tweetFields.add("entities");
        tweetFields.add("geo");
        tweetFields.add("in_reply_to_user_id");
        tweetFields.add("lang");
        tweetFields.add("context_annotations");

        mediaFields.add("media_key");
        mediaFields.add("preview_image_url");
        mediaFields.add("url");

        userFields.add("created_at");
        userFields.add("description");
        userFields.add("entities");
        userFields.add("id");
        userFields.add("location");
        userFields.add("name");
        userFields.add("profile_image_url");
        userFields.add("protected");
        userFields.add("url");
        userFields.add("username");
        userFields.add("verified");
        userFields.add("withheld");

        expansionsFields.add("attachments.media_keys");
        expansionsFields.add("author_id");
        expansionsFields.add("entities.mentions.username");
        expansionsFields.add("geo.place_id");
        expansionsFields.add("referenced_tweets.id");
        expansionsFields.add("referenced_tweets.id.author_id");
    }

}
