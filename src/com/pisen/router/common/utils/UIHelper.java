/**
 * 
 */
package com.pisen.router.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pisen.router.R;

/**
 * 
 * @author Liuhc
 * @version 1.0 2015年5月15日 下午4:21:16
 */
public class UIHelper {

	/**
	 * 设置→图片
	 * @param ctx
	 * @param id
	 * @param tv
	 */
	public static void setCompoundDrawablesRight(Context ctx,int id, EditText tv) {
		Drawable drawable = ctx.getResources().getDrawable(id);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv.setCompoundDrawables(null, null, drawable, null);
	}
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getVersion(Context ctx) {
	    try {
	    	 PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
	         return info.versionName;
	    } catch (Exception e) {
	    	e.printStackTrace(System.err);
	    }
	    return "";
	}
	
	public static String getCloudVersion(Context ctx){
		return String.format(ctx.getResources().getString(R.string.welcome_version), getVersion(ctx));
	}
	
	private static float density;
	public static int dipToPx(Context paramContext, int paramInt) {
		if (density <= 0.0F)
			density = paramContext.getResources().getDisplayMetrics().density;
		return (int) (paramInt * density + 0.5F);
	}

	public static int pxToDip(Context paramContext, int paramInt) {
		if (density <= 0.0F)
			density = paramContext.getResources().getDisplayMetrics().density;
		return (int) (paramInt / density + 0.5F);
	}
	
	// private static Toast toast = null;

	public static void showToast(Context context, int resId) {
		showToast(context, context.getText(resId));
	}

	public static void showToast(Context context, CharSequence text) {
		makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(Context context, int resId, int duration) {
		makeText(context, context.getText(resId), duration).show();
	}

	public static void showToastLong(Context context, int resId) {
		showToastLong(context, context.getText(resId));
	}

	public static void showToastLong(Context context, CharSequence text) {
		makeText(context, text, Toast.LENGTH_LONG).show();
	}

	public static Toast makeText(Context context, CharSequence text, int duration) {
		View v = LayoutInflater.from(context).inflate( R.layout.ui_transient_notification, (ViewGroup)null);
		TextView tv = (TextView) v.findViewById(R.id.message);
		tv.setText(text);
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 200);
		toast.setView(v);
		toast.setDuration(duration);

		return toast;
	}
	
}
