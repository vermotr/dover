package uk.ac.kent.dover.fastGraph;


import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import org.json.*;

import uk.ac.kent.displayGraph.*;
import uk.ac.kent.displayGraph.drawers.GraphDrawerSpringEmbedder;
import uk.ac.kent.dover.fastGraph.ExactMotifFinder.IsoHolder;


/**
 * 
 * Graph class with redundant node connections. Stores nodes, edges, node connections, node labels, edge labels
 * in ByteBuffers. Note due to the complexity of storage, create only with the factory methods.
 * <p>
 * The design is scalable, has fast access, and allows quick file save and load of the ByteBuffers.
 * However, poor dynamic performance.
 * <p>
 * Storage:
 * node and edge indexes are integers and must start at 0 and end and size-1. Indexes are not stored,
 * they are assumed, so node info with nodeIndex n can be found starting in nodeBuf at offset n*nodeByteSize,
 * similarly edge with edgeIndex e starts in edgeBuf at e*edgeByteSize.
 * <p>
 * <ul>
 * <li>nodeBuf stores offset of label start in nodeLabelBuf and size (in chars) of labels.</li>
 * <li>nodeBuf stores in and out offset and in and out number (degree) of connecting nodes and edges start
 * which link to connectionBuf and size (in chars) of in or out edges.</li>
 * <li>edgeBuf stores offset of label start in edgeLabelBuf and size (in chars) of labels.</li>
 * <li>connectionBuf stores pairs of edgeIndex-nodeIndex (both are stored for fastest access) which form a
 * list of connecting items, with the in edge-nodes first, then out edge-nodes</li>
 * </ul>
 * json from <a href="https://github.com/stleary/JSON-java"> json library </a>
 * 
 * @author Peter Rodgers
 * @author Rob Baker
 */
public class FastGraph {

	public static final int NODE_LABEL_START_OFFSET = 0; // integer
	public static final int NODE_LABEL_LENGTH_OFFSET = 4; // short
	public static final int NODE_IN_CONNECTION_START_OFFSET = 6; // integer
	public static final int NODE_IN_DEGREE_OFFSET = 10; // integer
	public static final int NODE_OUT_CONNECTION_START_OFFSET = 14; // integer
	public static final int NODE_OUT_DEGREE_OFFSET = 18; // integer
	public static final int NODE_WEIGHT_OFFSET = 22; // integer
	public static final int NODE_TYPE_OFFSET = 26; // byte
	public static final int NODE_AGE_OFFSET = 27; // byte
	
	public static final int EDGE_NODE1_OFFSET = 0; // integer
	public static final int EDGE_NODE2_OFFSET = 4; // integer
	public static final int EDGE_LABEL_START_OFFSET = 8; // integer
	public static final int EDGE_LABEL_LENGTH_OFFSET = 12; // short
	public static final int EDGE_WEIGHT_OFFSET = 14; // integer
	public static final int EDGE_TYPE_OFFSET = 18; // byte
	public static final int EDGE_AGE_OFFSET = 19; // byte
	
	public static final int CONNECTION_EDGE_OFFSET = 0; // integer, edge is first of the pair
	public static final int CONNECTION_NODE_OFFSET = 4; // integer, node is straight after the edge
	
	public static final int DEFAULT_AVERAGE_LABEL_LENGTH = 20;
	
	public static final int NODE_BYTE_SIZE = 28;
	public static final int EDGE_BYTE_SIZE = 20;
	public static final int CONNECTION_PAIR_SIZE = 8; // this is an edge index plus an node index
	
	public static final String INFO_SPLIT_STRING = "~";
	
	public static final int MAX_BYTE_BUFFER_SIZE = Integer.MAX_VALUE-5000;
	

	private ByteBuffer nodeBuf;
	private ByteBuffer edgeBuf;
	private ByteBuffer connectionBuf;
	private ByteBuffer nodeLabelBuf;
	private ByteBuffer edgeLabelBuf;

