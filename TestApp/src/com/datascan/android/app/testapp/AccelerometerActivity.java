package com.datascan.android.app.testapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

public class AccelerometerActivity extends Activity {
	
	boolean accelerometerAvailable = false;
	
	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
	}
	
	private void init() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		//sensor is not available
		if (accelerometerSensor == null) {
			setResult(MainActivity.RESULT_FAIL);
			finish();
		}
	}

}
