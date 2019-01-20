package com.pisen.router.core.flashtransfer.scan.protocol;

public class ProtocolContext {

	private String hostIp;
	private int command; // 命令
	private String extraData; // 附加数据

	public ProtocolContext(String hostIp, String protocolString) {
		this.hostIp = hostIp;
		String[] data = protocolString.split(":"); // 以:分割协议串
		if (data != null && data.length >= 2) {
			command = Integer.parseInt(data[3]);
			extraData = protocolString;
		}
	}

	public String getHostIp() {
		return hostIp;
	}

	public int getCommand() {
		return command;
	}

	public String getExtraData() {
		return extraData;
	}

}
