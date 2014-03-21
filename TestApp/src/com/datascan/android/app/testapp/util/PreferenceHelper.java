package com.datascan.android.app.testapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceHelper {
	public static final String PREF_HOLDER_NAME = "QCApp";
	public static final String PREF_IN_TEST = "in_test";

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
		return sp.getBoolean(PREF_IN_TEST, false);
	}
}
