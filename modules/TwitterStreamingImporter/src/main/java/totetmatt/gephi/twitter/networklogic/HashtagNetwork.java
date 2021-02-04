package totetmatt.gephi.twitter.networklogic;

import java.awt.Color;
import java.util.Arrays;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;
import twitter4j.HashtagEntity;
import twitter4j.Status;

/**
 * Create a network from tweet based only on hashtag -> hashtag If there is multiple
 * time the same link between hashtags, the weight of the edge is incremented
 *
 * If a hashtag is part of the query, it will be ignored, as every other hashtags
 * will be link to it. Can be discussed, or put as an option later.
 * 
 * 
 * 
 * @author totetmatt
 */
@ServiceProvider(service = Networklogic.class)
public class HashtagNetwork extends Networklogic {


    public HashtagNetwork() {
     
    }

    @Override
    public void processStatus(Status status) {
        long currentMillis = System.currentTimeMillis();
        for(HashtagEntity h1:status.getHashtagEntities()){
            if(!Arrays.asList(this.track).contains(h1.getText().toLowerCase())) {
                for(HashtagEntity h2:status.getHashtagEntities()){
                    if(!Arrays.asList(this.track).contains(h2.getText().toLowerCase()) &&
                       !h1.getText().toLowerCase().equals(h2.getText().toLowerCase())) {
                        Node n1 = createHashtag(h1.getText().toLowerCase());
                        Node n2 = createHashtag(h2.getText().toLowerCase());
                        n1.addTimestamp(currentMillis);
                        n2.addTimestamp(currentMillis);
                        Edge link = graphModel.getGraph().getEdge(n1, n2);
                        if (link == null) {
                            link = graphModel.factory().newEdge(n1, n2,false);
                            link.setWeight(1.0);
                            link.setColor(Color.GRAY);
                            graphModel.getGraph().addEdge(link);
                        } else {
                            link.setWeight(link.getWeight()+1);
                        }
                        link.addTimestamp(currentMillis);

                    }
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "Hashtag Network";
    }

    @Override
    public int index() {
        return 2;
    }

}
