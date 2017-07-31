package uk.ac.kent.dover.fastGraph.isomorphism;

import uk.ac.kent.dover.fastGraph.Connected;
import uk.ac.kent.dover.fastGraph.ExactIsomorphism;
import uk.ac.kent.dover.fastGraph.FastGraph;
import uk.ac.kent.dover.fastGraph.FastGraphException;

import java.math.BigInteger;
import java.util.*;

/**
 * Graph isomorphism
 * Copyright (c) 2017 Romain Vermot
 *
 * This class is based on the work of Gilleain Torrance:
 * https://github.com/gilleain/group
 *
 * Refines vertex partitions until they are discrete, and therefore equivalent
 * to permutations. These permutations are automorphisms of the graph that was
 * used during the refinement to guide the splitting of partition blocks.
 *
 * @author maclean
 * @author Romain Vermot <rfmv2@kent.ac.uk>
 */
public class FastGraphIsomorphism {
	/**
	 * The result of a comparison between the current partition
	 * and the best permutation found so far.
	 *
	 */
	public enum Result { WORSE, EQUAL, BETTER }

	/**
	 * The block of the partition that is being refined
	 */
	private int currentBlockIndex;

	/**
	 * The blocks to be refined, or at least considered for refinement
	 */
	private Queue<Set<Integer>> blocksToRefine;

	/**
	 * If true, then at least one partition has been refined
	 * to a permutation (IE: to a discrete partition).
	 */
	private boolean bestExist;

	/**
	 * The best permutation is the one that gives the maximal
	 * half-matrix string (so far) when applied to the graph.
	 */
	private Permutation best;

	/**
	 * The first permutation seen when refining.
	 */
	private Permutation first;

	/**
	 * The automorphism group that is used to prune the search.
	 */
	private PermutationGroup group;

	/**
	 * The current graph
	 */
	private FastGraph graph;

	/**
	 * The neighbours of nodes in graph
	 */
	private ArrayList<HashSet<Integer>> neighbours;

	/**
	 * This class contains a FastGraph class and has methods to
	 * check the isomorphism of two graphs.
	 *
	 * @param graph the graph
	 * @throws FastGraphException if the graph is not connected
	 */
	public FastGraphIsomorphism(FastGraph graph) throws FastGraphException {
		// if the graph is disconnected
		if (!Connected.connected(graph)) {
			throw new FastGraphException("Graph must be connected to test for isomorphism.");
		}
		this.graph = graph;
		this.bestExist = false;
		this.best = null;
		this.neighbours = ExactIsomorphism.findNeighbours(graph, graph.maximumOutDegree());
	}

	/**
	 * Find |a &cap; b| - that is, the size of the intersection between a and b.
	 *
	 * @param block a set of numbers
	 * @param vertexIndex the node to compare
	 * @return the size of the intersection
	 */
	public int neighboursInBlock(Set<Integer> block, int vertexIndex) {
		Map<Integer, Integer> neighbourCounts = new HashMap<>();
		Iterator it;
		int n;

		for (Integer elem : block) {
			HashSet<Integer> elemNeighbours = neighbours.get(elem);
			it = elemNeighbours.iterator();
			while (it.hasNext()) {
				n = (int) it.next();

				if (neighbourCounts.containsKey(n)) {
					neighbourCounts.put(n, neighbourCounts.get(n) + 1);
				} else {
					neighbourCounts.put(n, 1);
				}
			}
		}
		return neighbourCounts.containsKey(vertexIndex) ? neighbourCounts.get(vertexIndex) : 0;
	}

	/**
	 * Get the connectivity between two vertices as an integer, to allow
	 * for multigraphs : so a single edge is 1, a double edge 2, etc. If
	 * there is no edge, then 0 should be returned.
	 *
	 * @param vertexI a vertex of the graph
	 * @param vertexJ a vertex of the graph
	 * @return the multiplicity of the edge (0, 1, 2, 3, ...)
	 */
	public int getConnectivity(int vertexI, int vertexJ) {
		HashSet<Integer> elemNeighbours = neighbours.get(vertexI);
		Iterator it;
		int connectivity = 0, n;

		it = elemNeighbours.iterator();
		while (it.hasNext()) {
			n = (int) it.next();
			if (vertexJ == n) {
				connectivity++;
			}
		}
		return connectivity;
	}

