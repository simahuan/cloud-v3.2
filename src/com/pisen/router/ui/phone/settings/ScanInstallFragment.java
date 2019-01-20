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
 * 扫码安装
 * @author  mahuan
 * @version 1.0 2015年5月21日 下午3:45:48
 */
public class ScanInstallFragment extends HomeFragment {
	private String title;
	
	
	public ScanInstallFragment(){
		
	}
	/**
	 * 构造器
	 */
	public ScanInstallFragment(String title) {
		this.title = title;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 View view  = inflater.inflate(R.layout.cloud_settings_scan_install, container, false);
		 initView(view);
		 return view;
	}
	
	public void initView(View view){
		DefaultNavigationBar navibar = (DefaultNavigationBar) view.findViewById(R.id.navibar_install);
		if (title != null){
			navibar.setTitle(title);
		}else{
			navibar.setTitle("扫码安装");
		}
		navibar.setLeftButton("返回", new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
	}
}
