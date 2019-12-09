package com.gillsoft.distribusion.client;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateBookingRequest implements Serializable {

	private static final long serialVersionUID = -5994998706456773770L;

	private String booking;

	@JsonProperty("marketing_carrier")
	private String carrier;

	@JsonProperty("departure_station")
	private String departureStation;

	@JsonProperty("arrival_station")
	private String arrivalStation;

	@JsonProperty("departure_time")
	private Date departure;

	@JsonProperty("arrival_time")
	private Date arrival;

	@JsonProperty("retailer_partner_number")
	private String partnerNumber;

	private String title;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	private String email;

	private String phone;

	private String city;

	@JsonProperty("zip_code")
	private String zipCode;

	@JsonProperty("street_and_number")
	private String streetAndNumber;

	@JsonProperty("execute_payment")
	private boolean executePayment;

	@JsonProperty("payment_method")
	private String paymentMethod;

	@JsonProperty("total_price")
	private BigDecimal totalPrice;

	private int pax;

	@JsonProperty("terms_accepted")
	private boolean termsAccepted;

	private String locale;

	private String currency;

	@JsonProperty("send_customer_email")
	private boolean sendCustomerEmail;

	private List<Passenger> passengers;

	public String getBooking() {
		return booking;
	}

	public void setBooking(String booking) {
		this.booking = booking;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getDepartureStation() {
		return departureStation;
	}

	public void setDepartureStation(String departureStation) {
		this.departureStation = departureStation;
	}

	public String getArrivalStation() {
		return arrivalStation;
	}

	public void setArrivalStation(String arrivalStation) {
		this.arrivalStation = arrivalStation;
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

	public String getPartnerNumber() {
		return partnerNumber;
	}

	public void setPartnerNumber(String partnerNumber) {
		this.partnerNumber = partnerNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getStreetAndNumber() {
		return streetAndNumber;
	}

	public void setStreetAndNumber(String streetAndNumber) {
		this.streetAndNumber = streetAndNumber;
	}

	public boolean isExecutePayment() {
		return executePayment;
	}

	public void setExecutePayment(boolean executePayment) {
		this.executePayment = executePayment;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public int getPax() {
		return pax;
	}

	public void setPax(int pax) {
		this.pax = pax;
	}

	public boolean isTermsAccepted() {
		return termsAccepted;
	}

	public void setTermsAccepted(boolean termsAccepted) {
		this.termsAccepted = termsAccepted;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public boolean isSendCustomerEmail() {
		return sendCustomerEmail;
	}

	public void setSendCustomerEmail(boolean sendCustomerEmail) {
		this.sendCustomerEmail = sendCustomerEmail;
	}

	public List<Passenger> getPassengers() {
		return passengers;
	}

	public void setPassengers(List<Passenger> passengers) {
		this.passengers = passengers;
	}

}
