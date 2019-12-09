package com.gillsoft.distribusion.client;

import java.io.Serializable;
import java.util.List;

public class ErrorResponse implements Serializable {

	private static final long serialVersionUID = 7956006236466571155L;
	
	private List<Error> errors;
	
	public List<Error> getErrors() {
		return errors;
	}

	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

}
