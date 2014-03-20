package com.datascan.android.app.testapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
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

import com.datascan.android.app.testapp.util.Counter;
import com.datascan.android.app.testapp.util.LogUtil;
import com.datascan.android.app.testapp.util.ScanHelper;

/**
 * In this class we are going to have three shots and an interval time between
 * each. After shot, analyze the bit data to see if there is something wrong
 * with scan engine
 * 
 * @author yue
 * 
 */
public class BlackLevelActivity extends Activity {

	private ImageView previewImageView;
	private Button skipButton, retryButton;
	private TextView displayTextView;

	private static final String TAG = LogUtil
			.makeLogTag(BlackLevelActivity.class);

	private boolean exiting;
	
	private String exitState;

	// Key code
	private static final int KEY_BOTTOM_SCAN = 190;
	private static final int KEY_TOP_SCAN = 191;

	private ScanHelper scanHelper;

	private boolean doSnapshotFlag;
	private static final int PREPARE_TIME = 1000;

	private boolean retryFlag;

	private void resetState() {
		exiting = false;
		retryFlag = false;
		doSnapshotFlag = true;
		exitState = "";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decode);
		findUI();
		setTitle(R.string.title_blacklevel);
		resetState();
	}

	@Override
	public void onResume() {
		super.onResume();
		scanHelper = new ScanHelper(this);
		// show tutorial
		showHint();
	}

	private void showHint() {
		setDisplayTextView(getString(R.string.hint_black_level));
	}

	@Override
	public void onPause() {
		super.onPause();
		if (scanHelper != null) {
			scanHelper.close();
		}
	}

	private void analyze(int[] data) {
		boolean testResult = true;
		StringBuilder sb = new StringBuilder();
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
			finish();
		} else {
			setDisplayTextView("Failed");
			exitState = getString(R.string.failed);
			Intent i = new Intent();
			sb.insert(0, "failed bits: ");
			i.putExtra(MainActivity.FAIL_REASON, sb.toString());
			setResult(MainActivity.RESULT_FAIL, i);
			runOnUiThread(new Runnable() {
				public void run() {
					showRetryButton();
				}
			});
			onFinish();
		}
	}

	private void setDisplayTextView(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				displayTextView.setText(str);
			}
		});
	}

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

	public void showPreview(byte[] abData) {
		Bitmap bmSnap = BitmapFactory.decodeByteArray(abData, 0, abData.length);
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
				analyze(loadCounter(data));
				Log.e(TAG, "showPreview good");
			}
		}).start();
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
			if (doSnapshotFlag) { // first time scan
				scanHelper.doSnap();
				advancedHint();
			} else { // scan stuck, try manual restart
				scanHelper.restart();
				scanHelper.doSnap();
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void advancedHint() {
		String addHint = getString(R.string.hint_snapshot_advanced);
		setDisplayTextView(addHint);

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
								displayTextView
										.setText(exitState + ", next test coming in "
												+ num);
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

}
