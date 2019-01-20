package com.pisen.router.core.document;

import java.text.DecimalFormat;

public class FileCapacityUtil {

	/**
	 * 将大小专程mb
	 * @param size
	 * @return
	 */
	public static String formatSizeToMB(double size) {
		return formatSizeToMB(size, 0);
	}

	/**
	 * 如果缓存值小于忽略值，那么显示为0M，其它正常计算显示
	 * 
	 * @param size
	 *            缓存值
	 * @param ignore
	 *            忽略的值
	 * @return
	 */
	public static String formatSizeToMB(double size, double ignore) {
		if (size < ignore) {
			return "0.0MB";
		} else {
			size = (size / 1024) / 1024;
			DecimalFormat df = new DecimalFormat("0.0");
			return df.format(size) + "MB";
		}
	}
}
