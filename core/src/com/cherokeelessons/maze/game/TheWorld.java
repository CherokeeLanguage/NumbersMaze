package com.cherokeelessons.maze.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Shape.Type;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.cherokeelessons.maze.Effect;
import com.cherokeelessons.maze.entity.Arrow;
import com.cherokeelessons.maze.entity.ArrowGroup;
import com.cherokeelessons.maze.entity.Entity;
import com.cherokeelessons.maze.stage.StageBase;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class TheWorld {

	public static final short TYPE_UNDEFINED = 0;
	public static final short TYPE_ALL = (short) 0xffff;
	public static final short TYPE_WALL = 0x0001;
	public static final short TYPE_BLOCK = 0x0002;
	public static final short TYPE_PLAYER = 0x0004;
	public static final short TYPE_SENSOR = 0x0008;
	public static final short TYPE_FLOOR = 0x0010;
	public static final short TYPE_GATEWAY = 0x0020;
	public static final short TYPE_ARROW = 0x0040;
	public static final short TYPE_PORTAL = 0x0080;
	public static final short TYPE_EXPLOSION = 0x0100;
	public static final short TYPE_ENEMY = 0x0200;

	private Array<ArrowGroup> orphan_tracker = new Array<>();

	public int pointsInLimbo() {
		int total = 0;
		for (int ix = orphan_tracker.size - 1; ix >= 0; ix--) {
			total += orphan_tracker.get(ix).accumulator;
		}
		return total;
	}

	private int badAccumulator = 0;

	public void processOrphans(StageBase stage) {
		for (int ix = orphan_tracker.size - 1; ix >= 0; ix--) {
			ArrowGroup arrowGroup = orphan_tracker.get(ix);
			if (arrowGroup.group.getStage() == null) {
				stage.addActor(arrowGroup.group);
			}
			if (arrowGroup.group.getChildren().size < 1) {
				if (arrowGroup.boxCount > 0) {
					setBadAccumulator(getBadAccumulator()
							+ arrowGroup.accumulator);
				}
				arrowGroup.group.remove();
				orphan_tracker.removeIndex(ix);
				
			}
		}
	}

	Array<Integer> dieDeck = new Array<>();

	public int badValue_getNext(int maxFaceValue) {
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
				int sides = 6;
				if (badAccumulator > 30 && maxFaceValue > 30) {
					sides = 7;
				}
				if (badAccumulator > 100 && maxFaceValue > 100) {
					sides = 8;
				}
				for (int i = 0; i < sides; i++) {
					dieDeck.add(i + 1);
				}
				dieDeck.shuffle();
			}
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

	public boolean badValue_hasPending() {
		return badAccumulator > 0;
	}

	private World world = null;
	public Array<Entity[]> c = new Array<>();

	private Array<Entity> prevCollides = new Array<>();

	public int getCollidesCount() {
		return prevCollides.size;
	}

	private boolean hasBoom = false;

	public void step(float delta) {
		if (world == null) {
			return;
		}
		for (int ix = 0; ix < prevCollides.size; ix++) {
			prevCollides.get(ix).clearCollides();
		}
		prevCollides.clear();
		hasBoom = false;
		world.step(delta, 8, 3);
	}
	public static void post(Object o) {
		bus.post(o);
	}
	private static EventBus bus=new EventBus();
	public TheWorld() {
		bus=new EventBus();
		bus.register(this);
		setWorld(new World(new Vector2(0, -10f), true));
		world.setContactListener(collider);
		world.setAutoClearForces(true);
	}

	public World getWorld() {
		return world;
	}

	private void setWorld(World world) {
		this.world = world;
	}

	public void dispose() {
		world.dispose();
		world = null;
	}

	public int getBadAccumulator() {
		return badAccumulator;
	}

	public void setBadAccumulator(int badAccumulator) {
		this.badAccumulator = badAccumulator;
	}

	public Array<ArrowGroup> getOrphan_tracker() {
		return orphan_tracker;
	}

	public static class AddOrphan {
		public final ArrowGroup arrowGroup=new ArrowGroup();
	}
	@Subscribe
	public void addOrphan(AddOrphan event) {
		addToOrphan_tracker(event.arrowGroup);
	}
	public void addToOrphan_tracker(ArrowGroup orphan) {
		orphan_tracker.add(orphan);
	}

	ContactListener collider = new ContactListener() {

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			float[] i = impulse.getNormalImpulses();
			if (i.length == 0) {
				return;
			}
			final Body b1 = contact.getFixtureA().getBody();
			final Body b2 = contact.getFixtureB().getBody();
			if (b1.getUserData() instanceof Entity
					&& b2.getUserData() instanceof Entity) {
				final Entity e1 = (Entity) b1.getUserData();
				final Entity e2 = (Entity) b2.getUserData();
				do {
					if (hasBoom) {
						break;
					}
					if (e1 == null || e2 == null){
						break;
					}
					if (e1.identity != Entity.BLOCK
							&& e2.identity != Entity.BLOCK) {
						break;
					}
					if (e1.identity == Entity.EXPLOSION
							|| e1.identity == Entity.ARROW){
						break;
					}
					if (e2.identity == Entity.EXPLOSION
							|| e2.identity == Entity.ARROW){
						break;
					}
					//force of 9+ for between block & non-block
					if ((e1.identity!=Entity.BLOCK||e2.identity!=Entity.BLOCK) && i[0] < 9) {
						return;
					}
					//force of 4+ for between blocks
					if (i[0]<4) {
						return;
					}
					Gdx.app.log(this.getClass().getSimpleName(),"FORCE: "+i[0]+", BETWEEN: "+e1.identity+" <-> "+e2.identity);
					hasBoom=true;
					Gdx.app.postRunnable(new Runnable() {
						final Vector2 v1=new Vector2().set(b1.getWorldCenter());
						final Vector2 v2=new Vector2().set(b2.getWorldCenter());
						final World world=b1.getWorld();
						@Override
						public void run() {
							ArrowGroup arrowGroup = new ArrowGroup();
							Arrow a;
							a = new Arrow();
							a.getColor().a = 0f;// make invisible
							a.setWorldScale(e1.getWorldScale());
							arrowGroup.group.addActor(a);
							a.addToWorld(world, v1, arrowGroup);
							a.fire(new Vector2(0f,0f));
							a = new Arrow();
							a.getColor().a = 0f;// make invisible
							a.setWorldScale(e2.getWorldScale());
							arrowGroup.group.addActor(a);
							a.addToWorld(world, v2, arrowGroup);
							a.fire(new Vector2(0f,0f));
							orphan_tracker.add(arrowGroup);
						}
					});
				} while (false);
			}
		}

		@Override
		public void endContact(Contact contact) {
		}

		@Override
		public void beginContact(Contact contact) {
			Fixture fa=contact.getFixtureA();
			Fixture fb=contact.getFixtureB();
			Object a = fa.getBody().getUserData();
			Object b = fb.getBody().getUserData();
			if (!Entity.class.isAssignableFrom(a.getClass())
					|| !Entity.class.isAssignableFrom(b.getClass())) {
				return;
			}
			Entity ea = (Entity) a;
			Entity eb = (Entity) b;
			Vector2 pos=new Vector2();
			if (contact.getWorldManifold().getNumberOfContactPoints()<1) {
				if (fa.isSensor()) {
					if (fa.getShape().getType()==Type.Circle) {
						CircleShape cs=(CircleShape)fa.getShape();
						pos.set(cs.getPosition()).add(ea.getWorldCenter());
					}
				}
				if (fb.isSensor()) {
					if (fb.getShape().getType()==Type.Circle) {
						CircleShape cs=(CircleShape)fb.getShape();
						pos.set(cs.getPosition()).add(eb.getWorldCenter());
					}
				}
			} else {
				pos.set(contact.getWorldManifold().getPoints()[0]);
			}
			ea.addCollision(eb, pos);
			eb.addCollision(ea, pos);
			prevCollides.add(ea);
			prevCollides.add(eb);
			if (ea.identity == Entity.BLOCK && eb.identity == Entity.WALL
					|| ea.identity == Entity.WALL && eb.identity == Entity.BLOCK) {
				ea.addAudio(Effect.BOX_MOVED);
			}
			if (ea.identity == Entity.BLOCK && eb.identity == Entity.BLOCK) {
				ea.addAudio(Effect.PLINK);
			}
			if (ea.identity == Entity.ARROW || eb.identity == Entity.ARROW) {
				ea.addAudio(Effect.PLINK);
			}
		}
	};
}
