package com.feifei.bill.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.feifei.bill.R;

public class SearchListAdapter extends BaseAdapter {

	private Context context;
	private List<Map<String, Object>> listResults;

	public SearchListAdapter(Context context, List<Map<String, Object>> list) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.listResults = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listResults.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listResults.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void refresh(List<Map<String, Object>> list) {
		listResults = list;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.search_item_layout,
					null);
			holder.image = (ImageView) convertView.findViewById(R.id.s_person);
			holder.text_type = (TextView) convertView.findViewById(R.id.s_type);
			holder.text_money = (TextView) convertView
					.findViewById(R.id.s_money);
			holder.text_desc = (TextView) convertView.findViewById(R.id.s_desc);
			holder.text_time = (TextView) convertView.findViewById(R.id.s_time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.image.setImageResource((int)listResults.get(position).get("image"));
		if(position%2==0){
			convertView.setBackgroundColor(Color.argb(250, 255, 255, 255));
		}
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(context, "11", Toast.LENGTH_SHORT).show();
			}
		});
		return convertView;
	}

	static class ViewHolder {

		TextView text_type, text_money, text_desc, text_time;

		ImageView image;
	}
}
