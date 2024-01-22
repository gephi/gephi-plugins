package simulationModel.node;

import simulationModel.transition.Transition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class NodeRole {
    private String name;
    private String description;
    private List<Transition> transitionMap;

    public NodeRole(String name) {
        this.name = name;
    }
}
