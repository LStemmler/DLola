package dlolaObject;

import java.text.ParseException;
import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;

import semanticAnalysis.DLolaType;
import semanticAnalysis.SymbolTable;

public class Output extends DLolaExpTreeObject {

	Node parentNode;
	ArrayList<Input> inputDependencies = new ArrayList<Input>();
	Boolean trigger = false;

	public Output(ParseTree defnode, SymbolTable symbolTable) throws ParseException {
		super(defnode, symbolTable);
		String parentIdent = DLolaObject.getParentNodeIdentifier(defnode);
		parentNode = (Node) symbolTable.getObject(parentIdent);
		parentNode.addOutput(this);
	}

	public void addTrigger() throws ParseException {
		ensure(type == DLolaType.BOOL, "Trigger on non-boolean output " + identifier);
		ensure(!trigger, "Multiple triggers on output " + identifier);
		this.trigger = true;
		parentNode.triggerList.add(this);
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

}
