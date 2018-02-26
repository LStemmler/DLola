package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.gui.TreeViewer;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.Viewer;

import parser.DLolaLexer;
import parser.DLolaParser;
import semanticAnalysis.SystemModel;
import ui.UI;

public class DLolaRunner {
	public static int debugVerbosity = 19;		//0: Off, 1-3: Critical, 4-6 Error, 7-9 Warning, 10 Status, 11 Module, 12 Submodule, 13-15 Major Details, 16-20 Minor Details
	public static DLolaParser parser;
	public static List<String> ruleNames;
	public static SystemModel systemModel;
	public static UI ui = new UI();

	public static void main(String[] args) throws Exception {
		
		//BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		
		String str;
		
		try(BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\blur\\Eclipse_Workspace\\ANTLR Playground Java\\src\\file.dlola"))) {
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

		parser = new DLolaParser(tokens);
		ruleNames = Arrays.asList(parser.getRuleNames());
		ParseTree tree = parser.dlolaFormat();
		Debug.out(16, "Parse Tree: "+ tree.toStringTree(parser)); // print LISP-style tree
		
		ui.displayAST(ruleNames,tree);

        systemModel = new SystemModel(tree);
        ui.setSystemModel(systemModel);
        systemModel.generate();

	}
}