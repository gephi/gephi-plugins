package Random.p;/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

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

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */

import lombok.Getter;
import lombok.Setter;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

import java.util.Random;

/**
 * Filter that detects crossing edge and randomly deletes one of the crossing
 * edge.
 * <p>
 * This example shows how to implement a <code>ComplexFilter<</code>. Unlike
 * <code>NodeFilter</code> or <code>EdgeFilter</code> which acts on a single
 * element this filter has the freedom to transform the whole graph structure.
 * It is suitable for recursive deletion or when the filter depends on both
 * nodes and edges. A filter works by deleting objects so the role of a complex
 * filter is to read nodes/edges and delete some of them according to the
 * parameters.
 * <p>
 * This example doesn't have parmeters but the mechanism is the same as other
 * filters.
 *
 * @author Mathieu Bastian
 */
public class RandomPRemovalStrategy implements ComplexFilter {

    @Getter @Setter
    private Double P = 0.1;
    @Getter @Setter
    private Integer Seed = 0;

    @Override
    public String getName() {
        return "Remove nodes with P probability";
    }

    @Override
    public FilterProperty[] getProperties(){
        try{
            return new FilterProperty[]{
                    FilterProperty.createProperty(this, P.getClass(), "P"),
                    FilterProperty.createProperty(this, Seed.getClass(), "Seed")
            };
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public Graph filter(Graph graph) {
        Node[] nodes = graph.getNodes().toArray();

        Random random = (Seed == 0) ? new Random() : new Random(Seed);

        for(int i =0; i< nodes.length; i++){
            var p = random.nextDouble();
            if(p <= P){
                graph.removeNode(nodes[i]);
            }
        }
        return graph;
    }
}