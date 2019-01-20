package com.pisen.router.core.flashtransfer.scan.protocol;

/**
 * 协议常量
 * @author  2012/2/10
 */
public class IpMessageConst {

	public static final int VERSION = 0x001; // 版本号
	public static final String BROADCAST_IP = "224.0.0.1"; //"230.0.0.1"; //// "239.255.255.250";
	public static final String BROADCAST_MARK_OFFLINE= "hotPotOffLine";
	public static final String BROADCAST_MARK_ONLINE = "hotPotOnLine";
	public static final int GROUP_MESSAGE_PORT = 24250;
	public static final int GERNERAL_MESSAGE_PORT = 24260;
	public static final int GERNERAL_HOT_MESSAGE_PORT = 52300;
	public static final String CHARSET_NAME = "UTF-8";

	public static final int IPMSG_BR_ENTRY = 1001; // 用户上线
	public static final int IPMSG_BR_ENTRY_REPLY = 0x1001; // 用户上线回复
	public static final int IPMSG_BR_EXIT  = 1002; // 用户退出
	public static final int IPMSG_BR_EXIT_IPHONE  = 1008; // IOS退出
	public static final int IPMSG_BR_EXIT_REPLY  = 0x1002; // 用户退出回复
	public static final int IPMSG_CONNECT_REQUEST  = 1004; // 请求连接
	public static final int IPMSG_CONNECT_REQUEST_REPLY  = 0x1004; // 请求连接回复
//	public static final int IPMSG_ANSENTRY = 1003; // 通报在线
//	public static final int IPMSG_RECVMSG = 0x1003; // 通报收到消息
//	public static final int IPMSG_BR_BUSY  = 1005; // 用户忙
//    public static final int IPMSG_HT_ENTRY = 1006 ; //热点模式用户上线
//    public static final int IPMSG_HT_OFFLINE = 1007 ; //热点模式用户下线
//	public static final int IPMSG_SENDMSG = 1008; // 发送消息

	// custom Interactive
//	public static final int IPMSG_INTERACTIVE = 1005; // 请求连接
//	public static final int IPMSG_DISCONNECT = 2005; // 断开连接
//	public static final int IPMSG_CANCLECONNECT= 1007; //用户拒绝连接
	// public static final int IPMSG_REFRESH = 0x00000073;
	
	public static final String CHARSET = "utf-8";

}
