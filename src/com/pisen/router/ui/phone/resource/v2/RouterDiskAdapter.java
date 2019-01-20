package com.pisen.router.ui.phone.resource.v2;

import java.util.List;

import android.content.Context;
import android.studio.view.widget.SimpleAdapter;
import android.studio.view.widget.ViewHolderBaseAdapter.VHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.core.monitor.DiskMonitor.DiskEntity;

/**
 * 文件浏览adapter
 * 
 * @author mugabutie
 * 
 */
public class RouterDiskAdapter extends SimpleAdapter<DiskEntity> {
	
	public RouterDiskAdapter(Context context) {
		super(context);
	}

	public RouterDiskAdapter(Context context, List<DiskEntity> data) {
		super(context, data);
	}

	static public class DiskViewHolder extends VHolder {

		public ImageView imgDiskIcon;
		public TextView txtDiskName;
		public ProgressBar progressStorage;
		public TextView txtUseStorage;
		public TextView txtFreeStorage;

		public DiskViewHolder(View itemView) {
			super(itemView);

			imgDiskIcon = (ImageView) itemView.findViewById(R.id.imgDiskIcon);
			txtDiskName = (TextView) itemView.findViewById(R.id.txtDiskName);
			progressStorage = (ProgressBar) itemView.findViewById(R.id.progressStorage);
			txtUseStorage = (TextView) itemView.findViewById(R.id.txtUseStorage);
			txtFreeStorage = (TextView) itemView.findViewById(R.id.txtFreeStorage);
		}

	}

	@Override
	public void bindView(Context arg0, View view, int position) {
		DiskEntity item = getItem(position);
		DiskViewHolder holder = (DiskViewHolder) view.getTag();
		holder.imgDiskIcon.setImageResource(item.getExtDiskMount() ? R.drawable.equipment_usb : R.drawable.filetype_cloudusb);
		holder.txtDiskName.setText(item.volume);
		holder.progressStorage.setProgress(item.getUsedPercent());
		holder.txtUseStorage.setText(String.format("已用: %s", item.getUsedString()));
		holder.txtFreeStorage.setText(String.format("可用: %s", item.getFreeString()));
	}

	@Override
	public View newView(Context context, ViewGroup parent, int i) {
		View itemView = View.inflate(context, R.layout.resource_home_item, null);
		itemView.setTag(new DiskViewHolder(itemView));
		return itemView;
	}

}
