package com.pisen.router.ui.phone.resource.v2.upload;

import java.util.List;

import android.os.Bundle;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.AsyncTaskUtils.TaskContainer;
import android.view.View;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.LocalResourceManager;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;

public abstract class UploadCategoryFragment extends UploadFragment implements IChoiceActionBar {

	private String dirPath;
	private FileType type;
	private LocalResourceManager sardineManager;
	private TaskContainer taskContainer;

	public UploadCategoryFragment(RootUploadActivity activity, String dirPath, FileType type) {
		super(activity);
		this.dirPath = dirPath;
		this.type = type;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		sardineManager = new LocalResourceManager(getActivity());

		loadingData();
	}

	@Override
	public void onDestroyView() {
		if (taskContainer != null) {
			taskContainer.cancelRequest();
		}
		super.onDestroyView();
	}

	private void loadingData() {
		showLoadingMsg();
		taskContainer = AsyncTaskUtils.execute(new InBackgroundCallback<List<ResourceInfo>>() {

			@Override
			public List<ResourceInfo> doInBackground() {
				return sardineManager.listRecursively(dirPath, type);
			}

			@Override
			public void onPostExecute(List<ResourceInfo> results) {
				onLoadFinished(results);
				hideLoadingMsg();
			}

		});
	}

	private void showLoadingMsg() {
		((TextView) findViewById(R.id.msgToast)).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.msgToast)).setText("正在获取，请稍候...");
	}

	private void hideLoadingMsg() {
		((TextView) findViewById(R.id.msgToast)).setVisibility(View.GONE);
	}

	/**
	 * 数据加载完成回调
	 * 
	 * @param results
	 */
	protected abstract void onLoadFinished(List<ResourceInfo> results);
}