	/**
	 * Setup the group and refiner; it is important to call this method before
	 * calling {@link #refine} otherwise the refinement process will fail.
	 *
	 * @param group a group (possibly empty) of automorphisms
	 */
	public void setup(PermutationGroup group) {
		this.bestExist = false;
		this.best = null;
		this.group = group;
	}

	/**
	 * Refines the coarse partition <code>a</code> into a finer one.
	 *
	 * @param a the partition to refine
	 * @return a finer partition
	 */
	public Partition refine(Partition a) {
		Partition b = new Partition(a);
		// System.out.println("Refining " + a);

		blocksToRefine = new LinkedList<>();
		for (int i = 0; i < b.size(); i++) {
			blocksToRefine.add(b.copyBlock(i));
		}

		int numberOfVertices = graph.getNumberOfNodes();
		while (!blocksToRefine.isEmpty()) {
			Set<Integer> t = blocksToRefine.remove();
			currentBlockIndex = 0;
			while (currentBlockIndex < b.size() && b.size() < numberOfVertices) {
				if (!b.isDiscreteCell(currentBlockIndex)) {

					// get the neighbor invariants for this block
					Map<Integer, SortedSet<Integer>> invariants = getInvariants(b, t);

					// split the block on the basis of these invariants
					split(invariants, b);
					// System.out.println(blocksToRefine);
				}
				currentBlockIndex++;
			}
			// System.out.println(b);

			// the partition is discrete
			if (b.size() == numberOfVertices) {
				return b;
			}
		}
		// System.out.println("To " + b);
		return b;
	}

	/**
	 * Gets the neighbor invariants for the block j as a map of
	 * |N<sub>g</sub>(v) &cap; T| to elements of the block j. That is, the
	 * size of the intersection between the set of neighbors of element v in
	 * the graph and the target block T.
	 *
	 * @param partition the current partition
	 * @param targetBlock the current target block of the partition
	 * @return a map of set intersection sizes to elements
	 */
	private Map<Integer, SortedSet<Integer>> getInvariants(Partition partition, Set<Integer> targetBlock) {
		Map<Integer, SortedSet<Integer>> setList = new HashMap<>();
		for (int u : partition.getCell(currentBlockIndex)) {
			Integer h = neighboursInBlock(targetBlock, u);
			if (setList.containsKey(h)) {
				setList.get(h).add(u);
			} else {
				SortedSet<Integer> set = new TreeSet<>();
				set.add(u);
				setList.put(h, set);
			}
		}
		// System.out.println("current block " + partition.getCell(currentBlockIndex) +
		// " target block " + targetBlock + " inv " + setList);
		return setList;
	}

	/**
	 * Split the current block using the invariants calculated in getInvariants.
	 *
	 * @param invariants a map of neighbor counts to elements
	 * @param partition the partition that is being refined
	 */
	private void split(Map<Integer, SortedSet<Integer>> invariants, Partition partition) {
		int nonEmptyInvariants = invariants.keySet().size();
		if (nonEmptyInvariants > 1) {
			List<Integer> invariantKeys =  new ArrayList<>();
			// System.out.println(invariants.keySet());
			invariantKeys.addAll(invariants.keySet());
			partition.removeCell(currentBlockIndex);
			int k = currentBlockIndex;
			Collections.sort(invariantKeys);
			for (Integer h : invariantKeys) {
				SortedSet<Integer> setH = invariants.get(h);
				// System.out.println("adding block " + setH + " at " + k + " h=" + h);
				partition.insertCell(k, setH);
				blocksToRefine.add(setH);
				k++;

			}
			// skip over the newly added blocks
			currentBlockIndex += nonEmptyInvariants - 1;
		}
	}

	public BigInteger getCertificate() {
		return calculateCertificate(this.getBest());
	}

	public BigInteger calculateCertificate(Permutation p) {
		int k = 0;
		BigInteger certificate = BigInteger.valueOf(0);
		int n = graph.getNumberOfNodes();
		for (int j = n - 1; j > 0; j--) {
			for (int i = j - 1; i >= 0; i--) {
				if (getConnectivity(p.get(i), p.get(j)) > 0) {
					certificate = certificate.add(BigInteger.valueOf((int)Math.pow(2, k)));
				}
				k++;
			}
		}
		return certificate;
	}

	/**
	 * Get the automorphism group used to prune the search.
	 *
	 * @return the automorphism group
	 */
	public PermutationGroup getAutomorphismGroup() {
		return this.group;
	}

