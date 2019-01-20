package com.pisen.router.core.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.pisen.router.R;

/**
 * 
 * 
 * @Description:自定义照相机预览控件
 * @author MouJunFeng
 * @date 2014-8-15 下午4:52:00
 * @version V1.0
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";

	private static final String TAG_GPS_IMG_DIRECTION = "GPSImgDirection";
	private static final String TAG_GPS_IMG_DIRECTION_REF = "GPSImgDirectionRef";

	private Paint p = new Paint();
	private DecimalFormat decimalFormat = new DecimalFormat("#0.0");
	private Camera.CameraInfo camera_info = new Camera.CameraInfo();
	private Matrix camera_to_preview_matrix = new Matrix();
	private Matrix preview_to_camera_matrix = new Matrix();
	private RectF face_rect = new RectF();
	private Rect text_bounds = new Rect();
	private int display_orientation = 0;

	private boolean ui_placement_right = true;

	private boolean app_is_paused = true;
	private SurfaceHolder mHolder = null;
	private boolean has_surface = false;
	private boolean has_aspect_ratio = false;
	private double aspect_ratio = 0.0f;
	private Camera camera = null;
	private int cameraId = 0;
	private boolean is_video = false;
	private MediaRecorder video_recorder = null;
	private boolean video_start_time_set = false;
	private long video_start_time = 0;
	public String video_name = null;
	private int[] current_fps_range = new int[2];

	private final int PHASE_NORMAL = 0;
	private final int PHASE_TIMER = 1;
	public final int PHASE_TAKING_PHOTO = 2;
	public final int PHASE_PREVIEW_PAUSED = 3; // the paused state after taking
												// a photo
	public int phase = PHASE_NORMAL;
	/*
	 * private boolean is_taking_photo = false; private boolean
	 * is_taking_photo_on_timer = false;
	 */
	private Timer takePictureTimer = new Timer();
	private TimerTask takePictureTimerTask = null;
	private Timer beepTimer = new Timer();
	private TimerTask beepTimerTask = null;
	private Timer restartVideoTimer = new Timer();
	private TimerTask restartVideoTimerTask = null;
	private TimerTask flashVideoTimerTask = null;
	private long take_photo_time = 0;
	private int remaining_burst_photos = 0;
	private int n_burst = 1;
	private int remaining_restart_video = 0;

	private boolean is_preview_started = false;
	// private boolean is_preview_paused = false; // whether we are in the
	// paused state after taking a photo
	private String preview_image_name = null;
	private Bitmap thumbnail = null; // thumbnail of last picture taken
	private boolean thumbnail_anim = false; // whether we are displaying the
											// thumbnail animation
	private long thumbnail_anim_start_ms = -1; // time that the thumbnail
												// animation started
	private RectF thumbnail_anim_src_rect = new RectF();
	private RectF thumbnail_anim_dst_rect = new RectF();
	private Matrix thumbnail_anim_matrix = new Matrix();
	private int[] gui_location = new int[2];

	private int current_orientation = 0; // orientation received by
											// onOrientationChanged
	private int current_rotation = 0; // orientation relative to camera's
										// orientation (used for
										// parameters.setRotation())
	private boolean has_level_angle = false;
	private double level_angle = 0.0f;
	private double orig_level_angle = 0.0f;

	private float free_memory_gb = -1.0f;
	private long last_free_memory_time = 0;

	private boolean has_zoom = false;
	private int zoom_factor = 0;
	private int max_zoom_factor = 0;
	private ScaleGestureDetector scaleGestureDetector;
	private List<Integer> zoom_ratios = null;
	private boolean touch_was_multitouch = false;

	private List<String> supported_flash_values = null; // our "values" format
	private int current_flash_index = -1; // this is an index into the
											// supported_flash_values array, or
											// -1 if no flash modes available

	private List<String> supported_focus_values = null; // our "values" format
	private int current_focus_index = -1; // this is an index into the
											// supported_focus_values array, or
											// -1 if no focus modes available

	private List<String> color_effects = null;
	private List<String> scene_modes = null;
	private List<String> white_balances = null;
	private String iso_key = null;
	private List<String> isos = null;
	private List<String> exposures = null;
	private int min_exposure = 0;
	private int max_exposure = 0;

	private List<Camera.Size> supported_preview_sizes = null;

	private List<Camera.Size> sizes = null;
