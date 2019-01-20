package com.pisen.router.core.flashtransfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.studio.net.http.NanoHTTPD.Response.Status;
import android.studio.net.http.SimpleHttpServlet;
import android.studio.os.PreferencesUtils;
import android.studio.util.URLUtils;
import android.text.TextUtils;
import android.util.Log;

import com.pisen.router.BuildConfig;
import com.pisen.router.CloudApplication;
import com.pisen.router.common.utils.FileUtils;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferControl;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferProvider;
import com.pisen.router.core.filemanager.transfer.TransferStatus;

/**
 * (接收类)
 * @author yangyp
 */
public class RecvFlashTransfer {
	private static final String TAG = RecvFlashTransfer.class.getSimpleName();

	private Context context;
	// 默认接收存储目录
	private String storageDir = CloudApplication.DOWNLOAD_PATH.getAbsolutePath();
	// 接收实例
	private static RecvFlashTransfer instance;
	// http接收服务端
	private FlashTransferServer httpServlet;
	private ContentResolver resolver;

	private RecvFlashTransfer(Context context) {
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		this.httpServlet = new FlashTransferServer(FlashTransferConfig.PORT_HTTP_RECV_FILE);
		httpServlet.setSoTimeout(30 * 1000);
	}

	/**
	 * 获取闪电互传接收实例
	 * 
	 * @param context
	 * @param dbHelper
	 * @return
	 */
	public static RecvFlashTransfer getInstance(Context context) {
		if (instance == null) {
			instance = new RecvFlashTransfer(context);
		}

		return instance;
	}

	public void setStorageDir(String storageDir) {
		this.storageDir = storageDir;
	}

