package com.pisen.router.core.flashtransfer;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.studio.os.PreferencesUtils;
import android.studio.util.URLUtils;
import android.text.TextUtils;
import android.util.Log;
import android.support.v4.util.LongSparseArray;

import com.pisen.router.common.utils.FileUtils;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferControl;
import com.pisen.router.core.filemanager.transfer.TransferDbHelper;
import com.pisen.router.core.filemanager.transfer.TransferException;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.core.flashtransfer.scan.DeviceContainer;
import com.pisen.router.core.monitor.WifiMonitor;

/**
 * (发送类)
 * @author yangyp
 */
public class SendFlashTransfer {
	private static final String TAG = SendFlashTransfer.class.getSimpleName();
	private static final boolean DEBUG = true;

	private Context context;
	//发送实例
	private static SendFlashTransfer instance;
	private TransferDbHelper dbHelper;
	//发送线程池
	private static ExecutorService executorService = Executors.newFixedThreadPool(1);
	private LongSparseArray<TransferInfo> transferInfos;

	private SendFlashTransfer(Context context) {
		this.context = context;
		this.dbHelper = TransferDbHelper.getInstance(context);
		transferInfos = new LongSparseArray<TransferInfo>();
	}

	/**
	 * 获取闪电互传发送实例
	 * @return 闪电互传发送实例
	 */
	public static SendFlashTransfer getInstance(Context context) {
		if(instance == null) {
			instance = new SendFlashTransfer(context);
		}

		return instance;
	}

	/**
	 * 发送单个文件
	 * @param url 文件接收服务端url
	 * @param info 发送的文件
	 */
	private void sendFile(String url, TransferInfo info) {
		try {
			startTask(url, info);
		} catch (TransferException e) {
			e.printStackTrace();
			info.status = TransferStatus.UNKNOWN_ERROR;
			//更新数据库
			updateToDb(info);
			sendNotifyBroadcast(info);
		}
	}

	/**
	 * 批量发送文件
	 * @param url	文件接收服务端url
	 * @param infos	发送的文件
	 */
	public void sendFile(String url, TransferInfo... infos) {
		if(!TextUtils.isEmpty(url) && infos != null && infos.length >0) {
			TransferInfo tmp = null;
			for(int i=0, length = infos.length; i < length; i++) {
				tmp = infos[i];
				sendFile(url, tmp);
			}
		}
	}

	private void startTask(String url, TransferInfo info) throws TransferException {
		if(info != null && info._id >0) {
			if(info.ctag != TransferCTag.FlashSend) {
				throw new TransferException(TransferStatus.UNKNOWN_ERROR, "发送类型不一致");
			}

			TransferInfo tmp = transferInfos.get(info._id);
			if(tmp != null) {
				switch (tmp.status) {
				case RUNNING:				//任务正在进行
					if(DEBUG) Log.d(TAG, "refuse start an existed send task!!!");
					break;
				case PENDING:				//等待进行
				case PAUSE:					//任务暂停
				case SUCCESS:				//任务完成
				case CANCELED:				//任务取消
				case UNKNOWN_ERROR:			//任务出错
				case HTTP_ERROR:			//任务出错
				case CANNOT_RESUME_ERROR:	//任务出错
					tmp.takeControl = TransferControl.START;
					info.status = TransferStatus.PENDING;
					transferInfos.put(info._id, info);
					executorService.execute(new SendFileRunnable(url, info));
					break;
				default:
					break;
				}
			}else {
				info.takeControl = TransferControl.START;
				info.status = TransferStatus.PENDING;
				transferInfos.put(info._id, info);
				executorService.execute(new SendFileRunnable(url, info));
			}
		}
	}

	/**
	 * 暂停任务
	 * @param info
	 */
	public void pauseTask(TransferInfo info) {
		if(info != null && info._id >0) {
			TransferInfo tmp = transferInfos.get(info._id);
			if(tmp != null) {
				tmp.takeControl = TransferControl.PAUSE;
				tmp.status = TransferStatus.PAUSE;
			}
		}
	}
	
	/**
	 * 删除所有任务
	 */
	public void removeAllTask() {
		if(transferInfos != null && transferInfos.size() >0) {
			int size = transferInfos.size();
			for(int i=0; i<size; i++) {
				TransferInfo tmp = transferInfos.valueAt(i);
				if(tmp != null) {
					tmp.takeControl = TransferControl.PAUSE;
					tmp.status = TransferStatus.PAUSE;
				}
			}
		}
	}

