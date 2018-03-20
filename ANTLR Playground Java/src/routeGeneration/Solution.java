package routeGeneration;

import java.util.ArrayList;

import dlolaObject.Input;
import dlolaObject.Output;
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
			OutputDelays[i] = pt.getAvailableExprSectionDelay(out.getParentNode(), out.getExprSection());
		}
	}
	

	public void printSolution(int verbosity) {
		if (Global.debugVerbosity < verbosity) return;
		String offset = "  ";
		String str;

		for (int i=0; i<outputs.size(); i++) {
			int delay = OutputDelays[i];
			Output out= outputs.get(i);
			if (delay == Global.STAT_DELAY) {
				Debug.out(verbosity, offset + "Output "+ out.getIdentifier() + " at node " + out.getParentNode().getIdentifier() + " is statically calculable.");
			} else {
				Debug.out(verbosity, offset + "Output "+ out.getIdentifier() + " at node " + out.getParentNode().getIdentifier() + " has delay: " + delay);
			}
		}

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
}
