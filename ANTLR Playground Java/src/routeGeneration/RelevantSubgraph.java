package routeGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import dlolaExprTree.ExprSection;
import dlolaObject.Channel;
import dlolaObject.DLolaObject;
import dlolaObject.Input;
import main.Global;

public final class RelevantSubgraph {

	dlolaObject.Node targetNode;
	SingleGraph channelTree;
	Node target;

	public dlolaObject.Node getOutputNode() {
		return targetNode;
	}

	public SingleGraph getChannelTree() {
		return channelTree;
	}

	public Node getTarget() {
		return target;
	}

	public RelevantSubgraph(dlolaObject.Node outputNode) {
		this.targetNode = outputNode;
		generateChannelTree();
	}

	private void generateChannelTree() {
		channelTree = new SingleGraph("CT " + targetNode.getIdentifier());

		boolean[] visited = new boolean[Global.systemModel.getNetworkGraph().getNodeCount()];
		boolean[] passed = new boolean[Global.symtable.getChannelList().size()];

		target = calculateChannelTrees(targetNode, 0, visited, passed);

	}

	private Node calculateChannelTrees(dlolaObject.Node node, int delay, boolean[] visited, boolean[] passed) {
		
		//TODO clean up, better relevant subgraphs after introducing multipoint channels
		// calculateIndirectChannelTrees einbinden
		// evtl bei calculateIndirectChannelTrees Optionen ohne reachable Inputs streichen
		
		

		Node n = node.getGraphNode();
		if (visited[n.getIndex()]) {
			// becomes indirect
			return calculateIndirectChannelTrees(node, delay, new boolean[visited.length], new boolean[passed.length]);
		} else {
			HashMap<Input, Integer> minDelay = new HashMap<>();
			Node ownNode = null;
			HashSet<Input> reachedInputs = new HashSet<>();

			for (Input in : node.getInputList()) {
				if (ownNode == null) {
					ownNode = createNextIndexedNode(node);
				}
				setMinDelay(ownNode, in, delay);
				minDelay.put(in, delay);
				reachedInputs.add(in);
			}
			visited[n.getIndex()] = true;

			for (Edge entering : n.getEachEnteringEdge()) {
				Node fromNode;

				// Necessary since undirected
				if (entering.getNode1().equals(n)) {
					fromNode = entering.getNode0();
				} else {
					fromNode = entering.getNode1();
				}

				Channel chan = Global.symtable.getChannel(entering);
				int index = Global.symtable.getChannelList().indexOf(chan);
				
				boolean indirect = false;
				if (passed[index]) {
					indirect = true;
				} else {
					passed[index] = true;
				}

				int channelDelay = Global.symtable.getChannel(entering).getDelay();
				Node prevTreeNode;
				if (!indirect) {
					prevTreeNode = calculateChannelTrees(Global.symtable.getNode(fromNode), delay + channelDelay,
							visited, passed);
				} else {
					prevTreeNode = calculateIndirectChannelTrees(node, delay, new boolean[visited.length], new boolean[passed.length]);
				}

				// relevant subgraph
				if (prevTreeNode != null) {
					if (ownNode == null) {
						ownNode = createNextIndexedNode(node);
					}
					for (Input in : getReachableInputs(prevTreeNode)) {
						if (minDelay.getOrDefault(in, Integer.MAX_VALUE) > getMinDelay(prevTreeNode, in)) {
							minDelay.put(in, getMinDelay(prevTreeNode, in));
						}
						reachedInputs.add(in);
					}
					Edge pathEdge = channelTree.addEdge(createNextEdgeIndex(entering.getId()), prevTreeNode, ownNode,
							true);
					setEdgeIdentifier(pathEdge, entering.getId());
					setEdgeChannel(pathEdge, Global.systemModel.getChannel(entering));
					setIndirect(pathEdge, indirect);
				}
				if (!indirect) {
					passed[index] = false;
				}
			}
			if (ownNode != null) {
				for (Input in : minDelay.keySet()) {
					setMinDelay(ownNode, in, minDelay.get(in));
				}
				setCurDelay(ownNode, delay);
				setReachableInputs(ownNode, reachedInputs);
			}
			visited[n.getIndex()] = false;
			return ownNode;
		}
	}
	
