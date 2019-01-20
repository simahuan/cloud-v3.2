package com.pisen.router.ui.phone.flashtransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.pisen.router.R;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.ui.phone.settings.IconResource;

/**
 * 手机搜索视图,最多支持5个设备显示
 * @author ldj
 * @version 1.0 2015年5月20日 下午7:29:19
 */
public class SearchDeviceLayout extends FrameLayout {
	private ImageView lightView;
	private TextView tipView;
	private ObjectAnimator roateAnimator;
//	private List<DeviceViewHolder> deviceViews;
	private HashMap<String, DeviceViewHolder> deviceViews;
	private List<Rect> usedRects;
	private OnDeviceSelectListener listener;
	private static final int MAX_COUNT = 4;
	//最大随机计算位置次数，防止死循环
	private static final int MAX_RADOM_LOCATION_COUNT = 2000;
	private List<String> ips;
	private static final byte[] LOCK = new byte[0];

	public SearchDeviceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		View.inflate(context, R.layout.flash_transfer_searchdevice, this);

		findView();
		initView();
	}

	private void findView() {
		lightView = (ImageView) findViewById(R.id.imgLight);
		tipView = (TextView) findViewById(R.id.txtTip);
	}

	private void initView() {
		ips = new ArrayList<String>();
		deviceViews = new HashMap<String, SearchDeviceLayout.DeviceViewHolder>();
		usedRects = new ArrayList<Rect>();

		roateAnimator = ObjectAnimator.ofFloat(lightView, "rotation", 0, 360).setDuration(1500);
		roateAnimator.setRepeatCount(-1);
		roateAnimator.setInterpolator(new LinearInterpolator());

		//默认隐藏提示控件
		tipView.setVisibility(View.GONE);
	}

	private AnimatorSet getScaleAnimSet(View v) {
		AnimatorSet scaleAnimatorSet = new AnimatorSet();
		ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(v, "scaleX", 0.3f, 1f);
		ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(v, "scaleY", 0.3f, 1f);
		scaleAnimatorSet.setDuration(200);
		scaleAnimatorSet.play(scaleXAnimator).with(scaleYAnimator);

		return scaleAnimatorSet;
	}

	/**
	 * 开始扫描
	 */
	public void startScan() {
		resetView();
		startScanAnimation();
	}

	public void resetView() {
		tipView.setText("");
		tipView.setVisibility(View.GONE);
		clearDeviceView();

		lightView.setVisibility(View.VISIBLE);
	}

	/**
	 * 清空设备视图
	 */
	private void clearDeviceView() {
		if(deviceViews.size() >0) {
			for(DeviceViewHolder h : deviceViews.values()) {
				removeView(h.convertView);
			}
		}
		deviceViews.clear();
		usedRects.clear();
		ips.clear();
	}

	/**
	 * 开始扫描动画
	 */
	private void startScanAnimation() {
		roateAnimator.start();
	}

	/**
	 * 停止扫描
	 */
	public void stopScan() {
		if(roateAnimator.isRunning()) {
			roateAnimator.cancel();
			lightView.setRotation(0);
		}
	}
	
	/**
	 * 显示提示信息
	 * @param msg
	 */
	public void showTips(String msg) {
		if(!TextUtils.isEmpty(msg)) {
			clearDeviceView();
			
			tipView.setVisibility(View.VISIBLE);
			lightView.setVisibility(View.GONE);
			
			tipView.setText(msg);
		}
	}

	public int getDeviceCount() {
		return deviceViews.size();
	}
	public void addDevice(Collection<TransferDevice> devices) {
		if(devices != null && !devices.isEmpty()) {
			for(TransferDevice td : devices) {
				addDevice(td);
			}
		}
	}

	/**
	 * 新增设备
	 * @param device
	 */
	public void addDevice(TransferDevice device) {
		if(lightView.getVisibility() == View.VISIBLE) {
			lightView.setVisibility(View.GONE);
			roateAnimator.cancel();
		}
		addDeviceView(device);
	}

	/**
	 * 移除设备
	 * @param device
	 */
	public void removeDevice(TransferDevice device) {
		if(device != null) {
			if(device.isGateway) {
				removeView(deviceViews.get(device.ssid).convertView);
				deviceViews.remove(device.ssid);
				//XXX 需移除rect
			}else {
				DeviceViewHolder tmp = deviceViews.get(device.ip);
				if(tmp != null) {
					removeView(tmp.convertView);
					deviceViews.remove(device.ip);
				}
			}
		}
	}
	
