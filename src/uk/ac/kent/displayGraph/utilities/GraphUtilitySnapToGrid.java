package uk.ac.kent.displayGraph.utilities;import java.awt.event.*;/** * Snaps the nodes and edge bends in a graph to the grid size. Takes * no account of grid position conflicts. */public class GraphUtilitySnapToGrid extends GraphUtility {	final static int GRID = 50;/** Trivial constructor. */	public GraphUtilitySnapToGrid() {		super(KeyEvent.VK_G,"Snap to Grid",KeyEvent.VK_G);	}/** Trivial constructor. */	public GraphUtilitySnapToGrid(int key, String s) {		super(key,s);	}	public GraphUtilitySnapToGrid(int key, String s, int mnemonic) {		super(key,s,mnemonic);	}/** Changes the graph. */	public void apply() {		getGraph().snapToGrid(GRID,GRID);		getGraphPanel().update(getGraphPanel().getGraphics());	}}