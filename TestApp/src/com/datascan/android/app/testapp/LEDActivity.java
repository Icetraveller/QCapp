package com.datascan.android.app.testapp;

import com.datascan.android.app.testapp.util.LogUtil;

import android.app.Activity;
import android.app.LEDManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LEDActivity extends Activity {
	
	private static final String TAG = LogUtil.makeLogTag(LEDActivity.class);

	private ImageView previewImageView;
	private Button skipButton, retryButton,failButton, passButton;
	private TextView displayTextView;
	private LEDManager lm;
	private ButtonListener buttonListener = new ButtonListener();
	
	private boolean flashFlag = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_led);
		findUI();
		setTitle(R.string.title_led);
	}

	public void onResume() {
		super.onResume();
		hint();
		flashLED();
	}

	private void findUI() {
		previewImageView = (ImageView) findViewById(R.id.preview_imageview);
		previewImageView.setVisibility(View.INVISIBLE);
		skipButton = (Button) findViewById(R.id.skip_button);
		retryButton = (Button) findViewById(R.id.retry_button);
		retryButton.setVisibility(View.INVISIBLE);
		passButton = (Button) findViewById(R.id.pass_button);
		failButton = (Button) findViewById(R.id.fail_button);
		displayTextView = (TextView) findViewById(R.id.display_textview);
		
		skipButton.setOnClickListener(buttonListener);
		retryButton.setOnClickListener(buttonListener);
		passButton.setOnClickListener(buttonListener);
		failButton.setOnClickListener(buttonListener);
	}
	
	private void hint(){
		displayTextView.setText(R.string.hint_led);
	}

	private void flashLED() {
		lm = (LEDManager) getSystemService("led");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int count = 0;
				while (flashFlag) {
					if (count % 2 == 0) {
						lm.setRedLED(true);
						lm.setGreenLED(false);
					} else {
						lm.setRedLED(false);
						lm.setGreenLED(true);
					}
					count++;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
				}
			}
		}).start();
	}
	
	private class ButtonListener implements OnClickListener{
		@Override
		public void onClick(View view) {
			switch(view.getId()){
			case R.id.fail_button:
				setResult(MainActivity.RESULT_FAIL);
				break;
			case R.id.pass_button:
				setResult(RESULT_OK);
				break;
			case R.id.skip_button:
				setResult(RESULT_CANCELED);
				break;
			case R.id.retry_button:
				setResult(MainActivity.RESULT_RETRY);
				break;
			}
			flashFlag = false;
			finish();
		}
	}

}
