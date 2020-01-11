package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;

public class ArrowGroup {
	public int boxCount=0;
	public final Group group=new Group();
	public int accumulator=0;
	private final Vector2 pos=new Vector2();
	private final Vector2 wpos=new Vector2();
	public ArrowGroup() {
		group.setName("ArrowGroup");
	}
	public Vector2 getPos() {
		return pos;
	}
	public void setPos(Vector2 p) {
			pos.set(p);
	}
	public void setPos(float x, float y) {
		pos.x=x;
		pos.y=y;
	}
	public Vector2 getWorldPos() {
		return wpos;
	}
	public void setWorldPos(Vector2 p) {
			wpos.set(p);
	}
	public void setWorldPos(float x, float y) {
		wpos.x=x;
		wpos.y=y;
	}
}