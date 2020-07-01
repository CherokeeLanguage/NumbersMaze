package com.cherokeelessons.maze.entity;

import java.util.ArrayList;
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
	Array<Integer> dieDeck = new Array<>();
	private Entity holding = null;

	private Joint joint = null;
	private boolean prev_btn_x = false;
	private boolean prev_btn_a = false;
	private boolean toggledOn = false;

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

		toggledOn = false;
//		toggledOff = false;
		if (!prev_btn_x && gamepad.btn_x) {
			toggledOn = true;
		}
		if (prev_btn_x && !gamepad.btn_x) {
//			toggledOff = true;
		}

		aabb_lower.x = worldCenter.x - aabb_size.x / 2;
		aabb_lower.y = worldCenter.y - aabb_size.y / 2;
		aabb_upper.x = worldCenter.x + aabb_size.x / 2;
		aabb_upper.y = worldCenter.y + aabb_size.y / 2;

		// if (not carrying) && (button X just now pressed down) try to grab a
		// box!
		if (joint == null && toggledOn) {
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
			// find first collides with
			float d = 0;
			for (final Entity item : getCollidesWith()) {
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
				holding = box;
//				holding.setColor(Color.RED);
				holding.getColor().a = .4f;
//				holding.body.setTransform(holding.body.getWorldCenter(), 0f);
				final RopeJointDef jdef = new RopeJointDef();
				jdef.bodyA = body;
				jdef.bodyB = holding.body;
				jdef.collideConnected = false;
				jdef.maxLength = 2f;
				jdef.localAnchorA.x = body.getLocalCenter().x;
				jdef.localAnchorA.y = body.getLocalCenter().y;
				jdef.localAnchorB.x = holding.body.getLocalCenter().x;
				jdef.localAnchorB.y = holding.body.getLocalCenter().y;
				joint = body.getWorld().createJoint(jdef);
//				holding.body.setTransform(worldCenter, 0f);
				final Array<Fixture> f = holding.body.getFixtureList();
				for (final Fixture af : f) {
					af.setSensor(false);
				}
				holding.toFront();
				toFront();
			}
		} else if (joint != null && toggledOn) {
			final Array<Joint> joints = new Array<>();
			body.getWorld().getJoints(joints);
			for (final Joint j : joints) {
				if (j.equals(joint)) {
					addAudio(Effect.DROP_IT);
					body.getWorld().destroyJoint(joint);
					holding.body.setGravityScale(1f);
					final Array<Fixture> f = holding.body.getFixtureList();
					for (final Fixture af : f) {
						af.setSensor(false);
					}
					holding.body.setFixedRotation(false);
//					holding.setColor(Color.WHITE);
					holding.getColor().a = 1f;
				}
			}
			holding = null;
			joint = null;
		}

//		impulse.x = 0.75f * gamepad.deltaX;
//		impulse.y = 0.75f * gamepad.deltaY;
		
		impulse.x = 0.7f * gamepad.deltaX;
		impulse.y = 0.7f * gamepad.deltaY;
		
//		impulse.x = 1f * gamepad.deltaX;
//		impulse.y = 1f * gamepad.deltaY;

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

		if (gamepad.btn_a && !prev_btn_a) {
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
			// ff.add();
			final ArrowGroup arrowGroup = new ArrowGroup();
			arrowGroupTracker.add(arrowGroup);
			
//			addArrow(arrowGroup, ff);
			addBoom(arrowGroup, fireImpulse);
		}

		prev_btn_x = gamepad.btn_x;
		prev_btn_a = gamepad.btn_a;
	}

	private void addArrow(final ArrowGroup arrowGroup, final Vector2 arrowImpulse) {
		final Arrow a = new Arrow();
		a.setWorldScale(worldScale);
		arrowGroup.group.addActor(a);
		getStage().addActor(arrowGroup.group);
		a.addToWorld(body.getWorld(), body.getWorldCenter(), arrowGroup);
		a.body.setLinearVelocity(body.getLinearVelocity());
		a.fire(arrowImpulse);
		a.toFront();
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
		holding = null;
		joint = null;
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
			return 0;
		}
		if (badAccumulator == 1) {
			badAccumulator = 0;
			return 1;
		}
		do {
			if (dieDeck.size == 0) {
				int sides = 8;
				for (int i = 1; i <= sides; i++) {
					if (i==7 && (20 > badAccumulator || 20 > maxFaceValue)) {
						continue;
					}
					if (i==8 && (80 > badAccumulator|| 80 > maxFaceValue)) {
						continue;
					}
					if (i > maxFaceValue) {
						continue;
					}
					if (i > badAccumulator) {
						continue;
					}
					dieDeck.add(i);
				}
				dieDeck.sort();
				dieDeck.reverse();
				
				if (minFaceValue>6 && dieDeck.size>3 && new Random().nextInt(100) > 10) {
					dieDeck.sort();
					dieDeck.reverse();
					dieDeck.removeRange(3, dieDeck.size-1);
				}
				
				Gdx.app.log(this.getClass().getSimpleName(), "Die Deck: "+dieDeck.toString());
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
		return gbv;
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
			resetFixtures(regionWidth , regionHeight);
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
