package uk.ac.kent.dover.fastGraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import uk.ac.kent.displayGraph.Graph;

/**
 * Testing the structural similarity of two FastGraphs
 * 
 * @author Peter Rodgers
 *
 */
public class ExactIsomorphism {

	private int DECIMAL_PLACES = 6; // number of decimal places to round to
	
	private FastGraph fastGraph;
	private AdjacencyMatrix am1;
	private AdjacencyMatrix am2;
	private int[][] matrix1;
	private int[][] matrix2;
	private double[] eigenvalues1;
	private double[] eigenvalues2;

	
	private int[] matches1;
	private int[] matches2;

	private int[] degrees1;
	private int[] degrees2;

	private int[] degreeBuckets1; // how many of nodes each degree
	private int[] degreeBuckets2; // how many of nodes each degree
	
	private int maxDegree1;
	private int maxDegree2;

	private ArrayList<HashSet<Integer>> neighbours1;  // Non self-sourcing neighbour nodes for each node
	private ArrayList<HashSet<Integer>> neighbours2;  // Non self-sourcing neighbour nodes for each node

	private static int numberOfIsomorphismTests = 0;
	private static int numberOfOldIsomorphismTests = 0;
	private static int numberOfEigenvalueTests = 0;
	
	private static long timeForIsomorphismTests = 0;
	private static long timeForBruteForceTests = 0;
	private static long timeForOldIsomorphismTests = 0;
	private static long timeForEigenvalueTests = 0;
	private static long isomorphismStartTime = -1;
	private static long bruteForceStartTime = -1;
	
	private static int failOnNodeCount = 0;
	private static int failOnEdgeCount = 0;
	private static int failOnEigenvalues = 0;
	private static int failOnDegreeComparison = 0;
	private static int failOnNodeMatches = 0;
	private static int failOnBruteForce = 0;
	private static int succeed = 0;
	
	/**
	 * Call this after getting true from a call to isomorphism().
	 * 
	 * @return the nodes matched from graph if the last call to isomorphism() returned true, undefined otherwise
	 */
	public int[] getLastMatch() {
		return matches1;
	}
	
	/**
	 *
	 * Create an ExactIsomorphism before running isomorphic. This makes multiple tests against
	 * one graph to be more efficient as data for that graph does not need to be recreated. The
	 * graph must be connected.
	 * 
	 * @param fastGraph one graph to be tested.
	 * @throws FastGraphException if the graph is not connected
	 *
	 */
	public ExactIsomorphism(FastGraph fastGraph) throws FastGraphException {

		this.fastGraph = fastGraph;

		if(!Connected.connected(fastGraph)) {
			throw new FastGraphException("Graphs must be connected to test for isomorphism.");
		}
		am1 = new AdjacencyMatrix(fastGraph);
		if(fastGraph.getNumberOfNodes() == 0) {
			matrix1 = new int[0][0];
			eigenvalues1 = new double[0];
		} else {
			matrix1 = am1.buildIntAdjacencyMatrix();
			eigenvalues1 = am1.findEigenvalues(matrix1);
			eigenvalues1 = Util.roundArray(eigenvalues1,DECIMAL_PLACES);
		}
		
		matches1 = new int[fastGraph.getNumberOfNodes()];
		matches2 = new int[fastGraph.getNumberOfNodes()];
		
		degrees1 = fastGraph.findDegrees();
		
		maxDegree1 = fastGraph.maximumDegree();

		degreeBuckets1 = new int[maxDegree1+1];
		fastGraph.findDegreeBuckets(degreeBuckets1,degrees1);
		
		neighbours1 = findNeighbours(fastGraph,maxDegree1);
	}

	/**
	 * Generate a string that can be used to put graph in buckets before final brute force comparison.
	 * 
	 * @return a string based on calculated values
	 */
	protected String generateStringForHash() {
		
		StringBuffer sb = new StringBuffer();
		sb.append(Integer.toString(fastGraph.getNumberOfNodes()));
		sb.append(",");
		sb.append(Integer.toString(fastGraph.getNumberOfEdges()));
		sb.append(Arrays.toString(degreeBuckets1));
		sb.append(Arrays.toString(eigenvalues1));
		sb.append(generateTimeString());
		return sb.toString();

	}
	
