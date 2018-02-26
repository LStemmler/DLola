package semanticAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.util.parser.ParseException;

import dlolaObject.DLolaExpTreeObject;
import dlolaObject.DLolaObject;
import dlolaObject.Input;
import dlolaObject.Output;
import dlolaObject.Virtual;
import main.DLolaRunner;
import main.Debug;

public class SystemModel {
	
	ParseTree tree;
	MultiGraph networkGraph = new MultiGraph("NetworkGraph");
	SingleGraph dependencyGraph = new SingleGraph("DependencyGraph");
	SymbolTable symtable;
	
	public SymbolTable getSymbolTable() {
		return symtable;
	}

	public SystemModel (ParseTree tree) {
		this.tree = tree;
	}
	
	public void generate() {
		Debug.out(11, "Generating SystemModel");
		
		generateSymbolTable();
		DLolaRunner.ui.displayNetworkGraph();
		typecheck();
		generateDependencyGraph();
		analyzeDependencyCycles();
		subgraphAnalysis();
		Debug.out(11, "SystemModel generated");
	}

	private void generateSymbolTable() {
		Debug.out(12, "Generating Symbol Table");
		symtable = new SymbolTable(tree);
		Debug.out(12, "Symbol Table generated");
	}

	private void typecheck() {
//		Debug.out(12, "Starting Typecheck");
//		// TODO Auto-generated method stub
//		Debug.out(12, "Typecheck complete");
	}
	
	private void generateDependencyGraph() {
		Debug.out(12, "Generating Dependency Graph");
		// TODO Auto-generated method stub
		for (DLolaObject o: symtable.inputList) {dependencyGraph.addNode(o.getIdentifier());};
		for (DLolaObject o: symtable.virtualList) {dependencyGraph.addNode(o.getIdentifier());};
		for (DLolaObject o: symtable.outputList) {dependencyGraph.addNode(o.getIdentifier());};
		for (DLolaExpTreeObject o: symtable.virtualList) {addDependencies(o);};
		for (DLolaExpTreeObject o: symtable.outputList) {addDependencies(o);};
		for (Node n: dependencyGraph) {n.setAttribute("inputDependencies", new ArrayList<Input>());};
		DLolaRunner.ui.displayDependencyGraph();
		Debug.out(12, "Dependency Graph generated");
	}

	private void addDependencies(DLolaExpTreeObject o) {
		addDependencies(o.getIdentifier(), o.getExpTree());
	}

	private void addDependencies(String id, ParseTree tree) {
		String nodeType = Utils.escapeWhitespace(Trees
				.getNodeText(tree, Arrays.asList(DLolaRunner.parser.getRuleNames())), false);
		switch (nodeType) {
		case "identifier":
			String targetId = tree.getChild(0).getText();
			addDependency(id, targetId, 0);
			break;
		case "shiftExpr":
			targetId = tree.getChild(0).getChild(0).getText();
			int shift = Integer.valueOf(tree.getChild(2).getChild(0).getText());
			addDependency(id, targetId, shift);
			break;
		default:
			for (int i=0; i<tree.getChildCount(); i++) {
				addDependencies(id, tree.getChild(i));
			}
		}
	}
	
	private void addDependency(String id, String targetId, int shift) {
		DLolaObject target = symtable.getObject(targetId);
		if (target instanceof Input || target instanceof Output || target instanceof Virtual) {
			String edgeId = id+"->"+targetId;
			
			if (dependencyGraph.getEdge(edgeId) == null) {
				Edge e = dependencyGraph.addEdge(edgeId, id, targetId, true);
				TreeSet<Integer> shiftSet = new TreeSet<Integer>();
				shiftSet.add(shift);
				e.setAttribute("shiftSet", shiftSet);
			} else {
				Edge e = dependencyGraph.getEdge(edgeId);
				TreeSet<Integer> shiftSet = e.getAttribute("shiftSet");
				shiftSet.add(shift);
				e.setAttribute("shiftSet", shiftSet);
			}
		}
	}

	private void analyzeDependencyCycles() {
		Debug.out(12, "Dependence cycle analysis begun");
		boolean[] visited = new boolean [dependencyGraph.getNodeCount()];
		boolean[] recStack = new boolean [dependencyGraph.getNodeCount()];
		int[] shiftOffset = new int [dependencyGraph.getNodeCount()];
		for (Node node : dependencyGraph.getEachNode()) {
			if (!visited[node.getIndex()]) {
				try {
					nonnegCycleDFS(node.getIndex(), visited, recStack, shiftOffset);
				} catch (ParseException e) {
					Debug.err(3, e.getMessage());
					Debug.err(2, "DLola specification not well-formed or not efficiently monitorable");
					Debug.abort();
				}
			}
		}

		
		Debug.out(12, "Dependency cycle analysis complete");
	}

