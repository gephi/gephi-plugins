package SimulationModel.Transition;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransitionNoCondition extends Transition {
    private Double probability;
}
