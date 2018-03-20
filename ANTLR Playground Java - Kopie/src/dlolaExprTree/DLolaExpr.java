package dlolaExprTree;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;
import org.graphstream.util.parser.ParseException;

import dlolaObject.DLolaExpTreeObject;
import dlolaObject.DLolaObject;
import dlolaObject.Input;
import main.Debug;
import main.Global;
import routeGeneration.ParseTreeTools;
import routeGeneration.SubexpressionTree;
import routeGeneration.Task;
import semanticAnalysis.UntypedException;

public abstract class DLolaExpr {

	public boolean relevant;					// True if head of an identified expression or a local optimum
	public boolean localOptimum;				// Whether the bandwidth is lower than that of the subexpressions
	public int optSubBandwidth;					// Optimal bandwidth of the subexpressions (including their subexpressions)
	public boolean recursive = false;			// Whether the Expression can reach itself (time-shifted) over its subexpressions
	public ArrayList<Input> requiredInputs;		//
	public ArrayList<DLolaExpr> subexpressions;	// direct subexpressions
	public DLolaType type;						// int or bool

	public boolean shouldSplit() {
		return type.size() > optSubBandwidth;
	}
	
	
	
	
	// returns true if the expressions may be equivalent, then the subclasses need to compare the rest
	public boolean mayEqual(DLolaExpr expr) {
		if (expr == null) {
			return false;
		}
		if (!DLolaExpr.class.isAssignableFrom(expr.getClass())) {
			return false;
		}
		if (!(this.type == ((DLolaExpr) expr).type)) {
			return false;
		} else return true;
	}

	public abstract boolean equals(DLolaExpr expr);
	public abstract DLolaType typecheck() throws TypeException;
	public abstract String toString();
	public abstract String toCode();
	
	public boolean requiresInputs() {
		return !requiredInputs.isEmpty();
	}
	
	public DLolaExpr(ParseTree tree) {
		ExpressionMap.putExpression(tree, this);
		requiredInputs = new ArrayList<>();
		subexpressions = new ArrayList<>();
	}
	
	public boolean complete() {
		//TODO
		return false;
	}
	
	protected ArrayList<Input> getSubexpRequiredInputs() {
		ArrayList<Input> subexpRequiredInputs = new ArrayList<>();
		for (DLolaExpr subexp: subexpressions) {
			for (Input in: subexp.requiredInputs) {
				if (!subexpRequiredInputs.contains(in))  subexpRequiredInputs.add(in);
			}
		}
		return subexpRequiredInputs;
	}

	
	protected void updateRequiredInputsAndRecursion() {
		ArrayList<DLolaExpr> visited = new ArrayList<>();
		for (DLolaExpr subexp: subexpressions) {
			if (!visited.contains(subexp)) subexp.updateRequiredInputs(visited, visited);
		}
		ArrayList<Input> subexpRequiredInputs = getSubexpRequiredInputs();
		for (Input in: subexpRequiredInputs) {
			if (!requiredInputs.contains(in))  requiredInputs.add(in);
		}
	}

	protected void updateRequiredInputs(ArrayList<DLolaExpr> visitedOnce, ArrayList<DLolaExpr> visitedTwice) {
		
		if (recursive) return;		// Already calculated
		
		visitedOnce = (ArrayList<DLolaExpr>) visitedOnce.clone();
		visitedTwice = (ArrayList<DLolaExpr>) visitedTwice.clone();
		
		
		if (visitedOnce.contains(this)) {
			if (visitedTwice.contains(this)) {
				for (DLolaExpr astnode: visitedTwice) {
					astnode.recursive = true;
				}
				return;
			}
			visitedTwice.add(this);
		} else {
			visitedOnce.add(this);
		}
		for (DLolaExpr subexp: subexpressions) {
			subexp.updateRequiredInputs(visitedOnce, visitedTwice);
		}
		ArrayList<Input> subexpRequiredInputs = getSubexpRequiredInputs();
		for (Input in: subexpRequiredInputs) {
			if (!requiredInputs.contains(in))  requiredInputs.add(in);
		}
	}
	
	
	
	public static DLolaExpr generateDLolaExpr(ParseTree tree) {
		DLolaExpr expr = ExpressionMap.getExpression(tree);
		if (expr != null) {
			return expr;
		}

		String nodeType = Utils.escapeWhitespace(Trees.getNodeText(tree, Global.ruleNames), false);
		
		if (tree.getChildCount() == 1) {
			switch(nodeType) {
			case "identifier":
				return new IdExpr(tree);
			case "literal":
				return new LitExpr(tree);
			default:
				// Irrelevant node will be pruned
				return generateDLolaExpr(tree.getChild(0));
			}
		}
		
		
		
		switch(nodeType) {
		case "expression":
			return new ParenExpr(tree);
		case "equivExpr":
			return new EquivExpr(tree);
		case "impExpr":
			return new ImpExpr(tree);
		case "orExpr":
			return new OrExpr(tree);
		case "andExpr":
			return new AndExpr(tree);
		case "negExpr":
			return new NegExpr(tree);
		case "equExpr":
			return new EquExpr(tree);
		case "comparExpr":
			return new CompExpr(tree);
		case "addExpr":
			return new AddExpr(tree);
		case "subExpr":
			return new SubExpr(tree);
		case "multExpr":
			return new MultExpr(tree);
		case "expExpr":
			return new ExpExpr(tree);
		case "ifExpr":
			return new IfExpr(tree);
		case "elseExpr":
			if (tree.getChild(0).toString().equals("else"))
				return new ElseExpr(tree);
			else return new ElifExpr(tree);
		case "shiftExpr":
			return new ShiftExpr(tree);
		default:
			try {
				throw new ParseException("No DLolaExpr could be created for the ParseTree "+ tree.getText() + " with NodeType "+ nodeType);
			} catch (ParseException e) {
				e.printStackTrace();
				Debug.abort();
				return null;
			}
		}
	}
		
	public int getBandwidth () {
		return type.size();
	}

	boolean checkMergeability(DLolaExpr compExpr) {
		if (!(this.requiredInputs.containsAll(compExpr.requiredInputs)
				&& compExpr.requiredInputs.containsAll(this.requiredInputs))) {
			return false;
		}
		return this.equals(compExpr);
	}

	public boolean updateRequiredBandwidth() {
		int oldOptSubBandwidth = optSubBandwidth;
		optSubBandwidth = 0;
		for (DLolaExpr subexp: subexpressions) optSubBandwidth += Math.min(subexp.getBandwidth(), subexp.optSubBandwidth);
		return optSubBandwidth != oldOptSubBandwidth;
	}
}
