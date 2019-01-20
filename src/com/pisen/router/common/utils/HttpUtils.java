package com.pisen.router.common.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.studio.os.LogCat;
import android.text.TextUtils;

import com.pisen.router.CloudApplication;

/**
 * URLConnection 网络请求,post&get (适用于区分业务逻辑,在异步方法中调用即可)
 * 
 * @author Liuhc
 * @version 1.0 2015年5月26日 上午10:11:53
 */
public class HttpUtils {

	/**
	 * 向服务器发送get请求
	 * 
	 * @param urlAddress
	 *            url地址
	 * @return
	 */
	public static String get(String urlAddress) {
		HttpURLConnection urlConn = null;
		StringBuffer buffer = new StringBuffer();
		try {
			String ipAddress = NetUtil.getGetwayIPAddress(CloudApplication.getInstance());
			if (TextUtils.isEmpty(ipAddress)) {
				LogCat.e("<ZX>error:ipAddress null");
				return "";
			}

			urlAddress = String.format(urlAddress, ipAddress);
			LogCat.i("请求:" + urlAddress);
			URL url = new URL(urlAddress);
			/*
			 * 【1】 获取HttpURLConnection的对象。通过调用URL.openConnection()，
			 * 并将类型适配为HttpURLConnection类型。
			 * 如果是处理https，则使用HttpsURLConnecction，相关的代码参考：
			 * http://developer.android
			 * .com/reference/javax/net/ssl/HttpsURLConnection.html
			 */
			urlConn = (HttpURLConnection) url.openConnection();

			/* 【2】 处理request的header，设置超时属性 。 */
			urlConn.setRequestMethod("GET");
			urlConn.setRequestProperty("encoding", "UTF-8");
			urlConn.setConnectTimeout(10000);
			urlConn.setReadTimeout(30000);
			urlConn.setUseCaches(false);
			/* 【3】 处理request的body。HTTP Get 没有body，相关的在HTTP POST中演示 */
			/* 【4】读取response。 */
			int responseCode = urlConn.getResponseCode();
			LogCat.d("Response code = " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// 读取body
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null) {
					// LogCat.i("data:"+line);
					buffer.append(line);
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (urlConn != null)
				urlConn.disconnect();
		}
		LogCat.i("返回:" + buffer.toString());
		return buffer.toString();
	}

	/**
	 * 向服务器发送post请求
	 * @param urlAddress
	 *            url地址
	 * @param jsonParams
	 *            标准格式json字符串
	 * @return
	 */
	public static String post(String urlAddress, byte[] jsonParams) {
		HttpURLConnection urlConn = null;
		StringBuffer buffer = new StringBuffer();
		DataOutputStream out = null;
		try {
			URL url = new URL(urlAddress);
			urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			urlConn.setRequestMethod("POST");
			urlConn.setConnectTimeout(20000);
			urlConn.setReadTimeout(20000);
			urlConn.setRequestProperty("Connection", "Keep-Alive");
			urlConn.setRequestProperty("Charset", "UTF-8");  
			urlConn.setRequestProperty("Content-Length", String.valueOf(jsonParams.length));  
			urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
			out = new DataOutputStream(urlConn.getOutputStream());
			out.write(jsonParams);
			out.flush();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			int responseCode = urlConn.getResponseCode();
			LogCat.d("Response code = " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null) {
					buffer.append(line);
				}
				out.close();
				in.close();
			}
		} catch (IOException e) {
			buffer.append("true");
			e.printStackTrace();
		}finally {
			if (urlConn != null)
				urlConn.disconnect();
		}
		return buffer.toString();
	}

	/**
	 * post方式，完成文件的上传
	 */
	private static void uploadFile() {
		HttpURLConnection conn = null;
		OutputStream out = null;
		InputStream in = null;
		FileInputStream fin = null;
		String filePath = "c:\\android帮助文档.rar";
		try {
			fin = new FileInputStream(filePath);
			// 1.得到HttpURLConnection实例化对象
			conn = (HttpURLConnection) new URL("http://127.0.0.1:8080/Day18/servlet/UploadTest").openConnection();
			// 2.设置请求方式
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			// 不使用缓存
			conn.setUseCaches(false);
			// conn.setRequestProperty("Range",
			// "bytes="+start+"-"+end);多线程请求部分数据
			// 3.设置请求头属性
			// 上传文件的类型 rard Mime-type为application/x-rar-compressed
			conn.setRequestProperty("content-type", "application/x-rar-compressed");
			/*
			 * (1).在已知文件大小，需要上传大文件时，应该设置下面的属性，即文件长度
			 * 当文件较小时，可以设置头信息即conn.setRequestProperty("content-length",
			 * "文件字节长度大小"); (2).在文件大小不可知时，使用setChunkedStreamingMode();
			 */
			conn.setFixedLengthStreamingMode(fin.available());
			String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
			// 可以将文件名称信息已头文件方式发送，在servlet中可以使用request.getHeader("filename")读取
			conn.setRequestProperty("filename", fileName);

			// 4.向服务器中发送数据
			out = new BufferedOutputStream(conn.getOutputStream());
			long totalSize = fin.available();
			long currentSize = 0;
			int len = -1;
			byte[] bytes = new byte[1024 * 5];
			while ((len = fin.read(bytes)) != -1) {
				out.write(bytes);
				currentSize += len;
				System.out.println("已经长传:" + (int) (currentSize * 100 / (float) totalSize) + "%");
			}

			System.out.println("上传成功！");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 5.释放相应的资源
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * 
	 */
	public static String get2(String urlAddress) {
		String response = "";
		try {
			HttpGet httpGet = new HttpGet(urlAddress);
			HttpResponse ht = new DefaultHttpClient().execute(httpGet);
			if (ht.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream is = ht.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));

				String readLine = null;
				while ((readLine = br.readLine()) != null) {
					response = response + readLine;
				}
				is.close();
				br.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LogCat.i("返回:" + response);
		return response;
	}

	public static String post2(String url, String param) {
		String response = "";
		try {
			HttpPost request = new HttpPost(url);  
			// 先封装一个 JSON 对象  
//			JSONObject param = new JSONObject();
//			param.put("name", "rarnu");
//			param.put("password", "123456");
			// 绑定到请求 Entry  
			StringEntity se = new StringEntity(param);   
			request.setEntity(se);  
			// 发送请求  
			HttpResponse httpResponse = new DefaultHttpClient().execute(request);  
			// 得到应答的字符串，这也是一个 JSON 格式保存的数据  
			String retSrc = EntityUtils.toString(httpResponse.getEntity());  
			response = new JSONObject( retSrc).toString();  
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		LogCat.i("返回:" + response);
		return response;
	}
	
	public static String doPost(String urlAddress, List<NameValuePair> list) {
		String response = "";
		try {
			String ipAddress = NetUtil.getGetwayIPAddress(CloudApplication.getInstance());
			if (TextUtils.isEmpty(ipAddress)) {
				LogCat.e("<ZX>error:ipAddress null");
				return "";
			}

			urlAddress = String.format(urlAddress, ipAddress);
			HttpPost httpPost = new HttpPost(urlAddress);
			httpPost.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));
			HttpClient hc = new DefaultHttpClient();
			HttpResponse ht = hc.execute(httpPost);
			if (ht.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream is = ht.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));

				String readLine = null;
				while ((readLine = br.readLine()) != null) {
					response = response + readLine;
				}
				is.close();
				br.close();

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LogCat.i("返回:" + response);
		return response;
	}
}
