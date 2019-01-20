package com.pisen.router.common;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import com.pisen.router.R;

/**
 * @author  mahuan
 * @des 
 * @version 1.0 2015年5月14日 下午5:23:21
 */
public class CustomPopupWindow extends PopupWindow implements OnItemClickListener{
	
	private View menuView;
	private GridView grdIcon;
	private List<Map<String,Object>> list;
	private Context ctx;
	private OnItemClickListener itemsOnClick;
	private Button btnCancle;
//	private PopupAdapter mPopupAdapter;
	
	public CustomPopupWindow(Context ctx , OnItemClickListener itemsOnClick ,List<Map<String, Object>> list) {
		this.ctx = ctx;
		this.itemsOnClick = itemsOnClick;
		this.list = list;
		
		initView();
	}
	
	public void initView(){
		menuView = LayoutInflater.from(ctx).inflate(R.layout.custom_popup_window, (ViewGroup)null);
		btnCancle = (Button) menuView.findViewById(R.id.btnCancle);
		btnCancle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		grdIcon = (GridView) menuView.findViewById(R.id.grdIcon);
		grdIcon.setAdapter(getAdaper());
		grdIcon.setOnItemClickListener(this);
//		mPopupAdapter = new PopupAdapter();
//		mPopupAdapter.setData(list);
		this.setContentView(menuView);
		this.setWidth(LayoutParams.FILL_PARENT);
		this.setHeight(LayoutParams.FILL_PARENT);
		this.setFocusable(true);
//		this.setAnimationStyle(R.style.AnimBottom);
		ColorDrawable dw = new ColorDrawable(0x55333333);
		this.setBackgroundDrawable(dw);
		menuView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				int height = menuView.findViewById(R.id.pop_layout).getTop();
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y<height){
						dismiss();
					}
				}				
				return true;
			}
		});
	}
	
	public ListAdapter getAdaper(){
		SimpleAdapter smAdapter = new SimpleAdapter(ctx, list, R.layout.custom_popup_window_item,
				new String[]{"image","name"}, new int[]{R.id.imgIcon,R.id.txtName});
		return smAdapter;
				
	}
	
/*	class PopupAdapter extends BaseListAdapter<Map<String, Object>>{
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder viewHolder;
			if(view == null){
				view = LayoutInflater.from(ctx).inflate(R.layout.custom_popup_window_item, (ViewGroup)null);
				view.setTag(viewHolder =  new ViewHolder());
				viewHolder.imgIcon = (ImageView) view.findViewById(R.id.imgIcon);
				viewHolder.txtName = (TextView) view.findViewById(R.id.txtName);
			}else {
				viewHolder = (ViewHolder) view.getTag();
			}
			viewHolder.imgIcon.setImageDrawable(drawable);
			viewHolder.txtName.setText(getItem(position).get(String));
			return view;
		}
		class ViewHolder{
			ImageView imgIcon;
			TextView  txtName;
		}
	}*/

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
	}
	
}
