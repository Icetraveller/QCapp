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

public class DecodeActivity extends Activity {

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

	public void scan(int keycode) {
		currentKeyCode = keycode;
		if (doTopScan && currentKeyCode == KEY_TOP_SCAN) {
			scanHelper.doDecode();
		}
		if (!doTopScan && doBottomScan && currentKeyCode == KEY_BOTTOM_SCAN) {
			scanHelper.doDecode();
		}
	}

	public void showMessage(String msg, int Symbology, int length) {
		boolean decodePassed = false; // if this scan decodes successfully
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
		if (!doTopScan && !doBottomScan) {
			if (topResult && bottomResult) {
				setResult(RESULT_OK);
				String resultStr = getString(R.string.passed);
				displayTextView.setText(resultStr);
				finish();
			} else {
				setResult(RESULT_CANCELED);
				final String resultStr = getString(R.string.failed);
				if(exiting == true){
					return;
				}
				new Thread(new Runnable(){
					@Override
					public void run() {
						int count = 5;
						exiting = true;
						while (count > 0) {
							count--;
							final int num = count;
							try {
								Thread.sleep(ESCAPE_TIME);
								runOnUiThread(new Runnable() {
									public void run() {
										displayTextView.setText(resultStr
												+ ", next test coming in " + num);
									}
								});
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								continue;
							}
							if (retryFlag) {
								break;
							}
						}
						finish();
					}}).start();
			}
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
				if (doTopScan) {
					topResult = false;
					doTopScan = false;
					currentKeyCode = KEY_TOP_SCAN;
					showRetryButton();
				} else if (doBottomScan) {
					bottomResult = false;
					doBottomScan = false;
					currentKeyCode = KEY_BOTTOM_SCAN;
					showRetryButton();
				} else {
					skipButton.setVisibility(View.INVISIBLE);
				}

				provideHint();
				checkComplete();
			}
		});
	}

	private void showRetryButton() {
		retryButton.setVisibility(View.VISIBLE);
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (!doBottomScan) {
//					currentKeyCode = KEY_BOTTOM_SCAN;
//					doBottomScan = true;
//				} else if (!doTopScan) {
//					currentKeyCode = KEY_TOP_SCAN;
//					doTopScan = true;
//				} else {
//					retryButton.setVisibility(View.INVISIBLE);
//				}
//				retryFlag = true;
//				provideHint();
				retryFlag = true;
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

}
