package com.pisen.router.ui.phone.settings;

import java.util.HashMap;
import java.util.LinkedHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

import com.pisen.router.CloudApplication;
import com.pisen.router.R;

/**
 * @author  mahuan
 * @version 1.0 2015年5月28日 上午11:16:46
 */
public class IconResource {
	//默认头像
	private static final int KEY_ICON_DEFAULT = 1001;
	private static LruCache<Integer, Bitmap> cache = new LruCache<Integer, Bitmap>(1* 1024 * 1024) {
		protected int sizeOf(Integer key, Bitmap value) {
			return value.getRowBytes() * value.getHeight();
		};
	};
	
	@SuppressLint("UseSparseArrays")
	static final LinkedHashMap<Integer, Integer> icons = new LinkedHashMap<Integer, Integer>();
	static {
		icons.put(1001, R.drawable.head_1);
		icons.put(1002, R.drawable.head_2);
		icons.put(1003, R.drawable.head_3);
		icons.put(1004, R.drawable.head_4);
		icons.put(1005, R.drawable.head_5);
		icons.put(1006, R.drawable.head_6);
		icons.put(1007, R.drawable.head_7);
		icons.put(1008, R.drawable.head_8);
	}
	
	/**
	 * @return the icons
	 */
	public static HashMap<Integer, Integer> getAllIcons() {
		return icons;
	}
	
	public static boolean isOriginalIcon(int key) {
		return key == -1 || (key >=1001 && key <= 1008); 
	}
	
	@Deprecated
	public static Integer getIcon(int key) {
		return icons.get(key) == null? icons.get(KEY_ICON_DEFAULT):icons.get(key);
	}
	
	/**
	 * 支持自定义头像的头像获取方法
	 * @param ctx
	 * @param key	资源id或者自定义头像文件名（包括文件后缀名）
	 * @return
	 */
	public static Bitmap getIconWithCustom(Context ctx, int key) {
		Bitmap bmp = getIconV2(ctx, key);
		
		if(bmp == null) {
			bmp = getDefaultIcon(ctx, key);
		}
		return bmp;
	}
	
	/**
	 * 获取自定义头像
	 * @param ctx
	 * @param key
	 * @param empty	获取不到头像时，是否返回空头像
	 * @return
	 */
	public static Bitmap getIconWithCustom(Context ctx, int key, boolean empty) {
		if(!empty) return getIconWithCustom(ctx, key);
		
		Bitmap bmp = getIconV2(ctx, key);
		
		if(bmp == null) {
			bmp = getEmptyDefaultIcon(ctx, key);
		}
		return bmp;
	}

	private static Bitmap getIconV2(Context ctx, int key) {
		Bitmap bmp = null;
		try {
			if(key == -1 || (key <=1008 && key >= 1001)) {
				bmp = BitmapFactory.decodeResource(ctx.getResources(), getIcon(key));
			}else {
				bmp = getCustomIcon(key);
			}
		} catch (Exception e) {
			bmp = getCustomIcon(key);
		}
		return bmp;
	}
	
	private static Bitmap getDefaultIcon(Context ctx, int key) {
		return BitmapFactory.decodeResource(ctx.getResources(), getIcon(-1));
	}
	
	private static Bitmap getEmptyDefaultIcon(Context ctx, int key) {
		return BitmapFactory.decodeResource(ctx.getResources(), R.drawable.icon_head_empty);
	}

	/**
	 * 获取自定义头像
	 * @param fileName	头像文件名,不带后缀，默认后缀为.png
	 * @return
	 */
	private static Bitmap getCustomIcon(int fileName) {
		Bitmap bmp = cache.get(fileName);
		if(bmp == null || bmp.isRecycled()) {
			try {
				bmp = BitmapFactory.decodeFile(String.format("%s/%s.png", CloudApplication.HEAD_PATH, fileName));
			} catch (Exception e) {
			}
			if(bmp != null) {
				cache.put(fileName, bmp);
			}
		}
		return bmp;
	}
}
