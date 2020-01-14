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

	public BitmapFont scoreFont=null;
	
	private float avatarScale=.8f;
	private Random r=new Random();
	
	public int theChallenge = 1;
	public int myScore = 0;
	
	private int badAccumulator=0;
	public boolean badValue_hasPending(){
		return badAccumulator>0;
	}
	public int badValue_getPending(){
		return badAccumulator;
	}
	public void badValue_add(int value) {
		badAccumulator+=value;
	}
	Array<Integer> dieDeck=new Array<>();
	public int badValue_getNext(int maxFaceValue){
		int gbv=0;
		if (badAccumulator<1) {
			return 0;
		}
		if (badAccumulator==1) {
			badAccumulator=0;
			return 1;
		}
		do {
			if (dieDeck.size==0) {
				int sides=6;
				if (badAccumulator>30 && maxFaceValue>30) {
					sides=7;
				}
				if (badAccumulator>100 && maxFaceValue>100) {
					sides=8;
				}
				for (int i=0; i<sides; i++) {
					dieDeck.add(i+1);
				}
				dieDeck.shuffle();
			}
			gbv = dieDeck.removeIndex(0);
			//convert die face into VALUE
			if (gbv==7) {
				gbv=20;
			}
			if (gbv==8) {
				gbv=80;
			}
		} while (gbv>badAccumulator);
		badAccumulator-=gbv;
		return gbv;
	}
	public void badValue_clear(){
		badAccumulator=0;
	}

	private Entity holding = null;
	private Joint joint = null;
	private boolean prev_btn_x = false;
	private boolean prev_btn_a = false;
	private boolean toggledOn = false;
	private boolean toggledOff = false;

	private int arrows = 10;

	final private Vector2 aabb_size = new Vector2();
	final private Vector2 aabb_lower = new Vector2();
	final private Vector2 aabb_upper = new Vector2();

	private int pendingScore = 0;
	
	
	
	public boolean pendingArrows() {
		return false;
	}
	
	public int pointsInLimbo() {
		int total = 0;
		for (int ix = arrow_tracker.size() - 1; ix >= 0; ix--) {
			total += arrow_tracker.get(ix).accumulator;
		}
		return total;
	}

	final Vector2 impulse = new Vector2();
	@Override
	public void act(float delta) {
		super.act(delta);
		if (body == null) {
			return;
		}

		for (int ix = arrow_tracker.size() - 1; ix >= 0; ix--) {
			ArrowGroup arrowGroup = arrow_tracker.get(ix);
			if (arrowGroup.group.getChildren().size < 1) {
				if (arrowGroup.boxCount > 0) {
					if (arrowGroup.accumulator==theChallenge) {
						final int newPoints = arrowGroup.boxCount*arrowGroup.accumulator;
						addToPendingScore(newPoints);
						if (scoreFont!=null) {
							final Vector2 p=arrowGroup.getPos();
							ScorePopup s = new ScorePopup(newPoints, p.x+16, p.y+16, scoreFont);
							getStage().addActor(s.getLabel());
						}
					} else {
						Gdx.app.log(this.getClass().getSimpleName(),"DEATH ORB!");
						badAccumulator+=arrowGroup.accumulator;
						DeathOrb orb=new DeathOrb(body.getWorld(), worldScale, arrowGroup.getWorldPos(), arrowGroup.accumulator);
						getStage().addActor(orb);
					}
				}
				arrowGroup.group.remove();
				arrow_tracker.remove(ix);
			}
		}

		toggledOn = false;
		toggledOff = false;
		if (!prev_btn_x && gamepad.btn_x) {
			toggledOn = true;
		}
		if (prev_btn_x && !gamepad.btn_x) {
			toggledOff = true;
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
				public boolean reportFixture(Fixture fixture) {
					Object a = fixture.getBody().getUserData();
					if (!Entity.class.isAssignableFrom(a.getClass())) {
						return true;
					}
					Entity e = (Entity) a;
					if (e.identity == Entity.BLOCK) {
						getCollidesWith().add(e);
						return true; // keep looking
					}
					return true;
				}
			}, aabb_lower.x, aabb_lower.y, aabb_upper.x, aabb_upper.y);
			// find first collides with
			float d = 0;
			for (Entity item : getCollidesWith()) {
				if (item.identity == Entity.BLOCK) {
					float d2 = worldCenter.dst(item.getBody().getWorldCenter());
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
				holding.getColor().a=.4f;
//				holding.body.setTransform(holding.body.getWorldCenter(), 0f);
				RopeJointDef jdef = new RopeJointDef();
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
				Array<Fixture> f = holding.body.getFixtureList();
				for (Fixture af: f) {
					af.setSensor(false);
				}
				holding.toFront();
				toFront();
			}
		} else if (joint != null && toggledOn) {
			Array<Joint> joints = new Array<>();
			body.getWorld().getJoints(joints);
			for (Joint j: joints) {
				if (j.equals(joint)) {
					addAudio(Effect.DROP_IT);
					body.getWorld().destroyJoint(joint);
					holding.body.setGravityScale(1f);
					Array<Fixture> f = holding.body.getFixtureList();
					for (Fixture af: f) {
						af.setSensor(false);
					}
					holding.body.setFixedRotation(false);
//					holding.setColor(Color.WHITE);
					holding.getColor().a=1f;
				}
			}
			holding = null;
			joint = null;
		}

		impulse.x = 0.75f * gamepad.deltaX;
		impulse.y = 0.75f * gamepad.deltaY;

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
			Vector2 ff = new Vector2(2f, 0f);
			switch (lastDir) {
			case EAST:
				break;
			case WEST:
				ff.rotate(180);
				break;
			case NORTH:
				ff.rotate(90);
				break;
			case SOUTH:
				ff.rotate(-90);
				break;
			}
			//ff.add();
			ArrowGroup arrowGroup = new ArrowGroup();
			Arrow a = new Arrow();
			a.setWorldScale(worldScale);
			arrowGroup.group.addActor(a);
			getStage().addActor(arrowGroup.group);
			a.addToWorld(body.getWorld(), body.getWorldCenter(), arrowGroup);
			a.body.setLinearVelocity(body.getLinearVelocity());
			a.fire(ff);
			a.toFront();
			arrow_tracker.add(arrowGroup);
		}

		prev_btn_x = gamepad.btn_x;
		prev_btn_a = gamepad.btn_a;
	}

	final public static int NORTH = 0;
	final public static int EAST = 1;
	final public static int WEST = 2;
	final public static int SOUTH = 3;

	public PlayerInput gamepad = new PlayerInput();

	private AtlasRegion[][] ar;

	private ArrayList<ArrowGroup> arrow_tracker = new ArrayList<>();

	public Player() {
		super();
		r.setSeed(0);
		TextureAtlas atlas = S.getPar().playerAtlas;
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

	

	private int lastDir = -1;
	private int lastX = -1;
	private int lastY = -1;

	@Override
	public void setBody(Body body) {
		super.setBody(body);
		if (body==null) {
			return;
		}
		resetFixtures(getWidth(), getHeight());
		body.setGravityScale(0f);
	}

	private void showAvatar(int dir) {
		if (dir < 0 || dir > 3) {
			Gdx.app.log(this.getClass().getSimpleName(),"Bad dir: " + dir);
			return;
		}

		if (lastDir == dir && lastX == getX() && lastY == getY()) {
			return;
		}
		int i = 0;
		if (dir == NORTH || dir == SOUTH) {
			i = (int) getY() % 4;
		}
		if (dir == EAST || dir == WEST) {
			i = (int) (getX() % 48) / 12;
		}
		if (i < 0) {
			i = -i;
		}
		if (ar[dir][i] != null) {
			lastDir = dir;
			TextureRegionDrawable d = new TextureRegionDrawable(ar[dir][i]);
			setDrawable(d);
			final int regionWidth = d.getRegion().getRegionWidth();
			setWidth(regionWidth);
			final int regionHeight = d.getRegion().getRegionHeight();
			setHeight(regionHeight);
			setOrigin(regionWidth / 2, regionHeight / 2);
			setScale(avatarScale);
			setOffsetX(-regionWidth / 2);
			setOffsetY(-regionHeight / 2);
			resetFixtures(regionWidth*avatarScale, regionHeight*avatarScale);
		} else {
			Gdx.app.log(this.getClass().getSimpleName(),"DIR IS NULL: " + dir + ", i: " + i);
		}
	}

	private void resetFixtures(final float regionWidth, final float regionHeight) {
		if (body != null) {
			Array<Fixture> f = new Array<>();
			f.addAll(body.getFixtureList());
			for (Fixture fixture: f) {
				body.destroyFixture(fixture);
			}
			f.clear();

			float w = (regionWidth - 1) / worldScale;
			float h = (regionHeight - 1) / worldScale;

			aabb_size.x = w;
			aabb_size.y = h;

			float rad;
			if (w < h) {
				rad = w / 4;
			} else {
				rad = h / 4;
			}

			FixtureDef fDef;

			CircleShape circle = new CircleShape();
			circle.setRadius(rad);

			float div=avatarScale*avatarScale;
			// TC
			circle.setPosition(new Vector2(w / 2, rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.1f/div;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = TheWorld.TYPE_PLAYER;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_BLOCK);
			body.createFixture(fDef);
			// BC
			circle.setPosition(new Vector2(w / 2, h - rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.1f/div;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = TheWorld.TYPE_PLAYER;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_BLOCK);
			body.createFixture(fDef);
			// LC
			circle.setPosition(new Vector2(rad, h / 2));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.1f/div;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = TheWorld.TYPE_PLAYER;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_BLOCK);
			body.createFixture(fDef);
			// RC
			circle.setPosition(new Vector2(w - rad, h / 2));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.1f/div;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = TheWorld.TYPE_PLAYER;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_BLOCK);
			body.createFixture(fDef);
			// TL
			circle.setPosition(new Vector2(rad, rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.1f/div;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = TheWorld.TYPE_PLAYER;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_BLOCK);
			body.createFixture(fDef);
			// TR
			circle.setPosition(new Vector2(w - rad, rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.1f/div;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = TheWorld.TYPE_PLAYER;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_BLOCK);
			body.createFixture(fDef);
			// BL
			circle.setPosition(new Vector2(rad, h - rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.1f/div;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = TheWorld.TYPE_PLAYER;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_BLOCK);
			body.createFixture(fDef);
			// BR
			circle.setPosition(new Vector2(w - rad, h - rad));
			fDef = new FixtureDef();
			fDef.shape = circle;
			fDef.density = 0.1f/div;
			fDef.friction = 0;
			fDef.restitution = 0f;
			fDef.filter.categoryBits = TheWorld.TYPE_PLAYER;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_BLOCK);
			body.createFixture(fDef);
			circle.dispose();
		}
	}

	public Player(AtlasRegion ar) {
		super(ar);
		r.setSeed(0);
	}

	public void addToWorld(TheWorld world) {
		r.setSeed(theChallenge);
		float wx = 32 / worldScale;
		float wy = 32 / worldScale;
		BodyDef bodyDef = new BodyDef();
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
	public int getPendingScore() {
		return pendingScore;
	}
	public void setPendingScore(int pendingScore) {
		synchronized (this) {
			this.pendingScore = pendingScore;
		}
	}
	public void addToPendingScore(int pendingScore) {
		synchronized (this) {
			this.pendingScore+=pendingScore;
		}
	}
	public int getPendingScoreAndZeroOut() {
		int r;
		synchronized (this) {
			r=pendingScore;
			pendingScore=0;
		}
		return r;
	}
}
