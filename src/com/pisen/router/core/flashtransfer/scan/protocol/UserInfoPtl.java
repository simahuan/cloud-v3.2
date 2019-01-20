package com.pisen.router.core.flashtransfer.scan.protocol;

import android.studio.os.PreferencesUtils;

import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;

/**
 * (用户信息)
 * 
 * @author yangyp
 */
public class UserInfoPtl extends UdpMsgProtocol {

	public String hostName; // 主机名
	public String hostType; // 手机类型
	public String hostIp;
	public long lastModified; // 最后更新时间

	public UserInfoPtl(int cmd) {
		super(cmd);
		this.hostName =  PreferencesUtils.getString(KeyUtils.NICK_NAME, android.os.Build.MODEL) ;
		//添加头像支持
		this.hostType = String.format("%s_%s",FlashTransferConfig.PHONE_TYPE_ANDROID, PreferencesUtils.getInt(KeyUtils.NICK_HEAD, -1));
		this.hostIp = NetUtil.getLocalIpAddressString();
		this.lastModified = System.currentTimeMillis();
	}

	@Override
	public String getMsg() {
		//以-分割会碰到主机名带-时拆分bug，修改为$（ldj）
		StringBuffer sb = new StringBuffer(super.getMsg());
		sb.append(hostName);
		sb.append("$");
		sb.append(hostType);
		sb.append("$");
		sb.append(hostIp);
		sb.append("$");
		sb.append(lastModified);
		return sb.toString();
	}

	@Override
	public void read(String extraData) {
		String[] data = extraData.split("\\$");
		hostName = data[0];
		hostType = data[1];
		hostIp = data[2];
		lastModified = Long.parseLong(data[3]);
	}

}
