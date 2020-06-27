package com.cherokeelessons.maze;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class AtlasRegionGet {
	private TextureAtlas atlas = null;
	private final HashMap<String, AtlasRegion> cache = new HashMap<>();

	public void dispose() {
		cache.clear();
		atlas.dispose();
		atlas = null;
	}

	public AtlasRegion findRegion(final String key) {
		if (atlas == null) {
			return null;
		}
		AtlasRegion ar;
		ar = cache.get(key);
		if (ar != null) {
			return ar;
		}
		ar = atlas.findRegion(key);
		cache.put(key, ar);
		if (ar == null) {
			Gdx.app.log("AtlasRegion", "Unable to find region: " + key);
		}
		return ar;
	}

	public AtlasRegion[] findRegions(final String key, final int start, final int stopAt, final int len) {
		if (atlas == null) {
			return null;
		}
		final AtlasRegion[] ar = new AtlasRegion[stopAt - start + 1];
		for (int ix = start; ix <= stopAt; ix++) {
			String sfx = "" + ix;
			while (sfx.length() < len) {
				sfx = "0" + sfx;
			}
			ar[ix] = findRegion(key + sfx);
		}
		return ar;
	}

	public void init(final TextureAtlas textureAtlas) {
		cache.clear();
		setAtlas(textureAtlas);
	}

	public void setAtlas(final TextureAtlas atlas) {
		this.atlas = atlas;
	}
}
