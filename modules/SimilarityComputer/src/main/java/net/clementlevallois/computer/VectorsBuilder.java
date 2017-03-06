package net.clementlevallois.computer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Multiset;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.clementlevallois.utils.Clock;
import no.uib.cipr.matrix.sparse.SparseVector;

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
public class VectorsBuilder {

    private final Map<String, Map<String, Integer>> attributesToValues = new TreeMap();
    private BiMap<String, Integer> mapNodesBuilder = HashBiMap.create();
    private Map<String, Map<String, Multiset<String>>> datastruct;
    private Set<String> setNodes = new TreeSet();
    private ImmutableBiMap<String, Integer> immutableBiMap;

    public VectorsBuilder(Map<String, Map<String, Multiset<String>>> datastruct) {
        this.datastruct = datastruct;
    }

    public ImmutableBiMap<String, Integer> dataPreparation() {
        //***
        //
        //#### 1. reading the data structure and preparing sets and maps from it
        //
        //***

        int n = 0;

        for (String key : datastruct.keySet()) {

            setNodes.add(key);
            mapNodesBuilder.put(key, n);
            n++;

            Map<String, Multiset<String>> attributes = datastruct.get(key);

            //filling the map for all attributes and all their values
            for (String attribute : attributes.keySet()) {
                Map<String, Integer> valuesToIndex = attributesToValues.get(attribute);
                if (valuesToIndex == null) {
                    valuesToIndex = new TreeMap();
                }
                Set<String> values = attributes.get(attribute).elementSet();
                for (String value : values) {
                    if (!valuesToIndex.keySet().contains(value)) {
                        valuesToIndex.put(value, (valuesToIndex.size() - 1) + 1);
                    }
                }
                attributesToValues.put(attribute, valuesToIndex);
            }

        }

        Logger.getLogger("").log(Level.INFO, "Number of nodes treated: {0}", datastruct.keySet().size());
        Logger.getLogger("").log(Level.INFO, "Size of the list of vectors: {0}", setNodes.size());

        immutableBiMap = ImmutableBiMap.copyOf(mapNodesBuilder);

        return immutableBiMap;
    }

    public Map<String, SparseVector[]> sparseVectorArrayBuilder() throws IOException {

        //***
        //
        //#### 2. looping through each node, and constructing vectors from it.
        //
        //***
        Clock matrixCreation = new Clock("creating the adjacency matrices for each attribute from the file");

        // FIRST LOOP: through all attributes
        Iterator<String> attributesToValuesIt = attributesToValues.keySet().iterator();

        Map<String, SparseVector[]> attributeTolistVectors = new HashMap();

        SparseVector[] listVectors;
        while (attributesToValuesIt.hasNext()) {
            String attribute = attributesToValuesIt.next();

            listVectors = new SparseVector[setNodes.size()];

            Iterator<String> nodesIt = setNodes.iterator();

            Map<String, Multiset<String>> attributes;

            // INNER LOOP: for a given attribute: loop through all nodes.
            while (nodesIt.hasNext()) {

                String node = nodesIt.next();

                SparseVector vectorMJT = new SparseVector(attributesToValues.get(attribute).size());

                attributes = datastruct.get(node);

                Multiset<String> values = attributes.get(attribute);

                for (String value : values.elementSet()) {
                    int vectorPos = attributesToValues.get(attribute).get(value);
                    vectorMJT.set(vectorPos, (double) values.count(value));
                }
                listVectors[immutableBiMap.get(node)] = vectorMJT;
            }
            attributeTolistVectors.put(attribute, listVectors);

        }

        Logger.getLogger("").log(Level.INFO, "adjacency matrix created!");

        matrixCreation.closeAndPrintClock();

        return attributeTolistVectors;

    }
}
