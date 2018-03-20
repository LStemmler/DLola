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

public class RouteBuilder {

	PathTree initialTree;
	public ArrayList<Solution> generatedRoutes = new ArrayList<>();
	int solutionCounter = 0;

	public RouteBuilder() {
		Debug.out(12, "Initializing RouteBuilder");
		initialTree = new PathTree();
		Debug.out(12, "RouteBuilder initialized");
	}

	public void generateRoutes() {
		Debug.out(11, "Generating Routes");
		routeGeneration(initialTree);
		solutionCounter += generatedRoutes.size();
		Debug.out(12, solutionCounter + " Routes generated");
		if (generatedRoutes.size() == 0) {
			Debug.err(4, "Could not find a non-starving route");
		}
		Debug.out(11, "Route generation complete");
	}

	private void routeGeneration(PathTree pt) {
		if (pt.taskList.isEmpty()) {
			if (pt.finalCalculations()) {
				Solution sol = new Solution(pt);
				generatedRoutes.add(sol);
				Debug.out(15, solutionCounter + generatedRoutes.size() + " Routes generated");
				if (Global.debugVerbosity > 16) {
					sol.printSolution();
				}
				if (generatedRoutes.size() % 10000 == 0) {
					solutionCounter += generatedRoutes.size();
					Debug.out(13, solutionCounter + " Routes generated");
					Global.evaluator.evaluate();
					generatedRoutes.clear();
				}
			}
			return;
		}

		selectNextOption(pt);

	}

	private void selectNextOption(PathTree pt) {

		int index = evaluateAvailableTasks(pt);
		ArrayList<org.graphstream.graph.Node> optionArray = pt.taskList.get(index).evaluateAvailableOptions(pt);

		for (org.graphstream.graph.Node option : optionArray) {
			PathTree attempt = pt.clone();
			Task t = attempt.cloneTask(index);
			try {
				if (t.pathAndSplit(attempt, option)) {
					if (checkConstraints(attempt, t)) {
						if (Global.debugVerbosity > 18) {
							printTask(attempt, t, true);
						}
						routeGeneration(attempt);
					}
				}
			} catch (UnmonitorableRecursionException e) {
				Debug.out(18, "UnmonitorableRecursionException during Route generation");
			}
			if (Global.debugVerbosity > 18) {
				printTask(attempt, t, false);
			}
		}

		// TODO (?)
	}

	private boolean checkConstraints(PathTree attempt, Task t) {
		// TODO Proper constraint checking
		for (int i = 0; i < t.PathChannels.size(); i++) {
			int requiredBandwidth = 0;
			Channel c = t.PathChannels.get(i);
			if (!c.isBandwidth_infinite()) {
				HashMap<Node, HashSet<SubexpressionTree>> directionalSubexps = new HashMap<>();
				for (Task tasks : attempt.getChannelUsage(c)) {
					Node source = tasks.pathChannelSource(c);
					HashSet<SubexpressionTree> subexps = directionalSubexps.get(source);
					if (subexps == null) {
						subexps = new HashSet<>();
						directionalSubexps.put(source, subexps);
					}
					subexps.add(tasks.subext);
				}
				for (HashSet<SubexpressionTree> subexpSet : directionalSubexps.values()) {
					requiredBandwidth += calculateBandwidth(subexpSet);
				}
				if (requiredBandwidth > c.getBandwidth()) {
					return false;
				}
			}
		}
		return true;
	}

	private int calculateBandwidth(HashSet<SubexpressionTree> subexpSet) {
		// TODO Auto-generated method stub
		int bandwidth = 0;
		for (SubexpressionTree subexp : subexpSet) {
			bandwidth += subexp.getBandwidth();
		}
		return bandwidth;
	}

	private int evaluateAvailableTasks(PathTree pt) {
		// TODO Strategy
		return 0;
	}


	public static void printTask(PathTree attempt, Task t, boolean success) {
		String offset = new String();
		for (int i = 0; i < attempt.solvedTasks.size(); i++) {
			offset += "  ";
		}
		String str;
		if (t.resolved) {
			if (t.subext.input) {
				Debug.out(19, offset + "Resolved input "
						+ ((Input) t.subext.getRequiredInputs().toArray()[0]).getIdentifier());
			} else {
				Debug.out(19,
						offset + "Resolved " + t.subext.getHead().getText() + " / " + t.subext.getOptTree().getText());
			}
			if (t.PathNodes.size() == 1) {
				str = "at ";
			} else {
				str = "via ";
			}
			str += t.PathNodes.get(0).getIdentifier();
			for (int i = 1; i < t.PathNodes.size(); i++) {
				str += " --" + t.PathChannels.get(i - 1).getIdentifier() + "-> " + t.PathNodes.get(i).getIdentifier();
			}
			Debug.out(19, offset + offset + str);
		} else {
			if (success) {
				str = "SUCC: ";
			} else {
				str = "FAIL: ";
			}
			if (t.subext.input) {
				Debug.out(19, offset + str + "Moved input "
						+ ((Input) t.subext.getRequiredInputs().toArray()[0]).getIdentifier());
			} else {
				Debug.out(19, offset + str + "Moved and splitted " + t.subext.getHead().getText() + " / "
						+ t.subext.getOptTree().getText());
			}
			if (t.subext.hasOptimalBandwidth()) {
				str = "with bandwidth " + t.subext.getBandwidth();
			} else {
				str = "with suboptimal bandwidth " + t.subext.getBandwidth() + " (opt: " + t.subext.getOptSubBandwidth()
						+ ")";
			}
			if (t.PathNodes.size() == 1) {
				str += " at ";
			} else {
				str += " via ";
			}
			str += t.PathNodes.get(0).getIdentifier();
			for (int i = 1; i < t.PathNodes.size(); i++) {
				str += " --" + t.PathChannels.get(i - 1).getIdentifier() + "-> " + t.PathNodes.get(i).getIdentifier();
			}
			Debug.out(19, offset + offset + str);
		}

	}

}