	private int numberOfNodes;
	private int numberOfEdges;
	
	
	private String name = "";
	private boolean direct;
	
		
	/**
	 * No direct access to constructor, as a number of data structures need to be created when
	 * graph nodes and edges are added.
	 * 
	 * @param nodeTotal the number of nodes in the graph
	 * @param edgeTotal the number of edges in the graph
	 * @param direct if true then off heap ByteBuffers, if false then on heap ByteBuffers
	 */
	private FastGraph(int nodeTotal, int edgeTotal, boolean direct) {
		
		this.numberOfNodes = nodeTotal;
		this.numberOfEdges = edgeTotal;
		this.direct = direct;
		
		init();
		
	}

	
	/**
	 * @param args not used
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		Debugger.enabled = true;
		
		long time = 0;
		
		
//		FastGraph g1 = randomGraphFactory(1,0,false);
//		FastGraph g1 = randomGraphFactory(2,1,false);
//		FastGraph g1 = randomGraphFactory(5,6,1,true);
//		FastGraph g1 = randomGraphFactory(8,9,1,false);
//		FastGraph g1 = randomGraphFactory(10,50,1,false);
//		FastGraph g1 = randomGraphFactory(100,1000,1,false); // 1 hundred nodes, 1 thousand edges
//		FastGraph g1 = randomGraphFactory(10000,100000,1,false); // 10 thousand nodes, 100 thousand edges
//		FastGraph g1 = randomGraphFactory(100000,1000000,1,false); // 100 thousand nodes, 1 million edges
//		FastGraph g1 = randomGraphFactory(1000000,10000000,1,false); // 1 million nodes, 10 million edges
//		FastGraph g1 = randomGraphFactory(5000000,50000000,1,false); // limit for edgeLabelBuf at 20 chars per label
//		FastGraph g1 = randomGraphFactory(4847571,68993773,1,false); // Size of LiveJournal1 example from SNAP
//		FastGraph g1 = randomGraphFactory(10000000,100000000,1,false); // 10 million nodes, 100 million edges, close to edgeBuf limit, but fails on heap space with 14g, but pass with heap space of 30g

		time = Debugger.createTime();
//		FastGraph g1 = adjacencyListGraphFactory(7115,103689,null,"Wiki-Vote.txt",false);
//		FastGraph g1 = adjacencyListGraphFactory(36692,367662,null,"Email-Enron1.txt",false);
//		FastGraph g1 = adjacencyListGraphFactory(81306,2420766,null,"twitter_combined.txt",false); // SNAP web page gives 1768149 edges
//		FastGraph g1 = adjacencyListGraphFactory(1696415,11095298,null,"as-skitter.txt",false);
//		FastGraph g1 = adjacencyListGraphFactory(1632803,30622564,null,"soc-pokec-relationships.txt",false);
//		FastGraph g1 = adjacencyListGraphFactory(4847571,68993773,null,"soc-LiveJournal1.txt",false);


//Debugger.outputTime("snap load time ");
/*		
		time = Debugger.createTime();
		
		g1.saveBuffers(null,g1.getName());
		Debugger.outputTime("saveBuffers test time ");
		time = Debugger.createTime();
*/
//String name = "random-n-100-e-1000";
//String name = "random-n-2-e-1";
//String name = "as-skitter.txt";
//String name = "soc-pokec-relationships.txt-reduced";
//String name = "soc-pokec-relationships.txt-veryshort-veryshort";
String name = "Wiki-Vote.txt";
//		String name = g1.getName();
//		FastGraph g2 = g1;
		try {
			time = Debugger.createTime();
			FastGraph g2 = loadBuffersGraphFactory(null,name);
			//g2 = FastGraph.randomGraphFactory(100,1000,1,true,false); // 2 hundred nodes, 2 thousand edges
			
			Debugger.log("Connected: " + Connected.connected(g2));
			
			Debugger.log("Number of nodes: " + g2.getNumberOfNodes());
			Debugger.log("Number of edges: " + g2.getNumberOfEdges());
			
			Debugger.resetTime();
			
			ExactMotifFinder emf = new ExactMotifFinder(g2);
			emf.findAndExportAllMotifs(10, 4, 4, 0, true);
			HashMap<String,LinkedList<IsoHolder>> referenceBuckets = emf.getHashBuckets();
			emf.findAndExportAllMotifs(0, 4, 4, 0, false);
			HashMap<String,LinkedList<IsoHolder>> realBuckets = emf.getHashBuckets();
			emf.compareAndExportResults(referenceBuckets, realBuckets);
//			emf.outputHashBuckets(realBuckets);
			Debugger.outputTime("Time total motif detection");
			
/*
			//ISO CHECK REWIRING
			FastGraph g1 = FastGraph.randomGraphFactory(12,24,1,true,false);
			FastGraph g2 = g1.generateRandomRewiredGraph(10, 1);
			ExactIsomorphism em = new ExactIsomorphism(g1);
			boolean iso = em.isomorphic(g2);
			Debugger.log("g1 and g2 iso: " + iso);
			
			uk.ac.kent.displayGraph.Graph dg = g1.generateDisplayGraph();
			dg.randomizeNodePoints(new Point(20,20),300,300);
			uk.ac.kent.displayGraph.display.GraphWindow gw = new uk.ac.kent.displayGraph.display.GraphWindow(dg, true);
			uk.ac.kent.displayGraph.drawers.BasicSpringEmbedder bse = new uk.ac.kent.displayGraph.drawers.BasicSpringEmbedder();
			GraphDrawerSpringEmbedder se = new GraphDrawerSpringEmbedder(KeyEvent.VK_Q,"Spring Embedder - randomize, no animation",true);
			se.setAnimateFlag(false);
			se.setIterations(100);
			se.setTimeLimit(200);
			se.setGraphPanel(gw.getGraphPanel());
			se.layout();
			
			uk.ac.kent.displayGraph.Graph dg2 = g2.generateDisplayGraph();
			dg2.randomizeNodePoints(new Point(20,20),300,300);
			uk.ac.kent.displayGraph.display.GraphWindow gw2 = new uk.ac.kent.displayGraph.display.GraphWindow(dg2, true);
			uk.ac.kent.displayGraph.drawers.BasicSpringEmbedder bse2 = new uk.ac.kent.displayGraph.drawers.BasicSpringEmbedder();
			GraphDrawerSpringEmbedder se2 = new GraphDrawerSpringEmbedder(KeyEvent.VK_Q,"Spring Embedder - randomize, no animation",true);
			se2.setAnimateFlag(false);
			se2.setIterations(100);
			se2.setTimeLimit(200);
			se2.setGraphPanel(gw2.getGraphPanel());
			se2.layout();
	*/		
			//EnumerateSubgraphFanmod es = new EnumerateSubgraphFanmod(g2);
			//EnumerateSubgraphNeighbourhood es = new EnumerateSubgraphNeighbourhood(g2);
			//HashSet<FastGraph> gs = es.enumerateSubgraphs(4,1,10);
		//	HashSet<Integer> startingNodes = new HashSet<Integer>();
		//	HashSet<Integer> nodes = new HashSet<Integer>();
		//	startingNodes.add(5490);
		//	Debugger.log("startingNodes: " + startingNodes);
		//	Debugger.log("nodes: " + nodes);
		//	es.buildNeighbourhood(startingNodes, nodes, 20);
		//	Debugger.log("neighbourhood size: " + nodes.size());
			
		//	Debugger.outputTime("Time for enumeration");			
		//	Debugger.log("number of subgraphs " + gs.size());	
	/*		
			time = Debugger.createTime();
			LinkedList<Integer> nodes = new LinkedList<Integer>();
			LinkedList<Integer> edges = new LinkedList<Integer>();
			
			//FastGraph g3 = g2.removeNodesAndEdgesFromGraph(nodes,edges,1500,100);
			//FastGraph g3 = g2.removeNodesAndEdgesFromGraph(nodes,edges,1500000,10000000);
			FastGraph g3 = g2;
			
			//Debugger.outputTime("deletion test time ");
			
			time = Debugger.createTime();
			g3.relabelFastGraph(g3.getNumberOfNodes()/10);
			Debugger.log("relabelling test time ");
			//Debugger.log("deletion test time (from before) " + deletionTime+" seconds");
			Debugger.outputTime("New graph has: nodes: " + g3.getNumberOfNodes() + " and edges: " + g3.getNumberOfEdges());
	
			time = Debugger.createTime();
			g3.setName(g2.getName()+"-relabelled");
			g3.saveBuffers(null,g3.getName());
			Debugger.outputTime("saveBuffers test time ");
				
			FastGraph g4 = loadBuffersGraphFactory(null,g3.getName());
			
		//	Graph dg = g2.generateDisplayGraph();
		//	boolean consistent = dg.consistent();
		//	Debugger.log("consistent: " + consistent);
			
	*/		
			//just for testing
		//	Debugger.log();
	/*		
			Debugger.log("graph now has the labels (taken from the buffer):");
			FastGraphNodeType[] ntypes = FastGraphNodeType.values();
			for(int j = 0; j < g4.getNumberOfNodes(); j++) {
				byte type = g4.getNodeType(j);
				Debugger.log(g4.getNodeLabel(j) + " " + type + " (" + ntypes[type] + ")");
			}
			Debugger.log();
			Debugger.log("edges now have the types (taken from the buffer):");
			for(int j = 0; j < g4.getNumberOfEdges(); j++) {
				byte type = g4.getEdgeType(j);
				Debugger.log("n1" + g4.getEdgeNode1(j) + " n2" + g4.getEdgeNode2(j) + " type " + type + " (" + g4.getEdgeLabel(j) + ")");
			}
		*/	
//			int[] degrees = g2.countInstancesOfNodeDegrees(4);
//			Debugger.log(Arrays.toString(degrees));
			
	/*		
			LinkedList<int[]> rewiring = new LinkedList<int[]>();
			Random rand = new Random(78);
			for(int i = 0; i < g2.numberOfEdges; i++) {
				int[] r = {i,rand.nextInt(g2.getNumberOfNodes()),rand.nextInt(g2.getNumberOfNodes())};
				rewiring.add(r);
			}
			FastGraph h = g2.generateRewiredGraph(rewiring);
Debugger.log("g2 "+g2.checkConsistency());
Debugger.log("h "+h.checkConsistency());
*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}

 		
	}
	
	/**
	 * Relabels the current FastGraph with the family groups in subgraphs/families folder.<br>
	 * Any remaining nodes and edges are labelled randomly.
	 * Each family is tested against each of the induced subgraphs and will be relabeled if the two are isomorphic
	 * 
	 * @param subgraphsToTest How many subgraphs will be induced for each family.
	 * 
	 * @throws Exception If there is a problem loading the family subgraphs, or if there is a problem inducing a subgraph
	 */
	public void relabelFastGraph(int subgraphsToTest) throws Exception{
		Debugger.log("Relabelling FastGraph");
		long time = 0;
		time = Debugger.createTime();
		
		//load node and edge labels arrays
		String[] nodeLabels = new String[this.getNumberOfNodes()];
		String[] edgeLabels = new String[this.getNumberOfEdges()];
		
		//load node and edge Types arrays
		byte[] nodeTypes = new byte[this.getNumberOfNodes()];
		byte[] edgeTypes = new byte[this.getNumberOfEdges()];
		
		//create induction class
		InducedSubgraph is = new InducedSubgraph(this);
		
		//create Name Picker class
		NamePicker np = new NamePicker();
		
		//number of families found
		int fams = 0;
		
		//load family subgraphs
		FastGraph[] families = loadFamilies();
		for(FastGraph family : families) {
			Debugger.log("Testing family " + family.getName());
			
			ExactIsomorphism ei = new ExactIsomorphism(family);
			
			int familyNodesSize = family.getNumberOfNodes();			
			
			for (int i = 0; i < subgraphsToTest; i++) { //induce subgraphs to test
				
				if(i%10000 == 0) {
					Debugger.log("Testing subgraph " + i);
				}
				
				LinkedList<Integer> subNodes = new LinkedList<Integer>();
				LinkedList<Integer> subEdges = new LinkedList<Integer>();
				
				//create a subgraph and build to a FastGraph
				is.createInducedSubgraph(subNodes, subEdges, familyNodesSize);
				FastGraph subgraph = this.generateGraphFromSubgraph(Util.convertLinkedList(subNodes), Util.convertLinkedList(subEdges));
				
				//is this FastGraph isomorphic to the one in the constructor (i.e. the family)
				boolean isomorphic = ei.isomorphic(subgraph);
				
				if(isomorphic) {
					fams++;
					//rename original graph
					
					//pick a surname, so all family members have the same surname
					String surname = np.getSurname();
				//	Debugger.log("Family name: " + surname);
					for(int n : subNodes) {
						nodeLabels[n] = np.getForename() + " " + surname;
						nodeTypes[n] = FastGraphNodeType.CHILD.getValue();
					}
					
					//set the parents
					nodeTypes[subNodes.get(0)] = FastGraphNodeType.PARENT.getValue();
					nodeTypes[subNodes.get(1)] = FastGraphNodeType.PARENT.getValue();
										
					//label the edges with types rather than names
					for(int e : subEdges) {
						
						//if this is the parent's relationship
						if ((getEdgeNode1(e) == subNodes.get(0) && getEdgeNode2(e) == subNodes.get(1)) ||
						(getEdgeNode1(e) == subNodes.get(1) && getEdgeNode2(e) == subNodes.get(0))) {
							edgeTypes[e] = FastGraphEdgeType.MARRIED.getValue();
							edgeLabels[e] = FastGraphEdgeType.MARRIED.toString();
							//if this is the parent child relationship
						} else if (getEdgeNode1(e) == subNodes.get(0) || getEdgeNode1(e) == subNodes.get(1) ||
								getEdgeNode2(e) == subNodes.get(0) || getEdgeNode2(e) == subNodes.get(1)) {
							edgeTypes[e] = FastGraphEdgeType.PARENT.getValue();
							edgeLabels[e] = FastGraphEdgeType.PARENT.toString();
							//otherwise these are siblings
						} else {
							edgeTypes[e] = FastGraphEdgeType.SIBLING.getValue();
							edgeLabels[e] = FastGraphEdgeType.SIBLING.toString();
						}		
					}//end for each subEdge	
				} //end if isomorphic
			}//end for each subgraph to test	
		}//end foreach family
		
		Debugger.log("## Number of families found: " + fams);
		
		//replace the blanks with other names
		for(int j = 0; j < nodeLabels.length; j++) {						
			if (nodeLabels[j] == null) {
				nodeLabels[j] = np.getName();
			}
		}
		
		//Leave node types as they are - these are not used
		
		Random r = new Random(nodeBuf.getLong(0));
		//replace the blanks with other edge types
		FastGraphEdgeType[] values = FastGraphEdgeType.values();
		for(int j = 0; j < edgeTypes.length; j++) {						
			if (edgeTypes[j] == FastGraphEdgeType.UNKNOWN.getValue()) {
				//pick a random relationship
				byte relationship = (byte) (r.nextInt(values.length - 4)+4); //ignore the family relationships
				edgeTypes[j] = relationship;
				edgeLabels[j] = values[relationship].toString();
			}
		}		
		
		//Set all node & edge labels, and node & edge types
		this.setAllNodeLabels(nodeLabels);
		this.setAllEdgeLabels(edgeLabels);
		for(int i = 0; i < nodeTypes.length; i++) {
			this.setNodeType(i, nodeTypes[i]);			
		}
		for(int i = 0; i < edgeTypes.length; i++) {
			this.setEdgeType(i, edgeTypes[i]);			
		}

		
	}
	
	/**
	 * Loads the families subgraphs
	 * @return A list of FastGraphs based on the family subgraphs
	 * @throws Exception If the node or edge count can't be converted, or the file can't be loaded
	 */
	public FastGraph[] loadFamilies() throws Exception {
		File folder = new File(Launcher.startingWorkingDirectory+File.separatorChar+"subgraphs"+File.separatorChar+"families");
		File[] listOfFiles = folder.listFiles();
		LinkedList<FastGraph> graphs = new LinkedList<>();
		
		for (File f : listOfFiles) {
			String[] splits = f.getName().split("-");
			int nodeCount = Integer.parseInt(splits[2]);
			int edgeCount = Integer.parseInt(splits[4]);
			Debugger.log(f + " n" + nodeCount + "e" + edgeCount);
			FastGraph g = nodeListEdgeListGraphFactory(nodeCount, edgeCount, folder.getPath() + File.separatorChar + f.getName(), f.getName(), direct);
			Debugger.log("new g nodes" + g.getNumberOfNodes());
			graphs.add(g);
		}
		
		//Debugger.log(Arrays.toString(listOfFiles));
		
		FastGraph[] graphArray = new FastGraph[graphs.size()];
		Util.convertLinkedListObject(graphs, graphArray);
		return graphArray;
	}
	
	/**
	 * This method creates a new FastGraph of the rough size given in targetNodes and targetEdges. <br>
	 * The new graph will never have fewer nodes than the target, but may have fewer edges. <br>
	 * <b>Note: This may take some time to complete</b>
	 * 
	 * @param nodes The list of nodes to be removed
	 * @param edges The list of edges to be removed
	 * @param targetNodes The target number of nodes
	 * @param targetEdges The target number of edges
	 * @return A new FastGraph that is roughly the size of the target
	 * @throws FastGraphException If there is an exception here, e.g. targetNodes is too big
	 */
	public FastGraph removeNodesAndEdgesFromGraph(LinkedList<Integer> nodes, LinkedList<Integer> edges, int targetNodes, int targetEdges) throws FastGraphException {
		
		Debugger.log("Suggesting nodes and egdes to remove");
		long time = Debugger.createTime();
		
		int currentTotalNodes = getNumberOfNodes();
		int currentTotalEdges = getNumberOfEdges();
		
		//if a graph of the same size has been specified
		if (targetNodes == currentTotalNodes && targetEdges == currentTotalEdges) {
			return this;
		}
		
		//if the node target is too big
		if(targetNodes > currentTotalNodes) {
			throw new FastGraphException("The target node size is too big");
		}
		//if the edge target is too big
		if(targetEdges > currentTotalEdges) {
			throw new FastGraphException("The target edge size is too big");
		}
		
		
		int nodeReductionAmount = currentTotalNodes - targetNodes; //how many nodes we need to remove
		int edgeReductionAmount = currentTotalEdges - targetEdges; //how many edges we need to remove
		LinkedHashSet<Integer> edgesToRemove = new LinkedHashSet<Integer>(); //edges that need removing
		LinkedHashSet<Integer> nodesToRemove = new LinkedHashSet<Integer>(); //nodes that need removing
		
		Debugger.log("Current Nodes: " + currentTotalNodes + " Target Nodes: " + targetNodes);
		Debugger.log("Current Edges: " + currentTotalEdges + " Target Edges: " + targetEdges);
		
		Debugger.outputTime("setup test time ");
		Debugger.log();
		
		time = Debugger.createTime();
		Debugger.log("# Starting STEP ONE");
		//STEP ONE:
		//Find a subgraph with the required number of nodes. Remove it
		InducedSubgraph is = new InducedSubgraph(this);
		LinkedList<Integer> subNodes = new LinkedList<Integer>();
		LinkedList<Integer> subEdges = new LinkedList<Integer>();
		is.createInducedSubgraph(subNodes, subEdges, nodeReductionAmount);
		
		nodesToRemove.addAll(subNodes);
		edgesToRemove.addAll(subEdges);
		
		//delete all edges connecting to the nodes to be deleted
		for(int n : nodesToRemove) {
			Util.addAll(edgesToRemove, this.getNodeConnectingInEdges(n));
			Util.addAll(edgesToRemove, this.getNodeConnectingOutEdges(n));
		}
		
		Debugger.outputTime("After induction test time ");
		Debugger.log("nodes to remove size: " + nodesToRemove.size() + " edges to remove size: " + edgesToRemove.size());
		Debugger.log();
		
		
		//STEP TWO:
		//if we haven't removed enough nodes
		Debugger.log("# Starting STEP TWO");
		time = Debugger.createTime();
		if(nodeReductionAmount > nodesToRemove.size()) { //could we thread these to make this quicker?
			Random r = new Random(nodeBuf.getLong(1));
			
			//make local stores, as we might not want to remove these nodes if they are too big
			LinkedHashSet<Integer> localEdgesToRemove = new LinkedHashSet<Integer>(); //edges that need removing
			LinkedHashSet<Integer> localNodesToRemove = new LinkedHashSet<Integer>(); //nodes that need removing
			
			int chances = 10; //if a tree is too big, then skip it. But only do this 10 times, in case we are stuck
			while(nodeReductionAmount > nodesToRemove.size() && chances > 0) {
				//long time2 = System.currentTimeMillis();
				localNodesToRemove.clear();
				localEdgesToRemove.clear();
				
				int stillToRemove = nodeReductionAmount - nodesToRemove.size(); //what nodes are left to remove
				this.buildTree(localNodesToRemove, localEdgesToRemove, r, 3);
				
				//Debugger.log("Tree: " + localNodesToRemove);				
				//Debugger.log("nodeRA: " + nodeReductionAmount + " stillTR: " + stillToRemove + " localNodesToRemove: " + localNodesToRemove.size());
				
				if (localNodesToRemove.size() <= stillToRemove) {
					nodesToRemove.addAll(localNodesToRemove);
					edgesToRemove.addAll(localEdgesToRemove);
					
					//delete all edges connecting to the nodes in this tree
					for(int n : localNodesToRemove) {
						Util.addAll(edgesToRemove, this.getNodeConnectingInEdges(n));
						Util.addAll(edgesToRemove, this.getNodeConnectingOutEdges(n));
					}
					
				} else {
					chances--; //Avoids getting stuck if there are no further options
					continue;
				}
				
				//Debugger.log("After this tree test time " + (System.currentTimeMillis()-time2)/1000.0+" seconds");
			}
			
		}
		Debugger.outputTime("After tree test time ");
		Debugger.log("nodes to remove size: " + nodesToRemove.size() + " edges to remove size: " + edgesToRemove.size());
		Debugger.log();
		
		
		//STEP THREE:
		//if we haven't removed enough nodes
		//pick some at random
		Debugger.log("# Starting STEP THREE");
		time = Debugger.createTime();
		if(nodeReductionAmount > nodesToRemove.size()) {
			Random r = new Random(nodeBuf.getLong(2));		
			while(nodeReductionAmount > nodesToRemove.size()) {
				int n = r.nextInt(this.getNumberOfNodes());
				nodesToRemove.add(n);
				edgesToRemove.addAll(Util.convertArray(this.getNodeConnectingEdges(n)));
			}
		}
		Debugger.outputTime("After node removal test time ");
		Debugger.log("nodes to remove size: " + nodesToRemove.size() + " edges to remove size: " + edgesToRemove.size());
		Debugger.log();
		
		
		//STEP FOUR:
		//if we haven't removed enough edges
		//pick some at random
		Debugger.log("# Starting STEP FOUR");
		time = Debugger.createTime();
		if(edgeReductionAmount > edgesToRemove.size()) {
			Random r = new Random(edgeBuf.getLong(2));
			while(edgeReductionAmount > edgesToRemove.size()) {
				int e = r.nextInt(this.getNumberOfEdges());
				edgesToRemove.add(e);
			}
			
		}
		Debugger.outputTime("After edge removal test time ");
		Debugger.log("nodes to remove size: " + nodesToRemove.size() + " edges to remove size: " + edgesToRemove.size());
		Debugger.log();		
		
		nodes.addAll(nodesToRemove);
		edges.addAll(edgesToRemove);

		time = Debugger.createTime();
		Debugger.log("Building new FastGraph");
		FastGraph g = this.generateGraphByDeletingItems(Util.convertLinkedList(nodes), Util.convertLinkedList(edges), false);
		Debugger.outputTime("After FastGraph building test time ");
		return g;
	}
	
	/**
	 * Builds a tree like structure from a random node, to a particular depth
	 * 
	 * @param nodes A LinkedHashSet of nodes, ready to be populated with nodes to be removed
	 * @param edges A LinkedHashSet of edges, ready to be populated with nodes to be removed
	 * @param r A random number generator used to pick a starting place.
	 * @param depth The depth of the tree. 1 would equal the starting node and it's children. 2 would be the same as 1, but with grandchildren.
	 */
	public void buildTree(LinkedHashSet<Integer> nodes, LinkedHashSet<Integer> edges, Random r, int depth) {
		int startingNode = r.nextInt(this.getNumberOfNodes());
		
		nodes.add(startingNode);
		edges.addAll(Util.convertArray(this.getNodeConnectingEdges(startingNode)));
		
		LinkedList<Integer> startingNodes = new LinkedList<>();
		startingNodes.add(startingNode);

		//while we are not at the required depth
		while(depth != 0) {
			//Debugger.log("Starting Node: " + startingNode);
			int[] cn = new int[0]; //get ready to store connecting nodes
			for (int sn : startingNodes) { //for each of the starting nodes
				cn = this.getNodeConnectingNodes(sn); //get this node's connecting nodes
				//Debugger.log("    Connecting Nodes: " + Arrays.toString(cn));
				for(int n : cn) {
					nodes.add(n); //add them all the the tree
					edges.addAll(Util.convertArray(this.getNodeConnectingEdges(n))); //add the edges too
				}
			}
			startingNodes = Util.convertArray(cn); // make the connections the starting nodes for the next loop
			depth--; //"We need to go deeper"
		}		
	}
	

	/**
	 * @return the number of nodes in the graph
	 */
	public int getNumberOfNodes() {
		return numberOfNodes;
	}


	/**
	 * @return the number of edges in the graph
	 */
	public int getNumberOfEdges() {
		return numberOfEdges;
	}

	
	/**
	 * @return the graph name
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * @return the direct flag, false is on heap, true is off heap
	 */
	public boolean getDirect() {
		return direct;
	}
	
	/**
	 * 
	 * @return the node ByteBuffer
	 */
	public ByteBuffer getNodeBuf() {
		return nodeBuf;
	}


	/**
	 * 
	 * @return the edge ByteBuffer
	 */
	public ByteBuffer getEdgeBuf() {
		return edgeBuf;
	}


	/**
	 * 
	 * @return the node label ByteBuffer
	 */
	public ByteBuffer getNodeLabelBuf() {
		return nodeLabelBuf;
	}


	/**
	 * 
	 * @return the edge label ByteBuffer
	 */
	public ByteBuffer getEdgeLabelBuf() {
		return edgeLabelBuf;
	}


	/**
	 * 
	 * @return the connections ByteBuffer
	 */
	public ByteBuffer getConnectionBuf() {
		return connectionBuf;
	}



	/**
	 * @param nodeIndex the node
	 * @return the node label
	 */
	public String getNodeLabel(int nodeIndex) {
		
		int labelStart = nodeBuf.getInt(NODE_LABEL_START_OFFSET+nodeIndex*NODE_BYTE_SIZE);
		int labelLength = nodeBuf.getShort(NODE_LABEL_LENGTH_OFFSET+nodeIndex*NODE_BYTE_SIZE);
		
		char[] label = new char[labelLength];
		for(int i = 0; i < labelLength; i++) {
			int offset = labelStart+i*2;
			char c = nodeLabelBuf.getChar(offset);
			label[i] = c;
		}
		String ret = new String(label);
		return ret;
	}
	
	
	/**
	 * @param nodeIndex the node
	 * @return the node weight
	 */
	public int getNodeWeight(int nodeIndex) {
		int weight = nodeBuf.getInt(NODE_WEIGHT_OFFSET+nodeIndex*NODE_BYTE_SIZE);
		return weight;
	}
	
	
	/**
	 * @param nodeIndex the node
	 * @return the node type
	 */
	public byte getNodeType(int nodeIndex) {
		byte type = nodeBuf.get(NODE_TYPE_OFFSET+nodeIndex*NODE_BYTE_SIZE);
		return type;
	}
	
	
	/**
	 * @param nodeIndex the node
	 * @return the node age
	 */
	public byte getNodeAge(int nodeIndex) {
		byte age = nodeBuf.get(NODE_AGE_OFFSET+nodeIndex*NODE_BYTE_SIZE);
		return age;
	}
	

	/**
	 * @param nodeIndex the node
	 * @return the node degree (number of connecting edges)
	 */
	public int getNodeDegree(int nodeIndex) {
		int degree = getNodeInDegree(nodeIndex)+getNodeOutDegree(nodeIndex);
		return degree;
	}
	

	/**
	 * @param nodeIndex the node
	 * @return the node in-degree (number of edges entering the node)
	 */
	public int getNodeInDegree(int nodeIndex) {
		int degree = nodeBuf.getInt(NODE_IN_DEGREE_OFFSET+nodeIndex*NODE_BYTE_SIZE);
		return degree;
	}

	
	/**
	 * @param nodeIndex the node
	 * @return the node out-degree (number of edges leaving the node)
	 */
	public int getNodeOutDegree(int nodeIndex) {
		int degree = nodeBuf.getInt(NODE_OUT_DEGREE_OFFSET+nodeIndex*NODE_BYTE_SIZE);
		return degree;
	}
	

	/**
	 * @param nodeIndex the node
	 * @return all connecting edges
	 */
	public int[] getNodeConnectingEdges(int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_IN_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeDegree(nodeIndex);
		
		int[] ret = new int[degree];
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_EDGE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
		
		return ret;
	}
	

	/**
	 * This version puts the connecting edges in the argument array, to avoid repeated object creation and so speed up multiple accesses.
	 * create array with size of either getNodeDegree(nodeIndex) or maxDegree(). array elements beyond nodeDegree(nodeIndex)-1 are undefined.
	 * Will throw an exception if ret is not large enough.
	 * 
	 * @param ret this is populated with the connecting edges found
	 * @param nodeIndex the node
	 * @return all connecting edges via parameter array. 
	 */
	public void getNodeConnectingEdges(int[] ret, int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_IN_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeDegree(nodeIndex);
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_EDGE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
	}
	

	/**
	 * @param nodeIndex the node
	 * @return all node neighbours. 
	 */
	public int[] getNodeConnectingNodes(int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_IN_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeDegree(nodeIndex);
		int[] ret = new int[degree];
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_NODE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
		
		return ret;
	}
	

	/**
	 * This version puts the connecting nodes in the argument array, to avoid repeated object creation and so speed up multiple accesses.
	 * create array with size of either getNodeDegree(nodeIndex) or maxDegree(). array elements beyond nodeDegree(nodeIndex)-1 are undefined.
	 * Will throw an exception if ret is not large enough.
	 * 
	 * @param ret this is populated with the connecting nodes found
	 * @param nodeIndex the node
	 * @return all node neighbours. 
	 */
	public void getNodeConnectingNodes(int[] ret, int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_IN_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeDegree(nodeIndex);
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_NODE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
	}
	

	/**
	 * For directed graphs.
	 * 
	 * @param nodeIndex the node
	 * @return all connecting edges for the node
	 */
	public int[] getNodeConnectingInEdges(int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_IN_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeInDegree(nodeIndex);
		
		int[] ret = new int[degree];
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_EDGE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
		
		return ret;
	}
	

	/**
	 * For directed graphs.
	 * This version puts the connecting edges in the argument array, to avoid repeated object creation and so speed up multiple accesses.
	 * create array with size of either getNodeInDegree(nodeIndex) or maxDegree(). array elements beyond nodeDegree(nodeIndex)-1 are undefined.
	 * Will throw an exception if ret is not large enough.
	 * 
	 * @param ret this is populated with the connecting edges found
	 * @param nodeIndex the node
	 * @return all connecting edges that enter the node via the parameter array. 
	 */
	public void getNodeConnectingInEdges(int[] ret, int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_IN_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeInDegree(nodeIndex);
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_EDGE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
	}


	/**
	 * For directed graphs.
	 * 
	 * @param nodeIndex the node
	 * @return all node neighbours that are on the end of edges that enter the node. 
	 */
	public int[] getNodeConnectingInNodes(int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_IN_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeInDegree(nodeIndex);
		
		int[] ret = new int[degree];
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_NODE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
		
		return ret;
	}

	/**
	 * For directed graphs.
	 * This version puts the connecting nodes in the argument array, to avoid repeated object creation and so speed up multiple accesses.
	 * create array with size of either getNodeInDegree(nodeIndex) or maxDegree(). array elements beyond nodeDegree(nodeIndex)-1 are undefined.
	 * Will throw an exception if ret is not large enough.
	 * 
	 * @param ret this is populated with the connecting nodes found
	 * @param nodeIndex the node
 	 * @return all node neighbours that are on the end of edges that enter the node via the parameter array.
	 */
	public void getNodeConnectingInNodes(int[] ret, int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_IN_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeInDegree(nodeIndex);
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_NODE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
	}

	/**
	 * For directed graphs.
	 * 
	 * @param nodeIndex the node
	 * @return all edges that leave the node. 
	 */
	public int[] getNodeConnectingOutEdges(int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_OUT_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeOutDegree(nodeIndex);
		int[] ret = new int[degree];
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_EDGE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
		
		return ret;
	}
	
	/**
	 * For directed graphs.
	 * This version puts the connecting nodes in the argument array, to avoid repeated object creation and so speed up multiple accesses.
	 * create array with size of either getNodeOutDegree(nodeIndex) or maxDegree(). array elements beyond nodeDegree(nodeIndex)-1 are undefined.
	 * Will throw an exception if ret is not large enough.
	 * 
	 * @param ret this is populated with the connecting edges found
	 * @param nodeIndex the node
	 * @return all edges that leave the node via the argument array. 
	 */
	public void getNodeConnectingOutEdges(int[] ret, int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_OUT_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeOutDegree(nodeIndex);
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_EDGE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
	}


	/**
	 * For directed graphs. 
	 *
	 * @param nodeIndex the node
 	 * @return all node neighbours that are on the end of edges that leave the passed node. 
	 */
	public int[] getNodeConnectingOutNodes(int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_OUT_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeOutDegree(nodeIndex);
		
		int[] ret = new int[degree];
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_NODE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
		
		return ret;
	}
	
	
	/**
	 * For directed graphs. 
	 * This version puts the connecting nodes in the argument array, to avoid repeated object creation and so speed up multiple accesses.
	 * create array with size of either getNodeOutDegree(nodeIndex) or maxDegree(). array elements beyond nodeDegree(nodeIndex)-1 are undefined.
	 * Will throw an exception if ret is not large enough.
	 * 
	 * @param ret this is populated with the connecting nodes found
	 * @param nodeIndex the node
  	 * @return all node neighbours that are on the end of edges that leave the passed node via the parameter array. 
	 */
	public void getNodeConnectingOutNodes(int[] ret, int nodeIndex) {
		
		int connectionOffset = nodeBuf.getInt(NODE_OUT_CONNECTION_START_OFFSET+nodeIndex*NODE_BYTE_SIZE); // in offset is the first one
		int degree = getNodeOutDegree(nodeIndex);
		
		for(int i = 0; i < degree; i++) {
			// don't need the edge, so step over edge/node pairs and the ege
			int nodeOffset = connectionOffset+(i*CONNECTION_PAIR_SIZE)+CONNECTION_NODE_OFFSET;
			int node = connectionBuf.getInt(nodeOffset);
			ret[i] = node;
		}
	}


	/**
	 * @param edgeIndex the edge
	 * @return the edge label
	 */
	public String getEdgeLabel(int edgeIndex) {
		int labelStart = edgeBuf.getInt(EDGE_LABEL_START_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
		int labelLength = edgeBuf.getShort(EDGE_LABEL_LENGTH_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
		char[] label = new char[labelLength];
		for(int i = 0; i < labelLength; i++) {
			int offset = labelStart+i*2;
			char c = edgeLabelBuf.getChar(offset);
			label[i] = c;
		}
		String ret = new String(label);
		return ret;
	}

	
	/**
	 * @param edgeIndex the edge
	 * @return the first connecting node (the node the edge leaves for directed graphs).
	 */
	public int getEdgeNode1(int edgeIndex) {
		int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
		return n1;
	}
	
	
	/**
	 * @param edgeIndex the edge
	 * @return the second connecting node (the node the edge enters for directed graphs).
	 */
	public int getEdgeNode2(int edgeIndex) {
		int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
		return n2;
	}
	
	
	/**
	 * @param edgeIndex the edge
	 * @return the edge weight
	 */
	public int getEdgeWeight(int edgeIndex) {
		int type = edgeBuf.getInt(EDGE_WEIGHT_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
		return type;
	}
	
	
	/**
	 * @param edgeIndex the edge
	 * @return the edge type
	 */
	public byte getEdgeType(int edgeIndex) {
		byte type= edgeBuf.get(EDGE_TYPE_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
		return type;
	}
	
	
	/**
	 * @param edgeIndex the edge
	 * @return the edge age
	 */
	public byte getEdgeAge(int edgeIndex) {
		byte age = edgeBuf.get(EDGE_AGE_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
		return age;
	}
	
	
	/**
	 * Names should be simple alphanumeric. Spaces and dashes are permitted. Note that tilde ("~") cannot be used.
	 * @param name the name of the graph
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @param nodeIndex the node
	 * @param weight the new node weight
	 */
	public void setNodeWeight(int nodeIndex, int weight) {
		nodeBuf.putInt(NODE_WEIGHT_OFFSET+nodeIndex*NODE_BYTE_SIZE, weight);
	}
	
	
	/**
	 * @param nodeIndex the node
	 * @param type the new node type
	 */
	public void setNodeType(int nodeIndex, byte type) {
		nodeBuf.put(NODE_TYPE_OFFSET+nodeIndex*NODE_BYTE_SIZE, type);
	}
	
	
	/**
	 * @param nodeIndex the node
	 * @return the node age
	 */
	public void setNodeAge(int nodeIndex, byte age) {
		nodeBuf.put(NODE_AGE_OFFSET+nodeIndex*NODE_BYTE_SIZE, age);
	}
	

	
	/**
	 * @param edgeIndex the edge
	 * @param weight the new edge weight
	 */
	public void setEdgeWeight(int edgeIndex, int weight) {
		edgeBuf.putInt(EDGE_WEIGHT_OFFSET+edgeIndex*EDGE_BYTE_SIZE, weight);
	}
	
	
	/**
	 * @param edgeIndex the edge
	 * @param type the new edge type
	 */
	public void setEdgeType(int edgeIndex, byte type) {
		edgeBuf.put(EDGE_TYPE_OFFSET+edgeIndex*EDGE_BYTE_SIZE, type);
	}
	
	
	/**
	 * @param edgeIndex the edge
	 * @return the edge age
	 */
	public void setEdgeAge(int edgeIndex, byte age) {
		edgeBuf.put(EDGE_AGE_OFFSET+edgeIndex*EDGE_BYTE_SIZE, age);
	}
	

	/**
	 * Change all the node labels in the graph. Creates a new nodeLabelBuf, changes the label pointers in nodeBuf.
	 * 
	 * @param labels Must contain the same number of labels as number of nodes in the graph
	 */
	public void setAllNodeLabels(String[] labels) {
		
		long totalLabelLength = 0;
		
		for(int i = 0; i < numberOfNodes; i++) {
			totalLabelLength += labels[i].length();
		}
		
		if(totalLabelLength*2 > MAX_BYTE_BUFFER_SIZE) {
			throw new OutOfMemoryError("Tried to create a nodeLabelBuf with too many chars");
		}
		int bufSize = (int)(totalLabelLength*2); // this cast is safe because of the previous test
		
		if(!direct) {
			nodeLabelBuf = ByteBuffer.allocate(bufSize);
		} else {
			nodeLabelBuf = ByteBuffer.allocateDirect(bufSize);
		}
		nodeLabelBuf.clear();
		int labelOffset = 0;
		for(int i = 0; i < numberOfNodes; i++) {
			String label = labels[i];
			char[] labelArray = label.toCharArray();
			short labelLength = (short)(labelArray.length);
	
			nodeBuf.putInt(NODE_LABEL_START_OFFSET+i*NODE_BYTE_SIZE,labelOffset); // label start
			nodeBuf.putShort(NODE_LABEL_LENGTH_OFFSET+i*NODE_BYTE_SIZE,labelLength); // label size
	
			for(int j = 0; j < labelArray.length; j++) {
				char c = labelArray[j];
				nodeLabelBuf.putChar(labelOffset,c);
				labelOffset += 2;  // increment by 2 as it is a char (2 bytes)
			}
		}
		
	}
	
	/**
	 * Change all the edge labels in the graph. Creates a new edgeLabelBuf, changes the label pointers in edgeBuf
	 * 
	 * @param labels Must contain the same number of labels as there are edges in the graph
	 * @throws OutofMemoryError
	 */
	public void setAllEdgeLabels(String[] labels)  {
		
		long totalLabelLength = 0;
		
		for(int i = 0; i < numberOfEdges; i++) {
			totalLabelLength += labels[i].length();
		}
		
		if(totalLabelLength*2 > MAX_BYTE_BUFFER_SIZE) {
			throw new OutOfMemoryError("Tried to create a edgeLabelBuf with too many chars");
		}
		int bufSize = (int)(totalLabelLength*2); // this cast is safe because of the previous test
		
		if(!direct) {
			edgeLabelBuf = ByteBuffer.allocate(bufSize);
		} else {
			edgeLabelBuf = ByteBuffer.allocateDirect(bufSize);
		}
		edgeLabelBuf.clear();

		int labelOffset = 0;
		for(int i = 0; i < numberOfEdges; i++) {
			String label = labels[i];
			char[] labelArray = label.toCharArray();
			short labelLength = (short)(labelArray.length);
	
			edgeBuf.putInt(EDGE_LABEL_START_OFFSET+i*EDGE_BYTE_SIZE,labelOffset); // label start
			edgeBuf.putShort(EDGE_LABEL_LENGTH_OFFSET+i*EDGE_BYTE_SIZE,labelLength); // label size
	
			for(int j = 0; j < labelArray.length; j++) {
				char c = labelArray[j];
				edgeLabelBuf.putChar(labelOffset,c);
				labelOffset += 2;  // increment by 2 as it is a char (2 bytes)
			}
		}

	}
	
	
	/**
	 * gets the other node connecting to the edge.
	 * If the argument node is not connected to the edge, then an undefined node will
	 * be returned.
	 * 
	 * @param edge the edge
	 * @param node the known node
	 * @return the node on the opposite side of the edge
	 */
	public int oppositeEnd(int edge, int node) {
		int n1 = getEdgeNode1(edge);
		int n2 = getEdgeNode2(edge);
		
		if(n1 == node) {
			return n2;
		}
		return n1;
	}


	
	
	/**
	 * Allocates space for the node, edge and connection ByteBuffers. The label ByteBuffers
	 * are created later
	 */
	private void init() {

		if(!direct) {
			nodeBuf = ByteBuffer.allocate(numberOfNodes*NODE_BYTE_SIZE);
			edgeBuf = ByteBuffer.allocate(numberOfEdges*EDGE_BYTE_SIZE);
			connectionBuf = ByteBuffer.allocate(numberOfEdges*2*CONNECTION_PAIR_SIZE);
			// nodeLabelBuf and edgeLabelBuf now created in Factories by setAllNodeLabels
		} else {
			nodeBuf = ByteBuffer.allocateDirect(numberOfNodes*NODE_BYTE_SIZE);
			edgeBuf = ByteBuffer.allocateDirect(numberOfEdges*EDGE_BYTE_SIZE);
			connectionBuf = ByteBuffer.allocateDirect(numberOfEdges*2*CONNECTION_PAIR_SIZE);
			// nodeLabelBuf and edgeLabelBuf now created in Factories by setAllNodeLabels
		}
		
		nodeBuf.clear();
		edgeBuf.clear();
		connectionBuf.clear();
		
	}


	/**
	 * Create a FastGraph from a json string.
	 *
	 * @param json the json as a string
	 * @param direct if true then off heap ByteBuffers, if false then on heap ByteBuffers
	 * @return the created FastGraph.
	 */
	public static FastGraph jsonStringGraphFactory(String json, boolean direct) {
		
		int nodeCount = 0;
		int edgeCount = 0;
		
		JSONObject jsonObj = new JSONObject(json);
		
		String graphName = jsonObj.getString("name");
		
		JSONArray nodes = jsonObj.getJSONArray("nodes");
		Iterator<Object> itNodes = nodes.iterator();
		while(itNodes.hasNext()) {
			JSONObject node = (JSONObject)(itNodes.next());
			int index = node.getInt("nodeIndex");
			if(index+1 > nodeCount) {
				nodeCount = index+1;
			}
		}
		
		JSONArray edges = jsonObj.getJSONArray("edges");
		Iterator<Object> itEdges = edges.iterator();
		while(itEdges.hasNext()) {
			JSONObject edge = (JSONObject)(itEdges.next());
			int index = edge.getInt("edgeIndex");
			if(index+1 > edgeCount) {
				edgeCount = index+1;
			}
		}

		FastGraph g = new FastGraph(nodeCount,edgeCount,direct);
		g.populateFromJsonString(jsonObj);
		g.setName(graphName);
		
		return g;
	}
	

	/**
	 * Generate a random graph of the desired size. Self sourcing edges and parallel edges may exist.
	 * 
	 * @param numberOfNodes the number of nodes in the graph
	 * @param numberOfEdges the number of edges in the graph
	 * @param direct if true then off heap ByteBuffers, if false then on heap ByteBuffers
	 * @return the created FastGraph
	 * @throws Exception 
	 */
	public static FastGraph randomGraphFactory(int numberOfNodes, int numberOfEdges, boolean direct) throws Exception {
		FastGraph graph = randomGraphFactory(numberOfNodes, numberOfEdges, -1, false, direct);
		return graph;
	}
	
	/**
	 * Generate a random graph of the desired size. Self sourcing edges and parallel edges may exist.
	 * 
	 * @param numberOfNodes the number of nodes in the graph
	 * @param numberOfEdges the number of edges in the graph
	 * @param seed random number seed, -1 for current time
	 * @param direct if true then off heap ByteBuffers, if false then on heap ByteBuffers
	 * @return the created FastGraph
	 * @throws Exception 
	 */
	public static FastGraph randomGraphFactory(int numberOfNodes, int numberOfEdges, long seed, boolean direct) throws Exception {
		FastGraph graph = randomGraphFactory(numberOfNodes, numberOfEdges, seed, false, direct);
		return graph;
	}
	
	
	/**
	 * Generate a random graph of the desired size. Self sourcing edges and parallel edges may exist.
	 * 
	 * @param numberOfNodes the number of nodes in the graph
	 * @param numberOfEdges the number of edges in the graph
	 * @param seed random number seed, -1 for current time
	 * @param direct if true then off heap ByteBuffers, if false then on heap ByteBuffers
	 * @return the created FastGraph
	 * @throws Exception
	 */
	public static FastGraph randomGraphFactory(int numberOfNodes, int numberOfEdges, long seed, boolean simple, boolean direct) throws Exception {
		FastGraph g = new FastGraph(numberOfNodes,numberOfEdges,direct);
		g.setName("random-n-"+numberOfNodes+"-e-"+numberOfEdges);
		g.populateRandomGraph(simple, seed);
		return g;
	}
	

	/**
	 * creates a FastGraph by loading in various files from the given directory, or data under
	 * current working directory if directory is null.
	 * 
	 * @param directory where the files are held, or if null fileBaseName under data under the current working directory
	 * @param fileBaseName the name of the files, to which extensions are added
	 * @return the created FastGraph
	 * @throws IOException If the buffers cannot be loaded
	 * @See loadBuffers
	 */
	public static FastGraph loadBuffersGraphFactory(String directory, String fileBaseName) throws IOException {
		FastGraph g = loadBuffers(directory,fileBaseName);
		return g;
	}

	
	
	/**
	 * Populates the FastGraph ByteBuffers from a json string.
	 * @param jsonObj the json code after parsing
	 */
	private void populateFromJsonString(JSONObject jsonObj) {

		//long time;

		String[] nodeLabels = new String[numberOfNodes];
		String[] edgeLabels = new String[numberOfEdges];
		int inStart = -888;
		int inLength = -3;
		int outStart = -777;
		int outLength = -2;
		int index = -1;
		int weight = -5;
		byte type = -7;
		byte age = -9;
		String label;
		
		//the nodes are the first elements
		JSONArray nodes = jsonObj.getJSONArray("nodes");
		Iterator<Object> itNodes = nodes.iterator();
		while(itNodes.hasNext()) {
			
			JSONObject node = (JSONObject)(itNodes.next());
			index = node.getInt("nodeIndex");
			weight = node.getInt("nodeWeight");
			type = (byte)(node.getInt("nodeType"));
			age = (byte)(node.getInt("nodeAge"));
			label = node.getString("nodeLabel");
			
			nodeBuf.putInt(NODE_IN_CONNECTION_START_OFFSET+index*NODE_BYTE_SIZE,inStart); // offset for inward connecting edges/nodes
			nodeBuf.putInt(NODE_IN_DEGREE_OFFSET+index*NODE_BYTE_SIZE,inLength); // number of inward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_CONNECTION_START_OFFSET+index*NODE_BYTE_SIZE,outStart); // offset for outward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_DEGREE_OFFSET+index*NODE_BYTE_SIZE,outLength); // number of outward connecting edges/nodes
			nodeBuf.putInt(NODE_WEIGHT_OFFSET+index*NODE_BYTE_SIZE,weight); // weight
			nodeBuf.put(NODE_TYPE_OFFSET+index*NODE_BYTE_SIZE,type); // type
			nodeBuf.put(NODE_AGE_OFFSET+index*NODE_BYTE_SIZE,age); // age
			
			// save labels for later
			nodeLabels[index] = label;

		}

		setAllNodeLabels(nodeLabels);

		ArrayList<ArrayList<Integer>> nodeIn = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of inward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeIn.add(i,edges);
		}
		
		ArrayList<ArrayList<Integer>> nodeOut = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of outward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeOut.add(i,edges);
		}
		
		ArrayList<Integer> inEdgeList;	
		ArrayList<Integer> outEdgeList;	
		int node1;
		int node2;
		index = -1;
		weight = -101;
		type = -103;
		age = -105;
		//time = Debugger.createTime();
		
		//populate the edges
		JSONArray edges = jsonObj.getJSONArray("edges");
		Iterator<Object> itEdges = edges.iterator();
		while(itEdges.hasNext()) {
			
			JSONObject edge = (JSONObject)(itEdges.next());
			index = edge.getInt("edgeIndex");
			node1 = edge.getInt("node1");
			node2 = edge.getInt("node2");
			weight = edge.getInt("edgeWeight");
			type = (byte)(edge.getInt("edgeType"));
			age = (byte)(edge.getInt("edgeAge"));
			label = edge.getString("edgeLabel");
			
			edgeBuf.putInt(EDGE_NODE1_OFFSET+index*EDGE_BYTE_SIZE,node1); // one end of edge
			edgeBuf.putInt(EDGE_NODE2_OFFSET+index*EDGE_BYTE_SIZE,node2); // other end of edge
			edgeBuf.putInt(EDGE_WEIGHT_OFFSET+index*EDGE_BYTE_SIZE,weight); // weight
			edgeBuf.put(EDGE_TYPE_OFFSET+index*EDGE_BYTE_SIZE,type); // type
			edgeBuf.put(EDGE_AGE_OFFSET+index*EDGE_BYTE_SIZE,age); // age
			
			// save labels for later
			edgeLabels[index] = label;
			
			// store connecting nodes
			inEdgeList = nodeIn.get(node2);
			inEdgeList.add(index);
			outEdgeList = nodeOut.get(node1);
			outEdgeList.add(index);
			

		}

		setAllEdgeLabels(edgeLabels);
	
		// Initialise the connection buffer, modifying the node buffer connection data
		int offset = 0;
		for(int i = 0; i < numberOfNodes; i++) {
			// setting the in connection offset and length
			ArrayList<Integer> inEdges = nodeIn.get(i);
			int inEdgeLength = inEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_DEGREE_OFFSET,inEdgeLength);
			// now put the in edge/node pairs
			for(int edgeIndex : inEdges) {
				int n = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				if(n1 == i) {
					n = n2;
				} else if(n2 == i) {
					n = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+edgeIndex+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,edgeIndex);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,n);
				offset += CONNECTION_PAIR_SIZE;
			}
			
			// setting the out connection offset and length
			ArrayList<Integer> outEdges = nodeOut.get(i);
			int outEdgeLength = outEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_DEGREE_OFFSET,outEdgeLength);

			// now put the out edge/node pairs
			for(int edgeIndex : outEdges) {
				int n = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				if(n1 == i) {
					n = n2;
				} else if(n2 == i) {
					n = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+edgeIndex+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,edgeIndex);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,n);
				offset += CONNECTION_PAIR_SIZE;
			}
		}

	}



