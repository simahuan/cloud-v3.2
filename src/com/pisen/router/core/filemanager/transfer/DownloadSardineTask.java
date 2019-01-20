package com.pisen.router.core.filemanager.transfer;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import io.vov.vitamio.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.studio.util.URLUtils;
import android.util.Pair;

import com.pisen.router.common.utils.FileUtils;
import com.pisen.router.config.AppFileUtils;
import com.pisen.router.core.filemanager.ResourceInfo;

/**
 * 传输任务
 * 
 * @author yangyp
 */
public class DownloadSardineTask extends SardineTransferTask {

	public static final String ACTION_PROGRESS = "com.pisen.router.transfer.DOWNLOAD_PROGRESS";

	public DownloadSardineTask(Context context, ContentResolver resolver, TransferInfo info) {
		super(context, info, null, null);
	}

	@Override
	protected String notifyUpdateProgressAction() {
		return ACTION_PROGRESS;
	}

	@Override
	public void checkPausedOrCanceled() throws TransferException {
		synchronized (mInfo) {
			super.checkPausedOrCanceled();
			if (TransferCTag.Download.equals(mInfo.ctag)) {
				if (!AppFileUtils.hasSDAvailableSize()) {
					throw new TransferException(TransferStatus.UNKNOWN_ERROR, "target disk do not enough");
				}
			}
		}
	}

	@Override
	protected List<TransferInfo> listFile(String url) throws Exception {
		List<TransferInfo> results = new ArrayList<TransferInfo>();
		List<ResourceInfo> files = sardine.list(url);
		for (ResourceInfo res : files) {
			TransferInfo info = new TransferInfo(TransferCTag.Download);
			info.url = res.path;
			info.filename = res.name;
			// info.type = res.getContentType();
			info.isDir = res.isDirectory;
			info.filesize = res.size;
			info.dataCreated = System.currentTimeMillis();
			info.lastUpdated = info.dataCreated;
			// info.parentDir = url;
			results.add(info);
		}
		return results;
	}

	@Override
	protected void finalizeDestination() {
		super.finalizeDestination();
		if (TransferInfo.isStatusError(mInfo.status)) {
			// 删除临时文件
			if (mInfo.storageDir != null) {
				new File(mInfo.storageDir).delete();
				// mInfo.storageDir = null;
			}

		} else if (TransferInfo.isStatusSuccess(mInfo.status)) {
			// 重命名临时文件
			// if (mInfo.storageDir != null) {
			// File tempFile = new File(mInfo.getStoragePath());
			// if (tempFile.renameTo(new File(mInfo.storageDir))) {
			// mediaScannerFile(mContext, mInfo.getStoragePath());
			// }
			// }
		}
		mInfo = null;
	}

	/**
	 * 通知更新媒体库
	 * 
	 * @param context
	 * @param paths
	 */
	protected void mediaScannerFile(Context context, String... paths) {
		MediaScannerConnection.scanFile(context, paths, null, new MediaScannerConnection.OnScanCompletedListener() {
			public void onScanCompleted(String path, Uri uri) {

			}
		});
	}

	/**
	 * 执行下载操作
	 */
	@Override
	protected void executeTransfer(TransferInfo info) throws TransferException {
		final boolean resuming = true;// mInfo.currentBytes != 0;
		URL url = newURL(info.url);
		int redirectionCount = 0;
		while (redirectionCount++ < MAX_REDIRECTS) {
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.setConnectTimeout(DEFAULT_TIMEOUT);
				conn.setReadTimeout(DEFAULT_TIMEOUT);
				final int responseCode = conn.getResponseCode();
				switch (responseCode) {
				case HTTP_OK:
					// 如果不支持断点传输,那么重新下载
					if (resuming) {
						// 删除临时文件
						if (mInfo.storageDir != null) {
							// new File(mInfo.storageDir).delete();
							// mInfo.storageDir = null;
							// sardine.delete(path);
						}
					}
					transferData(conn, false, info);
					return;

				case HTTP_PARTIAL:
					if (!resuming) {
						throw new TransferException(TransferStatus.CANNOT_RESUME_ERROR, "Expected OK, but received partial");
					}
					transferData(conn, true, info);
					return;

				case HTTP_MOVED_PERM:
				case HTTP_MOVED_TEMP:
				case HTTP_SEE_OTHER:
				case 307:
					final String location = conn.getHeaderField("Location");
					url = new URL(url, location);
					if (responseCode == HTTP_MOVED_PERM) {
						mInfo.url = url.toString();
					}
					continue;

				default:
					TransferException.throwUnhandledHttpError(TransferStatus.UNKNOWN_ERROR, conn.getResponseMessage());
				}

			} catch (IOException e) {
				throw new TransferException(TransferStatus.HTTP_ERROR, e);

			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
		}

		throw new TransferException(TransferStatus.HTTP_ERROR, "Too many redirects");
	}

	/**
	 * 创建连接
	 * 
	 * @param url
	 * @return
	 * @throws TransferException
	 */
	private static URL newURL(String url) throws TransferException {
		try {
			return new URL(URLUtils.encodeURL(url));
		} catch (MalformedURLException e) {
			throw new TransferException(TransferStatus.HTTP_ERROR, e);
		}
	}

