package com.cherokeelessons.maze.game;

import java.util.Random;

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
import com.cherokeelessons.maze.entity.ChainedExplosions;
import com.cherokeelessons.maze.entity.Entity;
import com.cherokeelessons.maze.stage.StageBase;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class TheWorld {

	public static class AddOrphan {
		public final ChainedExplosions arrowGroup = new ChainedExplosions();
	}

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

	private static EventBus bus = new EventBus();

	public static void post(final Object o) {
		bus.post(o);
	}

	private final Array<ChainedExplosions> orphan_tracker = new Array<>();

	private int badAccumulator = 0;

	Array<Integer> dieDeck = new Array<>();

	private World world = null;

	public Array<Entity[]> c = new Array<>();
	private final Array<Entity> prevCollides = new Array<>();

	private boolean hasBoom = false;

	ContactListener collider = new ContactListener() {

		@Override
		public void beginContact(final Contact contact) {
			final Fixture fa = contact.getFixtureA();
			final Fixture fb = contact.getFixtureB();
			final Object a = fa.getBody().getUserData();
			final Object b = fb.getBody().getUserData();
			if (!Entity.class.isAssignableFrom(a.getClass()) || !Entity.class.isAssignableFrom(b.getClass())) {
				return;
			}
			final Entity ea = (Entity) a;
			final Entity eb = (Entity) b;
			final Vector2 pos = new Vector2();
			if (contact.getWorldManifold().getNumberOfContactPoints() < 1) {
				if (fa.isSensor()) {
					if (fa.getShape().getType() == Type.Circle) {
						final CircleShape cs = (CircleShape) fa.getShape();
						pos.set(cs.getPosition()).add(ea.getWorldCenter());
					}
				}
				if (fb.isSensor()) {
					if (fb.getShape().getType() == Type.Circle) {
						final CircleShape cs = (CircleShape) fb.getShape();
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

		@Override
		public void endContact(final Contact contact) {
		}

		@Override
		public void postSolve(final Contact contact, final ContactImpulse impulse) {
			final float[] i = impulse.getNormalImpulses();
			if (i.length == 0) {
				return;
			}
			final Body b1 = contact.getFixtureA().getBody();
			final Body b2 = contact.getFixtureB().getBody();
			if (b1.getUserData() instanceof Entity && b2.getUserData() instanceof Entity) {
				final Entity e1 = (Entity) b1.getUserData();
				final Entity e2 = (Entity) b2.getUserData();
				do {
					if (hasBoom) {
						break;
					}
					if (e1 == null || e2 == null) {
						break;
					}
					if (e1.identity != Entity.BLOCK && e2.identity != Entity.BLOCK) {
						break;
					}
					if (e1.identity == Entity.EXPLOSION || e1.identity == Entity.ARROW) {
						break;
					}
					if (e2.identity == Entity.EXPLOSION || e2.identity == Entity.ARROW) {
						break;
					}
					// force of 12+ for between block & non-block
					if ((e1.identity != Entity.BLOCK || e2.identity != Entity.BLOCK) && i[0] < 12) {
						return;
					}
					// force of 7+ for between blocks
					if (i[0] < 7) {
						return;
					}
					Gdx.app.log(this.getClass().getSimpleName(),
							"FORCE: " + i[0] + ", BETWEEN: " + e1.identity + " <-> " + e2.identity);
					hasBoom = true;
					Gdx.app.postRunnable(new Runnable() {
						final Vector2 v1 = new Vector2().set(b1.getWorldCenter());
						final Vector2 v2 = new Vector2().set(b2.getWorldCenter());
						final World world = b1.getWorld();

						@Override
						public void run() {
							final ChainedExplosions chainedExplosions = new ChainedExplosions();
							Arrow a;
							a = new Arrow();
							a.getColor().a = 0f;// make invisible
							a.setWorldScale(e1.getWorldScale());
							chainedExplosions.group.addActor(a);
							a.addToWorld(world, v1, chainedExplosions);
							a.fire(new Vector2(0f, 0f));
							a = new Arrow();
							a.getColor().a = 0f;// make invisible
							a.setWorldScale(e2.getWorldScale());
							chainedExplosions.group.addActor(a);
							a.addToWorld(world, v2, chainedExplosions);
							a.fire(new Vector2(0f, 0f));
							orphan_tracker.add(chainedExplosions);
						}
					});
				} while (false);
			}
		}

		@Override
		public void preSolve(final Contact contact, final Manifold oldManifold) {
		}
	};

	public TheWorld() {
		bus = new EventBus();
		bus.register(this);
		setWorld(new World(new Vector2(0, -10f), true));
		world.setContactListener(collider);
		world.setAutoClearForces(true);
	}

	@Subscribe
	public void addOrphan(final AddOrphan event) {
		addToOrphan_tracker(event.arrowGroup);
	}

	public void addToOrphan_tracker(final ChainedExplosions orphan) {
		orphan_tracker.add(orphan);
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
					if (i==7 && (20 > badAccumulator || 20 > maxFaceValue)) {
						continue;
					}
					if (i==8 && (80 > badAccumulator|| 80 > maxFaceValue)) {
						continue;
					}
					if (i > maxFaceValue) {
						Gdx.app.log(this.getClass().getSimpleName(), "i > maxFaceValue: "+i+">"+maxFaceValue);
						continue;
					}
					if (i > badAccumulator) {
						Gdx.app.log(this.getClass().getSimpleName(), "i > badAccumulator: "+i+">"+badAccumulator);
						continue;
					}
					dieDeck.add(i);
					Gdx.app.log(this.getClass().getSimpleName(), "X Die Deck: "+dieDeck.toString());
				}
				Gdx.app.log(this.getClass().getSimpleName(), "Y Die Deck: "+dieDeck.toString());
				if (minFaceValue>6 && dieDeck.size>3 && new Random().nextInt(100) > 74) {
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
		if (badAccumulator<0) {
			badAccumulator=0;
		}
		return gbv > 0 ? gbv : 0;
	}

	public boolean badValue_hasPending() {
		return badAccumulator > 0;
	}

	public void dispose() {
		world.dispose();
		world = null;
	}

	public int getBadAccumulator() {
		return badAccumulator;
	}

	public int getCollidesCount() {
		return prevCollides.size;
	}

	public Array<ChainedExplosions> getOrphan_tracker() {
		return orphan_tracker;
	}

	public World getWorld() {
		return world;
	}

	public int pointsInLimbo() {
		int total = 0;
		for (int ix = orphan_tracker.size - 1; ix >= 0; ix--) {
			total += orphan_tracker.get(ix).accumulator;
		}
		return total;
	}

	public void processOrphans(final StageBase stage) {
		for (int ix = orphan_tracker.size - 1; ix >= 0; ix--) {
			final ChainedExplosions arrowGroup = orphan_tracker.get(ix);
			if (arrowGroup.group.getStage() == null) {
				stage.addActor(arrowGroup.group);
			}
			if (arrowGroup.group.getChildren().size < 1) {
				if (arrowGroup.boxCount > 0) {
					setBadAccumulator(getBadAccumulator() + arrowGroup.accumulator);
				}
				arrowGroup.group.remove();
				orphan_tracker.removeIndex(ix);

			}
		}
	}

	public void setBadAccumulator(final int badAccumulator) {
		this.badAccumulator = badAccumulator;
	}

	private void setWorld(final World world) {
		this.world = world;
	}

	public void step(final float delta) {
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
}
