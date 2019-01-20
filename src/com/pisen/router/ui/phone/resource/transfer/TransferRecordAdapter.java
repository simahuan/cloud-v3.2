package com.pisen.router.ui.phone.resource.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceCategory.MediaFileType;
import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferControl;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.ui.adapter.ResourceChoiceAdapter;
import com.pisen.router.ui.phone.resource.IMultiChoice;
import com.squareup.picasso.Picasso;
import com.sticky.listheaders.StickyListHeadersAdapter;

/**
 * 纪录adapter
 * @author ldj
 * @version 1.0 2015年5月4日 下午4:17:29
 */
public abstract class TransferRecordAdapter extends ResourceChoiceAdapter<TransferInfo> implements StickyListHeadersAdapter, IMultiChoice<TransferInfo> {

	protected Context context;
	// 进行中任务数据
	protected List<TransferInfo> runningData;
	// 已完成任务数据
	protected List<TransferInfo> completeData;
	// 所有展示数据
	protected List<TransferInfo> data;
	protected LayoutInflater inflater;

	public static final int STATUS_RUNNING = 0;
	public static final int STATUS_COMPLETE = 1;
	public static final int STATUS_INVALIDE = -1;

	protected int itemTextColorNormal;
	protected int itemTextColorDisable;
	protected TransferManagerV2 transferManager;
	
	public void setTransferManager(TransferManagerV2 transferManager) {
		this.transferManager = transferManager;
	}

	public TransferRecordAdapter(Context ctx) {
		super(ctx);
		this.context = ctx;
		
		inflater = LayoutInflater.from(ctx);
		selectedData = new ArrayList<TransferInfo>();
		itemTextColorNormal = ctx.getResources().getColor(R.color.transferrecord_item_normal);
		itemTextColorDisable = ctx.getResources().getColor(R.color.transferrecord_item_disable);
	}
	
	public List<TransferInfo> getData() {
		return data;
	}

	public void setData(List<TransferInfo> data) {
		this.data = data;
	}

	/**
	 * 数据更新
	 * @param info
	 */
	public abstract void refreshItemView(View convertView, TransferInfo info);

	/**
	 * 从data中，获取id相匹配的数据项
	 * 
	 * @param id
	 * @return
	 */
	protected TransferInfo getTransferInfo(List<TransferInfo> data, long id) {
		TransferInfo result = null;

		if (data != null) {
			for (TransferInfo tmp : data) {
				if (tmp._id == id) {
					result = tmp;
					break;
				}
			}
		}

		return result;
	}
	
