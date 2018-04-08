package dlolaExprTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.naming.ldap.ExtendedResponse;

import org.antlr.v4.runtime.tree.ParseTree;

import dlolaObject.Constant;
import dlolaObject.DLolaExpTreeObject;
import dlolaObject.DLolaObject;
import dlolaObject.Output;
import dlolaObject.Virtual;
import main.Debug;
import main.Global;

public class ExpressionMap {

	
	static HashMap<ParseTree, DLolaExpr> treeExpressionMap = new HashMap<>();
	static HashMap<DLolaExpr, ArrayList<DLolaExpTreeObject>> objectMap = new HashMap<>();
	static HashMap<DLolaExpr, ExprSection> exprSectionMap = new HashMap<>();
	
	
	
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


	public static ArrayList<DLolaExpTreeObject> getObjects(DLolaExpr expr) {
		return objectMap.get(expr);
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
			Debug.out(18, expr.toCode()+ " requires bandwidth of "+expr.getBandwidth() +", its optimal subexpressions require " + expr.optSubBandwidth);;
		}
		
		for (Output out : Global.symtable.getOutputList() ) {
			ExprSection.createExprSection(getExpression(out.getExpTree()));
		}
		for (ExprSection section: ExpressionMap.exprSectionMap.values()) {
			section.calculateTimeShift();
			Debug.out(16, section.toString());
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



	public static Collection<ExprSection> getExprSections() {
		return exprSectionMap.values();
	}
	
	
	public static void linkExpTreeObjects() {
		for (Output obj: Global.symtable.getOutputList()) {
			addExpTreeObjectLink(obj);
		}
		for (Virtual obj: Global.symtable.getVirtualList()) {
			addExpTreeObjectLink(obj);
		}
		for (Constant obj: Global.symtable.getConstantList()) {
			addExpTreeObjectLink(obj);	
		}
	}
	
	private static void addExpTreeObjectLink(DLolaExpTreeObject obj) {
		DLolaExpr expr = obj.getExpression();
		ArrayList<DLolaExpTreeObject> set = objectMap.get(expr);
		if (set == null) {
			set = new ArrayList<>();
			objectMap.put(expr, set);
		}
		set.add(obj);
		try {
			DLolaExpr exprHead = getExprSection(expr).getHead();
			if (exprHead != expr) {
				set = objectMap.get(exprHead);
				if (set == null) {
					set = new ArrayList<>();
					objectMap.put(exprHead, set);
				}
				set.add(obj);
			}
		} catch (NullPointerException e) {}
	}
	
}
