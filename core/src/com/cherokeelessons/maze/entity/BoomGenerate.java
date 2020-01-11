package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;

public class BoomGenerate implements Runnable {

	@Override
	public void run() {
		if (count<1) count=1;
		float i=45f;
		if (count>0) i=360f/count;
		int startingAngle=MathUtils.random(360);
		for (float angle = startingAngle; angle < (startingAngle+360f); angle += i) {
			final Boom b = new Boom(owner);
			final Vector2 force = new Vector2(16f+count, 0);
			final Vector2 pos = new Vector2(worldPosition);
			force.setAngle(angle);
			b.setWorldScale(worldScale);
			b.addToWorld(world, worldPosition, new Vector2(0,0));
			b.updatePosition(true);
			owner.group.addActor(b);
			b.getColor().a=0f;
			b.body.setActive(false);
			DelayAction da = Actions.delay((angle-startingAngle)/2880f, Actions.run(new Runnable() {
				@Override
				public void run() {
					try {
						b.body.setActive(true);
						b.body.setLinearVelocity(0f, 0f);
						b.body.applyLinearImpulse(force, pos, true);
						b.getColor().a=1f;
					} catch (Exception e) {
					}
				}
			}));
			b.addAction(da);
		}		
	}
	private World world=null;
	public void setWorld(World world){
		this.world=world;
	}
	private float worldScale=1f;
	public void setWorldScale(float worldScale) {
		this.worldScale=worldScale;		
	}
	private float count=9;
	public void setCount(int value) {
		count=value;		
	}
	private Vector2 worldPosition=new Vector2();
	public void setWorldPosition(Vector2 pos) {
		worldPosition.set(pos);		
	}
	private Vector2 linearVelocity=new Vector2();
	public void setLinearVelocity(Vector2 linearVelocity) {
		this.linearVelocity.set(linearVelocity);		
	}
	ArrowGroup owner=new ArrowGroup();
	public void setOwner(ArrowGroup group) {
		owner=group;		
	}

}
