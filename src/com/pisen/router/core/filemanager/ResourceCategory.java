package com.pisen.router.core.filemanager;

import java.util.HashMap;
import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.pisen.router.R;

/**
 * 文件分类
 */
public abstract class ResourceCategory {

	/** 文件类型 */
	static public enum FileType {
		All, // 当前目录下所有目录与文件
		// ALL_Dir, // 当前目录下所有目录
		Audio, Video, Image, Document, Apk, Compress, Unknown;

		public static FileType valueOfEnum(String name) {
			for (FileType type : values()) {
				if (type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
			return All;
		}
	}

	static public class MediaFileType {
		public final FileType fileType;
		public final String mimeType;
		public final int iconResId; // 资源图标

		MediaFileType(FileType fileType, String mimeType, int iconResId) {
			this.fileType = fileType;
			this.mimeType = mimeType;
			this.iconResId = iconResId;
		}
	}

	private static final HashMap<String, MediaFileType> sFileTypeMap = new HashMap<String, MediaFileType>();

	static void addFileType(String extension, FileType fileType, int iconResId, String mimeType) {
		sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType, iconResId));
	}

	static void addFileType(String extension, FileType fileType, int iconResId) {
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
		if (mimeType == null) {
			switch (fileType) {
			case Audio:
				mimeType = "audio/*";
				break;
			case Video:
				mimeType = "video/*";
				break;
			case Image:
				mimeType = "image/*";
				break;
			case Document:
				mimeType = "text/*";
				break;
			case Apk:
				mimeType = "application/vnd.android.package-archive";
				break;
			case Compress:
			case Unknown:
			default:
				mimeType = "*/*";
				break;
			}
		}
		addFileType(extension, fileType, iconResId, mimeType);
	}

	/**
	 * 获取打开文件的Intent
	 * 
	 * @param fileName
	 * @param uri
	 * @return
	 */
	public static Intent openFile(String fileName, Uri uri) {
		FileType fType = getFileType(fileName);
		switch (fType) {
		case Audio:
		case Video:
		case Image:
		case Document:
		case Apk: {
			String miniType = getMimeType(fileName);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(uri, miniType);
			return intent;
		}
		case Compress:
		case Unknown:
		default: {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(uri, "*/*");
			return intent;
		}
		}
	}

