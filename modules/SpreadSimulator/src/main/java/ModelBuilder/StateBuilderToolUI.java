package ModelBuilder;

import ModelBuilder.TransitionBuilder.TransitionBuilder;
import ModelBuilder.TransitionBuilder.TransitionBuilderPanel;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StateBuilderToolUI implements ToolUI {
    TransitionBuilder transitionBuilder = new TransitionBuilder();
    @Override
    public JPanel getPropertiesBar(Tool tool) {
        return null;
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

    private class ApplyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            transitionBuilder.execute();
        }
    }
}