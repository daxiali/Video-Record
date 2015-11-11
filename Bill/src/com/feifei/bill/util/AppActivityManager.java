package com.feifei.bill.util;

import java.util.Stack;

import android.app.Activity;
import android.util.Log;

/**
 * 控制activity管理
 * 
 * @author Administrator
 *
 */
public class AppActivityManager {
	String TAG = "AppActivityManager";
	private static Stack<Activity> activityStack;
	private static AppActivityManager instance = new AppActivityManager();

	private AppActivityManager() {
	}

	/**
	 * 单一实例
	 */
	public static AppActivityManager getAppManager() {
		return instance;
	}

	/**
	 * 添加Activity到堆栈
	 */
	public void addActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		if (activityStack.contains(activity)) {
			activityStack.remove(activity);
		}
		activityStack.add(activity);
	}

	/**
	 * 移出Activity栈
	 */
	public void removeActivity(Activity activity) {
		if (activityStack != null) {
			activityStack.remove(activity);
		}
	}

	/**
	 * 获取当前Activity（堆栈中最后一个压入的）
	 */
	public Activity currentActivity() {
		if (activityStack != null) {
			Activity activity = activityStack.lastElement();
			return activity;
		}
		return null;
	}

	/**
	 * 结束当前Activity（堆栈中最后一个压入的）
	 */
	public void finishActivity() {
		if (activityStack != null) {
			Activity activity = activityStack.lastElement();
			finishActivity(activity);
		} else {
			Log.w(TAG,"When invork 'finishActivity()' activityStack is null !.");
		}
	}

	/**
	 * 结束指定的Activity
	 */
	public void finishActivity(Activity activity) {
		if (null != activityStack && activity != null) {
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		} else {
			Log.w(TAG,
					"When invork 'finishActivity(Activity activity)' activityStack is ["
							+ activityStack + "]" + " activity is [" + activity
							+ "]");
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void finishActivity(Class<?> cls) {
		if (null != activityStack) {
			for (Activity activity : activityStack) {
				if (activity.getClass().equals(cls)) {
					finishActivity(activity);
				}
			}
		} else {
			Log.w(TAG,
					"When invork 'finishActivity(Class<?> cls)' activityStack is null");
		}
	}

	/**
	 * 结束所有Activity
	 */
	public void finishAllActivity() {
		if (null != activityStack) {
			for (int i = 0, size = activityStack.size(); i < size; i++) {
				if (null != activityStack.get(i)) {
					activityStack.get(i).finish();
				}
			}
			activityStack.clear();
		} else {
			Log.w(TAG,
					"When invork 'finishAllActivity()' activityStack is null");
		}
	}

	/**
	 * 遍历所有Activity
	 */
	public void listAllActivity() {
		if (null != activityStack) {
			if(activityStack.size()>0){
				for (int i = 0, size = activityStack.size(); i < size; i++) {
					System.out.println(activityStack.get(i));
				}
			}else{
				System.out.println("activity size equal 0!");
			}
		}
	}
}
