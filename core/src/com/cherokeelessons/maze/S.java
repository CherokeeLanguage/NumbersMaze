package com.cherokeelessons.maze;


public class S {
	public static boolean ultimateMode=false;
	private static Font fnt = null;
	private static Font fnt2 = null;
	private static AtlasRegionGet arg = null;
	private static PlayerAtlasRegion par = null;

	public static void init() {
		arg = new AtlasRegionGet();
		fnt = new Font(Font.Face.CherokeeHandone);
		fnt2 = new Font(Font.Face.Digohweli);
		par = new PlayerAtlasRegion();
	}

	public static PlayerAtlasRegion getPar() {
		return par;
	}

	public static Font getFnt() {
		return fnt;
	}
	public static Font getFnt2() {
		return fnt2;
	}
	public static AtlasRegionGet getArg() {
		return arg;
	}

	public static void dispose() {
		if (fnt != null) {
			fnt.dispose();
		}
		if (fnt2 != null) {
			fnt2.dispose();
		}
		if (arg != null) {
			arg.dispose();
		}
		if (par != null) {
			par.dispose();
		}
		arg = null;
		fnt = null;
		fnt2 = null;
		par = null;
	}
}
