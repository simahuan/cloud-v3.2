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

import android.studio.util.URLUtils;
import android.text.TextUtils;
import android.util.Log;

import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.core.filemanager.transfer.TransferException;

/**
 * (发送类)
 * @author yangyp
 */
public class HeadHttpSender {
	private static final String TAG = HeadHttpSender.class.getSimpleName();
	private static final boolean DEBUG = true;

//	private Context context;
	//发送实例
	private static HeadHttpSender instance;
	//发送线程池
	private static ExecutorService executorService = Executors.newFixedThreadPool(1);

	private HeadHttpSender() {
//		this.context = context;
	}

	/**
	 * 获取闪电互传发送实例
	 * @return 闪电互传发送实例
	 */
	public static HeadHttpSender getInstance() {
		if(instance == null) {
			instance = new HeadHttpSender();
		}

		return instance;
	}

	/**
	 * 发送单个文件
	 * @param url 文件接收服务端url
	 * @param info 发送的文件
	 */
	public void sendFile(String url, File info) {
		try {
			startTask(url, info);
		} catch (TransferException e) {
			e.printStackTrace();
		}
	}

	private void startTask(String url, File info) throws TransferException {
		if(info != null && !TextUtils.isEmpty(url)) {
			executorService.execute(new SendFileRunnable(url, info));
		}
	}

	/**
	 * 文件发送Runnable
	 * @author ldj
	 * @version 1.0 2015年4月2日 下午2:36:03
	 */
	private final class SendFileRunnable implements Runnable {
		private File info;
		private String url;
		private HttpPost request;
		private DefaultHttpClient client;
		
		private static final int MAX_RETRY_COUNT = 3;
		private int retry;

		public SendFileRunnable(String url, File info) {
			this.url = url;
			this.info = info;
		}
		
		@Override
		public void run() {
			if(info != null && !TextUtils.isEmpty(url)) {
				try {
					retry ++;
					if(info.exists()) {
						request = new HttpPost(url);
						request.setHeader("fileName", URLUtils.encodeURIComponent(info.getName().split("\\.")[0]));
						request.setHeader("fileLength", String.valueOf(info.length()));
						request.setHeader("hostIp", NetUtil.getLocalIpAddressString());
						request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
						request.setEntity(new FileEntity(info, "binary/octet-stream")); 
						
						HttpParams params = new BasicHttpParams();  
						HttpConnectionParams.setConnectionTimeout(params, 30*1000);//连接超时
						HttpConnectionParams.setSoTimeout(params, 30*1000); //等待读取超时
						client = new DefaultHttpClient(params);
						client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
						HttpResponse response = client.execute(request);

						if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {//提交成功
							if(DEBUG)Log.d(TAG, "send SUCCESS->" );
						}else {//提交失败
							if(DEBUG)Log.d(TAG, "send ERROR");
							if(retry < MAX_RETRY_COUNT) {
								run();
								return;
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
					if(DEBUG)Log.e(TAG, "send ERROr");
					if(retry < MAX_RETRY_COUNT) {
						run();
						return;
					}
				}  finally {
					stopTransfer();
					if(client!=null)
					{
						client.getConnectionManager().shutdown();
					}
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
