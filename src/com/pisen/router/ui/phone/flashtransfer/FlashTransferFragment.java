package com.pisen.router.ui.phone.flashtransfer;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.studio.os.LogCat;
import android.studio.os.PreferencesUtils;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.pisen.router.R;
import com.pisen.router.common.TimeIntervalClickListener;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.view.FlashTransferGuidePopuWindow;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.core.flashtransfer.FlashTransferManager;
import com.pisen.router.core.flashtransfer.scan.DeviceContainer;
import com.pisen.router.core.flashtransfer.scan.DeviceContainer.OnDeviceChangeListener;
import com.pisen.router.core.flashtransfer.scan.DeviceScanService;
import com.pisen.router.core.flashtransfer.scan.DeviceScanService.DeviceScanBinder;
import com.pisen.router.core.flashtransfer.scan.protocol.UserInfoPtlV2;
import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.base.FragmentSupport;
import com.pisen.router.ui.phone.settings.IconResource;

/**
 * 闪电互传fragment
 * @author ldj
 * @version 1.0 2015年5月25日 下午4:09:11
 */
public class FlashTransferFragment extends FragmentSupport implements OnPageChangeListener, OnDeviceChangeListener {

	private View contentView;
	private DeviceContainer container;
	private PagerSlidingTabStrip tabStrip;
	private ViewPager viewPager;
	private FlashTransferFragmentAdapter adapter;
	private ImageButton connectButton;
	private ImageButton routerButton;
	private ImageButton recordButton;
	private ImageView newRecordTipView;
	private View usersView;
	private LinearLayout userListLayout;
	private ViewGroup transferCountLayout;
	private ImageButton closeConnectButton;
	// 当前显示fragment位置
	private int curPosition = 0;
	private static final int REQUEST_CONNECT = 0X1000;
	private TimeIntervalClickListener clickListener;
	
    private FlashTransferGuidePopuWindow guideStepPopuWindow;
    private static View guideStepView;
	private  Handler mHandler;
	private static final String KEY_TRANSFER_FIRST_INIT = "is_transfer_first_init";
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = inflater.inflate(R.layout.flash_transfer_activity, container, false);

