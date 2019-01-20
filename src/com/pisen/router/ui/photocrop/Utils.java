package com.pisen.router.ui.photocrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;

import com.pisen.router.CloudApplication;

/**
 * @author albin
 * @date 24/6/15
 */
public class Utils {

    public static Uri getImageUri(String path) {
        return Uri.fromFile(new File(path));
    }
    
    public static Uri getImageSaveUri() {
        return Uri.fromFile(new File(CloudApplication.HEAD_PATH, String.format("%s.png", getHeadFileName())));
    }

	private static long getHeadFileName() {
		SimpleDateFormat sf = new SimpleDateFormat("ddHHmmsss");
		return Long.parseLong(sf.format(new Date(System.currentTimeMillis())));
	}
}
