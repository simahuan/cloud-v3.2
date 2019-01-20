/**
 * 
 */
package com.pisen.router.ui.phone.device.bean;

import java.io.Serializable;

/**
 * 
 * @author Liuhc
 * @version 1.0 2015年5月14日 下午4:34:22
 * @des 无线中断Bean
 */
public class StaBean implements Serializable {

	private static final long serialVersionUID = 1L;
	String exist;
	String physics_state;
	String ssid;
	String encryption;
	String net_state;
	String ipaddr;
	String netmask;
	String gateway;
	String dns;

	// pisen
	String wirelessTerm;
	String relaySwitch;
	String currentAp;
	String apPassword;

	// 穿墙王Item
	String essid;
	String password;
	String encoding;
	String sw;

	public String getEssid() {
		return essid;
	}

	public void setEssid(String essid) {
		this.essid = essid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getSw() {
		return sw;
	}

	public void setSw(String sw) {
		this.sw = sw;
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

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getEncryption() {
		return encryption;
	}

	public void setEncryption(String encryption) {
		this.encryption = encryption;
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

	public String getDns() {
		return dns;
	}

	public void setDns(String dns) {
		this.dns = dns;
	}

	public String getWirelessTerm() {
		return wirelessTerm;
	}

	public void setWirelessTerm(String wirelessTerm) {
		this.wirelessTerm = wirelessTerm;
	}

	public String getRelaySwitch() {
		return relaySwitch;
	}

	public void setRelaySwitch(String relaySwitch) {
		this.relaySwitch = relaySwitch;
	}

	public String getCurrentAp() {
		return currentAp;
	}

	public void setCurrentAp(String currentAp) {
		this.currentAp = currentAp;
	}

	public String getApPassword() {
		return apPassword;
	}

	public void setApPassword(String apPassword) {
		this.apPassword = apPassword;
	}

}
