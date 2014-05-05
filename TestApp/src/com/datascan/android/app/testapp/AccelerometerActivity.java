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

/**
 * The goal of the activity is to examine the accelerometer, to see if sensor
 * can reads different values while user move scanner. The activity register
 * sensor manager and listen to accelerometer, once it reach the threshold, the
 * test will pass.
 * @author yue
 * 
 */
public class AccelerometerActivity extends Activity {

	private static final String TAG = LogUtil
			.makeLogTag(AccelerometerActivity.class);

	private ImageView previewImageView;
	private Button skipButton, retryButton, failButton, passButton;
	private TextView displayTextView;
	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private ButtonListener buttonListener;
	private float x = 0;
	private float y = 0;
	private float z = 0;

	private boolean firstRead;

	/** Accuracy configuration */
	private static float threshold = 12.0f;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_light);
		findUI();
		setTitle(R.string.title_accelerometer);
	}

	public void finish() {
		sensorManager.unregisterListener(sensorEventListener);
		super.finish();
	}

	public void onResume() {
		super.onResume();
		init();
		hint();
	}

	/**
	 * register sensor manager and setup listener.
	 */
	private void init() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// sensor is not available
		if (accelerometerSensor == null) {
			setResult(MainActivity.RESULT_FAIL);
			finish();
		}

		sensorManager.registerListener(sensorEventListener,
				accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void hint() {
		displayTextView.setText(R.string.hint_accelerometer);
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

		buttonListener = new ButtonListener();
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

	/**
	 * The listener that listen to events from the accelerometer listener
	 */
	private SensorEventListener sensorEventListener = new SensorEventListener() {
		private long now = 0;
		private long lastUpdate = 0;

		private float lastX = 0;
		private float lastY = 0;
		private float lastZ = 0;
		private float force = 0;

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				now = event.timestamp;

				x = event.values[0];
				y = event.values[1];
				z = event.values[2];

				if (firstRead) {
					lastUpdate = now;
					lastX = x;
					lastY = y;
					lastZ = z;
					firstRead = false;
				} else {
					if (now - lastUpdate > 0) {
						force = Math.abs(x + y + z - lastX - lastY - lastZ);
						if (Float.compare(force, threshold) > 0) {
							Log.e(TAG, "onSensorChanged");
							// change detected, pass test
							setResult(RESULT_OK);
							finish();
						}
					}
				}
			}
		}

	};

}