//	public void removeAllDevice() {
//				removeView(deviceViews.get(device.ssid).convertView);
//				deviceViews.remove(device.ssid);
//				//XXX 需移除rect
//	}

	public void setOnDeviceSelectListener(OnDeviceSelectListener listener) {
		this.listener = listener;
	}

	/**
	 * 新增设备视图
	 * @param device
	 */
	private void addDeviceView(final TransferDevice device) {
		synchronized (LOCK) {
			if(deviceViews.size() >= MAX_COUNT ||(!device.isGateway && ips.contains(device.ip))) return;
			
			DeviceViewHolder holder = new DeviceViewHolder(getContext());
			
			String[] infos = device.deviceType.split("_");
			/*处理设备名称*/
			String name = device.deviceName;
			if(infos.length >0) {
//				name = String.format("%s的%s", device.deviceName, infos[0].equals(FlashTransferConfig.PHONE_TYPE_ANDROID) ? "Android" : "Iphone");
				holder.iconView.setImageResource(infos[0].equals(FlashTransferConfig.PHONE_TYPE_ANDROID) ? R.drawable.ic_andorid : R.drawable.ic_ios);
			}
			//设置名称
			holder.nameView.setText(name);
			
			//设置头像
			if(infos.length >1 && !TextUtils.isEmpty(infos[1]) && !infos[1].equalsIgnoreCase("null")) {
				int head = -1;
				try {
					head = Integer.parseInt(infos[1]);
				} catch (Exception e) {
				}
				holder.iconView.setImageBitmap(IconResource.getIconWithCustom(getContext(), head, true));
			}
			
			holder.convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(listener != null) {
						listener.selected(device);
					}
				}
			});
			holder.convertView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			FrameLayout.LayoutParams params = getAvailableLayoutParams(holder.convertView.getMeasuredWidth(), holder.convertView.getMeasuredHeight());
			addView(holder.convertView, params);
			
			if(device.isGateway) {
				deviceViews.put(device.ssid, holder);
			}else {
				ips.add(device.ip);
				deviceViews.put(device.ip, holder);
			}
			startDeviceViewAnim(holder.convertView);
		}
	}

	private void startDeviceViewAnim(View v) {
		getScaleAnimSet(v).start();
	}

	private Rect getRandomRect(int width, int height) {
		int x = (int) (Math.random() * (getWidth() - width));
		int y = 0;
		if(x == 0) {
			y = (int) (Math.random() * (getHeight() - height));
		}else {
			if(Math.random() >0.5) {
				y = getHeight() - height;
			}else {
				y = 0;
			}
		}
		return new Rect(x, y, x+ width, y+height);
	}

	private LayoutParams getAvailableLayoutParams(int width, int height) {
		Rect rect = getRandomRect(width, height);
		int count = 0;
		while(isOverlap(rect)) {
			count++;
			if(count >= MAX_RADOM_LOCATION_COUNT) {
				break;
			}
			rect = getRandomRect(width, height);
		}

		usedRects.add(rect);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = rect.left;
		params.topMargin = rect.top;

		return params;

	}
	
	private boolean isOverlap(Rect rect) {
		boolean result = false;
		if(usedRects != null && !usedRects.isEmpty()) {
			for(Rect h : usedRects) {
				if(Rect.intersects(h, rect)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 设备视图辅助类
	 * @author ldj
	 * @version 1.0 2015年5月21日 上午11:56:22
	 */
	private static class DeviceViewHolder {
		View convertView;
		ImageView iconView;
		TextView nameView;

		public DeviceViewHolder(Context ctx) {
			convertView  = View.inflate(ctx, R.layout.item_flashtransfer_device, null);
			iconView = (ImageView) convertView.findViewById(R.id.imgIcon);
			nameView = (TextView) convertView.findViewById(R.id.txtName);
		}
	}

	public static class TransferDevice {
		public String deviceName;
		public String deviceType;
		public String ip;
		public long lastModified; 	// 最后更新时间
		public boolean isGateway;	//
		public String ssid;		//ssid
		
		/**
		 * 获取设备头像id
		 * @return
		 */
		public int getIconResourceId() {
			int icon = -1;
			try {
				if (!TextUtils.isEmpty(deviceType)) {
					String[] infos = deviceType.split("_");
					//设置头像
					if (infos.length > 1) {
						icon = Integer.parseInt(infos[1]);
					}
				}
			} catch (Exception e) {
			}
			return icon;
		}
	}

	public static interface OnDeviceSelectListener {
		void selected(TransferDevice device);
	}

}
