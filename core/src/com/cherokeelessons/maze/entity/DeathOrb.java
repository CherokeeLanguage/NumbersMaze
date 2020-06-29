package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.cherokeelessons.maze.game.TheWorld;
import com.cherokeelessons.maze.game.TheWorld.AddOrphan;

public class DeathOrb extends Entity {

	private final class MyRayCastCallback implements RayCastCallback {
		private float fraction = 1f;
		private final Vector2 point = new Vector2();
		private Entity entity = null;
		private final Vector2 normal = new Vector2();

		private MyRayCastCallback() {
		}

		public Entity getEntity() {
			return entity;
		}

		public float getFraction() {
			return fraction;
		}

		public Vector2 getNormal() {
			return normal;
		}

		public Vector2 getPoint() {
			return new Vector2(point);
		}

		@Override
		public float reportRayFixture(final Fixture fixture, final Vector2 newPoint, final Vector2 newNormal,
				final float newFraction) {
			final Object o = fixture.getBody().getUserData();
			if (o != null && o instanceof Entity) {
				final Entity newEntity = (Entity) o;
				if (newEntity.identity != Entity.FLOOR) {
					if (entity == null || fraction > newFraction) {
						entity = (Entity) o;
						fraction = newFraction;
						point.set(newPoint);
						getNormal().set(newNormal);
						return newFraction;
					}
				}
			}
			return fraction;
		}

		public void reset() {
			fraction = 1f;
			point.set(0f, 0f);
			entity = null;
			normal.set(0f, 0f);
		}
	}

	private static AtlasRegion[] atlas = null;
	final private static long DEATH_ORB_LIFE_SPAN = 8000;

	public static AtlasRegion[] getAtlas() {
		return atlas;
	}

	public static long getLifeSpan() {
		return DEATH_ORB_LIFE_SPAN;
	}

	public static void setAtlas(final AtlasRegion[] atlas) {
		DeathOrb.atlas = atlas;
	}

	private TextureRegionDrawable[] trd = null;

	final private long totalFrames = 64;

	final private long totalAnimTime = 2600;

	private long expires = 0;
	private float finalRad = 0f;
	private float activeRad = 0f;

	private final float realScale = 1f;

	private int activeFrame = 0;

	private int lastFrame = -1;
	final MyRayCastCallback report_ray_cb = new MyRayCastCallback();

	boolean[] wasWall = new boolean[4];
	private int impulseDir;
	private final Array<Entity> ray_entity = new Array<>();
	private final Array<Float> ray_fraction = new Array<>();
	private final Array<Vector2> ray_point = new Array<>();
	private final Array<Vector2> ray_normal = new Array<>();

	public DeathOrb(final World world, final float worldScale, final Vector2 worldPos, @SuppressWarnings("unused") final int accumulator) {
		super();
		identity = Entity.DEATH_ORB;
		getColor().a = 0f;
		setWorldScale(worldScale);
		setName("DeathOrb");
		trd = new TextureRegionDrawable[atlas.length];
		for (int ix = 0; ix < atlas.length; ix++) {
			trd[ix] = new TextureRegionDrawable(atlas[ix]);
		}
		setDrawable(trd[0]);
		pack();
		addToWorld(world, worldPos);
		updatePosition(true);
		expires = System.currentTimeMillis() + DEATH_ORB_LIFE_SPAN;
	}

