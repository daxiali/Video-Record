package com.feifei.video.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.feifei.video.R;
import com.feifei.video.adapter.VideoFileListAdapter;
import com.feifei.video.util.CommonUtils;
import com.feifei.video.util.Constants;
import com.feifei.video.util.CustomeToast;
import com.feifei.video.util.FileUtils;

public class HomeMediaRecorderActivity extends BaseActivity implements
		SurfaceHolder.Callback, OnClickListener {

	/**
	 * 获取目录文件列表
	 */
	public static final String BROADCAST_MSG_FILE_LIST = "broadcast.msg.file.list";
	/**
	 * 视频文件
	 */
	public static final String BROADCAST_MSG_FILE_LIST_VIDEO = "broadcast.msg.file.list.video";
	
	/**
	 * 长按视频列表
	 */
	public static final String BROADCAST_MSG_FILE_LIST_VIDEO_LONG_CLICK = "broadcast.msg.file.list.video.long.click";
	
	/**
	 * 上传进度列表
	 */
	public static final String BROADCAST_MSG_UPLOAD_PROGRESS_LIST = "broadcast.msg.upload.progress.list";
	
	/**
	 * 获取sdcard剩余空间大小
	 */
	private static final int HANDLER_MSG_SDCARD_FREE = 1;
	
	private static final int HANDLER_MSG_SHOW_FILE_DIR_LIST = 2;
	
	private static final int HANDLER_MSG_SHOW_FILE_DIR_VIDEO_LIST = 3;
	
	private static final int HANDLER_MSG_SHOW_FILE_DIR_VIDEO_PLAY = 4;
	
	private static final int HANDLER_MSG_SHOW_VIDEO_TIME = 5;
	
	private static final int HANDLER_MSG_SHOW_STOP_RECORD = 6;
	
	private static final int HANDLER_MSG_SHOW_FILE_PROGRESS = 7;
	
	private Button start;// 开始录制按钮
	private Button stop;// 停止录制按钮
	private Button menu_back,menu_pause,menu_start;
	private ImageButton videoScale,uploadStatus,serverConfig,about,menu;
	private TextView sdcardSizeText;//显示sdcard大小
	private TextView videoTime;//显示视频时间
	private ListView recordList;
	private LinearLayout menuLiner,operationLiner;
	
	private MediaRecorder mediarecorder;// 录制视频的类
	private MediaPlayer player;
	private Camera camera;
	
	private SurfaceView surfaceview;// 显示视频的控件
	// 用来显示视频的一个接口，我靠不用还不行，也就是说用mediarecorder录制视频还得给个界面看
	// 想偷偷录视频的同学可以考虑别的办法。。嗯需要实现这个接口的Callback接口
	private SurfaceHolder surfaceHolder;
	private Timer timer,VideoTimer;
	private ToolBroadcastReceive toolBroadcastReceive;
	private VideoFileListAdapter adapter;
	private String videoOutPath = "";
	private ListView listProgress; 
	private List<String>progressList = new ArrayList<String>();
	/**
	 * 用于返回遗留数据
	 */
	private List<Map<String,Object>> resultDir;
	
	/**
	 * 判断是否是目录还是文件列表
	 */
	private boolean isReturn = false;
	/**
	 * 记录当前目录，用于录制完成后刷新数据
	 */
	private File currentDir = null;
	
	private SharedPreferences sp;
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_MSG_SDCARD_FREE:
				sdcardSizeText.setText("Free: "+CommonUtils.getSdcardFree(HomeMediaRecorderActivity.this)+"M");
				break;
			case HANDLER_MSG_SHOW_FILE_DIR_LIST:
				adapter.refresh((List<Map<String,Object>>) msg.obj);
				break;
			case HANDLER_MSG_SHOW_FILE_DIR_VIDEO_LIST:
				List<Map<String,Object>> list = (List<Map<String,Object>>) msg.obj;
				if(list.size()>0){
					menuLiner.setVisibility(View.VISIBLE);
					adapter.refresh(list);					
				}else{
					isReturn = false;
					CustomeToast.showDefaultToast(HomeMediaRecorderActivity.this, "文件为空！", "");
				}
				break;
			case HANDLER_MSG_SHOW_FILE_DIR_VIDEO_PLAY:
				menu_pause.setVisibility(View.VISIBLE);
				menu_start.setVisibility(View.GONE);
				play((File)msg.obj);
				break;
			case HANDLER_MSG_SHOW_VIDEO_TIME:
				videoTime.setText(msg.obj+"");
				break;
			case HANDLER_MSG_SHOW_STOP_RECORD:
				stopRecord("loop");
				break;
			case HANDLER_MSG_SHOW_FILE_PROGRESS:
				if(listProgress!=null){
					BaseAdapter adapter = new ArrayAdapter<String>(HomeMediaRecorderActivity.this,R.layout.upload_progress_item_layout,progressList);
					listProgress.setAdapter(adapter);
				}
				break;
			default:
				break;
			}
		};
	};
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置横屏显示
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// 选择支持半透明模式,在有surfaceview的activity中使用。
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentViewWithoutTitle(R.layout.media_recorder_layout);
		
		toolBroadcastReceive = new ToolBroadcastReceive();
		IntentFilter btfilter = new IntentFilter(BROADCAST_MSG_FILE_LIST);
		btfilter.addAction(BROADCAST_MSG_FILE_LIST_VIDEO);
		btfilter.addAction(BROADCAST_MSG_FILE_LIST_VIDEO_LONG_CLICK);
		btfilter.addAction(BROADCAST_MSG_UPLOAD_PROGRESS_LIST);
		registerReceiver(toolBroadcastReceive, btfilter);
		/**
		 * 扫描sdcard free
		 */
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.obtainMessage(HANDLER_MSG_SDCARD_FREE).sendToTarget();
			}
		}, 1000, 5000);
		//开启service服务
    	startService(new Intent(this, com.feifei.video.service.BackgroundService.class));
		init();
	}

	/**
	 * 初始化组件
	 */
	@SuppressWarnings("deprecation")
	private void init() {
		sp = this.getSharedPreferences("videoRecord", 0);
		recordList = (ListView)this.findViewById(R.id.recordList);
		menuLiner = (LinearLayout)this.findViewById(R.id.menuLiner);
		operationLiner = (LinearLayout)this.findViewById(R.id.operationLiner);
		start = (Button) this.findViewById(R.id.start);
		stop = (Button) this.findViewById(R.id.stop);
		stop.setVisibility(View.GONE);
		
		menu_back = (Button)this.findViewById(R.id.menu_back);
		menu_pause = (Button)this.findViewById(R.id.menu_pause);
		menu_start = (Button)this.findViewById(R.id.menu_start);
		videoScale = (ImageButton)this.findViewById(R.id.videoScale);
		//uploadStatus,serverConfig,about,menu
		uploadStatus = (ImageButton)this.findViewById(R.id.uploadStatus);
		serverConfig = (ImageButton)this.findViewById(R.id.serverConfig);
		about = (ImageButton)this.findViewById(R.id.about);
		menu = (ImageButton)this.findViewById(R.id.menu);
		
		
		sdcardSizeText = (TextView)this.findViewById(R.id.sdcardSizeText);
		sdcardSizeText.setText("Free: "+CommonUtils.getSdcardFree(this)+"M");
		
		videoTime = (TextView)this.findViewById(R.id.videoTime);
		
		
		start.setOnClickListener(this);
		stop.setOnClickListener(this);
		menu_back.setOnClickListener(this);
		menu_pause.setOnClickListener(this);
		menu_start.setOnClickListener(this);
		videoScale.setOnClickListener(this);
		
		surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
		SurfaceHolder holder = surfaceview.getHolder();// 取得holder
		holder.addCallback(this); // holder加入回调接口
		// setType必须设置，要不出错.
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		adapter = new VideoFileListAdapter(this,new ArrayList<Map<String,Object>>());
		recordList.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.start:
			record();
			break;
		case R.id.stop:
			stopRecord("");
			break;
		case R.id.menu_back:
			menuLiner.setVisibility(View.GONE);
			menu_pause.setVisibility(View.GONE);
			menu_start.setVisibility(View.GONE);
			if(player!=null)
				player.reset();
			isReturn = false;
			adapter.refresh(resultDir);
			break;
		case R.id.menu_pause:
			if(player.isPlaying()){
				player.pause();
			}
			menu_start.setVisibility(View.VISIBLE);
			menu_pause.setVisibility(View.GONE);
			break;
		case R.id.menu_start:
			player.start();
			menu_pause.setVisibility(View.VISIBLE);
			menu_start.setVisibility(View.GONE);
			break;
		case R.id.videoScale:
			/*FrameLayout.LayoutParams layoutParams;
			layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			surfaceview.setLayoutParams(layoutParams);*/
			/*if(videoScale.getText().equals("+")){
				operationLiner.setVisibility(View.GONE);
				videoScale.setText("-");				
			}else{
				operationLiner.setVisibility(View.VISIBLE);
				videoScale.setText("+");	
			}*/
			break;
		case R.id.uploadStatus:
			break;
		case R.id.serverConfig:
			break;
		case R.id.about:
			break;
		case R.id.menu:
			break;
		default:
			break;
		}

	}

	public void countVideoTimer(final String type,  final int count){
		VideoTimer = new Timer();
		VideoTimer.schedule(new TimerTask() {
			int c = count;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(type.equals("--")){
					c--;
				}else{
					c++;
					if(c>=600){
						System.out.println("停止这一时段的录制----延迟3s开始下一个--600---");
//						stopRecord();
						handler.obtainMessage(HANDLER_MSG_SHOW_STOP_RECORD,null).sendToTarget();					
					}
				}
				if(c>=0){
					handler.obtainMessage(HANDLER_MSG_SHOW_VIDEO_TIME,countTime(c)).sendToTarget();					
				}
			}
		}, 1000, 1000);
	}
	
	/**
	 * 时间秒数转换
	 * @param count
	 * @return
	 */
	public static String countTime(int count){
		String time = "";
		if(count<10){
			time = "00:"+"0"+count;
		}else{
			if(count>=10&&count<60){
				time = "00:"+count;
			}else if(count<3600){
				int a = count%60;
				int b = count/60;
				String minu = "";
				String sec = "";
				System.out.println(a+"--"+b);
				if(b<10){
					minu = "0"+ b;
				}else{
					if(b>=10&&b<60){
						minu = b+"";
					}
				}
				sec = a<10?("0"+a):a+"";
				time = minu+":"+sec;
			}else{
				System.out.println("超长时间！！！");
			}
		}
		System.out.println(time);
		return time;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
		surfaceHolder = holder;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
		surfaceHolder = holder;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Canvas canvas = surfaceHolder.lockCanvas(null);//获取画布
//				canvas.drawColor(Color.WHITE);
				Paint mPaint = new Paint();  
                mPaint.setColor(Color.BLACK);
                Resources res = getResources();  
                Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.background);
                canvas.drawBitmap(bmp, 0, 0, mPaint);