	/**
	 * 开启闪电互传文件接收服务
	 */
	public void startRecvService() {
		if (httpServlet != null && !httpServlet.isRunning()) {
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

	/**
	 * 删除接收任务
	 * 
	 * @param info
	 */
	public void removeTask(TransferInfo info) {
		if (httpServlet != null) {
			httpServlet.removeTask(info);
		}
	}

	private long insertToDatabase(TransferInfo info) {
		if (resolver == null)
			resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(TransferInfo.Table.url, info.url);
		values.put(TransferInfo.Table.filename, info.filename);
		values.put(TransferInfo.Table.filesize, info.filesize);
		values.put(TransferInfo.Table.storageDir, info.storageDir);
		values.put(TransferInfo.Table.remoteHostName, info.remoteHostName);
		values.put(TransferInfo.Table.remoteHostType, info.remoteHostType);
		values.put(TransferInfo.Table.takeControl, info.takeControl.value);
		values.put(TransferInfo.Table.currentBytes, info.currentBytes);
		values.put(TransferInfo.Table.status, info.status.value);
		values.put(TransferInfo.Table.dataCreated, System.currentTimeMillis());
		values.put(TransferInfo.Table.lastUpdated, System.currentTimeMillis());
		values.put(TransferInfo.Table.isDir, info.isDir);
		values.put(TransferInfo.Table.ctag, info.ctag.value);
		values.put(TransferInfo.Table.inboxRecordDeleted, info.inboxRecordDeleted);
		return ContentUris.parseId(resolver.insert(TransferProvider.CONTENT_URI, values));
	}

	private void updateToDatabase(TransferInfo info) {
		if (resolver == null)
			resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(TransferInfo.Table.filename, info.filename);
		values.put(TransferInfo.Table.filesize, info.filesize);
		values.put(TransferInfo.Table.storageDir, info.storageDir);
		values.put(TransferInfo.Table.remoteHostName, info.remoteHostName);
		values.put(TransferInfo.Table.remoteHostType, info.remoteHostType);
		values.put(TransferInfo.Table.currentBytes, info.currentBytes);
		values.put(TransferInfo.Table.status, info.status.value);
		values.put(TransferInfo.Table.lastUpdated, System.currentTimeMillis());
		values.put(TransferInfo.Table.isDir, info.isDir);
		values.put(TransferInfo.Table.ctag, info.ctag.value);

		resolver.update(info.getTransferUri(), values, TransferInfo.Table._ID + " == " + info._id, null);
	}

	/**
	 * 闪电互传文件接收服务servlet,底层已经实现多线程处理请求，所以不用新启线程处理post请求
	 * 
	 * @author ldj
	 * @version 1.0 2015年4月2日 下午5:12:34
	 */
	private class FlashTransferServer extends SimpleHttpServlet {
		// http服务运行状态
		private boolean isRunning;
		private ConcurrentHashMap<Long, TransferInfo> transferInfos;

		public FlashTransferServer(int port) {
			super(port);
			transferInfos = new ConcurrentHashMap<Long, TransferInfo>();
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

		/**
		 * 删除接收任务
		 * 
		 * @param info
		 */
		private void removeTask(TransferInfo info) {
			if (info != null && info._id > 0) {
				TransferInfo tmp = transferInfos.get(info._id);
				if (tmp != null) {
					tmp.takeControl = TransferControl.DELETE;
					tmp.status = TransferStatus.CANCELED;
					transferInfos.remove(tmp._id);
				}
			}
		}

		/**
		 * 判断任务是否取消
		 * 
		 * @param info
		 * @return
		 */
		private boolean isCanceled(TransferInfo info) {
			boolean canceled = true;
			if (info != null) {
				switch (info.takeControl) {
				case START:
					canceled = false;
					break;
				case PAUSE:
				case DELETE:
					canceled = true;
					break;
				default:
					break;
				}
			}

			return canceled;
		}

		static final int BUFFER = 4 * 1024;
		
		
		@Override
		protected void doPost(IHTTPSession session, Response response) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "开始接收");
			TransferInfo info = null;
			BufferedOutputStream bos = null;
			FileOutputStream fos = null;
			File recvFile = null;
			try {
				info = generateTransferInfo(session.getHeaders());
				if (info != null) {
					long id = insertToDatabase(info);
					if (id >= 0) {
						newRecv();
						info._id = id;
						transferInfos.put(id, info);

						recvFile = new File(info.storageDir, info.filename);
						if (recvFile.exists()) {
							recvFile = FileUtils.createCopyFile(recvFile, 1);
							recvFile.createNewFile();
							info.filename = recvFile.getName();
						} else {
							if (!recvFile.getParentFile().exists()) {
								recvFile.getParentFile().mkdirs();
							}
							recvFile.createNewFile();
						}
						fos = new FileOutputStream(recvFile);
						bos = new BufferedOutputStream(fos);
						InputStream is = session.getInputStream();
						byte[] buffer = new byte[BUFFER];
						long totalCount = info.filesize;
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
							info.currentBytes = curCount;
							// 通知更新
							sendProgressBroadcast(info);
							if (isCanceled(info)) {
								response.setStatus(Status.INTERNAL_ERROR);
								info.status = TransferStatus.CANCELED;
								Log.e(TAG, "cancel...->" + info._id);
								return;
							}
						}

						if (curCount != totalCount) {
							response.setStatus(Status.BAD_REQUEST);
							if (info.status != TransferStatus.CANCELED) {
								info.status = TransferStatus.HTTP_ERROR;
							}
							if (BuildConfig.DEBUG)
								Log.e(TAG, String.format("read file %s error!! totalcount is %d and readcount is %d", info.filename, totalCount, curCount));
						} else {
							response.setStatus(Status.OK);
							info.status = TransferStatus.SUCCESS;
							handleComplete(recvFile);
							if (BuildConfig.DEBUG)
								Log.e(TAG, "成功接收文件");
						}
					} else {// 插入数据库失败
						response.setStatus(Status.INTERNAL_ERROR);
						info.status = TransferStatus.UNKNOWN_ERROR;
						if (BuildConfig.DEBUG)
							Log.d(TAG, "闪电互传文件接收插入数据库失败");
					}
				} else {// http头缺少必要参数
					response.setStatus(Status.BAD_REQUEST);
					if (BuildConfig.DEBUG)
						Log.d(TAG, "闪电互传文件接收缺少http头信息!!!!");
				}
			} catch (Exception e) {
				response.setStatus(Status.INTERNAL_ERROR);
				if (info != null) {
					info.status = TransferStatus.UNKNOWN_ERROR;
				}
				e.printStackTrace();
			} finally {
				Log.d(TAG, "finally...");
				if (info != null) {
					transferInfos.remove(info._id);
					
					// 更新数据库数据
					if (info._id >= 0) {
						updateToDatabase(info);
					} else {
						insertToDatabase(info);
					}

					if (info.status != TransferStatus.SUCCESS) {// 操作不成功，则删除文件
						if (recvFile != null && recvFile.exists()) {
							recvFile.delete();
						}
					}
					// 发送进度通知广播
					sendProgressBroadcast(info);
				}

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
		 * 接收到新文件的处理
		 */
		private void newRecv() {
			sendNewRecvBroadcast();
			PreferencesUtils.setLong(KeyUtils.TIME_LAST_RECV, System.currentTimeMillis());
		}

		/**
		 * 接收完成时处理
		 * 
		 * @param recvFile
		 */
		private void handleComplete(final File recvFile) {
			FileType type = ResourceCategory.getFileType(recvFile.getAbsolutePath());
			switch (type) {
			case Image:
				scanAndNotify(recvFile, FlashTransferManager.ACTION_TRANSFER_COMPLETE_RECEIVE_IMAGE);
				break;
			case Video:
				scanAndNotify(recvFile, FlashTransferManager.ACTION_TRANSFER_COMPLETE_RECEIVE_MOVIE);
				break;
			case Audio:
				scanAndNotify(recvFile, FlashTransferManager.ACTION_TRANSFER_COMPLETE_RECEIVE_MUSIC);
				break;
			case Document:
				scanAndNotify(recvFile, FlashTransferManager.ACTION_TRANSFER_COMPLETE_RECEIVE_DOCUMENT);
				break;
			case Apk:
				context.sendBroadcast(new Intent(FlashTransferManager.ACTION_TRANSFER_COMPLETE_RECEIVE_APK));
				break;
			default:
				break;
			}
		}

		/**
		 * 更新媒体库，并发送通知完成广播
		 * 
		 * @param recvFile
		 */
		private void scanAndNotify(final File recvFile, final String action) {
			MediaScannerConnection.scanFile(context, new String[] { recvFile.getAbsolutePath() }, null, new OnScanCompletedListener() {

				@Override
				public void onScanCompleted(String path, Uri uri) {
					if (BuildConfig.DEBUG)
						Log.d(TAG, "文件接收成功");
					Log.e(TAG, "url->" + uri + "  path->" + path);
					context.sendBroadcast(new Intent(action));
				}
			});
		}

		/**
		 * 发送下载动态提示广播
		 */
		private void sendProgressBroadcast(TransferInfo info) {
			Intent in = new Intent(FlashTransferConfig.ACTION_TRANSFER_RECV_REFRESH);
			in.putExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_ID, info._id);
			in.putExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_STATUS, info.status);
			in.putExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_CURBYTES, info.currentBytes);
			in.putExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_FILESIZE, info.filesize);
			in.putExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_HOSTNAME, info.remoteHostName);
			context.sendBroadcast(in);
		}
		
		private void sendNewRecvBroadcast() {
			Intent in = new Intent(FlashTransferManager.ACTION_TRANSFER_RECEIVE_BEGIN);
			context.sendBroadcast(in);
		}

		/**
		 * 通过http header生成TransferInfo
		 * 
		 * @param headers
		 * @return
		 */
		private TransferInfo generateTransferInfo(Map<String, String> headers) {
			TransferInfo info = null;
			if (headers != null && !headers.isEmpty()) {
				try {
					String fileName = URLUtils.decodeURI(headers.get("filename"));
					String fileLength = headers.get("filelength");
					String phoneType = headers.get("phonetype");
					String hostName = headers.get("username");
					hostName = URLUtils.decodeURI(hostName);
					if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(fileLength) || TextUtils.isEmpty(hostName)) {
						throw new InvalidParameterException("接收文件必要的参数未设置");
					}

					info = new TransferInfo(TransferCTag.FlashRecv);
					info.filename = fileName;
					info.filesize = Long.parseLong(fileLength);
					info.remoteHostType = phoneType;
					info.remoteHostName = hostName;
					info.storageDir = storageDir;
					info.status = TransferStatus.RUNNING;
					info.takeControl = TransferControl.START;
					info.lastUpdated = System.currentTimeMillis();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
			return info;
		}
	}
}
