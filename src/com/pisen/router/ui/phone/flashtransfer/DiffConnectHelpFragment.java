package com.pisen.router.ui.phone.flashtransfer;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pisen.router.R;

/**
 * @author  mahuan
 * @version 1.0 2015年5月22日 下午2:44:23
 */
public class DiffConnectHelpFragment extends AHelpFragmentSupport {
	
	public DiffConnectHelpFragment() {
		super();
		setPageTitle("不同局域网");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.flash_transfer_diff_connect_help, container, false);
		initView(view);
		return view;
	}

	/**
	 * @param view
	 */
	private void initView(View view) {
		((TextView)view.findViewById(R.id.txtConnector)).setText(Html.fromHtml("<font color=\"#666666\">请您点击</font><font color=\"#0073FF\"> &#x0020; &quot;创建连接&quot; &#x0020;</font>"));
		((TextView)view.findViewById(R.id.txtSetting)).setText(Html.fromHtml("<font color=\"#666666\">请苹果设备到</font><font color=\"#0073FF\"> &#x0020; &quot;设置-无线局域网&quot; &#x0020;</font><font color=\"#666666\">选取您创建的网络热点</font>"));
		((TextView)view.findViewById(R.id.txtPinsen)).setText(Html.fromHtml("<font color=\"#666666\">请苹果设备回到</font><font color=\"#0073FF\"> &#x0020; &quot;品胜云-闪电互传&quot; &#x0020;,&#x0020; &quot;摇一摇&quot;&#x0020;</font><font color=\"#666666\">找朋友</font>"));
	}
}
