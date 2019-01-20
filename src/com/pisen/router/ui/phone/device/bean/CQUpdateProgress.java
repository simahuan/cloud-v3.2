package com.pisen.router.ui.phone.device.bean;

import java.io.Serializable;

/**
 * @author  "simahuan"
 * @version 1.0 2015年10月26日 下午4:06:01
 * @desc{tags}
 */
public class CQUpdateProgress implements Serializable {

	/** TODO */
	private static final long serialVersionUID = 1L;
	String total;

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getCurrent() {
		return current;
	}

	public void setCurrent(String current) {
		this.current = current;
	}

	String current;

}
