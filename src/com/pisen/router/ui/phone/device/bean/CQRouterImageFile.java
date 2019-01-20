package com.pisen.router.ui.phone.device.bean;

import java.io.Serializable;

/**
 * @author  mahuan
 * @version 1.0 2015年10月9日 下午2:48:51
 * @desc{穿墙王.固件　信息bean}
 */
public class CQRouterImageFile implements Serializable {

	/** TODO */
	private static final long serialVersionUID = 1L;

	/*
	 * "status":"xxx", "version":"xxx", "description":"xxx"
	 */
	private String status;
	private String version;
	private String description;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
