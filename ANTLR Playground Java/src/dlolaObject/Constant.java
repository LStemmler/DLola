package dlolaObject;

import java.text.ParseException;

import org.antlr.v4.runtime.tree.ParseTree;

import semanticAnalysis.DLolaType;
import semanticAnalysis.SymbolTable;

public class Constant extends DLolaExpTreeObject {
	
	String value;

	public Constant(ParseTree constantDef, SymbolTable symbolTable) throws ParseException {
		super(constantDef, symbolTable);
		type = getType(constantDef);
		if (type == DLolaType.BOOL) {
			value = expressionTree.getChild(0).getText();
		} else {
			value = expressionTree.getChild(0).getChild(0).getText();
		}
	}
}
