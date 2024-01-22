package simulationModel.node;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class NodeRoleDecorator {
    private Double coverage;
    private Integer minCoverage;
    private NodeRole nodeRole;
    private List<NodeStateDecorator> nodeStates;

    public NodeRoleDecorator(Double coverage, Integer minCoverage, NodeRole nodeRole, List<NodeStateDecorator> nodeStates) {
        this.coverage = coverage;
        this.minCoverage = minCoverage;
        this.nodeRole = nodeRole;
        this.nodeStates = nodeStates;
    }

    public NodeRoleDecorator(NodeRole nodeRole) {
        this.nodeRole = nodeRole;
    }
}
