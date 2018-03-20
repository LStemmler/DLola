package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public abstract class BoolExpr extends DLolaExpr {
	
	Operator op;
	
	public BoolExpr(ParseTree tree) {
		super(tree);
		type = DLolaType.BOOL;
		op = Operator.fromString(tree.getChild(1).getChild(0).toString());
		subexpressions.add(generateDLolaExpr(tree.getChild(0)));
		subexpressions.add(generateDLolaExpr(tree.getChild(2)));
	}
	
	public BoolExpr(Operator op, ParseTree tree) {
		super(tree);
		type = DLolaType.BOOL;
		this.op = op;
	}
	

	@Override
	public DLolaType typecheck() throws TypeException {
		if(subexpressions.get(0).typecheck()== DLolaType.BOOL)
			if(subexpressions.get(1).typecheck()== DLolaType.BOOL)
				return DLolaType.BOOL;
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
