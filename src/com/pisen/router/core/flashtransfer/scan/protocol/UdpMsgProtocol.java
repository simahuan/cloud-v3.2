package com.pisen.router.core.flashtransfer.scan.protocol;

public abstract class UdpMsgProtocol {

	protected String appType;
	protected int cmd; // 指令

	public UdpMsgProtocol(int cmd) {
		super();
		this.cmd = cmd;
	}

	public int getCmd() {
		return cmd;
	}

	public String getMsg() {
		StringBuffer sb = new StringBuffer();
		sb.append(appType);
		sb.append(":");
		return sb.toString();
	}

	public void read(String extraData) {

	}

}
