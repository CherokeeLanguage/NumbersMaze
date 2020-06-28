package com.cherokeelessons.maze.screen;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

public class SaveLoadScreen extends ScreenBase {
	public class GameSlot {
		String name = "";
		int slot = 0;
		int score = 0;
		int level = 0;
		boolean ultimate = false;
		long elapsed = 0;
		long modified = 0;

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("GameSlot [");
			if (name != null) {
				builder.append("name=");
				builder.append(name);
				builder.append(", ");
			}
			builder.append("slot=");
			builder.append(slot);
			builder.append(", score=");
			builder.append(score);
			builder.append(", level=");
			builder.append(level);
			builder.append(", ultimate=");
			builder.append(ultimate);
			builder.append(", elapsed=");
			builder.append(elapsed);
			builder.append(", modified=");
			builder.append(modified);
			builder.append("]");
			return builder.toString();
		}

	}

	public enum SaveLoadMode {
		Save, Load;
	}

//	private final NumbersMaze app;
	private final InputMultiplexer mux;
	private SaveLoadMode slm = SaveLoadMode.Save;
	private final Preferences slots;

	private final ArrayList<TextButton> slotButton;

	private final NinePatch checked_9;
	private final NinePatch unchecked_9;
	private final NinePatchDrawable checked;
	private final NinePatchDrawable unchecked;
	private final BitmapFont slotFont;
	private final TextButtonStyle mainStyle;
	final private DataBundle data = new DataBundle();
	int menu_item = 0;

	final Runnable doButton = new Runnable() {
		@Override
		public void run() {
			final ScreenChangeEvent e = new ScreenChangeEvent();
			final GameSlot gs = loadSlot(menu_item);
			switch (slm) {
			case Load:
				e.data.putInteger("level", gs.level);
				e.data.putInteger("score", gs.score);
				e.data.putBoolean("ultimate", gs.ultimate);
				e.screen = ScreenList.SinglePlayerMazeScreen;
				NumbersMaze.post(e);
				break;
			case Save:
				gs.slot = menu_item;
				gs.level = data.getInteger("level");
				gs.score = data.getInteger("score");
				gs.ultimate = data.getBoolean("ultimate");
				saveSlot(gs);
				e.data.putInteger("level", gs.level);
				e.data.putInteger("score", gs.score);
				e.data.putBoolean("ultimate", gs.ultimate);
				e.screen = ScreenList.SinglePlayerMazeScreen;
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
			for (final TextButton element : slotButton) {
				element.setChecked(false);
			}
			slotButton.get(menu_item).setChecked(true);
			if (last_item != menu_item) {
				final SoundPlayEvent e = new SoundPlayEvent();
				e.name = "box_moved";
				NumbersMaze.post(e);
				last_item = menu_item;
			}
		}
	};

	private final PlayerInput menuResponder = new PlayerInput() {
		private PovDirection lastDirNS = PovDirection.center;
		private PovDirection lastDirEW = PovDirection.center;
		
		@Override
		public boolean axisMoved(final Controller controller, final int axisCode, final float value) {
			if (axisCode == Xbox.AXIS_LEFT_Y) {
				if (value <= -.5 && lastDirNS != PovDirection.north) {
					lastDirNS = PovDirection.north;
					return povMoved(controller, -5, PovDirection.north);
				}
				if (value >= .5 && lastDirNS != PovDirection.south) {
					lastDirNS = PovDirection.south;
					return povMoved(controller, -5, PovDirection.south);
				}
				if (value >= -.5 && value <= .5 && lastDirNS != PovDirection.center) {
					lastDirNS = PovDirection.center;
					return povMoved(controller, -5, PovDirection.center);
				}
			}
			if (axisCode == Xbox.AXIS_LEFT_X) {
				if (value <= -.5 && lastDirEW != PovDirection.west) {
					lastDirEW = PovDirection.west;
					return povMoved(controller, -5, PovDirection.west);
				}
				if (value >= .5 && lastDirEW != PovDirection.east) {
					lastDirEW = PovDirection.east;
					return povMoved(controller, -5, PovDirection.east);
				}
				if (value >= -.5 && value <= .5 && lastDirEW != PovDirection.center) {
					lastDirEW = PovDirection.center;
					return povMoved(controller, -5, PovDirection.center);
				}
			}
			return super.axisMoved(controller, axisCode, value);
		}

		@Override
		public boolean keyDown(final int keycode) {
			if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
				final ScreenChangeEvent e = new ScreenChangeEvent();
				e.data.put(data);
				e.screen = ScreenList.Previous;
				NumbersMaze.post(e);
				return true;
			}
			return super.keyDown(keycode);
		}
		
		@Override
		public boolean buttonDown(Controller controller, int buttonCode) {
			if (buttonCode == Xbox.BUTTON_A) {
				doButton.run();
				return true;
			}
			return super.buttonDown(controller, buttonCode);
		}

		@Override
		public boolean povMoved(final Controller controller, final int povCode, final PovDirection value) {
			if (value.equals(PovDirection.east)) {
				menu_item += 6;
				if (menu_item > 11) {
					menu_item -= 12;
				}
			}
			if (value.equals(PovDirection.west)) {
				menu_item -= 6;
				if (menu_item < 0) {
					menu_item += 12;
				}
			}
			if (value.equals(PovDirection.north)) {
				menu_item--;
			}
			if (value.equals(PovDirection.south)) {
				menu_item++;
			}
			while (menu_item < 0) {
				menu_item += slotButton.size();
			}
			while (menu_item >= slotButton.size()) {
				menu_item -= slotButton.size();
			}
			setButton.run();
			return true;
		}
	};

	public SaveLoadScreen(@SuppressWarnings("unused") final NumbersMaze _app, final SaveLoadMode mode, final DataBundle _data) {
		super();
		data.put(_data);
//		app = _app;
		slm = mode;
		setDebug(true);
		backgroundColor.set(Color.WHITE);
		setBackdrop();

		slots = NumbersMaze.getPreferences("GameSlots");
		slotButton = new ArrayList<>();

		checked_9 = new NinePatch(S.getArg().findRegion("Blocks_01_64x64_Alt_04_003x"), 10, 10, 10, 10);
		checked_9.setPadding(20, 20, 0, 10);
		checked = new NinePatchDrawable(checked_9);

		unchecked_9 = new NinePatch(S.getArg().findRegion("Blocks_01_64x64_Alt_04_003x"), 10, 10, 10, 10);
		unchecked_9.setPadding(20, 20, 0, 10);
		unchecked_9.getColor().a = .3f;
		unchecked = new NinePatchDrawable(unchecked_9);

		slotFont = S.getFnt().getFont(42);

		mainStyle = new TextButtonStyle(unchecked, unchecked, checked, slotFont);
		mainStyle.checkedFontColor = Color.LIGHT_GRAY;
//		mainStyle.font = slotFont;
		mainStyle.fontColor = Color.DARK_GRAY;

		Gdx.app.log(this.getClass().getSimpleName(), "Loading Save Slots");
		final int padding = 6;
		for (int ix = 0; ix < 12; ix++) {
			final GameSlot slot = loadSlot(ix);
			if (slot.level == 0) {
				if (slm.equals(SaveLoadMode.Save)) {
					slot.name = "Empty Save Slot";
				} else {
					slot.name = "Level: 1, Score: 0";
				}
			} else {
				slot.name = "Level: " + slot.level + ", Score: " + slot.score;
			}
			final TextButton btn = new TextButton(slot.name, mainStyle);
			btn.setChecked(false);
			btn.pack();
			btn.setWidth(overscan.getWidth() / 2 - padding * 2);
			btn.setX(ix / 6 * (overscan.getWidth() / 2) + overscan.x + padding);
			btn.setY((5 - ix % 6) * (btn.getHeight() + padding) + overscan.y + padding);
			String x = btn.getText().toString();
			while (!x.isEmpty() && btn.getLabel().getGlyphLayout().width + padding > btn.getWidth()) {
				x = x.substring(0, x.length() - 1);
				btn.setText(x + "...");
			}
			btn.addListener(new ClickListener() {
				int item = slotButton.size();

				@Override
				public void clicked(final InputEvent event, final float x1, final float y) {
					Gdx.app.log(this.getClass().getSimpleName(), "New Game!");
					menu_item = item;
					setButton.run();
					doButton.run();
				}
			});
			slotButton.add(btn);
		}
		for (final TextButton element : slotButton) {
			hud.addActor(element);
		}

		mux = new InputMultiplexer();
		setInputProcessor(mux);
		mux.addProcessor(hud);
		mux.addProcessor(menuResponder);

		menu_item = 0;
		setButton.run();
	}

	@Override
	public void hide() {
		super.hide();
		Controllers.removeListener(menuResponder);
	}

	private GameSlot loadSlot(final int ix) {
		final GameSlot gs = new GameSlot();
		gs.elapsed = slots.getLong(ix + "-elapsed", 0l);
		gs.level = slots.getInteger(ix + "-level", 0);
		gs.modified = slots.getLong(ix + "-modified", 0);
		gs.name = slots.getString(ix + "-name", "");
		gs.score = slots.getInteger(ix + "-score", 0);
		gs.ultimate = slots.getBoolean(ix + "-ultimate", false);
		gs.slot = ix;
		return gs;
	}

	private GameSlot saveSlot(final GameSlot gs) {
		final int ix = gs.slot;
		gs.modified = System.currentTimeMillis();
		slots.putLong(ix + "-elapsed", gs.elapsed);
		slots.putInteger(ix + "-level", gs.level);
		slots.putLong(ix + "-modified", gs.modified);
		slots.putString(ix + "-name", gs.name);
		slots.putInteger(ix + "-score", gs.score);
		slots.putBoolean(ix + "-ultimate", gs.ultimate);
		slots.flush();
		Gdx.app.log(this.getClass().getSimpleName(), "Saved Game: " + gs.toString());
		return gs;
	}

	private void setBackdrop() {
		final AtlasRegion bg = S.getArg().findRegion("background-1");
		final Image background = new Image(bg);
		background.setColor(1, 1, 1, .35f);
		background.setFillParent(true);
		background.setScaling(Scaling.fill);
		backDrop.addActor(background);
	}

	@Override
	public void show() {
		super.show();
		Controllers.addListener(menuResponder);
	}

}
