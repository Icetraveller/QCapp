package com.datascan.android.app.testapp.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class writeTimeFile {

	public void writeTime(long currentDateTime) {

		File timeFile = new File("/sdcard/timeFile.txt");

		if (!timeFile.exists()) {
			try {
				timeFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {

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

}
