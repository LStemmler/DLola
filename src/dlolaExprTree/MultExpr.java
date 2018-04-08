package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public class MultExpr extends IntExpr {

	public MultExpr (ParseTree tree) {
		super(tree);
	}

	@Override
	public boolean equals(DLolaExpr expr) {
		if (!this.mayEqual(expr))
			return false;
		if (expr instanceof MultExpr) {
			if (op.equals(((MultExpr) expr).op)) {
				if (this == expr)
					return true;
				if (this.subexpressions.get(0).equals(expr.subexpressions.get(0))
						&& this.subexpressions.get(1).equals(expr.subexpressions.get(1))) {
					return true;
				}
				if (this.op == Operator.MUL) {
					if (this.subexpressions.get(0).equals(expr.subexpressions.get(1))
							&& this.subexpressions.get(1).equals(expr.subexpressions.get(0))) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
