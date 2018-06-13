/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf;

import org.gephi.graph.api.Graph;

/**
 *
 * @author puig
 */
public interface IPartitionGraph {
    void setGraph(Graph oldGraph);
    void partition();
    Graph getGraph();
}
