/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf.algorithm;

import org.gephi.graph.api.Graph;

/**
 *
 * @author puig
 */

/*
This is a interface to make it transparent to the partitioncontroller
It implements the partition algorithms
All should have a contructor with the graph and be created using the GraphParitionAlgorithmFactory
*/
public interface IGraphPartitionAlgorithm {
    Graph doPartition();
}
