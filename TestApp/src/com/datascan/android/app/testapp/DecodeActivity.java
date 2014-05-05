package com.datascan.android.app.testapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.datascan.android.app.testapp.util.Counter;
import com.datascan.android.app.testapp.util.LogUtil;
import com.datascan.android.app.testapp.util.ScanHelper;
import com.motorolasolutions.adc.decoder.BarCodeReader;

/**
 * The goal of the activity is to check scan engine decode functionality and two
 * physical scan keys. The activity implemets {@link ScanHelper}.
 * {@link CallBacks} to received decode callbacks.
 * 
 * @author yue
 * 
 */
public class DecodeActivity extends Activity implements ScanHelper.CallBacks {

	private ImageView previewImageView;
	private Button skipButton, retryButton;
	private TextView displayTextView;

	// Key code
	private static final int KEY_BOTTOM_SCAN = 190;
	private static final int KEY_TOP_SCAN = 191;

	private boolean doTopScan = true;
	private boolean doBottomScan = true;

	private static final String TAG = LogUtil.makeLogTag(DecodeActivity.class);

	private int currentKeyCode = 0;

	// for result
	private boolean topResult = false;
	private boolean bottomResult = false;

	private boolean retryFlag = false;

	private ScanHelper scanHelper;

	private static int ESCAPE_TIME = 1000;
	private boolean exiting = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decode);
		findUI();
		setTitle(R.string.title_decode);
	}

	@Override
	public void onResume() {
		super.onResume();
		scanHelper = new ScanHelper(this);
		provideHint();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (scanHelper != null) {
			Log.e(TAG, "close");
			scanHelper.close();
		}
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

	/**
	 * start scan process and check which key is under test.
	 * @param keycode The key that invoke scan process
	 */
	public void scan(int keycode) {
		currentKeyCode = keycode;
		if (doTopScan && currentKeyCode == KEY_TOP_SCAN) {
			displayTextView.setText(R.string.processing);
			scanHelper.doDecode();
		}
		if (!doTopScan && doBottomScan && currentKeyCode == KEY_BOTTOM_SCAN) {
			displayTextView.setText(R.string.processing);
			scanHelper.doDecode();
		}
	}

	public void processDecodedData(String msg, int Symbology, int length) {
		// default test result
		boolean decodePassed = false; 
		
		//check length to make sure it's valid decoded data
		if (length <= 0) {
			if (length == BarCodeReader.DECODE_STATUS_MULTI_DEC_COUNT) {
				return;
			} else {// failed
				decodePassed = false;
				showRetryButton();
			}
		} else {
			decodePassed = true;
		}

		/*
		 * Check which scan is this, also prevent second time failed solution
		 * when retry
		 */
		if (currentKeyCode == KEY_TOP_SCAN) {
			doTopScan = false; // mark it's been tested
			if (!topResult) {
				topResult = decodePassed;
			}
		} else if (currentKeyCode == KEY_BOTTOM_SCAN) {
			doBottomScan = false; // mark it's been tested
			if (!bottomResult) {
				bottomResult = decodePassed;
			}
		}
		retryFlag = false;
		provideHint();
		checkComplete();
	}

	private void provideHint() {
		String formatArgs = "";
		if (doTopScan) {
			formatArgs = getString(R.string.top_scan_key);
		} else if (doBottomScan) {
			formatArgs = getString(R.string.bottom_scan_key);
		}
		if (!TextUtils.isEmpty(formatArgs)) {
			String hint = getString(R.string.hint_scan, formatArgs);
			displayTextView.setText(hint);
		}
	}

	private void checkComplete() {

		if (!doTopScan && !topResult) {
			doBottomScan = false;
			setResult(MainActivity.RESULT_FAIL);
			final String resultStr = getString(R.string.failed);
			displayTextView.setText(resultStr);
		} else if (!doBottomScan && !bottomResult) {
			doTopScan = false;
			setResult(MainActivity.RESULT_FAIL);
			final String resultStr = getString(R.string.failed);
			displayTextView.setText(resultStr);
		} else if (!doTopScan && !doBottomScan) {
			setResult(RESULT_OK);
			String resultStr = getString(R.string.passed);
			displayTextView.setText(resultStr);
			finish();
		}
	}

	public void finish() {
		if (retryFlag) {
			setResult(MainActivity.RESULT_RETRY);
		}
		super.finish();
	}

	private void showSkipButton() {
		skipButton.setVisibility(View.VISIBLE);
		skipButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

	private void showRetryButton() {
		retryButton.setVisibility(View.VISIBLE);
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(MainActivity.RESULT_RETRY);
				finish();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KEY_TOP_SCAN:
		case KEY_BOTTOM_SCAN:
			scan(keyCode);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onVideoFrame(byte[] frameData) {
		// ignored, not used

	}

	@Override
	public void onDecodeComplete(String decodeDataString, int symbology,
			int length) {
		processDecodedData(decodeDataString, length, length);

	}

}
