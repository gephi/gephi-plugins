package fr.totetmatt.gephi.twitter.networklogics.utils;

/**
 *
 * @author totetmatt
 */
public enum TwitterNodeColumn {
    NODE_TYPE("twitter_type", String.class),
    NODE_TWEET_GEO_LATITUDE("lat", Double.class),
    NODE_TWEET_GEO_LONGITUDE("lng", Double.class),
    NODE_TWEET_PLACE_COUNTRY("place_country", String.class),
    NODE_TWEET_PLACE_ID("place_id", String.class),
    NODE_TWEET_PLACE_TYPE("place_type", String.class),
    NODE_TWEET_PLACE_COORD_JSON("place_coord_json", String.class),
    NODE_TWEET_PLACE_NAME("place_name", String.class),
    NODE_CREATED_AT("created_at", String.class),
    NODE_LANG("lang", String.class),
    NODE_CONVERSATION_ID("conversation_id", String.class),
    NODE_POSSIBLY_SENSITIVE("possibly_sensitive", Boolean.class),
    NODE_QUOTED_STATUS_PERMALINK("quoted_status_permalink", String.class),
    NODE_USER_DESCRIPTION("description", String.class),
    NODE_USER_EMAIL("email", String.class),
    NODE_USER_PROFILE_IMAGE("profile_image", String.class),
    NODE_USER_FRIENDS_COUNT("friends_count", Integer.class),
    NODE_USER_FOLLOWERS_COUNT("followers_count", Integer.class),
    NODE_USER_REAL_NAME("real_name", String.class),
    NODE_USER_LOCATION("location", String.class),
    NODE_MEDIA_TYPE("media_type", String.class),
    NODE_EMOJI_ALIAS("emoji_alias", String.class),
    NODE_EMOJI_HTML_DECIMAL("emoji_html_decimal", String.class),
    NODE_EMOJI_UTF8("emoji_utf8", String.class);

    final public String label;
    final public Class<?> classType;

    private TwitterNodeColumn(String label, Class<?> classType) {
        this.label = label;
        this.classType = classType;
    }

}
