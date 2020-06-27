package com.cherokeelessons.maze.object;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;

public class Xbox {
	public static final String ID = "Microsoft X-Box 360 pad";

	public static final int BUTTON_A;
	public static final int BUTTON_X;
	public static final int BUTTON_Y;
	public static final int BUTTON_B;
	// public static final int BUTTON_DPAD_UP;
	// public static final int BUTTON_DPAD_DOWN;
	// public static final int BUTTON_DPAD_RIGHT;
	// public static final int BUTTON_DPAD_LEFT;
	public static final int BUTTON_L1;
	public static final int BUTTON_L2;
	public static final int BUTTON_L3;
	public static final int BUTTON_R1;
	public static final int BUTTON_R2;
	public static final int BUTTON_R3;
	public static final int AXIS_LEFT_X;
	public static final int AXIS_LEFT_Y;
	public static final int AXIS_LEFT_TRIGGER;
	public static final int AXIS_RIGHT_X;
	public static final int AXIS_RIGHT_Y;
	public static final int AXIS_RIGHT_TRIGGER;

	// for windows
	public static final int DPAD_AXIS_X;
	public static final int DPAD_AXIS_Y;
	static {
		set: {
			if (Gdx.app.getType().equals(ApplicationType.Android)) {
				// via onAxis
				AXIS_LEFT_X = 0;
				AXIS_LEFT_Y = 1;
				BUTTON_L2 = 2;
				AXIS_RIGHT_X = 3;
				AXIS_RIGHT_Y = 4;
				BUTTON_R2 = 5;
				AXIS_LEFT_TRIGGER = 2;
				AXIS_RIGHT_TRIGGER = 5;
				// we throw these back to POV for consistent processing!
				DPAD_AXIS_X = 6;
				DPAD_AXIS_Y = 7;
				// via onButton
				BUTTON_L1 = 102;
				BUTTON_L3 = 106;
				BUTTON_R1 = 103;
				BUTTON_R3 = 107;
				BUTTON_BACK = 109;
				BUTTON_START = 108;
				BUTTON_MENU = 110;
				BUTTON_A = 96;
				BUTTON_X = 99;
				BUTTON_Y = 100;
				BUTTON_B = 97;
				// pov moved dpad index
				DPAD_POV = 0;
				break set;
			}
			if (Gdx.app.getType().equals(ApplicationType.Desktop)) {
				if (OS.isUnix || OS.isMac) {
					// via onAxis
					AXIS_LEFT_X = 0;
					AXIS_LEFT_Y = 1;
					BUTTON_L2 = 2;
					AXIS_RIGHT_X = 3;
					AXIS_RIGHT_Y = 4;
					BUTTON_R2 = 5;
					DPAD_AXIS_X = -1;
					DPAD_AXIS_Y = -2;
					AXIS_LEFT_TRIGGER = 2;
					AXIS_RIGHT_TRIGGER = 5;
					// via onButton
					BUTTON_L1 = 4;
					BUTTON_L3 = 9;
					BUTTON_R1 = 5;
					BUTTON_R3 = 10;
					BUTTON_BACK = 6;
					BUTTON_START = 7;
					BUTTON_MENU = 8;
					BUTTON_A = 0;
					BUTTON_X = 2;
					BUTTON_Y = 3;
					BUTTON_B = 1;
					// pov moved dpad index
					DPAD_POV = 0;
					break set;
				}
				if (OS.isWindows) {
					// via onAxis
					AXIS_LEFT_X = 1;
					AXIS_LEFT_Y = 0;
					BUTTON_L2 = 2;
					AXIS_RIGHT_X = 4;
					AXIS_RIGHT_Y = 3;
					BUTTON_R2 = 5;
					DPAD_AXIS_X = -1;
					DPAD_AXIS_Y = -2;
					AXIS_LEFT_TRIGGER = 2;
					AXIS_RIGHT_TRIGGER = 5;
					// via onButton
					BUTTON_L1 = 4;
					BUTTON_L3 = 9;
					BUTTON_R1 = 5;
					BUTTON_R3 = 10;
					BUTTON_BACK = 6;
					BUTTON_START = 7;
					BUTTON_MENU = 8;
					BUTTON_A = 0;
					BUTTON_X = 2;
					BUTTON_Y = 3;
					BUTTON_B = 1;
					// pov moved dpad index
					DPAD_POV = 0;
					break set;
				}
			}
			// via onAxis
			AXIS_LEFT_X = 0;
			AXIS_LEFT_Y = 1;
			BUTTON_L2 = 2;
			AXIS_RIGHT_X = 3;
			AXIS_RIGHT_Y = 4;
			BUTTON_R2 = 5;
			DPAD_AXIS_X = -1;
			DPAD_AXIS_Y = -2;
			AXIS_LEFT_TRIGGER = 2;
			AXIS_RIGHT_TRIGGER = 5;
			// via onButton
			BUTTON_L1 = 4;
			BUTTON_L3 = 9;
			BUTTON_R1 = 5;
			BUTTON_R3 = 10;
			BUTTON_BACK = 6;
			BUTTON_START = 7;
			BUTTON_MENU = 8;
			BUTTON_A = 0;
			BUTTON_X = 2;
			BUTTON_Y = 3;
			BUTTON_B = 1;
			// pov moved dpad index
			DPAD_POV = 0;
		}
	}
	public static final int BUTTON_BACK;
	public static final int BUTTON_START;
	public static final int BUTTON_MENU;
	public static final int DPAD_POV;
}