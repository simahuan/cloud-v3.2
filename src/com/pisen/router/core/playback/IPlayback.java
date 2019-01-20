package com.pisen.router.core.playback;

public interface IPlayback {
	// 播放
	void startPlay();
	//播放指定index多媒体
	void startPlayByIndex(int playIndex);
	// 位置
	void seekTo(int msec);

	// 暂停
	void pausePlay();

	// 停止
	void stopPlay();

	// 是否播放
	boolean isPlaying();

	//当前播放进度
	int getCurrentPosition();

	//总时长
	int getDuration();

	int getItemSelectedIndex();

	void next();

	void prev();
	
	/**
	 * 设置音量
	 * @param progress
	 */
	void setVolume(float progress);

}