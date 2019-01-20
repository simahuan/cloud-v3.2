package com.pisen.router.ui.adapter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceInfo;

/**
 * 时间轴适配器
 * @author ldj
 * @version 1.0 2015年6月8日 下午12:19:57
 */
public abstract  class TimeAxisSectionAdapter extends GridSectionChoiceAdapter<ResourceInfo> {
	// 列数
	private int numCloumns;
	private int itemWidth;
	protected FrameLayout.LayoutParams itemImageLayoutParams;
	
	public TimeAxisSectionAdapter(Context context) {
		super(context);

		initParams();
	}
	
	/**
	 * 根据屏幕尺寸、密度等信息初始化item参数
	 */
	private void initParams() {
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int contentWidth = (int) (screenWidth - 75 * dm.density);

		Bitmap mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.thumbnail_pic);
		int iconWidth = mBitmap.getWidth();

		numCloumns = (int) ((contentWidth - 12 * dm.density ) / (iconWidth ));
		itemWidth = (int) ((contentWidth - 12 * dm.density )/numCloumns);
		itemImageLayoutParams = new FrameLayout.LayoutParams(itemWidth, itemWidth);
	}

	public int getNumCloumns() {
		return numCloumns;
	}
	
	/**
	 * 数据集中是否包含
	 * @param data
	 * @param info
	 * @return
	 */
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
	
	public boolean remove(List<ResourceInfo> data,ResourceInfo info) {
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

	@Override
	public long getHeaderId(int position) {
		return  Long.parseLong(timeToDate(getItem(position).lastModified * 1000));
	}
	
	/**
	 * 把时间转换为date数值，去掉小时、分钟、秒值,如19990213
	 * @param time
	 * @return
	 */
	private String timeToDate(long time) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(time));
		return String.format("%s%s%s", c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
	}

	protected String getDate(long date) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(date));
		return String.format("%s/%s", c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
	}

	@Override
	protected Object getHeaderByPosition(int position) {
		return getHeaderId(position);
	}

	protected void nofityCountChanged() {
		if(countChangeListener != null) {
			countChangeListener.selectedCount(selectedData.size());
		}
	}
}
