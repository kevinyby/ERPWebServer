package com.modules.util;

public class DLog {
	
	public static boolean isDebugingMode = true;
	
	public static void log(String message) {
		if (! isDebugingMode) return; 
		StackTraceElement[] elements = new Throwable().getStackTrace();
		String classNameString = elements[1].getClassName();
		System.out.println("\n ----- " + "[" + classNameString + "]" + "  " + message);
	}
}
