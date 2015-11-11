package com.feifei.bill.application;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.feifei.bill.service.BackgroundService;
/**
 * ³ÌÐòÈë¿Ú
 * @author Administrator
 *
 */
public class BillApplication extends Application {

	private boolean serviceIsRunning = false;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
	    ActivityManager manager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)){
	    	System.out.println(service.service.getClassName());
	    	if("com.feifei.bill.service.BackgroundService".equals(service.service.getClassName())){
	    		System.out.println("++++++++++++service is running++++++++++++++++++"+service.service.getClassName());
	    		serviceIsRunning = true;
	    		break;
	    	}
	    }
	    if(!serviceIsRunning){
    		System.out.println("++++++++++++app start service ++++++++++++++++++");
	    	startService(new Intent(this, BackgroundService.class));
	    }
	}
	
}
