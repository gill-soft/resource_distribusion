package com.gillsoft.distribusion.client;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gillsoft.util.StringUtil;

public class Attributes implements Serializable {

	private static final long serialVersionUID = -6684211482343298430L;

	private String name;

	private String code;

	@JsonProperty("street_and_number")
	private String streetAndNumber;

	@JsonProperty("zip_code")
	private String zipCode;

	private String description;

	private BigDecimal latitude;

	private BigDecimal longitude;

	@JsonProperty("time_zone")
	private String timeZone;

	@JsonProperty("departure_time")
	private String departure;

	@JsonProperty("arrival_time")
	private String arrival;

	private int duration;

	@JsonProperty("cheapest_total_adult_price")
	private int cheapestTotalAdultPrice;

	@JsonProperty("booked_out")
	private boolean bookedOut;

	private boolean vacant;

	@JsonProperty("total_price")
	private int totalPrice;

	private String title;

	@JsonProperty("state")
	private String state;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	private String email;

	private String phone;

	private String city;

	@JsonProperty("execute_payment")
	private boolean executePayment;

	@JsonProperty("payment_method")
	private String paymentMethod;

	@JsonProperty("payment_token")
	private String paymentToken;

	@JsonProperty("payer_id")
	private String payerId;

	private int pax;

	@JsonProperty("flight_number")
	private String flightNumber;

	@JsonProperty("distribusion_booking_number")
	private String distribusionBookingNumber;

	@JsonProperty("marketing_carrier_booking_number")
	private String marketingCarrierBookingNumber;

	@JsonProperty("termsAccepted")
	private boolean terms_accepted;

	@JsonProperty("send_customer_email")
	private boolean sendCustomerEmail;

	@JsonProperty("retailer_partner_number")
	private String retailerPartnerNumber;

	@JsonProperty("created_at")
	private String created;

	@JsonProperty("trade_name")
	private String tradeName;

	@JsonProperty("legal_name")
	private String legalName;

	private String address;

	private String fax;

	@JsonProperty("customer_service_phone")
	private String customerServicePhone;

	@JsonProperty("commercial_register")
	private String commercialRegister;

	@JsonProperty("commercial_register_number")
	private String commercialRegisterNumber;

	@JsonProperty("vat_no")
	private String vatNo;

	@JsonProperty("authorised_representative")
	private String authorisedRepresentative;

	@JsonProperty("white_label_logo")
	private String whiteLabelLogo;

	@JsonProperty("white_label_colour_code")
	private String whiteLabelColour;

	private String terms;

	@JsonProperty("flight_number_required")
	private boolean flightNumberRequired;

	@JsonProperty("booking_fee")
	private int bookingFee;

	private boolean cancellationAllowed;

	@JsonProperty("cancellation_fee")
	private int cancellationFee;

	@JsonProperty("cancellation_cutoff")
	private int cancellationCutoff;

	private String type;

	private int price;

	private boolean allowed;

	private int fee;

	private String cutoff;

	@JsonProperty("total_refund")
	private int totalRefund;
	
	@JsonProperty("min_age")
	private int minAge;
	
	@JsonProperty("max_age")
	private int maxAge;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getStreetAndNumber() {
		return streetAndNumber;
	}

