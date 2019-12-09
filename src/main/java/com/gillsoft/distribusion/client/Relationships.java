package com.gillsoft.distribusion.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Relationships implements Serializable {

	private static final long serialVersionUID = 2637491377486012688L;

	private DataItem city;

	@JsonProperty("departure_station")
	private DataItem departure;

	@JsonProperty("arrival_station")
	private DataItem arrival;

	@JsonProperty("marketing_carrier")
	private DataItem marketingCarrier;

	@JsonProperty("operating_carrier")
	private DataItem operatingCarrier;

	private DataItems passengers;

	@JsonProperty("passenger_types")
	private DataItems passengerTypes;

	@JsonProperty("cancellation")
	private Cancellation cancellation;

	private DataItems segments;

	public DataItem getCity() {
		return city;
	}

	public void setCity(DataItem city) {
		this.city = city;
	}

	public DataItem getDeparture() {
		return departure;
	}

	public void setDeparture(DataItem departure) {
		this.departure = departure;
	}

	public DataItem getArrival() {
		return arrival;
	}

	public void setArrival(DataItem arrival) {
		this.arrival = arrival;
	}

	public DataItem getMarketingCarrier() {
		return marketingCarrier;
	}

	public void setMarketingCarrier(DataItem marketingCarrier) {
		this.marketingCarrier = marketingCarrier;
	}

	public DataItem getOperatingCarrier() {
		return operatingCarrier;
	}

	public void setOperatingCarrier(DataItem operatingCarrier) {
		this.operatingCarrier = operatingCarrier;
	}

	public DataItems getPassengers() {
		return passengers;
	}

	public void setPassengers(DataItems passengers) {
		this.passengers = passengers;
	}

	public DataItems getSegments() {
		return segments;
	}

	public void setSegments(DataItems segments) {
		this.segments = segments;
	}

	public DataItems getPassengerTypes() {
		return passengerTypes;
	}

	public void setPassengerTypes(DataItems passengerTypes) {
		this.passengerTypes = passengerTypes;
	}

	public Cancellation getCancellation() {
		return cancellation;
	}

	public void setCancellation(Cancellation cancellation) {
		this.cancellation = cancellation;
	}

	@Override
	public String toString() {
		return "Relationships [city=" + city + ", departure=" + departure + ", arrival=" + arrival
				+ ", marketingCarrier=" + marketingCarrier + ", operatingCarrier=" + operatingCarrier + ", passengers="
				+ passengers + ", passengerTypes=" + passengerTypes + ", cancellation=" + cancellation + ", segments="
				+ segments + "]";
	}

}
