package com.pisen.router.core.recorder;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * 录音机
 */
public class SoundRecorder implements IRecorder, OnCompletionListener, OnErrorListener {
	private static final String SAMPLE_PATH_KEY = "sample_path";
	private static final String SAMPLE_LENGTH_KEY = "sample_length";
	// public static final String SAMPLE_DEFAULT_DIR = "/sound_recorder";

	private int mState = IDLE_STATE; // 录音机状态
	private Context mContext;
	private OnStateChangedListener mOnStateChangedListener = null; // 状态改变回调
	// private boolean hasPaused;// 是否暂停过；暂停过的录音文件需要拼接
	private RecorderFile recorderFile;// 录音文件
	private MediaPlayer mPlayer = null;

	public SoundRecorder(Context context) {
		mContext = context;
		recorderFile = new RecorderFile();
		// syncStateWithService();
	}

	public RecorderFile getRecorderFile() {
		return recorderFile;
	}

	public boolean syncStateWithService() {
		if (RecorderService.isRecording()) {
			mState = RECORDING_STATE;
			recorderFile.recordStart = RecorderService.getStartTime();
			recorderFile.recordFile = new File(RecorderService.getFilePath());
			return true;
		} else if (mState == RECORDING_STATE) {
			return false;
		} else if (recorderFile.recordFile != null && recorderFile.recordLength == 0) {
			return false;
		}
		return true;
	}

	public void saveState(Bundle recorderState) {
		recorderState.putString(SAMPLE_PATH_KEY, recorderFile.recordFile.getAbsolutePath());
		recorderState.putInt(SAMPLE_LENGTH_KEY, recorderFile.recordLength);
	}

	public int getMaxAmplitude() {
		if (mState != RECORDING_STATE)
			return 0;
		return RecorderService.getMaxAmplitude();
	}

	public void restoreState(Bundle recorderState) {
		String samplePath = recorderState.getString(SAMPLE_PATH_KEY);
		if (samplePath == null)
			return;
		int sampleLength = recorderState.getInt(SAMPLE_LENGTH_KEY, -1);
		if (sampleLength == -1)
			return;

		File file = new File(samplePath);
		if (!file.exists())
			return;
		if (recorderFile.recordFile != null && recorderFile.recordFile.getAbsolutePath().compareTo(file.getAbsolutePath()) == 0)
			return;

		delete();
		recorderFile.recordFile = file;
		recorderFile.recordLength = sampleLength;
		signalStateChanged(IDLE_STATE);
	}

	public void setOnStateChangedListener(OnStateChangedListener listener) {
		mOnStateChangedListener = listener;
	}

	/**
	 * 获取录音机状态
	 */
	@Override
	public int getState() {
		return mState;
	}

	/**
	 * 获取录音或播放进度
	 */
	@Override
	public int getProgress() {
		if (mState == RECORDING_STATE) {
			return (int) ((System.currentTimeMillis() - recorderFile.recordStart) / 1000);
		} else if (mState == PLAYING_STATE || mState == PLAYING_PAUSED_STATE) {
			if (mPlayer != null) {
				return (int) (mPlayer.getCurrentPosition() / 1000);
			}
		}
		return 0;
	}

	/**
	 * 获取播放进度
	 */
	@Override
	public float getPlayProgress() {
		if (mPlayer != null) {
			return ((float) mPlayer.getCurrentPosition()) / mPlayer.getDuration();
		}
		return 0.0f;
	}

	public void renameSampleFile(String name) {
		if (recorderFile.recordFile != null && mState != RECORDING_STATE && mState != PLAYING_STATE) {
			if (!TextUtils.isEmpty(name)) {
				String oldName = recorderFile.recordFile.getAbsolutePath();
				String extension = oldName.substring(oldName.lastIndexOf('.'));
				File newFile = new File(recorderFile.recordFile.getParent() + "/" + name + extension);
				if (!TextUtils.equals(oldName, newFile.getAbsolutePath())) {
					if (recorderFile.recordFile.renameTo(newFile)) {
						recorderFile.recordFile = newFile;
					}
				}
			}
		}
	}

	public void delete() {
		stop();
		if (recorderFile.recordFile != null) {
			recorderFile.recordFile.delete();
		}
		recorderFile.reset();
		signalStateChanged(IDLE_STATE);
	}

	public void clear() {
		stop();
		recorderFile.recordLength = 0;
		signalStateChanged(IDLE_STATE);
	}

	public void reset() {
		stop();
		recorderFile.reset();
		mState = IDLE_STATE;
		// File sampleDir = new
		// File(Environment.getExternalStorageDirectory().getAbsolutePath() +
		// SAMPLE_DEFAULT_DIR);
		// if (!sampleDir.exists()) {
		// sampleDir.mkdirs();
		// }
		signalStateChanged(IDLE_STATE);
	}

