package com.datascan.android.app.testapp;

import com.datascan.android.app.testapp.util.LogUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = LogUtil.makeLogTag(MainActivity.class);
	
	private boolean doDecodeTest = true;
	private boolean passDecodeTest = false;
	
	private boolean doSnapshotTest = true;
	private boolean passSnapshotTest = false;
	
	private static int DECODE_TEST_REQUEST = 0;
	private static int SNAPSHOT_TEST_REQUEST = 1;
	
	public static final int RESULT_RETRY = -100;
	public static final int RESULT_FAIL = -101;
	public static final String FAIL_REASON = "fail_reason";
	
	private TextView resultTextView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		resultTextView = (TextView) findViewById(R.id.result);
	} 

	@Override
	public void onResume(){
		super.onResume();
		Log.e(TAG, "Main onResume");
		testProcess();
		updateResult();
	}
	
	private void updateResult(){
		StringBuilder sb = new StringBuilder();
		if(passDecodeTest){
			sb.append("Decode Test \t Passed");
		}else{
			sb.append("Decode Test \t Failed");
		}
		sb.append("\n");
		if(passSnapshotTest){
			sb.append("Snapshot Test \t Passed");
		}else{
			sb.append("Snapshot Test \t Failed");
		}
		sb.append("\n");
		
		resultTextView.setText(sb.toString());
	}
	
	
	public void testProcess(){
		if(doDecodeTest){
			Intent intent = new Intent(this, DecodeActivity.class);
			startActivityForResult(intent, DECODE_TEST_REQUEST);
		}
		else if(doSnapshotTest){
			Intent intent = new Intent(this, BlackLevelActivity.class);
			startActivityForResult(intent, SNAPSHOT_TEST_REQUEST);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == DECODE_TEST_REQUEST) {
	        if (resultCode == RESULT_OK) { //passed the test
	        	doDecodeTest = false;
	        	passDecodeTest = true;
	        }
	        if(resultCode == RESULT_CANCELED){ //failed the test
	        	doDecodeTest = false;
	        	passDecodeTest =false;
	        }
	        if(resultCode == RESULT_RETRY){//request retry
	        	doDecodeTest = true;
	        	passDecodeTest =false;
	    	}
	        if(resultCode == RESULT_FAIL){
	        	doDecodeTest = false;
	        	passDecodeTest =false;
	    	}
	    }
	    if(requestCode == SNAPSHOT_TEST_REQUEST){
	    	if (resultCode == RESULT_OK) { //passed the test
	    		doSnapshotTest = false;
	        	passSnapshotTest = true;
	        }
	    	if(resultCode == RESULT_CANCELED){ //failed the test
	        	doSnapshotTest = false;
	        	passSnapshotTest =false;
	        }
	    	if(resultCode == RESULT_RETRY){//request retry
	    		doSnapshotTest = true;
	    		passSnapshotTest =false;
	    	}
	    	if(resultCode == RESULT_FAIL){
	    		doSnapshotTest = false;
	    		passSnapshotTest =false;
	    		String reasonString = data.getStringExtra(FAIL_REASON); // update fail reason
	    	}
	    }
	}
	
}