	public void setStreetAndNumber(String streetAndNumber) {
		this.streetAndNumber = streetAndNumber;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public Date getDeparture() {
		return parse(departure);
	}

	public void setDeparture(String departure) {
		this.departure = departure;
	}

	public Date getArrival() {
		return parse(arrival);
	}

	public void setArrival(String arrival) {
		this.arrival = arrival;
	}
	
	private Date parse(String date) {
		if (date != null) {
			try {
				return StringUtil.fullDateFormat.parse(date.replace("T", " "));
			} catch (ParseException e) {
			}
		}
		return null;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getCheapestTotalAdultPrice() {
		return cheapestTotalAdultPrice;
	}

	public void setCheapestTotalAdultPrice(int cheapestTotalAdultPrice) {
		this.cheapestTotalAdultPrice = cheapestTotalAdultPrice;
	}

	public boolean isBookedOut() {
		return bookedOut;
	}

	public void setBookedOut(boolean bookedOut) {
		this.bookedOut = bookedOut;
	}

	public boolean isVacant() {
		return vacant;
	}

	public void setVacant(boolean vacant) {
		this.vacant = vacant;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
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

	public String getPaymentToken() {
		return paymentToken;
	}

	public void setPaymentToken(String paymentToken) {
		this.paymentToken = paymentToken;
	}

	public String getPayerId() {
		return payerId;
	}

	public void setPayerId(String payerId) {
		this.payerId = payerId;
	}

	public int getPax() {
		return pax;
	}

	public void setPax(int pax) {
		this.pax = pax;
	}

	public String getFlightNumber() {
		return flightNumber;
	}

	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}

	public String getDistribusionBookingNumber() {
		return distribusionBookingNumber;
	}

	public void setDistribusionBookingNumber(String distribusionBookingNumber) {
		this.distribusionBookingNumber = distribusionBookingNumber;
	}

	public String getMarketingCarrierBookingNumber() {
		return marketingCarrierBookingNumber;
	}

	public void setMarketingCarrierBookingNumber(String marketingCarrierBookingNumber) {
		this.marketingCarrierBookingNumber = marketingCarrierBookingNumber;
	}

	public boolean isTerms_accepted() {
		return terms_accepted;
	}

	public void setTerms_accepted(boolean terms_accepted) {
		this.terms_accepted = terms_accepted;
	}

	public boolean isSendCustomerEmail() {
		return sendCustomerEmail;
	}

	public void setSendCustomerEmail(boolean sendCustomerEmail) {
		this.sendCustomerEmail = sendCustomerEmail;
	}

	public String getRetailerPartnerNumber() {
		return retailerPartnerNumber;
	}

	public void setRetailerPartnerNumber(String retailerPartnerNumber) {
		this.retailerPartnerNumber = retailerPartnerNumber;
	}

	public Date getCreated() {
		return parse(created);
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getTradeName() {
		return tradeName;
	}

	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}

	public String getLegalName() {
		return legalName;
	}

	public void setLegalName(String legalName) {
		this.legalName = legalName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getCustomerServicePhone() {
		return customerServicePhone;
	}

	public void setCustomerServicePhone(String customerServicePhone) {
		this.customerServicePhone = customerServicePhone;
	}

	public String getCommercialRegister() {
		return commercialRegister;
	}

	public void setCommercialRegister(String commercialRegister) {
		this.commercialRegister = commercialRegister;
	}

	public String getCommercialRegisterNumber() {
		return commercialRegisterNumber;
	}

	public void setCommercialRegisterNumber(String commercialRegisterNumber) {
		this.commercialRegisterNumber = commercialRegisterNumber;
	}

	public String getVatNo() {
		return vatNo;
	}

	public void setVatNo(String vatNo) {
		this.vatNo = vatNo;
	}

	public String getAuthorisedRepresentative() {
		return authorisedRepresentative;
	}

	public void setAuthorisedRepresentative(String authorisedRepresentative) {
		this.authorisedRepresentative = authorisedRepresentative;
	}

	public String getWhiteLabelLogo() {
		return whiteLabelLogo;
	}

	public void setWhiteLabelLogo(String whiteLabelLogo) {
		this.whiteLabelLogo = whiteLabelLogo;
	}

	public String getWhiteLabelColour() {
		return whiteLabelColour;
	}

	public void setWhiteLabelColour(String whiteLabelColour) {
		this.whiteLabelColour = whiteLabelColour;
	}

	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}

	public boolean isFlightNumberRequired() {
		return flightNumberRequired;
	}

	public void setFlightNumberRequired(boolean flightNumberRequired) {
		this.flightNumberRequired = flightNumberRequired;
	}

	public int getBookingFee() {
		return bookingFee;
	}

	public void setBookingFee(int bookingFee) {
		this.bookingFee = bookingFee;
	}

	public boolean isCancellationAllowed() {
		return cancellationAllowed;
	}

	public void setCancellationAllowed(boolean cancellationAllowed) {
		this.cancellationAllowed = cancellationAllowed;
	}

	public int getCancellationFee() {
		return cancellationFee;
	}

	public void setCancellationFee(int cancellationFee) {
		this.cancellationFee = cancellationFee;
	}

	public int getCancellationCutoff() {
		return cancellationCutoff;
	}

	public void setCancellationCutoff(int cancellationCutoff) {
		this.cancellationCutoff = cancellationCutoff;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

	public int getFee() {
		return fee;
	}

	public void setFee(int fee) {
		this.fee = fee;
	}

	public Date getCutoff() {
		return parse(cutoff);
	}

	public void setCutoff(String cutoff) {
		this.cutoff = cutoff;
	}

	public int getTotalRefund() {
		return totalRefund;
	}

	public void setTotalRefund(int totalRefund) {
		this.totalRefund = totalRefund;
	}

	public int getMinAge() {
		return minAge;
	}

	public void setMinAge(int minAge) {
		this.minAge = minAge;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

}