	/**
	 * 删除任务
	 * @param info
	 */
	public void removeTask(TransferInfo info) {
		if(info != null && info._id >0) {
			TransferInfo tmp = transferInfos.get(info._id);
			if(tmp != null) {
				tmp.takeControl = TransferControl.DELETE;
				tmp.status = TransferStatus.CANCELED;
				transferInfos.remove(tmp._id);
			}
		}
	}

	/**
	 * 更新数据库数据
	 * @param info	需要更新的数据项
	 */
	private void updateToDb(TransferInfo info) {
		if(info != null && dbHelper != null) {
			ContentValues values = new ContentValues();
			values.put(TransferInfo.Table.currentBytes, info.currentBytes);
			values.put(TransferInfo.Table.lastUpdated, System.currentTimeMillis());
			values.put(TransferInfo.Table.status, String.valueOf(info.status.value));
			dbHelper.update(values, info._id);
		}
	}

	/**
	 * 发送上传动态提示广播
	 */
	private void sendNotifyBroadcast(TransferInfo info) {
		Intent in = new Intent(FlashTransferConfig.ACTION_TRANSFER_SEND_REFRESH);
		in.putExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_ID, info._id);
		in.putExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_STATUS, info.status);
		in.putExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_CURBYTES, info.currentBytes);
		in.putExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_FILESIZE, info.filesize);
		context.sendBroadcast(in);
	}

	/**
	 * 文件发送Runnable
	 * @author ldj
	 * @version 1.0 2015年4月2日 下午2:36:03
	 */
	private final class SendFileRunnable implements Runnable, OnProgressListener {
		private TransferInfo info;
		private String url;
		private HttpPost request;
		private DefaultHttpClient client;
		
		private WifiApManager wifiApManager;
		private long lastTime;

		public SendFileRunnable(String url, TransferInfo info) {
			this.url = url;
			this.info = info;
			wifiApManager = new WifiApManager(context);
		}
		

		@Override
		public void run() {
			if(info != null  && !TextUtils.isEmpty(info.url) && !TextUtils.isEmpty(info.filename)) {//&& info.filesize 
				try {
					//任务删除则不执行传输
					if(info.takeControl == TransferControl.DELETE) {
						return;
					}
					
					setTransferStatus(TransferStatus.RUNNING);
					updateToDb(info);

					File tmp = new File(info.url, info.filename);
					if(tmp.exists()) {
						request = new HttpPost(url);
						String tmpName = info.filename;
						if(tmpName.endsWith(".apk")) {
							tmpName = FileUtils.getApkName(context, tmp.getAbsolutePath());
							if(TextUtils.isEmpty(tmpName)) {
								tmpName = info.filename;
							}else {
								tmpName = String.format("%s.apk", tmpName);
							}
						}
						request.setHeader("fileName", URLUtils.encodeURIComponent(tmpName));
						request.setHeader("fileType", String.valueOf(getIosFileType(info.filename)));
						request.setHeader("fileLength", info.filesize == 0 ? String.valueOf(tmp.length()) :String.valueOf(info.filesize));
						request.setHeader("phoneType", String.format("%s_%s", FlashTransferConfig.PHONE_TYPE_ANDROID, PreferencesUtils.getInt(KeyUtils.NICK_HEAD, -1)));
						request.setHeader("userName", URLUtils.encodeURIComponent(PreferencesUtils.getString(KeyUtils.NICK_NAME, android.os.Build.MODEL)));
						request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
						request.setEntity(new ProgressEntityWrapper(new FileEntity(tmp, "binary/octet-stream"), 
								new OnProgressListener() {							
							@Override
							public void onTransferred(int curCount) {
								SendFileRunnable.this.onTransferred(curCount);
								if(System.currentTimeMillis() - lastTime > 1500 && !isConnectionEnabled()) {
									Log.e(TAG, "connect changed!! abort request");
									request.abort();
								}
							}
						}
						)); 
						lastTime = System.currentTimeMillis();
						
						HttpParams params = new BasicHttpParams();  
						HttpConnectionParams.setConnectionTimeout(params, 30*1000);//连接超时
						HttpConnectionParams.setSoTimeout(params, 30*1000); //等待读取超时
						client = new DefaultHttpClient(params);
						client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
						HttpResponse response = client.execute(request);

						if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {//提交成功
							setTransferStatus(TransferStatus.SUCCESS);	
							if(DEBUG)Log.d(TAG, "send SUCCESS->" + info.filename );
						}else {//提交失败
							setTransferStatus(TransferStatus.HTTP_ERROR);
							if(DEBUG)Log.d(TAG, "send ERROR-> " + info.filename + "   HttpStatus->" + response.getStatusLine().getStatusCode());
						}
					}else {
						setTransferStatus(TransferStatus.UNKNOWN_ERROR);
					}

				} catch (Exception e) {
					e.printStackTrace();
					setTransferStatus(TransferStatus.HTTP_ERROR);
					if(DEBUG)Log.e(TAG, "send ERROR->" + info.filename + "   url->" + url);
				}  finally {
					//更新数据库
					updateToDb(info);
					if(info.status != TransferStatus.SUCCESS) {
						//传输失败，下线用户
						DeviceContainer.getInstance(context).removeDevice(info.storageDir);
					}
					
					sendNotifyBroadcast(info);
					//移除引用 
					transferInfos.remove(info._id);
					stopTransfer();
					if(client!=null)
					{
						client.getConnectionManager().shutdown();
					}
				}
			}
		}
		
		private boolean isConnectionEnabled() {
			return ( WifiMonitor.getInstance().isWifiConnected() || wifiApManager.isWifiApEnabled() );
		}
		
		private int getIosFileType(String fileName) {
			int type = -1;
			if(!TextUtils.isEmpty(fileName)) {
				FileType fileType = ResourceCategory.getFileType(fileName);
				switch (fileType) {
				case Image:
					type = 1;
					break;
				case Audio:
					type = 2;
					break;
				case Video:
					type = 3;
					break;
				case Document:
					type = 4;
					break;
				default:
					break;
				}
			}
			return type;
		}

		/**
		 * 设置传输状态
		 * @param status
		 */
		private void setTransferStatus(TransferStatus status) {
			if(info != null) {
				info.status = status;
			}
		}

		/**
		 * 检查任务控制状态
		 * @throws TransferException 
		 */
		private void checkTransferControl() {
			if(info != null) {
				switch (info.takeControl) {
				case START:		//开始
					break;
				case PAUSE:		//暂停
					setTransferStatus(TransferStatus.PAUSE);
					updateToDb(info);
					sendNotifyBroadcast(info);
					stopTransfer();
				case DELETE:	//删除
					setTransferStatus(TransferStatus.CANCELED);
					sendNotifyBroadcast(info);
					stopTransfer();
				default:
					break;
				}
			}
		}

		/**
		 * 停止传输
		 */
		private void stopTransfer() {
			if(request != null) {
				request.abort();
				request = null;
			}
		}

		private int sendCount = 0;
		private int notifyCount = 10*1024;
		@Override
		public void onTransferred(int curCount) {
			// 进度更新
			info.currentBytes = curCount;
			//发送进度更新广播
			if(curCount - sendCount >= notifyCount) {
				if(DEBUG) Log.d(TAG, "http file send! current send byte->" + curCount);
				sendNotifyBroadcast(info);
				sendCount =curCount;
			}
			//检查任务控制状态
			checkTransferControl();
		}

		/**
		 * 支持发送进度提醒的http实体
		 * @author ldj
		 * @version 1.0 2015年4月2日 下午3:49:05
		 */
		private class ProgressEntityWrapper extends HttpEntityWrapper {
			private ProgressOutputStream pos;
			private OnProgressListener listener;

			public ProgressEntityWrapper(HttpEntity wrapped, OnProgressListener listener) {
				super(wrapped);

				this.listener = listener;
			}

			@Override
			public void writeTo(OutputStream outstream) throws IOException {
				if(pos == null) {
					pos = new ProgressOutputStream(outstream, listener);
				}
				wrappedEntity.writeTo(pos);
			}
		}

		/**
		 * 支持发送字节数变更通知的输出流包装类
		 * @author ldj
		 * @version 1.0 2015年4月2日 下午3:45:55
		 */
		private class ProgressOutputStream extends FilterOutputStream {

			private int transferCount;
			private OnProgressListener listener;
			//进度通知阀值
			//			private static final int THRESHOLD_NOTIFY = 150;

			public ProgressOutputStream(OutputStream out, OnProgressListener listener) {
				super(out);
				this.listener = listener;
				transferCount = 0;
			}

			@Override
			public void write(byte[] buffer, int offset, int length) throws IOException {
				out.write(buffer, offset, length);
				transferCount += length;
				if(listener != null) {
					listener.onTransferred(transferCount);
				}
				out.flush();
			}

			@Override
			public void write(int oneByte) throws IOException {
				out.write(oneByte);
				transferCount ++;
				if(listener != null) {
					listener.onTransferred(transferCount);
				}
				out.flush();
			}
		}
	}

	/**
	 * 发送进度变更接口
	 * @author ldj
	 * @version 1.0 2015年4月2日 下午3:49:37
	 */
	private interface OnProgressListener {
		void onTransferred(int curCount);
	}
}
