package com.gillsoft.distribusion.client;

import java.util.List;

public class DataItems extends ErrorResponse {

	private static final long serialVersionUID = -6356938153797879480L;

	private List<Data> data;

	private List<Data> included;

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}

	public List<Data> getIncluded() {
		return included;
	}

	public void setIncluded(List<Data> included) {
		this.included = included;
	}

}
