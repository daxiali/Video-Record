package com.feifei.bill.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.feifei.bill.R;
import com.feifei.bill.adapter.SearchListAdapter;
import com.feifei.bill.util.CommonUitls;

public class BillSearchActivity extends BaseActivity implements OnItemSelectedListener {
	private TextView reacordTitle;
	private ImageView imageTitle;
	private Spinner search_type,search_person;
	private ListView search_list;
	private SearchListAdapter listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentViewWithoutTitle(R.layout.search_bill_layout);
		initLayout();
	}

	/**
	 * 初始化组件
	 */
	public void initLayout() {
		reacordTitle = (TextView) this.findViewById(R.id.headerTitle);
		imageTitle = (ImageView) this.findViewById(R.id.headerImage);
		reacordTitle.setText("账单查询");
		
		search_type = (Spinner)this.findViewById(R.id.search_type);
		BaseAdapter adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice,
				CommonUitls.getRecordType());
		search_type.setAdapter(adapter);
		search_type.setOnItemSelectedListener(this);
		
		search_person= (Spinner)this.findViewById(R.id.search_person);
		BaseAdapter adapterPerson = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice,
				CommonUitls.getRecordPerson());
		search_person.setAdapter(adapterPerson);
		search_person.setOnItemSelectedListener(this);
		listAdapter = new SearchListAdapter(this,new ArrayList<Map<String,Object>>());
		search_list = (ListView)this.findViewById(R.id.search_list);
//		search_list.addHeaderView(getLayoutInflater().inflate(R.layout.search_item_header_layout, null));
		search_list.setAdapter(listAdapter);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if(view.getId()==R.id.search_type){
			
		}else if(view.getId()==R.id.search_person){
			
		}
		listAdapter.refresh(getMenusData());
		Toast.makeText(BillSearchActivity.this, "111"+((CheckedTextView)view).getText(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	public List<Map<String, Object>> getMenusData() {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("image", R.drawable.rectmenuidi_hover);
		m.put("text_type", "消费记账");
		data.add(m);

		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("image", R.drawable.rectmenuidi_hover);
		m2.put("text_type", "账单查询");
		data.add(m2);

		Map<String, Object> m3 = new HashMap<String, Object>();
		m3.put("image", R.drawable.rectmenuidi_hover);
		m3.put("text_type", "未来天气");
		data.add(m3);
		return data;
	}
}