	/**
	 * Generates a count of the nodes at each age.<br>
	 * Uses relative ages, so if a graph had 1 node of age 4, 2 of age 5 and 3 of age 6, would be represented as [1,2,3]
	 * @return The age string
	 */
	protected String generateTimeString() {
		int maxAge = fastGraph.findMaximumNodeAge();
		int minAge = fastGraph.findMinimumNodeAge();
		//Debugger.log("minAge: " + minAge + " maxAge: " + maxAge);
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int i = minAge; i < maxAge; i++) {
			sb.append(fastGraph.countNodesOfAge(i));
			sb.append(",");
		}
		sb.append(fastGraph.countNodesOfAge(maxAge));
		sb.append("]");
		return sb.toString();
	}


	/**
	 * gives the neighbours of nodes in g, without duplicates and without self sourcing.
	 * 
	 * @param g the graph
	 * @param maxDegree The maximum degree of the nodes
	 * @return the neighbours for each node in the graph
	 */
	public static ArrayList<HashSet<Integer>> findNeighbours(FastGraph g, int maxDegree) {
		
		ArrayList<HashSet<Integer>> ret = new ArrayList<HashSet<Integer>>(g.getNumberOfNodes());
		for(int n = 0; n < g.getNumberOfNodes(); n++) {
			HashSet<Integer> neighbours = new HashSet<Integer>(maxDegree);
			int[] connections = g.getNodeConnectingNodesOfSameAge(n);
			for(int i = 0; i < connections.length; i++) {
				Integer connectingNode = connections[i];
				if(n == connectingNode) {
					continue;
				}
				if(!neighbours.contains(connectingNode)) {
					neighbours.add(connectingNode);
				}
			}
			ret.add(neighbours);
		}
		return ret;
	}

	/**
	 * Equality of graphs. Returns a mapping if this graph is equal
	 * to the given graph. Graphs must be connected.
	 *
	 * @param g the graph to compare
	 * @return true if there is an equality with the given graph, null if is not.
	 * @throws FastGraphException if the graph is disconnected
	 */
	public boolean isomorphic(FastGraph g) throws FastGraphException {
numberOfIsomorphismTests++;
isomorphismStartTime = System.currentTimeMillis();

		FastGraph g1 = fastGraph;
		FastGraph g2 = g;
		
		int numberOfNodes1 = g1.getNumberOfNodes();
		int numberOfNodes2 = g2.getNumberOfNodes();

		int numberOfEdges1 = g1.getNumberOfEdges();
		int numberOfEdges2 = g2.getNumberOfEdges();

		// ensure that the same graph returns true
		if(g1 == g2) {
			return(true);
		}
		
		if(numberOfNodes1 == 0 && numberOfNodes2 == 0) {
//System.out.println("Isomorphic: empty graphs");
succeed++;
timeForIsomorphismTests += System.currentTimeMillis()-isomorphismStartTime;
isomorphismStartTime = -1;		
			return true;
		}

// note that the following two tests are reasonable, but have been commented out as defined behaviour on disconnected graph is to throw exception
/*		if(!Connected.connected(g1) && Connected.connected(g2)) {
			return false;
		}
		if(Connected.connected(g1) && !Connected.connected(g2)) {
			return false;
		}
*/
		if(!Connected.connected(g2)) {
			throw new FastGraphException("Graph must be connected to test for isomorphism.");
		}
				
		if(numberOfNodes1 != numberOfNodes2) {
//System.out.println("Not isomorphic: different number of edges");
failOnNodeCount++;
timeForIsomorphismTests += System.currentTimeMillis()-isomorphismStartTime;
isomorphismStartTime = -1;		
			return false;
		}
				
		if(numberOfEdges1 != numberOfEdges2) {
//System.out.println("Not isomorphic: different number of nodes");
failOnEdgeCount++;
timeForIsomorphismTests += System.currentTimeMillis()-isomorphismStartTime;
isomorphismStartTime = -1;		
			return false;
		}
		
		degrees2 = g.findDegrees();
		
		maxDegree2 = g.maximumDegree();

		// check the number of nodes at each degree
		degreeBuckets2 = new int[maxDegree2+1];
		g.findDegreeBuckets(degreeBuckets2,degrees2);
		if(!Arrays.equals(degreeBuckets1, degreeBuckets2)) {
//System.out.println("Not isomorphic: different quantities of nodes with the same degree");
failOnDegreeComparison++;
timeForIsomorphismTests += System.currentTimeMillis()-isomorphismStartTime;
isomorphismStartTime = -1;		
			return false;
		}

		
		
		am2 = new AdjacencyMatrix(g);
		matrix2 = am2.buildIntAdjacencyMatrix();
		eigenvalues2 = am2.findEigenvalues(matrix2);
		eigenvalues2 = Util.roundArray(eigenvalues2, DECIMAL_PLACES);
//System.out.println(Arrays.toString(eigenvalues1));
//System.out.println(Arrays.toString(eigenvalues2));
		if(!compareEigenValues(eigenvalues2)) {
//System.out.println("Not isomorphic: eigenvalues are different");
failOnEigenvalues++;
timeForIsomorphismTests += System.currentTimeMillis()-isomorphismStartTime;
isomorphismStartTime = -1;		
			return false;
		}
		
if(isomorphismStartTime == -1) {
	isomorphismStartTime = System.currentTimeMillis();
}
		
		neighbours2 = findNeighbours(g,maxDegree2);
		
		int[] numberOfMatches = new int[numberOfNodes1]; // gives the number of relevant elements in the second array of possibleMatches 
		int[][] possibleMatches = new int[numberOfNodes1][numberOfNodes1]; // first element is the node, second is a list of potential matches
		
		for(int n1 = 0; n1 < numberOfNodes1; n1++) {
			int i = 0;
			for(int n2 = 0; n2 < numberOfNodes2; n2++) {
				if(matrix1[n1][n1] != matrix2[n2][n2]) { // check that they have the same number of self sourcing edges
					continue;
				}
				if(degrees1[n1] != degrees2[n2]) { // make sure the number of connecting edges is equal
					continue;
				}
				possibleMatches[n1][i] = n2;
				i++;
			}
			if(i == 0) {
//System.out.println("Not isomorphic: no possible match for a node");
failOnNodeMatches++;
timeForIsomorphismTests += System.currentTimeMillis()-isomorphismStartTime;
isomorphismStartTime = -1;		
				return false;
			}
			numberOfMatches[n1] = i;
//System.out.println("node "+n1+" number of matches "+numberOfMatches[n1]+" possibleMatches "+Arrays.toString(possibleMatches[n1]));
		}

		bruteForceStartTime = System.currentTimeMillis();
		
		int[] matchesIndex = new int[numberOfNodes1]; // current indexes for the search
		Arrays.fill(matchesIndex,-1);
		Arrays.fill(matches1,-1);
		Arrays.fill(matches2,-1);

		// backtracking search here
		int currentNode = 0;
		matchesIndex[currentNode] = 0;
		while(currentNode < numberOfNodes1) {

//System.out.println("current progress "+Arrays.toString(matchesIndex));

			if(matchesIndex[currentNode] == numberOfMatches[currentNode]) { // backtrack here to previous node if all nodes have been tried
//System.out.println("Backtracking from node "+ currentNode+ " matched node "+matches1[currentNode]);
				if(matches1[currentNode] != -1) {
					matches2[matches1[currentNode]] = -1;
				}
				matches1[currentNode] = -1;
				matchesIndex[currentNode] = -1;
				matchesIndex[currentNode]= 0;
				currentNode--;
				if(currentNode == -1) {
//System.out.println("Not isomorphic: brute force");
failOnBruteForce++;
timeForIsomorphismTests += System.currentTimeMillis()-isomorphismStartTime;
isomorphismStartTime = -1;		
timeForBruteForceTests += System.currentTimeMillis()-bruteForceStartTime;
bruteForceStartTime = -1;		
					return false;
				}
				if(matches1[currentNode] != -1) { // reset the previous match
					matches2[matches1[currentNode]] = -1;
				}
				matches1[currentNode] = -1; // reset the previous match
				matchesIndex[currentNode]++; // increment to the next node of the previous
				continue; // might have to happen multiple times
			}

			
			int possibleMatch = possibleMatches[currentNode][matchesIndex[currentNode]];
			if(isAMatch(currentNode,possibleMatch)) { // successful match, try the next node
//System.out.println("SUCCESSFUL match node "+ currentNode+" with node "+possibleMatch);
				matches1[currentNode] = possibleMatch;
				matches2[possibleMatch] = currentNode;
				currentNode++;
				if(currentNode == numberOfNodes1) {
//System.out.println("Isomorphic");
succeed++;
timeForIsomorphismTests += System.currentTimeMillis()-isomorphismStartTime;
isomorphismStartTime = -1;		
timeForBruteForceTests += System.currentTimeMillis()-bruteForceStartTime;
bruteForceStartTime = -1;		
					return true;
				}
				matchesIndex[currentNode] = 0;
			} else {
//System.out.println("Not a successful match node "+ currentNode+" with node "+possibleMatch);
				matchesIndex[currentNode]++; // fail so try the next node
			}

		}

// Never gets to here
System.out.println("Isomorphic - Should never get to here");
succeed++;
timeForIsomorphismTests += System.currentTimeMillis()-isomorphismStartTime;
isomorphismStartTime = -1;		
timeForBruteForceTests += System.currentTimeMillis()-bruteForceStartTime;
bruteForceStartTime = -1;		
		return true;
		
	}

	

	/**
	 * Check to see if the matched neighbours of n1 are neighbours of n2.
	 * Checks number of connecting edges, assumes checks on number of neighbours for each node has been performed.
	 * 
	 * @param n1 node in fastGraph
	 * @param n2 node in g
	 * @return true if the neighbours match, false otherwise
	 */
	private boolean isAMatch(int n1, int n2) {
		
		
//if(n1 == 2 && n2 == 1) {
//System.out.println("AAA");
//}
		if(matches1[n1] != -1) {
			return false;
		}
		if(matches2[n2] != -1) {
			return false;
		}
		
		HashSet<Integer> n1Neighbours = neighbours1.get(n1);
		HashSet<Integer> n2Neighbours = neighbours2.get(n2);

		int numberOfn1NeigboursMatched = 0;
		for(int node : n1Neighbours) {
			
			
			int matchNode = matches1[node];
			if(matchNode == -1) { // no match, so nothing to do here
				continue;
			}
			if(!n2Neighbours.contains(matchNode)) { // a neighbour of n1 has a matched node that is not a neigbour of n2
				return false;
			}
			
			/*
			// removed edges Count check, never seems to be used
			int edgeCount = matrix1[n1][node];
			int matchedEdgeCount = matrix2[n2][matchNode];
			if(edgeCount != matchedEdgeCount) { // different number of edge between the nodes and the matched nodes
//System.out.println("edge counts differ");
				return false;
			}
			*/
			
			numberOfn1NeigboursMatched++;
		}

		// now test it the other way - are all matched neighbours of n2 neighbours of n1
		int numberOfn2NeigboursMatched = 0;
		for(int node : n2Neighbours) {
			int matchNode = matches2[node];
			if(matchNode == -1) { // no match, so nothing to do here
				continue;
			}
			if(!n1Neighbours.contains(matchNode)) { // a neighbour of n2 has a matched node that is not a neighbour of n1
				return false;
			}
			numberOfn2NeigboursMatched++;
		}
		
		// test the same number of matches
		if(numberOfn1NeigboursMatched != numberOfn2NeigboursMatched) {
			return false;
		}


		return true;
	}



	/**
	 * Check if the two graphs have the same structure.
	 * 
	 * @param g the FastGraph from which a subgraph will be found
	 * @param nodes the nodes in g2 that form the subgraph
	 * @param edges the edges in g2 that form the subgraph
	 * @return true if the g1 and the subgraph of g2 are isomorphic, false otherwise
	 * @throws FastGraphException 
	 */
	public boolean isomorphic(FastGraph g, int[] nodes, int[] edges) throws FastGraphException {

		FastGraph subGraph = g.generateGraphFromSubgraph(nodes,edges);
		
		boolean iso = isomorphic(subGraph);

		return iso;
	}


		

	/**
	 * Check if the given graph has the same structure as the graph passed to the constructor.
	 * 
	 * @param g the FastGraph to be tested
	 * @return true if the two graphs are isomorphic, false otherwise
	 */
	public boolean isomorphicOld(FastGraph g) {
		
		Graph displayGraph = fastGraph.generateDisplayGraph();
		Graph dg = g.generateDisplayGraph();
		boolean iso = displayGraph.isomorphic(dg);
		return iso;
	}


	/**
	 * Check if two graphs have the same structure. Use the constructor and non static method if
	 * lots of comparisons are going to be made against the same graph.
	 * 
	 * @param g1 one FastGraph to be tested
	 * @param g2 the other FastGraph to be tested
	 * @return true if g1 and g2 are isomorphic, false otherwise
	 * @throws FastGraphException 
	 */
	public static boolean isomorphic(FastGraph g1, FastGraph g2) throws FastGraphException {
		ExactIsomorphism ei = new ExactIsomorphism(g1);
		boolean ret = ei.isomorphic(g2);
		return ret;
	}
	

	/**
	 * Compare graphs by their eigenvalues
	 * @param g the graph to compare
	 * @return true if they are equal by eigenvalue, false otherwise
	 */
	public boolean compareByEigenvalues(FastGraph g) {
		AdjacencyMatrix gam = new AdjacencyMatrix(g);
		int[][] gMatrix = gam.buildIntAdjacencyMatrix();
		double[] gEigenvalues = gam.findEigenvalues(gMatrix);
		gam = null;
		//System.gc();
		return compareEigenValues(gEigenvalues);
	}
	
	
	/**
	 * Compare eigenvalues
	 * @param values the values to compare
	 * @return true if the eigenvalues are equal, false otherwise
	 */
	public boolean compareEigenValues(double[] values) {
		boolean ret = Arrays.equals(eigenvalues1,values);

		return ret;
	}

	/**
	 * Generates a random graph isomorphic to the input one. Used for testing isomorphism as
	 * testing two isomorphic graphs guarantees that the brute force part of the algorithm is used.
	 * @param inGraph The starting graph
	 * @param seed The random seed
	 * @param direct If the graph is to be on heap (false) or off heap (tre)
	 * @return The new graph, isomorphic to the old one but with swapped node ids.
	 */
	public static FastGraph generateRandomIsomorphicGraph(FastGraph inGraph, long seed, boolean direct) {
		ArrayList<NodeStructure> allNodes = new ArrayList<NodeStructure>();
		ArrayList<EdgeStructure> allEdges = new ArrayList<EdgeStructure>();
		
		Random r = new Random(seed);
		
		ArrayList<Integer> remainingNodes = new ArrayList<Integer>(inGraph.getNumberOfNodes());
		for(int i = 0; i < inGraph.getNumberOfNodes(); i++) {
			remainingNodes.add(i);
		}

		HashMap<Integer,Integer> inToNewNodeMapping = new HashMap<Integer,Integer>(inGraph.getNumberOfNodes()*3);
		int index = 0;
		while(!remainingNodes.isEmpty()) {
			int nodeIndex = r.nextInt(remainingNodes.size());
			int node = remainingNodes.get(nodeIndex);
			remainingNodes.remove(nodeIndex);
			String label = inGraph.getNodeLabel(node);
			int weight = inGraph.getNodeWeight(node);
			byte type = inGraph.getNodeType(node);
			byte age = inGraph.getNodeAge(node);
			NodeStructure ns = new NodeStructure(index,label,weight,type,age);
			allNodes.add(ns);
			inToNewNodeMapping.put(node, index);
			index++;
		}
		

		ArrayList<Integer> remainingEdges = new ArrayList<Integer>(inGraph.getNumberOfEdges());
		for(int i = 0; i < inGraph.getNumberOfEdges(); i++) {
			remainingEdges.add(i);
		}

		index = 0;
		while(!remainingEdges.isEmpty()) {
			int edgeIndex = r.nextInt(remainingEdges.size());
			int edge = remainingEdges.get(edgeIndex);
			remainingEdges.remove(edgeIndex);
			String label = inGraph.getEdgeLabel(edge);
			int weight = inGraph.getEdgeWeight(edge);
			byte type = inGraph.getEdgeType(edge);
			byte age = inGraph.getEdgeAge(edge);
			int oldNode1 = inGraph.getEdgeNode1(edge);
			int oldNode2 = inGraph.getEdgeNode2(edge);
			int node1 = inToNewNodeMapping.get(oldNode1);
			int node2 = inToNewNodeMapping.get(oldNode2);
			EdgeStructure es = new EdgeStructure(index,label,weight,type,age,node1,node2);
			allEdges.add(es);
			index++;
		}
		
		FastGraph g = FastGraph.structureFactory(inGraph.getName()+"-isomorphic",(byte)0,allNodes,allEdges,direct);
		
		return g;
	}
	
	

	/**
	 * Output timing
	 */
	public static void reportTimes() {
		if(numberOfIsomorphismTests > 0) {
			System.out.println("Isomorphism test average "+(timeForIsomorphismTests/(1000.0*numberOfIsomorphismTests))+" seconds total tests "+numberOfIsomorphismTests+" total time "+(timeForIsomorphismTests/1000.0)+" seconds");
		} else {
			System.out.println("Isomorphism total tests "+numberOfIsomorphismTests);
		}
/*		if(numberOfOldIsomorphismTests > 0) {
			System.out.println("Old Isomorphism test average "+(timeForOldIsomorphismTests/(1000.0*numberOfOldIsomorphismTests))+" seconds total tests "+numberOfOldIsomorphismTests+" total time "+(timeForOldIsomorphismTests/1000.0)+" seconds");
		}
		if(numberOfEigenvalueTests > 0) {
			System.out.println("Eigenvalue test average "+(timeForEigenvalueTests/(1000.0*numberOfEigenvalueTests))+" total tests "+numberOfEigenvalueTests+" total time "+(timeForEigenvalueTests/1000.0)+" seconds");
		} else {
			System.out.println("Eigenvalue total tests "+numberOfEigenvalueTests);
		}
		if(numberOfSubgraphsGenerated > 0) {
			System.out.println("Subgraph generation average "+(timeForSubgraphsGenerated/(1000.0*numberOfSubgraphsGenerated))+" seconds total generated "+numberOfSubgraphsGenerated+" total time "+(timeForSubgraphsGenerated/1000.0)+" seconds");
		}
*/	}

	
	/**
	 * Output counts
	 */
	public static void reportFailRatios() {
		
		double total = failOnNodeCount+failOnEdgeCount+failOnEigenvalues+failOnDegreeComparison+failOnNodeMatches+failOnBruteForce+succeed;
		
		System.out.println("fail on Node Count "+failOnNodeCount+" "+(100.0*failOnNodeCount/total)+" % of calls");
		System.out.println("fail on Edge Count "+failOnEdgeCount+" "+(100.0*failOnEdgeCount/total)+" % of calls");
		System.out.println("fail on Degree Comparison "+failOnDegreeComparison+" "+(100.0*failOnDegreeComparison/total)+" % of calls");
		System.out.println("fail on Eigenvalues "+failOnEigenvalues+" "+(100.0*failOnEigenvalues/total)+" % of calls");
		System.out.println("fail on Node Matches "+failOnNodeMatches+" "+(100.0*failOnNodeMatches/total)+" % of calls");
		System.out.println("fail on Brute Force "+failOnBruteForce+" "+(100.0*failOnBruteForce/total)+" % of calls");
		System.out.println("succeed "+succeed+" "+(100.0*succeed/total)+" "+" % of calls");

	}


	/**
	 * sets all the profiling counts and timing to zero
	 */
	public static void resetProfiling() {
		numberOfIsomorphismTests = 0;
		numberOfOldIsomorphismTests = 0;
		numberOfEigenvalueTests = 0;
		
		timeForIsomorphismTests = 0;
		timeForBruteForceTests = 0;
		timeForOldIsomorphismTests = 0;
		timeForEigenvalueTests = 0;
		isomorphismStartTime = -1;
		bruteForceStartTime = -1;
		
		failOnNodeCount = 0;
		failOnEdgeCount = 0;
		failOnEigenvalues = 0;
		failOnDegreeComparison = 0;
		failOnNodeMatches = 0;
		failOnBruteForce = 0;
		succeed = 0;
	
	}

}
