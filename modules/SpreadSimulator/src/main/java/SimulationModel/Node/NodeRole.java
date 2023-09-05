package SimulationModel.Node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeRole {
    private String name;
    private String description;
    private Double coverage;
    private Integer minCoverage;
    private List<NodeState> nodeStates;

    public boolean Validation(){
        return nodeStates.stream().mapToDouble(x -> x.getCoverage()).sum() == 1;
    }
}
