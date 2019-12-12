package com.gillsoft.distribusion.client;

import com.gillsoft.model.AbstractJsonModel;
import com.gillsoft.model.Customer;

public class ServiceIdModel extends AbstractJsonModel {

	private static final long serialVersionUID = -3740187059130223371L;
	
	private TripIdModel idModel;
	private Customer customer;
	private String typeId;
	private int price;
	
	public ServiceIdModel() {
		
	}

	public ServiceIdModel(TripIdModel idModel, Customer customer, String typeId, int price) {
		this.idModel = idModel;
		this.customer = customer;
		this.typeId = typeId;
		this.price = price;
	}

	public TripIdModel getIdModel() {
		return idModel;
	}

	public void setIdModel(TripIdModel idModel) {
		this.idModel = idModel;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public ServiceIdModel create(String json) {
		return (ServiceIdModel) super.create(json);
	}
	
}
