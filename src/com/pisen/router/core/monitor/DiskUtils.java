package com.pisen.router.core.monitor;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * 磁盘转换工具类
 * 
 * @author yangyp
 * @version 1.0, 2014年7月24日 上午9:52:25
 */
public abstract class DiskUtils {

	public static final int KB = 1 * 1024;
	public static final int MB = 1 * 1024 * KB;
	public static final int GB = 1 * 1024 * MB;

	/**
	 * 将byte转为相应GB/MB/KB/B
	 * 
	 * @param size
	 *            文件大小
	 * @return
	 */
	public static String getFileSize(double size) {
		String result = null;
		if (size > GB) {
			DecimalFormat df = new DecimalFormat("0.##");
			result = String.format("%sGB", df.format(size / GB));
		} else if (size > MB) {
			DecimalFormat df = new DecimalFormat("0.##");
			result = String.format("%sMB", df.format(size / MB));
		} else if (size > KB) {
			DecimalFormat df = new DecimalFormat("0.##");
			result = String.format("%sKB", df.format(size / KB));
		} else {
			result = String.format("%sB", size);
		}

		return result;
	}

	/**
	 * 将GB/MB/KB/B转化为byte
	 * 
	 * @param total
	 * @return
	 */
	public static long getFileSize(String total) {
		try {
			double result = -1;
			total = total.toUpperCase(Locale.getDefault());
			if (total.endsWith("GB") || total.endsWith("G")) {
				// total.split("[KB|K]")[0])
				String totalSize = total.substring(0, total.lastIndexOf("G"));
				result = Double.parseDouble(totalSize) * GB;
			} else if (total.endsWith("MB") || total.endsWith("M")) {
				String totalSize = total.substring(0, total.lastIndexOf("M"));
				result = Double.parseDouble(totalSize) * MB;
			} else if (total.endsWith("KB") || total.endsWith("K")) {
				String totalSize = total.substring(0, total.lastIndexOf("K"));
				result = Double.parseDouble(totalSize) * KB;
			}

			return Math.round(result);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
