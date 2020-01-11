package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;


public class WallFixture extends Entity {

	private Fixture fixture=null;
	
	public WallFixture(AtlasRegion wall_tile, Vector2 offset) {
		super(wall_tile);
		fixtureOffset.set(offset);
		this.setName("WallFixture");
	}

	protected final Vector2 fixtureOffset=new Vector2();
	
	@Override
	public void updatePosition() {		
		if (body!=null && fixture!=null) {
			worldCenter.set(body.getWorldCenter());
			worldCenter.add(fixtureOffset);
			float angle = body.getAngle()
					* MathUtils.radiansToDegrees;
			setX(worldCenter.x * worldScale + getOffsetX());
			setY(worldCenter.y * worldScale + getOffsetY());
			setRotation(angle);
			layout();
		}
	}

	@Override
	public void cullCheck() {
		if (body == null || fixture==null)
			return;		
		updatePosition();
		if (getCull() != null) {
			if (worldCenter.x < getCull().x || worldCenter.y < getCull().y
					|| worldCenter.x > getCull().x + getCull().width
					|| worldCenter.y > getCull().y + getCull().height) {
				remove();
			}
		}
	}

	@Override
	public void act(float delta) {
		// TODO Auto-generated method stub
		super.act(delta);
	}

	@Override
	public boolean remove(boolean fromWorldAlso) {
		// TODO Auto-generated method stub
		return super.remove(fromWorldAlso);
	}

	public Fixture getFixture() {
		return fixture;
	}

	public void setFixture(Fixture fixture) {
		this.fixture = fixture;
		this.fixture.setUserData(this);
	}

	public Vector2 getFixtureOffset() {
		return fixtureOffset;
	}

	public void setFixtureOffset(Vector2 fixtureOffset) {
		this.fixtureOffset.set(fixtureOffset);
	}
	
}
