package com.cherokeelessons.maze.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.cherokeelessons.maze.Effect;
import com.cherokeelessons.maze.S;
import com.cherokeelessons.maze.game.TheWorld;

public class Player extends Entity {

	final public static int NORTH = 0;

	final public static int EAST = 1;
	final public static int WEST = 2;

	final public static int SOUTH = 3;
	public BitmapFont scoreFont = null;

	private final Random r = new Random();
	public int theChallenge = 1;
	public int myScore = 0;
	private int badAccumulator = 0;
	private Array<Integer> dieDeck = new Array<>();

	private static class HeldBlock {
		public HeldBlock(Entity box, Joint joint) {
			this.box = box;
			this.joint = joint;
		}

		private Entity box;
		private Joint joint;
	}

	private final List<HeldBlock> holding;

	final private Vector2 aabb_size = new Vector2();

	final private Vector2 aabb_lower = new Vector2();
	final private Vector2 aabb_upper = new Vector2();
	private int pendingScore = 0;

	final Vector2 impulse = new Vector2();

	public PlayerInput gamepad = new PlayerInput();

	private AtlasRegion[][] ar;

	private final ArrayList<ArrowGroup> arrowGroupTracker = new ArrayList<>();
	private int lastDir = -1;

	private final int lastX = -1;
	private final int lastY = -1;

	public Player() {
		super();
		holding = new ArrayList<>();
		r.setSeed(0);
		final TextureAtlas atlas = S.getPar().playerAtlas;
		ar = new AtlasRegion[4][4];
		for (int ix = 0; ix < 4; ix++) {
			ar[NORTH][ix] = atlas.findRegion("north-" + ix);
			ar[EAST][ix] = atlas.findRegion("east-" + ix);
			ar[WEST][ix] = atlas.findRegion("west-" + ix);
			ar[SOUTH][ix] = atlas.findRegion("south-" + ix);
		}
		showAvatar(SOUTH);
		identity = Entity.PLAYER;
		setName("Player");
	}

	public Player(final AtlasRegion ar) {
		super(ar);
		holding = new ArrayList<>();
		r.setSeed(0);
	}

