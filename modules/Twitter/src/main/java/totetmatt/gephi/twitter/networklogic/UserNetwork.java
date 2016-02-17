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

    static private int MENTION = 1;
    static private int RETWEET = 2;

    public UserNetwork() {

    }

    @Override
    public void onStatus(Status status) {
        // get the original user from the tweet
        String originScreenName = status.getUser().getScreenName().toLowerCase();

        // For each mention
        for (UserMentionEntity mention : status.getUserMentionEntities()) {

            String targetScreenName = mention.getScreenName().toLowerCase();
            // Avoid self-loop
            if (!originScreenName.equals(targetScreenName)) {

                // Create or get the nodes
                Node origin = graphController.getGraphModel().getGraph().getNode("@" + originScreenName);
                if (origin == null) {
                    origin = graphController.getGraphModel()
                            .factory()
                            .newNode("@" + originScreenName);
                    origin.setLabel("@" + originScreenName);
                    origin.setColor(STANDARD_COLOR_USER);
                    origin.setX((float) Math.random());
                    origin.setY((float) Math.random());

                    graphController.getGraphModel().getGraph().addNode(origin);
                }

                Node target = graphController.getGraphModel().getGraph().getNode("@" + targetScreenName);
                if (target == null) {
                    target = graphController.getGraphModel()
                            .factory()
                            .newNode("@" + targetScreenName);
                    target.setLabel("@" + targetScreenName);
                    target.setColor(STANDARD_COLOR_USER);
                    target.setX((float) Math.random());
                    target.setY((float) Math.random());
                    graphController.getGraphModel().getGraph().addNode(target);
                }
                int typeEdge;
                if (status.isRetweet()) {
                    typeEdge = RETWEET;
                } else {
                    typeEdge = MENTION;
                }
                // Check if there is already an edge for the nodes
                Edge mentionEdge = graphController.getGraphModel()
                        .getGraph()
                        .getEdge(origin, target, typeEdge);

                if (mentionEdge == null) { // If no, create it
                    mentionEdge = graphController.getGraphModel()
                            .factory()
                            .newEdge(origin, target, typeEdge, true);
                    mentionEdge.setWeight(1.0);
                    mentionEdge.setColor(Color.GRAY);
                    graphController.getGraphModel().getGraph().addEdge(mentionEdge);
                } else { // If yes, increment the weight
                    mentionEdge.setWeight(mentionEdge.getWeight() + 1);
                }
                if (status.getRetweetedStatus() != null) {
                    onStatus(status.getRetweetedStatus());
                }
            }

        }

    }

    @Override
    public String getName() {
        return "User Network";
    }

}
