package com.gillsoft.distribusion.client;

import java.io.Serializable;

public class Data implements Serializable {

	private static final long serialVersionUID = 8893764049117801762L;
	
	public static final String BOOKINGS_TYPE = "bookings";

	private String id;

	private String type;

	private Attributes attributes;

	private Relationships relationships;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public Relationships getRelationships() {
		return relationships;
	}

	public void setRelationships(Relationships relationships) {
		this.relationships = relationships;
	}
	
	public boolean isTripAvailable() {
		return !getAttributes().isBookedOut()
				&& getRelationships().getSegments() != null
				&& getRelationships().getSegments().getData().size() == 1;
	}
	
	public boolean isAdultPassengerType() {
		return getAttributes().getName() != null
				&& getAttributes().getName().toLowerCase().contains("adult");
	}

}
