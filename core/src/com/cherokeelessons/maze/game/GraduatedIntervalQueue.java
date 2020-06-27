package com.cherokeelessons.maze.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntArray;

public class GraduatedIntervalQueue {

	public static class Point {

		protected int x, y;

		public Point() {

			setPoint(0, 0);

		}

		public Point(final int coordx, final int coordy) {
			setPoint(coordx, coordy);
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public void setPoint(final int coordx, final int coordy) {
			x = coordx;
			y = coordy;
		}

		public String toPrint() {
			return "[" + x + "," + y + "]";
		}

	}

	private boolean debug = true;

	private boolean doubleMode = false;

	private ArrayList<Integer> startingEntries;

	private ArrayList<Integer> intervalQueue;

	private ArrayList<Integer> bounderiesNameList;

	private HashMap<Integer, Integer> bounderiesByPosition;

	private HashMap<Integer, Integer> bounderiesByName;
	private ArrayList<Point> levelMarks;

	private boolean briefList = false;

	private boolean shortList = false;

	private HashMap<String, String> syllabaryFor = null;

	public GraduatedIntervalQueue() {
		super();
	}

	public void calculateLevelStarts(final int levels) {
		int startingPoint;
		int item;
		int listSize = 0;
		int ix, index;

		levelMarks = new ArrayList<>();
		Point thisLevelMarks = null;
		Point prevLevelMarks = null;

		listSize = bounderiesNameList.size();
		for (ix = 0; ix < levels; ix++) {
			index = (int) Math.ceil(listSize * ((float) ix / (float) levels));
			if (index >= bounderiesNameList.size()) {
				continue;
			}
			item = bounderiesNameList.get(index);
			startingPoint = bounderiesByName.get(item);
			thisLevelMarks = new Point(startingPoint, intervalQueue.size() - 1);
			if (prevLevelMarks != null) {
				prevLevelMarks.y = startingPoint - 1;
			}
			prevLevelMarks = thisLevelMarks;
			levelMarks.add(thisLevelMarks);
		}
	}

	@SuppressWarnings("unused")
	private ArrayList<Integer> dedupeAndSort(final ArrayList<Integer> list) {
		ArrayList<Integer> newList;
		newList = new ArrayList<>();
		newList = new ArrayList<>(new HashSet<>(list));
		Collections.sort(newList);
		return newList;
	}

	@SuppressWarnings("unused")
	private void dumpList(final ArrayList<Integer> listToDump) {
		int ix, len;
		if (!debug) {
			return;
		}
		Gdx.app.log(this.getClass().getSimpleName(), "===================================");
		for (ix = 0, len = listToDump.size(); ix < len; ix++) {
			Gdx.app.log(this.getClass().getSimpleName(), ix + ": " + listToDump.get(ix));
		}
		Gdx.app.log(this.getClass().getSimpleName(), "===================================");
		Gdx.app.log(this.getClass().getSimpleName(), "");
	}

	public Integer getEntry(final int ix) {
		if (intervalQueue.size() > ix && ix >= 0) {
			return intervalQueue.get(ix);
		}
		return 0;
	}

	public ArrayList<Integer> getIntervalQueue() {
		return intervalQueue;
	}

	int getLevelCount() {
		return levelMarks.size();
	}

	int getLevelEndPosition(final int level) {
		return levelMarks.get(level).y;
	}

	int getLevelStartName(final int level) {
		int position;
		position = getLevelStartPosition(level);
		return bounderiesByPosition.get(position);
	}

	int getLevelStartPosition(final int level) {
		return levelMarks.get(level).x;
	}

	/**
	 * based on getOffsetsReal from 'translations.php'
	 *
	 * @return ArrayList<Integer>
	 */
	private ArrayList<Integer> getOffsets() {
		ArrayList<Integer> o1;
		int ip, depth = 6, stagger = 2, ix, basePower = 2;
		o1 = new ArrayList<>();

		if (isBriefList()) {
			depth = 6;
			stagger = 1;
			basePower = 3;
		}

		if (isShortList()) {
			depth = 2;
			stagger = 1;
			basePower = 2;
		}

		for (ix = 0; ix < stagger; ix++) {
			for (ip = 0; ip <= depth; ip++) {
				o1.add((int) Math.pow(basePower + ix, ip));
			}
		}
		return o1;
	}

	/**
	 * based on getOffsetsReal from 'translations.php'
	 *
	 * @return ArrayList<Integer>
	 */
	private ArrayList<Integer> getOffsetsDoubled() {
		ArrayList<Integer> o1;
		int ip;
		final int depth = 6, stagger = 4;
		int ix;

		o1 = new ArrayList<>();

		for (ix = 0; ix < stagger; ix++) {
			for (ip = 0; ip <= depth; ip++) {
				o1.add((int) Math.pow(2 + ix, ip));
			}
		}
		return o1;
	}

	private ArrayList<Integer> getQueue(final ArrayList<Integer> startingEntries2) {
		int ix, iy, ia;
		ArrayList<Integer> offsets;
		ArrayList<Integer> newQueue = null;
		ArrayList<Integer> samples;

		newQueue = new ArrayList<>();
		samples = new ArrayList<>();
		if (isDoubleMode()) {
			offsets = getOffsetsDoubled();
		} else {
			offsets = getOffsets();
		}

		samples.addAll(startingEntries2);
		// mixUpSamples(samples);
		// orderBySizeAsc(samples);
		// orderBySizeAscCustom(samples);

		/**
		 * process samples creating non-random work queue
		 */
		for (ix = 0; ix < samples.size(); ix++) {
			ia = 0;
			for (iy = 0; iy < offsets.size(); iy++) {
				while (newQueue.size() < ia + 1) {
					newQueue.add(0);
				}
				while (newQueue.get(ia) != 0) {
					ia++;
					while (newQueue.size() < ia + 1) {
						newQueue.add(0);
					}
				}
				newQueue.set(ia, samples.get(ix));
				ia += offsets.get(iy);
			}
		}
		removeGaps(newQueue);

		return newQueue;
	}

