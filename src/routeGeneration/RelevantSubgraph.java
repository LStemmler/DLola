package routeGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import dlolaExprTree.ExprSection;
import dlolaObject.Channel;
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

		target = calculateChannelTrees(targetNode, 0, visited, passed, true);
		setTarget(target);

	}

	private Node calculateChannelTrees(dlolaObject.Node node, int delay, boolean[] visited, boolean[] passed, boolean reliable) {
		
		Node n = node.getGraphNode();
		if (visited[n.getIndex()]) {
			// becomes indirect
			return calculateIndirectChannelTrees(node, delay, new boolean[visited.length], new boolean[passed.length], reliable);
		} else {
			HashMap<Input, Integer> minDelay = new HashMap<>();
			HashMap<Input, Integer> minUnreliableDelay = new HashMap<>();
			Node ownNode = null;

			for (Input in : node.getInputList()) {
				if (ownNode == null) {
					ownNode = createNextIndexedNode(node);
				}
				if (reliable) {
					minDelay.put(in, delay);
				} else {
					minUnreliableDelay.put(in, delay);
				}
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
				boolean prevReliable = reliable && chan.isReliable();

				int channelDelay = Global.symtable.getChannel(entering).getDelay();
				Node prevTreeNode;
				if (!indirect) {
					prevTreeNode = calculateChannelTrees(Global.symtable.getNode(fromNode), delay + channelDelay,
							visited, passed, prevReliable);
				} else {
					boolean[] newvisited = new boolean[visited.length];
					boolean[] newpassed = new boolean[passed.length];
					
					newvisited[n.getIndex()] = true;
					newpassed[index] = true;
					
					prevTreeNode = calculateIndirectChannelTrees(Global.symtable.getNode(fromNode), delay + channelDelay, newvisited, newpassed, reliable && chan.isReliable());
				}

				// relevant subgraph
				if (prevTreeNode != null) {
					if (ownNode == null) {
						ownNode = createNextIndexedNode(node);
					}
					for (Input in : getReachableInputs(prevTreeNode)) {
						int inDelay = getMinDelay(prevTreeNode, in);
						if (minDelay.getOrDefault(in, Integer.MAX_VALUE) > inDelay) {
							minDelay.put(in, inDelay);
						}
						if (minUnreliableDelay.getOrDefault(in, Integer.MIN_VALUE) >= inDelay) {
							minUnreliableDelay.remove(in);
						}
					}
					for (Input in : getUnreliablyReachableInputs(prevTreeNode)) {
						int inDelay = getMinUnreliableDelay(prevTreeNode, in);
						if (minDelay.getOrDefault(in, Integer.MAX_VALUE) > inDelay) {
							if (minUnreliableDelay.getOrDefault(in, Integer.MAX_VALUE) > inDelay) {
								minUnreliableDelay.put(in, inDelay);
							}
						}
					}
					Edge pathEdge = channelTree.addEdge(createNextEdgeIndex(entering.getId()), prevTreeNode, ownNode,
							true);
					setEdgeIdentifier(pathEdge, entering.getId());
					setEdgeChannel(pathEdge, Global.systemModel.getChannel(entering));
					setIndirect(pathEdge, indirect || visited[getDLolaNode(prevTreeNode).getGraphNode().getIndex()]);
				}
				if (!indirect) {
					passed[index] = false;
				}
			}
			if (ownNode != null) {
				setCurDelay(ownNode, delay);
				for (Input in : minDelay.keySet()) {
					setMinDelay(ownNode, in, minDelay.get(in));
				}
				setReachableInputs(ownNode, new HashSet<>(minDelay.keySet()));
				for (Input in : minUnreliableDelay.keySet()) {
					setMinUnreliableDelay(ownNode, in, minUnreliableDelay.get(in));
				}
				setUnreliablyReachableInputs(ownNode, new HashSet<>(minUnreliableDelay.keySet()));
			}
			visited[n.getIndex()] = false;
			AddRelevantInputs(node, reliable);
			return ownNode;
		}
	}
	
	private Node calculateIndirectChannelTrees(dlolaObject.Node node, int delay, boolean[] visited, boolean[] passed, boolean reliable) {
		Node n = node.getGraphNode();
		if (visited[n.getIndex()]) {
			// useless circle
			return null;
		} else {
			HashMap<Input, Integer> minDelay = new HashMap<>();
			HashMap<Input, Integer> minUnreliableDelay = new HashMap<>();
			Node ownNode = null;

			for (Input in : node.getInputList()) {
				if (ownNode == null) {
					ownNode = createNextIndexedNode(node);
				}
				if (reliable) {
					minDelay.put(in, delay);
				} else {
					minUnreliableDelay.put(in, delay);
				}
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
				boolean prevReliable = reliable && chan.isReliable();

				int channelDelay = chan.getDelay();
				Node prevTreeNode = calculateIndirectChannelTrees(Global.symtable.getNode(fromNode), delay + channelDelay,
						visited, passed, prevReliable);

				// relevant subgraph
				if (prevTreeNode != null) {
					if (visited[getDLolaNode(prevTreeNode).getGraphNode().getIndex()]) {
						channelTree.removeNode(prevTreeNode);
					} else {
						if (ownNode == null) {
							ownNode = createNextIndexedNode(node);
						}
						for (Input in : getReachableInputs(prevTreeNode)) {
							int inDelay = getMinDelay(prevTreeNode, in);
							if (minDelay.getOrDefault(in, Integer.MAX_VALUE) > inDelay) {
								minDelay.put(in, inDelay);
							}
							if (minUnreliableDelay.getOrDefault(in, Integer.MIN_VALUE) >= inDelay) {
								minUnreliableDelay.remove(in);
							}
						}
						for (Input in : getUnreliablyReachableInputs(prevTreeNode)) {
							int inDelay = getMinUnreliableDelay(prevTreeNode, in);
							if (minDelay.getOrDefault(in, Integer.MAX_VALUE) > inDelay) {
								if (minUnreliableDelay.getOrDefault(in, Integer.MAX_VALUE) > inDelay) {
									minUnreliableDelay.put(in, inDelay);
								}
							}
						}
						Edge pathEdge = channelTree.addEdge(createNextEdgeIndex(entering.getId()), prevTreeNode, ownNode,
								true);
						setEdgeIdentifier(pathEdge, entering.getId());
						setEdgeChannel(pathEdge, Global.systemModel.getChannel(entering));
						setIndirect(pathEdge, true);
					}
					
				}
				passed[index] = false;
			}
			if (ownNode != null) {
				setCurDelay(ownNode, delay);
				for (Input in : minDelay.keySet()) {
					setMinDelay(ownNode, in, minDelay.get(in));
				}
				setReachableInputs(ownNode, new HashSet<>(minDelay.keySet()));
				for (Input in : minUnreliableDelay.keySet()) {
					setMinUnreliableDelay(ownNode, in, minUnreliableDelay.get(in));
				}
				setUnreliablyReachableInputs(ownNode, new HashSet<>(minUnreliableDelay.keySet()));
			}
			visited[n.getIndex()] = false;
			return ownNode;
		}
	}
	
	
	private void AddRelevantInputs(dlolaObject.Node node, boolean reliable) {
		if (reliable) {
			for (Input in: targetNode.getInputDependencies()) {
				node.addRelevantInput(in);
			}
		}
		for (Input in: targetNode.getNonessentialInputDependencies()) {
			node.addUnreliableRelevantInputs(in);
		}
	}
	
	
	public boolean isTarget(Node n) {
		return n.getAttribute("Target");
	}
	public void setTarget(Node n) {
		n.setAttribute("Target", true);
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

	public boolean isReliable(Edge e) {
		return ((Channel) e.getAttribute("Channel")).isReliable();
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

	private void setUnreliablyReachableInputs(Node n, HashSet<Input> reachableInputs) {
		n.setAttribute("UnreliablyReachableInputs", reachableInputs);
	}
	public HashSet<Input> getUnreliablyReachableInputs() {
		return target.getAttribute("UnreliablyReachableInputs");
	}
	
	public HashSet<Input> getUnreliablyReachableInputs(Node n) {
		return n.getAttribute("UnreliablyReachableInputs");
	}

	public HashSet<Input> getUnreliablyReachableInputs(Edge e) {
		return e.getSourceNode().getAttribute("UnreliablyReachableInputs");
	}
	
	public int getMinUnreliableDelay(Node n, Input in) {
		return n.getAttribute("MinUnreliableDelay " + in.getIdentifier());
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
	
	private void setMinUnreliableDelay(Node n, Input in, int delay) {
		n.setAttribute("MinUnreliableDelay " + in.getIdentifier(), delay);
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
	
	public HashSet<Node> getOptions(PathTree pt, ExprSection exprSec, boolean essential) throws PathGenerationException {
		ArrayList<Input> requiredInputs = exprSec.getRequiredInputs();
		if (essential) {
			if (!getReachableInputs().containsAll(requiredInputs)) {
				throw new PathGenerationException("Unreachable inputs during getOptions");
			}
		} else {
			for (Input in: requiredInputs) {
				if (!getReachableInputs().contains(in) && !getUnreliablyReachableInputs().contains(in)) {
					throw new PathGenerationException("Unreachable inputs during getOptions");
				}
			}
		}
		HashSet<Node> options;
		if (exprSec.getOptSize() == 0 || exprSec.shouldSplit()) {
			options = new HashSet<Node>();
			options.add(target);
			return options;
		}

		options = getOptions(pt, exprSec, target, !essential);
		if (options.isEmpty()) {
			throw new PathGenerationException("No options found in path generation.");
		}
		return options;
	}

	private HashSet<Node> getOptions(PathTree pt, ExprSection exprSec, Node n, boolean unreliable) {
		ArrayList<Input> requiredInputs = exprSec.getRequiredInputs();
		HashSet<Node> options = new HashSet<Node>();
		dlolaObject.Node node = getDLolaNode(n);

		if (unreliable) {
			for (Input in : requiredInputs) {
				if (!getReachableInputs().contains(in) && !getUnreliablyReachableInputs().contains(in))
					return options;
			}
		} else {
			if (!getReachableInputs(n).containsAll(requiredInputs))
				return options;
		}

		boolean isAvailable = pt.isAvailableExprSection(node, exprSec, unreliable);
		if (isAvailable) {
			// contains parent (without split)
			options.add(n);
			if (!(unreliable && !pt.unreliableExprSectionPresent(node, exprSec))) {
				// If there's no unreliable path for an unreliable output planned yet, it might yield a better solution
				return options;
			}
		}
		// Inputs cannot be split, they must be resolved
		if (exprSec.getChildren().size() != 0) {
			options.add(n);
			boolean containsChildren = true;
			int index = 0;
			while (containsChildren) {
				if (index == exprSec.getChildren().size()) {
					// contains all children
					return options;
				}
				containsChildren = pt.isAvailableExprSection(node, exprSec.getChildren().get(index++), unreliable);
			}
		}
		for (Edge e : n.getEnteringEdgeSet()) {
			if (!isIndirect(e) && (isReliable(e) || unreliable)) {
				options.addAll(getOptions(pt, exprSec, e.getSourceNode(), unreliable));
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
