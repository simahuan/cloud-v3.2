package com.pisen.router.core.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.entity.InputStreamEntity;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.MediaColumns;
import android.studio.database.sqlite.CursorWapper;
import android.studio.database.sqlite.ICursor;
import android.studio.os.LogCat;

import com.pisen.router.common.utils.FileUtils;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo.RSource;

import de.aflx.sardine.impl.handler.VoidResponseHandler;

/**
 * 本地资源操作管理类
 * 
 * @author Liuhc
 * @version 1.0 2015年4月14日 下午3:13:52
 */
public class LocalResourceManager extends ResourceManager {

	private static final String[] projection = { MediaColumns._ID, //
			MediaColumns.DATA, //
			MediaColumns.DISPLAY_NAME, //
			MediaColumns.SIZE, //
			MediaColumns.DATE_MODIFIED, //
			MediaColumns.MIME_TYPE //
	};

	private Context context;

	public LocalResourceManager(Context context) {
		this.context = context;
	}

	@Override
	public List<ResourceInfo> list(String dirPath) {
		List<ResourceInfo> results = new ArrayList<ResourceInfo>();
		File f = new File(dirPath);
		File[] files = f.listFiles();
		if (files != null) {
			for (File file : files) {
				if (isTempFile(file)) {
					continue;
				}

				ResourceInfo info = toResourceInfo(file);
				results.add(info);
			}
		}
		return results;
	}

	@Override
	protected List<ResourceInfo> listFileChooser(String dir, boolean dirOnly) {
		List<ResourceInfo> results = new ArrayList<ResourceInfo>();
		File f = new File(dir);
		File[] files = f.listFiles();
		if (files != null) {
			for (File file : files) {
				if (isTempFile(file)) {
					continue;
				}

				if (dirOnly) {
					if (file.isDirectory()) {
						ResourceInfo info = toResourceInfo(file);
						results.add(info);
					}
				} else {
					ResourceInfo info = toResourceInfo(file);
					results.add(info);
				}
			}
		}
		return results;
	}

	private static boolean isTempFile(File file) {
		return file.getName().startsWith(".") || file.getName().endsWith(".~tmp");
	}

	private static ResourceInfo toResourceInfo(File file) {
		ResourceInfo info = new ResourceInfo(RSource.Local);
		info.isDirectory = file.isDirectory();
		info.path = file.getAbsolutePath();
		info.name = file.getName();
		info.size = file.length(); //.getTotalSpace();
		info.lastModified = file.lastModified();
		return info;
	}

	@Override
	public List<ResourceInfo> listRecursively(String dirPath, FileType type) {
		switch (type) {
		case All:
			return list(dirPath);
		case Audio:
			return listAudio(dirPath);
		case Video:
			return listFile(context, VideoThumbprojection, Videoprojection, videoUri);
		case Image:
			return listFile(context, ImagesThumbprojection, Imageprojection, imageUri);
		case Document:
			return listDocument(dirPath);
		case Apk:
			return listInstall(dirPath);
		case Compress:
			return listCompress(dirPath);
		default:
			return Collections.emptyList();
		}
	}

	protected List<ResourceInfo> listAudio(String dirPath) {
		List<ResourceInfo> data = filterFile(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null);
		return filterData(data, new String[] { "mp3", "MP3", "m4a", "M4A" });
	}

	/**
	 * 过滤音乐数据(by ldj)
	 * 
	 * @param data
	 * @param validePostfix
	 * @return
	 */
	private List<ResourceInfo> filterData(List<ResourceInfo> data, String[] suffixes) {
		if (data != null && !data.isEmpty()) {
			if (suffixes != null && suffixes.length > 0) {
				List<ResourceInfo> validData = new ArrayList<ResourceInfo>();
				int size = data.size();
				ResourceInfo info = null;
				for (int i = 0; i < size; i++) {
					info = data.get(i);
					for (String v : suffixes) {
						if (info.name.endsWith(v)) {
							validData.add(info);
							break;
						}
					}
				}

				return validData;
			}
		}
		return data;
	}

	protected List<ResourceInfo> listVideo(String dirPath) {
		return filterFile(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null);
	}

	protected List<ResourceInfo> listImage(String dirPath) {
		return filterFile(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null);
	}

	protected List<ResourceInfo> listDocument(String dirPath) {
		String[] DOC_MIME_TYPE = { "text/plain", "application/pdf", "application/msword", "application/vnd.ms-excel" };
		Iterator<String> it = Arrays.asList(DOC_MIME_TYPE).iterator();
		StringBuilder selection = new StringBuilder("(");
		while (it.hasNext()) {
			selection.append(FileColumns.MIME_TYPE + "=='" + it.next() + "'");
			if (it.hasNext()) {
				selection.append(" OR ");
			}
		}
		selection.append(")");

		return filterFile(context, MediaStore.Files.getContentUri("external"), projection, selection.toString(), null);
	}

	protected List<ResourceInfo> listInstall(String dirPath) {
		String selection = FileColumns.DATA + " LIKE '%.apk'";
		return filterFile(context, MediaStore.Files.getContentUri("external"), projection, selection, null);
	}

	protected List<ResourceInfo> listCompress(String dirPath) {
		String selection = FileColumns.MIME_TYPE + " == 'application/zip'";
		return filterFile(context, MediaStore.Files.getContentUri("external"), projection, selection, null);
	}

