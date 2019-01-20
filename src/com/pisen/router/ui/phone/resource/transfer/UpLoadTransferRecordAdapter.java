package com.pisen.router.ui.phone.resource.transfer;

import android.content.Context;
import android.util.Log;
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
import com.pisen.router.core.filemanager.transfer.TransferControl;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferStatus;

/**
 * 传输纪录adapter
 * 
 * @author ldj
 * @version 1.0 2015年5月4日 下午4:17:29
 */
class UpLoadTransferRecordAdapter extends TransferRecordAdapter implements OnClickListener, OnLongClickListener {
	protected String completeCountLabel;
	protected String runningCountLabel;
	protected boolean isStartAllShow;
	protected String typeLbl;

	public UpLoadTransferRecordAdapter(Context ctx) {
		super(ctx);

		completeCountLabel = ctx.getResources().getString(R.string.transfer_complete_count);
		runningCountLabel = ctx.getResources().getString(R.string.upload_ing_count);
		typeLbl = "上传";
	}

	@Override
	public View newItemView(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.item_transfer_record, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.iconView = (ImageView) convertView.findViewById(R.id.img);
		holder.playIconView = (ImageView) convertView.findViewById(R.id.imgPlay);
		holder.titleView = (TextView) convertView.findViewById(R.id.txttitle);
		holder.dateView = (TextView) convertView.findViewById(R.id.txtdate);
		holder.lblView = (TextView) convertView.findViewById(R.id.txtlbl);
		holder.checkBox = (CheckBox) convertView.findViewById(R.id.chkchoice);
		holder.progressView = (RoundProgressView) convertView.findViewById(R.id.roundpbar);
//		holder.actionButton = (ImageButton) convertView.findViewById(R.id.ibtnAction);

		convertView.setTag(Integer.MAX_VALUE, holder);
		return convertView;
	}

	@Override
	public void handleItemView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = (ViewHolder) convertView.getTag(Integer.MAX_VALUE);
		TransferInfo tmp = data.get(position);
		// 进度刷新时获取对应view
		convertView.setTag(tmp._id);
		convertView.setTag(Integer.MAX_VALUE-1, position);
		holder.progressView.setTag(tmp._id);
//		holder.actionButton.setTag(tmp._id);

