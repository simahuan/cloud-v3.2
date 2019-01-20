package com.pisen.router.core.flashtransfer.scan.protocol;

import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.core.flashtransfer.scan.UserInfo;

/**
 * android端udp监测协议封装
 * 
 * @author ldj
 * @version 1.0 2015年3月31日 上午11:07:11
 */
public class AndroidTransferIpMessageProtocol extends UdpCmdProtocol {

	public AndroidTransferIpMessageProtocol(String protocolString) {
		super(protocolString);
	}

	public AndroidTransferIpMessageProtocol(int commandNo, String extraData) {
		this(FlashTransferConfig.VERSION_IP_MESSAGE, System.currentTimeMillis(), android.os.Build.MODEL, NetUtil.getLocalIpAddressString(),
				FlashTransferConfig.PHONE_TYPE_ANDROID, commandNo, extraData);
	}

	public AndroidTransferIpMessageProtocol(String version, long packetNo, String senderName, String senderHost, String phoneType, int commandNo,
			String extraData) {
		super(version, packetNo, senderName, senderHost, phoneType, commandNo, extraData);
	}

	/**
	 * 获取userinfo信息
	 * @return
	 */
	public UserInfo getUserInfo() {
		UserInfo info = new UserInfo();
		info.hostName = getSenderName();
		info.hostType = getPhoneType();
		info.ip = getSenderHost();
		info.lastModified = System.currentTimeMillis();

		return info;
	}
}
