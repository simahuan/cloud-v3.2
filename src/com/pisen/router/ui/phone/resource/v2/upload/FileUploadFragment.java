package com.pisen.router.ui.phone.resource.v2.upload;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.AsyncTaskUtils.TaskContainer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.LocalResourceManager;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.SortComparator.FileSort;
import com.pisen.router.ui.phone.resource.v2.category.FileResourceListAdapter;

/**
 * 硬盘显示区
 * 
 * @author yangyp
 */
public class FileUploadFragment extends UploadFragment implements OnItemClickListener {

	private ListView listContent;
	private FileResourceListAdapter resourceAdapter;
	private TextView msgToast;

	public IResource sardineManager;
	private RootUploadFragment rootFragment;
	private String parentPath;

	private TaskContainer taskContainer;

	public FileUploadFragment(RootUploadFragment rootFragment, String parentPath) {
		super(rootFragment.getUploadActivity());
		this.rootFragment = rootFragment;
		this.parentPath = parentPath;
	}

	@Override
	public View onInjectCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_update_disk_file, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initViews();

		getUploadActivity().updateActionBarChanged();
		sardineManager = new LocalResourceManager(getActivity());
		loadResourceData();
	}

	@Override
	public void onDestroyView() {
		if (taskContainer != null) {
			taskContainer.cancelRequest();
		}
		super.onDestroyView();
	}

	private void initViews() {
		listContent = (ListView) findViewById(R.id.listContent);
		msgToast = (TextView) findViewById(R.id.msgToast);

		resourceAdapter = new FileResourceListAdapter(getActivity());
		resourceAdapter.setCheckedEnabled(true);
		resourceAdapter.setOnItemClickListener(this);
//		resourceAdapter.setOnCheckBoxAreaClickListener(this);
		listContent.setAdapter(resourceAdapter);
	}

	private void loadResourceData() {
		showLoading();
		taskContainer = AsyncTaskUtils.execute(new InBackgroundCallback<List<ResourceInfo>>() {
			@Override
			public List<ResourceInfo> doInBackground() {
				List<ResourceInfo> results = null;
				try {
					results = sardineManager.list(parentPath);
					sardineManager.sort(results, FileSort.NAME_ASC);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return results;
			}

			@Override
			public void onPostExecute(List<ResourceInfo> result) {
				resourceAdapter.setData(result);
				if (result == null ||result.isEmpty()) {
					msgToast.setText("目录为空");
				} else {
					hideLoading();
				}
			}
		});
	}

	/**
	 * 加载中
	 */
	private void showLoading() {
		msgToast.setVisibility(View.VISIBLE);
		msgToast.setText("读取中，请稍候...");
	}

	private void hideLoading() {
		msgToast.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ResourceInfo resource = resourceAdapter.getItem(position);
		if (resource.isDirectory) {
			rootFragment.startChildFragment(new FileUploadFragment(rootFragment, resource.path));
		} else {
			if(resourceAdapter.isCheckedEnabled()) {
				resourceAdapter.toggleItemChecked(position);
				getUploadActivity().updateActionBarChanged();
			}
		}
	}

	@Override
	public List<ResourceInfo> getItemAll() {
		//TODO 数据过滤
		return getCheckableData(resourceAdapter.getData());
	}

	private List<ResourceInfo> getCheckableData(List<ResourceInfo> data) {
		List<ResourceInfo> result = new ArrayList<ResourceInfo>();
		if(data != null && !data.isEmpty()) {
			for(ResourceInfo info : data) {
				if(!info.isDirectory) {
					result.add(info);
				}
			}
		}
		return result;
	}

	@Override
	public List<ResourceInfo> getCheckedItemAll() {
		return resourceAdapter.getItemCheckedAll();
	}

	@Override
	public void onActionBarItemCheckAll(boolean checked) {
		resourceAdapter.setItemCheckedAll(checked);
		resourceAdapter.notifyDataSetChanged();
	}

//	@Override
//	public void onCheckBoxAreaClick(View v, int position) {
//		resourceAdapter.toggleItemChecked(position);
//		getUploadActivity().updateActionBarChanged();
//	}
}
