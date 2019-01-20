package com.pisen.router.ui.base;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.studio.app.ActivitySupport;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.ui.phone.resource.v2.NavigationBar;

public abstract class CloudActivity extends ActivitySupport {

	private INavigationBar navigationBar;

	@Override
	public Dialog newLoadingDialog() {
		return new LoadingDialog(this);
	}

	@Override
	public void showProgressDialog(CharSequence message) {
		if(!isFinishing()) {
			super.showProgressDialog(message);
		}
	}
	
	@Override
	public void setContentView(int layoutResID) {
		getWindow().setContentView(layoutResID);
		initNavigationBar();
		//ButterKnife.inject(this);
	}

	@Override
	public void setContentView(View view) {
		getWindow().setContentView(view);
		initNavigationBar();
		//ButterKnife.inject(this);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		getWindow().setContentView(view, params);
		initNavigationBar();
		//ButterKnife.inject(this);
	}

	@Override
	public void addContentView(View view, ViewGroup.LayoutParams params) {
		getWindow().addContentView(view, params);
		initNavigationBar();
		//ButterKnife.inject(this);
	}

    @Override
    public void showProgressDialog(CharSequence message, final DialogInterface.OnCancelListener listener) {
        if (this.progressDialog == null) {
            this.progressDialog = this.newLoadingDialog();
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.setOnCancelListener((DialogInterface.OnCancelListener) null);

        }

        if (null != listener) {
            this.progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_UP && listener != null) {
                        listener.onCancel(dialog);
                    }

                    return true;
                }
            });
        } else {
            this.progressDialog.setOnKeyListener(null);
        }

        if (this.progressDialog instanceof ProgressDialog) {
            ((ProgressDialog) this.progressDialog).setMessage(message);
        } else {
            this.progressDialog.setTitle(message);
        }

        this.progressDialog.show();
    }

    private void initNavigationBar() {
		if (navigationBar == null) {
			navigationBar = newNavigationBar();
		}
	}

	protected INavigationBar newNavigationBar() {
		return new NavigationBar(this);
	}

	public INavigationBar getNavigationBar() {
		return navigationBar;
	}

    public Dialog getProgressDialog(){
        return progressDialog;
    }

}
