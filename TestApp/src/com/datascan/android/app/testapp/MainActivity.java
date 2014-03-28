package com.datascan.android.app.testapp;

import com.datascan.android.app.testapp.util.LogUtil;
import com.datascan.android.app.testapp.util.PreferenceHelper;
import com.datascan.android.app.testapp.util.RTCTool;
import com.datascan.android.app.testapp.util.SaveHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = LogUtil.makeLogTag(MainActivity.class);

	private SparseBooleanArray doTest = new SparseBooleanArray();
	private SparseBooleanArray passTest = new SparseBooleanArray();

	private static final int DECODE_TEST_REQUEST = 0;
	private static final int BLACKLEVEL_TEST_REQUEST = 1;
	private static final int WIFI_TEST_REQUEST = 2;
	private static final int RTC_TEST_REQUEST = 3;
	private static final int ACCELEROMETER_TEST_REQUEST = 4;
	private static final int LIGHT_TEST_REQUEST = 5;
	private static final int LED_TEST_REQUEST = 6;
	private static final int DMSG_TEST_REQUEST = 7;
	private static final int TEST_NUM = 8;

	public static final int RESULT_RETRY = -100;
	public static final int RESULT_FAIL = -101;
	public static final String FAIL_REASON = "fail_reason";

	private TextView resultTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		resultTextView = (TextView) findViewById(R.id.result);
		Log.e(TAG, "" + PreferenceHelper.isTesting(this));
		if (PreferenceHelper.isTesting(this)) {
			Log.e(TAG, "load");
			String report = SaveHelper.loadReport();
			resultTextView.setText(report);
			SparseBooleanArray savedDoTest = SaveHelper.loadSavedDoTest();
			if (savedDoTest != null) {
				doTest = savedDoTest;
			}
			SparseBooleanArray savedPassTest = SaveHelper.loadSavedPassTest();
			if (savedPassTest != null) {
				passTest = savedPassTest;
			}
		}
	}

	private void init() {
		for (int i = 0; i < TEST_NUM; i++) {
			doTest.put(i, true);
			passTest.put(i, false);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		unlockScreenAndKeepOn();
		Log.e(TAG, "Main onResume");
		testProcess();
		updateResult();
		
	}
	
	private void unlockScreenAndKeepOn(){
		getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);  
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    switch(item.getItemId()){
	    case R.id.action_deletefiles:
	    	SaveHelper.deleteFiles();
	    	onRestart();
	        return true;            
	    }
	    return false;
	}

	private void updateResult() {
		StringBuilder sb = new StringBuilder();
		int key = 0;
		for (int i = 0; i < passTest.size(); i++) {
			key = passTest.keyAt(i);
			// get the object by the key.
			Boolean flag = (Boolean) passTest.get(key);
			String name = getTestName(key);
			sb.append(name).append(" ");
			if (flag) {
				sb.append("\t Passed");
			} else {
				sb.append("\t Failed");
			}
			sb.append("\n");
		}
		resultTextView.setText(sb.toString());
	}

	private String getTestName(int category) {
		String name = "";
		switch (category) {
		case DECODE_TEST_REQUEST:
			name = "Decode Test";
			break;
		case BLACKLEVEL_TEST_REQUEST:
			name = "Black Level Test";
			break;
		case WIFI_TEST_REQUEST:
			name = "Wifi Test";
			break;
		case RTC_TEST_REQUEST:
			name = "RTC Test";
			break;
		case LED_TEST_REQUEST:
			name = "LED Test";
			break;
		case ACCELEROMETER_TEST_REQUEST:
			name = "Accelerometer Test";
			break;
		case LIGHT_TEST_REQUEST:
			name = "Light Test";
			break;
		case DMSG_TEST_REQUEST:
			name = "Dmesg Troubleshooter";
			break;
		}
		return name;
	}

	/**
	 * Test process will pick a test from top list until it's mark skipped or
	 * failed. When rtc is reached, all reports and status should be save on sd
	 * card.
	 */
	public void testProcess() {
		// for test only
		// if (doTest.get(DMSG_TEST_REQUEST)) {
		// String report = resultTextView.getText().toString();
		// SaveHelper.save(report, doTest, passTest);
		// PreferenceHelper.setTesting(this, true);
		// Intent intent = new Intent(this, DmsgActivity.class);
		// startActivityForResult(intent, DMSG_TEST_REQUEST);
		// }
		Log.e(TAG, "" + PreferenceHelper.isTesting(this));
		if (doTest.get(DECODE_TEST_REQUEST)) {
			Intent intent = new Intent(this, DecodeActivity.class);
			startActivityForResult(intent, DECODE_TEST_REQUEST);
		} else if (doTest.get(BLACKLEVEL_TEST_REQUEST)) {
			Intent intent = new Intent(this, BlackLevelActivity.class);
			startActivityForResult(intent, BLACKLEVEL_TEST_REQUEST);
		} else if (doTest.get(LED_TEST_REQUEST)) {
			Intent intent = new Intent(this, LEDActivity.class);
			startActivityForResult(intent, LED_TEST_REQUEST);
		} else if (doTest.get(ACCELEROMETER_TEST_REQUEST)) {
			Intent intent = new Intent(this, AccelerometerActivity.class);
			startActivityForResult(intent, ACCELEROMETER_TEST_REQUEST);
		} else if (doTest.get(WIFI_TEST_REQUEST)) {
			Intent intent = new Intent(this, WifiActivity.class);
			startActivityForResult(intent, WIFI_TEST_REQUEST);
		} else if (doTest.get(LIGHT_TEST_REQUEST)) {
			Intent intent = new Intent(this, LightActivity.class);
			startActivityForResult(intent, LIGHT_TEST_REQUEST);
		} else if (doTest.get(RTC_TEST_REQUEST)) {
			Log.e(TAG, "save");
			String report = resultTextView.getText().toString();
			SaveHelper.save(report, doTest, passTest);
			PreferenceHelper.setTesting(this, true);
			Intent intent = new Intent(this, RTCActivity.class);
			startActivityForResult(intent, RTC_TEST_REQUEST);
		} else if (doTest.get(DMSG_TEST_REQUEST)) {
			Log.e(TAG, "save");
			String report = resultTextView.getText().toString();
			SaveHelper.save(report, doTest, passTest);
			PreferenceHelper.setTesting(this, true);
			Intent intent = new Intent(this, DmsgActivity.class);
			startActivityForResult(intent, DMSG_TEST_REQUEST);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "onActivityResult resultCode= " + resultCode
				+ " requestCode= " + requestCode);
		if (requestCode == RTC_TEST_REQUEST || requestCode == DMSG_TEST_REQUEST) {
			PreferenceHelper.setTesting(this, false);
			Log.e(TAG, "quit RTC or Dmsg");
		}
		if (resultCode == RESULT_OK) { // passed the test
			doTest.put(requestCode, false);
			passTest.put(requestCode, true);
		}
		if (resultCode == RESULT_CANCELED) { // failed the test
			doTest.put(requestCode, false);
			passTest.put(requestCode, false);
		}
		if (resultCode == RESULT_RETRY) {// request retry
			doTest.put(requestCode, true);
			passTest.put(requestCode, false);
		}
		if (resultCode == RESULT_FAIL) {
			doTest.put(requestCode, false);
			passTest.put(requestCode, false);
			if (data != null) {
				String reasonString = data.getStringExtra(FAIL_REASON);
			}
		}
	}

}
