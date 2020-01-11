package com.cherokeelessons.maze;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class Font {
	public enum Face {
		CherokeeHandone, Digohweli;
	}
	public Font(){
		this(Face.Digohweli);
	}
	public Font(Face face) {
		FileHandle ttfFile;
		fontCache.clear();
		switch (face) {
		case Digohweli:
			ttfFile = Gdx.files.internal("fonts/Digohweli_1_7.ttf");
			break;
		case CherokeeHandone:
		default:
			ttfFile = Gdx.files.internal("fonts/CherokeeHandone.ttf");
			break;
		}

		ttfgen = new FreeTypeFontGenerator(ttfFile);
		StringBuilder c=new StringBuilder();
		c.append(FreeTypeFontGenerator.DEFAULT_CHARS);
		for (char chr = 'Ꭰ'; chr<= 'Ᏼ'; chr++) {
			c.append(chr);
		}
		myCharSet=c.toString();
	}
	private String myCharSet;
	private FreeTypeFontGenerator ttfgen;
	public HashMap<String, BitmapFont> fontCache = new HashMap<>();
	public BitmapFont getFont(int size) {
		return getFont(size, false);
	}
	public BitmapFont getFont(int size, boolean fixedNumbers) {
		String fontKey=size+"|"+fixedNumbers;
		BitmapFont font=fontCache.get(fontKey);
		if (font!=null) {
			return font;
		}
		FreeTypeFontParameter parameter=new FreeTypeFontParameter();
		parameter.size=size;
		parameter.characters=myCharSet;
		parameter.incremental=false;
		font = ttfgen.generateFont(parameter);
		//font = ttfgen.generateFont(size, myCharSet, false);
		if (fixedNumbers) {
			font.setFixedWidthGlyphs("0123456789");
		}
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		fontCache.put(fontKey, font);
		return font;
	}
	public void dispose() {
		for(BitmapFont f: fontCache.values()) {
			f.dispose();
		}
		fontCache.clear();
		ttfgen.dispose();
		myCharSet=null;
	}
}