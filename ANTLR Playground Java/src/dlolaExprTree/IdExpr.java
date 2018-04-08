package dlolaExprTree;

import org.antlr.v4.runtime.tree.ParseTree;

import dlolaObject.DLolaExpTreeObject;
import dlolaObject.DLolaObject;
import dlolaObject.Input;
import main.Global;

public class IdExpr extends DLolaExpr {

	public String identifier;
	DLolaObject objRef;
	
	public IdExpr(ParseTree tree) {
		super(tree);

		identifier = tree.getChild(0).getText();
		objRef = Global.symtable.getObject(identifier);
		
		if (objRef instanceof Input) {
			this.type = ((Input) objRef).getType();
			this.requiredInputs.add((Input) objRef);
		} else {
			DLolaExpTreeObject targetExp = (DLolaExpTreeObject) objRef;
			this.type = targetExp.getType();
			subexpressions.add(generateDLolaExpr(targetExp.getExpTree()));
		}
	}
	
	
	@Override
	public boolean equals(dlolaExprTree.DLolaExpr expr) {
		if (expr instanceof IdExpr) {
			if (this instanceof ShiftExpr && expr instanceof ShiftExpr) {
				return (((ShiftExpr) this).equals((ShiftExpr) expr));
			}
			if (!(this instanceof ShiftExpr )&& !(expr instanceof ShiftExpr)) {
				if (this.identifier.equals(((IdExpr) expr).identifier)) {
					return true;
				} else if (!(objRef instanceof Input) && !(((IdExpr) expr).objRef instanceof Input)) {
					return this.subexpressions.get(0).equals(expr.subexpressions.get(0));
				}
			}
			if (!(objRef instanceof Input)) return this.subexpressions.get(0).equals(expr);
		}
		return false;
	}

	@Override
	public DLolaType typecheck() throws TypeException {
		return type;
	}

	@Override
	public String toString() {
		return identifier;
	}

	@Override
	public String toCode() {
		return this.toString();
	}

	@Override
	public boolean updateRequiredBandwidth() {
		if (objRef instanceof Input) {
			optSubBandwidth = type.size();
			return false;
		} else {
			int oldOptSubBandwidth = optSubBandwidth;
			optSubBandwidth = 0;
			for (DLolaExpr subexp: subexpressions) optSubBandwidth += Math.min(subexp.getBandwidth(), subexp.optSubBandwidth);
			return optSubBandwidth != oldOptSubBandwidth;
		}
	}
}
