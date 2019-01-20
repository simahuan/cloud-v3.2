package com.pisen.router.core.playback;

import java.util.List;

import com.pisen.router.core.filemanager.ResourceInfo;


/**
 * (播放类)
 * 
 * @author yangyp
 */
public abstract class VideoPlayback implements IPlayback {

	protected List<ResourceInfo> palylist;
	protected PlayMode mode; // 播放方式

	/**
	 * 设置播放模式
	 * 
	 * @param mode
	 *            PlayMode
	 */
	public void setMode(PlayMode mode) {
		this.mode = mode;
	}

	// 播放地址
	public void setDataSource(List<ResourceInfo> data) {
		this.palylist = data;
	}

	public ResourceInfo getItemSelected() {
		return (palylist != null && !palylist.isEmpty() && getItemSelectedIndex() >= 0) ? palylist.get(getItemSelectedIndex()) : null;

	}
}
