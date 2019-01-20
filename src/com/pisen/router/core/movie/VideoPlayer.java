package com.pisen.router.core.movie;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.utils.StringUtils;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceInfo.RSource;

public class VideoPlayer extends Activity implements OnClickListener, OnPreparedListener, OnCompletionListener {
	private static final int sDefaultTimeout = 3000;
	private static final int FADE_OUT = 1;
	private static final int SHOW_PROGRESS = 2;
	private static final int FADE_IN = 3;

	private boolean mShowing;
	private boolean mDragging;
	private boolean mInstantSeeking = false;
	private boolean mLock = false; // 锁屏标志
	private boolean mZoom = false; // fase 原始大小 ： true 全屏

	private long mDuration;
	private long mPosition;
	private AudioManager mAM;
	private MoviePlayback mPlayer;
	private SurfaceView surfaceView;

	// private FrameLayout mainFrame;
	private ImageView videoPlay; // 播放
	private ImageView videoPause; // 暂停
	private ImageView videoLastOne; // 上一个
	private ImageView videoNextOne; // 下一个
	private ImageView videoLock; // 屏锁
	private ImageView videoNavBack; // 返回
	private ImageView videoZoom; // 全屏--原始大小
	private TextView videoPlayName; // 视频文件名
	private SeekBar mProgress;
	private TextView mEndTime, mCurrentTime;
	LinearLayout videoPlayProgress;
	LinearLayout videoPlayControl;
	LinearLayout videoPlayerNav;
	LinearLayout videoBottomCtr;

