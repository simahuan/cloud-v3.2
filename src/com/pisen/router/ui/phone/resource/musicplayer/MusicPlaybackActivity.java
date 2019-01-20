package com.pisen.router.ui.phone.resource.musicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.Device;
import org.cybergarage.xml.Node;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.studio.os.LogCat;
import android.studio.util.URLUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.charon.dmc.engine.DLNAContainer;
import com.charon.dmc.engine.DLNAContainer.DeviceChangeListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceInfo.RSource;
import com.pisen.router.core.music.CyberLinkMusicPlayback;
import com.pisen.router.core.music.DLNAManager;
import com.pisen.router.core.music.IMusicPlaybackListener;
import com.pisen.router.core.music.Music;
import com.pisen.router.core.music.MusicPlayService;
import com.pisen.router.core.music.MusicPlayServiceFactory;
import com.pisen.router.core.music.MusicPlayServiceFactory.OnBindListener;
import com.pisen.router.core.music.MusicPlaybackFactory.Type;
import com.pisen.router.core.playback.MusicPlayback;
import com.pisen.router.core.playback.PlayStatus;
import com.pisen.router.ui.base.CloudActivity;

/**
 * 音乐播放
 * 
 * @author ldj
 * @version 1.0 2015年5月13日 上午11:08:15
 */
