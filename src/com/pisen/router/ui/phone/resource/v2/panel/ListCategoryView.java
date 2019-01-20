package com.pisen.router.ui.phone.resource.v2.panel;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.ui.phone.resource.v2.RouterFragment;
import com.pisen.router.ui.phone.resource.v2.ShearchFragment;
import com.pisen.router.ui.phone.resource.v2.category.ResourceListAdapter;

/**
 * 音频,视频,文档,应用展示列表
 * @author  mahuan
 * @version 1.0 2015年7月13日 上午11:24:04
 */
public class ListCategoryView extends CategoryView implements OnClickListener, OnItemLongClickListener, OnItemClickListener {

	private RouterFragment fragment;
	private ListView refreshListView;
	private ResourceListAdapter resourceAdapter;

	public ListCategoryView(RouterFragment fragment, String path, FileType type) {
		super(fragment, path, type);
		this.fragment = fragment;
		setContentView(R.layout.resource_home_category_list);
		initView(getContext());
	}

	private void initView(Context context) {
		refreshListView = (ListView) findViewById(R.id.refreshListView);
		resourceAdapter = new ResourceListAdapter(context);
		resourceAdapter.setOnItemClickListener(this);
		resourceAdapter.setOnItemLongClickListener(this);
		refreshListView.setAdapter(resourceAdapter);
		findViewById(R.id.btnSearch).setOnClickListener(this);
	}

	@Override
	protected void onLoadFinished(List<ResourceInfo> results) {
		resourceAdapter.setData(results);
		resourceAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSearch:
			ShearchFragment.start(getContext(), parentPath, type);
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (resourceAdapter.isCheckedEnabled()) {
			resourceAdapter.toggleItemChecked(position);
			fragment.updateActionBarChanged();
		} else {
			ResourceInfo resource = resourceAdapter.getItem(position);
			ResourceInfo.doOpenFile(getContext(), resource, resourceAdapter.getData());
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		resourceAdapter.setCheckedEnabled(true);
		resourceAdapter.toggleItemChecked(position);
		fragment.showSelectionMenu();
		return false;
	}

	/**
	 * 取消选中菜单
	 */
	private void hideCheckedMenu() {
		fragment.hideSelectionMenu();
		resourceAdapter.setCheckedEnabled(false);
	}

	@Override
	public void onActionBarCompleted() {
		super.onActionBarCompleted();
		hideCheckedMenu();
	}

	@Override
	public void onActionBarItemCheckCancel() {
		hideCheckedMenu();
	}

	@Override
	public List<ResourceInfo> getItemAll() {
		return resourceAdapter.getData();
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

}
