package totetmatt.gephi.twitter.networklogic;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.openide.util.lookup.ServiceProvider;
import static totetmatt.gephi.twitter.networklogic.utils.TwitterEdgeColumn.EDGE_HASHTAG;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

/**
 * Create a network from tweet based only on User -> User and create a link per hashtag shared by users.
 * Original Projection from @Bernardamus : https://twitter.com/Bernardamus/status/1131334028043411456
 *
 * @author totetmatt
 */
@ServiceProvider(service = Networklogic.class)
public class UserHashtagContextNetwork extends Networklogic {


    private Map<String,Integer> edgeTypes = new HashMap<String,Integer>();
    public UserHashtagContextNetwork() {
     
    }

    @Override
    public void refreshGraphModel(){
        super.refreshGraphModel();
        if(graphModel.getEdgeTable().getColumn(EDGE_HASHTAG.label) == null) {
            graphModel.getEdgeTable().addColumn(EDGE_HASHTAG.label, EDGE_HASHTAG.classType, Origin.DATA);
        }
        
    }
    private int getOrCreateType(String hashtag) {
        if(edgeTypes.containsKey(hashtag)) {
            return edgeTypes.get(hashtag);
        } else {
            int newType = graphModel.addEdgeType(hashtag);
            edgeTypes.put(hashtag,newType);
            return newType;
        }
    }
    @Override
    public void processStatus(Status status) {
        long currentMillis = System.currentTimeMillis();
        
        for(HashtagEntity hashtag: status.getHashtagEntities()) {
            String stringHashtag= "#"+hashtag.getText().toLowerCase();
            int edgeType = getOrCreateType(stringHashtag);
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
                        createLink(origin, target, edgeType, stringHashtag, currentMillis);   
                    }
                }
            }

            // Manage Quoted Status
            if(status.getQuotedStatus() != null && 
               // Avoid self-loop
               !status.getUser().getScreenName().equals(status.getQuotedStatus().getUser().getScreenName()) ) {

                Node origin = createUser(status.getUser());
                Node target = createUser(status.getQuotedStatus().getUser());
                createLink(origin, target, edgeType, stringHashtag, System.currentTimeMillis());
                onStatus(status.getQuotedStatus());
            }

            //Manage Retweeted
            if (status.getRetweetedStatus() != null && 
                // Avoid self-loop
                !status.getUser().getScreenName().equals(status.getRetweetedStatus().getUser().getScreenName()) ) {

                Node origin = createUser(status.getUser());
                Node target = createUser(status.getRetweetedStatus().getUser());
                createLink(origin, target, edgeType, stringHashtag, System.currentTimeMillis());
                onStatus(status.getRetweetedStatus());
            } 
        }

    }
    
    private void createLink(Node origin, Node target, int type, String hashtag, long currentMillis) {
        Edge edge = graphModel.getGraph().getEdge(origin, target, type);
                    if (edge == null) { // If no, create it
                    edge = graphModel
                            .factory()
                            .newEdge(origin, target, type, true);
                    edge.setWeight(1.0);
                    edge.setColor(Color.GRAY);
                    edge.setLabel(hashtag);
                    edge.setAttribute(EDGE_HASHTAG.label, hashtag);
                    graphModel.getGraph().addEdge(edge);
                    } else { // If yes, increment the weight
                        edge.setWeight(edge.getWeight() + 1);
                    }
                    edge.addTimestamp(currentMillis);
    }
    @Override
    public String getName() {
        return "Bernardamus Projection";
    }

    @Override
    public int index() {
        return 10;
    }

}
