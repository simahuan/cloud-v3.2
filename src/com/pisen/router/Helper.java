package com.pisen.router;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;

import com.pisen.router.core.filemanager.ResourceInfo;
public class Helper {

    private static final String TAG = Helper.class.getCanonicalName();
    /**
     * KB换算
     */
    public static final int KB = 1 * 1024;
    /**
     * MB换算
     */
    public static final int MB = 1 * 1024 * 1024;
    /**
     * GB换算
     */
    public static final int GB = 1 * 1024 * 1024 * 1024;
    /**
     * 判断SD卡是否存在
     * @return 存在返回true
     */
    public static boolean isSdcardExist(){
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static boolean isHiden(String folder) {
        return false;
    }

    /**
     * 截取文件路径的最后文件名. 根目录返回 根目录 /, 如果文件名最后是/, 返回空字符串. / --> / /path --> /path
     * /path/1 --> 1 /path/1/ --> ""
     */
    public static String getFolderNameOfPath(String path) {
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        int index = path.lastIndexOf('/');
        if (index == -1 || index == 0)
            return path;
        return path.substring(index + 1);
    }

    /**
     * 获取文件的上级目录名称 ，根目录返回null . /mnt/sdcard -->/mnt /mnt/sdcard/1.txt
     * -->/mnt/sdcard /-->null
     *
     */
    public static String getParentNameofPath(String path) {
        if (path.equals("/")) {
            return null;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int index = path.lastIndexOf('/');
        if (path.length() == 1) {
            return path;
        }
        if (index == 0) {
            return "/";
        }
        return path.substring(0, index + 1);
    }

    public static void reverseList(List<?> list) {

    }

    /**
     * 将文件名后面,后缀前面加Str.
     */
    public static String getNameAppendStr(String path, String Str) {
        int i = path.lastIndexOf(".");
        if (i == -1 || i == 0)
            return path + Str;
        return path.substring(0, i) + Str + path.substring(i, path.length());
    }

    public static String newFolder(String currFolder, String newFolder) {
        if (!currFolder.endsWith("/")) {
            currFolder += "/";
        }
        return currFolder + newFolder;
    }

    /**
     * 验证文件名
     *
     * @param str
     * @return
     */
    public static boolean validateFileName(String str) {
        if (str.trim().length() == 0) {
            return false;
        }
        String strPattern = "[^/\\:*?\"<>|]+";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static Integer[] ints = new Integer[] { 0, 0 };

    /**
     * 递归获取文件夹里所有文件的总大小 BUG: 不能分辨链接文件
     */
    public static long getDirectorySize(File f) throws IOException {
        long size = 0;
        File flist[] = f.listFiles();
        if (flist == null)
            return f.length();
        int length = flist.length;
        for (int i = 0; i < length; i++) {
            if (flist[i].isDirectory()) {
                Log.i(TAG, "AbsolutePath=======>" + flist[i].getAbsolutePath());
                size = size + getDirectorySize(flist[i]);
                ints[0]++;
            } else {
                size = size + flist[i].length();
                ints[1]++;
            }
        }
        return size;
    }

    /**
     * BUG: 不能分辨链接文件
     */
    public static long getDirectorySize(String fp) throws IOException {
        long size = 0;
        File f = new File(fp);
        File flist[] = f.listFiles();
        if (flist == null)
            return f.length();
        int length = flist.length;
        for (int i = 0; i < length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getDirectorySize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    /**
     * 格式化文件的大小描述
     *
     * @param size
     *            文件大小
     * @return
     */
    public static String formatFromSize(double size) {

        if (size < 0)
            return "未知大小";
        String suffix = " B";
        if (size >= 1024) {
            suffix = " KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = " MB";
                size /= 1024;
            }
        }
        DecimalFormat df = new DecimalFormat("0.##");
        String st = df.format(size);
        return st + suffix;
    }

    /**
     * 将大小专程mb
     *
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
            return "0.0M";
        } else {
            size = (size / 1024) / 1024;
            DecimalFormat df = new DecimalFormat("0.0");
            return df.format(size) + "M";
        }
    }

    /**
     * 读取配置文件
     */
    public String[] loadProperties() {
        String[] limitFolders;
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("defaultConfig.properties"));
            String limitFolderStr = properties.getProperty("limitFolder");

            if (limitFolderStr.indexOf("|") > 0) {
                limitFolders = limitFolderStr.split("|");
            } else {
                limitFolders = new String[] { limitFolderStr };
            }
            return limitFolders;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用来得到没有安装上的应用程序的icon
     *
     * @param context
     * @param apkPath
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Drawable showUninstallAPKIcon(Context context, String apkPath) {
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            // apk包的文件路径
            // 这是一个Package 解释器, 是隐藏的
            // 构造函数的参数只有一个, apk文件的路径
            // PackageParser packageParser = new PackageParser(apkPath);
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
            Class<?>[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            Object pkgParser = pkgParserCt.newInstance(valueArgs);
            // Log.d("ANDROID_LAB", "pkgParser:" + pkgParser.toString());
            // 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            // PackageParser.Package mPkgInfo = packageParser.parsePackage(new
            // File(apkPath), apkPath,
            // metrics, 0);
            typeArgs = new Class[4];
            typeArgs[0] = File.class;
            typeArgs[1] = String.class;
            typeArgs[2] = DisplayMetrics.class;
            typeArgs[3] = Integer.TYPE;
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
            valueArgs = new Object[4];
            valueArgs[0] = new File(apkPath);
            valueArgs[1] = apkPath;
            valueArgs[2] = metrics;
            valueArgs[3] = 0;
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
            // 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
            // ApplicationInfo info = mPkgInfo.applicationInfo;
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
            // uid 输出为"-1"，原因是未安装，系统未分配其Uid。
            // Log.d("ANDROID_LAB", "pkg:" + info.packageName + " uid=" +
            // info.uid);
            // Resources pRes = getResources();
            // AssetManager assmgr = new AssetManager();
            // assmgr.addAssetPath(apkPath);
            // Resources res = new Resources(assmgr, pRes.getDisplayMetrics(),
            // pRes.getConfiguration());
            Class<?> assetMagCls = Class.forName(PATH_AssetManager);
            Constructor<?> assetMagCt = assetMagCls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = context.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = resCt.newInstance(valueArgs);
            // CharSequence label = null;
            // if (info.labelRes != 0) {
            // label = res.getText(info.labelRes);
            // }
            // if (label == null) {
            // label = (info.nonLocalizedLabel != null) ? info.nonLocalizedLabel
            // : info.packageName;
            // }
            // Log.d("ANDROID_LAB", "label=" + label);
            // 这里就是读取一个apk程序的图标
            if (info.icon != 0) {
                Drawable icon = res.getDrawable(info.icon);
                return icon;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过路径获得名称
     *
     * @param filepath
     *            filepath
     * @return String
     */
    public static String getNameFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(pos + 1);
        }
        return "";
    }

    /**
     *
     * @Description: SD卡信息
     * @author MouJunFeng
     * @date 2014 下午3:39:30
     * @version V1.0
     */
    public static class SDCardInfo {
        public long total;

        public long free;
    }

    /**
     * SD卡信息
     *
     * @return
     */
    public static SDCardInfo getSDCardInfo() {
        String sDcString = android.os.Environment.getExternalStorageState();
        if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
            File pathFile = android.os.Environment.getExternalStorageDirectory();

            try {
                android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());

                long nTotalBlocks = statfs.getBlockCount();


                long nBlocSize = statfs.getBlockSize();


                long nAvailaBlock = statfs.getAvailableBlocks();


                long nFreeBlock = statfs.getFreeBlocks();

                SDCardInfo info = new SDCardInfo();
                info.total = nTotalBlocks * nBlocSize;

                info.free = nAvailaBlock * nBlocSize;

                return info;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.toString());
            }
        }

        return null;
    }

    /**
     * Long转换String
     *
     * @param size
     * @return
     */
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    /**
     * 文件大小转换
     *
     * @param size
     * @return
     */
    public static long convertStorage(String size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        String sizetemp = size.toLowerCase();
        String sizesub = "";

        if (sizetemp.toLowerCase().contains("kb")) {
            sizesub = sizetemp.substring(0, sizetemp.indexOf("kb"));
            return (long) (Float.valueOf(sizesub) * kb);
        } else if (sizetemp.toLowerCase().contains("mb")) {
            sizesub = sizetemp.substring(0, sizetemp.indexOf("mb"));
            return (long) (Float.valueOf(sizesub) * mb);
        } else if (sizetemp.toLowerCase().contains("gb")) {
            sizesub = sizetemp.substring(0, sizetemp.indexOf("gb"));
            return (long) (Float.valueOf(sizesub) * gb);
        } else if (sizetemp.toLowerCase().contains("b")) {
            sizesub = sizetemp.substring(0, sizetemp.indexOf("b"));
            return (long) (Float.valueOf(sizesub) * 1);
        }
        if (sizetemp.toLowerCase().contains("k")) {
            sizesub = sizetemp.substring(0, sizetemp.indexOf("k"));
            return (long) (Float.valueOf(sizesub) * kb);
        } else if (sizetemp.toLowerCase().contains("m")) {
            sizesub = sizetemp.substring(0, sizetemp.indexOf("m"));
            return (long) (Float.valueOf(sizesub) * mb);
        } else if (sizetemp.toLowerCase().contains("g")) {
            sizesub = sizetemp.substring(0, sizetemp.indexOf("g"));
            return (long) (Float.valueOf(sizesub) * gb);
        } else {
            return 0;
        }
    }

    /**
     * URL编码
     *
     * @param str
     * @return
     */
    public static String getURLEncode(String str) {
        try {
            String fileStr = URLEncoder.encode(str.trim(), "utf-8").replaceAll("\\+", "%20");
            fileStr = fileStr.replaceAll("%3A", ":").replaceAll("%2F", "/");
            return fileStr;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return "";
    }

    /**
     * 版本号类型转化
     *
     * @param fileItem
     * @return
     */
    public static int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 修改web上文件名字
     *
     * @param fileItem
     *            FileItem
     * @param newName
     *            newName
     * @return
     */
	/*public static String[] reNameWebdav(ResourceInfo fileItem, String newName) {

		String tmpFile = fileItem.path;
		String prePath = getParentNameofPath(tmpFile);
		if (prePath != null) {
			if (!prePath.endsWith("/"))
				prePath += "/";
			if (newName.lastIndexOf(".") > 0)
				return new String[] { prePath + newName, newName };
			else {
				String extraName = fileItem.extra;
				if (extraName.toUpperCase().equals("FOLDER"))
					return new String[] { prePath + newName, newName };
				else
					return new String[] { prePath + newName + "." + extraName.toLowerCase(), newName + "." + extraName.toLowerCase() };
			}
		}

		return new String[] {};
	}*/

    /**
     * 格式化IP地址
     *
     * @param fileItem
     * @return
     */
    public static String getIPAddress(Context ctx) {
        WifiManager wifi_service = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        WifiInfo wifiinfo = wifi_service.getConnectionInfo();
        System.out.println("Wifi info----->" + wifiinfo.getIpAddress());
        System.out.println("DHCP info gateway----->" + Formatter.formatIpAddress(dhcpInfo.gateway));
        System.out.println("DHCP info netmask----->" + Formatter.formatIpAddress(dhcpInfo.netmask));
        // DhcpInfo中的ipAddress是一个int型的变量，通过Formatter将其转化为字符串IP地址
        return Formatter.formatIpAddress(dhcpInfo.ipAddress);
    }

    public static long String2long(String time){
        SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
        Date dt2 = null;
        try {
            dt2 = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //继续转换得到秒数的long型
        return dt2.getTime() / 1000;
    }
}