	private List<ResourceInfo> filterFile(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs) {
		String filterSd = "(" + MediaColumns.DATA + " like '/mnt/%' or " + MediaColumns.DATA + " like '/storage/%')";
		if (selection != null) {
			selection = filterSd + " and " + selection;
		}

		List<ResourceInfo> results = new ArrayList<ResourceInfo>();
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, MediaColumns.DATE_ADDED + " desc");
		if (cursor != null) {
			ICursor cw = new CursorWapper(cursor);
			while (cw.moveToNext()) {
				ResourceInfo info = new ResourceInfo();
				info.isDirectory = false;
				info.path = cw.getString(MediaColumns.DATA);
				info.name = new File(info.path).getName(); // cw.getString(MediaColumns.DISPLAY_NAME);
				info.size = cw.getInt(MediaColumns.SIZE);
				info.lastModified = cw.getInt(MediaColumns.DATE_MODIFIED);
				info.mimeType = cw.getString(MediaColumns.MIME_TYPE);
				//TODO 过滤掉不存在的文件
				File tmp = new File(info.path);
				if(tmp.length() >0) {
					results.add(info);
				}
			}
			cw.close();
		}
		return results;
	}

	@Override
	public boolean exists(String path) throws Exception {
		return FileUtils.exists(path);
	}

	@Override
	public void createDir(String path) throws Exception {
		FileUtils.createDirectory(path);
	}

	@Override
	public void copy(String sourcePath, String targetPath) throws Exception {
		FileUtils.copy(sourcePath, targetPath);
	}

	@Override
	public void move(String sourcePath, String targetPath) throws Exception {
		FileUtils.move(sourcePath, targetPath);
	}

	@Override
	public void delete(String path) throws Exception {
		FileUtils.deleteDirectory(path);
	}

	@Override
	public void rename(String sourcePath, String newName) throws Exception {
		try {
			File oldFile = new File(sourcePath);
			if (oldFile != null && oldFile.isFile()) {
				oldFile.renameTo(new File(oldFile.getParent(), newName));
			} else {
				throw new ResourceException(0, "本地资源重命名异常");
			}
		} catch (Exception e) {
			throw new ResourceException(0, "本地资源重命名异常");
		}
	}

	@Override
	public InputStream get(String path) throws Exception {
		return new FileInputStream(path);
	}

	@Override
	public void put(String path, InputStream inStream) throws Exception {
		FileOutputStream fos = null;
		File f = new File(path);
		if (!f.exists()) {
			f.createNewFile();
		}
		fos = new FileOutputStream(f);
		int hasRead = 0;
		byte[] buf = new byte[4096];
		try {
			while ((hasRead = inStream.read(buf)) > 0) {
				fos.write(buf, 0, hasRead);
			}
		} catch (Exception e) {

		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	@Override
	public void put(String path, InputStreamEntity inStream) throws Exception {
		put(path, inStream.getContent());
	}

	@Override
	public void put(String path, InputStreamEntity inStream,VoidResponseHandler responseHandler) throws Exception{
	}

	/****************************************************************************************************************************/

	String[] Videoprojection = new String[] { MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_MODIFIED, MediaStore.Video.Media.DISPLAY_NAME,
			MediaStore.Video.Media.SIZE, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media._ID };
	String[] VideoThumbprojection = new String[] {

	MediaStore.Video.Thumbnails.VIDEO_ID, MediaStore.Video.Thumbnails.DATA, };

	String[] ImagesThumbprojection = new String[] { MediaStore.Images.Thumbnails.IMAGE_ID, MediaStore.Images.Thumbnails.DATA,

	};
	Uri[] videoUri = new Uri[] { MediaStore.Video.Media.INTERNAL_CONTENT_URI, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
			MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI };

	Uri[] imageUri = new Uri[] { MediaStore.Images.Media.INTERNAL_CONTENT_URI, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI };

	String[] Imageprojection = new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.DISPLAY_NAME,
			MediaStore.Images.Media.SIZE, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID };

	/**
	 * 扫描图片、视频文件
	 * 
	 * @return
	 */
	public List<ResourceInfo> listFile(Context context, String[] thumb, String[] projection, Uri[] uriAddress) {
		ArrayList<ResourceInfo> listResult = new ArrayList<ResourceInfo>();
		Uri uri = uriAddress[1];
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, " date_added desc");

		if (cursor != null) {
			while (cursor.moveToNext()) {
				ResourceInfo res = new ResourceInfo();
				res.isDirectory = false;
				res.path = cursor.getString(cursor.getColumnIndex(projection[0]));
				res.lastModified = (long) cursor.getLong(cursor.getColumnIndex(projection[1]));
				res.name = cursor.getString(cursor.getColumnIndex(projection[2]));
				res.size = cursor.getLong(cursor.getColumnIndex(projection[3]));
				res.createTime = (long) cursor.getLong(cursor.getColumnIndex(projection[5]));
				res.source = RSource.Local;
				File tmp = new File(res.path);
				if(tmp.length() >0) {// XXX 屏蔽媒体库未及时刷新
					listResult.add(res);
				} else {
					LogCat.d("media file is not exist->%s", res.path);
				}
			}
			cursor.close();
		}
		return listResult;
	}
}
