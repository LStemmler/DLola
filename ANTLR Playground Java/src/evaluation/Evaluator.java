package evaluation;

import java.util.ArrayList;

import dlolaObject.Output;
import main.Debug;
import main.Global;
import routeGeneration.PathTree;
import routeGeneration.RouteBuilder;
import routeGeneration.Solution;

public class Evaluator {

	public ArrayList<Solution> generatedRoutes;
	public ArrayList<ArrayList<Solution>> equivalenceClasses = new ArrayList<>();
	public ArrayList<Integer> equivalenceClassMembers = new ArrayList<>();

	public Evaluator() {
		this.generatedRoutes = Global.routeBuilder.generatedRoutes;
	}

	public void evaluate() {
		if (generatedRoutes.size() == 0) {
			Debug.out(11, "Route evaluation cancelled as no routes were found");
			return;
		}

		Debug.out(11, "Evaluating Routes");
		ArrayList<Solution> filteredRoutes = paretoFilter(generatedRoutes);
		Debug.out(12, filteredRoutes.size() + " Routes remain after pareto pruning");
		equivalenceClasses(filteredRoutes);
		Debug.out(12, equivalenceClasses.size() + " Delay-based equivalence classes");

		if (Global.debugVerbosity >= 13)
			printDelayLimits();
		
		Debug.out(11, "Route evaluation complete");
	}

	// Find non-dominated solutions
	private ArrayList<Solution> paretoFilter(ArrayList<Solution> solutions) {

		ArrayList<Solution> remainingSolutions = new ArrayList<>();
		for (ArrayList<Solution> eclass: equivalenceClasses) {
			remainingSolutions.add(eclass.get(0));
		}
		
		
		// Ensure that no multiples of equivalence classes will be added
		remainingSolutions.addAll(solutions);
		
		
		
		boolean changed = true;
		while (changed) {
			boolean[] dominated = new boolean[remainingSolutions.size()];
			changed = false;

			for (int i = 0; i < remainingSolutions.size(); i++) {
				for (int j = i + 1; j < remainingSolutions.size(); j++) {
					if (dominated[i])
						break;
					if (!dominated[j])
						changed = changed | paretoCompare(remainingSolutions, i, j, dominated);
				}
			}

			for (int i = remainingSolutions.size() - 1; i >= equivalenceClasses.size(); i--) {
				if (dominated[i])
					remainingSolutions.remove(i);
			}
			for (int i = equivalenceClasses.size() - 1; i >=0; i--) {
				if (dominated[i]) {
					remainingSolutions.remove(i);
					equivalenceClasses.remove(i);
					equivalenceClassMembers.remove(i);
				}
			}

		}
		
		return remainingSolutions;
	}

	// returns true if either is dominated
	private boolean paretoCompare(ArrayList<Solution> remainingSolutions, int index1, int index2, boolean[] dominated) {
		Solution sol1 = remainingSolutions.get(index1);
		Solution sol2 = remainingSolutions.get(index2);
		boolean sol1dominated = true;
		boolean sol2dominated = true;

		for (int i = 0; i < sol1.OutputDelays.length; i++) {
			sol1dominated = sol1dominated && (sol1.OutputDelays[i] >= sol2.OutputDelays[i]);
			sol2dominated = sol2dominated && (sol2.OutputDelays[i] >= sol1.OutputDelays[i]);
			if (!sol1dominated && !sol2dominated)
				return false;
		}
		if (sol1dominated && sol2dominated)
			return false;
		if (sol1dominated)
			dominated[index1] = true;
		if (sol2dominated)
			dominated[index2] = true;
		return true;
	}

	// Places solutions in equivalence classes where all delays are equal
	private ArrayList<ArrayList<Solution>> equivalenceClasses(ArrayList<Solution> solutions) {

		for (int i = 0; i < solutions.size(); i++) {

			Solution sol = solutions.get(i);

			boolean equivalent = false;
			for (int eclass = 0; eclass < equivalenceClasses.size(); eclass++) {
				equivalent = true;
				Solution cmp = equivalenceClasses.get(eclass).get(0);
				for (int output = 0; output < sol.OutputDelays.length; output++) {
					equivalent &= (sol.OutputDelays[output] == cmp.OutputDelays[output]);
				}
				if (equivalent) {
					// equivalent, put into equivalence class eclass
					if (Global.storeMultipleEquivalentSolutions) {
						equivalenceClasses.get(eclass).add(sol);
					}
					equivalenceClassMembers.set(eclass, equivalenceClassMembers.get(eclass) + 1);
					break;
				}
			}
			// unequal, new equivalence class
			if (!equivalent) {
				ArrayList<Solution> newEqClass = new ArrayList<>();
				newEqClass.add(sol);
				equivalenceClasses.add(newEqClass);
				equivalenceClassMembers.add(1);
			}
		}

		return equivalenceClasses;
	}

	private void printDelayLimits() {
		String offset = "  ";
		Debug.out(13, "Evaluation results:");

		for (int i = 0; i < Global.symtable.getOutputList().size(); i++) {
			Output out = Global.symtable.getOutputList().get(i);

			int minDelay = Integer.MAX_VALUE;
			int maxDelay = Integer.MIN_VALUE;
			for (int eclass = 0; eclass < equivalenceClasses.size(); eclass++) {
				Solution sol = equivalenceClasses.get(eclass).get(0);
				minDelay = Math.min(minDelay, sol.OutputDelays[i]);
				maxDelay = Math.max(maxDelay, sol.OutputDelays[i]);
			}

			if (minDelay == Global.STAT_DELAY) {
				if (maxDelay == Global.STAT_DELAY) {
					Debug.out(13, offset + "Output " + out.getIdentifier() + " at node "
							+ out.getParentNode().getIdentifier() + " is statically calculable.");
				} else {
					Debug.err(5,
							offset + "Output " + out.getIdentifier() + " at node " + out.getParentNode().getIdentifier()
									+ " may be statically calculated or have a delay up to " + maxDelay);
					Debug.err(5, offset + "Statically calculable outputs should always be statically calculable");
				}
			} else {
				if (minDelay != maxDelay) {
					Debug.out(13,
							offset + "Output " + out.getIdentifier() + " at node " + out.getParentNode().getIdentifier()
									+ " may have a delay between " + minDelay + " and " + maxDelay);
				} else {
					Debug.out(13, offset + "Output " + out.getIdentifier() + " at node "
							+ out.getParentNode().getIdentifier() + " has a delay of " + minDelay);
				}
			}
		}
	}

	public void printEquivalenceClassDetails() {
		String offset = "  ";
		Debug.out(13, "Equivalence class details:");
		for (int eclass = 1; eclass < equivalenceClasses.size(); eclass++) {

			Solution sol = equivalenceClasses.get(eclass).get(0);
			Debug.out(13, offset + "Equivalence class " + eclass + ":");
			for (int i = 0; i < Global.symtable.getOutputList().size(); i++) {
				Output out = Global.symtable.getOutputList().get(i);
				int delay = sol.OutputDelays[i];
				if (delay == Global.STAT_DELAY) {
					Debug.out(13, offset + "Output " + out.getIdentifier() + " at node "
							+ out.getParentNode().getIdentifier() + " is statically calculable.");
				} else {
					Debug.out(13, offset + "Output " + out.getIdentifier() + " at node "
							+ out.getParentNode().getIdentifier() + " has a delay of " + delay);
				}
			}
			Debug.out(14, "");
			Debug.out(14, "Detailed solution:");
			sol.printSolution(14);
			Debug.out(14, "");
		}
	}
	
}
