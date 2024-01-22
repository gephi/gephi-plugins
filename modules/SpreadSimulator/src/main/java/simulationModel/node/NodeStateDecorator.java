package simulationModel.node;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.awt.*;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class NodeStateDecorator {
    private Double coverage;
    private Integer minCoverage;
    private NodeState nodeState;
    private Color color;

    public NodeStateDecorator(Double coverage, Integer minCoverage, NodeState nodeState) {
        this.coverage = coverage;
        this.minCoverage = minCoverage;
        this.nodeState = nodeState;
        this.color = Color.BLACK;
    }

    public NodeStateDecorator(NodeState nodeState) {
        this.nodeState = nodeState;
        this.color = Color.BLACK;
    }
}
