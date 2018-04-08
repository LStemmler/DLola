package main;

import java.util.Date;
import java.util.List;

import evaluation.Evaluator;
import parser.DLolaParser;
import routeGeneration.RouteBuilder;
import semanticAnalysis.SymbolTable;
import semanticAnalysis.SystemModel;
import ui.UI;

public class Global {
    public static final Global glob = new Global();				// Static is a singleton class
    
    public static long startTime = System.currentTimeMillis();

    
    public static boolean displayAST = true;
    public static boolean displayNetworkGraph = true;
    public static boolean displayDependencyGraph = true;
    public static boolean displayRelevantSubgraphs = true;
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
	public static final String newline = System.getProperty("line.separator");

	private Global() {
	}

	
	
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
	
}
