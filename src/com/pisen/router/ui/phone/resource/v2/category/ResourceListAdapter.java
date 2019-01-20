package com.pisen.router.ui.phone.resource.v2.category;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.studio.util.URLUtils;
import android.studio.view.widget.ChoiceAdapter;
import android.studio.view.widget.ViewHolderBaseAdapter.VHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.squareup.picasso.Picasso;

public class ResourceListAdapter extends ChoiceAdapter<ResourceInfo> {

	public interface OnCheckBoxAreaClickListener {
		void onCheckBoxAreaClick(View v, int position);
	}

	protected OnCheckBoxAreaClickListener checkBoxAreaClickListener;

	public ResourceListAdapter(Context context) {
		super(context);
	}

	public ResourceListAdapter(Context context, List<ResourceInfo> data) {
		super(context, data);
	}

	public void setOnCheckBoxAreaClickListener(OnCheckBoxAreaClickListener listener) {
		this.checkBoxAreaClickListener = listener;
	}

	@Override
	public boolean isItemChecked(ResourceInfo item) {
		for (ResourceInfo _info : getItemCheckedAll()) {
			if (_info.path.equals(item.path)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void removeItemChecked(ResourceInfo item) {
		if(getItemCheckedAll() != null) {
			for(ResourceInfo tmp : getItemCheckedAll()) {
				if(tmp.path.equals(item.path)) {
					getItemCheckedAll().remove(tmp);
					break;
				}
			}
		}
	}

	@Override
	public View newView(Context context, ViewGroup parent, final int position) {
		final View itemView = View.inflate(context, R.layout.resource_home_file_item, null);
		itemView.setTag(new ResourceHolder(itemView));
		return itemView;
	}

	@Override
	public void bindView(Context context, final View view, final int position) {
		ResourceHolder holder = (ResourceHolder) view.getTag();
		holder.checkAreaLayout.setOnClickListener(checkBoxAreaClickListener(view, position));
		holder.checkAreaLayout.setClickable(checkBoxAreaClickListener != null);

		ResourceInfo item = getItem(position);
		if (item.isDirectory) {
			Picasso.with(getContext()).load(R.drawable.ic_file_folder).placeholder(R.drawable.ic_file_folder).into(holder.imgIcon);
			holder.txtSize.setVisibility(View.GONE);
		} else {
			if (ResourceCategory.isFileType(item.name, FileType.Image)) {
				diplayImage(holder.imgIcon, item.path,item.getIconResId());
			} else if (ResourceCategory.isFileType(item.name, FileType.Video)) {
				diplayVideo(holder.imgIcon, item.path,item.getIconResId());
			}else {
				Picasso.with(getContext()).load(item.getIconResId()).placeholder(item.getIconResId()).into(holder.imgIcon);
			}

			holder.txtSize.setVisibility(View.VISIBLE);
			holder.txtSize.setText(item.getSizeString());
		}
		holder.txtName.setText(item.name);
		holder.txtDateUpdated.setText(item.getLastModifiedString());
		holder.checkStatus.setChecked(isItemChecked(position));
		holder.checkStatus.setVisibility(isCheckedEnabled() ? View.VISIBLE : View.INVISIBLE);
		holder.checkAreaLayout.setVisibility(isCheckedEnabled() ? View.VISIBLE : View.INVISIBLE);
	}

	protected void diplayImage(final ImageView imageView, String imagePath, int defaultResId) {
		Uri uri = URLUtil.isHttpUrl(imagePath) ? Uri.parse(URLUtils.encodeURL(imagePath)) : Uri.fromFile(new File(imagePath));
		Picasso.with(getContext()).load(uri).resize(200, 200).centerInside().placeholder(defaultResId).into(imageView);
	}

	protected void diplayVideo(final ImageView imageView, String imagePath, int defaultResId) {
		Uri uri = URLUtil.isHttpUrl(imagePath) ? Uri.parse(URLUtils.encodeURL(imagePath)) : Uri.fromFile(new File(imagePath));
		Picasso.with(getContext()).load(uri).placeholder(defaultResId).into(imageView);
	}

	private OnClickListener checkBoxAreaClickListener(final View view, final int position) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkBoxAreaClickListener != null) {
					checkBoxAreaClickListener.onCheckBoxAreaClick(view, position);
				}
			}
		};
	}

	static public class ResourceHolder extends VHolder {

		public ImageView imgIcon;
		public TextView txtName;
		public TextView txtDateUpdated;
		public TextView txtSize;
		public FrameLayout checkAreaLayout;
		public CheckBox checkStatus;

		public ResourceHolder(View itemView) {
			super(itemView);
			imgIcon = (ImageView) itemView.findViewById(R.id.imgIcon);
			txtName = (TextView) itemView.findViewById(R.id.txtName);
			txtDateUpdated = (TextView) itemView.findViewById(R.id.txtDateUpdated);
			txtSize = (TextView) itemView.findViewById(R.id.txtSize);
			checkAreaLayout = (FrameLayout) itemView.findViewById(R.id.checkAreaLayout);
			checkStatus = (CheckBox) itemView.findViewById(R.id.checkStatus);
		}
	}

}
