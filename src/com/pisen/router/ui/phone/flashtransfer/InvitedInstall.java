package com.pisen.router.ui.phone.flashtransfer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.pisen.router.R;
import com.pisen.router.ui.base.impl.DefaultNavigationBar;
import com.pisen.router.ui.phone.HomeFragment;

/**
 * 邀请安装
 * @author  mahuan
 * @version 1.0 2015年5月21日 下午3:10:01
 */
public class InvitedInstall extends HomeFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.flash_transfer_invited_install, container, false);
		DefaultNavigationBar navibar = (DefaultNavigationBar) view.findViewById(R.id.navibar_install);
		
		navibar.setTitle("邀请安装");
		navibar.setLeftButton("返回", new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
		return view;
	}
}
