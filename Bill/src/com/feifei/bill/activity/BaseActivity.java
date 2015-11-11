package com.feifei.bill.activity;

import com.feifei.bill.util.AppActivityManager;

import android.app.Activity;
import android.app.PendingIntent.OnFinished;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * all activity extends baseActivity .manager the activity
 * 
 * @author fliao
 *
 */
public class BaseActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		AppActivityManager.getAppManager().addActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		AppActivityManager.getAppManager().removeActivity(this);
	}
	/**
	 * specific layout without title
	 * 
	 * @param layoutResID
	 * @param titleLayoutResID
	 */
	public void setContentViewWithoutTitle(int layoutResID) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.setContentView(layoutResID);
	}

	/**
	 * full screen
	 * 
	 * @param layoutResID
	 */
	public void setContentViewFullScreen(int layoutResID) {
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Òþ²Ø±êÌâÀ¸
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // Òþ²Ø×´Ì¬À¸
		super.setContentView(layoutResID);
	}

	/**
	 * ues orifinal method that has not changed
	 * 
	 * @param layoutResID
	 */
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
	}
}
