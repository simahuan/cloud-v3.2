package com.pisen.router.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;

/**
 * 时间操作工具类
 * 
 * @author Liuhc
 * @version 1.0 2014年12月5日 上午10:39:31
 */
/**
 * @author  mahuan
 * @version 1.0 2015年3月9日 上午10:04:08
 * @updated [2015年3月9日 上午10:04:08]:
 */
@SuppressLint("SimpleDateFormat")
public class DateUtils {

	// 默认格式
	public static final String TIME_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";
	// 简单格式
	public static final String TIME_FORMAT_SIMPLE = "yyyy-MM-dd";
	// 缩写格式（天气查询使用）
	public static final String TIME_FORMAT_WEATHER = "yyyyMMddHHmm";

	/**
	 * 获取当前日期时间字符串 (格式为yyyy-MM-dd HH:mm:ss)
	 * 
	 * @return
	 */
	public static String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_DEFAULT);
		return sdf.format(new Date());
	}

	/**
	 * 根据格式获取当前时间
	 * 
	 * @param format
	 * @return
	 */
	public static String getCurrentTime(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());
	}

	/**
	 * 判断是否为夜晚
	 * @return
	 */
	public static boolean isNight() {
		return Calendar.getInstance().get(Calendar.AM_PM) == Calendar.PM ? true:false;
	}

	/**
	 * 根据获取当前日期
	 * 
	 * @return
	 */
	public static long getCurrentLongTime() {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_SIMPLE);
		try {
			return sdf.parse(getCurrentTime()).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date().getTime();
	}

	/**
	 * 时间转换
	 * 
	 * @param pTimeMillis
	 *            精确到秒(如果精确到毫秒需要/1000)
	 * @return
	 */
	public static String long2Time(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_DEFAULT);
		return sdf.format(new Date(time));
	}
	
	
	
	/**
	 * ms转简单日期string
	 * @param time
	 * @return
	 */
	public static String long2DateString(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_SIMPLE);
		return sdf.format(new Date(time));
	}

	/**
	 * 时间转换
	 * 
	 * @param time
	 *            精确到毫秒(如果精确到秒需要*1000)
	 * @return
	 */
	public static Date long2Date(long time) {
		return new Date(time);
	}

	/**
	 * 根据时间戳获取日期
	 * 
	 * @param pTimeMillis
	 * @return
	 */
	public static String getFormatTime(long pTimeMillis, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date(pTimeMillis));
	}

	/**
	 * 时间转换成date
	 * 
	 * @param time
	 * @return
	 */
	public static Date time2Date(String time) {
		SimpleDateFormat df = new SimpleDateFormat(TIME_FORMAT_DEFAULT);
		try {
			return df.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return 返回所有闹钟时间
	 */
	public static long[] getAlarmTimes() {
		long[] times = new long[2];
		times[0] = getAlarmTime("08:30");
		times[1] = getAlarmTime("11:30");
		times[2] = getAlarmTime("17:30");
		return times;
	}

	/**
	 * 时间转换成long
	 * 
	 * @param alarmTime
	 *            格式为24小时时间格式：如09:00，21:00
	 * @return
	 */
	public static long getAlarmTime(String alarmTime) {
		long time = 0;
		try {
			SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
			Date d = fmt.parse(alarmTime);

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(getCurrentLongTime());
			calendar.set(Calendar.HOUR_OF_DAY, d.getHours());
			calendar.set(Calendar.MINUTE, d.getMinutes());
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			time = calendar.getTimeInMillis();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}
	
	/**
	 * 多媒体播放时,转换成统一时间格式
	 * @describtion
	 * @param time
	 * @return
	 */
	public static String generateTime(long time) {
		int totalSeconds = (int) (time / 1000);
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;
		return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d:%02d", 00,minutes, seconds);
	}
	
	
	/**
	 * @describtion 当前系统毫秒时间
	 * @return 返回系统 毫秒时间
	 */
	public static long getCurrentTimeMillis(){
		return System.currentTimeMillis();
	}
	
	/**
	 * 毫秒数转换为“00:00”格式时间
	 * @param millSecond
	 * @return
	 */
	public static String getMusicTime(int millSecond) {
		try {
			int mintue = millSecond / 1000 / 60;
			int second = Math.round((millSecond - mintue * 60000) / 1000f);
			if (second < 10) {
				return "0" + mintue + ":0" + second;
			} else {
				return "0" + mintue + ":" + second;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return "00:00";
		}
	}
	
	/**
	 * 获取形如”上午05:66“格式时间
	 * @param time
	 * @return
	 */
	public static String getAMOrPMTime(long time) {
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return String.format("%s%s", calendar.get(Calendar.AM_PM) == 0 ? "上午" : "下午", String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE)) );
	}
	
	 /**
     * 返回是上午或下午
     *
     * @see Calendar.AM 0
     * @see Calendar.PM 1
     * @return
     */
    public static String getCurrDateAMOrPM(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.AM_PM) == 0 ? "上午" : "下午";
    }
}
