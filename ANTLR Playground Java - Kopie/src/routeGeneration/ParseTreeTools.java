package routeGeneration;

import java.util.ArrayList;
import java.util.HashSet;

import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import dlolaExprTree.DLolaType;
import dlolaObject.DLolaExpTreeObject;
import dlolaObject.DLolaObject;
import dlolaObject.Input;
import main.Debug;
import main.Global;
import semanticAnalysis.UntypedException;

public abstract class ParseTreeTools {

	


	public static boolean equals(ParseTree tree1, ParseTree tree2) {
		String node1Type = Utils.escapeWhitespace(Trees.getNodeText(tree1, Global.ruleNames), false);
		String node2Type = Utils.escapeWhitespace(Trees.getNodeText(tree1, Global.ruleNames), false);

		// ignore empty expression nodes
		if (tree1.getChildCount() == 1) {
			switch (node1Type) {
			case "expression":
			case "equivExpr":
			case "impExpr":
			case "orExpr":
			case "andExpr":
			case "negExpr":
			case "equExpr":
			case "comparExpr":
			case "addExpr":
			case "subExpr":
			case "multExpr":
			case "expExpr":
			case "restrExpr":
			}
		}
		if (tree2.getChildCount() == 1) {
			switch (node2Type) {
			case "expression":
			case "equivExpr":
			case "impExpr":
			case "orExpr":
			case "andExpr":
			case "negExpr":
			case "equExpr":
			case "comparExpr":
			case "addExpr":
			case "subExpr":
			case "multExpr":
			case "expExpr":
			case "restrExpr":
				return equals(tree1, tree2.getChild(0));
			}
		}
		if (node1Type.equals("expression") && tree1.getChildCount()==3) 
			return equals(tree1.getChild(0), tree2);
		if (node2Type.equals("expression") && tree2.getChildCount()==3) 
			return equals(tree1, tree2.getChild(0));
		
		
		
		
		
		// Equal expression types
		if (node1Type.equals(node2Type)) {
			switch (node1Type) {
			case "expression":
			case "equivExpr":
			case "impExpr":
			case "orExpr":
			case "andExpr":
			case "negExpr":
			case "equExpr":
			case "comparExpr":
			case "addExpr":
			case "subExpr":
			case "multExpr":
			case "expExpr":
			case "restrExpr":
			case "ifExpr":
			case "elseExpr":
			case "shiftExpr":
			case "identifier":
			case "literal":
			}
		}
		
		
			
			
			

		return false;
	}
	
	
	
	

	public static DLolaType exprType(ParseTree tree) throws UntypedException {
		String nodeType = Utils.escapeWhitespace(Trees.getNodeText(tree, Global.ruleNames), false);

		switch (nodeType) {
		case "equivExpr":
		case "impExpr":
		case "orExpr":
		case "andExpr":
		case "negExpr":
		case "equExpr":
		case "comparExpr":
			return DLolaType.BOOL;
		case "addExpr":
		case "subExpr":
		case "multExpr":
		case "expExpr":
			return DLolaType.INT;
		case "ifExpr":
			return exprType(tree.getChild(3));
		case "elseExpr":
			if (tree.getChild(0).getText().equals("else"))
				return exprType(tree.getChild(2));
			return exprType(tree.getChild(3));
		case "restrExpr":
		case "shiftExpr":
			return exprType(tree.getChild(0));
		case "identifier":
			return Global.symtable.getType(tree.getChild(0).getText());
		case "expression":
			if (tree.getChild(0).getText().equals("(")) {
				return exprType(tree.getChild(1));
			}
			return exprType(tree.getChild(0));
		case "literal":
			String childType = Utils.escapeWhitespace(Trees.getNodeText(tree.getChild(0), Global.ruleNames), false);
			if (childType.equals("integer")) {
				return DLolaType.INT;
			} else {
				return DLolaType.BOOL;
			}
		default:
			throw new UntypedException("getType on typeless Node " + nodeType);
		}
	}

	// Finds the next branch in the input dependency tree and returns the split
	// subtrees
	public static ArrayList<ParseTree> split(ParseTree tree) {
		HashSet<Input> reqs = Global.systemModel.getRequiredInputs(tree);
		if (reqs == null || reqs.isEmpty()) {
			return null;
		} else if (reqs.size() == 1) {
			return null;
		} else {
			ArrayList<ParseTree> subtrees = new ArrayList<>();
			for (int i = 0; i < tree.getChildCount(); i++) {
				ParseTree child = tree.getChild(i);
				HashSet<Input> chreqs = Global.systemModel.getRequiredInputs(child);
				if (!(chreqs == null || chreqs.isEmpty())) {
					subtrees.add(child);
				}
			}
			if (subtrees.size() > 1) {
				return subtrees;
			} else {
				return split(subtrees.get(0));
			}
		}
	}

	// Finds horizontal cut with the lowest bandwidth requirement through an
	// expression tree
	public static ArrayList<ParseTree> optimalCut(ParseTree tree) {
		int size = Global.systemModel.getBandwidth(tree);
		int optsize = Global.systemModel.getOptimalSubtreesBandwidth(tree);
		ArrayList<ParseTree> subtrees = new ArrayList<>();
		if (size > optsize) {

			String nodeType = Utils.escapeWhitespace(Trees.getNodeText(tree, Global.ruleNames), false);

			// Identifier that needs to be resolved
			if (nodeType.equals("identifier") || nodeType.equals("shiftExpr")) {
				String targetId;
				if (nodeType.equals("identifier")) {
					targetId = tree.getChild(0).getText();
				} else {
					targetId = tree.getChild(0).getChild(0).getText();
				}
				DLolaObject target = Global.symtable.getObject(targetId);
				if (target instanceof Input) {
					try {
						throw new Exception("Input size should never be larger than its optimum size");
					} catch (Exception e) {
						e.printStackTrace();
						Debug.abort();
					}
				} else {
					DLolaExpTreeObject targetExp = (DLolaExpTreeObject) target;
					subtrees.addAll(optimalCut(targetExp.getExpTree()));
				}

			} else {
				// Normal Expression
				for (int i = 0; i < tree.getChildCount(); i++) {
					ParseTree child = tree.getChild(i);
					HashSet<Input> chreqs = Global.systemModel.getRequiredInputs(child);
					if (!(chreqs == null || chreqs.isEmpty())) {
						subtrees.addAll(optimalCut(child));
					}
				}
			}
		} else {
			// Already optimal
			subtrees.add(tree);
		}
		return subtrees;
	}

}
