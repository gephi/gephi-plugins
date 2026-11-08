/* Copyright 2015 Wouter Spekkink
Authors : Wouter Spekkink <wouterspekkink@gmail.com>
Website : http://www.wouterspekkink.org
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
Copyright 2015 Wouter Spekkink. All rights reserved.
The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License. When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
Contributor(s): Wouter Spekkink

The plugin makes use of the MDSJ library, which is available under the Creative Commons License "by-nc-sa" 3.0.
Link to license: http://creativecommons.org/licenses/by-nc-sa/3.0/
Ref: "Algorithmics Group. MDSJ: Java Library for Multidimensional Scaling (Version 0.2). 
Available at http://www.inf.uni-konstanz.de/algo/software/mdsj/. University of Konstanz, 2009."

 */
package org.wouterspekkink.plugins.metric.mdsstatistics;

/**
 * The plugin makes use of the MDSJ library, which is available under the Creative Commons License "by-nc-sa" 3.0. Link to license: http://creativecommons.org/licenses/by-nc-sa/3.0/ Ref: "Algorithmics
 * Group. MDSJ: Java Library for Multidimensional Scaling (Version 0.2). Available at http://www.inf.uni-konstanz.de/algo/software/mdsj/. University of Konstanz, 2009."
 *
 * For the calculation of shortest paths the plugin uses the algorithm originally used by Gephi as a step in the calculation of centrality metrics.
 *
 * @author wouter
 */
public class MdsStatisticsPanel extends javax.swing.JPanel {

    /**
     * Creates new form MdsStatisticsPanel
     */
    public MdsStatisticsPanel() {
        initComponents();
    }

    public void setDissimilarity(boolean dissimilarity, boolean similarity) {
        if (dissimilarity) {
            edgeTypeButtonGroup.setSelected(edgeWeightDissimilarity.getModel(), true);
        } else if (similarity) {
            edgeTypeButtonGroup.setSelected(edgeWeightSimilarity.getModel(), true);
        } else {
            edgeTypeButtonGroup.setSelected(noWeightsButton.getModel(), true);
        }
    }

    public boolean isNoWeights() {
        return noWeightsButton.isSelected();
    }

    public boolean isDissimilarity() {
        return edgeWeightDissimilarity.isSelected();
    }

    public boolean isSimilarity() {
        return edgeWeightSimilarity.isSelected();
    }

    public void setDistanceWeight(int weight) {
        switch (weight) {
            case 0:
                weighDistanceButtonGroup.setSelected(equalWeight.getModel(), true);
                break;
            case -2:
                weighDistanceButtonGroup.setSelected(downWeight.getModel(), true);
                break;
            default:
                weighDistanceButtonGroup.setSelected(equalWeight.getModel(), true);
        }
    }

    public int getDistanceWeight() {
        if (equalWeight.isSelected()) {
            return 0;
        } else {
            return -2;
        }
    }

    public void setNumberDimensions(int number) {
        switch (number) {
            case 2:
                dimButtonGroup.setSelected(dim2Button.getModel(), true);
                break;
            case 3:
                dimButtonGroup.setSelected(dim3Button.getModel(), true);
                break;
            case 4:
                dimButtonGroup.setSelected(dim4Button.getModel(), true);
                break;
            case 5:
                dimButtonGroup.setSelected(dim5Button.getModel(), true);
                break;
            case 6:
                dimButtonGroup.setSelected(dim6Button.getModel(), true);
                break;
            case 7:
                dimButtonGroup.setSelected(dim7Button.getModel(), true);
                break;
            case 8:
                dimButtonGroup.setSelected(dim8Button.getModel(), true);
                break;
            case 9:
                dimButtonGroup.setSelected(dim9Button.getModel(), true);
                break;
            case 10:
                dimButtonGroup.setSelected(dim10Button.getModel(), true);
                break;
            default:
                dimButtonGroup.setSelected(dim2Button.getModel(), true);
        }
    }

