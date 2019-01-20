package com.pisen.router.ui.phone.welcome;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.content.Intent;
import android.studio.os.LogCat;
import android.studio.os.PreferencesUtils;
import android.studio.util.DigestUtils;

import com.google.gson.GsonUtils;
import com.pisen.router.CloudApplication;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.config.HttpKeys;

/**
 * 启动页图片下载服务
 * <p>
 * 杨元平2014-07-03: 修改了服务方式为IntentService
 * </p>
 * 
 * @author MouJunFeng
 * @version 1.0 2014-5-27 下午1:58:31
 */
public class DownLoadImageService extends IntentService {
	public static final int BUFFER_SIZE = 4 * 1024;
	private HttpClient client;

	public DownLoadImageService() {
		super("启动下载界面服务");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		client = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(HttpKeys.START_BANNER_URL);
		try {
			ResponseHandler<String> responseHandler = new StringResponseHandler();
			String responseBody = client.execute(getMethod, responseHandler);
			AdvertisesJsonResult jsonResult = GsonUtils.jsonDeserializer(responseBody, AdvertisesJsonResult.class);
			if (jsonResult != null && !jsonResult.isDataNull()) {
				downLoadImage(jsonResult.Advertises);
				}
			} catch (Exception e) {
			LogCat.e("onHandleIntent:%s\n ", e.toString());
		}
	}

	@Override
	public void onDestroy() {
		client.getConnectionManager().shutdown();
		super.onDestroy();
	}

	/**
	 * 检查返回HTTP Response的返回值，如果是3xx-6xx，不是2xx，则说明出错，例如404，Not Found。
	 * 
	 * @author yangyp
	 * @version 1.0, 2014-7-3 下午2:15:39
	 */
	private class StringResponseHandler implements ResponseHandler<String> {
		@Override
		public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() >= 300) {
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			if (entity == null) {
				return null;
			}

			return EntityUtils.toString(entity);
		}
	}

	/**
	 * 下载图片
	 * 
	 * @param advertiseList
	 *            广告集合
	 */
	public void downLoadImage(List<Advertise> advertiseList) {
		if (!advertiseList.isEmpty()) {
			File file = writeSD(advertiseList.get(0).ImageUrl);
			if (file != null) {
				PreferencesUtils.setString(KeyUtils.APP_START_IMAGE, file.getPath());
			}
		}
	}

	/**
	 * 将图片输入SD卡
	 * 
	 * @param url
	 *            图片网络路径
	 * @return 返回文件实体
	 */
	private File writeSD(String url) {
		String name = DigestUtils.MD5(url);
		File file = new File(CloudApplication.LOGO_PATH, name);
		if (file.exists()) {
			return file;
		}
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream in = conn.getInputStream();
			OutputStream out = new FileOutputStream(file);

			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.flush();
			out.close();
			in.close();
			return file;
		} catch (Exception e) {
			LogCat.e("writeSD: %s\n", e.toString());
			return null;
		}
	}
}