//	private int current_size_index = -1; // this is an index into the sizes
											// array, or -1 if sizes not yet set

	// video_quality can either be:
	// - an int, in which case it refers to a CamcorderProfile
	// - of the form [CamcorderProfile]_r[width]x[height] - we use the
	// CamcorderProfile as a base, and override the video resolution - this is
	// needed to support resolutions which don't have corresponding camcorder
	// profiles
	private List<String> video_quality = null;
	private int current_video_quality = -1; // this is an index into the
											// video_quality array, or -1 if not
											// found (though this shouldn't
											// happen?)
	private List<Camera.Size> video_sizes = null;

	private Location location = null;
	private boolean has_set_location = false;
	private float location_accuracy = 0.0f;
	private Bitmap location_bitmap = null;
	private Bitmap location_off_bitmap = null;
	private Rect location_dest = new Rect();

	// private ToastBoxer switch_camera_toast = new ToastBoxer();
	// private ToastBoxer switch_video_toast = new ToastBoxer();
	// private ToastBoxer flash_toast = new ToastBoxer();
	// private ToastBoxer focus_toast = new ToastBoxer();
	// private ToastBoxer exposure_lock_toast = new ToastBoxer();
	// private ToastBoxer take_photo_toast = new ToastBoxer();
	// private ToastBoxer stopstart_video_toast = new ToastBoxer();
	// private ToastBoxer change_exposure_toast = new ToastBoxer();

	private int ui_rotation = 0;

	private boolean supports_face_detection = false;
	private boolean using_face_detection = false;
	private Face[] faces_detected = null;
	private boolean has_focus_area = false;
	private int focus_screen_x = 0;
	private int focus_screen_y = 0;
	private long focus_complete_time = -1;
	private int focus_success = FOCUS_DONE;
	private static final int FOCUS_WAITING = 0;
	private static final int FOCUS_SUCCESS = 1;
	private static final int FOCUS_FAILED = 2;
	private static final int FOCUS_DONE = 3;
	private String set_flash_after_autofocus = "";
	private boolean successfully_focused = false;
	private long successfully_focused_time = -1;

	private IntentFilter battery_ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	private boolean has_battery_frac = false;
	private float battery_frac = 0.0f;
	private long last_battery_time = 0;

	// accelerometer and geomagnetic sensor info
	private final float sensor_alpha = 0.8f; // for filter
	private boolean has_gravity = false;
	private float[] gravity = new float[3];
	private boolean has_geomagnetic = false;
	private float[] geomagnetic = new float[3];
	private float[] deviceRotation = new float[9];
	private float[] cameraRotation = new float[9];
	private float[] deviceInclination = new float[9];
	private boolean has_geo_direction = false;
	private float[] geo_direction = new float[3];

	// for testing:
	public int count_cameraStartPreview = 0;
	public int count_cameraAutoFocus = 0;
	public int count_cameraTakePicture = 0;
	public boolean has_received_location = false;
	public boolean test_low_memory = false;
	public boolean test_have_angle = false;
	public float test_angle = 0.0f;
	public String test_last_saved_image = null;
	// 录像显示的时间
	public TextView txtVideoTime;
	// 录像的标记点
	//public ImageView img_mark;
	private static final String GT_I9200_MODEL = "GT-I9200";

	private String preference_lock_orientation_config = "none";//"portrait";
	Preview(Context context) {
		this(context, null);
	}

	@SuppressWarnings("deprecation")
	Preview(Context context, Bundle savedInstanceState) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // deprecated

		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

		if (savedInstanceState != null) {
			cameraId = savedInstanceState.getInt("cameraId", 0);
			if (cameraId < 0 || cameraId >= Camera.getNumberOfCameras()) {
				cameraId = 0;
			}
			zoom_factor = savedInstanceState.getInt("zoom_factor", 0);
		}
		Activity activity = (Activity) this.getContext();

		txtVideoTime = (TextView) activity.findViewById(R.id.txt_time);
		//img_mark = (ImageView) activity.findViewById(R.id.img_mark);
		// location_bitmap = BitmapFactory.decodeResource(this.getResources(),
		// R.drawable.earth);
		// location_off_bitmap =
		// BitmapFactory.decodeResource(this.getResources(),
		// R.drawable.earth_off);
	}

	/*
	 * private void previewToCamera(float [] coords) { float alpha = coords[0] /
	 * (float)this.getWidth(); float beta = coords[1] / (float)this.getHeight();
	 * coords[0] = 2000.0f * alpha - 1000.0f; coords[1] = 2000.0f * beta -
	 * 1000.0f; }
	 */

	/*
	 * private void cameraToPreview(float [] coords) { float alpha = (coords[0]
	 * + 1000.0f) / 2000.0f; float beta = (coords[1] + 1000.0f) / 2000.0f;
	 * coords[0] = alpha * (float)this.getWidth(); coords[1] = beta *
	 * (float)this.getHeight(); }
	 */

	private void calculateCameraToPreviewMatrix() {
		camera_to_preview_matrix.reset();
		// from
		// http://developer.android.com/reference/android/hardware/Camera.Face.html#rect
		Camera.getCameraInfo(cameraId, camera_info);
		// Need mirror for front camera.
		boolean mirror = (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
		camera_to_preview_matrix.setScale(mirror ? -1 : 1, 1);
		// This is the value for android.hardware.Camera.setDisplayOrientation.
		camera_to_preview_matrix.postRotate(display_orientation);
		// Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
		// UI coordinates range from (0, 0) to (width, height).
		camera_to_preview_matrix.postScale(this.getWidth() / 2000f, this.getHeight() / 2000f);
		camera_to_preview_matrix.postTranslate(this.getWidth() / 2f, this.getHeight() / 2f);
	}

	private void calculatePreviewToCameraMatrix() {
		calculateCameraToPreviewMatrix();
		if (!camera_to_preview_matrix.invert(preview_to_camera_matrix)) {
			Log.i(TAG, "calculatePreviewToCameraMatrix failed to invert matrix!?");
		}
	}

	private ArrayList<Camera.Area> getAreas(float x, float y) {
		float[] coords = { x, y };
		calculatePreviewToCameraMatrix();
		preview_to_camera_matrix.mapPoints(coords);
		float focus_x = coords[0];
		float focus_y = coords[1];

		int focus_size = 50;
		Rect rect = new Rect();
		rect.left = (int) focus_x - focus_size;
		rect.right = (int) focus_x + focus_size;
		rect.top = (int) focus_y - focus_size;
		rect.bottom = (int) focus_y + focus_size;
		if (rect.left < -1000) {
			rect.left = -1000;
			rect.right = rect.left + 2 * focus_size;
		} else if (rect.right > 1000) {
			rect.right = 1000;
			rect.left = rect.right - 2 * focus_size;
		}
		if (rect.top < -1000) {
			rect.top = -1000;
			rect.bottom = rect.top + 2 * focus_size;
		} else if (rect.bottom > 1000) {
			rect.bottom = 1000;
			rect.top = rect.bottom - 2 * focus_size;
		}

		ArrayList<Camera.Area> areas = new ArrayList<Camera.Area>();
		areas.add(new Camera.Area(rect, 1000));
		return areas;
	}

	private boolean isValidFocusArea(float x, float y) {
		final float scale = getResources().getDisplayMetrics().density;
		int topBottomPadding = (int) (90 * scale + 0.5f); 
//		int leftRightPadding = (int) (40 * scale + 0.5f);
		if( y < topBottomPadding || y >(getHeight()- topBottomPadding)) {//x<leftRightPadding || x > (getWidth() - leftRightPadding) ||
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		scaleGestureDetector.onTouchEvent(event);
		// invalidate();
		/*
		 * if( MyDebug.LOG ) { Log.d(TAG, "touch event: " + event.getAction());
		 * }
		 */
		if (event.getPointerCount() != 1) {
			// multitouch_time = System.currentTimeMillis();
			touch_was_multitouch = true;
			return true;
		}
		if (event.getAction() != MotionEvent.ACTION_UP) {
			if (event.getAction() == MotionEvent.ACTION_DOWN && event.getPointerCount() == 1) {
				touch_was_multitouch = false;
			}
			return true;
		}
		if (touch_was_multitouch) {
			return true;
		}
		if (!this.is_video && this.isTakingPhotoOrOnTimer()) {
			// if video, okay to refocus when recording
			return true;
		}

		// note, we always try to force start the preview (in case
		// is_preview_paused has become false)
		// except if recording video (firstly, the preview should be running;
		// secondly, we don't want to reset the phase!)
		if (!this.is_video) {
			startCameraPreview();
		}
		cancelAutoFocus();

		if (camera != null && !this.using_face_detection && isValidFocusArea(event.getX(), event.getY())) {
			Camera.Parameters parameters = camera.getParameters();
			String focus_mode = parameters.getFocusMode();
			this.has_focus_area = false;
			// getFocusMode() is documented as never returning null, however
			// I've had null pointer exceptions reported in Google Play
			if (parameters.getMaxNumFocusAreas() != 0
					&& focus_mode != null
					&& (focus_mode.equals(Camera.Parameters.FOCUS_MODE_AUTO) || focus_mode.equals(Camera.Parameters.FOCUS_MODE_MACRO)
							|| focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) || focus_mode
								.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))) {
				this.has_focus_area = true;
				this.focus_screen_x = (int) event.getX();
				this.focus_screen_y = (int) event.getY();

				ArrayList<Camera.Area> areas = getAreas(event.getX(), event.getY());
				parameters.setFocusAreas(areas);

				// also set metering areas
				if (parameters.getMaxNumMeteringAreas() == 0) {
					Log.d(TAG, "metering areas not supported");
				} else {
					parameters.setMeteringAreas(areas);
				}

				try {
					camera.setParameters(parameters);
				} catch (RuntimeException e) {
					Log.e(TAG, "failed to set parameters for focus area");
					e.printStackTrace();
				}
			} else if (parameters.getMaxNumMeteringAreas() != 0) {
				// don't set has_focus_area in this mode
				ArrayList<Camera.Area> areas = getAreas(event.getX(), event.getY());
				parameters.setMeteringAreas(areas);
				try {
					camera.setParameters(parameters);
				} catch (RuntimeException e) {
					Log.e(TAG, "failed to set parameters for focus area");
					e.printStackTrace();
				}
			}
		}

		tryAutoFocus(false, true);
		return true;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (Preview.this.camera != null && Preview.this.has_zoom) {
				Preview.this.scaleZoom(detector.getScaleFactor());
			}
			return true;
		}
	}

	public void clearFocusAreas() {
		if (camera == null) {
			return;
		}
		cancelAutoFocus();
		Camera.Parameters parameters = camera.getParameters();
		boolean update_parameters = false;
		if (parameters.getMaxNumFocusAreas() > 0) {
			parameters.setFocusAreas(null);
			update_parameters = true;
		}
		if (parameters.getMaxNumMeteringAreas() > 0) {
			parameters.setMeteringAreas(null);
			update_parameters = true;
		}
		if (update_parameters) {
			try {
				camera.setParameters(parameters);
			} catch (RuntimeException e) {
				Log.e(TAG, "failed to set parameters for focus area");
				e.printStackTrace();
			}
		}
		has_focus_area = false;
		focus_success = FOCUS_DONE;
		successfully_focused = false;
		// Log.d(TAG, "camera parameters null? " +
		// (camera.getParameters().getFocusAreas()==null));
	}

	/*
	 * private void setCameraParameters() { }
	 */

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		Log.e("xxx", "===surfaceCreated===");
		this.has_surface = true;
		this.openCamera();
		this.setWillNotDraw(false); // see
									// http://stackoverflow.com/questions/2687015/extended-surfaceviews-ondraw-method-never-called
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e("xxx", "===surfaceDestroyed===");
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		this.has_surface = false;
		this.closeCamera();
	}

	void stopVideo(boolean from_restart) {
		final CameraActivity main_activity = (CameraActivity) this.getContext();
		main_activity.unlockScreen();
		if (restartVideoTimerTask != null) {
			restartVideoTimerTask.cancel();
			restartVideoTimerTask = null;
		}
		if (flashVideoTimerTask != null) {
			flashVideoTimerTask.cancel();
			flashVideoTimerTask = null;
		}
		if (!from_restart) {
			remaining_restart_video = 0;
		}
		if (video_recorder != null) { // check again, just to be safe
			String toast = getResources().getString(R.string.stopped_recording_video);
			if (remaining_restart_video > 0) {
				toast += " (" + remaining_restart_video + " " + getResources().getString(R.string.repeats_to_go) + ")";
			}
			// showToast(stopstart_video_toast, toast);
			/*
			 * is_taking_photo = false; is_taking_photo_on_timer = false;
			 */
			this.phase = PHASE_NORMAL;
			try {
				video_recorder.setOnErrorListener(null);
				video_recorder.setOnInfoListener(null);
				video_recorder.stop();
			} catch (RuntimeException e) {
				Log.e(TAG, "runtime exception when stopping video");
			}
			video_recorder.reset();
			video_recorder.release();
			video_recorder = null;
			reconnectCamera(false); // n.b., if something went wrong with video,
									// then we reopen the camera - which may
									// fail (or simply not reopen, e.g., if app
									// is now paused)
			if (video_name != null) {
				File file = new File(video_name);
				if (file != null) {
					// need to scan when finished, so we update for the
					// completed file
					main_activity.broadcastFile(file, false, true);
				}
				// create thumbnail
				{
					long time_s = System.currentTimeMillis();
					Bitmap old_thumbnail = thumbnail;
					MediaMetadataRetriever retriever = new MediaMetadataRetriever();
					try {
						retriever.setDataSource(video_name);
						thumbnail = retriever.getFrameAtTime(-1);
					} catch (IllegalArgumentException ex) {
						// corrupt video file?
					} catch (RuntimeException ex) {
						// corrupt video file?
					} finally {
						try {
							retriever.release();
						} catch (RuntimeException ex) {
							// ignore
						}
					}
					if (thumbnail != null && thumbnail != old_thumbnail) {
						// ImageButton galleryButton = (ImageButton)
						// main_activity.findViewById(R.id.gallery);
						// int width = thumbnail.getWidth();
						// int height = thumbnail.getHeight();
						// if (MyDebug.LOG)
						// Log.d(TAG, "    video thumbnail size " + width +
						// " x " + height);
						// if (width > galleryButton.getWidth()) {
						// float scale = (float) galleryButton.getWidth() /
						// width;
						// int new_width = Math.round(scale * width);
						// int new_height = Math.round(scale * height);
						// if (MyDebug.LOG)
						// Log.d(TAG, "    scale video thumbnail to " +
						// new_width + " x " + new_height);
						// Bitmap scaled_thumbnail =
						// Bitmap.createScaledBitmap(thumbnail, new_width,
						// new_height, true);
						// // careful, as scaled_thumbnail is sometimes not a
						// // copy!
						// if (scaled_thumbnail != thumbnail) {
						// thumbnail.recycle();
						// thumbnail = scaled_thumbnail;
						// }
						// }
						// main_activity.runOnUiThread(new Runnable() {
						// public void run() {
						// main_activity.updateGalleryIconToBitmap(thumbnail);
						// }
						// });
						// if (old_thumbnail != null) {
						// // only recycle after we've set the new thumbnail
						// old_thumbnail.recycle();
						// }
					}
				}
				video_name = null;
			}
		}
	}

	private void restartVideo() {
		if (video_recorder != null) {
			stopVideo(true);
			if (remaining_restart_video > 0) {
				if (is_video) {
					takePicture();
					// must decrement after calling takePicture(), so that
					// takePicture() doesn't reset the value of
					// remaining_restart_video
					remaining_restart_video--;
				} else {
					remaining_restart_video = 0;
				}
			}
		}
	}

	private void reconnectCamera(boolean quiet) {
		if (camera != null) { // just to be safe
			try {
				camera.reconnect();
				this.startCameraPreview();
			} catch (IOException e) {
				Log.e(TAG, "failed to reconnect to camera");
				e.printStackTrace();
				closeCamera();
			}
			try {
				tryAutoFocus(false, false);
			} catch (RuntimeException e) {
				Log.e(TAG, "tryAutoFocus() threw exception: " + e.getMessage());
				e.printStackTrace();
				// this happens on Nexus 7 if trying to record video at bitrate
				// 50Mbits or higher - it's fair enough that it fails, but we
				// need to recover without a crash!
				// not safe to call closeCamera, as any call to getParameters
				// may cause a RuntimeException
				this.is_preview_started = false;
				camera.release();
				camera = null;
				openCamera();
			}
		}
	}

	private void closeCamera() {
		has_focus_area = false;
		focus_success = FOCUS_DONE;
		successfully_focused = false;
		has_set_location = false;
		has_received_location = false;
		// if( is_taking_photo_on_timer ) {
		if (this.isOnTimer()) {
			takePictureTimerTask.cancel();
			takePictureTimerTask = null;
			if (beepTimerTask != null) {
				beepTimerTask.cancel();
				beepTimerTask = null;
			}
			/*
			 * is_taking_photo_on_timer = false; is_taking_photo = false;
			 */
			this.phase = PHASE_NORMAL;
		}
		if (camera != null) {
			if (video_recorder != null) {
				stopVideo(false);
			}
			if (this.is_video) {
				// make sure we're into continuous video mode for closing
				// workaround for bug on Samsung Galaxy S5 with UHD, where if
				// the user switches to another (non-continuous-video) focus
				// mode, then goes to Settings, then returns and records video,
				// the preview freezes and the video is corrupted
				// so to be safe, we always reset to continuous video modesw
				this.updateFocusForVideo(false);
			}
			// need to check for camera being non-null again - if an error
			// occurred stopping the video, we will have closed the camera, and
			// may not be able to reopen
			if (camera != null) {
				// camera.setPreviewCallback(null);
				pausePreview();
				camera.release();
				camera = null;
			}
		}
	}

	void pausePreview() {
		if (camera == null) {
			Log.e(TAG, "camera not opened!");
			return;
		}
		this.setPreviewPaused(false);
		camera.stopPreview();
		this.phase = PHASE_NORMAL;
		this.is_preview_started = false;
		showGUI(true);
	}

	@SuppressLint("NewApi")
	private void openCamera() {
		// need to init everything now, in case we don't open the camera (but
		// these may already be initialised from an earlier call - e.g., if we
		// are now switching to another camera)
		resetParams();
		showGUI(true);
		if (!this.has_surface) {
			Log.e(TAG, "preview surface not yet available");
			return;
		}
		if (this.app_is_paused) {
			Log.e(TAG, "don't open camera as app is paused");
			return;
		}
		Activity activity = (Activity) this.getContext();
		try {
			camera = Camera.open(cameraId); // 获得一个照相机实例
		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to open camera: " + e.getMessage());
			e.printStackTrace();
			//ViewEffect.showToast(activity, "无法开启相机，请检查权限是否开启？");
			camera = null;
			activity.finish();
		}
		if (camera != null) {
			try {
			this.setCameraDisplayOrientation(activity); // 设置屏幕水平个垂直
			new OrientationEventListener(activity) {
				@Override
				public void onOrientationChanged(int orientation) {
					Preview.this.onOrientationChanged(orientation);
				}
			 }.enable();
			camera.setPreviewDisplay(mHolder); // 这里主要是可以预览照相机拍摄
			} catch (Exception e) {
				Log.e(TAG, "Failed to set preview display: " + e.getMessage());
				Toast.makeText(activity, "请检查是否用了第三方软件关闭了拍照权限?", Toast.LENGTH_SHORT).show();
				//View back = activity.findViewById(R.id.ibtn_back);
				//back.callOnClick();
				e.printStackTrace();
			}

			View switchCameraButton = (View) activity.findViewById(R.id.switch_camera);
			switchCameraButton.setVisibility(Camera.getNumberOfCameras() > 1 ? View.VISIBLE : View.GONE);
			setupCamera();
		}
	}

	private void resetParams() {
		has_set_location = false;
		has_received_location = false;
		has_focus_area = false;
		focus_success = FOCUS_DONE;
		successfully_focused = false;
		scene_modes = null;
		has_zoom = false;
		max_zoom_factor = 0;
		zoom_ratios = null;
		faces_detected = null;
		supports_face_detection = false;
		using_face_detection = false;
		color_effects = null;
		white_balances = null;
		isos = null;
		exposures = null;
		min_exposure = 0;
		max_exposure = 0;
		sizes = null;
//		current_size_index = -1;
		video_quality = null;
		current_video_quality = -1;
		supported_flash_values = null;
		current_flash_index = -1;
		supported_focus_values = null;
		current_focus_index = -1;
	}

	/*
	 * Should only be called after camera first opened, or after preview is
	 * paused.
	 */
	void setupCamera() {
		if (camera == null) {
			Log.e(TAG, "camera not opened!");
			return;
		}
		Activity activity = (Activity) this.getContext();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

		Camera.Parameters parameters = camera.getParameters();
		scene_modes = parameters.getSupportedSceneModes(); // 获得一个被支持的场景模式
		String scene_mode = setupValuesPref(scene_modes, getSceneModePreferenceKey(), Camera.Parameters.SCENE_MODE_AUTO); // 获得传去的场景模式
		if (scene_mode != null && !parameters.getSceneMode().equals(scene_mode)) {
			parameters.setSceneMode(scene_mode);
			// need to read back parameters, see comment above
			try {
				camera.setParameters(parameters);
			} catch (RuntimeException e) {
				Log.e(TAG, "failed to set parameters for focus area");
				e.printStackTrace();
			}
			parameters = camera.getParameters();
		}
		// parameters.setSceneMode("hdr");
		this.has_zoom = parameters.isZoomSupported();
		// ZoomControls zoomControls = (ZoomControls)
		// activity.findViewById(R.id.zoom);
		// SeekBar zoomSeekBar = (SeekBar)
		// activity.findViewById(R.id.zoom_seekbar);
		if (this.has_zoom) {
			this.max_zoom_factor = parameters.getMaxZoom();
			try {
				this.zoom_ratios = parameters.getZoomRatios();
			} catch (NumberFormatException e) {
				// crash java.lang.NumberFormatException: Invalid int: " 500"
				// reported in v1.4 on device "es209ra", Android 4.1, 3 Jan 2014
				// this is from java.lang.Integer.invalidInt(Integer.java:138) -
				// unclear if this is a bug in Open Camera, all we can do for
				// now is catch it
				Log.e(TAG, "NumberFormatException in getZoomRatios()");
				e.printStackTrace();
				this.has_zoom = false;
				this.zoom_ratios = null;
			}
		}

		// get face detection supported
		this.faces_detected = null;
		this.supports_face_detection = parameters.getMaxNumDetectedFaces() > 0;
		if (this.supports_face_detection) {
			this.using_face_detection = sharedPreferences.getBoolean("preference_face_detection", false);
		} else {
			this.using_face_detection = false;
		}

		if (this.using_face_detection) {
			/**
			 * @des : 面部识别检测系统
			 */
			class MyFaceDetectionListener implements Camera.FaceDetectionListener {
				@Override
				public void onFaceDetection(Face[] faces, Camera camera) {
					faces_detected = new Face[faces.length];
					System.arraycopy(faces, 0, faces_detected, 0, faces.length);
				}
			}
			camera.setFaceDetectionListener(new MyFaceDetectionListener()); // 面部检测
		}

		// get available color effects
		color_effects = parameters.getSupportedColorEffects();
		String color_effect = setupValuesPref(color_effects, getColorEffectPreferenceKey(), Camera.Parameters.EFFECT_NONE);
		if (color_effect != null) {
			parameters.setColorEffect(color_effect);
		}

		// get available white balances
		white_balances = parameters.getSupportedWhiteBalance();
		String white_balance = setupValuesPref(white_balances, getWhiteBalancePreferenceKey(), Camera.Parameters.WHITE_BALANCE_AUTO);
		if (white_balance != null) {
			parameters.setWhiteBalance(white_balance);
		}

		// get available isos - no standard value for this, see
		// http://stackoverflow.com/questions/2978095/android-camera-api-iso-setting
		{
			String iso_values = parameters.get("iso-values");
			if (iso_values == null) {
				iso_values = parameters.get("iso-mode-values"); // Galaxy Nexus
				if (iso_values == null) {
					iso_values = parameters.get("iso-speed-values"); // Micromax
																		// A101
					if (iso_values == null)
						iso_values = parameters.get("nv-picture-iso-values"); // LG
																				// dual
																				// P990
				}
			}
			if (iso_values != null && iso_values.length() > 0) {
				String[] isos_array = iso_values.split(",");
				if (isos_array != null && isos_array.length > 0) {
					isos = new ArrayList<String>();
					for (int i = 0; i < isos_array.length; i++) {
						isos.add(isos_array[i]);
					}
				}
			}
		}
		iso_key = "iso";
		if (parameters.get(iso_key) == null) {
			iso_key = "iso-speed"; // Micromax A101
			if (parameters.get(iso_key) == null) {
				iso_key = "nv-picture-iso"; // LG dual P990
				if (parameters.get(iso_key) == null)
					iso_key = null; // not supported
			}
		}
		if (iso_key != null) {
			if (isos == null) {
				// set a default for some devices which have an iso_key, but
				// don't give a list of supported ISOs
				isos = new ArrayList<String>();
				isos.add("auto");
				isos.add("100");
				isos.add("200");
				isos.add("400");
				isos.add("800");
				isos.add("1600");
			}
			String iso = setupValuesPref(isos, getISOPreferenceKey(), "auto");
			if (iso != null) {
				parameters.set(iso_key, iso);
			}
		}

		// get min/max exposure
		exposures = null;
		min_exposure = parameters.getMinExposureCompensation(); // 最小的曝光补偿
		max_exposure = parameters.getMaxExposureCompensation(); // 最大的曝光补偿
		if (min_exposure != 0 || max_exposure != 0) {
			exposures = new Vector<String>();
			for (int i = min_exposure; i <= max_exposure; i++) {
				exposures.add("" + i);
			}
			String exposure_s = setupValuesPref(exposures, getExposurePreferenceKey(), "0");
			if (exposure_s != null) {
				try {
					int exposure = Integer.parseInt(exposure_s);
					parameters.setExposureCompensation(exposure); // 曝光补偿的索引
				} catch (NumberFormatException exception) {
					Log.e(TAG, "exposure invalid format, can't parse to int");
				}
			}
		}

		// get available sizes
		sizes = parameters.getSupportedPictureSizes(); // 获得被支持的图pain大小
//		current_size_index = -1;
		String resolution_value = sharedPreferences.getString(getResolutionPreferenceKey(cameraId), "");
		if (resolution_value.length() > 0) {
			// parse the saved size, and make sure it is still valid
			int index = resolution_value.indexOf(' ');
			if (index == -1) {
				Log.e(TAG, "resolution_value invalid format, can't find space");
				Size size = getOptimalSize(sizes);
				if(size == null) {
					size = getClosestSize(sizes, ASPECT_TOLERANCE);
				}
				resolution_value = size.width + " " + size.height;
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(getResolutionPreferenceKey(cameraId), resolution_value);
				editor.apply();
			} 
		}

		// size set later in setPreviewSize()

		/*
		 * if( MyDebug.LOG ) Log.d(TAG, "Current image quality: " +
		 * parameters.getJpegQuality());
		 */
		int image_quality = getImageQuality();
		parameters.setJpegQuality(image_quality);

		// get available sizes
		initialiseVideoSizes(parameters); // 设置录像的大小
		initialiseVideoQuality(); // 设置录像的质量

		current_video_quality = -1;
		String video_quality_value_s = sharedPreferences.getString(getVideoQualityPreferenceKey(cameraId), "");
		if (video_quality_value_s.length() > 0) {
			// parse the saved video quality, and make sure it is still valid
			// now find value in valid list
			for (int i = 0; i < video_quality.size() && current_video_quality == -1; i++) {
				if (video_quality.get(i).equals(video_quality_value_s)) {
					current_video_quality = i;
				}
			}
			if (current_video_quality == -1) {
				Log.e(TAG, "failed to find valid video_quality");
			}
		}
		if (current_video_quality == -1 && video_quality.size() > 0) {
			// default to highest quality
			current_video_quality = 0;
		}
		if (current_video_quality != -1) {
			// now save, so it's available for PreferenceActivity
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(getVideoQualityPreferenceKey(cameraId), video_quality.get(current_video_quality));
			editor.apply();
		}

		parameters.getPreviewFpsRange(current_fps_range);
		// update parameters
		camera.setParameters(parameters);

		// we do the following after setting parameters, as these are done by
		// calling separate functions, that themselves set the parameters
		// directly
		List<String> supported_flash_modes = parameters.getSupportedFlashModes(); // Android
																					// format
		View flashButton = (View) activity.findViewById(R.id.flash);
//		View switchVideoButton = activity.findViewById(R.id.switch_video);
		current_flash_index = -1;
		if (supported_flash_modes != null && supported_flash_modes.size() > 1) {
			supported_flash_values = convertFlashModesToValues(supported_flash_modes); // convert
																						// to
																						// our
																						// format
																						// (also
																						// resorts)

			String flash_value = sharedPreferences.getString(getFlashPreferenceKey(cameraId), "");
			if (flash_value.length() > 0) {
				if (!updateFlash(flash_value)) {
					updateFlash(0);
				}
			} else {
				updateFlash(0);
			}
		} else {
			supported_flash_values = null;
		}
		flashButton.setVisibility(supported_flash_values != null ? View.VISIBLE : View.GONE);
		// switchVideoButton.setVisibility(supported_flash_values != null ?
		// View.VISIBLE : View.GONE);

		List<String> supported_focus_modes = parameters.getSupportedFocusModes(); // Android
																					// format
																					// View
																					// focusModeButton
																					// =
																					// (View)
																					// activity.findViewById(R.id.focus_mode);
		current_focus_index = -1;
		if (supported_focus_modes != null && supported_focus_modes.size() > 1) {
			supported_focus_values = convertFocusModesToValues(supported_focus_modes); // convert
																						// to
																						// our
																						// format
																						// (also
																						// resorts)

			String focus_value = sharedPreferences.getString(getFocusPreferenceKey(cameraId), "");
			if (focus_value.length() > 0) {
				if (!updateFocus(focus_value, false, false, true)) {
					updateFocus(0, false, true, true);
				}
			} else {
				updateFocus(0, false, true, true);
			}
		} else {
			supported_focus_values = null;
		}
		// focusModeButton.setVisibility(supported_focus_values != null ?
		// View.VISIBLE : View.GONE);

		// now switch to video if saved
		boolean saved_is_video = sharedPreferences.getBoolean(getIsVideoPreferenceKey(), false);
		Log.e("setUpCamera", "saved_is_video->" + saved_is_video + "  is_video-> " + is_video);
		if (saved_is_video != this.is_video) {
			Log.e("xxx", "switchVideo" );
			this.switchVideo(false, false);
		} else {
			Log.e("xxx", "showPhotoVideoToast" );
			showPhotoVideoToast();
		}

		// must be done after setting parameters, as this function may set
		// parameters
		if (this.has_zoom && zoom_factor != 0) {
			int new_zoom_factor = zoom_factor;
			zoom_factor = 0; // force zoomTo to actually update the zoom!
			zoomTo(new_zoom_factor, true);
		}

		// Must set preview size before starting camera preview
		// and must do it after setting photo vs video mode
		setPreviewSize(); // need to call this when we switch cameras, not just
							// when we run for the first time
		// Must call startCameraPreview after checking if face detection is
		// present - probably best to call it after setting all parameters that
		// we want
		startCameraPreview();
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				tryAutoFocus(true, false); // so we get the autofocus when
											// starting up - we do this on a
											// delay, as calling it immediately
											// means the autofocus doesn't seem
											// to work properly sometimes (at
											// least on Galaxy Nexus)
			}
		}, 500);
	}

	private String setupValuesPref(List<String> values, String key, String default_value) {
		if (values != null && values.size() > 0) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			String value = sharedPreferences.getString(key, default_value);
			// make sure result is valid
			if (!values.contains(value)) {
				if (values.contains(default_value))
					value = default_value;
				else
					value = values.get(0);
			}

			// now save, so it's available for PreferenceActivity
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(key, value);
			editor.apply();

			return value;
		} else {
			return null;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.e("xxx", "===surfaceChanged===");
		// surface size is now changed to match the aspect ratio of camera
		// preview - so we shouldn't change the preview to match the surface
		// size, so no need to restart preview here

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}
		if (camera == null) {
			return;
		}

		CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
		main_activity.layoutUI(); // need to force a layoutUI update (e.g., so
									// UI is oriented correctly when app goes
									// idle, device is then rotated, and app is
									// then resumed
	}
	
	private boolean contains(List<Size> data, Size size) {
		boolean result = false;
		if(data != null && !data.isEmpty() && size != null) {
			int sz = data.size();
			Size tmp = null;
			for(int i=0; i<sz; i++) {
				tmp = data.get(i);
				if(tmp.width == size.width && tmp.height == size.height) {
					result = true;
					break;
				}
			}
		}
		return result;
		
	}

	private void setPreviewSize() {
		// also now sets picture size
		setPictureSize();
		
		if (camera == null) {
			return;
		}
		if (is_preview_started) {
			throw new RuntimeException();
		}
//      摄像头预览检测		
		Camera.Parameters parameters = camera.getParameters();

		supported_preview_sizes = parameters.getSupportedPreviewSizes();
		if (supported_preview_sizes.size() > 0) {
			Camera.Size best_size = null;
			if(contains(supported_preview_sizes, parameters.getPictureSize())) {
				best_size = parameters.getPictureSize();
			}else {
				best_size = getOptimalSize(supported_preview_sizes);
			}
			
			if(best_size == null) {
				best_size = getClosestSize(supported_preview_sizes, getScreenRatio());
			}
			
			//解决4.3系统前置摄像头花屏问题
			if(isVideo() && Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
				Size size = parameters.getPictureSize();
				parameters.setPreviewSize(size.width, size.height);
			}else {
				parameters.setPreviewSize(best_size.width, best_size.height);
			}
			this.setAspectRatio(((double) parameters.getPreviewSize().width) / (double) parameters.getPreviewSize().height);

			try {
				camera.setParameters(parameters);
			} catch (RuntimeException e) {
				Log.e(TAG, "failed to set parameters for focus area");
				e.printStackTrace();
			}
		}

	}
	private void setPictureSize() {

		if (camera == null) {
			return;
		}
		if (is_preview_started) {
			throw new RuntimeException();
		}
		Camera.Parameters parameters = camera.getParameters();
		if (this.is_video) {
			CamcorderProfile profile = getCamcorderProfile();
			double targetRatio = ((double) profile.videoFrameWidth) / (double) profile.videoFrameHeight;
			Camera.Size best_size = getOptimalVideoPictureSize(sizes, targetRatio);
			parameters.setPictureSize(best_size.width, best_size.height);
		} else {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			String resolution_value = sharedPreferences.getString(getResolutionPreferenceKey(cameraId), "");
			if(TextUtils.isEmpty(resolution_value) || resolution_value.indexOf(' ') <0) {
				Size size = getOptimalSize(sizes);
				if(size == null) {
					size = getClosestSize(sizes, getScreenRatio());
				}
				resolution_value = size.width + " " + size.height;
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(getResolutionPreferenceKey(cameraId), resolution_value);
				editor.apply();
			}
			
			int index = resolution_value.indexOf(' ');
			String resolution_w_s = resolution_value.substring(0, index);
			String resolution_h_s = resolution_value.substring(index + 1);
			try {
				int resolution_w = Integer.parseInt(resolution_w_s);
				int resolution_h = Integer.parseInt(resolution_h_s);
				parameters.setPictureSize(resolution_w, resolution_h);
			} catch (NumberFormatException exception) {
				Log.e(TAG, "resolution_value invalid format, can't parse w or h to int");
			}
		}

		try {
			camera.setParameters(parameters);
		} catch (RuntimeException e) {
			Log.e(TAG, "failed to set parameters for focus area");
			e.printStackTrace();
		}
	}

	private void sortVideoSizes() {
		Collections.sort(this.video_sizes, new Comparator<Camera.Size>() {
			public int compare(final Camera.Size a, final Camera.Size b) {
				return b.width * b.height - a.width * a.height;
			}
		});
	}


	private void initialiseVideoSizes(Camera.Parameters parameters) {
		video_sizes = parameters.getSupportedVideoSizes();
		if (video_sizes == null) {
			video_sizes = parameters.getSupportedPreviewSizes();
		}
		this.sortVideoSizes();
	}

	private void initialiseVideoQuality() {
		SparseArray<Pair<Integer, Integer>> profiles = new SparseArray<Pair<Integer, Integer>>();
		if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) {
			CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
			profiles.put(CamcorderProfile.QUALITY_HIGH, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
		}
		if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
			CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
			profiles.put(CamcorderProfile.QUALITY_1080P, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
		}
		if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
			CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
			profiles.put(CamcorderProfile.QUALITY_720P, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
		}
		if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
			CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
			profiles.put(CamcorderProfile.QUALITY_480P, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
		}
		if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_CIF)) {
			CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_CIF);
			profiles.put(CamcorderProfile.QUALITY_CIF, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
		}
		if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA)) {
			CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
			profiles.put(CamcorderProfile.QUALITY_QVGA, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
		}
		if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QCIF)) {
			CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QCIF);
			profiles.put(CamcorderProfile.QUALITY_QCIF, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
		}
		if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) {
			CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
			profiles.put(CamcorderProfile.QUALITY_LOW, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
		}
		initialiseVideoQualityFromProfiles(profiles);
	}

	private void addVideoResolutions(boolean done_video_size[], int base_profile, int min_resolution_w, int min_resolution_h) {
		if (video_sizes == null) {
			return;
		}

		for (int i = 0; i < video_sizes.size(); i++) {
			if (done_video_size[i])
				continue;
			Camera.Size size = video_sizes.get(i);
			if (size.width == min_resolution_w && size.height == min_resolution_h) {
				String str = "" + base_profile;
				video_quality.add(str);
				done_video_size[i] = true;
			} else if (base_profile == CamcorderProfile.QUALITY_LOW || size.width * size.height >= min_resolution_w * min_resolution_h) {
				String str = "" + base_profile + "_r" + size.width + "x" + size.height;
				video_quality.add(str);
				done_video_size[i] = true;
			}
		}
	}

	public void initialiseVideoQualityFromProfiles(SparseArray<Pair<Integer, Integer>> profiles) {
		video_quality = new Vector<String>();
		boolean done_video_size[] = null;
		if (video_sizes != null) {
			done_video_size = new boolean[video_sizes.size()];
			for (int i = 0; i < video_sizes.size(); i++)
				done_video_size[i] = false;
		}
		if (profiles.get(CamcorderProfile.QUALITY_HIGH) != null) {
			Log.d(TAG, "supports QUALITY_HIGH");
			Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_HIGH);
			addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_HIGH, pair.first, pair.second);
		}
		if (profiles.get(CamcorderProfile.QUALITY_1080P) != null) {
			Log.d(TAG, "supports QUALITY_1080P");
			Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_1080P);
			addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_1080P, pair.first, pair.second);
		}
		if (profiles.get(CamcorderProfile.QUALITY_720P) != null) {
			Log.d(TAG, "supports QUALITY_720P");
			Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_720P);
			addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_720P, pair.first, pair.second);
		}
		if (profiles.get(CamcorderProfile.QUALITY_480P) != null) {
			Log.d(TAG, "supports QUALITY_480P");
			Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_480P);
			addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_480P, pair.first, pair.second);
		}
		if (profiles.get(CamcorderProfile.QUALITY_CIF) != null) {
			Log.d(TAG, "supports QUALITY_CIF");
			Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_CIF);
			addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_CIF, pair.first, pair.second);
		}
		if (profiles.get(CamcorderProfile.QUALITY_QVGA) != null) {
			Log.d(TAG, "supports QUALITY_QVGA");
			Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_QVGA);
			// addVideoResolutions(done_video_size,
			// CamcorderProfile.QUALITY_QVGA, pair.first, pair.second);
		}
		if (profiles.get(CamcorderProfile.QUALITY_QCIF) != null) {
			Log.d(TAG, "supports QUALITY_QCIF");
			Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_QCIF);
			addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_QCIF, pair.first, pair.second);
		}
		if (profiles.get(CamcorderProfile.QUALITY_LOW) != null) {
			Log.d(TAG, "supports QUALITY_LOW");
			Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_LOW);
			addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_LOW, pair.first, pair.second);
		}
	}

	private CamcorderProfile getCamcorderProfile(String quality) {
		CamcorderProfile camcorder_profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH); // default
		try {
			String profile_string = quality;
			int index = profile_string.indexOf('_');
			if (index != -1) {
				profile_string = quality.substring(0, index);
			}
			int profile = Integer.parseInt(profile_string);
			camcorder_profile = CamcorderProfile.get(cameraId, profile);
			if (index != -1 && index + 1 < quality.length()) {
				String override_string = quality.substring(index + 1);
				if (override_string.charAt(0) == 'r' && override_string.length() >= 4) {
					index = override_string.indexOf('x');
					if (index == -1) {
						Log.e(TAG, "override_string invalid format, can't find x");
					} else {
						String resolution_w_s = override_string.substring(1, index);
						String resolution_h_s = override_string.substring(index + 1);
						// copy to local variable first, so that if we fail to
						// parse height, we don't set the width either
						int resolution_w = Integer.parseInt(resolution_w_s);
						int resolution_h = Integer.parseInt(resolution_h_s);
						camcorder_profile.videoFrameWidth = resolution_w;
						camcorder_profile.videoFrameHeight = resolution_h;
					}
				}
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "failed to parse video quality: " + quality);
			e.printStackTrace();
		}
		return camcorder_profile;
	}

	public CamcorderProfile getCamcorderProfile() {
		// 4K UHD video is not yet supported by Android API (at least testing on
		// Samsung S5 and Note 3, they do not return it via
		// getSupportedVideoSizes(), nor via a CamcorderProfile (either
		// QUALITY_HIGH, or anything else)
		// but it does work if we explicitly set the resolution (at least tested
		// on an S5)
		CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		CamcorderProfile profile = null;
		/*
		 * if (cameraId == 0 &&
		 * sharedPreferences.getBoolean("preference_force_video_4k", false) &&
		 * main_activity.supportsForceVideo4K()) { if (MyDebug.LOG) Log.d(TAG,
		 * "force 4K UHD video"); profile = CamcorderProfile.get(cameraId,
		 * CamcorderProfile.QUALITY_480P); //profile.videoFrameWidth = 3840;
		 * //profile.videoFrameHeight = 2160; profile.videoBitRate = (int)
		 * (profile.videoBitRate * 2.8); } else if (current_video_quality != -1)
		 * { profile =
		 * getCamcorderProfile(video_quality.get(current_video_quality)); } else
		 */
		if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
			profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
		}else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
			profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
		}else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
			profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
		}else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) {
			profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
		}else{
			profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
		}

		String bitrate_value = sharedPreferences.getString("preference_video_bitrate", "default");
		if (!bitrate_value.equals("default")) {
			try {
				int bitrate = Integer.parseInt(bitrate_value);
				profile.videoBitRate = bitrate;
			} catch (NumberFormatException exception) {
				Log.e(TAG, "bitrate invalid format, can't parse to int: " + bitrate_value);
			}
		}
		String fps_value = sharedPreferences.getString("preference_video_fps", "default");
		if (!fps_value.equals("default")) {
			try {
				int fps = Integer.parseInt(fps_value);
				profile.videoFrameRate = fps;
			} catch (NumberFormatException exception) {
				Log.e(TAG, "fps invalid format, can't parse to int: " + fps_value);
			}
		}
		return profile;
	}

	private static String formatFloatToString(final float f) {
		final int i = (int) f;
		if (f == i)
			return Integer.toString(i);
		return String.format(Locale.getDefault(), "%.2f", f);
	}

	private static int greatestCommonFactor(int a, int b) {
		while (b > 0) {
			int temp = b;
			b = a % b;
			a = temp;
		}
		return a;
	}

	private static String getAspectRatio(int width, int height) {
		int gcf = greatestCommonFactor(width, height);
		width /= gcf;
		height /= gcf;
		return width + ":" + height;
	}

	static String getAspectRatioMPString(int width, int height) {
		float mp = (width * height) / 1000000.0f;
		return "(" + getAspectRatio(width, height) + ", " + formatFloatToString(mp) + "MP)";
	}

	String getCamcorderProfileDescription(String quality) {
		CamcorderProfile profile = getCamcorderProfile(quality);
		String highest = "";
		if (profile.quality == CamcorderProfile.QUALITY_HIGH) {
			highest = "Highest: ";
		}
		String type = "";
		if (profile.videoFrameWidth == 3840 && profile.videoFrameHeight == 2160) {
			type = "4K Ultra HD ";
		} else if (profile.videoFrameWidth == 1920 && profile.videoFrameHeight == 1080) {
			type = "Full HD ";
		} else if (profile.videoFrameWidth == 1280 && profile.videoFrameHeight == 720) {
			type = "HD ";
		} else if (profile.videoFrameWidth == 720 && profile.videoFrameHeight == 480) {
			type = "SD ";
		} else if (profile.videoFrameWidth == 640 && profile.videoFrameHeight == 480) {
			type = "VGA ";
		} else if (profile.videoFrameWidth == 352 && profile.videoFrameHeight == 288) {
			type = "CIF ";
		} else if (profile.videoFrameWidth == 320 && profile.videoFrameHeight == 240) {
			type = "QVGA ";
		} else if (profile.videoFrameWidth == 176 && profile.videoFrameHeight == 144) {
			type = "QCIF ";
		}
		String desc = highest + type + profile.videoFrameWidth + "x" + profile.videoFrameHeight + " "
				+ getAspectRatioMPString(profile.videoFrameWidth, profile.videoFrameHeight);
		return desc;
	}

	public double getTargetRatioForPreview(Point display_size) {
		double targetRatio = 0.0f;
		Activity activity = (Activity) this.getContext();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
		String preview_size = sharedPreferences.getString("preference_preview_size", "preference_preview_size_wysiwyg");
		// should always use wysiwig for video mode, otherwise we get incorrect
		// aspect ratio shown when recording video (at least on Galaxy Nexus,
		// e.g., at 640x480)
		// also not using wysiwyg mode with video caused corruption on Samsung
		// cameras (tested with Samsung S3, Android 4.3, front camera, infinity
		// focus)
		if (preview_size.equals("preference_preview_size_wysiwyg") || this.is_video) {
			if (this.is_video) {
				CamcorderProfile profile = getCamcorderProfile();
				targetRatio = ((double) profile.videoFrameWidth) / (double) profile.videoFrameHeight;
			} else {
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size picture_size = parameters.getPictureSize();
				targetRatio = ((double) picture_size.width) / (double) picture_size.height;
			}
		} else {
			// base target ratio from display size - means preview will fill the
			// device's display as much as possible
			// but if the preview's aspect ratio differs from the actual
			// photo/video size, the preview will show a cropped version of what
			// is actually taken
			targetRatio = ((double) display_size.x) / (double) display_size.y;
		}
		return targetRatio;
	}

	public Camera.Size getClosestSize(List<Camera.Size> sizes, double targetRatio) {
		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(ratio - targetRatio);
			}
		}
		return optimalSize;
	}

	
