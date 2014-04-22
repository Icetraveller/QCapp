package com.datascan.android.app.testapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class PreferenceHelper {
	public static final String PREF_HOLDER_NAME = "QCApp";
	public static final String PREF_IN_TEST = "in_test";
	public static final String PREF_BOOT_RECEVIER = "boot_receiver";

	public static void setTesting(Context context, boolean isTesting) {
		SharedPreferences sp = context.getSharedPreferences(PREF_HOLDER_NAME,
				Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putBoolean(PREF_IN_TEST, isTesting);
		edit.apply();
	}

	public static boolean isTesting(Context context) {
		SharedPreferences sp = context.getSharedPreferences(PREF_HOLDER_NAME,
				Context.MODE_PRIVATE);
		Log.e("PreferenceHelper","In Test is "+sp.getBoolean(PREF_IN_TEST, false));
		return sp.getBoolean(PREF_IN_TEST, false);
	}
	public static void setOnBoot(Context context, boolean isOnBoot) {
		SharedPreferences sp = context.getSharedPreferences(PREF_HOLDER_NAME,
				Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putBoolean(PREF_BOOT_RECEVIER, isOnBoot);
		edit.apply();
	}
	
	public static boolean isOnBoot(Context context) {
		SharedPreferences sp = context.getSharedPreferences(PREF_HOLDER_NAME,
				Context.MODE_PRIVATE);
		Log.e("PreferenceHelper","on Boot is "+sp.getBoolean(PREF_BOOT_RECEVIER, false));
		return sp.getBoolean(PREF_BOOT_RECEVIER, false);
	}
}
