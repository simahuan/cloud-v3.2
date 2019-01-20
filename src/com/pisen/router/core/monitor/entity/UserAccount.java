package com.pisen.router.core.monitor.entity;

import org.simpleframework.xml.Attribute;

public abstract class UserAccount {

	@Attribute
	private String uri;

	@Attribute
	private String username;

	@Attribute
	private String password;

	public String getUri() {
		return uri;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
