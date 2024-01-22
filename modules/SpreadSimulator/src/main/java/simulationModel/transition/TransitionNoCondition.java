package simulationModel.transition;

import simulationModel.node.NodeState;
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
public class TransitionNoCondition extends Transition {
    public TransitionNoCondition(TransitionType transitionType, NodeState sourceState, NodeState destinationState, Double probability) {
        super(transitionType, sourceState, destinationState);
        this.probability = probability;
    }

    private Double probability;
}
