package dlolaObject;

import java.text.ParseException;
import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import main.DLolaRunner;
import semanticAnalysis.DLolaType;
import semanticAnalysis.SymbolTable;

public abstract class DLolaObject {
	final String identifier;

	public DLolaObject(ParseTree defnode, SymbolTable symbolTable) throws ParseException {
		this.identifier = extractIdentifier(defnode);
		ensure (this.identifier != null, "Node Identifier null");
		ensure(symbolTable.getObject(identifier) == null, "Identifier " + identifier + " already exists");
		symbolTable.getIdentifierList().put(identifier, this);
	}

	// Optional Duplicate check, only relevant for triggers (checkDuplicate false),
	// use above version otherwise
	public DLolaObject(ParseTree defnode, SymbolTable symbolTable, boolean checkDuplicate) throws ParseException {
		this.identifier = extractIdentifier(defnode);
		ensure(this.identifier != null, "Node Identifier null");
		if (checkDuplicate) {
			ensure(symbolTable.getObject(identifier) == null, "Identifier " + identifier + " already exists");
			symbolTable.getIdentifierList().put(identifier, this);
		}
	}

	public DLolaObject(String identifier, SymbolTable symbolTable) throws ParseException {
		this.identifier = identifier;
		ensure (this.identifier != null, "Node Identifier null");
		ensure(symbolTable.getObject(identifier) == null, "Identifier " + identifier + " already exists");
		symbolTable.getIdentifierList().put(identifier, this);
	}
	
	public boolean equals(DLolaObject obj) {
		return (this.getClass() == obj.getClass() && this.identifier == obj.identifier);
	}

	public String getIdentifier() {
		return identifier;
	}

	public static String extractIdentifier(ParseTree defnode) throws ParseException {
		String nodeType = Trees.getNodeText(defnode, DLolaRunner.ruleNames);
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
			error("Inputs and Triggers should use extractIdentifierList");
		default:
			error("Invalid node Type " + nodeType + " (Should not happen, check changes to DLola Syntax?)");
			return null;
		}
	}
	
	public static ArrayList<String> extractIdentifierList(ParseTree defnode) throws ParseException {
		//Inputs and Triggers may have multiple identifiers
		String nodeType = Trees.getNodeText(defnode, DLolaRunner.ruleNames);
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

	protected static DLolaType getType(ParseTree defnode) throws ParseException {
		String nodeType = Trees.getNodeText(defnode, DLolaRunner.ruleNames);
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
				error("Invalid type " + type + " (Should not happen, check changes to DLola Syntax?)");
			}
		default:
			error("NodeType " + nodeType + " has no type");
			return null;
		}
	}
	
	protected static String getParentNodeIdentifier(ParseTree defnode) throws ParseException {
		String nodeType = Trees.getNodeText(defnode, DLolaRunner.ruleNames);
		if (nodeType.equals("nodeDef")) {
			return extractIdentifier(defnode);
		} else {
			return getParentNodeIdentifier(defnode.getParent());
		}
	}
	

	protected static void ensure(boolean check, String errorMsg) throws ParseException {
		if (!check)
			throw new ParseException(errorMsg, 0);
	}

	protected static void error(String errorMsg) throws ParseException {
		throw new ParseException(errorMsg, 0);
	}
}