	@Override
	public void act(final float delta) {
		super.act(delta);
		if (body == null) {
			return;
		}

		for (int ix = arrowGroupTracker.size() - 1; ix >= 0; ix--) {
			final ArrowGroup arrowGroup = arrowGroupTracker.get(ix);
			if (arrowGroup.group.getChildren().size < 1) {
				if (arrowGroup.boxCount > 0) {
					if (arrowGroup.accumulator == theChallenge) {
						final int newPoints = arrowGroup.boxCount * arrowGroup.accumulator;
						addToPendingScore(newPoints);
						if (scoreFont != null) {
							final Vector2 p = arrowGroup.getPos();
							final ScorePopup s = new ScorePopup(newPoints, p.x + 16, p.y + 16, scoreFont);
							getStage().addActor(s.getLabel());
						}
					} else {
						Gdx.app.log(this.getClass().getSimpleName(), "DEATH ORB!");
						badAccumulator += arrowGroup.accumulator;
						final DeathOrb orb = new DeathOrb(body.getWorld(), worldScale, arrowGroup.getWorldPos(),
								arrowGroup.accumulator);
						getStage().addActor(orb);
					}
				}
				arrowGroup.group.remove();
				arrowGroupTracker.remove(ix);
			}
		}

		aabb_lower.x = worldCenter.x - aabb_size.x / 2;
		aabb_lower.y = worldCenter.y - aabb_size.y / 2;
		aabb_upper.x = worldCenter.x + aabb_size.x / 2;
		aabb_upper.y = worldCenter.y + aabb_size.y / 2;

		if (gamepad.btn_x && holding.size()>3) {
			cleanHeldList();
			if (holding.size()>2) {
				gamepad.btn_x=false; //mark as handled
				addAudio(Effect.PLINK);
			}
		}
		// if button X just now pressed down try to grab a box!
		if (gamepad.btn_x) {
			gamepad.btn_x = false; // mark as handled
			
			cleanHeldList();
			
			// grab the box (if any)
			Entity box = null;
			// AABB search to ensure we have any boxes in our sights!
			body.getWorld().QueryAABB(new QueryCallback() {
				@Override
				public boolean reportFixture(final Fixture fixture) {
					final Object a = fixture.getBody().getUserData();
					if (!Entity.class.isAssignableFrom(a.getClass())) {
						return true;
					}
					final Entity e = (Entity) a;
					if (e.identity == Entity.BLOCK) {
						getCollidesWith().add(e);
						return true; // keep looking
					}
					return true;
				}
			}, aabb_lower.x, aabb_lower.y, aabb_upper.x, aabb_upper.y);
			// find nearest collides with
			float d = 0;
			scan: for (final Entity item : getCollidesWith()) {
				for (HeldBlock held: holding) {
					if (held.box == item) {
						continue scan;
					}
				}
				if (item.identity == Entity.BLOCK) {
					final float d2 = worldCenter.dst(item.getBody().getWorldCenter());
					if (d2 < d || box == null) {
						box = item;
						d = d2;
					}
				}
			}

			if (box != null) {
				addAudio(Effect.PICK_UP);
				box.getColor().a = .4f;
				final RopeJointDef jdef = new RopeJointDef();
				if (holding.isEmpty()) {
					jdef.bodyA = body;
					jdef.maxLength = 2f;
				} else {
					jdef.bodyA = body;
					jdef.maxLength = 2f + holding.size()/3f;
				}
				jdef.bodyB = box.body;
				jdef.collideConnected = false;
				jdef.localAnchorA.x = body.getLocalCenter().x;
				jdef.localAnchorA.y = body.getLocalCenter().y;
				jdef.localAnchorB.x = box.body.getLocalCenter().x;
				jdef.localAnchorB.y = box.body.getLocalCenter().y;
				Joint createdJoint = body.getWorld().createJoint(jdef);
				final Array<Fixture> f = box.body.getFixtureList();
				for (final Fixture af : f) {
					af.setSensor(false);
				}
				holding.add(0, new HeldBlock(box, createdJoint));
				box.toFront();
				toFront();
			}
		}

		/*
		 * Drop all held items.
		 */
		if (gamepad.btn_y) {
			gamepad.btn_y = false; // mark as handled
			cleanHeldList();
			Array<Joint> worldJoints = new Array<>();
			body.getWorld().getJoints(worldJoints);
			Iterator<HeldBlock> iterator = holding.iterator();
			while (iterator.hasNext()) {
				HeldBlock held = iterator.next();
				if (worldJoints.contains(held.joint, true)) {
					addAudio(Effect.DROP_IT);
					body.getWorld().destroyJoint(held.joint);
				}
				if (held.box.body != null) {
					held.box.body.setGravityScale(1f);
					final Array<Fixture> f = held.box.body.getFixtureList();
					for (final Fixture af : f) {
						af.setSensor(false);
					}
					held.box.body.setFixedRotation(false);
				}
				held.box.getColor().a = 1f;
				iterator.remove();
			}
		}

		impulse.x = 0.7f * gamepad.deltaX;
		impulse.y = 0.7f * gamepad.deltaY;

		// change graphic and such to match new movement impulse
		// determine direction based on magnitude and update texture being
		// displayed.
		float xd = gamepad.deltaX;
		if (xd < 0) {
			xd = -xd;
		}
		float yd = gamepad.deltaY;
		if (yd < 0) {
			yd = -yd;
		}

		if (xd > yd) {
			if (gamepad.deltaX < 0) {
				showAvatar(WEST);
			} else if (gamepad.deltaX > 0) {
				showAvatar(EAST);
			}
		} else {
			if (gamepad.deltaY < 0) {
				showAvatar(SOUTH);
			} else if (gamepad.deltaY > 0) {
				showAvatar(NORTH);
			}
		}

		body.applyLinearImpulse(impulse, worldCenter, true);

		if (gamepad.btn_a) {
			gamepad.btn_a = false; // mark as handled
			cleanHeldList();
			final Vector2 fireImpulse;
			switch (lastDir) {
			case EAST:
				fireImpulse = new Vector2(4f, 0f);
				break;
			case WEST:
				fireImpulse = new Vector2(4f, 0f);
				fireImpulse.rotate(180);
				break;
			case NORTH:
				fireImpulse = new Vector2(8f, 0f);
				fireImpulse.rotate(90);
				break;
			case SOUTH:
				fireImpulse = new Vector2(1f, 0f);
				fireImpulse.rotate(-90);
				break;
			default:
				fireImpulse = new Vector2(1f, 0f);
				break;
			}
			final ArrowGroup arrowGroup = new ArrowGroup();
			arrowGroupTracker.add(arrowGroup);

			addBoom(arrowGroup, fireImpulse);
		}

	}

