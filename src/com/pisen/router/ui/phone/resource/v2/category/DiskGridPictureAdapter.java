package com.pisen.router.ui.phone.resource.v2.category;

import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.studio.util.URLUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.ui.adapter.TimeAxisSectionAdapter;
import com.squareup.picasso.Picasso;

/**
 * 图片视图适配器
 * 
 * @author ldj
 * @version 1.0 2015年5月18日 下午3:00:15
 */
public class DiskGridPictureAdapter extends TimeAxisSectionAdapter implements OnClickListener, OnLongClickListener {
	public boolean showCheck;

	public DiskGridPictureAdapter(Context context) {
		super(context);
	}

	public boolean isCheckedEnabled() {
		return showCheck;
	}

	public void setCheckedEnable(boolean check) {
		showCheck = check;
		if (!check) {
			reset();
		}
	}

	public void selectAll() {
		if (mData != null && !mData.isEmpty()) {
			selectedData.clear();
			selectedData.addAll(mData);
			notifyDataSetChanged();
		}
	}

	public class ItemHolder {
		ImageView picView;
		CheckBox checkView;
	}

	private class HeaderHolder {
		TextView dateView;
	}

	@Override
	public boolean onLongClick(View v) {
		if (itemLongClickListener != null) {
			return itemLongClickListener.onItemLongClick(null, v, (Integer) v.getTag(Integer.MAX_VALUE), v.getId());
		}
		return true;
	}

	public void reset() {
		selectedData.clear();

		notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.contentLayout:
			if (itemClickListener != null) {
				itemClickListener.onItemClick(null, v, (Integer) v.getTag(Integer.MAX_VALUE), id);
			}
			break;

		default:
			break;
		}

	}

	@Override
	public View newItemView(int position, View convertView, ViewGroup parent) {
		convertView = View.inflate(mContext, R.layout.item_flashtransfer_pic, null);

		ItemHolder holder = new ItemHolder();
		holder.picView = (ImageView) convertView.findViewById(R.id.img);
		holder.checkView = (CheckBox) convertView.findViewById(R.id.chkchoice);
		holder.picView.setLayoutParams(itemImageLayoutParams);
		convertView.setTag(holder);

		return convertView;
	}

	@Override
	public void handleItemView(int position, View convertView, ViewGroup parent) {
		ItemHolder holder = (ItemHolder) convertView.getTag();

		ResourceInfo info = getItem(position);
		// 显示图片
		Object tmp = convertView.getTag(Integer.MAX_VALUE) ;
		diplayImage(info.path, holder.picView, tmp == null ? true : (Integer)tmp!= position);
		convertView.setTag(Integer.MAX_VALUE, position);

		if (showCheck) {// 处于选中状态
			holder.checkView.setVisibility(View.VISIBLE);
			if (selectedData.contains(info)) {
				holder.checkView.setChecked(true);
			} else {
				holder.checkView.setChecked(false);
			}
		} else {// 没有处于选中状态
			holder.checkView.setVisibility(View.GONE);
		}

		convertView.setOnLongClickListener(this);
		convertView.setOnClickListener(this);
	}
	
	protected void diplayImage(String imagePath, final ImageView imageView, final boolean showLoading) {		
		Uri uri = URLUtil.isHttpUrl(imagePath) ? Uri.parse(URLUtils.encodeURL(imagePath)) : Uri.fromFile(new File(imagePath));
		Picasso.with(getContext()).load(uri).placeholder(R.drawable.thumbnail_pic).resize(itemImageLayoutParams.width*2, itemImageLayoutParams.height*2).centerInside().into(imageView);
	}

	@Override
	public View newHeaderView(int position, View convertView, ViewGroup parent) {
		convertView = View.inflate(mContext, R.layout.header_transfer_grid, null);

		HeaderHolder holder = new HeaderHolder();
		holder.dateView = (TextView) convertView.findViewById(R.id.txtDate);

		convertView.setTag(holder);

		holder.dateView.setText(getDate(getItem(position).lastModified * 1000));

		return convertView;
	}

	@Override
	public void handleHeaderView(int position, View convertView, ViewGroup parent) {
	}

	@Override
	public void toggleItemViewCheck(int position, View convertView, ViewGroup parent) {
		ResourceInfo info = getItem(position);
		if (!contains(selectedData, info)) {
			selectedData.add(info);
			((ItemHolder) convertView.getTag()).checkView.setChecked(true);
		} else {
			remove(selectedData, info);
			((ItemHolder) convertView.getTag()).checkView.setChecked(false);
//			selectedData.remove(info);
		}
//		notifyDataSetChanged();
	}

}
