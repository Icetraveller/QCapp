package com.datascan.android.app.testapp;

import java.io.File;
import java.io.IOException;

import com.datascan.android.app.testapp.util.DmesgReader;
import com.datascan.android.app.testapp.util.DmsgTool;
import com.datascan.android.app.testapp.util.LogUtil;
import com.datascan.android.app.testapp.util.SaveHelper;

import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class DmsgActivity extends Activity {
	
	private static final String TAG = LogUtil.makeLogTag(DmsgActivity.class);

	private static final int REBOOT_MAX = 2;

	void PassOrFail() {
		TextView t = (TextView) findViewById(R.id.tvMine);
		Button btn = (Button) findViewById(R.id.button1);
		
		
		DmesgReader myReader = new DmesgReader();
		int timesFailed = myReader.dmesgReadFile();
		
		DmsgTool readRebootFile = new DmsgTool();
		int timesRebooted = readRebootFile.rebootReadFile();
		
		if (timesFailed >= 1) {
			t.setText("Failed on reboot #: " + timesRebooted);
			btn.setEnabled(true);
			setResult(MainActivity.RESULT_FAIL);
			finish();
			return;

		} else if(timesFailed == 0){
			
			t.setText("Reboot: " + timesRebooted);
			
			
			if (timesRebooted < REBOOT_MAX){
				timesRebooted ++;
				DmsgTool fileWrite = new DmsgTool();
				fileWrite.rebootWrite(timesRebooted);
				
				new Thread( new X() ).start();
			}
			else{ 
				t.setText("Passed Test");
				btn.setEnabled(true);
				setResult(MainActivity.RESULT_OK);
				finish();
				return;
			}

		} else if(timesFailed == -1){
			t.setText("dmessage.txt Not Found");
			btn.setEnabled(true);
		} else if(timesFailed == -2){
			t.setText("dmessage.txt IO Exception");	
			btn.setEnabled(true);
		}


	}

	//$$$$$$$$$$$$$$$$$$ This reboots the scanner $$$$$$$$$$$$$$$$$$$$$$$$$$$
	void rebootScanner(){
		try{
			PowerManager pwrMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
			pwrMgr.reboot(null);
		}catch(Exception ex){

		}
	}






	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dmsg);
		Log.e(TAG, "onCreate");
		//Unlocks the unlock screen after a reboot
		unlockScanner();
		File rebootFileCheck = SaveHelper.getFile(SaveHelper.CATEGORY_DMSGFILE);
		if (rebootFileCheck.exists()){
			PassOrFail();
		} else {
			
			try {
				rebootFileCheck.createNewFile();
				DmsgTool fileWrite = new DmsgTool();
				fileWrite.rebootWrite(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		PassOrFail();
		}
		

	}

 

//$$$$$$$$$$$$$$$$$$$$ This starts after the start button has been pressed $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	public void sendMessage(View view) {
		// Do something in response to button click
		File rebootFile = SaveHelper.getFile(SaveHelper.CATEGORY_DMSGFILE);
		if (rebootFile.exists()){
			//rebootFile.delete();
			DmsgTool fileWrite = new DmsgTool();
			fileWrite.rebootWrite(0);
			

			rebootScanner();
			
		}else{
			
			try {
				rebootFile.createNewFile();
				
				DmsgTool fileWrite = new DmsgTool();
				fileWrite.rebootWrite(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			rebootScanner();
			
		}

	}

	
// $$$$$$$$$$$$$$$$$$$$$$$$$This auto starts the app after a reboot $$$$$$$$$$$$$$$$$$$$$$$$$$  
	public static class BootUpReceiver extends BroadcastReceiver{


		@Override
		public void onReceive(Context context, Intent intent) {
		
			Intent i = new Intent(context, DmsgActivity.class);  
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);


		}
	}

    public class X implements Runnable{
    	public void run(){
    		try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		rebootScanner();
    	}
    }
	
    //$$$$$$$$$$$$$$$$$$ Unlocks scanners lock screen after a reboot $$$$$$$$$$$$$$$$$$$$$$$
      void unlockScanner() {
		try {
			
			Window window = getWindow(); // in Activity's onCreate() for instance
			window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		
		} catch (Exception ex) {

		}
	}




}




