	/**
	 * 传输数据
	 * 
	 * @param conn
	 * @param resuming
	 * @throws TransferException
	 */
	private void transferData(HttpURLConnection conn, boolean resuming, TransferInfo infos) throws TransferException {
		final boolean hasLength = mInfo.totalBytes != -1;
		final boolean isConnectionClose = "close".equalsIgnoreCase(conn.getHeaderField("Connection"));
		final boolean isEncodingChunked = "chunked".equalsIgnoreCase(conn.getHeaderField("Transfer-Encoding"));

		final boolean finishKnown = hasLength || isConnectionClose || isEncodingChunked;
		if (!finishKnown) {
			throw new TransferException(TransferStatus.CANNOT_RESUME_ERROR, "can't know size of download, giving up");
		}

		InputStream in = null;
		OutputStream out = null;
		File recvFile = null;
		File tmpFile = null;//临时下载文件对象
		try {
			try {
				in = conn.getInputStream();
				recvFile = new File(infos.storageDir, infos.filename);
				if (recvFile.exists()) {
					recvFile = FileUtils.createCopyFile(recvFile, 1);
					recvFile.createNewFile();
					infos.filename = recvFile.getName();
				} else {
					if (!recvFile.getParentFile().exists()) {
						recvFile.getParentFile().mkdirs();
					}
					recvFile.createNewFile();
				}
				
				tmpFile = new File(infos.storageDir, infos.filename+".tmp");
				if (tmpFile.exists()) {
					tmpFile.delete();
				}
				out = new FileOutputStream(tmpFile);
				transferData(in, out);
				tmpFile.renameTo(recvFile);
				mediaScannerFile(mContext, new String[] { recvFile.getAbsolutePath() });
			} catch (IOException e) {
				e.printStackTrace();
				if (recvFile != null && recvFile.exists()) {
					recvFile.delete();
				}
				if (tmpFile != null && tmpFile.exists()) {
					tmpFile.delete();
				}
				throw new TransferException(TransferStatus.HTTP_ERROR, e);
			}
		} finally {
			IOUtils.closeSilently(in);
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 读取Http数据到目标目录
	 */
	private void transferData(InputStream in, OutputStream out) throws TransferException {
		final byte buffer[] = new byte[BUFFER_SIZE];
		while (true) {
			checkPausedOrCanceled();
			int len = readFromResponse(in, buffer);
			if (len == -1) {
				break;
			}
			try {
				out.write(buffer, 0, len);
				mInfo.currentBytes += len;
				if (mInfo.status != TransferStatus.PAUSE && mInfo.status != TransferStatus.CANCELED)
					updateProgress(mInfo);
			} catch (TransferException e1) {
				throw new TransferException(e1.getFinalStatus(), "TransferException");
			} catch (Exception e) {
				throw new TransferException(TransferStatus.HTTP_ERROR, e);
			}
		}

		// if (mInfo.totalBytes != -1 && mInfo.currentBytes != mInfo.totalBytes)
		// {
		// throw new TransferException(TransferStatus.HTTP_ERROR,
		// "Content length mismatch");
		// }
	}

	/**
	 * 读取数据
	 * 
	 * @param in
	 * @param buffer
	 * @return
	 * @throws TransferException
	 */
	private int readFromResponse(InputStream in, final byte[] buffer) throws TransferException {
		try {
			return in.read(buffer);
		} catch (IOException e) {
			throw new TransferException(TransferStatus.HTTP_ERROR, "Failed reading response: " + e, e);
		}
	}

	public static String normalizeMimeType(String type) {
		if (type == null) {
			return null;
		}
		type = type.trim().toLowerCase(Locale.ROOT);
		final int semicolonIndex = type.indexOf(';');
		if (semicolonIndex != -1) {
			type = type.substring(0, semicolonIndex);
		}
		return type;
	}

	/**
	 * 读取Header头信息
	 */
	private void parseOkHeaders(HttpURLConnection conn) throws TransferException {
		if (mInfo.storageDir == null) {
			try {
				mInfo.storageDir = TransferUtils.generateSaveFile(mInfo.url);
			} catch (IOException e) {
				throw new TransferException(TransferStatus.HTTP_ERROR, "Failed to generate storageDir: " + e);
			}
		}

		if (mInfo.filetype == null) {
			mInfo.filetype = normalizeMimeType(conn.getContentType());
		}

		final String transferEncoding = conn.getHeaderField("Transfer-Encoding");
		if (transferEncoding == null) {
			mInfo.totalBytes = getHeaderFieldLong(conn, "Content-Length", -1);
		} else {
			mInfo.totalBytes = -1;
		}

		mInfo.mETag = conn.getHeaderField("ETag");
		// writeToDatabaseOrThrow(info);
	}

	private static long getHeaderFieldLong(URLConnection conn, String field, long defaultValue) {
		try {
			return Long.parseLong(conn.getHeaderField(field));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 添加自定义Header信息
	 */
	private void addRequestHeaders(HttpURLConnection conn, boolean resuming) {
		for (Pair<String, String> header : mInfo.getHeaders()) {
			conn.addRequestProperty(header.first, header.second);
		}

		// Only splice in user agent when not already defined
		if (conn.getRequestProperty("User-Agent") == null) {
			conn.addRequestProperty("User-Agent", "Web-Android");
		}

		// 支持恢复部分下载
		conn.setRequestProperty("Accept-Encoding", "identity");
		conn.setRequestProperty("Connection", "close");

		if (resuming) {
			if (mInfo.ctag != null) {
				conn.addRequestProperty("If-Match", mInfo.ctag.toString());
			}
			conn.addRequestProperty("Range", "bytes=" + mInfo.currentBytes + "-");
		}
	}

	@Override
	protected void checkDownloadFile(TransferInfo info) throws TransferException {
		File f = new File(info.storageDir);
		if (f.exists()) {

		} else {
			// 判断目录是否存在
			File dir = new File(info.storageDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}
}
