package com.cherokeelessons.maze;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.cherokeelessons.maze.screen.ScreenBase;
import com.google.common.eventbus.Subscribe;

public class Effect {

	public static class DoAudioEvent {
		final public Array<Integer> audioQueue = new Array<>();
		final public Vector2 location = new Vector2();
	}

	public static class MusicPauseEvent {
		public String name = "";
	}

	public static class MusicPlayEvent {
		public String name = "";
		public float vol = 1f;
		public boolean loop = false;
	}

	public static class MusicStopEvent {
		public String name = "";
	}

	public class NumberSequenceRunnable implements Runnable {
		private Music activeNumber = null;
		private String activeNumberName = null;
		private final ScreenBase screen;
		private final Array<String> sequence;

		public NumberSequenceRunnable(final ScreenBase screen, final Array<String> list) {
			this.screen = screen;
			sequence = list;
		}

		@Override
		public void run() {
			synchronized (cache_music) {
				if (activeNumber != null && !activeNumber.isPlaying()) {
					activeNumber = cache_music.get(activeNumberName);
					if (activeNumber != null) {
						activeNumber.setVolume(0);
						activeNumber.stop();
						cache_music.remove(activeNumberName);
						activeNumber.dispose();
					}
					activeNumber = null;
					activeNumberName = null;
					if (sequence.size == 0) {
						return;
					}
				}
				if (activeNumber == null) {
					activeNumberName = sequence.removeIndex(0);
					activeNumber = play_music("numbers/" + activeNumberName, 1f, false);
					activeNumber.setVolume(volume_challenge);
				}
				screen.postRunnable(this);
			}
		}
	}

	public static class PlayNumberSequence {
		public ScreenBase screen = null;
		final public Array<String> list = new Array<>();
	}

	public static class PreloadEvent {
		public String name = "";
	}

	public static class SetMicPos {
		final public Vector2 pos = new Vector2();
	}

	public static class SoundPlayEvent {
		public String name = "";
		public float vol = 1f;
		public boolean loop = false;
	}

	public static class StopNumbers {
	}

	public static class VolumeChallenge {
		public float vol = 1f;
	}

	public static class VolumeEffects {
		public float vol = 1f;
	}

	public static class VolumeMusic {
		public float vol = 1f;
	}

	final static public int BOX_MOVED = 1;
	final static public int PLINK = 2;

	final static public int PICK_UP = 3;
	final static public int DROP_IT = 4;
	final static public int MENU_MOVE = 5;
	final static public int BOOM_A = 6;
	final static public int BOOM_B = 7;
	private float volume_challenge = .5f;
	private float volume_music = .1f;

	private float volume_effects = .75f;

	final protected Vector2 mic = new Vector2();
	private final HashMap<String, Sound> cache_sound = new HashMap<>();
	private final HashMap<String, Music> cache_music = new HashMap<>();

	private final HashMap<String, Long> lastPlayed = new HashMap<>();

