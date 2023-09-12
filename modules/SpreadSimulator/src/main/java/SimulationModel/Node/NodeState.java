package SimulationModel.Node;

import SimulationModel.Transition.Transition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Map;
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class NodeState {
    private String name;
    private String description;

    public NodeState(String name) {
        this.name = name;
    }
}