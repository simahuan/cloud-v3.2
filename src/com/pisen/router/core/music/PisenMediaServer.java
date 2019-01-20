package com.pisen.router.core.music;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.HTTPStatus;
import org.cybergarage.upnp.std.av.server.MediaServer;

import android.text.TextUtils;
import android.util.Log;

import com.pisen.router.BuildConfig;

/**
 * 媒体服务器
 * @author ldj
 * @version 1.0 2015年4月30日 下午3:07:46
 */
public class PisenMediaServer extends MediaServer {
	private static final String TAG = PisenMediaServer.class.getSimpleName();
	public static final String PRE_PATH = "/Pisen/";
	
	@Override
	public void httpRequestRecieved(HTTPRequest httpReq) {
		String uri = httpReq.getURI();
		if(uri.startsWith(PRE_PATH)) {
			try {
				File file = new File(MusicPlaybackUtil.decodeString(uri.substring(PRE_PATH.length())));
				if(BuildConfig.DEBUG) Log.d(TAG, "http request file->" + file.getAbsolutePath());
				if(!file.exists()) {
					if(BuildConfig.DEBUG) Log.e(TAG, "file is not exist!!! ");
					httpReq.returnBadRequest();
					return;
				}
				
				httpSend(httpReq, file);
			}
			catch (Exception e) {
				e.printStackTrace();
			} finally {
				
			}
		}else {
			super.httpRequestRecieved(httpReq);
		}
	}
	
	/**
	 * 断点续传media文件
	 * @param httpReq
	 * @param file
	 */
	private void httpSend(HTTPRequest httpReq, File file) {
		String range = httpReq.getHeaderValue("Range");
		if(!TextUtils.isEmpty(range)) {
			FileInputStream fis;
			try {
				/*获取断点开始位置*/
				int start=0;
				String[] values =range.split("=")[1].split("-");  
				start = Integer.parseInt(values[0]);  
				
				fis = new FileInputStream(file);
				long total = file.length() ;
				HTTPResponse httpRes = new HTTPResponse();
				httpRes.setContentType("audio/*");
				httpRes.setStatusCode(HTTPStatus.OK);
				httpRes.setContentLength(total);
				httpRes.setContentRange(start, total -1, total - start);
				httpRes.setContentInputStream(fis);
				
				httpReq.post(httpRes);
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			httpReq.returnBadRequest();
		}
	}
}