	public void dispose() {
		synchronized (cache_music) {
			for (final Music m : cache_music.values()) {
				try {
					m.stop();
					m.dispose();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			cache_music.clear();
		}
		synchronized (cache_sound) {
			for (final Sound s : cache_sound.values()) {
				try {
					s.stop();
					s.dispose();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			cache_sound.clear();
		}

	}

	private void doAudio(final Array<Integer> audioQueue, final Vector2 pos) {
		final float d = pos.dst(mic);
		// d=d*d;
		float volume;

		if (d > 0) {
			volume = 1 / d;
		} else {
			volume = 1;
		}
		if (volume < .01f) {
			return;
		}

		while (audioQueue.size > 0) {
			switch (audioQueue.get(0)) {
			case BOX_MOVED:
				play("box_moved", volume * .5f, false);
				break;
			case PLINK:
				play("plink", volume * .5f, false);
				break;
			case PICK_UP:
				play("pick_up", .25f, false);
				break;
			case DROP_IT:
				play("drop_it", .25f, false);
				break;
			case BOOM_A:
				play("explodemini", volume, false);
				break;
			case BOOM_B:
				play("explode", volume, false);
				break;
			default:
				break;
			}
			audioQueue.removeIndex(0);
		}
	}

	@Subscribe
	public void doAudio(final DoAudioEvent e) {
		doAudio(e.audioQueue, e.location);
	}

	public float getVolume_effects() {
		return volume_effects;
	}

	public float getVolume_music() {
		return volume_music;
	}

	public long loop(final String sound) {
		preload(sound);
		return cache_sound.get(sound).loop();
	}

	public long loop(final String sound, final float volume) {
		preload(sound);
		return cache_sound.get(sound).loop(volume);
	}

	@Subscribe
	public void pause_music(final MusicPauseEvent e) {
		pause_music(e.name);
	}

//	private long play(String sound) {
//		preload(sound);
//		if (System.currentTimeMillis() - lastPlayed.get(sound) < 125) {
//			return 0;
//		}
//		lastPlayed.put(sound, System.currentTimeMillis());
//		return cache_sound.get(sound).play(volume_effects);
//	}

	private void pause_music(final String music) {
		Music m = null;
		if (!cache_music.containsKey(music)) {
			m = Gdx.audio.newMusic(Gdx.files.internal("audio/" + music + ".ogg"));
			cache_music.put(music, m);
		} else {
			m = cache_music.get(music);
		}
		m.pause();
	}

	@Subscribe
	public void play(final MusicPlayEvent e) {
		play_music(e.name, e.vol, e.loop);
	}

	@Subscribe
	public void play(final SoundPlayEvent e) {
		play(e.name, e.vol, e.loop);
	}

	private long play(final String sound, final float volume, final boolean loop) {
		preload(sound);
		if (System.currentTimeMillis() - lastPlayed.get(sound) < 125) {
			return 0;
		}
		lastPlayed.put(sound, System.currentTimeMillis());
		final Sound s = cache_sound.get(sound);
		if (loop) {
			return s.loop(volume * volume_effects);
		} else {
			return s.play(volume * volume_effects);
		}
	}

	private Music play_music(final String music, final float vol, final boolean loop) {
		Music m;
		synchronized (cache_music) {
			if (!cache_music.containsKey(music)) {
				try {
					m = Gdx.audio.newMusic(Gdx.files.internal("audio/" + music + ".ogg"));
				} catch (final Exception e) {
					e.printStackTrace();
					return null;
				}
				cache_music.put(music, m);
			} else {
				m = cache_music.get(music);
			}
			m.setLooping(loop);
			m.setVolume(volume_music * vol);
			m.play();
		}
		Gdx.app.log(this.getClass().getSimpleName(), "music cache size: " + cache_music.size());
		return m;
	}

	@Subscribe
	public void play_number_sequence(final PlayNumberSequence e) {
		play_number_sequence(e.screen, e.list);
	}

	private void play_number_sequence(final ScreenBase screen, final Array<String> list) {
		screen.postRunnable(new NumberSequenceRunnable(screen, list));
	}

	@Subscribe
	public void preload(final PreloadEvent e) {
		preload(e.name);
	}

	private void preload(final String sound) {
		synchronized (cache_sound) {
			if (cache_sound.containsKey(sound)) {
				return;
			}
			final Sound s = Gdx.audio.newSound(Gdx.files.internal("audio/" + sound + ".ogg"));
			cache_sound.put(sound, s);
			lastPlayed.put(sound, 0l);
			Gdx.app.log(this.getClass().getSimpleName(), "Preload: " + sound);
		}
	}

	@Subscribe
	public void setMicrophonePosition(final SetMicPos p) {
		setMicrophonePosition(p.pos);
	}

	private void setMicrophonePosition(final Vector2 pos) {
		mic.x = pos.x;
		mic.y = pos.y;
	}

	@Subscribe
	public void setVolume(final VolumeChallenge v) {
		if (v.vol < 0f || v.vol > 1f) {
			return;
		}
		volume_challenge = v.vol;
	}

	@Subscribe
	public void setVolume(final VolumeEffects v) {
		if (v.vol < 0f || v.vol > 1f) {
			return;
		}
		volume_effects = v.vol;
	}

	@Subscribe
	public void setVolume(final VolumeMusic v) {
		if (v.vol < 0f || v.vol > 1f) {
			return;
		}
		volume_music = v.vol;
	}

	public void setVolume_effects(final float volume_effects) {
		this.volume_effects = volume_effects;
	}

	public void setVolume_music(final float volume_music) {
		this.volume_music = volume_music;
	}

	public void stop(final String sound) {
		preload(sound);
		cache_sound.get(sound).stop();
	}

	public void stop(final String sound, final long sound_id) {
		preload(sound);
		cache_sound.get(sound).stop(sound_id);
	}

	@Subscribe
	public void stop_music(final MusicStopEvent e) {
		stop_music(e.name);
	}

	private void stop_music(final String music) {
		synchronized (cache_music) {
			Music m;
			if (!cache_music.containsKey(music)) {
				return;
			}
			m = cache_music.get(music);
			cache_music.remove(music);
			m.setVolume(0);
			m.setLooping(false);
			m.stop();
			m.dispose();
		}
	}

	private void stop_numbers() {
		synchronized (cache_music) {
			for (int ix = 0; ix < 20; ix++) {
				final String number = "numbers/" + ix;
				if (cache_music.containsKey(number)) {
					cache_music.get(number).stop();
					cache_music.get(number).dispose();
					cache_music.remove(number);
				}
			}
			for (int ix = 20; ix < 100; ix += 10) {
				String number = "numbers/" + ix;
				if (cache_music.containsKey(number)) {
					cache_music.get(number).stop();
					cache_music.get(number).dispose();
					cache_music.remove(number);
				}
				number += "_";
				if (cache_music.containsKey(number)) {
					cache_music.get(number).stop();
					cache_music.get(number).dispose();
					cache_music.remove(number);
				}
			}
			for (int ix = 100; ix < 1000; ix += 100) {
				final String number = "numbers/" + ix;
				if (cache_music.containsKey(number)) {
					cache_music.get(number).stop();
					cache_music.get(number).dispose();
					cache_music.remove(number);
				}
			}
		}
	}

	@Subscribe
	public void stop_numbers(final StopNumbers n) {
		stop_numbers();
	}
}
