package com.pisen.router.core.message;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.studio.os.LogCat;
import android.studio.view.widget.BaseAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.DateUtils;

/**
 * @author  mahuan
 * @version 1.0 2015年3月27日 上午9:43:52
 * @updated [2015年3月27日 上午9:43:52]:
 */
public class MessageCenterActivity extends Activity implements OnItemClickListener ,OnItemLongClickListener{
	private ListView mLstView;
	private TextView mMsgTips;
	private MessageAdapter mMsgAdapter;
	private MessageCenterDbHelper mDbHelper;
	
	public static final int pageSize = 15;
	public int offSet = 0;
	private QueryMessageTask task = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_center_activity);
		initView();
		task = new QueryMessageTask();
		task.execute();
	}
//	1.单跳转阅读
//	2.长按删除消息
	
	public void initView(){
		mDbHelper =  new MessageCenterDbHelper(this);
//		mockData(35);
		
		mMsgTips = (TextView) findViewById(R.id.message_tip);
		mLstView = (ListView) findViewById(R.id.message_listview);
		mMsgAdapter =  new MessageAdapter(this);
		mLstView.setAdapter(mMsgAdapter);
		mLstView.setEmptyView(mMsgTips);
		mLstView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if ( view.getLastVisiblePosition() == view.getCount() - 1 &&  scrollState == OnScrollListener.SCROLL_STATE_IDLE ){
					LogCat.e("%d\n", scrollState);
					
					if(task != null && !task.isCancelled()){
						task.isCancelled();
						task = null;
					}
					offSet ++ ;
					mLstView.setSelection(view.getLastVisiblePosition());
					task = new QueryMessageTask();
					task.execute();
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, 
					int totalItemCount) {
			}
		});
	}
	
	
	class MessageAdapter extends BaseAdapter<MessageInfo>{
		
		public MessageAdapter(Context context) {
			super(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			View view = convertView;
			if (view == null){
				view = LayoutInflater.from(mContext).inflate(R.layout.msgcenter_list_item, null);
				view.setTag(holder = new ViewHolder());
				holder.tvTitle = (TextView) view.findViewById(R.id.msg_title);
				holder.tvTime = (TextView) view.findViewById(R.id.msg_time);
				holder.tvContent = (TextView) view.findViewById(R.id.msg_content);
			}else {
				holder = (ViewHolder) view.getTag();
			}
			holder.tvTitle.setText(getItem(position).title);
			holder.tvTime.setText(DateUtils.long2Time(getItem(position).recvTime));
			holder.tvContent.setText(getItem(position).content);
			return view;
		}
		
		class ViewHolder{
			TextView 	tvTitle;
			TextView 	tvTime;
			TextView 	tvContent;
		}
	}
	
	class QueryMessageTask extends AsyncTask<Integer, Void, List<MessageInfo>>{
		@Override
		protected List<MessageInfo> doInBackground(Integer... arg0) {
			List<MessageInfo> list = mDbHelper.findMessageInfo(offSet * pageSize, pageSize);
//			List<MessageInfo> list = mDbHelper.getMessageInfo(offSet * pageSize, pageSize);
			final int size = list.size();
			LogCat.e("size = %d \n", size);
			if (size > 0){
				long[] ids = new long[size];
				for (int i = 0 ; i < size ; i++){
					ids[i] = list.get(i).id;
				}
				mDbHelper.updateMessageByRead(ids);
			}
			return list;
		}
		
		@Override
		protected void onPostExecute(List<MessageInfo> result) {
			super.onPostExecute(result);
			mMsgAdapter.setData(result);
			mMsgAdapter.notifyDataSetChanged();
		}
	}
	
//	模拟测试数据
	public void mockData(int total){
		MessageInfo info  = new MessageInfo();
		LogCat.e("mockData = %d\n",total);
		for( int i = 0 ; i < total ; i++){
			 info.title = "品胜云路由第" + i + "次消息";
			 info.recvTime = System.currentTimeMillis() + i;
			 info.type =  10 ;
			 info.content = "品胜云路由第" + i + "次  消息实体内容.";
			 info.readFlag = MessageInfo.MESSAGE_UNREAD;
			 long result = mDbHelper.addMessage(info);
			 LogCat.e("result = %d\n", result);
		}
	}

	/**
	 * 短按事件,如果是富媒体就存在链接,判断类型跳转相应界面
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MessageInfo info = (MessageInfo) parent.getItemAtPosition(position);
	}

	/**
	 * 长按事件
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,long id) {
		MessageInfo info = (MessageInfo) parent.getItemAtPosition(position);
		return false;
	}
	
}
