package com.datascan.android.app.testapp;

import android.app.Activity;
import android.os.Bundle;

public class WifiActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	protected void onPause(){
		
	}
	
	/**
	 * Start the process of test. The test strategy is:
	 * Scanning wifi in an environment where there are wifi access point.
	 */
	private void startTest(){
		
	}
}
