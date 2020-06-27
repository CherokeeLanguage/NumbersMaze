package com.cherokeelessons.maze.object;

import java.util.Random;

import com.badlogic.gdx.utils.Array;

public class GenerateNumber {

	static private final String[] chr_xnumbers = { "ᏃᏘ", "ᏍᎪᎯᏥᏆ", "ᏔᎵᏥᏆ", "ᏦᎢᏥᏆ", "ᏅᎩᏥᏆ", "ᎯᏍᎩᏥᏆ", "ᏑᏓᎵᏥᏆ", "ᎦᎵᏉᏥᏆ",
			"ᏁᎳᏥᏆ", "ᏐᏁᎳᏥᏆ" };
	static private final String[] chr_ynumbers = { "ᏃᏘ", "ᏍᎪᎯ", "ᏔᎵᏍᎪᎯ", "ᏦᏍᎪᎯ", "ᏅᎩᏍᎪᎯ", "ᎯᏍᎩᏍᎪᎯ", "ᏑᏓᎵᏍᎪᎯ", "ᎦᎵᏆᏍᎪᎯ",
			"ᏁᎳᏍᎪᎯ", "ᏐᏁᎳᏍᎪᎯ" };
	static private final String[] chr_znumbers = { "ᏃᏘ", "ᏌᏊ", "ᏔᎵ", "ᏦᎢ", "ᏅᎩ", "ᎯᏍᎩ", "ᏑᏓᎵ", "ᎦᎵᏉᎩ", "ᏣᏁᎳ", "ᏐᏁᎳ",
			"ᏍᎪᎯ", "ᏌᏚ", "ᏔᎵᏚ", "ᏦᎦᏚ", "ᏂᎦᏚ", "ᏍᎩᎦᏚ", "ᏓᎳᏚ", "ᎦᎵᏆᏚ", "ᏁᎳᏚ", "ᏐᏁᎳᏚ" };

	static public Array<String> getAudioSequence(int number) {
		final Array<String> list = new Array<>();
		// 100 -> 900
		for (int ix = 9; ix > 0; ix--) {
			if (ix * 100 > number) {
				continue;
			}
			number -= ix * 100;
			list.add(ix * 100 + "");
		}
		// 20 -> 90
		for (int ix = 9; ix > 1; ix--) {
			if (ix * 10 > number) {
				continue;
			}
			number -= ix * 10;
			if (number > 0) {
				list.add(ix * 10 + "_");
			} else {
				list.add(ix * 10 + "");
			}
		}
		// 1 -> 19
		for (int ix = 19; ix > 0; ix--) {
			if (ix > number) {
				continue;
			}
			number -= ix;
			list.add(ix + "");
		}
		return list;
	}

	static public String getCardinal(int number) {
		String written = "";
		// 100 -> 900
		for (int ix = chr_xnumbers.length - 1; ix > 0; ix--) {
			if (ix * 100 > number) {
				continue;
			}
			written = written + " " + chr_xnumbers[ix];
			number -= ix * 100;
		}
		// 20 -> 90
		for (int ix = chr_ynumbers.length - 1; ix > 1; ix--) {
			if (ix * 10 > number) {
				continue;
			}
			written = written + " " + chr_ynumbers[ix];
			number -= ix * 10;
		}
		// 1 -> 19
		for (int ix = chr_znumbers.length; ix > 0; ix--) {
			if (ix > number) {
				continue;
			}
			written = written.replaceAll("Ꭿ$", "") + " " + chr_znumbers[ix];
			number -= ix;
		}
		return written;
	}

	static public String getRandomCardinal(final int from, final int to) {
		return getCardinal(roll(1, to - from + 1) + from);
	}

	static public String getRandomWeightedCardinal() {
		String num;
		num = getRandomCardinal(1, 10);
		if (snakeEyes()) {
			num = getRandomCardinal(11, 19);
			if (snakeEyes()) {
				num = getRandomCardinal(20, 99);
				if (snakeEyes()) {
					num = getRandomCardinal(100, 999);
				}
			}
		}
		return num;
	}

	static private int roll(final int count, final int sides) {
		final Random r = new Random();
		int result = 0;
		for (int ix = 0; ix < count; ix++) {
			result += r.nextInt(sides) + 1;
		}
		return result;
	}

	static private boolean snakeEyes() {
		return roll(2, 6) == 1;
	}

}
