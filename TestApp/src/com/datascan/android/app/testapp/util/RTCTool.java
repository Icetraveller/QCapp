package com.datascan.android.app.testapp.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

public class RTCTool {

	public String readTime() {

		String dateAndTime = null;

		InputStream fileStream;
		try {
			fileStream = new FileInputStream(SaveHelper.getFile(SaveHelper.CATEGORY_TIMEFILE));
			InputStreamReader inputreader = new InputStreamReader(fileStream);
			BufferedReader buffreader = new BufferedReader(inputreader);

			try {
				dateAndTime = buffreader.readLine();
				buffreader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return dateAndTime;

	}
	
	public void writeTime(long currentDateTime) {

		File timeFile = SaveHelper.getFile(SaveHelper.CATEGORY_TIMEFILE);

		if (!timeFile.exists()) {
			Log.e("RTC","create time file");
			try {
				timeFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("RTC",e.getMessage());
			}
		}

		try {
			Log.e("RTC","write time file");
			FileWriter logWriter = new FileWriter(timeFile);
			BufferedWriter outer = new BufferedWriter(logWriter);
			// FileOutputStream fos = new FileOutputStream(timeFile);
			outer.write(String.valueOf(currentDateTime));
			// fos.write(currentDateTime.getBytes());

			if (outer != null) {
				outer.flush();
				outer.close();

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void deleteTimeFile(){
		File timeFile = SaveHelper.getFile(SaveHelper.CATEGORY_TIMEFILE);
		if (!timeFile.exists()) {
			timeFile.delete();
		}
	}

}