//	public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes) {
//		if (sizes == null)
//			return null;
//		Camera.Size optimalSize = null;
//		double minDiff = Double.MAX_VALUE;
//		Point display_size = new Point();
//		Activity activity = (Activity) this.getContext();
//		{
//			Display display = activity.getWindowManager().getDefaultDisplay();
//			display.getSize(display_size);
//		}
//		double targetRatio = getTargetRatioForPreview(display_size);
//		int targetHeight = Math.min(display_size.y, display_size.x);
//		if (targetHeight <= 0) {
//			targetHeight = display_size.y;
//		}
//		// Try to find an size match aspect ratio and size
//		for (Camera.Size size : sizes) {
//			double ratio = (double) size.width / size.height;
//			if(ratio == targetRatio &&  size.width >= display_size.y) {
//				optimalSize = size;
//				break;
//			}
//			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
//				continue;
//			if (Math.abs(size.height - targetHeight) < minDiff) {
//				optimalSize = size;
//				minDiff = Math.abs(size.height - targetHeight);
//			}
//		}
//		if (optimalSize == null) {
//			optimalSize = getClosestSize(sizes, targetRatio);
//		}
//		return optimalSize;
//	}
	
	final double ASPECT_TOLERANCE = 0.05;
	public Camera.Size getOptimalSize(List<Camera.Size> sizes) {
		Size optimalSize = null;
		if(sizes != null && !sizes.isEmpty()) {
			double screenRatio = getScreenRatio();
			Point p = getScreenSize();
			int size = sizes.size();
			Size tmp = null;
			for(int i=0; i<size; i++) {
				tmp = sizes.get(i);
				if(tmp.width == p.y && tmp.height == p.x) {
					optimalSize = tmp;
					break;
				}
				double ratio = (double) tmp.width / tmp.height;
				if(Math.abs(ratio - screenRatio) <= ASPECT_TOLERANCE ) {
					if(optimalSize == null || optimalSize.width * optimalSize.height <= tmp.width * tmp.height) {
						optimalSize = tmp;
					}
				}
			}
		}
		
		return optimalSize;
	}

	private double getScreenRatio() {
		Point screenPoint = new Point();
		Activity activity = (Activity) this.getContext();
		Display display = activity.getWindowManager().getDefaultDisplay();
		display.getSize(screenPoint);
		return screenPoint.y / ((double)screenPoint.x);
	} 
	
	private Point getScreenSize() {
		Point screenPoint = new Point();
		Activity activity = (Activity) this.getContext();
		Display display = activity.getWindowManager().getDefaultDisplay();
		display.getSize(screenPoint);
		return screenPoint;
	} 

	public Camera.Size getOptimalVideoPictureSize(List<Camera.Size> sizes, double targetRatio) {
		final double ASPECT_TOLERANCE = 0.05;
		if (sizes == null)
			return null;
		Camera.Size optimalSize = null;
		// Try to find largest size that matches aspect ratio
		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (optimalSize == null || size.width > optimalSize.width) {
				optimalSize = size;
			}
		}
		if (optimalSize == null) {
			optimalSize = getClosestSize(sizes, targetRatio);
		}

		return optimalSize;
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		if (!this.has_aspect_ratio) {
			super.onMeasure(widthSpec, heightSpec);
			return;
		}
		int previewWidth = MeasureSpec.getSize(widthSpec);
		int previewHeight = MeasureSpec.getSize(heightSpec);

		// Get the padding of the border background.
		int hPadding = getPaddingLeft() + getPaddingRight();
		int vPadding = getPaddingTop() + getPaddingBottom();

		// Resize the preview frame with correct aspect ratio.
		previewWidth -= hPadding;
		previewHeight -= vPadding;

		boolean widthLonger = previewWidth > previewHeight;
		int longSide = (widthLonger ? previewWidth : previewHeight);
		int shortSide = (widthLonger ? previewHeight : previewWidth);
		if (longSide > shortSide * aspect_ratio) {
			longSide = (int) ((double) shortSide * aspect_ratio);
		} else {
			shortSide = (int) ((double) longSide / aspect_ratio);
		}
		if (widthLonger) {
			previewWidth = longSide;
			previewHeight = shortSide;
		} else {
			previewWidth = shortSide;
			previewHeight = longSide;
		}

		// Add the padding of the border.
		previewWidth += hPadding;
		previewHeight += vPadding;

		// Ask children to follow the new preview dimension.
		super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
	}

	private void setAspectRatio(double ratio) {
		if (ratio <= 0.0)
			throw new IllegalArgumentException();

		has_aspect_ratio = true;
		if (aspect_ratio != ratio) {
			aspect_ratio = ratio;
			requestLayout();
		}
	}

	// for the Preview - from
	// http://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
	// note, if orientation is locked to landscape this is only called when
	// setting up the activity, and will always have the same orientation
	void setCameraDisplayOrientation(Activity activity) throws Exception {
		if (camera == null)
			return;
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
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

		int result = 0;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else {
			result = (info.orientation - degrees + 360) % 360;
		}

		try
		{
		   camera.setDisplayOrientation(result);
		   this.display_orientation = result;
		}catch(Exception e)
		{
			try{
		     camera.setPreviewCallback(null) ;
             camera.stopPreview();
             camera.release();
			}
             catch (Exception e2) {
				// TODO: handle exception
            	 throw new Exception();
			}finally
			{
             camera = null;
			}
			 e.printStackTrace();	
		}
	}

	// for taking photos - from
	// http://developer.android.com/reference/android/hardware/Camera.Parameters.html#setRotation(int)
	private void onOrientationChanged(int orientation) {
		/*
		 * if( MyDebug.LOG ) { Log.d(TAG, "onOrientationChanged()"); Log.d(TAG,
		 * "orientation: " + orientation); }
		 */
		if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN)
			return;
		if (camera == null)
			return;
		Camera.getCameraInfo(cameraId, camera_info);
		orientation = (orientation + 45) / 90 * 90;
		this.current_orientation = orientation % 360;
		int new_rotation = 0;
		if (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			new_rotation = (camera_info.orientation - orientation + 360) % 360;
		} else {
			new_rotation = (camera_info.orientation + orientation) % 360;
		}
		if (new_rotation != current_rotation) {
			/*
			 * if( MyDebug.LOG ) { Log.d(TAG, "    current_orientation is " +
			 * current_orientation); Log.d(TAG, "    info orientation is " +
			 * camera_info.orientation); Log.d(TAG,
			 * "    set Camera rotation from " + current_rotation + " to " +
			 * new_rotation); }
			 */
			this.current_rotation = new_rotation;
		}
	}

	private int getDeviceDefaultOrientation() {
		WindowManager windowManager = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
		Configuration config = getResources().getConfiguration();
		int rotation = windowManager.getDefaultDisplay().getRotation();
		if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
				|| ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
			return Configuration.ORIENTATION_LANDSCAPE;
		} else {
			return Configuration.ORIENTATION_PORTRAIT;
		}
	}

	/*
	 * Returns the rotation to use for images/videos, taking the
	 * preference_lock_orientation into account.
	 */
	private int getImageVideoRotation() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		String lock_orientation = sharedPreferences.getString("preference_lock_orientation", preference_lock_orientation_config);
		if (lock_orientation.equals("landscape")) {
			int device_orientation = getDeviceDefaultOrientation();
			Camera.getCameraInfo(cameraId, camera_info);
			int result = 0;
			if (device_orientation == Configuration.ORIENTATION_PORTRAIT) {
				// should be equivalent to onOrientationChanged(270)
				if (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					result = (camera_info.orientation + 90) % 360;
				} else {
					result = (camera_info.orientation + 270) % 360;
				}
			} else {
				// should be equivalent to onOrientationChanged(0)
				result = camera_info.orientation;
			}
			return result;
		} else if (lock_orientation.equals("portrait")) {
			Camera.getCameraInfo(cameraId, camera_info);
			int result = 0;
			int device_orientation = getDeviceDefaultOrientation();
			if (device_orientation == Configuration.ORIENTATION_PORTRAIT) {
				// should be equivalent to onOrientationChanged(0)
				result = camera_info.orientation;
			} else {
				// should be equivalent to onOrientationChanged(90)
				if (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					result = (camera_info.orientation + 270) % 360;
				} else {
					result = (camera_info.orientation + 90) % 360;
				}
			}
			return result;
		}
		return this.current_rotation;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (this.app_is_paused) {
			return;
		}

		CameraActivity main_activity = (CameraActivity) this.getContext();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		final float scale = getResources().getDisplayMetrics().density;
		if (camera != null && sharedPreferences.getString("preference_grid", "preference_grid_none").equals("preference_grid_3x3")) {
			p.setColor(Color.WHITE);
			canvas.drawLine(canvas.getWidth() / 3.0f, 0.0f, canvas.getWidth() / 3.0f, canvas.getHeight() - 1.0f, p);
			canvas.drawLine(2.0f * canvas.getWidth() / 3.0f, 0.0f, 2.0f * canvas.getWidth() / 3.0f, canvas.getHeight() - 1.0f, p);
			canvas.drawLine(0.0f, canvas.getHeight() / 3.0f, canvas.getWidth() - 1.0f, canvas.getHeight() / 3.0f, p);
			canvas.drawLine(0.0f, 2.0f * canvas.getHeight() / 3.0f, canvas.getWidth() - 1.0f, 2.0f * canvas.getHeight() / 3.0f, p);
		}
		if (camera != null && sharedPreferences.getString("preference_grid", "preference_grid_none").equals("preference_grid_4x2")) {
			p.setColor(Color.GRAY);
			canvas.drawLine(canvas.getWidth() / 4.0f, 0.0f, canvas.getWidth() / 4.0f, canvas.getHeight() - 1.0f, p);
			canvas.drawLine(canvas.getWidth() / 2.0f, 0.0f, canvas.getWidth() / 2.0f, canvas.getHeight() - 1.0f, p);
			canvas.drawLine(3.0f * canvas.getWidth() / 4.0f, 0.0f, 3.0f * canvas.getWidth() / 4.0f, canvas.getHeight() - 1.0f, p);
			canvas.drawLine(0.0f, canvas.getHeight() / 2.0f, canvas.getWidth() - 1.0f, canvas.getHeight() / 2.0f, p);
			p.setColor(Color.WHITE);
			int crosshairs_radius = (int) (20 * scale + 0.5f); // convert dps to
																// pixels
			canvas.drawLine(canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f - crosshairs_radius, canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f
					+ crosshairs_radius, p);
			canvas.drawLine(canvas.getWidth() / 2.0f - crosshairs_radius, canvas.getHeight() / 2.0f, canvas.getWidth() / 2.0f + crosshairs_radius,
					canvas.getHeight() / 2.0f, p);
		}

		if (camera != null && this.thumbnail_anim && this.thumbnail != null) {
			long time = System.currentTimeMillis() - this.thumbnail_anim_start_ms;
			final long duration = 500;
			if (time > duration) {
				this.thumbnail_anim = false;
			} else {
				thumbnail_anim_src_rect.left = 0;
				thumbnail_anim_src_rect.top = 0;
				thumbnail_anim_src_rect.right = this.thumbnail.getWidth();
				thumbnail_anim_src_rect.bottom = this.thumbnail.getHeight();
				thumbnail_anim_matrix.setRectToRect(thumbnail_anim_src_rect, thumbnail_anim_dst_rect, Matrix.ScaleToFit.FILL);
				if (ui_rotation == 90 || ui_rotation == 270) {
					float ratio = ((float) thumbnail.getWidth()) / (float) thumbnail.getHeight();
					thumbnail_anim_matrix.preScale(ratio, 1.0f / ratio, thumbnail.getWidth() / 2, thumbnail.getHeight() / 2);
				}
				thumbnail_anim_matrix.preRotate(ui_rotation, thumbnail.getWidth() / 2, thumbnail.getHeight() / 2);
				canvas.drawBitmap(this.thumbnail, thumbnail_anim_matrix, p);
			}
		}

		canvas.save();
		canvas.rotate(ui_rotation, canvas.getWidth() / 2, canvas.getHeight() / 2);
		int text_y = (int) (20 * scale + 0.5f); // convert dps to pixels
		int text_base_y = 0;
		if (ui_rotation == 0) {
			text_base_y = canvas.getHeight() - (int) (0.5 * text_y);
		} else if (ui_rotation == 180) {
			text_base_y = canvas.getHeight() - (int) (2.5 * text_y);
		} else if (ui_rotation == 90 || ui_rotation == 270) {
			int view_left = gui_location[0];
			this.getLocationOnScreen(gui_location);
			int this_left = gui_location[0];
			int diff_x = view_left - (this_left + canvas.getWidth() / 2);
			int max_x = canvas.getWidth();
			if (ui_rotation == 90) {
				max_x -= (int) (1.5 * text_y);
			}
			if (canvas.getWidth() / 2 + diff_x > max_x) {
				diff_x = max_x - canvas.getWidth() / 2;
			}
			text_base_y = canvas.getHeight() / 2 + diff_x - (int) (0.5 * text_y);
		}

		final double close_angle = 1.0f;
		if (camera != null && this.phase != PHASE_PREVIEW_PAUSED) {
			boolean draw_angle = this.has_level_angle && sharedPreferences.getBoolean("preference_show_angle", true);
			boolean draw_geo_direction = this.has_geo_direction && sharedPreferences.getBoolean("preference_show_geo_direction", true);
			if (draw_angle) {
				// int color = Color.WHITE;
				p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
				// int pixels_offset_x = 0;
				if (draw_geo_direction) {
					// pixels_offset_x = -(int) (80 * scale + 0.5f); // convert
					// dps
					// to pixels
					p.setTextAlign(Paint.Align.LEFT);
				} else {
					p.setTextAlign(Paint.Align.CENTER);
				}
				if (Math.abs(this.level_angle) <= close_angle) {
					// color = Color.GREEN;
				}
			}
			if (draw_geo_direction) {
				// int color = Color.WHITE;
				p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
				if (draw_angle) {
					p.setTextAlign(Paint.Align.LEFT);
				} else {
					p.setTextAlign(Paint.Align.CENTER);
				}
				float geo_angle = (float) Math.toDegrees(this.geo_direction[0]);
				if (geo_angle < 0.0f) {
					geo_angle += 360.0f;
				}
			}
			// if( this.is_taking_photo_on_timer ) {
			if (this.isOnTimer()) {
				long remaining_time = (take_photo_time - System.currentTimeMillis() + 999) / 1000;
				if (remaining_time >= 0) {
					p.setTextSize(42 * scale + 0.5f); // convert dps to pixels
					p.setTextAlign(Paint.Align.CENTER);
					drawTextWithBackground(canvas, p, "" + remaining_time, Color.RED, Color.rgb(75, 75, 75), canvas.getWidth() / 2, canvas.getHeight() / 2);
				}
			} else if (this.video_recorder != null && video_start_time_set) {
				long video_time = (System.currentTimeMillis() - video_start_time);
				// int ms = (int)(video_time % 1000);
				video_time /= 1000;
				int secs = (int) (video_time % 60);
				video_time /= 60;
				int mins = (int) (video_time % 60);
				video_time /= 60;
				long hours = video_time;
				// String time_s = hours + ":" + String.format("%02d", mins) +
				// ":" + String.format("%02d", secs) + ":" +
				// String.format("%03d", ms);
				String time_s = hours + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs);
				/*
				 * if( MyDebug.LOG ) Log.d(TAG, "video_time: " + video_time +
				 * " " + time_s);
				 */
				if (main_activity.isScreenLocked()) {
					time_s += "  " + getResources().getString(R.string.screen_lock_message);
				}
				if (this.txtVideoTime != null) {
					this.txtVideoTime.setText("0" + time_s);
				}

			} else if (this.video_recorder == null) {
				if (this.is_video) {
					if (this.txtVideoTime != null) {
						this.txtVideoTime.setText("00:00:00");
					}
				}
			}
		} else if (camera == null) {
			/*
			 * if( MyDebug.LOG ) { Log.d(TAG, "no camera!"); Log.d(TAG, "width "
			 * + canvas.getWidth() + " height " + canvas.getHeight()); }
			 */
			p.setColor(Color.WHITE);
			p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
			p.setTextAlign(Paint.Align.CENTER);
		}
		if (this.has_zoom && camera != null && sharedPreferences.getBoolean("preference_show_zoom", true)) {
			float zoom_ratio = this.zoom_ratios.get(zoom_factor) / 100.0f;
			// only show when actually zoomed in
			if (zoom_ratio > 1.0f + 1.0e-5f) {
				// Convert the dps to pixels, based on density scale
				int pixels_offset_y = 2 * text_y;
				p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
				p.setTextAlign(Paint.Align.CENTER);
//				drawTextWithBackground(canvas, p, getResources().getString(R.string.zoom) + ": " + zoom_ratio + "x", Color.WHITE, Color.BLACK,
//						canvas.getWidth() / 2, text_base_y - pixels_offset_y);
			}
		}
		if (camera != null && sharedPreferences.getBoolean("preference_free_memory", true)) {
			// int pixels_offset_y = 1 * text_y;
			p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
			p.setTextAlign(Paint.Align.CENTER);
			long time_now = System.currentTimeMillis();
			if (free_memory_gb < 0.0f || time_now > last_free_memory_time + 1000) {
				long free_mb = main_activity.freeMemory();
				if (free_mb >= 0) {
					free_memory_gb = free_mb / 1024.0f;
					last_free_memory_time = time_now;
				}
			}
			if (free_memory_gb >= 0.0f) {
				// drawTextWithBackground(canvas, p,
				// getResources().getString(R.string.free_memory) + ": " +
				// decimalFormat.format(free_memory_gb) + "GB",
				// Color.WHITE, Color.BLACK, canvas.getWidth() / 2, text_base_y
				// - pixels_offset_y);
			}
		}

		{
			if (!this.has_battery_frac || System.currentTimeMillis() > this.last_battery_time + 60000) {
				// only check periodically - unclear if checking is costly in
				// any way
				Intent batteryStatus = main_activity.registerReceiver(null, battery_ifilter);
				int battery_level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int battery_scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				has_battery_frac = true;
				battery_frac = battery_level / (float) battery_scale;
				last_battery_time = System.currentTimeMillis();
			}
			// battery_frac = 0.2999f; // test
			int battery_x = (int) (5 * scale + 0.5f); // convert dps to pixels
			int battery_y = battery_x;
			int battery_width = (int) (5 * scale + 0.5f); // convert dps to
															// pixels
			int battery_height = 4 * battery_width;
			if (ui_rotation == 90 || ui_rotation == 270) {
				int diff = canvas.getWidth() - canvas.getHeight();
				battery_x += diff / 2;
				battery_y -= diff / 2;
			}
			if (ui_rotation == 90) {
				battery_y = canvas.getHeight() - battery_y - battery_height;
			}
			if (ui_rotation == (ui_placement_right ? 180 : 0)) {
				battery_x = canvas.getWidth() - battery_x - battery_width;
			}
			// p.setColor(Color.WHITE);
			// p.setStyle(Paint.Style.STROKE);
			// canvas.drawRect(battery_x, battery_y, battery_x + battery_width,
			// battery_y + battery_height, p);
			// p.setColor(battery_frac >= 0.3f ? Color.GREEN : Color.RED);
			// p.setStyle(Paint.Style.FILL);
			// canvas.drawRect(battery_x + 1, battery_y + 1 + (1.0f -
			// battery_frac) * (battery_height - 2), battery_x + battery_width -
			// 1, battery_y
			// + battery_height - 1, p);
		}

		boolean store_location = sharedPreferences.getBoolean("preference_location", false);
		if (store_location) {
			int location_x = (int) (20 * scale + 0.5f); // convert dps to pixels
			int location_y = (int) (5 * scale + 0.5f); // convert dps to pixels
			int location_size = (int) (20 * scale + 0.5f); // convert dps to
															// pixels
			if (ui_rotation == 90 || ui_rotation == 270) {
				int diff = canvas.getWidth() - canvas.getHeight();
				location_x += diff / 2;
				location_y -= diff / 2;
			}
			if (ui_rotation == 90) {
				location_y = canvas.getHeight() - location_y - location_size;
			}
			if (ui_rotation == (ui_placement_right ? 180 : 0)) {
				location_x = canvas.getWidth() - location_x - location_size;
			}
			location_dest.set(location_x, location_y, location_x + location_size, location_y + location_size);
			if (has_set_location) {
				canvas.drawBitmap(location_bitmap, null, location_dest, p);
				int indicator_x = location_x + location_size;
				int indicator_y = location_y;
				p.setStyle(Paint.Style.FILL_AND_STROKE);
				p.setColor(location_accuracy < 25.01f ? Color.GREEN : Color.YELLOW);
				canvas.drawCircle(indicator_x, indicator_y, location_size / 10, p);
			} else {
				canvas.drawBitmap(location_off_bitmap, null, location_dest, p);
			}
		}

		{
			p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
			p.setTextAlign(Paint.Align.LEFT);
			int location_x = (int) (50 * scale + 0.5f); // convert dps to pixels
			int location_y = (int) (15 * scale + 0.5f); // convert dps to pixels
			if (ui_rotation == 90 || ui_rotation == 270) {
				int diff = canvas.getWidth() - canvas.getHeight();
				location_x += diff / 2;
				location_y -= diff / 2;
			}
			if (ui_rotation == 90) {
				location_y = canvas.getHeight() - location_y;
			}
			if (ui_rotation == (ui_placement_right ? 180 : 0)) {
				location_x = canvas.getWidth() - location_x;
				p.setTextAlign(Paint.Align.RIGHT);
			}
			// Calendar c = Calendar.getInstance();
			// String current_time =
			// DateFormat.getTimeInstance().format(c.getTime());
			// drawTextWithBackground(canvas, p, current_time, Color.WHITE,
			// Color.BLACK, location_x, location_y);
		}

		canvas.restore();

		if (camera != null && this.phase != PHASE_PREVIEW_PAUSED && has_level_angle && sharedPreferences.getBoolean("preference_show_angle_line", false)) {
			// n.b., must draw this without canvas rotation
			int radius_dps = (ui_rotation == 90 || ui_rotation == 270) ? 60 : 80;
			int radius = (int) (radius_dps * scale + 0.5f); // convert dps to
															// pixels
			double angle = -this.orig_level_angle;
			// see
			// http://android-developers.blogspot.co.uk/2010/09/one-screen-turn-deserves-another.html
			int rotation = main_activity.getWindowManager().getDefaultDisplay().getRotation();
			switch (rotation) {
			case Surface.ROTATION_90:
			case Surface.ROTATION_270:
				angle += 90.0;
				break;
			}
			/*
			 * if( MyDebug.LOG ) { Log.d(TAG, "orig_level_angle: " +
			 * orig_level_angle); Log.d(TAG, "angle: " + angle); }
			 */
			int off_x = (int) (radius * Math.cos(Math.toRadians(angle)));
			int off_y = (int) (radius * Math.sin(Math.toRadians(angle)));
			int cx = canvas.getWidth() / 2;
			int cy = canvas.getHeight() / 2;
			if (Math.abs(this.level_angle) <= close_angle) { // n.b., use
																// level_angle,
																// not angle or
																// orig_level_angle
				p.setColor(Color.GREEN);
			} else {
				p.setColor(Color.WHITE);
			}
			canvas.drawLine(cx - off_x, cy - off_y, cx + off_x, cy + off_y, p);
		}

		if (this.focus_success != FOCUS_DONE) {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_focus);

			int size = (int) (50 * scale + 0.5f); // convert dps to pixels
			if (this.focus_success == FOCUS_SUCCESS)
				p.setColor(Color.GREEN);
			else if (this.focus_success == FOCUS_FAILED)
				p.setColor(Color.RED);
			else
				p.setColor(Color.WHITE);
			p.setStyle(Paint.Style.STROKE);
			int pos_x = 0;
			int pos_y = 0;
			if (has_focus_area) {
				pos_x = focus_screen_x;
				pos_y = focus_screen_y;
			} else {
				pos_x = canvas.getWidth() / 2;
				pos_y = canvas.getHeight() / 2;
			}
			canvas.drawBitmap(bitmap, pos_x - size, pos_y - size, p);
			// canvas.drawRect(pos_x - size, pos_y - size, pos_x + size, pos_y +
			// size, p);
			if (focus_complete_time != -1 && System.currentTimeMillis() > focus_complete_time + 1000) {
				focus_success = FOCUS_DONE;
			}
			p.setStyle(Paint.Style.FILL); // reset
		}
		if (this.using_face_detection && this.faces_detected != null) {
			p.setColor(Color.YELLOW);
			p.setStyle(Paint.Style.STROKE);
			for (Face face : faces_detected) {
				// Android doc recommends filtering out faces with score less
				// than 50
				if (face.score >= 50) {
					calculateCameraToPreviewMatrix();
					face_rect.set(face.rect);
					this.camera_to_preview_matrix.mapRect(face_rect);
					/*
					 * int eye_radius = (int) (5 * scale + 0.5f); // convert dps
					 * to pixels int mouth_radius = (int) (10 * scale + 0.5f);
					 * // convert dps to pixels float [] top_left =
					 * {face.rect.left, face.rect.top}; float [] bottom_right =
					 * {face.rect.right, face.rect.bottom};
					 * canvas.drawRect(top_left[0], top_left[1],
					 * bottom_right[0], bottom_right[1], p);
					 */
					canvas.drawRect(face_rect, p);
					/*
					 * if( face.leftEye != null ) { float [] left_point =
					 * {face.leftEye.x, face.leftEye.y};
					 * cameraToPreview(left_point);
					 * canvas.drawCircle(left_point[0], left_point[1],
					 * eye_radius, p); } if( face.rightEye != null ) { float []
					 * right_point = {face.rightEye.x, face.rightEye.y};
					 * cameraToPreview(right_point);
					 * canvas.drawCircle(right_point[0], right_point[1],
					 * eye_radius, p); } if( face.mouth != null ) { float []
					 * mouth_point = {face.mouth.x, face.mouth.y};
					 * cameraToPreview(mouth_point);
					 * canvas.drawCircle(mouth_point[0], mouth_point[1],
					 * mouth_radius, p); }
					 */
				}
			}
			p.setStyle(Paint.Style.FILL); // reset
		}
	}

	private void drawTextWithBackground(Canvas canvas, Paint paint, String text, int foreground, int background, int location_x, int location_y) {
		final float scale = getResources().getDisplayMetrics().density;
		p.setStyle(Paint.Style.FILL);
		paint.setColor(background);
		paint.setAlpha(127);
		paint.getTextBounds(text, 0, text.length(), text_bounds);
		final int padding = (int) (2 * scale + 0.5f); // convert dps to pixels
		if (paint.getTextAlign() == Paint.Align.RIGHT || paint.getTextAlign() == Paint.Align.CENTER) {
			float width = paint.measureText(text); // n.b., need to use
													// measureText rather than
													// getTextBounds here
			/*
			 * if( MyDebug.LOG ) Log.d(TAG, "width: " + width);
			 */
			if (paint.getTextAlign() == Paint.Align.CENTER)
				width /= 2.0f;
			text_bounds.left -= width;
			text_bounds.right -= width;
		}
		/*
		 * if( MyDebug.LOG ) Log.d(TAG, "text_bounds left-right: " +
		 * text_bounds.left + " , " + text_bounds.right);
		 */
		text_bounds.left += location_x - padding;
		text_bounds.top += location_y - padding;
		text_bounds.right += location_x + padding;
		text_bounds.bottom += location_y + padding;
		canvas.drawRect(text_bounds, paint);
		paint.setColor(foreground);
		canvas.drawText(text, location_x, location_y, paint);
	}

	public void scaleZoom(float scale_factor) {
		if (this.camera != null && this.has_zoom) {
			float zoom_ratio = this.zoom_ratios.get(zoom_factor) / 100.0f;
			zoom_ratio *= scale_factor;

			int new_zoom_factor = zoom_factor;
			if (zoom_ratio <= 1.0f) {
				new_zoom_factor = 0;
			} else if (zoom_ratio >= zoom_ratios.get(max_zoom_factor) / 100.0f) {
				new_zoom_factor = max_zoom_factor;
			} else {
				// find the closest zoom level
				if (scale_factor > 1.0f) {
					// zooming in
					for (int i = zoom_factor; i < zoom_ratios.size(); i++) {
						if (zoom_ratios.get(i) / 100.0f >= zoom_ratio) {
							new_zoom_factor = i;
							break;
						}
					}
				} else {
					// zooming out
					for (int i = zoom_factor; i >= 0; i--) {
						if (zoom_ratios.get(i) / 100.0f <= zoom_ratio) {
							new_zoom_factor = i;
							break;
						}
					}
				}
			}
			zoomTo(new_zoom_factor, true);
		}
	}

	public void zoomIn() {
		if (zoom_factor < max_zoom_factor) {
			zoomTo(zoom_factor + 1, true);
		}
	}

	public void zoomOut() {
		if (zoom_factor > 0) {
			zoomTo(zoom_factor - 1, true);
		}
	}

	public void zoomTo(int new_zoom_factor, boolean update_seek_bar) {
		if (new_zoom_factor < 0)
			new_zoom_factor = 0;
		if (new_zoom_factor > max_zoom_factor)
			new_zoom_factor = max_zoom_factor;
		// problem where we crashed due to calling this function with null
		// camera should be fixed now, but check again just to be safe
		if (new_zoom_factor != zoom_factor && camera != null) {
			Camera.Parameters parameters = camera.getParameters();
			if (parameters.isZoomSupported()) {
				parameters.setZoom((int) new_zoom_factor);
				try {
					try {
						camera.setParameters(parameters);
					} catch (RuntimeException e) {
						Log.e(TAG, "failed to set parameters for focus area");
						e.printStackTrace();
					}
					zoom_factor = new_zoom_factor;
					if (update_seek_bar) {
						// Activity activity = (Activity) this.getContext();
						// SeekBar zoomSeekBar = (SeekBar)
						// activity.findViewById(R.id.zoom_seekbar);
						// zoomSeekBar.setProgress(max_zoom_factor -
						// zoom_factor);
					}
				} catch (RuntimeException e) {
					Log.e(TAG, "runtime exception in ZoomTo()");
					e.printStackTrace();
				}
				clearFocusAreas();
			}
		}
	}

	/**
	 * 璁剧疆鏇濆厜
	 * 
	 * @param change
	 * @param update_seek_bar
	 */
	public void changeExposure(int change, boolean update_seek_bar) {
		if (change != 0 && camera != null && (min_exposure != 0 || max_exposure != 0)) {
			Camera.Parameters parameters = camera.getParameters();
			int current_exposure = parameters.getExposureCompensation();
			int new_exposure = current_exposure + change;
			setExposure(new_exposure, update_seek_bar);
		}
	}

	public void setExposure(int new_exposure, boolean update_seek_bar) {
		if (camera != null && (min_exposure != 0 || max_exposure != 0)) {
			cancelAutoFocus();
			Camera.Parameters parameters = camera.getParameters();
			int current_exposure = parameters.getExposureCompensation();
			if (new_exposure < min_exposure)
				new_exposure = min_exposure;
			if (new_exposure > max_exposure)
				new_exposure = max_exposure;
			if (new_exposure != current_exposure) {
				parameters.setExposureCompensation(new_exposure);
				try {
					camera.setParameters(parameters);
					// now save
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(getExposurePreferenceKey(), "" + new_exposure);
					editor.apply();
					// showToast(change_exposure_toast,
					// getResources().getString(R.string.exposure_compensation)
					// + " " + new_exposure);
					if (update_seek_bar) {
					}
				} catch (RuntimeException e) {
					Log.e(TAG, "runtime exception in changeExposure()");
					e.printStackTrace();
				}
			}
		}
	}

	void switchCamera() {
		// if( is_taking_photo && !is_taking_photo_on_timer ) {
		if (this.phase == PHASE_TAKING_PHOTO) {
			// just to be safe - risk of cancelling the autofocus before taking
			// a photo, or otherwise messing things up
			Log.e(TAG, "currently taking a photo");
			return;
		}
		int n_cameras = Camera.getNumberOfCameras();
		if (n_cameras > 1) {
			closeCamera();
			cameraId = (cameraId + 1) % n_cameras;
			Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, info);
			Activity activity = (Activity) this.getContext();
			ImageButton flash = (ImageButton) activity.findViewById(R.id.flash);
			if (activity != null) {
				if (cameraId > 0) {
					flash.setVisibility(View.GONE);
				} else {
					flash.setVisibility(View.VISIBLE);
				}
			}

			// zoom_factor = 0; // reset zoom when switching camera
			Log.e("switchcamera", "call openCamera");
			this.openCamera();

			// we update the focus, in case we weren't able to do it when
			// switching video with a camera that didn't support focus modes
			updateFocusForVideo(true);
		}
	}

	private void showPhotoVideoToast() {
		CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
		if (main_activity.cameraInBackground())
			return;
		String toast_string = "";
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		if (this.is_video) {
			CamcorderProfile profile = getCamcorderProfile();
			String bitrate_string = "";
			if (profile.videoBitRate >= 10000000)
				bitrate_string = profile.videoBitRate / 1000000 + "Mbps";
			else if (profile.videoBitRate >= 10000)
				bitrate_string = profile.videoBitRate / 1000 + "Kbps";
			else
				bitrate_string = profile.videoBitRate + "bps";

			String timer_value = sharedPreferences.getString("preference_video_max_duration", "0");
			toast_string = getResources().getString(R.string.video) + ": " + profile.videoFrameWidth + "x" + profile.videoFrameHeight + ", "
					+ profile.videoFrameRate + "fps, " + bitrate_string;
			if (timer_value.length() > 0 && !timer_value.equals("0")) {
				String[] entries_array = getResources().getStringArray(R.array.preference_video_max_duration_entries);
				String[] values_array = getResources().getStringArray(R.array.preference_video_max_duration_values);
				int index = Arrays.asList(values_array).indexOf(timer_value);
				String entry = entries_array[index];
				toast_string += "\nMax duration: " + entry;
			}
		} else {
			toast_string = getResources().getString(R.string.photo);
//			if (current_size_index != -1 && sizes != null) {
//				Camera.Size current_size = sizes.get(current_size_index);
//				toast_string += " " + current_size.width + "x" + current_size.height;
//			}
		}
		String lock_orientation = sharedPreferences.getString("preference_lock_orientation", preference_lock_orientation_config);
		if (lock_orientation.equals("landscape")) {
			toast_string += "\nLocked to landscape";
		} else if (lock_orientation.equals("portrait")) {
			toast_string += "\nLocked to portrait";
		}
//		 showToast(switch_video_toast, toast_string);
	}

	private void matchPreviewFpsToVideo() {
		CamcorderProfile profile = getCamcorderProfile();
		Camera.Parameters parameters = camera.getParameters();

		List<int[]> fps_ranges = parameters.getSupportedPreviewFpsRange();
		int selected_min_fps = -1, selected_max_fps = -1, selected_diff = -1;
		for (int[] fps_range : fps_ranges) {
			int min_fps = fps_range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
			int max_fps = fps_range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
			if (min_fps <= profile.videoFrameRate * 1000 && max_fps >= profile.videoFrameRate * 1000) {
				int diff = max_fps - min_fps;
				if (selected_diff == -1 || diff < selected_diff) {
					selected_min_fps = min_fps;
					selected_max_fps = max_fps;
					selected_diff = diff;
				}
			}
		}
		if (selected_min_fps == -1) {
			selected_diff = -1;
			int selected_dist = -1;
			for (int[] fps_range : fps_ranges) {
				int min_fps = fps_range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
				int max_fps = fps_range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
				int diff = max_fps - min_fps;
				int dist = -1;
				if (max_fps < profile.videoFrameRate * 1000)
					dist = profile.videoFrameRate * 1000 - max_fps;
				else
					dist = min_fps - profile.videoFrameRate * 1000;
				if (selected_dist == -1 || dist < selected_dist || (dist == selected_dist && diff < selected_diff)) {
					selected_min_fps = min_fps;
					selected_max_fps = max_fps;
					selected_dist = dist;
					selected_diff = diff;
				}
			}

			parameters.setPreviewFpsRange(selected_min_fps, selected_max_fps);
			try {
				camera.setParameters(parameters);
			} catch (RuntimeException e) {
				Log.e(TAG, "failed to set parameters for focus area");
				e.printStackTrace();
			}
		} else {
			parameters.setPreviewFpsRange(selected_min_fps, selected_max_fps);
			try {
				camera.setParameters(parameters);
			} catch (RuntimeException e) {
				Log.e(TAG, "failed to set parameters for focus area");
				e.printStackTrace();
			}
		}
	}

	/**
	 * @describtion  交换视频/图片 抓取
	 * @param save
	 * @param update_preview_size
	 */
	void switchVideo(boolean save, boolean update_preview_size) {
		if (this.camera == null) {
			return;
		}
		
		boolean old_is_video = is_video;
		Log.e("switchVideo", "old_is_video-->" + old_is_video);
		if (this.is_video) {
			if (video_recorder != null) {
				if (camera != null) {
					Parameters p = camera.getParameters();
					p.setFlashMode(Parameters.FLASH_MODE_OFF);
					try {
						camera.setParameters(p);
					} catch (RuntimeException e) {
						Log.e(TAG, "failed to set parameters for focus area");
						e.printStackTrace();
					}
				}
				stopVideo(false);
			}
			this.is_video = false;
			// showPhotoVideoToast();
			Activity activity = (Activity) this.getContext();
			View v = activity.findViewById(R.id.switch_video);
			v.setBackgroundResource(R.drawable.camera_video_change_style);
		} else {
			// if( is_taking_photo_on_timer ) {
			if (this.isOnTimer()) {
				takePictureTimerTask.cancel();
				takePictureTimerTask = null;
				if (beepTimerTask != null) {
					beepTimerTask.cancel();
					beepTimerTask = null;
				}
				/*
				 * is_taking_photo_on_timer = false; is_taking_photo = false;
				 */
				this.phase = PHASE_NORMAL;
				this.is_video = true;
			}
			// else if( this.is_taking_photo ) {
			else if (this.phase == PHASE_TAKING_PHOTO) {
				Log.i(TAG, "wait until photo taken");
			} else {
				this.is_video = true;
			}

			if (this.is_video) {
				Activity activity = (Activity) this.getContext();
				View v = activity.findViewById(R.id.switch_video);
				v.setBackgroundResource(R.drawable.camera_audio_change_style);
				// showPhotoVideoToast();
			}
		}
		Log.e("switchVideo", "old_is_video-->" + old_is_video + "  is_video-> " + is_video);
		if (is_video != old_is_video) {
			updateFocusForVideo(true);

			Activity activity = (Activity) this.getContext();
			ImageButton view = (ImageButton) activity.findViewById(R.id.take_photo);
			view.setBackgroundResource(is_video ? R.drawable.camera_rec_take_play : R.drawable.camera_take);
			if (is_video) {
				if (this.txtVideoTime != null) {
					this.txtVideoTime.setVisibility(View.VISIBLE);
				}
//				if (this.img_mark != null) {
//					this.img_mark.setVisibility(View.VISIBLE);
//				}
			} else {
				if (this.txtVideoTime != null) {
					this.txtVideoTime.setVisibility(View.GONE);
				}
//				if (this.img_mark != null) {
//					this.img_mark.setVisibility(View.GONE);
//				}
			}

			if (save) {
				// now save
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean(getIsVideoPreferenceKey(), is_video);
				editor.apply();
			}

			if (update_preview_size) {
				if (this.is_preview_started) {
					camera.stopPreview();
					this.is_preview_started = false;
				}
				//  设置预览尺寸
				setPreviewSize();
				if (!is_video) {
					// if is_video is true, we set the preview fps range in
					// startCameraPreview()
					Camera.Parameters parameters = camera.getParameters();
					// String flashMode = getCurrentFlashMode();
					// parameters.setFlashMode(flashMode);
					parameters.setPreviewFpsRange(current_fps_range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
							current_fps_range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
					camera.setParameters(parameters);
				}
				// if(camera != null){
				// Camera.Parameters parameters = camera.getParameters();
				// String falsh_mode = this.getCurrentFlashMode();
				// if(falsh_mode != null){
				// parameters.setFlashMode(falsh_mode);
				// }
				// camera.setParameters(parameters);
				// }
				// always start the camera preview, even if it was previously
				// paused
				this.startCameraPreview();
			}
		}
	}

	private void updateFocusForVideo(boolean auto_focus) {
		if (this.supported_focus_values != null && camera != null) {
			Camera.Parameters parameters = camera.getParameters();
			String current_focus_mode = parameters.getFocusMode();
			// getFocusMode() is documented as never returning null, however
			// I've had null pointer exceptions reported in Google Play
			boolean focus_is_video = current_focus_mode != null && current_focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			if (focus_is_video != is_video) {
				updateFocus(is_video ? "focus_mode_continuous_video" : "focus_mode_auto", true, true, auto_focus);
			}
		}
	}

	void cycleFlash() {
		// if( is_taking_photo && !is_taking_photo_on_timer ) {
		if (this.phase == PHASE_TAKING_PHOTO) {
			// just to be safe - risk of cancelling the autofocus before taking
			// a photo, or otherwise messing things up
			Log.d(TAG, "currently taking a photo");
			return;
		}
		if (this.supported_flash_values != null && this.supported_flash_values.size() > 1) {
			int new_flash_index = (current_flash_index + 1) % this.supported_flash_values.size();
			updateFlash(new_flash_index);

			// now save
			String flash_value = supported_flash_values.get(current_flash_index);
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(getFlashPreferenceKey(cameraId), flash_value);
			editor.apply();
		}
	}

	private boolean updateFlash(String flash_value) {
		if (supported_flash_values != null) {
			int new_flash_index = supported_flash_values.indexOf(flash_value);
			if (new_flash_index != -1) {
				updateFlash(new_flash_index);
				return true;
			}
		}
		return false;
	}

	private void updateFlash(int new_flash_index) {
		// updates the Flash button, and Flash camera mode
		if (supported_flash_values != null && new_flash_index != current_flash_index) {
			boolean initial = current_flash_index == -1;
			current_flash_index = new_flash_index;

			Activity activity = (Activity) this.getContext();
			ImageButton flashButton = (ImageButton) activity.findViewById(R.id.flash);
			String[] flash_entries = getResources().getStringArray(R.array.flash_entries);
			String[] flash_icons = getResources().getStringArray(R.array.flash_icons);
			String flash_value = supported_flash_values.get(current_flash_index);
			String[] flash_values = getResources().getStringArray(R.array.flash_values);
			for (int i = 0; i < flash_values.length; i++) {
				/*
				 * if( MyDebug.LOG ) Log.d(TAG, "    compare to: " +
				 * flash_values[i]);
				 */
				if (flash_value.equals(flash_values[i])) {
					// flashButton.setText(flash_entries[i]);
					int resource = getResources().getIdentifier(flash_icons[i], null, activity.getApplicationContext().getPackageName());
					flashButton.setBackgroundResource(resource);
					if (!initial) {
						// showToast(flash_toast, flash_entries[i]);
					}
					break;
				}
			}
			this.setFlash(flash_value);
		}
	}

	private void setFlash(String flash_value) {
		set_flash_after_autofocus = ""; // this overrides any previously saved
										// setting, for during the startup
										// autofocus
		cancelAutoFocus();
		Camera.Parameters parameters = camera.getParameters();
		String flash_mode = convertFlashValueToMode(flash_value);
		if (flash_mode.length() > 0 && !flash_mode.equals(parameters.getFlashMode())) {
			parameters.setFlashMode(flash_mode);
			try {
				camera.setParameters(parameters);
			} catch (RuntimeException e) {
				Log.e(TAG, "failed to set parameters for focus area");
				e.printStackTrace();
			}
		}
	}

	// this returns the flash mode indicated by the UI, rather than from the
	// camera parameters (may be different, e.g., in startup autofocus!)
	public String getCurrentFlashMode() {
		if (current_flash_index == -1)
			return null;
		String flash_value = supported_flash_values.get(current_flash_index);
		String flash_mode = convertFlashValueToMode(flash_value);
		return flash_mode;
	}

	private String convertFlashValueToMode(String flash_value) {
		String flash_mode = "";
		if (flash_value.equals("flash_off")) {
			flash_mode = Camera.Parameters.FLASH_MODE_OFF;
		} else if (flash_value.equals("flash_auto")) {
			flash_mode = Camera.Parameters.FLASH_MODE_AUTO;
		} else if (flash_value.equals("flash_on")) {
			flash_mode = Camera.Parameters.FLASH_MODE_ON;
		} else if (flash_value.equals("flash_torch")) {
			flash_mode = Camera.Parameters.FLASH_MODE_TORCH;
		} else if (flash_value.equals("flash_red_eye")) {
			flash_mode = Camera.Parameters.FLASH_MODE_RED_EYE;
		}
		return flash_mode;
	}

	private List<String> convertFlashModesToValues(List<String> supported_flash_modes) {
		List<String> output_modes = new Vector<String>();
		if (supported_flash_modes != null) {
			/*
			 * for(String flash_mode : supported_flash_modes) { if(
			 * flash_mode.equals(Camera.Parameters.FLASH_MODE_OFF) ) {
			 * output_modes.add("flash_off"); } else if(
			 * flash_mode.equals(Camera.Parameters.FLASH_MODE_AUTO) ) {
			 * output_modes.add("flash_auto"); } else if(
			 * flash_mode.equals(Camera.Parameters.FLASH_MODE_ON) ) {
			 * output_modes.add("flash_on"); } else if(
			 * flash_mode.equals(Camera.Parameters.FLASH_MODE_TORCH) ) {
			 * output_modes.add("flash_torch"); } else if(
			 * flash_mode.equals(Camera.Parameters.FLASH_MODE_RED_EYE) ) {
			 * output_modes.add("flash_red_eye"); } }
			 */
			// also resort as well as converting
			// first one will be the default choice
			if (supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
				output_modes.add("flash_auto");
			}
			if (supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
				output_modes.add("flash_off");
			}
			if (supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_ON)) {
				output_modes.add("flash_on");
			}
			// if
			// (supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_TORCH))
			// {
			// output_modes.add("flash_torch");
			// if (MyDebug.LOG)
			// Log.d(TAG, " supports flash_torch");
			// }
			// if
			// (supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_RED_EYE))
			// {
			// output_modes.add("flash_red_eye");
			// if (MyDebug.LOG)
			// Log.d(TAG, " supports flash_red_eye");
			// }
		}
		return output_modes;
	}

	void cycleFocusMode() {
		// if( is_taking_photo && !is_taking_photo_on_timer ) {
		if (this.phase == PHASE_TAKING_PHOTO) {
			// just to be safe - otherwise problem that changing the focus mode
			// will cancel the autofocus before taking a photo, so we never take
			// a photo, but is_taking_photo remains true!
			Log.d(TAG, "currently taking a photo");
			return;
		}
		if (this.supported_focus_values != null && this.supported_focus_values.size() > 1) {
			int new_focus_index = (current_focus_index + 1) % this.supported_focus_values.size();
			updateFocus(new_focus_index, false, true, true);
		}
	}

	private boolean updateFocus(String focus_value, boolean quiet, boolean save, boolean auto_focus) {
		if (this.supported_focus_values != null) {
			int new_focus_index = supported_focus_values.indexOf(focus_value);
			if (new_focus_index != -1) {
				updateFocus(new_focus_index, quiet, save, auto_focus);
				return true;
			}
		}
		return false;
	}

	private void updateFocus(int new_focus_index, boolean quiet, boolean save, boolean auto_focus) {
		// updates the Focus button, and Focus camera mode
		if (this.supported_focus_values != null && new_focus_index != current_focus_index) {
			boolean initial = current_focus_index == -1;
			current_focus_index = new_focus_index;

			// Activity activity = (Activity) this.getContext();
			// ImageButton focusModeButton = (ImageButton)
			// activity.findViewById(R.id.focus_mode);
			// String[] focus_icons =
			// getResources().getStringArray(R.array.focus_mode_icons);
			String focus_value = supported_focus_values.get(current_focus_index);
			String[] focus_values = getResources().getStringArray(R.array.focus_mode_values);
			for (int i = 0; i < focus_values.length; i++) {
				if (focus_value.equals(focus_values[i])) {
					// int resource =
					// getResources().getIdentifier(focus_icons[i], null,
					// activity.getApplicationContext().getPackageName());
					// focusModeButton.setImageResource(resource);
					if (!initial && !quiet) {
						// showToast(focus_toast, focus_entries[i]);
					}
					break;
				}
			}
			this.setFocusValue(focus_value, auto_focus);

			if (save) {
				// now save
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(getFocusPreferenceKey(cameraId), focus_value);
				editor.apply();
			}
		}
	}

	public String getFocusValue() {
		if (camera == null) {
			Log.d(TAG, "null camera");
			return null;
		}
		if (this.supported_focus_values != null && this.current_focus_index != -1)
			return this.supported_focus_values.get(current_focus_index);
		return null;
	}

	private void setFocusValue(String focus_value, boolean auto_focus) {
		if (camera == null) {
			Log.e(TAG, "null camera");
			return;
		}
		cancelAutoFocus();
		Camera.Parameters parameters = camera.getParameters();
		if (focus_value.equals("focus_mode_auto") || focus_value.equals("focus_mode_manual")) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		} else if (focus_value.equals("focus_mode_infinity")) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
		} else if (focus_value.equals("focus_mode_macro")) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
		} else if (focus_value.equals("focus_mode_fixed")) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
		} else if (focus_value.equals("focus_mode_edof")) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_EDOF);
		} else if (focus_value.equals("focus_mode_continuous_video")) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			//优化I9200拍照8s后不再录像的问题
			if(android.os.Build.MODEL != null && android.os.Build.MODEL.equals(GT_I9200_MODEL)){
			    parameters.setRecordingHint(true);
			}
		}
		try {
			camera.setParameters(parameters);
		} catch (RuntimeException e) {
			Log.e(TAG, "failed to set parameters for focus area");
			e.printStackTrace();
		}
		clearFocusAreas();
		// n.b., we reset even for manual focus mode
		if (auto_focus) {
			tryAutoFocus(false, false);
		}
	}

	private List<String> convertFocusModesToValues(List<String> supported_focus_modes) {
		List<String> output_modes = new Vector<String>();
		if (supported_focus_modes != null) {
			// also resort as well as converting
			// first one will be the default choice
			if (supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				output_modes.add("focus_mode_auto");
			}
			if (supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
				output_modes.add("focus_mode_infinity");
			}
			if (supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
				output_modes.add("focus_mode_macro");
			}
			if (supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				output_modes.add("focus_mode_manual");
			}
			if (supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
				output_modes.add("focus_mode_fixed");
			}
			if (supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_EDOF)) {
				output_modes.add("focus_mode_edof");
			}
			if (supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				output_modes.add("focus_mode_continuous_video");
			}
		}
		return output_modes;
	}

	void takePicturePressed() {
		if (camera == null) {
			Log.d(TAG, "camera not available");
			/*
			 * is_taking_photo_on_timer = false; is_taking_photo = false;
			 */
			this.phase = PHASE_NORMAL;
			return;
		}
		if (!this.has_surface) {
			/*
			 * is_taking_photo_on_timer = false; is_taking_photo = false;
			 */
			this.phase = PHASE_NORMAL;
			return;
		}
		// if( is_taking_photo_on_timer ) {
		if (this.isOnTimer()) {
			takePictureTimerTask.cancel();
			takePictureTimerTask = null;
			if (beepTimerTask != null) {
				beepTimerTask.cancel();
				beepTimerTask = null;
			}
			/*
			 * is_taking_photo_on_timer = false; is_taking_photo = false;
			 */
			this.phase = PHASE_NORMAL;
			// showToast(take_photo_toast, R.string.cancelled_timer);
			return;
		}
		// if( is_taking_photo ) {
		if (this.phase == PHASE_TAKING_PHOTO) {
			if (is_video) {
				if (!video_start_time_set || System.currentTimeMillis() - video_start_time < 500) {
					// if user presses to stop too quickly, we ignore
					// firstly to reduce risk of corrupt video files when
					// stopping too quickly (see RuntimeException we have to
					// catch in stopVideo),
					// secondly, to reduce a backlog of events which slows
					// things down, if user presses start/stop repeatedly too
					// quickly
					Log.d(TAG, "ignore pressing stop video too quickly after start");
				} else {
					if (camera != null) {
						Parameters p = camera.getParameters();
						p.setFlashMode(Parameters.FLASH_MODE_OFF);
						try {
							camera.setParameters(p);
						} catch (RuntimeException e) {
							Log.e(TAG, "failed to set parameters for focus area");
							e.printStackTrace();
						}
					}
					stopVideo(false);
					Activity activity = (Activity) this.getContext();
					View v = activity.findViewById(R.id.take_photo);
					v.setBackgroundResource(R.drawable.camera_rec_take_play);
					// View switchView =
					// activity.findViewById(R.id.switch_video);
					// switchView.setVisibility(View.VISIBLE);
				}
			}
			return;
		}

		// make sure that preview running (also needed to hide trash/share
		// icons)
		this.startCameraPreview();

		// is_taking_photo = true;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		String timer_value = sharedPreferences.getString("preference_timer", "0");
		long timer_delay = 0;
		try {
			timer_delay = Integer.parseInt(timer_value) * 1000;
		} catch (NumberFormatException e) {
			Log.e(TAG, "failed to parse preference_timer value: " + timer_value);
			e.printStackTrace();
			timer_delay = 0;
		}

		String burst_mode_value = sharedPreferences.getString("preference_burst_mode", "1");
		try {
			n_burst = Integer.parseInt(burst_mode_value);
		} catch (NumberFormatException e) {
			Log.e(TAG, "failed to parse preference_burst_mode value: " + burst_mode_value);
			e.printStackTrace();
			n_burst = 1;
		}
		remaining_burst_photos = n_burst - 1;

		if (timer_delay == 0) {
			takePicture();
		} else {
			takePictureOnTimer(timer_delay, false);
		}

	}

	public boolean isIs_video() {
		return is_video;
	}

	public int getPhase() {
		return phase;
	}

	private void takePictureOnTimer(long timer_delay, boolean repeated) {
		this.phase = PHASE_TIMER;
		class TakePictureTimerTask extends TimerTask {
			public void run() {
				if (beepTimerTask != null) {
					beepTimerTask.cancel();
					beepTimerTask = null;
				}
				CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
				main_activity.runOnUiThread(new Runnable() {
					public void run() {
						// we run on main thread to avoid problem of camera
						// closing at the same time
						// but still need to check that the camera hasn't closed
						// or the task halted, since TimerTask.run() started
						if (camera != null && takePictureTimerTask != null)
							takePicture();
						else {
							Log.d(TAG, "takePictureTimerTask: don't take picture, as already cancelled");
						}
					}
				});
			}
		}
		take_photo_time = System.currentTimeMillis() + timer_delay;
		if (!repeated) {
			// showToast(take_photo_toast, R.string.started_timer);
		}
		takePictureTimer.schedule(takePictureTimerTask = new TakePictureTimerTask(), timer_delay);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		if (sharedPreferences.getBoolean("preference_timer_beep", true)) {
			class BeepTimerTask extends TimerTask {
				public void run() {
					try {
						Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
						Activity activity = (Activity) getContext();
						Ringtone r = RingtoneManager.getRingtone(activity.getApplicationContext(), notification);
						r.play();
					} catch (Exception e) {
					}
				}
			}
			beepTimer.schedule(beepTimerTask = new BeepTimerTask(), 0, 1000);
		}
	}

	private void flashVideo() {
		Camera.Parameters parameters = camera.getParameters();
		String flash_mode = parameters.getFlashMode();
		// getFlashMode() may return null if flash not supported!
		if (flash_mode == null)
			return;
		String flash_mode_ui = getCurrentFlashMode();
		if (flash_mode_ui == null)
			return;
		if (flash_mode_ui.trim().equals("on")) {
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		}
		try {
			camera.setParameters(parameters);
		} catch (RuntimeException e) {
			Log.e(TAG, "failed to set parameters for focus area");
			e.printStackTrace();
		}

		// if (flash_mode_ui.equals("flash_torch"))
		// return;
		// if (flash_mode.equals(Parameters.FLASH_MODE_TORCH)) {
		// // shouldn't happen? but set to what the UI is
		// cancelAutoFocus();
		// String flash_mode_from_ui = convertFlashValueToMode(flash_mode_ui);
		// if (flash_mode_from_ui.length() > 0) {
		// parameters.setFlashMode(flash_mode_from_ui);
		// camera.setParameters(parameters);
		// }
		// return;
		// }
		// // turn on torch
		// cancelAutoFocus();
		// parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		// camera.setParameters(parameters);
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// // turn off torch
		// cancelAutoFocus();
		// parameters.setFlashMode(flash_mode);

	}

	private void onVideoError(int message_id, int what, int extra, String debug_value) {
		// if (message_id != 0)
		// showToast(null, message_id);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("last_video_error", debug_value);
		editor.apply();
		stopVideo(false);
	}
	
	private void modifyRecorderRoate() {
        // See android.hardware.Camera.Parameters.setRotation for
        // documentation.
        // Note that mOrientation here is the device orientation, which is the opposite of
        // what activity.getWindowManager().getDefaultDisplay().getRotation() would return,
        // which is the orientation the graphics need to rotate in order to render correctly.
        int rotation = 0;
        Activity activity = (Activity) this.getContext();
        int mOrientation = activity.getWindowManager().getDefaultDisplay().getRotation();
        if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
        	CameraInfo info= new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - mOrientation + 360) % 360;
            } else {  // back-facing camera
                rotation = (info.orientation + mOrientation) % 360;
            }
        } else {
        	//Get the right original orientation
        	CameraInfo info= new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, info);// CameraHolder.instance().getCameraInfo()[mCameraId];
            rotation = info.orientation;
        }
