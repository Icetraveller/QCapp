package com.datascan.android.app.testapp.util;

import android.util.Log;

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
		while (i < length ) {
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
		for(int i=0; i<8;i++){
			if((byte) ((b >> i) & 0x1) == 0x1){
				bit[i] = true;
			}else{
				bit[i] = false;
			}
		}
		return bit;
//		return "" + (byte) ((b >> 7) & 0x1) + " " + (byte) ((b >> 6) & 0x1)
//				+ " " + (byte) ((b >> 5) & 0x1) + " " + (byte) ((b >> 4) & 0x1)
//				+ " " + (byte) ((b >> 3) & 0x1) + " " + (byte) ((b >> 2) & 0x1)
//				+ " " + (byte) ((b >> 1) & 0x1) + " " + (byte) ((b >> 0) & 0x1);
	}

	// public static int getCount(byte word, TreeMap<Byte, Integer>
	// frequencyData)
	// {
	// if (frequencyData.containsKey(word))
	// { // The word has occurred before, so get its count from the map
	// return frequencyData.get(word); // Auto-unboxed
	// }
	// else
	// { // No occurrences of this word
	// return 0;
	// }
	// }
	//
	public void printAllCounts() {

		for (int word : bits) {
			Log.d("counter", "" + word);
		}

	}

}