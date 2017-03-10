package org.bitnine.MyImporter.plugin;


import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.openide.util.lookup.ServiceProvider;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dehowefeng
 */
@ServiceProvider(service = ImporterUI.class)
public class MyImporterUI implements ImporterUI {

   private JPanel panel;
   private JCheckBox option;
   private MyImporter importer;

   public void setup(Importer importer) {
     this.importer = (MyImporter)importer;
   }

   public JPanel getPanel() {
     panel = new JPanel();
     option = new JCheckBox("Option");
     panel.add(option);
     return panel;
   }

   public void unsetup(boolean update) {
     if(update) {
        importer.setOption(option.isSelected());
     }
     panel = null;
     importer = null;
     option = null;
   }

   public String getDisplayName() {
     return "Importer Foo";
   }

   public boolean isUIForImporter(Importer importer) {
     return importer instanceof MyImporter;
   }

    public void setup(Importer[] imprtrs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}