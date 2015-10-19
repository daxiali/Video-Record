package com.feifei.video.util;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.net.ftp.FTPClient;

import com.feifei.video.activity.HomeMediaRecorderActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Environment;

/**
 * 文件操作工具类
 * 
 * @author Administrator
 *
 */
public class FileUtils {
	private static FTPClient ftp;

	/**
	 * 
	 * @param path
	 *            上传到ftp服务器哪个路径下
	 * @param addr
	 *            地址
	 * @param port
	 *            端口号
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 * @throws Exception
	 */
	public static boolean connect(String path, String addr, int port,
			String username, String password) throws Exception {
		boolean result = false;
		ftp = new FTPClient();
		ftp.setConnectTimeout(5000);//连接超时
		int reply;
		ftp.connect(addr, port);
		result = ftp.login(username, password);
//		ftp.enterLocalPassiveMode();//被动模式
		ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
		ftp.setBufferSize(1024*1024*10);//设置缓存大小。默认1024 1k
		// 权限问题。暂时屏蔽
		/*
		 * reply = ftp.getReplyCode(); if
		 * (!FTPReply.isPositiveCompletion(reply)) { ftp.disconnect(); return
		 * result; }
		 */
		ftp.changeWorkingDirectory(path);
		// result = true;
		return result;
	}

	/**
	 * 
	 * @param file
	 *            上传的文件或文件夹
	 * @throws Exception
	 */
	public static void upload(File file,Context context) throws Exception {
		uploadFile("",file,ftp,0,context);
		/*if (file.isDirectory()) {
			ftp.makeDirectory(file.getName());
			ftp.changeWorkingDirectory(file.getName());
			String[] files = file.list();
			for (int i = 0; i < files.length; i++) {
				File file1 = new File(file.getPath() + "\\" + files[i]);
				if (file1.isDirectory()) {
					upload(file1);
					ftp.changeToParentDirectory();
				} else {
					File file2 = new File(file.getPath() + "\\" + files[i]);
					FileInputStream input = new FileInputStream(file2);
					ftp.storeFile(file2.getName(), input);
					input.close();
				}
			}
		} else {
			File file2 = new File(file.getPath());
			FileInputStream input = new FileInputStream(file2);
			ftp.storeFile(file2.getName(), input);
			input.close();
		}*/
	}

