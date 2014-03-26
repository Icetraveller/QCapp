package com.datascan.android.app.testapp;

import java.util.HashMap;
import java.util.List;

import com.datascan.android.app.testapp.util.NetworkHelper;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class WifiActivity extends Activity{
	
	private ImageView previewImageView;
	private Button skipButton, retryButton;
	private TextView displayTextView;
	
	private WifiScanReceiver wifiScanReceiver;
	
	private boolean retryFlag = false;
	private boolean exiting;
	private String exitState;
	private static final int PREPARE_TIME = 1000;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decode);
		findUI();
		setTitle(R.string.title_wifi);
		
		wifiScanReceiver = new WifiScanReceiver();
		registerReceiver(wifiScanReceiver, new IntentFilter("android.net.wifi.SCAN_RESULTS"));
	}
	
	private void findUI() {
		previewImageView = (ImageView) findViewById(R.id.preview_imageview);
		previewImageView.setVisibility(View.INVISIBLE);
		skipButton = (Button) findViewById(R.id.skip_button);
		showSkipButton();
		retryButton = (Button) findViewById(R.id.retry_button);
		retryButton.setVisibility(View.INVISIBLE);
		displayTextView = (TextView) findViewById(R.id.display_textview);
	}
	
	private void showSkipButton() {
		skipButton.setVisibility(View.VISIBLE);
		skipButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				exitState = getString(R.string.skip);
				showRetryButton();
				onFinish();
			}
		});
	}
	
	private void showRetryButton() {
		retryButton.setVisibility(View.VISIBLE);
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				retryFlag = true;
				exitState = getString(R.string.retry);
				finish();
			}
		});
	}
	
	public void onFinish() {
		if (exiting)
			return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				int count = 3;
				exiting = true;
				while (count > 0) {
					count--;
					final int num = count;
					try {
						Thread.sleep(PREPARE_TIME);
						runOnUiThread(new Runnable() {
							public void run() {
								displayTextView.setText(exitState
										+ ", next test coming in " + num);
							}
						});
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
				}
				finish();
			}
		}).start();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		startTest();
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
		displayTextView.setText(R.string.processing);
		NetworkHelper networkHelper = new NetworkHelper(this);
		networkHelper.setWiFi(true);
		networkHelper.startScanWifi();
	}
	
	private void getResult(boolean result){
		if(result){
			setResult(RESULT_OK);
			exitState = getString(R.string.passed);
			finish();
			return;
		}else{
			setResult(MainActivity.RESULT_FAIL);
			exitState = getString(R.string.failed);
			showRetryButton();
			onFinish();
		}
	}
	
	private class WifiScanReceiver extends BroadcastReceiver {
		private WifiManager wifiManager;
		private HashMap<String, String> wifiMap = new HashMap<String, String>();
		
		private Context context;
		private static final String TAG = "WifiScanReceiver";
		
		public void onReceive(Context context, Intent intent) {
			Log.e(TAG,"WIFI Scan receiver");
			this.context = context;
			wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			List<ScanResult> wifiList = wifiManager.getScanResults();
			if(wifiList==null || wifiList.size() <1){
				getResult(false);
			}else{
				getResult(true);
			}
		}
	}
}