	/**
	 * Get the best permutation found.
	 *
	 * @return the permutation that gives the maximal half-matrix string
	 */
	public Permutation getBest() {
		return this.best;
	}

	/**
	 * Get the first permutation reached by the search.
	 *
	 * @return the first permutation reached
	 */
	public Permutation getFirst() {
		return this.first;
	}

	/**
	 * Check for a canonical graph, without generating the whole
	 * automorphism group.
	 *
	 * @return true if the graph is canonical
	 */
	public boolean isCanonical() {
		return isCanonical(Partition.unit(graph.getNumberOfNodes()));
	}

	public boolean isCanonical(Partition partition) {
		int n = graph.getNumberOfNodes();
		if (partition.size() == n) {
			return partition.toPermutation().isIdentity();
		} else {
			int l = partition.getIndexOfFirstNonDiscreteCell();
			int first = partition.getFirstInCell(l);
			Partition finerPartition =
					refine(partition.splitBefore(l, first));
			return isCanonical(finerPartition);
		}
	}

	public int getNumberOfNodes() {
		return graph.getNumberOfNodes();
	}

	/**
	 * Refine the partition. The main entry point for subclasses.
	 *
	 * @param partition the initial partition of the vertices
	 */
	public void canon(Partition partition) {
		canon(this.group, partition);
	}

	/**
	 * Does the work of the class, that refines a coarse partition into a finer
	 * one using the supplied automorphism group to prune the search.
	 *
	 * @param group the automorphism group of the graph
	 * @param coarser the partition to refine
	 */
	public void canon(PermutationGroup group, Partition coarser) {
		int vertexCount = graph.getNumberOfNodes();

		Partition finer = refine(coarser);

		int firstNonDiscreteCell = finer.getIndexOfFirstNonDiscreteCell();
		if (firstNonDiscreteCell == -1) {
			firstNonDiscreteCell = vertexCount;
		}

		Permutation pi1 = new Permutation(firstNonDiscreteCell);
		// System.out.println(finer);
		Result result = Result.BETTER;
		if (bestExist) {
			pi1 = finer.setAsPermutation(firstNonDiscreteCell);
			result = compare(pi1);
		}

		// partition is discrete
		if (finer.size() == vertexCount) {
			if (!bestExist) {
				best = finer.toPermutation();
				first = finer.toPermutation();
				bestExist = true;
			} else {
				if (result == Result.BETTER) {
					best = new Permutation(pi1);
				} else if (result == Result.EQUAL) {
					group.enter(pi1.multiply(best.invert()));
				}
			}
		} else {
			if (result != Result.WORSE) {
				Set<Integer> blockCopy = finer.copyBlock(firstNonDiscreteCell);
				for (int vertexInBlock = 0; vertexInBlock < vertexCount; vertexInBlock++) {
					if (blockCopy.contains(vertexInBlock)) {
						Partition nextPartition =
								finer.splitBefore(firstNonDiscreteCell, vertexInBlock);

						this.canon(group, nextPartition);

						int[] permF = new int[vertexCount];
						int[] invF = new int[vertexCount];
						for (int i = 0; i < vertexCount; i++) {
							permF[i] = i;
							invF[i] = i;
						}

						for (int j = 0; j <= firstNonDiscreteCell; j++) {
							int x = nextPartition.getFirstInCell(j);
							int i = invF[x];
							int h = permF[j];
							permF[j] = x;
							permF[i] = h;
							invF[h] = i;
							invF[x] = j;
						}
						Permutation pPermF = new Permutation(permF);
						group.changeBase(pPermF);
						for (int j = 0; j < vertexCount; j++) {
							Permutation g = group.get(firstNonDiscreteCell, j);
							if (g != null) {
								blockCopy.remove(g.get(vertexInBlock));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Checks a permutation to see if it is better, equal, or worse than the
	 * current best.
	 *
	 * @param perm the permutation to check
	 * @return BETTER, EQUAL, or WORSE
	 */
	private Result compare(Permutation perm) {
		int m = perm.size();
		for (int i = 0; i < m - 1; i++) {
			for (int j = i + 1; j < m; j++) {
				int x = getConnectivity(best.get(i), best.get(j));
				int y = getConnectivity(perm.get(i), perm.get(j));
				if (x > y) return Result.BETTER;
				if (x < y) return Result.WORSE;
			}
		}
		return Result.EQUAL;
	}
}
