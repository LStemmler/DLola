package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public class OrExpr extends BoolExpr {

	public OrExpr(ParseTree tree) {
		super(tree);
	}

	
	@Override
	public boolean equals(DLolaExpr expr) {
		if (!this.mayEqual(expr))
			return false;
		if (expr instanceof OrExpr) {
			if (this == expr)
				return true;
			if (this.subexpressions.get(0).equals(expr.subexpressions.get(0))
					&& this.subexpressions.get(1).equals(expr.subexpressions.get(1))) {
				return true;
			}
			if (this.subexpressions.get(0).equals(expr.subexpressions.get(1))
					&& this.subexpressions.get(1).equals(expr.subexpressions.get(0))) {
				return true;
			}
		}
		return false;
	}
	
}
