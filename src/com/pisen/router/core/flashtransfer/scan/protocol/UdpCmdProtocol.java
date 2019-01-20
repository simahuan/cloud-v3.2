package com.pisen.router.core.flashtransfer.scan.protocol;

import java.security.InvalidParameterException;

/**
 * IPMSG协议抽象类 IPMSG协议格式： Ver(1):
 * PacketNo:SenderName:SenderHost:CommandNo:AdditionalSection
 * 每部分分别对应为：版本号（现在是1）:数据包编号:发送主机:命令:附加数据 其中： 数据包编号，一般是取毫秒数。利用这个数据，可以唯一的区别每个数据包；
 * SenderName指的是发送者的昵称(实际上是计算机登录名) 发送主机，指的是发送主机的主机名；（主机名）
 * 命令，指的是协议中定义的一系列命令，具体见下文；
 * 附加数据，指的是对应不同的具体命令，需要提供的数据。当为上线报文时，附加信息内容是用户名和分组名，中间用"\0"分隔
 * 
 * 例如： 1:100:shirouzu:jupiter:32:Hello 表示 shirouzu用户发送了 Hello
 * 这条消息（32对应为IPMSG_SEND_MSG这个命令，具体需要看源码中的宏定义）。
 * 
 * @author
 */
public class UdpCmdProtocol {
	private String version; // 协议版本（保留，后期可考虑不同版本协议兼容）
	private long packetNo; // 包号
	private String senderName; // 发送者昵称
	private String senderHost; // 发送主机名
	private String phoneType; // 手机类型(拓展字段)
	private int commandNo; // 命令
	private String extraData; // 附加数据

	public UdpCmdProtocol(String protocolString) {
		String[] args = protocolString.split(":"); // 以:分割协议串
		if (args != null && args.length >= 6) {
			version = args[0];
			packetNo = Long.parseLong(args[1]);
			senderName = args[2];
			senderHost = args[3];
			phoneType = args[4];
			commandNo = Integer.parseInt(args[5]);
			if (args.length > 6) {
				extraData = args[6];
			}
		} else {
			throw new InvalidParameterException("invalid protocol string");
		}
	}

	public UdpCmdProtocol(String version, long packetNo, String senderName, String senderHost, String phoneType, int commandNo, String extraData) {
		this.version = version;
		this.packetNo = packetNo;
		this.senderName = senderName;
		this.senderHost = senderHost;
		this.phoneType = phoneType;
		this.commandNo = commandNo;
		this.extraData = extraData;
	}

	/**
	 * 获取协议串
	 * 
	 * @return
	 */
	public String getProtocolString() {
		StringBuffer sb = new StringBuffer();
		sb.append(version);
		sb.append(":");
		sb.append(packetNo);
		sb.append(":");
		sb.append(senderName);
		sb.append(":");
		sb.append(senderHost);
		sb.append(":");
		sb.append(phoneType);
		sb.append(":");
		sb.append(commandNo);
		sb.append(":");
		sb.append(extraData);
		return sb.toString();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getPacketNo() {
		return packetNo;
	}

	public void setPacketNo(long packetNo) {
		this.packetNo = packetNo;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderHost() {
		return senderHost;
	}

	public void setSenderHost(String senderHost) {
		this.senderHost = senderHost;
	}

	public String getPhoneType() {
		return phoneType;
	}

	public void setPhoneType(String phoneType) {
		this.phoneType = phoneType;
	}

	public int getCommandNo() {
		return commandNo;
	}

	public void setCommandNo(int commandNo) {
		this.commandNo = commandNo;
	}

	public String getExtraData() {
		return extraData;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}
}
