package main;


import java.util.List;

import dlolaExprTree.DLolaType;
import evaluation.Evaluator;
import parser.DLolaParser;
import routeGeneration.RouteBuilder;
import semanticAnalysis.SymbolTable;
import semanticAnalysis.SystemModel;
import ui.UI;


/**
 * Global is a singleton class containing global variables and some often-used references and functions
 **/
public class Global {
    
	
	// Global variables specifying the target file, output verbosity, and bandwidth requirements. They can be changed at will

    public static String filePath = "./src/file1.dlola";		// Path to the file to be evaluated
    public static boolean displayAST = true;						// Whether the syntax tree should be shown
    public static boolean displayNetworkGraph = true;				// Whether the network graph should be shown
    public static boolean displayDependencyGraph = true;			// Whether the dependency graph should be shown
    public static boolean displayRelevantSubgraphs = false;			// Whether the relevant subgraphs should be shown
    public static boolean storeMultipleEquivalentSolutions = false;	// Whether more than one solution in each Pareto class should be stored. Will cause high memory usage
	public static int debugVerbosity = 14;		//0: Off, 1-3: Critical, 4-6 Error, 7-9 Warning, 10 Status, 11 Module, 12 Submodule, 13-15 Major Details, 16-20 Minor Details
	

	/* 
	 * Bandwidth requirements of the DLola types. They do not semantically imply any particular unit and directly subtracted from the remaining bandwidth of used channels.
	 */
	public static int sizeofType (DLolaType type) {	
		switch (type) {											
		case INT: return 4;
		case BOOL: return 1;
		default: return 0;
		}
	}
	
	
	
	
	// Internal stuff follows. It should not be changed.

	public static DLolaParser parser;
	public static List<String> ruleNames;
	public static SystemModel systemModel;
	public static SymbolTable symtable;
	public static RouteBuilder routeBuilder;
	public static Evaluator evaluator;
	public static UI ui = new UI();
	public static final int STAT_DELAY = Integer.MIN_VALUE;		// Delay value representing a statically calculable variable.
	public static final String newline = System.getProperty("line.separator");	// OS-independent line separator character
    public static long startTime = System.currentTimeMillis();

	public static String timeString(long time) {
		int secs = (int) (time / 1000);
		int mins = secs / 60;
		int hours = mins / 60;
		secs = secs % 60;
		mins = mins % 60;
		String secstr =("00" + secs).substring((""+secs).length());
		String minstr =("00" + mins).substring((""+mins).length());
		if (hours > 0) {
			return hours +":"+minstr+":"+secstr;
		}
		return minstr+":"+secstr;
	}

	private Global() {
	}
}