//        mMediaRecorder.setOrientationHint(rotation);

		if (cameraId == CameraInfo.CAMERA_FACING_FRONT) {
			if (rotation == 270 || rotation == 90 || rotation == 180) {
				video_recorder.setOrientationHint(180);
			} else {
				video_recorder.setOrientationHint(0);
			}
		} else {
			if (rotation == 180){
				video_recorder.setOrientationHint(180);
			}else{
				video_recorder.setOrientationHint(0);
			}
		}

	}

	/**
	 * @des 抓屏和实实录像
	 */
	private void takePicture() {
		this.thumbnail_anim = false;
		this.phase = PHASE_TAKING_PHOTO;
		if (camera == null) {
			Log.d(TAG, "camera not available");
			/*
			 * is_taking_photo_on_timer = false; is_taking_photo = false;
			 */
			this.phase = PHASE_NORMAL;
			showGUI(true);
			return;
		}
		if (!this.has_surface) {
			/*
			 * is_taking_photo_on_timer = false; is_taking_photo = false;
			 */
			this.phase = PHASE_NORMAL;
			showGUI(true);
			return;
		}
		focus_success = FOCUS_DONE; // clear focus rectangle

		updateParametersFromLocation();

		if (is_video) {
			Activity activity = (Activity) this.getContext();
			View v = activity.findViewById(R.id.take_photo);
			v.setBackgroundResource(R.drawable.camera_rec_take_stop);
			// View switchView = activity.findViewById(R.id.switch_video);
			// switchView.setVisibility(View.GONE);
			CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
			
			File videoFile = main_activity.getOutputMediaFile(CameraActivity.MEDIA_TYPE_VIDEO);
			if (videoFile == null) {
				Log.e(TAG, "Couldn't create media video file; check storage permissions?");
				// showToast(null, R.string.failed_to_save_video);
			} else {
				video_name = videoFile.getAbsolutePath();
				CamcorderProfile profile = getCamcorderProfile();

				this.camera.unlock();
				video_recorder = new MediaRecorder();
				//TODO 
				modifyRecorderRoate();
				video_recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
					@Override
					public void onInfo(MediaRecorder mr, int what, int extra) {
						if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
							int message_id = 0;
							if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
								message_id = R.string.video_max_duration;
							} else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
								message_id = R.string.video_max_filesize;
							}
							final int final_message_id = message_id;
							final int final_what = what;
							final int final_extra = extra;
							CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
							main_activity.runOnUiThread(new Runnable() {
								public void run() {
									// we run on main thread to avoid problem of
									// camera closing at the same time
									String debug_value = "info_" + final_what + "_" + final_extra;
									onVideoError(final_message_id, final_what, final_extra, debug_value);
								}
							});
						}
					}
				});
				video_recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
					public void onError(MediaRecorder mr, int what, int extra) {
						int message_id = R.string.video_error_unknown;
						final int final_message_id = message_id;
						final int final_what = what;
						final int final_extra = extra;
						CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
						main_activity.runOnUiThread(new Runnable() {
							public void run() {
								// we run on main thread to avoid problem of
								// camera closing at the same time
								String debug_value = "error_" + final_what + "_" + final_extra;
								onVideoError(final_message_id, final_what, final_extra, debug_value);
							}
						});
					}
				});
				video_recorder.setCamera(camera);
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
				boolean record_audio = sharedPreferences.getBoolean("preference_record_audio", true);
				if (record_audio) {
					String pref_audio_src = sharedPreferences.getString("preference_record_audio_src", "audio_src_camcorder");
					int audio_source = MediaRecorder.AudioSource.CAMCORDER;
					if (pref_audio_src.equals("audio_src_mic")) {
						audio_source = MediaRecorder.AudioSource.MIC;
					}
					video_recorder.setAudioSource(audio_source);
				}
				video_recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

				boolean store_location = sharedPreferences.getBoolean("preference_location", false);
				// Android camera source claims we need to check lat/long !=
				// 0.0d
				if (store_location && location != null && (location.getLatitude() != 0.0d || location.getLongitude() != 0.0d)) {
					video_recorder.setLocation((float) location.getLatitude(), (float) location.getLongitude());
				}

				if (record_audio) {
					video_recorder.setProfile(profile);
				} else {
					// from
					// http://stackoverflow.com/questions/5524672/is-it-possible-to-use-camcorderprofile-without-audio-source
					video_recorder.setOutputFormat(profile.fileFormat);
					video_recorder.setVideoFrameRate(profile.videoFrameRate);
					video_recorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
					video_recorder.setVideoEncodingBitRate(profile.videoBitRate);
					video_recorder.setVideoEncoder(profile.videoCodec);

				}

				video_recorder.setOrientationHint(getImageVideoRotation());
				video_recorder.setOutputFile(video_name);
				try {
					showGUI(false);
					if (sharedPreferences.getBoolean("preference_lock_video", false)) {
						main_activity.lockScreen();
					}
					/*
					 * if( true ) // test throw new IOException();
					 */
					video_recorder.setPreviewDisplay(mHolder.getSurface());
					video_recorder.prepare();
					video_recorder.start();
					video_start_time = System.currentTimeMillis();
					video_start_time_set = true;
					// showToast(stopstart_video_toast,
					// R.string.started_recording_video);
					// don't send intent for ACTION_MEDIA_SCANNER_SCAN_FILE yet
					// - wait until finished, so we get completed file

					// handle restart timer
					String timer_value = sharedPreferences.getString("preference_video_max_duration", "0");
					long timer_delay = 0;
					try {
						timer_delay = Integer.parseInt(timer_value) * 1000;
					} catch (NumberFormatException e) {
						Log.e(TAG, "failed to parse preference_video_max_duration value: " + timer_value);
						e.printStackTrace();
						timer_delay = 0;
					}

					if (timer_delay > 0) {
						if (remaining_restart_video == 0) {
							String restart_value = sharedPreferences.getString("preference_video_restart", "0");
							try {
								remaining_restart_video = Integer.parseInt(restart_value);
							} catch (NumberFormatException e) {
								Log.e(TAG, "failed to parse preference_video_restart value: " + restart_value);
								e.printStackTrace();
								remaining_restart_video = 0;
							}
						}
						class RestartVideoTimerTask extends TimerTask {
							public void run() {
								CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
								main_activity.runOnUiThread(new Runnable() {
									public void run() {
										// we run on main thread to avoid
										// problem of camera closing at the same
										// time
										// but still need to check that the
										// camera hasn't closed or the task
										// halted, since TimerTask.run() started
										if (camera != null && restartVideoTimerTask != null)
											restartVideo();
										else {
											Log.d(TAG, "restartVideoTimerTask: don't restart video, as already cancelled");
										}
									}
								});
							}
						}
						restartVideoTimer.schedule(restartVideoTimerTask = new RestartVideoTimerTask(), timer_delay);
					}
					// sharedPreferences.getBoolean("preference_video_flash",
					// false) &&
					if (supportsFlash()) {
						if (camera != null)
							flashVideo();

						// class FlashVideoTimerTask extends TimerTask {
						// public void run() {
						// if (MyDebug.LOG)
						// Log.e(TAG, "FlashVideoTimerTask");
						// CameraActivity main_activity = (CameraActivity)
						// Preview.this.getContext();
						// main_activity.runOnUiThread(new Runnable() {
						// public void run() { // we run on main thread
						// // to avoid //
						// // problem of camera
						// // closing at the same
						// // //
						// // time // but
						// // still need to check
						// // that the // camera
						// // hasn't closed
						// // or the task //
						// // halted, since
						// // TimerTask.run()
						// // started
						// if (camera != null && flashVideoTimerTask != null)
						// flashVideo();
						// else {
						// if (MyDebug.LOG)
						// Log.d(TAG,
						// "flashVideoTimerTask: don't flash video, as already cancelled");
						// }
						// }
						// });
						// }
						// }
						// flashVideoTimer.schedule(flashVideoTimerTask = new
						// FlashVideoTimerTask(), 0, 1000);

					}
				} catch (IOException e) {
					Log.e(TAG, "failed to save video");
					e.printStackTrace();
					// showToast(null, R.string.failed_to_save_video);
					video_recorder.reset();
					video_recorder.release();
					video_recorder = null;
					/*
					 * is_taking_photo = false; is_taking_photo_on_timer =
					 * false;
					 */
					this.phase = PHASE_NORMAL;
					showGUI(true);
					this.reconnectCamera(true);
				} catch (RuntimeException e) {
					// needed for emulator at least - although MediaRecorder not
					// meant to work with emulator, it's good to fail gracefully
					Log.e(TAG, "runtime exception starting video recorder");
					e.printStackTrace();
					video_recorder.reset();
					video_recorder.release();
					video_recorder = null;
					/*
					 * is_taking_photo = false; is_taking_photo_on_timer =
					 * false;
					 */
					this.phase = PHASE_NORMAL;
					showGUI(true);
					this.reconnectCamera(true);
				}
			}
			return;
		}

		showGUI(false);
		/*
		 * Camera.Parameters parameters = camera.getParameters(); String
		 * focus_mode = parameters.getFocusMode(); if( MyDebug.LOG ) Log.d(TAG,
		 * "focus_mode is " + focus_mode);
		 */
		String focus_value = current_focus_index != -1 ? supported_focus_values.get(current_focus_index) : null;
		if (this.successfully_focused && System.currentTimeMillis() < this.successfully_focused_time + 5000) {
			takePictureWhenFocused();
		}
		// else if( focus_mode.equals(Camera.Parameters.FOCUS_MODE_AUTO) ||
		// focus_mode.equals(Camera.Parameters.FOCUS_MODE_MACRO) ) {
		else if (focus_value != null && (focus_value.equals("focus_mode_auto") || focus_value.equals("focus_mode_macro"))) {
			Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					takePictureWhenFocused();
				}
			};

			try {
				camera.autoFocus(autoFocusCallback);
				count_cameraAutoFocus++;
			} catch (RuntimeException e) {
				// just in case? We got a RuntimeException report here from 1
				// user on Google Play:
				// 21 Dec 2013, Xperia Go, Android 4.1
				autoFocusCallback.onAutoFocus(false, camera);
				Log.e(TAG, "runtime exception from autoFocus when trying to take photo");
				e.printStackTrace();
			}
		} else {
			takePictureWhenFocused();
		}
	}

	private void takePictureWhenFocused() {
		// should be called when auto-focused
		if (camera == null) {
			Log.d(TAG, "camera not available");
			/*
			 * is_taking_photo_on_timer = false; is_taking_photo = false;
			 */
			this.phase = PHASE_NORMAL;
			showGUI(true);
			return;
		}
		if (!this.has_surface) {
			Log.d(TAG, "preview surface not yet available");
			/*
			 * is_taking_photo_on_timer = false; is_taking_photo = false;
			 */
			this.phase = PHASE_NORMAL;
			showGUI(true);
			return;
		}
		successfully_focused = false; // so next photo taken will require an
										// autofocus

		Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
			// don't do anything here, but we need to implement the callback to
			// get the shutter sound (at least on Galaxy Nexus and Nexus 7)
			public void onShutter() {
				Log.d(TAG, "shutterCallback.onShutter()");
			}
		};

		Camera.PictureCallback jpegPictureCallback = new Camera.PictureCallback() {
			public void onPictureTaken(byte[] data, Camera cam) {
				// n.b., this is automatically run in a different thread
				System.gc();

				CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
				boolean image_capture_intent = false;
				Uri image_capture_intent_uri = null;
				String action = main_activity.getIntent().getAction();
				if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)) {
					image_capture_intent = true;
					Bundle myExtras = main_activity.getIntent().getExtras();
					if (myExtras != null) {
						image_capture_intent_uri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
					}
				}

				boolean success = false;
				Bitmap bitmap = null;
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Preview.this.getContext());
				boolean auto_stabilise = sharedPreferences.getBoolean("preference_auto_stabilise", false);
				if (auto_stabilise && has_level_angle && main_activity.supportsAutoStabilise()) {
					// level_angle = -129;
					if (test_have_angle)
						level_angle = test_angle;
					while (level_angle < -90)
						level_angle += 180;
					while (level_angle > 90)
						level_angle -= 180;
					BitmapFactory.Options options = new BitmapFactory.Options();
					// options.inMutable = true;
					options.inPurgeable = true;
					bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();
					/*
					 * for(int y=0;y<height;y++) { for(int x=0;x<width;x++) {
					 * int col = bitmap.getPixel(x, y); col = col & 0xffff0000;
					 * // mask out red component bitmap.setPixel(x, y, col); } }
					 */
					if (test_low_memory) {
						level_angle = 45.0;
					}
					Matrix matrix = new Matrix();
					double level_angle_rad_abs = Math.abs(Math.toRadians(level_angle));
					int w1 = width, h1 = height;
					double w0 = (w1 * Math.cos(level_angle_rad_abs) + h1 * Math.sin(level_angle_rad_abs));
					double h0 = (w1 * Math.sin(level_angle_rad_abs) + h1 * Math.cos(level_angle_rad_abs));
					// apply a scale so that the overall image size isn't
					// increased
					float orig_size = w1 * h1;
					float rotated_size = (float) (w0 * h0);
					float scale = (float) Math.sqrt(orig_size / rotated_size);
					if (test_low_memory) {
						scale *= 2.0f; // test 20MP
						// scale *= 1.613f; // test 13MP
					}
					matrix.postScale(scale, scale);
					w0 *= scale;
					h0 *= scale;
					w1 *= scale;
					h1 *= scale;
					Camera.CameraInfo info = new Camera.CameraInfo();
					Camera.getCameraInfo(cameraId, info);
					if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
						matrix.postRotate((float) -level_angle);
					} else {
						matrix.postRotate((float) level_angle);
					}
					Bitmap new_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
					// careful, as new_bitmap is sometimes not a copy!
					if (new_bitmap != bitmap) {
						bitmap.recycle();
						bitmap = new_bitmap;
					}
					System.gc();
					double tan_theta = Math.tan(level_angle_rad_abs);
					double sin_theta = Math.sin(level_angle_rad_abs);
					double denom = (double) (h0 / w0 + tan_theta);
					double alt_denom = (double) (w0 / h0 + tan_theta);
					if (denom == 0.0 || denom < 1.0e-14) {
						Log.d(TAG, "zero denominator?!");
					} else if (alt_denom == 0.0 || alt_denom < 1.0e-14) {
						Log.d(TAG, "zero alt denominator?!");
					} else {
						int w2 = (int) ((h0 + 2.0 * h1 * sin_theta * tan_theta - w0 * tan_theta) / denom);
						int h2 = (int) (w2 * h0 / (double) w0);
						int alt_h2 = (int) ((w0 + 2.0 * w1 * sin_theta * tan_theta - h0 * tan_theta) / alt_denom);
						int alt_w2 = (int) (alt_h2 * w0 / (double) h0);
						if (alt_w2 < w2) {
							w2 = alt_w2;
							h2 = alt_h2;
						}
						if (w2 <= 0)
							w2 = 1;
						else if (w2 >= bitmap.getWidth())
							w2 = bitmap.getWidth() - 1;
						if (h2 <= 0)
							h2 = 1;
						else if (h2 >= bitmap.getHeight())
							h2 = bitmap.getHeight() - 1;
						int x0 = (bitmap.getWidth() - w2) / 2;
						int y0 = (bitmap.getHeight() - h2) / 2;
						new_bitmap = Bitmap.createBitmap(bitmap, x0, y0, w2, h2);
						if (new_bitmap != bitmap) {
							bitmap.recycle();
							bitmap = new_bitmap;
						}
						System.gc();
					}
				}

				String exif_orientation_s = null;
				String picFileName = null;
				File picFile = null;
				try {
					OutputStream outputStream = null;
					if (image_capture_intent) {
						if (image_capture_intent_uri != null) {
							// Save the bitmap to the specified URI (use a
							// try/catch block)
							outputStream = main_activity.getContentResolver().openOutputStream(image_capture_intent_uri);
						} else {
							// If the intent doesn't contain an URI, send the
							// bitmap as a parcel
							// (it is a good idea to reduce its size to ~50k
							// pixels before)
							if (bitmap == null) {
								BitmapFactory.Options options = new BitmapFactory.Options();
								// options.inMutable = true;
								options.inPurgeable = true;
								bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
								int width = bitmap.getWidth();
								int height = bitmap.getHeight();
								final int small_size_c = 128;
								if (width > small_size_c) {
									float scale = ((float) small_size_c) / (float) width;
									Matrix matrix = new Matrix();
									matrix.postScale(scale, scale);
									Bitmap new_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
									// careful, as new_bitmap is sometimes not a
									// copy!
									if (new_bitmap != bitmap) {
										bitmap.recycle();
										bitmap = new_bitmap;
									}
								}
							}
							main_activity.setResult(Activity.RESULT_OK, new Intent("inline-data").putExtra("data", bitmap));
							main_activity.finish();
						}
					} else {
						picFile = main_activity.getOutputMediaFile(CameraActivity.MEDIA_TYPE_IMAGE);
						if (picFile == null) {
							Log.e(TAG, "Couldn't create media image file; check storage permissions?");
							// showToast(null, R.string.failed_to_save_image);
						} else {
							picFileName = picFile.getAbsolutePath();
							outputStream = new FileOutputStream(picFile);
						}
					}

					if (outputStream != null) {
						if (bitmap == null && data != null) {
							BitmapFactory.Options options = new BitmapFactory.Options();
							// options.inMutable = true;
							options.inPurgeable = true;
							bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
						} 
						
						if (bitmap != null) {
							int options = 50;
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							bitmap.compress(Bitmap.CompressFormat.JPEG, options, os);    
					        if ( os.toByteArray().length / 1024 > 1024) {  
					            os.reset();  
					            options = 50;
					            bitmap.compress(Bitmap.CompressFormat.JPEG, options, os);  
					        }
					        outputStream.write(os.toByteArray());    
					        outputStream.flush(); 
					        os.close();
						}else{
							outputStream.write(data);
						}
				        
						outputStream.close();
						success = true;
						if (picFile != null) {
							if (bitmap != null) {
								// need to update EXIF data!
								File tempFile = File.createTempFile("opencamera_exif", "");
								OutputStream tempOutputStream = new FileOutputStream(tempFile);
								tempOutputStream.write(data);
								tempOutputStream.close();
								ExifInterface exif = new ExifInterface(tempFile.getAbsolutePath());
								String exif_aperture = exif.getAttribute(ExifInterface.TAG_APERTURE);
								String exif_datetime = exif.getAttribute(ExifInterface.TAG_DATETIME);
								String exif_exposure_time = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
								String exif_flash = exif.getAttribute(ExifInterface.TAG_FLASH);
								String exif_focal_length = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
								String exif_gps_altitude = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
								String exif_gps_altitude_ref = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
								String exif_gps_datestamp = exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
								String exif_gps_latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
								String exif_gps_latitude_ref = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
								String exif_gps_longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
								String exif_gps_longitude_ref = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
								String exif_gps_processing_method = exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
								String exif_gps_timestamp = exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
								// leave width/height, as this will have
								// changed!
								String exif_iso = exif.getAttribute(ExifInterface.TAG_ISO);
								String exif_make = exif.getAttribute(ExifInterface.TAG_MAKE);
								String exif_model = exif.getAttribute(ExifInterface.TAG_MODEL);
								String exif_orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
								exif_orientation_s = exif_orientation;
								String exif_white_balance = exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE);

								if (!tempFile.delete()) {
									Log.e(TAG, "failed to delete temp " + tempFile.getAbsolutePath());
								}
								ExifInterface exif_new = new ExifInterface(picFile.getAbsolutePath());
								if (exif_aperture != null)
									exif_new.setAttribute(ExifInterface.TAG_APERTURE, exif_aperture);
								if (exif_datetime != null)
									exif_new.setAttribute(ExifInterface.TAG_DATETIME, exif_datetime);
								if (exif_exposure_time != null)
									exif_new.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, exif_exposure_time);
								if (exif_flash != null)
									exif_new.setAttribute(ExifInterface.TAG_FLASH, exif_flash);
								if (exif_focal_length != null)
									exif_new.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, exif_focal_length);
								if (exif_gps_altitude != null)
									exif_new.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, exif_gps_altitude);
								if (exif_gps_altitude_ref != null)
									exif_new.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, exif_gps_altitude_ref);
								if (exif_gps_datestamp != null)
									exif_new.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, exif_gps_datestamp);
								if (exif_gps_latitude != null)
									exif_new.setAttribute(ExifInterface.TAG_GPS_LATITUDE, exif_gps_latitude);
								if (exif_gps_latitude_ref != null)
									exif_new.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, exif_gps_latitude_ref);
								if (exif_gps_longitude != null)
									exif_new.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, exif_gps_longitude);
								if (exif_gps_longitude_ref != null)
									exif_new.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, exif_gps_longitude_ref);
								if (exif_gps_processing_method != null)
									exif_new.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, exif_gps_processing_method);
								if (exif_gps_timestamp != null)
									exif_new.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, exif_gps_timestamp);
								// leave width/height, as this will have
								// changed!
								if (exif_iso != null)
									exif_new.setAttribute(ExifInterface.TAG_ISO, exif_iso);
								if (exif_make != null)
									exif_new.setAttribute(ExifInterface.TAG_MAKE, exif_make);
								if (exif_model != null)
									exif_new.setAttribute(ExifInterface.TAG_MODEL, exif_model);
								if (exif_orientation != null)
									exif_new.setAttribute(ExifInterface.TAG_ORIENTATION, exif_orientation);
								if (exif_white_balance != null)
									exif_new.setAttribute(ExifInterface.TAG_WHITE_BALANCE, exif_white_balance);
								setGPSDirectionExif(exif_new);
								exif_new.saveAttributes();
							} else if (Preview.this.has_geo_direction && sharedPreferences.getBoolean("preference_location", false)) {
								ExifInterface exif = new ExifInterface(picFile.getAbsolutePath());
								setGPSDirectionExif(exif);
								exif.saveAttributes();
							}
