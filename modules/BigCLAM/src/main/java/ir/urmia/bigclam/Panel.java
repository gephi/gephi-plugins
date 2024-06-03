
package ir.urmia.bigclam;

import javax.swing.*;
import java.awt.*;


public class Panel extends JPanel {

    private JTextField iterationsValue;
    private JTextField kValue;
    public Panel() {
        JPanel panel = this;
        panel.setPreferredSize(new Dimension(300, 100));

        JLabel labelK = new JLabel("Number of communities:");
        JLabel labelIterations = new JLabel("Number of iterations:");
        kValue = new JTextField();
        iterationsValue = new JTextField();

        labelK.setAlignmentX(0.0f);
        kValue.setAlignmentX(0.0f);
        labelIterations.setAlignmentX(0.0f);
        iterationsValue.setAlignmentX(0.0f);

        this.add(labelK);
        this.add(kValue);
        this.add(labelIterations);
        this.add(iterationsValue);

        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        panel.add(labelK);
        panel.add(kValue);
        panel.add(labelIterations);
        panel.add(iterationsValue);
    }

    public int getK() {
        int i = 0;
        try {
            i = Integer.valueOf(kValue.getText());
        } catch (Exception ex) {
            return 0;
        }
        return i;
    }

    public void setK(int k) {
        this.kValue.setText(String.valueOf(k));
    }

    public void setIterations(int i) {
        this.iterationsValue.setText(String.valueOf(i));
    }

    public int getIterations() {
        int i = 0;
        try {
            i = Integer.valueOf(iterationsValue.getText());
        } catch (Exception ex) {
            return 0;
        }
        return i;
    }
}