	private Node calculateIndirectChannelTrees(dlolaObject.Node node, int delay, boolean[] visited, boolean[] passed) {
		Node n = node.getGraphNode();
		if (visited[n.getIndex()]) {
			// useless circle
			return null;
		} else {
			HashMap<Input, Integer> minDelay = new HashMap<>();
			Node ownNode = null;
			HashSet<Input> reachedInputs = new HashSet<>();

			for (Input in : node.getInputList()) {
				if (ownNode == null) {
					ownNode = createNextIndexedNode(node);
				}
				setMinDelay(ownNode, in, delay);
				minDelay.put(in, delay);
				reachedInputs.add(in);
			}
			visited[n.getIndex()] = true;

			for (Edge entering : n.getEachEnteringEdge()) {
				Node fromNode;

				// Necessary since undirected
				if (entering.getNode1().equals(n)) {
					fromNode = entering.getNode0();
				} else {
					fromNode = entering.getNode1();
				}

				Channel chan = Global.symtable.getChannel(entering);
				int index = Global.symtable.getChannelList().indexOf(chan);
				if (passed[index]) {
					continue; //Already passed that (probably multinode) channel, no need to visit
				}
				passed[index] = true;

				int channelDelay = chan.getDelay();
				Node prevTreeNode = calculateIndirectChannelTrees(Global.symtable.getNode(fromNode), delay + channelDelay,
						visited, passed);

				// relevant subgraph
				if (prevTreeNode != null) {
					if (ownNode == null) {
						ownNode = createNextIndexedNode(node);
					}
					for (Input in : getReachableInputs(prevTreeNode)) {
						if (minDelay.getOrDefault(in, Integer.MAX_VALUE) > getMinDelay(prevTreeNode, in)) {
							minDelay.put(in, getMinDelay(prevTreeNode, in));
						}
						reachedInputs.add(in);
					}
					Edge pathEdge = channelTree.addEdge(createNextEdgeIndex(entering.getId()), prevTreeNode, ownNode,
							true);
					setEdgeIdentifier(pathEdge, entering.getId());
					setEdgeChannel(pathEdge, Global.systemModel.getChannel(entering));
					setIndirect(pathEdge, true);
				}
				passed[index] = false;
			}
			if (ownNode != null) {
				for (Input in : minDelay.keySet()) {
					setMinDelay(ownNode, in, minDelay.get(in));
				}
				setCurDelay(ownNode, delay);
				setReachableInputs(ownNode, reachedInputs);
			}
			visited[n.getIndex()] = false;
			return ownNode;
		}
	}

	public String getNodeIdentifier(Node n) {
		return n.getAttribute("Identifier");
	}

	public dlolaObject.Node getDLolaNode(Node n) {
		return n.getAttribute("Node");
	}

	private void setEdgeIdentifier(Edge e, String str) {
		e.setAttribute("Identifier", str);
	}

	public String getEdgeIdentifier(Edge e) {
		return e.getAttribute("Identifier");
	}

	private void setEdgeChannel(Edge e, Channel c) {
		e.setAttribute("Channel", c);
	}

	public Channel getEdgeChannel(Edge e) {
		return e.getAttribute("Channel");
	}

	public boolean isIndirect(Edge e) {
		return e.getAttribute("Indirect");
	}

	public void setIndirect(Edge e, boolean indirect) {
		e.setAttribute("Indirect", indirect);
	}

	public int getCurDelay(Node n) {
		return n.getAttribute("CurDelay");
	}

	private void setReachableInputs(Node n, HashSet<Input> reachableInputs) {
		n.setAttribute("ReachableInputs", reachableInputs);
	}
	
	public HashSet<Input> getReachableInputs() {
		return target.getAttribute("ReachableInputs");
	}

