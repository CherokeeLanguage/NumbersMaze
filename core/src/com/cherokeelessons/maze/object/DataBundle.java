package com.cherokeelessons.maze.object;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Preferences;

public class DataBundle implements Preferences, Serializable {
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataBundle [");
		if (store != null) {
			builder.append("store=");
			builder.append(store);
		}
		builder.append("]");
		return builder.toString();
	}

	private static final long serialVersionUID = 5415240308910700436L;
	
	private HashMap<String, Object> store=new HashMap<>();
	
	@Override
	public Preferences putBoolean(String key, boolean val) {
		store.put(key, val);
		return this;
	}

	@Override
	public Preferences putInteger(String key, int val) {
		store.put(key, val);
		return this;
	}

	@Override
	public Preferences putLong(String key, long val) {
		store.put(key, val);
		return this;
	}

	@Override
	public Preferences putFloat(String key, float val) {
		store.put(key, val);
		return this;
	}

	@Override
	public Preferences putString(String key, String val) {
		store.put(key, val);
		return this;
	}

	@Override
	public Preferences put(Map<String, ?> vals) {
		store.putAll(vals);
		return this;
	}

	@Override
	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	@Override
	public int getInteger(String key) {
		return getInteger(key, 0);
	}

	@Override
	public long getLong(String key) {
		return getLong(key, 0l);
	}

	@Override
	public float getFloat(String key) {
		return getFloat(key, 0f);
	}

	@Override
	public String getString(String key) {
		return getString(key, null);
	}

	@Override
	public boolean getBoolean(String key, boolean defValue) {
		Object o = store.get(key);
		if (o instanceof Boolean) {
			return (Boolean)store.get(key);
		}
		return defValue;
	}

	@Override
	public int getInteger(String key, int defValue) {
		Object o = store.get(key);
		if (o instanceof Integer) {
			return (Integer)store.get(key);
		}
		return defValue;
	}

	@Override
	public long getLong(String key, long defValue) {
		Object o = store.get(key);
		if (o instanceof Long) {
			return (Long)store.get(key);
		}
		return defValue;
	}

	@Override
	public float getFloat(String key, float defValue) {
		Object o = store.get(key);
		if (o instanceof Float) {
			return (Float)store.get(key);
		}
		return defValue;
	}

	@Override
	public String getString(String key, String defValue) {
		Object o = store.get(key);
		if (o instanceof String) {
			return (String)store.get(key);
		}
		return defValue;
	}

	@Override
	public Map<String, ?> get() {
		return store;
	}

	@Override
	public boolean contains(String key) {
		return store.containsKey(key);
	}

	@Override
	public void clear() {
		store.clear();
	}

	@Override
	public void remove(String key) {
		store.remove(key);
	}

	@Override
	public void flush() {
	}

	public void put(DataBundle data) {
		put(data.get());
	}

}
