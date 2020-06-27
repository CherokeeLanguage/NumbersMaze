package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.cherokeelessons.maze.Effect;
import com.cherokeelessons.maze.S;
import com.cherokeelessons.maze.game.TheWorld;

public class Boom extends Entity {

	private static final int initial_maxBounces = 16;

	private static final int maxLife_ms = 4500;
	protected static AtlasRegion r = null;

	public static AtlasRegion getRegion() {
		return r;
	}

	public static void setRegion(final AtlasRegion _r) {
		r = _r;
	}

	protected int myMaxLife = 0;

	protected int maxBounces = initial_maxBounces;

	private ArrowGroup owner;

	private long start = 0;

	public Boom(final ArrowGroup owner) {
		super(r);
		identity = Entity.EXPLOSION;
		setOwner(owner);
		setScale(.85f);
		layout();
		setName("Boom");
		doCullCheck = false;
		myMaxLife = maxLife_ms;
	}

	@Override
	public void act(final float delta) {
		super.act(delta);
		if (body == null) {
			return;
		}
		getOwner().setPos(this.getX(), this.getY());
		getOwner().setWorldPos(body.getWorldCenter());
		if (body.getLinearVelocity().len() < .25f) {
			remove(true);
			return;
		}
		final long delta_ms = System.currentTimeMillis() - start;
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
				final Vector2 impulse = new Vector2(2f, 0f);
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
				bg.setCount(entity.value);
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

		final Body body = world.createBody(bodyDef);
		setBody(body);

		CircleShape circle;
		FixtureDef fDef;

		circle = new CircleShape();
		circle.setRadius(rad);
		fDef = new FixtureDef();
		fDef.isSensor = false;
		fDef.density = 1f;
		fDef.friction = 1f;
		fDef.restitution = .1f;
		fDef.shape = circle;
		fDef.filter.categoryBits = TheWorld.TYPE_EXPLOSION;
		fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_FLOOR ^ TheWorld.TYPE_EXPLOSION);
		body.createFixture(fDef);

		circle = new CircleShape();
		circle.setRadius(rad);
		fDef = new FixtureDef();
		fDef.isSensor = true;
		fDef.density = 1f;
		fDef.friction = 1f;
		fDef.restitution = .1f;
		fDef.shape = circle;
		fDef.filter.categoryBits = TheWorld.TYPE_EXPLOSION;
		fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_FLOOR);
		body.createFixture(fDef);

		circle.dispose();

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

	public ArrowGroup getOwner() {
		return owner;
	}

	public void setOwner(final ArrowGroup owner) {
		this.owner = owner;
	}
}
