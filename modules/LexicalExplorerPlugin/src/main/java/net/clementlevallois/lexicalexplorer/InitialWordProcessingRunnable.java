/*
 * author: Clément Levallois
 */
package net.clementlevallois.lexicalexplorer;

import org.gephi.graph.api.GraphModel;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

public class InitialWordProcessingRunnable implements LongTask, Runnable {

    private ProgressTicket progressTicket;
    private boolean cancelled = false;
    private GraphModel graphModel;
    private String columnName;
    private String lang;

    public InitialWordProcessingRunnable(GraphModel graphModel, String columnName, String lang) {
        this.graphModel = graphModel;
        this.columnName = columnName;
        this.lang = lang;
    }

    @Override
    public void run() {
        Progress.setDisplayName(progressTicket, "initial word analysis in progress");
        Progress.start(progressTicket);
        TopTermExtractor topTermExtractor = new TopTermExtractor();
        topTermExtractor.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, columnName, lang);
        Progress.finish(progressTicket, "initial word analysis complete");

    }

    @Override
    public boolean cancel() {
        this.cancelled = true;
        Progress.finish(progressTicket, "initial word analysis interrupted");
        return false;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

}
