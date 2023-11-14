/*
 * author: Clément Levallois
 */
package net.clementlevallois.wordcloud;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import org.gephi.graph.api.Node;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.visualization.VizController;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class TopWordsFinderRunnable implements LongTask, Runnable {

    private ProgressTicket progressTicket;
    private final LinkedBlockingQueue<String> results = new LinkedBlockingQueue();
    private final Integer pauseBetweenComputations;
    private Integer topWordsToRetrieve = StaticProperties.DEFAULT_WORDS_TO_DISPLAY;
    private boolean cancelled = false;
    private static final ResourceBundle bundle = NbBundle.getBundle(LexplorerTopComponent.class);
    private TopTermExtractor topTermExtractor = new TopTermExtractor();

    public TopWordsFinderRunnable(Integer pauseBetweenComputations, int topWordsToRetrieve) {
        this.pauseBetweenComputations = pauseBetweenComputations;
        this.topWordsToRetrieve = topWordsToRetrieve;
    }

    @Override
    public void run() {

        Progress.start(progressTicket);
        Progress.setDisplayName(progressTicket, bundle.getString("expression.warning.wordcloud_analysis_running"));
        Set<Node> previousSelectedNodes = new HashSet();
        while (!this.cancelled) {
            introduceAPauseBetweenCalculations();
            try {
                if (!VizController.getInstance().getSelectionManager().isBlocked() && VizController.getInstance().getSelectionManager().isSelectionEnabled()) {
                    List<Node> selectedNodes = VizController.getInstance().getSelectionManager().getSelectedNodes();
                    Set<Node> setNodesForTestChange = new HashSet(selectedNodes);
                    if (selectedNodes.isEmpty()) {
                        results.put("");
                    } else if (!setNodesForTestChange.equals(previousSelectedNodes)) {
                        // retrieve the textual attributes of these nodes
                        // and compute the top terms of these
                        String topTermsExtractorFromSelectedNodes = topTermExtractor.topTermsExtractorFromSelectedNodes(selectedNodes, topWordsToRetrieve);
                        results.put(topTermsExtractorFromSelectedNodes);
                    }
                    previousSelectedNodes = new HashSet(setNodesForTestChange);
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void introduceAPauseBetweenCalculations() {
        try {
            Thread.sleep(pauseBetweenComputations);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public String getIntermediary() throws InterruptedException {
        return results.take();
    }

    @Override
    public boolean cancel() {
        this.cancelled = true;
        Progress.finish(progressTicket, bundle.getString("expression.warning.wordcloud_analysis_stopped"));
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public void setTopWordsToRetrieve(Integer topWordsToRetrieve) {
        this.topWordsToRetrieve = topWordsToRetrieve;
    }
}
