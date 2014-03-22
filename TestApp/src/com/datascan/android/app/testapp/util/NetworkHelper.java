package com.datascan.android.app.testapp.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * This class is a helper to modify WiFi, BlueTooth, or other network things
 * 
 * @author yue
 * 
 */
public class NetworkHelper {
	private WifiManager wifiManager;
	private BluetoothAdapter bluetoothAdapter;
	private Context context;

	private static final String TAG = LogUtil.makeLogTag(NetworkHelper.class);

	/**
	 * The contructor will init the system serivce of wifi and bluetooth
	 * @param context
	 */
	public NetworkHelper(Context context) {
		this.context = context;
		getWiFiManager();
		getBluetoothAdapter();
	}

	private void getWiFiManager() {
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	private void getBluetoothAdapter() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public boolean getWifiStatus() {
		int state = wifiManager.getWifiState();
		switch(state){
			case WifiManager.WIFI_STATE_DISABLED:
			case WifiManager.WIFI_STATE_DISABLING:
				return false;
			case WifiManager.WIFI_STATE_ENABLED:
			case WifiManager.WIFI_STATE_ENABLING:
				return true;
			case WifiManager.WIFI_STATE_UNKNOWN:
				break;
		}
		return false;
	}

	public void setWiFi(boolean enabled) {
		wifiManager.setWifiEnabled(enabled);
	}

	public boolean getBlueToothStatus() {
		return bluetoothAdapter.isEnabled();
	}

	public void setBlueTooth(boolean enabled) {
		if (enabled)
			bluetoothAdapter.enable();
		else
			bluetoothAdapter.disable();
	}

	public boolean isWifiConnected() {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

	public void connectToWifi() {
		if (isWifiConnected())
			return;
		wifiManager.startScan();
		Log.d(TAG, "Scanning");
	}

}
