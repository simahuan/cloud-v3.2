package com.pisen.router.core.filemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.studio.util.StringUtils;

import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.SortComparator.FileSort;

/**
 * 资源管理
 * 
 * @author yangyp
 */
public abstract class ResourceManager implements IResource {

	@Override
	public List<ResourceInfo> list(String dir) throws Exception {
		return listFileChooser(dir, false);
	}

	@Override
	public List<ResourceInfo> listFileChooser(String dir) {
		return listFileChooser(dir, true);
	}

	protected abstract List<ResourceInfo> listFileChooser(String dir, boolean dirOnly);

	@Override
	public List<ResourceInfo> search(String dirPath, String keyword, FileType type, SearchCallback callback) throws Exception {
		List<ResourceInfo> results = new ArrayList<ResourceInfo>();
		searchRecursively(results, dirPath, keyword, type, callback);
		return results;
	}

	protected void searchRecursively(List<ResourceInfo> results, String dirPath, String keyword, FileType type, SearchCallback callback) throws Exception {
		List<ResourceInfo> fileList = list(dirPath);
		for (ResourceInfo resource : fileList) {
			if (resource.isDirectory) {
				searchRecursively(results, resource.path, keyword, type, callback);
			} else {
				if (containsIgnoreCase(resource.name, keyword, type)) {
					results.add(resource);
					if (callback != null) {
						callback.onSearch(resource);
					}
				}
			}
		}
	}

	public static boolean containsIgnoreCase(String filename, String keyword, FileType type) {
		return ((type == FileType.All || ResourceCategory.isFileType(filename, type))) && StringUtils.containsIgnoreCase(filename, keyword);
	}

	@Override
	public List<ResourceInfo> sort(List<ResourceInfo> dataSource, FileSort sortType) {
		Collections.sort(dataSource, new SortComparator(sortType));
		return dataSource;
	}

}
