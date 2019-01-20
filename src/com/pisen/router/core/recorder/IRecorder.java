package com.pisen.router.core.recorder;

/**
 * (录音机)接口
 */
public interface IRecorder {
	// 状态常量 定义
	public static final int IDLE_STATE = 0;
	public static final int RECORDING_STATE = 1;
	public static final int RECORDING_PAUSE_STATE = 2;
	public static final int PLAYING_STATE = 3;
	public static final int PLAYING_PAUSED_STATE = 4;
	// 错误常量 定义
	public static final int NO_ERROR = 0;
	public static final int STORAGE_ACCESS_ERROR = 1;
	public static final int INTERNAL_ERROR = 2;
	public static final int IN_CALL_RECORD_ERROR = 3;

	public interface OnStateChangedListener {
		public void onStateChanged(int state);

		public void onError(int error);
	}

	/**
	 * 开始录音
	 */
	public void startRecording();

	/**
	 * 开始录音
	 * 
	 * @param highQuality
	 *            true高质量 | false低质量, 默认为true
	 */
	public void startRecording(boolean highQuality);

	/**
	 * 暂停录音
	 */
	public void pauseRecording();

	/**
	 * 停止录音
	 */
	public void stopRecording();

	/**
	 * 获取状态
	 */
	public int getState();

	/**
	 * 获取录音进度
	 */
	public int getProgress();

	/**
	 * 是否录音中
	 */
	public boolean isRecording();

	/**
	 * 设置状态变化回调
	 */
	public void setOnStateChangedListener(OnStateChangedListener listener);

	/**
	 * 播放录音
	 */
	public void startPlayback(float percentage);

	/**
	 * 暂停播放
	 */
	public void pausePlayback();

	/**
	 * 停止播放 
	 */
	public void stopPlayback(boolean changeState);
	
	/**
	 * 获取播放进度
	 */
	public float getPlayProgress();
}
