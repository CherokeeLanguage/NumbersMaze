package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.cherokeelessons.maze.Effect;
import com.cherokeelessons.maze.game.TheWorld;

public class Arrow extends Entity {

	int maxBounces = 0;

	protected static AtlasRegion r = null;

	public Arrow() {
		super(r);
		identity = Entity.ARROW;
		setName("Arrow");
		doCullCheck=false;
	}

	public static AtlasRegion getRegion() {
		return r;
	}

	public static void setRegion(AtlasRegion _r) {
		r = _r;
	}

	private ArrowGroup owner=null;
	public void addToWorld(World world, Vector2 pos, ArrowGroup owner) {
		this.setOwner(owner);
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(pos);
		Body body = world.createBody(bodyDef);
		setBody(body);
		PolygonShape box = new PolygonShape();
		box.setAsBox((getWidth() - 1) / 2 / worldScale, (getHeight() - 1) / 2
				/ worldScale);
		FixtureDef fDef = new FixtureDef();
		fDef.density = 2f;
		fDef.friction = 0f;
		fDef.restitution = -1f;
		fDef.shape = box;
		fDef.filter.categoryBits = TheWorld.TYPE_ARROW;
		fDef.filter.maskBits = (short) (TheWorld.TYPE_EXPLOSION
				| TheWorld.TYPE_ARROW | TheWorld.TYPE_BLOCK
				| TheWorld.TYPE_SENSOR | TheWorld.TYPE_WALL);
		body.createFixture(fDef);
		body.setUserData(this);
		box.dispose();
		body.setBullet(true);
		body.setFixedRotation(false);
		body.setLinearVelocity(new Vector2(0f, 0f));
		body.setLinearDamping(0f);
		body.setAngularDamping(10f);
		body.setGravityScale(.25f);
		setOffsetX(-getWidth() / 2);
		setOffsetY(-getHeight() / 2);
		setOriginX(getWidth() / 2);
		setOriginY(getHeight() / 2);
	}

//	ArrayList<Entity> toRemove = new ArrayList<Entity>();

	@Override
	public void act(float delta) {
		super.act(delta);
		if (System.currentTimeMillis()-start>3000) {
			remove(true);
		}
		if (body == null)
			return;
		for (Entity entity : getCollidesWith()) {
			if (entity.identity == Entity.EXPLOSION) {
				entity.remove(true);
				remove(true);
				return;
			}
			if (entity.identity == Entity.BLOCK) {
				addAudio(Effect.BOOM_A);
				getOwner().boxCount++;
				getOwner().accumulator+=entity.value;
				Vector2 pos = body.getWorldCenter();
				World world = body.getWorld();
				Boom b;
				Vector2 force;
				Vector2 source = body.getLinearVelocity();
				BoomGenerate bg = new BoomGenerate();
				bg.setWorld(world);
				bg.setWorldScale(worldScale);
				bg.setCount(entity.value);
				bg.setWorldPosition(pos);
				bg.setLinearVelocity(body.getLinearVelocity());
				bg.setOwner(getOwner());
				bg.run();
				entity.identity=Entity.EXPLOSION;
				entity.value=0;
				entity.remove(true);
				remove(true);
				return;
			}
		}
		if (getCollidesWith().size > 0 && maxBounces-- < 1) {
			for(Entity e: getCollidesWith()){
				if (e.identity==Entity.FLOOR) break;
				if (e.identity==Entity.PORTAL) break;
				if (e.identity==Entity.ARROW) break;
				remove(true);
				return;
			}			
		}
	}

	private long start;
	public void fire(Vector2 impulse) {
		start=System.currentTimeMillis();
		if (body == null)
			return;
		setWorldRotation(impulse.angle());
		body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
	}

	public ArrowGroup getOwner() {
		return owner;
	}

	public void setOwner(ArrowGroup owner) {
		this.owner = owner;
	}

}
