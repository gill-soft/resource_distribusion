package com.gillsoft.distribusion.client;

import java.io.Serializable;

public class Cancellation implements Serializable {

	private static final long serialVersionUID = -231984113752921197L;
	
	private Data data;

	public void setData(Data data) {
		this.data = data;
	}

	public Data getData() {
		return data;
	}

}
