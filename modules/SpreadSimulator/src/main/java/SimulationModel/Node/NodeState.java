package SimulationModel.Node;

import SimulationModel.Transition.Transition;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Map;
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeState {
    private String name;
    private String description;
    private Double coverage;
    private Integer minCoverage;
    private Map<String, Transition> transitionMap;
}