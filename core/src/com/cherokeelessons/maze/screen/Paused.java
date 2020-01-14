package com.cherokeelessons.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cherokeelessons.maze.NumbersMaze;
import com.cherokeelessons.maze.NumbersMaze.ScreenChangeEvent;
import com.cherokeelessons.maze.NumbersMaze.ScreenList;
import com.cherokeelessons.maze.S;

public class Paused extends ScreenBase {
	
	
	private final class ExitScreenClick extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
//			super.clicked(event, x, y);
			Gdx.app.log(this.getClass().getSimpleName(),"EVENT: "+event.getType().name());
			ScreenChangeEvent e = new ScreenChangeEvent();
			e.screen=ScreenList.Previous;
			NumbersMaze.post(e);
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	private String msg = "Tap here to resume.";
	private TextButton displayText;
	private TextButtonStyle displayStyle;
	private int fontSize = 48;
	private Group underlay=new Group();
	private Pixmap screenShot;
	
	NumbersMaze game=null;
	private float scaleX;
	private float scaleY;
	private NinePatchDrawable checked;
	public Paused(NumbersMaze game) {
		super();
		this.game=game;
		
		Pixmap pm = getStageshot();
		screenShot = new Pixmap(pm.getWidth(), pm.getHeight(), Pixmap.Format.RGBA8888);
		screenShot.drawPixmap(pm, 0, 0);
		pm.dispose();
		
		NinePatch checked_9 = new NinePatch(new Texture(Gdx.files.internal("9patch/Blocks_01_64x64_Alt_04_003x.png")), 12, 12, 12, 12);
		checked_9.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		checked = new NinePatchDrawable(checked_9);
		
		displayStyle = new TextButtonStyle(checked, checked, checked, S.getFnt().getFont(fontSize));
		displayStyle.checkedFontColor = Color.LIGHT_GRAY;
//		displayStyle.font = S.getFnt().getFont(fontSize);
		displayStyle.fontColor = Color.DARK_GRAY;
		
		displayText = new TextButton(msg, displayStyle);
		displayText.setTouchable(Touchable.enabled);
		
		displayText.addListener(new ExitScreenClick());

		gameStage.addActor(underlay);
		hud.addActor(displayText);
		
		backgroundColor.set(Color.DARK_GRAY);
	}
	
	private void updateMessage(){
		displayText.setStyle(displayStyle);
		displayText.setText(msg);
		displayText.pack();
		displayText.setX((gameStage.getWidth()-displayText.getWidth())/2);
		displayText.setY((gameStage.getHeight()-displayText.getHeight())/2);
	}
	
	
	
	@Override
	public void dispose() {
		super.dispose();
		screenShot.dispose();
	}

	private void updateBackground() {
		int slice=64;
		int w = screenShot.getWidth();
		int h = screenShot.getHeight();
		
		underlay.clear();
		underlay.setTransform(true);
		underlay.setOrigin(w/2, h/2);
		if (scaleX>scaleY) {
			underlay.setScale(scaleX);
		} else {
			underlay.setScale(scaleY);
		}
		underlay.setPosition((stageSize.w-w)/2, (stageSize.h-h)/2);
		underlay.getColor().a=.25f;
		for (int x=0; x<w; x+=slice){
			for (int y=0; y<h; y+=slice){
				int pw=w-x;
				int ph=h-y;
				if (pw>slice) {
					pw=slice;
				}
				if (ph>slice) {
					ph=slice;
				}
				Pixmap pSlice = new Pixmap(pw, ph, Format.RGBA8888);
				pSlice.setColor(Color.BLACK);
				pSlice.fill();
				pSlice.drawPixmap(screenShot, 0, 0, x, y, pw, ph);
				Texture t = new Texture(pSlice);
				t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
				TextureRegion r = new TextureRegion(t);
				Image i = new Image(r);
				i.pack();
				i.setOrigin(i.getWidth()/2, i.getHeight()/2);
				i.setScaleY(-1f);
				i.setScaleX(1f);
				i.layout();
				i.setX(x);
				i.setY(y);
				underlay.addActor(i);
				pSlice.dispose();
			}
		}
	}
	
	@Override
	public void resume() {
		super.resume();
		updateBackground();
		updateMessage();
	}

	@Override
	public void show() {
		super.show();
		updateBackground();
		updateMessage();
	}
	
	private Pixmap getStageshot() {
		Pixmap pixmap;
		byte[] lines;
		
		int width=Gdx.graphics.getWidth();
		int height=Gdx.graphics.getHeight();
		lines = ScreenUtils.getFrameBufferPixels(0,
				0, width, height, false);
		pixmap = new Pixmap(width, height, Format.RGBA8888);
		pixmap.getPixels().clear();
		pixmap.getPixels().put(lines);
		Gdx.app.log(this.getClass().getSimpleName(),"getStageshot size: "+pixmap.getWidth()+"x"+pixmap.getHeight());
		Gdx.app.log(this.getClass().getSimpleName(),"viewport size: "+gameStage.getWidth()+"x"+gameStage.getHeight());
		scaleX=gameStage.getWidth()/pixmap.getWidth();
		scaleY=gameStage.getHeight()/pixmap.getHeight();
		return pixmap;
	}
}
