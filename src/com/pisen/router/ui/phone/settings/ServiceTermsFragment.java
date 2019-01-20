package com.pisen.router.ui.phone.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.router.R;
import com.pisen.router.ui.base.NavigationFragment;

/**
 * @author  mahuan
 * @version 1.0 2015年5月25日 上午11:39:18
 */
public class ServiceTermsFragment extends NavigationFragment {

	@Override
	protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setTitle("服务条款");
		return inflater.inflate(R.layout.cloud_settings_service_term, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		super.onViewCreated(view, bundle);
	}
}
