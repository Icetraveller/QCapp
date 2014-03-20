package com.datascan.android.app.testapp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;

/**
 * This class provide methods to save and load previous settings in internal
 * storage/batteryTestApp folder
 * 
 * @author yue
 * 
 */
public class SaveHelper {
	private static final String TAG = LogUtil.makeLogTag(SaveHelper.class);
	private static final String SAVEFILES_PATH = "DSTestApp";
	private static final String SAVEFILES_NAME = "SavedProgress";

	/**
	 * Constructor
	 * 
	 * @param contex
	 *            caller's activity
	 */
	public SaveHelper(Context contex) {
	}
	
	public static void save(){
		
	}

	/**
	 * This method mainly to save progress files to internal storage.
	 */

	private static void write(int category, String outString) {
		try {
			File f = getFile(category);
			FileOutputStream fOut = new FileOutputStream(f);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(outString);
			myOutWriter.close();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static File getFile(int category){
		File dir = new File(
				android.os.Environment.getExternalStorageDirectory(),
				SAVEFILES_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File f = null;
		f = new File(dir + File.separator
				+ SAVEFILES_NAME);
		if (f != null) {
			return f;
		}else{
			return null;
		}
	}

	/**
	 * This method mainly to load strategy files from internal storage. It reads
	 * strategy data from files and save it to strategy data structure in
	 * sharedbox
	 */
	public static void load() {
		try {
			File dir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					SAVEFILES_PATH);
			if (!dir.exists()) {
				return;
			}
			File savedFile = new File(dir + File.separator
					+ SAVEFILES_NAME);
			if (savedFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(
						savedFile));
				br.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
