/*
 * author: Clement Levallois
 */
package net.clementlevallois.wordcloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;
import net.clementlevallois.stopwords.StopWordsRemover;
import net.clementlevallois.umigon.model.TextFragment;
import net.clementlevallois.umigon.model.TypeOfTextFragment.TypeOfTextFragmentEnum;
import net.clementlevallois.umigon.tokenizer.controller.UmigonTokenizer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.openide.util.Exceptions;

/**
 *
 * @author LEVALLOIS
 */
public class TopTermExtractor {

    Boolean initialAnalysisInterruptedByUser = false;

    public static DefaultListModel<String> returnListOfLanguages() {
        DefaultListModel<String> listModel = new DefaultListModel();
        String[] SUPPORTED_LANGUAGES = StaticProperties.SUPPORTED_LANGUAGES;
        for (String lang : SUPPORTED_LANGUAGES) {
            listModel.addElement(lang);
        }
        return listModel;
    }

    public boolean tokenizeSelectedTextualAttributeForTheEntireGraph(GraphModel gm, String attributeName, List<String> langs) {

        Graph graph = gm.getGraph();
        graph.readLock();
        initialAnalysisInterruptedByUser = false;

        // selecting the column corresponding to the attribute we want to analyze
        Column attributeToBeAnalyzed = gm.getNodeTable().getColumn(attributeName);

        // we will iterate / loop on the nodes of the graph
        NodeIterable nodes = graph.getNodes();

        // we will store the text of the attribute for each node in a list
        Map<Node, String> textsFromTheAttribute = new HashMap();

        // doing the iteration now
        Iterator<Node> iteratorOnNodes = nodes.iterator();
        while (iteratorOnNodes.hasNext() & !initialAnalysisInterruptedByUser) {
            Node node = iteratorOnNodes.next();
            String descriptionForOneNode = (String) node.getAttribute(attributeToBeAnalyzed);
            if (descriptionForOneNode != null && !descriptionForOneNode.isBlank()) {
                textsFromTheAttribute.put(node, descriptionForOneNode.toLowerCase());
            }
        }
        graph.readUnlockAll();

        Set<String> languageSpecificLexicon = new HashSet();

        /*
        1. we loop through the list of textual descriptions and tokenize them
        2. The result is a list of TextFragment objects
        3. We keep only the text fragments that are not punctuation signs nor whitespaces
         */
        Map<Node, List<String>> mapOfNodesToTheirTextFragments = new HashMap();

        List<StopWordsRemover> stopWordsRemovers = new ArrayList();

        if (langs == null) {
            langs = new ArrayList();
            langs.add(StaticProperties.DEFAULT_TEXT_LANGUAGE);
        }
        for (String lang : langs) {
            StopWordsRemover stopWordsRemover = new StopWordsRemover(3, lang);
            stopWordsRemovers.add(stopWordsRemover);
        }

        Iterator<Map.Entry<Node, String>> iteratorOnNodesAndTheirTextualAttribute = textsFromTheAttribute.entrySet().iterator();
        while (iteratorOnNodesAndTheirTextualAttribute.hasNext() & !initialAnalysisInterruptedByUser) {
            try {
                Map.Entry<Node, String> next = iteratorOnNodesAndTheirTextualAttribute.next();
                String textualAttribute = next.getValue();
                Node node = next.getKey();
                List<TextFragment> textFragmentsForOneDescription = UmigonTokenizer.tokenize(textualAttribute, languageSpecificLexicon);
                for (TextFragment oneTextFragment : textFragmentsForOneDescription) {
                    if (oneTextFragment.getTypeOfTextFragmentEnum() == TypeOfTextFragmentEnum.WHITE_SPACE
                            || oneTextFragment.getTypeOfTextFragmentEnum() == TypeOfTextFragmentEnum.PUNCTUATION
                            || oneTextFragment.getOriginalForm().length() < StaticProperties.MIN_WORD_LENGTH) {
                        continue;
                    }
                    boolean removeIt = false;
                    for (StopWordsRemover swr : stopWordsRemovers) {
                        removeIt = swr.shouldItBeRemoved(oneTextFragment.getOriginalForm());
                        if (removeIt) {
                            break;
                        }
                    }
                    if (!removeIt) {
                        List<String> textFragmentsForThisNode = mapOfNodesToTheirTextFragments.getOrDefault(node, new ArrayList());
                        textFragmentsForThisNode.add(oneTextFragment.getOriginalForm());
                        mapOfNodesToTheirTextFragments.put(node, textFragmentsForThisNode);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }

        DataManager.setMapOfNodeIdsToTheirTextFragments(mapOfNodesToTheirTextFragments);

        return true;
    }

    public String topTermsExtractorFromSelectedNodes(List<Node> selectedNodes, int maxNumberOfTerms) {
        Map<Node, List<String>> mapOfNodeIdsToTheirTextFragments = DataManager.getMapOfNodeIdsToTheirTextFragments();

        List<String> flattenedListOfTermsForSelectedNodes = selectedNodes.stream()
                .filter(mapOfNodeIdsToTheirTextFragments::containsKey)
                .map(mapOfNodeIdsToTheirTextFragments::get)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // once we have all the terms, we can sort the terms from the most to the least frequent and select the top n
        List<Map.Entry<String, Long>> topNList = flattenedListOfTermsForSelectedNodes.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(maxNumberOfTerms)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Long> tf : topNList) {
            sb
                    .append(tf.getKey())
                    .append(" (")
                    .append(tf.getValue())
                    .append(")")
                    .append("<br/>");
        }
        return sb.toString();
    }

    public void setInitialAnalysisInterruptedByUser(Boolean initialAnalysisInterruptedByUser) {
        this.initialAnalysisInterruptedByUser = initialAnalysisInterruptedByUser;
    }

}
