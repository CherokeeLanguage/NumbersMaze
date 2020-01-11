package com.cherokeelessons.maze;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class AtlasRegionGet {
	private TextureAtlas atlas=null;
	private HashMap<String, AtlasRegion> cache=new HashMap<String, AtlasRegion>();
	public void init(TextureAtlas textureAtlas){
		cache.clear();
		setAtlas(textureAtlas);
	}
	public void setAtlas(TextureAtlas atlas){
		this.atlas=atlas;
	}	
	public AtlasRegion findRegion(String key) {
		if (atlas==null) return null;
		AtlasRegion ar;
		ar = cache.get(key);
		if (ar!=null) return ar;
		ar=atlas.findRegion(key);
		cache.put(key, ar);
		if (ar==null) {
			Gdx.app.log("AtlasRegion", "Unable to find region: "+key);
		}
		return ar;
	}
	public AtlasRegion[] findRegions(String key, int start, int stopAt, int len) {
		if (atlas==null) return null;
		AtlasRegion[] ar=new AtlasRegion[stopAt-start+1];
		for (int ix=start; ix<=stopAt; ix++) {
			String sfx=""+ix;
			while(sfx.length()<len) sfx="0"+sfx;
			ar[ix]=findRegion(key+sfx);
		}
		return ar;
	}
	public void dispose() {
		cache.clear();
		atlas.dispose();
		atlas=null;
	}
}
