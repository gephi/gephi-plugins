/*
 * author: Clément Levallois
 */
package net.clementlevallois.lexicalexplorer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.gephi.graph.api.GraphModel;
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
    private Set<String> previousSelectedNodes = new HashSet();
        private static final ResourceBundle bundle = NbBundle.getBundle(LexplorerTopComponent.class);

    public TopWordsFinderRunnable(Integer pauseBetweenComputations) {
        this.pauseBetweenComputations = pauseBetweenComputations;
    }

    @Override
    public void run() {
        Progress.start(progressTicket);
        Progress.setDisplayName(progressTicket, bundle.getString("expression.warning.wordcloud_analysis_running"));
        while (true && !this.cancelled) {
            if (!VizController.getInstance().getSelectionManager().isBlocked() && VizController.getInstance().getSelectionManager().isSelectionEnabled()) {
                List<Node> selectedNodes = VizController.getInstance().getSelectionManager().getSelectedNodes();
                Set<String> setIdsForTestChange = selectedNodes.stream().map(Node::getId).map(Object::toString).collect(Collectors.toSet());
                if (selectedNodes.isEmpty()) {
                    try {
                        results.put("");
                        try {
                            Thread.sleep(pauseBetweenComputations);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        previousSelectedNodes = new HashSet(setIdsForTestChange);
                        continue;
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (setIdsForTestChange.equals(previousSelectedNodes)) {
                    try {
                        Thread.sleep(pauseBetweenComputations);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    continue;
                }
                // retrieve the textual attributes of these nodes
                // and compute the top terms of these
                TopTermExtractor topTermExtractor = new TopTermExtractor();
                List<String> selectedNodesIds = new ArrayList(setIdsForTestChange);
                String topTermsExtractorFromSelectedNodes = topTermExtractor.topTermsExtractorFromSelectedNodes(selectedNodesIds, topWordsToRetrieve);
                try {
                    Thread.sleep(pauseBetweenComputations);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                try {
                    results.put(topTermsExtractorFromSelectedNodes);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                previousSelectedNodes = new HashSet(setIdsForTestChange);

            }
        }
    }

    public String getResult() {
        return "";
    }

    public String getIntermediary() throws InterruptedException {
        return results.take();
    }

    @Override
    public boolean cancel() {
        this.cancelled = true;
        Progress.finish(progressTicket, bundle.getString("expression.warning.wordcloud_analysis_stopped"));
        return false;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public void setTopWordsToRetrieve(Integer topWordsToRetrieve) {
        this.topWordsToRetrieve = topWordsToRetrieve;
    }
}
