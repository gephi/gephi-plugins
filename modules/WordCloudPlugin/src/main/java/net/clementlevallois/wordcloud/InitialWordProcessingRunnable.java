/*
 * author: Clement Levallois
 */
package net.clementlevallois.wordcloud;

import java.util.ResourceBundle;
import org.gephi.graph.api.GraphModel;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

public class InitialWordProcessingRunnable implements LongTask, Runnable {

    private ProgressTicket progressTicket;
    private boolean cancelled = false;
    private Boolean initialAnalysisInterruptedByUser = false;
    private final GraphModel graphModel;
    private final String columnName;
    private final String lang;
    private static final ResourceBundle bundle = NbBundle.getBundle(LexplorerTopComponent.class);
    private TopTermExtractor topTermExtractor = new TopTermExtractor();

    public InitialWordProcessingRunnable(GraphModel graphModel, String columnName, String lang) {
        this.graphModel = graphModel;
        this.columnName = columnName;
        this.lang = lang;
    }

    @Override
    public void run() {
        Progress.setDisplayName(progressTicket, bundle.getString("progress.initial_analysis_running"));
        Progress.start(progressTicket);
        topTermExtractor.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, columnName, lang);
        Progress.finish(progressTicket, bundle.getString("progress.initial_analysis_complete"));

    }

    @Override
    public boolean cancel() {
        this.cancelled = true;
        Progress.finish(progressTicket, bundle.getString("progress.initial_analysis_interrupted"));
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public void interruptInitialAnalysis() {
        topTermExtractor.setInitialAnalysisInterruptedByUser(true);
    }

}
