package com.cherokeelessons.maze.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Maze {
	private int seed;
	private int width;
	private int height;
	private MazeCell[][] maze;

	public Maze(int seed, int width, int height) {
		this.width = width;
		this.height = height;
		this.seed = seed;
		maze = new MazeCell[width][height];
		generate();
	}

	private void generate() {
		Random r = new Random(seed);
		int totalCells = width * height;
		
		ArrayList<CellPosition> stack = new ArrayList<CellPosition>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				maze[x][y] = new MazeCell();
			}
		}
		List<CellPosition> neighbors = new ArrayList<Maze.CellPosition>();
		CellPosition currentCell = new CellPosition(r.nextInt(width), r.nextInt(height));
		int visitedCells = 1; //start at one to account for our starting cell
		while (visitedCells < totalCells) {
			int cx, cy;
			neighbors.clear();
			int x = currentCell.x;
			int y = currentCell.y;
			// check west
			cx = x - 1;
			cy = y;
			if (cx >= 0 && maze[cx][cy].hasAllWalls())
				neighbors.add(new CellPosition(cx, cy));
			// check east
			cx = x + 1;
			cy = y;
			if (cx < width && maze[cx][cy].hasAllWalls())
				neighbors.add(new CellPosition(cx, cy));
			// check south
			cx = x;
			cy = y - 1;
			if (cy >= 0 && maze[cx][cy].hasAllWalls())
				neighbors.add(new CellPosition(cx, cy));
			// check north
			cx = x;
			cy = y + 1;
			if (cy < height && maze[cx][cy].hasAllWalls())
				neighbors.add(new CellPosition(cx, cy));

			if (neighbors.size() > 0) {
				int next=r.nextInt(neighbors.size());
				CellPosition newCell = neighbors
						.get(next);
				if (newCell.x != currentCell.x) {
					if (newCell.x < currentCell.x) {
						maze[newCell.x][newCell.y].wall.e = false;
						maze[currentCell.x][currentCell.y].wall.w = false;
					} else {
						maze[newCell.x][newCell.y].wall.w = false;
						maze[currentCell.x][currentCell.y].wall.e = false;
					}
				}
				if (newCell.y != currentCell.y) {
					if (newCell.y < currentCell.y) {
						maze[newCell.x][newCell.y].wall.n = false;
						maze[currentCell.x][currentCell.y].wall.s = false;
					} else {
						maze[newCell.x][newCell.y].wall.s = false;
						maze[currentCell.x][currentCell.y].wall.n = false;
					}
				}
				stack.add(currentCell);
				currentCell = newCell;
				visitedCells++;
			} else {
				if (stack.size() == 0){
					System.out.println("MAZE GEN BUG!");
					System.out.println("VISITED CELLS: "+visitedCells);
					System.out.println("TOTAL CELLS: "+totalCells);
					break;
				}
				currentCell = stack.get(stack.size() - 1);				
				stack.remove(stack.size() - 1);
			}
		}
	}
	
	public MazeCell[][] get() {
		return maze;
	}

	public static class CellPosition {
		public int x;
		public int y;

		public CellPosition(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

	}
}
