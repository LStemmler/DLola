package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public class ElseExpr extends DLolaExpr {

	public ElseExpr(ParseTree tree) {
		super(tree);
		subexpressions.add(generateDLolaExpr(tree.getChild(2)));
	}
	
	@Override
	public boolean equals(dlolaExprTree.DLolaExpr expr) {
		if (!this.mayEqual(expr))
			return false;
		if (expr instanceof ElseExpr) {
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
		return "else {" + subexpressions.get(0).toString() + "}";
	}

	@Override
	public String toCode() {
		return "else {" + subexpressions.get(0).toCode() + "}";
	}

}
