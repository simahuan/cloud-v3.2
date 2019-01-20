package com.pisen.router.common.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * @author  mahuan
 * @version 1.0 2015年9月29日 下午12:00:15
 * @desc{tags}
 */
public class WindowUtils {
	
	public static  void  hideSoftwareWindow(Context context, View view){
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}
