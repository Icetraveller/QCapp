package com.datascan.android.app.testapp.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class readTimeFile {

	public String readTime() {

		String dateAndTime = null;

		InputStream fileStream;
		try {
			fileStream = new FileInputStream("/sdcard/timeFile.txt");
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

}
