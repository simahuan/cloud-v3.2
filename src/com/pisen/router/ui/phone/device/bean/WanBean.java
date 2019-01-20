/**
 * 
 */
package com.pisen.router.ui.phone.device.bean;

import java.io.Serializable;

/**
 * @author Liuhc
 * @version 1.0 2015年5月14日 下午4:34:22
 * @des 有线连接Bean
 */
public class WanBean implements Serializable{
	/** TODO */
	private static final long serialVersionUID = 1L;
	String exist;
	String physics_state;
	String proto;//[dhcp/pppoe/static/cable error]
	String net_state;
	String ipaddr;
	String netmask;
	String gateway;
	String dns1;
	String dns2;
	String username;
	String password;
	
	//pisen
	String wiredMode;
	String wiredStatus;
	String wired_ip;
	String wired_submask;
	String wired_gateway;
	String wired_first_dns;
	String wired_spare_dns;
	String pppoeAccount;
	String pppoePassword;
	String pppoelink;
	
	//穿墙王R300M
	String mode;
	String ip;
	String netMask;
	String firstDns;
	String secondDns;
	String account;
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getNetMask() {
		return netMask;
	}
	public void setNetMask(String netMask) {
		this.netMask = netMask;
	}
	public String getFirstDns() {
		return firstDns;
	}
	public void setFirstDns(String firstDns) {
		this.firstDns = firstDns;
	}
	public String getSecondDns() {
		return secondDns;
	}
	public void setSecondDns(String secondDns) {
		this.secondDns = secondDns;
	}
	public String getPppoeAccount() {
		return pppoeAccount;
	}
	public void setPppoeAccount(String pppoeAccount) {
		this.pppoeAccount = pppoeAccount;
	}
	public String getPppoePassword() {
		return pppoePassword;
	}
	public void setPppoePassword(String pppoePassword) {
		this.pppoePassword = pppoePassword;
	}
	public String getPppoelink() {
		return pppoelink;
	}
	public void setPppoelink(String pppoelink) {
		this.pppoelink = pppoelink;
	}
	public String getDns1() {
		return dns1;
	}
	public void setDns1(String dns1) {
		this.dns1 = dns1;
	}
	public String getDns2() {
		return dns2;
	}
	public void setDns2(String dns2) {
		this.dns2 = dns2;
	}
	public String getExist() {
		return exist;
	}
	public void setExist(String exist) {
		this.exist = exist;
	}
	public String getPhysics_state() {
		return physics_state;
	}
	public void setPhysics_state(String physics_state) {
		this.physics_state = physics_state;
	}
	public String getProto() {
		return proto;
	}
	public void setProto(String proto) {
		this.proto = proto;
	}
	public String getNet_state() {
		return net_state;
	}
	public void setNet_state(String net_state) {
		this.net_state = net_state;
	}
	public String getIpaddr() {
		return ipaddr;
	}
	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}
	public String getNetmask() {
		return netmask;
	}
	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}
	public String getGateway() {
		return gateway;
	}
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	public String getWiredMode() {
		return wiredMode;
	}
	public void setWiredMode(String wiredMode) {
		this.wiredMode = wiredMode;
	}
	public String getWiredStatus() {
		return wiredStatus;
	}
	public void setWiredStatus(String wiredStatus) {
		this.wiredStatus = wiredStatus;
	}
	public String getWired_ip() {
		return wired_ip;
	}
	public void setWired_ip(String wired_ip) {
		this.wired_ip = wired_ip;
	}
	public String getWired_submask() {
		return wired_submask;
	}
	public void setWired_submask(String wired_submask) {
		this.wired_submask = wired_submask;
	}
	public String getWired_gateway() {
		return wired_gateway;
	}
	public void setWired_gateway(String wired_gateway) {
		this.wired_gateway = wired_gateway;
	}
	public String getWired_first_dns() {
		return wired_first_dns;
	}
	public void setWired_first_dns(String wired_first_dns) {
		this.wired_first_dns = wired_first_dns;
	}
	public String getWired_spare_dns() {
		return wired_spare_dns;
	}
	public void setWired_spare_dns(String wired_spare_dns) {
		this.wired_spare_dns = wired_spare_dns;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
