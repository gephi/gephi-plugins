package SimulationModel.Transition;


import SimulationModel.Node.NodeState;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransitionCondition extends Transition {
    public TransitionCondition(TransitionType transitionType, NodeState sourceState, NodeState destinationState, Double probability, List<String> provocativeNeighborName) {
        super(transitionType, sourceState, destinationState);
        this.probability = probability;
        this.provocativeNeighborName = provocativeNeighborName;
    }

    private Double probability;
    private List<String> provocativeNeighborName;
}
