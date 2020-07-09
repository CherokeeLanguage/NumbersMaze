package com.cherokeelessons.maze.screen;

public class GameSlot {
	private String name = "";
	private int slot = 0;
	private int score = 0;
	private int level = 0;
	private long elapsed = 0;
	private long modified = 0;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getElapsed() {
		return elapsed;
	}

	public void setElapsed(long elapsed) {
		this.elapsed = elapsed;
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("GameSlot [");
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		builder.append("slot=");
		builder.append(slot);
		builder.append(", score=");
		builder.append(score);
		builder.append(", level=");
		builder.append(level);
		builder.append(", elapsed=");
		builder.append(elapsed);
		builder.append(", modified=");
		builder.append(modified);
		builder.append("]");
		return builder.toString();
	}

}