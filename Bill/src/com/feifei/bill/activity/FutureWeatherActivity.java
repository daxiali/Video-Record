package com.feifei.bill.activity;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.amap.api.location.AMapLocalDayWeatherForecast;
import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.LocationManagerProxy;
import com.feifei.bill.R;
import com.feifei.bill.util.CustomeToast;
import com.feifei.bill.util.WeatherUtil;

public class FutureWeatherActivity extends BaseActivity implements
		AMapLocalWeatherListener {

	private final static int HANDLER_MSG_GETWEATHER_SUCCESS = 1;
	private final static int HANDLER_MSG_GETWEATHER_failure = 2;

	private TextView reacordTitle;
	private ImageView imageTitle;
	private ListView weahterDetailListView;
	private ProgressBar progressBarWeather;
	private LocationManagerProxy mLocationManagerProxy;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_MSG_GETWEATHER_SUCCESS:
				progressBarWeather.setVisibility(View.GONE);
				List<Map<String, Object>> data = (List<Map<String, Object>>) msg.obj;
				SimpleAdapter adapter = new SimpleAdapter(
						FutureWeatherActivity.this, data,
						R.layout.future_weather_item, new String[] { "image",
								"details", "details2", "details3", "details4" }, new int[] {
								R.id.futureImage, R.id.futureTemp,
								R.id.futureTemp2,R.id.futureTemp3,R.id.futureTemp4 });
				weahterDetailListView.setAdapter(adapter);
				break;
			case HANDLER_MSG_GETWEATHER_failure:
				progressBarWeather.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentViewWithoutTitle(R.layout.weather_bill_layout);
		initLayout();

	}

	/**
	 * 初始化组件
	 */
	public void initLayout() {
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		mLocationManagerProxy.requestWeatherUpdates(
				LocationManagerProxy.WEATHER_TYPE_LIVE, this);

		mLocationManagerProxy.requestWeatherUpdates(
				LocationManagerProxy.WEATHER_TYPE_FORECAST, this);
		reacordTitle = (TextView) this.findViewById(R.id.headerTitle);
		imageTitle = (ImageView) this.findViewById(R.id.headerImage);
		weahterDetailListView = (ListView) this
				.findViewById(R.id.weahterDetailListView);
		progressBarWeather = (ProgressBar)this.findViewById(R.id.progressBarWeather);
		reacordTitle.setText("未来天气");
	}

	@Override
	public void onWeatherForecaseSearched(
			AMapLocalWeatherForecast aMapLocalWeatherForecast) {
		// TODO Auto-generated method stub
		if (aMapLocalWeatherForecast != null
				&& aMapLocalWeatherForecast.getAMapException().getErrorCode() == 0) {

			List<AMapLocalDayWeatherForecast> forcasts = aMapLocalWeatherForecast
					.getWeatherForecast();
			List<Map<String, Object>> data = WeatherUtil
					.initThreeDatesWeather(forcasts);
			System.out.println(data);
			handler.obtainMessage(HANDLER_MSG_GETWEATHER_SUCCESS, data)
					.sendToTarget();
		} else {
			CustomeToast.showCustomeToast(this, "获取天气预报失败",
					R.drawable.rectmenuidi_hover, "bottom");
			handler.obtainMessage(HANDLER_MSG_GETWEATHER_failure, null)
					.sendToTarget();
		}
	}

	@Override
	public void onWeatherLiveSearched(AMapLocalWeatherLive arg0) {
		// TODO Auto-generated method stub

	}
}
