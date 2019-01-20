package com.pisen.router.ui.phone.flashtransfer;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.DateUtils;
import com.pisen.router.common.utils.FileUtils;
import com.pisen.router.core.filemanager.ApkResourceInfo;
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceCategory.MediaFileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.ui.adapter.ResourceChoiceAdapter;

/**
 * 互传listview型视图adapter
 * @author ldj
 * @version 1.0 2015年5月27日 下午4:10:26
 */
public class FlashTransferListAdapter extends ResourceChoiceAdapter<ResourceInfo> implements  OnClickListener, OnLongClickListener {
	private Context context;
	
	public FlashTransferListAdapter(Context context) {
		super(context);
		
		this.context = context;
	}
	
	@Override
	public boolean contains(List<ResourceInfo> data, ResourceInfo info) {
		boolean result = false;
		if(data != null && !data.isEmpty() && info != null) {
			for(ResourceInfo r : data) {
				if(r.path.equals(info.path)) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}

	@Override
	public boolean remove(List<ResourceInfo> data, ResourceInfo info) {
		ResourceInfo result = null;
		if(data != null && !data.isEmpty() && info != null) {
			for(ResourceInfo r : data) {
				if(r.path.equals(info.path)) {
					result = r;
					break;
				}
			}
		}
		
		if(result != null) {
			return data.remove(result);
		}
		
		return false;
	}

	@Override
	public View newItemView(int position, View convertView, ViewGroup parent) {
		convertView = View.inflate(context,R.layout.item_flash_transfer_simplelist, null);

		ItemHolder holder = new ItemHolder();
		holder.fileTypeView = (ImageView) convertView.findViewById(R.id.imgType);
		holder.fileNameView = (TextView) convertView.findViewById(R.id.txtFileName);
		holder.dateView = (TextView) convertView.findViewById(R.id.txtDate);
		holder.fileSizeView = (TextView) convertView.findViewById(R.id.txtSize);
		holder.checkBox = (CheckBox) convertView.findViewById(R.id.chkchoice);

		convertView.setTag(Integer.MAX_VALUE, holder);
		convertView.setTag(position);
		return convertView;
	}

	@Override
	public void handleItemView(int position, View convertView, ViewGroup parent) {
		ItemHolder holder = (ItemHolder) convertView.getTag(Integer.MAX_VALUE);
		convertView.setTag(position);;

		ResourceInfo info = mData.get(position);
		if(info instanceof ApkResourceInfo) {
			holder.fileTypeView.setImageDrawable(((ApkResourceInfo)info).icon);
			holder.fileNameView.setText(((ApkResourceInfo)info).apkName);
		} else {
			if(TextUtils.isEmpty(info.name)) {
				info.name = info.path.substring(info.path.lastIndexOf("/")+1);
			}
			holder.fileTypeView.setImageResource(getFileTypeIcon(info.name));
			holder.fileNameView.setText(info.name);
		}
		holder.fileSizeView.setText(FileUtils.formatFileSize(info.size));
		holder.dateView.setText(DateUtils.getFormatTime(info.lastModified * 1000, "yyyy-MM-dd"));
		
		if(isCheckEnabled()) {//处于选中状态
			holder.checkBox.setVisibility(View.VISIBLE);
			if(contains(selectedData,info)) {
				holder.checkBox.setChecked(true);
			}else {
				holder.checkBox.setChecked(false);
			}
		}else {//没有处于选中状态
			holder.checkBox.setVisibility(View.GONE);
		}
		
		convertView.setOnLongClickListener(this);
		convertView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.contentLayout:
			int position = (Integer)v.getTag();
			if(isCheckEnabled()) {//多选
				toggleItemViewCheck(position,v,null);
				nofityCountChanged();
			}else {
				if(itemClickListener != null) {
					itemClickListener.onItemClick(null, v, position, v.getId());
				}
			}
			break;
		default:
			break;
		}
	}
	
//	public void toggleItemChecked(int position) {
//		ResourceInfo info = getItem(position);
//		if(!contains(selectedData, info)) {
//			selectedData.add(info);
//		}else {
//			remove(selectedData,info);
//		}
//		
//		notifyDataSetChanged();
//	}
	
	protected void nofityCountChanged() {
		if(countChangeListener != null) {
			countChangeListener.selectedCount(selectedData.size());
		}
	}
	
	/**
	 * 获取传输文件icon资源
	 * @param info
	 * @return
	 */
	protected int getFileTypeIcon(String fileName) {
		MediaFileType type = ResourceCategory.getMediaType(fileName);
		return type == null ? R.drawable.ic_file_unknown : type.iconResId;
	}


	private class ItemHolder {
		ImageView fileTypeView; // 文件类型icon
		TextView fileNameView; // 文件名称
		TextView dateView; // 时间
		TextView fileSizeView; // 文件大小
		CheckBox checkBox; // 多选
	}

	@Override
	public boolean onLongClick(View v) {
		if(!isCheckEnabled()) {
			setCheckEnabled(true);
			notifyDataSetChanged();
		}
		if(itemLongClickListener != null){
			return itemLongClickListener.onItemLongClick(null, v,(Integer) v.getTag(), v.getId());
		}
		return true;
	}
	
	public void reset() {
		setCheckEnabled(false);
		selectedData.clear();
		
		notifyDataSetChanged();
	}

	@Override
	public void toggleItemViewCheck(int position, View convertView, ViewGroup parent) {
		ResourceInfo info = getItem(position);
		if(!contains(selectedData, info)) {
			selectedData.add(info);
//			((ItemHolder)convertView.getTag(Integer.MAX_VALUE)).checkBox.setChecked(true);
		}else {
			remove(selectedData,info);
//			((ItemHolder)convertView.getTag(Integer.MAX_VALUE)).checkBox.setChecked(false);
		}
		
		notifyDataSetChanged();
	}

	
}