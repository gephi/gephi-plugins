package modelBuilder;

import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolUI;

import javax.swing.*;

public class ModelBuilderToolUI implements ToolUI {
    private final ModelBuilderTool modelBuilderTool;

    public ModelBuilderToolUI(ModelBuilderTool modelBuilderTool) {
        this.modelBuilderTool = modelBuilderTool;
    }

    @Override
    public JPanel getPropertiesBar(Tool tool) {
        JPanel panel = new JPanel();
        panel.add(modelBuilderTool.statusLabel);
        return panel;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getName() {
        return "Create State";
    }

    @Override
    public String getDescription() {
        return "Create State by click";
    }

    @Override
    public int getPosition() {
        return 1000;
    }
}