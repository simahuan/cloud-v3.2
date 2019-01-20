package com.pisen.router.core.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 文件下载工具类
 */
public class DownloadUtil {
	final public static int DownloadBegin = 0; // 下载文件保存目录
	final public static int Downloading = 1; // 下载文件保存目录
	final public static int DownloadEnd = 2; // 下载文件保存目录

	final public static String SAVE_DIR = "/tmpFile"; // 下载文件保存目录
	private String saveDir; // 保存路径

	public DownloadUtil() {
		this.saveDir = SAVE_DIR;
	}

	public DownloadUtil(String saveDir) {
		this.saveDir = saveDir;
	}

	/**
	 * 下载文件
	 * 
	 * @param urlString
	 *            url
	 * @param fileName
	 *            保存的文件名
	 * @param handler
	 *            handler (跟新进度)
	 */
	public void downloadFile(String urlString, String fileName, Handler handler) {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		HttpURLConnection connection = null;
		int downedFileLength = 0;
		/* 连接 */
		try {
			URL url = new URL(urlString);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(5000);
			// if (connection.getReadTimeout() == 5) {
			// Log.i("---------->", "当前网络有问题");
			// // return;
			// }
			inputStream = connection.getInputStream();

		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * 设置文件的保存路径和和文件名，如果不存在则新建
		 */
		String savePAth = Environment.getExternalStorageDirectory() + saveDir;
		File file1 = new File(savePAth);
		if (!file1.exists()) {
			file1.mkdir();
		}
		String savePathString = Environment.getExternalStorageDirectory() + saveDir + "/" + fileName;
		File file = new File(savePathString);
		file.deleteOnExit();
		if (!file.exists()) {//文件不存在
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*
		 * 写入文件,用Handle传递进度
		 */
		Message message = new Message();
		try {
			outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[1024*4];
			int fileLength = connection.getContentLength();
			message.what = DownloadBegin;
			message.obj = fileLength;
			handler.sendMessage(message);
//			while (downedFileLength < fileLength) {
//				outputStream.write(buffer);
//				downedFileLength += inputStream.read(buffer);
//				Log.i("-------->", downedFileLength + "");
//				Message message1 = new Message();
//				message1.what = Downloading;
//				message1.obj = downedFileLength;
//				handler.sendMessage(message1);
//			}
			int len;
			// 开始下载   
	         while ((len = inputStream.read(buffer)) != -1) {   
	        	 outputStream.write(buffer, 0, len);  
	        	 Message message1 = new Message();
					message1.what = Downloading;
					downedFileLength +=len;
					message1.obj = downedFileLength;
					handler.sendMessage(message1);
	         }  
			Message message2 = new Message();
			message2.what = DownloadEnd;
			handler.sendMessage(message2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
