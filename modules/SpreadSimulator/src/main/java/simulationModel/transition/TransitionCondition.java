package simulationModel.transition;


import simulationModel.node.NodeState;
import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransitionCondition extends Transition {
    public TransitionCondition(TransitionType transitionType, NodeState sourceState, NodeState destinationState, Double probability, List<String> provocativeNeighborName) {
        super(transitionType, sourceState, destinationState);
        this.probability = probability;
        this.provocativeNeighborName = provocativeNeighborName;
    }

    private Double probability;
    private List<String> provocativeNeighborName;
}
