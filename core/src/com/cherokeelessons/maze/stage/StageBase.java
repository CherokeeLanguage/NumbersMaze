package com.cherokeelessons.maze.stage;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public class StageBase extends Stage {

	public StageBase() {
		super();
	}

	public StageBase(Viewport viewport, Batch batch) {
		super(viewport, batch);
	}

	public StageBase(Viewport viewport) {
		super(viewport);
	}

	@Override
	public boolean keyUp(int keyCode) {
		return super.keyUp(keyCode);
	}
	
	@Override
	public boolean keyDown(int keyCode) {
		return super.keyDown(keyCode);
	}
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		boolean result=super.touchUp(screenX, screenY, pointer, button);
		return result;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		boolean result=super.touchDragged(screenX, screenY, pointer);
		return result;
	}
	
}