	private void cleanHeldList() {
		Array<Joint> worldJoints = new Array<>();
		body.getWorld().getJoints(worldJoints);
		Iterator<HeldBlock> iterator = holding.iterator();
		while (iterator.hasNext()) {
			HeldBlock held = iterator.next();
			if (!worldJoints.contains(held.joint, true)) {
				if (held.box.body!=null) {
					held.box.body.setGravityScale(1f);
					final Array<Fixture> f = held.box.body.getFixtureList();
					for (final Fixture af : f) {
						af.setSensor(false);
					}
					held.box.body.setFixedRotation(false);
				}
				held.box.getColor().a = 1f;
				iterator.remove();
				continue;
			}
			if (held.box.body == null) {
				iterator.remove();
				continue;
			}
		}
	}

	private void addBoom(final ArrowGroup arrowGroup, final Vector2 boomImpulse) {
		final PlayerBoom a = new PlayerBoom(arrowGroup);
		a.setWorldScale(worldScale);
		arrowGroup.group.addActor(a);
		getStage().addActor(arrowGroup.group);
		a.addToWorld(body.getWorld(), body.getWorldCenter(), boomImpulse);
		a.setOwner(arrowGroup);
		a.body.setLinearVelocity(body.getLinearVelocity().cpy().scl(4f));
		a.fire(boomImpulse);
		a.toFront();
	}

	public void addToPendingScore(final int addToScore) {
		synchronized (this) {
			this.pendingScore += addToScore;
		}
	}

	public void addToWorld(final TheWorld world) {
		r.setSeed(theChallenge);
		final float wx = 32 / worldScale;
		final float wy = 32 / worldScale;
		final BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(wx, wy);
		body = world.getWorld().createBody(bodyDef);
		setBody(body);
		body.setUserData(this);
		body.setFixedRotation(true);
		body.setLinearVelocity(new Vector2(0f, 0f));
		body.setLinearDamping(4.5f);
		body.setAngularDamping(4.5f);
		showAvatar(lastDir);
	}

	public void badValue_add(final int addValue) {
		badAccumulator += addValue;
	}

	public void badValue_clear() {
		badAccumulator = 0;
	}

	public int badValue_getNext(final int minFaceValue, final int maxFaceValue) {
		int gbv = 0;
		if (badAccumulator < 1) {
			return 1;
		}
		if (badAccumulator == 1) {
			badAccumulator = 0;
			return 1;
		}
		do {
			if (dieDeck.size == 0) {
				int sides = 8;
				for (int i = 1; i <= sides; i++) {
					if (i == 7 && (20 > badAccumulator || 20 > maxFaceValue)) {
						continue;
					}
					if (i == 8 && (80 > badAccumulator || 80 > maxFaceValue)) {
						continue;
					}
					if (i > maxFaceValue) {
						Gdx.app.log(this.getClass().getSimpleName(), "i > maxFaceValue: " + i + ">" + maxFaceValue);
						continue;
					}
					if (i > badAccumulator) {
						Gdx.app.log(this.getClass().getSimpleName(), "i > badAccumulator: " + i + ">" + badAccumulator);
						continue;
					}
					dieDeck.add(i);
					Gdx.app.log(this.getClass().getSimpleName(), "X Die Deck: " + dieDeck.toString());
				}
				Gdx.app.log(this.getClass().getSimpleName(), "Y Die Deck: " + dieDeck.toString());
				dieDeck.sort();
				dieDeck.reverse();

				if (minFaceValue > 6 && dieDeck.size > 3 && new Random().nextInt(100) > 10) {
					dieDeck.sort();
					dieDeck.reverse();
					dieDeck.removeRange(3, dieDeck.size - 1);
				}

				Gdx.app.log(this.getClass().getSimpleName(), "Die Deck: " + dieDeck.toString());
			}
			if (dieDeck.isEmpty()) {
				return 0;
			}
			dieDeck.shuffle();
			gbv = dieDeck.removeIndex(0);
			// convert die face into VALUE
			if (gbv == 7) {
				gbv = 20;
			}
			if (gbv == 8) {
				gbv = 80;
			}
		} while (gbv > badAccumulator);
		badAccumulator -= gbv;
		if (badAccumulator < 0) {
			badAccumulator = 0;
		}
		return gbv > 0 ? gbv : 0;
	}

	public int badValue_getPending() {
		return badAccumulator;
	}

	public boolean badValue_hasPending() {
		return badAccumulator > 0;
	}

	public int getPendingScore() {
		return pendingScore;
	}

