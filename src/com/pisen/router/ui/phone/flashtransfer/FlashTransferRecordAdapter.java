package com.pisen.router.ui.phone.flashtransfer;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.studio.os.PreferencesUtils;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
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
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceCategory.MediaFileType;
import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.ui.phone.settings.IconResource;
import com.sticky.listheaders.StickyListHeadersAdapter;

/**
 * 互传记录adpater
 * 
 * @author ldj
 * @version 1.0 2015年5月27日 上午11:35:44
 */
class FlashTransferRecordAdapter extends AbstractFlashTransferRecordAdapter implements StickyListHeadersAdapter, OnClickListener, OnLongClickListener{

	private static final int COLOR_NORMAL = Color.parseColor("#FF0073FF");
	private static final int COLOR_FAILED = Color.RED;
	//apk名称缓存
	private HashMap<String, String> apkNameMap = new HashMap<String, String>();
	private LruCache<String, Bitmap> iconsCache = new LruCache<String, Bitmap>(4*1024*1024) {
		protected int sizeOf(String key, Bitmap value) {
			Log.e("FlashTransferRecordAdapter", "size->" + value.getRowBytes() * value.getHeight() / 1024f/1024f);
			return value.getRowBytes() * value.getHeight();
		};
	};
	public FlashTransferRecordAdapter(Context ctx) {
		super(ctx);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TransferInfo tmp = data.get(position);
		String ctag = tmp.ctag.value;

		ViewHolder holder = null;
		if (convertView == null || !convertView.getTag().toString().equals(ctag)) {
			holder = new ViewHolder();
			if (ctag.equals(TransferCTag.FlashSend.value)) {
				convertView = inflater.inflate(R.layout.item_flash_transfer_record_sender, parent, false);
			} else {
				convertView = inflater.inflate(R.layout.item_flash_transfer_record_recver, parent, false);
				holder.fromView = (TextView) convertView.findViewById(R.id.txtFrom);
			}

			holder.headerView = (ImageView) convertView.findViewById(R.id.imgHead);
			holder.fileTypeView = (ImageView) convertView.findViewById(R.id.imgType);
			holder.playIconView = (ImageView) convertView.findViewById(R.id.imgPlay);
			holder.fileNameView = (TextView) convertView.findViewById(R.id.txtFileName);
			holder.lblView = (TextView) convertView.findViewById(R.id.txtStatus);
			holder.progressView = (TextView) convertView.findViewById(R.id.txtProgress);
			holder.fileSizeView = (TextView) convertView.findViewById(R.id.txtSize);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.chkchoice);

			convertView.setTag(Integer.MAX_VALUE, holder);
		} else {
			holder = (ViewHolder) convertView.getTag(Integer.MAX_VALUE);
		}
		convertView.setTag(tmp._id);
		convertView.setTag(Integer.MAX_VALUE -1, position);
		holder.checkBox.setTag(Integer.MAX_VALUE, tmp._id);

		initItemView(holder, tmp);
		handleMultiChoiceMode(holder, tmp);
		handleView(convertView);

