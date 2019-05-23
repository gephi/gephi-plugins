package org.gephi.plugins.linkprediction.warnings;

import javax.swing.*;

public abstract class LinkPredictionWarning {
    protected JFrame f;

    LinkPredictionWarning(String message) {
        f = new JFrame();
        JOptionPane.showMessageDialog(f, message, "Exception", JOptionPane.WARNING_MESSAGE);
    }

}
