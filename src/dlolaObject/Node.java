package dlolaObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;

import org.antlr.v4.runtime.tree.ParseTree;
import org.graphstream.graph.implementations.MultiNode;

import dlolaExprTree.DLolaExpr;
import dlolaExprTree.ExprSection;
import dlolaExprTree.ExpressionMap;
import dlolaExprTree.IdExpr;
import main.Debug;
import main.Global;
import routeGeneration.RelevantSubgraph;
import routeGeneration.Solution;
import routeGeneration.Task;
import semanticAnalysis.SymbolTable;

public class Node extends DLolaObject {

	MultiNode node;
	ArrayList<Input> inputList = new ArrayList<Input>();
	ArrayList<Input> inputDependencies = new ArrayList<Input>();
	ArrayList<Input> nonessentialInputDependencies = new ArrayList<Input>();
	HashSet<Input> relevantInputs = new HashSet<Input>();
	HashSet<Input> nonessentialRelevantInputs = new HashSet<Input>();
	RelevantSubgraph relevantSubgraph;
	ArrayList<Output> outputList = new ArrayList<Output>();
	ArrayList<Output> triggerList = new ArrayList<Output>();

	public Node(ParseTree defnode, SymbolTable symbolTable) throws ParseException {
		super(defnode, symbolTable);
		node = Global.systemModel.getNetworkGraph().addNode(identifier);
		node.setAttribute("Node", this);
	}

	public MultiNode getGraphNode() {
		return node;
	}

	public ArrayList<Input> getInputList() {
		return inputList;
	}

	public ArrayList<Output> getOutputList() {
		return outputList;
	}

	public ArrayList<Output> getTriggerList() {
		return triggerList;
	}

	public void addInput(Input input) throws ParseException {
		Debug.ensure(!inputList.contains(input),
				"Multiple instances of input " + input.identifier + " at node " + identifier);
		inputList.add(input);
	}

	public void addOutput(Output output) throws ParseException {
		Debug.ensure(!outputList.contains(output),
				"Multiple instances of output " + output.identifier + " at node " + identifier);
		outputList.add(output);
	}

	public void addTrigger(Output triggerOutput) throws ParseException {
		Debug.ensure(!triggerList.contains(triggerOutput),
				"Multiple instances of trigger " + triggerOutput.identifier + " at node " + identifier);
		triggerList.add(triggerOutput);
	}

	public HashSet<Input> getRelevantInputs() {
		return relevantInputs;
	}

	public HashSet<Input> getUnreliableRelevantInputs() {
		return nonessentialRelevantInputs;
	}

	public void addRelevantInput(Input in) {
		relevantInputs.add(in);
	}

	public void addUnreliableRelevantInputs(Input in) {
		nonessentialRelevantInputs.add(in);
	}

	public RelevantSubgraph getRelevantSubgraph() {
		return relevantSubgraph;
	}

	public boolean inRelevantSubgraph(Input in) {
		return relevantSubgraph.getReachableInputs().contains(in);
	}

	public boolean inUnreliableRelevantSubgraph(Input in) {
		return relevantSubgraph.getUnreliablyReachableInputs().contains(in);
	}

	public void generateRelevantSubgraph() {
		relevantSubgraph = new RelevantSubgraph(this);
	}

	public ArrayList<Input> getInputDependencies() {
		return inputDependencies;
	}

	public ArrayList<Input> getNonessentialInputDependencies() {
		return nonessentialInputDependencies;
	}

