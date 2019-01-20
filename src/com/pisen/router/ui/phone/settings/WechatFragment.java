package com.pisen.router.ui.phone.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.pisen.router.R;
import com.pisen.router.ui.base.impl.DefaultNavigationBar;
import com.pisen.router.ui.phone.HomeFragment;

/**
 * 微信公众号
 * @author  mahuan
 * @version 1.0 2015年5月21日 下午3:28:30
 */
public class WechatFragment extends HomeFragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cloud_settings_weixin_public, container , false);
		initView(view);
		return view;
	}
	
	public void initView(View view){
		DefaultNavigationBar navibar = (DefaultNavigationBar) view.findViewById(R.id.navibar_weixin_no);
		navibar.setTitle("微信公众号");
		navibar.setLeftButton("返回", new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
	}

}
