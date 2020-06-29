package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

public class WallFixture extends Entity {

	private Fixture fixture = null;

	protected final Vector2 fixtureOffset = new Vector2();

	public WallFixture(final AtlasRegion wall_tile, final Vector2 offset) {
		super(wall_tile);
		fixtureOffset.set(offset);
		this.setName("WallFixture");
	}

	@Override
	public void cullCheck() {
		if (body == null || fixture == null) {
			return;
		}
		updatePosition();
		if (getCull() != null) {
			if (worldCenter.x < getCull().x || worldCenter.y < getCull().y
					|| worldCenter.x > getCull().x + getCull().width
					|| worldCenter.y > getCull().y + getCull().height) {
				remove();
			}
		}
	}

	public Fixture getFixture() {
		return fixture;
	}

	public Vector2 getFixtureOffset() {
		return fixtureOffset;
	}

	public void setFixture(final Fixture fixture) {
		this.fixture = fixture;
		this.fixture.setUserData(this);
	}

	public void setFixtureOffset(final Vector2 fixtureOffset) {
		this.fixtureOffset.set(fixtureOffset);
	}

	@Override
	public void updatePosition() {
		if (body != null && fixture != null) {
			worldCenter.set(body.getWorldCenter());
			worldCenter.add(fixtureOffset);
			final float angle = body.getAngle() * MathUtils.radiansToDegrees;
			setX(worldCenter.x * worldScale + getOffsetX());
			setY(worldCenter.y * worldScale + getOffsetY());
			setRotation(angle);
			layout();
		}
	}

	@Override
	protected short maskBits() {
		return 0;
	}

	@Override
	protected short sensorMaskBits() {
		return 0;
	}

	@Override
	protected short categoryBits() {
		return 0;
	}

}
