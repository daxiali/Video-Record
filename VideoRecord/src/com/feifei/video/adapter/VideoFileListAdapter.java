package com.feifei.video.adapter;

import java.io.File;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.feifei.video.R;
import com.feifei.video.activity.HomeMediaRecorderActivity;
import com.feifei.video.util.Constants;

public class VideoFileListAdapter extends BaseAdapter {

	private Context context;
	private List<Map<String, Object>> fileList;

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fileList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return fileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void refresh(List<Map<String, Object>> list) {
		fileList = list;
		notifyDataSetChanged();
	}

	/**
	 * ¹¹Ôìº¯Êý
	 * @param context
	 * @param fileList
	 */
	public VideoFileListAdapter(Context context,
			List<Map<String, Object>> fileList) {
		this.context = context;
		this.fileList = fileList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context,
					R.layout.video_file_item_layout, null);
			holder.icon = (ImageView) convertView.findViewById(R.id.menuImage);
			holder.text = (TextView) convertView.findViewById(R.id.menuText);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final String fileName = fileList.get(position).get("text").toString();
		final String FText = fileList.get(position).get("FText").toString();
		holder.icon.setImageResource(Integer.parseInt((fileList.get(position).get("icon")+"")));
		holder.text.setText(fileName);
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Toast.makeText(context, Constants.DEFAULT_VIDEO_PATH+fileName, Toast.LENGTH_SHORT).show();
				Intent i = new Intent(HomeMediaRecorderActivity.BROADCAST_MSG_FILE_LIST_VIDEO);
				i.putExtra("fileName", FText/*Constants.DEFAULT_VIDEO_PATH+fileName*/);
				context.sendBroadcast(i);
			}
		});
		convertView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(HomeMediaRecorderActivity.BROADCAST_MSG_FILE_LIST_VIDEO_LONG_CLICK);
				i.putExtra("fileName", FText/*Constants.DEFAULT_VIDEO_PATH+fileName*/);
				context.sendBroadcast(i);
				return false;
			}
		});
		return convertView;
	}

	static class ViewHolder {

		TextView text;

		ImageView icon;
	}
}
