package com.datascan.android.app.testapp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class DmsgTool {

	public void rebootWrite(int rebootNumber) {

		// int rebootNumber;
		// try opening rebootFile.txt

		try {
			// open rebootFile.txt for writing
			File rebootFile = SaveHelper.getFile(SaveHelper.CATEGORY_DMSGFILE);
			// rebootFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(rebootFile);
			// write the contents of rebootNumber to the file
			int low = rebootNumber % 100;
			int high = rebootNumber / 100;

			fos.write(low);
			fos.write(high);

			// close the file
			fos.close();
			rebootNumber++;

		} catch (java.io.FileNotFoundException e) {

		} catch (IOException e) {

		}

	}
	
	public int rebootReadFile() {

		int timesRebooted = 0;
		// try opening rebootFile.txt

		try {
			File rebootFile = SaveHelper.getFile(SaveHelper.CATEGORY_DMSGFILE);
			InputStream instream = new FileInputStream(rebootFile);

			// prepare the file for reading
			InputStreamReader inputreader = new InputStreamReader(instream);
			BufferedReader buffreader = new BufferedReader(inputreader);

			int low = buffreader.read();
			int high = buffreader.read();
			// y = y << 8;
			timesRebooted = (high * 100) + low;
			// close the file again
			instream.close();

		} catch (java.io.FileNotFoundException e) {

			timesRebooted = -1;

		} catch (IOException e) {

			timesRebooted = -2;
		}

		return timesRebooted;

	}

}