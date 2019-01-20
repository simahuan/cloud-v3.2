package com.pisen.router.core.recorder.ui;

import java.io.File;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pisen.router.R;

public class PlaybackFilesAdapter extends BaseAdapter {
	private List<File> list ;
	public PlaybackFilesAdapter(List<File> list) {
		this.list = list ; 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder= new ViewHolder();
		if(convertView==null){
			convertView = View.inflate(parent.getContext(), R.layout.list_files_item, null);
			convertView.setTag(holder);
			holder.fileName = (TextView) convertView.findViewById(R.id.txtFileName);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.fileName.setText(list.get(position).getName());
		return convertView;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	static class ViewHolder {
		TextView fileName;
	}
}
