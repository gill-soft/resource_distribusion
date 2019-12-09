package com.gillsoft.distribusion.client;

import java.util.List;

public class DataItem extends ErrorResponse {

	private static final long serialVersionUID = -3367519872956563251L;

	private Data data;

	private List<Data> included;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public List<Data> getIncluded() {
		return included;
	}

	public void setIncluded(List<Data> included) {
		this.included = included;
	}

}
