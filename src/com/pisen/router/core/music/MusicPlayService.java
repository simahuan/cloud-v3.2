package com.pisen.router.core.music;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.pisen.router.core.music.MusicPlaybackFactory.Type;
import com.pisen.router.core.playback.MusicPlayback;

/**
 * 音乐播放控制Service
 * @author Liuhc
 * @version 1.0 2015年4月10日 上午9:32:32
 * <p>ldj2015-04-23:调整代码，实现通用后台音乐播放服务</p>
 */
public class MusicPlayService extends Service{
	
	public IBinder binder = new MusicPlayerBinder();
	private Type type;
	private MusicPlayback player;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	/**
	 * 获取音乐播放器
	 * @param type 
	 * @return
	 */
	public MusicPlayback getMusicPlayer(Type type) {
		if(player == null || !type.equals(this.type)) {
			if(player != null) {
				player.stopPlay();
				player.release();//释放资源
				player = null;
			}
			
			this.type = type;
			player = MusicPlaybackFactory.create(type);
		}
		
		return player;
	}
	
	public Type getCurrentType(){
		return type;
	}
	
	/**
	 * 获取当前播放器
	 * @return
	 */
	public MusicPlayback getCurrentMusicPlayer() {
		return player;
	}
	
	/**
	 * 是否后台正在播放
	 * @return
	 */
	public boolean isPlaying() {
		return player == null ? false : player.isPlaying();
	}
	
	public void release() {
		if(player != null) {
			player.stopPlay();
			player.release();
			player = null;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class MusicPlayerBinder extends Binder{
		//返回本地服务
		MusicPlayService getMusicPlayService(){
			return MusicPlayService.this;
		}
	}
}
