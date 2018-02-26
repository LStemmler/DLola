package dlolaObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import semanticAnalysis.DLolaType;
import semanticAnalysis.SymbolTable;

public class Input extends DLolaObject {

	DLolaType type;
	ArrayList<Node> nodeList = new ArrayList<Node>();

	public Input(String identifier, ParseTree defnode, SymbolTable symbolTable) throws ParseException {
		super(identifier, symbolTable);
		this.type = getType(defnode);
		String parentIdent = DLolaObject.getParentNodeIdentifier(defnode);
		Node parentNode = (Node) symbolTable.getObject(parentIdent);
		nodeList.add(parentNode);
		parentNode.addInput(this);
	}

	public void merge(ParseTree inputDef, SymbolTable symbolTable) throws ParseException {
		DLolaType inputType = getType(inputDef);
		ensure(inputType == type, "Differing types " + inputType + " and " + type + " for input " + identifier);
		String parentIdent = DLolaObject.getParentNodeIdentifier(inputDef);
		Node parentNode = (Node) symbolTable.getObject(parentIdent);
		nodeList.add(parentNode);
		parentNode.addInput(this);
	}

	public DLolaType getType() {
		return type;
	}
	
	public ArrayList<Node> getNodeList() {
		return nodeList;
	}

}
