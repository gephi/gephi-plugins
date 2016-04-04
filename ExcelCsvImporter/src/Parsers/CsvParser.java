/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Parsers;

import Controller.MyFileImporter;
import Utils.Utils;
import com.csvreader.CsvReader;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Issue.Level;
import org.gephi.io.importer.api.NodeDraft;
import org.openide.util.Exceptions;

/*
 Copyright 2008-2013 Clement Levallois
 Authors : Clement Levallois <clementlevallois@gmail.com>
 Website : http://www.clementlevallois.net


 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Clement Levallois. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s): Clement Levallois

 */
public class CsvParser {

    String filePath;
    public CsvReader csvReader;
    String textDelimiter;
    String fieldDelimiter;
    Multiset<String> nodes = HashMultiset.create();
    Multiset<String> nodesFirst = HashMultiset.create();
    Multiset<String> nodesSecond = HashMultiset.create();
    Multiset<String> edges = HashMultiset.create();
    ContainerLoader container;

    public CsvParser(String filePath, String textDelimiter, String fieldDelimiter) {
        this.filePath = filePath;
        this.fieldDelimiter = fieldDelimiter;
        this.textDelimiter = textDelimiter;
        this.init();

    }

    public final void init() {
        FileReader myFileReader = null;
        try {
            String nameFile = filePath;
            myFileReader = new FileReader(nameFile);
            BufferedReader myBufferedReader = new BufferedReader(myFileReader);

            String textDelimiterAsAString = textDelimiter;
            String fieldDelimiterAsAString = fieldDelimiter;

            if (textDelimiter.equals("text delimiter")) {
                textDelimiterAsAString = "\"";
            }

            if (fieldDelimiter.equals("field delimiter")) {
                fieldDelimiterAsAString = ",";
            }
            fieldDelimiterAsAString = Utils.getCharacter(fieldDelimiter);

            char fieldDelimiterAsACharacter = fieldDelimiterAsAString.charAt(0);

            csvReader = new CsvReader(myBufferedReader, fieldDelimiterAsACharacter);
//            csvReader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
            csvReader.setDelimiter(fieldDelimiterAsACharacter);
            char textDelimiterAsACharacter = textDelimiterAsAString.charAt(0);
            csvReader.setTextQualifier(textDelimiterAsACharacter);
            csvReader.setUseTextQualifier(true);

        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public String[] getHeaders() throws IOException {

        csvReader.readHeaders();
        return csvReader.getHeaders();

    }

    public void convertToNetwork() throws IOException {

        container = MyFileImporter.container;
        container.setEdgeDefault(EdgeDefault.UNDIRECTED);

        String firstDelimiter;
        String secondDelimiter;
        firstDelimiter = Utils.getCharacter(MyFileImporter.firstConnectorDelimiter);
        secondDelimiter = Utils.getCharacter(MyFileImporter.secondConnectorDelimiter);
        boolean oneTypeOfAgent = MyFileImporter.getFirstConnectedAgent().equals(MyFileImporter.getSecondConnectedAgent());

        Set<String> linesFirstAgent = new HashSet();
        Set<String> linesSecondAgent = new HashSet();

        Integer lineCounter = 0;
        while (csvReader.readRecord()) {
            String firstAgent = csvReader.get(MyFileImporter.getFirstConnectedAgent());

            if (firstAgent == null || firstAgent.isEmpty()) {
                Issue issue = new Issue("problem with line " + lineCounter + " (empty column " + MyFileImporter.getFirstConnectedAgent() + "). It was skipped in the conversion", Level.WARNING);
                MyFileImporter.getStaticReport().logIssue(issue);
                continue;
            }
            if (MyFileImporter.removeDuplicates) {
                boolean newLine = linesFirstAgent.add(firstAgent);
                if (!newLine) {
                    continue;
                }
            }
            String secondAgent = null;
            if (!oneTypeOfAgent) {
                secondAgent = csvReader.get(MyFileImporter.getSecondConnectedAgent()).trim();

                if (secondAgent == null || secondAgent.isEmpty()) {
                    Issue issue = new Issue("problem with line " + lineCounter + " (empty column " + MyFileImporter.getSecondConnectedAgent() + "). It was skipped in the conversion", Level.WARNING);
                    MyFileImporter.getStaticReport().logIssue(issue);
                    continue;
                }

                if (MyFileImporter.removeDuplicates) {
                    boolean newLine = linesSecondAgent.add(secondAgent);
                    if (!newLine) {
                        continue;
                    }
                }

            }
            lineCounter++;

            String[] firstAgentSplit;
            String[] secondAgentSplit;

            if (firstDelimiter != null) {
                firstAgentSplit = firstAgent.trim().split(firstDelimiter);
            } else {
                firstAgentSplit = new String[1];
                firstAgentSplit[0] = firstAgent;
            }
            for (String node : firstAgentSplit) {
                node = node.trim();
                if (!node.isEmpty()) {
                    nodesFirst.add(node);
                    nodes.add(node);
                }
            }

            if (!oneTypeOfAgent) {

                if (secondDelimiter != null) {
                    secondAgentSplit = secondAgent.trim().split(secondDelimiter);
                } else {
                    secondAgentSplit = new String[1];
                    secondAgentSplit[0] = secondAgent;
                }
                for (String node : secondAgentSplit) {
                    node = node.trim();
                    if (!node.isEmpty()) {
                        nodesSecond.add(node);
                        nodes.add(node);
                    }
                }
            } else {
                secondAgentSplit = null;
            }

            //let's find all connections between all the agents in this row
            Utils usefulTools = new Utils();

            if (!MyFileImporter.innerLinksIncluded) {
                for (String x : firstAgentSplit) {
                    for (String xx : secondAgentSplit) {
                        if (!(MyFileImporter.removeSelfLoops & x.equals(xx))) {
                            if (!x.trim().isEmpty() & !x.trim().isEmpty()) {
                                edges.add(x.trim() + "|" + xx.trim());
                            }
                        }
                    }
                }
            } else {
                List<String> connections;
                String[] both = ArrayUtils.addAll(firstAgentSplit, secondAgentSplit);
                connections = usefulTools.getListOfLinks(both, MyFileImporter.removeSelfLoops);
                edges.addAll(connections);
            }

        }

        NodeDraft node;
        AttributeTable atNodes = container.getAttributeModel().getNodeTable();
        AttributeColumn acFrequency = atNodes.addColumn("frequency", AttributeType.INT);
        AttributeColumn acType = atNodes.addColumn("type", AttributeType.STRING);
        StringBuilder type;
        boolean atLeastOneType = false;

        for (String n : nodes.elementSet()) {
            node = container.factory().newNodeDraft();
            node.setId(n);
            node.setLabel(n);
            type = new StringBuilder();
            node.addAttributeValue(acFrequency, nodes.count(n));
            if (nodesFirst.contains(n)) {
                type.append(MyFileImporter.getFirstConnectedAgent());
                atLeastOneType = true;
            }
            if (nodesSecond.contains(n)) {
                if (atLeastOneType) {
                    type.append("; ");
                }
                type.append(MyFileImporter.getSecondConnectedAgent());
            }
            node.addAttributeValue(acType, type);
            container.addNode(node);
        }

        //loop for edges
        Integer idEdge = 0;
        EdgeDraft edge;
        for (String e : edges.elementSet()) {
//            System.out.println("edge: " + e);
            String sourceNode = e.split("\\|")[0].trim();
            String targetNode = e.split("\\|")[1].trim();

            edge = container.factory().newEdgeDraft();
            idEdge = idEdge + 1;
            edge.setSource(container.getNode(sourceNode));
            edge.setTarget(container.getNode(targetNode));
            edge.setWeight((float) edges.count(e));
            edge.setId(String.valueOf(idEdge));
            edge.setType(EdgeDraft.EdgeType.UNDIRECTED);
            container.addEdge(edge);
        }
    }
}
