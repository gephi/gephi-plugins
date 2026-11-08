package fr.totetmatt.blueskygephi.graphmanipulator;

import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.datalab.spi.nodes.NodesManipulatorBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author totetmatt
 */
@ServiceProvider(service = NodesManipulatorBuilder.class)
public class BlueskyGephiMainGraphManipulatorBuilder implements NodesManipulatorBuilder {

    @Override
    public NodesManipulator getNodesManipulator() {
        return new BlueskyGephiMainGraphManipulator();
    }

}
