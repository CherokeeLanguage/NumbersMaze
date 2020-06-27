package com.cherokeelessons.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cherokeelessons.maze.Effect.MusicPauseEvent;
import com.cherokeelessons.maze.Effect.MusicPlayEvent;
import com.cherokeelessons.maze.Effect.MusicStopEvent;
import com.cherokeelessons.maze.Effect.PreloadEvent;
import com.cherokeelessons.maze.NumbersMaze;
import com.cherokeelessons.maze.NumbersMaze.ScreenChangeEvent;
import com.cherokeelessons.maze.NumbersMaze.ScreenList;

public class LoadingScreen extends ScreenBase {

	NumbersMaze app;
	
	public LoadingScreen(NumbersMaze _app) {
		app=_app;
		showOverScan=false;
		pack=new PixmapPacker(512, 512, Format.RGBA8888, 2, true);
		for (int i=0; i<25; i++) {
			pack.pack(i+"", new Pixmap(Gdx.files.internal("libgdx/1080p_"+i+".png")));
		}
		ta = pack.generateTextureAtlas(TextureFilter.Linear, TextureFilter.Linear, false);
		
		int px=0;
		int py=0;
		for (int x=0; x<5; x++) {
			py=0;
			Image i=null;
			for (int y=0; y<5; y++) {
				int z = 4-y;
				int p = z*5+x;
				i = new Image(ta.findRegion(p+""));
				i.setX(px);
				i.setY(py);
				py+=i.getHeight();
				logo.addActor(i);
			}
			if (i!=null) {
				px+=i.getWidth();
			}
		}
		logo.setSize(px, py);
		
		this.backgroundColor=Color.BLACK;
		start=System.currentTimeMillis();
		
		logo.setOrigin(logo.getWidth()/2, logo.getHeight()/2);
		
		float wscale=overscan.width/logo.getWidth();
		float hscale=overscan.height/logo.getHeight();
		if (wscale>hscale) {
			logo.setScale(hscale);
		} else {
			logo.setScale(wscale);
		}
		logo.setX(overscan.x+(overscan.width-logo.getWidth())/2);
		logo.setY(overscan.y+(overscan.height-logo.getHeight())/2);
		hud.addActor(logo);
		
		logo.setColor(1, 1, 1, 0);
		logo.addAction(Actions.fadeIn(2));
		logo.addAction(Actions.delay(4, Actions.fadeOut(1)));
		logo.addAction(Actions.delay(4, Actions.delay(2)));
		
		logo.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				ScreenChangeEvent e = new ScreenChangeEvent();
				e.screen=ScreenList.MainMenu;
				NumbersMaze.post(e);
			}			
		});
		
		PreloadEvent p;
		p = new PreloadEvent();
		p.name="box_moved";
		NumbersMaze.post(p);
		
		p = new PreloadEvent();
		p.name="ding2";
		NumbersMaze.post(p);
		
		p = new PreloadEvent();
		p.name="drop_it";
		NumbersMaze.post(p);
		
		p = new PreloadEvent();
		p.name="explode";
		NumbersMaze.post(p);
		
		p = new PreloadEvent();
		p.name="explodemini";
		NumbersMaze.post(p);
		
		p = new PreloadEvent();
		p.name="level_finished";
		NumbersMaze.post(p);
		
		p = new PreloadEvent();
		p.name="pick_up";
		NumbersMaze.post(p);
		
		p = new PreloadEvent();
		p.name="plink";
		NumbersMaze.post(p);
	}

	long start=0;
	Group logo=new Group();
	private PixmapPacker pack;
	private TextureAtlas ta;
	
	@Override
	public void show() {
		super.show();
		MusicPlayEvent e = new MusicPlayEvent();
		e.name="libGdx/atmoseerie03";
		e.loop=true;
		NumbersMaze.post(e);
	}

	@Override
	public void hide() {
		super.hide();
		MusicPauseEvent e=new MusicPauseEvent();
		e.name="libGdx/atmoseerie03";
		NumbersMaze.post(e);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		MusicStopEvent e = new MusicStopEvent();
		e.name="libGdx/atmoseerie03";
		NumbersMaze.post(e);
		gameStage.clear();
		hud.clear();
		backDrop.clear();
		ta.dispose();
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		
		if (logo.getActions().size==0) {
			ScreenChangeEvent e = new ScreenChangeEvent();	
			e.screen=ScreenList.MainMenu;
			NumbersMaze.post(e);
		}
		if (System.currentTimeMillis()-start>13000) {
			ScreenChangeEvent e = new ScreenChangeEvent();	
			e.screen=ScreenList.MainMenu;
			NumbersMaze.post(e);
		}		
	}
	
	

}