		initItemView(holder, tmp);
		handleMultiChoiceMode(holder, tmp);
		handleView(convertView);

	}

	@Override
	public void toggleItemViewCheck(int position, View convertView, ViewGroup parent) {
		TransferInfo info = getItem(position);
		if(!contains(selectedData, info)) {
			selectedData.add(info);
			((ViewHolder)convertView.getTag(Integer.MAX_VALUE)).checkBox.setChecked(true);
		}else {
			remove(selectedData,info);
			((ViewHolder)convertView.getTag(Integer.MAX_VALUE)).checkBox.setChecked(false);
		}
//		notifyDataSetChanged();
	}
		
		

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.header_transfer_record, parent, false);
		}

		int headerId = (int) getHeaderId(position);
		switch (headerId) {
		case STATUS_RUNNING:
			((TextView) convertView.findViewById(R.id.txtuploadcount)).setText(String.format(runningCountLabel, runningData.size()));
			TextView function = (TextView) convertView.findViewById(R.id.txtpauseall);
			function.setVisibility(View.VISIBLE);
			if(isStartAllShow) {
				function.setText("全部开始");
			}else {
				function.setText("全部暂停");
			}
			
			function.setOnClickListener(this);
//			if(!multiChoiceMode) {//多选模式不响应点击事件
//				function.setOnClickListener(this);
//			}else {
//				function.setOnClickListener(null);
//			}
			break;
		case STATUS_COMPLETE:
			((TextView) convertView.findViewById(R.id.txtuploadcount)).setText(String.format(completeCountLabel, completeData.size()));
			convertView.findViewById(R.id.txtpauseall).setVisibility(View.GONE);
			break;
		default:
			break;
		}

		return convertView;
	}

	private class ViewHolder {
		ImageView iconView; // icon
		ImageView playIconView; // play icon
		TextView titleView; // 标题
		TextView dateView; // 日期
		TextView lblView; // 标签显示
		RoundProgressView progressView; // 圆形进度
		CheckBox checkBox; // 多选
//		ImageButton actionButton;	//上传
	}

	/**
	 * 处理事件监听
	 * 
	 * @param holder
	 */
	private void handleView(View convertView) {
		convertView.setOnClickListener(this);
		convertView.setOnLongClickListener(this);
		
		((ViewHolder) convertView.getTag(Integer.MAX_VALUE)).progressView.setOnClickListener(this);
//		holder.actionButton.setOnClickListener(this);
	}

	/**
	 * 处理多选
	 * 
	 * @param holder
	 */
	private void handleMultiChoiceMode(ViewHolder holder, TransferInfo info) {
		if (isCheckEnabled()) {
			holder.progressView.setEnabled(true);//(false);
//			holder.progressView.setStatus(RoundProgressView.STATUS_DISABLE);
//			holder.actionButton.setEnabled(false);
			holder.checkBox.setVisibility(View.VISIBLE);
			if (getTransferInfo(selectedData, info._id) != null) {
				holder.checkBox.setChecked(true);
			} else {
				holder.checkBox.setChecked(false);
			}
		} else {
			holder.progressView.setEnabled(true);
//			holder.actionButton.setEnabled(true);
			if(info.status == TransferStatus.RUNNING || info.status == TransferStatus.PENDING) {
				holder.progressView.setStatus(RoundProgressView.STATUS_RUNNING);
			}else {
				holder.progressView.setStatus(RoundProgressView.STATUS_IDLE);
			}
			holder.checkBox.setVisibility(View.GONE);
		}
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.roundpbar: // 进度条点击
//			((RoundProgressView) v).toggle();
			toggleTask(getTransferInfo(data, (Long) v.getTag()));
			break;
		case R.id.txtpauseall:	//全部开始、全部暂停按钮
			if(isStartAllShow) {
				isStartAllShow = false;
				startAllTask();
			}else {
				pauseAllTask();
				isStartAllShow = true;
			}
			//切换全部暂停、开始显示
			notifyDataSetChanged();
			break;
		case R.id.contentLayout:
			int position = (Integer)v.getTag(Integer.MAX_VALUE -1);
			if(isCheckEnabled()) {//多选
				toggleItemViewCheck(position,v,null);
				// 通知回调
				if (countChangeListener != null) {
					countChangeListener.selectedCount(selectedData.size());
				}
			}else {
				if(itemClickListener != null) {
					itemClickListener.onItemClick(null, v, position, v.getId());
				}
			}
			break;
//		case R.id.ibtnAction:	//上传
//			restartTask(getTransferInfo(data, (Long) v.getTag()));
//			break;
		default:
			break;
		}

	}

	@Override
	public void refreshItemView(View convertView, TransferInfo info) {
		TransferInfo orginal = getTransferInfo(data, info._id);
		if (orginal != null && orginal.status != TransferStatus.SUCCESS) {
			orginal.status = info.status;
			orginal.currentBytes = info.currentBytes;
			orginal.filesize = info.filesize;
			initItemView((ViewHolder) convertView.getTag(Integer.MAX_VALUE), orginal);
		}
	}

	/**
	 * 初始化item视图相应视图
	 * @param holder
	 * @param info
	 */
	private void initItemView(ViewHolder holder, TransferInfo info) {
		holder.titleView.setText(info.filename);
		holder.titleView.setTextColor(itemTextColorNormal);
		//处理icon
		setFileTypeIcon(info, holder.iconView);
		/*处理播放按钮*/
		MediaFileType type = ResourceCategory.getMediaType(info.filename);
		if(type != null && type.fileType == FileType.Video) {
			holder.playIconView.setVisibility(View.VISIBLE);
		}else {
			holder.playIconView.setVisibility(View.GONE);
		}

		TransferControl control = info.takeControl;
		switch (control) {
		case START:
			initStartStatusView(holder, info);
			break;
		case PAUSE:
			transferPause(holder, info);
			break;
		case DELETE:
			throw new IllegalArgumentException();
		default:
			break;
		}
	}

	private void initStartStatusView(ViewHolder holder, TransferInfo info) {
		TransferStatus status = info.status;
		switch (status) {
		case SUCCESS: // 完成
			transferSuccess(holder, info);
			break;
		case PENDING: // 等待
			transferPending(holder, info);
			break;
		case RUNNING: // 进行中
			transferRunning(holder, info);
			break;
		case PAUSE: // 任务暂停
			transferPause(holder, info);
			break;
		case CANCELED: // 任务取消
			transferCanceld(holder, info);
			break;
		case INSUFFICIENT_STORAGE_ERROR:
		case HTTP_ERROR: // 任务失败
		case CANNOT_RESUME_ERROR:
		case NET_NO_CONNECTION_ERROR:
		case UNKNOWN_ERROR:
			transferError(holder, info);
			break;
		default:
			break;
		}
	}

	private void transferError(ViewHolder holder, TransferInfo info) {
		holder.progressView.setVisibility(View.VISIBLE);
		holder.lblView.setVisibility(View.VISIBLE);
//		holder.actionButton.setVisibility(View.VISIBLE);
		
		holder.dateView.setText(DateUtils.long2DateString(info.dataCreated));
		holder.lblView.setText(typeLbl + "失败");
		//提示空间不足
		if(info.status == TransferStatus.INSUFFICIENT_STORAGE_ERROR){
			holder.lblView.setText(typeLbl + "失败,空间不足");
		}
		holder.lblView.setTextColor(0xffff0000);
		holder.titleView.setTextColor(itemTextColorDisable);
		holder.progressView.setProgress(0);// 不支持断点续传
		holder.progressView.setStatus(RoundProgressView.STATUS_IDLE);
	}

	private void transferCanceld(ViewHolder holder, TransferInfo info) {
		holder.progressView.setVisibility(View.GONE);
		holder.lblView.setVisibility(View.VISIBLE);
//		holder.actionButton.setVisibility(View.GONE);

		holder.dateView.setText(DateUtils.long2DateString(info.dataCreated));
		holder.lblView.setText("已移除");
		holder.titleView.setTextColor(itemTextColorDisable);
	}

	private void transferPause(ViewHolder holder, TransferInfo info) {
		if(info.status != TransferStatus.SUCCESS) {
			holder.progressView.setVisibility(View.VISIBLE);
			holder.progressView.setStatus(RoundProgressView.STATUS_IDLE);
			holder.progressView.setProgress(info.getProgress());// 不支持断点续传
		}else {
			holder.progressView.setVisibility(View.GONE);
		}
		holder.lblView.setVisibility(View.GONE);
//		holder.actionButton.setVisibility(View.GONE); 

		holder.dateView.setText(DateUtils.long2DateString(info.dataCreated));
	}

	private void transferRunning(ViewHolder holder, TransferInfo info) {
		holder.progressView.setVisibility(View.VISIBLE);
		holder.lblView.setVisibility(View.GONE);
//		holder.actionButton.setVisibility(View.GONE);

		holder.progressView.setStatus(RoundProgressView.STATUS_RUNNING);
		holder.dateView.setText(String.format("%s / %s", FileUtils.formatFileSize(info.currentBytes), FileUtils.formatFileSize(info.filesize)));
		holder.progressView.setProgress(info.getProgress());
	}

	private void transferPending(ViewHolder holder, TransferInfo info) {
		holder.progressView.setVisibility(View.VISIBLE);
		holder.lblView.setVisibility(View.VISIBLE);
//		holder.actionButton.setVisibility(View.GONE);

		holder.progressView.setStatus(RoundProgressView.STATUS_RUNNING);
		holder.dateView.setText(String.format("%s", FileUtils.formatFileSize(info.filesize)));
		holder.lblView.setText("等待" + typeLbl);
		holder.lblView.setTextColor(0xff34b869);
		holder.progressView.setStatus(RoundProgressView.STATUS_RUNNING);
		holder.progressView.setProgress(0);
	}

	private void transferSuccess(ViewHolder holder, TransferInfo info) {
		holder.progressView.setVisibility(View.GONE);
		holder.lblView.setVisibility(View.GONE);
//		holder.actionButton.setVisibility(View.GONE);

		holder.dateView.setText(String.format("%s    %s", DateUtils.long2DateString(info.dataCreated), FileUtils.formatFileSize(info.filesize)));
	}

	@Override
	public boolean onLongClick(View v) {
		if(itemLongClickListener != null){
			return itemLongClickListener.onItemLongClick(null, v,(Integer) v.getTag(Integer.MAX_VALUE-1), v.getId());
		}
		return true;
	}
}