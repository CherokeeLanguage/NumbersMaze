package com.cherokeelessons.maze.screen;

import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.maze.Effect.MusicPauseEvent;
import com.cherokeelessons.maze.Effect.MusicPlayEvent;
import com.cherokeelessons.maze.Effect.MusicStopEvent;
import com.cherokeelessons.maze.Effect.SoundPlayEvent;
import com.cherokeelessons.maze.NumbersMaze;
import com.cherokeelessons.maze.NumbersMaze.ScreenChangeEvent;
import com.cherokeelessons.maze.NumbersMaze.ScreenList;
import com.cherokeelessons.maze.S;
import com.cherokeelessons.maze.entity.PlayerInput;
import com.cherokeelessons.maze.object.Xbox;

public class MainMenu extends ScreenBase {

	NumbersMaze app;

	final String music_file = "George_Ellinas_-_Pulse_(George_Ellinas_remix)";
	private int menu_item = 0;
	private final PlayerInput menuResponder = new PlayerInput() {
		private PovDirection lastDir = PovDirection.center;

		@Override
		public boolean axisMoved(final Controller controller, final int axisCode, final float value) {
			if (axisCode == Xbox.AXIS_LEFT_Y) {
				if (value <= -.5 && lastDir != PovDirection.north) {
					lastDir = PovDirection.north;
					return povMoved(controller, -5, PovDirection.north);
				}
				if (value >= .5 && lastDir != PovDirection.south) {
					lastDir = PovDirection.south;
					return povMoved(controller, -5, PovDirection.south);
				}
				if (value >= -.5 && value <= .5 && lastDir != PovDirection.center) {
					lastDir = PovDirection.center;
					return povMoved(controller, -5, PovDirection.center);
				}
			}
			return super.axisMoved(controller, axisCode, value);
		}

		@Override
		public boolean buttonDown(final Controller controller, final int buttonCode) {
			if (buttonCode == Xbox.BUTTON_A) {
				doButton.run();
				return true;
			}
			return super.buttonDown(controller, buttonCode);
		}

		@Override
		public boolean keyDown(final int keycode) {
			if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
				menu_item = 0;
				setButton.run();
				return true;
			}
			if (keycode == Keys.CENTER) {
				Controller c;
				final Array<Controller> controllers = Controllers.getControllers();
				if (!controllers.isEmpty()) {
					c = controllers.first();
				} else {
					c = null;
				}
				return buttonDown(c, Xbox.BUTTON_A);
			}
			return super.keyDown(keycode);
		}