	/**
	 * saves the current graph to several files, in directory given to base name given (i.e. fileBaseName should have no extension).
	 * If directory is null, then to a directory named data under current working directory.
	 * 
	 * @param directory where the files are to be stored, or if null fileBaseName under data under the current working directory
	 * @param fileBaseName the name of the files, to which extensions are added
	 */
	public void saveBuffers(String directory, String fileBaseName) {
		
		String directoryAndBaseName = "";
		if(directory != null) {
			if(directory.charAt(directory.length()-1)== File.separatorChar) {
				directoryAndBaseName = directory+fileBaseName;
			} else {
				directoryAndBaseName = directory+File.separatorChar+fileBaseName;
			}
			new File(Launcher.startingWorkingDirectory+File.separatorChar+directory).mkdirs();
		} else {
			directoryAndBaseName = Launcher.startingWorkingDirectory+File.separatorChar+"data"+File.separatorChar+fileBaseName+File.separatorChar+fileBaseName;
			new File(Launcher.startingWorkingDirectory+File.separatorChar+"data"+File.separatorChar+fileBaseName).mkdirs();
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(directoryAndBaseName+".info");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
			writer.write("name"+INFO_SPLIT_STRING+name+"\n");
			writer.write("numberOfNodes"+INFO_SPLIT_STRING+numberOfNodes+"\n");
			writer.write("numberOfEdges"+INFO_SPLIT_STRING+numberOfEdges+"\n");
			writer.write("numberOfNodeLabelBytes"+INFO_SPLIT_STRING+nodeLabelBuf.capacity()+"\n");
			writer.write("numberOfEdgeLabelBytes"+INFO_SPLIT_STRING+edgeLabelBuf.capacity()+"\n");
			String directValue = "false";
			if(direct) {
				directValue = "true";
			}
			writer.write("direct"+INFO_SPLIT_STRING+directValue+"\n");

			writer.close();
			fos.close();
		} catch(Exception e) {
			Debugger.log("ERROR executing info file save in saveBuffers("+directory+","+fileBaseName+")");
			e.printStackTrace();
		}
			
		try {
			writeBuf(directoryAndBaseName+".nodeBuf",nodeBuf);
			writeBuf(directoryAndBaseName+".edgeBuf",edgeBuf);
			writeBuf(directoryAndBaseName+".connectionBuf",connectionBuf);
			writeBuf(directoryAndBaseName+".nodeLabelBuf",nodeLabelBuf);
			writeBuf(directoryAndBaseName+".edgeLabelBuf",edgeLabelBuf);
		} catch(Exception e) {
			Debugger.log("ERROR executing buffer save in saveBuffers("+directory+","+fileBaseName+")");
			e.printStackTrace();
		}
			
	}
	

