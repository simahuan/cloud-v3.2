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
 * @version 1.0 2015年5月21日 下午6:08:01
 */
public class SameConnectHelpFragment extends AHelpFragmentSupport {
	public SameConnectHelpFragment() {
		super();
		setPageTitle("同一局域网");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view  = inflater.inflate(R.layout.flash_transfer_same_connect_help, container, false);
		initView(view);
		return view;
	}
	
	public void initView(View view){
//		 String shark = getActivity().getResources().getString(R.string.transfer_help_shark);
//		((TextView)view.findViewById(R.id.txtShark)).setText(String.format(shark, Html.fromHtml("<font color=\"#0073FF\"> &#x0020; &quot;摇一摇&quot; &#x0020;</font>")));
		((TextView)view.findViewById(R.id.txtShark)).setText(Html.fromHtml("<font color=\"#666666\">请苹果设备</font><font color=\"#0073FF\"> &#x0020; &quot;摇一摇&quot; &#x0020;</font><font color=\"#666666\">寻找朋友</font>"));
		((TextView)view.findViewById(R.id.txtSeach)).setText(Html.fromHtml("<font color=\"#666666\">请您点击</font><font color=\"#0073FF\"> &#x0020; &quot;搜索加入&quot; &#x0020;</font><font color=\"#666666\">按钮寻找苹果设备</font>"));
	}
}