	public boolean addInputDependency(Input in, boolean essential) {
		if (!inputDependencies.contains(in)) {
			if (essential) {
				inputDependencies.add(in);
				if (nonessentialInputDependencies.contains(in)) {
					nonessentialInputDependencies.remove(in);
				}
				return true;
			} else {
				if (!nonessentialInputDependencies.contains(in)) {
					nonessentialInputDependencies.add(in);
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasInputDependency(Input in) {
		return inputDependencies.contains(in);
	}

	public boolean hasNonessentialInputDependency(Input in) {
		return nonessentialInputDependencies.contains(in);
	}

	public boolean addInputDependencies(Output out) {
		ArrayList<Input> dependencies = out.getInputDependencies();
		boolean essential = out.isEssential();
		boolean changed = false;
		if (dependencies != null) {
			for (Input in : dependencies) {
				addInputDependency(in, essential);
			}
		}
		return changed;
	}

	public void ensureReachability() throws ParseException {
		for (Input in : inputDependencies) {
			try {
				if (relevantSubgraph.getUnreliablyReachableInputs().contains(in)) {
					Debug.ensure(relevantSubgraph.getReachableInputs().contains(in), "Node " + identifier
							+ " requires essential input " + in.identifier + " which is not reliably reachable");
				} else {
					Debug.ensure(relevantSubgraph.getReachableInputs().contains(in),
							"Node " + identifier + " requires unreachable input " + in.identifier);
				}
			} catch (NullPointerException e) {
				Debug.error("Node " + identifier + " requires unreachable input " + in.identifier);
			}
		}
		for (Input in : nonessentialInputDependencies) {
			try {
				Debug.ensure(
						relevantSubgraph.getReachableInputs().contains(in)
								|| relevantSubgraph.getUnreliablyReachableInputs().contains(in),
						"Node " + identifier + " requires unreachable nonessential input " + in.identifier);
			} catch (NullPointerException e) {
				Debug.error("Node " + identifier + " requires unreachable nonessential input " + in.identifier);
			}
		}
	}

	public String generateNodeSolution(Solution sol, ArrayList<Task> tasklist) {
		String newline = Global.newline;
		String nodestring = "Node " + identifier + ":" + newline;
		String receivestring = "";
		String sendstring = "";
		String generatestring = "";
		String outputstring = "";
		String inputstring = "";

		for (Input in : inputList) {
			inputstring += "Receive input " + in.identifier + newline;
		}
		for (Output out : outputList) {
			int delay = sol.getPathTree().getAvailableExprSectionDelay(this, out.getExprSection(), !out.essential);
			if (delay == Global.STAT_DELAY) {
				outputstring += "Output statically calculable expression \""+out.identifier + " := "+out.getExprSection().getHead().toCode()+"\""+newline;
			} else if (out.essential) {
				outputstring += "Output \""+out.identifier + " := "+out.getExprSection().getHead().toCode()+"\" with delay " +delay + newline;
			} else {
				outputstring += "Output \""+out.identifier + " := "+out.getExprSection().getHead().toCode()+"\" nonessential with delay " +delay + newline;
			}
		}

		for (Task t : tasklist) {
			int pathIndex = t.PathNodes.indexOf(this);

			ExprSection exprSec = t.exprSec;
			boolean receive = false;
			boolean send = false;
			boolean generateOpt = false;
			boolean generateHead = false;
			boolean headOpt = exprSec.getHead() == exprSec.getOpt();
			int delay = sol.getPathTree().getAvailableExprSectionDelay(this, exprSec, !t.essential);
			
			
			ArrayList<DLolaExpTreeObject> headObjects = ExpressionMap.getObjects(exprSec.getHead());
			String identifierString = "";
			boolean identifiedExpression = headObjects != null && headObjects.size() > 0;
			if (identifiedExpression) {
				if (headObjects.size() == 1) {
					identifierString = " (id: "+headObjects.get(0).identifier+")";
				} else {
					identifierString = " (ids: "+headObjects.get(0).identifier;
					
					for (int i=1; i<headObjects.size(); i++) {
						identifierString += ", "+ headObjects.get(i).identifier;
					}
							
							
					identifierString += ")";
				}
			}

			if (pathIndex == 0) {
				// First Node
				if (t.PathNodes.size() == 1) {
					//Expression is not transmitted
					if (identifiedExpression) {
						generateHead = true;
					} else {
						continue;
					}
				} else {
					generateOpt = true;
					send = true;
				}
			} else if (pathIndex == t.PathNodes.size() - 1) {
				// Last Node
				receive = true;
				generateHead = !headOpt;
			} else {
				// Middle Node
				receive = true;
				send = true;
			}

			if (receive) {
				if (t.essential) {
					receivestring += "Receive"; //esssential expression
				} else {
					receivestring += "Receive nonessential expression";
				}
				if (headOpt) {
					receivestring += identifierString+" \"" + exprSec.getOpt().toCode() +  "\" from "+t.PathChannels.get(pathIndex-1).channelType+" "
							+ t.PathChannels.get(pathIndex - 1).identifier + " with delay " + delay + newline;
				} else {
					receivestring += " \"" + exprSec.getOpt().toCode() + "\" as optimal subexpression for \"" 
							+ exprSec.getOpt().toCode() +  "\""+ identifierString+" from "+t.PathChannels.get(pathIndex-1).channelType+" "
							+ t.PathChannels.get(pathIndex - 1).identifier + " with delay " + delay + newline;
				}
			}
			if (send) {
				if (t.essential) {
					sendstring += "Send";
				} else {
					sendstring += "Send nonessential expression";
				}
				if (headOpt) {
					sendstring += identifierString+" \"" + exprSec.getOpt().toCode() + "\" over "+t.PathChannels.get(pathIndex).channelType+" "
							+ t.PathChannels.get(pathIndex).identifier + " with delay " + delay + newline;
				} else {
					sendstring += " \"" + exprSec.getOpt().toCode() + "\" as optimal subexpression for \"" 
							+ exprSec.getOpt().toCode() +  "\""+ identifierString+" over "+t.PathChannels.get(pathIndex).channelType+" "
							+ t.PathChannels.get(pathIndex).identifier + " with delay " + delay + newline;
				}
			}
			if(generateHead) {
				if (t.essential) {
					generatestring += "Generate";
				} else {
					generatestring += "Generate nonessential expression";
				}
				if (exprSec.getHead() instanceof IdExpr) {
					DLolaExpr altHead = exprSec.getHead();
					try {
						altHead = exprSec.getRepresentedRelevantExpressions().get(1);
					} catch (IndexOutOfBoundsException e) {}
					if (!headOpt && !exprSec.getOpt().equals(altHead)) {
						generatestring += identifierString+" \"" + altHead.toCode() +  "\" from expression \""
								+ exprSec.getOpt().toCode() + "\" with delay " + delay + newline;
					}
					generatestring += identifierString+" \"" + altHead.toCode() + "\" with delay " + delay + newline;
				} else if (headOpt) {
					generatestring += identifierString+" \"" + exprSec.getHead().toCode() + "\" with delay " + delay + newline;
				} else {
					generatestring += identifierString+" \"" + exprSec.getHead().toCode() +  "\" from expression \""
							+ exprSec.getOpt().toCode() + "\" with delay " + delay + newline;
				}
			}
			if(generateOpt) {
				if (t.essential) {
					generatestring += "Generate";
				} else {
					generatestring += "Generate nonessential expression";
				}
				if (headOpt || exprSec.isRecursive()) {
					generatestring += identifierString+" \"" + exprSec.getOpt().toCode() + "\" with delay " + delay + newline;
				} else {
					generatestring += identifierString+" \"" + exprSec.getOpt().toCode() + "\" with delay " + delay + newline;
				}
			}

		}

		nodestring += inputstring +receivestring +generatestring+sendstring+ outputstring;
		
		
		return nodestring;
	}

}
