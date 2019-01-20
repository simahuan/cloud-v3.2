package com.pisen.router.ui.phone.resource.v2;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Message;
import android.studio.os.LogCat;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.dialog.DeviceSearchDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.utils.WifiSearcher;
import com.pisen.router.common.utils.WifiSearcher.ErrorType;
import com.pisen.router.common.utils.WifiSearcher.SearchWifiListener;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.base.FragmentSupport;
import com.pisen.router.ui.phone.device.DeviceListActivity;
import com.pisen.router.ui.phone.device.bean.WifiBean;
import com.pisen.router.ui.phone.flashtransfer.FlashTransferNetUtil;
import com.pisen.router.ui.phone.leftmenu.LeftMenuFragment;
import com.pisen.router.ui.phone.settings.HuiYuanDiFragment;

/**
 * 私有云
 * 
 * @author yangyp
 */
public class RouterShopPanel implements OnClickListener {

	private LinearLayout shopLayout;
	private Context mContext;
	private FlashTransferNetUtil mFlashTransferNetUtil;
	public RouterShopPanel(FragmentSupport fragment,Context ctx) {
		mContext = ctx;
		shopLayout = (LinearLayout) fragment.findViewById(R.id.shopLayout);
		fragment.findViewById(R.id.btnScan).setOnClickListener(this);
		fragment.findViewById(R.id.btnShop).setOnClickListener(this);
		mFlashTransferNetUtil = FlashTransferNetUtil.getInstance(mContext);
	}

	public void show() {
		shopLayout.setVisibility(View.VISIBLE);
	}

	public void hide() {
		shopLayout.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnScan:
			searchDevices();
			break;
		case R.id.btnShop:
			FragmentActivity.startFragment(mContext, HuiYuanDiFragment.class);
			break;
		}

	}
	
	/**
	 * 查找设备
	 */
	boolean isGiveUp = false; //放弃　
	private DeviceSearchDialog waitDialog;
	public void searchDevices() {
		isGiveUp = false;
		if (waitDialog == null) {
			waitDialog = new DeviceSearchDialog(mContext);
			waitDialog.setOnKeyListener(new OnKeyListener() {				
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					isGiveUp = true;
					releaseDialog();
					return true;
				}
			});
		}
		if (!waitDialog.isShowing()) {
			waitDialog.show();
		}
		
		if (mFlashTransferNetUtil.isWifiApEnabled()){
			mFlashTransferNetUtil.closeAp();
		}
		
		WifiSearcher searcher = new WifiSearcher(mContext, new SearchWifiListener() {
			@Override
			public void onSearchWifiSuccess(List<ScanResult> results) {
				releaseDialog();
				LogCat.i("wifi list:"+results.size());
				if (isGiveUp) {
					return;
				}
				if (!results.isEmpty()) {
					ArrayList<WifiBean> result = new ArrayList<WifiBean>();
					WifiBean wifi = null;
					for (ScanResult scanResult : results) {
						if (scanResult.BSSID.startsWith(WifiMonitor.PISEN_BSSID_PREFIX)) {
							wifi = new WifiBean();
							wifi.setSsid(scanResult.SSID);
							wifi.setSignal(scanResult.level+"");
							String pwdType = scanResult.capabilities;
							if (pwdType.contains("WEP")) {
								wifi.setEncryption("WEP");
							}else if (pwdType.contains("WPA")) {
								wifi.setEncryption("WPA");
							}else if (pwdType.contains("WPA2")) {
								wifi.setEncryption("WPA2");
							}else{
								wifi.setEncryption("");
							}
							result.add(wifi);
						}
					}
					
					Intent i = new Intent(mContext, DeviceListActivity.class);
					i.putExtra("list", result);
					mContext.startActivity(i);
					results.clear();
					result.clear();
				}else{
					mHandler.sendEmptyMessage(LeftMenuFragment.MSG_REFRESH_DEVICE_DIALOG);
				}
			}
			 
			@Override
			public void onSearchWifiFailed(ErrorType errorType) {
				releaseDialog();
				if (mHandler != null) {
					if (errorType == ErrorType.NO_WIFI_FOUND) {
						mHandler.sendEmptyMessage(LeftMenuFragment.MSG_REFRESH_DEVICE_DIALOG);
					}else if (errorType == ErrorType.SEARCH_WIFI_TIMEOUT) {
						mHandler.sendEmptyMessage(LeftMenuFragment.MSG_SCAN_DEVICE_TIMEOUT);
					}else if (errorType == ErrorType.SEARCH_WIFI_TIMEOUT) {
						mHandler.sendEmptyMessage(LeftMenuFragment.MSG_SCAN_DEVICE_TIMEOUT);
					}else{
						mHandler.sendEmptyMessage(LeftMenuFragment.MSG_DEVICE_SCAN_FAILED);
					}
				}
			}
		});
		searcher.search();
	}

	/**
	 * 用户停止搜索
	 */
	private void releaseDialog(){
		if (waitDialog != null) {
			waitDialog.dismiss();
			waitDialog = null;
		}
	}
	
	/**
	 * 显示查找设备对话框
	 */
	private void showDeviceSearchDialog(){
		// 未发现设备
		ConfirmDialog dialog = new ConfirmDialog(mContext);
		dialog.setTitle("未检测到可连接的设备");
		dialog.setMessage("1,如果您已经有品胜云路由请检查后重新查找  \n2,如果您还没有品胜云路由可以点击购买");
		dialog.setNegativeButton("重新查找", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				dialog = null;
				searchDevices();
			}
		});
		dialog.setPositiveButton("点击购买", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				FragmentActivity.startFragment(mContext, HuiYuanDiFragment.class);
				dialog.dismiss();
				dialog = null;
			}
		});
		dialog.show();
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == LeftMenuFragment.MSG_REFRESH_DEVICE_DIALOG) {
				showDeviceSearchDialog();
			}else if (msg.what == LeftMenuFragment.MSG_SCAN_DEVICE_TIMEOUT){
				UIHelper.showToast(mContext, "扫描设备超时");
			}else if (msg.what == LeftMenuFragment.MSG_AP_ENABLED) {
				UIHelper.showToast(mContext, "请先关闭热点功能后再试");
			}else if(msg.what == LeftMenuFragment.MSG_DEVICE_SCAN_FAILED){
				UIHelper.showToast(mContext, "扫描设备失败,请检查后再试");
			}
		}
	};
	
	
}
