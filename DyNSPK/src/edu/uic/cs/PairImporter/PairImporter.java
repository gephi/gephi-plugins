/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uic.cs.PairImporter;

import edu.uic.cs.PairImporter.PairImporterUI.Options;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.io.importer.api.*;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;

/**
 *
 * @author joshua
 */
public class PairImporter implements FileImporter, LongTask{
   private Reader reader;
   private ContainerLoader container;
   private Report report;
   private ProgressTicket progressTicket;
   private boolean cancel = false;
    private boolean option;
    private boolean date;
    private boolean dateFlag, dateTimeFlag, doubleFlag, customFlag;
    private String customDateField;
    @Override
    public void setReader(Reader reader) {
         this.reader = reader;
    }

    @Override
    public boolean execute(ContainerLoader loader) {
       this.container = loader;
      this.report = new Report();
      LineNumberReader lineReader = ImportUtils.getTextReader(reader);
        String line;
        try {
            while((line = lineReader.readLine()) != null){
             String inputLine [] = line.split("\\s");
             String pairdate = inputLine[0].split("T")[0];
             if(dateFlag){
                 loader.setTimeFormat(DynamicModel.TimeFormat.DATE);
             }
             else if (dateTimeFlag){
                 loader.setTimeFormat(DynamicModel.TimeFormat.DATETIME);
             }
             else if (doubleFlag){
                  loader.setTimeFormat(DynamicModel.TimeFormat.DOUBLE);
             }
             else if (customFlag){
                 SimpleDateFormat inputFormatter = new SimpleDateFormat(customDateField);
                 Date tempdate;
                    try {
                       tempdate = inputFormatter.parse(pairdate); 
                        SimpleDateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        pairdate = ISO8601Local.format(tempdate);
                         loader.setTimeFormat(DynamicModel.TimeFormat.DATETIME);
                        
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                 
             }
            addEdge(inputLine[1], inputLine[2], pairdate);
            
             
             
            }
           
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
      return !cancel;
    }
  private void addNode(String id, String label, String timeInterval) {
        NodeDraft node;
        if (!container.nodeExists(id)) {
            node = container.factory().newNodeDraft();
            node.setId(id);
            node.setLabel(label);
            node.addTimeInterval(timeInterval, timeInterval);
            container.addNode(node);
        }
    }

    private void addEdge(String source, String target, String timeInterval) {
        addEdge(source, target, 1, timeInterval);
    }

    private void addEdge(String source, String target, float weight, String timeInterval) {
        NodeDraft sourceNode;
        if (!container.nodeExists(source)) {
            sourceNode = container.factory().newNodeDraft();
            sourceNode.setId(source);
            container.addNode(sourceNode);
            sourceNode.addTimeInterval(timeInterval, timeInterval);
        } else {
            sourceNode = container.getNode(source);
        }
        NodeDraft targetNode;
        if (!container.nodeExists(target)) {
            targetNode = container.factory().newNodeDraft();
            targetNode.setId(target);
            container.addNode(targetNode);
            targetNode.addTimeInterval(timeInterval, timeInterval);
        } else {
            targetNode = container.getNode(target);
        }
        EdgeDraft edge = container.getEdge(sourceNode, targetNode);
        if (edge == null) {
            edge = container.factory().newEdgeDraft();
            edge.setSource(sourceNode);
            edge.setTarget(targetNode);
            container.addEdge(edge);
            edge.addTimeInterval(timeInterval, timeInterval);
        } else {
            edge.setWeight(edge.getWeight() + weight);
        }
    }
    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
       return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

 

    void setOption(Options options) {
         switch(options){
            case DATE:
                dateFlag = true;
                break;
            case DATETIME:
                dateTimeFlag = true;
                break;
            case DOUBLE:
                doubleFlag = true;
                break;
            case CUSTOM:
                customFlag = true;
                break;
                
            
                
        }
    }

    void setCustomField(String text) {
        customDateField = text;
    }
    
}
