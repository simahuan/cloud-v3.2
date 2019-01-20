package com.pisen.router.ui.phone.resource.musicplayer;

import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.Device;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pisen.router.R;

/**
 * 音乐播放设备选择pop
 * @author ldj
 * @version 1.0 2015年5月14日 上午9:28:23
 */
public class MusicDeviceSelectPopupWindow extends PopupWindow implements OnItemClickListener{
	
	private Context context;
	private View contentView;
	private ListView listView;
	private OnItemClickListener listener;
	private String selectedDeviceName;
	
	public MusicDeviceSelectPopupWindow(Context context) {
		super(context);
		
		this.context = context;
		contentView = LayoutInflater.from(context).inflate(R.layout.popupwindow_music_device_select, null);
		setContentView(contentView);
		setWidth(context.getResources().getDimensionPixelOffset(R.dimen.popupwindow_music_width));
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true); 
		setTouchable(true); 
		setBackgroundDrawable(new BitmapDrawable());
		
		findView();
		initView();
	}

	private void initView() {
	}
	
	private void findView() {
		listView = (ListView) contentView.findViewById(R.id.lstDevice);
	}

	public void setData(List<Device> data) {
		setData(data, null);
	}
	
	public void setData(List<Device> data, Device selectedDevice) {
		if(selectedDevice != null) {
			selectedDeviceName = selectedDevice.getFriendlyName();
		}else {
			//默认首项选中
			selectedDeviceName = data.get(0).getFriendlyName();
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.item_device_music, getDeviceNames(data)){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView textVew = (TextView) super.getView(position, convertView, parent);
				textVew.setTextColor(0XFFFFFFFF);
				if(!TextUtils.isEmpty(selectedDeviceName)) {
					if(getItem(position).equals(selectedDeviceName)) {
						textVew.setTextColor(0XFF4d9dff);
					}
				}
				return textVew;
			}
		};
		
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}
	
	private List<String> getDeviceNames(List<Device> devices) {
		List<String> names = null;
		if(devices != null) {
			names = new ArrayList<String>();
			int size = devices.size();
			for(int i=0; i<size; i++) {
				names.add(devices.get(i).getFriendlyName());
			}
		}
		return names;
	}
	
	/**
	 * 设置点击监听
	 * @param listener
	 */
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectedDeviceName =String.valueOf(parent.getAdapter().getItem(position));
		if(listener != null) {
			listener.onItemClick(parent, view, position, id);
		}
	}
}
