package net.clementlevallois.computer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import net.clementlevallois.utils.Clock;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
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
public class CosineCalculation implements Callable {

    private final SparseVector[] listVectors;
    private final String attribute;

    public CosineCalculation(String attribute, SparseVector[] listVectors) {
        this.listVectors = listVectors;
        this.attribute = attribute;
    }

    @Override
    public FlexCompColMatrix call() throws Exception {
        SparseVector oneVector;
        SparseVector anotherVector;

        //the number of nodes which will appear in the final similarity network
        int numNodes = listVectors.length;

        //this is where the adjacency matrix for the final network is built
        FlexCompColMatrix similarityMatrix = new FlexCompColMatrix(numNodes, numNodes);

        //1. iteration through all nodes of the similarityMatrix
        ArrayList<Double> norms = new ArrayList();

        Clock matrixClock = new Clock("clocking the cosine calculus for " + attribute);

        for (int i = 0; i < numNodes; i++) {

            oneVector = listVectors[i];
            norms.add(oneVector.norm(Vector.Norm.Two));

            for (int j = 0; j < numNodes; j++) {
                if (listVectors[j] == null) {
                    continue;
                }

                if (j < i) {
                    anotherVector = listVectors[j];

                    synchronized (similarityMatrix) {
                        int[] sourceIndexes = oneVector.getIndex();
                        List<Integer> listSourceIndex = new ArrayList();
                        for (int s = 0; s < sourceIndexes.length; s++) {
                            listSourceIndex.add(sourceIndexes[s]);
                        }
                        int[] targetIndexes = anotherVector.getIndex();

                        List<Integer> listTargetIndex = new ArrayList();
                        for (int s = 0; s < targetIndexes.length; s++) {
                            listTargetIndex.add(targetIndexes[s]);
                        }

                        listSourceIndex.retainAll(listTargetIndex);
                        if (!listSourceIndex.isEmpty()) {
                            double result = oneVector.dot(anotherVector) / (norms.get(i) * norms.get(j));
                            similarityMatrix.set(i, j, result);
                        }
                    }
                } else {
                    similarityMatrix.set(i, j, 0);
                }
            }
        }
        matrixClock.closeAndPrintClock();
        return similarityMatrix;
    }

}