	static {
		addFileType("MP3", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("MPGA", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("M4A", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("WAV", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("AMR", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("AWB", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("WMA", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("OGG", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("OGG", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("OGA", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("AAC", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("AAC", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("MKA", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("FLAC", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("MID", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("MIDI", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("XMF", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("RTTTL", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("SMF", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("IMY", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("RTX", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("OTA", FileType.Audio, R.drawable.ic_file_audio);
		addFileType("MXMF", FileType.Audio, R.drawable.ic_file_audio);

		addFileType("MPEG", FileType.Video, R.drawable.ic_file_video);
		addFileType("MPG", FileType.Video, R.drawable.ic_file_video);
		addFileType("MP4", FileType.Video, R.drawable.ic_file_video);
		addFileType("M4V", FileType.Video, R.drawable.ic_file_video);
		addFileType("3GP", FileType.Video, R.drawable.ic_file_video);
		addFileType("3GPP", FileType.Video, R.drawable.ic_file_video);
		addFileType("3G2", FileType.Video, R.drawable.ic_file_video);
		addFileType("3GPP2", FileType.Video, R.drawable.ic_file_video);
		addFileType("MKV", FileType.Video, R.drawable.ic_file_video);
		addFileType("WEBM", FileType.Video, R.drawable.ic_file_video);
		addFileType("TS", FileType.Video, R.drawable.ic_file_video);
		addFileType("AVI", FileType.Video, R.drawable.ic_file_video);
		addFileType("WMV", FileType.Video, R.drawable.ic_file_video);
		addFileType("ASF", FileType.Video, R.drawable.ic_file_video);
		addFileType("FLV", FileType.Video, R.drawable.ic_file_video);
		addFileType("MOV", FileType.Video, R.drawable.ic_file_video);
		addFileType("RMVB", FileType.Video, R.drawable.ic_file_video);
		addFileType("RM", FileType.Video, R.drawable.ic_file_video);
		addFileType("QMV", FileType.Video, R.drawable.ic_file_video);

		addFileType("JPG", FileType.Image, R.drawable.ic_file_image);
		addFileType("JPEG", FileType.Image, R.drawable.ic_file_image);
		addFileType("GIF", FileType.Image, R.drawable.ic_file_image);
		addFileType("PNG", FileType.Image, R.drawable.ic_file_image);
		addFileType("BMP", FileType.Image, R.drawable.ic_file_image);
		addFileType("WBMP", FileType.Image, R.drawable.ic_file_image);
		addFileType("WEBP", FileType.Image, R.drawable.ic_file_image);

		addFileType("TXT", FileType.Document, R.drawable.ic_file_txt);
		addFileType("HTM", FileType.Document, R.drawable.ic_file_htm);
		addFileType("HTML", FileType.Document, R.drawable.ic_file_htm);
		addFileType("XML", FileType.Document, R.drawable.ic_file_htm, "text/*");
		addFileType("PDF", FileType.Document, R.drawable.ic_file_pdf, "text/*");
		addFileType("DOC", FileType.Document, R.drawable.ic_file_doc, "text/*");
		addFileType("DOCX", FileType.Document, R.drawable.ic_file_doc, "text/*");
		addFileType("DOT", FileType.Document, R.drawable.ic_file_doc, "text/*");
		addFileType("DOTX", FileType.Document, R.drawable.ic_file_doc, "text/*");
		addFileType("XLS", FileType.Document, R.drawable.ic_file_xls, "text/*");
		addFileType("XLSX", FileType.Document, R.drawable.ic_file_xls, "text/*");
		addFileType("XLT", FileType.Document, R.drawable.ic_file_xls, "text/*");
		addFileType("XLTX", FileType.Document, R.drawable.ic_file_xls, "text/*");
		addFileType("PPT", FileType.Document, R.drawable.ic_file_ppt, "text/*");
		addFileType("PPTX", FileType.Document, R.drawable.ic_file_ppt, "text/*");
		addFileType("POT", FileType.Document, R.drawable.ic_file_ppt, "text/*");
		addFileType("POTX", FileType.Document, R.drawable.ic_file_ppt, "text/*");
		addFileType("PPS", FileType.Document, R.drawable.ic_file_ppt, "text/*");
		addFileType("PPSX", FileType.Document, R.drawable.ic_file_ppt, "text/*");

		// addFileType("APK", ResourceType.Install, R.drawable.ic_file_apk);
		addFileType("APK", FileType.Apk, R.drawable.file_type_apk);

		// addFileType("Z", ResourceType.Compress, R.drawable.ic_file_zip);
		// addFileType("BZ2", ResourceType.Compress, R.drawable.ic_file_zip);
		// addFileType("GZ", ResourceType.Compress, R.drawable.ic_file_zip);
		// addFileType("TAR", ResourceType.Compress, R.drawable.ic_file_zip);
		// addFileType("TAR.GZ", ResourceType.Compress, R.drawable.ic_file_zip);
		// addFileType("ZIP", ResourceType.Compress, R.drawable.ic_file_zip);
		// addFileType("RAR", ResourceType.Compress, R.drawable.ic_file_zip);
		// addFileType("ISO", ResourceType.Compress, R.drawable.ic_file_zip);
		// addFileType("JAR", ResourceType.Compress, R.drawable.ic_file_zip);
		// addFileType("7Z", ResourceType.Compress, R.drawable.ic_file_zip);
	}

	/**
	 * 权限文件路径获取文件类型
	 * 
	 * @param path
	 * @return
	 */
	public static MediaFileType getMediaType(String fileName) {
		String extensionName = getExtension(fileName);
		MediaFileType type = sFileTypeMap.get(extensionName.toUpperCase(Locale.ROOT));
		return type == null ? new MediaFileType(FileType.Unknown, "*/*", R.drawable.ic_file_unknown) :type;
	}

	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 * 
	 * @param path
	 * @return null or mimeType
	 */
	public static String getMimeType(String fileName) {
		MediaFileType mediaType = getMediaType(fileName);
		return (mediaType == null ? null : mediaType.mimeType);
	}

	/**
	 * 权限文件路径获取文件图标
	 * 
	 * @param path
	 * @return
	 */
	public static int getIconResId(String fileName) {
		MediaFileType mediaType = getMediaType(fileName);
		return mediaType != null ? mediaType.iconResId : R.drawable.ic_file_unknown;
	}

	/**
	 * 权限文件名获取文件类型
	 * 
	 * @param fileName
	 * @return
	 */
	public static FileType getFileType(String fileName) {
		MediaFileType mediaType = getMediaType(fileName);
		return mediaType != null ? mediaType.fileType : FileType.Unknown;
	}

	/**
	 * 判断文件是否为指定类型
	 * 
	 * @param fileType
	 * @param fileName
	 * @return
	 */
	public static boolean isFileType(String fileName, FileType type) {
		return getFileType(fileName) == type;
	}

	/**
	 * 判断文件是否媒体文件
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isMimeTypeMedia(String fileName) {
		return isFileType(fileName, FileType.Audio) || isFileType(fileName, FileType.Video) || isFileType(fileName, FileType.Image);
	}

	/**
	 * 获取文件扩展名(jpg/txt)
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getExtension(String fileName) {
		if (fileName == null) {
			return "";
		}
		int index = fileName != null ? fileName.lastIndexOf(".") : -1;
		return index != -1 ? fileName.substring(index + 1) : "";
	}

}