	/**
	 * Save a ByteBuffer to a file.
	 * 
	 * @param file name to write to
	 * @param buf buffer to be written
	 * @throws Exception if file save fails
	 */
	private void writeBuf(String fileName, ByteBuffer buf) throws Exception {
		
		try {
			buf.rewind();
			File file = new File(fileName);
			FileOutputStream fos = new FileOutputStream(file, false);
			FileChannel wChannel = fos.getChannel();
			wChannel.write(buf);
			fos.flush();
			wChannel.close();
			fos.close();
		} catch(Exception e) {
			Debugger.log("ERROR executing writeBuf("+fileName+","+buf+")");
			e.printStackTrace();
		}

	}

	
	/**
	 * Creates a graph from a SNAP .txt adjacency list file. Number of nodes and edges are given
	 * by the <a href="https://snap.stanford.edu/data/">SNAP website</a>. Assumes edges
	 * represented by one node index pair per line delimited by tabs or spaces, ignores lines starting with # and any line without a tab.
	 * Looks for the file in given directory If directory is null, then to a
	 * directory named data/snap under current working directory.
	 * 
	 * @param nodeCount the number of nodes
	 * @param edgeCount the number of edges
	 * @param dir the directory for the file, if null then a directory called data/ under the current working directory
	 * @param fileName the file name for the file
	 * @param direct if true the ByteBuffers are direct, if false they are allocated on the heap
	 * 
	 * @throws Exception Throws if the adjacency list cannot be built correctly. Might be an IO error
	 */
	public static FastGraph adjacencyListGraphFactory(int nodeCount, int edgeCount, String dir, String fileName, boolean direct) throws Exception {
		FastGraph g = new FastGraph(nodeCount,edgeCount,direct);
		g.setName(fileName);
		g.loadAdjacencyListGraph(dir,fileName);
		return g;
	}


	
	/**
     * Assumes edges represented by one node index pair per line delimited by
     * tabs or spaces, ignores lines starting with # and any line without a tab.
	 * Looks for the file in given directory. If directory is null, then to a
	 * directory named /data/snap under current working directory.
	 * 
	 * @param dir the directory for the file, if null then a directory called data/ under the current working directory
	 * @param fileName the fileName for the file
	 * 
	 * @throws IOException If the buffers cannot be loaded
	 */
	private void loadAdjacencyListGraph(String dir, String fileName) throws Exception {
	
		String directory = dir;
		if(directory == null) {
			directory = Launcher.startingWorkingDirectory+File.separatorChar+"data"+File.separatorChar+"snap";
		}
		String path = null;
		if(directory.charAt(directory.length()-1)== File.separatorChar) {
			path = directory+fileName;
		} else {
			path = directory+File.separatorChar+fileName;
		}
		
		int edgeIndex = 0;
		int nodeIndex = 0;
		HashMap<String,Integer> nodeSnapIdToIndexMap = new HashMap<String,Integer>(numberOfNodes*4);
		HashMap<Integer,String> nodeIndexToSnapIdMap = new HashMap<Integer,String>(numberOfNodes*4);
		HashMap<Integer,Integer> edgeNode1Map = new HashMap<Integer,Integer>(numberOfEdges*4);
		HashMap<Integer,Integer> edgeNode2Map = new HashMap<Integer,Integer>(numberOfEdges*4);
		
		File f = new File(path);
		if(!f.exists()) {
			throw new IOException("Problem loading file "+path+". If you expect to access a SNAP file try downloading the file from:\nhttps://snap.stanford.edu/data/\nthen unzipping it and placing it in the directory "+directory);
			//System.exit(1);
		}
		
		FileInputStream is = new FileInputStream(path);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		String[] splitLine;
		
		String line = "";
long time = Debugger.createTime();			
		while(line != null) {
			line = br.readLine();
			if(line == null) {
				continue;
			}
			if(line.length() == 0) {
				continue;
			}
			if(line.charAt(0) == '#') {
				continue;
			}
			splitLine = line.split(" ");
			if(splitLine.length < 2) {
				splitLine = line.split("\t");
				if(splitLine.length < 2) {
					Debugger.log("FAILED TO RECOGNISE LINE:"+line+" in loadAdjacencyListGraph("+directory+","+fileName+")");
					continue;
				}
			}
	
			String node1String = splitLine[0];
			String node2String = splitLine[1];
			
			if(!nodeSnapIdToIndexMap.containsKey(node1String)) {
				nodeSnapIdToIndexMap.put(node1String,nodeIndex);
				nodeIndexToSnapIdMap.put(nodeIndex,node1String);
				nodeIndex++;
			}
			if(!nodeSnapIdToIndexMap.containsKey(node2String)) {
				nodeSnapIdToIndexMap.put(node2String,nodeIndex);
				nodeIndexToSnapIdMap.put(nodeIndex,node2String);
				nodeIndex++;
			}
			edgeNode1Map.put(edgeIndex, nodeSnapIdToIndexMap.get(node1String));
			edgeNode2Map.put(edgeIndex, nodeSnapIdToIndexMap.get(node2String));
				
			edgeIndex++;
if(edgeIndex%1000000==0 ) {
	Debugger.outputTime("edgesLoaded "+edgeIndex+" time ");
}
		}

		String[] nodeLabels = new String[numberOfNodes];
		String[] edgeLabels = new String[numberOfEdges];
		int inStart = -88;
		int inLength = -33;
		int outStart = -77;
		int outLength = -22;
		int weight = -55;
		byte type = -77;
		byte age = -99;
		for(int i = 0; i < numberOfNodes; i++) {
			nodeBuf.putInt(NODE_IN_CONNECTION_START_OFFSET+i*NODE_BYTE_SIZE,inStart); // offset for inward connecting edges/nodes
			nodeBuf.putInt(NODE_IN_DEGREE_OFFSET+i*NODE_BYTE_SIZE,inLength); // number of inward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_CONNECTION_START_OFFSET+i*NODE_BYTE_SIZE,outStart); // offset for outward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_DEGREE_OFFSET+i*NODE_BYTE_SIZE,outLength); // number of outward connecting edges/nodes
			nodeBuf.putInt(NODE_WEIGHT_OFFSET+i*NODE_BYTE_SIZE,weight); // weight
			nodeBuf.put(NODE_TYPE_OFFSET+i*NODE_BYTE_SIZE,type); // type
			nodeBuf.put(NODE_AGE_OFFSET+i*NODE_BYTE_SIZE,age); // age

			// save labels for later
			String label = nodeIndexToSnapIdMap.get(i);
			nodeLabels[i] = label;

		}

		setAllNodeLabels(nodeLabels);

		ArrayList<ArrayList<Integer>> nodeIn = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of inward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeIn.add(i,edges);
		}
		
