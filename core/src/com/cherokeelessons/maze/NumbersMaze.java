package com.cherokeelessons.maze;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.cherokeelessons.maze.entity.Arrow;
import com.cherokeelessons.maze.entity.Boom;
import com.cherokeelessons.maze.entity.DeathOrb;
import com.cherokeelessons.maze.entity.PlayerInput;
import com.cherokeelessons.maze.object.DataBundle;
import com.cherokeelessons.maze.screen.LoadingScreen;
import com.cherokeelessons.maze.screen.MainMenu;
import com.cherokeelessons.maze.screen.OnePlayerScreen;
import com.cherokeelessons.maze.screen.Paused;
import com.cherokeelessons.maze.screen.SaveLoadScreen;
import com.cherokeelessons.maze.screen.SaveLoadScreen.SaveLoadMode;
import com.cherokeelessons.maze.screen.SinglePlayerMazeScreen;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;


public class NumbersMaze extends Game {
	private Effect e=null;
	
	public static Preferences getPreferences(){
		return Gdx.app.getPreferences(NumbersMaze.class.getCanonicalName());
	}
	public static Preferences getPreferences(String tag){
		return Gdx.app.getPreferences(NumbersMaze.class.getCanonicalName()+"."+tag);
	}
	final private static EventBus b;
	
	static {
		b = new EventBus("event bus");
	}
	
	private static EventBus getBus(){
		return b;
	}
	public static void post(Object event) {
		getBus().post(event);
	}
	
//	private NumbersMaze app=null;
	public ArrayList<Controller> gamepads=new ArrayList<>();
	public int textureSize;
	private ControllerListener padWatchDog;
	
	@Override
	public void dispose() {
		super.dispose();
//		app=null;
		Controllers.removeListener(padWatchDog);
		S.dispose();
	}

	@Override
	public void create() {
		
		getBus().register(this);
		
		e=new Effect();
		getBus().register(e);
		
		textureSize = getMaxTextureSize();
		S.init();
		S.getArg().init(packTextures(textureSize));
		S.getPar().init(textureSize);
		
		Gdx.input.setCatchKey(Input.Keys.BACK, true);
		Gdx.input.setCatchKey(Input.Keys.MENU, true);
		
		OS.check();
		
		initGamepads();
		
		ScreenChangeEvent e = new ScreenChangeEvent();
		e.screen=ScreenList.Loading;
		getBus().post(e);
		
		Arrow.setRegion(S.getArg().findRegion("arrow1"));
		Boom.setRegion(S.getArg().findRegion("explosion"));
		DeathOrb.setAtlas(S.getArg().findRegions("a_fireball", 0, 63, 3));

		System.out.print("Loading level song list and ");
		FileHandle plist = Gdx.files.getFileHandle("audio/plist-levels.txt", Files.FileType.Internal);
		songs = plist.readString().split("\n");
		for (int ix=0; ix<songs.length; ix++) {
			songs[ix] = songs[ix].replace(".ogg", "");
		}
		plist=null;
		Random x = new Random(0);
		for (int ix=0; ix<songs.length; ix++) {
			int iy=x.nextInt(songs.length);
			String temp=songs[ix];
			songs[ix]=songs[iy];
			songs[iy]=temp;
		}
		System.out.println(songs.length+" songs found.");
	}

	private TextureAtlas packTextures(int packSize) {

		TextureAtlas newAtlas=null;
		
		System.out.println("PACKING TEXTURES");
		final PixmapPacker packer = new PixmapPacker(packSize, packSize,
				Format.RGBA8888, 1, true);

		ArrayList<String> imgList = new ArrayList<>();
		imgList.addAll(Arrays.asList(Gdx.files.internal("720p/plist.txt").readString("UTF-8").split("\n")));
		System.out.println("Read " + imgList.size() + " plist entries.");
		for (int ix = 0; ix < imgList.size(); ix++) {
			String img = imgList.get(ix);
			if (img.trim().length() < 1) {
				continue;
			}
			FileHandle internal = Gdx.files.internal(img);
			Pixmap p = new Pixmap(internal);
			packer.pack(internal.nameWithoutExtension(), p);
			p.dispose();
		}
		newAtlas = packer.generateTextureAtlas(TextureFilter.Linear,
				TextureFilter.Linear, false);
		return newAtlas;
	}

