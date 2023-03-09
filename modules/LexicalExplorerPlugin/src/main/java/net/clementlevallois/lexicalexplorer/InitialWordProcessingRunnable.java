/*
 * author: Clément Levallois
 */
package net.clementlevallois.lexicalexplorer;

import java.util.ResourceBundle;
import org.gephi.graph.api.GraphModel;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

public class InitialWordProcessingRunnable implements LongTask, Runnable {

    private ProgressTicket progressTicket;
    private boolean cancelled = false;
    private final GraphModel graphModel;
    private final String columnName;
    private final String lang;
    private static final ResourceBundle bundle = NbBundle.getBundle(LexplorerTopComponent.class);

    public InitialWordProcessingRunnable(GraphModel graphModel, String columnName, String lang) {
        this.graphModel = graphModel;
        this.columnName = columnName;
        this.lang = lang;
    }

    @Override
    public void run() {
        Progress.setDisplayName(progressTicket, bundle.getString("progress.initial_analysis_running"));
        Progress.start(progressTicket);
        TopTermExtractor topTermExtractor = new TopTermExtractor();
        topTermExtractor.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, columnName, lang);
        Progress.finish(progressTicket, bundle.getString("progress.initial_analysis_complete"));

    }

    @Override
    public boolean cancel() {
        this.cancelled = true;
        Progress.finish(progressTicket, bundle.getString("progress.initial_analysis_interrupted"));
        return false;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

}
