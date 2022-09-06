package org.gephi.plugins.positionranking;

import java.util.Arrays;
import java.util.Optional;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;

public class PositionTransformerTest {

    @Test
    public void testTransform() {
        Graph graph = GraphGenerator.build().withProject().generateTinyGraph().addIntNodeColumn().getGraph();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel model = appearanceController.getModel();

        Function[] functions = model.getNodeFunctions();
        Optional<Function> function = Arrays.stream(functions).filter(f -> f.getTransformer() instanceof PositionTransformer).findFirst();
        Assert.assertTrue(function.isPresent());

        PositionTransformer positionTransformer = function.get().getTransformer();
        positionTransformer.setAxe(PositionTransformer.X_AXIS);

        Node node = graph.getNode("1");
        positionTransformer.transform(node, null, null, 1f);
        Assert.assertEquals(positionTransformer.getMax(), node.x(), 0.0);

        positionTransformer.transform(node, null, null, 0.5f);
        Assert.assertEquals((positionTransformer.getMax() - positionTransformer.getMin()) / 2.0, node.x(), 0.0);

        positionTransformer.setAxe(PositionTransformer.Y_AXIS);
        positionTransformer.transform(node, null, null, 1f);
        Assert.assertEquals(positionTransformer.getMax(), node.y(), 0.0);

        positionTransformer.setAxe(PositionTransformer.Z_AXIS);
        positionTransformer.transform(node, null, null, 1f);
        Assert.assertEquals(positionTransformer.getMax(), node.z(), 0.0);
    }

    @Test
    public void testTransformerUI() {
        GraphGenerator.build().withProject();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel model = appearanceController.getModel();

        Function[] functions = model.getNodeFunctions();
        Optional<Function> function = Arrays.stream(functions).filter(f -> f.getTransformer() instanceof PositionTransformer).findFirst();
        Assert.assertTrue(function.isPresent());

        TransformerUI ui = function.get().getUI();
        Assert.assertNotNull(ui);
        Assert.assertNotNull(ui.getPanel(function.get()));
    }
}
