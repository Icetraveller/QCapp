package com.datascan.android.app.testapp.util;

import com.datascan.android.app.testapp.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver{
	private static final String TAG = LogUtil.makeLogTag(BootReceiver.class);
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean active = PreferenceHelper.isOnBoot(context);
		Log.e(TAG,"In Test is "+active);
		if(active){
			Intent mainIntent = new Intent(context.getApplicationContext(), MainActivity.class);
			mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			context.startActivity(mainIntent);
		}
		
	}

}
