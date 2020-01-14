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

	public PlayerInput() {}

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
		switch (value) {
		case center:
			deltaX=0;
			deltaY=0;
			break;
		case east:
			deltaX=1;
			deltaY=0;
			break;
		case north:
			deltaX=0;
			deltaY=1;
			break;
		case northEast:
			deltaX=1;
			deltaY=1;
			break;
		case northWest:
			deltaX=-1;
			deltaY=1;
			break;
		case south:
			deltaX=0;
			deltaY=-1;
			break;
		case southEast:
			deltaX=1;
			deltaY=-1;
			break;
		case southWest:
			deltaX=-1;
			deltaY=-1;
			break;
		case west:
			deltaX=-1;
			deltaY=0;
			break;
		}
		
		return false;
	}

	@Override
	public void disconnected(Controller controller) {
		Gdx.app.log(this.getClass().getSimpleName(),"LOST CONTROLLER!");
	}

	@Override
	public void connected(Controller controller) {
		Gdx.app.log(this.getClass().getSimpleName(),"NEW CONTROLLER!");
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
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
			btn_a=false;
		}
		if (Xbox.BUTTON_B == buttonCode) {
			btn_b=false;
		}
		if (Xbox.BUTTON_X == buttonCode) {
			btn_x=false;
		}
		if (Xbox.BUTTON_Y == buttonCode) {
			btn_y=false;
		}
		if (Xbox.BUTTON_L1 == buttonCode) {
			leftTrigger=false;
		}
		if (Xbox.BUTTON_R1 == buttonCode) {
			rightTrigger=false;
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
	public boolean buttonDown(Controller controller, int buttonCode) {
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
			btn_a=true;
		}
		if (Xbox.BUTTON_B == buttonCode) {
			btn_b=true;
		}
		if (Xbox.BUTTON_X == buttonCode) {
			btn_x=true;
		}
		if (Xbox.BUTTON_Y == buttonCode) {
			btn_y=true;
		}
		if (Xbox.BUTTON_L1 == buttonCode) {
			leftTrigger=true;
		}
		if (Xbox.BUTTON_R1 == buttonCode) {
			rightTrigger=true;
		}
		if (Xbox.BUTTON_L3 == buttonCode) {
			leftStick = true;
		}
		if (Xbox.BUTTON_R3 == buttonCode) {
			rightStick = true;
		}
		return false;
	}

	HashMap<Integer, Float> lastAxis = new HashMap<>();
	public float deltaX = 0;
	public float deltaY = 0;

	private int current_dpad_x=0;
	private int current_dpad_y=0;
	
	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		// d-pad on android check part I
		if (axisCode==Xbox.DPAD_AXIS_X || axisCode==Xbox.DPAD_AXIS_Y) {
			if (axisCode==Xbox.DPAD_AXIS_X) {
				current_dpad_x=(int)value;
			}
			if (axisCode==Xbox.DPAD_AXIS_Y) {
				current_dpad_y=(int)value;
			}
			//there are BUNCHES of possible directions to send on
			if (current_dpad_x==0) {
				if (current_dpad_y==0) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.center);
				}
				if (current_dpad_y==-1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.north);
				}
				if (current_dpad_y==1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.south);
				}
			}
			if (current_dpad_x==1) {
				if (current_dpad_y==0) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.east);
				}
				if (current_dpad_y==-1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.northEast);
				}
				if (current_dpad_y==1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.southEast);
				}
			}
			if (current_dpad_x==-1) {
				if (current_dpad_y==0) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.west);
				}
				if (current_dpad_y==-1) {
					return povMoved(controller, Xbox.DPAD_POV, PovDirection.northWest);
				}
				if (current_dpad_y==1) {
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
		float delta = lastAxis.get(axisCode) - value;
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
	public boolean accelerometerMoved(Controller controller,
			int accelerometerCode, Vector3 value) {
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		Controller c;
		Array<Controller> controllers = Controllers.getControllers();
		if (!controllers.isEmpty()) {
			c = controllers.first();
		} else {
			c= null;
		}
		switch(keycode) {
		case Keys.MEDIA_PLAY_PAUSE:
			return buttonDown(c,Xbox.BUTTON_X);
		case Keys.CENTER:
			return buttonDown(c, Xbox.BUTTON_A);
		case Keys.Z:
			return buttonDown(c, Xbox.BUTTON_X);
		case Keys.X:
			return buttonDown(c, Xbox.BUTTON_B);
		case Keys.C:
			return buttonDown(c, Xbox.BUTTON_A);
		case Keys.UP:
			return axisMoved(c, Xbox.DPAD_AXIS_Y, -1);
		case Keys.DOWN:
			return axisMoved(c, Xbox.DPAD_AXIS_Y, 1);
		case Keys.RIGHT:
			return axisMoved(c, Xbox.DPAD_AXIS_X, 1);
		case Keys.LEFT:
			return axisMoved(c, Xbox.DPAD_AXIS_X, -1);
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		Controller c;
		Array<Controller> controllers = Controllers.getControllers();
		if (!controllers.isEmpty()) {
			c = controllers.first();
		} else {
			c= null;
		}
		switch(keycode) {
		case Keys.MEDIA_PLAY_PAUSE:
			return buttonUp(c,Xbox.BUTTON_X);
		case Keys.CENTER:
			return buttonUp(c, Xbox.BUTTON_A);
		case Keys.Z:
			return buttonUp(c, Xbox.BUTTON_X);
		case Keys.X:
			return buttonUp(c, Xbox.BUTTON_B);
		case Keys.C:
			return buttonUp(c, Xbox.BUTTON_A);
		case Keys.UP:
			return axisMoved(c, Xbox.DPAD_AXIS_Y, 0);
		case Keys.DOWN:
			return axisMoved(c, Xbox.DPAD_AXIS_Y, 0);
		case Keys.RIGHT:
			return axisMoved(c, Xbox.DPAD_AXIS_X, 0);
		case Keys.LEFT:
			return axisMoved(c, Xbox.DPAD_AXIS_X, 0);
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}

