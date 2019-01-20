package com.pisen.router.core.flashtransfer.scan;

import java.io.Serializable;

/**
 * (用户信息)
 * 
 * @author yangyp
 */
public class UserInfo implements Serializable {

	public String hostType; // 手机类型
	public String hostName; // 主机名
	public String ip;
	public long lastModified; // 最后更新时间
}
