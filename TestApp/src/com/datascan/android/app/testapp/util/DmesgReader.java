package com.datascan.android.app.testapp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DmesgReader {
	
	public int dmesgReadFile() {
		// $$$$$$$$$$$$$$$$$$This opens the file dmesg.txt and looks for
		// "Exception"$$$$$$$$$$$$$$$$$$$$$$$$$$$$
		int timesFailed = 0;

		// try opening dmesg.txt
		try {
			File dmesgFile = new File("/data/dmessage.txt");
			InputStream instream = new FileInputStream(dmesgFile);

			// prepare the file for reading
			InputStreamReader inputreader = new InputStreamReader(instream);
			BufferedReader buffreader = new BufferedReader(inputreader);

			String line = buffreader.readLine();
			

			// read every line of the file into the line-variable, on line at
			// the time
			while (line != null) {

				String test = "exception";
				Boolean containLine = line.contains(test);

				if (containLine == true) {
					timesFailed++;
				}

				line = buffreader.readLine();

			}
			
			// close the file again
			instream.close();

		} catch (java.io.FileNotFoundException e) {

			timesFailed = -1;

		} catch (IOException e) {

			timesFailed = -2;
		}

		return timesFailed;
		

	}
}
