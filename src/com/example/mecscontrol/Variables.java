package com.example.mecscontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Variables {
	public static final Variables INSTANCE = new Variables();

	private String IpAddress;
	private int volume;

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;

	}

	private SharedPreferences sp = null;
	private SharedPreferences.Editor editor;

	public String getIpAddress() {
		return IpAddress;
	}

	public void setIpAddress(String ipAddress) {
		IpAddress = ipAddress;
	}

	public void writeVolumetoSettings(Context context) {
		if (sp == null) {
			sp = PreferenceManager.getDefaultSharedPreferences(context);
			editor = sp.edit();
		}
		editor.putInt("volume", volume);
		editor.commit();
	}

	public void readFromSettings(Context context) {
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		editor = sp.edit();
		IpAddress = sp.getString("address", "192.168.1.119");
	}

	public void readVolumeFromSettings(Context context) {
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		editor = sp.edit();
		volume = sp.getInt("volume", 16);
	}
}
