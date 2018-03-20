package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public class IfExpr extends DLolaExpr {

	public IfExpr(ParseTree tree) {
		super(tree);
		subexpressions.add(generateDLolaExpr(tree.getChild(1)));
		subexpressions.add(generateDLolaExpr(tree.getChild(3)));
		subexpressions.add(generateDLolaExpr(tree.getChild(5)));
	}
	
	
	
	@Override
	public boolean equals(dlolaExprTree.DLolaExpr expr) {
		if (!this.mayEqual(expr))
			return false;
		if (expr instanceof IfExpr) {
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
	public DLolaType typecheck() throws TypeException {
		if (subexpressions.get(0).typecheck() == DLolaType.BOOL) {
			DLolaType t1 = subexpressions.get(1).typecheck();
			DLolaType t2 = subexpressions.get(2).typecheck();
			if (t1 != t2) throw new TypeException("Condition resulting in unequal types");
			else  {
				this.type = t1;
				return t1;
			}
		}
		throw new TypeException("If-condition not boolean");
	}

	@Override
	public String toString() {
		return "if " + subexpressions.get(0).toString() + " {" + subexpressions.get(1).toString() + "} " + subexpressions.get(2).toString();
	}

	@Override
	public String toCode() {
		return "if " + subexpressions.get(0).toCode() + " {" + subexpressions.get(1).toCode() + "} " + subexpressions.get(2).toCode();
	}

}