		@Override
		public boolean keyUp(final int keycode) {
			if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
				return true;
			}
			if (keycode == Keys.CENTER) {
				Controller c;
				final Array<Controller> controllers = Controllers.getControllers();
				if (!controllers.isEmpty()) {
					c = controllers.first();
				} else {
					c = null;
				}
				return buttonUp(c, Xbox.BUTTON_A);
			}
			return super.keyUp(keycode);
		}

		@Override
		public boolean povMoved(final Controller controller, final int povCode, final PovDirection value) {
			if (value.equals(PovDirection.north)) {
				menu_item--;
			}
			if (value.equals(PovDirection.south)) {
				menu_item++;
			}
			while (menu_item < 0) {
				menu_item += menuItems.size();
			}
			while (menu_item >= menuItems.size()) {
				menu_item -= menuItems.size();
			}
			setButton.run();
			return true;
		}
	};

	final Runnable doButton = new Runnable() {
		@Override
		public void run() {
			final ScreenChangeEvent e = new ScreenChangeEvent();

			// these numbers must match the arraylist indexes the items sit under!
			switch (menu_item) {
			case 0:
				e.screen = ScreenList.LOAD_GAME_SLOT;
				NumbersMaze.post(e);
				break;
			case 1:
				// app.switchTo(ScreenList.OptionsScreen);
				break;
			case 2:
				// app.switchTo(ScreenList.AboutScreen);
				break;
			default:
				break;
			}
		}
	};

	final Runnable setButton = new Runnable() {
		int last_item = -1;

		@Override
		public void run() {
			for (final TextButton menuItem : menuItems) {
				menuItem.setChecked(false);
			}
			menuItems.get(menu_item).setChecked(true);
			if (last_item != menu_item) {
				final SoundPlayEvent e = new SoundPlayEvent();
				e.name = "box_moved";
				NumbersMaze.post(e);
				last_item = menu_item;
			}
		}
	};

	private final ArrayList<TextButton> menuItems = new ArrayList<>();

	NinePatchDrawable checked = null;

	NinePatchDrawable titleBar = null;

	public MainMenu(final NumbersMaze app) {
		super();
		backgroundColor.set(Color.WHITE);
		this.app = app;
		setShowFPS(false);

		final NinePatch checked_9 = new NinePatch(S.getArg().findRegion("Blocks_01_64x64_Alt_04_003x"), 10, 10, 10, 10);
		checked = new NinePatchDrawable(checked_9);

		final NinePatch title_9 = new NinePatch(S.getArg().findRegion("Blocks_01_128x128_Alt_04_002x"), 21, 21, 21, 21);
		titleBar = new NinePatchDrawable(title_9);

		final BitmapFont font = S.getFnt().getFont(65);
		final BitmapFont titleFont = S.getFnt().getFont(80);

		final AtlasRegion bg = S.getArg().findRegion("background-1");
		final Image background = new Image(bg);
		background.setColor(1, 1, 1, .35f);
		background.setFillParent(true);
		background.setScaling(Scaling.fill);
		backDrop.addActor(background);

		final TextButtonStyle mainStyle = new TextButtonStyle(null, null, checked, font);
		mainStyle.checkedFontColor = Color.LIGHT_GRAY;
//		mainStyle.font = font;
		mainStyle.fontColor = Color.DARK_GRAY;

		menuItems.clear();

//		final TextButton newGame = new TextButton("New Game (1 Player)", mainStyle);
//		newGame.addListener(new ClickListener() {
//			int item = menuItems.size();
//
//			@Override
//			public void clicked(final InputEvent event, final float x, final float y) {
//				Gdx.app.log(this.getClass().getSimpleName(), "New Game!");
//				menu_item = item;
//				setButton.run();
//				doButton.run();
//			}
//		});
//		menuItems.add(newGame);

		final TextButton loadGame = new TextButton("Play Game", mainStyle);
		loadGame.addListener(new ClickListener() {
			int item = menuItems.size();

			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				Gdx.app.log(this.getClass().getSimpleName(), "Play Game!");
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		menuItems.add(loadGame);

		final TextButton options = new TextButton("Options", mainStyle);
		options.addListener(new ClickListener() {
			int item = menuItems.size();

			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				Gdx.app.log(this.getClass().getSimpleName(), "Options!");
				menu_item = item;
				setButton.run();
				doButton.run();
			}

		});
		menuItems.add(options);

		final TextButton about = new TextButton("About", mainStyle);
		about.addListener(new ClickListener() {
			int item = menuItems.size();

			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				Gdx.app.log(this.getClass().getSimpleName(), "About!");
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		menuItems.add(about);

		final LabelStyle titleStyle = new LabelStyle();
		titleStyle.background = titleBar;
		titleStyle.font = titleFont;
		titleStyle.fontColor = Color.LIGHT_GRAY;

		final Label title = new Label("Cherokee Numbers Maze", titleStyle);
		title.pack();
		title.setHeight(title.getHeight() * .75f);
		title.setX(overscan.x + (overscan.width - title.getWidth()) / 2);
		title.setY(overscan.y + overscan.height - title.getHeight());

		final float th = title.getHeight();
		float totalH = 0;
		for (final TextButton tb : menuItems) {
			tb.pack();
			tb.setHeight(tb.getHeight() * .7f);
			tb.pad(0);
			tb.padLeft(10);
			tb.padRight(10);
			tb.pack();
			totalH += tb.getHeight();			
		}
		final float gap = (overscan.height - totalH - th) / menuItems.size();

		Collections.reverse(menuItems);
		for (int ix = 0; ix < menuItems.size(); ix++) {
			final TextButton tb = menuItems.get(ix);
			tb.setY(gap / 2 + ix * (gap + tb.getHeight()) + overscan.y);
			tb.setX(overscan.width / 2 - tb.getWidth() / 2 + overscan.x);
		}
		Collections.reverse(menuItems);

		hud.clear();
		hud.addActor(title);
		for (final TextButton menuItem : menuItems) {
			hud.addActor(menuItem);
		}

		final AtlasRegion ar_controls = S.getArg().findRegion("touch_buttons_0");
		final Image controls = new Image(ar_controls);
		controls.setX(0);
		controls.setY(0);

		menu_item = 0;
		setButton.run();

		final InputMultiplexer mux = new InputMultiplexer();
		mux.addProcessor(hud);
		mux.addProcessor(menuResponder);
		setInputProcessor(mux);
	}

	private void background_music(final boolean on) {
		if (on) {
			final MusicPlayEvent e = new MusicPlayEvent();
			e.name = music_file;
			e.loop = true;
			NumbersMaze.post(e);
		} else {
			final MusicPauseEvent e = new MusicPauseEvent();
			e.name = music_file;
			NumbersMaze.post(e);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		menuItems.clear();
		hud.clear();
		backDrop.clear();
		checked.getPatch().getTexture().dispose();
		final MusicStopEvent e = new MusicStopEvent();
		e.name = music_file;
		NumbersMaze.post(e);
	}

	@Override
	public void hide() {
		Gdx.app.log(this.getClass().getSimpleName(), "main menu hide");
		super.hide();
		background_music(false);
		Controllers.removeListener(menuResponder);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void render(final float delta) {
		super.render(delta);
	}

	@Override
	public void resize(final int width, final int height) {
		super.resize(width, height);
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public void show() {
		super.show();
		Controllers.addListener(menuResponder);
		final SoundPlayEvent e = new SoundPlayEvent();
		e.name = "box_moved";
		NumbersMaze.post(e);
		background_music(true);
	}

}