	public int getPendingScoreAndZeroOut() {
		int tmp;
		synchronized (this) {
			tmp = pendingScore;
			pendingScore = 0;
		}
		return tmp;
	}

	public boolean pendingArrows() {
		return false;
	}

	public int pointsInLimbo() {
		int total = 0;
		for (int ix = arrowGroupTracker.size() - 1; ix >= 0; ix--) {
			total += arrowGroupTracker.get(ix).accumulator;
		}
		return total;
	}

	private void resetFixtures(final float regionWidth, final float regionHeight) {
		if (body != null) {
			final Array<Fixture> f = new Array<>();
			f.addAll(body.getFixtureList());
			for (final Fixture fixture : f) {
				body.destroyFixture(fixture);
			}
			f.clear();

			final float w = (regionWidth - 1) / worldScale;
			final float h = (regionHeight - 1) / worldScale;

			aabb_size.x = w;
			aabb_size.y = h;

			float rad;
			if (w < h) {
				rad = w / 4;
			} else {
				rad = h / 4;
			}

			FixtureDef fDef;

			final CircleShape circle = new CircleShape();
			circle.setRadius(rad);

			// TC
			circle.setPosition(new Vector2(w / 2, rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.2f;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = categoryBits();
			fDef.filter.maskBits = maskBits();
			body.createFixture(fDef);
			// BC
			circle.setPosition(new Vector2(w / 2, h - rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.2f;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = categoryBits();
			fDef.filter.maskBits = maskBits();
			body.createFixture(fDef);
			// LC
			circle.setPosition(new Vector2(rad, h / 2));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.2f;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = categoryBits();
			fDef.filter.maskBits = maskBits();
			body.createFixture(fDef);
			// RC
			circle.setPosition(new Vector2(w - rad, h / 2));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.2f;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = categoryBits();
			fDef.filter.maskBits = maskBits();
			body.createFixture(fDef);
			// TL
			circle.setPosition(new Vector2(rad, rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.2f;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = categoryBits();
			fDef.filter.maskBits = maskBits();
			body.createFixture(fDef);
			// TR
			circle.setPosition(new Vector2(w - rad, rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.2f;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = categoryBits();
			fDef.filter.maskBits = maskBits();
			body.createFixture(fDef);
			// BL
			circle.setPosition(new Vector2(rad, h - rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.2f;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = categoryBits();
			fDef.filter.maskBits = maskBits();
			body.createFixture(fDef);
			// BR
			circle.setPosition(new Vector2(w - rad, h - rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.2f;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = categoryBits();
			fDef.filter.maskBits = maskBits();
			body.createFixture(fDef);
			circle.dispose();
		}
	}

	@Override
	public void setBody(final Body body) {
		super.setBody(body);
		if (body == null) {
			return;
		}
		resetFixtures(getWidth(), getHeight());
		body.setGravityScale(0f);
	}

	public void setPendingScore(final int pendingScore) {
		synchronized (this) {
			this.pendingScore = pendingScore;
		}
	}

	private void showAvatar(final int dir) {
		if (dir < 0 || dir > 3) {
			Gdx.app.log(this.getClass().getSimpleName(), "Bad dir: " + dir);
			return;
		}

		if (lastDir == dir && lastX == getX() && lastY == getY()) {
			return;
		}
		int i = 0;
		if (dir == NORTH || dir == SOUTH) {
			i = (int) (getY() % (58 / 4)) % 4;
		}
		if (dir == EAST || dir == WEST) {
			i = (int) (getX() / (38 / 4)) % 4;
		}
		if (i < 0) {
			i = -i;
		}
		if (ar[dir][i] != null) {
			lastDir = dir;
			final TextureRegionDrawable d = new TextureRegionDrawable(ar[dir][i]);
			setDrawable(d);
			final int regionWidth = d.getRegion().getRegionWidth();
			setWidth(regionWidth);
			final int regionHeight = d.getRegion().getRegionHeight();
			setHeight(regionHeight);
			setOrigin(regionWidth / 2, regionHeight / 2);
			setOffsetX(-regionWidth / 2);
			setOffsetY(-regionHeight / 2);
			resetFixtures(regionWidth, regionHeight);
		} else {
			Gdx.app.log(this.getClass().getSimpleName(), "DIR IS NULL: " + dir + ", i: " + i);
		}
	}

	@Override
	protected short maskBits() {
		return (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_BLOCK);
	}

	@Override
	protected short sensorMaskBits() {
		return 0;
	}

	@Override
	protected short categoryBits() {
		return TheWorld.TYPE_PLAYER;
	}
}
