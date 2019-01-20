package com.pisen.router.ui.phone.resource.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pisen.router.R;

public class SingleChoiceDialog extends Dialog implements OnItemClickListener {
	private ListView lstContent;
	private ArrayAdapter<String> adapter;
	private OnChoiceClickListener listener;
	
	public SingleChoiceDialog(Context context) {
		this(context, R.style.dialog);
	}

	public SingleChoiceDialog(Context context, int theme) {
		super(context, theme);
		setContentView(R.layout.dialog_singlechoice);
		findView();
		initView();
	}
	
	/**
	 * 设置显示数据源
	 * @param data
	 */
	public void setData(List<String> data) {
		adapter.clear();
		adapter.addAll(data);
		adapter.notifyDataSetChanged();
	}
	
	public void setData(String[] data) {
		setData(Arrays.asList(data));
	}
	
	/**
	 * 设置点击回调
	 * @param listener
	 */
	public void setOnChoiceClickListener(OnChoiceClickListener listener ) {
		this.listener = listener;
	}
	
	private void findView() {
		lstContent = (ListView) findViewById(R.id.lstcontent);
	}

	private void initView() {
		adapter = new ArrayAdapter<String>(getContext(), R.layout.item_singlechoice, R.id.txtsinglechoice, new ArrayList<String>());
		lstContent.setAdapter(adapter);
		lstContent.setOnItemClickListener(this);
		
		setCanceledOnTouchOutside(true);
	}

	public void showAtYPosition(int y) {
		if(y <0) {
			y =0;
		}else {
			int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
//			int dialogHeight = 
		}
		Window win = getWindow();
		LayoutParams params = win.getAttributes();
		params.gravity = Gravity.TOP;
		params.y = y;
		win.setAttributes(params);
		show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(listener != null) {
			listener.OnChoiceClick(position);
		}
		
		dismiss();
	}
	
	public interface OnChoiceClickListener {
		void OnChoiceClick(int position);
	}

}
