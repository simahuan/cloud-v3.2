package com.pisen.router.core.filemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.entity.InputStreamEntity;

import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.cancheinfo.IResourceCache;
import com.pisen.router.core.filemanager.cancheinfo.IResourceCache.ResourceFilter;
import com.pisen.router.core.filemanager.cancheinfo.WebdavCacheServiceUtils;
import com.pisen.router.core.monitor.entity.RouterConfig;

import de.aflx.sardine.DavResource;
import de.aflx.sardine.impl.handler.VoidResponseHandler;

/**
 * 路由文件操作
 */
public class SardineCacheResource extends SardineResourceManager {

	private IResourceCache cacheManager;

	public SardineCacheResource() {
		this(null, null);
	}

	public SardineCacheResource(String username, String password) {
		super(username, password);
		cacheManager = WebdavCacheServiceUtils.getCacheManager();
	}

	public IResourceCache getCacheManager() {
		return cacheManager;
	}

	@Override
	public List<ResourceInfo> list(String dir) throws Exception{
		List<ResourceInfo> results = new ArrayList<ResourceInfo>();
//		try {
			List<DavResource> files = sardine.list(encodeURL(dir));
			files.remove(0);

			//cacheManager.removeCache(dir);
			for (DavResource res : files) {
				ResourceInfo info = toResourceInfo(dir, res);
				results.add(info);
				cacheManager.addResourceCache(info.path, res);
			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		return results;
	}

	@Override
	protected void searchRecursively(final List<ResourceInfo> results, String dirPath, final String keyword, final FileType type, final SearchCallback callback)
			throws Exception {
		cacheManager.listRecursively(dirPath, new ResourceFilter() {
			@Override
			public boolean accept(ResourceInfo resource) {
				if (resource.isDirectory) {
					return false;
				} else {
					if (containsIgnoreCase(resource.name, keyword, type)) {
						results.add(resource);
						if (callback != null) {
							callback.onSearch(resource);
						}
						return true;
					}
				}
				return false;
			}
		});
	}

	@Override
	public List<ResourceInfo> listRecursively(final String dirPath, final FileType type) {
		switch (type) {
		case All:
			try {
				return list(dirPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		case Audio:
		case Video:
		case Image:
		case Document:
		case Apk:
		case Compress:
			final List<ResourceInfo> results = new ArrayList<ResourceInfo>();
			if (dirPath == null) {
				RouterConfig config = cacheManager.getRouterConfig();
				if (config != null) {
					String rootUrl = config.getWebdavRootUrl();
					listRecursively(results, rootUrl, type);
				}
			} else {
				listRecursively(results, dirPath, type);
			}

			return results;
		default:
			return Collections.emptyList();
		}
	}

	private void listRecursively(final List<ResourceInfo> results, String rootUrl, final FileType type) {
		cacheManager.listRecursively(rootUrl, new ResourceFilter() {
			@Override
			public boolean accept(ResourceInfo resource) {
				if (!resource.isDirectory && ResourceCategory.isFileType(resource.name, type)) {
					results.add(resource);
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void createDir(String path) throws Exception {
		path = pathToDir(path);
		super.createDir(path);
		cacheManager.addResourceCache(path);
	}

	@Override
	public void put(String path, InputStreamEntity inStream) throws Exception {
		super.put(path, inStream);
		cacheManager.addResourceCache(path);
	}

	//增加Content-Length,Date字段
	@Override
	public void put(String path, InputStreamEntity inStream,VoidResponseHandler responseHandler) throws Exception {
		inStream.setChunked(false);
		super.put(path, inStream,responseHandler);
		cacheManager.addResourceCache(path);
	}

	@Override
	public void copy(String sourcePath, String targetPath) throws Exception {
		super.copy(sourcePath, targetPath);
		cacheManager.addResourceCache(targetPath);
	}

	@Override
	public void move(String sourcePath, String targetPath) throws Exception {
		super.move(sourcePath, targetPath);
		cacheManager.updateCache(sourcePath, targetPath);
	}

	@Override
	public void delete(String path) throws Exception {
		super.delete(path);
		cacheManager.removeCache(path);
	}

	@Override
	public void rename(String sourcePath, String newName) throws Exception {
		super.rename(sourcePath, newName);
		cacheManager.updateCache(sourcePath, newName);
	}
}
