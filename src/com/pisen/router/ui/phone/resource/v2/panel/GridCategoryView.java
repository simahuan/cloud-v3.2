package com.pisen.router.ui.phone.resource.v2.panel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.lidroid.xutils.view.annotation.event.OnClick;
import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.ui.phone.resource.v2.RouterFileFragment;
import com.pisen.router.ui.phone.resource.v2.RouterFragment;
import com.pisen.router.ui.phone.resource.v2.category.DiskGridMovieAdapter;
import com.pisen.router.ui.phone.resource.v2.category.DiskGridPictureAdapter;
import com.sticky.gridheaders.StickyGridHeadersGridView;

public class GridCategoryView extends CategoryView implements OnItemLongClickListener, OnItemClickListener ,ISelectionActionBar<ResourceInfo>{

	private RouterFragment fragment;
	private StickyGridHeadersGridView headerGridView;
	private DiskGridPictureAdapter diskGridAdapter;
	private FileType type;

	public GridCategoryView(RouterFragment fragment, String path, FileType type) {
		super(fragment, path, type);
		this.type = type;
		this.fragment = fragment;
		setContentView(R.layout.resource_home_category_grid);
		initView(getContext());
	}

	public void initView(Context context) {
		if (type == FileType.Image) {
			diskGridAdapter = new DiskGridPictureAdapter(context);
		} else {
			diskGridAdapter = new DiskGridMovieAdapter(context);
		}
		findViewById(R.id.divider_ver).setVisibility(View.GONE);
		headerGridView = (StickyGridHeadersGridView) findViewById(R.id.headerGridView);
		headerGridView.setHeadersIgnorePadding(true);
		headerGridView.setAreHeadersSticky(false);
		headerGridView.setAdapter(diskGridAdapter);
		diskGridAdapter.setOnItemClickListener(this);
		diskGridAdapter.setOnItemLongClickListener(this);
		headerGridView.setNumColumns(diskGridAdapter.getNumCloumns());
	}

	@Override
	protected void onLoadFinished(List<ResourceInfo> results) {
		if (results.isEmpty()) {
			findViewById(R.id.divider_ver).setVisibility(View.GONE);
			diskGridAdapter.clear();
		} else {
//			dateSort(results);//路由设备时间无效
			findViewById(R.id.divider_ver).setVisibility(View.VISIBLE);
			diskGridAdapter.setData(results);
			diskGridAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 排序时间排序
	 * 
	 * @param results
	 */
	private void dateSort(List<ResourceInfo> results) {
		if(results != null && !results.isEmpty()) {
			try {
				Collections.sort(results, new Comparator<ResourceInfo>() {
					@Override
					public int compare(ResourceInfo lhs, ResourceInfo rhs) {
						//					return -(int) (lhs.lastModified - rhs.lastModified);
						return (int) (rhs.lastModified - lhs.lastModified);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@OnClick(R.id.btnSearch)
	public void submit(View view) {

	}

	@Override
	public void onActionBarCompleted() {
		fragment.hideSelectionMenu();
		diskGridAdapter.setCheckedEnable(false);
		//diskGridAdapter.notifyDataSetChanged();
		refreshData();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (diskGridAdapter.isCheckedEnabled()) {
			diskGridAdapter.toggleItemViewCheck(position, view, parent);
			fragment.updateActionBarChanged();
		} else {
			ResourceInfo resource = diskGridAdapter.getItem(position);
			ResourceInfo.doOpenFileForResult(fragment.getActivity(), RouterFragment.REQUEST_VIEW,resource, diskGridAdapter.getData());
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (!diskGridAdapter.isCheckedEnabled()) {
			diskGridAdapter.setCheckedEnable(true);
			diskGridAdapter.notifyDataSetChanged();
			fragment.showSelectionMenu();
		}
		return true;
	}

	@Override
	public void onActionBarItemCheckCancel() {
		fragment.hideSelectionMenu();
		diskGridAdapter.setCheckedEnable(false);
		diskGridAdapter.reset();
	}

	@Override
	public List<ResourceInfo> getItemAll() {
		return diskGridAdapter.getData();
	}

	@Override
	public List<ResourceInfo> getCheckedItemAll() {
		return diskGridAdapter.getSelectedData();
	}

	@Override
	public void onActionBarItemCheckAll(boolean checked) {
		if (checked) {
			diskGridAdapter.selectAll();
		} else {
			diskGridAdapter.reset();
		}
	}
}
