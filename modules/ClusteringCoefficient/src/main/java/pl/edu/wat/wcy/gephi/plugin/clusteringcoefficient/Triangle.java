package pl.edu.wat.wcy.gephi.plugin.clusteringcoefficient;

import org.gephi.graph.api.Node;

import java.util.List;
import java.util.Objects;

/**
 * Created by Krystian on 29.04.2018.
 */
public class Triangle {

    private final List<Node> nodes;

    public Triangle(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.nodes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Triangle other = (Triangle) obj;
        if (!Objects.equals(this.nodes, other.nodes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Triangle{"
                + "node1 =" + nodes.get(0).getId().toString() + " node2= " + nodes.get(1).getId().toString() + " node3 = " + nodes.get(2).getId().toString()
                + '}' + "\n";
    }
}
