package com.pisen.router.ui.phone.resource.v2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.DialogInterface;
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
import com.pisen.router.common.dialog.InputDialog;
import com.pisen.router.common.dialog.InputDialog.SimpleClickListener;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.core.filemanager.SortComparator.FileSort;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.ui.base.FragmentSupport;
import com.pisen.router.ui.phone.resource.v2.category.ResourceListAdapter;

/**
 * 硬盘显示区
 * 
 * @author yangyp
 */
public class FileChooserFragment extends FragmentSupport implements View.OnClickListener, OnItemClickListener {

	TextView txtCurrentPath;
	private ListView listContent;
	private ResourceListAdapter resourceAdapter;
	private TextView msgToast;

	public IResource sardineManager;
	private FileSort sort = FileSort.NAME_ASC;

	private FileChooserActivity rootActivity;
	private String parentPath;

	private TaskContainer taskContainer;

	public FileChooserFragment(FileChooserActivity rootActivity, String parentPath) {
		this.parentPath = parentPath;
		this.rootActivity = rootActivity;
	}

	@Override
	public View onInjectCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_file_chooser_dir, container, false);
	}

	public String getParentPath() {
		return parentPath;
	}

	@Override
	public void onDestroyView() {
		if (taskContainer != null) {
			taskContainer.cancelRequest();
		}
		super.onDestroyView();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initViews();

		try {
			txtCurrentPath.setText(new URL(parentPath).getPath());
		} catch (MalformedURLException e) {
			txtCurrentPath.setText(parentPath);
		}

		sardineManager = new SardineCacheResource(null, null);
		WifiMonitor wifi = WifiMonitor.getInstance();
		if (wifi.isPisenWifiConnected()) {
			loadResourceData(parentPath);
		} else {
			showErrorView();
		}
	}

	private void initViews() {
		txtCurrentPath = (TextView) findViewById(R.id.txtCurrentPath);
		listContent = (ListView) findViewById(R.id.listContent);
		msgToast = (TextView) findViewById(R.id.msgToast);

		resourceAdapter = new ResourceListAdapter(getActivity());
		resourceAdapter.setOnItemClickListener(this);
		listContent.setAdapter(resourceAdapter);
	}

	private void loadResourceData(final String path) {
		showLoading();
		taskContainer = AsyncTaskUtils.execute(new InBackgroundCallback<List<ResourceInfo>>() {
			@Override
			public List<ResourceInfo> doInBackground() {
				List<ResourceInfo> results = sardineManager.listFileChooser(path);
				sardineManager.sort(results, sort);
				return results;
			}

			@Override
			public void onPostExecute(List<ResourceInfo> result) {
				if (result.isEmpty()) {
					msgToast.setText("目录为空");
				} else {
					hideLoading();
					resourceAdapter.setData(result);
					resourceAdapter.notifyDataSetChanged();
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

	/**
	 * 未连接品胜Wifi或Wifi已关闭
	 */
	private void showErrorView() {
		msgToast.setVisibility(View.VISIBLE);
		msgToast.setText("未连接品胜Wifi或Wifi已关闭.");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnNewFolder:
			newFolder();
			break;
		default:
			break;
		}
	}

	/**
	 * 创建目录
	 */
	private void newFolder() {
		InputDialog createNewFolder = new InputDialog(getActivity());
		createNewFolder.setTitle("新建文件夹");
		createNewFolder.setOnClickListener(new SimpleClickListener() {
			@Override
			public void onOk(DialogInterface dialog, final String inputText) {
				AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
					@Override
					public Boolean doInBackground() {
						try {
							sardineManager.createDir(parentPath + inputText);
							return true;
						} catch (Exception e) {
							return false;
						}
					}

					@Override
					public void onPostExecute(Boolean result) {
						if (result) {
							loadResourceData(parentPath);
						} else {
							UIHelper.showToast(getActivity(), inputText + " 目录创建失败.");
						}
					}
				});
			}
		});
		createNewFolder.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ResourceInfo resource = resourceAdapter.getItem(position);
		rootActivity.startChildFragment(new FileChooserFragment(rootActivity, resource.path));
	}

}
