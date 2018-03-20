package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public class CompExpr extends BoolExpr {
	
	public CompExpr(ParseTree tree) {
		super(tree);
	}

	@Override
	public boolean equals(DLolaExpr expr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DLolaType typecheck() throws TypeException {
		if(subexpressions.get(0).typecheck()== DLolaType.INT)
			if(subexpressions.get(1).typecheck()== DLolaType.INT)
				return DLolaType.BOOL;
		throw new TypeException();
	}
}
