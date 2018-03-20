package routeGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import dlolaObject.Channel;
import dlolaObject.Node;
import main.Debug;
import main.Global;

public class Task {
	public final Node n;
	public final SubexpressionTree subext;
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
		return (n.equals(((Task) obj).n) && subext.equals(((Task) obj).subext));
	}

	// for cloning unresolved /undecided tasks
	private Task(Task t) {
		n = t.n;
		subext = t.subext;
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

	public Task(Node n, SubexpressionTree subext) {
		this.n = n;
		this.subext = subext;
	}
	
	public boolean tryCalculateDelays(PathTree pt, Task head) throws UnmonitorableRecursionException {
		if (pt.calculatedDelays.contains(this)) return true;
		if (subext.isRecursive()) {
			if (!pt.nonstarvingRecursions.contains(this)) {
				ensureNonstarvingRecursion(pt);
			}
		}

		if (selectedOption == null) return false;
		Node source = PathNodes.get(0);
		
		boolean subexpressionsCalculated = true;
		for (Task child: getChildren(pt)) {
			if (child != head) {
				
				if (child == null)
				{
					Debug.err(5, "Child null");
				}
				subexpressionsCalculated  &= child.tryCalculateDelays(pt, head);
			}
		}
		if (!subexpressionsCalculated) return false;
		if (pt.calculatedDelays.contains(this)) return true;
		
		int delay = Global.STAT_DELAY;
		Integer subDelay;
		if (resolved) {
			subDelay = pt.getAvailableSubexTreesDelay(source, subext);
			if (!(subext.isRecursive() && source == head.n && subext == head.subext)) {
				if (subDelay == null) return false;
				delay = subDelay;
			}
		} else {
			for (SubexpressionTree child: subext.getSplitList()) {
				subDelay = pt.getAvailableSubexTreesDelay(source, child);
				if (!(subext.isRecursive() && source == head.n && child == head.subext)) {
					if (subDelay == null) return false;
					delay = Math.max(delay, subDelay);
				}
			}
			// maximum delay at source node
			if (delay != Global.STAT_DELAY) delay += subext.timeShift;
		}
		pt.setAvailableSubexTreesDelay(PathNodes.get(0), subext, delay);
		for (int i=1; i < PathNodes.size(); i++) {
			if (delay != Global.STAT_DELAY) delay += PathChannels.get(i-1).getDelay();
			pt.setAvailableSubexTreesDelay(PathNodes.get(i), subext, delay);
		}
		pt.calculatedDelays.add(this);
		return true;
	}
	
	
	// returns whether currently calculable or throws an exception
	private boolean ensureNonstarvingRecursion(PathTree pt) throws UnmonitorableRecursionException {
		HashMap<Node, HashMap<SubexpressionTree, Integer>> relativeDelays = new HashMap<>();
		return ensureNonstarvingRecursion(pt, 0, relativeDelays);
	}
	private boolean ensureNonstarvingRecursion(PathTree pt, int currentDelay, HashMap<Node, HashMap<SubexpressionTree, Integer>> relativeDelays) throws UnmonitorableRecursionException {
		HashMap<SubexpressionTree, Integer> subexpDelays = relativeDelays.get(n);
		if (subexpDelays == null) {
			subexpDelays = new HashMap<>();
			relativeDelays.put(n, subexpDelays);
		}
		Integer formerDelay = subexpDelays.get(subext);
		if (formerDelay != null) {
			if (currentDelay > formerDelay) {
				Debug.err(18, "Attempted solution for " + subext.getHead().getText() +" at node " + n.getIdentifier() + " not efficiently monitorable due to positive delay cycle");
				throw new UnmonitorableRecursionException("Attempted solution for " + subext.getHead().getText() +" at node " + n.getIdentifier() + " not efficiently monitorable due to positive delay cycle");
			} else {
				return true;
			}
		} else {
			subexpDelays.put(subext, currentDelay);
			for (Task child: getChildren(pt)) {
				if (!pt.nonstarvingRecursions.contains(child)) {
					int delayChange = child.n.getRelevantSubgraph().getCurDelay(child.selectedOption);
					delayChange += subext.timeShift;
					ensureNonstarvingRecursion(pt, currentDelay+ delayChange, relativeDelays);
				}
			}
			subexpDelays.remove(subext);
		}
		pt.nonstarvingRecursions.add(this);
		return true;
	}

	// can fail if Input not present at selected option
	public boolean pathAndSplit(PathTree pt, org.graphstream.graph.Node selectedOption) throws UnmonitorableRecursionException {
		
		this.selectedOption = selectedOption;
		Node node = n.getRelevantSubgraph().getDLolaNode(selectedOption);
		
		if (pt.getAvailableSubexTrees(node).containsKey(subext)) {
			//No split required, but safe
			resolved = true;
		} else {
			for (SubexpressionTree child : subext.getSplitList()) {
				Task spawnedTask = new Task(node, child);
				Task resultingTask = pt.addOrMerge(spawnedTask);
				spawnedTasks.add(resultingTask);
			}
		}
		PathNodes = n.getRelevantSubgraph().getPathNodes(selectedOption);
		PathChannels = n.getRelevantSubgraph().getPathChannels(selectedOption);
		for (Node pathnode : PathNodes) {
			pt.putAvailableSubexTrees(pathnode, subext);
		}
		for (Channel pathchannel : PathChannels) {
			pt.putChannelUser(pathchannel, this);
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
			HashSet<org.graphstream.graph.Node> options = n.getRelevantSubgraph().getOptions(pt, subext);
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