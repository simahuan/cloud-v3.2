package com.pisen.router.core.filemanager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.studio.util.DateUtils;
import android.text.TextUtils;

import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.AppFileUtils;
import com.pisen.router.core.document.DownloadAndOpenDocActivity;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.image.ImageViewerActivity;
import com.pisen.router.ui.phone.resource.SendAndReceiveDataHelper;
import com.pisen.router.ui.phone.resource.musicplayer.MusicPlaybackActivity;
import com.pisen.router.ui.phone.videoplayer.VideoViewBuffer;

/**
 * 资源字段： 文件基本属性
 * 
 * @author mugabutie
 * 
 */
@SuppressWarnings("serial")
public class ResourceInfo implements Serializable {

	// 文件位置
	public static enum RSource {
		Local, Remote
	}

	public ResourceInfo() {
	}

	public ResourceInfo(RSource source) {
		this.source = source;
	}

	public RSource source; // 远程文件/本地文件
	public boolean isDirectory; // 是否目录
	public String path; // 原始路径
	public String name; // 文件名
	public long size; // 文件大小
	public long lastModified; // 最后修改时间
	public long createTime; // 文件创建时间（ldj）
	public String mimeType;
	public String thumbnailUrl; // 缩略图路径
	public String mediaType;

	public String destPath; // 目标路径

	// public FileSort sort; // 排序方式
	// public String parentPath;// 上级目录

	public String getParent() {
		if (path.lastIndexOf(File.separatorChar) == path.length() - 1) {// 如果路径以“/”结尾,去掉“/”
			path = path.substring(0, path.lastIndexOf(File.separatorChar));
		}
		int index = path.lastIndexOf(File.separatorChar);
		return path.substring(0, index);
	}

	public String getSizeString() {
		return AppFileUtils.formatFileSize(size);
	}

	public String getLastModifiedString() {
		return DateUtils.format(new Date(lastModified), "yyyy-MM-dd HH:mm");
	}

	public String getLastModifiedOfDateTime() {
		return DateUtils.format(new Date(lastModified), "yyyy-MM-dd HH:mm:ss");
	}

	public FileType getFileType() {
		return ResourceCategory.getFileType(name);
	}

	public int getIconResId() {
		return ResourceCategory.getIconResId(name);
	}

	public Uri getPathUri() {
		return source == RSource.Remote ? Uri.parse(path) : Uri.fromFile(new File(path));
	}

	public static void doOpenFile(Context context, ResourceInfo resource) {
		doOpenFile(context, resource, Arrays.asList(resource));
	}

	/**
	 * 打开文件
	 * 
	 * @param fileItem
	 */
	public static void doOpenFile(Context context, ResourceInfo resource, List<ResourceInfo> resourceList) {

		if (!resource.exists()) {
			UIHelper.showToast(context, R.string.file_not_exist);
			return;
		}

		try {
			String path = resource.path;
			Uri uri = resource.source == RSource.Remote ? Uri.parse(path) : Uri.fromFile(new File(path));
			FileType fType = ResourceCategory.getFileType(resource.name);
			switch (fType) {
			case Video:
				resourceList = getFileTypeResourceList(resourceList, fType);
				VideoViewBuffer.start(context, resource, resourceList);
				break;
			case Image:
				resourceList = getFileTypeResourceList(resourceList, fType);
				ImageViewerActivity.start(context, resource, resourceList);
				break;
			case Audio: {
				resourceList = getFileTypeResourceList(resourceList, fType);
				MusicPlaybackActivity.start(context, resource, resourceList);
				break;
			}
			case Document:
			case Apk:
				SendAndReceiveDataHelper.startActivityUseResourceInfo(context, DownloadAndOpenDocActivity.class, resource, null, null);
				break;
			case Compress:
			case Unknown:
			default: {
				Intent intent = ResourceCategory.openFile(resource.name, uri);
				context.startActivity(intent);
				break;
			}
			}
		} catch (Exception e) {
			UIHelper.showToast(context, "无法打开，请安装相应的软件!");
		}
	}

	public static void doOpenFileForResult(Activity context, int requestCode, ResourceInfo resource, List<ResourceInfo> resourceList) {
		if (!resource.exists()) {
			UIHelper.showToast(context, R.string.file_not_exist);
			return;
		}
		try {
			String path = resource.path;
			Uri uri = resource.source == RSource.Remote ? Uri.parse(path) : Uri.fromFile(new File(path));
			FileType fType = ResourceCategory.getFileType(resource.name);
			switch (fType) {
			case Video:
				resourceList = getFileTypeResourceList(resourceList, fType);
				VideoViewBuffer.start(context, resource, resourceList);
				break;
			case Image:
				resourceList = getFileTypeResourceList(resourceList, fType);
				ImageViewerActivity.startForResult(context, requestCode, resource, resourceList);
				break;
			case Audio: {
				resourceList = getFileTypeResourceList(resourceList, fType);
				MusicPlaybackActivity.start(context, resource, resourceList);
				break;
			}
			case Document:
			case Apk:
				SendAndReceiveDataHelper.startActivityUseResourceInfo(context, DownloadAndOpenDocActivity.class, resource, null, null);
				break;
			case Compress:
			case Unknown:
			default: {
				Intent intent = ResourceCategory.openFile(resource.name, uri);
				context.startActivity(intent);
				break;
			}
			}
		} catch (Exception e) {
			UIHelper.showToast(context, "无法打开，请安装相应的软件!");
		}
	}

	private static List<ResourceInfo> getFileTypeResourceList(List<ResourceInfo> resourceList, FileType type) {
		List<ResourceInfo> results = new ArrayList<ResourceInfo>();
		for (ResourceInfo info : resourceList) {
			if (ResourceCategory.isFileType(info.name, type)) {
				results.add(info);
			}
		}

		return results;
	}

	public boolean exists() {
		if (TextUtils.isEmpty(this.path)) {
			return false;
		}
		if (RSource.Remote != this.source) {
			File file = new File(this.path);
			if (null == file || !file.exists()) {
				return false;
			}
			return true;
		}
		return true;
	}

}
