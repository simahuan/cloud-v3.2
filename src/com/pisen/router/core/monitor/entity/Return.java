package com.pisen.router.core.monitor.entity;

import org.simpleframework.xml.Attribute;

public class Return {

	@Attribute(name = "status")
	public boolean status;

	@Attribute(required = false)
	public String model;

}
