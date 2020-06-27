package com.cherokeelessons.maze.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cherokeelessons.maze.Effect.SoundPlayEvent;
import com.cherokeelessons.maze.NumbersMaze;

public class ScorePopup {
//	private static BitmapFont theFont=null;
//	private static synchronized BitmapFont getFont(){
//		if (theFont==null) {
//			StringBuilder c=new StringBuilder();
//			c.append(FreeTypeFontGenerator.DEFAULT_CHARS);
//			for (char chr = 'Ꭰ'; chr<= 'Ᏼ'; chr++) {
//				c.append(chr);
//			}
//			String myCharSet=c.toString();
//
//			FileHandle ttfFile = Gdx.files.getFileHandle("fonts/CherokeeHandone.ttf", FileType.Internal);
//			FreeTypeFontGenerator ttfgen = new FreeTypeFontGenerator(ttfFile);
//			theFont = ttfgen.generateFont(40, myCharSet, false);
//			theFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
////			theFont.setFixedWidthGlyphs("+-01234567890%");
//			ttfgen.dispose();
//		}
//		return theFont;
//	}
	private Table table = null;
	private Label msg = null;
	private LabelStyle style = null;
	private final Runnable selfDestruct = new Runnable() {
		@Override
		public void run() {
			table.remove();
			msg.remove();
		}
	};
	private final Runnable ding2 = new Runnable() {
		@Override
		public void run() {
			final SoundPlayEvent e = new SoundPlayEvent();
			e.name = "ding2";
			NumbersMaze.post(e);

		}
	};

	public ScorePopup(int count, final float x, final float y, final BitmapFont font) {
		final String scoreText = "+" + count;

		style = new LabelStyle(font, new Color(Color.RED));
		msg = new Label(scoreText, style);

		table = new Table();
		table.setX(x);
		table.setY(y);
		table.add(msg);

		table.setTransform(true);
		table.addAction(Actions.delay(0.0f, Actions.scaleTo(16, 16, 1.5f)));
		table.addAction(Actions.delay(0.0f, Actions.alpha(0, 1.5f)));
		table.addAction(Actions.delay(3, Actions.run(selfDestruct)));
		int c = 0;
		while (count > 0) {
			table.addAction(Actions.delay(c / 4f, Actions.run(ding2)));
			c += 1;
			count /= 5;
		}
	}

	public Table getLabel() {
		return table;
	}
}
