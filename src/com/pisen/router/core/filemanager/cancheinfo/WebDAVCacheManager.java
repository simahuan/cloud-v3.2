package com.pisen.router.core.filemanager.cancheinfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.studio.util.URLUtils;
import android.util.Log;

import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceInfo.RSource;
import com.pisen.router.core.monitor.entity.RouterConfig;

import de.aflx.sardine.DavResource;
import de.aflx.sardine.Sardine;
import de.aflx.sardine.SardineFactory;

/**
 * 缓存管理类
 * 
 * @author yangyp
 * @version 1.0, 2014-7-18 下午1:13:18
 */
public class WebDAVCacheManager implements IResourceCache {

	public interface OnCacheChangeListener {
		void onWebDAVCacheCompleted(IResourceCache cache);
	}

	// 主线程服务
	private CacheThreadQueue queueThread;
	private RouterConfig routerConfig;
	private boolean cacheCompleted = false;
	private Map<String, ResourceInfo> resourceMap = new HashMap<String, ResourceInfo>();
	private OnCacheChangeListener changeCallback;

	public WebDAVCacheManager() {
		cacheCompleted = false;
	}

	public void setRouterConfig(RouterConfig routerConfig) {
		this.routerConfig = routerConfig;
	}

	@Override
	public RouterConfig getRouterConfig() {
		return routerConfig;
	}

	@Override
	public boolean isCacheCompleted() {
		return cacheCompleted;
	}

	@Override
	public void setOnChangeListener(OnCacheChangeListener listener) {
		this.changeCallback = listener;
	}

	public void setCacheCompleted(boolean cacheCompleted) {
		this.cacheCompleted = cacheCompleted;
		if (changeCallback != null) {
			changeCallback.onWebDAVCacheCompleted(this);
		}
	}

	@Override
	public List<ResourceInfo> getResourceAll() {
		return new ArrayList<ResourceInfo>(resourceMap.values());
	}

	/**
	 * 启动刷新
	 * 
	 * @param callback
	 */
	public void startCacheThread() {
		stopCacheThread();
		removeAllCache();
		queueThread = new CacheThreadQueue(this, routerConfig);
		queueThread.start();
	}

	/**
	 * 停止刷新
	 */
	public void stopCacheThread() {
		if (queueThread != null) {
			queueThread.cancel();
		}
	}

	/**
	 * 销毁
	 */
	public void onDestroy() {
		stopCacheThread();
		removeAllCache();
	}

	@Override
	public List<ResourceInfo> list(String webdavurl) {
		ResourceInfo resource = resourceMap.get(webdavurl);
		if (resource == null) {
			return Collections.emptyList();
		}

		List<ResourceInfo> results = new ArrayList<ResourceInfo>();
		if (resource.isDirectory) {
			for (Map.Entry<String, ResourceInfo> entry : resourceMap.entrySet()) {
				String parentURL = URLUtils.getParentURI(entry.getKey());
				if (webdavurl.equals(parentURL)) {
					results.add(entry.getValue());
				}
			}
		} else {
			results.add(resource);
		}

		return results;
	}

	@Override
	public List<ResourceInfo> listRecursively(String webdavurl, ResourceFilter filter) {
		List<ResourceInfo> results = new ArrayList<ResourceInfo>();
		for (Map.Entry<String, ResourceInfo> entry : resourceMap.entrySet()) {
			String key = entry.getKey();
			if (webdavurl == null || key.startsWith(webdavurl)) {
				ResourceInfo resource = entry.getValue();
				if (!resource.isDirectory) {
					if (filter.accept(resource)) {
						results.add(resource);
					}
				}
			}
		}
		return results;
	}

	@Override
	public void addResourceCache(String webdavurl) {
		try {
			Sardine sardine = SardineFactory.begin(routerConfig.getWebdavUsername(), routerConfig.getWebdavPassword());
			final List<DavResource> resources = sardine.list(webdavurl);
			DavResource resource = resources.get(0);
			addResourceCache(webdavurl, resource);
		} catch (Exception e) {
		}
	}

