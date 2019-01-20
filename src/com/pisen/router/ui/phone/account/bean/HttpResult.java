package com.pisen.router.ui.phone.account.bean;

import com.google.gson.GsonUtils;

public class HttpResult {

	public boolean IsError;
	public boolean IsSuccess;
	public String ErrCode;
	public String ErrMsg;
	public String DetailError;
	public String Message;

	public static HttpResult fromJson(String json) {
		return GsonUtils.jsonDeserializer(json, HttpResult.class);
	}

}
