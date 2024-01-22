package components.simulationLogic.report;
import simulationModel.node.NodeRoleDecorator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SimulationStepReport {

    private Integer step;
    private List<NodeRoleReport> roleReports;

    public SimulationStepReport(Integer step, List<NodeRoleDecorator> nodeRoleDecoratorList){
        this.step = step;
        roleReports = nodeRoleDecoratorList.stream().map( x -> new NodeRoleReport(x)).collect(Collectors.toList());
    }

    @Getter
    public class NodeRoleReport {
        private String nodeRoleName;
        private List<StateElement> statesReport;

        public NodeRoleReport(NodeRoleDecorator nodeRoleDecorator){
            nodeRoleName = nodeRoleDecorator.getNodeRole().getName();
            var nodeStates = nodeRoleDecorator.getNodeStates();
            statesReport = nodeStates.stream().map(nodeState -> {
                var stateName = nodeState.getNodeState().getName();
                var numOfNodes = nodeState.getMinCoverage();
                var coverage = nodeState.getCoverage();
                return new StateElement(stateName, Double.valueOf(numOfNodes), coverage);
            }).collect(Collectors.toList());
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Setter
        public class StateElement{
            private String nodeStateName;
            private Double numberOfNodes;
            private Double coverage;
        }
    }
}
