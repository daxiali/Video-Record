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
	 * ��ȡĿ¼�ļ��б�
	 */
	public static final String BROADCAST_MSG_FILE_LIST = "broadcast.msg.file.list";
	/**
	 * ��Ƶ�ļ�
	 */
	public static final String BROADCAST_MSG_FILE_LIST_VIDEO = "broadcast.msg.file.list.video";
	
	/**
	 * ������Ƶ�б�
	 */
	public static final String BROADCAST_MSG_FILE_LIST_VIDEO_LONG_CLICK = "broadcast.msg.file.list.video.long.click";
	
	/**
	 * �ϴ������б�
	 */
	public static final String BROADCAST_MSG_UPLOAD_PROGRESS_LIST = "broadcast.msg.upload.progress.list";
	
	/**
	 * ��ȡsdcardʣ��ռ��С
	 */
	private static final int HANDLER_MSG_SDCARD_FREE = 1;
	
	private static final int HANDLER_MSG_SHOW_FILE_DIR_LIST = 2;
	
	private static final int HANDLER_MSG_SHOW_FILE_DIR_VIDEO_LIST = 3;
	
	private static final int HANDLER_MSG_SHOW_FILE_DIR_VIDEO_PLAY = 4;
	
	private static final int HANDLER_MSG_SHOW_VIDEO_TIME = 5;
	
	private static final int HANDLER_MSG_SHOW_STOP_RECORD = 6;
	
	private static final int HANDLER_MSG_SHOW_FILE_PROGRESS = 7;
	
	private Button start;// ��ʼ¼�ư�ť
	private Button stop;// ֹͣ¼�ư�ť
	private Button menu_back,menu_pause,menu_start;
	private ImageButton videoScale,uploadStatus,serverConfig,about,menu;
	private TextView sdcardSizeText;//��ʾsdcard��С
	private TextView videoTime;//��ʾ��Ƶʱ��
	private ListView recordList;
	private LinearLayout menuLiner,operationLiner;
	
	private MediaRecorder mediarecorder;// ¼����Ƶ����
	private MediaPlayer player;
	private Camera camera;
	
	private SurfaceView surfaceview;// ��ʾ��Ƶ�Ŀؼ�
	// ������ʾ��Ƶ��һ���ӿڣ��ҿ����û����У�Ҳ����˵��mediarecorder¼����Ƶ���ø������濴
	// ��͵͵¼��Ƶ��ͬѧ���Կ��Ǳ�İ취��������Ҫʵ������ӿڵ�Callback�ӿ�
	private SurfaceHolder surfaceHolder;
	private Timer timer,VideoTimer;
	private ToolBroadcastReceive toolBroadcastReceive;
	private VideoFileListAdapter adapter;
	private String videoOutPath = "";
	private ListView listProgress; 
	private List<String>progressList = new ArrayList<String>();
	/**
	 * ���ڷ�����������
	 */
	private List<Map<String,Object>> resultDir;
	
	/**
	 * �ж��Ƿ���Ŀ¼�����ļ��б�
	 */
	private boolean isReturn = false;
	/**
	 * ��¼��ǰĿ¼������¼����ɺ�ˢ������
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
					CustomeToast.showDefaultToast(HomeMediaRecorderActivity.this, "�ļ�Ϊ�գ�", "");
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
		// ���ú�����ʾ
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// ѡ��֧�ְ�͸��ģʽ,����surfaceview��activity��ʹ�á�
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentViewWithoutTitle(R.layout.media_recorder_layout);
		
		toolBroadcastReceive = new ToolBroadcastReceive();
		IntentFilter btfilter = new IntentFilter(BROADCAST_MSG_FILE_LIST);
		btfilter.addAction(BROADCAST_MSG_FILE_LIST_VIDEO);
		btfilter.addAction(BROADCAST_MSG_FILE_LIST_VIDEO_LONG_CLICK);
		btfilter.addAction(BROADCAST_MSG_UPLOAD_PROGRESS_LIST);
		registerReceiver(toolBroadcastReceive, btfilter);
		/**
		 * ɨ��sdcard free
		 */
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.obtainMessage(HANDLER_MSG_SDCARD_FREE).sendToTarget();
			}
		}, 1000, 5000);
		//����service����
    	startService(new Intent(this, com.feifei.video.service.BackgroundService.class));
		init();
	}

	/**
	 * ��ʼ�����
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
		SurfaceHolder holder = surfaceview.getHolder();// ȡ��holder
		holder.addCallback(this); // holder����ص��ӿ�
		// setType�������ã�Ҫ������.
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
						System.out.println("ֹͣ��һʱ�ε�¼��----�ӳ�3s��ʼ��һ��--600---");
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
	 * ʱ������ת��
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
				System.out.println("����ʱ�䣡����");
			}
		}
		System.out.println(time);
		return time;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// ��holder�����holderΪ��ʼ��oncreat����ȡ�õ�holder����������surfaceHolder
		surfaceHolder = holder;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// ��holder�����holderΪ��ʼ��oncreat����ȡ�õ�holder����������surfaceHolder
		surfaceHolder = holder;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Canvas canvas = surfaceHolder.lockCanvas(null);//��ȡ����
//				canvas.drawColor(Color.WHITE);
				Paint mPaint = new Paint();  
                mPaint.setColor(Color.BLACK);
                Resources res = getResources();  
                Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.background);
                canvas.drawBitmap(bmp, 0, 0, mPaint);
//                canvas.drawText("׼��¼��", 100, 100, mPaint);
                surfaceHolder.unlockCanvasAndPost(canvas);//�����������ύ���õ�ͼ��
			}
		}).start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// surfaceDestroyed��ʱ��ͬʱ��������Ϊnull
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
	 * ����ʱ���ʽ
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
	 * ������Ƶ�ļ�·��
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
	 * ֹͣ¼�ƣ��ͷ���Դ
	 */
	public void stopRecord(String type){
		stop.setVisibility(View.GONE);
		start.setVisibility(View.VISIBLE);
		CustomeToast.showCustomeToast(HomeMediaRecorderActivity.this, "¼����ɣ�", R.drawable.rectmenuidi_hover, "bottom");
		VideoTimer.cancel();
		if (mediarecorder != null) {
			// ֹͣ¼��
			mediarecorder.stop();
			// �ͷ���Դ
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
	 * ¼����Ƶ
	 */
	public void record(){
		//�ͷŲ�����Դ
		if(player!=null){
			if (player.isPlaying()) {
				player.stop();
			}
			player.release();
		}
		start.setVisibility(View.GONE);
		stop.setVisibility(View.VISIBLE);
		CustomeToast.showCustomeToast(HomeMediaRecorderActivity.this, "��ʼ¼�ƣ�", R.drawable.rectmenuidi_hover, "bottom");
		mediarecorder = new MediaRecorder();// ����mediarecorder����
		// ����¼����ƵԴΪCamera(���)
		mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		//����¼��ԴΪ��˷�
		mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// ����¼����ɺ���Ƶ�ķ�װ��ʽTHREE_GPPΪ3gp.MPEG_4Ϊmp4
		mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		// ����¼�Ƶ���Ƶ����h263 h264
		mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		// ������Ƶ¼�Ƶķֱ��ʡ�����������ñ���͸�ʽ�ĺ��棬���򱨴�
		mediarecorder.setVideoSize(1920, 1080);
		/*DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		mediarecorder.setVideoSize(screenWidth, screenHeight);*/
		// ����¼�Ƶ���Ƶ֡�ʡ�����������ñ���͸�ʽ�ĺ��棬���򱨴�
//		mediarecorder.setVideoFrameRate(20);
		mediarecorder.setVideoEncodingBitRate(10*1024*1024);
		//������Ƶ����
		mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); 
		mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
		// ������Ƶ�ļ������·��
		videoOutPath = getFileName();
		mediarecorder.setOutputFile(videoOutPath);
		try {
			// ׼��¼��
			mediarecorder.prepare();
			// ��ʼ¼��
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
	 * ������Ƶ
	 * @param f ����·��
	 */
	public void play(File f){
        try {
        	if(VideoTimer!=null)
        		VideoTimer.cancel();
        	//�����ͷ���Դ
        	stop.setVisibility(View.GONE);
			start.setVisibility(View.VISIBLE);
			if (mediarecorder != null) {
				// ֹͣ¼��
				mediarecorder.stop();
				// �ͷ���Դ
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
				CustomeToast.showCustomeToast(HomeMediaRecorderActivity.this, "���¼��-¼����ɣ�", R.drawable.rectmenuidi_hover, "bottom");
			}
			
			//����mediaplayer
			if(player!=null){
				player.reset();//����Ϊ��ʼ״̬	
				VideoTimer.cancel();
			}
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);//����������������
    		player.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					menu_pause.setVisibility(View.GONE);
					menu_start.setVisibility(View.GONE);
					VideoTimer.cancel();
//					player.reset();//����Ϊ��ʼ״̬
				}
			});
    		player.setDisplay(surfaceview.getHolder());//����videoӰƬ��surfaceview holder����
        	//����·��
			player.setDataSource(f.getAbsolutePath());
			player.prepare();//����
			player.start();//����
//			CustomeToast.showDefaultToast(HomeMediaRecorderActivity.this, "����Ƶ���ȣ�"+player.getDuration(), "");
			countVideoTimer("--",(int)(player.getDuration()/1000));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			menu_pause.setVisibility(View.GONE);
			menu_start.setVisibility(View.GONE);
			CustomeToast.showDefaultToast(HomeMediaRecorderActivity.this, "��Ǹ������Ƶ���ܲ��ţ�", "");
			e.printStackTrace();
//			player.stop();
		}
	}
	
	/**
	 * refresh video file ˢ������
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
				System.out.println("��ʼ�ϴ�Ȩ�ޣ�");
			}else{
				editor.putBoolean("startUpload", false);
				System.out.println("�ر��ϴ�Ȩ�ޣ�");
			}
			editor.commit();
		}
		return super.onMenuItemSelected(featureId, item);
	}
	/**
	 * �����㲥�ڲ���
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
//					System.out.println("��δʵ�ֲ��ţ�");
					handler.obtainMessage(HANDLER_MSG_SHOW_FILE_DIR_VIDEO_PLAY, f).sendToTarget();
				}
			}else if(intent.getAction().equals(BROADCAST_MSG_FILE_LIST_VIDEO_LONG_CLICK)){
				final File f = new File(intent.getStringExtra("fileName"));
				if(!f.isDirectory()){
					
				}
			}else if(intent.getAction().equals(BROADCAST_MSG_UPLOAD_PROGRESS_LIST)){
				String fileName = intent.getStringExtra("fileName");
				String progress = intent.getStringExtra("progress");
				System.out.println("�ļ�����"+fileName+"���ȣ�"+progress);
				String msg = fileName+"---"+progress+"%";
				//������ʾ�����⡣��Ҫ��������ʾ����ͬʱ���е��ϴ�����
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
