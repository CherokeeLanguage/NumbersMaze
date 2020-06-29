package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.math.Vector2;
import com.cherokeelessons.maze.game.TheWorld;

public class PlayerBoom extends Boom {

	public PlayerBoom(ArrowGroup owner) {
		super(owner);
		maxBounces=DEFAULT_MAX_BOUNCES*2;
		identity = Entity.ARROW;
		setName("Arrow");
		doCullCheck = false;
	}
	
	@Override
	protected short categoryBits() {
		return TheWorld.TYPE_ARROW;
	}

	@Override
	protected short maskBits() {
		return (short) (TheWorld.TYPE_EXPLOSION | TheWorld.TYPE_ARROW | TheWorld.TYPE_BLOCK
				| TheWorld.TYPE_SENSOR | TheWorld.TYPE_WALL);
	}

	public void fire(Vector2 impulse) {
		start = System.currentTimeMillis();
		if (body == null) {
			return;
		}
		setWorldRotation(impulse.angle());
		body.applyLinearImpulse(impulse, body.getWorldCenter(), true);	
		body.setAngularVelocity(0);
	}
}
