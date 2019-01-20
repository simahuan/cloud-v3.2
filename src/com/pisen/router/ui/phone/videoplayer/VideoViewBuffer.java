package com.pisen.router.ui.phone.videoplayer;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.CMediaController;
import io.vov.vitamio.widget.CMediaController.OnHiddenListener;
import io.vov.vitamio.widget.CMediaController.OnShownListener;
import io.vov.vitamio.widget.CVideoView;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.core.filemanager.ResourceInfo;

/**
 * 视频缓冲播放器
 * @author  mahuan
 * @version 1.0 2015年7月9日 下午2:38:55
 * <p>王姜强2015-10-14:修改睡眠后继续播放的问题</p>
 */
public class VideoViewBuffer extends Activity implements OnInfoListener, OnBufferingUpdateListener, View.OnClickListener {
	private View actionBar;
	private TextView txtFileName;
	private Button btnScreensSwitch;
	private CVideoView mVideoView;
	private CMediaController mediaController;
	private ProgressBar pb;
	private TextView downloadRateView, loadRateView;
	private ImageButton btnVideoLock;
	private boolean videoLocked = false;
	private static ResourceInfo curInfo;
	private static List<ResourceInfo> playlist;
	private static Context mContext;
	//记录home键按下时的位置
	private long currentPostion;
	//是否是home键按下
	private boolean isFinishing;
	
