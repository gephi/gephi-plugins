package Components.Simulation.Report;
import SimulationModel.Node.NodeRoleDecorator;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;


public class SimulationStepReport {

    private Integer step;
    private List<NodeRoleReport> roleReports;

    public SimulationStepReport(Integer step, List<NodeRoleDecorator> nodeRoleDecoratorList){
        this.step = step;
        roleReports = nodeRoleDecoratorList.stream().map( x -> new NodeRoleReport(x)).collect(Collectors.toList());
    }

    private class NodeRoleReport {
        private String nodeRoleName;
        private List<StateElement> statesReport;

        public NodeRoleReport(NodeRoleDecorator nodeRoleDecorator){
            nodeRoleName = nodeRoleDecorator.getNodeRole().getName();
            var nodeStates = nodeRoleDecorator.getNodeStates();
            statesReport = nodeStates.stream().map(nodeState -> {
                var stateName = nodeState.getNodeState().getName();
                var numOfNodes = nodeState.getMinCoverage();
                var coverage = nodeState.getCoverage();
                return new StateElement(stateName, numOfNodes, coverage);
            }).collect(Collectors.toList());
        }

        @AllArgsConstructor
        @NoArgsConstructor
        private class StateElement{
            private String nodeStateName;
            private Integer numberOfNodes;
            private Double coverage;
        }
    }
}





