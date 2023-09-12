package SimulationModel;

import SimulationModel.Node.NodeRoleDecorator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class SimulationModel {
    private String name;
    private String description;
    private List<NodeRoleDecorator> nodeRoles;

    public boolean Validation() {
        return nodeRoles.stream().mapToDouble(x -> x.getCoverage()).sum() - 1 < 0.01;
    }
}
