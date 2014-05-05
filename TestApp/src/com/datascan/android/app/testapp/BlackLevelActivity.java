package com.datascan.android.app.testapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.datascan.android.app.testapp.util.Counter;
import com.datascan.android.app.testapp.util.LogUtil;
import com.datascan.android.app.testapp.util.ScanHelper;

/**
 * The goal of the activity is to examine the scanner by checking last 4 bits of
 * video frame. The activity implements {@link ScanHelper.CallBacks} and will
 * receive the first non-empty video frame and do analysis.
 * 
 * @author yue
 * 
 */
public class BlackLevelActivity extends Activity implements
		ScanHelper.CallBacks {

	private ImageView previewImageView;
	private Button skipButton, retryButton;
	private TextView displayTextView;

	private static final String TAG = LogUtil
			.makeLogTag(BlackLevelActivity.class);

	private String exitState; // display current state when exiting.
	private boolean enablePress = true; // prevent user multi-press

	// Key code
	private static final int KEY_BOTTOM_SCAN = 190;
	private static final int KEY_TOP_SCAN = 191;

	private ScanHelper scanHelper;

	private boolean inProcess = false;

	private boolean retryFlag; // whether to do a retry.

	/**
	 * Reset key state values
	 */
	private void resetState() {
		retryFlag = false;
		exitState = "";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blacklevel);
		findUI();
		setTitle(R.string.title_blacklevel);
		resetState();

	}

	@Override
	public void onResume() {
		super.onResume();
		scanHelper = new ScanHelper(this);
		init();
		// show user hint
		showHint();
	}

	private void init() {
		setDisplayTextView(getString(R.string.initializing));
	}

	private void showHint() {
		setDisplayTextView(getString(R.string.hint_black_level));
		try {
			Bitmap bmSnap = BitmapFactory.decodeResource(getResources(),
					R.drawable.hint_blacklevel);
			if (bmSnap == null) {
				return;
			}
			previewImageView.setImageBitmap(bmSnap);
			previewImageView.setVisibility(View.VISIBLE);
		} catch (NotFoundException e) {
			previewImageView.setImageBitmap(null);
			previewImageView.setVisibility(View.INVISIBLE);
		}

	}

	/**
	 * 
	 * @param data
	 *            int array of {@link Counter}, contain the processed video
	 *            frame data.
	 * @return result of test
	 */
	private boolean analyze(int[] data) {
		boolean testResult = true;
		StringBuilder sb = new StringBuilder();
		// we only care the last for bits
		for (int j = 4; j < 8; j++) {
			if (data[j] == 752 * 480) {
				testResult = false;
				sb.append(j).append(" ");
			}
		}
		if (testResult) {
			setDisplayTextView("Passed");
			exitState = getString(R.string.passed);
			setResult(RESULT_OK);
			return true;
		} else {
			setDisplayTextView("Failed");
			exitState = getString(R.string.failed);
			setDisplayTextView("Fail:\n failed bits:" + sb.toString());
			Intent i = new Intent();
			sb.insert(0, "failed bits: ");
			i.putExtra(MainActivity.FAIL_REASON, sb.toString());
			setResult(MainActivity.RESULT_FAIL, i);
			runOnUiThread(new Runnable() {
				public void run() {
					showRetryButton();
				}
			});
			return false;
		}
	}

	private void setDisplayTextView(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				displayTextView.setText(str);
			}
		});
	}

	/**
	 * UI Helper
	 * 
	 * @param bitmap
	 *            the image to show on UI thread
	 */
	private void setPreivewView(final Bitmap bitmap) {
		if (bitmap == null) {
			runOnUiThread(new Runnable() {
				public void run() {
					previewImageView.setImageBitmap(null);
					previewImageView.setVisibility(View.INVISIBLE);
				}
			});
		} else {
			runOnUiThread(new Runnable() {
				public void run() {
					previewImageView.setImageBitmap(bitmap);
					previewImageView.setVisibility(View.VISIBLE);
				}
			});
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
	 * Called on received video frame data from scan helper. Start a new thread
	 * to process video frame.
	 * 
	 * @param abDataa
	 */
	public void processVideoFrame(byte[] abDataa) {
		final byte[] abData = abDataa;
		Bitmap bmSnap = BitmapFactory.decodeByteArray(abData, 0, abData.length);
		Log.e(TAG,
				"" + previewImageView.getWidth() + " "
						+ previewImageView.getHeight());
		bmSnap = Bitmap.createScaledBitmap(bmSnap, previewImageView.getWidth(),
				previewImageView.getHeight(), false);
		if (bmSnap == null) {
			return;
		}
		final Bitmap bitmapImage = bmSnap;
		final byte[] data = abData;
		new Thread(new Runnable() {
			@Override
			public void run() {
				setPreivewView(bitmapImage);
				setDisplayTextView(getString(R.string.processing));
				Log.e(TAG, "showPreview good");
				boolean result = analyze(loadCounter(data));
				if (result) {
					finish();
				}
			}
		}).start();
		inProcess = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (scanHelper != null) {
			Log.e(TAG, "close");
			scanHelper.close();
		}
	}

	/**
	 * Before finish, release all resource if necessary and check retry flag.
	 */
	public void finish() {
		try {
			previewImageView = null;
			if (retryFlag) {
				setResult(MainActivity.RESULT_RETRY);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			super.finish();
		}

	}

	private void showSkipButton() {
		skipButton.setVisibility(View.VISIBLE);
		skipButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				exitState = getString(R.string.skip);
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

	/**
	 * Prepare counter and video frame data.
	 * 
	 * @param abData
	 *            Video frame data
	 * @return int array of each bit of the frame
	 */
	private int[] loadCounter(byte[] abData) {
		Counter counter = new Counter(abData);
		counter.count();
		return counter.getBits();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KEY_TOP_SCAN:
		case KEY_BOTTOM_SCAN:
			if (!inProcess) {
				inProcess = true;
				setPreivewView(null);
				scanHelper.doVideo();
				advancedHint();
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void advancedHint() {
		String addHint = getString(R.string.hint_snapshot_advanced);
		setDisplayTextView(addHint);
	}

	@Override
	public void onVideoFrame(byte[] frameData) {
		processVideoFrame(frameData);
	}

	@Override
	public void onDecodeComplete(String decodeDataString, int symbology,
			int length) {
		// ignored in this activity
	}

}
