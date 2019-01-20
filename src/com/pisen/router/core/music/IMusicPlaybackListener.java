package com.pisen.router.core.music;

import com.pisen.router.core.playback.PlayStatus;


/**
 * 音乐播放监听接口
 * @author Liuhc
 * @version 1.0 2015年4月8日 下午4:03:21
 */
public interface IMusicPlaybackListener {
	/**
	 * 播放进度更新
	 * @param progress
	 * @param curTime
	 * @param totalTime
	 */
	void onProgressUpdate(int progress,String curTime,String totalTime);
	
	/**开始播放*/
	void onPlay();
	/**暂停*/
	void onPaused();
	/**播放完成*/
	void onCompleted();
	/**播放停止*/
	void onStopped();
	/**
	 * 播放出错
	 * @param error
	 */
	void onError(PlayStatus status, String msg);
}
