package com.pisen.router.ui.phone.flashtransfer;

import java.io.File;

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
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.ui.adapter.TimeAxisSectionAdapter;
import com.squareup.picasso.Picasso;

/**
 * 图片视图适配器
 * 
 * @author ldj
 * @version 1.0 2015年5月18日 下午3:00:15
 */
public class PictureTransferAdapter extends TimeAxisSectionAdapter implements  OnClickListener, OnLongClickListener {
	public PictureTransferAdapter(Context context) {
		super(context);
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
		//显示图片
		diplayImage(info.path, holder.picView);
		convertView.setTag(Integer.MAX_VALUE, position);
		
		if(selectedSectionHeaders.contains(getHeaderId(position))) {//处于选中状态
			holder.checkView.setVisibility(View.VISIBLE);
			if(contains(selectedData, info)) {
				holder.checkView.setChecked(true);
			}else {
				holder.checkView.setChecked(false);
			}
		}else {//没有处于选中状态
			holder.checkView.setVisibility(View.GONE);
		}
		
		convertView.setOnLongClickListener(this);
		convertView.setOnClickListener(this);
	}

	@Override
	public View newHeaderView(int position, View convertView, ViewGroup parent) {
		convertView = View.inflate(mContext, R.layout.header_flashtransfer_pic, null);

		HeaderHolder holder = new HeaderHolder();
		holder.dateView = (TextView) convertView.findViewById(R.id.txtDate);
		holder.selectView = (TextView) convertView.findViewById(R.id.txtselect);

		convertView.setTag(holder);
		return convertView;
	}

	@Override
	public void handleHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderHolder holder = (HeaderHolder) convertView.getTag();
		holder.selectView.setTag(position);

		Object headerId = getHeaderId(position);
		if(selectedSectionHeaders.contains(headerId)) {//处于选中状态
			if(isSectionAllChecked(headerId)) {
				((TextView)holder.selectView).setText("取消全选");
			}else {
				((TextView)holder.selectView).setText("全选");
			}
		}else {//没有处于选中状态
			((TextView)holder.selectView).setText("选择");
		}
		holder.dateView.setText(getDate(getItem(position).lastModified * 1000));
		
		holder.selectView.setOnClickListener(this);
	}
	
	protected void diplayImage(String imagePath, final ImageView imageView) {
//		if(!new File(imagePath).exists()) {
//			Log.e("diplayImage", "file not exist->" + imagePath);
//		}
		Picasso.with(getContext()).load(new File(imagePath)).placeholder(R.drawable.thumbnail_pic).error(R.drawable.thumbnail_pic_fail).centerInside().resize(itemImageLayoutParams.width, itemImageLayoutParams.height).into(imageView);
	}
	
	@Override
	public boolean onLongClick(View v) {
		selectedSectionHeaders.add(getHeaderId((Integer) v.getTag(Integer.MAX_VALUE)));
		notifyDataSetChanged();
		if (itemLongClickListener != null) {
			return itemLongClickListener.onItemLongClick(null, v,(Integer) v.getTag(Integer.MAX_VALUE), v.getId());
		}
		return true;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.txtselect:	//选择
			Object headerId = getHeaderByPosition((Integer) v.getTag());
			if(selectedSectionHeaders.contains(headerId)) {
				checkSectionAll(headerId, !isSectionAllChecked(headerId));
			}else {
				selectedSectionHeaders.add(headerId);
			}
			notifyDataSetChanged();
			nofityCountChanged();
			break;
		case R.id.contentLayout:
			int position = (Integer)v.getTag(Integer.MAX_VALUE);
			if(selectedSectionHeaders.contains(getHeaderId(position))) {//多选
				toggleItemViewCheck(position,v, null);
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

	class ItemHolder {
		ImageView picView;
		CheckBox checkView;
	}

	private class HeaderHolder {
		TextView dateView;
		TextView selectView;
	}

	@Override
	public void toggleItemViewCheck(int position, View convertView, ViewGroup parent) {
		ResourceInfo info = getItem(position);
		if (!contains(selectedData, info)) {
			selectedData.add(info);
//			((ItemHolder) convertView.getTag()).checkView.setChecked(true);
		} else {
			remove(selectedData, info);
//			((ItemHolder) convertView.getTag()).checkView.setChecked(false);
		}
		notifyDataSetChanged();
//		nofityCountChanged();
	}
}
