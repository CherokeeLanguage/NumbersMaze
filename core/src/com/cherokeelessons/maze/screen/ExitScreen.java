package com.cherokeelessons.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

public class ExitScreen extends ScreenBase {

	@Override
	public void show() {
		super.show();
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		Gdx.app.exit();
	}

	public ExitScreen() {
		showOverScan = false;
		// add a controller listener to prevent "index out of bounds" error on
		// exit
		// when a gamepad button state changes from down to up during exit
		// process
		Controllers.addListener(new ControllerListener() {

			@Override
			public boolean ySliderMoved(Controller controller, int sliderCode,
					boolean value) {
				return false;
			}

			@Override
			public boolean xSliderMoved(Controller controller, int sliderCode,
					boolean value) {
				return false;
			}

			@Override
			public boolean povMoved(Controller controller, int povCode,
					PovDirection value) {
				return false;
			}

			@Override
			public void disconnected(Controller controller) {
			}

			@Override
			public void connected(Controller controller) {
			}

			@Override
			public boolean buttonUp(Controller controller, int buttonCode) {
				return false;
			}

			@Override
			public boolean buttonDown(Controller controller, int buttonCode) {
				return false;
			}

			@Override
			public boolean axisMoved(Controller controller, int axisCode,
					float value) {
				return false;
			}

			@Override
			public boolean accelerometerMoved(Controller controller,
					int accelerometerCode, Vector3 value) {
				return false;
			}
		});
	}

}