//                canvas.drawText("准备录制", 100, 100, mPaint);
                surfaceHolder.unlockCanvasAndPost(canvas);//解锁画布，提交画好的图像
			}
		}).start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// surfaceDestroyed的时候同时对象设置为null
		System.out.println("surfaceDestroyed----");
//		surfaceview = null;
//		surfaceHolder = null;
		mediarecorder = null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (toolBroadcastReceive != null) {
			unregisterReceiver(toolBroadcastReceive);
		}
		if(player!=null){
			if (player.isPlaying()) {
				player.stop();
			}
			player.release();
		}
		timer.cancel();
	}
	/**
	 * 日期时间格式
	 * @param type
	 * @return
	 */
	public String getDate(String type) {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
//		int day = c.get(Calendar.DAY_OF_MONTH);
		String str = (new SimpleDateFormat("yyyy-MM-dd HHmmss")).format(c
				.getTime());
		if (type.equals("M")) {
			return year + "-" + month;
		}
		return str;
	}

	/**
	 * 保存视频文件路径
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public String getFileName(){
		String dir = Constants.DEFAULT_VIDEO_PATH+ getDate("M") + "-video/";
		String fpath = dir+ getDate("A")+"recording.mp4";
		File fdir = new File(dir);
		try {
			if(!fdir.exists()){
				fdir.mkdirs();	
				fdir.createNewFile();
			}
			return fpath;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 停止录制，释放资源
	 */
	public void stopRecord(String type){
		stop.setVisibility(View.GONE);
		start.setVisibility(View.VISIBLE);
		CustomeToast.showCustomeToast(HomeMediaRecorderActivity.this, "录制完成！", R.drawable.rectmenuidi_hover, "bottom");
		VideoTimer.cancel();
		if (mediarecorder != null) {
			// 停止录制
			mediarecorder.stop();
			// 释放资源
			mediarecorder.release();
			mediarecorder = null;
			sdcardSizeText.setText("Free: "+CommonUtils.getSdcardFree(this)+"M");
			File f = new File(videoOutPath);
			f.renameTo(new File(videoOutPath.replace("recording", "")));
			videoOutPath = "";
			f.delete();
			
			if(currentDir!=null)
				refreshVideoFile(currentDir);
		}
		if(type.equals("loop")){
			try {
				Thread.sleep(3000);
				record();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * 录制视频
	 */
	public void record(){
		//释放播放资源
		if(player!=null){
			if (player.isPlaying()) {
				player.stop();
			}
			player.release();
		}
		start.setVisibility(View.GONE);
		stop.setVisibility(View.VISIBLE);
		CustomeToast.showCustomeToast(HomeMediaRecorderActivity.this, "开始录制！", R.drawable.rectmenuidi_hover, "bottom");
		mediarecorder = new MediaRecorder();// 创建mediarecorder对象
		// 设置录制视频源为Camera(相机)
		mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		//设置录音源为麦克风
		mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
		mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		// 设置录制的视频编码h263 h264
		mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
		mediarecorder.setVideoSize(1920, 1080);
		/*DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		mediarecorder.setVideoSize(screenWidth, screenHeight);*/
		// 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
//		mediarecorder.setVideoFrameRate(20);
		mediarecorder.setVideoEncodingBitRate(10*1024*1024);
		//设置音频编码
		mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); 
		mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
		// 设置视频文件输出的路径
		videoOutPath = getFileName();
		mediarecorder.setOutputFile(videoOutPath);
		try {
			// 准备录制
			mediarecorder.prepare();
			// 开始录制
			mediarecorder.start();
			countVideoTimer("++",0);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 播放视频
	 * @param f 播放路径
	 */
	public void play(File f){
        try {
        	if(VideoTimer!=null)
        		VideoTimer.cancel();
        	//互相释放资源
        	stop.setVisibility(View.GONE);
			start.setVisibility(View.VISIBLE);
			if (mediarecorder != null) {
				// 停止录制
				mediarecorder.stop();
				// 释放资源
				mediarecorder.release();
				mediarecorder = null;
				sdcardSizeText.setText("Free: "+CommonUtils.getSdcardFree(this)+"M");
				
				File fRecording = new File(videoOutPath);
				fRecording.renameTo(new File(videoOutPath.replace("recording", "")));
				videoOutPath = "";
				fRecording.delete();
				
				if(currentDir!=null)
					refreshVideoFile(currentDir);
				VideoTimer.cancel();
				CustomeToast.showCustomeToast(HomeMediaRecorderActivity.this, "打断录制-录制完成！", R.drawable.rectmenuidi_hover, "bottom");
			}
			
			//设置mediaplayer
			if(player!=null){
				player.reset();//重置为初始状态	
				VideoTimer.cancel();
			}
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置音乐流的类型
    		player.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					menu_pause.setVisibility(View.GONE);
					menu_start.setVisibility(View.GONE);
					VideoTimer.cancel();
//					player.reset();//重置为初始状态
				}
			});
    		player.setDisplay(surfaceview.getHolder());//设置video影片以surfaceview holder播放
        	//设置路径
			player.setDataSource(f.getAbsolutePath());
			player.prepare();//缓冲
			player.start();//播放
//			CustomeToast.showDefaultToast(HomeMediaRecorderActivity.this, "该视频长度："+player.getDuration(), "");
			countVideoTimer("--",(int)(player.getDuration()/1000));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			menu_pause.setVisibility(View.GONE);
			menu_start.setVisibility(View.GONE);
			CustomeToast.showDefaultToast(HomeMediaRecorderActivity.this, "抱歉，该视频不能播放！", "");
			e.printStackTrace();
//			player.stop();
		}
	}
	
	/**
	 * refresh video file 刷新数据
	 */
	public void refreshVideoFile(File f){
		isReturn = true;
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		for (String str : CommonUtils.getDirFiles(f)) {
			Map<String,Object> m = new HashMap<String,Object>();
			m.put("icon", R.drawable.rectmenuidi_hover);
			m.put("text", str.substring(8, str.length()));
			m.put("FText", f.getAbsolutePath()+"/"+str);
			result.add(m);
		}
		handler.obtainMessage(HANDLER_MSG_SHOW_FILE_DIR_VIDEO_LIST, result).sendToTarget();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId()==R.id.progress){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.setCanceledOnTouchOutside(true);
			dialog.getWindow().setContentView(R.layout.upload_progress_layout);
			listProgress = (ListView)dialog.findViewById(R.id.progressListView);
		}else if(item.getItemId()==R.id.declared){
			Editor editor = sp.edit();
			if(!sp.getBoolean("startUpload", false)){
				editor.putBoolean("startUpload", true);
				System.out.println("开始上传权限！");
			}else{
				editor.putBoolean("startUpload", false);
				System.out.println("关闭上传权限！");
			}
			editor.commit();
		}
		return super.onMenuItemSelected(featureId, item);
	}
	/**
	 * 公共广播内部类
	 * @author Administrator
	 *
	 */
	class ToolBroadcastReceive extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(BROADCAST_MSG_FILE_LIST)){
				List<String> list = intent.getStringArrayListExtra("FileList");
				System.out.println("-----------"+list);
				resultDir = new ArrayList<Map<String,Object>>();
				for (String str : list) {
					Map<String,Object> m = new HashMap<String,Object>();
					m.put("icon", R.drawable.rectmenuidi_hover);
					m.put("text", str);
					m.put("FText", Constants.DEFAULT_VIDEO_PATH+str);
					resultDir.add(m);
				}
				if(!isReturn){
					handler.obtainMessage(HANDLER_MSG_SHOW_FILE_DIR_LIST, resultDir).sendToTarget();					
				}
			}else if(intent.getAction().equals(BROADCAST_MSG_FILE_LIST_VIDEO)){
				isReturn = true;
				CustomeToast.showDefaultToast(HomeMediaRecorderActivity.this, intent.getStringExtra("fileName")+"", "");
				File f = new File(intent.getStringExtra("fileName"));
				if(f.isDirectory()){
					currentDir = f;
					refreshVideoFile(f);
					/*List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
					for (String str : CommonUtils.getDirFiles(f)) {
						Map<String,Object> m = new HashMap<String,Object>();
						m.put("icon", R.drawable.rectmenuidi_hover);
						m.put("text", str.substring(8, str.length()));
						m.put("FText", f.getAbsolutePath()+"/"+str);
						result.add(m);
					}
					handler.obtainMessage(HANDLER_MSG_SHOW_FILE_DIR_VIDEO_LIST, result).sendToTarget();*/
				}else{
//					System.out.println("暂未实现播放！");
					handler.obtainMessage(HANDLER_MSG_SHOW_FILE_DIR_VIDEO_PLAY, f).sendToTarget();
				}
			}else if(intent.getAction().equals(BROADCAST_MSG_FILE_LIST_VIDEO_LONG_CLICK)){
				final File f = new File(intent.getStringExtra("fileName"));
				if(!f.isDirectory()){
					
				}
			}else if(intent.getAction().equals(BROADCAST_MSG_UPLOAD_PROGRESS_LIST)){
				String fileName = intent.getStringExtra("fileName");
				String progress = intent.getStringExtra("progress");
				System.out.println("文件名："+fileName+"进度："+progress);
				String msg = fileName+"---"+progress+"%";
				//进度显示有问题。需要调整，显示所有同时进行的上传进度
				if(progressList.size()==0){
					progressList.add(msg);					
				}else{
					for(int i=0;i<progressList.size();i++){
						if(progressList.get(i).contains("100%")){
							progressList.remove(i);
						}else{
							if(progressList.get(i).contains(fileName)){
								progressList.remove(i);
								progressList.add(msg);
//								break;
							}else{
								progressList.add(msg);
							}							
						}
					}
				}
				handler.obtainMessage(HANDLER_MSG_SHOW_FILE_PROGRESS, progressList).sendToTarget();
			}
		}
		
	}
}
