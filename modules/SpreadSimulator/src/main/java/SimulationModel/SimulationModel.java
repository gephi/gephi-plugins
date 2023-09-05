package SimulationModel;

import SimulationModel.Node.NodeRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationModel {
    private String name;
    private String description;
    private List<NodeRole> nodeRoles;

    public boolean Validation() {
        return nodeRoles.stream().mapToDouble(x -> x.getCoverage()).sum() == 1;
    }

}
