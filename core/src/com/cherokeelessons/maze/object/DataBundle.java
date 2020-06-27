package com.cherokeelessons.maze.object;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Preferences;

public class DataBundle implements Preferences, Serializable {

	private static final long serialVersionUID = 5415240308910700436L;

	private final HashMap<String, Object> store = new HashMap<>();

	@Override
	public void clear() {
		store.clear();
	}

	@Override
	public boolean contains(final String key) {
		return store.containsKey(key);
	}

	@Override
	public void flush() {
	}

	@Override
	public Map<String, ?> get() {
		return store;
	}

	@Override
	public boolean getBoolean(final String key) {
		return getBoolean(key, false);
	}

	@Override
	public boolean getBoolean(final String key, final boolean defValue) {
		final Object o = store.get(key);
		if (o instanceof Boolean) {
			return (Boolean) store.get(key);
		}
		return defValue;
	}

	@Override
	public float getFloat(final String key) {
		return getFloat(key, 0f);
	}

	@Override
	public float getFloat(final String key, final float defValue) {
		final Object o = store.get(key);
		if (o instanceof Float) {
			return (Float) store.get(key);
		}
		return defValue;
	}

	@Override
	public int getInteger(final String key) {
		return getInteger(key, 0);
	}

	@Override
	public int getInteger(final String key, final int defValue) {
		final Object o = store.get(key);
		if (o instanceof Integer) {
			return (Integer) store.get(key);
		}
		return defValue;
	}

	@Override
	public long getLong(final String key) {
		return getLong(key, 0l);
	}

	@Override
	public long getLong(final String key, final long defValue) {
		final Object o = store.get(key);
		if (o instanceof Long) {
			return (Long) store.get(key);
		}
		return defValue;
	}

	@Override
	public String getString(final String key) {
		return getString(key, null);
	}

	@Override
	public String getString(final String key, final String defValue) {
		final Object o = store.get(key);
		if (o instanceof String) {
			return (String) store.get(key);
		}
		return defValue;
	}

	public void put(final DataBundle data) {
		put(data.get());
	}

	@Override
	public Preferences put(final Map<String, ?> vals) {
		store.putAll(vals);
		return this;
	}

	@Override
	public Preferences putBoolean(final String key, final boolean val) {
		store.put(key, val);
		return this;
	}

	@Override
	public Preferences putFloat(final String key, final float val) {
		store.put(key, val);
		return this;
	}

	@Override
	public Preferences putInteger(final String key, final int val) {
		store.put(key, val);
		return this;
	}

	@Override
	public Preferences putLong(final String key, final long val) {
		store.put(key, val);
		return this;
	}

	@Override
	public Preferences putString(final String key, final String val) {
		store.put(key, val);
		return this;
	}

	@Override
	public void remove(final String key) {
		store.remove(key);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DataBundle [");
		if (store != null) {
			builder.append("store=");
			builder.append(store);
		}
		builder.append("]");
		return builder.toString();
	}

}