	public HashSet<Input> getReachableInputs(Node n) {
		return n.getAttribute("ReachableInputs");
	}

	public HashSet<Input> getReachableInputs(Edge e) {
		return e.getSourceNode().getAttribute("ReachableInputs");
	}
	
	public int getMinDelay(Node n, Input in) {
		return n.getAttribute("MinDelay " + in.getIdentifier());
	}

	private void setCurDelay(Node n, int delay) {
		n.setAttribute("CurDelay", delay);
	}

	private void setMinDelay(Node n, Input in, int delay) {
		n.setAttribute("MinDelay " + in.getIdentifier(), delay);
	}

	private Node createNextIndexedNode(dlolaObject.Node node) {
		String str = node.getIdentifier();
		int i = 1;
		while (channelTree.getNode(str + "(" + i + ")") != null) {
			i++;
		}
		Node n = channelTree.addNode(str + "(" + i + ")");
		n.setAttribute("Identifier", str);
		n.setAttribute("Node", node);
		return n;
	}

	private String createNextEdgeIndex(String str) {
		int i = 1;
		while (channelTree.getEdge(str + "(" + i + ")") != null) {
			i++;
		}
		return str + "(" + i + ")";
	}

	public static void generateRelevantSubraphs() {
		for (dlolaObject.Node node : Global.symtable.getNodeList()) {
			node.generateRelevantSubgraph();
		}
	}
	
	public HashSet<Node> getOptions(PathTree pt, ExprSection exprSec) throws PathGenerationException {
		ArrayList<Input> requiredInputs = exprSec.getRequiredInputs();
		if (!getReachableInputs().containsAll(requiredInputs)) {
			throw new PathGenerationException("Unreachable inputs during getOptions");
		}
		HashSet<Node> options;
		if (exprSec.getOptSize() == 0 || exprSec.shouldSplit()) {
			options = new HashSet<Node>();
			options.add(target);
			return options;
		}
		options = getOptions(pt, exprSec, target);
		if (options.isEmpty()) {
			throw new PathGenerationException("No options found in path generation.");
		}
		return options;
	}

	private HashSet<Node> getOptions(PathTree pt, ExprSection exprSec, Node n) {
		ArrayList<Input> requiredInputs = exprSec.getRequiredInputs();
		HashSet<Node> options = new HashSet<Node>();
		if (getReachableInputs(n).containsAll(requiredInputs)) {

			if (pt.getAvailableExprSections(getDLolaNode(n)).containsKey(exprSec)) {
				// contains parent (without split)
				options.add(n);
				return options;
			} else {
				// Inputs cannot be split, they must be resolved
				if(exprSec.getChildren().size() != 0) {
					options.add(n);
					boolean containsChildren = true;
					int index = 0;
					while (containsChildren) {
						if (index == exprSec.getChildren().size()) {
							// contains all children
							return options;
						}
						containsChildren = pt.getAvailableExprSections(getDLolaNode(n))
								.containsKey(exprSec.getChildren().get(index++));
					}
				}
			}
			for (Edge e : n.getEnteringEdgeSet()) {
				if (!isIndirect(e)) {
					options.addAll(getOptions(pt, exprSec, e.getSourceNode()));
				}
			}
		}
		return options;
	}

	public ArrayList<dlolaObject.Node> getPathNodes(Node node) {
		ArrayList<dlolaObject.Node> pathNodes = new ArrayList<>();
		while (!node.getLeavingEdgeSet().isEmpty()) {
			pathNodes.add(getDLolaNode(node));
			node = node.getLeavingEdge(0).getTargetNode();
		}
		pathNodes.add(getDLolaNode(node));
		return pathNodes;
	}

	public ArrayList<Channel> getPathChannels(Node node) {
		ArrayList<Channel> pathChannels = new ArrayList<>();
		while (!node.getLeavingEdgeSet().isEmpty()) {
			pathChannels.add(getEdgeChannel(node.getLeavingEdge(0)));
			node = node.getLeavingEdge(0).getTargetNode();
		}
		return pathChannels;
	}
}