		return convertView;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.header_flash_transfer_record, parent, false);
		}

		((TextView) convertView).setText(DateUtils.getFormatTime(data.get(position).dataCreated, "yyyy年MM月dd日"));

		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		return Long.parseLong(timeToDate(data.get(position).dataCreated));
	}

	private class ViewHolder {
		ImageView headerView; // icon
		TextView fromView; // 来自谁
		ImageView fileTypeView; // 文件类型icon
		ImageView playIconView;
		TextView fileNameView; // 文件名称
		TextView lblView; // 标签
		TextView progressView; // 进度
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
	public void onClick(View v) {
		int position = (Integer)v.getTag(Integer.MAX_VALUE -1);
		if(multiChoiceMode) {//多选
			toggleItemChecked(v,position);
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
	

//	@Override
//	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//		long id = (Long) buttonView.getTag(Integer.MAX_VALUE);
//		if (isChecked) {
//			if (getTransferInfo(selectedData, id) == null) {
//				selectedData.add(getTransferInfo(data, id));
//			}
//		} else {
//			removeTransferInfo(selectedData, id);
//		}
//
//		// 通知回调
//		if (listener != null) {
//			listener.selectedCount(selectedData.size());
//		}
//	}

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
	
	private Bitmap getHeadIcon(int headId) {
		Bitmap head = iconsCache.get(String.valueOf(headId));
		if(head == null) {
			head = IconResource.getIconWithCustom(context, headId);
			if(head != null) {
				iconsCache.put(String.valueOf(headId), head);
			}
		}
		
		return head;
	}

	/**
	 * 初始化item视图相应视图
	 * 
	 * @param holder
	 * @param info
	 */
	private void initItemView(ViewHolder holder, TransferInfo info) {
		if (info.ctag.value.equals(TransferCTag.FlashRecv.value)) {
			String[] infos = info.remoteHostType.split("_");
			/*处理设备名称*/
			if(infos.length >0) {
				holder.fromView.setText(String.format("来自%s", info.remoteHostName));
			}
			//设置头像
			if(infos.length >1) {
				int head = -1;
				try {
					head = Integer.parseInt(infos[1]);
				} catch (Exception e) {
				}
				holder.headerView.setImageBitmap(getHeadIcon(head));
			}else {
				holder.headerView.setImageBitmap(getHeadIcon(-1));
			}
		}else {
			holder.headerView.setImageBitmap(getHeadIcon(PreferencesUtils.getInt(KeyUtils.NICK_HEAD, -1)));
		}
		
		// 处理icon
		setFileTypeIcon(info, holder.fileTypeView);
		/*处理播放按钮*/
		MediaFileType type = ResourceCategory.getMediaType(info.filename);
		if(type != null && type.fileType == FileType.Video) {
			holder.playIconView.setVisibility(View.VISIBLE);
		}else {
			holder.playIconView.setVisibility(View.GONE);
		}
		//处理文件名
		if(type != null && type.fileType == FileType.Apk && info.ctag == TransferCTag.FlashSend) {
			holder.fileNameView.setText(getApkName(info));
		}else {
			holder.fileNameView.setText(info.filename);
		}
		//文件大小
		holder.fileSizeView.setText(FileUtils.formatFileSize(info.filesize));

		TransferStatus control = info.status;
		switch (control) {
		case SUCCESS:
			holder.progressView.setVisibility(View.GONE);
			
			holder.lblView.setTextColor(COLOR_NORMAL);
			holder.lblView.setText(DateUtils.getAMOrPMTime(info.dataCreated));
			break;
		case PENDING:
			holder.progressView.setVisibility(View.GONE);
			
			holder.lblView.setTextColor(COLOR_NORMAL);
			holder.lblView.setText("等待传输...");
			break;
		case RUNNING:
			holder.progressView.setVisibility(View.VISIBLE);
			holder.progressView.setText(getPorgress(info));
			
			holder.lblView.setTextColor(COLOR_NORMAL);
			holder.lblView.setText("传输中...");
			break;
		case PAUSE:
		case CANCELED:
		case UNKNOWN_ERROR:
		case NET_NO_CONNECTION_ERROR:
		case CANNOT_RESUME_ERROR:
		case HTTP_ERROR:
			holder.progressView.setVisibility(View.GONE);
			
			holder.lblView.setTextColor(COLOR_FAILED);
			if (info.ctag.value.equals(TransferCTag.FlashRecv.value)) {
				holder.lblView.setText("接收失败");
			}else {
				holder.lblView.setText("发送失败");
			}
			break;
		default:
			break;
		}
	}

	private String getApkName(TransferInfo info) {
		String path = String.format("%s/%s", info.url, info.filename);
		String apkName = apkNameMap.get(path);
		if(TextUtils.isEmpty(apkName)) {
			apkName =  FileUtils.getApkName(context, path);
			if(!TextUtils.isEmpty(apkName)) {
				apkName = String.format("%s.apk",apkName);
				apkNameMap.put(path, apkName);
			}
		}
		
		return apkName;
	}

	/**
	 * 获取进度百分比
	 * @param info
	 * @return
	 */
	private CharSequence getPorgress(TransferInfo info) {
		String zero = "0%";
		if(info != null) {
			long cur = info.currentBytes;
			long total = info.filesize;
			return total >0 ?  (cur * 100/total + "%") : zero;
		}
		return zero;
	}

	@Override
	public boolean onLongClick(View v) {
		if(onItemLongClickListener != null) {
			onItemLongClickListener.onItemLongClick(null, v, (Integer) v.getTag(Integer.MAX_VALUE -1), v.getId());
			return true;
		}
		return false;
	}
}