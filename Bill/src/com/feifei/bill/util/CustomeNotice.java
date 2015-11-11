package com.feifei.bill.util;

import com.feifei.bill.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;


public class CustomeNotice {
	/**
	 * 
	 * 
	 * @param context
	 * @param nm
	 *            通知管理器
	 * @param pd
	 *            内容显示
	 * @param geoLat
	 *            经度
	 * @param geoLng
	 *            纬度 void
	 */
	@SuppressWarnings("deprecation")
	public static void NotifiyManager(Context context, NotificationManager nm,
			PendingIntent pd,String msg) {
		
		// 新建状态栏通知
		Notification baseNF = new Notification();
		// 设置通知在状态栏显示的图标
		baseNF.icon = R.drawable.rectmenuidi_hover;
		// 通知时在状态栏显示的内容
		baseNF.tickerText = "开机自动启动！";
		
		// 通知的默认参数 DEFAULT_SOUND, DEFAULT_VIBRATE, DEFAULT_LIGHTS.
		// 如果要全部采用默认值, 用 DEFAULT_ALL.
		// 此处采用默认声音
		baseNF.defaults |= Notification.DEFAULT_SOUND;
		baseNF.defaults |= Notification.DEFAULT_VIBRATE;
		baseNF.defaults |= Notification.DEFAULT_LIGHTS;

		// 让声音、振动无限循环，直到用户响应
		// baseNF.flags |= Notification.FLAG_INSISTENT;

		// 通知被点击后，自动消失
		baseNF.flags |= Notification.FLAG_AUTO_CANCEL;

		// 点击'Clear'时，不清楚该通知(QQ的通知无法清除，就是用的这个)
		// baseNF.flags |= Notification.FLAG_NO_CLEAR;

		// 第二个参数 ：下拉状态栏时显示的消息标题 expanded message title
		// 第三个参数：下拉状态栏时显示的消息内容 expanded message text
		// 第四个参数：点击该通知时执行页面跳转
		baseNF.setLatestEventInfo(context, "service已启动！", msg, pd);

		// 发出状态栏通知
		// The first parameter is the unique ID for the Notification
		// and the second is the Notification object.
		nm.notify(110, baseNF);
	}
}
