package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import dlolaExprTree.DLolaExpr;
import dlolaExprTree.ExpressionMap;
import dlolaObject.Output;
import evaluation.Evaluator;
import parser.DLolaLexer;
import parser.DLolaParser;
import routeGeneration.RouteBuilder;
import semanticAnalysis.SystemModel;

public class DLolaRunner {

	public static void main(String[] args) throws Exception {
		
		//BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		
		String str;
		
		try(BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\blur\\git\\Bachelorprojekt\\ANTLR Playground Java\\src\\file.dlola"))) {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();
		    
		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    str = sb.toString();
		}
		
		ANTLRInputStream input = new ANTLRInputStream(str);

		DLolaLexer lexer = new DLolaLexer(input);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		Global.parser = new DLolaParser(tokens);
		Global.ruleNames = Arrays.asList(Global.parser.getRuleNames());
		ParseTree tree = Global.parser.dlolaFormat();
		Debug.out(16, "Parse Tree: "+ tree.toStringTree(Global.parser)); // print LISP-style tree
		
		Global.ui.displayAST(Global.ruleNames,tree);

		Global.systemModel = new SystemModel(tree);
		Global.ui.setSystemModel(Global.systemModel);
		Global.systemModel.generate();
		
		ExpressionMap.generateExpressions();
		ExpressionMap.linkExpTreeObjects();

		
		
		
		
		Global.routeBuilder = new RouteBuilder();
		Global.evaluator = new Evaluator();
		Global.routeBuilder.generateRoutes();
		Global.evaluator.evaluate();
		Global.evaluator.printEquivalenceClassDetails();

		Debug.out(10, "Program execution complete"); // print LISP-style tree
		
		
		
//		ArrayList<ParseTree> ecut = DLolaTree.optimalCut(((Output)Global.symtable.getObject("e")).getExpTree());
//
//		String cutstring = "";
//		for (int i = 0; i < ecut.size(); i++) {
//			cutstring += Trees.getNodeText(ecut.get(i), Global.ruleNames) + ", ";
//		}
//		Debug.out(19, "e-cut:" + cutstring);
		
//		SystemModel sm = Global.systemModel;
//		Debug.out(10, "wait");
//		sm.generate();
	}
}