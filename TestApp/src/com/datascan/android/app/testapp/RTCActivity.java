package com.datascan.android.app.testapp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.datascan.android.app.testapp.util.readTimeFile;
import com.datascan.android.app.testapp.util.writeTimeFile;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RTCActivity extends Activity {

	void dateAndTime() {
		File timeFile = new File("/sdcard/timeFile.txt");

		if (!timeFile.exists()) {

			try {
				timeFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TextView tv2 = (TextView) findViewById(R.id.textView2);
			TextView tv3 = (TextView) findViewById(R.id.textView3);
			Calendar rightNow = Calendar.getInstance();

			// Get date and time in milliseconds for writing to file and for
			// date/time comparison
			long currentTime = rightNow.getTimeInMillis();

			writeTimeFile fileWrite = new writeTimeFile();
			fileWrite.writeTime(currentTime);

			readTimeFile dateAndTime = new readTimeFile();
			String checkFileContents = dateAndTime.readTime();

			// Displays a message if timeFile.txt is empty. There should always
			// be something in that file if its present
			if (checkFileContents == null) {
				tv3.setText(getResources().getString(R.string.oops));

			}

			WifiManager wifi = (WifiManager) this
					.getSystemService(Context.WIFI_SERVICE);
			wifi.setWifiEnabled(false);

			SimpleDateFormat df3 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
			String formattedDate3 = df3.format(rightNow.getTime());

			// Dialog box for the ability to power down the scanner
			Builder builder = new AlertDialog.Builder(RTCActivity.this);
			AlertDialog dialog = builder.create();
			dialog.setTitle("Current Date/Time: " + formattedDate3);
			dialog.setMessage("Press \"Power Down\". Once scanner has powered off, remove batteries, wait at least 10 seconds, then place batteries back into scanner and reboot scanner.");
			dialog.setButton("Power Down", listenerAccept);

			dialog.setCancelable(false);
			dialog.show();

		} else {
			TextView tv2 = (TextView) findViewById(R.id.textView2);
			TextView tv3 = (TextView) findViewById(R.id.textView3);

			readTimeFile dateAndTime = new readTimeFile();
			String dateAndTime1 = dateAndTime.readTime();

			long readDateAndTime = Long.valueOf(dateAndTime1).longValue();

			Calendar rightNow = Calendar.getInstance();
			long currentTime = rightNow.getTimeInMillis();

			if (currentTime < readDateAndTime) {
				SimpleDateFormat df3 = new SimpleDateFormat(
						"dd-MM-yyyy HH:mm:ss a");
				String formattedDate3 = df3.format(rightNow.getTime());

				tv2.setText(String.valueOf(formattedDate3));
				tv3.setText("RTC TEST FAILED");
			} else {
				SimpleDateFormat df3 = new SimpleDateFormat(
						"dd-MM-yyyy HH:mm:ss a");
				String formattedDate3 = df3.format(rightNow.getTime());

				tv2.setText(String.valueOf(formattedDate3));
				tv3.setText("RTC TEST PASSED");
			}

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rtc);
		addListenerOnButton();

		TextView tv3 = (TextView) findViewById(R.id.textView3);
		readTimeFile dateAndTime = new readTimeFile();
		String checkFileContents = dateAndTime.readTime();

		File timeFile = new File("/sdcard/timeFile.txt");

		if (timeFile.exists() && checkFileContents == null) {
			// Displays a message on the screen when something goes wrong
			tv3.setText(getResources().getString(R.string.oops));

		} else {
			dateAndTime();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void addListenerOnButton() {
		Button button;
		final Context context = this;
		button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				File timeFile = new File("/sdcard/timeFile.txt");
				timeFile.delete();
				dateAndTime();
				// Intent intent = new Intent(context, MainActivity.class);
				// startActivity(intent);

			}

		});

	}

	// OnClickLister for the pop up button
	DialogInterface.OnClickListener listenerAccept = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			sleepScanner();
			TextView tv3 = (TextView) findViewById(R.id.textView3);
			tv3.setText("");
			// Toast.makeText(MainActivity.this, "Great! Welcome.",
			// Toast.LENGTH_SHORT).show();
		}
	};

	// Method for sleeping the scanner
	void sleepScanner() {
		try {
			PowerManager pwrMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
			pwrMgr.goToSleep(10000000);
		} catch (Exception ex) {

		}
	}

}
