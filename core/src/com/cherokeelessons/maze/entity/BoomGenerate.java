package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;

public class BoomGenerate implements Runnable {

	private World world = null;
	private float worldScale = 1f;
	private float count = 9;
	private final Vector2 worldPosition = new Vector2();
	private final Vector2 linearVelocity = new Vector2();
	ChainedExplosions owner = new ChainedExplosions();

	@Override
	public void run() {
		if (count < 1) {
			count = 1;
		}
		float i = 45f;
		if (count > 0) {
			i = 360f / count;
		}
		final int startingAngle = MathUtils.random(360);
		for (float angle = startingAngle; angle < startingAngle + 360f; angle += i) {
			final Boom b = new Boom(owner);
			final Vector2 force = new Vector2(count, 0);
			final Vector2 pos = new Vector2(worldPosition);
			force.setAngle(angle);
			b.setWorldScale(worldScale);
			b.addToWorld(world, worldPosition, new Vector2(0, 0));
			b.updatePosition(true);
			owner.group.addActor(b);
			b.getColor().a = 0f;
			b.body.setActive(false);
			final DelayAction da = Actions.delay((angle - startingAngle) / 2880f, Actions.run(new Runnable() {
				@Override
				public void run() {
					try {
						b.body.setActive(true);
						b.body.setLinearVelocity(0f, 0f);
						b.body.applyLinearImpulse(force, pos, true);
						b.getColor().a = 1f;
					} catch (final Exception e) {
						//ignore
					}
				}
			}));
			b.addAction(da);
		}
	}

	public void setCount(final int value) {
		count = value;
	}

	public void setLinearVelocity(final Vector2 linearVelocity) {
		this.linearVelocity.set(linearVelocity);
	}

	public void setOwner(final ChainedExplosions group) {
		owner = group;
	}

	public void setWorld(final World world) {
		this.world = world;
	}

	public void setWorldPosition(final Vector2 pos) {
		worldPosition.set(pos);
	}

	public void setWorldScale(final float worldScale) {
		this.worldScale = worldScale;
	}

}
