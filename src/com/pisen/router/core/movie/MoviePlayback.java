package com.pisen.router.core.movie;

import io.vov.vitamio.MediaMetadataRetriever;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import io.vov.vitamio.utils.ScreenResolution;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.studio.os.LogCat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.pisen.router.BuildConfig;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.playback.VideoPlayback;

/**
 * (视频播放器)
 * 
 * @author yangyp
 */
public class MoviePlayback extends VideoPlayback implements OnCompletionListener, OnPreparedListener, OnErrorListener, OnVideoSizeChangedListener,
		OnSeekCompleteListener, SurfaceHolder.Callback, OnInfoListener {
	private static final String TAG = MoviePlayback.class.getSimpleName();

	/* 视频显示模式 */
	public static final int VIDEO_LAYOUT_ORIGIN = 0;
	// public static final int VIDEO_LAYOUT_SCALE = 1;
	public static final int VIDEO_LAYOUT_STRETCH = 2;
	public static final int VIDEO_LAYOUT_ZOOM = 3;
	public static final int VIDEO_LAYOUT_FIT_PARENT = 4;
	/* 视频状态 */
	public static final int STATE_ERROR = -1;
	public static final int STATE_IDLE = 0;
	public static final int STATE_PREPARING = 1;
	public static final int STATE_PREPARED = 2;
	public static final int STATE_PLAYING = 3;
	public static final int STATE_PAUSED = 4;
	public static final int STATE_PLAYBACK_COMPLETED = 5;
	/* 视频数据scheme */
	private static final String SCHEME_HTTP = "http";
	private static final String SCHEME_FILE = "file";

	// 当前播放器状态
	private int currentState = STATE_IDLE;
	// 播放器目标状态
	private int targetState = STATE_IDLE;
	// 视频默认显示模式
	private int videoLayout = VIDEO_LAYOUT_ORIGIN;
	// 视频宽度
	private int videoWidth;
	// 视频高度
	private int videoHeight;
	private MovieOrientation videoOrientation;
	private float videoAspectRatio;
	// 屏幕宽高比
	private float aspectRatio = 0;
	/*
	 * //播放数据列表 private List<ResourceInfo> data;
	 */
	// 当前选中视频index
	public static int itemSelectedIndex ;

	private Context ctx;
	private SurfaceView surfView;
	private SurfaceHolder holder;
	private MediaPlayer mediaPlayer;

	/* 播放相关事件回调 */
	private OnPreparedListener onPreparedListener;
	private OnCompletionListener onCompletionListener;
	private OnErrorListener onErrorListener;

	Long t1,t2,t3,t4,t5,t6,t7,t8,t9,t10,t11,t12,t13;//测试用
	// 视频方向
	private enum MovieOrientation {
		landscape, // 横屏
		portrait // 竖屏
	}

	public MoviePlayback(Context ctx, SurfaceView surfView) {
		this.ctx = ctx;
		this.surfView = surfView;
		this.holder = surfView.getHolder();
		holder.addCallback(this);
		// 设置像素模式，否则vitamio底层调用会crash
		holder.setFormat(PixelFormat.RGBA_8888);
	}


	@Override
	public void startPlay() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "===startPlay===");
		if (currentState == STATE_PLAYING) {// 正在播放
			return;
		} else if (currentState == STATE_PAUSED && mediaPlayer != null) {// 继续播放
			currentState = STATE_PLAYING;
			mediaPlayer.start();
			// 发送进度条更新msg
			if (ctx instanceof VideoPlayer) {
				((VideoPlayer) ctx).sendProgessMessage();
			}
			return;
		}
		t1 = System.currentTimeMillis();
		releaseMedia(false);
		pauseMusic();
		if (palylist != null && !palylist.isEmpty()) {
			if (itemSelectedIndex < 0 || itemSelectedIndex >= palylist.size()) {
				itemSelectedIndex = 0;
			}

			ResourceInfo info = palylist.get(itemSelectedIndex);
			/*
			 * if (!new File(info.path).exists()) { if (onErrorListener != null)
			 * onErrorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_IO,
			 * 0); return; }
			 */
			
			setVideoLayout(VIDEO_LAYOUT_ZOOM);
			//showProgress();
			try {
				t2 = System.currentTimeMillis();
				mediaPlayer = new MediaPlayer(ctx);
				t3 = System.currentTimeMillis();
				mediaPlayer.setScreenOnWhilePlaying(true);
				t4 = System.currentTimeMillis();
				mediaPlayer.setDataSource(ctx, Uri.parse(info.path));
				t5 = System.currentTimeMillis();
				mediaPlayer.setDisplay(holder);
				t6 = System.currentTimeMillis();
				//mediaPlayer.setBufferSize(4*1024*1024);
				//mediaPlayer.setCacheDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
				t7 = System.currentTimeMillis();
				showProgress();
				mediaPlayer.prepareAsync();
				currentState = STATE_PREPARING;
				targetState = STATE_PLAYING;

				// 播放结束监听
				mediaPlayer.setOnCompletionListener(this);
				// 准备就绪监听
				mediaPlayer.setOnPreparedListener(this);
				// 跳转播放监听
				mediaPlayer.setOnSeekCompleteListener(this);
				mediaPlayer.setOnVideoSizeChangedListener(this);
				mediaPlayer.setOnInfoListener(this);
				if (ctx instanceof Activity) {
					((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);
				}

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				currentState = STATE_ERROR;
				targetState = STATE_ERROR;
				if (onErrorListener != null)
					onErrorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			} catch (SecurityException e) {
				e.printStackTrace();
				currentState = STATE_ERROR;
				targetState = STATE_ERROR;
				if (onErrorListener != null)
					onErrorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				currentState = STATE_ERROR;
				targetState = STATE_ERROR;
				if (onErrorListener != null)
					onErrorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			} catch (IOException e) {
				e.printStackTrace();
				currentState = STATE_ERROR;
				targetState = STATE_ERROR;
				if (onErrorListener != null)
					onErrorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_IO, 0);
			}
		} else {
			LogCat.d("movie data is null");
		}
	}

	@Override
	public void seekTo(int msec) {
		if (mediaPlayer != null) {
			mediaPlayer.seekTo(msec);
		}
	}

	@Override
	public void pausePlay() {
		targetState = STATE_PAUSED;

		if (mediaPlayer != null) {
			mediaPlayer.pause();
			currentState = STATE_PAUSED;
		}
	}

	@Override
	public void stopPlay() {
		currentState = STATE_IDLE;
		targetState = STATE_IDLE;

		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	public boolean isPlaying() {
		return mediaPlayer != null ? mediaPlayer.isPlaying() : false;
	}

	public boolean isPause() {
		return currentState == STATE_PAUSED;
	}

	@Override
	public int getCurrentPosition() {
		int position = -1;
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			position = (int) mediaPlayer.getCurrentPosition();
		}
		return position;
	}

	@Override
	public int getDuration() {
		int duration = -1;
		if (mediaPlayer != null) {
			duration = (int) mediaPlayer.getDuration();
		}
		return duration;
	}

	@Override
	public int getItemSelectedIndex() {
		return itemSelectedIndex;
	}

	@Override
	public void next() {
		stopPlay();
		itemSelectedIndex++;
		if (itemSelectedIndex >= palylist.size()) {
			itemSelectedIndex = 0;
		}
		startPlay();
	}

	@Override
	public void prev() {
		stopPlay();
		itemSelectedIndex--;
		if (itemSelectedIndex < 0) {
			itemSelectedIndex = palylist.size() - 1;
		}
		startPlay();
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.i("testMsg", "onVideoSizeChanged");
		if (BuildConfig.DEBUG)
			Log.e(TAG, "===onVideoSizeChanged===");
		if (width == 0 || height == 0) {
			Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
			releaseMedia(true);
			if (onErrorListener != null)
				onErrorListener.onError(mp, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
		/*
		 * if(currentState == STATE_PLAYING) { if(BuildConfig.DEBUG) Log.d(TAG,
		 * "video size changed and video is palying"); return; }
		 */

		videoWidth = width;
		videoHeight = height;
		//videoOrientation = getMovieOrientation(getItemSelected().path);
		videoAspectRatio = mp.getVideoAspectRatio();
		adaptVideoWithScreen(width, height, videoOrientation);
		
		// holder.setFixedSize(videoWidth, videoHeight);
		if (targetState == STATE_PLAYING && currentState == STATE_PREPARED) {
			startVideoPlayback();
		} else {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "video size changed , wait play...");
		}
	}

	/**
	 * 进行视频与屏幕的适配
	 */
	private void adaptVideoWithScreen(int videoWidth, int videoHeight, MovieOrientation videoOrientation) {
		int surfWidth = 0;
		int surfHeight = 0;
		LayoutParams lp = surfView.getLayoutParams();
		Pair<Integer, Integer> res = ScreenResolution.getResolution(ctx);
		int windowWidth = res.first.intValue(); // 获取窗口宽
		int windowHeight = res.second.intValue(); // 获取窗口高
		float windowRatio = windowWidth / (float) windowHeight; // 计算窗口的宽高比
		float videoRatio = aspectRatio <= 0.01f ? videoAspectRatio : aspectRatio; // 视频宽高比
		surfHeight = videoHeight; // 初始值设置为传进来的视频高度
		surfWidth = videoWidth; // 初始值设置为传进来的视频宽度
		if(videoRatio==0){
			videoRatio=windowRatio;
		}
		Log.i("testMsg", "videoWidth = " + videoWidth);
		Log.i("testMsg", "videoHeight = " + videoHeight);
		Log.i("testMsg", "videoOrientation = " + videoOrientation);
		Log.i("testMsg", "videoRatio = " + videoRatio);
		Log.i("testMsg", "windowWidth = " + windowWidth);
		Log.i("testMsg", "windowHeight = " + windowHeight);
		Log.i("testMsg", "windowRatio = " + windowRatio);
		Log.i("testMsg", "videoLayout = " + videoLayout);
		
		if (VIDEO_LAYOUT_ORIGIN == videoLayout && surfWidth < windowWidth && surfHeight < windowHeight) {
			//Log.i("testMsg", "videoLayout = " + "VIDEO_LAYOUT_ORIGIN");
			lp.width = (int) (surfHeight * videoRatio);
			lp.height = surfHeight;
		} else if (videoLayout == VIDEO_LAYOUT_ZOOM) {
			//Log.i("testMsg", "videoLayout = " + "VIDEO_LAYOUT_ZOOM");
			
			if(videoRatio>=1&&windowRatio<1){
				lp.width = windowWidth;
				lp.height = (int) (windowWidth / videoRatio) ;
			}
            if(videoRatio<=1&&windowRatio>1){
            	lp.width = (int) (videoRatio * windowHeight);
				lp.height = windowHeight;
			}
            if((videoRatio<=1&&windowRatio<=1)||(videoRatio>1&&windowRatio>1)){
            	lp.width = windowWidth;
            	lp.height = windowHeight;
			}
			/*lp.width = windowRatio > videoRatio ? windowWidth : (int) (videoRatio * windowHeight);
			lp.height = windowRatio < videoRatio ? windowHeight : (int) (windowWidth / videoRatio);*/
		} else if (videoLayout == VIDEO_LAYOUT_FIT_PARENT) {
			ViewGroup parent = (ViewGroup) surfView.getParent();
			float parentRatio = ((float) parent.getWidth()) / ((float) parent.getHeight());
			lp.width = (parentRatio < videoRatio) ? parent.getWidth() : Math.round(((float) parent.getHeight()) * videoRatio);
			lp.height = (parentRatio > videoRatio) ? parent.getHeight() : Math.round(((float) parent.getWidth()) / videoRatio);
		} else {
			//Log.i("testMsg", "videoLayout = " + "VIDEO_LAYOUT_STRETCH");
			boolean full = videoLayout == VIDEO_LAYOUT_STRETCH;
			lp.width = (full || windowRatio < videoRatio) ? windowWidth : (int) (videoRatio * windowHeight);
			lp.height = (full || windowRatio > videoRatio) ? windowHeight : (int) (windowWidth / videoRatio);
		}
		surfView.setLayoutParams(lp);
		holder.setFixedSize(lp.width, lp.height);
		aspectRatio = videoAspectRatio;
		Log.i("testMsg", "lp.width = " + lp.width);
		Log.i("testMsg", "lp.height = " + lp.height);
	}


	@Override
	public void onPrepared(MediaPlayer mp) {
		hideProgress();
		t8 = System.currentTimeMillis();
		Log.i("testMsg","time2 - 1 = "+ Long.toString(t2-t1));
		Log.i("testMsg","time3 - 2 = "+ Long.toString(t3-t2));
		Log.i("testMsg","time4 - 3 = "+ Long.toString(t4-t3));
		Log.i("testMsg","time5 - 4 = "+ Long.toString(t5-t4));
		Log.i("testMsg","time6 - 5 = "+ Long.toString(t6-t5));
		Log.i("testMsg","time7 - 6 = "+ Long.toString(t7-t6));
		Log.i("testMsg","time8 - 7 = "+ Long.toString(t8-t7));

		if (BuildConfig.DEBUG)
			Log.e(TAG, "===onPrepared===");
		currentState = STATE_PREPARED;

		if (targetState == STATE_PLAYING && currentState == STATE_PREPARED) {
			startVideoPlayback();
		}

		if (onPreparedListener != null) {
			onPreparedListener.onPrepared(mp);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// 恢复状态
		currentState = STATE_PLAYBACK_COMPLETED;
		targetState = STATE_PLAYBACK_COMPLETED;
		//videoLayout = VIDEO_LAYOUT_ORIGIN;
		if (onCompletionListener != null) {
			onCompletionListener.onCompletion(mp);
		}
		releaseMedia(true);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		currentState = STATE_ERROR;
		targetState = STATE_ERROR;

		if (onErrorListener != null) {
			return onErrorListener.onError(mp, what, extra);
		}
		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		/*((VideoPlayer)ctx).resumePlay();
    	hideProgress();*/
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "===surfaceCreated===");

		/*Bitmap bitmap = getMoviewFrame(getCurrentMovie());
		if (bitmap != null) {
			Canvas c = holder.lockCanvas();
			c.drawBitmap(bitmap, (surfView.getWidth() - bitmap.getWidth()) / 2f, (surfView.getHeight() - bitmap.getHeight()) / 2f, null);
			holder.unlockCanvasAndPost(c);
		}*/
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		releaseMedia(true);
		resetParams();
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}


	public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
		this.onPreparedListener = onPreparedListener;
	}

	public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
		this.onCompletionListener = onCompletionListener;
	}

	public void setOnErrorListener(OnErrorListener onErrorListener) {
		this.onErrorListener = onErrorListener;
	}

	/**
	 * 设置视频显示模式
	 * 
	 * @param layout
	 *            视频模式
	 *            <p>
	 *            VIDEO_LAYOUT_ORIGIN
	 *            </p>
	 *            <p>
	 *            VIDEO_LAYOUT_SCALE
	 *            </p>
	 *            <p>
	 *            VIDEO_LAYOUT_STRETCH
	 *            </p>
	 *            <p>
	 *            VIDEO_LAYOUT_ZOOM
	 *            </p>
	 *            <p>
	 *            VIDEO_LAYOUT_FIT_PARENT
	 *            </p>
	 */
	public void setVideoLayout(int videoLayout) {
		this.videoLayout = videoLayout;

		adaptVideoWithScreen(videoWidth, videoHeight, videoOrientation);
	}

	/**
	 * 旋转屏幕
	 */
	public void rotateScreen() {
		Log.i("testMsg", "rotateScreen*************************");
		if(mediaPlayer!=null){
			videoAspectRatio = mediaPlayer.getVideoAspectRatio();
			adaptVideoWithScreen(videoWidth, videoHeight, videoOrientation);
		}

	}

	/**
	 * 正式播放视频
	 */
	private void startVideoPlayback() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "videoWidth-->" + videoWidth + "  videoHeight-->" + videoHeight);
		currentState = STATE_PLAYING;
		mediaPlayer.start();
	}

	/**
	 * 释放media资源
	 * 
	 * @param clearTargetState
	 *            是否清除目标播放状态
	 */
	public void releaseMedia(boolean clearTargetState) {
		currentState = STATE_IDLE;
		if (clearTargetState) {
			targetState = STATE_IDLE;
		}

		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	public void resetParams() {
		videoWidth = 0;
		videoHeight = 0;
		//itemSelectedIndex = -1;
	}

	/**
	 * 获取下一个视频资源
	 * 
	 * @return
	 */
	private ResourceInfo getNextMovie() {
		ResourceInfo info = null;
		if (palylist != null && !palylist.isEmpty()) {
			int nextIndex = itemSelectedIndex + 1;
			if (nextIndex >= palylist.size()) {
				nextIndex = 0;
			}
			info = palylist.get(nextIndex);
		}
		return info;
	}

	/**
	 * 获得当前视频资源
	 * 
	 * @return
	 */
	public ResourceInfo getCurrentMovie() {
		ResourceInfo info = null;
		if (palylist != null && !palylist.isEmpty()) {
			info = palylist.get(itemSelectedIndex);
		}
		return info;
	}

	/**
	 * 获取上一个视频资源
	 * 
	 * @return
	 */
	private ResourceInfo getPrevMovie() {
		ResourceInfo info = null;
		if (palylist != null && !palylist.isEmpty()) {
			int prevIndex = itemSelectedIndex - 1;
			if (prevIndex < 0) {
				prevIndex = palylist.size();
			}
			info = palylist.get(prevIndex);
		}
		return info;
	}

	/**
	 * 获取上一个视频第一帧图片
	 * 
	 * @return
	 */
	/*private Bitmap getPrevMovieFirstFrame() {
		ResourceInfo info = getPrevMovie();
		return getMoviewFrame(info);
	}*/

	/**
	 * 获取下一个视频第一帧图片
	 * 
	 * @return
	 */
	/*private Bitmap getNextMovieFirstFrame() {
		ResourceInfo info = getNextMovie();
		return getMoviewFrame(info);
	}*/

	/**
	 * 获取视频第一帧图片
	 * 
	 * @param info
	 *            资源对象
	 * @return bitmap
	 */
	/*private Bitmap getMoviewFrame(ResourceInfo info) {
		Bitmap bitmap = null;
		if (info != null) {
			try {
				MediaMetadataRetriever mmr = new MediaMetadataRetriever(ctx);
				mmr.setDataSource(info.path);
				bitmap = mmr.getFrameAtTime(1);
				mmr.release();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}*/

	/**
	 * 暂停音乐播放
	 */
	private void pauseMusic() {
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		ctx.sendBroadcast(i);
	}

	/**
	 * 获取视频文件方向
	 * 
	 * @param path
	 * @return
	 */
	private MovieOrientation getMovieOrientation(String path) {
		if (TextUtils.isEmpty(path)) {
			return MovieOrientation.landscape;
		}

		MovieOrientation mo = MovieOrientation.landscape;
		MediaMetadataRetriever mmr = null;
		try {
			mmr = new MediaMetadataRetriever(ctx);
			mmr.setDataSource(path);
			String rotation = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

			if (BuildConfig.DEBUG)
				Log.d(TAG,
						"vWidth-->" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) + "  vHeight-->"
								+ mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) + "  rotation-->" + rotation + "   path" + path);
			if ("90".equals(rotation))
				mo = MovieOrientation.portrait;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (mmr != null) {
				mmr.release();
			}
		}
		return mo;
	}

	@Override
	public void startPlayByIndex(int playIndex) {

	}

	public void setItemSelectedIndex(int itemSelectedIndex) {
		this.itemSelectedIndex = itemSelectedIndex;
	}

	@Override
	public void setVolume(float progress) {
		// TODO Auto-generated method stub
		
	}

	private ProgressDialog mPD ; 
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
            //开始缓存，暂停播放
        	showProgress();
        	((VideoPlayer)ctx).pausePlay();;
            break;
        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
            //缓存完成，继续播放
        	((VideoPlayer)ctx).resumePlay();
        	hideProgress();
            break;
        /*case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
            //缓存完成，继续播放
        	UIHelper.showToast(ctx, "NOT_SEEKABLE");
            break;*/

        }
        return true;
	}
	
	private void showProgress(){
		if(mPD==null){
			mPD = new ProgressDialog(ctx) ;
			mPD.setCancelable(true);
			mPD.setMessage("加载中...");
		}
        mPD.show();
	}
	
	private void hideProgress(){
		if(mPD!=null){
    		mPD.dismiss();
    	}
	}
	
}
