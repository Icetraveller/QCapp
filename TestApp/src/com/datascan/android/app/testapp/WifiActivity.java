package com.datascan.android.app.testapp;

import java.util.HashMap;
import java.util.List;

import com.datascan.android.app.testapp.util.LogUtil;
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
import android.widget.Toast;

/**
 * The goal of the activity is to examine wifi module of the scanner by enabling
 * wifi and getting a non-empty scanned result list.
 * 
 * @author yue
 * 
 */
public class WifiActivity extends Activity {

	private static final String TAG = LogUtil.makeLogTag(WifiActivity.class);

	private ImageView previewImageView;
	private Button skipButton, retryButton;
	private TextView displayTextView;

	private WifiScanReceiver wifiScanReceiver;

	private boolean retryFlag = false;
	private boolean exiting;
	private String exitState;
	private static final int PREPARE_TIME = 1000;

	private int scanTimeOut = 2 * 30;
	private int scanTimeCount = 0;
	private boolean getScanResult = false;
	private boolean breakFlag = false; // stop two test threads

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decode);
		findUI();
		setTitle(R.string.title_wifi);
		wifiScanReceiver = new WifiScanReceiver(); // init receiver
		registerReceiver(wifiScanReceiver, new IntentFilter(
				"android.net.wifi.SCAN_RESULTS")); // register intent
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
				finish();
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

	@Override
	protected void onResume() {
		super.onResume();
		startTest();
	}

	public void finish() {
		if (retryFlag) {
			setResult(MainActivity.RESULT_RETRY);
		}
		try {
			unregisterReceiver(wifiScanReceiver); // unregister receiver
		} catch (IllegalArgumentException e) {
			// do nothing, because the receiver is not registered or already
			// been unregistered.
		}
		super.finish();
	}

	private void updateDisplay(final int count, final String text) {
		if (count % 2 != 0)
			return;
		runOnUiThread(new Runnable() {
			public void run() {
				displayTextView.setText(text + ", " + count / 2);
			}
		});
	}

	private void updateDisplay(final String text) {
		runOnUiThread(new Runnable() {
			public void run() {
				displayTextView.setText(text);
			}
		});
	}

	/**
	 * Start the process of test. Assume the scanner is under the environment
	 * that wifi AP is visible. Strategy is 1)enable wifi(turn on wifi) in a
	 * limit time. Bad wifi module will encounter problem and unable to turn on
	 * wifi. 2) scan wifi AP in a limit time. Bad wifi module will not be able
	 * to get non-empty result list.
	 */
	private void startTest() {
		displayTextView.setText(R.string.processing);
		new Thread(new Runnable() {
			@Override
			public void run() {
				int timeOut = 2 * 10;
				int timeCount = 0;
				NetworkHelper networkHelper = new NetworkHelper(
						getApplicationContext());
				int enabled = -1;
				networkHelper.setWiFi(true);
				Log.e(TAG, "start enable");
				while (!breakFlag) {
					if (timeCount >= timeOut) {// timeout
						Log.e(TAG, "enable timeout");
						setResult(MainActivity.RESULT_FAIL);
						updateDisplay("Fail:\n failed to enable WIFI");
						// finish();
						return;
					}
					Log.e(TAG, "see if enabled");
					enabled = networkHelper.getWifiStatus();
					if (enabled == WifiManager.WIFI_STATE_UNKNOWN) { // can't
																		// enable
						Log.e(TAG, "enable error");
						setResult(MainActivity.RESULT_FAIL);
						updateDisplay("Fail:\n unknown WIFI status");
						// finish();
						return;
					} else if (enabled == WifiManager.WIFI_STATE_ENABLED) { // enabled
																			// wifi
																			// successfully
						break;
					}
					try {
						Thread.sleep(500);
						updateDisplay(timeCount,
								getString(R.string.initializing));
						timeCount++;
					} catch (InterruptedException e) {
						e.printStackTrace();
						continue;
					}
				}
				Log.e(TAG, "start scan");
				networkHelper.startScanWifi();
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (!breakFlag) {
							if (scanTimeCount >= scanTimeOut) { // enabled wifi,
																// but scan
																// timeout
								setResult(MainActivity.RESULT_FAIL);
								updateDisplay("Fail:\n Timeout on getting scan result");
								// finish();
								return;
							}
							if (getScanResult) { // successfully get result
								return;
							}
							try {
								updateDisplay(scanTimeCount,
										getString(R.string.scanning));
								Thread.sleep(500);
								scanTimeCount++;
							} catch (InterruptedException e) {
								e.printStackTrace();
								continue;
							}
						}
					}
				}).start();
			}
		}).start();
	}

	private void getResult(boolean result) {
		if (result) {
			setResult(RESULT_OK);
			exitState = getString(R.string.passed);
			finish();
			return;
		} else {
			setResult(MainActivity.RESULT_FAIL);
			exitState = getString(R.string.failed);
			showRetryButton();
			// onFinish();
		}
	}

	/**
	 * Wifi scan recevier to receive wifi scan result list from action of
	 * ScanWifi().
	 * @author yue
	 * 
	 */
	private class WifiScanReceiver extends BroadcastReceiver {
		private WifiManager wifiManager;

		private static final String TAG = "WifiScanReceiver";

		public void onReceive(Context context, Intent intent) {
			Log.e(TAG, "WIFI Scan receiver");
			getScanResult = true;
			wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			List<ScanResult> wifiList = wifiManager.getScanResults();
			if (wifiList == null || wifiList.size() < 1) {
				getResult(false);
			} else {
				getResult(true);
			}
		}
	}
}