	/**
	 * 压缩文件-由于out要在递归调用外,所以封装一个方法用来 调用ZipFiles(ZipOutputStream out,String
	 * path,File... srcFiles)
	 * 
	 * @param zip
	 *            压缩包
	 * @param path
	 *            压缩路径
	 * @param srcFiles
	 *            源文件
	 * @throws IOException
	 * 
	 * File zip = new File(rootDir+"/"+zipName+".zip");
	 * FileUtils.ZipFiles(zip, "", f);
	 * FileUtils.upload(zip);
	 */
	public static void ZipFiles(File zip, String path, File... srcFiles)
			throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
		ZipFiles(out, path, srcFiles);
		out.close();
		System.out.println("*****************压缩完毕*******************");
	}

	/**
	 * 压缩文件-File
	 * 
	 * @param zipFile
	 *            zip文件
	 * @param srcFiles
	 *            被压缩源文件
	 */
	public static void ZipFiles(ZipOutputStream out, String path,
			File... srcFiles) {
		path = path.replaceAll("\\*", "/");
		if (!path.endsWith("/")) {
			path += "/";
		}
		byte[] buf = new byte[1024];
		try {
			for (int i = 0; i < srcFiles.length; i++) {
				if (srcFiles[i].isDirectory()) {
					File[] files = srcFiles[i].listFiles();
					String srcPath = srcFiles[i].getName();
					srcPath = srcPath.replaceAll("\\*", "/");
					if (!srcPath.endsWith("/")) {
						srcPath += "/";
					}
					out.putNextEntry(new ZipEntry(path + srcPath));
					ZipFiles(out, path + srcPath, files);
				} else {
					FileInputStream in = new FileInputStream(srcFiles[i]);
					System.out.println(path + srcFiles[i].getName());
					out.putNextEntry(new ZipEntry(path + srcFiles[i].getName()));
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.closeEntry();
					in.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解压zip格式的压缩包
	 * 
	 * @param filePath
	 *            压缩文件路径
	 * @param outPath
	 *            输出路径
	 * @param type
	 *            下载类型
	 * 
	 * @return 解压成功或失败标志
	 * 
	 */
	public static Boolean unZip(String filePath, String outPath, String type) {
		String unzipfile = filePath; // 解压缩的文件名
		try {
			ZipInputStream zin = new ZipInputStream(new FileInputStream(
					unzipfile));
			ZipEntry entry;
			// 创建文件夹
			while ((entry = zin.getNextEntry()) != null) {
				if (type.equals("apk")) {
					File myFile = new File(entry.getName());
					FileOutputStream fout = new FileOutputStream(outPath
							+ myFile.getPath());
					DataOutputStream dout = new DataOutputStream(fout);
					byte[] b = new byte[1024];
					int len = 0;
					while ((len = zin.read(b)) != -1) {
						dout.write(b, 0, len);
					}
					dout.close();
					fout.close();
					zin.closeEntry();
				}
			}
			// }
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 遍历tcv_skin文件夹，删除指定pid vid文件,保证皮肤文件独立性
	 * 
	 * @param pidVid
	 */
	public static void scanFileDelete(String pidVid) {
		String skin_path = Environment.getExternalStorageDirectory()
				+ "/tcv_skin";
		File skinFile = new File(skin_path);
		if (skinFile.exists()) {
			File[] item_file = skinFile.listFiles();
			for (int i = 0; i < item_file.length; i++) {
				if (item_file[i].getName().contains(pidVid)) {
					// item_file[i].delete();
					File item2 = item_file[i];
					if (item2.isDirectory()) {
						deleteDir(item2);
					} else {
						item2.delete();
					}
				}
			}
		}

	}

	/**
	 * 查看文件是否存在
	 */
	public static boolean scanFileExist(String fileName){
		String skin_path = Environment.getExternalStorageDirectory()
				+ "/tcv_skin";
		File skinFile = new File(skin_path);
		if (skinFile.exists()) {
			File[] item_file = skinFile.listFiles();
			for (int i = 0; i < item_file.length; i++) {
				if (item_file[i].getName().contains(fileName)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param dir
	 *            将要删除的文件目录
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * 清理sdcard
	 */
	public static void cleanSdcardTcv(){
		File file = Environment.getExternalStorageDirectory();
		File[] children = file.listFiles();
		for(int i=0;i<children.length;i++){
			File item = children[i];
			if(item.getName().contains("Phone Link")){
				FileUtils.deleteDir(item);
			}
		}
	}
	/**
	 * 解压文件到指定目录
	 * 
	 * @param zipFile
	 * @param descDir
	 */
	@SuppressWarnings("rawtypes")
	public static void unZipFiles(File zipFile, String descDir) throws IOException {
		File pathFile = new File(descDir);
		if (!pathFile.exists()) {
			pathFile.mkdirs();
		}
		ZipFile zip = new ZipFile(zipFile);
		for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String zipEntryName = entry.getName();
			InputStream in = zip.getInputStream(entry);
			String outPath = (descDir + zipEntryName).replaceAll("\\*", "/");
			;
			// 判断路径是否存在,不存在则创建文件路径
			File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
			if (!file.exists()) {
				file.mkdirs();
			}
			// 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
			if (new File(outPath).isDirectory()) {
				continue;
			}
			// 输出文件路径信息
			System.out.println(outPath);

			OutputStream out = new FileOutputStream(outPath);
			byte[] buf1 = new byte[1024];
			int len;
			while ((len = in.read(buf1)) > 0) {
				out.write(buf1, 0, len);
			}
			in.close();
			out.close();
		}
		System.out.println("******************解压完毕********************");
	}

	 /**  
     * 上传文件到服务器,新上传和断点续传  
     * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变  
     * @param localFile 本地文件File句柄，绝对路径  
     * @param processStep 需要显示的处理进度步进值  
     * @param ftpClient FTPClient引用  
     * @return  
     * @throws IOException  
     */  
    public static boolean uploadFile(String remoteFile,File localFile,FTPClient ftpClient,long remoteSize,Context context) throws IOException{   
        //显示进度的上传   
        long step = localFile.length() / 100;   
        long process = 0;   
        long localreadbytes = 0L;   
        RandomAccessFile raf = new RandomAccessFile(localFile,"r");
        
        OutputStream out = null;
        if("".equals(remoteFile)){
        	out = ftpClient.appendFileStream(new String(localFile.getName().getBytes("GBK"),"iso-8859-1"));        	
        }else{
        	out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"),"iso-8859-1"));        	
        }
//        byte[] bytes = new byte[1024];   
        byte[] bytes = new byte[ftpClient.getBufferSize() >= 0 ? ftpClient.getBufferSize() : 1024];
        int c;   
        while((c = raf.read(bytes))!= -1){   
            out.write(bytes,0,c);   
            localreadbytes+=c;   
            if(localreadbytes / step != process){   
                process = localreadbytes / step;   
//                System.out.println("上传进度:"+localFile.getName()+"---" + process);   
                //TODO 汇报上传状态   
                Intent i = new Intent();
                i.setAction(HomeMediaRecorderActivity.BROADCAST_MSG_UPLOAD_PROGRESS_LIST);
                i.putExtra("fileName", localFile.getName());
                i.putExtra("progress", process+"");
                context.sendBroadcast(i);
            }   
        }   
        out.flush();   
        raf.close();   
        out.close();   
        boolean result =ftpClient.completePendingCommand();   
        return result;
    }   
	public static void main(String[] args) {
		try {
			boolean connect = FileUtils.connect("", "10.11.0.241", 21,
					" report_log", "sigma_log_A12B1");
			System.out.println(connect);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
