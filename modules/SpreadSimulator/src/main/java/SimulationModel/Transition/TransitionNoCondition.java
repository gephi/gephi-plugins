package SimulationModel.Transition;

import SimulationModel.Node.NodeState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class TransitionNoCondition extends Transition {
    public TransitionNoCondition(TransitionType transitionType, NodeState sourceState, NodeState destinationState, Double probability) {
        super(transitionType, sourceState, destinationState);
        this.probability = probability;
    }

    private Double probability;
}