	/**
	 * 添加到缓存
	 * 
	 * @param resource
	 */
	@Override
	public void addResourceCache(String webdavurl, DavResource resource) {
		ResourceInfo info = new ResourceInfo(RSource.Remote);
		info.isDirectory = resource.isDirectory();
		info.path = webdavurl;
		info.name = resource.getName();
		info.size = resource.getContentLength();
		info.lastModified = updateResourceLastModified(resource); // resource.getModified().getTime();

		addResourceCache(webdavurl, info);
	}

	private long updateResourceLastModified(DavResource resource) {
		try {
			String fileName = resource.getName();
			// 只有即拍即传的图片或录音的音频进度日期转化，其它文件不做处理，文件格式为"Pic_201408191555222.jpg/Vid_201408191555222.mp4"
			String regExp = "(Pic_([0-9]{14}|[0-9]{17}).jpg)|(Vid_([0-9]{14}|[0-9]{17}).mp4)";
			Pattern pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
			Matcher match = pattern.matcher(fileName);
			if (match.matches()) {
				String[] names = fileName.split("(Pic_|Vid_)|(.jpg|.mp4)");
				if (names.length == 2) {
					String dataString = names[1];
					String dPattern = dataString.length() == 14 ? "yyyyMMddHHmmss" : "yyyyMMddHHmmssSSS";
					SimpleDateFormat dateTimeFormat = new SimpleDateFormat(dPattern, Locale.getDefault());
					Date date = dateTimeFormat.parse(dataString);
					return date.getTime();
				}
			}
		} catch (ParseException e) {
		}
		return resource.getModified() == null ?  0 : resource.getModified().getTime();
	}

	@Override
	public void addResourceCache(String key, ResourceInfo resource) {
		// Log.i("addResourceCache", "resourceCache: " + resource.path);
		synchronized (resourceMap) {
			resourceMap.put(key, resource);
		}
	}

	/**
	 * 更新缓存
	 * 
	 * @param oldURL
	 * @param newURL
	 */
	@Override
	public void updateCache(String oldURL, String newName) {
		synchronized (resourceMap) {
			ResourceInfo resource = resourceMap.get(oldURL);
			if (resource != null) {
				String newURL = URLUtils.getParentURI(oldURL) + newName + (resource.isDirectory ? "/" : "");
				resource.path = newURL;
				resource.name = newName;
				addResourceCache(newURL, resource);
				removeCache(oldURL);

				// 更新子目录路径
				if (resource.isDirectory) {
					for (Map.Entry<String, ResourceInfo> entry : resourceMap.entrySet()) {
						ResourceInfo _resource = entry.getValue();
						if (_resource.path.startsWith(oldURL)) {
							Log.i("updateCache", "resourceCache: " + _resource.path);
							String path = resource.path;
							String subPath = newURL + path.substring(path.indexOf(oldURL) + oldURL.length());
							_resource.path = subPath;
							addResourceCache(subPath, resource);
							resourceMap.remove(entry.getKey());
						}
					}
				}
			}
		}
	}

	/**
	 * 清除指定缓存
	 * 
	 * @param webdavurl
	 */
	@Override
	public void removeCache(String webdavurl) {
		synchronized (resourceMap) {
			ResourceInfo resource = resourceMap.remove(webdavurl);
			if(resource != null) {
				Log.i("addResourceCache", "resourceCache: " + resource.path);
	
				// 删除子目录路径
				if (resource.isDirectory) {
					String[] entrySet = resourceMap.keySet().toArray(new String[resourceMap.entrySet().size()]);
					for (String key : entrySet) {
						ResourceInfo _resource = resourceMap.get(key);
						if (_resource.path.startsWith(webdavurl)) {
							Log.i("addResourceCache", "resourceCache: " + _resource.path);
							resourceMap.remove(key);
						}
					}
				}
			}
		}
	}

	/**
	 * 清除所有缓存
	 */
	@Override
	public void removeAllCache() {
		synchronized (resourceMap) {
			resourceMap.clear();
		}
	}

}
