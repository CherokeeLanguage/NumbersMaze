package com.cherokeelessons.maze.screen;

import java.util.ArrayList;

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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.maze.Effect.SoundPlayEvent;
import com.cherokeelessons.maze.NumbersMaze;
import com.cherokeelessons.maze.NumbersMaze.ScreenChangeEvent;
import com.cherokeelessons.maze.NumbersMaze.ScreenList;
import com.cherokeelessons.maze.S;
import com.cherokeelessons.maze.entity.PlayerInput;
import com.cherokeelessons.maze.object.DataBundle;
import com.cherokeelessons.maze.object.Xbox;

public class OnePlayerScreen extends ScreenBase {
	NumbersMaze app;

	final private String startupMessage = "" + " * Explode dice in combinations to equal the challenge number.\n"
			+ " * Early levels will show a hint at the bottom left, later levels will not.\n"
			+ " * Each explosion combination must only be caused by a single shot.\n"
			+ " * The more dice in an explosion sequence, the more points you get.\n"
			+ " * If you can't make a combination for the challenge, collect dice\n"
			+ "   into a pile and shoot them to cause new combinations to appear.\n"
			+ " * After completing the challenges for each level, a 'gateway' will\n"
			+ "   appear to go to the next level.\n" //
			+ "\n" //
			+ " Use left joystick or dpad to move around.\n" //
			+ " Use [X] to pick up and drop boxes.\n" //
			+ " Use [A] to fire a bolt to trigger an explosion.\n" //
			+ "\n" //
			+ " Press [A] when ready.";
	private Label msgBox = null;
	private LabelStyle msgBoxStyle = null;
	private NinePatchDrawable npd = null;
	private NinePatch np = null;
	private BitmapFont msgFont = null;
	private AtlasRegion ar = null;

	private final Group betweenOptions = new Group();

	private NinePatch checked_9;
	private NinePatchDrawable checked;

	private TextButtonStyle mainStyle;

	private BitmapFont menufont;

	final private DataBundle data = new DataBundle();