	@Override
	public void act(final float delta) {
		if (System.currentTimeMillis() > expires) {
			remove(true);
			return;
		}
		super.act(delta);
		long tick = System.currentTimeMillis() - start;
		if (tick >= totalAnimTime) {
			tick = tick % totalAnimTime;
			start = System.currentTimeMillis() - tick;
		}
		activeFrame = (int) (tick * totalFrames / totalAnimTime);
		final float newAlpha = (expires - System.currentTimeMillis()) / (float) DEATH_ORB_LIFE_SPAN;
		getColor().a = newAlpha * .65f + .35f;
		if (lastFrame != activeFrame) {
			setDrawable(trd[activeFrame]);
			pack();
			lastFrame = activeFrame;
			if (body != null) {
				if (activeRad < finalRad) {
					activeRad += .001f;
					if (activeRad > finalRad) {
						activeRad = finalRad;
					}
					float scale = activeRad / finalRad;
					if (scale > 1.0f) {
						scale = 1.0f;
					}
					setScale(scale * realScale);
					resetFixtures(activeRad, body);
				}
			}
		}

		ray_entity.clear();
		ray_fraction.clear();
		ray_point.clear();
		ray_normal.clear();
		final Vector2 endOfRay = new Vector2();
		// e, n, w, s
		for (float len = finalRad + 1.75f; len < 10f; len += 2f) {
			for (int a = 0; a < 360; a += 30) {
				endOfRay.set(len, 0f);
				endOfRay.setAngle(a);
				endOfRay.add(worldCenter);
				report_ray_cb.reset();
				body.getWorld().rayCast(report_ray_cb, worldCenter, endOfRay);
				if (report_ray_cb.getEntity() != null) {
					ray_entity.add(report_ray_cb.getEntity());
					ray_fraction.add(report_ray_cb.getFraction());
					ray_point.add(report_ray_cb.getPoint());
					ray_normal.add(report_ray_cb.getNormal());
				}
			}
			if (ray_entity.size > 1) {
				break;
			}
		}
		// 0-e, 1-n, 2-w, 3-s
		final boolean[] isWall = new boolean[4];
		isWall[0] = false;
		isWall[1] = false;
		isWall[2] = false;
		isWall[3] = false;
		final Vector2 cpos = new Vector2();
		final int gap = 30;
//		setColor(Color.WHITE);
		for (int ix = 0; ix < ray_entity.size; ix++) {
			final Entity e = ray_entity.get(ix);
			cpos.set(ray_point.get(ix));
			cpos.sub(worldCenter);
			final float a = cpos.angle();
			if (e.identity == Entity.EXPLOSION) {
				final Vector2 kill = new Vector2(body.getMass() * .5f, 0f);
				kill.setAngle(cpos.angle() + 90f);
				body.applyLinearImpulse(kill, worldCenter, true);
				continue;
			}
			if (e.identity == Entity.PLAYER) {
				final Vector2 kill = new Vector2(body.getMass() * .5f, 0f);
				kill.setAngle(cpos.angle());
				body.applyLinearImpulse(kill, worldCenter, true);
				continue;
			}
			if (e.identity == Entity.BLOCK) {
				final Vector2 kill = new Vector2(body.getMass() * .51f, 0f);
				kill.setAngle(cpos.angle());
				body.applyLinearImpulse(kill, worldCenter, true);
				continue;
			}
			if (e.identity == Entity.DEATH_ORB) {
				final Vector2 runAway = new Vector2(body.getMass() * 2f, 0f);
				runAway.setAngle(cpos.angle() - 90f);
				body.applyLinearImpulse(runAway, worldCenter, true);
			}
			if (e.identity == Entity.WALL || e.identity != Entity.DEATH_ORB) {
				if (a >= 315 + gap || a < 45 - gap) {
					isWall[0] = true;
				}
				if (a >= 45 + gap && a < 135 - gap) {
					isWall[1] = true;
				}
				if (a >= 135 + gap && a < 225 - gap) {
					isWall[2] = true;
				}
				if (a >= 225 + gap && a < 315 - gap) {
					isWall[3] = true;
				}
			}
		}
		boolean maybeChangeDir = false;
		for (int ix = 0; ix < 4; ix++) {
			if (wasWall[ix] != isWall[ix]) {
				maybeChangeDir = true;
			}
			wasWall[ix] = isWall[ix];
		}

		final Array<Integer> dir = new Array<>();
		if (isWall[impulseDir] || maybeChangeDir && MathUtils.random(5) == 0) {
			for (int ix = 0; ix < 4; ix++) {
				if (!isWall[ix]) {
					dir.add(ix);
				}
			}
			if (dir.size > 0) {
				if (dir.size > 1) {
					final int oppDir = (impulseDir + 2) % 4;
					dir.removeValue(oppDir, false);
				}
				impulseDir = dir.random();
//				body.setLinearVelocity(0f, 0f);
				final Vector2 oldVector = body.getLinearVelocity();
				oldVector.scl(.35f);
				body.setLinearVelocity(oldVector);
//				body.applyLinearImpulse(oldVector, worldCenter);
			}
		}
//		if (body.getLinearVelocity().len() > body.getMass()*20f) {
//			body.setLinearVelocity(0f, 0f);
//		}
		if (body.getLinearVelocity().len() < body.getMass() * 10f) {
			Vector2 i;
			i = new Vector2(body.getMass(), 0f).scl(.3f);
			i.rotate(impulseDir * 90f);
			body.applyLinearImpulse(i, worldCenter, true);
		}

//		if (body.getLinearVelocity().len() < .01f) {
//			Vector2 i;
//			i = new Vector2(body.getMass(), 0f);
//			i.rotate(impulseDir * 90f + 91f);
//			body.applyLinearImpulse(i, worldCenter);
//		}

//		Entity target_entity = null;
//		float target_fraction = 1f;
//		final Vector2 target_point = new Vector2();
//		final Vector2 normal_vector = new Vector2();
//		// any number blocks? pick one
//		for (int ix = 0; ix < ray_entity.size(); ix++) {
//			if (ray_entity.get(ix).identity == Entity.BLOCK) {
//				boolean thisOne;
//				if (doFarthest) {
//					thisOne = target_fraction < ray_fraction.get(ix);
//				} else {
//					thisOne = target_fraction > ray_fraction.get(ix);
//				}
//				if (target_entity == null || thisOne) {
//					target_entity = ray_entity.get(ix);
//					target_fraction = ray_fraction.get(ix);
//					target_point.set(ray_point.get(ix));
//					normal_vector.set(ray_normal.get(ix));
//				}
//			}
//		}
//
//		// if no number blocks, scan for non-floor
//		if (target_entity == null) {
//			for (int ix = 0; ix < ray_entity.size(); ix++) {
//				boolean thisOne;
//				if (doFarthest) {
//					thisOne = target_fraction < ray_fraction.get(ix);
//				} else {
//					thisOne = target_fraction > ray_fraction.get(ix);
//				}
//				if (target_entity == null || thisOne) {
//					target_entity = ray_entity.get(ix);
//					target_fraction = ray_fraction.get(ix);
//					target_point.set(ray_point.get(ix));
//					normal_vector.set(ray_normal.get(ix));
//				}
//			}
//		}
//		if (target_entity != null) { // && body.getLinearVelocity().len()<1f) {
//			do {
//				float dst=target_point.dst(worldCenter);
////				if (dst>0f) {
////					dst=1f/dst;
////				}
//				if (target_entity.identity == Entity.BLOCK) {
//					final Vector2 p1 = new Vector2();
//					final Vector2 p2 = new Vector2();
//					p1.set(worldCenter);
//					p2.set(target_point);
//					float a = p2.sub(p1).angle();
//					p1.set(body.getMass()*.5f, 0f);
//					p1.setAngle(a);
//					body.applyLinearImpulse(p1, worldCenter);
//					break;
//				}
//				if (target_entity.identity == Entity.EXPLOSION) {
//					final Vector2 p1 = new Vector2();
//					final Vector2 p2 = new Vector2();
//					p1.set(worldCenter);
//					p2.set(target_point);
//					float a = p2.sub(p1).angle();
//					p1.set(body.getMass()*.25f, 0f);
//					p1.setAngle(a);
//					body.applyLinearImpulse(p1, worldCenter);
//					break;
//				}
//				final Vector2 p1 = new Vector2();
//				final Vector2 p2 = new Vector2();
//				p1.set(worldCenter);
//				p2.set(target_point);
//				p2.sub(p1);
//				float a=p2.angle();
//				p1.set(body.getMass(), 0f);
//				Gdx.app.log("doFarthest", doFarthest+", "+doFarthestCounter);
//				p1.setAngle(a+90f);
//				body.applyLinearImpulse(p1, worldCenter);
//				if (doFarthest) {
//					p1.mul(dst);
//					p1.setAngle(a);//165f + MathUtils.random(30f));
//					body.applyLinearImpulse(p1, worldCenter);
//				} else {
//					p1.setAngle(a+180f);//165f + MathUtils.random(30f));
//					body.applyLinearImpulse(p1, worldCenter);
//					body.setGravityScale(-body.getGravityScale());
//				}
//				//body.applyLinearImpulse(p1, worldCenter);
//				break;
//			} while (false);
//		}
		for (int ix = 0; ix < collidesWith.size; ix++) {
			final Entity e = collidesWith.get(ix);
			if (e.identity == Entity.BLOCK) {
				Gdx.app.postRunnable(new Runnable() {
					final float scale = worldScale;
					final World world = DeathOrb.this.body.getWorld();
					final Vector2 vel = new Vector2().set(body.getLinearVelocity());
					final Vector2 pos = worldCenter;

					@Override
					public void run() {
						final AddOrphan orphan = new AddOrphan();
						final ArrowGroup arrowGroup = orphan.arrowGroup;
						final BoomGenerate bg = new BoomGenerate();
						bg.setWorld(world);
						bg.setWorldScale(scale);
						bg.setCount(7);
						bg.setWorldPosition(pos);
						bg.setLinearVelocity(vel);
						bg.setOwner(arrowGroup);
						bg.run();
						TheWorld.post(orphan);
					}
				});
				remove(true);
				break;
			}
		}
	}

