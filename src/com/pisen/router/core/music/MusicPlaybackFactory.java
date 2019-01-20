package com.pisen.router.core.music;

import com.pisen.router.core.playback.MusicPlayback;

/**
 * 
 * @author ldj
 * @version 1.0 2015年4月23日 上午9:59:34
 */
public class MusicPlaybackFactory implements IMusicPlaybackFactory {
	
	/**
	 * 播放器类别
	 * @author ldj
	 * @version 1.0 2015年4月23日 上午10:00:02
	 */
	public static enum Type {
		Local,		//本地播放器
		CyberLink,	//dlna播放器
		XiaMi		//虾米播放器
	}
	
	/**
	 * 获取对应type播放器
	 * @param type
	 * @return
	 */
	public static MusicPlayback create(Type type) {
		MusicPlayback player = null;
		switch (type) {
		case Local:
			player = new LocalMusicPlayback();
			break;
		case CyberLink:
			player = new CyberLinkMusicPlayback();
			break;
		case XiaMi:
			player = new XiaMiMusicPlayback();
			break;
		default:
			break;
		}
		return player;
	}
}
