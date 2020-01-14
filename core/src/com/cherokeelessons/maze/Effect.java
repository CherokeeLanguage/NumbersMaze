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

	public static class MusicStopEvent {
		public String name="";
	}
	
	public static class MusicPauseEvent {
		public String name="";
	}
	
	public static class MusicPlayEvent {
		public String name="";
		public float vol=1f;
		public boolean loop=false;
	}
	
	public static class SoundPlayEvent {
		public String name="";
		public float vol=1f;
		public boolean loop=false;
	}
	
	@Subscribe
	public void play(MusicPlayEvent e) {
		play_music(e.name, e.vol, e.loop);
	}
	
	@Subscribe
	public void play(SoundPlayEvent e) {
		play(e.name, e.vol, e.loop);
	}
	
	private float volume_challenge = .5f;
	private float volume_music = .1f;
	private float volume_effects = .75f;
	
	public static class VolumeChallenge {
		public float vol=1f;
	}
	public static class VolumeMusic {
		public float vol=1f;
	}
	public static class VolumeEffects {
		public float vol=1f;
	}
	@Subscribe
	public void setVolume(VolumeChallenge v){
		if (v.vol<0f || v.vol>1f) {
			return;
		}
		volume_challenge=v.vol;
	}
	@Subscribe
	public void setVolume(VolumeMusic v){
		if (v.vol<0f || v.vol>1f) {
			return;
		}
		volume_music=v.vol;
	}
	@Subscribe
	public void setVolume(VolumeEffects v){
		if (v.vol<0f || v.vol>1f) {
			return;
		}
		volume_effects=v.vol;
	}

	final static public int BOX_MOVED = 1;
	final static public int PLINK = 2;
	final static public int PICK_UP = 3;
	final static public int DROP_IT = 4;
	final static public int MENU_MOVE = 5;
	final static public int BOOM_A = 6;
	final static public int BOOM_B = 7;

	final protected Vector2 mic = new Vector2();

	public static class SetMicPos {
		final public Vector2 pos=new Vector2();
	}
	@Subscribe
	public void setMicrophonePosition(SetMicPos p) {
		setMicrophonePosition(p.pos);
	}
	private void setMicrophonePosition(Vector2 pos) {
		mic.x = pos.x;
		mic.y = pos.y;
	}
	 
	public static class PreloadEvent {
		public String name="";
	}
	@Subscribe
	public void preload(PreloadEvent e) {
		preload(e.name);
	}
	public static class DoAudioEvent {
		final public Array<Integer> audioQueue=new Array<>();
		final public Vector2 location=new Vector2();
	}
	@Subscribe
	public void doAudio(DoAudioEvent e) {
		doAudio(e.audioQueue, e.location);
	}
	private void doAudio(Array<Integer> audioQueue, Vector2 pos) {
		float d = pos.dst(mic);
		// d=d*d;
		float volume;

		if (d > 0) {
			volume = 1 / d;
		} else {
			volume = 1;
		}
		if (volume<.01f) {
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

	private HashMap<String, Sound> cache_sound = new HashMap<>();
	private HashMap<String, Music> cache_music = new HashMap<>();
	private HashMap<String, Long> lastPlayed = new HashMap<>();

	private void preload(String sound) {
		synchronized (cache_sound) {
			if (cache_sound.containsKey(sound)) {
				return;
			}
			Sound s = Gdx.audio.newSound(Gdx.files.internal("audio/" + sound
					+ ".ogg"));
			cache_sound.put(sound, s);
			lastPlayed.put(sound, 0l);
			Gdx.app.log(this.getClass().getSimpleName(),"Preload: " + sound);
		}
	}

//	private long play(String sound) {
//		preload(sound);
//		if (System.currentTimeMillis() - lastPlayed.get(sound) < 125) {
//			return 0;
//		}
//		lastPlayed.put(sound, System.currentTimeMillis());
//		return cache_sound.get(sound).play(volume_effects);
//	}

	private long play(String sound, float volume, boolean loop) {
		preload(sound);
		if (System.currentTimeMillis() - lastPlayed.get(sound) < 125) {
			return 0;
		}
		lastPlayed.put(sound, System.currentTimeMillis());
		Sound s = cache_sound.get(sound);
		if (loop) {
			return s.loop(volume * volume_effects);
		} else {
			return s.play(volume * volume_effects);
		}
	}

	public void stop(String sound, long sound_id) {
		preload(sound);
		cache_sound.get(sound).stop(sound_id);
	}

	public void stop(String sound) {
		preload(sound);
		cache_sound.get(sound).stop();
	}

	public long loop(String sound) {
		preload(sound);
		return cache_sound.get(sound).loop();
	}

	public class NumberSequenceRunnable implements Runnable {
		private Music activeNumber = null;
		private String activeNumberName = null;
		private ScreenBase screen;
		private Array<String> sequence;

		public NumberSequenceRunnable(ScreenBase screen, Array<String> list) {
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
					activeNumber = play_music("numbers/" + activeNumberName,
							1f, false);
					activeNumber.setVolume(volume_challenge);
				}
				screen.postRunnable(this);
			}
		}
	}

	public static class PlayNumberSequence {
		public ScreenBase screen=null;
		final public Array<String> list=new Array<>();
	}
	@Subscribe
	public void play_number_sequence(PlayNumberSequence e) {
		play_number_sequence(e.screen, e.list);
	}
	private void play_number_sequence(final ScreenBase screen,
			final Array<String> list) {
		screen.postRunnable(new NumberSequenceRunnable(screen, list));
	}

	public static class StopNumbers {}
	@Subscribe
	public void stop_numbers(StopNumbers n) {
		stop_numbers();
	}
	private void stop_numbers() {
		synchronized (cache_music) {
			for (int ix = 0; ix < 20; ix++) {
				String number = "numbers/" + ix;
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
				String number = "numbers/" + ix;
				if (cache_music.containsKey(number)) {
					cache_music.get(number).stop();
					cache_music.get(number).dispose();
					cache_music.remove(number);
				}
			}
		}
	}

	@Subscribe
	public void stop_music(MusicStopEvent e) {
		stop_music(e.name);
	}
	private void stop_music(String music) {
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

	private Music play_music(String music, float vol, boolean loop) {
		Music m;
		synchronized (cache_music) {
			if (!cache_music.containsKey(music)) {
				try {
					m = Gdx.audio.newMusic(Gdx.files.internal("audio/" + music
							+ ".ogg"));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				cache_music.put(music, m);
			} else {
				m = cache_music.get(music);
			}
			m.setLooping(loop);
			m.setVolume(volume_music*vol);
			m.play();
		}
		Gdx.app.log(this.getClass().getSimpleName(),"music cache size: " + cache_music.size());
		return m;
	}

	public void dispose() {
		synchronized (cache_music) {
			for (Music m : cache_music.values()) {
				try {
					m.stop();
					m.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			cache_music.clear();
		}
		synchronized (cache_sound) {
			for (Sound s : cache_sound.values()) {
				try {
					s.stop();
					s.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			cache_sound.clear();
		}

	}

	public float getVolume_music() {
		return volume_music;
	}

	public void setVolume_music(float volume_music) {
		this.volume_music = volume_music;
	}

	public float getVolume_effects() {
		return volume_effects;
	}

	public void setVolume_effects(float volume_effects) {
		this.volume_effects = volume_effects;
	}

	public long loop(String sound, float volume) {
		preload(sound);
		return cache_sound.get(sound).loop(volume);
	}

	@Subscribe
	public void pause_music(MusicPauseEvent e) {
		pause_music(e.name);
	}
	private void pause_music(String music) {
		Music m = null;
		if (!cache_music.containsKey(music)) {
			m = Gdx.audio.newMusic(Gdx.files
					.internal("audio/" + music + ".ogg"));
			cache_music.put(music, m);
		} else {
			m = cache_music.get(music);
		}
		m.pause();
	}
}
