package routeGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import dlolaExprTree.ExprSection;
import dlolaObject.Channel;
import dlolaObject.Node;
import main.Debug;
import main.Global;

public class Task {
	public final Node n;
	public final ExprSection exprSec;
	public org.graphstream.graph.Node selectedOption;
	public ArrayList<Node> PathNodes = new ArrayList<>();
	public ArrayList<Channel> PathChannels = new ArrayList<>();
	public boolean resolved = false;
	public HashSet<Task> spawnedTasks = new HashSet<>();
	

	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!Task.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
		return (n.equals(((Task) obj).n) && exprSec.equals(((Task) obj).exprSec));
	}

	// for cloning unresolved /undecided tasks
	private Task(Task t) {
		n = t.n;
		exprSec = t.exprSec;
		if (t.selectedOption != null) {
			try {
				throw new PathGenerationException("Should only clone undecided tasks");
			} catch (PathGenerationException e) {
				e.printStackTrace();
				Debug.abort();
			}
		}
	}

	public Task clone() {
		return new Task(this);
	}

	public Task(Node n, ExprSection exprSec) {
		this.n = n;
		this.exprSec = exprSec;
	}
	
	public boolean tryCalculateDelays(PathTree pt, Task head) throws UnmonitorableRecursionException {
		if (pt.calculatedDelays.contains(this)) return true;
		if (exprSec.isRecursive()) {
			if (!pt.nonstarvingRecursions.contains(this)) {
				ensureNonstarvingRecursion(pt);
			}
		}

		if (selectedOption == null) return false;
		Node source = PathNodes.get(0);
		
		boolean subexpressionsCalculated = true;
		for (Task child: getChildren(pt)) {
			if (child != head) {
				subexpressionsCalculated  &= child.tryCalculateDelays(pt, head);
			}
		}
		if (!subexpressionsCalculated) return false;
		if (pt.calculatedDelays.contains(this)) return true;
		
		int delay = Global.STAT_DELAY;
		Integer subDelay;
		if (resolved) {
			subDelay = pt.getAvailableExprSectionDelay(source, exprSec);
			if (!(exprSec.isRecursive() && source == head.n && exprSec == head.exprSec)) {
				if (subDelay == null) return false;
				delay = subDelay;
			}
		} else {
			for (ExprSection child: exprSec.getChildren()) {
				subDelay = pt.getAvailableExprSectionDelay(source, child);
				if (!(exprSec.isRecursive() && source == head.n && child == head.exprSec)) {
					if (subDelay == null) return false;
					delay = Math.max(delay, subDelay);
				}
			}
			// maximum delay at source node
			if (delay != Global.STAT_DELAY) delay += exprSec.getTimeShift();
		}
		pt.setAvailableExprSectionDelay(PathNodes.get(0), exprSec, delay);
		for (int i=0; i < PathChannels.size(); i++) {
			if (delay != Global.STAT_DELAY) delay += PathChannels.get(i).getDelay();
			ArrayList<Node> listeningNodes = PathChannels.get(i).getNodes();
			for (Node n:listeningNodes) {
				if (n != PathNodes.get(i))
					pt.setAvailableExprSectionDelay(n, exprSec, delay);
			}
		}
		pt.calculatedDelays.add(this);
		return true;
	}
	
	
	// returns whether currently calculable or throws an exception
	private boolean ensureNonstarvingRecursion(PathTree pt) throws UnmonitorableRecursionException {
		HashMap<Node, HashMap<ExprSection, Integer>> relativeDelays = new HashMap<>();
		return ensureNonstarvingRecursion(pt, 0, relativeDelays);
	}
	private boolean ensureNonstarvingRecursion(PathTree pt, int currentDelay, HashMap<Node, HashMap<ExprSection, Integer>> relativeDelays) throws UnmonitorableRecursionException {
		HashMap<ExprSection, Integer> subexpDelays = relativeDelays.get(n);
		if (subexpDelays == null) {
			subexpDelays = new HashMap<>();
			relativeDelays.put(n, subexpDelays);
		}
		Integer formerDelay = subexpDelays.get(exprSec);
		if (formerDelay != null) {
			if (currentDelay > formerDelay) {
				Debug.err(18, "Attempted solution for " + exprSec.getHead().toString() +" at node " + n.getIdentifier() + " not efficiently monitorable due to positive delay cycle");
				throw new UnmonitorableRecursionException("Attempted solution for " + exprSec.getHead().toString() +" at node " + n.getIdentifier() + " not efficiently monitorable due to positive delay cycle");
			} else {
				return true;
			}
		} else {
			subexpDelays.put(exprSec, currentDelay);
			for (Task child: getChildren(pt)) {
				if (!pt.nonstarvingRecursions.contains(child)) {
					int delayChange = child.n.getRelevantSubgraph().getCurDelay(child.selectedOption);
					delayChange += exprSec.getTimeShift();
					ensureNonstarvingRecursion(pt, currentDelay+ delayChange, relativeDelays);
				}
			}
			subexpDelays.remove(exprSec);
		}
		pt.nonstarvingRecursions.add(this);
		return true;
	}

	// can fail if Input not present at selected option
	public boolean pathAndSplit(PathTree pt, org.graphstream.graph.Node selectedOption) throws UnmonitorableRecursionException {
		
		this.selectedOption = selectedOption;
		Node node = n.getRelevantSubgraph().getDLolaNode(selectedOption);
		
		if (pt.getAvailableExprSections(node).containsKey(exprSec)) {
			//No split required, but safe
			resolved = true;
		} else {
			for (ExprSection child : exprSec.getChildren()) {
				Task spawnedTask = new Task(node, child);
				Task resultingTask = pt.addOrMerge(spawnedTask);
				spawnedTasks.add(resultingTask);
			}
		}
		PathNodes = n.getRelevantSubgraph().getPathNodes(selectedOption);
		PathChannels = n.getRelevantSubgraph().getPathChannels(selectedOption);
		for (Channel pathchannel : PathChannels) {
			pt.putChannelUser(pathchannel, this);
			for (Node listener: pathchannel.getNodes()) {
				pt.putAvailableExprSections(listener, exprSec);
			}
		}
		pt.taskList.remove(this);
		pt.solvedTasks.add(this);
		return true;
	}
	
	public Node pathChannelSource(Channel c) {
		int index = PathChannels.indexOf(c);
		return PathNodes.get(index);
	}
	
	ArrayList<org.graphstream.graph.Node> evaluateAvailableOptions(PathTree pt) {
		ArrayList<org.graphstream.graph.Node> orderedList = new ArrayList<>();
		try {
			HashSet<org.graphstream.graph.Node> options = n.getRelevantSubgraph().getOptions(pt, exprSec);
			orderedList.addAll(options);
		} catch (PathGenerationException e) {
			e.printStackTrace();
			Debug.abort();
		}
		return orderedList;
	}


	public HashSet<Task> getChildren(PathTree pt) {
		HashSet<Task> ret = new HashSet<>();
		for (Task originalChild: spawnedTasks) {
			ret.add(pt.childMap.get(originalChild));
		}
		return ret;
	}
	
}