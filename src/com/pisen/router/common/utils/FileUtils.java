package com.pisen.router.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.studio.os.EnvironmentUtils;
import android.text.TextUtils;
import android.util.Log;

/**
 * 文件类操作工具包
 * 
 * @author Liuhc
 * @version 1.0 2015年4月14日14:15:21
 */
public class FileUtils {

	// SD卡存放Lancher相关文件根目录
	public final static String APPROOT = "Android/data/com.pisen./files";

	public final static String PISEN_QR = "pisen_qr";

	/**
	 * 判断SD卡是否可用
	 * 
	 * @return
	 */
	public static boolean isSDAvailable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取SD卡根路径
	 * 
	 * @return
	 */
	public static String getSdcardPath() {
		String[] roots = EnvironmentUtils.getExternalStorageDirectoryAll();
		String rootPath = "";
		if (roots != null && roots.length > 0) {
			rootPath = roots[0];
		}
		return rootPath;
	}

	public static String getSharedFriends(){
		return getSdcardPath()+File.separator + PISEN_QR;
	}

	/**
	 * 获取app根根目录
	 * 
	 * @return
	 */
	public static String getAppPath() {
		return Environment.getExternalStorageDirectory() + File.separator + APPROOT;
	}

	/**
	 * 获取文件大小，单位为byte（若为目录，则包括所有子目录和文件）
	 * 
	 * @param file
	 * @return
	 */
	public static long getFileSize(File file) {
		long size = 0;
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] subFiles = file.listFiles();
				if (subFiles != null) {
					int num = subFiles.length;
					for (int i = 0; i < num; i++) {
						size += getFileSize(subFiles[i]);
					}
				}
			} else {
				size += file.length();
			}
		}
		return size;
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param dir
	 * @param fileName
	 * @return
	 */
	public static boolean exists(File dir, String fileName) {
		return new File(dir, fileName).exists();
	}

	public static boolean exists(String dir, String fileName) {
		return new File(dir, fileName).exists();
	}

	public static boolean exists(String filePath) {
		return new File(filePath).exists();
	}

	/**
	 * 根据文件绝对路径获取文件名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath) {
		if (TextUtils.isEmpty(filePath))
			return "";
		String result = filePath.substring(filePath.length()-1,filePath.length());
		if (File.separator.equals(result)) {
			result = filePath.substring(0,filePath.lastIndexOf(File.separator));
			result = result.substring(result.lastIndexOf(File.separator) + 1);
		}else{
			result = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
		}
		return result;
	}

	/**
	 * 获取上一级目录
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getUpFileName(String filePath) {
		if (TextUtils.isEmpty(filePath))
			return "";
		String result = filePath.substring(filePath.length()-1,filePath.length());
		if (File.separator.equals(result)) {
			result = filePath.substring(0,filePath.lastIndexOf(File.separator));
			result = result.substring(0,result.lastIndexOf(File.separator) + 1);
		}else{
			result = filePath.substring(0,filePath.lastIndexOf(File.separator) + 1);
		}
		return result;
	}

	/**
	 * 根据文件的绝对路径获取文件名但不包含扩展名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileNameNoFormat(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return "";
		}
		if (!filePath.contains(".")) {
			return filePath;
		}
		int point = filePath.lastIndexOf('.');
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1, point);
	}

	/**
	 * 获取文件扩展名
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName) {
		if (TextUtils.isEmpty(fileName) || !fileName.contains(".")){
			return "";
		}
		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}
	

	/**
	 * 获取文件大小
	 * 
	 * @param filePath
	 * @return
	 */
	public static long getFileSize(String filePath) {
		long size = 0;
		File file = new File(filePath);
		if (file != null && file.exists()) {
			size = file.length();
		}
		return size;
	}

	/**
	 * 获取文件大小
	 * 
	 * @param size
	 *            字节
	 * @return
	 */
	public static String getFileSize(long size) {
		if (size <= 0)
			return "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
		float temp = (float) size / 1024;
		if (temp >= 1024) {
			return df.format(temp / 1024) + "M";
		} else {
			return df.format(temp) + "K";
		}
	}

	/**
	 * 转换文件大小
	 * 
	 * @param fileS
	 * @return B/KB/MB/GB
	 */
	public static String formatFileSize(long fileS) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		String fileSizeString = "";
		if(fileS <=0) {
			fileSizeString = "0.00KB";
		} else if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取目录文件大小
	 * 
	 * @param dir
	 * @return
	 */
	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
		if (!dir.isDirectory()) {
			return 0;
		}
		long dirSize = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				dirSize += file.length();
			} else if (file.isDirectory()) {
				dirSize += file.length();
				dirSize += getDirSize(file); // 递归调用继续统计
			}
		}
		return dirSize;
	}

	/**
	 * 获取目录文件个数
	 * 
	 * @param f
	 * @return
	 */
	public long getFileList(File dir) {
		long count = 0;
		File[] files = dir.listFiles();
		count = files.length;
		for (File file : files) {
			if (file.isDirectory()) {
				count = count + getFileList(file);// 递归
				count--;
			}
		}
		return count;
	}

	/**
	 * 向App写图片
	 * 
	 * @param buffer
	 * @param folder
	 * @param fileName
	 * @return 成功返回True
	 */
	public static boolean writeFile(byte[] buffer, String folder, String fileName) {
		boolean writeSucc = false;

		if (!TextUtils.isEmpty(folder)) {
			File fileDir = new File(folder);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}

			File file = new File(folder, fileName);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(file);
				out.write(buffer);
				writeSucc = true;
			} catch (Exception e) {
				e.printStackTrace();
				writeSucc = false;
			} finally {
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					e.printStackTrace();
					writeSucc = false;
				}
			}
		}
		return writeSucc;
	}

	public static byte[] readStream(InputStream inStream) {
		// 把数据读取存放到内存中去
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		try {
			while ((len = inStream.read(buffer)) != -1) {
				outSteam.write(buffer, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outSteam.close();
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return outSteam.toByteArray();
	}

	public static String readInStream(FileInputStream inStream) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}

			outStream.close();
			inStream.close();
			return outStream.toString();
		} catch (IOException e) {
			Log.i("FileTest", e.getMessage());
		}
		return null;
	}

	public static String readInStream(InputStream inStream) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}

			outStream.close();
			inStream.close();
			return outStream.toString();
		} catch (IOException e) {
			Log.i("FileTest", e.getMessage());
		}
		return null;
	}

	/**
	 * 计算SD卡的剩余空间
	 * 
	 * @return 返回-1，说明没有安装sd卡
	 */
	public static long getFreeDiskSpace() {
		String status = Environment.getExternalStorageState();
		long freeSpace = 0;
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			try {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				freeSpace = availableBlocks * blockSize / 1024;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return -1;
		}
		return (freeSpace);
	}

	/**
	 * 新建目录
	 * 
	 * @param directoryName
	 * @return
	 */
	public static boolean createDirectory(String pathName) {
		if (!TextUtils.isEmpty(pathName)) {
			File newPath = new File(pathName);
			return newPath.mkdir();
		}
		return false;
	}

	public static File createFile(String folderPath, String fileName) {
		File destDir = new File(folderPath);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		return new File(folderPath, fileName);
	}

	/**
	 * 移动
	 * 
	 * @param fromPath
	 * @param toPath
	 */
	public static void move(String fromPath, String toPath) {
		copy(fromPath, toPath);
		deleteDirectory(fromPath);
	}

	/**
	 * 拷贝文件或文件夹下所有文件
	 * 
	 * @param fromPath
	 *            原始对象
	 * @param toPath
	 *            目标路径
	 * @return 操作成功次数
	 */
	public static int copy(String fromPath, String toPath) {
		int successNum = 0;
		if (TextUtils.isEmpty(fromPath) || TextUtils.isEmpty(toPath)) {
			return successNum;
		}

		// 要复制的文件目录
		File fromFile = new File(fromPath);
		if (fromFile == null || !fromFile.exists()) {
			return successNum;
		}
		// 目标目录
		File targetDir = new File(toPath);
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		if (!targetDir.isDirectory()) {
			return successNum;
		}
		if (fromFile.isDirectory()) {

			// 如果存在则获取当前目录下的全部文件 填充数组
			File[] currentFiles = fromFile.listFiles();
			for (File file : currentFiles) {
				if (file.isDirectory()) {
					successNum = copy(file.getPath() + "/", toPath + file.getName() + "/");
				} else {
					successNum = copyFile(file.getPath(), toPath + "/" + file.getName());
				}
			}
		} else if (fromFile.isFile()) {
			successNum = copyFile(fromPath, targetDir.getAbsolutePath() + "/" + fromFile.getName());
		}
		return successNum;
	}

	/**
	 * 所有非子目录文件拷贝
	 * 
	 * @param fromPath
	 *            原始对象
	 * @param toPath
	 *            目标路径
	 * @return 1:负责成功1次,0失败一次
	 */
	public static int copyFile(String fromPath, String toPath) {
		try {
			InputStream isfrom = new FileInputStream(fromPath);
			OutputStream osto = new FileOutputStream(toPath);
			byte bt[] = new byte[1024];
			int c;
			while ((c = isfrom.read(bt)) > 0) {
				osto.write(bt, 0, c);
			}
			isfrom.close();
			osto.close();
			return 1;

		} catch (Exception ex) {
		}
		return 0;
	}

	/**
	 * 递归删除文件目录(包括：目录里的所有文件)
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean deleteDirectory(String filePath) {
		boolean result = false;
		if (!TextUtils.isEmpty(filePath)) {
			File file = new File(filePath);
			if (!file.exists()) {
				return result;
			}
			if (file.isDirectory()) {
				String[] listfile = file.list();
				try {
					for (String subFileName : listfile) {
						File subFile = new File(filePath, subFileName);
						if (subFile.isDirectory()) {
							result &= deleteDirectory(subFile.getPath());
						} else {
							result &= subFile.delete();
						}
					}
					result &= file.delete();
					Log.i("deleteDirectory", filePath);
				} catch (Exception e) {
					e.printStackTrace();
					result = false;
				}
			} else {
				result = deleteFile(filePath);
			}
		}

		return result;
	}

	/**
	 * 删除单个文件
	 * 
	 * @param filePath
	 *            文件绝对路径
	 * @return
	 */
	public static boolean deleteFile(String filePath) {
		if (!TextUtils.isEmpty(filePath)) {
			File newPath = new File(filePath);
			if (newPath.isFile()) {
				Log.i("deleteFile", filePath);
				return newPath.delete();
			}
		}
		return false;
	}

	/**
	 * 创建副本文件
	 * @param file	
	 * @param i
	 * @return
	 */
	public static File createCopyFile(File file, int i) {
		if (file.exists()) {
			String fileName = file.getName();
			int lastIndex = fileName.lastIndexOf(".");
			// 带扩展名的副本名
			if (lastIndex != -1) {
				String namePrefix = fileName.substring(0, lastIndex);
				String nameSuffix = fileName.substring(lastIndex);
				fileName = String.format("%s(%s)%s", namePrefix, i++, nameSuffix);
			} else {
				fileName = String.format("%s(%s)", fileName, i++);
			}
			File tmp = new File(file.getParent(), fileName);
			if(tmp.exists()) {
				return createCopyFile(file, i);
			}else {
				return tmp;
			}
		}
		return file;
	}

	public static String strSplit(String text){
		int index = 1;
		String [] str = text.split("[0-9]");
		StringBuffer sb = new StringBuffer();
		for(String s:str){
			if(!s.equals("")){
				sb.append("\t").append(index++).append(s).append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 *	获取apk文件应用名称
	 * @param ctx
	 * @param path
	 * @return
	 */
	public static String getApkName(Context ctx, String path) {
		String apkName = "";
		PackageManager pm = ctx.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
		if (info != null) {
			apkName = pm.getApplicationLabel(info.applicationInfo).toString();;
		}
		
		return apkName;
	}
	
	public static Drawable getApkIcon(Context ctx, String path) {
		Drawable icon = null;
		PackageManager pm = ctx.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
		if (info != null) {
			info.applicationInfo.publicSourceDir = path;
			icon = pm.getApplicationIcon(info.applicationInfo);
		}
		
		return icon;
	}
}