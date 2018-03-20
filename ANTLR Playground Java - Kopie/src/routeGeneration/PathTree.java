package routeGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dlolaObject.Channel;
import dlolaObject.Input;
import dlolaObject.Node;
import dlolaObject.Output;
import main.Debug;
import main.Global;

public class PathTree {

	
	// Node provides subexTree with delay;
	HashMap<Node, HashMap<SubexpressionTree, Integer>> availableSubexTrees = new HashMap<>(); 
	HashMap<Node, HashSet<SubexpressionTree>> requiredSubexTrees = new HashMap<>();
	
	HashMap<Task, Task> childMap = new HashMap<>();
	HashMap<Task, Task> inverseChildMap = new HashMap<>();
	
	
	HashMap<Channel, HashSet<Task>> channelUsage = new HashMap<>();
	ArrayList<Task> taskList = new ArrayList<Task>();
	ArrayList<Task> solvedTasks = new ArrayList<Task>();
	HashSet<Task> calculatedDelays= new HashSet<>();
	HashSet<Task> nonstarvingRecursions= new HashSet<>();

	public PathTree() {
		for (Node n : Global.symtable.getNodeList()) {
			for (Input in : n.getInputList()) {
				setAvailableSubexTreesDelay(n, in.getInputSubexp(), 0);
			}
			for (Output out : n.getOutputList()) {
				Task task = new Task(n, out.getSubexpressionTree());
				childMap.put(task, task);
				inverseChildMap.put(task, task);
				taskList.add(task);
				putRequiredSubexTrees(n, out.getSubexpressionTree());
			}
		}
	}

	// clone
	@SuppressWarnings("unchecked")
	private PathTree(PathTree pt) {
		for (Node n: pt.availableSubexTrees.keySet()) {
			HashMap<SubexpressionTree, Integer> clone = (HashMap<SubexpressionTree, Integer>) pt.getAvailableSubexTrees(n).clone();
			availableSubexTrees.put(n, clone);
		}
		for (Node n: pt.requiredSubexTrees.keySet()) {
			requiredSubexTrees.put(n, (HashSet<SubexpressionTree>) pt.getRequiredSubexTrees(n).clone());
		}
		for (Channel c: pt.channelUsage.keySet()) {
			channelUsage.put(c, (HashSet<Task>) pt.getChannelUsage(c).clone());
		}
		childMap = (HashMap<Task, Task>) pt.childMap.clone();
		inverseChildMap = (HashMap<Task, Task>) pt.inverseChildMap.clone();
		
		taskList = (ArrayList<Task>) pt.taskList.clone();
		solvedTasks = (ArrayList<Task>) pt.solvedTasks.clone();
		calculatedDelays = (HashSet<Task>) pt.calculatedDelays.clone();
		
	}

	public PathTree clone() {
		return new PathTree(this);
	}
	
	Task cloneTask(int index) {
		if (index < 0 || index > taskList.size()) {
			Debug.err(5, "cloneTask called on out-of-bounds index");
			Debug.abort();
		}
		Task t = taskList.get(index);
		Task tclone = t.clone();
		taskList.set(index, tclone);
		Task original = inverseChildMap.get(t);
		inverseChildMap.put(tclone, original);
		childMap.put(original, tclone);
		return tclone;
	}

	HashMap<SubexpressionTree, Integer> getAvailableSubexTrees(Node n) {
		HashMap<SubexpressionTree, Integer> available = availableSubexTrees.get(n);
		if (available == null) {
			available = new HashMap<SubexpressionTree, Integer>();
			availableSubexTrees.put(n, available);
		}
		return available;
	}
	
	Integer getAvailableSubexTreesDelay(Node n, SubexpressionTree subex) {
		HashMap<SubexpressionTree, Integer> available = getAvailableSubexTrees(n);
		return available.get(subex);
	}

	private HashSet<SubexpressionTree> getRequiredSubexTrees(Node n) {
		HashSet<SubexpressionTree> required = requiredSubexTrees.get(n);
		if (required == null) {
			required = new HashSet<SubexpressionTree>();
			requiredSubexTrees.put(n, required);
		}
		return required;
	}

	public HashSet<Task> getChannelUsage(Channel c) {
		HashSet<Task> sent = channelUsage.get(c);
		if (sent == null) {
			sent = new HashSet<Task>();
			channelUsage.put(c, sent);
		}
		return sent;
	}
	
	void putAvailableSubexTrees(Node n, SubexpressionTree subex) {
		HashMap<SubexpressionTree, Integer> available = availableSubexTrees.get(n);
		if (available == null) {
			available = new HashMap<SubexpressionTree, Integer>();
			availableSubexTrees.put(n, available);
		}
		if (available.get(subex) == null) available.put(subex, null);
	}

	void putRequiredSubexTrees(Node n, SubexpressionTree subex) {
		HashSet<SubexpressionTree> required = requiredSubexTrees.get(n);
		if (required == null) {
			required = new HashSet<SubexpressionTree>();
			requiredSubexTrees.put(n, required);
		}
		required.add(subex);
	}
	
	void putChannelUser(Channel c, Task t) {
		HashSet<Task> sent = channelUsage.get(c);
		if (sent == null) {
			sent = new HashSet<Task>();
			channelUsage.put(c, sent);
		}
		sent.add(t);
	}
	

	// Fails if worse than currently available delay, scrapping entire pathTree
	boolean setAvailableSubexTreesDelay(Node n, SubexpressionTree subex, Integer delay) {
		HashMap<SubexpressionTree, Integer> available = getAvailableSubexTrees(n);
		if (available.get(subex) != null) {
			if (available.get(subex) < delay) {
				return false;
			}
		}
		available.put(subex, delay);
		return true;
	}
	
	// returns new or existing task
	Task addOrMerge (Task spawnedTask) {
		Task existingTask;
		
		int index = taskList.indexOf(spawnedTask);
		if (index == -1) {
			index = solvedTasks.indexOf(spawnedTask);
			if (index == -1) {
				taskList.add(spawnedTask);
				childMap.put(spawnedTask, spawnedTask);
				inverseChildMap.put(spawnedTask, spawnedTask);
				return spawnedTask;
			} else {
				existingTask = inverseChildMap.get(solvedTasks.get(index));
			}
		} else {
			existingTask = inverseChildMap.get(taskList.get(index));
		}
		return existingTask;
	}
	

	@SuppressWarnings("unchecked")
	boolean finalCalculations() {
		ArrayList<Task> uncalculatedDelays = new ArrayList<Task>();
		HashSet<Task> oldCalculatedDelays = new HashSet<Task>();
		uncalculatedDelays.addAll(solvedTasks);
		
		while (!uncalculatedDelays.isEmpty()) {
			for (Task t: uncalculatedDelays) {
				try {
					if (t.tryCalculateDelays(this, t)) {
						calculatedDelays.add(t);
					}
				} catch (UnmonitorableRecursionException e) {
					return false;
				}
			}
			if (calculatedDelays.equals(oldCalculatedDelays)) {
				Debug.err(3, "Infinite Loop in delay calculation");
			}
			uncalculatedDelays.removeAll(calculatedDelays);
			oldCalculatedDelays = (HashSet<Task>) calculatedDelays.clone();
		}
		return true;
	}
	
}
