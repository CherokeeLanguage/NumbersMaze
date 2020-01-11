package com.cherokeelessons.maze.object;

public class OS {
	final public static String name;
	final public static boolean isWindows;
	final public static boolean isMac;
	final public static boolean isUnix;
	final public static boolean isSolaris;
	static {
		name = System.getProperty("os.name").toLowerCase();
		isWindows = name.contains("win");
		isMac = name.contains("mac");
		isUnix = name.contains("nix") || name.contains("nux") || name.contains("aix");
		isSolaris = name.contains("sunos");
	}
}