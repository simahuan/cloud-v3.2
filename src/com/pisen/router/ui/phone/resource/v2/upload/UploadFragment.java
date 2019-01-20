package com.pisen.router.ui.phone.resource.v2.upload;

import com.pisen.router.ui.base.FragmentSupport;

/**
 * 硬盘显示区
 * 
 * @author yangyp
 */
public abstract class UploadFragment extends FragmentSupport implements IChoiceActionBar {

	protected RootUploadActivity activity;

	public UploadFragment(RootUploadActivity activity) {
		this.activity = activity;
	}

	public RootUploadActivity getUploadActivity() {
		return activity;
	}
}
