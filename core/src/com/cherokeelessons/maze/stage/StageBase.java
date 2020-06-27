package com.cherokeelessons.maze.stage;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public class StageBase extends Stage {

	public StageBase() {
		super();
	}

	public StageBase(final Viewport viewport) {
		super(viewport);
	}

	public StageBase(final Viewport viewport, final Batch batch) {
		super(viewport, batch);
	}

	@Override
	public boolean keyDown(final int keyCode) {
		return super.keyDown(keyCode);
	}

	@Override
	public boolean keyUp(final int keyCode) {
		return super.keyUp(keyCode);
	}

	@Override
	public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
		final boolean result = super.touchDragged(screenX, screenY, pointer);
		return result;
	}

	@Override
	public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
		final boolean result = super.touchUp(screenX, screenY, pointer, button);
		return result;
	}

}
