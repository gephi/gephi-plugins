package SimulationModel.Transition;

import SimulationModel.Node.NodeState;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Transition {
    protected TransitionType transitionType;
    protected NodeState sourceState;
    protected NodeState destinationState;
}
