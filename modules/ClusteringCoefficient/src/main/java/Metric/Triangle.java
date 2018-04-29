package Metric;


import org.gephi.graph.api.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Krystian on 29.04.2018.
 */
public class Triangle {

    private List<Node> nodes;



    public Triangle(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Triangle){
            int k = 0;
            List<Node> givenNodeList = ((Triangle) obj).getNodes();
            for(int i = 0; i<nodes.size(); i++ ){
                for (int j = i; j<givenNodeList.size(); j++){
                    if(nodes.get(i).equals(givenNodeList.get(j))){
                        k++;
                    }
                    if(k==nodes.size()){
                        return true;
                    }
                }
            }
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "node1 =" + nodes.get(0).getId().toString() + " node2= " + nodes.get(1).getId().toString() + " node3 = " + nodes.get(2).getId().toString() +
                '}' + "\n";
    }
}