public class MusicPlaybackActivity extends CloudActivity implements OnClickListener, OnItemClickListener, OnSeekBarChangeListener, DeviceChangeListener,
		IMusicPlaybackListener {

	private MusicPlayServiceFactory musicPlayFactory;
	private MusicPlayback musicPlayer;
	private MusicPlayService musicPlayService;
	// 默认播放器类型
	private Type musicPlaybackType = Type.Local;

	private TextView titleView;
	private TextView artistNameView;
	private TextView totalTimeView;
	private TextView curTimeView;
	private SeekBar seekBar;
	private ImageButton backButton;
	private ImageButton dlnaButton;
	private ImageButton preButton;
	private ImageButton playButton;
	private ImageButton nextButton;
	// cd侧边滑块
	private ImageView slideView;
	// 专辑封面
	private ImageView albumArtView;
	// cd旋转动画视图
	private View cdLayout;
	// 播放设备选择窗体
	private MusicDeviceSelectPopupWindow popupWindow;
	/* 动画 */
	private ObjectAnimator cdAnimation;
	private ObjectAnimator slideAnimation;
	private ObjectAnimator slideOutAnimation;

	private static List<ResourceInfo> playlist;
	private static ResourceInfo curInfo;
	private List<Music> data;
	// 首次播放位置
	private int firstPlayIndex;
	private AudioManager audioManager;
	private VolumeBroadcastReceiver volumeReceiver;

	public static void start(Context context, ResourceInfo info, List<ResourceInfo> playlist) {
		MusicPlaybackActivity.playlist = playlist;
		curInfo = info;
		context.startActivity(new Intent(context, MusicPlaybackActivity.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.musicplayback_activity);

		findView();
		initView();
		initMusic();
		DLNAContainer.getInstance().clear();
		// 扫描dlna设备
		DLNAManager.startSearch(this);
		registerVolumeReceiver();
	}

	@Override
	protected void onDestroy() {
		DLNAManager.stopSearch(this);

		playlist = null;
		curInfo = null;
		relaseMusicPlayer();

		if (musicPlayFactory != null) {
			musicPlayFactory.unBindService(this);
		}

		if (volumeReceiver != null) {
			unregisterReceiver(volumeReceiver);
		}
		super.onDestroy();
	}

	/**
	 * 注册当音量发生变化时接收的广播
	 */
	private void registerVolumeReceiver() {
		volumeReceiver = new VolumeBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
		registerReceiver(volumeReceiver, filter);
	}

	private void findView() {
		titleView = (TextView) findViewById(R.id.txtTitle);
		artistNameView = (TextView) findViewById(R.id.txtName);
		curTimeView = (TextView) findViewById(R.id.txtCurTime);
		totalTimeView = (TextView) findViewById(R.id.txtTotalTime);
		seekBar = (SeekBar) findViewById(R.id.sBarMusic);
		backButton = (ImageButton) findViewById(R.id.ibtnBack);
		dlnaButton = (ImageButton) findViewById(R.id.ibtnDlna);
		preButton = (ImageButton) findViewById(R.id.ibtnPre);
		playButton = (ImageButton) findViewById(R.id.ibtnPlay);
		nextButton = (ImageButton) findViewById(R.id.ibtnNext);
		slideView = (ImageView) findViewById(R.id.imgSlide);
		albumArtView = (ImageView) findViewById(R.id.imgCd_center);
		cdLayout = findViewById(R.id.cdLayout);
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		backButton.setOnClickListener(this);
		dlnaButton.setOnClickListener(this);
		preButton.setOnClickListener(this);
		playButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		seekBar.setOnSeekBarChangeListener(this);
		DLNAManager.setOnDeviceChangListener(this);

		cdAnimation = ObjectAnimator.ofFloat(cdLayout, "rotation", 0, 360).setDuration(17000);
		cdAnimation.setRepeatCount(-1);
		cdAnimation.setInterpolator(new LinearInterpolator());
		slideAnimation = ObjectAnimator.ofFloat(slideView, "rotation", 50, 0).setDuration(500);
		slideOutAnimation = ObjectAnimator.ofFloat(slideView, "rotation", 0, 50).setDuration(500);
	}

	/**
	 * 进行音乐播放相关初始化
	 */
	private void initMusic() {
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

		musicPlayFactory = new MusicPlayServiceFactory();
		musicPlayFactory.bindService(this, new OnBindListener() {

			@Override
			public void onBind(MusicPlayService service) {
				musicPlayService = service;
				if (musicPlayService.isPlaying()) {
					musicPlayService.release();
				}

				musicPlayer = musicPlayService.getMusicPlayer(musicPlaybackType);
				musicPlayer.setOnMusicPlaybackListener(MusicPlaybackActivity.this);
				data = initMusicData();
				if (data != null && !data.isEmpty()) {
					musicPlayer.setDataSource(new ArrayList<Music>(data));
					musicPlayer.startPlayByIndex(firstPlayIndex);
//					showPlayAnimation(true);
					refreshMusicView(musicPlayer.getItemSelected());
				} else {
					UIHelper.showToast(MusicPlaybackActivity.this, "获取数据异常");
					finish();
				}
			}
		});
	}

	/**
	 * 初始化音乐数据
	 * 
	 * @return
	 */
	protected List<Music> initMusicData() {
		List<Music> data = null;
		if (playlist != null && !playlist.isEmpty()) {
			data = new ArrayList<Music>();
			int size = playlist.size();
			ResourceInfo info = null;
			Music m = null;
			for (int i = 0; i < size; i++) {
				info = playlist.get(i);

				if (info.source == RSource.Remote) {
					m = getNetMusic(info.path);
				} else {
					m = getLocalMusic(info.path);
				}
				if (m != null) {
					data.add(m);
					if (info.path.equals(curInfo.path)) {
						firstPlayIndex = data.size() - 1;// firstPlayIndex = i;
															// (有可能在媒体库搜索不出某些歌曲)
					}
				}
			}
		}
		return data;
	}

	/**
	 * 从intent获取需要播放的音乐数据
	 * 
	 * @return
	 */
	protected Music generateDataFromIntent() {
		Music music = null;
		Intent intent = getIntent();
		if (intent != null && intent.getData() != null) {
			String uri = intent.getData().toString();
			if (URLUtil.isFileUrl(uri)) {// 本地音乐
				music = getLocalMusic(uri);
			} else if (URLUtil.isNetworkUrl(uri)) {// 网络音乐
				music = getNetMusic(uri);
			}
		}
		return music;
	}

	private Music getNetMusic(String uri) {
		Music music = null;
		;
		if (!TextUtils.isEmpty(uri)) {
			music = new Music();
			music.setMusicName(uri.substring(uri.lastIndexOf("/") + 1));
			music.setSavePath(URLUtils.encodeURL(uri));
			music.setAlbumName("未知");
			music.setSinger("未知");
			music.setAlbumId(-1);
			music.setId(-1);
		}

		return music;
	}

	public Music getLocalMusic(String uriString) {
		Uri uri = Uri.parse(uriString);
		String path = uri.getPath();
		File file = new File(path);
		Music music = null;
		if (file.exists()) {
			if (path.contains("'")) {
				String tempStr = path.substring(0, path.indexOf("'")) + "''" + path.subSequence(path.indexOf("'") + 1, path.length());
				path = tempStr;
			}
			if (path != null) {
				ContentResolver cr = this.getContentResolver();
				StringBuffer buff = new StringBuffer();
				buff.append("(").append(Audio.AudioColumns.DATA).append("=").append("'" + path + "'").append(")");
				Cursor c = null;
				try {
					c = cr.query(Audio.Media.EXTERNAL_CONTENT_URI, null, buff.toString(), null, null);
					for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
						music = new Music();
						// 取得音乐的名字
						music.setId(c.getInt(c.getColumnIndex("_id")));
						music.setMusicName(file.getName());// (c.getString(c.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
						music.setSavePath(c.getString(c.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
						music.setAlbumName(c.getString(c.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
						music.setSinger(c.getString(c.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)));
						music.setTime(c.getString(c.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
						music.setAlbumkey(c.getString(c.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_KEY)));
						music.setAlbumId(c.getInt(c.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)));
						// music.setAlbumPath(c.getString(c.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART)));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					if (c != null) {
						c.close();
					}
				}
			}
		}

		return music;
	}

	public void setVolume(float progress) {
		if (musicPlayer != null) {
			musicPlayer.setVolume(progress);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibtnPlay:
			if (musicPlayer.isPlaying()) {
				musicPlayer.pausePlay();
			} else {
				musicPlayer.startPlay();
//				showPlayAnimation(false);
			}
			break;
		case R.id.ibtnPre:
			playPreMusic();
			break;
		case R.id.ibtnNext:
			playNextMusic();
			break;
		case R.id.ibtnBack:
			finish();
			break;
		case R.id.ibtnDlna:
			// 扫描设备
			DLNAManager.startSearch(this);
			showDeviceChoseWindow(v);
			break;
		default:
			break;
		}
	}

	/**
	 * 显示dlna设备选择窗口
	 */
	private void showDeviceChoseWindow(View anchor) {
		if (popupWindow == null) {
			popupWindow = new MusicDeviceSelectPopupWindow(this);
			popupWindow.setOnItemClickListener(this);
		}

		/* 修正后台播放时播放器为dlna设备，再次进入时默认选项问题 */
		if (musicPlayer instanceof CyberLinkMusicPlayback) {
			popupWindow.setData(getDevicsWithLocal(), ((CyberLinkMusicPlayback) musicPlayer).getRemoteDevice());
		} else {
			popupWindow.setData(getDevicsWithLocal());
		}
		popupWindow.showAsDropDown(anchor, -(getResources().getDimensionPixelOffset(R.dimen.popupwindow_music_width) - anchor.getWidth()), 20);
	}

	/**
	 * 获取设备列表数据，默认添加本机设备
	 * 
	 * @return
	 */
	private List<Device> getDevicsWithLocal() {
		List<Device> data = new ArrayList<Device>(DLNAContainer.getInstance().getDevices());
		/* 增加本机选项 */
		Device d = new Device();
		d.setDeviceNode(new Node());
		d.setFriendlyName(Build.MODEL);
		data.add(0, d);
		return data;
	}

	/**
	 * 设置设备列表数据｜可能出现发现不了推送设备
	 */
	private void setPopupWindowData() {
		// 修正后台播放时，播放器为dlna丢失选中项问题
		if (musicPlayer instanceof CyberLinkMusicPlayback) {
			popupWindow.setData(getDevicsWithLocal(), ((CyberLinkMusicPlayback) musicPlayer).getRemoteDevice());
		} else {
			popupWindow.setData(getDevicsWithLocal());
		}
	}

	/**
	 * 跳转到指定进度
	 * 
	 * @param progress
	 */
	private void seekTo(final int progress) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (musicPlayer != null && progress > 0) {
					int duration = musicPlayer.getDuration();
					if (duration > 0 ) {
						int seekToTime = duration * progress / 100;
						 if(seekToTime >= duration) {
							 seekToTime = duration -3*1000;//防止直接拖到结束位置，界面不刷新
						 }
						musicPlayer.seekTo(seekToTime);
					}
				}
			}
		}).start();
	}

	/**
	 * 监听可播放设备
	 */
	@Override
	public void onDeviceChange(Device device) {
		// DLNA设备变化，如设备选择窗口可见，则刷新
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (popupWindow != null && popupWindow.isShowing()) {
					
					setPopupWindowData();
				}
			}
		});
	}

	private int playerIndex;
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		popupWindow.dismiss();
		if(playerIndex != position) {
			playerIndex = position;
			if (position == 0) {// 选择本机
				playMusicByLoacle();
			} else {// DLNA播放
				DLNAContainer.getInstance().setSelectedDevice(DLNAContainer.getInstance().getDevices().get(--position));
				playMusicByDLNA();
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
//		if(musicPlayer == null || !musicPlayer.isPlaying()) {
//			seekBar.setEnabled(false);
//			Toast.makeText(this, "已停止播放", Toast.LENGTH_SHORT).show();
//		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// 进度跳转
		seekTo(seekBar.getProgress());
	}

	/**
	 * 使用本地播放器播放
	 */
	private void playMusicByLoacle() {
		int position = 0;
//		int playPosition = 0;
		// 保留当前播放位置
		if (musicPlayer != null) {
			position = musicPlayer.getItemSelectedIndex();
//			playPosition = musicPlayer.getCurrentPosition();
			relaseMusicPlayer();
		}

		musicPlayer = musicPlayService.getMusicPlayer(musicPlaybackType);
		musicPlayer.setOnMusicPlaybackListener(this);
		musicPlayer.setDataSource(new ArrayList<Music>(data));
		musicPlayer.startPlayByIndex(position);
//		musicPlayer.seekTo(playPosition);	//切换播放器时，可能出现onerror
//		showPlayAnimation(true);
	}

	/**
	 * 使用DLNA设备播放
	 */
	private void playMusicByDLNA() {
		// 保留当前播放位置
		int position = 0;
//		int playPosition = 0;
		if (musicPlayer != null) {
			position = musicPlayer.getItemSelectedIndex();
//			playPosition = musicPlayer.getCurrentPosition();
			relaseMusicPlayer();
		}

		musicPlayer = musicPlayService.getMusicPlayer(Type.CyberLink);
		((CyberLinkMusicPlayback) musicPlayer).setRemoteDevice(DLNAContainer.getInstance().getSelectedDevice());
		((CyberLinkMusicPlayback) musicPlayer).subscribe();
		musicPlayer.setOnMusicPlaybackListener(this);
		musicPlayer.setDataSource(new ArrayList<Music>(data));
		musicPlayer.startPlayByIndex(position);
//		musicPlayer.seekTo(playPosition);
//		showPlayAnimation(true);
	}

	/**
	 * 释放播放器资源
	 */
	private void relaseMusicPlayer() {
		if (musicPlayer != null) {
			musicPlayer.setOnMusicPlaybackListener(null);
			musicPlayer.release();
			musicPlayer = null;
		}
	}

	/**
	 * 重置时间视图
	 */
	private void resetPlayTime() {
		curTimeView.setText("00:00");
		totalTimeView.setText("00:00");
		seekBar.setProgress(0);
	}

	@Override
	public void onProgressUpdate(final int progress, final String curTime, final String totalTime) {
//		LogCat.e("onProgressUpdate->%s//%s",curTime , totalTime);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				seekBar.setProgress(progress);
				curTimeView.setText(curTime);
				totalTimeView.setText(totalTime);
			}
		});
	}

	/**
	 * 显示开始播放动画
	 * 
	 * @param reset
	 *            是否重置cd开始旋转位置
	 */
	private synchronized void showPlayAnimation(boolean reset) {
		if (!cdAnimation.isRunning()) {
			/* 设置旋转中心 */
			slideView.setPivotX(slideView.getWidth());
			slideView.setPivotY(slideView.getHeight() - 10);
			ViewHelper.setRotation(slideView, 50);
			slideView.setVisibility(View.VISIBLE);
			slideAnimation.start();
			
			// 修正起始播放位置
			if (reset) {
				cdAnimation.setFloatValues(0, 360);
			} else {
				cdAnimation.setFloatValues(cdLayout.getRotation(), cdLayout.getRotation() + 360);
			}
			cdAnimation.start();
		}
	}

	/**
	 * 显示停止播放动画
	 */
	private synchronized void showPauseAnimation() {
		/* 设置旋转中心 */
		slideView.setPivotX(slideView.getWidth());
		slideView.setPivotY(slideView.getHeight() - 10);
		slideOutAnimation.start();
		cdAnimation.cancel();
	}

	/**
	 * 停止动画，并显示播放按钮
	 */
	private void showReadyView() {
		showPauseAnimation();
		playButton.setBackgroundResource(R.drawable.music_play_bg);
	}

	@Override
	public void onPlay() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				seekBar.setEnabled(true);
				if (musicPlayer != null && playButton != null) {
					playButton.setBackgroundResource(R.drawable.music_pause_bg);
					refreshMusicView(musicPlayer.getItemSelected());
					 showPlayAnimation(false);
				}
			}
		});
	}

	@Override
	public void onPaused() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showReadyView();
			}
		});
	}

	@Override
	public void onStopped() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showReadyView();
			}

		});
	}

	@Override
	public void onCompleted() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LogCat.d("===onCompleted===");
				if (musicPlayer != null)
					musicPlayer.stopPlay();
				// playNextMusic();
			}
		});
	}

	@Override
	public void onError(final PlayStatus status, final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				UIHelper.showToast(MusicPlaybackActivity.this, msg);
				switch (status) {
				case ERROR_OTHERCLIENT_PUSH:
					playerIndex = 0;
					playMusicByLoacle();
					break;
				case ERROR_OPERATE_FAILED:
				case ERROR_UNKNOWN:
					showReadyView();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 播放上一首
	 */
	private void playPreMusic() {
//		showPlayAnimation(true);
		showPauseAnimation();
		resetPlayTime();

		musicPlayer.prev();
		refreshMusicView(musicPlayer.getItemSelected());
	}

	/**
	 * 播放下一首
	 */
	private void playNextMusic() {
//		showPlayAnimation(true);
		showPauseAnimation();
		resetPlayTime();

		musicPlayer.next();
		refreshMusicView(musicPlayer.getItemSelected());
	}

	/**
	 * 刷新cd相关视图
	 */
	private void refreshMusicView(Music music) {
		if (music != null) {
			titleView.setText(music.getMusicName());
			artistNameView.setText(music.getSinger());
			albumArtView.setImageBitmap(getRoundedCornerBitmap(MediaUtil.getArtwork(this, -1, -1, false), 1));
		}
	}

	/**
	 * 图像圆形
	 */
	public Bitmap getRoundedCornerBitmap(Bitmap bitmap, float ratio) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, bitmap.getWidth() / ratio, bitmap.getHeight() / ratio, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.music_cd_default);
		float scaleWidth = ((float) bmp.getWidth()) / bitmap.getWidth();
		float scaleHeight = ((float) bmp.getHeight()) / bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(output, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		return resizedBitmap;
	}

	private class VolumeBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
				float max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				float current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				if (musicPlayer != null) {
					setVolume(current / max);
				}
			}
		}
	}
}
