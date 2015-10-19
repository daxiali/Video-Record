package com.feifei.video.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.feifei.video.activity.HomeMediaRecorderActivity;
import com.feifei.video.util.CommonUtils;
import com.feifei.video.util.Constants;
import com.feifei.video.util.FileUtils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class BackgroundService extends Service {

	private final static String TAG = "###Background service###";
	/**
	 * ɨ���ļ��б�׼���ϴ�
	 */
	private Timer timer;
	
	private Thread deleteThread;
	
	private static ExecutorService uploadThreadPool;
	
	private static Context context;
	private SharedPreferences sp;
	private final static int DEFAULE_DELETE_TIME = 10*1000;
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:

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
		sp = this.getSharedPreferences("videoRecord", 0);
		context = this;
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				getVideoDir(new File(dir_default));	
				List<String> list = CommonUtils.getDirFiles(new File(Constants.DEFAULT_VIDEO_PATH));
				Intent fileListIntent = new Intent();
				fileListIntent.setAction(HomeMediaRecorderActivity.BROADCAST_MSG_FILE_LIST);
				fileListIntent.putStringArrayListExtra("FileList", (ArrayList<String>) list);
				sendBroadcast(fileListIntent);
			}
		}, 1000, 30000);
		/**
		 * �̳߳ش�СΪ5
		 */
		uploadThreadPool = Executors.newFixedThreadPool(5);
		deleteFile();
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		timer.cancel();
	}
	
	/**
	 * ���������ļ��б����ҵ������ϴ����ļ�
	 */
	public static void  getVideoDir(File fdir){
//		File fdir = new File(dir);
		if(fdir.exists()&&fdir.isDirectory()){
			File[] first = fdir.listFiles();
			for(int i=0;i<first.length;i++){
				String name = first[i].getName();
				if(!first[i].isDirectory()){
					if(name.contains("uploading")){
						//�Ѿ��ϴ���ɵĿ���ɾ��
						System.out.println("�����ϴ�.."+first[i].getAbsolutePath());
					}else if(name.contains("recording")){
						//����¼�ƵĲ����ϴ�
						System.out.println("����¼�ƵĲ����ϴ�"+first[i].getAbsolutePath());
					}else{
						//�ϴ�
						/**
						 * http://blog.csdn.net/sunny2038/article/details/6926079 ��̬�������ܵ��ö�̬�ڲ���  public class
						 */
						UploadFileRunnable runnable = new UploadFileRunnable(first[i]);
						uploadThreadPool.execute(runnable);
						int threadCount = ((ThreadPoolExecutor)uploadThreadPool).getActiveCount();
						System.out.println("��ǰ�̳߳ػ�߳�����"+threadCount);
					}
				}else{
					getVideoDir(first[i]);					
				}
//				Log.e(TAG,first[i].getAbsolutePath()/*+"   size:"+first[i].length()/(1024*1024)+"M"*/);
			}
		}
	}

	/**
	 * ��ȡ�ļ���Ŀ¼����
	 * @param fdir
	 * @return
	 */
	public static List<String>  getDirFiles(File fdir){
		List<String> list = new ArrayList<String>();
		if(fdir.exists()&&fdir.isDirectory()){
			File[] first = fdir.listFiles();
			for(int i=0;i<first.length;i++){
				Log.e(TAG+"list dir",first[i].getName()+"   size:"+first[i].length()/(1024*1024)+"M");
				list.add(first[i].getName());
			}
		}
		return list;
	}
	/**
	 * 
	 * ɨ���ļ�
	 */
	public void deleteFile(){
		deleteThread = new Thread(new ScanSFileRunnable());
		deleteThread.setDaemon(true);
		deleteThread.start();
	}
	/**
	 * �ϴ��ļ���ɺ����������ļ���ʾ�Ƿ��ϴ�����fileName+"_upload".1Сʱ����һ���ļ���֤�ռ��С
	 * ������ʾ�ϴ���ɵ��б�������ϴ����ļ��б�
	 * @author Administrator
	 *
	 */
	class ScanSFileRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				try {
					if(sp.getBoolean("startUpload", false)){
						getVideoDir(new File(Constants.DEFAULT_VIDEO_PATH));						
					}else{
						System.out.println("�������ϴ���");
					}
					Thread.sleep(DEFAULE_DELETE_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	};
	static class UploadFileRunnable implements Runnable {
		File f = null;
		UploadFileRunnable(File f){
			this.f = f;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			boolean success = true;
			File uF = new File(f.getAbsolutePath().replace(".mp4", "uploading.mp4"));
			try {
				boolean connect = FileUtils.connect("", "10.11.0.239", 21,"ftp_1", "laio890818");
				System.out.println("��ʼ���ӣ�����������"+connect+"---"+f.getAbsolutePath());
				if(connect){
					System.out.println("��ʼ�ϴ�������������");
					f.renameTo(uF);
					f.delete();
					FileUtils.upload(uF,context);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				success = false;
				System.out.println("�ϴ�ʧ�ܣ�"+f.getName());
				uF.renameTo(f);
				uF.delete();
			}
			if(success){
				System.out.println("�ϴ��ɹ���ɾ���ļ�"+f.getName());
				uF.delete();
				//�㲥ˢ���б�
				Intent i = new Intent(HomeMediaRecorderActivity.BROADCAST_MSG_FILE_LIST_VIDEO);
				i.putExtra("fileName", uF.getParent()/*Constants.DEFAULT_VIDEO_PATH+fileName*/);
				context.sendBroadcast(i);
			}
		/*	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		try {
			if (Build.VERSION.SDK_INT > 18) {
				Log.v(TAG, "startForgroundCompat");
				startForeground(1120, new Notification());
			} else {
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	
}
