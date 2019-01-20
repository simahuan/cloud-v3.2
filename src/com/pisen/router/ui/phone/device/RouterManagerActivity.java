package com.pisen.router.ui.phone.device;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.pisen.router.R;
import com.pisen.router.ui.base.INavigationBar;
import com.pisen.router.ui.base.NavigationBarActivity;
import com.pisen.router.ui.base.impl.DefaultNavigationBar;

/**
 * 150M路由管理界面
 * 
 * @author Liuhc
 * @version 1.0 2015年5月28日 下午7:05:50
 */
public class RouterManagerActivity extends NavigationBarActivity {
	// 超时处理
	private static final int LOAD_TIMEOUT = 10 * 1000;
	private Handler mHandler;
	private WebView mWebView;
	private ProgressBar mProgressBar;
	private boolean isTimeout;
	private RelativeLayout mErrorLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_router_mgr);
		setTitle("路由管理");

		mWebView = (WebView) findViewById(R.id.webView);
		mProgressBar = (ProgressBar) findViewById(R.id.proWait);
		mErrorLayout = (RelativeLayout) findViewById(R.id.errorLayout);
		mHandler = new Handler();
		connection();
	}

	@Override
	protected INavigationBar createNavigationBar(LayoutInflater inflater) {
		DefaultNavigationBar navBar = new DefaultNavigationBar(this);
		navBar.setLeftButton("返回", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebBackForwardList list = mWebView.copyBackForwardList();
				if (mWebView != null && !isTimeout && mWebView.canGoBack() && list.getCurrentIndex() != 1) {
					mWebView.goBack();
				} else {
					backOnclick();
				}
			}
		});
		return navBar;
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage)
				return;
			Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
		}
	}

	private ValueCallback<Uri> mUploadMessage;
	private final static int FILECHOOSER_RESULTCODE = 1;

	/**
	 * 连接本地的html
	 */
	private void connection() {
		String url = "http://" + NetUtils.getGateway(RouterManagerActivity.this);
		LogCat.e("url=" + url);
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

		mWebView.setWebChromeClient(new WebChromeClient() {
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {

				mUploadMessage = uploadMsg;
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				RouterManagerActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
			}

			public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
				mUploadMessage = uploadMsg;
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				RouterManagerActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
			}

			public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
				mUploadMessage = uploadMsg;
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				RouterManagerActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
			}
		});
	}

	public void backOnclick() {
		this.finish();
	}

	private void refreshData() {
		isTimeout = false;
		mWebView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
		// mErrorLayout.setVisibility(View.GONE);
		// connection();
	}

	private void timeout() {
		mHandler.removeCallbacks(timeoutTask);
		mWebView.stopLoading();
		mWebView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.GONE);
		mErrorLayout.setVisibility(View.VISIBLE);
		isTimeout = true;
	}

	private void loadSuccess() {
		mHandler.removeCallbacks(timeoutTask);
		mProgressBar.setVisibility(View.GONE);
		// mErrorLayout.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
	}
}
