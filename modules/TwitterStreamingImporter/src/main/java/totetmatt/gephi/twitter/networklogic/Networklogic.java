package totetmatt.gephi.twitter.networklogic;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
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
    
    protected GraphModel graphModel;
    
    public Networklogic(){
       
    }
    public void setTrack(String[] track){
        this.track = track;
    }
    
    // Used to keep reference to the "current" workspace
    // Should be called before a new stream
    public void refreshGraphModel(){
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    }
    // This is call for each tweet received, it *needs* to be defined afterward.
    public final void onStatus(Status status) {
        try {
            graphModel.getGraph().writeLock();
            processStatus(status);  
        } catch (Exception e){
            Logger.getLogger(Networklogic.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graphModel.getGraph().writeUnlock();  
        }
    }
    public abstract void processStatus(Status status);
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
