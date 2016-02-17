package totetmatt.gephi.twitter.networklogic;

import java.awt.Color;
import org.gephi.graph.api.GraphController;
import org.openide.util.Lookup;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

/**
 *
 * @author totetmatt
 */
public abstract class Networklogic implements StatusListener {
    // Track word passed on the Stream FilterQuery
    String[] track;
    
    // Basic Color code used since the beginning.
    // Not real standard but ensure same color code for all node type
    public final static Color STANDARD_COLOR_USER = new Color(0.5f,0,0);
    public final static Color STANDARD_COLOR_TWEET = new Color(0.5f,0.5f,0);
    public final static Color STANDARD_COLOR_HASHTAG = new Color(0,0.5f,0);
    public final static Color STANDARD_COLOR_MEDIA= new Color(0,0.5f,0.5f);
    public final static Color STANDARD_COLOR_URL= new Color(0,0,0.5f);
    public final static Color STANDARD_COLOR_SYMBOL = new Color(0.5f,0,0.5f);
    
    protected GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

    public Networklogic(){
       
    }
    public void setTrack(String[] track){
        this.track = track;
    }
    
    // This is call for each tweet received, it *needs* to be defined afterward.
    public abstract void onStatus(Status status) ;
    
    // This is mainly for the name in the UI.
    public abstract String getName();
    
    // Other method can be overidden for dedicated usage.
    
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

    public void onScrubGeo(long userId, long upToStatusId) {}

    public void onStallWarning(StallWarning warning) {}

    public void onException(Exception ex) {}
    
    @Override
    public String toString(){
        return this.getName();
    }
    
}
