package uk.ac.kent.dover.fastGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Graph isomorphism
 *
 * This class will determine if two graphs are isomorphic or not.
 *
 * @author Romain Vermot <rfmv2@kent.ac.uk>
 */
public class Isomorphism {
	private FastGraph graph1;
	private FastGraph graph2;

	private int numberOfNodes;
	private int maximumDegree;
	private int maximumOutDegree;

	private HashSet<Integer> nodeAssignedInGraph1;
	private HashSet<Integer> nodeAssignedInGraph2;

	private ArrayList<HashSet<Integer>> neighbours1;
	private ArrayList<HashSet<Integer>> neighbours2;

	/**
	 * Constructor initialises the class with a graph.
	 *
	 * @param graph One graph to be tested
	 * @throws FastGraphException if the graph is not connected
	 */
	public Isomorphism(FastGraph graph) throws FastGraphException {
		// if the graph is disconnected
		if (!Connected.connected(graph)) {
			throw new FastGraphException("Graph must be connected to test for isomorphism.");
		}

		this.graph1 = graph;

		this.numberOfNodes = graph1.getNumberOfNodes();
		this.maximumDegree = graph1.maximumDegree();
		this.maximumOutDegree = graph1.maximumOutDegree();

		this.neighbours1 = ExactIsomorphism.findNeighbours(graph1, maximumDegree);

		this.nodeAssignedInGraph1 = new HashSet<>();
		this.nodeAssignedInGraph2 = new HashSet<>();
	}

	/**
	 * This method finds and returns a list of nodes with the most adjacent nodes.
	 *
	 * @param graph The graph to compute
	 * @return A list of vertices with the most adjacent vertices
	 */
	private HashSet<Integer> getNodesWithMaximumOutDegree(FastGraph graph)  {
		HashSet<Integer> nodes = new HashSet<>();

		for (int i = 0; i < numberOfNodes; i++) {
			if (graph.getNodeOutDegree(i) == maximumOutDegree) {
				nodes.add(i);
			}
		}
		System.out.println();
		return nodes;
	}

	/**
	 * This recursive method determines if two graphs are isomorphic.
	 *
	 * If each node is assigned, then it returns true.
	 * Else it will start the backtracking and try to assign each node.
	 * When there is no solution, it goes back and unassigned nodes to try another solution.
	 *
	 * @return The result
	 */
	private boolean areIsomorphicRecursion(HashSet<Integer> nodes1, HashSet<Integer> nodes2) {
		// if with have associated every node, work is done
		if (numberOfNodes == nodeAssignedInGraph2.size()) {
			return true;
		}

		int n1, n2;
		Iterator it1, it2;

		// iterates the first list
		it1 = nodes1.iterator();
		while (it1.hasNext()) {
			n1 = (int) it1.next();
			// if node is not assigned in graph 1
			if (!nodeAssignedInGraph1.contains(n1)) {
				// iterates the second list
				it2 = nodes2.iterator();
				while (it2.hasNext()) {
					n2 = (int) it2.next();
					// if node is not assigned in graph 2
					if (!nodeAssignedInGraph2.contains(n2)) {
						HashSet<Integer> nextNodes1 = neighbours1.get(n1);
						HashSet<Integer> nextNodes2 = neighbours2.get(n2);
						// if the two nodes that compare have the same number of edges
						if (nextNodes1.size() == nextNodes2.size()) {

							// we assigned nodes of graph 1 and 2
							nodeAssignedInGraph1.add(n1);
							nodeAssignedInGraph2.add(n2);

							// we starts the recursion with neighbours
							if (areIsomorphicRecursion(nextNodes1, nextNodes2)) {
								return true;
							}

							// the recursion failed, so we unassigned nodes
							nodeAssignedInGraph1.remove(n1);
							nodeAssignedInGraph2.remove(n2);
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * This method determines if two graphs are isomorphic.
	 *
	 * Firstly, it check basic things as the number of nodes and secondly
	 * retrieves nodes with the most edges in each graph and then calls a private
	 * method that will compute the result by using a backtracking algorithm.
	 *
	 * The main idea is to associate each node of the first graph with nodes of the second graph.
	 *
	 * @param graph The graph to compare
	 * @return true if there is an equality with the given graph, false if is not
	 * @throws FastGraphException if the graph is not connected
	 */
	public boolean isomorphic(FastGraph graph) throws FastGraphException {
		// if the graph is disconnected
		if (!Connected.connected(graph)) {
			throw new FastGraphException("Graph must be connected to test for isomorphism.");
		}

		// if the number of nodes differs between the two graphs return false
		if (numberOfNodes != graph.getNumberOfNodes()) {
			return false;
		}

		this.graph2 = graph;

		// finds nodes with most edges
		HashSet<Integer> entryPoints1 = getNodesWithMaximumOutDegree(graph1);
		HashSet<Integer> entryPoints2 = getNodesWithMaximumOutDegree(graph2);

		// if the number of nodes with the most edges differs between the two graphs return false
		if (entryPoints1.size() != entryPoints2.size()) {
			return false;
		}

		this.neighbours2 = ExactIsomorphism.findNeighbours(graph2, maximumDegree);

		// starts the recursion
		return areIsomorphicRecursion(entryPoints1, entryPoints2);
	}

	/**
	 * Check if two graphs are isomorphic.
	 *
	 * Use the constructor and non static method if ots of comparisons are
	 * going to be made against the same graph.
	 *
	 * @param g1 one FastGraph to be tested
	 * @param g2 the other FastGraph to be tested
	 * @return true if there is an equality with the given graph, false if is not
	 * @throws FastGraphException if the graph is not connected
	 */
	public static boolean isomorphic(FastGraph g1, FastGraph g2) throws FastGraphException {
		Isomorphism i = new Isomorphism(g1);

		return i.isomorphic(g2);
	}
}
