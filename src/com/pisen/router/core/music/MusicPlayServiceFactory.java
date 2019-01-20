package com.pisen.router.core.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.pisen.router.core.music.MusicPlayService.MusicPlayerBinder;

/**
 * 音乐播放服务生产工厂
 * @author ldj
 * @version 1.0 2015年4月23日 上午9:41:08
 */
public class MusicPlayServiceFactory implements IMusicPlaybackFactory {
	
	//binder
	private MusicPlayService.MusicPlayerBinder binder;
	//bind回调接口
	private OnBindListener listener;
	//服务连接，获取服务实例
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (MusicPlayerBinder) service;
			if(listener != null) {
				listener.onBind(binder.getMusicPlayService());
			}
		}
	};
	
	/**
	 * 绑定后台音乐播放服务
	 * @param ctx	上下文
	 * @param listener	绑定回调接口
	 */
	public void bindService(Context ctx, OnBindListener listener) {
		this.listener = listener;
		
		Intent in = new Intent(ctx.getApplicationContext(), MusicPlayService.class);
//		ctx.startService(in); 暂不支持后台播放
		ctx.bindService(in, conn, Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * 解绑服务
	 * @param ctx
	 */
	public void unBindService(Context ctx) {
		ctx.unbindService(conn);
	}
	
	public static interface OnBindListener {
		void onBind(MusicPlayService service);
	}

}
