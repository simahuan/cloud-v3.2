package com.pisen.router.core.camera;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * (照相机)
 * 
 * @author yangyp
 */
public class CameraPreview extends SurfaceView implements ICameraPreview, SurfaceHolder.Callback {

	public CameraPreview(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCameraPreview() {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchCamera() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isVideo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getFlashMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFocusMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void zoomIn() {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoomOut() {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestAutoFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pauseVideo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopVideo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void takePicture() {
		// TODO Auto-generated method stub

	}

}