//						            发送捕获图片广播
							main_activity.broadcastFile(picFile, true, false);
							test_last_saved_image = picFileName;
						}
						if (image_capture_intent) {
							main_activity.setResult(Activity.RESULT_OK);
							main_activity.finish();
						}
					}
				} catch (FileNotFoundException e) {
					Log.e(TAG, "File not found: " + e.getMessage());
					e.getStackTrace();
					// showToast(null, R.string.failed_to_save_photo);
				} catch (IOException e) {
					Log.e(TAG, "I/O error writing file: " + e.getMessage());
					e.getStackTrace();
					// showToast(null, R.string.failed_to_save_photo);
				}

				is_preview_started = false; // preview automatically stopped due
											// to taking photo
				phase = PHASE_NORMAL; // need to set this even if remaining
										// burst photos, so we can restart the
										// preview
				if (remaining_burst_photos > 0) {
					// we need to restart the preview; and we do this in the
					// callback, as we need to restart after saving the image
					// (otherwise this can fail, at least on Nexus 7)
					startCameraPreview();
				} else {
					phase = PHASE_NORMAL;
					boolean pause_preview = sharedPreferences.getBoolean("preference_pause_preview", false);
					if (pause_preview && success) {
						setPreviewPaused(true);
						preview_image_name = picFileName;
					} else {
						// we need to restart the preview; and we do this in the
						// callback, as we need to restart after saving the
						// image
						// (otherwise this can fail, at least on Nexus 7)
						startCameraPreview();
						showGUI(true);
					}
				}

				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}

				if (success && picFile != null) {
					// update thumbnail - this should be done after restarting
					// preview, so that the preview is started asap
					long time_s = System.currentTimeMillis();
					Camera.Parameters parameters = cam.getParameters();
					Camera.Size size = parameters.getPictureSize();
					int ratio = (int) Math.ceil((double) size.width / Preview.this.getWidth());
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inMutable = false;
					options.inPurgeable = true;
					options.inSampleSize = Integer.highestOneBit(ratio) * 4;
					if (!sharedPreferences.getBoolean("preference_thumbnail_animation", true)) {
						// can use lower resolution if we don't have the
						// thumbnail animation
						options.inSampleSize *= 4;
					}
					Bitmap old_thumbnail = thumbnail;
					thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length, options);
					int thumbnail_rotation = 0;
					// now get the rotation from the Exif data
					try {
						if (exif_orientation_s == null) {
							// haven't already read the exif orientation
							ExifInterface exif = new ExifInterface(picFile.getAbsolutePath());
							exif_orientation_s = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
						}
						int exif_orientation = 0;
						// from http://jpegclub.org/exif_orientation.html
						if (exif_orientation_s.equals("0") || exif_orientation_s.equals("1")) {
							// leave at 0
						} else if (exif_orientation_s.equals("3")) {
							exif_orientation = 180;
						} else if (exif_orientation_s.equals("6")) {
							exif_orientation = 90;
						} else if (exif_orientation_s.equals("8")) {
							exif_orientation = 270;
						} else {
							Log.e(TAG, "    unsupported exif orientation: " + exif_orientation_s);
						}
						thumbnail_rotation = (thumbnail_rotation + exif_orientation) % 360;
					} catch (IOException exception) {
						Log.e(TAG, "exif orientation ioexception");
						exception.printStackTrace();
					}

					if (thumbnail_rotation != 0) {
						Matrix m = new Matrix();
						m.setRotate(thumbnail_rotation, thumbnail.getWidth() * 0.5f, thumbnail.getHeight() * 0.5f);
						Bitmap rotated_thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), m, true);
						if (rotated_thumbnail != thumbnail) {
							thumbnail.recycle();
							thumbnail = rotated_thumbnail;
						}
					}

					if (sharedPreferences.getBoolean("preference_thumbnail_animation", true)) {
						thumbnail_anim = true;
						thumbnail_anim_start_ms = System.currentTimeMillis();
					}
					main_activity.updateGalleryIconToBitmap(thumbnail);
					if (old_thumbnail != null) {
						// only recycle after we've set the new thumbnail
						old_thumbnail.recycle();
					}
				}

				System.gc();

				if (remaining_burst_photos > 0) {
					remaining_burst_photos--;

					String timer_value = sharedPreferences.getString("preference_burst_interval", "0");
					long timer_delay = 0;
					try {
						timer_delay = Integer.parseInt(timer_value) * 1000;
					} catch (NumberFormatException e) {
						Log.e(TAG, "failed to parse preference_burst_interval value: " + timer_value);
						e.printStackTrace();
						timer_delay = 0;
					}

					if (timer_delay == 0) {
						// we go straight to taking a photo rather than
						// refocusing, for speed
						// need to manually set the phase and rehide the GUI
						phase = PHASE_TAKING_PHOTO;
						showGUI(false);
						takePictureWhenFocused();
					} else {
						takePictureOnTimer(timer_delay, true);
					}
				}
			}
		};
		{
			Camera.Parameters parameters = camera.getParameters();
			parameters.setRotation(getImageVideoRotation());
			// parameters.set("scene-mode", "hdr");
		
			try {
				camera.setParameters(parameters);
			} catch (RuntimeException e) {
				Log.e(TAG, "failed to set parameters for focus area");
				e.printStackTrace();
			}
			String toast_text = "";
			if (n_burst > 1) {
				int photo = (n_burst - remaining_burst_photos);
				toast_text = getResources().getString(R.string.taking_photo) + "... (" + photo + " / " + n_burst + ")";
			} else {
				toast_text = getResources().getString(R.string.taking_photo) + "...";
			}
			try {
				camera.takePicture(shutterCallback, null, jpegPictureCallback);
				count_cameraTakePicture++;
				// showToast(take_photo_toast, toast_text);
			} catch (RuntimeException e) {
				Log.e(TAG, "runtime exception from takePicture");
				e.printStackTrace();
				// showToast(null, R.string.failed_to_take_picture);
			}
		}
	}

	private void setGPSDirectionExif(ExifInterface exif) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		if (this.has_geo_direction && sharedPreferences.getBoolean("preference_location", false)) {
			float geo_angle = (float) Math.toDegrees(Preview.this.geo_direction[0]);
			if (geo_angle < 0.0f) {
				geo_angle += 360.0f;
			}
			// see
			// http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/GPS.html
			String GPSImgDirection_string = Math.round(geo_angle * 100) + "/100";
			exif.setAttribute(TAG_GPS_IMG_DIRECTION, GPSImgDirection_string);
			exif.setAttribute(TAG_GPS_IMG_DIRECTION_REF, "M");
		}
	}

	void clickedShare() {
		// if( is_preview_paused ) {
		if (this.phase == PHASE_PREVIEW_PAUSED) {
			if (preview_image_name != null) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/jpeg");
				intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + preview_image_name));
				Activity activity = (Activity) this.getContext();
				activity.startActivity(Intent.createChooser(intent, "Photo"));
			}
			startCameraPreview();
			tryAutoFocus(false, false);
		}
	}

	void clickedTrash() {
		// if( is_preview_paused ) {
		if (this.phase == PHASE_PREVIEW_PAUSED) {
			if (preview_image_name != null) {
				File file = new File(preview_image_name);
				if (!file.delete()) {
					Log.e(TAG, "failed to delete " + preview_image_name);
				} else {
					// showToast(null, R.string.photo_deleted);
					CameraActivity main_activity = (CameraActivity) this.getContext();
					main_activity.broadcastFile(file, false, false);
				}
			}
			startCameraPreview();
			tryAutoFocus(false, false);
		}
	}

	void requestAutoFocus() {
		cancelAutoFocus();
		tryAutoFocus(false, true);
	}

	private void tryAutoFocus(final boolean startup, final boolean manual) {
		if (camera == null) {
			Log.d(TAG, "no camera");
		} else if (!this.has_surface) {
			Log.d(TAG, "preview surface not yet available");
		} else if (!this.is_preview_started) {
			Log.d(TAG, "preview not yet started");
		}
		// else if( is_taking_photo ) {
		else if (!(manual && this.is_video) && this.isTakingPhotoOrOnTimer()) {
			Log.d(TAG, "currently taking a photo");
		} else {
			// it's only worth doing autofocus when autofocus has an effect
			// (i.e., auto or macro mode)
			Camera.Parameters parameters = camera.getParameters();
			String focus_mode = parameters.getFocusMode();
			// getFocusMode() is documented as never returning null, however
			// I've had null pointer exceptions reported in Google Play from the
			// below line (v1.7),
			// on Galaxy Tab 10.1 (GT-P7500), Android 4.0.3 - 4.0.4; HTC EVO 3D
			// X515m (shooteru), Android 4.0.3 - 4.0.4
			if (focus_mode != null && (focus_mode.equals(Camera.Parameters.FOCUS_MODE_AUTO) || focus_mode.equals(Camera.Parameters.FOCUS_MODE_MACRO))) {
				String old_flash = parameters.getFlashMode();
				set_flash_after_autofocus = "";
				// getFlashMode() may return null if flash not supported!
				if (startup && old_flash != null && old_flash != Camera.Parameters.FLASH_MODE_OFF) {
					set_flash_after_autofocus = old_flash;
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					try {
						camera.setParameters(parameters);
					} catch (RuntimeException e) {
						Log.e(TAG, "failed to set parameters for focus area");
						e.printStackTrace();
					}
				}
				Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						autoFocusCompleted(manual, success, false);
					}
				};

				this.focus_success = FOCUS_WAITING;
				this.focus_complete_time = -1;
				this.successfully_focused = false;
				try {
					camera.autoFocus(autoFocusCallback);
					count_cameraAutoFocus++;
				} catch (RuntimeException e) {
					// just in case? We got a RuntimeException report here from
					// 1 user on Google Play
					autoFocusCallback.onAutoFocus(false, camera);
					e.printStackTrace();
				}
			} else if (has_focus_area) {
				// do this so we get the focus box, for focus modes that support
				// focus area, but don't support autofocus
				focus_success = FOCUS_SUCCESS;
				focus_complete_time = System.currentTimeMillis();
			}
		}
	}

	private void cancelAutoFocus() {
		if (camera != null) {
			try {
				camera.cancelAutoFocus();
			} catch (RuntimeException e) {
				Log.e(TAG, "camera.cancelAutoFocus() failed");
				e.printStackTrace();
			}
			autoFocusCompleted(false, false, true);
		}
	}

	private void autoFocusCompleted(boolean manual, boolean success, boolean cancelled) {
		if (cancelled) {
			focus_success = FOCUS_DONE;
		} else {
			focus_success = success ? FOCUS_SUCCESS : FOCUS_FAILED;
			focus_complete_time = System.currentTimeMillis();
		}
		CameraActivity main_activity = (CameraActivity) Preview.this.getContext();
		if (manual && !cancelled && (success || main_activity.is_test)) {
			successfully_focused = true;
			successfully_focused_time = focus_complete_time;
		}
		//
		if (set_flash_after_autofocus.length() > 0 && camera != null) {
			Camera.Parameters parameters = camera.getParameters();
			parameters.setFlashMode(set_flash_after_autofocus);
			set_flash_after_autofocus = "";
			
			try {
				camera.setParameters(parameters);
			} catch (RuntimeException e) {
				Log.e(TAG, "failed to set parameters for focus area");
				e.printStackTrace();
			}
		}
		if (this.using_face_detection) {
			// On some devices such as mtk6589, face detection dooes not resume
			// as written in documentation so we have
			// to cancelfocus when focus is finished
			if (camera != null) {
				try {
					// Camera.Parameters parameters = camera.getParameters();
					// parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					// camera.setParameters(parameters);
					camera.cancelAutoFocus();
				} catch (RuntimeException e) {
					Log.e(TAG, "camera.cancelAutoFocus() failed");
					e.printStackTrace();
				}
			}
		}
	}

	public void startCameraPreview() {
		// if( camera != null && !is_taking_photo && !is_preview_started ) {
		if (camera != null && !this.isTakingPhotoOrOnTimer() && !is_preview_started) {
			Camera.Parameters parameters = camera.getParameters();
			// Calling setParameters here with continuous video focus mode
			// causes preview to not restart after taking a photo on Galaxy
			// Nexus?! (fine on my Nexus 7).
			// The issue seems to specifically be with setParameters (i.e.,
			// the problem occurs even if we don't setRecordingHint).
			// In addition, I had a report of a bug on HTC Desire X, Android
			// 4.0.4 where the saved video was corrupted.
			// This worked fine in 1.7, then not in 1.8 and 1.9, then was
			// fixed again in 1.10
			// The only thing in common to 1.7->1.8 and 1.9-1.10, that seems
			// relevant, was adding this code to setRecordingHint() and
			// setParameters() (unclear which would have been the problem),
			// so we should be very careful about enabling this code again!
			String focus_mode = parameters.getFocusMode();
			// getFocusMode() is documented as never returning null, however
			// I've had null pointer exceptions reported in Google Play
			if (focus_mode != null && !focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				parameters.setRecordingHint(this.is_video);
				try {
					camera.setParameters(parameters);
				} catch (RuntimeException e) {
					Log.e(TAG, "failed to set parameters for focus area");
					e.printStackTrace();
				}
			}

			if (this.is_video) {
				matchPreviewFpsToVideo();
			}
			// else, we reset the preview fps to default in switchVideo
			count_cameraStartPreview++;
			camera.startPreview();
			this.is_preview_started = true;
			if (this.using_face_detection) {
				try {
					camera.startFaceDetection();
				} catch (RuntimeException e) {
					Log.e(TAG, "face detection already started");
				}
				faces_detected = null;
			}
		}
		this.setPreviewPaused(false);
	}

	private void setPreviewPaused(boolean paused) {
		// Activity activity = (Activity) this.getContext();
		// View shareButton = (View) activity.findViewById(R.id.share);
		// View trashButton = (View) activity.findViewById(R.id.trash);
		/*
		 * is_preview_paused = paused; if( is_preview_paused ) {
		 */
		if (paused) {
			this.phase = PHASE_PREVIEW_PAUSED;
			// shareButton.setVisibility(View.VISIBLE);
			// trashButton.setVisibility(View.VISIBLE);
			// shouldn't call showGUI(false), as should already have been
			// disabled when we started to take a photo
		} else {
			this.phase = PHASE_NORMAL;
			// shareButton.setVisibility(View.GONE);
			// trashButton.setVisibility(View.GONE);
			preview_image_name = null;
			showGUI(true);
		}
	}

	/**
	 * @describtion
	 * @param show
	 */
	private void showGUI(final boolean show) {
		final Activity activity = (Activity) this.getContext();
		activity.runOnUiThread(new Runnable() {
			public void run() {
				final int visibility = show ? View.VISIBLE : View.GONE;
				View switchCameraButton = (View) activity.findViewById(R.id.switch_camera);
				// View switchVideoButton = (View)
				// activity.findViewById(R.id.switch_video);
				View flashButton = (View) activity.findViewById(R.id.flash);
				//View back = activity.findViewById(R.id.ibtn_back);
				// View focusButton = (View)
				// activity.findViewById(R.id.focus_mode);

				// if (!is_video)
				// switchVideoButton.setVisibility(visibility); // still allow
				// switch
				// video
				// when
				// recording
				// video

				// back
				// if (supported_focus_values != null)
				// focusButton.setVisibility(visibility);
				if (is_video) {
					if (supported_flash_values != null) {
						flashButton.setVisibility(visibility);
						// switchVideoButton.setVisibility(visibility);
					}
					//back.setVisibility(visibility);
					if (Camera.getNumberOfCameras() > 1) {
						switchCameraButton.setVisibility(visibility);

					}

				}
			}
		});
	}

	void onAccelerometerSensorChanged(SensorEvent event) {
		/*
		 * if( MyDebug.LOG ) Log.d(TAG, "onAccelerometerSensorChanged: " +
		 * event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
		 */

		this.has_gravity = true;
		for (int i = 0; i < 3; i++) {
			// this.gravity[i] = event.values[i];
			this.gravity[i] = sensor_alpha * this.gravity[i] + (1.0f - sensor_alpha) * event.values[i];
		}
		calculateGeoDirection();

		double x = gravity[0];
		double y = gravity[1];
		this.has_level_angle = true;
		this.level_angle = Math.atan2(-x, y) * 180.0 / Math.PI;
		if (this.level_angle < -0.0) {
			this.level_angle += 360.0;
		}
		this.orig_level_angle = this.level_angle;
		this.level_angle -= (float) this.current_orientation;
		if (this.level_angle < -180.0) {
			this.level_angle += 360.0;
		} else if (this.level_angle > 180.0) {
			this.level_angle -= 360.0;
		}

		this.invalidate();
	}

	void onMagneticSensorChanged(SensorEvent event) {
		this.has_geomagnetic = true;
		for (int i = 0; i < 3; i++) {
			// this.geomagnetic[i] = event.values[i];
			this.geomagnetic[i] = sensor_alpha * this.geomagnetic[i] + (1.0f - sensor_alpha) * event.values[i];
		}
		calculateGeoDirection();
	}

	private void calculateGeoDirection() {
		if (!this.has_gravity || !this.has_geomagnetic) {
			return;
		}
		if (!SensorManager.getRotationMatrix(this.deviceRotation, this.deviceInclination, this.gravity, this.geomagnetic)) {
			return;
		}
		SensorManager.remapCoordinateSystem(this.deviceRotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, this.cameraRotation);
		this.has_geo_direction = true;
		SensorManager.getOrientation(cameraRotation, geo_direction);
		// SensorManager.getOrientation(deviceRotation, geo_direction);
		/*
		 * if( MyDebug.LOG ) { Log.d(TAG, "geo_direction: " +
		 * (geo_direction[0]*180/Math.PI) + ", " +
		 * (geo_direction[1]*180/Math.PI) + ", " +
		 * (geo_direction[2]*180/Math.PI)); }
		 */
	}

	public boolean supportsFaceDetection() {
		return supports_face_detection;
	}

	List<String> getSupportedColorEffects() {
		return this.color_effects;
	}

	List<String> getSupportedSceneModes() {
		return this.scene_modes;
	}

	List<String> getSupportedWhiteBalances() {
		return this.white_balances;
	}

	String getISOKey() {
		return this.iso_key;
	}

	List<String> getSupportedISOs() {
		return this.isos;
	}

	public boolean supportsExposures() {
		return this.exposures != null;
	}

	int getMinimumExposure() {
		return this.min_exposure;
	}

	int getMaximumExposure() {
		return this.max_exposure;
	}

	int getCurrentExposure() {
		if (camera == null)
			return 0;
		Camera.Parameters parameters = camera.getParameters();
		int current_exposure = parameters.getExposureCompensation();
		return current_exposure;
	}

	List<String> getSupportedExposures() {
		return this.exposures;
	}

	List<Camera.Size> getSupportedPreviewSizes() {
		return this.supported_preview_sizes;
	}

	public List<Camera.Size> getSupportedPictureSizes() {
		return this.sizes;
	}

	public List<String> getSupportedVideoQuality() {
		return this.video_quality;
	}

	List<Camera.Size> getSupportedVideoSizes() {
		return this.video_sizes;
	}

	List<String> getSupportedFlashValues() {
		return supported_flash_values;
	}

	List<String> getSupportedFocusValues() {
		return supported_focus_values;
	}

	public int getCameraId() {
		return this.cameraId;
	}

	private int getImageQuality() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		String image_quality_s = sharedPreferences.getString("preference_quality", "90");
		int image_quality = 0;
		try {
			image_quality = Integer.parseInt(image_quality_s);
		} catch (NumberFormatException exception) {
			Log.e(TAG, "image_quality_s invalid format: " + image_quality_s);
			image_quality = 90;
		}
		return image_quality;
	}

	void onResume() {
		this.app_is_paused = false;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		String ui_placement = sharedPreferences.getString("preference_ui_placement", "ui_right");
		this.ui_placement_right = ui_placement.equals("ui_right");
		this.openCamera();
	}

	void onPause() {
		this.app_is_paused = true;
		this.closeCamera();
	}

	void onSaveInstanceState(Bundle state) {
		state.putInt("cameraId", cameraId);
		state.putInt("zoom_factor", zoom_factor);
	}

	void setUIRotation(int ui_rotation) {
		this.ui_rotation = ui_rotation;
	}

	void locationChanged(Location location) {
		this.has_received_location = true;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		boolean store_location = sharedPreferences.getBoolean("preference_location", false);
		if (store_location) {
			this.location = location;
			// Android camera source claims we need to check lat/long != 0.0d
			if (location != null && (location.getLatitude() != 0.0d || location.getLongitude() != 0.0d)) {
				this.has_set_location = true;
				this.location_accuracy = location.getAccuracy();
			}
		}
	}

	private void updateParametersFromLocation() {
		if (camera != null) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			boolean store_location = sharedPreferences.getBoolean("preference_location", false);
			// Android camera source claims we need to check lat/long != 0.0d
			if (store_location && location != null && (location.getLatitude() != 0.0d || location.getLongitude() != 0.0d)) {
				Camera.Parameters parameters = camera.getParameters();
				parameters.removeGpsData();
				parameters.setGpsTimestamp(System.currentTimeMillis() / 1000);
				parameters.setGpsLatitude(location.getLatitude());
				parameters.setGpsLongitude(location.getLongitude());
				parameters.setGpsProcessingMethod(location.getProvider()); // from
																		   // http://boundarydevices.com/how-to-write-an-android-camera-app/
				if (location.hasAltitude()) {
					parameters.setGpsAltitude(location.getAltitude());
				} else {
					// Android camera source claims we need to fake one if not
					// present
					// and indeed, this is needed to fix crash on Nexus 7
					parameters.setGpsAltitude(0);
				}
				if (location.getTime() != 0) { // from Android camera source
					parameters.setGpsTimestamp(location.getTime() / 1000);
				}
				try {
					camera.setParameters(parameters);
				} catch (RuntimeException e) {
					Log.e(TAG, "failed to set parameters for gps info");
					e.printStackTrace();
				}
			} else {
				Camera.Parameters parameters = camera.getParameters();
				parameters.removeGpsData();
				camera.setParameters(parameters);
				this.has_set_location = false;
				has_received_location = false;
			}
		}
	}

	public boolean isVideo() {
		return is_video;
	}

	// must be static, to safely call from other Activities
	public static String getFlashPreferenceKey(int cameraId) {
		return "flash_value_" + cameraId;
	}

	// must be static, to safely call from other Activities
	public static String getFocusPreferenceKey(int cameraId) {
		return "focus_value_" + cameraId;
	}

	// must be static, to safely call from other Activities
	public static String getResolutionPreferenceKey(int cameraId) {
		return "camera_resolution_" + cameraId;
	}

	// must be static, to safely call from other Activities
	public static String getVideoQualityPreferenceKey(int cameraId) {
		return "video_quality_" + cameraId;
	}

	// must be static, to safely call from other Activities
	public static String getIsVideoPreferenceKey() {
		return "is_video";
	}

	// must be static, to safely call from other Activities
	public static String getExposurePreferenceKey() {
		return "preference_exposure";
	}

	// must be static, to safely call from other Activities
	public static String getColorEffectPreferenceKey() {
		return "preference_color_effect";
	}

	// must be static, to safely call from other Activities
	public static String getSceneModePreferenceKey() {
		return "preference_scene_mode";
	}

	// must be static, to safely call from other Activities
	public static String getWhiteBalancePreferenceKey() {
		return "preference_white_balance";
	}

	// must be static, to safely call from other Activities
	public static String getISOPreferenceKey() {
		return "preference_iso";
	}

	// for testing:
	public Camera getCamera() {
		/*
		 * if( MyDebug.LOG ) Log.d(TAG, "getCamera: " + camera);
		 */
		return this.camera;
	}

	public boolean supportsFocus() {
		return this.supported_focus_values != null;
	}

	public boolean supportsFlash() {
		return this.supported_flash_values != null;
	}

	public String getCurrentFlashValue() {
		if (this.current_flash_index == -1)
			return null;
		return this.supported_flash_values.get(current_flash_index);
	}

	public boolean hasFocusArea() {
		return this.has_focus_area;
	}

	public boolean isTakingPhotoOrOnTimer() {
		// return this.is_taking_photo;
		return this.phase == PHASE_TAKING_PHOTO || this.phase == PHASE_TIMER;
	}

	public boolean isTakingPhoto() {
		return this.phase == PHASE_TAKING_PHOTO;
	}

	public boolean isOnTimer() {
		// return this.is_taking_photo_on_timer;
		return this.phase == PHASE_TIMER;
	}

	public boolean isPreviewStarted() {
		return this.is_preview_started;
	}

	public boolean hasSetLocation() {
		return this.has_set_location;
	}

	public void setVideo_start_time_set(boolean video_start_time_set) {
		this.video_start_time_set = video_start_time_set;
	}

	public void reset() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if (this.is_video) {
			editor.putBoolean(getIsVideoPreferenceKey(), false);
		}
		editor.putString(getFocusPreferenceKey(0), "focus_mode_auto");
		editor.apply();
	}
}