	private static List<ResourceInfo> playlist;
	private static ResourceInfo curInfo;
	//Long time1,time2,time3,time4;
	private Handler mHander = new Handler();
	private OnErrorListener mErrorListener = new OnErrorListener() {
	    public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
	        String message = framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ? "无效的资源": "未知错误";
            UIHelper.showToast(VideoPlayer.this, message);
	        finish();
	       return true;
	    }
	  };
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			long pos;
			switch (msg.what) {
			case FADE_IN:
				show(sDefaultTimeout);
				break;
			case FADE_OUT:
				hide();
				break;
			case SHOW_PROGRESS:
				pos = setProgress();
				if (!mDragging /* && mShowing */) {
					msg = obtainMessage(SHOW_PROGRESS);
					sendMessageDelayed(msg, 1000 - (pos % 1000));
					/* updatePausePlay(); */
				}
				break;
			}
		}
	};

	public static void start(Context context, ResourceInfo info, List<ResourceInfo> playlist) {
		VideoPlayer.playlist = playlist;
		curInfo = info;
		context.startActivity(new Intent(context, VideoPlayer.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 初始化Vitamio
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		// 设置全屏，无标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/*
		 * if (this.getResources().getConfiguration().orientation ==
		 * Configuration.ORIENTATION_PORTRAIT) {
		 * setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); }
		 */
		setContentView(R.layout.video_player_old);
		surfaceView = (SurfaceView) findViewById(R.id.surface);
		mPlayer = new MoviePlayback(this, surfaceView);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnCompletionListener(this); 
		mPlayer.setOnErrorListener(mErrorListener);
		initView();
		mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		
		  /*//测试数据
		  LocalResourceManager lrm = new LocalResourceManager(this) ;
		  //SardineResourceManager srm = new SardineResourceManager(); //
		  playlist = lrm.listRecursively("/storage/sdcard0/", FileType.Video); 
		  //playlist = srm.listRecursively("http://192.168.168.1/uploads/KINGSTON(sda1)/",FileType.Video); 
		  curInfo = playlist.get(0);*/
		 
		
		if (curInfo == null||playlist==null||playlist.isEmpty()) {
			finish();
			return;
		}
		int idx = getVideoIndex();
		initVideos();
//		time2 =  System.currentTimeMillis();
//		Log.i("testMsg","time2 - time1 = "+ Long.toString(time2-time1));
		if (curInfo.source == RSource.Local) {// 本地
			File file = new File(playlist.get(idx).path);
			if (!file.exists() || file.length() <= 0) {// 文件长度<=0
				UIHelper.showToast(this, "该视频不可播放");
			} else {
				// 播放本地视频(延时播放)
				/*mHander.postDelayed(new Runnable() {
					@Override
					public void run() {
						videoPlay.performClick();
					}
				}, 1000);*/
				videoPlay.performClick();
			}
		} else {// 云端
				// 播放云端视频(延时播放)
			/*mHander.postDelayed(new Runnable() {
				@Override
				public void run() {
					videoPlay.performClick();
				}
			}, 1000);*/
			videoPlay.performClick();
		}

	}

	private void initVideos() {
		if (playlist != null && curInfo != null && !playlist.isEmpty()) {
			// 设置播放数据
			mPlayer.setDataSource(playlist);
			mPlayer.setItemSelectedIndex(getVideoIndex());
		}
	}

	private int getVideoIndex() {
		if (playlist != null && curInfo != null && !playlist.isEmpty()) {
			for (int i = 0; i < playlist.size(); i++) {
				if (playlist.get(i).path.equals(curInfo.path)) {
					return i;
				}
			}
		}
		return -1;
	}

	private void initView() {
		videoPlay = (ImageView) findViewById(R.id.videoPlay);
		videoPlay.setOnClickListener(this);
		videoPause = (ImageView) findViewById(R.id.videoPause);
		videoPause.setOnClickListener(this);
		videoLastOne = (ImageView) findViewById(R.id.videoLastOne);
		videoLastOne.setOnClickListener(this);
		videoNextOne = (ImageView) findViewById(R.id.videoNextOne);
		videoNextOne.setOnClickListener(this);
		videoLock = (ImageView) findViewById(R.id.videoLock);
		videoLock.setOnClickListener(this);
		videoNavBack = (ImageView) findViewById(R.id.videoNavBack);
		videoNavBack.setOnClickListener(this);
		videoZoom = (ImageView) findViewById(R.id.videoZoom);
		videoZoom.setOnClickListener(this);
		videoPlayName = (TextView) findViewById(R.id.videoPlayName);
		mProgress = (SeekBar) findViewById(R.id.mediacontroller_seekbar);
		mProgress.setOnSeekBarChangeListener(mSeekListener);
		mEndTime = (TextView) findViewById(R.id.videoDuration);
		mCurrentTime = (TextView) findViewById(R.id.videoProgress);
		videoPlayProgress = (LinearLayout) findViewById(R.id.videoPlayProgress);
		videoPlayControl = (LinearLayout) findViewById(R.id.videoPlayControl);
		videoPlayerNav = (LinearLayout) findViewById(R.id.videoPlayerNav);
		videoBottomCtr = (LinearLayout) findViewById(R.id.videoBottomCtr);
		// mainFrame = (FrameLayout) findViewById(R.id.videoPlayerContent);
	}

	public void sendProgessMessage() {
		mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 100);
	}

	@Override
	protected void onDestroy() {
		mHandler.removeMessages(SHOW_PROGRESS);
		//playlist = null;
		//curInfo = null;
		if(mPlayer!=null){
			mPlayer.releaseMedia(true);
			mPlayer.resetParams();
		}
		super.onDestroy();
	}

	private long setProgress() {
		if (mPlayer == null || mDragging)
			return 0;

		long position = mPlayer.getCurrentPosition();
		long duration = mPlayer.getDuration();
		//Log.i("testMsg", "positon:duration " + position + ":" + duration);
		if (mProgress != null) {
			if (position >= 0 && duration > 0) {
				long pos = 1000L * position / duration;
				mProgress.setProgress((int) pos);
				mPosition = position;
			} else {
				mProgress.setProgress((int) (1000L * mPosition / duration));
			}
		}

		mDuration = duration;

		if (mEndTime != null)
			mEndTime.setText(StringUtils.generateTime(mDuration));
		if (mCurrentTime != null)
			mCurrentTime.setText(StringUtils.generateTime(position));

		return position;
	}

	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			mDragging = true;
			show(3600000);
			mHandler.removeMessages(SHOW_PROGRESS);
			if (mInstantSeeking)
				mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);

		}

		public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
			if (!fromuser)
				return;

			long newposition = (mDuration * progress) / 1000;
			String time = StringUtils.generateTime(newposition);
			if (mInstantSeeking)
				mPlayer.seekTo((int) newposition);

		}

		public void onStopTrackingTouch(SeekBar bar) {
			/* if (!mInstantSeeking) */
			mPosition = (mDuration * bar.getProgress()) / 1000;
			mPlayer.seekTo((int) mPosition);
			setProgress();

			show(sDefaultTimeout);
			mHandler.removeMessages(SHOW_PROGRESS);
			mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);
			mDragging = false;
			mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
		}
	};

	public void show(int timeout) {
		if (!mShowing) {
			videoPlayerNav.setVisibility(View.VISIBLE);
			videoBottomCtr.setVisibility(View.VISIBLE);
			mShowing = true;
		}
		// mHandler.sendEmptyMessage(SHOW_PROGRESS);

		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT);
			mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
		}
	}

	private void togglePlayAndPause(boolean b) {
		if (b) {
			videoPlay.setVisibility(View.GONE);
			videoPause.setVisibility(View.VISIBLE);
		} else {
			videoPlay.setVisibility(View.VISIBLE);
			videoPause.setVisibility(View.GONE);
		}
	}

	public boolean isShowing() {
		return mShowing;
	}

	public void hide() {
		if (mShowing) {
			// 隐藏上边导航与下边播放
			videoPlayerNav.setVisibility(View.GONE);
			videoBottomCtr.setVisibility(View.GONE);
			mShowing = false;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.videoPlay:// 播放
			resumePlay();
			break;
		case R.id.videoPause: // 暂停
			pausePlay();
			break;

		case R.id.videoLastOne:// 上一个
			mPlayer.prev();
			videoPlayName.setText(mPlayer.getCurrentMovie().name);// 设置导航的播放文件名
			togglePlayAndPause(true);
			break;
		case R.id.videoNextOne:// 下一个
			mPlayer.next();
			videoPlayName.setText(mPlayer.getCurrentMovie().name);// 设置导航的播放文件名
			togglePlayAndPause(true);
			break;
		case R.id.videoLock:// 屏幕锁
			toggleLock();
			break;
		case R.id.videoNavBack:// 返回
			finish();
			break;
		case R.id.videoZoom: // 全屏
			toggleZoom();
			break;
		}

	}
	
	public void pausePlay(){
		if (mPlayer.isPlaying()) {
			mPlayer.pausePlay();
			togglePlayAndPause(false);
			mHandler.removeMessages(SHOW_PROGRESS);
		}
	}
	
	public void resumePlay(){
		mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (mPlayer.isPlaying()) {
			return;
		}
		mPlayer.startPlay();
		videoPlayName.setText(mPlayer.getCurrentMovie().name);// 设置导航的播放文件名
		mShowing = true;
		togglePlayAndPause(true);
	}

	private void toggleZoom() {
		if (!mZoom) {// 原始大小，设置全屏
			mZoom = true;
			videoZoom.setImageResource(R.drawable.video_reduce);
			mPlayer.setVideoLayout(mPlayer.VIDEO_LAYOUT_ZOOM);
		} else {// 全屏，恢复原始大小
			mZoom = false;
			videoZoom.setImageResource(R.drawable.video_magnify);
			mPlayer.setVideoLayout(mPlayer.VIDEO_LAYOUT_ORIGIN);
		}
	}

	private void toggleLock() {
		if (!mLock) {// 处于未锁定的状态，执行锁定操作
			mLock = true;
			videoLock.setImageResource(R.drawable.video_close);
		} else { // 处于锁定状态 ，执行解锁操作
			mLock = false;
			videoLock.setImageResource(R.drawable.video_open);
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// mainFrame.setVisibility(View.VISIBLE);
		Message msg = Message.obtain(mHandler, SHOW_PROGRESS);
		mHandler.sendMessage(msg);
		// 设置初始全屏
		mZoom = true;
		videoZoom.setImageResource(R.drawable.video_reduce);
		mPlayer.setVideoLayout(mPlayer.VIDEO_LAYOUT_ZOOM);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// 获取锁屏锁的在屏幕的位置rect
		Rect rect = new Rect(0, 0, 0, 0);
		videoLock.getGlobalVisibleRect(rect);
		if (rect.contains((int) ev.getX(), (int) ev.getY())) { // 如果事件点击到屏幕锁位置
			if (!mLock) {
				mHandler.removeMessages(FADE_OUT);
				hide();
			} else {
				show(sDefaultTimeout);
			}
			// 传播事件
			return super.dispatchTouchEvent(ev);
		}
		if (mLock) {
			// 阻止传播事件
			return true;
		} else {
			// if (mShowing) {
			// /*mHandler.removeMessages(FADE_OUT);
			// mHandler.sendEmptyMessageDelayed(FADE_OUT, 100);*/
			// } else {
			// mHandler.removeMessages(FADE_OUT);
			// mHandler.sendEmptyMessageDelayed(FADE_IN, 100);
			// }

			mHandler.removeMessages(FADE_OUT);
			mHandler.sendEmptyMessageDelayed(FADE_IN, 100);

		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// 恢复按键初始状态
		togglePlayAndPause(false);
		// 显示控制板
		show(0);
		// 恢复状态
		mShowing = true;
		mDragging = false;
		// 调整进度条到末端
		mProgress.setProgress(970);
		mCurrentTime.setText(StringUtils.generateTime(mDuration));
		mHandler.removeMessages(SHOW_PROGRESS);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//Log.i("testMsg", "onConfigurationChanged");
		mPlayer.rotateScreen();
		/*
		 * if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE) { //横向
		 * //setContentView(R.layout.file_list_landscape); } else { //竖向 //
		 * setContentView(R.layout.file_list); }
		 */
	}
	@Override
	protected void onStop() {
		 pausePlay();
		super.onStop();
	}
	@Override
	protected void onRestart() {
		//mPlayer.setCurrentState(mPlayer.STATE_PAUSED);
		resumePlay();
		super.onRestart();
	}
	
}
