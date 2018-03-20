package dlolaExprTree;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.tree.ParseTree;

import dlolaObject.Output;
import main.Debug;
import main.Global;

public class ExpressionMap {

	
	static HashMap<ParseTree, DLolaExpr> treeExpressionMap = new HashMap<>();
	static HashMap<DLolaExpr, ExprSection> exprSectionMap = new HashMap<>();
	private static ArrayList<DLolaExpr> expressions;
	
	
	
	public static DLolaExpr getExpression(ParseTree t) {
		return treeExpressionMap.get(t);
	}



	public static void putExpression(ParseTree t, DLolaExpr expr) {
		treeExpressionMap.put(t, expr);
	}



	public static ExprSection getExprSection(DLolaExpr expr) {
		return exprSectionMap.get(expr);
	}



	public static void putExprSection(DLolaExpr expr, ExprSection exprSection) {
		exprSectionMap.put(expr, exprSection);
	}



	public static final ExpressionMap expressionMap = new ExpressionMap();
	
	public static void generateExpressions() {
		for (Output out : Global.symtable.getOutputList() ) {
			DLolaExpr expr = DLolaExpr.generateDLolaExpr(out.getExpTree());
			try {
				DLolaType type = expr.typecheck();
				if (type != out.getType()) throw new TypeException("Output type "+ type.toString()+" of output " + out.getIdentifier() +" does not match specified type "+ out.getType().toString());
			} catch (TypeException e) {
				e.printStackTrace();
				Debug.abort();
			}
		}
		for (DLolaExpr expr : treeExpressionMap.values() ) {
			expr.updateRequiredInputsAndRecursion();
		}
		while(mergeExpressions());
		boolean changed = true;
		while (changed) {
			changed = false;
			for (DLolaExpr expr : treeExpressionMap.values() ) {
				if (expr.updateRequiredBandwidth()) {
					changed = true;
				}
			}
		}

		for (DLolaExpr expr : treeExpressionMap.values() ) {
			Debug.out(15, expr.toCode()+ " requires bandwitdth of "+expr.getBandwidth() +", its optimal subexpressions require " + expr.optSubBandwidth);;
		}
		
	}
	

	static boolean mergeExpressions() {
		for (DLolaExpr expr : treeExpressionMap.values() ) {
			for (DLolaExpr compExpr : treeExpressionMap.values() ) {
				//Avoid stupid infinite loop
				if (expr != compExpr) {
					boolean mergeSuccess = expr.checkMergeability(compExpr);
					if (mergeSuccess) {
						Debug.out(16, "Expressions "+expr.toString() +" and "+compExpr.toString() + " are mergeable.");
						merge(expr, compExpr);	//TODO: Might cause a concurrent modification exception, keep in mind
						return true;
					}
				}
			}
		}
		return false;
	}
	
	static void merge (DLolaExpr mergeableExpr, DLolaExpr targetExpr) {
		for (DLolaExpr expr : treeExpressionMap.values() ) {
			for (int i=0; i<expr.subexpressions.size(); i++) {
				if (expr.subexpressions.get(i) == mergeableExpr) expr.subexpressions.set(i, targetExpr);
			}
		}
		for (ParseTree tree: treeExpressionMap.keySet()) {
			DLolaExpr value = treeExpressionMap.get(tree);
				if (value == mergeableExpr) putExpression(tree, targetExpr);
		}
	}
	
	
	
}
