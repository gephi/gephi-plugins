package org.openuniversityofisrael.katzcentrality;

import org.openide.util.Exceptions;

public class KatzCentralityPanel extends javax.swing.JPanel {
    private org.jdesktop.swingx.JXHeader header;
    private javax.swing.JTextField alpha;
    private javax.swing.JLabel alphaLabel;
    private javax.swing.JLabel supportedGraphsLabel;
    private static final String ALPHA_LABEL_TEXT = "Alpha";
    private static final String TITLE = "Katz Centrality";
    private static final String SUPPORTED_GRAPHS_TEXT = "Note: directed or undirected graphs are supported.";
    private static final String DESCRIPTION = "The Katz Centrality of a network node is the infinite series sum of the number of walks of length n leading to it, discounted by the parameter alpha raised to the n-th power.  This series converges when the absolute value of the parameter alpha is smaller than the inverse of the largest eigenvalue of the network adjacency matrix.";


    public KatzCentralityPanel() {
        initComponents();
    }

    public void setAlpha(double alpha){
        this.alpha.setText(alpha + "");
    }

    public double getAlpha(){
        try{
            double alpha = Double.parseDouble(this.alpha.getText());
            return alpha;
        }catch(Exception e){
            Exceptions.printStackTrace(e);
        }
        return 0;
    }

    private void initComponents() {
        this.alpha = new javax.swing.JTextField(KatzCentrality.DEFAULT_ALPHA + "");
        this.alpha.setMinimumSize(new java.awt.Dimension(59, 25));
        this.alpha.setPreferredSize(new java.awt.Dimension(59, 25));
        this.alphaLabel = new javax.swing.JLabel(ALPHA_LABEL_TEXT);
        this.supportedGraphsLabel = new javax.swing.JLabel(SUPPORTED_GRAPHS_TEXT);
        this.header = new org.jdesktop.swingx.JXHeader();
        this.header.setDescription(DESCRIPTION);
        this.header.setTitle(TITLE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 536, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(alphaLabel)
                                .addGap(45, 45, 45)
                                .addComponent(alpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(supportedGraphsLabel)
                        )
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(alphaLabel)
                                        .addGap(45, 45, 45)
                                        .addComponent(alpha, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(supportedGraphsLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        )
        );
    }
}
