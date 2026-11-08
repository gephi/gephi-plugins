package fr.totetmatt.blueskygephi.graphmanipulator;

import java.awt.event.KeyEvent;
import javax.swing.Icon;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author totetmatt
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class BlueskyGephiMainGraphManipulator implements NodesManipulator, GraphContextMenuItem {

    @Override
    public void setup(Node[] nodes, Node node) {

    }

    @Override
    public ContextMenuItemManipulator[] getSubItems() {
        return new ContextMenuItemManipulator[]{
            new BlueskyGephiFollowsGraphManipulator(),
            new BlueskyGephiFollowersGraphManipulator()
        };
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Integer getMnemonicKey() {
        return KeyEvent.VK_W;
    }

    @Override
    public void execute() {

    }

    @Override
    public String getName() {
        return "Bluesky";
    }

    @Override
    public String getDescription() {
        return "Fetch network from the selected nodes";
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return null;
    }

    @Override
    public int getType() {
        return 200;
    }

    @Override
    public int getPosition() {
        return 200;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public void setup(Graph graph, Node[] nodes) {

    }

}
