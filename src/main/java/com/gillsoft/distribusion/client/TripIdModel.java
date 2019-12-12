package com.gillsoft.distribusion.client;

import java.util.Date;

import com.gillsoft.model.AbstractJsonModel;

public class TripIdModel extends AbstractJsonModel {

	private static final long serialVersionUID = 4877205938571264589L;
	
	private String carrier;
	private String from;
	private String to;
	private Date departure;
	private Date arrival;
	
	public TripIdModel() {
		
	}

	public TripIdModel(String id, String carrier, String from, String to, Date departure, Date arrival) {
		setId(id);
		this.carrier = carrier;
		this.from = from;
		this.to = to;
		this.departure = departure;
		this.arrival = arrival;
	}
	
	public TripIdModel(Data item) {
		setId(item.getRelationships().getSegments().getData().get(0).getId());
		setCarrier(item.getRelationships().getMarketingCarrier().getData().getId());
		setFrom(item.getRelationships().getDeparture().getData().getId());
		setTo(item.getRelationships().getArrival().getData().getId());
		setDeparture(item.getAttributes().getDeparture());
		setArrival(item.getAttributes().getArrival());
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public Date getDeparture() {
		return departure;
	}

	public void setDeparture(Date departure) {
		this.departure = departure;
	}

	public Date getArrival() {
		return arrival;
	}

	public void setArrival(Date arrival) {
		this.arrival = arrival;
	}

	@Override
	public TripIdModel create(String json) {
		return (TripIdModel) super.create(json);
	}
	
}
