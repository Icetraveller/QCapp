package com.datascan.android.app.testapp;

import java.util.ArrayList;

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

	// Key code
	private static final int KEY_BOTTOM_SCAN = 190;
	private static final int KEY_TOP_SCAN = 191;

	private ArrayList<int[]> array;

	private ScanHelper scanHelper;

	private boolean doSnapshotFlag;
	private int snapshotCount = 1;
	private static int REQUIRED_SNAPSHOTS = 2;
	private static final int PREPARE_TIME = 1000;

	private boolean retryFlag;
	private Thread processThread;

	private void resetState() {
		exiting = false;
		retryFlag = false;
		doSnapshotFlag = true;
		snapshotCount = 1;
		if (processThread != null)
			processThread.interrupt();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decode);
		findUI();
		setTitle(R.string.title_blacklevel);
		array = new ArrayList<int[]>();
		resetState();
	}

	@Override
	public void onResume() {
		super.onResume();
		scanHelper = new ScanHelper(this);
		// start process
		process();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (scanHelper != null) {
			scanHelper.close();
		}
	}

	private void process() {
		retryFlag = false;
		processThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (snapshotCount <= REQUIRED_SNAPSHOTS) {
					if (doSnapshotFlag) {
						Log.e(TAG, "process");
						interval();
						scanHelper.doSnap();
						snapshotCount++;
						doSnapshotFlag = false;
					}
				}
				while (true) {
					if (doSnapshotFlag) {
						Log.e(TAG, "anaylize");
						anaylize();
						break;
					}
				}
			}
		});
		processThread.start();
	}

	private void anaylize() {
		boolean testResult = true;
		int[] bits = new int[8];
		int length = array.size();
		if (length <= 0) {
			testResult = false;
			return;
		}
		int[] tempBits;
		for (int i = 0; i < length; i++) {
			tempBits = array.get(i);
			for (int j = 0; j < 8; j++) {
				bits[j] += tempBits[j];
			}
		}
		for (int word : bits) {
			if (word == 0) {
				Log.e("counter", "one bit is zero.");
				testResult = false;
			}
			Log.d("counter", "" + word);
		}
		if (testResult) {
			setDisplayTextView("Passed");
			setResult(RESULT_OK);
			finish();
		} else {
			setDisplayTextView("Failed");
			setResult(RESULT_CANCELED);
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

	private void interval() {
		int count = 3;
		while (count > 0) {
			count--;
			final int num = count;
			try {
				Thread.sleep(PREPARE_TIME);
				setPreivewView(null);
				setDisplayTextView("take picture[" + snapshotCount + "] in: "
						+ num);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
		advancedHint();
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
				array.add(loadCounter(data));
				Log.e(TAG, "showPreview good");
				doSnapshotFlag = true;
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
				finish();
			}
		});
	}

	private String setupBitsOutputString(int index, int bit) {
		String prefix = "Bit[" + index + "]: ";
		if (bit == 752 * 480 || bit == 0) {
			return prefix + "Fail";
		} else {
			return prefix + bit;
		}
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
			if (!doSnapshotFlag) {
				scanHelper.restart();
				scanHelper.doSnap();
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void advancedHint() {
		String originalString = displayTextView.getText().toString();
		String addHint = getString(R.string.hint_snapshot_advanced);
		setDisplayTextView(originalString + "\n" + addHint);

	}

	public void onFinish() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int count = 5;
				exiting = true;
				while (count > 0) {
					count--;
					final int num = count;
					try {
						Thread.sleep(PREPARE_TIME);
						runOnUiThread(new Runnable() {
							public void run() {
								displayTextView
										.setText("Failed, next test coming in "
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
