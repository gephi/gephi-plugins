/**
 * Provides data structures and algorithms for network analysis.
 *
 * <p>
 * Currently, the focus of this package is restricted to clustering algorithms
 * (also known as community detection algorithms) and the associated data
 * structures.
 * </p>
 *
 * <p>
 * The classes {@link cwts.networkanalysis.Network} and {@link
 * cwts.networkanalysis.Clustering} represent the core data structures. The
 * classes {@link cwts.networkanalysis.LeidenAlgorithm} and {@link
 * cwts.networkanalysis.LouvainAlgorithm} represent the core clustering
 * algorithms. These two classes are embedded in a hierarchy of classes and
 * interfaces for representing clustering algorithms.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
package cwts.networkanalysis;
