package totetmatt.gephi.twitter.networklogic;

import java.awt.Color;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.joda.time.LocalTime;
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
        processStatus(status,false);
    }
    public void processStatus(Status status,boolean isQuote) {
        long currentMillis = LocalTime.now().toDateTimeToday().getMillis();
        // get the original user from the tweet
        String originScreenName = status.getUser().getScreenName().toLowerCase();

        // For each mention
        for (UserMentionEntity mention : status.getUserMentionEntities()) {

            String targetScreenName = mention.getScreenName().toLowerCase();
            // Avoid self-loop
            if (!originScreenName.equals(targetScreenName)) {

                // Create or get the nodes
                Node origin = createUser(status.getUser());
                Node target = createUser(mention);
                origin.addTimestamp(currentMillis);
                target.addTimestamp(currentMillis);
                
                int typeEdge;
                if(!isQuote){
                    if (status.isRetweet()) {
                        typeEdge = RETWEET;
                    } else {
                        typeEdge = MENTION;
                    }
                } else {
                    typeEdge = QUOTE;
                }
                // Check if there is already an edge for the nodes
                Edge mentionEdge = graphModel.getGraph().getEdge(origin, target, typeEdge);
                
                if (mentionEdge == null) { // If no, create it
                    mentionEdge = graphModel
                            .factory()
                            .newEdge(origin, target, typeEdge, true);
                    mentionEdge.setWeight(1.0);
                    mentionEdge.setColor(Color.GRAY);
                    graphModel.getGraph().addEdge(mentionEdge);
                } else { // If yes, increment the weight
                    mentionEdge.setWeight(mentionEdge.getWeight() + 1);
                }
                mentionEdge.addTimestamp(currentMillis);
                if (status.getRetweetedStatus() != null) {
                    onStatus(status.getRetweetedStatus());
                }
                if(status.getQuotedStatus() != null) {   
                    processStatus(status.getQuotedStatus(),true);
                }
            }

        }

    }

    @Override
    public String getName() {
        return "User Network";
    }

}
