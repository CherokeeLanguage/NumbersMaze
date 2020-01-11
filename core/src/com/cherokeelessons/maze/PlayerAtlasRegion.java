package com.cherokeelessons.maze;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class PlayerAtlasRegion {
	public TextureAtlas playerAtlas=null;
	public void init(int packSize) {
		System.out.println("PACKING PLAYER TEXTURES");
		final PixmapPacker packer = new PixmapPacker(packSize, packSize,
				Format.RGBA8888, 1, true);

		String plist_name = ("player-64px/plist.txt");
		FileHandle plist = Gdx.files.internal(plist_name);
		String list = plist.readString();
		ArrayList<String> imgList = new ArrayList<String>();
		imgList.addAll(Arrays.asList(list.split("\n")));
		System.out.println("Read player " + imgList.size() + " plist entries.");
		for (int ix = 0; ix < imgList.size(); ix++) {
			String img = imgList.get(ix);
			if (img.trim().length() < 1)
				continue;
			File f = new File(img);
			Pixmap p = new Pixmap(Gdx.files.internal(img));
			packer.pack(f.getName().replace(".png", ""), p);
			p.dispose();
		}
		TextureAtlas atlas = packer.generateTextureAtlas(TextureFilter.Linear,
				TextureFilter.Linear, false);
		playerAtlas=atlas;
	}
	public void dispose(){
		playerAtlas.dispose();
		playerAtlas=null;
	}
}
