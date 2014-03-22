package com.datascan.android.app.testapp;

import com.datascan.android.app.testapp.util.WifiScanReceiver;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;

public class WifiActivity extends Activity{
	
	private WifiScanReceiver wifiScanReceiver;
	
	private boolean retryFlag = false;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		wifiScanReceiver = new WifiScanReceiver();
		registerReceiver(wifiScanReceiver, new IntentFilter("android.net.wifi.SCAN_RESULTS"));
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	protected void onPause(){
		
	}
	
	public void finish() {
		if (retryFlag) {
			setResult(MainActivity.RESULT_RETRY);
		}
		
		unregisterReceiver(wifiScanReceiver);
		super.finish();
	}
	
	/**
	 * Start the process of test. The test strategy is:
	 * Scanning wifi in an environment where there are wifi access point.
	 */
	private void startTest(){
		
	}
}
