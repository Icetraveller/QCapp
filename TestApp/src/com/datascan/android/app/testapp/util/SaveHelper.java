package com.datascan.android.app.testapp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

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
	private static final String SAVEFILES_NAME = "SavedDoTest";
	private static final String SAVEFILES2_NAME = "SavedPassTest";
	private static final String REPORT_NAME = "report";

	private static final int CATEGORY_DOTEST = 1;
	private static final int CATEGORY_PASSTEST = 2;
	private static final int CATEGORY_REPORT = 3;

	/**
	 * Constructor
	 * 
	 * @param contex
	 *            caller's activity
	 */
	public SaveHelper(Context contex) {
	}

	/**
	 * The save strategy is save report in sd card and save doTest as a sequence
	 * 
	 * @param report
	 *            the report that is already print on the log.
	 * @param doTest
	 *            the flag that reflect a test is done or not.
	 */
	public static void save(String report, SparseBooleanArray doTestArray,SparseBooleanArray passTestArray) {
		if (TextUtils.isEmpty(report)) {
			return;
		}
		write(CATEGORY_REPORT, report);
		save(CATEGORY_DOTEST,doTestArray);
		save(CATEGORY_PASSTEST,passTestArray);
	}
	
	private static void save(int category, SparseBooleanArray array){
		StringBuilder sb = new StringBuilder();
		int key = 0;
		for (int i = 0; i < array.size(); i++) {
			key = array.keyAt(i);
			// get the object by the key.
			Boolean flag = (Boolean) array.get(key);
			sb.append(key).append("#");
			if (flag) {
				sb.append(1);
			} else {
				sb.append(0);
			}
			sb.append(" ");
		}
		write(category, sb.toString());
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

	public static File getFile(int category) {
		File dir = new File(
				android.os.Environment.getExternalStorageDirectory(),
				SAVEFILES_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File f = null;
		switch (category) {
		case CATEGORY_DOTEST:
			f = new File(dir + File.separator + SAVEFILES_NAME);
			break;
		case CATEGORY_PASSTEST:
			f = new File(dir + File.separator + SAVEFILES2_NAME);
			break;
		case CATEGORY_REPORT:
			f = new File(dir + File.separator + REPORT_NAME);
			break;
		}
		return f;
	}

	/**
	 * This method mainly to load strategy files from internal storage. It reads
	 * strategy data from files and save it to strategy data structure in
	 * sharedbox
	 */
	public static SparseBooleanArray loadSavedDoTest() {
		try {
			File dir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					SAVEFILES_PATH);
			if (!dir.exists()) {
				return null;
			}
			// load progress
			File savedFile = getFile(CATEGORY_DOTEST);
			if (savedFile.exists()) {
				BufferedReader br = new BufferedReader(
						new FileReader(savedFile));
				SparseBooleanArray saves = new SparseBooleanArray();
				saves = processProgress(readBuffer(br));
				br.close();
				return saves;
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static SparseBooleanArray loadSavedPassTest() {
		try {
			File dir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					SAVEFILES_PATH);
			if (!dir.exists()) {
				return null;
			}
			// load progress
			File savedFile = getFile(CATEGORY_PASSTEST);
			if (savedFile.exists()) {
				BufferedReader br = new BufferedReader(
						new FileReader(savedFile));
				SparseBooleanArray saves = new SparseBooleanArray();
				saves = processProgress(readBuffer(br));
				br.close();
				return saves;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String loadReport() {
		String report = null;
		try {
			File dir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					SAVEFILES_PATH);
			if (!dir.exists()) {
				return null;
			}
			// load report
			File reportFile = getFile(CATEGORY_REPORT);
			if (reportFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(
						reportFile));
				report = readBuffer(br);
				br.close();
				return report;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return report;
	}

	private static String readBuffer(BufferedReader br) throws IOException {
		StringBuilder text = new StringBuilder();
		String line;

		while ((line = br.readLine()) != null) {
			text.append(line);
			text.append('\n');
		}
		return text.toString();
	}

	private static SparseBooleanArray processProgress(String text) {
		if (TextUtils.isEmpty(text)) {
			return null;
		}
		SparseBooleanArray array = new SparseBooleanArray();
		String[] tests = text.split(" ");
		for (String test : tests) {
			String[] param = test.split("#");
			if (param.length != 2){
				continue;
			}
			int key = Integer.parseInt(param[0]);
			int flag = Integer.parseInt(param[1]);
			boolean value = flag == 1 ? true : false;
			Log.e(TAG, "key: " + key + " value: " + value);
			array.put(key, value);
		}
		return array;
	}

}