	private int getMaxTextureSize() {
		IntBuffer buf = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buf);
		int size=buf.get();
		if (size>2048) {
			size=2048;
		}
		Gdx.app.log("glinfo", "Runtime texture pack size = " + textureSize);
		return size;
	}
	
	private void initGamepads() {
		padWatchDog=new PlayerInput(){
			@Override
			public void disconnected(Controller controller) {
				for (int ix=0; ix<gamepads.size(); ix++) {
					if (gamepads.get(ix).equals(controller)) {
						gamepads.set(ix, null);
						System.out.println("LOST PLAYER INPUT: "+ix);
						break;
					}
				}
			}
			@Override
			public void connected(Controller controller) {
				for (int ix=0; ix<gamepads.size(); ix++) {
					if (gamepads.get(ix)==null) {
						gamepads.set(ix, controller);
						System.out.println("REPLACED PLAYER INPUT: "+ix);
						return;
					}
					if (gamepads.get(ix).equals(controller)) {
						System.out.println("DUPLICATE PLAYER INPUT: "+ix);
						return;
					}
				}
				System.out.println("NEW PLAYER INPUT: "+gamepads.size());
				gamepads.add(controller);
			}
		};
		Controllers.addListener(padWatchDog);
		
		Array<Controller> pads = Controllers.getControllers();
		for (Controller controller : pads) {
			gamepads.add(controller);
			System.out.println("Found input device: "+controller.getName());
		}
	}

	public String[] songs=new String[0];
	
	public LoadingScreen loadingScreen;
	public MainMenu mainMenu;
	public SinglePlayerMazeScreen singlePlayerMazeScreen;
	public SinglePlayerMazeScreen ultimatePlayerMazeScreen;
	public OnePlayerScreen onePlayer;
	
	public static enum ScreenList {
		MainMenu, SinglePlayerMazeScreen, Loading, OnePlayer, UltimateScreen, Previous, Paused, SaveGame, LoadGame, UltimateOnePlayer;
	}
	
	Array<ScreenList> screenStack=new Array<>();
	
	public ScreenList getActiveScreen(){
		if (screenStack.size<1) {
			return null;
		}
		return screenStack.peek();
	}
	public void popUntilScreenStack(ScreenList until) {
		 while (screenStack.size>0){
			 ScreenList lastScreen = screenStack.pop();
			 if (lastScreen.equals(until)){
				 break;
			 }
		 }
	}
	public ScreenList popScreenStack(ScreenList onlyPopIfThis) {
		 while (screenStack.size>0){
			 if (!screenStack.peek().equals(onlyPopIfThis)){
				 break;
			 }
			 screenStack.pop();
		 }
		 return screenStack.peek();
	}
	Paused paused=null;
	private SaveLoadScreen saveGameScreen=null;
	private SaveLoadScreen loadGameScreen=null;
	private OnePlayerScreen uOnePlayer;
	public static class ScreenChangeEvent {
		public ScreenList screen=ScreenList.MainMenu;
		final public DataBundle data = new DataBundle();
	}
	@Subscribe
	public void switchToHandler(ScreenChangeEvent event) {
		Gdx.app.log("switchToHandler", event.screen.name()+" "+event.data.toString());
		switch (event.screen) {
		case Loading:
			if (loadingScreen==null) {
				loadingScreen=new LoadingScreen(this);
			}
			screenStack.add(ScreenList.Loading);
			setScreen(loadingScreen);
			break;
		case LoadGame:
			if (loadGameScreen!=null) {
				loadGameScreen.dispose();
			}
			loadGameScreen=new SaveLoadScreen(this, SaveLoadMode.Load, event.data);
			setScreen(loadGameScreen);
			screenStack.add(ScreenList.LoadGame);
			break;
		case MainMenu:
			if (mainMenu==null) {
				mainMenu=new MainMenu(this);
			}
			popUntilScreenStack(ScreenList.MainMenu);
			screenStack.add(ScreenList.MainMenu);
			setScreen(mainMenu);
			break;
		case OnePlayer:
			if (!event.data.getBoolean("paused")) {
				if (onePlayer!=null) {
					onePlayer.dispose();
					onePlayer=null;
				}
			}
			if (onePlayer==null) {
				onePlayer = new OnePlayerScreen(this, event.data);
			}
			screenStack.add(ScreenList.OnePlayer);
			setScreen(onePlayer);
			break;
		case Paused:
			if (!getActiveScreen().equals(ScreenList.Paused)) {
				if (paused != null) {
					paused.dispose();
				}
				paused = new Paused(this);
				setScreen(paused);
				screenStack.add(ScreenList.Paused);
			}
			break;
		case Previous:
			popScreenStack();
			popScreenStack(ScreenList.Paused);
			ScreenChangeEvent e = new ScreenChangeEvent();
			e.screen=getActiveScreen();
			if (e.screen==null) {
				e.screen=ScreenList.MainMenu;
			}
			getBus().post(e);
			break;
		case SaveGame:
			if (saveGameScreen!=null) {
				saveGameScreen.dispose();
			}
			saveGameScreen=new SaveLoadScreen(this, SaveLoadMode.Save, event.data);
			setScreen(saveGameScreen);
			screenStack.add(ScreenList.SaveGame);
			break;
		case SinglePlayerMazeScreen:
			if (!event.data.getBoolean("paused")) {
				if (singlePlayerMazeScreen!=null) {
					singlePlayerMazeScreen.dispose();
					singlePlayerMazeScreen=null;
				}
			}
			if (singlePlayerMazeScreen==null) {
				singlePlayerMazeScreen=new SinglePlayerMazeScreen(this, event.data);
			}
			screenStack.add(ScreenList.SinglePlayerMazeScreen);
			setScreen(singlePlayerMazeScreen);
			break;
		case UltimateScreen:
			if (!event.data.getBoolean("paused")) {
				if (uOnePlayer!=null) {
					uOnePlayer.dispose();
					uOnePlayer=null;
				}
			}
			if (uOnePlayer==null) {
//				DataBundle data=new DataBundle();
//				data.putInteger("level", 50);
//				data.putInteger("score", 0);
//				data.putBoolean("ultimate", true);
				uOnePlayer = new OnePlayerScreen(this, event.data);
			}
			screenStack.add(ScreenList.OnePlayer);
			setScreen(uOnePlayer);
			break;
		default:
			break;
		}
	}
	private ScreenList popScreenStack() {
		if (screenStack.size > 0) {
			return screenStack.pop();
		}
		return null;
	}
	
	public static class OS {
		private static void check() {
			String OS = System.getProperty("os.name").toLowerCase();
			isWindows=OS.indexOf("win") >= 0;
			isMac=OS.indexOf("mac") >= 0;
			isUnix=OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0;
			isSolaris=OS.indexOf("sunos") >= 0;
		}
		public static boolean isWindows=false;
		public static boolean isMac=false;
		public static boolean isUnix=false;
		public static boolean isSolaris=false;
		
		public static boolean isOnAndroidTV() {
		    return Gdx.app.getType().equals(Application.ApplicationType.Android) &&
		            !Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen);
		}
	}
	

}


