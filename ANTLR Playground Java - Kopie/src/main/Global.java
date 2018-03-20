package main;

import java.util.List;

import evaluation.Evaluator;
import parser.DLolaParser;
import routeGeneration.RouteBuilder;
import semanticAnalysis.SymbolTable;
import semanticAnalysis.SystemModel;
import ui.UI;

public class Global {
    public static final Global glob = new Global();				// Static is a singleton class
    

    
    public static boolean displayAST = true;
    public static boolean displayNetworkGraph = true;
    public static boolean displayDependencyGraph = true;
    public static boolean displayRelevantSubgraphs = false;
    public static boolean storeMultipleEquivalentSolutions = false;
	public static int debugVerbosity = 16;		//0: Off, 1-3: Critical, 4-6 Error, 7-9 Warning, 10 Status, 11 Module, 12 Submodule, 13-15 Major Details, 16-20 Minor Details
	public static DLolaParser parser;
	public static List<String> ruleNames;
	public static SystemModel systemModel;
	public static SymbolTable symtable;
	public static RouteBuilder routeBuilder;
	public static Evaluator evaluator;
	public static UI ui = new UI();
	public static final int STAT_DELAY = Integer.MIN_VALUE;		// Delay value representing a statically calculable variable

	public Global() {
	}

}
