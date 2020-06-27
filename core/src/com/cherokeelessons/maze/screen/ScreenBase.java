package com.cherokeelessons.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cherokeelessons.maze.DisplaySize;
import com.cherokeelessons.maze.NumbersMaze;
import com.cherokeelessons.maze.NumbersMaze.ScreenChangeEvent;
import com.cherokeelessons.maze.NumbersMaze.ScreenList;
import com.cherokeelessons.maze.stage.StageBase;

public class ScreenBase implements Screen {

	protected interface Renderer {
		void render(float delta);
	}

	protected boolean isDisposed = false;
	protected SpriteBatch sb;
	protected AssetManager assets;
	protected StageBase gameStage;
	protected StageBase stageSafeZone;
	protected StageBase backDrop;
	protected StageBase hud;
	protected DisplaySize.Resolution stageSize;
	private boolean debug;

	private boolean showFPS;

	final protected Vector2 viewPortSize = new Vector2();

	public DisplaySize mode;
	protected Color backgroundColor;
	private float currentElapsed;
	private final FPSLogger fps;
	protected boolean showOverScan = true;

	protected Renderer r_clear = new Renderer() {
		@Override
		public void render(final float delta) {
			Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
	};

	protected Renderer r_backDrop = new Renderer() {
		@Override
		public void render(final float delta) {
			backDrop.getViewport().apply();
			backDrop.draw();
		}
	};
	long gameStageActTime = 0;
	long gameStageRenderTime = 0;
	protected Renderer r_stage = new Renderer() {
		@Override
		public void render(final float delta) {
			long start;

			start = System.currentTimeMillis();
			gameStage.act(delta);
			gameStageActTime = (System.currentTimeMillis() - start + gameStageActTime * 4) / 5;

			start = System.currentTimeMillis();
			gameStage.getViewport().apply();
			gameStage.draw();
			gameStageRenderTime = (System.currentTimeMillis() - start + gameStageRenderTime * 4) / 5;
		}
	};
	protected Renderer r_hud = new Renderer() {

		@Override
		public void render(final float delta) {
			hud.act(delta);
			hud.getViewport().apply();
			hud.draw();
		}
	};
	protected Renderer r_overscan = new Renderer() {
		@Override
		public void render(final float delta) {
			if (showOverScan) {
				drawOverscan();
			}
		}
	};
	private ShapeRenderer r = null;
	final public Rectangle overscan = new Rectangle();

	InputProcessor screenInputProcessor = null;
	protected Array<Runnable> screenRunnable = new Array<>();

	public ScreenBase() {
		sb = new SpriteBatch();
		stageSize = DisplaySize._720p.size();
		gameStage = new StageBase(new FitViewport(stageSize.w, stageSize.h));
		stageSafeZone = new StageBase(new FitViewport(stageSize.w, stageSize.h));
		backDrop = new StageBase(new FitViewport(stageSize.w, stageSize.h));
		hud = new StageBase(new FitViewport(stageSize.w, stageSize.h));
		setDebug(true);
		setShowFPS(true);
		backgroundColor = new Color(Color.BLACK);
		currentElapsed = 0;
		fps = new FPSLogger();
		assets = new AssetManager();

		final float gap = 0.075f;
		final float gw = stageSize.w * gap;
		final float gh = stageSize.h * gap;
		overscan.x = gw;
		overscan.y = gh;
		overscan.width = stageSize.w - gw * 2;
		overscan.height = stageSize.h - gh * 2;
	}

	private void connectInputProcessor() {
		if (screenInputProcessor == null) {
			Gdx.input.setInputProcessor(hud);
		} else {
			Gdx.input.setInputProcessor(screenInputProcessor);
		}
	}

	private void disconnectInputProcessor() {
		if (Gdx.input.getInputProcessor() == null) {
			return;
		}
		screenInputProcessor = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void dispose() {
		if (isDisposed) {
			return;
		}
		isDisposed = true;
		Gdx.app.log(this.getClass().getSimpleName(), "Dispose: " + getClass().getSimpleName());
		disconnectInputProcessor();

		gameStage.clear();
		gameStage.dispose();
		gameStage = null;

		stageSafeZone.clear();
		stageSafeZone.dispose();
		stageSafeZone = null;

		backDrop.clear();
		backDrop.dispose();
		backDrop = null;

		assets.clear();
		assets.dispose();
		assets = null;

		if (r != null) {
			r.dispose();
			r = null;
		}
	}

	private void drawOverscan() {
		stageSafeZone.getViewport().apply(true);
		final Camera cam = stageSafeZone.getCamera();
		if (r == null) {
			return;
		}
		r.setProjectionMatrix(cam.combined);
		r.begin(ShapeType.Line);
		r.setColor(Color.RED);
		r.rect(overscan.x, overscan.y, overscan.width, overscan.height);
		r.setColor(Color.BLUE);
		r.rect(-1, -1, DisplaySize._720p.width() + 1, DisplaySize._720p.height() + 1);
		r.end();
	}

	@Override
	public void hide() {
		if (isDisposed) {
			return;
		}
		Gdx.app.log(this.getClass().getSimpleName(), "Hide: " + getClass().getSimpleName());
		disconnectInputProcessor();
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isShowFPS() {
		return showFPS;
	}

	@Override
	public void pause() {
		if (isDisposed) {
			return;
		}
		Gdx.app.log(this.getClass().getSimpleName(), "Pause: " + getClass().getSimpleName());
		final ScreenChangeEvent e = new ScreenChangeEvent();
		e.screen = ScreenList.Paused;
		NumbersMaze.post(e);
	}

	public void postRunnable(final Runnable runnable) {
		synchronized (screenRunnable) {
			screenRunnable.add(runnable);
		}
	}

	@Override
	public void render(final float delta) {
		if (isDisposed) {
			return;
		}
		synchronized (screenRunnable) {
			int times = screenRunnable.size;
			while (times > 0) {
				screenRunnable.removeIndex(0).run();
				times--;
			}
		}
		r_clear.render(delta);
		r_backDrop.render(delta);
		r_stage.render(delta);
		r_hud.render(delta);
		r_overscan.render(delta);

		currentElapsed += delta;
		if (currentElapsed > 2) {
			currentElapsed = 0;
			if (isShowFPS()) {
				fps.log();
			}
		}
	}

	@Override
	public void resize(final int width, final int height) {
		if (isDisposed) {
			return;
		}
		float scale_h;
		float scale;
		float newWidth;
		float newHeight;

		/*
		 * http://www.badlogicgames.com/forum/viewtopic.php?f=11&t=3422
		 */
		scale = (float) stageSize.w / (float) width;
		scale_h = (float) stageSize.h / (float) height;

		if (scale_h > scale) {
			scale = scale_h;
		}

		newWidth = (float) Math.ceil(scale * width);
		newHeight = (float) Math.ceil(scale * height);

		viewPortSize.x = newWidth;
		viewPortSize.y = newHeight;

		if (isDebug()) {
			Gdx.app.log(this.getClass().getSimpleName(), "=============================");
			Gdx.app.log(this.getClass().getSimpleName(), "scale: " + scale);
			Gdx.app.log(this.getClass().getSimpleName(), "Width: " + newWidth + ", Height: " + newHeight);
			Gdx.app.log(this.getClass().getSimpleName(), "=============================");
		}

		backDrop.getViewport().update(width, height, false);

		// gameStage.getViewport().update(stageSize.w / 2, stageSize.h / 2, false);
		gameStage.getViewport().update(width, height, false);

//		Camera cam=gameStage.getCamera();
//		cam.viewportHeight = newHeight;
//		cam.viewportWidth = newWidth;
//		cam.position.set(stageSize.w / 2, stageSize.h / 2, 0);
//		cam.update();

//		hud.getViewport().update(stageSize.w / 2, stageSize.h / 2, false);
		hud.getViewport().update(width, height, false);
//		Camera hudcam=hud.getCamera();
//		hudcam.viewportHeight = newHeight;
//		hudcam.viewportWidth = newWidth;
//		hudcam.position.set(stageSize.w / 2, stageSize.h / 2, 0);
//		hudcam.update();

		stageSafeZone.getViewport().update(width, height, false);
//		stageSafeZone.getViewport().update(stageSize.w / 2, stageSize.h / 2, false);
//		Camera scam=stageSafeZone.getCamera();
//		scam.viewportHeight = newHeight;
//		scam.viewportWidth = newWidth;
//		scam.position.set(stageSize.w / 2, stageSize.h / 2, 0);
//		scam.update();

	}

	@Override
	public void resume() {
		if (isDisposed) {
			return;
		}
		Gdx.app.log(this.getClass().getSimpleName(), "Resume: " + getClass().getSimpleName());
		if (r == null) {
			r = new ShapeRenderer();
		}
	}

	public void setDebug(final boolean debug) {
		this.debug = debug;
	}

	protected void setInputProcessor(final InputProcessor ip) {
		screenInputProcessor = ip;
	}

	public void setShowFPS(final boolean showFPS) {
		this.showFPS = showFPS;
	}

	@Override
	public void show() {
		if (isDisposed) {
			return;
		}
		if (isDebug()) {
			Gdx.app.log(this.getClass().getSimpleName(), "Show: " + getClass().getSimpleName());
		}
		connectInputProcessor();
	}
}
