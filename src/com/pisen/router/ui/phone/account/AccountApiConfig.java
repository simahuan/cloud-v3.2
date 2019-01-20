package com.pisen.router.ui.phone.account;

import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.pisen.router.config.HttpKeys;

public class AccountApiConfig {

	public static final String KEY_ACCOUNT = "account_info";

	/** 获取公共请求键值序列　 */
	private static TreeMap<String, String> getCommonMap(String body, String method) {
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("AppKey", HttpKeys.AppKey);
		map.put("Body", body);
		map.put("Format", "json");
		map.put("Method", method);
		map.put("SessionKey", "");
		map.put("Version", "");
		// 排序
		map = HmacEncryptUtils.sortMapByKey(map);
		map.put("Sign", HmacEncryptUtils.encryptByHmacSha1(map));
		return map;
	}

	/** 获取图片验证码请求键值　 */
	public static TreeMap<String, String> getIconCodeMap() {
		return getCommonMap("{}", "Pisen.Service.Share.SSO.Contract.ICustomerService.AppGetVerifyCode");
	}

	/**
	 * 获取手机验证码请求键值　
	 * 
	 * @param phone
	 *            手机号
	 * @param sendType
	 *            0注册,1找回密码
	 * @return
	 */
	public static TreeMap<String, String> getPhoneCodeMap(String phone, int sendType) {
		JSONObject body = new JSONObject();
		try {
			body.put("SendType", String.valueOf(sendType));
			body.put("PhoneNumber", phone);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return getCommonMap(body.toString(), "Pisen.Service.Share.SSO.Contract.ICustomerService.APPSendMsg");
	}

	/**
	 * 获取注册时提交参数键值序列
	 * 
	 * @return
	 */
	public static TreeMap<String, String> getRegisterMap(String phone,String password,String phoneCode) {
		JSONObject body = new JSONObject();
		try {
			body.put("UserName", "");
			body.put("PhoneNumber", phone);
			body.put("PassWord", password);
			body.put("PhoneCode", phoneCode);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return getCommonMap(body.toString(), "Pisen.Service.Share.SSO.Contract.ICustomerService.AppRegister");
	}

	/**
	 * 获取登录参数参数键值序列
	 * 
	 * @return
	 */
	public static TreeMap<String, String> getLoginMap(String phone,String password) {
		return getLoginMap(phone,password,"","",false);
	}
	/**
	 * 获取登录参数参数键值序列
	 * 
	 * @return
	 */
	public static TreeMap<String, String> getLoginMap(String phone,String password,String verifyKey,String verifyCode,boolean needVerify) {
		JSONObject body = new JSONObject();
		try {
			body.put("PhoneNumber", phone);
			body.put("PassWord", password);
			body.put("VerifyKey", verifyKey);
			body.put("VerifyCode", verifyCode);
			body.put("NeedVerify", String.valueOf(needVerify));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return getCommonMap(body.toString(), "SOA.EC.Customer.Contract.ICustomerService.AppLogin");
	}

	/**
	 * 验证手机（找回密码的）验证码
	 * 
	 * @return
	 */
	public static TreeMap<String, String> getFindPwdVerifyMap(String phone,String code) {
		JSONObject body = new JSONObject();
		try {
			body.put("PhoneNumber", phone);
			body.put("PhoneCode", code);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return getCommonMap(body.toString(), "Pisen.Service.Share.SSO.Contract.ICustomerService.AppPhoneCodeVerify");
	}

	/**
	 * 获取重置密码键值序列
	 * 
	 * @return
	 */
	public static TreeMap<String, String> getResetPwdMap(String phone,String newPwd,String oldPwd,boolean isUpdate) {
		JSONObject body = new JSONObject();
		try {
			body.put("PhoneNumber", phone);
			body.put("PassWord", newPwd);
			body.put("OldPassword", oldPwd);
			body.put("IsUpdate", String.valueOf(isUpdate));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return getCommonMap(body.toString(), "Pisen.Service.Share.SSO.Contract.ICustomerService.AppResetPassWord");
	}

	/**
	 * 获取上传头像参数参数键值序列
	 * 
	 * @param body
	 * @return
	 */
	public static TreeMap<String, String> getPostAvatarMap(String body) {
		return getCommonMap(body, "Pisen.Service.Share.SSO.Contract.ICustomerService.AppCustomerUploadImage");
	}
	
	/**
	 * 获取环信账号参数
	 * 
	 * @return
	 */
	public static TreeMap<String, String> getEaseMobAccountJson(String phone) {
		JSONObject body = new JSONObject();
		try {
			body.put("Account", phone);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return getCommonMap(body.toString(), "SOA.PisenCloud.Contract.IEaseMobService.AppGetEaseMobUser");

	}
}
