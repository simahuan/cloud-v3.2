package com.pisen.router.core.playback;

import java.util.List;
import java.util.Random;

import android.studio.os.LogCat;

import com.pisen.router.core.music.IMusicPlaybackListener;
import com.pisen.router.core.music.Music;

/**
 * 音乐播放器
 * @author ldj
 * @version 1.0 2015年4月23日 下午4:30:10
 */
public abstract class MusicPlayback implements IPlayback {

	protected List<Music> playList;
	// 默认顺序播放
	protected PlayMode mode = PlayMode.queue; 
	protected IMusicPlaybackListener listener;

	/**
	 * 设置播放模式
	 * @param mode PlayMode
	 */
	public void setMode(PlayMode mode) {
		this.mode = mode;
	}

	/**
	 * 设置播放数据
	 */
	public void setDataSource(List<Music> data) {
		this.playList = data;
	}

	/**
	 * 获取播放数据
	 * @return
	 */
	public List<Music> getDataSource() {
		return playList;
	}

	/**
	 * 获取当前音乐
	 * @return
	 */
	public Music getItemSelected() {
		return (playList != null && !playList.isEmpty() && getItemSelectedIndex() >=0) ? playList.get(getItemSelectedIndex()) : null;
	}

	/**
	 * 设置音乐播放监听
	 * @param listener
	 */
	public void setOnMusicPlaybackListener(IMusicPlaybackListener listener){
		this.listener = listener;
	}

	private Random random =  new Random();
	/**
	 * 随机获取下一个index
	 * @param curIndex	当前index
	 * @param count	总条数（最大值）
	 * @return
	 */
	protected int getRandomIndex(int curIndex, int count) {
		int index = random.nextInt(count);
		return index == curIndex ? getRandomIndex(curIndex, count) : index;
	}

	/**
	 * 资源释放
	 */
	public abstract void release();

	/**
	 * 根据播放模式获取下一曲播放index
	 * @return
	 */
	protected int getNextIndex() {
		int curIndex = getItemSelectedIndex();
		if(playList != null) {
			int totalCount = playList.size();
			switch (mode) {
			case queue:
				curIndex ++;
				if(curIndex >= totalCount) {
					curIndex = 0;
				}
				break;
			case random:
				curIndex = getRandomIndex(curIndex, totalCount);
				break;
			case single:
				break;
			default:
				break;
			}
		}else {
			LogCat.d("playlist is null");
		}

		return curIndex;
	}

	/**
	 * 根据播放模式获取前一首歌曲index
	 * @return
	 */
	protected int getPreIndex() {
		int curIndex = getItemSelectedIndex();
		if(playList != null) {
			int totalCount = playList.size();
			switch (mode) {
			case queue:
				curIndex --;
				if(curIndex < 0) {
					curIndex = totalCount -1;
				}
				break;
			case random:
				curIndex = getRandomIndex(curIndex, totalCount);
				break;
			case single:
				break;
			default:
				break;
			}
		}else {
			LogCat.d("playlist is null");
		}

		return curIndex;
	}
}