	public HashMap<String, String> getSyllabaryFor() {
		return syllabaryFor;
	}

	public boolean isBriefList() {
		return briefList;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isDoubleMode() {
		return doubleMode;
	}

	public boolean isShortList() {
		return shortList;
	}

	public void load(final ArrayList<Integer> _startingEntries) {
		startingEntries = new ArrayList<>();
		bounderiesNameList = new ArrayList<>();
		bounderiesByName = new HashMap<>();
		bounderiesByPosition = new HashMap<>();
		startingEntries.addAll(_startingEntries);// dedupeAndSort(_startingEntries);
		intervalQueue = getQueue(startingEntries);
		locateBounderies();
		calculateLevelStarts(18);
	}

	public void load(final IntArray _startingEntries) {
		final ArrayList<Integer> tmp = new ArrayList<>(_startingEntries.size);
		for (final int i : _startingEntries.items) {
			tmp.add(i);
		}
		load(tmp);
	}

	private void locateBounderies() {
		int ix, len;
		int item;

		for (ix = 0, len = intervalQueue.size(); ix < len; ix++) {
			item = intervalQueue.get(ix);
			if (bounderiesNameList.contains(item)) {
				continue;
			}
			bounderiesNameList.add(item);
			bounderiesByName.put(item, ix);
			bounderiesByPosition.put(ix, item);
		}
	}

	@SuppressWarnings("unused")
	private void mixUpSamples(final ArrayList<Integer> samples) {
		ArrayList<Integer> mixedUp;
		ArrayList<Integer> offsets;

		mixedUp = new ArrayList<>();
		offsets = getOffsets();
		/**
		 * re-arrange samples into a non-random, non-alpha order
		 */
		for (int ix = 0; ix < 10; ix++) {
			while (samples.size() > 0) {
				for (int iy = 0; samples.size() > 0 && iy < offsets.size(); iy++) {
					int ia = offsets.get(iy);
					while (mixedUp.size() > ia && mixedUp.get(ia) != 0) {
						ia++;
					}
					while (mixedUp.size() < ia + 1) {
						mixedUp.add(0);
					}
					mixedUp.set(ia, samples.get(0));
					samples.remove(0);
				}
			}
			removeGaps(mixedUp); // removes "holes"
			Collections.reverse(mixedUp);
			samples.clear();
			samples.addAll(mixedUp);
			mixedUp.clear();
		}
	}

	private void orderBySizeAsc(final ArrayList<String> samples) {
		Collections.sort(samples, new Comparator<String>() {
			@Override
			public int compare(final String o1, final String o2) {
				if (o1.length() < o2.length()) {
					return -1;
				}
				if (o1.length() > o2.length()) {
					return 1;
				}
				return o1.compareTo(o2);
			}
		});
	}

	@SuppressWarnings("unused")
	private void orderBySizeAscCustom(final ArrayList<String> samples) {
		if (syllabaryFor == null) {
			orderBySizeAsc(samples);
			return;
		}
		Collections.sort(samples, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (syllabaryFor.containsKey(o1)) {
					o1 = syllabaryFor.get(o1);
				}
				if (syllabaryFor.containsKey(o2)) {
					o2 = syllabaryFor.get(o2);
				}
				if (o1.length() < o2.length()) {
					return -1;
				}
				if (o1.length() > o2.length()) {
					return 1;
				}
				return o1.compareTo(o2);
			}
		});
	}

	public void removeGaps(final ArrayList<Integer> queue) {
		int ix = 0, repeat;
		ArrayList<Integer> vx1 = null;
		ArrayList<Integer> vx2 = null;
		boolean hasDupes = true;
		int prev = 0;
		int current = 0;

		vx1 = new ArrayList<>();
		vx2 = new ArrayList<>();

		/**
		 * scan for and try and prevent "repeats"
		 */
		for (repeat = 0; hasDupes && repeat < 10; repeat++) {
			prev = 0;
			vx1.clear();
			vx2.clear();
			hasDupes = false;
			for (ix = 0; ix < queue.size(); ix++) {
				if (queue.get(ix) == 0) {
					continue;
				}
				current = queue.get(ix);
				if (current != prev) {
					vx1.add(current);
					prev = current;
				} else {
					vx2.add(current);
					hasDupes = true;
				}
			}
			queue.clear();
			queue.addAll(vx1);
			queue.addAll(vx2);
		}

		vx1.clear();
		vx2.clear();
	}

	public void setBriefList(final boolean briefList) {
		this.briefList = briefList;
	}

	public void setDebug(final boolean debug) {
		this.debug = debug;
	}

	public void setDoubleMode(final boolean doubleMode) {
		this.doubleMode = doubleMode;
	}

	public void setShortList(final boolean shortList) {
		this.shortList = shortList;
	}

	public void setSyllabaryFor(final HashMap<String, String> syllabary) {
		syllabaryFor = syllabary;
	}

}
