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
			OutputDelays[i] = pt.getAvailableSubexTreesDelay(out.getParentNode(), out.getSubexpressionTree());
		}
	}
	

	public void printSolution() {
		String offset = "  ";
		String str;

		for (int i=0; i<outputs.size(); i++) {
			int delay = OutputDelays[i];
			Output out= outputs.get(i);
			if (delay == Global.STAT_DELAY) {
				Debug.out(17, offset + "Output "+ out.getIdentifier() + " at node " + out.getParentNode().getIdentifier() + " is statically calculable.");
			} else {
				Debug.out(17, offset + "Output "+ out.getIdentifier() + " at node " + out.getParentNode().getIdentifier() + " has delay: " + delay);
			}
		}
		
		if (Global.debugVerbosity > 17) {
			for (Task t : pt.solvedTasks) {
				if (t.resolved) {
					if (t.subext.input) {
						Debug.out(18, offset + "Resolved input "
								+ ((Input) t.subext.getRequiredInputs().toArray()[0]).getIdentifier());
					} else {
						Debug.out(18, offset + "Resolved " + t.subext.getHead().getText() + " / "
								+ t.subext.getOptTree().getText());
					}
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
					Debug.out(18, offset + offset + str);
				} else {
					if (t.PathNodes.size() == 1) {
						Debug.out(18, offset + "Splitted " + t.subext.getHead().getText() + " / "
								+ t.subext.getOptTree().getText());
					} else {
						Debug.out(18, offset + "Moved and splitted " + t.subext.getHead().getText() + " / "
								+ t.subext.getOptTree().getText());
					}
					if (t.subext.hasOptimalBandwidth()) {
						str = "with bandwidth " + t.subext.getBandwidth();
					} else {
						str = "with suboptimal bandwidth " + t.subext.getBandwidth() + " (opt: "
								+ t.subext.getOptSubBandwidth() + ")";
					}
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
					Debug.out(18, offset + offset + str);
				}
			}
		}
	}
}
