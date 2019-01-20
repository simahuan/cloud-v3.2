package com.pisen.router.core.filemanager.transfer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.studio.io.CountingInputStreamEntity;
import android.studio.io.CountingOutputStream.CountingListener;
import android.studio.util.URLUtils;
import android.util.Log;

import com.pisen.router.CloudApplication;
import com.pisen.router.config.Config;

import org.apache.http.HttpResponse;

import de.aflx.sardine.impl.SardineException;
import de.aflx.sardine.impl.handler.VoidResponseHandler;

/**
 * (上传传输类)
 * 
 * @author yangyp
 */
public class UploadSardineTask extends SardineTransferTask {

	public static final String ACTION_PROGRESS = "com.pisen.router.transfer.UPLOAD_PROGRESS";
	public static final String ACTION_ADD = "com.pisen.router.transfer.ACTION_ADD";
	//定义磁盘提示信息常量
	public static final int INSUFFICIENT_STORAGE = 507;
	public static final String INSUFFICIENT_STORAGE_TOAST = "Insufficient Storage";
	private Context context;

	//定义磁盘空间不足信息的处理handler
	private VoidResponseHandler responseHandler = new VoidResponseHandler(){
		@Override
		public Void handleResponse(HttpResponse response) throws IOException {
			if(response.getStatusLine().getStatusCode() == INSUFFICIENT_STORAGE && context.getApplicationContext() instanceof CloudApplication){
				//((CloudApplication)context.getApplicationContext()).handler.sendMessage(((CloudApplication)context.getApplicationContext()).handler.obtainMessage(INSUFFICIENT_STORAGE));
			}
			return  super.handleResponse(response);
		}
	};

	public UploadSardineTask(Context context, ContentResolver resolver, TransferInfo info) {
		super(context, info, null, null);
		this.context = context;
	}

	@Override
	protected String notifyUpdateProgressAction() {
		return ACTION_PROGRESS;
	}

	@Override
	public void checkPausedOrCanceled() throws TransferException {
		synchronized (mInfo) {
			super.checkPausedOrCanceled();
			try {
				if (TransferCTag.Upload.equals(mInfo.ctag) || TransferCTag.CameraUpload.equals(mInfo.ctag)) {
					// if (!sardine.exists(mInfo.storageDir)) {
					// // 当没有存储空间或者上传路径失效时
					// mInfo.status = TransferStatus.UNKNOWN_ERROR;
					// }
					if (!Config.hasStorage()) {
						// 当没有存储空间或者上传路径失效时
						mInfo.status = TransferStatus.UNKNOWN_ERROR;
					}
				}
			} catch (Exception e) {
				throw new TransferException(TransferStatus.UNKNOWN_ERROR, "download error by service");
			}
		}
	}

	@Override
	protected List<TransferInfo> listFile(String url) throws Exception {
		List<TransferInfo> results = new ArrayList<TransferInfo>();
		File[] childList = new File(url).listFiles();
		if (childList != null) {
			for (File f : childList) {
				TransferInfo info = new TransferInfo(TransferCTag.Upload);
				info.url = f.getPath();
				info.filename = f.getName();
				info.isDir = f.isDirectory();
				info.filesize = f.length();
				mInfo.totalBytes += f.length();
				info.dataCreated = System.currentTimeMillis();
				info.lastUpdated = info.dataCreated;
				results.add(info);
			}
		}
		return results;
	}

	@Override
	protected void finalizeDestination() {
		super.finalizeDestination();
		if (TransferInfo.isStatusError(mInfo.status)) {
			// 删除临时文件
			if (mInfo.storageDir != null) {
				try {
					// sardine.delete(mInfo.storageDir);
				} catch (Exception e) {

				}
				mInfo.storageDir = null;
			}

		} else if (TransferInfo.isStatusSuccess(mInfo.status)) {
			// 重命名临时文件
			if (mInfo.storageDir != null) {
				File tempFile = new File(mInfo.storageDir);
				if (tempFile.renameTo(new File(mInfo.storageDir))) {
				}
			}
		}

		mInfo = null;
	}

	/**
	 * 执行传输,可以抛异常
	 */
	@Override
	protected void executeTransfer(TransferInfo info) throws Exception {
		try {
			transProcess(info);
		} catch (RuntimeException e) {
			if(e instanceof TransferException) {
				throw new TransferException(((TransferException)e).getFinalStatus(), "");
			} else {
				throw new TransferException(TransferStatus.UNKNOWN_ERROR, "");
			}
		}
	}

	/**
	 * 检测文件是否存在
	 * 
	 * @param info
	 * @throws TransferException
	 */
	protected void checkDownloadFile(TransferInfo info) throws TransferException {
		try {
			// 判断目标文件是否存在
			if (!sardine.exists(info.storageDir)) {
				String webdavUri = createDirectoryWebDav(info.storageDir);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new TransferException(TransferStatus.UNKNOWN_ERROR, "创建目录出错: ", ex);
		}
	}

	/**
	 * WebDAV不支持创建多目录，这里使用递归创建目录
	 * 
	 * @param webdavUri
	 * @return
	 * @throws IOException
	 */
	private String createDirectoryWebDav(String webdavUri) throws Exception {
		if (!sardine.exists(webdavUri)) {
			String parentUri = URLUtils.getParentURI(webdavUri);
			createDirectoryWebDav(parentUri);
			sardine.createDir(webdavUri);
		}
		return webdavUri;
	}

	/**
	 * 传输实现
	 * 
	 * @param info
	 * @throws TransferException
	 * @throws IOException
	 * @throws TransferException
	 */
	public void transProcess(final TransferInfo info) throws TransferException, Exception {
		InputStream mStream = null;
		try {
			checkDownloadFile(info);
			mStream = new BufferedInputStream(new FileInputStream(info.url));
			//增加Content-Length,Date字段
			sardine.put(info.storageDir + info.filename, new CountingInputStreamEntity(mStream, mStream.available(), new CountingListener() {
				@Override
				public void onChange(long counterBytes, int bytesRead) throws IOException {
					checkPausedOrCanceled();
					mInfo.currentBytes += bytesRead;
					if (mInfo.status != TransferStatus.PAUSE && mInfo.status != TransferStatus.CANCELED)
						updateProgress(mInfo);
				}
			}),responseHandler);
		} catch (Exception e) {
			e.printStackTrace();
			//修改空间不足异常
			if(e instanceof  SardineException && ((SardineException) e).getStatusCode() == INSUFFICIENT_STORAGE){
				throw new TransferException(TransferStatus.INSUFFICIENT_STORAGE_ERROR, e);
			}else {
				throw new TransferException(TransferStatus.HTTP_ERROR, e);
			}
		} finally {
			if (mStream != null) {
				mStream.close();
				mStream = null;
			}
		}
	}

}
