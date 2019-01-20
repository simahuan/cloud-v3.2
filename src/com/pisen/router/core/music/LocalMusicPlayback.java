package com.pisen.router.core.music;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.studio.os.LogCat;
import android.util.Log;

import com.pisen.router.common.utils.DateUtils;
import com.pisen.router.core.playback.MusicPlayback;
import com.pisen.router.core.playback.PlayStatus;

/**
 * 音乐播放控制核心类
 * (外部监听类：IMusicPlayerListener)
 * @author Liuhc
 * @version 1.0 2015年4月10日 上午11:31:49
 */
public class LocalMusicPlayback extends MusicPlayback implements OnCompletionListener, OnErrorListener, OnPreparedListener {

	public static MediaPlayer musicPlayer;
	// 当前播放的歌曲下标
	private int itemSelectedIndex = -1; 
	// 当前播放歌曲
	private Music curPlayMusic;
	//当前播放状态
	public static PlayStatus playStatus = PlayStatus.STATE_IDLE; 
	private Handler handler = new Handler();
	private boolean isSeekCalled; 
	private int seekToMsc;

	public LocalMusicPlayback() {
		musicPlayer = new MediaPlayer();
		musicPlayer.setOnCompletionListener(this);
		musicPlayer.setOnErrorListener(this);
	}

	private Runnable updateRunnable = new Runnable() {
		@Override
		public void run() {
			int progress = 0;
			if(musicPlayer != null) {
				int curMusicPosition = musicPlayer.getCurrentPosition();
				int curMusicTotalLength = musicPlayer.getDuration();
				if (curMusicTotalLength >0 && listener != null) {
					progress = (curMusicPosition*100)/curMusicTotalLength;
					listener.onProgressUpdate(progress
							, DateUtils.getMusicTime(curMusicPosition)
							, DateUtils.getMusicTime(curMusicTotalLength));
				}
				if(Math.round(curMusicPosition/1000f) >= Math.round(curMusicTotalLength/1000f)) {
					LogCat.d("stopAutoGetProgress");
					stopAutoGetProgress();
				}
			}

			handler.postDelayed(this, 1000 -  progress * 9);
		}
	};

	@Override
	public void startPlay() {
		if(playList == null) return;
		try {
			if(playStatus == PlayStatus.STATE_PAUSED) {
				play();
			}else {
				curPlayMusic = playList.get(itemSelectedIndex);
				if (curPlayMusic != null){// && (Integer.parseInt(curPlayMusic.getTime()) > 0)) {
					musicPlayer.reset();
					musicPlayer.setDataSource(curPlayMusic.getSavePath());
					musicPlayer.setOnPreparedListener(this);
					musicPlayer.prepareAsync();
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 播放音乐
	 */
	private void play() {
		musicPlayer.start();
		playStatus = PlayStatus.STATE_PLAYING;
		if(isSeekCalled && seekToMsc >0) {
			musicPlayer.seekTo(seekToMsc);
		}
		startAutoGetProgress();
		if(listener != null) {
			listener.onPlay();
		}
	}

	@Override
	public void startPlayByIndex(int playIndex) {
		if(playIndex != itemSelectedIndex) {
			itemSelectedIndex = playIndex;
			startPlay();
		}
	}

	@Override
	public void seekTo(int msec) {
		if(musicPlayer != null) {
			if( playStatus != PlayStatus.STATE_PLAYING) {
				isSeekCalled = true;
				seekToMsc = msec;
				startPlay();
			}else {
				musicPlayer.seekTo(msec);
			}
		}
	}

	@Override
	public void next() {
		stopAutoGetProgress();
		playStatus = PlayStatus.STATE_IDLE;
		itemSelectedIndex = getNextIndex();
		startPlay();
	}

	@Override
	public void prev() {
		stopAutoGetProgress();
		playStatus = PlayStatus.STATE_IDLE;
		itemSelectedIndex = getPreIndex();
		startPlay();
	}

	@Override
	public boolean isPlaying() {
		if (musicPlayer == null) {
			return false;
		}
		return musicPlayer.isPlaying();
	}

	@Override
	public void pausePlay() {
		if (playStatus == PlayStatus.STATE_PLAYING) {
			playStatus = PlayStatus.STATE_PAUSED;
			musicPlayer.pause();

			stopAutoGetProgress();
			if(listener != null) {
				listener.onPaused();
			}
		}
	}

	/**
	 * 更新进度
	 */
	//	private void updateProgress(){
	//		if (isPlaying()) {
	//			handler.postDelayed(updateRunnable, 1);
	//		}else{
	//			handler.removeCallbacks(updateRunnable);
	//		}
	//	}

	private void startAutoGetProgress() {
		handler.postDelayed(updateRunnable, 1);
	}

	private void stopAutoGetProgress() {
		handler.removeCallbacks(updateRunnable);
	}

	@Override
	public void stopPlay() {
		playStatus = PlayStatus.STATE_STOP;
		if (musicPlayer != null) {
			musicPlayer.stop();

			stopAutoGetProgress();
			if(listener != null) {
				listener.onStopped();
			}
		}
	}

	@Override
	public int getCurrentPosition() {
		return musicPlayer.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return musicPlayer.getDuration();
	}

	/*	public Music getMusicSelected() {
		return curMusic;
	}*/

	@Override
	public void release() {
		stopPlay();
		if(musicPlayer != null) {
			musicPlayer.reset();
			musicPlayer.release();
			musicPlayer = null;
		}

	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e("onError", "what->" + what + "   extra->" + extra);
		if(listener != null) {
			listener.onError(PlayStatus.ERROR_UNKNOWN, "播放出错");
		}
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if(listener != null) {
			listener.onCompleted();
		}
	}

	@Override
	public int getItemSelectedIndex() {
		return itemSelectedIndex;
	}

	@Override
	public void setVolume(float progress) {
		//本地不需要实现该接口
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d("localmusic", "===onPrepared");
		play();
	}
}
