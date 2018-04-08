package routeGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import dlolaExprTree.ExprSection;
import dlolaExprTree.ExpressionMap;
import dlolaObject.Channel;
import dlolaObject.Input;
import dlolaObject.Node;
import dlolaObject.Output;
import main.Debug;
import main.Global;

public class PathTree {

	
	// Node provides subexTree with delay;
	HashMap<Node, HashMap<ExprSection, AvailabilityTuple[]>> availableExprSections = new HashMap<>();
	
	HashMap<Task, Task> childMap = new HashMap<>();
	HashMap<Task, Task> inverseChildMap = new HashMap<>();
	
	
	HashMap<Channel, HashSet<Task>> channelUsage = new HashMap<>();
	ArrayList<Task> taskList = new ArrayList<Task>();
	ArrayList<Task> solvedTasks = new ArrayList<Task>();
	HashSet<Task> calculatedDelays= new HashSet<>();
	HashSet<Task> nonstarvingRecursions= new HashSet<>();

	public PathTree() {
		for (Node n : Global.symtable.getNodeList()) {
			for (ExprSection exprSec: ExpressionMap.getExprSections()) {
				if (n.getInputList().containsAll(exprSec.getRequiredInputs())) {
					setAvailableExprSectionDelay(n, exprSec, exprSec.getTimeShift(), false);
					Debug.out(14, "Expression "+exprSec.getHead().toString() +" freely available at "+ n.getIdentifier() +" as all inputs are present" );
				}
			}
			for (Output out : n.getOutputList()) {
				Task task = new Task(n, ExpressionMap.getExprSection(out.getExpression()), out.isEssential());
				childMap.put(task, task);
				inverseChildMap.put(task, task);
				taskList.add(task);
			}
		}
	}

	// clone
	@SuppressWarnings("unchecked")
	private PathTree(PathTree pt) {
		for (Node n: pt.availableExprSections.keySet()) {
			
			HashMap<ExprSection, AvailabilityTuple[]> toClone = (HashMap<ExprSection, AvailabilityTuple[]>) pt.getAvailableExprSections(n);
			HashMap<ExprSection, AvailabilityTuple[]> clone = new HashMap<ExprSection, AvailabilityTuple[]>();
			for (ExprSection expr:pt.getAvailableExprSections(n).keySet()) {
				AvailabilityTuple[] availabilities = toClone.get(expr).clone();
				for (int i=0; i<availabilities.length; i++) {
					availabilities[i] = availabilities[i].clone();
				}
				clone.put(expr, availabilities);
			}
			availableExprSections.put(n, clone);
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

	HashMap<ExprSection, AvailabilityTuple[]> getAvailableExprSections(Node n) {
		HashMap<ExprSection, AvailabilityTuple[]> available = availableExprSections.get(n);
		if (available == null) {
			available = new HashMap<ExprSection, AvailabilityTuple[]>();
			availableExprSections.put(n, available);
		}
		return available;
	}
	
	public Integer getAvailableExprSectionDelay(Node n, ExprSection exprSec, boolean unreliable) {
		AvailabilityTuple[] available = getOrInsertAvailabilityTuples(n, exprSec);
		if (unreliable) {
			if (available[0].available && available[0].delay != null) {
				if (available[1].available && available[1].delay != null) {
					return Math.min(available[0].delay, available[1].delay);
				} else {
					return available[0].delay;
				}
			} else {
				return available[1].delay;
			}
		} else {
			return available[0].delay;
		}
	}

	
	boolean unreliableExprSectionPresent(Node n, ExprSection exprSec) {
		AvailabilityTuple[] available = getOrInsertAvailabilityTuples(n, exprSec);
		return available[1].available;
	}
	
	boolean isAvailableExprSection(Node n, ExprSection exprSec, boolean unreliable) {
		AvailabilityTuple[] available = getOrInsertAvailabilityTuples(n, exprSec);
		if (unreliable) {
			if (available[1].available) return true;
		}
		if (available[0].available) return true;
		return false;
	}
	
	public HashSet<Task> getChannelUsage(Channel c) {
		HashSet<Task> sent = channelUsage.get(c);
		if (sent == null) {
			sent = new HashSet<Task>();
			channelUsage.put(c, sent);
		}
		return sent;
	}
	
	void putAvailableExprSections(Node n, ExprSection exprSec, boolean unreliable) {
		AvailabilityTuple[] available = getOrInsertAvailabilityTuples(n, exprSec);
		if (unreliable) {
			if (!available[1].available) available[1] = new AvailabilityTuple(true, null);
		} else {
			if (!available[0].available) available[0] = new AvailabilityTuple(true, null);
		}
	}
	
	AvailabilityTuple[] getOrInsertAvailabilityTuples(Node n, ExprSection exprSec) {
		HashMap<ExprSection, AvailabilityTuple[]> available = availableExprSections.get(n);
		if (available == null) {
			available = new HashMap<ExprSection, AvailabilityTuple[]>();
			availableExprSections.put(n, available);
		}
		AvailabilityTuple[] tuples = available.get(exprSec);
		if (tuples == null) {
			tuples = new AvailabilityTuple[2];	//TODO 2 for reliable/unreliable, may increase for more options
			tuples[0] = new AvailabilityTuple(false, null);
			tuples[1] = new AvailabilityTuple(false, null);
			available.put(exprSec, tuples);
		}
		return tuples;
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
	boolean setAvailableExprSectionDelay(Node n, ExprSection exprSec, Integer delay, boolean unreliable) {
		AvailabilityTuple[] available = getOrInsertAvailabilityTuples(n, exprSec);
		if (unreliable) {
			if (available[1].available && available[1].delay != null && available[1].delay < delay) {
				return false;
			}
		}
		if (available[0].available && available[0].delay != null && available[0].delay < delay) {
			return false;
		}
		if (unreliable) {
			available[1] = new AvailabilityTuple(true, delay);
		} else {
			available[0] = new AvailabilityTuple(true, delay);
		}
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
	
	public int solvedTaskSize () {
		return solvedTasks.size();
	}
	
	
	class AvailabilityTuple { 
		  public final boolean available; 
		  public final Integer delay; 
		  AvailabilityTuple(boolean available, Integer delay) { 
		    this.available = available;
		    this.delay = delay;
		  }
		  protected AvailabilityTuple clone() {
			  if (delay != null) {
				  final Integer d = new Integer(delay);
				  return new AvailabilityTuple(available, d);
			  } else {
				  return new AvailabilityTuple(available, null);
			  }
		  }
		  boolean available() {
			  return available;
		  }
		  Integer getDelay() {
			  return delay;
		  }
		  
		} 
	
}
