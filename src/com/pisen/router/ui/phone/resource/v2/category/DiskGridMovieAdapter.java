package com.pisen.router.ui.phone.resource.v2.category;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.squareup.picasso.Picasso;

/**
 * 视频视图适配器
 * 
 * @author ldj
 * @version 1.0 2015年5月18日 下午3:00:15
 */
public class DiskGridMovieAdapter extends DiskGridPictureAdapter {
	
	public DiskGridMovieAdapter(Context context) {
		super(context);
	}

	@Override
	public void handleItemView(int position, View convertView, ViewGroup parent) {
//		super.handleItemView(position, convertView, parent);
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
		Picasso.with(getContext()).load(new File(imagePath)).placeholder(R.drawable.thumbnail_video).into(imageView);
	}


}
