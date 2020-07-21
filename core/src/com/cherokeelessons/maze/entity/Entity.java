package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.cherokeelessons.maze.Effect.DoAudioEvent;
import com.cherokeelessons.maze.NumbersMaze;

public class Entity extends Image {

	public static class Collides {
		Entity e = null;
		final Vector2 pos = new Vector2();
	}

	public static final int OTHER = -1;
	public static final int FLOOR = 0;
	public static final int WALL = 1;
	public static final int BLOCK = 2;
	public static final int ARROW = 4;
	public static final int PORTAL = 5;
	public static final int EXPLOSION = 6;
	public static final int PLAYER = 7;
	public static final int DEATH_ORB = 8;

	public static final int POINTS_BLOCK = 9;
	protected long start;
	public int value = 0;
	public int identity = Entity.OTHER;

	public String tag = "";
	final protected Array<Entity> collidesWith = new Array<>();
	final protected Array<Vector2> collidesWithPos = new Array<>();

	final protected Array<Integer> audioQueue = new Array<>();

	protected Body body = null;

	protected final Vector2 offset = new Vector2();

	protected float worldScale = 1;

	/**
	 * If this object's world position is not in these bounds, don't draw it. Should
	 * point to a commonly shared Rectangle object that is updated at a single point
	 * before draw is called.
	 */
	protected Rectangle cull = null;

	final protected Vector2 worldCenter = new Vector2(0, 0);

	Array<Action> actions;

	protected boolean doCullCheck = true;

	long sleepTime = 0;
	Vector2 worldStart = new Vector2();

	private boolean forceLayout = true;

	private boolean isCulled = false;

	public Entity() {
		super();
		actions = getActions();
		audioClear();
		setName("Entity");
		offset.x = 0;
		offset.y = 0;
		setLayoutEnabled(false);
	}

	public Entity(final AtlasRegion ar) {
		super(ar);
		actions = getActions();
		audioClear();
		setName("Entity");
		offset.x = 0;
		offset.y = 0;
		setLayoutEnabled(false);
	}

	public Entity(final TiledDrawable tiledDrawable) {
		super(tiledDrawable);
		actions = getActions();
		audioClear();
		setName("TiledEntity");
		offset.x = 0;
		offset.y = 0;
		setLayoutEnabled(false);
	}

	@Override
	public void act(final float delta) {
		doActions(delta);
		updatePosition();
		if (doCullCheck) {
			cullCheck();
		}
	}

