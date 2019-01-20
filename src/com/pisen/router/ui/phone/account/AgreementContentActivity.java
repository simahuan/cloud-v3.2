package com.pisen.router.ui.phone.account;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.ui.base.NavigationBarActivity;

public class AgreementContentActivity extends NavigationBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_agreement_activity);
		setTitle("品胜用户协议");
		TextView content = (TextView) findViewById(R.id.txt_content);
		CharSequence txt = Html.fromHtml(getString(R.string.user_agreement));
		content.setText(txt);
	}
}