	public void addToWorld(final World world, final Vector2 worldPos) {
		start = System.currentTimeMillis();
		finalRad = .45f * (getWidth() / 2) / worldScale;
		activeRad = finalRad / 2f;
		final BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(worldPos);

		final Body newBody = world.createBody(bodyDef);
		setBody(newBody);
		resetFixtures(activeRad, newBody);
		newBody.setUserData(this);
		newBody.setBullet(true);
		newBody.setFixedRotation(true);
		newBody.setLinearDamping(1f);
		newBody.setAngularDamping(1f);
//		body.applyAngularImpulse(.01f);
		newBody.setGravityScale(0f);
		setOffsetX(-getWidth() / 2);
		setOffsetY(-getHeight() / 2);
		setOriginX(getWidth() / 2);
		setOriginY(getHeight() / 2);
		newBody.setActive(true);
		newBody.setLinearVelocity(0f, 0f);
	}

	private void resetFixtures(final float rad, final Body bodyToReset) {
		final Array<Fixture> f = new Array<>();
		f.addAll(bodyToReset.getFixtureList());
		for (final Fixture fix : f) {
			bodyToReset.destroyFixture(fix);
		}
		CircleShape circle;
		FixtureDef fDef;
		//final Vector2 offset = new Vector2(rad * 2, 0);

		circle = new CircleShape();
		circle.setRadius(rad);
		fDef = new FixtureDef();
		fDef.isSensor = false;
		fDef.density = 1f;
		fDef.friction = 0f;
		fDef.restitution = 1f;
		fDef.shape = circle;
		fDef.filter.categoryBits = categoryBits();
		fDef.filter.maskBits = maskBits();
		bodyToReset.createFixture(fDef);
		circle.dispose();
	}

	@Override
	protected short maskBits() {
		return (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_FLOOR ^ TheWorld.TYPE_PLAYER
				^ TheWorld.TYPE_ENEMY);
	}

	@Override
	protected short sensorMaskBits() {
		return 0;
	}

	@Override
	protected short categoryBits() {
		return TheWorld.TYPE_ENEMY;
	}
}
