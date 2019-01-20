package com.pisen.router.core.filemanager.cancheinfo;

import java.util.List;

import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.cancheinfo.WebDAVCacheManager.OnCacheChangeListener;
import com.pisen.router.core.monitor.entity.RouterConfig;

import de.aflx.sardine.DavResource;

/**
 * 资源缓存接口
 * 
 * @author yangyp
 * @version 1.0, 2014-7-18 下午1:05:57
 */
public interface IResourceCache {

	public interface ResourceFilter {
		boolean accept(ResourceInfo resource);
	}

	/**
	 * 获取路由配置信息
	 * 
	 * @return
	 */
	RouterConfig getRouterConfig();

	/**
	 * 缓存是否处理完成
	 * 
	 * @return
	 */
	boolean isCacheCompleted();

	/**
	 * 监听缓存处理状态
	 * 
	 * @param cacheCompleted
	 */
	void setOnChangeListener(OnCacheChangeListener listener);

	/**
	 * 获取所有缓存文件(包括目录)
	 * 
	 * @return
	 */
	List<ResourceInfo> getResourceAll();

	/**
	 * 根据地址获取目录下列表
	 * 
	 * @param webdavurl
	 * @return
	 */
	List<ResourceInfo> list(String webdavurl);

	/**
	 * 要所对象获取所有过滤资源
	 * 
	 * @param webdavurl
	 * @param fileter
	 * @return
	 */
	List<ResourceInfo> listRecursively(String webdavurl, ResourceFilter filter);

	/**
	 * 添加缓存
	 * 
	 * @param webdavurl
	 */
	void addResourceCache(String webdavurl);

	/**
	 * 添加缓存
	 * 
	 * @param webdavurl
	 * @param resource
	 */
	void addResourceCache(String webdavurl, DavResource resource);
	
	/**
	 * 添加缓存
	 * @param key
	 * @param resource
	 */
	void addResourceCache(String key, ResourceInfo resource);

	/**
	 * 更新缓存
	 * 
	 * @param oldURL
	 * @param newName
	 */
	void updateCache(String oldURL, String newName);

	/**
	 * 删除缓存
	 * 
	 * @param webdavurl
	 */
	void removeCache(String webdavurl);

	/**
	 * 清除所有缓存
	 */
	void removeAllCache();
}
