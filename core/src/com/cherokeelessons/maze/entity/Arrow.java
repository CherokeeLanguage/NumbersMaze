package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.cherokeelessons.maze.Effect;
import com.cherokeelessons.maze.game.TheWorld;

public class Arrow extends Entity {

	protected static AtlasRegion r = null;

	public static AtlasRegion getRegion() {
		return r;
	}

	public static void setRegion(final AtlasRegion _r) {
		r = _r;
	}

	int maxBounces = 0;

	private ChainedExplosions owner = null;

	public Arrow() {
		super(r);
		identity = Entity.ARROW;
		setName("Arrow");
		doCullCheck = false;
	}

	@Override
	public void act(final float delta) {
		super.act(delta);
		if (System.currentTimeMillis() - start > 3000) {
			remove(true);
		}
		if (body == null) {
			return;
		}
		for (final Entity entity : getCollidesWith()) {
			if (entity.identity == Entity.EXPLOSION) {
				entity.remove(true);
				remove(true);
				return;
			}
			if (entity.identity == Entity.BLOCK) {
				addAudio(Effect.BOOM_A);
				getOwner().boxCount++;
				getOwner().accumulator += entity.value;
				final Vector2 pos = body.getWorldCenter();
				final World world = body.getWorld();
				// Boom b;
				// Vector2 force;
				// Vector2 source = body.getLinearVelocity();
				final BoomGenerate bg = new BoomGenerate();
				bg.setWorld(world);
				bg.setWorldScale(worldScale);
				bg.setCount(entity.value);
				bg.setWorldPosition(pos);
				bg.setLinearVelocity(body.getLinearVelocity());
				bg.setOwner(getOwner());
				bg.run();
				entity.identity = Entity.EXPLOSION;
				entity.value = 0;
				entity.remove(true);
				remove(true);
				return;
			}
		}
		if (getCollidesWith().size > 0 && maxBounces-- < 1) {
			for (final Entity e : getCollidesWith()) {
				if (e.identity == Entity.FLOOR) {
					break;
				}
				if (e.identity == Entity.PORTAL) {
					break;
				}
				if (e.identity == Entity.ARROW) {
					break;
				}
				remove(true);
				return;
			}
		}
	}

//	ArrayList<Entity> toRemove = new ArrayList<Entity>();

	public void addToWorld(final World world, final Vector2 pos, final ChainedExplosions newOwner) {
		this.setOwner(newOwner);
		final BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(pos);
		final Body newBody = world.createBody(bodyDef);
		setBody(newBody);
		final PolygonShape box = new PolygonShape();
		box.setAsBox((getWidth() - 1) / 2 / worldScale, (getHeight() - 1) / 2 / worldScale);
		final FixtureDef fDef = new FixtureDef();
		fDef.density = 2f;
		fDef.friction = 0f;
		fDef.restitution = -1f;
		fDef.shape = box;
		fDef.filter.categoryBits = categoryBits();
		fDef.filter.maskBits = maskBits();
		newBody.createFixture(fDef);
		box.dispose();
		
		newBody.setUserData(this);
		newBody.setBullet(true);
		newBody.setFixedRotation(false);
		newBody.setLinearVelocity(new Vector2(0f, 0f));
		newBody.setLinearDamping(0f);
		newBody.setAngularDamping(10f);
		newBody.setGravityScale(.25f);
		setOffsetX(-getWidth() / 2);
		setOffsetY(-getHeight() / 2);
		setOriginX(getWidth() / 2);
		setOriginY(getHeight() / 2);
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

	public void fire(final Vector2 impulse) {
		start = System.currentTimeMillis();
		if (body == null) {
			return;
		}
		setWorldRotation(impulse.angle());
		body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
	}

	public ChainedExplosions getOwner() {
		return owner;
	}

	public void setOwner(final ChainedExplosions owner) {
		this.owner = owner;
	}
}
