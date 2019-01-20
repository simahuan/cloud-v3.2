package com.pisen.router.ui.phone.settings.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * http网络请求管理类
 * @author MouJunFeng
 * @time 2014-5-12上午9:29:00
 */
public class HttpJsonRequest extends HttpManager{


	public HttpJsonRequest(Context ctx){
		super(ctx);
	}
	/**
	 * 
	 * Http post请求
	 * @param url  地址
	 * @param params 参数
	 * @param result void
	 */
	public void execute(String url, Map<String, String> map , OnHttpCallBack result){
		String str = "";
		if(map != null && !map.isEmpty()){
			for(Entry<String, String> entry : map.entrySet()){
				str += entry.getKey()+"="+entry.getValue()+"&";
			}
		}
		str = str.substring(0, str.length()-1);
		execute(new String[]{"温馨提示","数据加载中，请稍候！"}, url, str, result);
	}
	/**
	 * 
	 * Http post请求
	 * @param url  地址
	 * @param params 参数
	 * @param result void
	 */
	public void execute(String url, String params , OnHttpCallBack result){
		execute(new String[]{"温馨提示","数据加载中，请稍候！"}, url, params, result);
	}
	/**
	 * 
	 * Http post请求
	 * @param dialogs 提示框信息
	 * @param url  地址
	 * @param params 参数
	 * @param result void
	 */
	public void execute(String[] dialogs ,String url, String params , OnHttpCallBack result) {
		this.setResult(result);
		showDialog(dialogs[0] , dialogs[1]);
		new MyAsyncTask(params).execute(url);
	}

	/**
	 * 
	 * Http 异步加载
	 * @author MouJunFeng
	 * @version 1.0 2014-5-26 上午10:17:13
	 */
	private class MyAsyncTask extends AsyncTask<String, Integer, String> {
		private String param;

		public MyAsyncTask(String param) {
			this.param = param;
		}

		@Override
		protected String doInBackground(String... params) {	
			String result = "";
			try {
				// 实现将请求 的参数封装封装到HttpEntity中。
				StringEntity entity = new StringEntity(param, "UTF-8");
				// 使用HttpPost请求方式
				HttpPost httpPost = new HttpPost(params[0]);
				httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
				// 设置请求参数到Form中。
				httpPost.setEntity(entity);
				// 实例化一个默认的Http客户端
				HttpClient client = new DefaultHttpClient();
				// 设置连接超时时间为3s
				HttpConnectionParams.setConnectionTimeout(client.getParams(),
						CONNECT_TIME_OUT);
				// 设置读取超时为5s
				HttpConnectionParams.setSoTimeout(client.getParams(),
						READ_TIME_OUT);
				// 执行请求，并获得响应数据
				HttpResponse httpResponse = client.execute(httpPost);
				// 判断是否请求成功，为200时表示成功，其他均问有问题。
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// 通过HttpEntity获得返回数据
					result = EntityUtils.toString(httpResponse
							.getEntity());
					return result;
				}
				return result;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return result;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return result;
			} catch (IOException e) {
				e.printStackTrace();
				return result;
			} catch(Exception e){
				e.printStackTrace();
				return result;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			closeDialog();
			HttpJsonRequest.this.getResult().getHttpResult(result);
		}
	}
	
}
