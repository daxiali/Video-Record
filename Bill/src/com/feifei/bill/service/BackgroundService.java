package com.feifei.bill.service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.amap.api.location.AMapLocalDayWeatherForecast;
import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.LocationManagerProxy;
import com.feifei.bill.R;
import com.feifei.bill.activity.WeatherShowActivity;
import com.feifei.bill.util.CustomeToast;
import com.feifei.bill.util.WeatherUtil;


public class BackgroundService extends Service{

	private final static String TAG="###Background service###";
	/**
	 * 弹出系统窗口提示天气状况
	 */
	private final static int HANDLER_MSG_SHOW_SCREEN_WEATHER = 0;
	private LocationManagerProxy mLocationManagerProxy;
	private Thread showWeatherThread;
	private MediaPlayer  mMediaPlayer;
	private List<Map<String, Object>> data;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_MSG_SHOW_SCREEN_WEATHER:
				startAlarm();
				Intent i = new Intent();
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.setClass(BackgroundService.this, WeatherShowActivity.class);
				startActivity(i);
				break;

			default:
				break;
			}
		};
	};
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e(TAG, "on Create()");
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
//		getMenusData();
		showWeatherThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					while(true){
						Calendar calendar = Calendar.getInstance();
						int hour = calendar.get(Calendar.HOUR_OF_DAY);
						int minute = calendar.get(Calendar.MINUTE);
						int second = calendar.get(Calendar.SECOND);
						//三个时间点发送信息
						Log.e(TAG, hour+"--"+minute+"--"+second);
						if((hour==17||hour==11||hour==7)&&minute==00&&second==00){
							Log.e(TAG, hour+"--"+minute+"--"+second);
							handler.obtainMessage(HANDLER_MSG_SHOW_SCREEN_WEATHER).sendToTarget();
						}else if((hour==17||hour==11||hour==7)&&minute==00&&second==20){
							mMediaPlayer.stop();
						}
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		showWeatherThread.setDaemon(true);
		showWeatherThread.start();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	/**
	 * 定时向屏幕展示一个天气资料弹窗
	 */
	public void showWeatherToScreen(List<Map<String,Object>>weatherData){
		//TODO 定时向屏幕展示一个天气资料弹窗
		AlertDialog.Builder builder = new AlertDialog.Builder(BackgroundService.this);
		final AlertDialog dialog = builder.create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
		dialog.setCanceledOnTouchOutside(true);
		dialog.getWindow().setContentView(R.layout.dialog_weather_screen_layout);
		ListView weatherToScreenListView = (ListView)dialog.getWindow().findViewById(R.id.weatherToScreenListView);
		SimpleAdapter adapter = new SimpleAdapter(this, weatherData, R.layout.dialog_weather_screen_item_layout,
				new String[]{"image","details5"}, new int[]{R.id.weatherImage,R.id.weatherText});
		weatherToScreenListView.setAdapter(adapter);
	}
	public List<Map<String, Object>> getMenusData() {
		data = new ArrayList<Map<String, Object>>();
		mLocationManagerProxy.requestWeatherUpdates(LocationManagerProxy.WEATHER_TYPE_FORECAST,new AMapLocalWeatherListener() {
			@Override
			public void onWeatherLiveSearched(AMapLocalWeatherLive arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onWeatherForecaseSearched(AMapLocalWeatherForecast aMapLocalWeatherForecast) {
				// TODO Auto-generated method stub
				if (aMapLocalWeatherForecast != null
						&& aMapLocalWeatherForecast.getAMapException().getErrorCode() == 0) {

					List<AMapLocalDayWeatherForecast> forcasts = aMapLocalWeatherForecast.getWeatherForecast();
					data = WeatherUtil.initThreeDatesWeather(forcasts);
					System.out.println("data----"+data);
				} else {
					CustomeToast.showCustomeToast(BackgroundService.this, "获取天气预报失败",R.drawable.rectmenuidi_hover, "bottom");
				}
			}
		});
		return data;
	}
/*	 定义一个倒计时的内部类 
	private CountDownTimer timer = new CountDownTimer(10000, 1000) {
		@Override
		public void onTick(long millisUntilFinished) {
		}

		@Override
		public void onFinish() {
//			showWeatherToScreen();
			handler.obtainMessage(0).sendToTarget();
		}
	};*/
	
	//获取系统默认铃声的Uri  
    private Uri getSystemDefultRingtoneUri() {  
        return RingtoneManager.getActualDefaultRingtoneUri(this,  
                RingtoneManager.TYPE_RINGTONE);  
    }  
    private void startAlarm() {  
    	mMediaPlayer = MediaPlayer.create(this, getSystemDefultRingtoneUri());  
        mMediaPlayer.setLooping(true);  
        try {  
            mMediaPlayer.prepare();  
        } catch (IllegalStateException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        mMediaPlayer.start();  
    } 
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	// TODO Auto-generated method stub
    	try {
            if (Build.VERSION.SDK_INT > 18) {
                Log.v(TAG, "startForgroundCompat");
                startForeground(1120, new Notification());
            }else{
            	
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    	return super.onStartCommand(intent, flags, startId);
    }
    
    
}
