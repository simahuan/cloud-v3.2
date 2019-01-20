package com.pisen.router.ui.phone.account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.text.TextUtils;
import android.util.Base64;

import com.pisen.router.config.HttpKeys;

//import com.loopj.android.http.Base64;

public class HmacEncryptUtils {
	private static final String MAC_NAME = "HmacSHA1";
	private static final String ENCODING = "UTF-8";

	/*
	 * 展示了一个生成指定算法密钥的过程 初始化HMAC密钥
	 * 
	 * @return
	 * 
	 * @throws Exception
	 * 
	 * public static String initMacKey() throws Exception { //得到一个 指定算法密钥的密钥生成器
	 * KeyGenerator KeyGenerator keyGenerator
	 * =KeyGenerator.getInstance(MAC_NAME); //生成一个密钥 SecretKey secretKey
	 * =keyGenerator.generateKey(); return null; }
	 */

	/**
	 * 按键排序
	 * 
	 * @param oriMap
	 * @return
	 */
	public static TreeMap<String, String> sortMapByKey(Map<String, String> oriMap) {
		if (oriMap == null || oriMap.isEmpty()) {
			return null;
		}
		List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(oriMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
			// 升序排序
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}

		});

		TreeMap<String, String> sortedMap = new TreeMap<String, String>();
		for (int i = 0; i < list.size(); i++) {
			String key = list.get(i).getKey();
			String value = list.get(i).getValue();
			sortedMap.put(key, value);
		}
		return sortedMap;
	}

	/**
	 * 过滤 空白串是指由空格、制表符、回车符、换行符组成的字符串
	 * 
	 * @param input
	 * @return String
	 */
	public static String filterObj(String input) {
		if (input.contains("\\t")) {
			input = input.replaceAll("\\t", "");
		}
		if (input.contains("\\r")) {
			input = input.replaceAll("\\r", "");
		}
		if (input.contains("\n")) {
			input = input.replaceAll("\n", "");
		}
		return input;
		// return input.replaceAll("[\\t\\n\\r]", "");
	}

	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	public static String encryptByHmacSha1(Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		if (null != map && map.entrySet().size() > 0) {
			for (Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, String> entity = (Entry<String, String>) iterator.next();
				if (!TextUtils.isEmpty(entity.getKey()) && !TextUtils.isEmpty(entity.getValue()) && /*iterator.hasNext()*/!"Sign".equals(entity.getKey())) {
					sb.append(entity.getKey()).append("=").append(entity.getValue()).append("&");
				}
			}
			String result = sb.toString();
			result = result.substring(0, result.length() - 1);
			result = replaceBlank(encryptByHmacSha1(result));
			return result;
		}
		return "";
	}

	public static String encryptByHmacSha1(String encryptText) {
		try {
			return Base64.encodeToString(encryptByHmacSha1(encryptText, HttpKeys.AppSecret), Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
	 * 
	 * @param encryptText
	 *            被签名的字符串
	 * @param encryptKey
	 *            密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByHmacSha1(String encryptText, String encryptKey) throws Exception {
		byte[] data = encryptKey.getBytes(ENCODING);
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
		// 生成一个指定 Mac 算法 的 Mac 对象
		Mac mac = Mac.getInstance(MAC_NAME);
		// 用给定密钥初始化 Mac 对象
		mac.init(secretKey);
		byte[] text = encryptText.getBytes(ENCODING);
		// 完成 Mac 操作
		return mac.doFinal(text);
	}
}
