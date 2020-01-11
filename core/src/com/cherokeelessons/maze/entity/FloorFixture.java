package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;

public class FloorFixture extends WallFixture {

	public FloorFixture(AtlasRegion wall_tile, Vector2 offset) {
		super(wall_tile, offset);
		fixtureOffset.set(offset);
		this.setName("FloorFixture");
	}

}
