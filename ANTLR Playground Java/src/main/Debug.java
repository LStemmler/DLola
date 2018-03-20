package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

public class Debug {
    public static final Debug debug = new Debug();				// Debug is a singleton class

	public Debug() {
	}
	
	public static void out(int Priority, String str) {
		if (Global.debugVerbosity >=Priority) {
			System.out.println("DEBUG P"+Priority+": "+str);
		}
	}
	public static void err(int Priority, String str) {
		if (Global.debugVerbosity >=Priority) {
			System.err.println("DEBUG P"+Priority+": "+str);
		}
	}
	
	public static void abort() {
		err(1, "Aborting");
	    System.exit(1);
	}

	

	public static void ensure(boolean check, String errorMsg)  {
		if (!check)
			try {
				Debug.err(3, "Assertion failed: "+errorMsg);
				throw new ParseException(errorMsg, 0);
			} catch (ParseException e) {
				e.printStackTrace();
				abort();
			}
	}

	public static void error(String errorMsg)  {
		try {
			Debug.err(3, "Assertion failed: "+errorMsg);
			throw new ParseException(errorMsg, 0);
		} catch (ParseException e) {
			e.printStackTrace();
			abort();
		}
	}
}