	public void addAudio(final int s) {
		audioQueue.add(s);
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				Entity.this.playAudio();
			}
		});
	}

	public void addCollision(final Entity e, final Vector2 p) {
		collidesWith.add(e);
		collidesWithPos.add(p);
	}

	public void audioClear() {
		audioQueue.clear();
	}

	public void clearCollides() {
		collidesWith.clear();
		collidesWithPos.clear();
	}

	public void cullCheck() {
		if (body == null) {
			return;
		}
		if (getCull() != null) {
			if (worldCenter.x < getCull().x || worldCenter.y < getCull().y
					|| worldCenter.x > getCull().x + getCull().width
					|| worldCenter.y > getCull().y + getCull().height) {
				remove();
				setCulled(true);
				return;
			}
		}
		if (isCulled()) {
			setCulled(false);
			updatePosition();
		}
	}

	public void doActions(final float delta) {
		for (int i = 0, n = actions.size; i < n; i++) {
			final Action action = actions.get(i);
			if (action.act(delta)) {
				actions.removeIndex(i);
				action.setActor(null);
				i--;
				n--;
			}
		}
	}

	@Override
	public void draw(final Batch batch, final float parentAlpha) {
		if (isCulled()) {
			return;
		}
		super.draw(batch, parentAlpha);
	}

	public Array<Integer> getAudioQueue() {
		return audioQueue;
	}

	public Body getBody() {
		return body;
	}

	public Array<Entity> getCollidesWith() {
		return collidesWith;
	}

	public Rectangle getCull() {
		return cull;
	}

	public Vector2 getOffset() {
		return offset;
	}

	public float getOffsetX() {
		return offset.x;
	}

	public float getOffsetY() {
		return offset.y;
	}

	public Vector2 getWorldCenter() {
		return worldCenter;
	}

	public float getWorldScale() {
		return worldScale;
	}

	public boolean isCulled() {
		return isCulled;
	}

	private void playAudio() {
		if (audioQueue.size > 0) {
			final DoAudioEvent e = new DoAudioEvent();
			e.audioQueue.addAll(audioQueue);
			e.location.set(worldCenter);
			audioQueue.clear();
			NumbersMaze.post(e);
		}
	}

	@Override
	public boolean remove() {
		clearActions();
		return super.remove();
	}

	public boolean remove(final boolean fromWorldAlso) {
		if (fromWorldAlso) {
			if (body != null) {
				final World world = body.getWorld();
//				for (Fixture f: body.getFixtureList()) {
//					f.setUserData(null);
//				}
//				body.setUserData(null);
				world.destroyBody(body);
				body = null;
			}
		}
		return remove();
	}

	public void setAudioQueue(final Array<Integer> audioQueue) {
		this.audioQueue.clear();
		this.audioQueue.addAll(audioQueue);
	}

	public void setBody(final Body body) {
		this.body = body;
		if (body == null) {
			return;
		}
		body.setSleepingAllowed(true);
		body.setUserData(this);
		body.setBullet(true);
		worldCenter.set(body.getWorldCenter());
		worldStart.set(getWorldCenter());
	}

	/**
	 * If this object's world position is not in these bounds, don't draw it. Should
	 * point to a commonly shared Rectangle object that is updated at a single point
	 * before draw is called.
	 *
	 * @param cull
	 */
	public void setCull(final Rectangle cull) {
		this.cull = cull;
	}

	public void setCulled(final boolean isCulled) {
		this.isCulled = isCulled;
	}

	public void setOffset(final Vector2 v) {
		offset.set(v);
	}

	public void setOffsetX(final float offsetX) {
		offset.x = offsetX;
	}

	public void setOffsetY(final float offsetY) {
		offset.y = offsetY;
	}

	public void setWorldPosition(final float x, final float y) {
		if (body == null) {
			return;
		}
		body.setTransform(x, y, 0f);
	}

	public void setWorldPosition(final Vector2 pos) {
		if (body == null) {
			return;
		}
		body.setTransform(pos, 0f);
	}

	public void setWorldRotation(final float degrees) {
		if (body == null) {
			return;
		}
		body.setTransform(body.getWorldCenter(), degrees * MathUtils.degreesToRadians);
	}

	public void setWorldScale(final float worldScale) {
		this.worldScale = worldScale;
	}

	public void updatePosition() {
		boolean doLayout = false;
		if (body != null && (body.isAwake() || forceLayout)) {
			worldCenter.set(body.getWorldCenter());
			final float angle = body.getAngle() * MathUtils.radiansToDegrees;
			final float newX = worldCenter.x * worldScale + getOffsetX();
			final float newY = worldCenter.y * worldScale + getOffsetY();
			if ((int) getX() != (int) newX) {
				setX(newX);
				doLayout = true;
			}
			if ((int) getY() != (int) newY) {
				setY(newY);
				doLayout = true;
			}
			if ((int) getRotation() != (int) angle) {
				setRotation(angle);
				doLayout = true;
			}
			if (forceLayout) {
				doLayout = true;
				forceLayout = false;
			}
		}
		if (doLayout) {
			layout();
		}
	}

	public void updatePosition(final boolean doForceLayout) {
		this.forceLayout = doForceLayout;
		updatePosition();
	}

	protected short maskBits() {
		return -1;
	}

	protected short sensorMaskBits() {
		return maskBits();
	}

	protected short categoryBits() {
		return 1;
	}

	protected short sensorCategoryBits() {
		return categoryBits();
	}

}
