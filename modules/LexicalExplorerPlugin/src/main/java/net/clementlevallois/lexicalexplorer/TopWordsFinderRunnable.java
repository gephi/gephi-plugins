/*
 * author: Clément Levallois
 */
package net.clementlevallois.lexicalexplorer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.gephi.graph.api.Node;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.visualization.VizController;
import org.openide.util.Exceptions;

public class TopWordsFinderRunnable implements LongTask, Runnable {

    private ProgressTicket progressTicket;
    private final LinkedBlockingQueue<String> results = new LinkedBlockingQueue();
    private Integer nbTopTerms;
    private Integer pauseBetweenComputations;
    private boolean cancelled = false;
    private Set<String> previousSelectedNodes = new HashSet();

    public TopWordsFinderRunnable(Integer nbTopTerms, Integer pauseBetweenComputations) {
        this.nbTopTerms = nbTopTerms;
        this.pauseBetweenComputations = pauseBetweenComputations;
    }

    @Override
    public void run() {
        Progress.start(progressTicket);
        Progress.setDisplayName(progressTicket, "message");
        while (true && !this.cancelled) {
            if (!VizController.getInstance().getSelectionManager().isBlocked() && VizController.getInstance().getSelectionManager().isSelectionEnabled()) {
                List<Node> selectedNodes = VizController.getInstance().getSelectionManager().getSelectedNodes();
                Set<String> setIdsForTestChange = selectedNodes.stream().map(Node::getId).map(Object::toString).collect(Collectors.toSet());
                if (setIdsForTestChange.equals(previousSelectedNodes)){
                    try {
                        Thread.sleep(pauseBetweenComputations);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    continue;
                }
                previousSelectedNodes = new HashSet(setIdsForTestChange);
                if (selectedNodes.isEmpty()) {
                    try {
                        results.put("no node selected");
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    // retrieve the textual attributes of these nodes
                    // and compute the top terms of these
                    TopTermExtractor topTermExtractor = new TopTermExtractor();
                    List<String> selectedNodesIds = selectedNodes.stream().map(Node::getId).map(Object::toString).collect(Collectors.toList());

                    String topTermsExtractorFromSelectedNodes = topTermExtractor.topTermsExtractorFromSelectedNodes(selectedNodesIds, nbTopTerms);
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
                }
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
        return false;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

}
