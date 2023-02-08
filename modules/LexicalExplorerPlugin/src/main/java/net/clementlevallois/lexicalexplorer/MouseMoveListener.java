/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.clementlevallois.lexicalexplorer;

import java.util.List;
import java.util.stream.Collectors;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizEvent;
import org.gephi.visualization.apiimpl.VizEventListener;

/**
 *
 * @author LEVALLOIS
 */
public class MouseMoveListener implements VizEventListener {

    @Override
    public void handleEvent(VizEvent ve) {
        if (VizController.getInstance().getSelectedNodes().isEmpty()) {
            return;
        }
        List<Node> selectedNodes = VizController.getInstance().getSelectedNodes();
        List<String> selectedNodesIds = selectedNodes.stream().map(Node::getId).map(Object::toString).collect(Collectors.toList());
        TopTermExtractor topTermExtractor = new TopTermExtractor();
        Integer numberTopTerms = (Integer) LexplorerTopComponent.jSpinnerNumberTopTerms.getValue();
        String topTermsExtractorFromSelectedNodes = topTermExtractor.topTermsExtractorFromSelectedNodes(selectedNodesIds, numberTopTerms);
        LexplorerTopComponent.placeHolderForTopTerms.setText(topTermsExtractorFromSelectedNodes);
    }

    @Override
    public VizEvent.Type getType() {
        return VizEvent.Type.MOUSE_MOVE;
    }

}
