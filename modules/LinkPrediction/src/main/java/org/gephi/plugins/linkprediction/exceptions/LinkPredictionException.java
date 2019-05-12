package org.gephi.plugins.linkprediction.exceptions;

import javax.swing.*;

public abstract class LinkPredictionException {
    protected JFrame f;

    LinkPredictionException(String message) {
        f = new JFrame();
        JOptionPane.showMessageDialog(f, message, "Exception", JOptionPane.WARNING_MESSAGE);
    }

}
