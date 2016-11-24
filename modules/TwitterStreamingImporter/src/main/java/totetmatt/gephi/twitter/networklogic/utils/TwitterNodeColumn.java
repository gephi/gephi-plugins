/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package totetmatt.gephi.twitter.networklogic.utils;

import java.util.HashMap;

/**
 *
 * @author totetmatt
 */
public class TwitterNodeColumn {
    final public String label;
    final public Class<?> classType;

    private TwitterNodeColumn(String label, Class<?> classType) {
        this.label = label;
        this.classType = classType;
    }
    static public final String label(String key){
        return AllColumns.get(key).label;
    }
    static public final HashMap<String,TwitterNodeColumn> AllColumns;
    static {
        AllColumns = new HashMap<>();
        
        AllColumns.put("NODE_TYPE", new TwitterNodeColumn("twitter_type",String.class));
        AllColumns.put("NODE_TWEET_GEO_LATITUDE", new TwitterNodeColumn("lat",Double.class));
        AllColumns.put("NODE_TWEET_GEO_LONGITUDE", new TwitterNodeColumn("lng",Double.class));
        AllColumns.put("NODE_CREATED_AT", new TwitterNodeColumn("created_at",String.class));
        AllColumns.put("NODE_LANG", new TwitterNodeColumn("lang",String.class));
        
        AllColumns.put("NODE_USER_DESCRIPTION", new TwitterNodeColumn("description",String.class));
        AllColumns.put("NODE_USER_EMAIL", new TwitterNodeColumn("email",String.class));
        AllColumns.put("NODE_USER_PROFILE_IMAGE", new TwitterNodeColumn("profile_image",String.class));
        AllColumns.put("NODE_USER_FRIENDS_COUNT", new TwitterNodeColumn("friends_count",Integer.class));
        AllColumns.put("NODE_USER_FOLLOWERS_COUNT", new TwitterNodeColumn("followers_count",Integer.class));
        AllColumns.put("NODE_USER_REAL_NAME", new TwitterNodeColumn("real_name",String.class));
        AllColumns.put("NODE_USER_LOCATION", new TwitterNodeColumn("location",String.class));
    }
 
}
