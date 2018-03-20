package dlolaObject;

import java.text.ParseException;
import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;

import dlolaExprTree.DLolaType;
import main.Debug;
import main.Global;
import routeGeneration.SubexpressionTree;
import semanticAnalysis.SymbolTable;

public class Output extends DLolaExpTreeObject {

	private Node parentNode;
	ArrayList<Input> inputDependencies = new ArrayList<Input>();
	Boolean trigger = false;

	public Output(ParseTree defnode, SymbolTable symbolTable) throws ParseException {
		super(defnode, symbolTable);
		String parentIdent = DLolaObject.getParentNodeIdentifier(defnode);
		parentNode = (Node) symbolTable.getObject(parentIdent);
		getParentNode().addOutput(this);
	}

	public void addTrigger() throws ParseException {
		Debug.ensure(type == DLolaType.BOOL, "Trigger on non-boolean output " + identifier);
		Debug.ensure(!trigger, "Multiple triggers on output " + identifier);
		this.trigger = true;
		getParentNode().triggerList.add(this);
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

	public Node getParentNode() {
		return parentNode;
	}

	public SubexpressionTree getSubexpressionTree() {
		return Global.systemModel.getSubexpTree(expressionTree);
	}

}
