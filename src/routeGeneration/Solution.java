package routeGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import dlolaObject.Output;
import dlolaObject.Node;
import main.Debug;
import main.Global;

public final class Solution {

	final PathTree pt;
	public final int[] OutputDelays;
	final ArrayList<Output> outputs = Global.symtable.getOutputList();
	
	Solution (PathTree pt) { 
		OutputDelays = new int[Global.symtable.getOutputList().size()];
		this.pt = pt;
		
		for (int i=0; i<outputs.size(); i++) {
			Output out = outputs.get(i);
			OutputDelays[i] = pt.getAvailableExprSectionDelay(out.getParentNode(), out.getExprSection(), !out.isEssential());
		}
	}
	

	public void printSolution(int verbosity) {
		if (Global.debugVerbosity < verbosity) return;
		String offset = "  ";
		String str;

		for (Task t : pt.solvedTasks) {
			if (t.resolved) {
				Debug.out(verbosity,
						offset + "Resolved " + t.exprSec.getOpt().toString());
				if (t.PathNodes.size() == 1) {
					str = "at ";
				} else {
					str = "via ";
				}
				str += t.PathNodes.get(0).getIdentifier();
				for (int i = 1; i < t.PathNodes.size(); i++) {
					str += " --" + t.PathChannels.get(i - 1).getIdentifier() + "-> "
							+ t.PathNodes.get(i).getIdentifier();
				}
				Debug.out(verbosity, offset + offset + str);
			} else {
				if (t.PathNodes.size() == 1) {
					Debug.out(verbosity, offset + "Splitted " +t.exprSec.getOpt().toString());
				} else {
					Debug.out(verbosity, offset + "Moved and splitted " + t.exprSec.getOpt().toString());
				}
				str = "with bandwidth " + t.exprSec.getOptSize();
				if (t.PathNodes.size() == 1) {
					str += " at ";
				} else {
					str += " via ";
				}
				str += t.PathNodes.get(0).getIdentifier();
				for (int i = 1; i < t.PathNodes.size(); i++) {
					str += " --" + t.PathChannels.get(i - 1).getIdentifier() + "-> "
							+ t.PathNodes.get(i).getIdentifier();
				}
				Debug.out(verbosity, offset + offset + str);
			}
		}
	}
	
	public void printDelays(int verbosity) {
		if (Global.debugVerbosity < verbosity) return;
		String offset = "  ";

		for (int i=0; i<outputs.size(); i++) {
			int delay = OutputDelays[i];
			Output out= outputs.get(i);
			if (delay == Global.STAT_DELAY) {
				Debug.out(verbosity, offset + "Output "+ out.getIdentifier() + " at node " + out.getParentNode().getIdentifier() + " is statically calculable.");
			} else {
				Debug.out(verbosity, offset + "Output "+ out.getIdentifier() + " at node " + out.getParentNode().getIdentifier() + " has delay: " + delay);
			}
		}
	}
	
	public PathTree getPathTree() {
		return pt;
	}
	
	public String generateDetailedSolution() {
		HashMap<Node, ArrayList<Task>> tasksByNodes = new HashMap<>();
		for (Node n: Global.symtable.getNodeList()) {
			tasksByNodes.put(n, new ArrayList<Task>());
		}
		
		for (Task t: pt.solvedTasks) {
			for (Node n: t.PathNodes) {
				tasksByNodes.get(n).add(t);
			}
		}
		
		String retString = new String();
		String newline = Global.newline;
		

		for (int i=0; i<outputs.size(); i++) {
			int delay = OutputDelays[i];
			Output out= outputs.get(i);
			if (delay == Global.STAT_DELAY) {
				retString += "Output "+ out.getIdentifier() + " at node " + out.getParentNode().getIdentifier() + " is statically calculable." + newline;
			} else {
				retString += "Output "+ out.getIdentifier() + " at node " + out.getParentNode().getIdentifier() + " has delay: " + delay+newline;
			}
		}
		retString += newline;
		
		for (Entry<Node, ArrayList<Task>> e: tasksByNodes.entrySet()) {
			Node n = e.getKey();
			ArrayList<Task> tasklist = e.getValue();
			String nodestring = n.generateNodeSolution(this, tasklist);
			retString += nodestring + newline;
		}
		
		return retString;
	}
}
