package com.pisen.router.core.playback;

/**
 * 多媒体播放状态
 * @author ldj
 * @version 1.0 2015年4月15日 下午4:40:55
 */
public enum PlayStatus {
	STATE_ERROR,		//出错
	STATE_IDLE,			//停止（空闲）
	STATE_PREPARING,	//准备中
	STATE_PLAYING,		//播放中
	STATE_PAUSED,		//暂停
	STATE_PLAYBACK_COMPLETED,	//播放完成
	STATE_STOP,		//停止播放
//	private static final int STATE_PREPARED = 2;
//	private static final int STATE_SUSPEND = 6;
//	private static final int STATE_RESUME = 7;
//	private static final int STATE_SUSPEND_UNSUPPORTED = 8;
	
	/*出错状态*/
	ERROR_UNKNOWN,	//未知错误
	ERROR_OTHERCLIENT_PUSH,	//其他客户端推送
	ERROR_OPERATE_FAILED	//操作失败
}
