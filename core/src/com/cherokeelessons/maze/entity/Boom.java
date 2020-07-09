package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.cherokeelessons.maze.Effect;
import com.cherokeelessons.maze.S;
import com.cherokeelessons.maze.game.TheWorld;

public class Boom extends Entity {

	public static final int DEFAULT_MAX_BOUNCES = 16;

	private static final int MAX_LIFE_ms = 4500;
	protected static AtlasRegion r = null;

	public static AtlasRegion getRegion() {
		return r;
	}

	public static void setRegion(final AtlasRegion _r) {
		r = _r;
	}

	protected int myMaxLife = 0;

	protected int maxBounces;

	private ChainedExplosions owner;

	public Boom(final ChainedExplosions owner) {
		super(r);
		maxBounces = DEFAULT_MAX_BOUNCES;
		identity = Entity.EXPLOSION;
		setOwner(owner);
		setScale(.85f);
		layout();
		setName("Boom");
		doCullCheck = false;
		myMaxLife = MAX_LIFE_ms;
	}

	@Override
	public void act(final float delta) {
		super.act(delta);
		if (body == null) {
			return;
		}
		final long delta_ms = System.currentTimeMillis() - start;
		getOwner().setPos(this.getX(), this.getY());
		getOwner().setWorldPos(body.getWorldCenter());
		if (body.getLinearVelocity().len() < .25f && delta_ms>myMaxLife*3/4) {
			remove(true);
			return;
		}
		if (delta_ms > myMaxLife) {
			remove(true);
			return;
		}
		final float newAlpha1 = (float) (myMaxLife - delta_ms) / (float) myMaxLife * .65f + .35f;
		addAction(Actions.alpha(newAlpha1, .2f));
		for (final Entity entity : getCollidesWith()) {
			if (maxBounces-- < 1) {
				remove(true);
				return;
			}
			if (entity.identity == Entity.EXPLOSION) {
				final Vector2 impulse = new Vector2(1f, 0f);
				if (entity.body != null) {
					impulse.rotate(entity.body.getLinearVelocity().angle() + 37);
//					impulse.rotate(MathUtils.random(359));
				}
				if (S.ultimateMode) {
					maxBounces++;
//					body.setGravityScale(body.getGravityScale()*.7f);
					impulse.scl(5);
				}
				body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
			}
			if (entity.identity == Entity.BLOCK) {
				addAudio(Effect.BOOM_B);
				getOwner().boxCount++;
				getOwner().accumulator += entity.value;
				final Vector2 pos = body.getWorldCenter();
				final World world = body.getWorld();

				final BoomGenerate bg = new BoomGenerate();
				bg.setWorld(world);
				bg.setWorldScale(worldScale);
				bg.setCount(entity.value/2+2);
				bg.setWorldPosition(pos);
				bg.setLinearVelocity(body.getLinearVelocity());
				bg.setOwner(getOwner());
				bg.run();

				// prevent concurrent explosions, boxes should only explode once.
				entity.identity = Entity.EXPLOSION;
				entity.value = 0;
				entity.remove(true);
				remove(true);
				return;
			}
		}
	}

	public void addToWorld(final World world, final Vector2 pos, final Vector2 force) {
		start = System.currentTimeMillis();
		final float rad = getScaleX() * (getWidth() / 2) / worldScale;
		final BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(pos);

		setBody(world.createBody(bodyDef));
		
		Shape objectShape = new CircleShape();
		objectShape.setRadius(rad);
		FixtureDef 
		objectFixture = new FixtureDef();
		objectFixture.isSensor = false;
		objectFixture.density = 1f;
		objectFixture.friction = 1f;
		objectFixture.restitution = .1f;
		objectFixture.shape = objectShape;
		objectFixture.filter.categoryBits = categoryBits();
		objectFixture.filter.maskBits = maskBits();
		body.createFixture(objectFixture);
		objectShape.dispose();
		
		Shape sensorShape = new CircleShape();
		sensorShape.setRadius(rad);
		FixtureDef sensorFixture = new FixtureDef();
		sensorFixture.isSensor = true;
		sensorFixture.density = 1f;
		sensorFixture.friction = 1f;
		sensorFixture.restitution = .1f;
		sensorFixture.shape = sensorShape;
		sensorFixture.filter.categoryBits = sensorCategoryBits();
		sensorFixture.filter.maskBits = sensorMaskBits();
		body.createFixture(sensorFixture);
		sensorShape.dispose();

		body.setUserData(this);
		body.setBullet(true);
		body.setFixedRotation(false);
		body.setLinearVelocity(new Vector2(0f, 0f));
		body.setLinearDamping(.1f);
		body.setAngularDamping(.1f);
		body.setGravityScale(1f);
		if (S.ultimateMode) {
			body.setGravityScale(.9f);
		}
		setOffsetX(-getWidth() / 2);
		setOffsetY(-getHeight() / 2);
		setOriginX(getWidth() / 2);
		setOriginY(getHeight() / 2);
		body.applyLinearImpulse(pos, force, true);

		toFront();
	}

	public ChainedExplosions getOwner() {
		return owner;
	}

	public void setOwner(final ChainedExplosions owner) {
		this.owner = owner;
	}

	@Override
	protected short maskBits() {
		return (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_FLOOR ^ TheWorld.TYPE_EXPLOSION);
	}
	
	@Override
	protected short sensorMaskBits() {
		return (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_FLOOR);
	}

	@Override
	protected short categoryBits() {
		return TheWorld.TYPE_EXPLOSION;
	}
}
