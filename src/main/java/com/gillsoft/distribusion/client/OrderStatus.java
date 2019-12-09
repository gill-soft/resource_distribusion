package com.gillsoft.distribusion.client;

import java.io.Serializable;

public class OrderStatus implements Serializable {
	
	private static final long serialVersionUID = -5896544654954979977L;
	
	private Data data;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

}
