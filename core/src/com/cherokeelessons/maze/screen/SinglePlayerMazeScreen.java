package com.cherokeelessons.maze.screen;

//TODO
/**
 * (09:27:15 PM) Michael Joyner: I wonder if the collision routine tells me how much force is on a object when a box is sitting on a box? I could have the little boxes get squished if there is too much weight on top and have it explode.
 * (09:23:10 PM) Michael Joyner: you know an explosion is bad ass when your FPS on a quad core drops to 8
 * (09:23:30 PM) Charles Kauffman: Hah!
 * (09:23:56 PM) Michael Joyner: yeah.. gotta look into that... 80 fireballs... all at once...
 * (09:24:13 PM) Michael Joyner: I think I want to change it so that the fireballs don't all appear at once, but fountain out
 * (09:24:23 PM) Michael Joyner: rapidly over a short time frame
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.maze.DisplaySize;
import com.cherokeelessons.maze.Effect.MusicPauseEvent;
import com.cherokeelessons.maze.Effect.MusicPlayEvent;
import com.cherokeelessons.maze.Effect.MusicStopEvent;
import com.cherokeelessons.maze.Effect.NumberSequenceRunnable;
import com.cherokeelessons.maze.Effect.PlayNumberSequence;
import com.cherokeelessons.maze.Effect.SetMicPos;
import com.cherokeelessons.maze.Effect.SoundPlayEvent;
import com.cherokeelessons.maze.Effect.StopNumbers;
import com.cherokeelessons.maze.NumbersMaze;
import com.cherokeelessons.maze.NumbersMaze.ScreenChangeEvent;
import com.cherokeelessons.maze.NumbersMaze.ScreenList;
import com.cherokeelessons.maze.S;
import com.cherokeelessons.maze.entity.DeathOrb;
import com.cherokeelessons.maze.entity.Entity;
import com.cherokeelessons.maze.entity.Player;
import com.cherokeelessons.maze.entity.PlayerInput;
import com.cherokeelessons.maze.game.GraduatedIntervalQueue;
import com.cherokeelessons.maze.game.TheWorld;
import com.cherokeelessons.maze.object.DataBundle;
import com.cherokeelessons.maze.object.GenerateNumber;
import com.cherokeelessons.maze.object.Maze;
import com.cherokeelessons.maze.object.MazeCell;

public class SinglePlayerMazeScreen extends ScreenBase {

	private Array<Integer> challengeList = new Array<>();
	private int challengeTotalValue = 0;

	final int challengeSplit = 6;

	private void calculateChallengeList(int level) {
		level--;
		int challengeSet = level / challengeSplit + 1;
		int subSet = level % challengeSplit;
		int range = 7;
		int start = (challengeSet - 1) * range + 1;
		int end = start + range;

		IntArray seed = new IntArray();
		for (int ix = start; ix < end; ix++) {
			seed.add(ix);
		}
		if (start > range) {
			for (int ix = start - range; ix < end - range; ix++) {
				seed.add(ix);
			}
		} else {
			for (int ix = start; ix < end; ix++) {
				seed.add(ix);
			}
		}
		GraduatedIntervalQueue giq = new GraduatedIntervalQueue();
		giq.setShortList(true);
		giq.load(seed);
		challengeList.clear();
		ArrayList<Integer> list = giq.getIntervalQueue();
		System.out.println("MASTER CHALLENGE LIST: " + list.size() + " := "
				+ list);
		int split = list.size() / challengeSplit;
		int setStart = split * subSet;
		int nextSet = split * (subSet + 1);
		for (int ix = setStart; ix < nextSet; ix++) {
			challengeList.add(list.get(ix));
		}
		System.out.println("LEVEL CHALLENGE LIST: " + challengeList.size
				+ " := " + challengeList);
		challengeTotalValue = 0;
		for (Integer i : challengeList) {
			challengeTotalValue += i;
		}
	}

	private int getMaxChallenge() {
		int m = 0;
		for (Integer i : challengeList) {
			if (i > m) {
				m = i;
			}
		}
		return m;
	}

	NumbersMaze app;
	Group tiles;
	private Array<Controller> joylist;
	private Player player1;
	private Entity thePortal;
	float offsetX = 0;
	float offsetY = 0;
	private long startTime;

	private Label timeLeft = null;
	private long lastTimeLeft = 0;
	private Camera gameStage_camera;

	private int getSecondsLeft() {
		long remainingTime = timelimit
				- (System.currentTimeMillis() - startTime);
		return (int) remainingTime / 1000;
	}

	private void updateTimeLeft() {
		int remainingTime = getSecondsLeft();
		if (remainingTime < 0) {
			remainingTime = 0;
		}
		if (lastTimeLeft == remainingTime) {
			return;
		}
		lastTimeLeft = remainingTime;
		int minutes = remainingTime / 60;
		int seconds = remainingTime - minutes * 60;
		StringBuilder b = new StringBuilder();
		if (minutes < 10) {
			b.append("0");
		}
		b.append(minutes);
		b.append(":");
		if (seconds < 10) {
			b.append("0");
		}
		b.append(seconds);
		timeLeft.setText(b.toString());
	}

	final private DataBundle data = new DataBundle();

	public SinglePlayerMazeScreen(final NumbersMaze app, DataBundle _data) {
		super();
		data.put(_data);
		level = data.getInteger("level", 1);
		ultimate = data.getBoolean("ultimate", false);
		theScore = data.getInteger("score");

		backgroundColor = Color.WHITE;
		this.app = app;
		setShowFPS(false);

		AtlasRegion ar_controls = S.getArg().findRegion("touch_buttons_0");
		Image controls = new Image(ar_controls);
		controls.setX(0);
		controls.setY(0);

		hud.getRoot().setTransform(false);
		gameStage.getRoot().setTransform(false);
		backDrop.getRoot().setTransform(false);

		BitmapFont f1 = S.getFnt().getFont(40);
		BitmapFont f2 = S.getFnt().getFont(20);

		LabelStyle ls = new LabelStyle(f2, new Color(Color.DARK_GRAY));
		AtlasRegion ll_ar = S.getArg().findRegion("block_ltblue");
		NinePatch ll_9 = new NinePatch(ll_ar, 12, 12, 12, 12);
		ls.background = new NinePatchDrawable(ll_9);

		AtlasRegion infoStyle_ar = S.getArg().findRegion("block_ltblue");
		NinePatch infoStyle_9 = new NinePatch(infoStyle_ar, 12, 12, 12, 12);
		LabelStyle infoStyle = new LabelStyle(S.getFnt().getFont(20),
				new Color(Color.RED));
		infoStyle.background = new NinePatchDrawable(infoStyle_9);

		label_level = new Label("Maze: " + 0, ls);
		// label_level.getColor().a=.8f;
		label_level.setX(overscan.x);
		label_level.setY(overscan.y);
		hud.addActor(label_level);

		info_label = new Label("FPS: " + 0, infoStyle);
		info_label.setY(hud.getHeight() - info_label.getHeight());
		info_label.setX(0);
		// info_label.getColor().a=.5f;
		hud.addActor(info_label);

		AtlasRegion scoreStyle_ar = S.getArg().findRegion("block_ltblue");
		NinePatch scoreStyle_9 = new NinePatch(scoreStyle_ar, 12, 12, 12, 12);
		LabelStyle score_style = new LabelStyle(f1, new Color(Color.DARK_GRAY));
		score_style.background = new NinePatchDrawable(scoreStyle_9);
		score_style.font.setFixedWidthGlyphs("0123456789");

		label_score = new Label("000000000", score_style);
		label_score.pack();
		label_score.setX(overscan.x + (overscan.width - label_score.getWidth())
				/ 2);
		label_score.setOrigin(label_score.getWidth() / 2,
				label_score.getHeight() / 2);
		label_score.setHeight(label_score.getHeight() * .6f);
		label_score.layout();
		// label_score.getColor().a=.8f;
		label_score
				.setY(overscan.y + overscan.height - label_score.getHeight());
		hud.addActor(label_score);

		AtlasRegion bg = S.getArg().findRegion("background-1");
		Image background = new Image(bg);
		background.setColor(1, 1, 1, .35f);
		background.setFillParent(true);
		background.setScaling(Scaling.fill);
		backDrop.addActor(background);

		world = new TheWorld();
		debugRenderer = new Box2DDebugRenderer();

		tiles = new Group();

		tiles.setTransform(false);

		gameStage.addActor(tiles);

		System.out.println("STAGE SIZE: " + gameStage.getWidth() + "x"
				+ gameStage.getHeight());

		player1 = new Player();
		player1.setWorldScale(WORLD_TO_BOX);
		player1.addToWorld(world);
		player1.scoreFont = S.getFnt().getFont(40);

		gameStage.addActor(player1);
		playerPos = player1.getWorldCenter();
		joylist = Controllers.getControllers();

		for (Controller c : joylist) {
			System.out.println("INPUT: " + c.getName());
		}

		player1.gamepad = gamepadInput;

		startTime = System.currentTimeMillis();

		setupMaze();

		InputMultiplexer multi = new InputMultiplexer();
		multi.addProcessor(hud);
		multi.addProcessor(player1.gamepad);
		setInputProcessor(multi);
		tickOffset = System.currentTimeMillis();
		gameStage_camera = gameStage.getCamera();

		nextOrb = DeathOrb.getLifeSpan() / 1000f;
	}

	public void setupMaze() {
		System.out.println("MAZE: " + level);
		tiles.clear();
		blockList.clear();
		calculateChallengeList(level);
		int maxFaceValue = getMaxChallenge();
		theChallenge = challengeList.removeIndex(0);
		addMazeToStage(level, maxFaceValue);
		player1.theChallenge = theChallenge;
		player1.badValue_clear();
		hud.addActor(label_level);
		label_level.setY(overscan.y);
		int blockListValue = getValue(blockList);
		maxInPlay = blockListValue;
		if (blockListValue < challengeTotalValue) {
			player1.badValue_add(challengeTotalValue - blockListValue);
		}
		System.out.println("challengeTotalValue: " + challengeTotalValue);
		System.out.println("blockListValue: " + blockListValue);
		System.out
				.println("player1.badValue: " + player1.badValue_getPending());
		label_level.setText("Maze Level: " + level
				+ "\nExplode DICE in this combination: "
				+ GenerateNumber.getCardinal(theChallenge));
		label_level.pack();
		// S.getSnd().play_number_sequence(this, GenerateNumber
		// .getAudioSequence(theChallenge));

		if (activeSong != null) {
			MusicStopEvent e = new MusicStopEvent();
			e.name = activeSong;
			NumbersMaze.post(e);
		}
		activeSong = app.songs[(level - 1) % app.songs.length];
		MusicPlayEvent e = new MusicPlayEvent();
		e.name = activeSong;
		e.loop = true;
		NumbersMaze.post(e);
	}

	static final float WORLD_TO_BOX = 20f;
	static final float BOX_TO_WORLD = 1f / WORLD_TO_BOX;
	private TheWorld world;
	final protected Rectangle culler = new Rectangle();
	private Box2DDebugRenderer debugRenderer = null;

	private Label info_label;
	private Label label_level;
	private Label label_score;

	private Array<Vector2> initialPortal = new Array<>();
	private Array<Vector2> numberPortal = new Array<>();

	private String activeSong = null;
	private PlayerInput gamepadInput = new PlayerInput() {
		@Override
		public boolean keyUp(int keycode) {
			if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
				ScreenChangeEvent e = new ScreenChangeEvent();
				e.data.put(data);
				e.screen = ScreenList.MainMenu;
				NumbersMaze.post(e);
				return true;
			}
			return super.keyUp(keycode);
		}
	};

	int maxInPlay = 15;

	long tickOffset = 0;

	@Override
	public void show() {
		super.show();
		startTime += System.currentTimeMillis() - tickOffset;
		Controllers.addListener(player1.gamepad);
		System.out.println("LEVEL: " + level);
		if (activeSong != null) {
			MusicPlayEvent e = new MusicPlayEvent();
			e.name = activeSong;
			e.loop = true;
			NumbersMaze.post(e);
		}
		if (ultimate && timeLeft == null) {
			AtlasRegion timeRemainingRegion = S.getArg().findRegion(
					"block_ltblue");
			NinePatch timeRemaining_9 = new NinePatch(timeRemainingRegion, 12,
					12, 12, 12);
			LabelStyle timeRemainingStyle = new LabelStyle(S.getFnt().getFont(
					20), new Color(Color.DARK_GRAY));
			timeRemainingStyle.background = new NinePatchDrawable(
					timeRemaining_9);
			timeRemainingStyle.font.setFixedWidthGlyphs("0123456789");
			timeLeft = new Label("00:00", timeRemainingStyle);
			timeLeft.pack();
			timeLeft.setX(overscan.x + overscan.width - timeLeft.getWidth());
			timeLeft.setOrigin(timeLeft.getWidth() / 2,
					timeLeft.getHeight() / 2);
			timeLeft.setHeight(timeLeft.getHeight() * .6f);
			timeLeft.layout();
			timeLeft.setY(overscan.y + overscan.height - timeLeft.getHeight());
			hud.addActor(timeLeft);
			updateTimeLeft();
		}
	}

	private float simElapsed = 0;
	private float simStepRate = 1f / 60f;

	private long lastShowPortalTick = 0;
	private int totalValueLeft = -1;
	private int theChallenge = 1;

	public int theScore = 0;
	private int lastScore = 0;

	private long levelCompleteTick = 0;
	private long restoreBlockTick = 0;

	private final Array<Entity> eList = new Array<>();

	Vector2 playerPos = new Vector2();

	private boolean showPortal;

	private long mem1;
	private long mem2;

	private long sinceLastNotice = 0;
	private int lastChallenge = 0;
	private SetMicPos smp = new SetMicPos();
	private float nextOrb = 0;

	@Override
	public void render(float delta) {

		if (lastChallenge != theChallenge) {
			lastChallenge = theChallenge;
			PlayNumberSequence p = new PlayNumberSequence();
			p.list.addAll(GenerateNumber.getAudioSequence(theChallenge));
			p.screen = this;
			NumbersMaze.post(p);
		}

		if (ultimate) {
			updateTimeLeft();
		}

		if (maxInPlay < theChallenge) {
			maxInPlay = theChallenge;
		}

		float w = DisplaySize._720p.width() * BOX_TO_WORLD + 13 * 32
				* BOX_TO_WORLD;
		float h = DisplaySize._720p.height() * BOX_TO_WORLD + 13 * 32
				* BOX_TO_WORLD;

		culler.x = playerPos.x - w / 2;
		culler.y = playerPos.y - h / 2;
		culler.width = w;
		culler.height = h;

		int camX = (int) player1.getX();
		int camY = (int) player1.getY();

		gameStage_camera.position.set(camX, camY, 0);
		gameStage_camera.update();

		super.render(delta);

		if (world == null || world.getWorld() == null) {
			return;
		}
		if (player1.gamepad.leftTrigger && debugRenderer != null) {
			final Matrix4 debugMatrix = new Matrix4(gameStage_camera.combined);
			debugMatrix.scale(WORLD_TO_BOX, WORLD_TO_BOX, 1f);
			debugRenderer.render(world.getWorld(), debugMatrix);
		}

		mem1 = Gdx.app.getJavaHeap();
		mem2 = Gdx.app.getNativeHeap();
		info_label.setText("FPS: " + Gdx.graphics.getFramesPerSecond()
				+ "\nMEM (j/n): " + mem1 / (1024 * 1024) + ", "
				+ mem2 / (1024 * 1024));
		info_label.pack();
		info_label.setY(hud.getHeight() - info_label.getHeight());
		smp.pos.set(playerPos);
		NumbersMaze.post(smp);

		showPortal = false;
		if (player1.getPendingScore() != 0) {
			theScore += player1.getPendingScoreAndZeroOut();
			if (theScore < 0) {
				theScore = 0;
			}
			if (challengeList.size > 0) {
				theChallenge = challengeList.removeIndex(0);
				player1.theChallenge = theChallenge;
				label_level.setText("Level: " + level + "\n"
						+ GenerateNumber.getCardinal(theChallenge));
				label_level.pack();
			} else {
				label_level.setText("Level: " + level + "\nᏄᎳ! ᏄᎳ!");
				label_level.pack();
				while (blockList.size > 0) {
					blockList.removeIndex(0).remove(true);
				}
			}
			if (maxInPlay < theChallenge) {
				maxInPlay = theChallenge;
			}
		}
		if (lastScore != theScore) {
			label_score.setText(theScore + "");
			lastScore = theScore;
			label_score.setText(String.format("%09d", theScore));
		}

		world.processOrphans(gameStage);
		int blockListValue = getValue(blockList);
		int inLimbo = player1.pointsInLimbo() + world.pointsInLimbo();
		int badValue_pending = player1.badValue_getPending()
				+ world.getBadAccumulator();
		totalValueLeft = blockListValue + badValue_pending + inLimbo;

		// random death orbs, time gap between is based on level
		nextOrb -= delta;
		if (nextOrb < 0f) {
			nextOrb = MathUtils.random(1f / level) * 60f
					+ DeathOrb.getLifeSpan() / 2000f;
			Vector2 new_block_pos = numberPortal.get(MathUtils
					.random(numberPortal.size - 1));
			if (centerSpotIsEmpty(new_block_pos)) {
				new DeathOrb(world.getWorld(), player1.getWorldScale(),
						new_block_pos, 0);
			}
		}

		if (blockListValue + inLimbo < maxInPlay
				&& System.currentTimeMillis() - restoreBlockTick > 500) {
			Vector2 new_block_pos = numberPortal.get(MathUtils
					.random(numberPortal.size - 1));
			if (centerSpotIsEmpty(new_block_pos)) {
				int maxFaceValue = getMaxChallenge();
				if (player1.badValue_hasPending()) {
					int dieValue = player1.badValue_getNext(maxFaceValue);
					int dieFace = dieValue;
					if (dieFace == 20) {
						dieFace = 7;
					}
					if (dieFace == 80) {
						dieFace = 8;
					}
					Entity tile = generateBlockTile(number_tile,
							new_block_pos.x, new_block_pos.y, dieFace - 1);
					blockList.add(tile);
				} else if (world.badValue_hasPending()) {
					int dieValue = world.badValue_getNext(maxFaceValue);
					int dieFace = dieValue;
					if (dieFace == 20) {
						dieFace = 7;
					}
					if (dieFace == 80) {
						dieFace = 8;
					}
					Entity tile = generateBlockTile(number_tile,
							new_block_pos.x, new_block_pos.y, dieFace - 1);
					blockList.add(tile);
				}
				if (System.currentTimeMillis() > sinceLastNotice) {
					lastChallenge = 0;
					sinceLastNotice = System.currentTimeMillis() + 30000;
				}
			}
			System.out.println("=== totalValueRemaining: " + totalValueLeft);
			restoreBlockTick = System.currentTimeMillis();
		}
		// should portal be available check

		if (totalValueLeft < theChallenge) {
			showPortal = true;
		} else {
			lastShowPortalTick = System.currentTimeMillis();
		}

		if (showPortal && thePortal != null
				&& System.currentTimeMillis() - lastShowPortalTick > 1500) {
			if (System.currentTimeMillis() - levelCompleteTick > 4000) {
				SoundPlayEvent e = new SoundPlayEvent();
				e.name = "level_finished";
				e.vol = .25f;
				NumbersMaze.post(e);
				levelCompleteTick = System.currentTimeMillis();
			}
			tiles.addActor(thePortal);
			thePortal.toFront();
			if (thePortal.getCollidesWith().size > 0) {
				Iterator<Entity> i = thePortal.getCollidesWith().iterator();
				while (i.hasNext()) {
					Entity e = i.next();
					if (e.identity == Entity.PLAYER) {
						totalValueLeft = -1;
						if (ultimate) {
							theScore += getSecondsLeft();
						}
						ScreenChangeEvent e1 = new ScreenChangeEvent();
						e1.data.putInteger("level", level + 1);
						e1.data.putInteger("score", theScore);
						e1.data.putBoolean("ultimate", ultimate);
						e1.screen = ScreenList.OnePlayer;
						NumbersMaze.post(e1);
						return;
					}
				}

			}
		}
		updateBox2dWorld();
		updateStageWithWorld();
	}

	private boolean centerSpotIsEmpty(Vector2 new_block_pos) {
		class IsEmpty {
			boolean state = true;
		}
		final Vector2 start = new Vector2();
		final Vector2 box_size = new Vector2();
		final Vector2 warp_upper = new Vector2();
		final Vector2 warp_lower = new Vector2();

		start.set(new_block_pos);
		start.scl(tileGrideSize);// convert to pixels
		// shift by -16 for both x & y to match up for generateblock math
		start.add(-16, -16);

		box_size.set(9f, 9f); // PIXELS distance from CENTER
		warp_lower.set(start);
		warp_upper.set(start);

		warp_lower.sub(box_size);
		warp_upper.add(box_size);

		warp_lower.scl(BOX_TO_WORLD);
		warp_upper.scl(BOX_TO_WORLD);

		final IsEmpty isEmpty = new IsEmpty();
		isEmpty.state = true;
		world.getWorld().QueryAABB(new QueryCallback() {
			@Override
			public boolean reportFixture(Fixture fixture) {
				Body b = fixture.getBody();
				Object o = b.getUserData();
				if (o instanceof Entity) {
					Entity e = (Entity) o;
					if (e.identity != Entity.FLOOR) {
						isEmpty.state = false;
						return false;
					}
				}
				return true;
			}
		}, warp_lower.x, warp_lower.y, warp_upper.x, warp_upper.y);
		return isEmpty.state;
	}

	private long simStart = 0;

	private final Vector2 aabb_size = new Vector2();
	private final Vector2 aabb_lower = new Vector2();
	private final Vector2 aabb_upper = new Vector2();

	private final Array<Entity> floorTiles = new Array<>();

	private void updateBox2dWorld() {
		float simDelta = (System.currentTimeMillis() - simStart) / 1000f;
		simStart = System.currentTimeMillis();
		if (simDelta > simStepRate * 2) {
			simDelta = simStepRate * 2;
		}
		simElapsed += simDelta;
		if (simElapsed > simStepRate) {
			world.step(simStepRate);
			simElapsed -= simStepRate;
		}

	}

	private void updateStageWithWorld() {
		aabb_size.x = culler.width;// DisplaySize._720p.width() /
									// player1.getWorldScale() + 64f *
									// BOX_TO_WORLD;
		aabb_size.y = culler.height;// DisplaySize._720p.height()/
									// player1.getWorldScale() + 64f *
									// BOX_TO_WORLD;

		aabb_lower.x = playerPos.x - aabb_size.x / 2;
		aabb_lower.y = playerPos.y - aabb_size.y / 2;
		aabb_upper.x = playerPos.x + aabb_size.x / 2;
		aabb_upper.y = playerPos.y + aabb_size.y / 2;
		eList.clear();
		world.getWorld().QueryAABB(new QueryCallback() {
			@Override
			public boolean reportFixture(Fixture fixture) {
				Object a;
				a = fixture.getUserData();
				if (a == null) {
					a = fixture.getBody().getUserData();
				}
				if (a != null && a instanceof Entity) {
					Entity e = (Entity) a;
					eList.add(e);
				}
				return true;
			}
		}, aabb_lower.x, aabb_lower.y, aabb_upper.x, aabb_upper.y);

		// floors
		for (Entity e : eList) {
			if (e.identity != Entity.FLOOR) {
				continue;
			}
			e.remove();
			tiles.addActor(e);
			e.updatePosition(true);
		}
		// walls
		for (Entity e : eList) {
			if (e.identity != Entity.WALL) {
				continue;
			}
			e.remove();
			tiles.addActor(e);
			e.updatePosition(true);
		}
		// blocks
		for (Entity e : eList) {
			if (e.identity != Entity.BLOCK) {
				continue;
			}
			e.remove();
			tiles.addActor(e);
			e.updatePosition(true);
		}
		// actors
		for (Entity e : eList) {
			if (e.getParent()!=null) {
				continue;
			}
			if (e.identity == Entity.PORTAL) {
				continue;
			}
			if (e.identity == Entity.WALL | e.identity == Entity.BLOCK
					| e.identity == Entity.FLOOR) {
				continue;
			}
			
			e.updatePosition(true);

			gameStage.addActor(e);
			e.updatePosition(true);

		}

	}

	private int getValue(Array<Entity> g) {
		int value = 0;
		for (Entity e : g) {
			if (e.identity == Entity.BLOCK) {
				value += e.value;
			}
		}
		return value;
	}

	public int level = 1;

	int mazeW = 20;
	int mazeH = 10;
	int tileGrideSize = 32;

	AtlasRegion[] number_tile = null;
	private Array<Entity> blockList = new Array<>();

	private void addMazeToStage(int mazeNumber, int maxFaceValue) {
		if (maxFaceValue < 1) {
			maxFaceValue = 1;
		}
		Random number_tile_random = new Random(mazeNumber);

		number_tile = new AtlasRegion[8];
		number_tile[0] = S.getArg().findRegion("d1");
		number_tile[1] = S.getArg().findRegion("d2");
		number_tile[2] = S.getArg().findRegion("d3");
		number_tile[3] = S.getArg().findRegion("d4");
		number_tile[4] = S.getArg().findRegion("d5");
		number_tile[5] = S.getArg().findRegion("d6");
		number_tile[6] = S.getArg().findRegion("super-die-20");
		number_tile[7] = S.getArg().findRegion("super-die-80");

		AtlasRegion[] floor_tile = new AtlasRegion[5];
		for (int ix = 0; ix < 5; ix++) {
			floor_tile[ix] = S.getArg().findRegion("floor" + ix);
		}

		AtlasRegion wall_tile = S.getArg().findRegion("wall");
		AtlasRegion wall_tile2 = S.getArg().findRegion("wall2");
		AtlasRegion wall_tile3 = S.getArg().findRegion("wall3");
		AtlasRegion wall_tile4 = S.getArg().findRegion("wall4");
		AtlasRegion wall_tile8 = S.getArg().findRegion("wall8");
		AtlasRegion wall_tilev2 = S.getArg().findRegion("wallv2");
		AtlasRegion wall_tilev3 = S.getArg().findRegion("wallv3");
		AtlasRegion wall_tilev4 = S.getArg().findRegion("wallv4");
		AtlasRegion wall_tilev8 = S.getArg().findRegion("wallv8");
		AtlasRegion floor_portal = S.getArg().findRegion("floor-portal");

		initialPortal.clear();
		numberPortal.clear();

		mazeW = mazeNumber + 6;
		mazeH = mazeNumber + 3;
		if (mazeW > 16) {
			mazeW = 16 + mazeNumber / 16;
		}
		if (mazeH > 9) {
			mazeH = 9 + mazeNumber / 9;
		}
		Maze mazeGen = new Maze(mazeNumber, mazeW, mazeH);
		MazeCell[][] maze = mazeGen.get();
		mazeGen = null;
		int displayGrid[][] = new int[maze.length * 3 + 1][maze[0].length * 3 + 1];

		Array<Entity> imgList = new Array<>();

		final int tile_blank = -1;
		final int tile_floor = 0;
		final int tile_h1 = 1;
		final int tile_h3 = 6;
		final int tile_h4 = 4;
		final int tile_h8 = 5;
		final int tile_v8 = 7;
		final int tile_v4 = 8;
		final int tile_v2 = 9;
		final int tile_block = 2;
		final int tile_portal = 3;
		final int tile_h2 = 10;
		final int tile_v3 = 11;

		for (int ix = 0; ix < maze.length; ix++) {
			for (int iy = 0; iy < maze[0].length; iy++) {
				MazeCell cell = maze[ix][iy];
				// Entity tile;
				boolean isWestEdge = ix == 0;
				boolean isEastEdge = ix + 1 == maze.length;
				boolean isNorthEdge = iy + 1 == maze[0].length;
				boolean isSouthEdge = iy == 0;

				// 4x4 tiled grid for each maze cell
				int grid[][] = new int[4][4];
				for (int bx = 0; bx < 4; bx++) {
					for (int by = 0; by < 4; by++) {
						grid[bx][by] = tile_floor; // only floors to start with
					}
				}
				int wallCount = 0;
				if (cell.wall.e) {
					wallCount++;
					if (isEastEdge) {
						grid[3][0] = tile_h1;
						grid[3][1] = tile_h1;
						grid[3][2] = tile_h1;
						grid[3][3] = tile_h1;
					}
				}
				if (cell.wall.w) {
					wallCount++;
					grid[0][0] = tile_h1;
					grid[0][1] = tile_h1;
					grid[0][2] = tile_h1;
					grid[0][3] = tile_h1;
				}
				if (cell.wall.n) {
					wallCount++;
					grid[0][3] = tile_h1;
					grid[1][3] = tile_h1;
					grid[2][3] = tile_h1;
					grid[3][3] = tile_h1;
				}
				if (cell.wall.s) {
					wallCount++;
					if (isSouthEdge) {
						grid[0][0] = tile_h1;
						grid[1][0] = tile_h1;
						grid[2][0] = tile_h1;
						grid[3][0] = tile_h1;
					}
				}

				// add tiles to match grid pattern
				for (int bx = 0; bx < 4; bx++) {
					for (int by = 0; by < 4; by++) {
						int c = grid[bx][by];
						if (c != tile_h1) {
							continue;
						}
						displayGrid[3 * ix + bx][3 * iy + by] = c;
					}
				}
				// if three sides are walled, mark as a number block portal
				if (wallCount > 2) {
					initialPortal.add(new Vector2(3 * ix + 2, 3 * iy + 2));
					// displayGrid[3 * ix + 2][3 * iy + 2] = tile_block;
				}
				// NE edge is special exit thingy
				if (isEastEdge && isNorthEdge) {
					displayGrid[3 * ix + 2][3 * iy + 2] = tile_portal;
				}
				numberPortal.add(new Vector2(3 * ix + 2, 3 * iy + 2));
			}
		}

		boolean createComposites = true;
		if (createComposites) {
			// look for horizontal block runs that can be converted into
			// composites
			for (int ix = 0; ix < displayGrid.length; ix++) {
				for (int iy = 0; iy < displayGrid[0].length; iy++) {
					int len = 0;
					int tag = 0;

					// convert incomplete 8 wide composites to single blocks
					len = 8;
					tag = tile_h8;
					if (displayGrid[ix][iy] == tile_h1) {
						int c = 0;
						for (int dx = 1; dx < len
								&& ix + dx < displayGrid.length; dx++) {
							if (displayGrid[ix + dx][iy] == tile_h1) {
								c++;
								continue;
							}
							break;
						}
						if (c == len - 1) {
							displayGrid[ix][iy] = tag;
							for (int dx = 1; dx < len
									&& ix + dx < displayGrid.length; dx++) {
								displayGrid[ix + dx][iy] = tile_blank;
							}
						}
					}
					// convert incomplete 4 wide composites to single blocks
					len = 4;
					tag = tile_h4;
					if (displayGrid[ix][iy] == tile_h1) {
						int c = 0;
						for (int dx = 1; dx < len
								&& ix + dx < displayGrid.length; dx++) {
							if (displayGrid[ix + dx][iy] == tile_h1) {
								c++;
								continue;
							}
							break;
						}
						if (c == len - 1) {
							displayGrid[ix][iy] = tag;
							for (int dx = 1; dx < len
									&& ix + dx < displayGrid.length; dx++) {
								displayGrid[ix + dx][iy] = tile_blank;
							}
						}
					}
					// convert incomplete 3 wide composites to single blocks
					len = 3;
					tag = tile_h3;
					if (displayGrid[ix][iy] == tile_h1) {
						int c = 0;
						for (int dx = 1; dx < len
								&& ix + dx < displayGrid.length; dx++) {
							if (displayGrid[ix + dx][iy] == tile_h1) {
								c++;
								continue;
							}
							break;
						}
						if (c == len - 1) {
							displayGrid[ix][iy] = tag;
							for (int dx = 1; dx < len
									&& ix + dx < displayGrid.length; dx++) {
								displayGrid[ix + dx][iy] = tile_blank;
							}
						}
					}
					// convert incomplete 2 wide composites to single blocks
					len = 2;
					tag = tile_h2;
					if (displayGrid[ix][iy] == tile_h1) {
						int c = 0;
						for (int dx = 1; dx < len
								&& ix + dx < displayGrid.length; dx++) {
							if (displayGrid[ix + dx][iy] == tile_h1) {
								c++;
								continue;
							}
							break;
						}
						if (c == len - 1) {
							displayGrid[ix][iy] = tag;
							for (int dx = 1; dx < len
									&& ix + dx < displayGrid.length; dx++) {
								displayGrid[ix + dx][iy] = tile_blank;
							}
						}
					}
				}
			}

			// look for vertical block runs that can be converted into
			// composites

			for (int iy = 0; iy < displayGrid[0].length; iy++) {
				for (int ix = 0; ix < displayGrid.length; ix++) {
					int len = 0;
					int tag = 0;

					// convert incomplete 8 high composites to single blocks
					len = 8;
					tag = tile_v8;
					if (displayGrid[ix][iy] == tile_h1) {
						int c = 0;
						for (int dy = 1; dy < len
								&& iy + dy < displayGrid[0].length; dy++) {
							if (displayGrid[ix][iy + dy] == tile_h1) {
								c++;
								continue;
							}
							break;
						}
						if (c == len - 1) {
							displayGrid[ix][iy] = tag;
							for (int dy = 1; dy < len
									&& iy + dy < displayGrid.length; dy++) {
								displayGrid[ix][iy + dy] = tile_blank;
							}
						}
					}
					// convert incomplete 4 high composites to single blocks
					len = 4;
					tag = tile_v4;
					if (displayGrid[ix][iy] == tile_h1) {
						int c = 0;
						for (int dy = 1; dy < len
								&& iy + dy < displayGrid[0].length; dy++) {
							if (displayGrid[ix][iy + dy] == tile_h1) {
								c++;
								continue;
							}
							break;
						}
						if (c == len - 1) {
							displayGrid[ix][iy] = tag;
							for (int dy = 1; dy < len
									&& iy + dy < displayGrid.length; dy++) {
								displayGrid[ix][iy + dy] = tile_blank;
							}
						}
					}
					// convert incomplete 3 high composites to single blocks
					len = 3;
					tag = tile_v3;
					if (displayGrid[ix][iy] == tile_h1) {
						int c = 0;
						for (int dy = 1; dy < len
								&& iy + dy < displayGrid[0].length; dy++) {
							if (displayGrid[ix][iy + dy] == tile_h1) {
								c++;
								continue;
							}
							break;
						}
						if (c == len - 1) {
							displayGrid[ix][iy] = tag;
							for (int dy = 1; dy < len
									&& iy + dy < displayGrid.length; dy++) {
								displayGrid[ix][iy + dy] = tile_blank;
							}
						}
					}
					// convert incomplete 2 high composites to single blocks
					len = 2;
					tag = tile_v2;
					if (displayGrid[ix][iy] == tile_h1) {
						int c = 0;
						for (int dy = 1; dy < len
								&& iy + dy < displayGrid[0].length; dy++) {
							if (displayGrid[ix][iy + dy] == tile_h1) {
								c++;
								continue;
							}
							break;
						}
						if (c == len - 1) {
							displayGrid[ix][iy] = tag;
							for (int dy = 1; dy < len
									&& iy + dy < displayGrid.length; dy++) {
								displayGrid[ix][iy + dy] = tile_blank;
							}
						}
					}
				}
			}
		}

		// stick floor tiles under everything, starting 1 block in all
		// directions
		floorTiles.clear();
		int xLen = displayGrid.length;
		int yLen = displayGrid[0].length;
		for (int ix = 1; ix < xLen; ix += 8) {
			for (int iy = 1; iy < yLen; iy += 8) {
				int w = xLen - ix - 2;
				int h = yLen - iy - 2;
				if (w > 7) {
					w = 7;
				}
				if (h > 7) {
					h = 7;
				}
				float posX = ix - w / 2f;
				float posY = iy - h / 2f;
				float width = tileGrideSize * (w + 1);
				float height = tileGrideSize * (h + 1);
				int ml = floor_tile.length;
				int mi = level % ml;
				Entity tile = generateFloorTile(floor_tile[mi], posX, posY,
						width, height);
				tile.identity = Entity.FLOOR;
				imgList.add(tile);
				floorTiles.add(tile);
			}
		}
		for (int ix = 0; ix < displayGrid.length; ix++) {
			for (int iy = 0; iy < displayGrid[0].length; iy++) {
				// single block wall
				if (displayGrid[ix][iy] == tile_h1) {
					displayGrid[ix][iy] = tile_blank;
					Entity tile;
					tile = generateWallTile(wall_tile, ix, iy);
					tile.identity = Entity.WALL;
					imgList.add(tile);
				}
				// 2 wide composite block wall
				if (displayGrid[ix][iy] == tile_h2) {
					AtlasRegion a = wall_tile2;
					float to = 0.5f;
					displayGrid[ix][iy] = tile_blank;
					Entity tile;
					tile = generateWallTile(a, ix + to, iy);
					tile.identity = Entity.WALL;
					imgList.add(tile);
				}
				// 3 wide composite block wall
				if (displayGrid[ix][iy] == tile_h3) {
					displayGrid[ix][iy] = tile_blank;
					Entity tile;
					tile = generateWallTile(wall_tile3, ix + 1f, iy);
					tile.identity = Entity.WALL;
					imgList.add(tile);
				}
				// 4 wide composite block wall
				if (displayGrid[ix][iy] == tile_h4) {
					displayGrid[ix][iy] = tile_blank;
					Entity tile;
					tile = generateWallTile(wall_tile4, ix + 1.5f, iy);
					tile.identity = Entity.WALL;
					imgList.add(tile);
				}
				// 8 wide composite block wall
				if (displayGrid[ix][iy] == tile_h8) {
					displayGrid[ix][iy] = tile_blank;
					Entity tile;
					tile = generateWallTile(wall_tile8, ix + 3.5f, iy);
					tile.identity = Entity.WALL;
					imgList.add(tile);
				}
				// vertical composites
				// 8 tall composite block wall
				if (displayGrid[ix][iy] == tile_v8) {
					AtlasRegion a = wall_tilev8;
					float to = 3.5f;
					displayGrid[ix][iy] = tile_blank;
					Entity tile;
					tile = generateWallTile(a, ix, iy + to);
					tile.identity = Entity.WALL;
					imgList.add(tile);
				}
				// 4 high composite block wall
				if (displayGrid[ix][iy] == tile_v4) {
					AtlasRegion a = wall_tilev4;
					float to = 1.5f;
					displayGrid[ix][iy] = tile_blank;
					Entity tile;
					tile = generateWallTile(a, ix, iy + to);
					tile.identity = Entity.WALL;
					imgList.add(tile);
				}
				// 3 high composite block wall
				if (displayGrid[ix][iy] == tile_v3) {
					AtlasRegion a = wall_tilev3;
					float to = 1f;
					displayGrid[ix][iy] = tile_blank;
					Entity tile;
					tile = generateWallTile(a, ix, iy + to);
					tile.identity = Entity.WALL;
					imgList.add(tile);
				}
				// 2 high composite block wall
				if (displayGrid[ix][iy] == tile_v2) {
					AtlasRegion a = wall_tilev2;
					float to = 0.5f;
					displayGrid[ix][iy] = tile_blank;
					Entity tile;
					tile = generateWallTile(a, ix, iy + to);
					tile.identity = Entity.WALL;
					imgList.add(tile);
				}

				if (displayGrid[ix][iy] == tile_portal) {
					Entity tile = new Entity(floor_portal);
					tile.identity = Entity.PORTAL;
					thePortal = tile;
					// tile.setColor(Color.BLUE);
					// imgList.add(tile);

					float tileSize = tile.getWidth();
					// box2d is center of body based, set origin of object to
					// match
					tile.setOrigin(tile.getWidth() / 2, tile.getHeight() / 2);
					tile.setScale(1);
					tile.setOffsetX(-tile.getWidth() / 2);
					tile.setOffsetY(-tile.getHeight() / 2);
					tile.setWorldScale(WORLD_TO_BOX);

					BodyDef bodyDef = new BodyDef();
					bodyDef.type = BodyType.DynamicBody;
					bodyDef.position.set((-16 + ix * tileGrideSize)
							* BOX_TO_WORLD, (-16 + iy * tileGrideSize)
							* BOX_TO_WORLD);
					float rad = tile.getWidth() * BOX_TO_WORLD / 2;
					Body body = world.getWorld().createBody(bodyDef);
					body.setFixedRotation(false);
					body.setLinearVelocity(new Vector2(0f, 0f));
					body.setLinearDamping(1f);
					// CircleShape circle=new CircleShape();
					// circle.setRadius(rad);
					PolygonShape box = new PolygonShape();
					box.setAsBox((tile.getWidth() - 1) / 2 * BOX_TO_WORLD,
							(tile.getHeight() - 1) / 2 * BOX_TO_WORLD);
					FixtureDef fDef = new FixtureDef();
					fDef.density = .01f;
					fDef.friction = 1f;
					fDef.restitution = 0.6f;
					fDef.shape = box;
					fDef.isSensor = true;
					// fDef.shape=circle;
					fDef.filter.categoryBits = TheWorld.TYPE_SENSOR;
					fDef.filter.maskBits = TheWorld.TYPE_PLAYER;
					body.createFixture(fDef);
					// body.createFixture(box, 0.1f);
					body.setUserData(tile);
					box.dispose();
					// circle.dispose();
					body.setGravityScale(0f);
					tile.setBody(body);
				}
			}
		}
		// create and place the initial number blocks
		for (int ix = 0; ix < initialPortal.size; ix++) {
			Vector2 pos = initialPortal.get(ix);
			int die_face;
			if (maxFaceValue < 30) {
				do {
					die_face = number_tile_random.nextInt(6);
					if (level == 1 && ix == 0) {
						die_face = 0;
					}
				} while (die_face >= maxFaceValue);
			} else if (maxFaceValue < 100) {
				die_face = number_tile_random.nextInt(7);
			} else {
				die_face = number_tile_random.nextInt(8);
			}
			Entity tile = generateBlockTile(number_tile, pos.x, pos.y, die_face);
			imgList.add(tile);
			blockList.add(tile);
		}
		tiles.setTransform(false);
		tiles.clear();
		for (int i = 0; i < imgList.size; i++) {
			Entity x = imgList.get(i);
			x.setCull(culler);
			tiles.addActor(x);
		}
	}

	private Body wallBody = null;
	private Entity wallStart = null;
	private Body floorBody = null;
	private Entity floorStart = null;
	public boolean ultimate = false;
	public long timelimit = 0l;

	private Entity generateFloorTile(AtlasRegion floor_tile, float ix,
			float iy, float w_pix, float h_pix) {
		float px = ix * tileGrideSize + (w_pix - tileGrideSize);
		float py = iy * tileGrideSize + (h_pix - tileGrideSize);
		Entity tile;
		tile = new Entity(new TiledDrawable(floor_tile));
		tile.identity = Entity.FLOOR;
		// box2d is center of body based, set origin of object to
		// match
		tile.setWidth(w_pix);
		tile.setHeight(h_pix);
		tile.setOrigin(w_pix / 2, h_pix / 2);
		tile.setScale(1);
		tile.setOffsetX(-w_pix / 2);
		tile.setOffsetY(-h_pix / 2);
		tile.setWorldScale(WORLD_TO_BOX);
		tile.setPosition(px, py);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(px * BOX_TO_WORLD, py * BOX_TO_WORLD);
		float rad = tile.getWidth() * BOX_TO_WORLD / 2;
		Body body = world.getWorld().createBody(bodyDef);
		body.setFixedRotation(false);
		body.setLinearVelocity(new Vector2(0f, 0f));
		body.setLinearDamping(1f);
		PolygonShape box = new PolygonShape();
		box.setAsBox((tile.getWidth() - 1) / 2 * BOX_TO_WORLD,
				(tile.getHeight() - 1) / 2 * BOX_TO_WORLD);
		FixtureDef fDef = new FixtureDef();
		fDef.density = 10f;
		fDef.friction = 1f;
		fDef.restitution = 0.6f;
		fDef.shape = box;
		fDef.filter.categoryBits = TheWorld.TYPE_FLOOR;
		fDef.filter.maskBits = (short) (TheWorld.TYPE_FLOOR | TheWorld.TYPE_WALL);

		body.createFixture(fDef).setUserData(tile);
		body.setUserData(tile);
		box.dispose();
		tile.setBody(body);
		return tile;
	}

	private Entity generateWallTile(AtlasRegion wall_tile, float ix, float iy) {
		Entity tile;
		tile = new Entity(wall_tile);
		tile.identity = Entity.WALL;
		// box2d is center of body based, set origin of object to
		// match
		tile.setOrigin(tile.getWidth() / 2, tile.getHeight() / 2);
		tile.setScale(1);
		tile.setOffsetX(-tile.getWidth() / 2);
		tile.setOffsetY(-tile.getHeight() / 2);
		tile.setWorldScale(WORLD_TO_BOX);
		tile.setPosition(ix * tileGrideSize, iy * tileGrideSize);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(ix * tileGrideSize * BOX_TO_WORLD, iy
				* tileGrideSize * BOX_TO_WORLD);
		float rad = tile.getWidth() * BOX_TO_WORLD / 2;
		Body body = world.getWorld().createBody(bodyDef);
		body.setFixedRotation(false);
		body.setLinearVelocity(new Vector2(0f, 0f));
		body.setLinearDamping(1f);
		PolygonShape box = new PolygonShape();
		box.setAsBox((tile.getWidth() - 1) / 2 * BOX_TO_WORLD,
				(tile.getHeight() - 1) / 2 * BOX_TO_WORLD);
		FixtureDef fDef = new FixtureDef();
		fDef.density = 10f;
		fDef.friction = 1f;
		fDef.restitution = 0.6f;
		fDef.shape = box;
		fDef.filter.categoryBits = TheWorld.TYPE_WALL;
		fDef.filter.maskBits = TheWorld.TYPE_ALL;

		body.createFixture(fDef).setUserData(tile);
		body.setUserData(tile);
		box.dispose();
		tile.setBody(body);
		return tile;
	}

	private Entity generateBlockTile(AtlasRegion[] number_tile, float ix,
			float iy, final int die_face) {
		Entity tile = new Entity(number_tile[die_face]);
		tile.identity = Entity.BLOCK;
		tile.value = die_face + 1;
		if (die_face == 6) {
			tile.value = 20;
		}
		if (die_face == 7) {
			tile.value = 80;
		}
		tile.setColor(Color.WHITE);
		// float tileSize = tile.getWidth();
		// box2d is center of body based, set origin of object to
		// match
		tile.setOrigin(tile.getWidth() / 2, tile.getHeight() / 2);
		tile.setScale(1);
		tile.setOffsetX(-tile.getWidth() / 2);
		tile.setOffsetY(-tile.getHeight() / 2);
		tile.setWorldScale(WORLD_TO_BOX);
		tile.setScale(.9f);
		tile.setPosition(ix * tileGrideSize, iy * tileGrideSize);
		tile.layout();
		// tileSize *= tile.getScaleX();

		float width = tile.getWidth() * tile.getScaleX();
		float height = tile.getHeight() * tile.getScaleY();

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set((-16 + ix * tileGrideSize) * BOX_TO_WORLD,
				(-16 + iy * tileGrideSize) * BOX_TO_WORLD);
		// float rad = (tile.getWidth() * BOX_TO_WORLD / 2);
		Body body = world.getWorld().createBody(bodyDef);
		body.setFixedRotation(false);
		body.setLinearVelocity(new Vector2(0f, 0f));
		body.setLinearDamping(1f);
		PolygonShape box = new PolygonShape();
		box.setAsBox((width - 1) / 2 * BOX_TO_WORLD, (height - 1) / 2
				* BOX_TO_WORLD);
		FixtureDef fDef = new FixtureDef();
		fDef.density = tile.value * .01f;
		fDef.friction = 1f;
		fDef.restitution = 0.6f;
		fDef.shape = box;
		if (tile.value < 80) {
			fDef.filter.categoryBits = TheWorld.TYPE_BLOCK;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL
					^ TheWorld.TYPE_FLOOR ^ TheWorld.TYPE_PLAYER);
			;
		} else {
			fDef.filter.categoryBits = TheWorld.TYPE_WALL;
			fDef.filter.maskBits = (short) (TheWorld.TYPE_ALL ^ TheWorld.TYPE_FLOOR);
			;
		}
		body.createFixture(fDef);
		body.setUserData(tile);
		box.dispose();
		body.setGravityScale(1f);
		tile.setBody(body);
		return tile;
	}

	@Override
	public void hide() {
		super.hide();
		tickOffset = System.currentTimeMillis();
		if (activeSong != null) {
			MusicPauseEvent e = new MusicPauseEvent();
			e.name = activeSong;
			NumbersMaze.post(e);
		}
		if (player1 != null) {
			Controllers.removeListener(player1.gamepad);
		}
		for (int ix = 0; ix < screenRunnable.size; ix++) {
			if (screenRunnable.get(ix) instanceof NumberSequenceRunnable) {
				screenRunnable.removeIndex(ix);
				ix--;
			}
		}
		StopNumbers s = new StopNumbers();
		NumbersMaze.post(s);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (activeSong != null) {
			MusicStopEvent e = new MusicStopEvent();
			e.name = activeSong;
			NumbersMaze.post(e);
		}

		player1 = null;
		if (backDrop != null) {
			backDrop.clear();
		}
		if (gameStage != null) {
			gameStage.clear();
		}
		if (tiles != null) {
			tiles.clear();
		}
		if (debugRenderer != null) {
			debugRenderer.dispose();
			debugRenderer = null;
		}
		if (world != null) {
			world.dispose();
			world = null;
		}
	}
}
