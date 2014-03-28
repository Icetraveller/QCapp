package com.datascan.android.app.testapp;

import com.datascan.android.app.testapp.util.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LightActivity extends Activity {

	private static final String TAG = LogUtil.makeLogTag(LightActivity.class);
	private ImageView previewImageView;
	private Button skipButton, retryButton, failButton, passButton;
	private TextView displayTextView;
	private SensorManager sensorManager;
	private Sensor lightSensor;
	private ButtonListener buttonListener;

	private static float initLightValue = 0;
	private boolean firstRead;

	private SensorEventListener lightSensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
				if (firstRead) {
					initLightValue = event.values[0];
					firstRead = false;
				} else {
					if (initLightValue != event.values[0]) {
						Log.e(TAG, "onSensorChanged");
						// change detected, pass test
						setResult(RESULT_OK);
						finish();
					}
				}

			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		firstRead = true;
		setContentView(R.layout.activity_light);
		findUI();
		setTitle(R.string.title_light);
	}

	public void onResume() {
		super.onResume();
		init();
		hint();
	}

	public void finish() {
		sensorManager.unregisterListener(lightSensorEventListener);
		super.finish();
	}

	private void init() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		//sensor is not available
				if (lightSensor == null) {
					setResult(MainActivity.RESULT_FAIL);
					finish();
				}
		sensorManager.registerListener(lightSensorEventListener, lightSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void hint() {
		displayTextView.setText(R.string.hint_light);
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

	private class ButtonListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
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
			finish();
		}
	}
}
