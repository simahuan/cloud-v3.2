package com.pisen.router.ui.phone.resource.v2.panel;

import java.util.List;

import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.core.filemanager.SortComparator.FileSort;
import com.pisen.router.core.filemanager.cancheinfo.IResourceCache;
import com.pisen.router.core.filemanager.cancheinfo.WebDAVCacheManager.OnCacheChangeListener;
import com.pisen.router.ui.phone.resource.v2.RouterFragment;

public abstract class CategoryView extends FrameLayout implements ISelectionActionBar<ResourceInfo>, OnCacheChangeListener {

	protected String parentPath;
	protected FileType type;
	public SardineCacheResource sardineManager;

	public CategoryView(RouterFragment fragment, String path, FileType type) {
		super(fragment.getActivity());
		this.type = type;
		this.parentPath = path;
	}

	public void setContentView(int layoutResID) {
		View.inflate(getContext(), layoutResID, this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		sardineManager = new SardineCacheResource(null, null);

		showLoadingMsg();
		// 判断是否缓存完成
		if (sardineManager.getCacheManager().isCacheCompleted()) {
			loadingData();
		} else {
			sardineManager.getCacheManager().setOnChangeListener(this);
		}
	}

	private void loadingData() {
		AsyncTaskUtils.execute(new InBackgroundCallback<List<ResourceInfo>>() {

			@Override
			public List<ResourceInfo> doInBackground() {
				List<ResourceInfo> results = sardineManager.listRecursively(parentPath, type);
				sardineManager.sort(results, FileSort.NAME_ASC);
				return results;
			}

			@Override
			public void onPostExecute(List<ResourceInfo> results) {
				onLoadFinished(results);
				hideLoadingMsg();
				if (results.isEmpty()) {
					showEmptyMsg();
				}
			}

		});
	}

	/**
	 * 刷新数据
	 */
	public void refreshData() {
		showLoadingMsg();
		loadingData();
	}

	private void showLoadingMsg() {
		((TextView) findViewById(R.id.msgToast)).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.msgToast)).setText("正在获取，请稍候...");
	}

	private void hideLoadingMsg() {
		((TextView) findViewById(R.id.msgToast)).setVisibility(View.GONE);
	}

	private void showEmptyMsg() {
		((TextView) findViewById(R.id.msgToast)).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.msgToast)).setText("这里是空的哦,快来上传文件吧~");
		((TextView) findViewById(R.id.msgToast)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_no_file, 0, 0);
	}

	public void showTransferStatu() {

	}

	public void hideTransferStatu() {
		((TextView) findViewById(R.id.msgToast)).setVisibility(View.GONE);
	}

	@Override
	public void onWebDAVCacheCompleted(IResourceCache cache) {
		loadingData();
	}

	/**
	 * 数据加载完成回调
	 * 
	 * @param results
	 */
	protected abstract void onLoadFinished(List<ResourceInfo> results);

	@Override
	public void onActionBarCompleted() {
		refreshData();
	}
}
