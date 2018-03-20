package dlolaExprTree;

import org.antlr.v4.parse.ANTLRParser.throwsSpec_return;
import org.antlr.v4.runtime.tree.ParseTree;

import dlolaObject.DLolaExpTreeObject;
import dlolaObject.DLolaObject;
import dlolaObject.Input;
import main.Debug;
import main.Global;
import routeGeneration.ParseTreeTools;
import routeGeneration.SubexpressionTree;
import semanticAnalysis.UntypedException;

public class ShiftExpr extends IdExpr {

	int timeShift;
	LitExpr defaultValue;
	
	public ShiftExpr(ParseTree tree) {
		super(tree.getChild(0));

		timeShift = Integer.valueOf(tree.getChild(2).getChild(0).getText());
		defaultValue = new LitExpr(tree.getChild(4));
	}

	@Override
	public boolean equals(dlolaExprTree.DLolaExpr expr) {
		if (expr instanceof ShiftExpr) {
			if (this.identifier.equals(((ShiftExpr) expr).identifier)) {
				if (this.timeShift == ((ShiftExpr) expr).timeShift) {
					if (this.defaultValue.equals(((ShiftExpr) expr).defaultValue)) {
						return true;
					}
				}
			}
			return false;
		}
		return false;
	}
	
	@Override
	public DLolaType typecheck() throws TypeException {
		if (type != defaultValue.typecheck()) throw new TypeException("Default value type "+defaultValue.typecheck()+" of ShiftExpr "+ this.toCode()+" does not equal variable type "+ type);
		else return type;
	}

	@Override
	public String toString() {
		return identifier + "[" + timeShift + ", " + defaultValue +"]";
	}

	@Override
	public String toCode() {
		return this.toString();
	}
}
