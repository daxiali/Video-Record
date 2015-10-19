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
	 * 获取sdcard剩余大小
	 * @param ctx
	 * @return
	 */
	public static String getSdcardFree(Context ctx){
		String availableGbSizeStr = "";
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File filePath = Environment.getExternalStorageDirectory();    //获得sd卡的路径
			StatFs stat=new StatFs(filePath.getPath());                 //创建StatFs对象
			long blockSize=stat.getBlockSize();                         //获取block的size
			float totalBlocks=stat.getBlockCount();                     //获取block的总数
			float totalGbSize = (blockSize*totalBlocks)/1024/1024/1024;
			DecimalFormat df = new DecimalFormat("#0.0");
			String totalGbSizeStr = df.format(totalGbSize);            //总共大小
			long availableBlocks=stat.getAvailableBlocks();             //获取可用块大小
			String usedTotalGbSizeStr = df.format((totalBlocks-availableBlocks) * blockSize /1024/1024/1024);//已用大小
			availableGbSizeStr = df.format(availableBlocks * blockSize /1024/1024);
		}else{
			CustomeToast.showCustomeToast(ctx, "sd卡不存在！", R.drawable.rectmenuidi_hover, "bottom");
		}
		return availableGbSizeStr;
	}
	/**
	 * 获取文件子目录名称
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
