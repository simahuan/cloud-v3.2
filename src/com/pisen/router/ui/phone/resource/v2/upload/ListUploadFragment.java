package com.pisen.router.ui.phone.resource.v2.upload;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.ui.phone.resource.v2.category.ResourceListAdapter;

public class ListUploadFragment extends UploadCategoryFragment implements OnItemClickListener {

	private ListView listContent;
	private ResourceListAdapter resourceAdapter;

	public ListUploadFragment(RootUploadActivity activity, String dirPath, FileType type) {
		super(activity, dirPath, type);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_update_disk_file, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listContent = (ListView) findViewById(R.id.listContent);
		resourceAdapter = new ResourceListAdapter(getActivity());
		resourceAdapter.setCheckedEnabled(true);
		resourceAdapter.setOnItemClickListener(this);
		listContent.setAdapter(resourceAdapter);
	}

	@Override
	protected void onLoadFinished(List<ResourceInfo> results) {
		resourceAdapter.setData(results);
		resourceAdapter.notifyDataSetChanged();
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
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (resourceAdapter.isCheckedEnabled()) {
			resourceAdapter.toggleItemChecked(position);
			getUploadActivity().updateActionBarChanged();
		} else {
			ResourceInfo resource = resourceAdapter.getItem(position);
			ResourceInfo.doOpenFile(getActivity(), resource, resourceAdapter.getData());
		}
	}

}
