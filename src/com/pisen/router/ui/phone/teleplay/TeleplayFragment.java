package com.pisen.router.ui.phone.teleplay;

import android.os.Bundle;
import android.studio.os.NetUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.monitor.DiskMonitor;
import com.pisen.router.core.monitor.DiskMonitor.OnDiskChangedListener;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.core.monitor.WifiMonitor.WifiStateCallback;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.phone.settings.HuiYuanDiFragment;
import com.wefi.zhuiju.activity.follow.FollowFragment;
import com.wefi.zhuiju.activity.global.SnUtil;

/**
 * 追剧
 * 
 * @author yangyp
 */
public class TeleplayFragment extends FollowFragment implements WifiStateCallback,OnDiskChangedListener {
	
	private View zxView;
	private View pisenView;
	private ImageView leftBtn,rightBtn;
	private LinearLayout leftLayout,rightLayout;
	private TextView txtTitle;
	private Button pisen_btnShop,pisen_btnLook;
	private DiskMonitor diskMonitor;
	private WifiMonitor wifiMonitor;
	
	@Override
	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
		zxView = super.onCreateView(arg0, arg1, arg2);
		pisenView = View.inflate(getActivity(), R.layout.pisen_zhuiju_unconnect, null);
		FrameLayout rootView = new FrameLayout(getActivity());
		rootView.addView(zxView);
		rootView.addView(pisenView);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		View view = getView();
		TextView title = (TextView) view.findViewById(com.wefi.zhuiju.R.id.action_title_tv);
		txtTitle = (TextView) pisenView.findViewById(R.id.pisen_title_tv);
		txtTitle.setText(title.getText());
		pisen_btnLook = (Button) pisenView.findViewById(R.id.pisen_btnLook);
		pisen_btnShop = (Button) pisenView.findViewById(R.id.pisen_btnShop);
		pisen_btnShop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentActivity.startFragment(getActivity(), HuiYuanDiFragment.class);
			}
		});
		pisen_btnLook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 SnUtil.goOnlineVideo(getActivity());
			}
		});
		
		leftLayout = (LinearLayout) view.findViewById(com.wefi.zhuiju.R.id.action_btn1_ll);
		leftBtn = (ImageView) view.findViewById(com.wefi.zhuiju.R.id.action_btn1_iv);
		leftBtn.setImageResource(R.drawable.actionbar_follow_pisen);
		rightLayout = (LinearLayout) view.findViewById(com.wefi.zhuiju.R.id.action_btn2_ll);
		rightBtn = (ImageView) view.findViewById(com.wefi.zhuiju.R.id.action_btn2_iv);
		rightBtn.setImageResource(R.drawable.actionbar_download_pisen);

		wifiMonitor = WifiMonitor.getInstance();
		wifiMonitor.registerObserver(this);
		diskMonitor = DiskMonitor.getInstance();
		diskMonitor.registerObserver(this);
		setHeadView(NetUtils.isWifiConnected(getActivity()));
	}
	
	/**
	 * 是否显示展示页面
	 * @param isWifiEnable
	 */
	private void setHeadView(boolean isWifiEnable){
		if (isWifiEnable) {
			if (Model.RZHIXIANG.equals(ResourceConfig.getInstance(getActivity()).getDeviceMode())){
				leftLayout.setVisibility(View.VISIBLE);
				rightLayout.setVisibility(View.VISIBLE);
				zxView.setVisibility(View.VISIBLE);
				pisenView.setVisibility(View.GONE);
				return;
			}
		}
		leftLayout.setVisibility(View.GONE);
		rightLayout.setVisibility(View.GONE);
		zxView.setVisibility(View.GONE);
		pisenView.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			if (wifiMonitor != null) {
				wifiMonitor.unregisterObserver(this);
			}
			if (diskMonitor != null) {
				diskMonitor.unregisterObserver(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void onDisconnected(WifiConfig config) {
		setHeadView(false);
	}

	@Override
	public void onConnected(WifiConfig config) {}

	@Override
	public void onDiskChanged() {
		setHeadView(true);
	}
	
}
