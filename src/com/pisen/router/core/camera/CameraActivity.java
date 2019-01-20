package com.pisen.router.core.camera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.pisen.router.Helper;
import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.AppConfig;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;

/**
 * @Description:照相机
 * @author MouJunFeng
 * @date 2014-8-15 下午4:51:48
 * @version V1.0
 */
public class CameraActivity extends Activity{
	private static final String TAG = "CameraActivity";
	public static final File CAMERA_PATH = Environment.getExternalStoragePublicDirectory("PisenRouter/jpjc");
	private SensorManager mSensorManager = null; // 传感器管理
	private Sensor mSensorAccelerometer = null; // 加速传感器
	private Sensor mSensorMagnetic = null; // 磁力传感器
	private LocationManager mLocationManager = null; // 位置管理
	private LocationListener locationListener = null; // 位置监听
	private Preview preview = null; // 预览
	private int current_orientation = 0; // 当前方向
	private OrientationEventListener orientationEventListener = null; // 方向监听
	private boolean supports_auto_stabilise = false;
	private boolean supports_force_video_4k = false;
	private ArrayList<String> save_location_history = new ArrayList<String>(); // 保存位置集合
	private boolean camera_in_background = false; // 相机是否由一个片段/对话框(如设置或文件夹选择器)
	private GestureDetector gestureDetector; // 手势检测
	private boolean screen_is_locked = false;
	private int resultExit = 0;
	private TransferManagerV2 transManger; // 传输管理类
	// private ToastBoxer screen_locked_toast = new ToastBoxer();

	// for testing:
	public boolean is_test = false;
	public Bitmap gallery_bitmap = null;
	// 录像显示的时间
	public TextView txtVideoTime;
	// 录像的标记点
	public ImageView img_mark;
	public ImageButton mSwitchCamera;

	public static void start(Context context, String rootURL) {
		Intent intent = new Intent(context, CameraActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);
		// PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		// 初始化传输管理类
		transManger = TransferManagerV2.getInstance(this);
		if (getIntent() != null && getIntent().getExtras() != null) {
			is_test = getIntent().getExtras().getBoolean("test_project");
		}
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		// if( activityManager.getMemoryClass() >= 128 ) { // test
		if (activityManager.getLargeMemoryClass() >= 128) {
			supports_auto_stabilise = true;
		}

		if (activityManager.getMemoryClass() >= 128 || activityManager.getLargeMemoryClass() >= 512) {
			supports_force_video_4k = true;
		}

		setWindowFlagsForCamera();

		// read save locations
		save_location_history.clear();
		int save_location_history_size = sharedPreferences.getInt("save_location_history_size", 0);
		for (int i = 0; i < save_location_history_size; i++) {
			String string = sharedPreferences.getString("save_location_history_" + i, null);
			if (string != null) {
				save_location_history.add(string);
			}
		}
		// also update, just in case a new folder has been set
		updateFolderHistory();
		// updateFolderHistory("/sdcard/Pictures/OpenCameraTest");

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
			mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		}

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		updateGalleryIcon();

		preview = new Preview(this, savedInstanceState);
		((ViewGroup) findViewById(R.id.preview)).addView(preview);

		orientationEventListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				// MainActivity.this.onOrientationChanged(orientation);
			}
		};
		gestureDetector = new GestureDetector(this, new MyGestureDetector());
		txtVideoTime = (TextView) findViewById(R.id.txt_time);
		/* img_mark = (ImageView) findViewById(R.id.img_mark); */
		mSwitchCamera = (ImageButton) findViewById(R.id.switch_video);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void exitRecord() {
		/*
		 * renameDialog = ViewEffect.createComfirDialog(CameraActivity.this,
		 * R.string.title_rename_vedio, R.string.title_comfir_save, new
		 * OnPositiveClickListener() {
		 * 
		 * @Override public void onPositiveClick(DialogInterface dialog) {
		 * preview.phase = preview.PHASE_TAKING_PHOTO;
		 * preview.takePicturePressed(); dialog.dismiss();
		 * mSwitchCamera.setEnabled(true); }
		 * 
		 * @Override public void onCancel(DialogInterface dialog) {
		 * super.onCancel(dialog); // 不保存视频文件 preview.phase =
		 * preview.PHASE_TAKING_PHOTO; preview.video_name = null;
		 * preview.takePicturePressed(); mSwitchCamera.setEnabled(true); } });
		 * // 暂停录制 preview.phase = preview.PHASE_PREVIEW_PAUSED;
		 * renameDialog.show();
		 */
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN: {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String volume_keys = sharedPreferences.getString("preference_volume_keys", "volume_take_photo");
			if (volume_keys.equals("volume_take_photo")) {
				takePicture();
				return true;
			} else if (volume_keys.equals("volume_zoom")) {
				if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
					this.preview.zoomIn();
				else
					this.preview.zoomOut();
				return true;
			} else if (volume_keys.equals("volume_exposure")) {
				if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
					this.preview.changeExposure(1, true);
				else
					this.preview.changeExposure(-1, true);
				return true;
			} else if (volume_keys.equals("volume_really_nothing")) {
				return true;
			}
			break;
		}
		case KeyEvent.KEYCODE_MENU: {
			openSettings();
			return true;
		}
		case KeyEvent.KEYCODE_CAMERA: {
			if (event.getRepeatCount() == 0) {
				View view = findViewById(R.id.take_photo);
				clickedTakePhoto(view);
				return true;
			}
		}
		case KeyEvent.KEYCODE_FOCUS: {
			preview.requestAutoFocus();
			return true;
		}
		case KeyEvent.KEYCODE_ZOOM_IN: {
			preview.zoomIn();
			return true;
		}
		case KeyEvent.KEYCODE_ZOOM_OUT: {
			preview.zoomOut();
			return true;
		}
		case KeyEvent.KEYCODE_BACK: {
			if (preview.getPhase() == preview.PHASE_TAKING_PHOTO && preview.isIs_video()) {// 视频录制中
				exitRecord();
			} else {
				if (this.preview != null) {
					this.preview.reset();
				}
				this.finish();
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private SensorEventListener accelerometerListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			preview.onAccelerometerSensorChanged(event);
		}
	};

	private SensorEventListener magneticListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			preview.onMagneticSensorChanged(event);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		checkExternalStorage();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mSensorManager.registerListener(accelerometerListener, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(magneticListener, mSensorMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
		orientationEventListener.enable();

		// Define a listener that responds to location updates
		boolean store_location = sharedPreferences.getBoolean("preference_location", false);
		if (store_location) {
			locationListener = new LocationListener() {
				public void onLocationChanged(Location location) {
					preview.locationChanged(location);
				}

				public void onStatusChanged(String provider, int status, Bundle extras) {
				}

				public void onProviderEnabled(String provider) {
				}

				public void onProviderDisabled(String provider) {
				}
			};

			// see https://sourceforge.net/p/opencamera/tickets/1/
			if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			}
			if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			}
		}

		layoutUI();
		updateGalleryIcon();
		preview.onResume();
	}

	/**
	 * 检查SD卡
	 * @return
	 */
	private void checkExternalStorage() {
		findViewById(R.id.take_photo).setEnabled(true);
		if (!Helper.isSdcardExist()) {
			UIHelper.showToast(this, "使用相机请先插入SD卡");
			return;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(accelerometerListener);
		mSensorManager.unregisterListener(magneticListener);
		orientationEventListener.disable();
		if (this.locationListener != null) {
			mLocationManager.removeUpdates(locationListener);
		}
		preview.onPause();
	}

	public void layoutUI() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String ui_placement = sharedPreferences.getString("preference_ui_placement", "ui_right");
		boolean ui_placement_right = ui_placement.equals("ui_right");
		int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int relative_orientation = (current_orientation + degrees) % 360;
		int ui_rotation = (360 - relative_orientation) % 360;
		preview.setUIRotation(ui_rotation);
		if ((relative_orientation == 0 && ui_placement_right) || (relative_orientation == 180 && ui_placement_right) || relative_orientation == 90
				|| relative_orientation == 270) {
			if (!ui_placement_right && (relative_orientation == 90 || relative_orientation == 270)) {
			}

			setViewRotation(findViewById(R.id.take_photo), ui_rotation);
			setViewGroupRotation((ViewGroup) findViewById(R.id.toolbar_layout), ui_rotation);
		} else {

			setViewRotation(findViewById(R.id.take_photo), ui_rotation);
			setViewGroupRotation((ViewGroup) findViewById(R.id.toolbar_layout), ui_rotation);
		}

		{
			// set icon for taking photos vs videos
			ImageButton view = (ImageButton) findViewById(R.id.take_photo);
			if (preview != null) {
				view.setBackgroundResource(preview.isVideo() ? R.drawable.camera_rec_take_play : R.drawable.camera_take);
				if (preview.isVideo()) {
					if (this.txtVideoTime != null) {
						this.txtVideoTime.setVisibility(View.VISIBLE);
					}
					if (this.img_mark != null) {
						this.img_mark.setVisibility(View.VISIBLE);
					}
				} else {
					if (this.txtVideoTime != null) {
						this.txtVideoTime.setVisibility(View.GONE);
					}
					if (this.img_mark != null) {
						this.img_mark.setVisibility(View.GONE);
					}
				}
			}
		}
	}

	public static void setViewGroupRotation(ViewGroup vg, float rotation) {
		final int childCount = vg.getChildCount();

		for (int i = 0; i < childCount; i++) {
			View child = vg.getChildAt(i);

			if (child instanceof ViewGroup) {
				setViewGroupRotation((ViewGroup) child, rotation);
			} else {
				setViewRotation(child, rotation);
			}
		}
	}

	public static void setViewRotation(View v, float rotation) {
		v.animate().rotation(rotation).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// configuration change can include screen orientation
		// (landscape/portrait) when not locked (when settings is open)
		// needed if app is paused/resumed when settings is open and device is
		// in portrait mode
		try {
			preview.setCameraDisplayOrientation(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onConfigurationChanged(newConfig);
	}

	public void clickedTakePhoto(View view) {
		if (!Helper.isSdcardExist()) {
			UIHelper.showToast(this, "使用相机请先插入SD卡");
			return;
		}
		this.takePicture();
		// 照相之后刷新界面
		setResult(2);
	}

	public void clickedSwitchCamera(View view) {
		this.preview.switchCamera();
	}

	public void clickedSwitchVideo(View view) {
		if (!Helper.isSdcardExist()) {
			UIHelper.showToast(this, "使用摄像功能请先插入SD卡");
			return;
		}
		// if (preview.getPhase() == preview.PHASE_TAKING_PHOTO &&
		// preview.isIs_video()) {
		// exitRecord(true);
		// }else{
		//
		// }
		if (mSwitchCamera.isEnabled()) {
			this.preview.switchVideo(true, true);
		}
	}

	public void clickedFlash(View view) {
		this.preview.cycleFlash();
	}

	public void clickedFocusMode(View view) {
		this.preview.cycleFocusMode();
	}

	/*
	 * void setSeekBarExposure() { SeekBar seek_bar =
	 * ((SeekBar)findViewById(R.id.seekbar)); final int min_exposure =
	 * preview.getMinimumExposure(); seek_bar.setMax(
	 * preview.getMaximumExposure() - min_exposure ); seek_bar.setProgress(
	 * preview.getCurrentExposure() - min_exposure ); }
	 */

	public void clickedSettings(View view) {
		openSettings();
	}

	private void openSettings() {
		preview.stopVideo(false); // important to stop video, as we'll be
									// changing camera parameters when the
									// settings window closes

		Bundle bundle = new Bundle();
		bundle.putInt("cameraId", this.preview.getCameraId());
		bundle.putBoolean("supports_auto_stabilise", this.supports_auto_stabilise);
		bundle.putBoolean("supports_force_video_4k", this.supports_force_video_4k);
		bundle.putBoolean("supports_face_detection", this.preview.supportsFaceDetection());

		putBundleExtra(bundle, "color_effects", this.preview.getSupportedColorEffects());
		putBundleExtra(bundle, "scene_modes", this.preview.getSupportedSceneModes());
		putBundleExtra(bundle, "white_balances", this.preview.getSupportedWhiteBalances());
		putBundleExtra(bundle, "isos", this.preview.getSupportedISOs());
		putBundleExtra(bundle, "exposures", this.preview.getSupportedExposures());
		bundle.putString("iso_key", this.preview.getISOKey());
		if (this.preview.getCamera() != null) {
			bundle.putString("parameters_string", this.preview.getCamera().getParameters().flatten());
		}

		List<Camera.Size> preview_sizes = this.preview.getSupportedPreviewSizes();
		if (preview_sizes != null) {
			int[] widths = new int[preview_sizes.size()];
			int[] heights = new int[preview_sizes.size()];
			int i = 0;
			for (Camera.Size size : preview_sizes) {
				widths[i] = size.width;
				heights[i] = size.height;
				i++;
			}
			bundle.putIntArray("preview_widths", widths);
			bundle.putIntArray("preview_heights", heights);
		}

		List<Camera.Size> sizes = this.preview.getSupportedPictureSizes();
		if (sizes != null) {
			int[] widths = new int[sizes.size()];
			int[] heights = new int[sizes.size()];
			int i = 0;
			for (Camera.Size size : sizes) {
				widths[i] = size.width;
				heights[i] = size.height;
				i++;
			}
			bundle.putIntArray("resolution_widths", widths);
			bundle.putIntArray("resolution_heights", heights);
		}

		List<String> video_quality = this.preview.getSupportedVideoQuality();
		if (video_quality != null) {
			String[] video_quality_arr = new String[video_quality.size()];
			String[] video_quality_string_arr = new String[video_quality.size()];
			int i = 0;
			for (String value : video_quality) {
				video_quality_arr[i] = value;
				video_quality_string_arr[i] = this.preview.getCamcorderProfileDescription(value);
				i++;
			}
			bundle.putStringArray("video_quality", video_quality_arr);
			bundle.putStringArray("video_quality_string", video_quality_string_arr);
		}

		List<Camera.Size> video_sizes = this.preview.getSupportedVideoSizes();
		if (video_sizes != null) {
			int[] widths = new int[video_sizes.size()];
			int[] heights = new int[video_sizes.size()];
			int i = 0;
			for (Camera.Size size : video_sizes) {
				widths[i] = size.width;
				heights[i] = size.height;
				i++;
			}
			bundle.putIntArray("video_widths", widths);
			bundle.putIntArray("video_heights", heights);
		}

		putBundleExtra(bundle, "flash_values", this.preview.getSupportedFlashValues());
		putBundleExtra(bundle, "focus_values", this.preview.getSupportedFocusValues());

		setWindowFlagsForSettings();
		// MyPreferenceActivity fragment = new MyPreferenceActivity();
		// fragment.setArguments(bundle);
		// getFragmentManager().beginTransaction().add(R.id.prefs_container,
		// fragment, "PREFERENCE_FRAGMENT").addToBackStack(null).commit();
	}

	public void updateForSettings() {
		updateFolderHistory();

		// update camera for changes made in prefs - do this without closing and
		// reopening the camera app if possible for speed!
		// but need workaround for Nexus 7 bug, where scene mode doesn't take
		// effect unless the camera is restarted - I can reproduce this with
		// other 3rd party camera apps, so may be a Nexus 7 issue...
		boolean need_reopen = false;
		if (preview.getCamera() != null) {
			Camera.Parameters parameters = preview.getCamera().getParameters();
			String key = Preview.getSceneModePreferenceKey();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String value = sharedPreferences.getString(key, Camera.Parameters.SCENE_MODE_AUTO);
			if (!value.equals(parameters.getSceneMode())) {
				need_reopen = true;
			}
		}
		if (need_reopen || preview.getCamera() == null) { // if camera couldn't
															// be opened before,
															// might as well try
															// again
			preview.onPause();
			preview.onResume();
		} else {
			preview.pausePreview();
			preview.setupCamera();
		}
	}

	boolean cameraInBackground() {
		return this.camera_in_background;
	}

	//
	// MyPreferenceActivity getPreferenceFragment() {
	// MyPreferenceActivity fragment =
	// (MyPreferenceActivity)getFragmentManager().findFragmentByTag("PREFERENCE_FRAGMENT");
	// return fragment;
	// }

	@Override
	public void onBackPressed() {
		// final MyPreferenceActivity fragment = getPreferenceFragment();
		// if( screen_is_locked ) {
		// preview.showToast(screen_locked_toast, R.string.screen_is_locked);
		// return;
		// }
		// if( fragment != null ) {
		// if( MyDebug.LOG )
		// Log.d(TAG, "close settings");
		// setWindowFlagsForCamera();
		// updateForSettings();
		// }
		super.onBackPressed();
	}

	private void setWindowFlagsForCamera() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// force to landscape mode
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// keep screen active - see
		// http://stackoverflow.com/questions/2131948/force-screen-on
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (sharedPreferences.getBoolean("preference_show_when_locked", true)) {
			// keep Open Camera on top of screen-lock (will still need to unlock
			// when going to gallery or settings)
			getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		} else {
			getWindow().clearFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		}

		// set screen to max brightness - see
		// http://stackoverflow.com/questions/11978042/android-screen-brightness-max-value
		// done here rather than onCreate, so that changing it in preferences
		// takes effect without restarting app
		{
			WindowManager.LayoutParams layout = getWindow().getAttributes();
			if (sharedPreferences.getBoolean("preference_max_brightness", true)) {
				layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
			} else {
				layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
			}
			getWindow().setAttributes(layout);
		}

		camera_in_background = false;
	}

	private void setWindowFlagsForSettings() {
		// allow screen rotation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		// revert to standard screen blank behaviour
		getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		// settings should still be protected by screen lock
		getWindow().clearFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		{
			WindowManager.LayoutParams layout = getWindow().getAttributes();
			layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
			getWindow().setAttributes(layout);
		}

		camera_in_background = true;
	}

	class Media {
		public long id;
		public boolean video;
		public Uri uri;
		public long date;
		public int orientation;

		Media(long id, boolean video, Uri uri, long date, int orientation) {
			this.id = id;
			this.video = video;
			this.uri = uri;
			this.date = date;
			this.orientation = orientation;
		}
	}

	private Media getLatestMedia(boolean video) {
		Media media = null;
		Uri baseUri = video ? Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();
		String[] projection = video ? new String[] { VideoColumns._ID, VideoColumns.DATE_TAKEN } : new String[] { ImageColumns._ID, ImageColumns.DATE_TAKEN,
				ImageColumns.ORIENTATION };
		String selection = video ? "" : ImageColumns.MIME_TYPE + "='image/jpeg'";
		String order = video ? VideoColumns.DATE_TAKEN + " DESC," + VideoColumns._ID + " DESC" : ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID
				+ " DESC";
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(query, projection, selection, null, order);
			if (cursor != null && cursor.moveToFirst()) {
				long id = cursor.getLong(0);
				long date = cursor.getLong(1);
				int orientation = video ? 0 : cursor.getInt(2);
				Uri uri = ContentUris.withAppendedId(baseUri, id);
				media = new Media(id, video, uri, date, orientation);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return media;
	}

	private Media getLatestMedia() {
		Media image_media = getLatestMedia(false);
		Media video_media = getLatestMedia(true);
		Media media = null;
		if (image_media != null && video_media == null) {
			media = image_media;
		} else if (image_media == null && video_media != null) {
			media = video_media;
		} else if (image_media != null && video_media != null) {
			if (image_media.date >= video_media.date) {
				media = image_media;
			} else {
				media = video_media;
			}
		}
		return media;
	}

	public void updateGalleryIconToBlank() {
		// ImageButton galleryButton = (ImageButton)
		// this.findViewById(R.id.gallery);
		// int bottom = galleryButton.getPaddingBottom();
		// int top = galleryButton.getPaddingTop();
		// int right = galleryButton.getPaddingRight();
		// int left = galleryButton.getPaddingLeft();
		// /*if( MyDebug.LOG )
		// Log.d(TAG, "padding: " + bottom);*/
		// galleryButton.setImageBitmap(null);
		// galleryButton.setImageResource(R.drawable.gallery);
		// // workaround for setImageResource also resetting padding, Android
		// bug
		// galleryButton.setPadding(left, top, right, bottom);
		gallery_bitmap = null;
	}

	public void updateGalleryIconToBitmap(Bitmap bitmap) {
		// ImageButton galleryButton = (ImageButton)
		// this.findViewById(R.id.gallery);
		// galleryButton.setImageBitmap(bitmap);
		gallery_bitmap = bitmap;
	}

	public void updateGalleryIcon() {
		long time_s = System.currentTimeMillis();
		Media media = getLatestMedia();
		Bitmap thumbnail = null;
		if (media != null) {
			if (media.video) {
				thumbnail = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Video.Thumbnails.MINI_KIND, null);
			} else {
				thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
			}
			if (thumbnail != null) {
				if (media.orientation != 0) {
					Matrix matrix = new Matrix();
					matrix.setRotate(media.orientation, thumbnail.getWidth() * 0.5f, thumbnail.getHeight() * 0.5f);
					try {
						Bitmap rotated_thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
						// careful, as rotated_thumbnail is sometimes not a
						// copy!
						if (rotated_thumbnail != thumbnail) {
							thumbnail.recycle();
							thumbnail = rotated_thumbnail;
						}
					} catch (Throwable t) {
						Log.e(TAG, "failed to rotate thumbnail");
					}
				}
			}
		}
		if (thumbnail != null) {
			updateGalleryIconToBitmap(thumbnail);
		} else {
			updateGalleryIconToBlank();
		}
	}

	public void clickedGallery(View view) {
		// Intent intent = new Intent(Intent.ACTION_VIEW,
		// MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		Uri uri = null;
		Media media = getLatestMedia();
		if (media != null) {
			uri = media.uri;
		}

		if (uri != null) {
			try {
				ContentResolver cr = getContentResolver();
				ParcelFileDescriptor pfd = cr.openFileDescriptor(uri, "r");
				if (pfd == null) {
					uri = null;
				}
				if(pfd != null) pfd.close();
			} catch (IOException e) {
				uri = null;
			}
		}
		if (uri == null) {
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		}
		if (!is_test) {
			// don't do if testing, as unclear how to exit activity to finish
			// test (for testGallery())
			final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
			try {
				// REVIEW_ACTION means we can view video files without
				// autoplaying
				Intent intent = new Intent(REVIEW_ACTION, uri);
				this.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				// from
				// http://stackoverflow.com/questions/11073832/no-activity-found-to-handle-intent
				// - needed to fix crash if no gallery app installed
				// Intent intent = new Intent(Intent.ACTION_VIEW,
				// Uri.parse("blah")); // test
				if (intent.resolveActivity(getPackageManager()) != null) {
					this.startActivity(intent);
				} else {
					// preview.showToast(null, R.string.no_gallery_app);
				}
			}
		}
	}

	public void updateFolderHistory() {
		String folder_name = getSaveLocation();
		updateFolderHistory(folder_name);
	}

	private void updateFolderHistory(String folder_name) {
		while (save_location_history.remove(folder_name)) {
		}
		save_location_history.add(folder_name);
		while (save_location_history.size() > 6) {
			save_location_history.remove(0);
		}
		writeSaveLocations();
	}

	public void clearFolderHistory() {
		save_location_history.clear();
		updateFolderHistory(); // to re-add the current choice, and save
	}

	private void writeSaveLocations() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("save_location_history_size", save_location_history.size());
		for (int i = 0; i < save_location_history.size(); i++) {
			String string = save_location_history.get(i);
			editor.putString("save_location_history_" + i, string);
		}
		editor.apply();
	}

	static private void putBundleExtra(Bundle bundle, String key, List<String> values) {
		if (values != null) {
			String[] values_arr = new String[values.size()];
			int i = 0;
			for (String value : values) {
				values_arr[i] = value;
				i++;
			}
			bundle.putStringArray(key, values_arr);
		}
	}

	public void clickedShare(View view) {
		this.preview.clickedShare();
	}

	public void clickedTrash(View view) {
		this.preview.clickedTrash();
		// Calling updateGalleryIcon() immediately has problem that it still
		// returns the latest image that we've just deleted!
		// But works okay if we call after a delay. 100ms works fine on Nexus 7
		// and Galaxy Nexus, but set to 500 just to be safe.
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				updateGalleryIcon();
			}
		}, 500);
	}

	Dialog renameDialog = null;
	private void takePicture() {
		preview.takePicturePressed();
		if (preview.getPhase() == preview.PHASE_TAKING_PHOTO && preview.isIs_video()) {
			mSwitchCamera.setEnabled(false);
		} else {
			mSwitchCamera.setEnabled(true);
		}
	}

	void lockScreen() {
		((ViewGroup) findViewById(R.id.locker)).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
				// return true;
			}
		});
		screen_is_locked = true;
	}

	void unlockScreen() {
		((ViewGroup) findViewById(R.id.locker)).setOnTouchListener(null);
		screen_is_locked = false;
	}

	boolean isScreenLocked() {
		return screen_is_locked;
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				final ViewConfiguration vc = ViewConfiguration.get(CameraActivity.this);
				// final int swipeMinDistance = 4*vc.getScaledPagingTouchSlop();
				final float scale = getResources().getDisplayMetrics().density;
				final int swipeMinDistance = (int) (160 * scale + 0.5f); 	// convert
																			// dps
																			// to
																			// pixels
				final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
				float xdist = e1.getX() - e2.getX();
				float ydist = e1.getY() - e2.getY();
				float dist2 = xdist * xdist + ydist * ydist;
				float vel2 = velocityX * velocityX + velocityY * velocityY;
				if (dist2 > swipeMinDistance * swipeMinDistance && vel2 > swipeThresholdVelocity * swipeThresholdVelocity) {
					// preview.showToast(screen_locked_toast,
					// R.string.unlocked);
					unlockScreen();
				}
			} catch (Exception e) {
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			// preview.showToast(screen_locked_toast,
			// R.string.screen_is_locked);
			return true;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		if (this.preview != null) {
			preview.onSaveInstanceState(state);
		}
	}

	public void broadcastFile(File file, boolean is_new_picture, boolean is_new_video) {
		if (file.isDirectory()) {
		} else {
			MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {
				public void onScanCompleted(String path, Uri uri) {
					Log.d("ExternalStorage", "Scanned " + path + ":-> uri=" + uri);
				}
			});
			if (is_new_picture) {
				this.sendBroadcast(new Intent(Camera.ACTION_NEW_PICTURE, Uri.fromFile(file)));
				// for compatibility with some apps - apparently this is what
				// used to be broadcast on Android?
				this.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", Uri.fromFile(file)));
			} else if (is_new_video) {
				this.sendBroadcast(new Intent(Camera.ACTION_NEW_VIDEO, Uri.fromFile(file)));
			}

			addTrasport(file.getAbsolutePath(), this);
			UIHelper.showToast(this, "已添加到上传列表");
			playRingtone();
		}
	}

	/**
	 * 播放提示音
	 */
	private void playRingtone() {
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(this, notification);
		r.play();
	}

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private String getSaveLocation() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String folder_name = sharedPreferences.getString("preference_save_location", "OpenCamera");
		return folder_name;
	}

	public File getImageFolder() {
		File file = CAMERA_PATH;
		if (!file.exists()) {
			file.mkdirs();
		}

		return file;
	}

	
	/** Create a File for saving an image or video */
	@SuppressLint("SimpleDateFormat")
	public File getOutputMediaFile(int type) {
		File mediaStorageDir = getImageFolder();
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
			broadcastFile(mediaStorageDir, false, false);
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		String index = "";
		File mediaFile = null;
		for (int count = 1; count <= 100; count++) {
			if (type == MEDIA_TYPE_IMAGE) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator + "Pic_" + timeStamp + index + ".jpg");
			} else if (type == MEDIA_TYPE_VIDEO) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator + "Vid_" + timeStamp + index + ".mp4");
			} else {
				return null;
			}
			if (!mediaFile.exists()) {
				break;
			}
			// index = "_" + count; // try to find a unique filename
		}
		return mediaFile;
	}

	public boolean supportsAutoStabilise() {
		return this.supports_auto_stabilise;
	}

	public boolean supportsForceVideo4K() {
		return this.supports_force_video_4k;
	}

	@SuppressWarnings("deprecation")
	public long freeMemory() { // return free memory in MB
		try {
			File image_folder = this.getImageFolder();
			StatFs statFs = new StatFs(image_folder.getAbsolutePath());
			// cast to long to avoid overflow!
			long blocks = statFs.getAvailableBlocks();
			long size = statFs.getBlockSize();
			long free = (blocks * size) / 1048576;
			return free;
		} catch (IllegalArgumentException e) {
			// can fail on emulator, at least!
			return -1;
		}
	}

	// for testing:
	public Preview getPreview() {
		return this.preview;
	}

	public ArrayList<String> getSaveLocationHistory() {
		return this.save_location_history;
	}

	public void clickedBack(View v) {
		if (this.preview != null) {
			this.preview.reset();
		}
		this.finish();
	}

	@Override
	protected void onStop() {
		Log.i("Check", "onStop result: " + resultExit);

		super.onStop();
	}

	/**
	 * 添加到数据库并且上传
	 */
	private void addTrasport(String filePath, Context context) {
		File file = new File(filePath);
		if (file.exists()) {
			String saveFiler = getyyyMMdd(file.lastModified());
			String targetDir = AppConfig.getCurrentDiskPath() + "即拍即传/"+saveFiler+"/";
			ResourceInfo resource = new ResourceInfo();
			resource.path = filePath;
			resource.name = file.getName();
			resource.size = file.length();
			resource.destPath = targetDir;
			transManger.addUploadTask(targetDir, Arrays.asList(resource));
		}
	}
	
	private  String getyyyMMdd(long date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
		String saveFiler = dateFormat.format(date);
		return saveFiler;
	}

}
