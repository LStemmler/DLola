package dlolaObject;

import java.text.ParseException;

import org.antlr.v4.runtime.tree.ParseTree;

import dlolaExprTree.DLolaExpr;
import dlolaExprTree.DLolaType;
import dlolaExprTree.ExprSection;
import dlolaExprTree.ExpressionMap;
import main.Debug;
import semanticAnalysis.SymbolTable;

public abstract class DLolaExpTreeObject extends DLolaObject {
	DLolaType type;
	ParseTree expressionTree;
	DLolaExpr expression;
	ExprSection section;

	public DLolaExpTreeObject(ParseTree defnode, SymbolTable symbolTable) throws ParseException {
		super(defnode, symbolTable);
		type = getType(defnode);
		expressionTree = defnode.getChild(4);
	}

	public DLolaType getType() {
		return type;
	}

	public ParseTree getExpTree() {
		return expressionTree;
	}

	public DLolaExpr getExpression() {
		if (expression == null) expression = ExpressionMap.getExpression(expressionTree);
		return expression;
	}

	public ExprSection getExprSection() {
		if (section == null) section = ExpressionMap.getExprSection(ExpressionMap.getExpression(expressionTree));
		return section;
	}
}
