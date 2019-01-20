package com.pisen.router.core.monitor.entity;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.pisen.router.core.monitor.DiskUtils;

@Root(name = "getSysInfo")
public class RouterConfig {

	/**
	 * 修改：
	 * 
	 * 1 修改sysinfo上报的xml 1）内置存储使用AirDisk不变； 2）外置存储使用sdb1，不加前缀EXT_；
	 * 3）在sysinfo接口返回的xml中添加extusb字段标示外置存储。extusb=yes 标示外置存储，extusb=no是内置存储
	 * 
	 * 2 修改SSID： PISEN_POWER_XXXXXX（MAC后六位），WIFI密码： 00000000（8个零）
	 * 
	 * 3 修改wifi支持10个客户端；
	 * 
	 * 4 修改SSH/串口登陆密码：werouter2pisen
	 * 
	 * 5 MAC读取地址：u-boot-env@0x1fc00 ---》 art@0x1000
	 */
	// 150M默认webdav访问地址与帐号
	private static final String R150M_WEBDAV_URL = "http://192.168.222.254";
	private static final String R150M_WEBDAV_USER = "pisen";
	private static final String R150M_WEBDAV_PASSWORD = "123456";

	// @Element(name = "Storage")
	// public Storage storage;

	@ElementList(name = "Storage", entry = "Section")
	public List<Section> sectionList;

	@Element(name = "Account", required = false)
	public Account account;

	@Element(name = "Return", required = false)
	public Return result;

	public enum Model {
		R150M, R300M, RZHIXIANG
	}

	/**
	 * 如果帐号不存在，那么使用150M地址
	 * 
	 * @return
	 */
	public String getWebdavRootUrl() {
		return hasWebdavAccount() ? R150M_WEBDAV_URL : account.webdav.getUri();
	}

	/**
	 * 获取第一个磁盘访问地址
	 * 
	 * @return
	 */
	public String getWebdavSectionFirstUrl() {
		return isDiskMount() ? String.format("%s/%s/", getWebdavRootUrl(), sectionList.get(0).volume) : null;
	}

	/**
	 * 获取任意磁盘访问地址
	 * 
	 * @return
	 */
	public String getSectionWebdavUrl(Section section) {
		return isDiskMount() ? String.format("%s/%s/", getWebdavRootUrl(), section.volume) : null;
	}

	public String getWebdavUsername() {
		return hasWebdavAccount() ? R150M_WEBDAV_USER : account.webdav.getUsername();
	}

	public String getWebdavPassword() {
		return hasWebdavAccount() ? R150M_WEBDAV_PASSWORD : account.webdav.getPassword();
	}

	/**
	 * @return true {@link Account} 不存在
	 */
	private boolean hasWebdavAccount() {
		return account == null || account.webdav == null;
	}

	/**
	 * 磁盘是否挂载
	 * 
	 * @return true未挂载 | false已挂载 --->修正 true 已挂载|false 未挂载
	 */
	public boolean isDiskMount() {
		return !(sectionList == null || sectionList.isEmpty());
	}

	/**
	 * 获取第一个磁盘对象
	 * 
	 * @return
	 */
	public Section getSectionFirst() {
		return isDiskMount() ? sectionList.get(0) : null;
	}

	public String getTotal() {
		return DiskUtils.getFileSize(getTotalMB());
	}

	public double getTotalMB() {
		long result = 0;
		if (isDiskMount()) {
			for (Section section : sectionList) {
				result += section.getTotalUnit();
			}
		}
		return result;
	}

	public String getUsed() {
		return DiskUtils.getFileSize(getUsedMB());
	}

	public double getUsedMB() {
		long result = 0;
		if (isDiskMount()) {
			for (Section section : sectionList) {
				result += section.getUsedUnit();
			}
		}
		return result;
	}

	public String getFree() {
		return DiskUtils.getFileSize(getFreeMB());
	}

	public double getFreeMB() {
		long result = 0;
		if (isDiskMount()) {
			for (Section section : sectionList) {
				result += section.getFreeUnit();
			}
		}
		return result;
	}

	public Return getResult() {
		return result;
	}
}
