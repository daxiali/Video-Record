package com.feifei.video.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.feifei.video.R;

public class HomeVideoActivity extends BaseActivity {

	private Button startVideo;
	private static final int RESULT_CAPTURE_IMAGE = 1;// �����requestCode
	private static final int REQUEST_CODE_TAKE_VIDEO = 2;// ����������requestCode
	private static final int RESULT_CAPTURE_RECORDER_SOUND = 3;// ¼����requestCode

	private String strImgPath = "";// ��Ƭ�ļ�����·��
	private String strVideoPath = "";// ��Ƶ�ļ��ľ���·��
	private String strRecorderPath = "";// ¼���ļ��ľ���·��

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentViewWithoutTitle(R.layout.record_bill_layout);
		initLayout();
	}

	/**
	 * ��ʼ�����
	 */
	public void initLayout() {
		startVideo = (Button) this.findViewById(R.id.startVideo);
		startVideo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				videoMethod();
				Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");   
				startActivity(wifiSettingsIntent); 
			}
		});
	}
	/**
     * ���๦��
     */
    private void cameraMethod() {
            Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            strImgPath = Environment.getExternalStorageDirectory().toString() + "/CONSDCGMPIC/";//�����Ƭ���ļ���
            String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";//��Ƭ����
            File out = new File(strImgPath);
            if (!out.exists()) {
                    out.mkdirs();
            }
            out = new File(strImgPath, fileName);
            strImgPath = strImgPath + fileName;//����Ƭ�ľ���·��
            Uri uri = Uri.fromFile(out);
            imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            imageCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(imageCaptureIntent, RESULT_CAPTURE_IMAGE);

    }

    /**
     * ������Ƶ
     */
    private void videoMethod() {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
    }

    /**
     * ¼������
     */
    private void soundRecorderMethod() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/amr");
            startActivityForResult(intent, RESULT_CAPTURE_RECORDER_SOUND);
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case RESULT_CAPTURE_IMAGE:// ����
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, strImgPath, Toast.LENGTH_SHORT).show();
			}
			break;
		case REQUEST_CODE_TAKE_VIDEO:// ������Ƶ
			if (resultCode == RESULT_OK) {
				Uri uriVideo = data.getData();
				Cursor cursor = this.getContentResolver().query(uriVideo, null,null, null, null);
				if (cursor.moveToNext()) {
					/** _data���ļ��ľ���·�� ��_display_name���ļ��� */
					strVideoPath = cursor.getString(cursor.getColumnIndex("_data"));
					Toast.makeText(this, strVideoPath, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case RESULT_CAPTURE_RECORDER_SOUND:// ¼��
			if (resultCode == RESULT_OK) {
				Uri uriRecorder = data.getData();
				Cursor cursor = this.getContentResolver().query(uriRecorder,
						null, null, null, null);
				if (cursor.moveToNext()) {
					/** _data���ļ��ľ���·�� ��_display_name���ļ��� */
					strRecorderPath = cursor.getString(cursor
							.getColumnIndex("_data"));
					Toast.makeText(this, strRecorderPath, Toast.LENGTH_SHORT)
							.show();
				}
			}
			break;
		}
	}
}
