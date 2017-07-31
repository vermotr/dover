package uk.ac.kent.dover.fastGraph.isomorphism;

import uk.ac.kent.dover.fastGraph.FastGraph;
import uk.ac.kent.dover.fastGraph.FastGraphException;

import java.util.ArrayList;
import java.util.List;

/**
 * Graph isomorphism
 * Copyright (c) 2017 Romain Vermot
 *
 * This class will determine if two graphs are isomorphic or not.
 *
 * @author Romain Vermot <rfmv2@kent.ac.uk>
 */
public class Isomorphism {
	private FastGraphIsomorphism graph1;

	/**
	 * Constructor initialises the class with a graph.
	 *
	 * @param graph One graph to be tested
	 */
	public Isomorphism(FastGraphIsomorphism graph) {
		this.graph1 = graph;
	}

	/**
	 * Check if two graphs are isomorphic.
	 *
	 * @param g1 one FastGraph to be tested
	 * @param g2 the other FastGraph to be tested
	 * @return true if there is an equality with the given graph, false if is not
	 * @throws FastGraphException if one of the two graphs are not connected
	 */
	public static boolean isomorphic(FastGraph g1, FastGraph g2) throws FastGraphException {
		FastGraphIsomorphism fgi1, fgi2;

		fgi1 = new FastGraphIsomorphism(g1);
		fgi2 = new FastGraphIsomorphism(g2);
		Isomorphism i = new Isomorphism(fgi1);
		return i.isomorphic(fgi2);
	}

	/**
	 * Check if two graphs are isomorphic.
	 *
	 * @param graph2 The graph to compare
	 * @return true if there is an equality with the given graph, false if is not
	 */
	public boolean isomorphic(FastGraphIsomorphism graph2) {
		if (graph1.getNumberOfNodes() != graph2.getNumberOfNodes()) {
			return false;
		}

		Partition partition1 = new Partition();
		List<Integer> elements1 = new ArrayList<>();
		for (int i = 0, len = graph1.getNumberOfNodes(); i < len; i++) {
			elements1.add(i);
		}
		partition1.addCell(elements1);
		graph1.setup(new PermutationGroup(graph1.getNumberOfNodes()));
		graph1.canon(partition1);

		Partition partition2 = new Partition();
		List<Integer> elements2 = new ArrayList<>();
		for (int i = 0, len = graph2.getNumberOfNodes(); i < len; i++) {
			elements2.add(i);
		}
		partition2.addCell(elements2);
		graph2.setup(new PermutationGroup(graph2.getNumberOfNodes()));
		graph2.canon(partition2);

		return graph1.getCertificate().equals(graph2.getCertificate());
	}
}
