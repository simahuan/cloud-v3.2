package com.pisen.router.core.monitor.entity;

import org.simpleframework.xml.Element;

public class Account {

	@Element(name = "Webdav")
	public Webdav webdav;

	@Element(name = "Smb", required = false)
	public Smb smb;

	@Element(name = "Ftp", required = false)
	public Ftp ftp;

}