	// public boolean isRecordExisted(String path) {
	// if (!TextUtils.isEmpty(path)) {
	// File file = new File(mSampleDir.getAbsolutePath() + "/" + path);
	// return file.exists();
	// }
	// return false;
	// }

	/**
	 * 开始录音
	 */
	@Override
	public void startRecording(boolean highQuality) {
		// stop();
		recorderFile.recordFile = RecorderFile.getRecorderFile();
		Log.i("testMsg", " startRecording " + recorderFile.recordFile.getAbsolutePath());
		RecorderService.startRecording(mContext, recorderFile.recordFile.getAbsolutePath(), highQuality);
		recorderFile.recordStart = System.currentTimeMillis();
		mState = RECORDING_STATE;
		signalStateChanged(mState);
	}

	/**
	 * 停止录音
	 */
	@Override
	public void stopRecording() {
		if (RecorderService.isRecording()) {
			RecorderService.stopRecording(mContext);
		}
		if (recorderFile.recordingList.size() == 0) {
			recorderFile.finalFile = recorderFile.recordFile;
		} else {

			if (mState == RECORDING_STATE) {
				// 把录音文件加入list
				recorderFile.recordingList.add(recorderFile.recordFile.getAbsolutePath());
				// 计算时长
				recorderFile.recordLength = getProgress();
				// 总时长累加
				recorderFile.finalLength += recorderFile.recordLength;
			}

			// 合并list,输出最终文件
			recorderFile.finalFile = new File(recorderFile.getInputCollection(false));
			// 清理RecorderFile
			recorderFile.reset();
		}
		mState = IDLE_STATE;
	}

	/**
	 * 暂停录音
	 */
	@Override
	public void pauseRecording() {
		// 停止录音
		if (RecorderService.isRecording()) {
			RecorderService.stopRecording(mContext);
		}
		// 把录音文件加入list
		recorderFile.recordingList.add(recorderFile.recordFile.getAbsolutePath());
		// 计算时长
		recorderFile.recordLength = getProgress();
		// 总时长累加
		recorderFile.finalLength += recorderFile.recordLength;
		mState = RECORDING_PAUSE_STATE;
		// hasPaused = true;
	}

	/**
	 * 开始播放
	 */
	public void startPlayback(float percentage) {
		if (recorderFile.finalFile == null) {
			setError(INTERNAL_ERROR);
			return;
		}
		if (getState() == PLAYING_PAUSED_STATE) {
			recorderFile.recordStart = System.currentTimeMillis() - mPlayer.getCurrentPosition();
			mPlayer.seekTo((int) (percentage * mPlayer.getDuration()));
			mPlayer.start();
			setState(PLAYING_STATE);
		} else {
			// stop();
			mPlayer = new MediaPlayer();
			try {
				mPlayer.setDataSource(recorderFile.finalFile.getAbsolutePath());
				mPlayer.setOnCompletionListener(this);
				mPlayer.setOnErrorListener(this);
				mPlayer.prepare();
				mPlayer.seekTo((int) (percentage * mPlayer.getDuration()));
				mPlayer.start();
			} catch (IllegalArgumentException e) {
				setError(INTERNAL_ERROR);
				mPlayer = null;
				return;
			} catch (IOException e) {
				e.printStackTrace();
				setError(STORAGE_ACCESS_ERROR);
				mPlayer = null;
				return;
			}

			recorderFile.recordStart = System.currentTimeMillis();
			setState(PLAYING_STATE);
		}
	}

	/**
	 * 暂停播放
	 */
	@Override
	public void pausePlayback() {
		if (mPlayer == null) {
			return;
		}
		mPlayer.pause();
		setState(PLAYING_PAUSED_STATE);
	}

	/**
	 * 停止播放
	 */
	@Override
	public void stopPlayback(boolean changeState) {
		if (mPlayer == null) // we were not in playback
			return;

		mPlayer.stop();
		mPlayer.release();
		mPlayer = null;
		if (changeState) {
			setState(IDLE_STATE);
		}
	}

	public void stop() {
		stopRecording();
		stopPlayback(false);
	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
		stop();
		setError(STORAGE_ACCESS_ERROR);
		return true;
	}

	public void onCompletion(MediaPlayer mp) {
		stop();
	}

	public void setState(int state) {
		if (state == mState)
			return;
		mState = state;
		signalStateChanged(mState);
	}

	private void signalStateChanged(int state) {
		if (mOnStateChangedListener != null)
			mOnStateChangedListener.onStateChanged(state);
	}

	public void setError(int error) {
		if (mOnStateChangedListener != null)
			mOnStateChangedListener.onError(error);
	}

	@Override
	public void startRecording() {
		startRecording(true);
	}

	@Override
	public boolean isRecording() {
		return false;
	}
}
