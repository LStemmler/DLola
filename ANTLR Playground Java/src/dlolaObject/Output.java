package dlolaObject;

import java.text.ParseException;
import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import dlolaExprTree.DLolaExpr;
import dlolaExprTree.DLolaType;
import dlolaExprTree.ExprSection;
import dlolaExprTree.ExpressionMap;
import main.Debug;
import main.Global;
import semanticAnalysis.SymbolTable;

public class Output extends DLolaExpTreeObject {

	private Node parentNode;
	ArrayList<Input> inputDependencies = new ArrayList<Input>();
	boolean trigger = false;
	boolean essential = true;
	DLolaExpr expression;
	ExprSection section;

	public Output(ParseTree defnode, SymbolTable symbolTable) throws ParseException {
		super(defnode, symbolTable);
		String parentIdent = DLolaObject.getParentNodeIdentifier(defnode);
		parentNode = (Node) symbolTable.getObject(parentIdent);
		getParentNode().addOutput(this);
		String outputType = Trees.getNodeText(defnode.getChild(0), Global.ruleNames);
		switch (outputType) {
			case "uoutput": essential = false;
			case "output":
			default:
		}
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
	
	public boolean isEssential() {
		return essential;
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

	public DLolaExpr getExpression() {
		if (expression == null) expression = ExpressionMap.getExpression(expressionTree);
		return expression;
	}

	public ExprSection getExprSection() {
		if (section == null) section = ExpressionMap.getExprSection(ExpressionMap.getExpression(expressionTree));
		return section;
	}

}
