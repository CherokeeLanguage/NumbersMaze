package com.cherokeelessons.maze.entity;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.cherokeelessons.maze.object.Xbox;

public class PlayerInput implements ControllerListener, InputProcessor {

	public boolean btn_a = false;
	public boolean btn_b = false;
	public boolean btn_x = false;
	public boolean btn_y = false;

	public boolean leftTrigger = false;
	public boolean leftStick = false;
	public boolean rightTrigger = false;
	public boolean rightStick = false;

	public boolean showCoords = false;
	public boolean doOnBack = false;
	public boolean doOnStart = false;
	public boolean doOnXbox = false;

	HashMap<Integer, Float> lastAxis = new HashMap<>();

	public float deltaX = 0;

	public float deltaY = 0;

	private int current_dpad_x = 0;

	private int current_dpad_y = 0;

	public PlayerInput() {
	}

	@Override
	public boolean accelerometerMoved(final Controller controller, final int accelerometerCode, final Vector3 value) {
		return false;
	}

	@Override
	public boolean axisMoved(final Controller controller, final int axisCode, float value) {
		// d-pad on android check part I
		if (axisCode == Xbox.DPAD_AXIS_X || axisCode == Xbox.DPAD_AXIS_Y) {
			if (axisCode == Xbox.DPAD_AXIS_X) {
				current_dpad_x = (int) value;
			}
			if (axisCode == Xbox.DPAD_AXIS_Y) {
				current_dpad_y = (int) value;
			}
			// there are BUNCHES of possible directions to send on
			if (current_dpad_x == 0) {
				if (current_dpad_y == 0) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.center);
				}
				if (current_dpad_y == -1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.north);
				}
				if (current_dpad_y == 1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.south);
				}
			}
			if (current_dpad_x == 1) {
				if (current_dpad_y == 0) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.east);
				}
				if (current_dpad_y == -1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.northEast);
				}
				if (current_dpad_y == 1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.southEast);
				}
			}
			if (current_dpad_x == -1) {
				if (current_dpad_y == 0) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.west);
				}
				if (current_dpad_y == -1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.northWest);
				}
				if (current_dpad_y == 1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.southWest);
				}
			}
		}

		// deadzone check
		if (value < .2 && value > -.2) {
			value = 0;
		}
		if (!lastAxis.containsKey(axisCode)) {
			lastAxis.put(axisCode, 0f);
		}
		final float delta = lastAxis.get(axisCode) - value;
		// denoise
		if (delta < .005 && delta > -.005) {
			return false;
		}
		lastAxis.put(axisCode, value);

		if (axisCode == Xbox.AXIS_LEFT_X) {
			deltaX = value;
		}
		if (axisCode == Xbox.AXIS_LEFT_Y) {
			deltaY = -value;
		}
		return false;
	}

	@Override
	public boolean buttonDown(final Controller controller, final int buttonCode) {
		if (buttonCode == Xbox.BUTTON_BACK) {
			return keyDown(Keys.BACK);
		}
		if (buttonCode == Xbox.BUTTON_START) {
			return keyDown(Keys.MENU);
		}
		if (buttonCode == Xbox.BUTTON_MENU) {
			return keyDown(Keys.HOME);
		}
		if (Xbox.BUTTON_A == buttonCode) {
			btn_a = true;
		}
		if (Xbox.BUTTON_B == buttonCode) {
			btn_b = true;
		}
		if (Xbox.BUTTON_X == buttonCode) {
			btn_x = true;
		}
		if (Xbox.BUTTON_Y == buttonCode) {
			btn_y = true;
		}
		if (Xbox.BUTTON_L1 == buttonCode) {
			leftTrigger = true;
		}
		if (Xbox.BUTTON_R1 == buttonCode) {
			rightTrigger = true;
		}
		if (Xbox.BUTTON_L3 == buttonCode) {
			leftStick = true;
		}
		if (Xbox.BUTTON_R3 == buttonCode) {
			rightStick = true;
		}
		return false;
	}

	@Override
	public boolean buttonUp(final Controller controller, final int buttonCode) {
		if (buttonCode == Xbox.BUTTON_BACK) {
			return keyUp(Keys.BACK);
		}
		if (buttonCode == Xbox.BUTTON_START) {
			return keyUp(Keys.MENU);
		}
		if (buttonCode == Xbox.BUTTON_MENU) {
			return keyUp(Keys.HOME);
		}
		if (Xbox.BUTTON_A == buttonCode) {
			btn_a = false;
		}
		if (Xbox.BUTTON_B == buttonCode) {
			btn_b = false;
		}
		if (Xbox.BUTTON_X == buttonCode) {
			btn_x = false;
		}
		if (Xbox.BUTTON_Y == buttonCode) {
			btn_y = false;
		}
		if (Xbox.BUTTON_L1 == buttonCode) {
			leftTrigger = false;
		}
		if (Xbox.BUTTON_R1 == buttonCode) {
			rightTrigger = false;
		}
		if (Xbox.BUTTON_L3 == buttonCode) {
			leftStick = false;
		}
		if (Xbox.BUTTON_R3 == buttonCode) {
			rightStick = false;
		}
		return false;
	}

	@Override
	public void connected(final Controller controller) {
		Gdx.app.log(this.getClass().getSimpleName(), "NEW CONTROLLER!");
	}

	@Override
	public void disconnected(final Controller controller) {
		Gdx.app.log(this.getClass().getSimpleName(), "LOST CONTROLLER!");
	}

	@Override
	public boolean keyDown(final int keycode) {
		System.out.println("PlayerInput.java#keyDown: "+keycode);
		Controller c;
		final Array<Controller> controllers = Controllers.getControllers();
		if (!controllers.isEmpty()) {
			c = controllers.first();
		} else {
			c = null;
		}
		switch (keycode) {
		case Keys.MEDIA_PLAY_PAUSE:
			System.out.println("PlayerInput.java#keyDown-map-to:  [X]");
			return buttonDown(c, Xbox.BUTTON_X);
		case Keys.CENTER:
		case Keys.ENTER:
			System.out.println("PlayerInput.java#keyDown-map-to:  [A]");
			return buttonDown(c, Xbox.BUTTON_A);
		case Keys.Z:
			System.out.println("PlayerInput.java#keyDown-map-to:  [X]");
			return buttonDown(c, Xbox.BUTTON_X);
		case Keys.X:
			System.out.println("PlayerInput.java#keyDown-map-to:  [B]");
			return buttonDown(c, Xbox.BUTTON_B);
		case Keys.C:
			System.out.println("PlayerInput.java#keyDown-map-to:  [A]");
			return buttonDown(c, Xbox.BUTTON_A);
		case Keys.UP:
			System.out.println("PlayerInput.java#keyDown-map-to:  [AXIS-Y-DOWN]");
			return axisMoved(c, Xbox.DPAD_AXIS_Y, -1);
		case Keys.DOWN:
			System.out.println("PlayerInput.java#keyDown-map-to:  [AXYS-Y-UP]");
			return axisMoved(c, Xbox.DPAD_AXIS_Y, 1);
		case Keys.RIGHT:
			System.out.println("PlayerInput.java#keyDown-map-to:  [AXYS-X-RIGHT]");
			return axisMoved(c, Xbox.DPAD_AXIS_X, 1);
		case Keys.LEFT:
			System.out.println("PlayerInput.java#keyDown-map-to:  [AXYS-X-LEFT]");
			return axisMoved(c, Xbox.DPAD_AXIS_X, -1);
		default:
			System.out.println("PlayerInput.java#keyDown-map-to:  [NOT MAPPED]");
			break;
		}
		return false;
	}

	@Override
	public boolean keyTyped(final char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(final int keycode) {
		System.out.println("PlayerInput.java#keyUp: "+keycode);
		Controller c;
		final Array<Controller> controllers = Controllers.getControllers();
		if (!controllers.isEmpty()) {
			c = controllers.first();
		} else {
			c = null;
		}
		switch (keycode) {
		case Keys.MEDIA_PLAY_PAUSE:
			System.out.println("PlayerInput.java#keyUp-map-to:  [X]");
			return buttonUp(c, Xbox.BUTTON_X);
		case Keys.CENTER:
		case Keys.ENTER:
			System.out.println("PlayerInput.java#keyUp-map-to:  [A]");
			return buttonUp(c, Xbox.BUTTON_A);
		case Keys.Z:
			System.out.println("PlayerInput.java#keyUp-map-to:  [X]");
			return buttonUp(c, Xbox.BUTTON_X);
		case Keys.X:
			System.out.println("PlayerInput.java#keyUp-map-to:  [B]");
			return buttonUp(c, Xbox.BUTTON_B);
		case Keys.C:
			System.out.println("PlayerInput.java#keyUp-map-to:  [A]");
			return buttonUp(c, Xbox.BUTTON_A);
		case Keys.UP:
			System.out.println("PlayerInput.java#keyUp-map-to:  [AXIS-Y-DOWN]");
			return axisMoved(c, Xbox.DPAD_AXIS_Y, 0);
		case Keys.DOWN:
			System.out.println("PlayerInput.java#keyUp-map-to:  [AXIS-Y-UP]");
			return axisMoved(c, Xbox.DPAD_AXIS_Y, 0);
		case Keys.RIGHT:
			System.out.println("PlayerInput.java#keyUp-map-to:  [AXIS-X-RIGHT]");
			return axisMoved(c, Xbox.DPAD_AXIS_X, 0);
		case Keys.LEFT:
			System.out.println("PlayerInput.java#keyUp-map-to:  [AXIS-X-LEFT]");
			return axisMoved(c, Xbox.DPAD_AXIS_X, 0);
		default:
			System.out.println("PlayerInput.java#keyUp-map-to:  [NOT MAPPED]");
			break;
		}
		return false;
	}

	@Override
	public boolean mouseMoved(final int screenX, final int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean povMoved(final Controller controller, final int povCode, final PovDirection value) {
		switch (value) {
		case center:
			deltaX = 0;
			deltaY = 0;
			break;
		case east:
			deltaX = 1;
			deltaY = 0;
			break;
		case north:
			deltaX = 0;
			deltaY = 1;
			break;
		case northEast:
			deltaX = 1;
			deltaY = 1;
			break;
		case northWest:
			deltaX = -1;
			deltaY = 1;
			break;
		case south:
			deltaX = 0;
			deltaY = -1;
			break;
		case southEast:
			deltaX = 1;
			deltaY = -1;
			break;
		case southWest:
			deltaX = -1;
			deltaY = -1;
			break;
		case west:
			deltaX = -1;
			deltaY = 0;
			break;
		}

		return false;
	}

	@Override
	public boolean scrolled(final int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean xSliderMoved(final Controller controller, final int sliderCode, final boolean value) {
		return false;
	}

	@Override
	public boolean ySliderMoved(final Controller controller, final int sliderCode, final boolean value) {
		return false;
	}
}
