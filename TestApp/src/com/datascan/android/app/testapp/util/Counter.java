package com.datascan.android.app.testapp.util;

import android.util.Log;

/**
 * The class is used to count the bits of the image data. The data is assumed to
 * be in bmp format and will be chop off the first 1078 bytes from head.
 * 
 * @author yue
 * 
 */
public class Counter {
	int[] bits;

	public int[] getBits() {
		return bits;
	}

	byte[] data;

	public Counter(byte[] image) {
		data = image;
		bits = new int[8];
	}

	public void count() {
		int length = data.length;

		int i = 1078;
		while (i < length) {
			getCount(data[i]);
			i++;
		}

		printAllCounts();
	}

	private void getCount(byte word) {

		boolean[] bit = byteToBit(word);
		for (int i = 0; i < 8; i++) {
			if (bit[i]) {
				bits[i]++;
			}
		}
	}

	public static boolean[] byteToBit(byte b) {
		boolean[] bit = new boolean[8];
		for (int i = 0; i < 8; i++) {
			if ((byte) ((b >> i) & 0x1) == 0x1) {
				bit[i] = true;
			} else {
				bit[i] = false;
			}
		}
		return bit;
	}

	public void printAllCounts() {

		for (int word : bits) {
			Log.d("counter", "" + word);
		}

	}

}