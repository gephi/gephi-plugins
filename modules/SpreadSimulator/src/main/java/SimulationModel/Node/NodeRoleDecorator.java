package SimulationModel.Node;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeRoleDecorator {
    private Double coverage;
    private Integer minCoverage;
    private NodeRole nodeRole;
    private List<NodeStateDecorator> nodeStates;
}
