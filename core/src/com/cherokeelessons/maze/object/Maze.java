package com.cherokeelessons.maze.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;

public class Maze {
	public static class CellPosition {
		public int x;
		public int y;

		public CellPosition(final int x, final int y) {
			super();
			this.x = x;
			this.y = y;
		}

	}

	private final int seed;
	private final int width;
	private final int height;

	private final MazeCell[][] maze;

	public Maze(final int seed, final int width, final int height) {
		this.width = width;
		this.height = height;
		this.seed = seed;
		maze = new MazeCell[width][height];
		generate();
	}

	private void generate() {
		final Random r = new Random(seed);
		final int totalCells = width * height;

		final ArrayList<CellPosition> stack = new ArrayList<>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				maze[x][y] = new MazeCell();
			}
		}
		final List<CellPosition> neighbors = new ArrayList<>();
		CellPosition currentCell = new CellPosition(r.nextInt(width), r.nextInt(height));
		int visitedCells = 1; // start at one to account for our starting cell
		while (visitedCells < totalCells) {
			int cx, cy;
			neighbors.clear();
			final int x = currentCell.x;
			final int y = currentCell.y;
			// check west
			cx = x - 1;
			cy = y;
			if (cx >= 0 && maze[cx][cy].hasAllWalls()) {
				neighbors.add(new CellPosition(cx, cy));
			}
			// check east
			cx = x + 1;
			cy = y;
			if (cx < width && maze[cx][cy].hasAllWalls()) {
				neighbors.add(new CellPosition(cx, cy));
			}
			// check south
			cx = x;
			cy = y - 1;
			if (cy >= 0 && maze[cx][cy].hasAllWalls()) {
				neighbors.add(new CellPosition(cx, cy));
			}
			// check north
			cx = x;
			cy = y + 1;
			if (cy < height && maze[cx][cy].hasAllWalls()) {
				neighbors.add(new CellPosition(cx, cy));
			}

			if (neighbors.size() > 0) {
				final int next = r.nextInt(neighbors.size());
				final CellPosition newCell = neighbors.get(next);
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
				if (stack.size() == 0) {
					Gdx.app.log(this.getClass().getSimpleName(), "MAZE GEN BUG!");
					Gdx.app.log(this.getClass().getSimpleName(), "VISITED CELLS: " + visitedCells);
					Gdx.app.log(this.getClass().getSimpleName(), "TOTAL CELLS: " + totalCells);
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
}