	private PlayerInput pi = new PlayerInput() {
		private PovDirection lastDir = PovDirection.center;
		boolean was_on_exit = false;

		@Override
		public boolean axisMoved(final Controller controller, final int axisCode, final float value) {
			if (axisCode == Xbox.AXIS_LEFT_X) {
				if (value <= -.5 && lastDir != PovDirection.west) {
					lastDir = PovDirection.west;
					return povMoved(controller, -5, PovDirection.west);
				}
				if (value >= .5 && lastDir != PovDirection.east) {
					lastDir = PovDirection.east;
					return povMoved(controller, -5, PovDirection.east);
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
			System.out.println("OnePlayerScreen.java#buttonDown: "+buttonCode);
			if (buttonCode == Xbox.BUTTON_A) {
				doButton.run();
				return true;
			}
			return super.buttonDown(controller, buttonCode);
		}

		@Override
		public boolean keyDown(final int keycode) {
			if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
				was_on_exit = menu_item == exitIndex;
				menu_item = exitIndex;
				setButton.run();
				return true;
			}
//			if (keycode == Keys.CENTER) {
//				Controller c;
//				final Array<Controller> controllers = Controllers.getControllers();
//				if (!controllers.isEmpty()) {
//					c = controllers.first();
//				} else {
//					c = null;
//				}
//				return buttonDown(c, Xbox.BUTTON_A);
//			}
			return super.keyDown(keycode);
		}

		@Override
		public boolean keyUp(final int keycode) {
			if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
				if (was_on_exit) {
					doButton.run();
				}
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
			if (value.equals(PovDirection.west)) {
				menu_item--;
			}
			if (value.equals(PovDirection.east)) {
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
	private int menu_item = 0;

	private final ArrayList<TextButton> menuItems = new ArrayList<>();

	private int exitIndex = 0;
	final Runnable doButton = new Runnable() {
		@Override
		public void run() {
			final ScreenChangeEvent e = new ScreenChangeEvent();
			// these numbers must match the arraylist indexes the items sit under!
			switch (menu_item) {
			case 0:
				e.screen = ScreenList.RESUME_GAME;
				e.data.put(data);
				NumbersMaze.post(e);
				break;
			case 1:
				e.screen = ScreenList.SAVE_GAME;
				e.data.put(data);
				NumbersMaze.post(e);
				break;
			case 2:
				e.screen = ScreenList.MAIN_MENU;
				NumbersMaze.post(e);
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
			if (menuItems.size() == 0) {
				return;
			}
			for (final TextButton menuItem : menuItems) {
				menuItem.setChecked(false);
			}
			if (menu_item >= menuItems.size()) {
				menu_item = menuItems.size() - 1;
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

	InputMultiplexer mux;

	public OnePlayerScreen(final NumbersMaze _app, final DataBundle _data) {
		super();
		app = _app;
		data.put(_data.get());
		backgroundColor.set(Color.WHITE);
		setBackdrop();
		mux = new InputMultiplexer();
		setInputProcessor(mux);
	}

	private void createFirstLevelMsgBox() {
		ar = S.getArg().findRegion("Blocks_01_128x128_Alt_04_003x");
		np = new NinePatch(ar, 21, 21, 21, 21);
		np.setPadding(11, 21, 21, 21);
		np.setColor(Color.LIGHT_GRAY);
		npd = new NinePatchDrawable(np);
		msgBoxStyle = new LabelStyle();
		msgBoxStyle.background = npd;
		msgFont = S.getFnt().getFont(28);
		msgBoxStyle.font = msgFont;
		msgBoxStyle.fontColor = new Color(Color.LIGHT_GRAY);

		msgBox = new Label(startupMessage, msgBoxStyle);
		msgBox.pack();
		final float w = msgBox.getWidth();
		final float h = msgBox.getHeight();
		msgBox.setX((hud.getWidth() - w) / 2);
		msgBox.setY((hud.getHeight() - h) / 2);
		msgBox.setTouchable(Touchable.enabled);
		msgBox.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				final ScreenChangeEvent e = new ScreenChangeEvent();
				e.data.put(data);
				e.screen = ScreenList.RESUME_GAME;
				NumbersMaze.post(e);
			}
		});
//		pi = new PlayerInput() {
//			@Override
//			public boolean buttonUp(final Controller controller, final int buttonCode) {
//				if (buttonCode == Xbox.BUTTON_A) {
//					final ScreenChangeEvent e = new ScreenChangeEvent();
//					e.data.put(data);
//					e.screen = ScreenList.SinglePlayerMazeScreen;
//					NumbersMaze.post(e);
//					return true;
//				}
//				return super.buttonUp(controller, buttonCode);
//			}
//
//		};
	}

	private void createLevelCompleteMsgBox() {
		menuItems.clear();
		final String lvlUpMessage = "Level Completed: " + (data.getInteger("level") - 1) + "\n" + "Score: "
				+ data.getInteger("score");
		ar = S.getArg().findRegion("Blocks_01_128x128_Alt_04_003x");
		np = new NinePatch(ar, 21, 21, 21, 21);
		np.setPadding(11, 21, 21, 21);
		np.setColor(Color.LIGHT_GRAY);
		np.getColor().a = .7f;
		npd = new NinePatchDrawable(np);
		msgBoxStyle = new LabelStyle();
		msgBoxStyle.background = npd;
		msgFont = S.getFnt().getFont(72);
		msgBoxStyle.font = msgFont;
		msgBoxStyle.fontColor = new Color(Color.LIGHT_GRAY);

		msgBox = new Label(lvlUpMessage, msgBoxStyle);
		msgBox.pack();
		final float w = msgBox.getWidth();
		final float h = msgBox.getHeight();
		msgBox.setX((hud.getWidth() - w) / 2);
		msgBox.setY((hud.getHeight() - h) / 2);
		msgBox.setTouchable(Touchable.disabled);

		checked_9 = new NinePatch(S.getArg().findRegion("Blocks_01_64x64_Alt_04_003x"), 10, 10, 10, 10);
		checked_9.setPadding(10, 10, 10, 10);
		checked = new NinePatchDrawable(checked_9);

		menufont = S.getFnt().getFont(60);
		mainStyle = new TextButtonStyle(null, null, checked, menufont);
		mainStyle.checkedFontColor = Color.LIGHT_GRAY;
//		mainStyle.font = menufont;
		mainStyle.fontColor = Color.DARK_GRAY;

		final TextButton btn_continue = new TextButton("Continue", mainStyle);
		btn_continue.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				Gdx.app.log(this.getClass().getSimpleName(), "Continue");
				final int item = menuItems.indexOf(btn_continue);
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		final TextButton btn_save = new TextButton("Save Progress", mainStyle);
		btn_save.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				Gdx.app.log(this.getClass().getSimpleName(), "Save");
				final int item = menuItems.indexOf(btn_save);
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		final TextButton btn_quit = new TextButton("Quit", mainStyle);
		btn_quit.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				Gdx.app.log(this.getClass().getSimpleName(), "Quit");
				final int item = menuItems.indexOf(btn_quit);
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		final float btn_y = overscan.y;
		btn_continue.pack();
		btn_continue.setX(overscan.x);
		btn_continue.setY(btn_y);
		btn_save.pack();
		btn_save.setX(overscan.x + (overscan.width - btn_save.getWidth()) / 2);
		btn_save.setY(btn_y);
		btn_quit.pack();
		btn_quit.setX(overscan.x + overscan.width - btn_quit.getWidth());
		btn_quit.setY(btn_y);

		betweenOptions.clear();
		betweenOptions.addActor(msgBox);
		betweenOptions.addActor(btn_continue);
		betweenOptions.addActor(btn_save);
		betweenOptions.addActor(btn_quit);

		menuItems.add(btn_continue);
		menuItems.add(btn_save);
		menuItems.add(btn_quit);
		exitIndex = menuItems.indexOf(btn_quit);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void hide() {
		super.hide();
		if (pi != null) {
			Controllers.removeListener(pi);
		}
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
	public void resume() {
		super.resume();
	}

	private void setBackdrop() {
		final AtlasRegion bg = S.getArg().findRegion("background-1");
		final Image background = new Image(bg);
		background.setColor(1, 1, 1, .35f);
		background.setFillParent(true);
		background.setScaling(Scaling.fill);
		backDrop.addActor(background);
	}

	public void setData(final DataBundle data) {
		this.data.clear();
		this.data.put(data.get());
	}

	@Override
	public void show() {
		super.show();
		hud.clear();
		final int level = data.getInteger("level");
		if (level == 1) {
			createFirstLevelMsgBox();
			hud.addActor(msgBox);
		}
		if (level > 1) {
			createLevelCompleteMsgBox();
			hud.addActor(betweenOptions);
			setButton.run();
		}
		mux.clear();
		mux.addProcessor(hud);
		if (pi != null) {
			Controllers.addListener(pi);
			mux.addProcessor(pi);
		}
	}
}
