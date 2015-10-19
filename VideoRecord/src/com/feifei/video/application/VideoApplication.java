package com.feifei.video.application;

import com.feifei.video.activity.HomeMediaRecorderActivity;
import com.feifei.video.activity.HomeVideoActivity;

import android.app.Application;
import android.content.Intent;

/**
 * ³ÌÐòÈë¿Ú
 * 
 * @author Administrator
 *
 */
public class VideoApplication extends Application {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Intent i = new Intent();
		i.setClass(this, HomeMediaRecorderActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
}
