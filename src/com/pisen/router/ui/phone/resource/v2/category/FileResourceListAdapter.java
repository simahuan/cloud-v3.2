package com.pisen.router.ui.phone.resource.v2.category;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;

/**
 * 只支持文件选择的适配器
 * @author ldj
 * @version 1.0 2015年7月2日 上午11:42:42
 */
public class FileResourceListAdapter extends ResourceListAdapter {

	public FileResourceListAdapter(Context context) {
		super(context);
	}

	public FileResourceListAdapter(Context context, List<ResourceInfo> data) {
		super(context, data);
	}
	
	@Override
	public void setItemChecked(ResourceInfo item, boolean checked) {
		if(!item.isDirectory) {
			super.setItemChecked(item, checked);
		}
	}
	
	@Override
	public void bindView(Context context, final View view, final int position) {
		ResourceHolder holder = (ResourceHolder) view.getTag();
		ResourceInfo item = getItem(position);
		holder.checkAreaLayout.setOnClickListener(checkBoxAreaClickListener(view, position));
		holder.checkAreaLayout.setClickable(checkBoxAreaClickListener != null && !item.isDirectory);

		if (item.isDirectory) {
			holder.imgIcon.setImageResource(R.drawable.ic_file_folder);
			holder.txtSize.setVisibility(View.GONE);
			holder.checkStatus.setVisibility(View.GONE);
		} else {
			holder.imgIcon.setImageResource(item.getIconResId());
			if (ResourceCategory.isFileType(item.name, FileType.Image)) {
				diplayImage(holder.imgIcon, item.path,item.getIconResId());
			} else if (ResourceCategory.isFileType(item.name, FileType.Video)) {
				diplayVideo(holder.imgIcon, item.path,item.getIconResId());
			}

			holder.txtSize.setVisibility(View.VISIBLE);
			holder.txtSize.setText(item.getSizeString());
			holder.checkStatus.setVisibility(isCheckedEnabled() ? View.VISIBLE : View.INVISIBLE);
		}
		holder.txtName.setText(item.name);
		holder.txtDateUpdated.setText(item.getLastModifiedString());
		holder.checkStatus.setChecked(isItemChecked(position));
		holder.checkAreaLayout.setVisibility(isCheckedEnabled() ? View.VISIBLE : View.INVISIBLE);
	}


	private OnClickListener checkBoxAreaClickListener(final View view, final int position) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkBoxAreaClickListener != null && !mData.get(position).isDirectory) {
					checkBoxAreaClickListener.onCheckBoxAreaClick(view, position);
				}
			}
		};
	}
}
