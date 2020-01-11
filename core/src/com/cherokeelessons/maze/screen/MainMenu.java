package com.cherokeelessons.maze.screen;

import java.util.ArrayList;
import java.util.Collections;

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

	public MainMenu(NumbersMaze app) {
		super();
		backgroundColor.set(Color.WHITE);
		this.app = app;
		setShowFPS(false);
		
		NinePatch checked_9 = new NinePatch(S.getArg().findRegion("Blocks_01_64x64_Alt_04_003x"), 10, 10, 10, 10);
		checked = new NinePatchDrawable(checked_9);
		
		NinePatch title_9 = new NinePatch(S.getArg().findRegion("Blocks_01_128x128_Alt_04_002x"), 21, 21, 21, 21);
		titleBar = new NinePatchDrawable(title_9);
		
		BitmapFont font = S.getFnt().getFont(65);
		BitmapFont titleFont = S.getFnt().getFont(80);

		AtlasRegion bg = S.getArg().findRegion("background-1");
		Image background = new Image(bg);
		background.setColor(1, 1, 1, .35f);
		background.setFillParent(true);
		background.setScaling(Scaling.fill);
		backDrop.addActor(background);

		TextButtonStyle mainStyle = new TextButtonStyle(null, null, checked, font);
		mainStyle.checkedFontColor = Color.LIGHT_GRAY;
//		mainStyle.font = font;
		mainStyle.fontColor = Color.DARK_GRAY;
		
		menuItems.clear();
		
		TextButton newGame = new TextButton("New Game (1 Player)", mainStyle);
		newGame.addListener(new ClickListener() {
			int item=menuItems.size();
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.out.println("New Game!");
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		menuItems.add(newGame);
		
		
		TextButton loadGame = new TextButton("Load Game", mainStyle);
		loadGame.addListener(new ClickListener() {
			int item=menuItems.size();
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.out.println("Load Game!");
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		menuItems.add(loadGame);
		
		TextButton ultimateChallenge = new TextButton("Ultimate Challenge!", mainStyle);
		ultimateChallenge.addListener(new ClickListener() {
			int item=menuItems.size();
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.out.println("Ultimate Challenge!");
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		menuItems.add(ultimateChallenge);
		
		TextButton options = new TextButton("Options", mainStyle);
		options.addListener(new ClickListener() {
			int item=menuItems.size();
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.out.println("Options!");
				menu_item = item;
				setButton.run();
				doButton.run();
			}

		});
		menuItems.add(options);
		
		TextButton about = new TextButton("About", mainStyle);
		about.addListener(new ClickListener() {
			int item=menuItems.size();
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.out.println("About!");
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		menuItems.add(about);

		TextButton exit = new TextButton("Exit", mainStyle);
		exit.addListener(new ClickListener() {
			int item=menuItems.size();
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.out.println("Exit!");
				menu_item = item;
				setButton.run();
				doButton.run();
			}
		});
		menuItems.add(exit);
		exitIndex=menuItems.size()-1;
		
		LabelStyle titleStyle=new LabelStyle();
		titleStyle.background=titleBar;
		titleStyle.font=titleFont;
		titleStyle.fontColor=Color.LIGHT_GRAY;
		
		Label title = new Label("Cherokee Numbers Maze", titleStyle);
		title.setHeight(title.getHeight()*.75f);
		title.setY(overscan.y+overscan.height-title.getHeight());
		title.setX(overscan.x+(overscan.width-title.getWidth())/2);
		
		
		float th=title.getHeight();
		float totalH=0;
		for (int ix=0; ix<menuItems.size(); ix++) {
			TextButton tb = menuItems.get(ix);
			tb.setHeight(tb.getHeight()*.7f);
			tb.padBottom(22);
			totalH+=tb.getHeight();
		}
		float gap = (overscan.height-totalH-th)/menuItems.size();
		
		Collections.reverse(menuItems);
		for (int ix=0; ix<menuItems.size(); ix++) {
			TextButton tb = menuItems.get(ix);
			tb.setY(gap/2+ix*(gap+tb.getHeight())+overscan.y);
			tb.setX(overscan.width/2-tb.getWidth()/2+overscan.x);
		}
		Collections.reverse(menuItems);

		hud.clear();
		hud.addActor(title);
		for (int ix=0; ix<menuItems.size(); ix++) {
			hud.addActor(menuItems.get(ix));
		}
		
		AtlasRegion ar_controls = S.getArg().findRegion("touch_buttons_0");
		Image controls = new Image(ar_controls);
		controls.setX(0);
		controls.setY(0);

		menu_item=0;
		setButton.run();
		
		InputMultiplexer mux=new InputMultiplexer();
		mux.addProcessor(hud);
		mux.addProcessor(menuResponder);
		setInputProcessor(mux);
	}
	final String music_file = "George_Ellinas_-_Pulse_(George_Ellinas_remix)";
	@Override
	public void render(float delta) {
		super.render(delta);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	private int menu_item = 0;

	private PlayerInput menuResponder = new PlayerInput() {
		private PovDirection lastDir=PovDirection.center;
		@Override
		public boolean axisMoved(Controller controller, int axisCode,
				float value) {
			if (axisCode == Xbox.AXIS_LEFT_Y) {
				if (value <= -.5 && lastDir!=PovDirection.north){
					lastDir=PovDirection.north;
					return povMoved(controller, -5, PovDirection.north);
				}
				if (value >= .5 && lastDir!=PovDirection.south){
					lastDir=PovDirection.south;
					return povMoved(controller, -5, PovDirection.south);
				}
				if (value >= -.5 && value <= .5 && lastDir!=PovDirection.center){
					lastDir=PovDirection.center;
					return povMoved(controller, -5, PovDirection.center);
				}
			}
			return super.axisMoved(controller, axisCode, value);
		}
		@Override
		public boolean povMoved(Controller controller, int povCode,
				PovDirection value) {
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
		@Override
		public boolean buttonUp(Controller controller, int buttonCode) {
			if (buttonCode==Xbox.BUTTON_A) {
				doButton.run();
				return true;
			}
			return super.buttonUp(controller, buttonCode);
		}
		boolean was_on_exit=false;
		@Override
		public boolean keyDown(int keycode) {
			if (keycode==Keys.BACK || keycode==Keys.ESCAPE) {
				was_on_exit=menu_item==exitIndex;
				menu_item=exitIndex;
				setButton.run();
				return true;
			}
			return super.keyDown(keycode);
		}
		@Override
		public boolean keyUp(int keycode) {
			if (keycode==Keys.BACK || keycode==Keys.ESCAPE) {
				if (was_on_exit) {
					doButton.run();
				}
				return true;
			}
			return super.keyUp(keycode);
		}
	};

	final Runnable doButton = new Runnable() {
		@Override
		public void run() {
			ScreenChangeEvent e = new ScreenChangeEvent();
			
			//these numbers must match the arraylist indexes the items sit under!
			switch (menu_item) {
			case 0:
				e.data.putInteger("level", 1);
				e.data.putInteger("score", 0);
				e.data.putBoolean("ultimate", false);
				e.screen=ScreenList.OnePlayer;
				NumbersMaze.post(e);
				break;
			case 1:
				e.screen=ScreenList.LoadGame;
				NumbersMaze.post(e);
				break;
			case 2:
				e.data.putInteger("level", 50);
				e.data.putInteger("score", 0);
				e.data.putBoolean("ultimate", true);
				e.screen=ScreenList.OnePlayer;
				NumbersMaze.post(e);
				break;
			case 3:
				//app.switchTo(ScreenList.OptionsScreen);
				break;
			case 4:
				//app.switchTo(ScreenList.AboutScreen);
				break;
			case 5:
				e.screen=ScreenList.Exit;
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
			for (int ix = 0; ix < menuItems.size(); ix++) {
				menuItems.get(ix).setChecked(false);
			}
			menuItems.get(menu_item).setChecked(true);
			if (last_item != menu_item) {
				SoundPlayEvent e = new SoundPlayEvent();
				e.name="box_moved";
				NumbersMaze.post(e);
				last_item = menu_item;
			}
		}
	};

	private ArrayList<TextButton> menuItems = new ArrayList<>();

	private void background_music(boolean on) {
		if (on) {
			MusicPlayEvent e = new MusicPlayEvent();
			e.name=music_file;
			e.loop=true;
			NumbersMaze.post(e);
		} else {
			MusicPauseEvent e = new MusicPauseEvent();
			e.name=music_file;
			NumbersMaze.post(e);
		}
	}
	
	NinePatchDrawable checked = null;
	NinePatchDrawable titleBar = null;

	private int exitIndex;

	@Override
	public void show() {
		super.show();
		Controllers.addListener(menuResponder);
		SoundPlayEvent e = new SoundPlayEvent();
		e.name="box_moved";
		NumbersMaze.post(e);
		background_music(true);
	}

	@Override
	public void hide() {
		System.out.println("main menu hide");
		super.hide();
		background_music(false);
		Controllers.removeListener(menuResponder);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public void dispose() {
		super.dispose();
		menuItems.clear();
		hud.clear();
		backDrop.clear();
		checked.getPatch().getTexture().dispose();
		MusicStopEvent e = new MusicStopEvent();
		e.name=music_file;
		NumbersMaze.post(e);
	}
	
	
}
