package com.cherokeelessons.maze.object;

public class MazeCell {
	public Backtrack backtrack;
	public Solution solution;
	public Borders border;
	public Walls wall;

	public MazeCell() {
		backtrack=new Backtrack();
		solution=new Solution();
		border=new Borders();
		wall=new Walls();
	}

	public static class Backtrack {
		boolean w = false;
		boolean s = false;
		boolean e = false;
		boolean n = false;
	}

	public static class Solution {
		boolean w = false;
		boolean s = false;
		boolean e = false;
		boolean n = false;
	}

	public static class Borders {
		boolean w = false;
		boolean s = false;
		boolean e = false;
		boolean n = false;
	}

	public static class Walls {
		public boolean w = true;
		public boolean s = true;
		public boolean e = true;
		public boolean n = true;
	}

	public boolean hasAllWalls(){
		return wall.w && wall.s && wall.e && wall.n;
	}
	
	public boolean isSolid = true;
}
