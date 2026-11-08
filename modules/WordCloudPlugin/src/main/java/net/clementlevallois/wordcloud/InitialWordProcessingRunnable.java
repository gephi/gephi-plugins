/*
 * author: Clement Levallois
 */
package net.clementlevallois.wordcloud;

import java.util.List;
import java.util.ResourceBundle;
import org.gephi.graph.api.GraphModel;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

public class InitialWordProcessingRunnable implements LongTask, Runnable {

    private ProgressTicket progressTicket;
    private final GraphModel graphModel;
    private final String columnName;
    private final List<String> langs;
    private static final ResourceBundle bundle = NbBundle.getBundle(LexplorerTopComponent.class);
    private TopTermExtractor topTermExtractor;

    public InitialWordProcessingRunnable(GraphModel graphModel, String columnName, List<String> langs) {
        this.graphModel = graphModel;
        this.columnName = columnName;
        this.langs = langs;
    }

    @Override
    public void run() {
        Progress.setDisplayName(progressTicket, bundle.getString("progress.initial_analysis_running"));
        Progress.start(progressTicket);
        topTermExtractor = new TopTermExtractor();
        topTermExtractor.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, columnName, langs);
        Progress.finish(progressTicket, bundle.getString("progress.initial_analysis_complete"));

    }

    @Override
    public boolean cancel() {
        if (topTermExtractor != null) {
            topTermExtractor.setInitialAnalysisInterruptedByUser(true);
        }
        Progress.finish(progressTicket, bundle.getString("progress.initial_analysis_interrupted"));
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public void interruptInitialAnalysis() {
        if (topTermExtractor != null) {
            topTermExtractor.setInitialAnalysisInterruptedByUser(true);
        }
    }

}
