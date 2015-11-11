package com.feifei.bill.broadcast;

import com.feifei.bill.service.BackgroundService;
import com.feifei.bill.util.CustomeNotice;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.e("开机启动", "开机启动+++++++++++++++++++++");
		NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		CustomeNotice.NotifiyManager(context, nm, null, "++++++");
		context.startService(new Intent(context, BackgroundService.class));
	}

}
