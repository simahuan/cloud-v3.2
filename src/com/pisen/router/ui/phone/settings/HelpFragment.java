package com.pisen.router.ui.phone.settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.pisen.router.R;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.base.impl.DefaultNavigationBar;
import com.pisen.router.ui.phone.HomeFragment;

/**
 * @author  mahuan
 * @version 1.0 2015年6月4日 下午3:40:45
 */
public class HelpFragment extends HomeFragment {
	private String url = HttpKeys.USEING_HELP_URL;
	private static final int LOAD_TIMEOUT = 10 * 1000;// 10s
	private FragmentActivity activity;
	private Handler mHandler;
	private WebView mWebView;
	private RelativeLayout mErrorLayout;
	private Button mBtnRefresh;
	private boolean isTimeout;
	private View layoutView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity.showLoadingProgressDialog();
		layoutView = inflater.inflate(R.layout.cloud_settings_huiyuandi, (ViewGroup)null);
		DefaultNavigationBar naviBar = (DefaultNavigationBar) layoutView.findViewById(R.id.navibar);
		naviBar.setTitle("使用帮助");
		naviBar.setLeftButton("返回", new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		
		mWebView = (WebView) layoutView.findViewById(R.id.wv_main_huiyuandi_webview);
		mErrorLayout = (RelativeLayout) layoutView.findViewById(R.id.errorLayout);
		mBtnRefresh = (Button) layoutView.findViewById(R.id.btnRefresh);
		mBtnRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshData();
			}
		});
		mHandler = new Handler();
		connection(url);
		return layoutView;
	}
	
	@Override
	public void onStop() {
		mHandler.removeCallbacks(timeoutTask);
		super.onStop();
	}

	Runnable timeoutTask = new Runnable() {
		@Override
		public void run() {
			timeout();
		}
	};
	
	/**
	 * 连接本地的html
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void connection(String url) {
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(url);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				timeout();
			}

			@Override
			public void onPageStarted(final WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				mHandler.postDelayed(timeoutTask, LOAD_TIMEOUT);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (!isTimeout) {
					loadSuccess();
				}
			}
		});
	}


	private void refreshData() {
		activity.showLoadingProgressDialog();
		isTimeout = false;
		mWebView.setVisibility(View.GONE);
		mErrorLayout.setVisibility(View.GONE);
		connection(url);
	}

	private void timeout() {
		mHandler.removeCallbacks(timeoutTask);
		mWebView.stopLoading();
		mWebView.setVisibility(View.GONE);
		mErrorLayout.setVisibility(View.VISIBLE);
		isTimeout = true;
		activity.dismissProgressDialog();
	}

	private void loadSuccess() {
		mHandler.removeCallbacks(timeoutTask);
		activity.dismissProgressDialog();
		mErrorLayout.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (FragmentActivity)activity;
	}
}
