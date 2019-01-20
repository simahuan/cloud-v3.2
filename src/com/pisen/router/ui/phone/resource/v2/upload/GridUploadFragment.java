package com.pisen.router.ui.phone.resource.v2.upload;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.ui.phone.resource.v2.category.DiskGridMovieAdapter;
import com.pisen.router.ui.phone.resource.v2.category.DiskGridPictureAdapter;
import com.sticky.gridheaders.StickyGridHeadersGridView;

public class GridUploadFragment extends UploadCategoryFragment implements OnItemClickListener {

	private StickyGridHeadersGridView headerGridView;
	private DiskGridPictureAdapter diskGridAdapter;
	private FileType type;

	public GridUploadFragment(RootUploadActivity activity, String dirPath, FileType type) {
		super(activity, dirPath, type);
		this.type = type;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_home_category_grid, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (type == FileType.Image) {
			diskGridAdapter = new DiskGridPictureAdapter(getActivity());
		} else {
			diskGridAdapter = new DiskGridMovieAdapter(getActivity());
		}
		diskGridAdapter.setCheckedEnable(true);
		headerGridView = (StickyGridHeadersGridView) findViewById(R.id.headerGridView);
		headerGridView.setHeadersIgnorePadding(true);
		headerGridView.setAreHeadersSticky(false);
		headerGridView.setAdapter(diskGridAdapter);
		diskGridAdapter.setOnItemClickListener(this);
		headerGridView.setNumColumns(diskGridAdapter.getNumCloumns());
	}

	@Override
	protected void onLoadFinished(List<ResourceInfo> results) {
		diskGridAdapter.setData(results);
		diskGridAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (diskGridAdapter.isCheckedEnabled()) {
			diskGridAdapter.toggleItemViewCheck(position,view, parent);
			// updateCheckedText();
			getUploadActivity().updateActionBarChanged();
		} else {
			ResourceInfo resource = diskGridAdapter.getItem(position);
			ResourceInfo.doOpenFile(getActivity(), resource, diskGridAdapter.getData());
		}
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
