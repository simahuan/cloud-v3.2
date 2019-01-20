package com.pisen.router.ui.phone.resource.v2.upload;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
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
import com.pisen.router.core.monitor.DiskMonitor;
import com.pisen.router.core.monitor.DiskMonitor.DiskEntity;
import com.pisen.router.core.monitor.DiskMonitor.OnDiskChangedListener;
import com.pisen.router.ui.base.INavigationBar;
import com.pisen.router.ui.phone.resource.v2.NavigationBar;
import com.pisen.router.ui.phone.resource.v2.RouterDiskAdapter;

/**
 * 本地磁盘
 * 
 * @author yangyp
 */
public class RootUploadFragment extends UploadFragment implements OnDiskChangedListener, OnItemClickListener {

	private DiskMonitor diskMonitor;
	public IResource resourceManager;

	private ListView listContent;
	private RouterDiskAdapter diskAdapter;
	private TextView msgToast;

	private NavigationBar navigationBar;

	public RootUploadFragment(RootUploadActivity activity) {
		super(activity);
		this.navigationBar = (NavigationBar) activity.getNavigationBar();
		navigationBar.getRightButton().setVisibility(View.GONE);
	}

	public RootUploadActivity getUploadActivity() {
		return activity;
	}

	@Override
	public View onInjectCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_upload_disk, container, false);
	}

	@Override
	public INavigationBar getNavigationBar() {
		return activity.getNavigationBar();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		msgToast = (TextView) findViewById(R.id.msgToast);
		listContent = (ListView) findViewById(R.id.listContent);

		diskMonitor = DiskMonitor.getInstance();
		diskMonitor.registerObserver(this);
		resourceManager = new LocalResourceManager(getActivity());

		diskAdapter = new RouterDiskAdapter(getActivity());
		listContent.setAdapter(diskAdapter);
		listContent.setOnItemClickListener(this);

		if (diskMonitor.isScannerFinished()) {
			loadDiskData();
		} else {
			msgToast.setVisibility(View.VISIBLE);
			msgToast.setText("正在扫描磁盘，请稍候...");
		}
	}

	private void loadDiskData() {
		msgToast.setVisibility(View.GONE);
		DiskEntity[] disList = diskMonitor.getLocalDisk();
		diskAdapter.setData(disList);
		diskAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDiskChanged() {
		loadDiskData();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DiskEntity disk = diskAdapter.getItem(position);
		startChildFragment(new FileUploadFragment(this, disk.path));
		navigationBar.getRightButton().setVisibility(View.VISIBLE);
	}

	@Override
	public List<ResourceInfo> getItemAll() {
		return lastFragment == null ? new ArrayList<ResourceInfo>() : lastFragment.getItemAll();
	}

	@Override
	public List<ResourceInfo> getCheckedItemAll() {
		return lastFragment == null ? new ArrayList<ResourceInfo>() : lastFragment.getCheckedItemAll();
	}

	@Override
	public void onActionBarItemCheckAll(boolean checked) {
		lastFragment.onActionBarItemCheckAll(checked);
	}

	private boolean childPopBackStack() {
		FragmentManager fm = getChildFragmentManager();
		int backCount = fm.getBackStackEntryCount();
		if (backCount > 0) {
			fm.popBackStack();
			if (backCount <= 1) {
				navigationBar.getRightButton().setVisibility(View.GONE);
				lastFragment = null;
			} else {
				lastFragment = (FileUploadFragment) fm.getFragments().get(backCount - 2);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (childPopBackStack()) {
				if(lastFragment != null) {
					activity.updateActionBarChanged();
				}else {//本地存储设备选择视图
					activity.updateUpdateButtonText();
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	FileUploadFragment lastFragment;

	public void startChildFragment(FileUploadFragment fragment) {
		this.lastFragment = fragment;
		getChildFragmentManager().beginTransaction().add(R.id.listfileLayout, fragment).addToBackStack(null).commit();
	}

}
