package SimulationModel.Transition;

import SimulationModel.Node.NodeState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class Transition {
    protected TransitionType transitionType;
    protected NodeState sourceState;
    protected NodeState destinationState;
}
