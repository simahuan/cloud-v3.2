package com.pisen.router.core.flashtransfer.scan.protocol;

import java.io.UnsupportedEncodingException;

import android.studio.os.PreferencesUtils;
import android.util.Base64;

import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;

/**
 * (用户信息)
 * 协议格式：Pisen:Lin1:iphone:1001:192.168.1.101:24250
 * @author yangyp
 */
public class UserInfoPtlV2 extends UdpMsgProtocol {

	public String hostName; // 主机名
	public String hostType; // 手机类型
	public String hostIp;
	public int udpPort;
	public long lastModified; // 最后更新时间

//	public UserInfoPtlV2(int cmd) {
//		this(cmd, 0);
//	}
	
	public UserInfoPtlV2(int cmd, int port, String localIpAddr) {
		super(cmd);
		appType = "Pisen";
		this.hostName =  PreferencesUtils.getString(KeyUtils.NICK_NAME, android.os.Build.MODEL) ;
		//添加头像支持
		int headId = -1;
		try {
			headId = PreferencesUtils.getInt(KeyUtils.NICK_HEAD, -1);
		}catch(Exception e) {
		}
		this.hostType = String.format("%s_%s",FlashTransferConfig.PHONE_TYPE_ANDROID, headId);
		this.hostIp = localIpAddr;
		this.udpPort = port;
		this.lastModified = System.currentTimeMillis();
	}


	@Override
	public String getMsg() {
		StringBuffer sb = new StringBuffer(super.getMsg());
		try {
			//sb.append(hostName);
			sb.append(Base64.encodeToString(hostName.getBytes("utf-8"), Base64.NO_WRAP));
			sb.append(FlashTransferConfig.SPILIT_FIX);
			sb.append(hostType);
			sb.append(FlashTransferConfig.SPILIT_FIX);
			sb.append(cmd);
			sb.append(FlashTransferConfig.SPILIT_FIX);
			sb.append(hostIp);
			sb.append(FlashTransferConfig.SPILIT_FIX);
			sb.append(udpPort);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	@Override
	public void read(String extraData) {
		String[] data = extraData.split(FlashTransferConfig.SPILIT_FIX);
		if(data.length >=6) {
			try {
				try {
					hostName = new String(Base64.decode(data[1].getBytes("utf-8"), Base64.NO_WRAP), "utf-8");
				} catch (IllegalArgumentException e) {
					hostName = data[1];
				}
				hostType = data[2];
				cmd = Integer.parseInt(data[3]);
				hostIp = data[4];
				udpPort = Integer.parseInt(data[5]);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

}
