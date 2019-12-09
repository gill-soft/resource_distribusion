package com.gillsoft.distribusion.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Passenger implements Serializable {

	private static final long serialVersionUID = -6441904097974438543L;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	private String type;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
