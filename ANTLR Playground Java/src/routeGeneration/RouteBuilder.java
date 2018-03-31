package routeGeneration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dlolaExprTree.ExprSection;
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
	int[] workloadProgress = new int[30];
	int[] workloadPrediction = new int[30];

	public RouteBuilder() {
		Debug.out(12, "Initializing RouteBuilder");
		initialTree = new PathTree();
		Debug.out(12, "RouteBuilder initialized");
	}

	public void generateRoutes() {
		Debug.out(11, "Generating Routes");
		routeGeneration(initialTree);
		solutionCounter += generatedRoutes.size();
		
		
		double progress = approximateProgress();

	    long currentTime = System.currentTimeMillis();
		long runtime = currentTime - Global.startTime;

		Debug.out(12, solutionCounter + " Routes generated.");
		if (solutionCounter == 0) {
			Debug.err(4, "Could not find a non-starving route");
		}
		Debug.out(11, "Route generation complete. Elapsed time: "+Global.timeString(runtime));
		//Elapsed time: "+runtime.getHours()+":"+runtime.getMinutes()+":"+runtime.getSeconds()
	}

	private void routeGeneration(PathTree pt) {
		if (pt.taskList.isEmpty()) {
			if (pt.finalCalculations()) {
				Solution sol = new Solution(pt);
				generatedRoutes.add(sol);
				Debug.out(15, solutionCounter + generatedRoutes.size() + " Routes generated");
				if (generatedRoutes.size() % 10000 == 0) {
					solutionCounter += generatedRoutes.size();
					
					double progress = approximateProgress();
					
				    long currentTime = System.currentTimeMillis();
					long runtime = currentTime - Global.startTime;
					long expectedRuntime = (long) (runtime *((1-progress) / progress));
					
					
					String approximateProgress = String.format("%.2f", 100 * progress);
					Debug.out(12, solutionCounter + " Routes generated. Approximately " + approximateProgress + "% progress");
					Debug.out(13, "Elapsed time "+Global.timeString(runtime) + ", ETC "+ Global.timeString(expectedRuntime));
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

		for (int i = 0; i<optionArray.size(); i++) {
			org.graphstream.graph.Node option = optionArray.get(i);
			if (pt.solvedTasks.size()<30) {
				workloadProgress[pt.solvedTasks.size()] = i;
				workloadPrediction[pt.solvedTasks.size()] = optionArray.size();
			}
			
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
		for (int i = 0; i < t.PathChannels.size(); i++) {
			int requiredBandwidth = 0;
			Channel c = t.PathChannels.get(i);
			if (!c.isBandwidth_infinite()) {
				HashMap<Node, HashSet<ExprSection>> directionalSectionMoves = new HashMap<>();
				for (Task tasks : attempt.getChannelUsage(c)) {
					Node source = tasks.pathChannelSource(c);
					HashSet<ExprSection> subexps = directionalSectionMoves.get(source);
					if (subexps == null) {
						subexps = new HashSet<>();
						directionalSectionMoves.put(source, subexps);
					}
					subexps.add(tasks.exprSec);
				}
				for (HashSet<ExprSection> sectionSet : directionalSectionMoves.values()) {
					requiredBandwidth += calculateBandwidth(sectionSet);
				}
				if (requiredBandwidth > c.getBandwidth()) {
					return false;
				}
			}
		}
		return true;
	}

	private int calculateBandwidth(HashSet<ExprSection> sectionSet) {
		// TODO Auto-generated method stub
		int bandwidth = 0;
		for (ExprSection section : sectionSet) {
			bandwidth += section.getOptSize();
		}
		return bandwidth;
	}

	private int evaluateAvailableTasks(PathTree pt) {
		int options= Integer.MAX_VALUE;
		int bestIndex=0;
		for (int i = 0; i<pt.taskList.size(); i++) {
			Task t = pt.taskList.get(i);
			int tOptions = t.evaluateAvailableOptions(pt).size();
			if (tOptions < options) {
				options = tOptions;
				bestIndex = i;
				if (options==1) break;
			}
		}
		return bestIndex;
	}


	public static void printTask(PathTree attempt, Task t, boolean success) {
		String offset = new String();
		for (int i = 0; i < attempt.solvedTasks.size(); i++) {
			offset += "  ";
		}
		String str;
		if (t.resolved) {
			Debug.out(19,
						offset + "Resolved " + t.exprSec.getHead().toString() + " / " + t.exprSec.getOpt().toString());
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
			Debug.out(19, offset + str + "Moved and splitted " + t.exprSec.getHead().toString() + " / "
						+ t.exprSec.getOpt().toString());
			str = "with bandwidth " + t.exprSec.getOptSize();
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
	
	public double approximateProgress() {
		double fraction = 0;
		for (int i=29; i>=0; i--) {
			fraction = (workloadProgress[i] + fraction)/workloadPrediction[i];
		}
		return fraction;
	}
	
	
	

}