	@Override
	public boolean contains(List<TransferInfo> data, TransferInfo info) {
		boolean result = false;
		if(data != null && !data.isEmpty() && info != null) {
			for(TransferInfo r : data) {
				if(r._id == info._id) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}

	@Override
	public boolean remove(List<TransferInfo> data, TransferInfo info) {
		TransferInfo result = null;
		if(data != null && !data.isEmpty() && info != null) {
			for(TransferInfo r : data) {
				if(r._id == info._id) {
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
	
	/**
	 * 从指定data中移除id相同的值
	 * @param data
	 * @param id
	 * @return
	 */
	protected boolean removeTransferInfo(List<TransferInfo> data, long id) {
		boolean result = false;

		if (data != null) {
			for (TransferInfo tmp : data) {
				if (tmp._id == id) {
					data.remove(tmp);
					result = true;
					break;
				}
			}
		}

		return result;
	}

	public void setData(List<TransferInfo> runningData, List<TransferInfo> completeData) {
		this.runningData = runningData;
		this.completeData = completeData;

		initData(runningData, completeData);
	}

	private void initData(List<TransferInfo> runningData, List<TransferInfo> completeData) {
//		selectedData.clear();
		
		if (data != null) {
			data.clear();
			data = null;
		}

		data = new ArrayList<TransferInfo>();
		data.addAll(runningData);
		data.addAll(completeData);
	}

	@Override
	public int getCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public TransferInfo getItem(int position) {
		return data == null ? null : data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public long getHeaderId(int position) {
		return data == null ? STATUS_INVALIDE : (data.get(position).status == TransferStatus.SUCCESS ? STATUS_COMPLETE : STATUS_RUNNING);
	}
	
	@Override
	public void showMultiChoice() {
		setCheckEnabled(true);
		selectedData.clear();
		notifyDataSetChanged();
	}

	@Override
	public void dismissMultiChoice() {
		setCheckEnabled(false);
		selectedData.clear();
		notifyDataSetChanged();
	}

	@Override
	public void selectAll() {
		selectedData.clear();
		selectedData.addAll(data);
		notifyDataSetChanged();
	}
	
	@Override
	public void cancelSelectAll() {
		selectedData.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getSelectedCount() {
		return selectedData.size();
	}

	@Override
	public List<TransferInfo> getSelectedData() {
		return selectedData;
	}

	/**
	 * 重新开始任务
	 * @param info
	 */
	protected void restartTask(TransferInfo info) {
		info.takeControl = TransferControl.START;
		info.status = TransferStatus.PENDING;
		info.currentBytes = 0;
		transferManager.restartTransfer(info._id);
	}

	/**
	 * 暂停任务
	 * @param info
	 */
	protected void pauseTask(TransferInfo info) {
		info.takeControl = TransferControl.PAUSE;
		info.status = TransferStatus.PAUSE;
		info.currentBytes = 0;
		transferManager.pauseTransfer(info._id);
	}

	public void pauseAllTask() {
		//暂停所有正在进行的任务
		if(runningData != null && !runningData.isEmpty()) {
			int size = runningData.size();
			TransferInfo tmp = null;
			for(int i=0; i<size; i++) {
				tmp = runningData.get(i);
				pauseTask(tmp);
			}
		}
	}

	public void startAllTask() {
		//开始所有未开始的任务
		if(runningData != null && !runningData.isEmpty()) {
			int size = runningData.size();
			TransferInfo tmp = null;
			for(int i=0; i<size; i++) {
				tmp = runningData.get(i);
				if(tmp.status != TransferStatus.RUNNING && tmp.status != TransferStatus.PENDING && tmp.status != TransferStatus.CANCELED) {
					restartTask(tmp);
				}
			}
		}
	}
	
	protected void setFileTypeIcon(TransferInfo info, final ImageView displayView) {
		if(info.isDir) {
			displayView.setImageResource(R.drawable.ic_file_folder);
		} else {
			MediaFileType type = ResourceCategory.getMediaType(info.filename);
			if(type != null) {
				if(info.status == TransferStatus.SUCCESS || info.ctag == TransferCTag.Upload || info.ctag == TransferCTag.CameraUpload ) {
					String path = null;
					if(info.ctag == TransferCTag.Upload || info.ctag == TransferCTag.CameraUpload) {
						path = info.url;
					}else {
						path =  String.format("%s/%s", info.storageDir, info.filename);
					}
					setSuccessTypeIcon(displayView, type, path);
				}else {
					displayView.setImageResource(type.iconResId);
				}
			}else {
				displayView.setImageResource(R.drawable.ic_file_unknown);
			}
		}
	}
	
	private void setSuccessTypeIcon(final ImageView displayView, MediaFileType type, String path) {
		switch (type.fileType) {
		case Image:
			Picasso.with(context).load(new File(path)).placeholder(R.drawable.thumbnail_pic).resize(200, 200).centerInside().into(displayView);
			break;
		case Video:
			Picasso.with(context).load(new File(path)).placeholder(R.drawable.thumbnail_video).resize(200, 200).centerInside().into(displayView);
			break;
		default:
			displayView.setImageResource(type.iconResId);
			break;
		}
	}
	
	/**
	 * 变更任务状态
	 * 
	 * @param info
	 */
	protected void toggleTask(TransferInfo info) {
		TransferControl control = info.takeControl;
		switch (control) {
		case START:
			switch (info.status) {
			case PAUSE:
			case CANCELED:
			case UNKNOWN_ERROR:
			case NET_NO_CONNECTION_ERROR:
			case CANNOT_RESUME_ERROR:
			case HTTP_ERROR:
				// 重新开始上传
				restartTask(info);
				break;
			case PENDING:
			case RUNNING:
				// 暂停任务
				pauseTask(info);
				break;
			default:
				break;
			}
	
			break;
		case PAUSE:
			restartTask(info);
			break;
		case DELETE:
			throw new IllegalArgumentException();
		default:
			break;
		}
	}
}