		findView();
		initView();
		return contentView;
	}

	@Override
	public void onResume() {
		super.onResume();

		container.registOnDeviceChangeListener(this);
		refreshDeviceView(container.getUserList());
		refreshNewRecordTipView();
		registReceiver();
		((HomeActivity) getActivity()).setSlidingMenuScrollable(false);
	}

	private void refreshNewRecordTipView() {
		long readTime = PreferencesUtils.getLong(KeyUtils.TIME_LAST_READ, 0);
		long recvTime = PreferencesUtils.getLong(KeyUtils.TIME_LAST_RECV, 0);
		if (recvTime > readTime) {
			newRecordTipView.setVisibility(View.VISIBLE);
		} else {
			newRecordTipView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		container.unregistOnDeviceChangeListener(this);
		unregisterReceiver();
		cancelGuidePopuView();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
	
    /**
     * @describtion 蒙板向导
     */
	private void GuideStepPopuView() {
		final Window window = getActivity().getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.8f;
		window.setAttributes(lp);
		guideStepPopuWindow = new FlashTransferGuidePopuWindow(getActivity());
		guideStepPopuWindow.showAtLocation(FlashTransferFragment.this.findViewById(R.id.llayoutFlashTransfer), Gravity.CENTER
				| Gravity.CENTER, 0, 0);
//		guideStepPopuWindow.update();
		guideStepPopuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				WindowManager.LayoutParams lp = window.getAttributes();
				lp.alpha = 1f;
				window.setAttributes(lp);
				guideStepView = null;
				guideStepPopuWindow = null;
				System.gc();
			}
		});
	}

    private void initGuideStepView() {
        guideStepView = contentView.findViewById(R.id.llayoutFlashTransfer);
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable,200);
    }
	
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (guideStepView.getWidth() > 0) {
				GuideStepPopuView();
			} else {
				mHandler.postDelayed(this, 100);
			}
		}
	};
    
	/**
	 * @des  取消引导
	 */
	private void cancelGuidePopuView(){
		if (null != guideStepPopuWindow){
			guideStepPopuWindow.dismiss();
			guideStepPopuWindow = null;
			System.gc();
		}
	}
	
	private NewRecvReceiver newRecvReceiver;

	/**
	 * 注册刷新视图广播接收器
	 */
	private void registReceiver() {
		IntentFilter in = new IntentFilter();
		in.addAction(FlashTransferManager.ACTION_TRANSFER_RECEIVE_BEGIN);
		newRecvReceiver = new NewRecvReceiver();
		getActivity().registerReceiver(newRecvReceiver, in);
	}

	/**
	 * 注销广播接收器
	 */
	private void unregisterReceiver() {
		getActivity().unregisterReceiver(newRecvReceiver);
	}

	/**
	 * 初始化控件
	 */
	private void findView() {
		usersView = contentView.findViewById(R.id.usersLayout);
		userListLayout = (LinearLayout) contentView.findViewById(R.id.userListLayout);
		transferCountLayout = (ViewGroup) contentView.findViewById(R.id.countSendLayout);
		closeConnectButton = (ImageButton) contentView.findViewById(R.id.ibtnClose);
		viewPager = (ViewPager) contentView.findViewById(R.id.pager);
		tabStrip = (PagerSlidingTabStrip) contentView.findViewById(R.id.tabs);
		connectButton = (ImageButton) contentView.findViewById(R.id.ibtnFlashTransfer);
		routerButton = (ImageButton) contentView.findViewById(R.id.ibtnRouter);
		recordButton = (ImageButton) contentView.findViewById(R.id.ibtnRecord);
		newRecordTipView = (ImageView) contentView.findViewById(R.id.imgNewRecord);
	}

	/**
	 * 控件界面初始
	 */
	private void initView() {
		container = DeviceContainer.getInstance(getActivity().getApplicationContext());
		adapter = new FlashTransferFragmentAdapter(getChildFragmentManager());
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(1);
		tabStrip.setViewPager(viewPager);
		clickListener = new TimeIntervalClickListener() {
			@Override
			public void onTimeIntervalClick(View v) {
				FlashTransferFragment.this.onClick(v);
			}
		};

		tabStrip.setOnPageChangeListener(this);
		container.registOnDeviceChangeListener(this);
		connectButton.setOnClickListener(clickListener);
		routerButton.setOnClickListener(clickListener);
		recordButton.setOnClickListener(clickListener);
		closeConnectButton.setOnClickListener(clickListener);
		transferCountLayout.setOnClickListener(clickListener);
		
		if (PreferencesUtils.getBoolean(KEY_TRANSFER_FIRST_INIT, true)) {
			PreferencesUtils.setBoolean(KEY_TRANSFER_FIRST_INIT, false);
			initGuideStepView();
		}
	}

	/**
	 * 初始化已连接设备视图
	 */
	private void refreshDeviceView(List<UserInfoPtlV2> users) {
		if (users != null && !users.isEmpty()) {
			connectButton.setVisibility(View.GONE);
			usersView.setVisibility(View.VISIBLE);
			userListLayout.removeAllViews();
			initUserListLayout(users);
		} else {
			connectButton.setVisibility(View.VISIBLE);
			usersView.setVisibility(View.GONE);
			// 隐藏收发视图
			transferCountLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 初始化用户列表视图
	 */
	private void initUserListLayout(List<UserInfoPtlV2> users) {
		int size = users.size();
		UserInfoPtlV2 user = null;
		for (int i = 0; i < size; i++) {
			user = users.get(i);
			userListLayout.addView(initUserView(user));
		}
	}

	/**
	 * 根据用户信息初始化用户列表
	 * @param user 在线用户
	 * @return
	 */
	private View initUserView(UserInfoPtlV2 user) {
		View v = View.inflate(getActivity(), R.layout.item_flashtransfer_device_connected, null);
		((TextView) v.findViewById(R.id.txtName)).setText(user.hostName);
		String[] infos = user.hostType.split("_");
		// 设置设备类型
		if (infos.length > 0) {
			((ImageView) v.findViewById(R.id.imgPhoneType)).setImageResource(infos[0].equals(FlashTransferConfig.PHONE_TYPE_ANDROID) ? R.drawable.ic_andorid
					: R.drawable.ic_ios);
		}

		// 设置头像
		if (infos.length > 1) {
			int head = -1;
			try {
				head = Integer.parseInt(infos[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			((ImageView) v.findViewById(R.id.imgHead)).setImageBitmap(IconResource.getIconWithCustom(getActivity(), head));
		}
		return v;
	}

	/**
	 * 闪传按钮显示抖动动画
	 */
	private void showConnectShakeAnimation() {
		Animation anim = new TranslateAnimation(0, 5, 0, 5);
		anim.setRepeatCount(6);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setRepeatMode(Animation.REVERSE);
		anim.setDuration(40);
		connectButton.startAnimation(anim);
	}

	private class FlashTransferFragmentAdapter extends FragmentPagerAdapter {
		private List<FlashTransferContentFragment> fragmentList;

		public FlashTransferFragmentAdapter(FragmentManager fm) {
			super(fm);

			fragmentList = new ArrayList<FlashTransferContentFragment>();
			fragmentList.add(new PictureTransferFragment());
			fragmentList.add(new MovieTransferFragment());
			fragmentList.add(new MusicTransferFragment());
			fragmentList.add(new DocumentTransferFragment());
			fragmentList.add(new APKTransferFragment());
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return fragmentList.get(position).getTitle();
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}
	}

	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.ibtnFlashTransfer:
			startActivityForResult(new Intent(getActivity(), ConnectManageActivity.class), REQUEST_CONNECT);
			getActivity().overridePendingTransition(R.anim.alpha_activity_in, 0);
			break;
		case R.id.ibtnClose: // 停止服务,断开连接
			disconnectDialog();
			break;
		case R.id.countSendLayout:
			startActivity(new Intent(getActivity(), FlashTransferRecordActivity.class));
			break;
		case R.id.ibtnRouter:
			// 关闭选择状态
			dissmissBottomMenu();
			((HomeActivity) getActivity()).toggleMenu();
			break;
		case R.id.ibtnRecord:
			startActivity(new Intent(getActivity(), FlashTransferRecordActivity.class));
			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT:
			showConnectShakeAnimation();
			break;
		default:
			break;
		}
	}

	/**
	 * @des 断开(同小伙伴)连接
	 */
	public void disconnectDialog() {
		ConfirmDialog cd = new ConfirmDialog(getActivity());
		cd.setMessage("确定不再传会了 ?");
		cd.setNegativeButton("取消", null);
		cd.setPositiveButton("断开", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				boolean result = getActivity().bindService(new Intent(getActivity(), DeviceScanService.class), new ServiceConnection() {

					@Override
					public void onServiceDisconnected(ComponentName name) {
					}

					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						((DeviceScanBinder) service).getService().releaseAllResouce();
						((DeviceScanBinder) service).getService().stopSelf();
						getActivity().unbindService(this);
					}
				}, Service.BIND_AUTO_CREATE);
				if (!result) {
					LogCat.e("close connect called, but bind service failed!!!");
					getActivity().stopService(new Intent(getActivity(), DeviceScanService.class));
				}
			}
		});
		cd.show();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		if (curPosition > -1)
			dissmissBottomMenu();
		if (arg0 != 0)
			handleOffscreenPageLimit();
		curPosition = arg0;
	}

	private void handleOffscreenPageLimit() {
		int limit = viewPager.getOffscreenPageLimit();
		if (limit < adapter.getCount()) {
			viewPager.setOffscreenPageLimit(++limit);
		}
	}

	/**
	 * 隐藏底部工具栏，并重置数据
	 */
	private void dissmissBottomMenu() {
		FlashTransferContentFragment fragment = ((FlashTransferContentFragment) adapter.getItem(curPosition));
		fragment.cancelTransferSelectedResource();
		fragment.dismissBottomMenu();
	}

	@Override
	public void deviceChanged(final List<UserInfoPtlV2> users) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				refreshDeviceView(users);
				if (users.isEmpty()) {
					transferCountLayout.setVisibility(View.GONE);
					FlashTransferManager.release(false);
				}
			}
		});
	}

	private class NewRecvReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			newRecordTipView.setVisibility(View.VISIBLE);
		}
	}
}
