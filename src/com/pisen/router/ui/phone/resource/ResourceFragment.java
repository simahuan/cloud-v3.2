package com.pisen.router.ui.phone.resource;

import android.app.Activity;

import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.base.FragmentSupport;

public abstract class ResourceFragment extends FragmentSupport {

	private HomeActivity activity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (HomeActivity) activity;
	}

	public HomeActivity getHomeActivity() {
		return activity;
	}
	
}
