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
	public TextureAtlas playerAtlas = null;

	public void dispose() {
		playerAtlas.dispose();
		playerAtlas = null;
	}

	public void init(final int packSize) {
		Gdx.app.log(this.getClass().getSimpleName(), "PACKING PLAYER TEXTURES");
		final PixmapPacker packer = new PixmapPacker(packSize, packSize, Format.RGBA8888, 1, true);

		final String plist_name = "player-64px/plist.txt";
		final FileHandle plist = Gdx.files.internal(plist_name);
		final String list = plist.readString();
		final ArrayList<String> imgList = new ArrayList<>();
		imgList.addAll(Arrays.asList(list.split("\n")));
		Gdx.app.log(this.getClass().getSimpleName(), "Read player " + imgList.size() + " plist entries.");
		for (final String img : imgList) {
			if (img.trim().length() < 1) {
				continue;
			}
			final File f = new File(img);
			final Pixmap p = new Pixmap(Gdx.files.internal(img));
			packer.pack(f.getName().replace(".png", ""), p);
			p.dispose();
		}
		final TextureAtlas atlas = packer.generateTextureAtlas(TextureFilter.Linear, TextureFilter.Linear, false);
		playerAtlas = atlas;
	}
}