    public int getNumberDimensions() {
        if (dim2Button.isSelected()) {
            return 2;
        } else if (dim3Button.isSelected()) {
            return 3;
        } else if (dim4Button.isSelected()) {
            return 4;
        } else if (dim5Button.isSelected()) {
            return 5;
        } else if (dim6Button.isSelected()) {
            return 6;
        } else if (dim7Button.isSelected()) {
            return 7;
        } else if (dim8Button.isSelected()) {
            return 8;
        } else if (dim9Button.isSelected()) {
            return 9;
        } else {
            return 10;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        edgeTypeButtonGroup = new javax.swing.ButtonGroup();
        weighDistanceButtonGroup = new javax.swing.ButtonGroup();
        dimButtonGroup = new javax.swing.ButtonGroup();
        edgeWeightDissimilarity = new javax.swing.JRadioButton();
        edgeWeightSimilarity = new javax.swing.JRadioButton();
        noWeightsButton = new javax.swing.JRadioButton();
        equalWeight = new javax.swing.JRadioButton();
        downWeight = new javax.swing.JRadioButton();
        dim2Button = new javax.swing.JRadioButton();
        dim3Button = new javax.swing.JRadioButton();
        dim4Button = new javax.swing.JRadioButton();
        dim5Button = new javax.swing.JRadioButton();
        dim6Button = new javax.swing.JRadioButton();
        dim7Button = new javax.swing.JRadioButton();
        dim8Button = new javax.swing.JRadioButton();
        dim9Button = new javax.swing.JRadioButton();
        dim10Button = new javax.swing.JRadioButton();
        Introduction = new javax.swing.JLabel();
        Ref1 = new javax.swing.JLabel();
        Ref2 = new javax.swing.JLabel();
        DimLabel = new javax.swing.JLabel();
        edgesLabel = new javax.swing.JLabel();
        License1 = new javax.swing.JLabel();
        License2 = new javax.swing.JLabel();
        License3 = new javax.swing.JLabel();
        License4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        edgeTypeButtonGroup.add(edgeWeightDissimilarity);
        edgeWeightDissimilarity.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.edgeWeightDissimilarity.text")); // NOI18N

        edgeTypeButtonGroup.add(edgeWeightSimilarity);
        edgeWeightSimilarity.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.edgeWeightSimilarity.text")); // NOI18N

        edgeTypeButtonGroup.add(noWeightsButton);
        noWeightsButton.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.noWeightsButton.text")); // NOI18N

        weighDistanceButtonGroup.add(equalWeight);
        equalWeight.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.equalWeight.text")); // NOI18N

        weighDistanceButtonGroup.add(downWeight);
        downWeight.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.downWeight.text")); // NOI18N

        dimButtonGroup.add(dim2Button);
        dim2Button.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.dim2Button.text")); // NOI18N

        dimButtonGroup.add(dim3Button);
        dim3Button.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.dim3Button.text")); // NOI18N

        dimButtonGroup.add(dim4Button);
        dim4Button.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.dim4Button.text")); // NOI18N

        dimButtonGroup.add(dim5Button);
        dim5Button.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.dim5Button.text")); // NOI18N

        dimButtonGroup.add(dim6Button);
        dim6Button.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.dim6Button.text")); // NOI18N

        dimButtonGroup.add(dim7Button);
        dim7Button.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.dim7Button.text")); // NOI18N

        dimButtonGroup.add(dim8Button);
        dim8Button.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.dim8Button.text")); // NOI18N

        dimButtonGroup.add(dim9Button);
        dim9Button.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.dim9Button.text")); // NOI18N

        dimButtonGroup.add(dim10Button);
        dim10Button.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.dim10Button.text")); // NOI18N

        Introduction.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.Introduction.text_2")); // NOI18N

        Ref1.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.Ref1.text_2")); // NOI18N

        Ref2.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.Ref2.text_2")); // NOI18N

        DimLabel.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.DimLabel.text")); // NOI18N

        edgesLabel.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.edgesLabel.text_2")); // NOI18N

        License1.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.License1.text")); // NOI18N

        License2.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.License2.text")); // NOI18N

        License3.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.License3.text")); // NOI18N

        License4.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.License4.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.stressCalculationLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Ref2)
                    .addComponent(Introduction)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dim2Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dim3Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dim4Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dim5Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dim6Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dim7Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dim8Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dim9Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dim10Button))
                    .addComponent(DimLabel)
                    .addComponent(License1)
                    .addComponent(Ref1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(License3)
                            .addComponent(License2)
                            .addComponent(License4)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(edgesLabel)
                            .addComponent(noWeightsButton)
                            .addComponent(edgeWeightDissimilarity)
                            .addComponent(edgeWeightSimilarity))
                        .addGap(170, 170, 170)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(downWeight)
                            .addComponent(equalWeight)
                            .addComponent(jLabel1))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(DimLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dim2Button)
                    .addComponent(dim3Button)
                    .addComponent(dim4Button)
                    .addComponent(dim5Button)
                    .addComponent(dim6Button)
                    .addComponent(dim7Button)
                    .addComponent(dim8Button)
                    .addComponent(dim9Button)
                    .addComponent(dim10Button))
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edgesLabel)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(noWeightsButton)
                    .addComponent(downWeight))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edgeWeightDissimilarity)
                    .addComponent(equalWeight))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgeWeightSimilarity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(Introduction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Ref1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Ref2)
                .addGap(18, 18, 18)
                .addComponent(License1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(License2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(License3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(License4)
                .addContainerGap())
        );

        DimLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.DimLabel.text")); // NOI18N
        License2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MdsStatisticsPanel.class, "MdsStatisticsPanel.License2.text")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel DimLabel;
    private javax.swing.JLabel Introduction;
    private javax.swing.JLabel License1;
    private javax.swing.JLabel License2;
    private javax.swing.JLabel License3;
    private javax.swing.JLabel License4;
    private javax.swing.JLabel Ref1;
    private javax.swing.JLabel Ref2;
    private javax.swing.JRadioButton dim10Button;
    private javax.swing.JRadioButton dim2Button;
    private javax.swing.JRadioButton dim3Button;
    private javax.swing.JRadioButton dim4Button;
    private javax.swing.JRadioButton dim5Button;
    private javax.swing.JRadioButton dim6Button;
    private javax.swing.JRadioButton dim7Button;
    private javax.swing.JRadioButton dim8Button;
    private javax.swing.JRadioButton dim9Button;
    private javax.swing.ButtonGroup dimButtonGroup;
    private javax.swing.JRadioButton downWeight;
    private javax.swing.ButtonGroup edgeTypeButtonGroup;
    private javax.swing.JRadioButton edgeWeightDissimilarity;
    private javax.swing.JRadioButton edgeWeightSimilarity;
    private javax.swing.JLabel edgesLabel;
    private javax.swing.JRadioButton equalWeight;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton noWeightsButton;
    private javax.swing.ButtonGroup weighDistanceButtonGroup;
    // End of variables declaration//GEN-END:variables
}
