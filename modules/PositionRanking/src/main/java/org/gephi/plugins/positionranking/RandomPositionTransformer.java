package org.gephi.plugins.positionranking;

import java.util.Random;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Transformer.class, position = 999)
public class RandomPositionTransformer implements SimpleTransformer<Node> {

    private final Random random = new Random();
    public static final double SIZE = 100.0;

    @Override
    public void transform(Node node) {
        node.setX((float) (-SIZE / 2 + SIZE * random.nextDouble()));
        node.setY((float) (-SIZE / 2 + SIZE * random.nextDouble()));
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public boolean isEdge() {
        return false;
    }
}
