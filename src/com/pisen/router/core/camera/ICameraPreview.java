package com.pisen.router.core.camera;

/**
 * (照相机)
 * 
 * @author yangyp
 */
public interface ICameraPreview {

	void startCameraPreview();

	void switchCamera(); // 切换镜头

	boolean isVideo();

	String getFlashMode(); // 闪光模式

	String getFocusMode(); // 焦点模式

	void zoomIn(); // 放大

	void zoomOut(); // 缩小

	void requestAutoFocus(); // 聚焦

	void pauseVideo();

	void stopVideo();

	void takePicture(); // 拍照
}
