package com.pisen.router.core.filemanager.cancheinfo;

import java.util.Map;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Binder;
import android.os.IBinder;
import android.studio.os.NetUtils;
import android.studio.service.WifiService;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestManager;
import com.android.volley.RequestParams;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.SimpleXmlRequest;
import com.pisen.router.core.monitor.entity.RouterConfig;

/**
 * WebDAV缓存服务
 * 
 * @author yangyp
 * @version 1.0, 2014-7-14 上午11:33:12
 */
public class WebdavCacheService extends WifiService {

	static final String TAG = "WebdavCacheService";
	public static final String SysInfo = "http://%s/cgi-bin/SysInfo";

	private WebDAVCacheManager cacheManager;
	private boolean initRouterConfig = false;

	public WebDAVCacheManager getCacheManager() {
		return cacheManager;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "服务 onCreate");
		cacheManager = new WebDAVCacheManager();
		initRouterConfig();
	}

	/**
	 * 初始化路由配置信息
	 */
	private void initRouterConfig() {
		if (!initRouterConfig) {
			initRouterConfig = true;
			String gateway = NetUtils.getGateway(this);
			String configURL = String.format(SysInfo, gateway);
			RequestManager.sendRequest(new SimpleXmlRequest<RouterConfig>(Method.POST, configURL, RouterConfig.class) {
				@Override
				protected Map<String, String> getParams() throws AuthFailureError {
					return new RequestParams("data", "<getSysInfo><Storage/></getSysInfo>");
				}

				@Override
				public void onResponse(RouterConfig info) {
					cacheManager.setRouterConfig(info);
					cacheManager.startCacheThread();
					initRouterConfig = false;
				}

				@Override
				public void onErrorResponse(VolleyError volleyerror) {
					super.onErrorResponse(volleyerror);
					initRouterConfig = false;
				}

			}, this);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		public WebdavCacheService getService() {
			return WebdavCacheService.this;
		}
	}

	@Override
	public void onWifiConnected(WifiInfo info) {
		cacheManager.stopCacheThread();
		initRouterConfig();
	}

	@Override
	public void onWifiDisconnected(WifiInfo info) {
		cacheManager.stopCacheThread();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "服务 onDestroy");
		RequestManager.cancelAll(this);
		cacheManager.onDestroy();
		super.onDestroy();
	}

}
