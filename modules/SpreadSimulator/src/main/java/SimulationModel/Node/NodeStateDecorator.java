package SimulationModel.Node;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeStateDecorator {
    private Double coverage;
    private Integer minCoverage;
    private NodeState nodeState;
}
