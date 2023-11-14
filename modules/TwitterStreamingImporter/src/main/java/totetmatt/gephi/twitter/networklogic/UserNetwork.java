package totetmatt.gephi.twitter.networklogic;

import java.awt.Color;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

/**
 * Create a network from tweet based only on User -> User If there is multiple
 * time the same link between users, the weight of the edge is incremented
 *
 * @author totetmatt
 */
@ServiceProvider(service = Networklogic.class)
public class UserNetwork extends Networklogic {

    private int MENTION;
    private int RETWEET;
    private int QUOTE;

    public UserNetwork() {
     
    }

    @Override
    public void refreshGraphModel(){
        super.refreshGraphModel();
        MENTION = graphModel.addEdgeType("Mention");
        RETWEET = graphModel.addEdgeType("Retweet");
        QUOTE   = graphModel.addEdgeType("Quote");
    }
    
    @Override
    public void processStatus(Status status) {
        long currentMillis = System.currentTimeMillis();
        // Mentions
        if(status.getUserMentionEntities().length > 0) { 
            Node origin = createUser(status.getUser());
            for (UserMentionEntity mention : status.getUserMentionEntities()) {

                // Avoid self-loop
                if (!status.getUser().getScreenName().equals(mention.getScreenName())) {
                    Node target = createUser(mention);

                    origin.addTimestamp(currentMillis);
                    target.addTimestamp(currentMillis);
                    // Check if there is already an edge for the nodes
                    createLink(origin, target, MENTION, currentMillis);   
                }
            }
        }
        
        // Manage Quoted Status
        if(status.getQuotedStatus() != null && 
           // Avoid self-loop
           !status.getUser().getScreenName().equals(status.getQuotedStatus().getUser().getScreenName()) ) {
            
            Node origin = createUser(status.getUser());
            Node target = createUser(status.getQuotedStatus().getUser());
            createLink(origin, target, QUOTE, System.currentTimeMillis());
            onStatus(status.getQuotedStatus());
        }
        
        //Manage Retweeted
        if (status.getRetweetedStatus() != null && 
            // Avoid self-loop
            !status.getUser().getScreenName().equals(status.getRetweetedStatus().getUser().getScreenName()) ) {
            
            Node origin = createUser(status.getUser());
            Node target = createUser(status.getRetweetedStatus().getUser());
            createLink(origin, target, RETWEET, System.currentTimeMillis());
            onStatus(status.getRetweetedStatus());
        } 

    }
    
    private void createLink(Node origin, Node target, int type,long currentMillis) {
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
