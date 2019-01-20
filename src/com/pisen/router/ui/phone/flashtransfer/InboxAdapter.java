package com.pisen.router.ui.phone.flashtransfer;

import android.content.Context;
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
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceCategory.MediaFileType;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferStatus;

/**
 * 收件箱adapter
 * @author ldj
 * @version 1.0 2015年5月27日 下午4:10:26
 */
class InboxAdapter extends AbstractFlashTransferRecordAdapter implements  OnClickListener, OnLongClickListener {

	public InboxAdapter(Context ctx) {
		super(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_flash_transfer_inbox, parent, false);

			holder = new ViewHolder();
			holder.fileTypeView = (ImageView) convertView.findViewById(R.id.imgType);
			holder.playIconView = (ImageView) convertView.findViewById(R.id.imgPlay);
			holder.fileNameView = (TextView) convertView.findViewById(R.id.txtFileName);
			holder.dateView = (TextView) convertView.findViewById(R.id.txtDate);
			holder.fileSizeView = (TextView) convertView.findViewById(R.id.txtSize);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.chkchoice);

			convertView.setTag(Integer.MAX_VALUE, holder);
		} else {
			holder = (ViewHolder) convertView.getTag(Integer.MAX_VALUE);
		}
		convertView.setTag(Integer.MAX_VALUE-1, position);
		
		TransferInfo tmp = data.get(position);
		convertView.setTag(tmp.ctag.value);
		holder.checkBox.setTag(tmp._id);

		initItemView(holder, tmp);
		handleMultiChoiceMode(holder, tmp);
		handleView(convertView);

		return convertView;
	}

	private class ViewHolder {
		ImageView fileTypeView; // 文件类型icon
		ImageView playIconView;
		TextView fileNameView; // 文件名称
		TextView dateView; // 时间
		TextView fileSizeView; // 文件大小
		CheckBox checkBox; // 多选
	}

	/**
	 * 处理事件监听
	 * 
	 * @param holder
	 */
	private void handleView(View convertView) {
		convertView.setOnClickListener(this);
		convertView.setOnLongClickListener(this);
//		holder.checkBox.setOnCheckedChangeListener(this);
	}

	/**
	 * 处理多选
	 * 
	 * @param holder
	 */
	private void handleMultiChoiceMode(ViewHolder holder, TransferInfo info) {
		if (multiChoiceMode) {
			holder.checkBox.setVisibility(View.VISIBLE);
			if (getTransferInfo(selectedData, info._id) != null) {
				holder.checkBox.setChecked(true);
			} else {
				holder.checkBox.setChecked(false);
			}
		} else {
			holder.checkBox.setVisibility(View.GONE);
		}
	}

	@Override
	public void refreshItemView(View convertView, TransferInfo info) {
		TransferInfo orginal = getTransferInfo(data, info._id);
		if (orginal != null && orginal.status != TransferStatus.SUCCESS) {
			orginal.status = info.status;
			orginal.currentBytes = info.currentBytes;
			orginal.filesize = info.filesize;
			
			if(convertView != null) {
				initItemView((ViewHolder) convertView.getTag(Integer.MAX_VALUE), orginal);
			}
		}
	}

	/**
	 * 初始化item视图相应视图
	 * 
	 * @param holder
	 * @param info
	 */
	private void initItemView(ViewHolder holder, TransferInfo info) {
		setFileTypeIcon(info, holder.fileTypeView);
		holder.fileNameView.setText(info.filename);
		holder.fileSizeView.setText(FileUtils.formatFileSize(info.filesize));
		holder.dateView.setText(DateUtils.getFormatTime(info.dataCreated, "yyyy-MM-dd"));
		MediaFileType type = ResourceCategory.getMediaType(info.filename);
		if(type.fileType == FileType.Video) {
			holder.playIconView.setVisibility(View.VISIBLE);
		}else {
			holder.playIconView.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if(onItemLongClickListener != null) {
			onItemLongClickListener.onItemLongClick(null, v, (Integer) v.getTag(Integer.MAX_VALUE -1), v.getId());
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		int position = (Integer)v.getTag(Integer.MAX_VALUE -1);
		if(multiChoiceMode) {//多选
			toggleItemChecked(v, position);
			// 通知回调
			if (listener != null) {
				listener.selectedCount(selectedData.size());
			}
		}else {
			if(onItemClickListener != null) {
				onItemClickListener.onItemClick(null, v, position, v.getId());
			}
		}
	}
	
	public void toggleItemChecked(View convertView, int position) {
		TransferInfo info = data.get(position);
		if(info != null) {
			TransferInfo tmp = getTransferInfo(selectedData, info._id);
			if ( tmp == null) {
				selectedData.add(getTransferInfo(data, info._id));
				((ViewHolder)convertView.getTag(Integer.MAX_VALUE)).checkBox.setChecked(true);
			}else {
				removeTransferInfo(selectedData, info._id);
				((ViewHolder)convertView.getTag(Integer.MAX_VALUE)).checkBox.setChecked(false);
			}
			
//			notifyDataSetChanged();
		}
		
	}
}