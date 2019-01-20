package com.android.volley.extend;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;

import com.android.volley.extend.RequestManager.RequestListener;
import com.android.volley.toolbox.ImageLoader;

public class VolleyManager {

	private static int actionId = 0x5234;
	private SparseArray<HttpIDRequestLisenter> idListeners = new SparseArray<VolleyManager.HttpIDRequestLisenter>();
	private SparseArray<HttpRequestLisenter> listeners = new SparseArray<VolleyManager.HttpRequestLisenter>();

	private static VolleyManager instacne;
	private static ImageLoader mImageLoader;
	private static final String TAG = "Volley";
	/**
	 * should be invoked while application onCreate
	 * 
	 * @param context
	 */

	public static void init(Context context) {
		RequestManager.getInstance().init(context);
	}

	private VolleyManager() {
	}

	public static VolleyManager getInstance() {
		if (instacne == null) {
			synchronized (VolleyManager.class) {
				if (instacne == null) {
					instacne = new VolleyManager();
				}
			}
		}
		return instacne;
	}

	private RequestListener requestListener = new RequestListener() {

		@Override
		public void onRequest() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSuccess(String response, String url, int actionId) {
			Log.d(TAG, "onSuccess response = "+response);
			synchronized (idListeners) {
				HttpIDRequestLisenter httpRequestLisenter = idListeners.get(actionId);
				if (httpRequestLisenter != null) {
					httpRequestLisenter.onHttpFinished(true, response, actionId);
					idListeners.remove(actionId);
				}
			}

			synchronized (listeners) {
				HttpRequestLisenter httpRequestLisenter = listeners.get(actionId);
				if (httpRequestLisenter != null) {
					httpRequestLisenter.onHttpFinished(true, response);
					idListeners.remove(actionId);
				}
			}

		}

		@Override
		public void onError(String errorMsg, String url, int actionId) {
			Log.d(TAG, "onError errorMsg = "+errorMsg);
			synchronized (idListeners) {
				HttpIDRequestLisenter httpRequestLisenter = idListeners.get(actionId);
				if (httpRequestLisenter != null) {
					httpRequestLisenter.onHttpFinished(false, errorMsg, actionId);
					idListeners.remove(actionId);
				}
			}
			synchronized (listeners) {
				HttpRequestLisenter httpRequestLisenter = listeners.get(actionId);
				if (httpRequestLisenter != null) {
					httpRequestLisenter.onHttpFinished(false, errorMsg);
					idListeners.remove(actionId);
				}
			}
		}

	};

	public LoadControler get(String url, HttpIDRequestLisenter httpRequestListenner, int actionId) {
		synchronized (idListeners) {
			idListeners.put(actionId, httpRequestListenner);
		}
		return RequestManager.getInstance().get(url, requestListener, actionId);
	}

	public LoadControler get(String url, Object data, HttpIDRequestLisenter httpRequestListenner, int actionId) {
		synchronized (idListeners) {
			idListeners.put(actionId, httpRequestListenner);
		}
		return RequestManager.getInstance().get(url, data, requestListener, actionId);
	}

	public LoadControler post(final String url, Object data, HttpIDRequestLisenter httpRequestListenner, int actionId) {
		synchronized (idListeners) {
			idListeners.put(actionId, httpRequestListenner);
		}
		return RequestManager.getInstance().post(url, data, requestListener, actionId);
	}

	public LoadControler get(String url, HttpRequestLisenter httpRequestListenner) {
		synchronized (listeners) {
			actionId++;
			listeners.put(actionId, httpRequestListenner);
		}
		return RequestManager.getInstance().get(url, requestListener, actionId);
	}

	public LoadControler get(String url, Object data, HttpRequestLisenter httpRequestListenner) {
		synchronized (listeners) {
			actionId++;
			listeners.put(actionId, httpRequestListenner);
		}
		return RequestManager.getInstance().get(url, data, requestListener, actionId);
	}

	public LoadControler post(final String url, Object data, HttpRequestLisenter httpRequestListenner) {
		synchronized (listeners) {
			actionId++;
			listeners.put(actionId, httpRequestListenner);
		}
		return RequestManager.getInstance().post(url, data, requestListener, actionId);
	}

	public static ImageLoader.ImageContainer imageLoader(ImageView view, String url, int defaultImageResId) {
		return imageLoader(view, url, defaultImageResId, defaultImageResId);
	}

	public static ImageLoader.ImageContainer imageLoader(ImageView view, String url, int defaultImageResId, int errorImageResId) {
		return imageLoader(view, url, defaultImageResId, errorImageResId, 0, 0);
	}

	public static ImageLoader.ImageContainer imageLoader(ImageView view, String url, int defaultImageResId, int errorImageResId, int maxWidth, int maxHeight) {
		ImageLoader.ImageListener listener = ImageLoader.getImageListener(view, defaultImageResId, errorImageResId);
		return imageLoader(url, listener, maxWidth, maxHeight);
	}

	public static ImageLoader.ImageContainer imageLoader(String url, ImageLoader.ImageListener listener) {
		return imageLoader(url, listener, 0, 0);
	}

	public static ImageLoader.ImageContainer imageLoader(String url, ImageLoader.ImageListener listener, int maxWidth, int maxHeight) {
		return mImageLoader.get(url, listener, maxWidth, maxHeight);
	}

	public static interface HttpRequestLisenter {
		public void onHttpFinished(boolean success, String response);
	}

	public static interface HttpIDRequestLisenter {
		public void onHttpFinished(boolean success, String response, int actionId);
	}

}
