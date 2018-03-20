package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public class ParenExpr extends DLolaExpr {


	public ParenExpr(ParseTree tree) {
		super(tree);
		subexpressions.add(generateDLolaExpr(tree.getChild(1)));
	}

	@Override
	public boolean equals(DLolaExpr expr) {
		// TODO not good at all
		if (!this.mayEqual(expr))
			return false;
		if (expr instanceof ParenExpr) {
			if (this == expr)
				return true;
			if (this.subexpressions.get(0).equals(expr.subexpressions.get(0)))
				return true;
		}
		return false;
	}

	@Override
	public DLolaType typecheck() throws TypeException {
		this.type = subexpressions.get(0).typecheck();
		return type;
	}

	@Override
	public String toString() {
		return "(" + subexpressions.get(0).toString() + ")";
	}

	@Override
	public String toCode() {
		return "(" + subexpressions.get(0).toCode() + ")";
	}

}
