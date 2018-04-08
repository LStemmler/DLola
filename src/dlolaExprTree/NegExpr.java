package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public class NegExpr extends BoolExpr {

	public NegExpr(ParseTree tree) {
		super(Operator.fromString(tree.getChild(0).getChild(0).toString()), tree);
		subexpressions.add(generateDLolaExpr(tree.getChild(1)));
	}

	@Override
	public boolean equals(DLolaExpr expr) {
		// TODO negative equivalence
		if (!this.mayEqual(expr))
			return false;
		if (expr instanceof NegExpr) {
			if (this == expr)
				return true;
			if (this.subexpressions.get(0).equals(expr.subexpressions.get(0)))
				return true;
		}
		return false;
	}
	
	
	@Override
	public DLolaType typecheck() throws TypeException {
		if (subexpressions.get(0).typecheck() == DLolaType.BOOL)
			return DLolaType.BOOL;
		throw new TypeException();
	}

	@Override
	public String toString() {
		return op.toCode() + subexpressions.get(0).toString();
	}

	@Override
	public String toCode() {
		return op.toCode() + subexpressions.get(0).toCode();
	}
}
