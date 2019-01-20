package com.pisen.router.core.filemanager.cancheinfo;

import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.pisen.router.core.filemanager.cancheinfo.WebdavCacheService.LocalBinder;

public class WebdavCacheServiceUtils {

	private static final String TAG = "WebdavCacheServiceUtils";
	private static WebDAVCacheManager cacheManager;
	private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

	public static class ServiceToken {
		ContextWrapper mWrappedContext;

		ServiceToken(ContextWrapper context) {
			mWrappedContext = context;
		}
	}

	static private class ServiceBinder implements ServiceConnection {
		ServiceConnection mCallback;

		public ServiceBinder(ServiceConnection callback) {
			mCallback = callback;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			WebdavCacheService cacheService = ((LocalBinder) service).getService();
			cacheManager = cacheService.getCacheManager();
			if (mCallback != null) {
				mCallback.onServiceConnected(name, service);
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			if (mCallback != null) {
				mCallback.onServiceDisconnected(name);
			}
			cacheManager = null;
		}
	}

	/**
	 * 绑定服务
	 * 
	 * @param context
	 * @return
	 */
	public static ServiceToken bindToService(Context context, ServiceConnection callback) {
		ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
		cw.startService(new Intent(context, WebdavCacheService.class));
		ServiceBinder sb = new ServiceBinder(callback);
		if (context.bindService(new Intent(context, WebdavCacheService.class), sb, Context.BIND_AUTO_CREATE)) {
			sConnectionMap.put(cw, sb);
			return new ServiceToken(cw);
		}
		return null;
	}

	public static WebDAVCacheManager getCacheManager() {
		return cacheManager;
	}

	/**
	 * 解除服务
	 * 
	 * @param token
	 */
	public static void unbindFromService(ServiceToken token) {
		ContextWrapper cw = token.mWrappedContext;
		ServiceBinder sb = sConnectionMap.remove(cw);
		if (sb == null) {
			return;
		}
		cw.unbindService(sb);
		if (sConnectionMap.isEmpty()) {
			cw.stopService(new Intent(cw, WebdavCacheService.class));
			cacheManager = null;
		}
	}

}
