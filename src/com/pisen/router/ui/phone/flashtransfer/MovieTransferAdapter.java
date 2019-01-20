package com.pisen.router.ui.phone.flashtransfer;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.pisen.router.R;
import com.squareup.picasso.Picasso;

/**
 * 视频视图适配器
 * @author ldj
 * @version 1.0 2015年5月18日 下午3:00:15
 */
public class MovieTransferAdapter extends PictureTransferAdapter {
	
	public MovieTransferAdapter(Context context) {
		super(context);
	}
	
	@Override
	public View newItemView(int position, View convertView, ViewGroup parent) {
		convertView = View.inflate(mContext, R.layout.item_flashtransfer_movie, null);

		ItemHolder holder = new ItemHolder();
		holder.picView = (ImageView) convertView.findViewById(R.id.img);
		holder.checkView = (CheckBox) convertView.findViewById(R.id.chkchoice);
		holder.picView.setLayoutParams(itemImageLayoutParams);
		convertView.setTag(holder);

		return convertView;
	}

	@Override
	protected void diplayImage(final String imagePath, final ImageView imageView) {
		Picasso.with(getContext()).load(new File(imagePath)).placeholder(R.drawable.thumbnail_video).error(R.drawable.thumbnail_video_fail).into(imageView);
	}
}
