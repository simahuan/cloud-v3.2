package com.pisen.router.ui.phone.resource;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.content.Intent;

import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.image.ImageViewerActivity;
/**
 * 向Activity发送数据；Activity解析intent数据的帮助类
 * @author fq
 *
 */
public class SendAndReceiveDataHelper {
	public static final String EXTRA_PATH = "path"; //单个路径
	public static final String EXTRA_INDEX = "index"; //索引
	public static final String EXTRA_LIST = "list"; // list
	public static final String EXTRA_LOCAL_FLAG = "localFlag"; //true 本地资源  |false 云端资源
	public static final String EXTRA_GET_DATA_FROM_DIR = "dir"; //true 获取资源同目录下的同类型资源|false 不获取
	public SendAndReceiveDataHelper() {

	}

	/**
	 * 发送数据到activity,传送list<String>与index
	 * @param ctx
	 * @param cls
	 * @param res
	 * @param index
	 */
	static public void startActivity(Context ctx,Class<?> cls,List<String> res,int index){
		Intent intent = new Intent(ctx, cls);
		intent.putExtra(EXTRA_LIST, (Serializable)res);  
		intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_INDEX, index);
		ctx.startActivity(intent);
	}
	/**
	 * 发送数据到activity,只传一个路径
	 * @param ctx
	 * @param cls
	 * @param path
	 * @param localFlag
	 * @param dirFlag
	 */
	static public void startActivity(Context ctx,Class<?> cls,String path,String dirFlag,String localFlag){
		Intent intent = new Intent(ctx, cls);
		intent.putExtra(EXTRA_PATH, path);  
		//intent.putExtra(EXTRA_LOCAL_FLAG, localFlag);
		if(dirFlag!=null){
			intent.putExtra(EXTRA_GET_DATA_FROM_DIR, dirFlag);
		}
		if(localFlag!=null){
			intent.putExtra(EXTRA_LOCAL_FLAG, localFlag);
		}
		ctx.startActivity(intent);
	}
	/**
	 * 发送数据到activity,只传一个ResourceInfo
	 * @param ctx
	 * @param cls
	 * @param ri
	 * @param dirFlag
	 * @param localFlag
	 */
	static public void startActivityUseResourceInfo(Context ctx,Class<?> cls,ResourceInfo ri,String dirFlag,String localFlag){
		Intent intent = new Intent(ctx, cls);
		intent.putExtra(EXTRA_PATH, ri);
		//intent.putExtra(EXTRA_LOCAL_FLAG, localFlag);
		if(dirFlag!=null){
			intent.putExtra(EXTRA_GET_DATA_FROM_DIR, dirFlag);
		}
		if(localFlag!=null){
			intent.putExtra(EXTRA_LOCAL_FLAG, localFlag);
		}
		ctx.startActivity(intent);
	}
	/**
	 * 接收intent中传送过来的数据，组装成list<String>,index
	 * @param intent 接收数据的intent
	 * @param list   处理后的结果放到list中
	 * @param fileType 资源的类型，用于获取同目录下的相同资源
	 * @return  list 索引  
	 */
	static public int getIntentData(Intent intent,List<String> list,FileType fileType){
		String path = intent.getStringExtra(EXTRA_PATH);
		String dirFlag;
		String localFlag;
		int idx;
		if(null==path||path.isEmpty()){ //传递了list与index
			list.clear();
			list.addAll((List<String>) intent.getSerializableExtra(EXTRA_LIST));
			idx = intent.getIntExtra(EXTRA_INDEX, 0);
			return idx;
			
		}else{ // 只传递了一个路径
			dirFlag = intent.getStringExtra(EXTRA_GET_DATA_FROM_DIR);
			if(null==dirFlag||dirFlag.isEmpty()){//不获取相同路径下的资源
				list.clear();
				list.add(path);
				return 0;
			}else{//获取相同路径下同类型的资源
				
			}
		}
		return 0;
	}
	
	
	/**
	 * 接收intent中传送过来的数据，组装成list<ResourceInfo>,index
	 * @param intent
	 * @param list
	 * @param fileType
	 * @return
	 */
	static public int getIntentDataUseResourceInfo(Intent intent,List<ResourceInfo> list,FileType fileType){
		ResourceInfo ri = (ResourceInfo) intent.getSerializableExtra(EXTRA_PATH);
		String dirFlag;
		String localFlag;
		int idx;
		if(null==ri){ //传递了list与index
			list.clear();
			list.addAll((List<ResourceInfo>) intent.getSerializableExtra(EXTRA_LIST));
			idx = intent.getIntExtra(EXTRA_INDEX, 0);
			return idx;
			
		}else{ // 只传递了一个路径
			dirFlag = intent.getStringExtra(EXTRA_GET_DATA_FROM_DIR);
			if(null==dirFlag||dirFlag.isEmpty()){//不获取相同路径下的资源
				list.clear();
				list.add(ri);
				return 0;
			}else{//获取相同路径下同类型的资源
				
			}
		}
		return 0;
	}
	
	/**
	 * 获取相同目录下的图片
	 * 
	 */
	static public List<String> getSameDirectoryImages(){
		return null;
	} 
	
	/**
	 * 获取相同目录下的视频
	 * 
	 */
	static public List<String> getSameDirectoryVideos(){
		return null;
	} 
	
	/**
	 * 获取相同目录下的音乐
	 * 
	 */
	static public List<String> getSameDirectoryMusics(){
		return null;
	} 
}
