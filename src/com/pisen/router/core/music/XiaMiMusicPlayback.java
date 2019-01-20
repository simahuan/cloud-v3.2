package com.pisen.router.core.music;

import io.vov.vitamio.utils.Log;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

import com.pisen.router.BuildConfig;
import com.pisen.router.common.utils.DateUtils;
import com.pisen.router.core.playback.MusicPlayback;
import com.pisen.router.core.playback.PlayStatus;
import com.xiami.player.PlayMode;
import com.xiami.sdk.MusicPlayer;
import com.xiami.sdk.MusicPlayer.OnCompletionListener;
import com.xiami.sdk.MusicPlayer.OnErrorListener;
import com.xiami.sdk.MusicPlayer.OnSongChangedListener;
import com.xiami.sdk.entities.LocalSong;

/**
 * 基于虾米api的音乐播放器
 * @author ldj
 * @version 1.0 2015年4月22日 上午9:54:13
 */
public class XiaMiMusicPlayback extends MusicPlayback implements OnCompletionListener, OnErrorListener, OnSongChangedListener {

	private MusicPlayer musicPlayer;
	private List<LocalSong> songs;
	// 获取播放进度
	private static final int WHAT_GET_PROGRESS = 0x6000;
	private static final int WHAT_PLAY_FINISH = 0x6001;

	public XiaMiMusicPlayback() {
		initMusicPlayer();
	}

	/**
	 * 初始化播放器
	 */
	private void initMusicPlayer() {
		musicPlayer = new MusicPlayer();
		//设置默认顺序播放
		musicPlayer.setPlayMode(PlayMode.LOOP_LIST);
		//禁止自动播放下一首
		musicPlayer.setAutoPlayNext(false);
		musicPlayer.setAutoDownload(false);
		musicPlayer.setOnCompletionListener(this);
		musicPlayer.setOnErrorListener(this);
		musicPlayer.setOnSongChangeListener(this);
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;

			switch (what) {
			case WHAT_GET_PROGRESS:
				getProgress();
				startAutoGetProgress();
				break;
			case WHAT_PLAY_FINISH:
				next();
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void setMode(com.pisen.router.core.playback.PlayMode mode) {
		super.setMode(mode);
		switch (mode) {
		case queue:
			musicPlayer.setPlayMode(PlayMode.LOOP_LIST);
			break;
		case random:
			musicPlayer.setPlayMode(PlayMode.SHUFFLE);
			break;
		case single:
			musicPlayer.setPlayMode(PlayMode.LOOP_SINGLE);
			break;
		default:
			break;
		}
	}

	@Override
	public int getItemSelectedIndex() {
		int index = -1;
		if(musicPlayer != null) {
			LocalSong s = (LocalSong) musicPlayer.getCurrentSong();
			if(s != null) {
				index = (int) s.getSongId();
			}
			if(BuildConfig.DEBUG) Log.d("getItemSelectedIndex->" + index);
		}

		return index;
	}

	@Override
	public void startPlay() {
		if(musicPlayer != null) {
			if(!musicPlayer.isPlaying()) {
				startAutoGetProgress();
			}
			
			//虾米音乐默认设置数据后播放，所以放到此处设置播放器数据
			if(songs == null) {
				songs = convertMusic(playList);
				musicPlayer.setSongs(songs);
			}
			
			musicPlayer.play();
			if(listener != null) {
				listener.onPlay();
			}
		}
	}
	
	@Override
	public void startPlayByIndex(int playIndex) {
		int itemSelectedIndex = getItemSelectedIndex();
		if(playIndex != itemSelectedIndex) {
			if(songs == null) {
				songs = convertMusic(playList);
			}
			musicPlayer.setSongs(songs, playIndex);
			startPlay();
		}
	}

	@Override
	public void seekTo(int msec) {
		if(musicPlayer != null) {
			musicPlayer.seekTo(msec);
		}
	}

	@Override
	public void pausePlay() {
		if(musicPlayer != null) {
			musicPlayer.pause();
			stopAutoGetProgress();
			if(listener != null) {
				listener.onPaused();
			}
		}
	}

	@Override
	public void stopPlay() {
		release();
		stopAutoGetProgress();
		if(listener != null) {
			listener.onStopped();
		}
	}

	@Override
	public boolean isPlaying() {
		return musicPlayer != null ? musicPlayer.isPlaying() : false;
	}

	@Override
	public int getCurrentPosition() {
		return musicPlayer != null ? musicPlayer.getCurrentPosition() : -1;
	}

	@Override
	public int getDuration() {
		return musicPlayer != null ? musicPlayer.getDuration() : -1;
	}

	@Override
	public void next() {
		if(musicPlayer != null) {
			musicPlayer.playNext();
			if(listener != null) {
				listener.onPlay();
			}
		}
	}

	@Override
	public void prev() {
		if(musicPlayer != null) {
			musicPlayer.playPrev();
			if(listener != null) {
				listener.onPlay();
			}
		}
	}

	/**
	 * 停止播放，并回收资源
	 */
	public void release() {
		if(musicPlayer != null) {
			if(musicPlayer.isPlaying()) {
				musicPlayer.pause();
			}
			musicPlayer.release();
			musicPlayer = null;
		}

		if(playList != null) {
			playList.clear();
			playList = null;
		}
	}

	@Override
	public void onError(int arg0, int arg1) {
		if(listener != null) {
			listener.onError(PlayStatus.ERROR_UNKNOWN,"播放异常");
		}
	}

	@Override
	public void onCompletion(int arg0) {
		stopAutoGetProgress();
		if(listener != null) {
			listener.onCompleted();
		}
	}
	
	@Override
	public void onSongChanged() {
		stopAutoGetProgress();
		startAutoGetProgress();
	}
	
	/**
	 * 进行自动获取进度
	 */
	private void startAutoGetProgress() {
		handler.sendEmptyMessageDelayed(WHAT_GET_PROGRESS, 1000);
	}

	/**
	 * 停止自动获取进度
	 */
	private void stopAutoGetProgress() {
		handler.removeMessages(WHAT_GET_PROGRESS);
	}

	/**
	 * 转换music为虾米播放器能播放的实体
	 * @param data
	 * @return
	 */
	private List<LocalSong> convertMusic(List<Music> data) {
		List<LocalSong> songs = null;
		if(data != null && !data.isEmpty()) {
			songs = new ArrayList<LocalSong>();

			int size = data.size();
			Music m = null;
			LocalSong s = null;
			for(int i=0; i<size; i++) {
				m = data.get(i);
				s = new LocalSong();
				s.setSongId((long) i); //利用该字段获取index
				s.setListenFile(m.getSavePath());
				songs.add(s);
			}
		}
		return songs;
	}

	/**
	 * 获取播放进度
	 */
	private void getProgress() {
		int positon = musicPlayer.getCurrentPosition();
		int duration = musicPlayer.getDuration();

		if (listener != null) {
			listener.onProgressUpdate(positon * 100 / duration,  DateUtils.getMusicTime(positon),DateUtils.getMusicTime(duration));
		}
	}

	@Override
	public void setVolume(float progress) {
		// 本地播放器不需要实现
	}
}