    /** 
     * screen状态广播接收者 
     */  
	public BroadcastReceiver mScreenBroadcastReceiver = new BroadcastReceiver() {
        private String action = null;  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            action = intent.getAction();  
            if(Intent.ACTION_SCREEN_ON.equals(action)){  
//            	mVideoView.resume();
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)){  
            	mVideoView.pause();
            }  
        }  
	};
	
    /** 
     * 启动screen状态广播接收器 
     */  
    private void registerScreenBroadcast(){  
        IntentFilter filter = new IntentFilter();  
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
//        filter.addAction(Intent.ACTION_SCREEN_ON);  
        filter.addAction(Intent.ACTION_SCREEN_OFF);  
        mContext.registerReceiver(mScreenBroadcastReceiver, filter);  
    }  


	public static void start(Context context, ResourceInfo info, List<ResourceInfo> playlist) {
		VideoViewBuffer.curInfo = info;
		VideoViewBuffer.playlist = playlist;
		mContext = context;
		context.startActivity(new Intent(context, VideoViewBuffer.class));
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.video_player);
		registerScreenBroadcast();
		
		actionBar = findViewById(R.id.actionBarVideo);
		txtFileName = (TextView) findViewById(R.id.txtFileName);
		mVideoView = (CVideoView) findViewById(R.id.buffer);
		pb = (ProgressBar) findViewById(R.id.probar);

		downloadRateView = (TextView) findViewById(R.id.download_rate);
		loadRateView = (TextView) findViewById(R.id.load_rate);
		btnVideoLock = (ImageButton) findViewById(R.id.btnVideoLock);
		btnVideoLock.setOnClickListener(this); //播放视频锁

		findViewById(R.id.btnBack).setOnClickListener(this);
		btnScreensSwitch = (Button) findViewById(R.id.btnScreensSwitch);
		btnScreensSwitch.setOnClickListener(new OnClickListener() { //横竖切屏
			@Override
			public void onClick(View v) {
				if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					mVideoView.setVideoLayout(CVideoView.VIDEO_LAYOUT_SCALE, 0);
					btnScreensSwitch.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.video_magnify, 0);
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					mVideoView.setVideoLayout(CVideoView.VIDEO_LAYOUT_SCALE, 0);
					btnScreensSwitch.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.video_reduce, 0);
				}
				mVideoView.toggleMediaControlsVisiblity();
			}
		});

		actionBar.setVisibility(View.GONE);
		btnVideoLock.setVisibility(View.GONE);
		mediaController = new VideoMediaController(this);
		mediaController.setOnShownListener(new OnShownListener() {
			@Override
			public void onShown() {
				actionBar.setVisibility(View.VISIBLE);
				btnVideoLock.setVisibility(View.VISIBLE);
			}
		});
		mediaController.setOnHiddenListener(new OnHiddenListener() {
			@Override
			public void onHidden() {
				actionBar.setVisibility(View.GONE);
				if (!videoLocked) {
					btnVideoLock.setVisibility(View.GONE);
				}else {
					mVideoView.hidenVideoLockBtn();
				}
			}
		});

		Uri uri = curInfo.getPathUri(); // Uri.parse(curInfo);
		txtFileName.setText(curInfo.name);
		mVideoView.setVideoURI(uri);
		mVideoView.setMediaController(mediaController);
		mVideoView.requestFocus();
		mVideoView.setOnInfoListener(this);
		mVideoView.setOnBufferingUpdateListener(this);
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mediaPlayer) {
				// optional need Vitamio 4.0
				mediaPlayer.setPlaybackSpeed(1.0f);
			}
		});
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				finish();
			}
		});
		mVideoView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				hideLoadingView();
				ConfirmDialog.show(VideoViewBuffer.this, "无法播放该视频", null, "确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

				return true;
			}
		});
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBack:
			finish();
			break;
		case R.id.btnVideoLock: //传对像过去
			videoLocked = !videoLocked;
			btnVideoLock.setImageResource(videoLocked ? R.drawable.video_close : R.drawable.video_open);
			mVideoView.setVideoLock(videoLocked);
			mVideoView.setVideoLockObject(btnVideoLock);
			if (videoLocked) {
				mediaController.hide();
			}
			break;
		}
	}

	/**
	 * @des 前一视频
	 */
	private void prevVideo() {
		if (playlist == null || playlist.isEmpty())
			return;

		int ind = playlist.indexOf(curInfo);
		if (ind != -1) {
			curInfo = getVideoResourceInfo(--ind);
		}

		restPlay();
	}

	/**
	 * @des　后一视频
	 */
	private void nextVideo() {
		if (playlist == null || playlist.isEmpty())
			return;

		int ind = playlist.indexOf(curInfo);
		
		if (ind != -1) {
			curInfo = getVideoResourceInfo(++ind);
		}

		restPlay();
	}

	/**
	 * @des 
	 */
	private void restPlay() {
		if (curInfo != null) {
			mVideoView.stopPlayback();
			txtFileName.setText(curInfo.name);
			mVideoView.setVideoURI(curInfo.getPathUri());
		}
	}

	private ResourceInfo getVideoResourceInfo(int index) {
		if (playlist == null || playlist.isEmpty())
			return null;

		if (index < 0) {
			index = playlist.size() - 1;
		} else if (index > playlist.size() - 1) {
			index = 0;
		}
		return playlist.get(index);
	}
	
	@Override
	protected void onDestroy() {
		VideoViewBuffer.curInfo = null;
		VideoViewBuffer.playlist = null;
		if (mScreenBroadcastReceiver != null)
			unregisterReceiver(mScreenBroadcastReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			if (mVideoView.isPlaying()) {
				mVideoView.pause();
				showLoadingView();
			}
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			mVideoView.start();
			hideLoadingView();
			break;
		case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
			downloadRateView.setText(extra + "kb/s");
			break;
		}
		return true;
	}

	private void showLoadingView() {
		pb.setVisibility(View.VISIBLE);
		downloadRateView.setText("0kb/s");
		loadRateView.setText("0%");
		downloadRateView.setVisibility(View.VISIBLE);
		loadRateView.setVisibility(View.VISIBLE);
	}

	private void hideLoadingView() {
		pb.setVisibility(View.GONE);
		downloadRateView.setVisibility(View.GONE);
		loadRateView.setVisibility(View.GONE);
	}

	/**
	 * 系统缓冲更新
	 */
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		loadRateView.setText(percent + "%");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(currentPostion > 0){
			mVideoView.seekTo(currentPostion);
			currentPostion = 0;
		}
	}
	
	@Override
	public void finish() {
		super.finish();
		isFinishing = true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(!isFinishing){
			currentPostion = mVideoView.getCurrentPosition();
		}
	}
	/**
	 * 视频媒体播放控件器
	 * @author  mahuan
	 * @version 1.0 2015年7月9日 下午3:08:52
	 */
	class VideoMediaController extends CMediaController implements View.OnClickListener {

		public VideoMediaController(Context context) {
			super(context);
		}

		public VideoMediaController(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected View makeControllerView() {
			View rootView = getLayoutInflater().inflate(R.layout.video_controller, this);
			initView(getContext());
			return rootView;
		}

		private void initView(Context context) {
			findViewById(R.id.btnPrev).setOnClickListener(this);
			findViewById(R.id.btnNext).setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnPrev:
				prevVideo();
				break;
			case R.id.btnNext:
				nextVideo();
				break;
			default:
				break;
			}
		}
	}
}
