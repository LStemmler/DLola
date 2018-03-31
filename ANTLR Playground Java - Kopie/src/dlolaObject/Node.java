package dlolaObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;
import org.graphstream.graph.implementations.MultiNode;

import main.DLolaRunner;
import main.Debug;
import main.Global;
import routeGeneration.RelevantSubgraph;
import semanticAnalysis.SymbolTable;

public class Node extends DLolaObject {

	MultiNode node;
	ArrayList<Input> inputList = new ArrayList<Input>();
	ArrayList<Input> inputDependencies = new ArrayList<Input>();
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

	public Set<Input> getRelevantInputs() {
		return relevantSubgraph.getReachableInputs();
	}

	public RelevantSubgraph getRelevantSubgraph() {
		return relevantSubgraph;
	}
	
	public boolean inRelevantSubgraph(Input in) {
		return relevantSubgraph.getReachableInputs().contains(in);
	}

	public void generateRelevantSubgraph() {
		relevantSubgraph = new RelevantSubgraph(this);
	}

	public ArrayList<Input> getInputDependencies() {
		return inputDependencies;
	}

	public boolean addInputDependency(Input in) {
		if (!inputDependencies.contains(in)) {
			inputDependencies.add(in);
			return true;
		}
		return false;
	}

	public boolean hasInputDependency(Input in) {
		return inputDependencies.contains(in);
	}

	public boolean addInputDependencies(ArrayList<Input> dependencies) {
		boolean changed = false;
		if (dependencies != null) {
			for (Input in : dependencies) {
				addInputDependency(in);
			}
		}
		return changed;
	}

	public void ensureReachability() throws ParseException {
		for (Input in : inputDependencies) {
			try {
				Debug.ensure(relevantSubgraph.getReachableInputs().contains(in),
						"Node " + identifier + " requires unreachable input " + in.identifier);
			} catch (NullPointerException e) {
				Debug.error("Node " + identifier + " requires unreachable input " + in.identifier);
			}
		}
	}

}