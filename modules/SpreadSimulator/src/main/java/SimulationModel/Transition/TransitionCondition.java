package SimulationModel.Transition;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransitionCondition extends Transition {
    private Double probability;
    private List<String> provocativeNeighborName;
}
