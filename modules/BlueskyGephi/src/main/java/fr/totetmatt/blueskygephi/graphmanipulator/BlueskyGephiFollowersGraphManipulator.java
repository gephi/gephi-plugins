package fr.totetmatt.blueskygephi.graphmanipulator;

import fr.totetmatt.blueskygephi.BlueskyGephi;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Icon;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.Lookup;

/**
 *
 * @author totetmatt
 */
public class BlueskyGephiFollowersGraphManipulator implements NodesManipulator, GraphContextMenuItem {

    private Node[] nodes;

    @Override
    public void setup(Node[] nodes, Node node) {
        this.nodes = nodes;
    }

    @Override
    public ContextMenuItemManipulator[] getSubItems() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Integer getMnemonicKey() {
        return KeyEvent.VK_C;
    }

    @Override
    public void execute() {
        List<String> actors = Stream.of(nodes).map(x -> (String) x.getId()).collect(Collectors.toList());
        Lookup.getDefault()
                .lookup(BlueskyGephi.class)
                .fetchFollowerFollowsFromActors(actors, false, true, false);
    }

    @Override
    public String getName() {
        return "Fetch followers only data";
    }

    @Override
    public String getDescription() {
        return "Fetch only followers network from the selected nodes";
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
        this.nodes = nodes;
    }

}
