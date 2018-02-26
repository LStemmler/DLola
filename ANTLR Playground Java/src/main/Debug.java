package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Debug {
    public static final Debug debug = new Debug();				// Debug is a singleton class

	public Debug() {
	}
	
	public static void out(int Priority, String str) {
		if (DLolaRunner.debugVerbosity >=Priority) {
			System.out.println("DEBUG P"+Priority+": "+str);
		}
	}
	public static void err(int Priority, String str) {
		if (DLolaRunner.debugVerbosity >=Priority) {
			System.err.println("DEBUG P"+Priority+": "+str);
		}
	}
	
	public static void abort() {
		err(1, "Aborting");
	    System.exit(1);
	}
	
}