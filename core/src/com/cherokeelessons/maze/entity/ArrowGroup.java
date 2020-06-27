package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;

public class ArrowGroup {
	public int boxCount = 0;
	public final Group group = new Group();
	public int accumulator = 0;
	private final Vector2 pos = new Vector2();
	private final Vector2 wpos = new Vector2();

	public ArrowGroup() {
		group.setName("ArrowGroup");
	}

	public Vector2 getPos() {
		return pos;
	}

	public Vector2 getWorldPos() {
		return wpos;
	}

	public void setPos(final float x, final float y) {
		pos.x = x;
		pos.y = y;
	}

	public void setPos(final Vector2 p) {
		pos.set(p);
	}

	public void setWorldPos(final float x, final float y) {
		wpos.x = x;
		wpos.y = y;
	}

	public void setWorldPos(final Vector2 p) {
		wpos.set(p);
	}
}