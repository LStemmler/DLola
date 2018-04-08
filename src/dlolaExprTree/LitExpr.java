package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

public class LitExpr extends DLolaExpr {

	String value;
	
	public LitExpr (ParseTree tree) {
		super(tree);

		String contentStr  = tree.getChild(0).getText();
		if (contentStr.equals("true") || contentStr.equals("false")) {
			type = DLolaType.BOOL;
		} else {
			type = DLolaType.INT;
		}
		value = contentStr;
	}

	@Override
	public boolean equals(DLolaExpr expr) {
		if (!this.mayEqual(expr))
			return false;
		if (expr instanceof LitExpr) {
			if (this == expr)
				return true;
			if (this.value.equals(((LitExpr) expr).value)) {
				return true;
			} else {
				return false;
			}
		}
		return expr.equals(this);
	}

	public boolean equals(LitExpr expr) {
		return this.value.equals(expr.value);
	}

	@Override
	public DLolaType typecheck() {
		return this.type;
	}

	@Override
	public String toString() {
		return this.value;
	}

	@Override
	public String toCode() {
		return this.toString();
	}
}
