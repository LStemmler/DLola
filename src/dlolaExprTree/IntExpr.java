package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public abstract class IntExpr extends DLolaExpr {
	
	Operator op;
	
	public IntExpr(ParseTree tree) {
		super(tree);
		type = DLolaType.INT;
		op = Operator.fromString(tree.getChild(1).getChild(0).toString());
		subexpressions.add(generateDLolaExpr(tree.getChild(0)));
		subexpressions.add(generateDLolaExpr(tree.getChild(2)));
	}

	@Override
	public DLolaType typecheck() throws TypeException {
		if(subexpressions.get(0).typecheck()== DLolaType.INT)
			if(subexpressions.get(1).typecheck()== DLolaType.INT)
				return DLolaType.INT;
		throw new TypeException();
	}

	@Override
	public String toString() {
		return subexpressions.get(0).toString() + op.toCode() + subexpressions.get(1).toString();
	}

	@Override
	public String toCode() {
		return subexpressions.get(0).toCode() + op.toCode() + subexpressions.get(1).toCode();
	}

}
