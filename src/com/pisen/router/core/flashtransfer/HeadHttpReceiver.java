package com.pisen.router.core.flashtransfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Map;

import android.content.Context;
import android.studio.net.http.NanoHTTPD.Response.Status;
import android.studio.net.http.SimpleHttpServlet;
import android.studio.util.URLUtils;
import android.text.TextUtils;
import android.util.Log;

import com.pisen.router.BuildConfig;
import com.pisen.router.CloudApplication;
import com.pisen.router.core.flashtransfer.scan.DeviceContainer;
import com.pisen.router.core.flashtransfer.scan.protocol.UserInfoPtlV2;

/**
 * 头像接收
 * @author ldj
 * @version 1.0 2015年8月11日 下午1:22:25
 */
public class HeadHttpReceiver {
	private static final String TAG = HeadHttpReceiver.class.getSimpleName();

	private Context context;
	// 默认接收存储目录
	private String storageDir = CloudApplication.HEAD_PATH.getAbsolutePath();
	// 接收实例
	private static HeadHttpReceiver instance;
	// http接收服务端
	private ReceiverServer httpServlet;

	private HeadHttpReceiver(Context context) {
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		this.httpServlet = new ReceiverServer(FlashTransferConfig.PORT_HTTP_RECV_HEAD);
		httpServlet.setSoTimeout(30 * 1000);
	}

	/**
	 * 获取接收实例
	 * @param context
	 * @param dbHelper
	 * @return
	 */
	public static HeadHttpReceiver getInstance(Context context) {
		if (instance == null) {
			instance = new HeadHttpReceiver(context);
		}

		return instance;
	}

	/**
	 * 开启接收服务
	 */
	public void startRecvService() {
		Log.e(TAG, "===startRecvService===");
		if (httpServlet != null && !httpServlet.isRunning()) {
			Log.e(TAG, "===startRecvService===->" + "httpServlet start!");
			httpServlet.start();
		}
	}

	public boolean isRunning() {
		return httpServlet.isRunning;
	}

	/**
	 * 停止接收服务
	 */
	public void stopRecvService() {
		if (httpServlet != null) {
			httpServlet.stop();
			httpServlet = null;
		}
		instance = null;
	}

	private class ReceiverServer extends SimpleHttpServlet {
		// http服务运行状态
		private boolean isRunning;

		public ReceiverServer(int port) {
			super(port);
		}

		@Override
		public void start() {
			isRunning = true;
			super.start();
		}

		@Override
		public void stop() {
			isRunning = false;
			super.stop();
		}

		public boolean isRunning() {
			return isRunning;
		}

		static final int BUFFER = 4 * 1024;
		
		
		@Override
		protected void doPost(IHTTPSession session, Response response) {
			if (BuildConfig.DEBUG) Log.e(TAG, "开始接收头像");
			RecvBean info = null;
			BufferedOutputStream bos = null;
			FileOutputStream fos = null;
			File recvFile = null;
			try {
				info = generateTransferInfo(session.getHeaders());
				if (info != null){// && DeviceContainer.getInstance(context).getDevice(info.ip) != null) {
						recvFile = new File(storageDir, info.fileName + ".png");
						if (recvFile.exists()) {
							recvFile.delete();
						} 
						recvFile.createNewFile();
						
						fos = new FileOutputStream(recvFile);
						bos = new BufferedOutputStream(fos);
						InputStream is = session.getInputStream();
						byte[] buffer = new byte[BUFFER];
						long totalCount = info.fileLength;
						long curCount = 0;
						int readCount = 0;
						long free = 0;
						while (curCount < totalCount) {
							free = totalCount - curCount;
							int byteCount = (int) (free < BUFFER ? free : BUFFER);
							readCount = is.read(buffer, 0, byteCount);
							bos.write(buffer, 0, readCount);
							bos.flush();

							curCount += readCount;
						}

						if (curCount != totalCount) {
							response.setStatus(Status.BAD_REQUEST);
							recvFile.delete();
							if (BuildConfig.DEBUG) Log.e(TAG, String.format("recv head file %s error!! totalcount is %d and readcount is %d", info.fileName, totalCount, curCount));
						} else {
							response.setStatus(Status.OK);
							handleComplete(info);
							if (BuildConfig.DEBUG) Log.e(TAG, "成功接收文件");
						}
				} else {// http头缺少必要参数
					response.setStatus(Status.BAD_REQUEST);
					if (BuildConfig.DEBUG) Log.e(TAG, "接收头像文件缺少http头信息或用户不在线!!!!");
				}
			} catch (Exception e) {
				response.setStatus(Status.INTERNAL_ERROR);
				e.printStackTrace();
				if (BuildConfig.DEBUG) Log.e(TAG, "接收头像发生异常");
			} finally {
				try {
					if (fos != null) {
						fos.close();
						fos = null;
					}

					if (bos != null) {
						bos.close();
						bos = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		/**
		 * 接收完成时处理
		 * 
		 * @param info
		 */
		private void handleComplete(final RecvBean info) {
			if(info != null) {
				UserInfoPtlV2 device = DeviceContainer.getInstance(context).getDevice(info.ip);
				if(device != null) {
					device.hostType = String.format("%s_%s",device.hostType.split("_")[0], info.fileName);
					DeviceContainer.getInstance(context).notifyChanged();
				}
			}
		}
		
		private RecvBean generateTransferInfo(Map<String, String> headers) {
			RecvBean info = null;
			if (headers != null && !headers.isEmpty()) {
				try {
					String fileName = URLUtils.decodeURI(headers.get("filename"));
					String fileLength = headers.get("filelength");
					String hostIp = headers.get("hostip");
					if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(fileLength) || TextUtils.isEmpty(hostIp)) {
						throw new InvalidParameterException("接收文件必要的参数未设置");
					}

					info = new RecvBean();
					info.fileName = fileName;
					info.fileLength = Long.parseLong(fileLength);
					info.ip = hostIp;
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
			return info;
		}
		
		private class RecvBean {
			String fileName;
			long fileLength;
			String ip;
		}
	}
}
