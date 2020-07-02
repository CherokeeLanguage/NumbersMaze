package com.cherokeelessons.maze.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cherokeelessons.maze.NumbersMaze;

public class DesktopLauncher {
	@SuppressWarnings("unused")
	public static void main(final String[] arg) {
		final NumbersMaze game = new NumbersMaze();
		final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 720;
		config.width = 1280;
		config.title="Cherokee Numbers Maze";
		new LwjglApplication(game, config);
	}
}
