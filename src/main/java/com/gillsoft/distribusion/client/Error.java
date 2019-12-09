package com.gillsoft.distribusion.client;

import java.io.Serializable;

public class Error implements Serializable {

	private static final long serialVersionUID = -6125787650553297006L;

	private String code;

	private String title;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
