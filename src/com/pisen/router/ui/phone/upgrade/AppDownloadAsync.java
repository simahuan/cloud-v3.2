package com.pisen.router.ui.phone.upgrade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

/**
 * App下载逻辑
 * 
 * @author tanyixiu
 * @version 3.3, 2015年10月13日 上午11:25:20
 */
public class AppDownloadAsync extends AsyncTask<Void, Integer, Integer> {

	private static final int DOWN_FINISH = 0;
	private static final int DOWN_FAIL = 1;
	private static final String APK_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "PisenCloud";

	private boolean intercept = false;
	private String apkUrl = "";
	private String fileName = "";
	private AppDownloadListener listener;

	public interface AppDownloadListener {
		void onDownloading(int downloadedSize, int totalsize);

		void onDownloadFinish(String fileName);

		void onDownloadFail();
	}

	public AppDownloadAsync(String url, String version) {
		 this.apkUrl = url;
		this.fileName = APK_PATH + File.separator + "PisenCloudAndriod" + version + ".apk";
	}

	public void setAppDownloadListener(AppDownloadListener listener) {
		this.listener = listener;
	}

	/**
	 * 终止下载
	 */
	public void setIntercept() {
		intercept = true;
	}

	/**
	 * apk安装包是否已经存在
	 * 
	 * @return
	 */
	public boolean isApkExist() {
		File apkfile = new File(fileName);
		if (!apkfile.exists()) {
			return false;
		}
		if (!apkfile.isFile()) {
			return false;
		}
		return true;
	}

	/**
	 * 获取安装包路径名+文件名
	 * 
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 删除安装包
	 */
	public void deleteApkFile() {
		File apkfile = new File(fileName);
		if (!apkfile.exists()) {
			return;
		}
		if (!apkfile.isFile()) {
			return;
		}

		String tmpPath = apkfile.getParent() + File.separator + System.currentTimeMillis();
		File tmp = new File(tmpPath);
		apkfile.renameTo(tmp);
		tmp.delete();
	}

	@Override
	protected Integer doInBackground(Void... params) {
		if (TextUtils.isEmpty(apkUrl)) {
			return DOWN_FAIL;
		}
		createDirectory();

		try {
			downloading();
			return DOWN_FINISH;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return DOWN_FAIL;
		} catch (IOException e) {
			e.printStackTrace();
			return DOWN_FAIL;
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (null != listener) {
			listener.onDownloading(values[0], values[1]);
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		switch (result) {
		case DOWN_FAIL:
			deleteApkFile();
			if (null != listener) {
				listener.onDownloadFail();
			}
			break;
		case DOWN_FINISH:
			if (null != listener) {
				listener.onDownloadFinish(fileName);
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		deleteApkFile();
	}

	@Override
	protected void onCancelled(Integer result) {
		super.onCancelled(result);
		deleteApkFile();
	}

	/**
	 * 下载逻辑
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void downloading() throws MalformedURLException, IOException {
		URL url = new URL(apkUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.connect();
		int length = conn.getContentLength();
		InputStream is = conn.getInputStream();
		File apkFile = new File(fileName);
		FileOutputStream fos = new FileOutputStream(apkFile);
		int numread;
		int downloadedSize = 0;
		byte buf[] = new byte[1024];

		do {
			numread = is.read(buf);
			downloadedSize += numread;
			onProgressUpdate(downloadedSize, length);
			if (numread <= 0) {
				break;
			}
			fos.write(buf, 0, numread);
		} while (!intercept);

		fos.close();
		is.close();
	}

	/**
	 * 创建安装包目录
	 */
	private void createDirectory() {
		File fileDir = new File(APK_PATH);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
	}
}