	private boolean nonnegCycleDFS(int si, boolean[] visited, boolean[] recStack, int[] shiftOffset) throws ParseException
	{
	    if(!visited[si])
	    {
	        // Mark the current node as visited and part of recursion stack
	        visited[si] = true;
	        recStack[si] = true;
	        
	 
	        // Recur for all the vertices adjacent to this vertex
	        for(Edge e : dependencyGraph.getNode(si).getEachLeavingEdge())
	        {
	        	int ti = e.getTargetNode().getIndex();
            	TreeSet<Integer> shiftSet = e.getAttribute("shiftSet");
	            if (!visited[ti]) {
	            	shiftOffset[ti] = shiftOffset[si]+shiftSet.last();
	            	nonnegCycleDFS(ti, visited, recStack, shiftOffset);
	            }
	            else if (recStack[ti]) {
	            	if (shiftOffset[ti] <= shiftOffset[si]+shiftSet.last()) {
	            		//Nonnegative cycle found
	            		throw new ParseException("Nonnegative dependency cycle detected!");
	            	}
	            }
	        }
	 
	    }
	    recStack[si] = false;  // remove the vertex from recursion stack
	    return false;
	}
	
	
	
	
	

	private void subgraphAnalysis() {
		Debug.out(12, "Initial relevant subgraph analysis begun");
		
		Debug.out(13, "Input dependency analysis begun");
		// Dependency Graph analysis
		for (Input in: symtable.getInputList()) {
			Node stream = dependencyGraph.getNode(in.getIdentifier());
			addDGInputDependency(stream, in);
			DGinputDependencyAnalysis(stream);
		}

		// Add dependencies to node
		for (dlolaObject.Node n: symtable.getNodeList()) {
			for (Output o: n.getOutputList()) {
				o.addInputDependencies(getDGInputDependencies(o));
				n.addInputDependencies(getDGInputDependencies(o));
			}
		}
		Debug.out(13, "Input dependency analysis complete");

		Debug.out(13, "Calculating relevant subgraphs");

		boolean[] recStack;
		for (Input in: symtable.getInputList()) {
			for (dlolaObject.Node node: in.getNodeList()) {
				recStack = new boolean[networkGraph.getNodeCount()];
				calculateRelevantSubgraph(in, node, recStack);
			}
		}
		

		// Ensure all required inputs are reachable
		for (dlolaObject.Node n: symtable.getNodeList()) {
			try {
				if (DLolaRunner.debugVerbosity >= 16) {
					String deplist = new String();
					String subgraphs = new String();
					for (Input in: n.getInputDependencies()) {
						deplist += in.getIdentifier() + ", ";
					}
					for (Input in: n.getRelevantSubgraphs()) {
						subgraphs += in.getIdentifier() + ", ";
					}
					Debug.out(16, "Node "+n.getIdentifier() +" requires inputs: "+ 
							deplist.substring(0, Math.max(deplist.length()-2, 0)));
					Debug.out(16, "Node "+n.getIdentifier() +" relevant for inputs: "+ 
							subgraphs.substring(0, Math.max(subgraphs.length()-2, 0)));
				}
				n.ensureReachability();
			} catch (java.text.ParseException e) {
				Debug.err(3, e.getMessage());
				e.printStackTrace();
				Debug.err(2, "DLola specification not well-formed or not efficiently monitorable");
				Debug.abort();
			}
		}
		Debug.out(13, "Relevant subgraphs calculated");
		
		Debug.out(12, "Initial relevant subgraph analysis complete");
	}
	
	private boolean calculateRelevantSubgraph(Input in, dlolaObject.Node node, boolean[] recStack) {
		if (recStack[node.getGraphNode().getIndex()]) {
			//circle
			return false;
		} else {
			Node n = node.getGraphNode();
			if (node.inRelevantSubgraph(in)) return true;
			
			boolean relevant = node.hasInputDependency(in);
			
			
			recStack[n.getIndex()] = true;
			for (Edge out : n.getEachLeavingEdge()) {
				Node outNode;
				
				// Necessary since undirected
				if (out.getNode1().equals(n)) {
					outNode = out.getNode0();
				} else {outNode = out.getNode1();}
				relevant = calculateRelevantSubgraph(in, (dlolaObject.Node) symtable.getObject(outNode.getId()), recStack) || relevant ;
			}
			if (relevant) {
				node.addToRelevantSubgraph(in);
			}
			recStack[n.getIndex()] = false;
			return relevant;
		}
	}
	

	private void DGinputDependencyAnalysis(Node stream) {
		ArrayList<Input> dependencies = getDGInputDependencies(stream);
		for (Edge requirement : stream.getEachEnteringEdge()) {
			Node requiringNode = requirement.getNode0();
			if (addDGInputDependencies(requiringNode, dependencies)) {
				DGinputDependencyAnalysis(requiringNode);
			}
		}
	}

	public Graph getNetworkGraph() {
		return networkGraph;
	}

	public Graph getDependencyGraph() {
		return dependencyGraph;
	}

	public ArrayList<Input> getDGInputDependencies(DLolaObject stream) {
		Node streamnode = dependencyGraph.getNode(stream.getIdentifier());
		return getDGInputDependencies(streamnode);
	}

	public ArrayList<Input> getDGInputDependencies(Node streamnode) {
		return streamnode.getAttribute("inputDependencies");
	}

	public boolean addDGInputDependency (Node streamnode, Input in) {
		ArrayList<Input> inputDependencies = streamnode.getAttribute("inputDependencies");
		if (!inputDependencies.contains(in)) {
			inputDependencies.add(in);
			streamnode.setAttribute("inputDependencies", inputDependencies);
			return true;
		}
		return false;
	}

	public boolean addDGInputDependencies (Node streamnode, ArrayList<Input> dependencies) {
		boolean changed = false;
		for (Input in: dependencies) {
			changed = addDGInputDependency(streamnode, in) || changed;
		}
		return changed;
	}

}
