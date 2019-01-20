package com.pisen.router.ui.phone.flashtransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.lidroid.xutils.cache.LruMemoryCache;
import com.pisen.router.R;
import com.pisen.router.common.utils.FileUtils;
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceCategory.MediaFileType;
import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.ui.phone.resource.IMultiChoice;
import com.pisen.router.ui.phone.resource.OnSelectedCountChangeListener;
import com.squareup.picasso.Picasso;

/**
 * 闪电互传记录adpater
 * @author ldj
 * @version 1.0 2015年5月27日 上午11:15:17
 */
public abstract class AbstractFlashTransferRecordAdapter extends BaseAdapter implements IMultiChoice<TransferInfo> {

	protected Context context;
	// 所有展示数据
	protected List<TransferInfo> data;
	// 多选模式，被选中的数据
	protected List<TransferInfo> selectedData;
	protected LayoutInflater inflater;

	public static final int STATUS_INVALIDE = -1;
	// 是否处于多选模式
	protected boolean multiChoiceMode;
	protected OnSelectedCountChangeListener listener;

	protected int itemTextColorNormal;
	protected int itemTextColorDisable;
	LruMemoryCache<String, Bitmap> cache = new LruMemoryCache<String, Bitmap>(10*1024*1024) {
		@Override
		protected int sizeOf(String key, Bitmap value) {
			return value.getRowBytes() * value.getHeight();
	}};
	protected OnItemClickListener onItemClickListener;
	protected OnItemLongClickListener onItemLongClickListener;
	
	public AbstractFlashTransferRecordAdapter(Context ctx) {
		this.context = ctx;
		
		inflater = LayoutInflater.from(ctx);
		selectedData = new ArrayList<TransferInfo>();
		itemTextColorNormal = ctx.getResources().getColor(R.color.transferrecord_item_normal);
		itemTextColorDisable = ctx.getResources().getColor(R.color.transferrecord_item_disable);
	}
	
	/**
	 * 把时间转换为date数值，去掉小时、分钟、秒值,如19990213
	 * @param time
	 * @return
	 */
	protected String timeToDate(long time) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(time));
		return String.format("%s%s%s", c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
		
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

	public void setData(List<TransferInfo> data) {
		this.data = data;
	}
	
	public List<TransferInfo> getData() {
		return data;
	}

	@Override
	public int getCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public Object getItem(int position) {
		return data == null ? null : data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setMultiChoiceCountChangeListener(OnSelectedCountChangeListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void showMultiChoice() {
		multiChoiceMode = true;
		selectedData.clear();
		notifyDataSetChanged();
	}

	@Override
	public void dismissMultiChoice() {
		multiChoiceMode = false;
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
	 * 获取传输文件icon资源
	 * @param info
	 * @return
	 * @deprecated
	 */
	protected int getFileTypeIcon(TransferInfo info) {
		int resId = R.drawable.ic_file_unknown;
		if(info.isDir) {
			resId = R.drawable.ic_file_folder;
		} else {
			MediaFileType type = ResourceCategory.getMediaType(info.filename);
			if (type!= null) {
				resId = type.iconResId;
			}
		}
		return resId;
	}
	
	protected void setFileTypeIcon(TransferInfo info, final ImageView displayView) {
		if(info.isDir) {
			displayView.setImageResource(R.drawable.ic_file_folder);
		} else {
			MediaFileType type = ResourceCategory.getMediaType(info.filename);
			if(type != null) {
				if(info.status == TransferStatus.SUCCESS || info.ctag == TransferCTag.FlashSend) {
					String path = String.format("%s/%s", info.ctag == TransferCTag.FlashSend ? info.url : info.storageDir, info.filename);
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
		case Apk:
			Bitmap tmp  = getApkIcon(path);
			if(tmp != null) {
				displayView.setImageBitmap(tmp);
			}else {
				displayView.setImageResource(type.iconResId);
			}
			break;
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

	private Bitmap getApkIcon(String path) {
		Bitmap bitmap = (Bitmap) cache.get(path);
		if(bitmap == null) {
			Drawable tmp = FileUtils.getApkIcon(context, path);
			if(tmp != null) {
				bitmap = ((BitmapDrawable)tmp).getBitmap();
				cache.put(path, bitmap);
			}
		}
		return bitmap;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.onItemClickListener = listener;
	}
	
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		this.onItemLongClickListener = listener;
	}
}