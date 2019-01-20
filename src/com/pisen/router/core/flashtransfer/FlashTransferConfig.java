package com.pisen.router.core.flashtransfer;

/**
 * 闪电互传相关配置参数
 * @author ldj
 * @version 1.0 2015年3月30日 下午5:21:43
 */
public class FlashTransferConfig {
	public static final String SPILIT_FIX = ":";
	//udp广播地址host
//	public static final String HOST_UDP_BROADCAST = "224.0.0.1";
	//udp广播地址port
	public static final int PORT_UDP_BROADCAST = 24250;
//	//udp发现扫描接收方单播地址port
//	public static final int PORT_UDP_SINGLE_RECV = 24260;
	//udp发现扫描发送方单播地址port
	public static final int PORT_UDP_SINGLE_SEND = 24260;
	//http连接请求监听端口
//	public static final int PORT_HTTP_SCAN = 6654;
	//http文件接收监听端口
	public static final int PORT_HTTP_RECV_FILE = 12345;
	public static final int PORT_HTTP_RECV_HEAD = 22345;
	
	/*action*/
	public static final String ACTION_TRANSFER_AUTH = "action_transfer_auth";
	public static final String ACTION_TRANSFER_AUTH_AGREE = "action_transfer_auth_agree";
	public static final String ACTION_TRANSFER_AUTH_REFUSE = "action_transfer_auth_refuse";
	public static final String ACTION_TRANSFER_SEND_REFRESH = "action_transfer_send_refresh";
	public static final String ACTION_TRANSFER_RECV_REFRESH = "action_transfer_recv_refresh";
	
	/*intent extra*/
	public static final String EXTRA_TRANSFERINFO_ID = "extra_transferinfo_id";
	public static final String EXTRA_TRANSFERINFO_STATUS = "extra_transferinfo_status";
	public static final String EXTRA_TRANSFERINFO_CURBYTES = "extra_transferinfo_curbytes";
	public static final String EXTRA_TRANSFERINFO_FILESIZE = "extra_transferinfo_filesize";
	public static final String EXTRA_TRANSFERINFO_HOSTNAME = "extra_transferinfo_hostname";
	
	//Android端udp数据协议交互版本
	public static final String VERSION_IP_MESSAGE = "1.0";
	public static final String PHONE_TYPE_ANDROID = "Android";
	public static final String PHONE_TYPE_IOS = "iphone";
	
	//Intent extra key
	public static final String EXTRA_OBJ = "extra_obj";
	
}
