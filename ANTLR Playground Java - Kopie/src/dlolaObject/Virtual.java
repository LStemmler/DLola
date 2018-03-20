package dlolaObject;

import java.text.ParseException;

import org.antlr.v4.runtime.tree.ParseTree;

import semanticAnalysis.SymbolTable;

public class Virtual extends DLolaExpTreeObject {

	public Virtual(ParseTree defnode, SymbolTable symbolTable) throws ParseException {
		super(defnode, symbolTable);
	}
}
