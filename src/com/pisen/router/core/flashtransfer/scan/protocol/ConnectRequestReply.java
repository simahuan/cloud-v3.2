package com.pisen.router.core.flashtransfer.scan.protocol;

/**
 * (用户信息)
 * 
 * @author yangyp
 */
public class ConnectRequestReply extends UdpMsgProtocol {

	public boolean agree; // 是否同意

	public ConnectRequestReply() {
		super(IpMessageConst.IPMSG_CONNECT_REQUEST_REPLY);
	}

	@Override
	public String getMsg() {
		StringBuffer sb = new StringBuffer(super.getMsg());
		sb.append(agree);
		return sb.toString();
	}

	@Override
	public void read(String extraData) {
		String[] data = extraData.split("-");
		agree = Boolean.parseBoolean(data[0]);
	}
}
