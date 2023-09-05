package SimulationModel.Transition;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Transition {
    protected TransitionType TransitionType;
}
