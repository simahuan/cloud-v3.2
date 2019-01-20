package com.pisen.router.core.filemanager.async;

/**
 * 排序异步任务，当前支持，名称，时间排序
 */
import java.util.List;

import com.pisen.router.core.filemanager.ResourceException;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceManager;
import com.pisen.router.core.filemanager.SortComparator.FileSort;

public class SortAsnyTask extends ResourceAsyncResultTask {
	private FileSort sortType;

	public SortAsnyTask(ResourceManager resourceManager, List<ResourceInfo> source, FileSort sortType, ResourceItemCallback callback) {
		super(resourceManager, callback);
		this.resourceManager = resourceManager;
		this.sourceList = source;
		this.sortType = sortType;
		this.result.opeartorType = operatorType.Sort;
	}

	@Override
	protected void doInBackground(ResourceResult result) throws ResourceException, Exception {
		try {
			this.resourceManager.sort(sourceList, sortType);
		} catch (Exception e) {
			e.printStackTrace();
			result.setmStatus(ResourceResult.UNKNOWN_ERROR);
		}
		result.setmStatus(ResourceResult.Sucess);
	}
}
