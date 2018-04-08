package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public class ElifExpr extends IfExpr {

	public ElifExpr(ParseTree tree) {
		super(tree);
	}
	
	@Override
	public boolean equals(dlolaExprTree.DLolaExpr expr) {
		if (!this.mayEqual(expr))
			return false;
		if (expr instanceof ElifExpr) {
			if (this == expr)
				return true;
			if (this.subexpressions.get(0).equals(expr.subexpressions.get(0))) {
				if (this.subexpressions.get(1).equals(expr.subexpressions.get(1))) {
					if (this.subexpressions.get(2).equals(expr.subexpressions.get(2)))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "elif " + subexpressions.get(0).toString() + " {" + subexpressions.get(1).toString() + "} " + subexpressions.get(2).toString();
	}

	@Override
	public String toCode() {
		return "elif " + subexpressions.get(0).toCode() + " {" + subexpressions.get(1).toCode() + "} " + subexpressions.get(2).toCode();
	}

}
