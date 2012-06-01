/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uic.cs.PairImporter;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author joshua
 */
@ServiceProvider(service = ImporterUI.class)
public class PairImporterUI implements ImporterUI {
 
   private JPanel panel;
   private JCheckBox option;
   private PairImporter importer;
   private JTextField customField;
   private JRadioButton dateOption, dateTimeOption, doubleOption, customOption;
   private ButtonGroup bgroup;
   public enum Options {
    DATE, DATETIME, DOUBLE, CUSTOM 
}
 
   public void setup(Importer importer) {
     this.importer = (PairImporter)importer;
   }
 
   public JPanel getPanel() {
     panel = new JPanel();
     
     bgroup = new ButtonGroup();
     panel.setLayout(new GridLayout(5,1));
     panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Date and or Time format?"));
     customField = new JTextField("Date Format String");
     dateOption = new JRadioButton("Date", true);
     dateTimeOption = new JRadioButton("DateTime", false);
     doubleOption = new JRadioButton("Double", false);
     customOption = new JRadioButton("Custom" , false);
     bgroup.add(dateOption);
     bgroup.add(dateTimeOption);
     bgroup.add(doubleOption);
     bgroup.add(customOption);
     panel.add(dateOption);
     panel.add(dateTimeOption);
     panel.add(doubleOption);
     panel.add(customOption);
     panel.add(customField);
     return panel;
   }
 
   public void unsetup(boolean update) {
     if(update) {
        // dateFormat.setText("");
        if(dateOption.isSelected()){
            importer.setOption(Options.DATE);
        }
        if(dateTimeOption.isSelected()){
            importer.setOption(Options.DATETIME);
        }
        if(doubleOption.isSelected()){
            importer.setOption(Options.DOUBLE);
        }
        if(customOption.isSelected()){
            importer.setOption(Options.CUSTOM);
            importer.setCustomField(customField.getText());
        }
     }
     panel = null;
     importer = null;
     option = null;
   }
 
   public String getDisplayName() {
     return "Pair Importer";
   }
 
   public boolean isUIForImporter(Importer importer) {
     return importer instanceof PairImporter;
   }
}