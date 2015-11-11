package com.feifei.bill.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.feifei.bill.R;
import com.feifei.bill.util.CommonUitls;

public class BillRecordActivity extends BaseActivity implements OnClickListener {

	private TextView reacordTitle;
	private ImageView imageTitle;
	private Spinner record_type,record_person;
	private Button record_save, record_reset;
	private EditText record_money,record_desc,record_time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentViewWithoutTitle(R.layout.record_bill_layout);
		initLayout();
	}

	/**
	 * 初始化组件
	 */
	public void initLayout() {
		reacordTitle = (TextView) this.findViewById(R.id.headerTitle);
		imageTitle = (ImageView) this.findViewById(R.id.headerImage);
		reacordTitle.setText("消费记账");
		record_type = (Spinner) this.findViewById(R.id.record_type);
		BaseAdapter adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice,
				CommonUitls.getRecordType());
		record_type.setAdapter(adapter);
		
		
		record_person = (Spinner)this.findViewById(R.id.record_person);
		BaseAdapter adapterPerson = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice,
				CommonUitls.getRecordPerson());
		record_person.setAdapter(adapterPerson);

		record_reset = (Button) this.findViewById(R.id.record_reset);
		record_save = (Button) this.findViewById(R.id.record_save);
		record_reset.setOnClickListener(this);
		record_save.setOnClickListener(this);
		
		record_money = (EditText)this.findViewById(R.id.record_money);
		record_desc = (EditText)this.findViewById(R.id.record_desc);
		record_time = (EditText)this.findViewById(R.id.record_time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		record_time.setText(format.format(System.currentTimeMillis()));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.record_save:
			System.out.println(record_person.getSelectedItem()+"---"+record_type.getSelectedItem()+"-----"+record_money.getText()+"---"+record_desc.getText());
			break;
		case R.id.record_reset:
			record_money.setText("");
			record_desc.setText("");
			break;
		default:
			break;
		}
	}
}
