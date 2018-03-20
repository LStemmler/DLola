package dlolaObject;

import java.text.ParseException;
import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import dlolaExprTree.DLolaType;
import main.DLolaRunner;
import main.Debug;
import main.Global;
import semanticAnalysis.SymbolTable;

public abstract class DLolaObject {
	final String identifier;

	public DLolaObject(ParseTree defnode, SymbolTable symbolTable)  {
		this.identifier = extractIdentifier(defnode);
		Debug.ensure (this.identifier != null, "Node Identifier null");
		Debug.ensure(symbolTable.getObject(identifier) == null, "Identifier " + identifier + " already exists");
		symbolTable.getIdentifierList().put(identifier, this);
	}

	// Optional Duplicate check, only relevant for triggers (checkDuplicate false),
	// use above version otherwise
	public DLolaObject(ParseTree defnode, SymbolTable symbolTable, boolean checkDuplicate)  {
		this.identifier = extractIdentifier(defnode);
		Debug.ensure(this.identifier != null, "Node Identifier null");
		if (checkDuplicate) {
			Debug.ensure(symbolTable.getObject(identifier) == null, "Identifier " + identifier + " already exists");
			symbolTable.getIdentifierList().put(identifier, this);
		}
	}

	public DLolaObject(String identifier, SymbolTable symbolTable)  {
		this.identifier = identifier;
		Debug.ensure (this.identifier != null, "Node Identifier null");
		Debug.ensure(symbolTable.getObject(identifier) == null, "Identifier " + identifier + " already exists");
		symbolTable.getIdentifierList().put(identifier, this);
	}
	
	public boolean equals(DLolaObject obj) {
		return (this.getClass() == obj.getClass() && this.identifier == obj.identifier);
	}

	public String getIdentifier() {
		return identifier;
	}

	public static String extractIdentifier(ParseTree defnode)  {
		String nodeType = Trees.getNodeText(defnode, Global.ruleNames);
		switch (nodeType) {
		case "constantDef":
		case "virtualDef":
		case "outputDef":
			return defnode.getChild(2).getChild(0).getText();
		case "nodeDef":
		case "channelDef":
			return defnode.getChild(1).getChild(0).getText();
		case "inputDef":
		case "triggerDef":
			Debug.error("Inputs and Triggers should use extractIdentifierList");
		default:
			Debug.error("Invalid node Type " + nodeType + " (Should not happen, check changes to DLola Syntax?)");
			return null;
		}
	}
	
	public static ArrayList<String> extractIdentifierList(ParseTree defnode)  {
		//Inputs and Triggers may have multiple identifiers
		String nodeType = Trees.getNodeText(defnode, Global.ruleNames);
		ArrayList<String> list = new ArrayList<>();
		int i=1;
		String id;
		switch (nodeType) {
		case "inputDef":
			i++;
		case "triggerDef":
			while (i < defnode.getChildCount()) {
				id = defnode.getChild(i).getChild(0).getText();
				list.add(id);
				i+=2;
			}
			return list;
		default:
			list.add(extractIdentifier(defnode));
		}
		return list;
	}

	protected static DLolaType getType(ParseTree defnode)  {
		String nodeType = Trees.getNodeText(defnode, Global.ruleNames);
		switch (nodeType) {
		case "constantDef":
		case "virtualDef":
		case "inputDef":
		case "outputDef":
			String type = defnode.getChild(1).getChild(0).getText();
			switch (type) {
			case "int":
				return DLolaType.INT;
			case "bool":
				return DLolaType.BOOL;
			default:
				Debug.error("Invalid type " + type + " (Should not happen, check changes to DLola Syntax?)");
			}
		default:
			Debug.error("NodeType " + nodeType + " has no type");
			return null;
		}
	}
	
	protected static String getParentNodeIdentifier(ParseTree defnode) {
		String nodeType = Trees.getNodeText(defnode, Global.ruleNames);
		if (nodeType.equals("nodeDef")) {
			return extractIdentifier(defnode);
		} else {
			return getParentNodeIdentifier(defnode.getParent());
		}
	}
}
