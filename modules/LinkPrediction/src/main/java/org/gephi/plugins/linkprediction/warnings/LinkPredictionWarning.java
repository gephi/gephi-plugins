package org.gephi.plugins.linkprediction.warnings;

import javax.swing.*;

/**
 * Base class, used to expose warnings to the user.
 */
public abstract class LinkPredictionWarning {
    // Pop-up frame
    protected JFrame f;

    LinkPredictionWarning(String message) {
        f = new JFrame();
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(f, message, "Exception", JOptionPane.WARNING_MESSAGE));
    }

}
