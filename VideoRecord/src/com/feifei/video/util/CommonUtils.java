package com.feifei.video.util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.feifei.video.R;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class CommonUtils {
	private final static String TAG = "----CommonUtils----";
	/**
	 * ��ȡsdcardʣ���С
	 * @param ctx
	 * @return
	 */
	public static String getSdcardFree(Context ctx){
		String availableGbSizeStr = "";
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File filePath = Environment.getExternalStorageDirectory();    //���sd����·��
			StatFs stat=new StatFs(filePath.getPath());                 //����StatFs����
			long blockSize=stat.getBlockSize();                         //��ȡblock��size
			float totalBlocks=stat.getBlockCount();                     //��ȡblock������
			float totalGbSize = (blockSize*totalBlocks)/1024/1024/1024;
			DecimalFormat df = new DecimalFormat("#0.0");
			String totalGbSizeStr = df.format(totalGbSize);            //�ܹ���С
			long availableBlocks=stat.getAvailableBlocks();             //��ȡ���ÿ��С
			String usedTotalGbSizeStr = df.format((totalBlocks-availableBlocks) * blockSize /1024/1024/1024);//���ô�С
			availableGbSizeStr = df.format(availableBlocks * blockSize /1024/1024);
		}else{
			CustomeToast.showCustomeToast(ctx, "sd�������ڣ�", R.drawable.rectmenuidi_hover, "bottom");
		}
		return availableGbSizeStr;
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
}
