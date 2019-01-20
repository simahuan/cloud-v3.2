package com.pisen.router.core.recorder;

import java.io.IOException;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 录音服务
 */
public class RecorderService extends Service implements MediaRecorder.OnErrorListener {

	public final static String ACTION_NAME = "action_type";

	public final static int ACTION_INVALID = 0; 
	public final static int ACTION_START_RECORDING = 1;
	public final static int ACTION_STOP_RECORDING = 2;
	public final static int ACTION_ENABLE_MONITOR_REMAIN_TIME = 3;
	public final static int ACTION_DISABLE_MONITOR_REMAIN_TIME = 4;

	
	public final static String ACTION_PARAM_PATH = "path"; 
	public final static String ACTION_PARAM_HIGH_QUALITY = "high_quality";


	public final static String RECORDER_SERVICE_BROADCAST_NAME = "com.android.soundrecorder.broadcast";
	public final static String RECORDER_SERVICE_BROADCAST_STATE = "is_recording";
	public final static String RECORDER_SERVICE_BROADCAST_ERROR = "error_code";

	public final static int NOTIFICATION_ID = 62343234;

	private static MediaRecorder mRecorder = null;
	private static String mFilePath = null; //录音文件路径
	private static long mStartTime = 0; //开始时间


	private TelephonyManager mTeleManager;
	private WakeLock mWakeLock;
	private KeyguardManager mKeyguardManager;

	private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (state != TelephonyManager.CALL_STATE_IDLE) {
				localStopRecording();
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		mRecorder = null;
		mTeleManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTeleManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SoundRecorder");
		mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle bundle = intent.getExtras();
		if (bundle != null && bundle.containsKey(ACTION_NAME)) {
			switch (bundle.getInt(ACTION_NAME, ACTION_INVALID)) {
			case ACTION_START_RECORDING:
				localStartRecording(bundle.getString(ACTION_PARAM_PATH), bundle.getBoolean(ACTION_PARAM_HIGH_QUALITY));
				break;
			case ACTION_STOP_RECORDING:
				localStopRecording();
				break;
			default:
				break;
			}
			return START_STICKY;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		mTeleManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onLowMemory() {
		localStopRecording();
		super.onLowMemory();
	}

	/**
	 * 开始录音 
	 */
	private void localStartRecording(String path, boolean highQuality) {
		Log.i("testMsg", " in localStartRecording");
		if (mRecorder == null) {

			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setAudioSamplingRate(highQuality ? 16000 : 8000);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		}
		mRecorder.setOutputFile(path);
		mRecorder.setOnErrorListener(this);

		try {
			mRecorder.prepare();
		} catch (IOException exception) {
			sendErrorBroadcast(SoundRecorder.INTERNAL_ERROR);
			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;
			return;
		}
		try {
			mRecorder.start();
		} catch (RuntimeException exception) {
			AudioManager audioMngr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			boolean isInCall = (audioMngr.getMode() == AudioManager.MODE_IN_CALL);
			if (isInCall) {
				sendErrorBroadcast(SoundRecorder.IN_CALL_RECORD_ERROR);
			} else {
				sendErrorBroadcast(SoundRecorder.INTERNAL_ERROR);
			}
			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;
			return;
		}
		mFilePath = path;
		mStartTime = System.currentTimeMillis();
		mWakeLock.acquire();
		sendStateBroadcast();

	}

	/**
	 * 停止录音
	 */
	private void localStopRecording() {
		if (mRecorder != null) {
			try {
				mRecorder.stop();
			} catch (RuntimeException e) {
			}
			mRecorder.release();
			mRecorder = null;

			sendStateBroadcast();
		}
		stopSelf();
	}

	private void sendStateBroadcast() {
		Intent intent = new Intent(RECORDER_SERVICE_BROADCAST_NAME);
		intent.putExtra(RECORDER_SERVICE_BROADCAST_STATE, mRecorder != null);
		sendBroadcast(intent);
	}

	private void sendErrorBroadcast(int error) {
		Intent intent = new Intent(RECORDER_SERVICE_BROADCAST_NAME);
		intent.putExtra(RECORDER_SERVICE_BROADCAST_ERROR, error);
		sendBroadcast(intent);
	}

	public static boolean isRecording() {
		return mRecorder != null;
	}

	public static String getFilePath() {
		return mFilePath;
	}

	public static long getStartTime() {
		return mStartTime;
	}

	/**
	 * 开始录音--静态方法
	 */
	public static void startRecording(Context context, String path, boolean highQuality) {
		Intent intent = new Intent(context, RecorderService.class);
		intent.putExtra(ACTION_NAME, ACTION_START_RECORDING);
		intent.putExtra(ACTION_PARAM_PATH, path);
		intent.putExtra(ACTION_PARAM_HIGH_QUALITY, highQuality);
		context.startService(intent);
	}
	
    /**
     * 停止录音--静态方法 
     */
	public static void stopRecording(Context context) {
		Intent intent = new Intent(context, RecorderService.class);
		intent.putExtra(ACTION_NAME, ACTION_STOP_RECORDING);
		context.startService(intent);
	}

	public static int getMaxAmplitude() {
		return mRecorder == null ? 0 : mRecorder.getMaxAmplitude();
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		sendErrorBroadcast(SoundRecorder.INTERNAL_ERROR);
		localStopRecording();
	}
}
