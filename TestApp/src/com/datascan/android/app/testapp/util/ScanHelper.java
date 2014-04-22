package com.datascan.android.app.testapp.util;

import com.datascan.android.app.testapp.*;
import com.motorolasolutions.adc.decoder.BarCodeReader;
import com.motorolasolutions.adc.decoder.BarCodeReader.DecodeCallback;
import com.motorolasolutions.adc.decoder.BarCodeReader.PictureCallback;
import com.motorolasolutions.adc.decoder.BarCodeReader.VideoCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

/**
 * This helper hold one bar code reader object for the whole application
 * 
 * @author yue
 * 
 */
public class ScanHelper implements DecodeCallback, PictureCallback,
		VideoCallback {

	private final static String TAG = "ScanHelper";

	static {
		System.loadLibrary("IAL");
		System.loadLibrary("SDL");
		System.loadLibrary("barcodereader");
	}

	// BarCodeReader specifics
	private static BarCodeReader bcr = null;

	// states
	static final int STATE_IDLE = 0;
	static final int STATE_DECODE = 1;
	static final int STATE_HANDSFREE = 2;
	static final int STATE_PREVIEW = 3; // snapshot preview mode
	static final int STATE_SNAPSHOT = 4;
	static final int STATE_VIDEO = 5;
	static final int STATE_RESET = 6;

	private int state = STATE_IDLE;

	// sound related
	private ToneGenerator tg;

	private Context context;
	private TextView tv;

	private int decodeCount = 0;
	private int decodeNumber = 0;

	public ScanHelper(Context context) {
		this.context = context;
		// scan
		initReader();
		// sound
		tg = new ToneGenerator(AudioManager.STREAM_MUSIC,
				ToneGenerator.MAX_VOLUME);
	}

	private void initReader() {
		state = STATE_RESET;
		if (bcr == null) {
			boolean flag = openBcr();
		}
		bcr.setDecodeCallback(this);
		doDefaultParams();
		if (context instanceof DecodeActivity) {
			setParam(136, 15); // set timeout
		}
		if (context instanceof BlackLevelActivity) {
			setParam(BarCodeReader.ParamNum.IMG_FILE_FORMAT, 3);
			setParam(361, 0); // turn off illumination
			setParam(300, 0); // turn off aim pattern
			setParam(323, 0); // set timeout to minimum
			setParam(761, 0); // scale video
		}
		state = STATE_IDLE;

	}

	/**
	 * used when user press pause button in MainActivity
	 */
	public void stopDecode() {
		if (bcr != null && state == STATE_DECODE) {
			bcr.stopDecode();
			state = STATE_IDLE;
		}
	}

	/**
	 * Do decode
	 */
	public void doDecode() {
		if (bcr == null || !(context instanceof DecodeActivity)) {
			return;
		}
		if (setIdle() != STATE_IDLE)
			return;
		state = STATE_DECODE;
		Log.e(TAG, "Start decode");
		bcr.startDecode();
	}

	public void doSnap() {
		Log.e(TAG, "doSnap");
		if (bcr == null || !(context instanceof BlackLevelActivity)) {
			return;
		}
		if (setIdle() != STATE_IDLE)
			return;
		state = STATE_SNAPSHOT;
		bcr.stopPreview();
		bcr.startViewFinder();
		bcr.takePicture(this);
	}

	public void doVideo() {
		if (setIdle() != STATE_IDLE)
			return;

		state = STATE_VIDEO;
		// vidScreen(false); //start video
		// videoCapDisplayStarted = false;
		bcr.startVideoCapture(this); // start video
	}

	/**
	 * 
	 * @return successful or not
	 */
	private boolean openBcr() {
		try {
			bcr = BarCodeReader.open(1);
			if (bcr == null) {
				Log.e(TAG, "failed to open");
			} else {
				Log.e(TAG, "succeed to open");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void close() {
		Log.e(TAG, "bcr is about to close");
		if (bcr != null) {
			Log.e(TAG, "succeed to close");
			bcr.release();
			bcr = null;
		} else {
			Log.e(TAG, "bcr is null");
		}
	}

	public void beep() {
		if (tg != null)
			tg.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING);
	}

	public boolean setParam(int paramNum, int paramVal) {
		if (bcr == null)
			return false;
		int returnValue = bcr.setParameter(paramNum, paramVal);
		if (returnValue == bcr.BCR_SUCCESS)
			return true;
		else
			return false;
	}

	/**
	 * This method set scanner to default state
	 */
	public void doDefaultParams() {
		bcr.setDefaultParameters();
		setParam(136, 15);
	}

	public void enableAll() {
		bcr.enableAllCodeTypes();
	}

	public void disableAll() {
		bcr.disableAllCodeTypes();
	}

	@Override
	public void onDecodeComplete(int symbology, int length, byte[] data,
			BarCodeReader reader) {
		Log.e(TAG, "onDecodeComplete");
		if (decodeCount > decodeNumber)
			return;
		if (length == BarCodeReader.DECODE_STATUS_MULTI_DEC_COUNT) {
			Log.e(TAG, "multi decode");
			decodeNumber = symbology;
			decodeCount = 0;
		}
		String decodeDataString = "";
		if (length > 0) {
			state = STATE_IDLE;
			decodeCount++;
			if (symbology == 0x99) // type 99?
			{
				Log.e("asda", "1 " + symbology);
				symbology = data[0];
				int n = data[1];
				int s = 2;
				int d = 0;
				int len = 0;
				byte d99[] = new byte[data.length];
				for (int i = 0; i < n; ++i) {
					s += 2;
					len = data[s++];
					System.arraycopy(data, s, d99, d, len);
					s += len;
					d += len;
				}
				d99[d] = 0;
				data = d99;
			}
			decodeDataString += new String(data);

			if (decodeCount > 1) { // Add the next line only if multiple decode
				decodeDataString += new String(" ; ");
			}
			if (decodeCount == decodeNumber) {
				state = STATE_IDLE;
			}
			beep();
			Log.e(TAG, "decode successfully " + decodeDataString);
		} else { // no-decode
			switch (length) {
			case BarCodeReader.DECODE_STATUS_TIMEOUT:
				Log.e(TAG, "decode timed out");
				// displayTextView.setText("decode timed out");
				break;

			case BarCodeReader.DECODE_STATUS_CANCELED:
				Log.e(TAG, "decode cancelled");
				// displayTextView.setText("decode cancelled");
				break;

			case BarCodeReader.DECODE_STATUS_ERROR:
				break;
			case BarCodeReader.DECODE_STATUS_MULTI_DEC_COUNT:
				Log.e(TAG, "DECODE_STATUS_MULTI_DEC_COUNT");
				break;
			default:
				Log.e(TAG, "decode failed" + length);
				// displayTextView.setText("decode failed");
				break;
			}

			if (length != BarCodeReader.DECODE_STATUS_MULTI_DEC_COUNT) {
				state = STATE_IDLE;
			}
		}
		if (context instanceof DecodeActivity) {
			Log.e(TAG, "I'm a DecodeActivity");
			((DecodeActivity) context).showMessage(decodeDataString, symbology,
					length);
		}
		stopDecode();
	}

	@Override
	public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {
		// // TODO Auto-generated method stub
		// Log.e(TAG, "event= "+event+" info= "+info);
		// if(event ==7){
		// state = STATE_RESET;
		// if (context instanceof DecodeActivity) {
		// doDefaultParams();
		// setParam(136, 15); // set timeout
		// }
		// if (context instanceof BlackLevelActivity) {
		// doDefaultParams();
		// setParam(BarCodeReader.ParamNum.IMG_FILE_FORMAT,3);
		// setParam(361,0); // turn off illumination
		// setParam(300,0); // turn off aim pattern
		// setParam(323,0); // set timeout to minimum
		// }
		// state = STATE_IDLE;
		// }

	}

	private int setIdle() {
		int prevState = state;
		int ret = prevState; // for states taking time to chg/end

		state = STATE_IDLE;
		switch (prevState) {
		case STATE_DECODE:
			break;
		default:
			ret = STATE_IDLE;
		}
		return ret;
	}

	@Override
	public void onPictureTaken(int format, int width, int height,
			byte[] abData, BarCodeReader reader) {
		if (state != STATE_SNAPSHOT)
			return;
		Log.e(TAG, "onPictureTaken");
		if (context instanceof BlackLevelActivity) {
			Log.e(TAG, "I'm a SnapshotActivity");
			((BlackLevelActivity) context).showPreview(abData);
			bcr.stopPreview();
		}
		state = STATE_IDLE;
	}

	public void restart() {
		bcr.stopPreview();
		close();
		initReader();
	}

	@Override
	public void onVideoFrame(int format, int width, int height, byte[] data,
			BarCodeReader reader) {
		if (state != STATE_VIDEO)
			return;
		Log.e(TAG, "onVideoFrame");

		// display snapshot
		Bitmap bmSnap = BitmapFactory.decodeByteArray(data, 0, data.length);
		if (bmSnap != null) {

			if (context instanceof BlackLevelActivity) {
				Log.e(TAG, "I'm a BlackLevelActivity");
				((BlackLevelActivity) context).showPreview(data);
				bcr.stopPreview();
			}
			state = STATE_IDLE;
		}

	}

}
