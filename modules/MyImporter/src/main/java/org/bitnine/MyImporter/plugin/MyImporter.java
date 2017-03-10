package org.bitnine.MyImporter.plugin;


import java.io.Reader;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dehowefeng
 */
public class MyImporter implements FileImporter, LongTask {

   private Reader reader;
   private ContainerLoader container;
   private Report report;
   private ProgressTicket progressTicket;
   private boolean cancel = false;

   public void setReader(Reader reader) {
      this.reader = reader;
   }

   public boolean execute(ContainerLoader loader) {
      this.container = loader;
      this.report = new Report();
      //Import
      return !cancel;
   }

   public ContainerLoader getContainer() {
      return container;
   }

   public Report getReport() {
      return report;
   }

   public boolean cancel() {
      cancel = true;
      return true;
   }

   public void setProgressTicket(ProgressTicket progressTicket) {
      this.progressTicket = progressTicket;
   }

    void setOption(boolean selected) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}