package dlolaObject;

import java.text.ParseException;

import org.antlr.v4.runtime.tree.ParseTree;

import semanticAnalysis.DLolaType;
import semanticAnalysis.SymbolTable;

public abstract class DLolaExpTreeObject extends DLolaObject {
	DLolaType type;
	ParseTree expressionTree;

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
}
