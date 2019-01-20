package com.pisen.router.ui.phone.flashtransfer;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pisen.router.R;

/**
 * 闪电互传底部操作栏
 * @author ldj
 * @version 1.0 2015年5月25日 上午10:56:06
 */
public class TransferToolbarPopupWindow extends PopupWindow implements OnClickListener{
	
	private Context context;
	private View contentView;
	private TextView transferButton;
	private ImageButton closeButton;
	private String countString;
	private IFlashTransferControl control;
	
	public TransferToolbarPopupWindow(Context context) {
		super(context);
		
		this.context = context;
		contentView = LayoutInflater.from(context).inflate(R.layout.popupwindow_flashtransfer_do, null);
		setContentView(contentView);
		setWidth(context.getResources().getDisplayMetrics().widthPixels);
		setHeight((int) (60 * context.getResources().getDisplayMetrics().density));
		setBackgroundDrawable(new BitmapDrawable());
		
		findView();
		initView();
	}

	private void findView() {
		transferButton = (TextView) contentView.findViewById(R.id.txtTransfer);
		closeButton = (ImageButton) contentView.findViewById(R.id.ibtnClose);
	}
	
	private void initView() {
		countString = context.getString(R.string.flashtransfer_selected_count);
		transferButton.setText(String.format(countString, 0));
		
		transferButton.setOnClickListener(this);
		closeButton.setOnClickListener(this);
	}
	
	
	public void show(View v, int count) {
		transferButton.setText(String.format(countString, count));
		if(!isShowing()) {
			showAtLocation(v, Gravity.BOTTOM, 0, 0);
		}
	}
	
	/**
	 * 设置控制按钮监听
	 * @param control
	 */
	public void setFlashTransferControl(IFlashTransferControl control) {
		this.control = control;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
		case R.id.txtTransfer:
			if(control != null) {
				control.start();
			}
			break;
		case R.id.ibtnClose:
			if(control != null) {
				control.cancle();
			}
			break;
		default:
			break;
		}
	}
	
	public interface IFlashTransferControl {
		/**
		 * 开始传输已选择数据
		 */
		void start();
		/**
		 * 取消传输已选择数据
		 */
		void cancle();
	}
}
