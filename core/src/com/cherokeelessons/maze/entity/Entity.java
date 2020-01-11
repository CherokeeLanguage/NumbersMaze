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
	
	public static final int OTHER=-1;
	public static final int FLOOR=0;
	public static final int WALL=1;
	public static final int BLOCK=2;
	public static final int ARROW=4;
	public static final int PORTAL=5;
	public static final int EXPLOSION=6;
	public static final int PLAYER = 7;
	public static final int DEATH_ORB = 8;
	public static final int POINTS_BLOCK = 9;
	
	protected long start;
	public int value = 0;
	public int identity = Entity.OTHER;
	public String tag = "";

	public static class Collides {
		Entity e=null;
		final Vector2 pos=new Vector2();
	}
	final protected Array<Entity> collidesWith = new Array<>();
	final protected Array<Vector2> collidesWithPos = new Array<>();
	
	final protected Array<Integer> audioQueue=new Array<>();
	
	public void audioClear() {
		audioQueue.clear();
	}

	public void addCollision(Entity e, Vector2 p) {
		collidesWith.add(e);
		collidesWithPos.add(p);
	}

	public void clearCollides() {
		collidesWith.clear();
		collidesWithPos.clear();
	}

	protected Body body = null;

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
		if (body==null) {
			return;
		}
		body.setSleepingAllowed(true);
		body.setUserData(this);
		body.setBullet(true);
		worldCenter.set(body.getWorldCenter());
		worldStart.set(getWorldCenter());
	}

	public Vector2 getWorldCenter() {
		return worldCenter;
	}

	protected final Vector2 offset = new Vector2();
	protected float worldScale = 1;

	public Entity() {
		super();
		actions=getActions();
		audioClear();
		setName("Entity");
		offset.x=0;
		offset.y=0;
		setLayoutEnabled(false);
	}

	public Entity(AtlasRegion ar) {
		super(ar);
		actions=getActions();
		audioClear();
		setName("Entity");
		offset.x=0;
		offset.y=0;
		setLayoutEnabled(false);
	}

	public Entity(TiledDrawable tiledDrawable) {
		super(tiledDrawable);
		actions=getActions();
		audioClear();
		setName("TiledEntity");
		offset.x=0;
		offset.y=0;
		setLayoutEnabled(false);
	}

	public Rectangle getCull() {
		return cull;
	}

	/**
	 * If this object's world position is not in these bounds, don't draw it.
	 * Should point to a commonly shared Rectangle object that is updated at a
	 * single point before act is called.
	 * 
	 * @param cull
	 */
	public void setCull(Rectangle cull) {
		this.cull = cull;
	}

	protected Rectangle cull = null;
	final protected Vector2 worldCenter=new Vector2(0, 0);
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (isCulled()) {
			return;
		}
		super.draw(batch, parentAlpha);
	}
	Array<Action> actions;
	public void doActions(float delta) {
		for (int i = 0, n = actions.size; i < n; i++) {
			Action action = actions.get(i);
			if (action.act(delta)) {
				actions.removeIndex(i);
				action.setActor(null);
				i--;
				n--;
			}
		}
	}

	protected boolean doCullCheck=true;
	@Override
	public void act(float delta) {
		doActions(delta);
		updatePosition();
		if (doCullCheck) {
			cullCheck();
		}
	}

	long sleepTime=0;
	Vector2 worldStart=new Vector2();

	private void playAudio() {
		if (audioQueue.size>0) {
			DoAudioEvent e = new DoAudioEvent();
			e.audioQueue.addAll(audioQueue);
			e.location.set(worldCenter);
			audioQueue.clear();
			NumbersMaze.post(e);
		}
	}
	private boolean forceLayout=true;
	public void updatePosition(boolean forceLayout) {
		this.forceLayout=forceLayout;
		updatePosition();
	}
	public void updatePosition(){
		boolean doLayout=false;
		if (body != null && (body.isAwake()||forceLayout)) {
			worldCenter.set(body.getWorldCenter());
			float angle = body.getAngle() * MathUtils.radiansToDegrees;
			float newX = worldCenter.x * worldScale + getOffsetX();
			float newY = worldCenter.y * worldScale + getOffsetY();
			if ((int)getX() != (int)newX) {
				setX(newX);
				doLayout = true;
			}
			if ((int)getY() != (int)newY) {
				setY(newY);
				doLayout = true;
			}
			if ((int)getRotation() != (int)angle) {
				setRotation(angle);
				doLayout = true;
			}
			if (forceLayout) {
				doLayout=true;
				forceLayout=false;
			}
		}
		if (doLayout) {
			layout();
		}
	}

	private boolean isCulled=false;

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

	public void addAudio(int s) {
		audioQueue.add(s);
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				Entity.this.playAudio();
			}
		});
	}

	public float getOffsetX() {
		return offset.x;
	}

	public void setOffsetX(float offsetX) {
		offset.x = offsetX;
	}

	public float getOffsetY() {
		return offset.y;
	}

	public void setOffsetY(float offsetY) {
		offset.y = offsetY;
	}

	public void setOffset(Vector2 v) {
		offset.set(v);
	}

	public Vector2 getOffset() {
		return offset;
	}

	public float getWorldScale() {
		return worldScale;
	}

	public void setWorldScale(float worldScale) {
		this.worldScale = worldScale;
	}
	
	@Override
	public boolean remove() {
		clearActions();
		return super.remove();
	}

	public boolean remove(boolean fromWorldAlso) {
		if (fromWorldAlso) {
			if (body!=null) {
				World world=body.getWorld();
//				for (Fixture f: body.getFixtureList()) {
//					f.setUserData(null);
//				}
//				body.setUserData(null);
				world.destroyBody(body);
				body=null;
			}
		}
		return remove();
	}
	
	
	
	public void setWorldRotation(float degrees) {
		if (body==null) {
			return;
		}
		body.setTransform(body.getWorldCenter(), degrees*MathUtils.degreesToRadians);
	}
	public void setWorldPosition(Vector2 pos) {
		if (body==null) {
			return;
		}
		body.setTransform(pos, 0f);
	}
	
	public void setWorldPosition(float x, float y) {
		if (body==null) {
			return;
		}
		body.setTransform(x, y, 0f);
	}

	public  Array<Integer> getAudioQueue() {
		return audioQueue;
	}

	public void setAudioQueue(Array<Integer> audioQueue) {
		this.audioQueue.clear();
		this.audioQueue.addAll(audioQueue);
	}

	public boolean isCulled() {
		return isCulled;
	}

	public void setCulled(boolean isCulled) {
		this.isCulled = isCulled;
	}

	public Array<Entity> getCollidesWith() {
		return collidesWith;
	}

}