		ArrayList<ArrayList<Integer>> nodeOut = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of outward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeOut.add(i,edges);
		}
				
		ArrayList<Integer> inEdgeList;	
		ArrayList<Integer> outEdgeList;	
		int node1;
		int node2;
		weight = -51;
		type = -53;
		age = -55;
		for(int i = 0; i < numberOfEdges; i++) {
			node1 = edgeNode1Map.get(i);
			node2 = edgeNode2Map.get(i);
			edgeBuf.putInt(EDGE_NODE1_OFFSET+i*EDGE_BYTE_SIZE,node1); // one end of edge
			edgeBuf.putInt(EDGE_NODE2_OFFSET+i*EDGE_BYTE_SIZE,node2); // other end of edge
			edgeBuf.putInt(EDGE_WEIGHT_OFFSET+i*EDGE_BYTE_SIZE,weight); // weight
			edgeBuf.put(EDGE_TYPE_OFFSET+i*EDGE_BYTE_SIZE,type); // type
			edgeBuf.put(EDGE_AGE_OFFSET+i*EDGE_BYTE_SIZE,age); // age
			
			// store labels for later
			String label = "e"+i;
			edgeLabels[i] = label;
			
			// store connecting nodes
			inEdgeList = nodeIn.get(node2);
			inEdgeList.add(i);
			outEdgeList = nodeOut.get(node1);
			outEdgeList.add(i);

		}
		
		setAllEdgeLabels(edgeLabels);


		// Initialise the connection buffer, modifying the node buffer connection data
		//time = Debugger.createTime();
		int offset = 0;
		for(int i = 0; i < numberOfNodes; i++) {
			// setting the in connection offset and length
			ArrayList<Integer> inEdges = nodeIn.get(i);
			int inEdgeLength = inEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_DEGREE_OFFSET,inEdgeLength);
		
			// now put the in edge/node pairs
			for(int e : inEdges) {
				int n = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+e*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+e*EDGE_BYTE_SIZE);
				if(n1 == i) {
					n = n2;
				} else if(n2 == i) {
					n = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+e+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,e);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,n);
				offset += CONNECTION_PAIR_SIZE;
			}
			
			// setting the out connection offset and length
			ArrayList<Integer> outEdges = nodeOut.get(i);
			int outEdgeLength = outEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_DEGREE_OFFSET,outEdgeLength);
		
			// now put the out edge/node pairs
			for(int e : outEdges) {
				int n = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+e*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+e*EDGE_BYTE_SIZE);
				if(n1 == i) {
					n = n2;
				} else if(n2 == i) {
					n = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+e+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,e);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,n);
				offset += CONNECTION_PAIR_SIZE;
			}
		}
	}
	
	
	/**
	 * Creates a graph from two files: baseFileName.nodes and baseFileName.edges.
	 * Files are structured as line *"\n" separated) lists of items. Each element
	 * in an item is tab ("\t") separated. Hence no tabs in file names are allowed.
	 * <br/>
	 * Nodes are lists of <code>index	label	weight	type	age</code>
	 * <br>
	 * where index must start at 0 and end at nodeCount-1, label is a string, weight is
	 * integer valued, type is byte valued and age is byte valued.
	 * <br/> 
	 * Edges are lists of <code>index	node1Index	node2Index	label	weight	type	age</code>
	 * <br>
	 * where index must start at 0 and end at edgeCount-1, node1Index is a node index,
	 * node2Index is a nodeIndex, label is a string, weight is 
	 * integer valued, type is byte valued and age is byte valued.
	 * <br>
	 * Ignores empty lines and lines starting with a hash ("#").
	 * 
	 * @param nodeCount the number of nodes
	 * @param edgeCount the number of edges
	 * @param dir the directory for the file, if null then a directory called data/ under the current working directory
	 * @param baseFileName the base of the file name for the file, two files called baseFileName.nodes and baseFileName.edges are expected
	 * @param direct if true the ByteBuffers are direct, if false they are allocated on the heap
     *
	 * @throws Exception Throws if the graph cannot be built correctly. Might be an IO error
	 */
	public static FastGraph nodeListEdgeListGraphFactory(int nodeCount, int edgeCount, String dir, String baseFileName, boolean direct) throws Exception {
		FastGraph g = new FastGraph(nodeCount,edgeCount,direct);
		g.setName(baseFileName);
		g.loadnodeListEdgeListGraph(dir,baseFileName);
		return g;
	}


	
	/**
	 * Populates a graph from two files: baseFileName.nodes and baseFileName.edges.
	 * Files are structured as line *"\n" separated) lists of items. Each element
	 * in an item is tab ("\t") separated. Hence no tabs in file names are allowed.
	 * <br/>
	 * Nodes are lists of <code>index	label	weight	type	age</code>
	 * <br>
	 * where index must start at 0 and end at nodeCount-1, label is a string, weight is
	 * integer valued, type is byte valued and age is byte valued.
	 * <br/> 
	 * Edges are lists of <code>index	node1Index	node2Index	label	weight	type	age</code>
	 * <br>
	 * where index must start at 0 and end at edgeCount-1, node1Index is a node index,
	 * node2Index is a nodeIndex, label is a string, weight is 
	 * integer valued, type is byte valued and age is byte valued.
	 * <br>
	 * Ignores empty lines and lines starting with a hash ("#").
	 * 
	 * @param dir the directory for the file, if null then a directory called data/ under the current working directory
	 * @param baseFileName the base of the file name for the file, two files called baseFileName.nodes and baseFileName.edges are expected
	 * 
	 * @throws IOException If the buffers cannot be loaded
	 * 
	 */
	private void loadnodeListEdgeListGraph(String dir, String baseFileName) throws Exception {
	
		String directory = dir;
		if(directory == null) {
			directory = Launcher.startingWorkingDirectory+File.separatorChar+"data";
		}
		String basePath = null;
		if(directory.charAt(directory.length()-1)== File.separatorChar) {
			basePath = directory+baseFileName;
		} else {
			basePath = directory+File.separatorChar+baseFileName;
		}
		
		String nodePath = basePath+".nodes";
		File f = new File(nodePath);
		if(!f.exists()) {
			throw new IOException("Problem loading file "+nodePath);
		}
		
		FileInputStream is = new FileInputStream(nodePath);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		// load the nodes
		String[] splitLine;
		String[] nodeLabels = new String[numberOfNodes];
		int inStart = -18;
		int inLength = -13;
		int outStart = -21;
		int outLength = -23;
		int index = -1;
		String label;
		int weight = -15;
		byte type = -17;
		byte age = -19;
		String nodeLine = "";
		while(nodeLine != null) {
			nodeLine = br.readLine();
			if(nodeLine == null) {
				continue;
			}
			if(nodeLine.length() == 0) {
				continue;
			}
			if(nodeLine.charAt(0) == '#') {
				continue;
			}
			
			splitLine = nodeLine.split("\t");
			
			if(splitLine.length < 5) {
				br.close();
				throw new IOException("Not enough elements, needs 5 tab separated elements in "+nodeLine);
			}
	
			try {
				index = Integer.parseInt(splitLine[0]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing node index in line "+nodeLine);
			}
			if(index > numberOfNodes) {
				br.close();
				throw new IOException("index "+index+" is greater than the number of nodes "+numberOfNodes);
			}
			label = splitLine[1];
			try {
				weight = Integer.parseInt(splitLine[2]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing node weight in line "+nodeLine);
			}
			try {
				type = Byte.parseByte(splitLine[3]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing node type in line "+nodeLine);
			}
			try {
				age = Byte.parseByte(splitLine[4]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing node age in line "+nodeLine);
			}
			
			nodeBuf.putInt(NODE_IN_CONNECTION_START_OFFSET+index*NODE_BYTE_SIZE,inStart); // offset for inward connecting edges/nodes
			nodeBuf.putInt(NODE_IN_DEGREE_OFFSET+index*NODE_BYTE_SIZE,inLength); // number of inward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_CONNECTION_START_OFFSET+index*NODE_BYTE_SIZE,outStart); // offset for outward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_DEGREE_OFFSET+index*NODE_BYTE_SIZE,outLength); // number of outward connecting edges/nodes
			nodeBuf.putInt(NODE_WEIGHT_OFFSET+index*NODE_BYTE_SIZE,weight); // weight
			nodeBuf.put(NODE_TYPE_OFFSET+index*NODE_BYTE_SIZE,type); // type
			nodeBuf.put(NODE_AGE_OFFSET+index*NODE_BYTE_SIZE,age); // age

			// save labels for later
			nodeLabels[index] = label;

		}
		br.close();

		setAllNodeLabels(nodeLabels);

		String[] edgeLabels = new String[numberOfEdges];
		
		String edgePath = basePath+".edges";
		f = new File(edgePath);
		if(!f.exists()) {
			throw new IOException("Problem loading file "+edgePath+""+directory);
		}
		
		is = new FileInputStream(edgePath);
		isr = new InputStreamReader(is);
		br = new BufferedReader(isr);


		// load the Edges
		ArrayList<ArrayList<Integer>> nodeIn = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of inward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeIn.add(i,edges);
		}
		
		ArrayList<ArrayList<Integer>> nodeOut = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of outward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeOut.add(i,edges);
		}
				
		ArrayList<Integer> inEdgeList;	
		ArrayList<Integer> outEdgeList;	
		
		int node1 = -64;
		int node2 = -65;
		String edgeLine = "";
		while(edgeLine != null) {
			edgeLine = br.readLine();
			if(edgeLine == null) {
				continue;
			}
			if(edgeLine.length() == 0) {
				continue;
			}
			if(edgeLine.charAt(0) == '#') {
				continue;
			}
			
			splitLine = edgeLine.split("\t");
			
			if(splitLine.length < 7) {
				br.close();
				throw new IOException("Not enough elements, needs 7 tab separated elements in "+edgeLine);
			}
	
			try {
				index = Integer.parseInt(splitLine[0]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing edge index in line "+edgeLine);
			}
			if(index > numberOfEdges) {
				br.close();
				throw new IOException("index "+index+" is greater than the number of edges "+numberOfEdges);
			}
			try {
				node1 = Integer.parseInt(splitLine[1]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing node 1 index in line "+edgeLine);
			}
			try {
				node2 = Integer.parseInt(splitLine[2]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing node 2 index in line "+edgeLine);
			}
			label = splitLine[3];
			try {
				weight = Integer.parseInt(splitLine[4]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing edge weight in line "+edgeLine);
			}
			try {
				type = Byte.parseByte(splitLine[5]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing edge type in line "+edgeLine);
			}
			try {
				age = Byte.parseByte(splitLine[6]);
			} catch(NumberFormatException e) {
				br.close();
				throw new IOException("Problem parsing edge age in line "+edgeLine);
			}

			edgeBuf.putInt(EDGE_NODE1_OFFSET+index*EDGE_BYTE_SIZE,node1); // one end of edge
			edgeBuf.putInt(EDGE_NODE2_OFFSET+index*EDGE_BYTE_SIZE,node2); // other end of edge
			edgeBuf.putInt(EDGE_WEIGHT_OFFSET+index*EDGE_BYTE_SIZE,weight); // weight
			edgeBuf.put(EDGE_TYPE_OFFSET+index*EDGE_BYTE_SIZE,type); // type
			edgeBuf.put(EDGE_AGE_OFFSET+index*EDGE_BYTE_SIZE,age); // age

			// save labels for later
			edgeLabels[index] = label;
			
			// store connecting nodes
			inEdgeList = nodeIn.get(node2);
			inEdgeList.add(index);
			outEdgeList = nodeOut.get(node1);
			outEdgeList.add(index);
			
		}
		br.close();
			
		
		setAllEdgeLabels(edgeLabels);


		// Initialise the connection buffer, modifying the node buffer connection data
		//time = Debugger.createTime();
		int offset = 0;
		for(int i = 0; i < numberOfNodes; i++) {
			// setting the in connection offset and length
			ArrayList<Integer> inEdges = nodeIn.get(i);
			int inEdgeLength = inEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_DEGREE_OFFSET,inEdgeLength);
		
			// now put the in edge/node pairs
			for(int e : inEdges) {
				int n = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+e*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+e*EDGE_BYTE_SIZE);
				if(n1 == i) {
					n = n2;
				} else if(n2 == i) {
					n = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+e+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,e);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,n);
				offset += CONNECTION_PAIR_SIZE;
			}
			
			// setting the out connection offset and length
			ArrayList<Integer> outEdges = nodeOut.get(i);
			int outEdgeLength = outEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_DEGREE_OFFSET,outEdgeLength);
		
			// now put the out edge/node pairs
			for(int e : outEdges) {
				int n = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+e*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+e*EDGE_BYTE_SIZE);
				if(n1 == i) {
					n = n2;
				} else if(n2 == i) {
					n = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+e+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,e);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,n);
				offset += CONNECTION_PAIR_SIZE;
			}
		}
	}


	
	/**
	 * loads the current graph from several files created by saveBuffers,
	 * in directory given to base name given (i.e. fileBaseName should have no extension).
	 * If directory is null, then to a directory named data under current working directory.
	 * 
	 * @param directory where the files are held, or if null fileBaseName under data under the current working directory
	 * @param fileBaseName the name of the files, to which extensions are added
	 * @return the created FastGraph
	 * @throws IOException If the buffers cannot be loaded
	 */
	@SuppressWarnings("resource")
	private static FastGraph loadBuffers(String directory, String fileBaseName) throws IOException {
		String directoryAndBaseName = Launcher.startingWorkingDirectory+File.separatorChar+"data"+File.separatorChar+fileBaseName+File.separatorChar+fileBaseName;
		if(directory != null) {
			if(directory.charAt(directory.length()-1)== File.separatorChar) {
				directoryAndBaseName = directory+fileBaseName;
			} else {
				directoryAndBaseName = directory+File.separatorChar+fileBaseName;
			}
		}
		
		FastGraph g = null;
		
		File file;
		FileChannel rChannel;
		String line;
		String[] splitLine;

		FileInputStream is = new FileInputStream(directoryAndBaseName+".info");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		line = br.readLine();
		splitLine = line.split(INFO_SPLIT_STRING);
		String name = splitLine[1];
		line = br.readLine();
		splitLine = line.split(INFO_SPLIT_STRING);
		int inNodeTotal = Integer.parseInt(splitLine[1]);
		line = br.readLine();
		splitLine = line.split(INFO_SPLIT_STRING);
		int inEdgeTotal = Integer.parseInt(splitLine[1]);
		line = br.readLine();
		splitLine = line.split(INFO_SPLIT_STRING);
		int inNodeLabelSize = Integer.parseInt(splitLine[1]);
		line = br.readLine();
		splitLine = line.split(INFO_SPLIT_STRING);
		int inEdgeLabelSize = Integer.parseInt(splitLine[1]);
		line = br.readLine();
		splitLine = line.split(INFO_SPLIT_STRING);
		String directValue = splitLine[1];
		boolean inDirect = true;
		if(directValue.equals("false")) {
			inDirect = false;
		}
		br.close();
		g = new FastGraph(inNodeTotal, inEdgeTotal, inDirect);
		if(!inDirect) {
			g.nodeLabelBuf = ByteBuffer.allocate(inNodeLabelSize);
			g.edgeLabelBuf = ByteBuffer.allocate(inEdgeLabelSize);
		} else {
			g.nodeLabelBuf = ByteBuffer.allocateDirect(inNodeLabelSize);
			g.edgeLabelBuf = ByteBuffer.allocateDirect(inEdgeLabelSize);
		}
		
		g.setName(name);

		file = new File(directoryAndBaseName+".nodeBuf");
		rChannel = new FileInputStream(file).getChannel();
		rChannel.read(g.nodeBuf);
		rChannel.close();

		file = new File(directoryAndBaseName+".edgeBuf");
		rChannel = new FileInputStream(file).getChannel();
		rChannel.read(g.edgeBuf);
		rChannel.close();
		
		file = new File(directoryAndBaseName+".connectionBuf");
		rChannel = new FileInputStream(file).getChannel();
		rChannel.read(g.connectionBuf);
		rChannel.close();

		file = new File(directoryAndBaseName+".nodeLabelBuf");
		rChannel = new FileInputStream(file).getChannel();
		rChannel.read(g.nodeLabelBuf);
		rChannel.close();
		
		file = new File(directoryAndBaseName+".edgeLabelBuf");
		rChannel = new FileInputStream(file).getChannel();
		rChannel.read(g.edgeLabelBuf);
		rChannel.close();

		
		return g;
	}


	/**
	 * Creates a graph with the size specified by numberOfNodes and numberOfEdges. Possibly includes parallel edges and self sourcing nodes.
	 * If the graph is simple, and there are too many edges for the nodes, an exception is thrown
	 * 
	 * @param seed the random number generator seed, -1 for current time
	 * @param simple if true then no selfsourcing edges or parallel edges
	 * @throws FastGraphException If the desired number of edges is more than a complete graph for when simple is true
	 */
	public void populateRandomGraph(boolean simple, long seed) throws FastGraphException {
		
		if(simple) {
			if((numberOfNodes*(numberOfNodes-1))/2 < numberOfEdges) {
				throw new FastGraphException("Too many edges to generate a simple graph.");
			}
		}

		//long time;
		if(seed == -1) {
			seed = System.currentTimeMillis();
		}
		Random r = new Random(seed);

		String[] nodeLabels = new String[numberOfNodes];
		String[] edgeLabels = new String[numberOfEdges];
		int inStart = -888;
		int inLength = -3;
		int outStart = -777;
		int outLength = -2;
		int weight = -5;
		byte type = -7;
		byte age = -9;
		//generate the nodes
		for(int i = 0; i < numberOfNodes; i++) {
			weight = r.nextInt(100);
			nodeBuf.putInt(NODE_IN_CONNECTION_START_OFFSET+i*NODE_BYTE_SIZE,inStart); // offset for inward connecting edges/nodes
			nodeBuf.putInt(NODE_IN_DEGREE_OFFSET+i*NODE_BYTE_SIZE,inLength); // number of inward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_CONNECTION_START_OFFSET+i*NODE_BYTE_SIZE,outStart); // offset for outward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_DEGREE_OFFSET+i*NODE_BYTE_SIZE,outLength); // number of outward connecting edges/nodes
			nodeBuf.putInt(NODE_WEIGHT_OFFSET+i*NODE_BYTE_SIZE,weight); // weight
			nodeBuf.put(NODE_TYPE_OFFSET+i*NODE_BYTE_SIZE,type); // type
			nodeBuf.put(NODE_AGE_OFFSET+i*NODE_BYTE_SIZE,age); // age
			
			// store labels for later
			String label = "n"+i;
			nodeLabels[i] = label;

		}

		setAllNodeLabels(nodeLabels);

		ArrayList<ArrayList<Integer>> nodeIn = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of inward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeIn.add(i,edges);
		}
		
		ArrayList<ArrayList<Integer>> nodeOut = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of outward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeOut.add(i,edges);
		}

		ArrayList<Integer> inEdgeList;	
		ArrayList<Integer> outEdgeList;	
		int node1;
		int node2;
		weight = -101;
		type = -103;
		age = -105;
		//generate the edges, with random node connections
		HashSet<String> nodePairs = new HashSet<String>();
		if(simple) {
			nodePairs = new HashSet<String>(numberOfEdges);
		}
		for(int i = 0; i < numberOfEdges; i++) {
			weight = r.nextInt(100);
			node1 = r.nextInt(numberOfNodes);
			node2 = r.nextInt(numberOfNodes);
			if(simple) {
				boolean parallel = false;
				String pairString1 = Integer.toString(node1)+" "+Integer.toString(node2);
				String pairString2 = Integer.toString(node2)+" "+Integer.toString(node1);
				if(nodePairs.contains(pairString1) || nodePairs.contains(pairString2) ) {
					parallel = true;
				}
				while(node2 == node1 || parallel) {
					node1 = r.nextInt(numberOfNodes);
					node2 = r.nextInt(numberOfNodes);
					pairString1 = Integer.toString(node1)+" "+Integer.toString(node2);
					pairString2 = Integer.toString(node2)+" "+Integer.toString(node1);
					if(nodePairs.contains(pairString1) || nodePairs.contains(pairString2) ) {
						parallel = true;
					} else {
						parallel = false;
					}
				}
				nodePairs.add(pairString1);
			}
			
			edgeBuf.putInt(EDGE_NODE1_OFFSET+i*EDGE_BYTE_SIZE,node1); // one end of edge
			edgeBuf.putInt(EDGE_NODE2_OFFSET+i*EDGE_BYTE_SIZE,node2); // other end of edge
			edgeBuf.putInt(EDGE_WEIGHT_OFFSET+i*EDGE_BYTE_SIZE,weight); // weight
			edgeBuf.put(EDGE_TYPE_OFFSET+i*EDGE_BYTE_SIZE,type); // type
			edgeBuf.put(EDGE_AGE_OFFSET+i*EDGE_BYTE_SIZE,age); // age
			
			// label
			String label = "e"+i;
			edgeLabels[i] = label;
			
			// store connecting nodes
			inEdgeList = nodeIn.get(node2);
			inEdgeList.add(i);
			outEdgeList = nodeOut.get(node1);
			outEdgeList.add(i);
			
		}

		setAllEdgeLabels(edgeLabels);
		
		// Initialise the connection buffer, modifying the node buffer connection data
		int offset = 0;
		for(int i = 0; i < numberOfNodes; i++) {
			// setting the in connection offset and length
			ArrayList<Integer> inEdges = nodeIn.get(i);
			int inEdgeLength = inEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_DEGREE_OFFSET,inEdgeLength);
		
			// now put the in edge/node pairs
			for(int e : inEdges) {
				int n = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+e*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+e*EDGE_BYTE_SIZE);
				if(n1 == i) {
					n = n2;
				} else if(n2 == i) {
					n = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+e+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,e);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,n);
				offset += CONNECTION_PAIR_SIZE;
			}
			
			// setting the out connection offset and length
			ArrayList<Integer> outEdges = nodeOut.get(i);
			int outEdgeLength = outEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_DEGREE_OFFSET,outEdgeLength);
		
			// now put the out edge/node pairs
			for(int e : outEdges) {
				int n = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+e*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+e*EDGE_BYTE_SIZE);
				if(n1 == i) {
					n = n2;
				} else if(n2 == i) {
					n = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+e+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,e);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,n);
				offset += CONNECTION_PAIR_SIZE;
			}
			
		
		}
		//Debugger.outputTime("connection put time, direct "+edgeBuf.isDirect());

	}
	
	
	
	/**
	 * Creates a graph from a displayGraph.Graph. label becomes the displayGraph.Graph name
	 * node and edge labels, are taken from displayGraph.Graph nodes and edges.
	 * node and edge weights are from displayGraph node and edge scores. Types are
	 * from displayGraph edgeType edgeType if they can be parsed as bytes,
	 * otherwise they get a default of -1. Node and edge Age is from displayGraph age, but
	 * only least significant byte, as the displayGraph age is a integer.
	 * Order of nodes and edges is as in the displayGraph.Graph
	 * 
	 * @param displayGraph the graph that the new FastGraph is based on
	 * @param direct if true then off heap ByteBuffers, if false then on heap ByteBuffers
	 * @return new FastGraph with attributes based on the given displayGraph.
	 */
	public static FastGraph displayGraphFactory(Graph displayGraph, boolean direct) {
		FastGraph g = new FastGraph(displayGraph.getNodes().size(),displayGraph.getEdges().size(),direct);
		g.setName(displayGraph.getLabel());
		g.populateFromDisplayGraph(displayGraph);
		return g;
	}


	
	/**
	 * Populates byteBuffers based on the contents of the displayGraph.graph.
	 * Nodes and edges are in the order they appear in the displayGraph.
	 * 
	 * @param displayGraph the graph that the new FastGraph is based on
	 */
	private void populateFromDisplayGraph(Graph displayGraph) {

		String[] nodeLabels = new String[numberOfNodes];
		String[] edgeLabels = new String[numberOfEdges];
		int inStart = -27;
		int inLength = -37;
		int outStart = -47;
		int outLength = -57;
		int weight = -67;
		byte type = -77;
		byte age = -87;
		ByteBuffer bb = ByteBuffer.allocate(4); // used to convert from int to byte, due to lack of direct casting
		// nodes first, will be in the same order as the list in the displayGraph
		for(int i = 0; i < numberOfNodes; i++) {
			Node dgn = displayGraph.getNodes().get(i);
			weight = (int)(dgn.getScore());
			bb.putInt(0,dgn.getAge());
			age = bb.get(3); // get least significant byte of age
			try {
				type = Byte.parseByte(dgn.getType().getLabel());
			} catch(NumberFormatException e) {
				type = -1;
			}
			
			nodeBuf.putInt(NODE_IN_CONNECTION_START_OFFSET+i*NODE_BYTE_SIZE,inStart); // offset for inward connecting edges/nodes
			nodeBuf.putInt(NODE_IN_DEGREE_OFFSET+i*NODE_BYTE_SIZE,inLength); // number of inward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_CONNECTION_START_OFFSET+i*NODE_BYTE_SIZE,outStart); // offset for outward connecting edges/nodes
			nodeBuf.putInt(NODE_OUT_DEGREE_OFFSET+i*NODE_BYTE_SIZE,outLength); // number of outward connecting edges/nodes
			nodeBuf.putInt(NODE_WEIGHT_OFFSET+i*NODE_BYTE_SIZE,weight); // weight
			nodeBuf.put(NODE_TYPE_OFFSET+i*NODE_BYTE_SIZE,type); // type
			nodeBuf.put(NODE_AGE_OFFSET+i*NODE_BYTE_SIZE,age); // age

			// store labels for later
			String label = dgn.getLabel();
			nodeLabels[i] = label;

		}

		setAllNodeLabels(nodeLabels);

		ArrayList<ArrayList<Integer>> nodeIn = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of inward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeIn.add(i,edges);
		}
		
		ArrayList<ArrayList<Integer>> nodeOut = new ArrayList<ArrayList<Integer>>(numberOfNodes); // temporary store of outward edges
		for(int i = 0; i < numberOfNodes; i++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeOut.add(i,edges);
		}
				
		ArrayList<Integer> inEdgeList;	
		ArrayList<Integer> outEdgeList;	
		int node1;
		int node2;
		weight = -15;
		type = -25;
		age = -35;
		// edges once nodes exist, will be in the same order as the list in the displayGraph
		for(int i = 0; i < numberOfEdges; i++) {
			Edge dge = displayGraph.getEdges().get(i);
			node1 = displayGraph.getNodes().indexOf(dge.getFrom()); // we can find the FastGraph node index from its position in the displayGraph nodeList
			node2 = displayGraph.getNodes().indexOf(dge.getTo()); // we can find the FastGraph node index from its position in the displayGraph nodeList
			weight = (int)(dge.getScore());
			bb.putInt(0,dge.getAge());
			age = bb.get(3); // get least significant byte of age
			try {
				type = Byte.parseByte(dge.getType().getLabel());
			} catch(NumberFormatException e) {
				type = -1;
			}
			
			edgeBuf.putInt(EDGE_NODE1_OFFSET+i*EDGE_BYTE_SIZE,node1); // one end of edge
			edgeBuf.putInt(EDGE_NODE2_OFFSET+i*EDGE_BYTE_SIZE,node2); // other end of edge
			edgeBuf.putInt(EDGE_WEIGHT_OFFSET+i*EDGE_BYTE_SIZE,weight); // weight
			edgeBuf.put(EDGE_TYPE_OFFSET+i*EDGE_BYTE_SIZE,type); // type
			edgeBuf.put(EDGE_AGE_OFFSET+i*EDGE_BYTE_SIZE,age); // age
			
			// store labels for later
			String label = dge.getLabel();
			edgeLabels[i] = label;
			
			// store connecting nodes
			inEdgeList = nodeIn.get(node2);
			inEdgeList.add(i);
			outEdgeList = nodeOut.get(node1);
			outEdgeList.add(i);

		}
		
		setAllEdgeLabels(edgeLabels);

		// Initialise the connection buffer, modifying the node buffer connection data
		//time = Debugger.createTime();
		int offset = 0;
		for(int i = 0; i < numberOfNodes; i++) {
			// setting the in connection offset and length
			ArrayList<Integer> inEdges = nodeIn.get(i);
			int inEdgeLength = inEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_IN_DEGREE_OFFSET,inEdgeLength);
		
			// now put the in edge/node pairs
			for(int edgeIndex : inEdges) {
				int nodeIndex = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				if(n1 == i) {
					nodeIndex = n2;
				} else if(n2 == i) {
					nodeIndex = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+edgeIndex+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,edgeIndex);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,nodeIndex);
				offset += CONNECTION_PAIR_SIZE;
			}
			
			// setting the out connection offset and length
			ArrayList<Integer> outEdges = nodeOut.get(i);
			int outEdgeLength = outEdges.size();
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_CONNECTION_START_OFFSET,offset);
			nodeBuf.putInt(i*NODE_BYTE_SIZE+NODE_OUT_DEGREE_OFFSET,outEdgeLength);
		
			// now put the out edge/node pairs
			for(int edgeIndex : outEdges) {
				int nodeIndex = -1;
				int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				if(n1 == i) {
					nodeIndex = n2;
				} else if(n2 == i) {
					nodeIndex = n1;
				} else {
					Debugger.log("ERROR When finding connections for node "+i+" connecting edge "+edgeIndex+ " has connecting nodes "+n1+" "+n2);
				}
				connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,edgeIndex);
				connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,nodeIndex);
				offset += CONNECTION_PAIR_SIZE;
			}
		}
	}
	
	
	

	/**
	 * Generates a new graph from the subgraph specified by the parameters. All
	 * edges connected to deleted nodes are also removed.
	 *
	 * 
	 * @param nodesToDelete nodes in this graph that will not appear in the new graph
	 * @param edgesToDelete edges in this graph that will not appear in the new graph
	 * @param orphanEdgeCheckNeeded If the method calling this has already checked for orphan nodes, then false.
	 * @return the new FastGraph
	 */
	public FastGraph generateGraphByDeletingItems(int[] nodesToDelete, int[] edgesToDelete, boolean orphanEdgeCheckNeeded) {
		
		Debugger.log("Nodes to remove: " + Arrays.toString(nodesToDelete));
		
		long time = Debugger.createTime();

		LinkedList<Integer> allEdgesToDeleteList = new LinkedList<Integer>();
		LinkedList<Integer> allNodesToDeleteList = new LinkedList<Integer>();
		
		Debugger.outputTime("Z setup ");
		time = Debugger.createTime();
		
		for(int e : edgesToDelete) {
			allEdgesToDeleteList.add(e);
		}
		
		// delete the edges connecting to deleted nodes and create the node list
		for(int n : nodesToDelete) {
			allNodesToDeleteList.add(n);
			if(orphanEdgeCheckNeeded) { //only check for orphan nodes if needed
				int[] connectingEdges = getNodeConnectingEdges(n);
				for(int e : connectingEdges) {
					if(!allEdgesToDeleteList.contains(e)) {
						allEdgesToDeleteList.add(e);
					}
				}
			}			
		}
Debugger.outputTime("A Created the node and edge delete lists ");
time = Debugger.createTime();
		
		// find the nodes that will remain
		HashSet<Integer> remainingNodeList = new HashSet<Integer>(allNodesToDeleteList.size()*3);
		for(int i = 0; i < getNumberOfNodes(); i++) {
			remainingNodeList.add(i);
		}
		remainingNodeList.removeAll(allNodesToDeleteList); //this is quicker than checking each entry
		
		Debugger.outputTime("AAA created the node remain lists ");
		time = Debugger.createTime();
		// turn it into an array
		int[] remainingNodes = Util.convertHashSet(remainingNodeList);
		
		Debugger.outputTime("AA converted the node remain lists ");
		time = Debugger.createTime();

		// find the edges that will remain
		HashSet<Integer> remainingEdgeList = new HashSet<Integer>(allEdgesToDeleteList.size()*3);
		for(int i = 0; i < getNumberOfEdges(); i++) {
			remainingEdgeList.add(i);
		}
		remainingEdgeList.removeAll(allEdgesToDeleteList);
		
		Debugger.outputTime("AB Created the edge remain lists ");
		time = Debugger.createTime();
		// turn it into an array
		int[] remainingEdges = Util.convertHashSet(remainingEdgeList);

Debugger.outputTime("B converted the edge remain lists ");
time = Debugger.createTime();

		FastGraph g = generateGraphFromSubgraph(remainingNodes,remainingEdges);
		
		return g;
	}

	/**
	 * Generates a new graph from the subgraph specified by the parameters. The nodes at the end of the edges must be in subgraphEdges.
	 * 
	 * @param subgraphNodes nodes in this graph that will appear in the new graph
	 * @param subgraphEdges edges in this graph that will appear in the new graph, must connect only to subgraphNodes
	 * @return the new FastGraph
	 */
	public FastGraph generateGraphFromSubgraph(int[] subgraphNodes, int[] subgraphEdges) {
		
		long time = Debugger.createTime();

		FastGraph g = new FastGraph(subgraphNodes.length, subgraphEdges.length, getDirect());
		
		String[] nodeLabels = new String[subgraphNodes.length]; // stores the labels for creating the nodeLabelBuffer
		HashMap<Integer,Integer> oldNodesToNew = new HashMap<>(subgraphNodes.length*4); // for reference when adding edges, multiplier reduces chances of clashes
		// initial population of the new node array
		int weight = -98;
		byte type = -97;
		byte age = -96;
		int index = 0;
		for(int n : subgraphNodes) {

			weight = nodeBuf.getInt(NODE_WEIGHT_OFFSET+n*NODE_BYTE_SIZE);
			type = nodeBuf.get(NODE_TYPE_OFFSET+n*NODE_BYTE_SIZE);
			age = nodeBuf.get(NODE_AGE_OFFSET+n*NODE_BYTE_SIZE);

			g.nodeBuf.putInt(NODE_WEIGHT_OFFSET+index*NODE_BYTE_SIZE,weight);
			g.nodeBuf.put(NODE_TYPE_OFFSET+index*NODE_BYTE_SIZE,type);
			g.nodeBuf.put(NODE_AGE_OFFSET+index*NODE_BYTE_SIZE,age);
			
			// store labels for later
			nodeLabels[index] = getNodeLabel(n);
			// store old to new mapping for later
			oldNodesToNew.put(n, index);
			index++;
		}
//Debugger.outputTime("C popluated the new node buffer ");
time = Debugger.createTime();
		
		g.setAllNodeLabels(nodeLabels); // create the node label buffer
//Debugger.outputTime("D popluated the new node list buffer ");
time = Debugger.createTime();
		
		ArrayList<ArrayList<Integer>> nodeIn = new ArrayList<ArrayList<Integer>>(subgraphNodes.length); // temporary store of inward edges
		for(int nodeIndex = 0; nodeIndex < subgraphNodes.length; nodeIndex++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeIn.add(nodeIndex,edges);
		}
		
		ArrayList<ArrayList<Integer>> nodeOut = new ArrayList<ArrayList<Integer>>(subgraphNodes.length); // temporary store of outward edges
		for(int nodeIndex = 0; nodeIndex < subgraphNodes.length; nodeIndex++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeOut.add(nodeIndex,edges);
		}
//Debugger.outputTime("E created the neighbour store ");
time = Debugger.createTime();
		
//Debugger.log(oldNodesToNew);


		String[] edgeLabels = new String[subgraphEdges.length]; // stores the labels for creating the edgeLabelBuffer
		ArrayList<Integer> inEdgeList;	
		ArrayList<Integer> outEdgeList;	
		// create the edges
		index = 0;
		edgeBuf.position(0);
		g.edgeBuf.position(0);
		for(int e : subgraphEdges) {
			
			weight = edgeBuf.getInt(EDGE_WEIGHT_OFFSET+e*EDGE_BYTE_SIZE);
			type = edgeBuf.get(EDGE_TYPE_OFFSET+e*EDGE_BYTE_SIZE);
			age = edgeBuf.get(EDGE_AGE_OFFSET+e*EDGE_BYTE_SIZE);

			g.edgeBuf.putInt(EDGE_WEIGHT_OFFSET+index*EDGE_BYTE_SIZE,weight);
			g.edgeBuf.put(EDGE_TYPE_OFFSET+index*EDGE_BYTE_SIZE,type);
			g.edgeBuf.put(EDGE_AGE_OFFSET+index*EDGE_BYTE_SIZE,age);
			
			int n1 = edgeBuf.getInt(EDGE_NODE1_OFFSET+e*EDGE_BYTE_SIZE);
			int n2 = edgeBuf.getInt(EDGE_NODE2_OFFSET+e*EDGE_BYTE_SIZE);
			
			//System.out.print("old node n1: " + n1);
			
			int gn1 = oldNodesToNew.get(n1);
			//System.out.print(", new node n1: " + gn1);
			//System.out.print(", old node n2: " + n2);
			int gn2 = oldNodesToNew.get(n2);
			//System.out.print(", new node n2: " + gn2);
			//Debugger.log();
			
			g.edgeBuf.putInt(EDGE_NODE1_OFFSET+index*EDGE_BYTE_SIZE,gn1); // one end of edge
			g.edgeBuf.putInt(EDGE_NODE2_OFFSET+index*EDGE_BYTE_SIZE,gn2); // other end of edge
			
			// store labels for later
			edgeLabels[index] = getEdgeLabel(e);
			
			// store connecting edges
			inEdgeList = nodeIn.get(gn2);
			inEdgeList.add(index);
			outEdgeList = nodeOut.get(gn1);
			outEdgeList.add(index);
			index++;
		}
//Debugger.outputTime("F populated the new edge buffer ");
time = Debugger.createTime();

		g.setAllEdgeLabels(edgeLabels);
//Debugger.outputTime("G populated the new edge label buffer ");
time = Debugger.createTime();
		
		// Initialise the connection buffer, modifying the node buffer connection data
		//time = Debugger.createTime();
		int offset = 0;
		for(int node = 0; node < subgraphNodes.length; node++) {
if(node%100000 == 0) {
	//Debugger.outputTime("H populated "+node+" nodes in connection buffer ");
	time = Debugger.createTime();
}
			
			// setting the in connection offset and length
			ArrayList<Integer> inEdges = nodeIn.get(node);
			int inEdgeLength = inEdges.size();
			g.nodeBuf.putInt(node*NODE_BYTE_SIZE+NODE_IN_CONNECTION_START_OFFSET,offset);
			g.nodeBuf.putInt(node*NODE_BYTE_SIZE+NODE_IN_DEGREE_OFFSET,inEdgeLength);
			
			// now put the in edge/node pairs
			for(int edgeIndex : inEdges) {
				int nodeIndex = -1;
				int n1 = g.edgeBuf.getInt(EDGE_NODE1_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				int n2 = g.edgeBuf.getInt(EDGE_NODE2_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				if(n1 == node) {
					nodeIndex = n2;
				} else if(n2 == node) {
					nodeIndex = n1;
				} else {
					Debugger.log("ERROR A When finding connections for node "+node+" connecting edge "+edgeIndex+ " has connecting nodes "+n1+" "+n2);
				}
				g.connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,edgeIndex);
				g.connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,nodeIndex);
				offset += CONNECTION_PAIR_SIZE;
			}
			
			// setting the out connection offset and length
			ArrayList<Integer> outEdges = nodeOut.get(node);
			int outEdgeLength = outEdges.size();
			g.nodeBuf.putInt(node*NODE_BYTE_SIZE+NODE_OUT_CONNECTION_START_OFFSET,offset);
			g.nodeBuf.putInt(node*NODE_BYTE_SIZE+NODE_OUT_DEGREE_OFFSET,outEdgeLength);
			
			// now put the out edge/node pairs
			for(int edgeIndex : outEdges) {
				int nodeIndex = -1;
				int n1 = g.edgeBuf.getInt(EDGE_NODE1_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				int n2 = g.edgeBuf.getInt(EDGE_NODE2_OFFSET+edgeIndex*EDGE_BYTE_SIZE);

				if(n1 == node) {
					nodeIndex = n2;
				} else if(n2 == node) {
					nodeIndex = n1;
				} else {
					Debugger.log("ERROR B When finding connections for node "+node+" connecting edge "+edgeIndex+ " has connecting nodes "+n1+" "+n2);
				}
				g.connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,edgeIndex);
				g.connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,nodeIndex);
				offset += CONNECTION_PAIR_SIZE;
			}
		}
		
		return g;
	}


	/**
	 * Generates a new graph from the changes in edge wiring specified.
	 * 
	 * @param rewireEdges list of 3 element arrays, rewireEdges[0] is the edge, rewireEdges[1] is the new node1, rewireEdges[2] is the new node2
	 * @return the new FastGraph
	 */
	public FastGraph generateRewiredGraph(List<int[]> rewireEdges) {
		
//Debugger.resetTime();

		FastGraph g = new FastGraph(getNumberOfNodes(), getNumberOfEdges(), getDirect());
		
		g.nodeBuf = Util.cloneByteBuffer(nodeBuf);
		g.nodeLabelBuf = Util.cloneByteBuffer(nodeLabelBuf);
		g.edgeBuf = Util.cloneByteBuffer(edgeBuf);
		g.edgeLabelBuf = Util.cloneByteBuffer(edgeLabelBuf);

		ArrayList<ArrayList<Integer>> nodeIn = new ArrayList<ArrayList<Integer>>(g.getNumberOfNodes()); // temporary store of inward edges
		for(int nodeIndex = 0; nodeIndex < g.getNumberOfNodes(); nodeIndex++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeIn.add(nodeIndex,edges);
		}
		
		ArrayList<ArrayList<Integer>> nodeOut = new ArrayList<ArrayList<Integer>>(g.getNumberOfNodes()); // temporary store of outward edges
		for(int nodeIndex = 0; nodeIndex < g.getNumberOfNodes(); nodeIndex++) {
			ArrayList<Integer> edges = new ArrayList<Integer>(100);
			nodeOut.add(nodeIndex,edges);
		}

		ArrayList<Integer> inEdgeList;	
		ArrayList<Integer> outEdgeList;	
		
		// create the edges
		edgeBuf.position(0);
		g.edgeBuf.position(0);

		for(int[] eArray : rewireEdges) {
			
			int e = eArray[0];
			
			int gn1 = eArray[1];
			int gn2 = eArray[2];
			
			
			g.edgeBuf.putInt(EDGE_NODE1_OFFSET+e*EDGE_BYTE_SIZE,gn1); // one end of edge
			g.edgeBuf.putInt(EDGE_NODE2_OFFSET+e*EDGE_BYTE_SIZE,gn2); // other end of edge
		}
//Debugger.outputTime("A Changed the edge connections");
			
		// the edge connections have been changed, now change the connection lists
		for(int e = 0; e < g.numberOfEdges; e++) {
			
			int gn1 = g.getEdgeNode1(e);
			int gn2 = g.getEdgeNode2(e);
			
			// store connecting edges
			inEdgeList = nodeIn.get(gn2);
			inEdgeList.add(e);
			outEdgeList = nodeOut.get(gn1);
			outEdgeList.add(e);
		}
		
//for(int i = 0; i < g.numberOfNodes; i++) {
//	Debugger.log("node "+i+" in "+nodeIn.get(i)+" out "+nodeOut.get(i));
//}
		
		// Initialise the connection buffer, modifying the node buffer connection data
		//time = Debugger.createTime();
		int offset = 0;
		for(int node = 0; node < g.getNumberOfNodes(); node++) {
//if(node%100000 == 0) {
//	Debugger.outputTime("B populated "+node+" nodes in connection buffer");
//}
			
			// setting the in connection offset and length
			ArrayList<Integer> inEdges = nodeIn.get(node);
			int inEdgeLength = inEdges.size();
			g.nodeBuf.putInt(node*NODE_BYTE_SIZE+NODE_IN_CONNECTION_START_OFFSET,offset);
			g.nodeBuf.putInt(node*NODE_BYTE_SIZE+NODE_IN_DEGREE_OFFSET,inEdgeLength);
			
			// now put the in edge/node pairs
			for(int edgeIndex : inEdges) {
				int nodeIndex = -1;
				int n1 = g.edgeBuf.getInt(EDGE_NODE1_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				int n2 = g.edgeBuf.getInt(EDGE_NODE2_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				if(n1 == node) {
					nodeIndex = n2;
				} else if(n2 == node) {
					nodeIndex = n1;
				} else {
					Debugger.log("ERROR A When finding connections for node "+node+" connecting edge "+edgeIndex+ " has connecting nodes "+n1+" "+n2);
				}
				g.connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,edgeIndex);
				g.connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,nodeIndex);
				offset += CONNECTION_PAIR_SIZE;
			}
			// setting the out connection offset and length
			ArrayList<Integer> outEdges = nodeOut.get(node);
			int outEdgeLength = outEdges.size();
			g.nodeBuf.putInt(node*NODE_BYTE_SIZE+NODE_OUT_CONNECTION_START_OFFSET,offset);
			g.nodeBuf.putInt(node*NODE_BYTE_SIZE+NODE_OUT_DEGREE_OFFSET,outEdgeLength);
			
			// now put the out edge/node pairs
			for(int edgeIndex : outEdges) {
				int nodeIndex = -1;
				int n1 = g.edgeBuf.getInt(EDGE_NODE1_OFFSET+edgeIndex*EDGE_BYTE_SIZE);
				int n2 = g.edgeBuf.getInt(EDGE_NODE2_OFFSET+edgeIndex*EDGE_BYTE_SIZE);

				if(n1 == node) {
					nodeIndex = n2;
				} else if(n2 == node) {
					nodeIndex = n1;
				} else {
					Debugger.log("ERROR B When finding connections for node "+node+" connecting edge "+edgeIndex+ " has connecting nodes "+n1+" "+n2);
				}
				g.connectionBuf.putInt(CONNECTION_EDGE_OFFSET+offset,edgeIndex);
				g.connectionBuf.putInt(CONNECTION_NODE_OFFSET+offset,nodeIndex);
				offset += CONNECTION_PAIR_SIZE;
			}
		}
//Debugger.outputTime("C finished");
		
		return g;
	}
	

	/**
	 * Create an integer array, length of maximum degree in the graph. Each element arr[i] of the array contains a count of the number
	 * of nodes with degree i.
	 * 		
	 * @return the degree profile of the graph. each index contains the number of nodes with that degree
	 */
	public int[] degreeProfile() {
		int maxDegree = maximumDegree();
		
		if(getNumberOfNodes() == 0) {
			return new int[0];
		}
		
		int[] ret = new int[maxDegree+1];
		Arrays.fill(ret, 0);
		for(int i = 0; i < getNumberOfNodes(); i++) {
			int degree = getNodeDegree(i);
			ret[degree]++;
		}
		
		return ret;
	}


	/**
	 * Create an integer array, length of maximum in degree in the graph. Each element arr[i] of the array contains a count of the number
	 * of nodes with in degree i.
	 * 		
	 * @return the in degree profile of the graph. each index contains the number of nodes with that in degree
	 */
	public int[] inDegreeProfile() {
		int maxDegree = maximumInDegree();
		
		if(getNumberOfNodes() == 0) {
			return new int[0];
		}
		
		int[] ret = new int[maxDegree+1];
		Arrays.fill(ret, 0);
		for(int i = 0; i < getNumberOfNodes(); i++) {
			int degree = getNodeInDegree(i);
			ret[degree]++;
		}
		
		return ret;
	}


	
	/**
	 * Create an integer array, length of maximum in degree in the graph. Each element arr[i] of the array contains a count of the number
	 * of nodes with in degree i.
	 * 		
	 * @return the in degree profile of the graph. each index contains the number of nodes with that in degree
	 */
	public int[] outDegreeProfile() {
		int maxDegree = maximumOutDegree();
		
		if(getNumberOfNodes() == 0) {
			return new int[0];
		}
		
		int[] ret = new int[maxDegree+1];
		Arrays.fill(ret, 0);
		for(int i = 0; i < getNumberOfNodes(); i++) {
			int degree = getNodeOutDegree(i);
			ret[degree]++;
		}
		
		return ret;
	}


	
	/**
	 * @return the largest degree for a node in the graph.
	 */
	public int maximumDegree() {
		
		int max = 0;
		for(int i = 0; i < numberOfNodes; i++) {
			int inDegree = nodeBuf.getInt(NODE_IN_DEGREE_OFFSET+i*NODE_BYTE_SIZE);
			int outDegree = nodeBuf.getInt(NODE_OUT_DEGREE_OFFSET+i*NODE_BYTE_SIZE);
			int degree = inDegree+outDegree;
			if(degree > max) {
				max = degree;
			}
		}
		return max;
	}
	
	
	/**
	 * @return the largest degree for a node in the graph.
	 */
	public int maximumInDegree() {
		
		int max = 0;
		for(int i = 0; i < numberOfNodes; i++) {
			int inDegree = nodeBuf.getInt(NODE_IN_DEGREE_OFFSET+i*NODE_BYTE_SIZE);
			if(inDegree > max) {
				max = inDegree;
			}
		}
		return max;
	}
	
	
	/**
	 * @return the largest degree for a node in the graph.
	 */
	public int maximumOutDegree() {
		
		int max = 0;
		for(int i = 0; i < numberOfNodes; i++) {
			int outDegree = nodeBuf.getInt(NODE_OUT_DEGREE_OFFSET+i*NODE_BYTE_SIZE);
			if(outDegree > max) {
				max = outDegree;
			}
		}
		return max;
	}
	

	/**
	 * Creates a displayGraph.Graph which can then be accessed, manipulated and visualized
	 * using that package. displayGraph.Graph name becomes this FastGraph label
	 * The displayGraph.Graph node and edge labels, are taken
	 * from this FastGraph nodes and edges. node and edge weights become node
	 * and edge scores node and edge ages become ages in displayGraph nodes and edges.
	 * New NodeType and EdgeType are created if needed with label of the integer of this type.
	 * Order of nodes and edges in the displayGraph.Graph is as this FastGraph.
	 * 
	 * @return a displayGraph.Graph with the same data as this Fast Graph
	 */
	public Graph generateDisplayGraph() {
		
		Graph g = new Graph(getName());
		
		for(int i = 0; i < numberOfNodes; i++) {
			Node n = new Node();
			n.setLabel(getNodeLabel(i));
			n.setScore(getNodeWeight(i));
			n.setAge(getNodeAge(i));
			String typeLabel = Integer.toString(getNodeType(i));
			NodeType type = NodeType.withLabel(typeLabel);
			if(type == null) {
				type = new NodeType(typeLabel);
			}
			n.setType(type);
			g.addNode(n);
		}
		
		for(int i = 0; i < numberOfEdges; i++) {
			Node n1 = g.getNodes().get(getEdgeNode1(i));
			Node n2 = g.getNodes().get(getEdgeNode2(i));
			Edge e = new Edge(n1,n2);
			e.setLabel(getEdgeLabel(i));
			e.setScore(getEdgeWeight(i));
			e.setAge(getEdgeAge(i));
			String typeLabel = Integer.toString(getEdgeType(i));
			EdgeType type = EdgeType.withLabel(typeLabel);
			if(type == null) {
				type = new EdgeType(typeLabel);
			}
			e.setType(type);
			g.addEdge(e);
		}
		
		return g;
	}	
	
	/**
	 * Counts the number of instances of nodes with various degrees.
	 * 
	 * @param maxDegrees The maximum number of degrees to look for. If given 3, will count all nodes with degrees 0,1,2.
	 * @return The list of number of nodes at each degree.
	 */
	public int[] countInstancesOfNodeDegrees(int maxDegrees) {
		int[] res = new int[maxDegrees];
		
		for(int n = 0; n < getNumberOfNodes(); n++) {
			int deg = getNodeDegree(n);
			if (deg < maxDegrees) {
				//System.out.print(deg + " ");
				//Debugger.log(res[deg]);
				res[deg]++;
			}
		}		
		return res;
	}
	
	
	/**
	 * Check the consistency of a graph. Checks: <ul>
	 * <li> If edges link to node indexes outside of the current range</li>
	 * <li> If all edges are reflected in the connection lists</li>
	 * <li> If the connection list data points to the correct edges</li>
	 * <li> If the nodes and edges in the connection list are correct</li>
	 * </ul>
	 * 
	 * @return true if the graph is consistent, false otherwise
	 */
	public boolean checkConsistency() {

		// consistency of edges
		for(int e = 0; e < getNumberOfEdges(); e++) {
			int node1 = getEdgeNode1(e);
			int node2 = getEdgeNode2(e);
			if(node1 < 0 || node1 >= getNumberOfNodes()) {
				Debugger.log("INCONSISTENT. Edge "+e+" has node1 "+node1+ " but there are only "+getNumberOfNodes()+" nodes");
				return false;
			}
			if(node2 < 0 || node2 >= getNumberOfNodes()) {
				Debugger.log("INCONSISTENT. Edge "+e+" has node2 "+node2+ " but there are only "+getNumberOfNodes()+" nodes");
				return false;
			}
			if(!Util.convertArray(getNodeConnectingOutEdges(node1)).contains(e)) {
				Debugger.log("INCONSISTENT. Edge "+e+" has node1 "+node1+ " but it is not in the node out list");
				return false;
			}
			if(!Util.convertArray(getNodeConnectingInEdges(node2)).contains(e)) {
				Debugger.log("INCONSISTENT. Edge "+e+" has node2 "+node2+ " but it is not in the node in list");
				return false;
			}
		}
		
		// consistency of nodes and connection lists
		for(int n = 0; n < getNumberOfNodes(); n++) {
			if(getNodeConnectingOutEdges(n).length != getNodeConnectingOutNodes(n).length) {
				Debugger.log("INCONSISTENT. Node "+n+" has different number of out edges to out nodes");
				return false;
			}
			if(getNodeConnectingInEdges(n).length != getNodeConnectingInNodes(n).length) {
				Debugger.log("INCONSISTENT. Node "+n+" has different number of in edges to in nodes");
				return false;
			}
			for(int i = 0; i < getNodeConnectingOutEdges(n).length; i++) {
				int connectingEdge = getNodeConnectingOutEdges(n)[i];
				int otherEnd = oppositeEnd(connectingEdge, n);
				int connectingNode = getNodeConnectingOutNodes(n)[i];
				if(otherEnd != connectingNode) {
					Debugger.log("INCONSISTENT. Node "+n+" has inconsitent edge and node in connecting out list");
					return false;
				}
				if(n != oppositeEnd(connectingEdge, otherEnd)) {
					Debugger.log("INCONSISTENT. Node "+n+" has edge in connecting  out list that does not point to the node");
					return false;
				}
			}
				
				for(int i = 0; i < getNodeConnectingInEdges(n).length; i++) {
					int connectingEdge = getNodeConnectingInEdges(n)[i];
					int otherEnd = oppositeEnd(connectingEdge, n);
					int connectingNode = getNodeConnectingInNodes(n)[i];
					if(otherEnd != connectingNode) {
						Debugger.log("INCONSISTENT. Node "+n+" has inconsitent edge and node in connecting in list");
					return false;
				}
				if(n != oppositeEnd(connectingEdge, otherEnd)) {
					Debugger.log("INCONSISTENT. Node "+n+" has edge in connecting in list that does not point to the node");
					return false;
				}

			}
		}
		
		return true;
	}



	
	/**
	 * This generates a new random graph based on the existing graph, but with rewired edges. The node degrees are maintained.
	 * It performs multiple rewrites 
	 * 
	 * @param iterations the number of times to rewire, more means better chance of a truly random graph
	 * @param seed random number generator seed
	 * @return a new graph, based on g, but rewired graph
	 */
	public FastGraph generateRandomRewiredGraph(int iterations, long seed) {
		
		String name = this.getName();
		long time = Debugger.createTime();
		long theSeed = seed;
		FastGraph g = this;
		for(int i = 0; i< iterations; i++) {
			Debugger.log("        iteration number: " + i);
			FastGraph h = g.oneIterationGenerateRandomRewiredGraph(theSeed);
			if(h == null) {
				return null;
			} else {
				h.setName(name+"-"+i);
			}
			g = h;
			theSeed += g.getNumberOfEdges()*10;
Debugger.outputTime("time for rewiring");
//h.saveBuffers(null, g.getName());
		}
		
		return g;
	}
	
	
	
	
	/**
	 * This generates a new random graph based on the existing graph, but with rewired edges. The node degrees are maintained.
	 * 
	 * 
	 * @param seed random number generator seed
	 * @return a new graph, based on g, but rewired graph
	 */
	private FastGraph oneIterationGenerateRandomRewiredGraph(long seed) {
			
		final int ITERATIONS_TIME_OUT = 10000;

		Random r = new Random(seed);
		
		ArrayList<Integer> remainingNodes = new ArrayList<Integer>(getNumberOfNodes()*3);
		for(Integer i = 0; i < getNumberOfNodes(); i++) {
			remainingNodes.add(i);
		}
		
		int startNode = -1;
		int startEdge = -1;
		
		boolean foundStart = false;
		int count = 0;
		while(!foundStart) {
			count++;
			if(count > ITERATIONS_TIME_OUT) {
				// graph is too sparse for rewiring
				return null;
			}
			// need at least 2 in edges, 1 out edge
			// leaving 1 out edge and 1 in edge to finish the rewiring with
			int testNode = r.nextInt(getNumberOfNodes());
			if(getNodeInDegree(testNode) > 1 && getNodeOutDegree(testNode) > 0) {
				startNode = testNode;
				startEdge = getNodeConnectingInEdges(startNode)[0];
				foundStart = true;
			}
		}

		remainingNodes.remove(startNode);
		
		LinkedList<int[]> rewireEdges = new LinkedList<int[]>();
		int[] rewire = new int[3];
		int nextNode = oppositeEnd(startEdge, startNode);
		rewire[0] = startEdge;
		rewire[1] = nextNode;
		rewire[2] = startNode;
		rewireEdges.add(rewire);
//Debugger.log("first rewire "+rewire[0]+" "+rewire[1]+" "+rewire[2]);
		
		boolean in = false;

		boolean loop = true;
		while(loop) {
			rewire = new int[3];
			
			if(in) {
				// next is an in edge
				count = 0;
				boolean foundEdge = false;
				while(!foundEdge) {
					count++;
					if(count > ITERATIONS_TIME_OUT) { // end here and attach to start node
						rewire[1] = nextNode;
						rewire[2] = startNode;
						int[] connectingEdges = getNodeConnectingInEdges(startNode);
					
						int pos = r.nextInt(connectingEdges.length);
						int edge = connectingEdges[pos];
						while(edge == startEdge) {
							pos = r.nextInt(connectingEdges.length);
							edge = connectingEdges[pos];
						}
						rewire[0] = edge;
						foundEdge = true;
						loop = false;
//Debugger.log("in finish on no more nodes "+rewire[0]+" "+rewire[1]+" "+rewire[2]);
					}
					
					int testInd = r.nextInt(remainingNodes.size());
					int testNode = remainingNodes.get(testInd);
					if(getNodeInDegree(testNode) > 0) { // found a suitable node
						rewire[1] = nextNode;
						rewire[2] = testNode;
						int[] connectingEdges = getNodeConnectingInEdges(testNode);
						int pos = r.nextInt(connectingEdges.length);
						int edge = connectingEdges[pos];
						rewire[0] = edge;
						nextNode = testNode;
						remainingNodes.remove(testInd);
						in = false;
						foundEdge = true;
//Debugger.log("in rewire "+rewire[0]+" "+rewire[1]+" "+rewire[2]+" remaining "+remainingNodes.size());
					}
				}
			} else {
				// next is an out edge
				count = 0;
				boolean foundEdge = false;
				while(!foundEdge) {
					count++;
					if(count > ITERATIONS_TIME_OUT) { // end here and attach to start node
						rewire[1] = startNode;
						rewire[2] = nextNode;
						int[] connectingEdges = getNodeConnectingOutEdges(startNode);
						int pos = r.nextInt(connectingEdges.length);
						int edge = connectingEdges[pos];
						while(edge == startEdge) {
							pos = r.nextInt(connectingEdges.length);
							edge = connectingEdges[pos];
						}
						rewire[0] = edge;
						foundEdge = true;
						loop = false;
//Debugger.log("out finish on no more nodes "+rewire[0]+" "+rewire[1]+" "+rewire[2]);
					}
					int testInd = r.nextInt(remainingNodes.size());
					int testNode = remainingNodes.get(testInd);
					if(getNodeOutDegree(testNode) > 0) { // found a suitable node
						rewire[1] = testNode;
						rewire[2] = nextNode;
						int[] connectingEdges = getNodeConnectingOutEdges(testNode);
						int pos = r.nextInt(connectingEdges.length);
						int edge = connectingEdges[pos];
						rewire[0] = edge;
						nextNode = testNode;
						remainingNodes.remove(testInd);
						in = true;
						foundEdge = true;
//Debugger.log("out rewire "+rewire[0]+" "+rewire[1]+" "+rewire[2]+" remaining "+remainingNodes.size());
					}
				}
			}
			if(remainingNodes.size() == 0) { // connect back to the start node and finish
				if(in) {
					rewire[1] = nextNode;
					rewire[2] = startNode;
					int[] connectingEdges = getNodeConnectingInEdges(startNode);
					int pos = r.nextInt(connectingEdges.length);
					int edge = connectingEdges[pos];
					while(edge == startEdge) {
						pos = r.nextInt(connectingEdges.length);
						edge = connectingEdges[pos];
					}
//Debugger.log("in finish on empty "+rewire[0]+" "+rewire[1]+" "+rewire[2]);
				} else {
					rewire[1] = startNode;
					rewire[2] = nextNode;
					int[] connectingEdges = getNodeConnectingOutEdges(startNode);
					int pos = r.nextInt(connectingEdges.length);
					int edge = connectingEdges[pos];
					while(edge == startEdge) {
						pos = r.nextInt(connectingEdges.length);
						edge = connectingEdges[pos];
					}
//Debugger.log("out finish on empty "+rewire[0]+" "+rewire[1]+" "+rewire[2]);
				}

				loop = false;
			}
		}
		
		
		FastGraph ret = generateRewiredGraph(rewireEdges);
		
		return ret;
		
	}
	
	/**
	 * Displays a FastGraph onscreen for the user
	 */
	public void displayFastGraph() {
		uk.ac.kent.displayGraph.Graph dg = this.generateDisplayGraph();
		dg.randomizeNodePoints(new Point(20,20),300,300);
		uk.ac.kent.displayGraph.display.GraphWindow gw = new uk.ac.kent.displayGraph.display.GraphWindow(dg, true);
		uk.ac.kent.displayGraph.drawers.BasicSpringEmbedder bse = new uk.ac.kent.displayGraph.drawers.BasicSpringEmbedder();
		GraphDrawerSpringEmbedder se = new GraphDrawerSpringEmbedder(KeyEvent.VK_Q,"Spring Embedder - randomize, no animation",true);
		se.setAnimateFlag(false);
		se.setIterations(100);
		se.setTimeLimit(200);
		se.setGraphPanel(gw.getGraphPanel());
		se.layout();
	}
		
	
}
