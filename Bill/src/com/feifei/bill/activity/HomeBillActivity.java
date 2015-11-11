package com.feifei.bill.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.feifei.bill.R;
import com.feifei.bill.service.BackgroundService;
import com.feifei.bill.util.AppActivityManager;
import com.feifei.bill.util.CustomeToast;

public class HomeBillActivity extends BaseActivity {
	private ListView homeMenuListView;
	private ImageView loopImageView;
	/**
	 * ����ͼƬ�Ķ�ʱ��ѯ
	 */
	private Timer autoTimer;

	// �˳���
	private static int exit = 0;
	private static Long start;
	private static Long end;

	/**
	 * ��Ҫ�޸�Ϊ���Դ��������ѡ��ͼƬ�Զ���չʾ
	 */
	private int[] images = new int[] { R.drawable.background,
			R.drawable.background1, R.drawable.background2 };
	private int defaultImage = 0;

	private final static int HANDLER_MSG_CHANGE_BACKGROUND = 0;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_MSG_CHANGE_BACKGROUND:
				loopImageView.setBackgroundResource(images[(int) msg.obj]);
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentViewFullScreen(R.layout.home_bill_layout);
		AppActivityManager.getAppManager().listAllActivity();
		loopImageView = (ImageView) this.findViewById(R.id.loopImageView);
		homeMenuListView = (ListView) this.findViewById(R.id.homeMenuListView);
		SimpleAdapter adapter = new SimpleAdapter(this, getMenusData(),
				R.layout.home_bill_item_layout,
				new String[] { "icon", "text" }, new int[] { R.id.menuImage,
						R.id.menuText });
		homeMenuListView.setAdapter(adapter);
		homeMenuListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				System.out.println(position);
				Intent i = new Intent();
				switch (position) {
				case 0:
					i.setClass(HomeBillActivity.this, BillRecordActivity.class);
					break;
				case 1:
					i.setClass(HomeBillActivity.this, BillSearchActivity.class);
					break;
				case 2:
					i.setClass(HomeBillActivity.this, FutureWeatherActivity.class);
					break;
				default:
					break;
				}
				startActivity(i);
			}
		});
	
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//��ʼִ��ͼƬչ������
		autoTimer = new Timer();
		autoTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				defaultImage++;
				if (defaultImage > images.length - 1) {
					defaultImage = 0;
				}
				handler.obtainMessage(HANDLER_MSG_CHANGE_BACKGROUND,
						defaultImage).sendToTarget();
			}
		}, 5000, 5000);
	}
	public List<Map<String, Object>> getMenusData() {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("icon", R.drawable.rectmenuidi_hover);
		m.put("text", "���Ѽ���");
		data.add(m);

		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("icon", R.drawable.rectmenuidi_hover);
		m2.put("text", "�˵���ѯ");
		data.add(m2);

		Map<String, Object> m3 = new HashMap<String, Object>();
		m3.put("icon", R.drawable.rectmenuidi_hover);
		m3.put("text", "δ������");
		data.add(m3);
		return data;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		autoTimer.cancel();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//ֹͣ����
//		stopService(new Intent(this, BackgroundService.class));
//		AppActivityManager.getAppManager().finishAllActivity();
//		AppActivityManager.getAppManager().listAllActivity();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		exit++;
		System.out.println("###############exit" + exit);
		if (exit == 1) {
			start = System.currentTimeMillis();
			CustomeToast.showCustomeToast(this, "�ٵ���˳�Ӧ��",R.drawable.rectmenuidi_hover, "bottom");
			return;
		} else {
			end = System.currentTimeMillis();
			if (end - start < 3000) {
//				System.exit(0);
				AppActivityManager.getAppManager().finishAllActivity();
			} else {
				CustomeToast.showCustomeToast(this, "�ٵ���˳�Ӧ��",R.drawable.rectmenuidi_hover, "bottom");
				start = System.currentTimeMillis();
				return;
			}
		}
	}
}
