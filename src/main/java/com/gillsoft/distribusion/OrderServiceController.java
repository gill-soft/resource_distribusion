package com.gillsoft.distribusion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.abstract_rest_service.AbstractOrderService;
import com.gillsoft.distribusion.client.Data;
import com.gillsoft.distribusion.client.DataItem;
import com.gillsoft.distribusion.client.OrderIdModel;
import com.gillsoft.distribusion.client.RestClient;
import com.gillsoft.distribusion.client.ServiceIdModel;
import com.gillsoft.distribusion.client.TripIdModel;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Document;
import com.gillsoft.model.DocumentType;
import com.gillsoft.model.Price;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.RestError;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.util.RestTemplateUtil;

@RestController
public class OrderServiceController extends AbstractOrderService {
	
	@Autowired
	private RestClient client;
	
	@Autowired
	private SearchServiceController search;

	@Override
	public OrderResponse createResponse(OrderRequest request) {
		
		// формируем ответ
		OrderResponse response = new OrderResponse();
		response.setCustomers(request.getCustomers());
		
		List<ServiceItem> resultItems = new ArrayList<>();
		
		OrderIdModel orderId = new OrderIdModel();
		
		Map<String, List<ServiceItem>> groupedServices = groupeByTariff(request);
		
		// проверяем возможность оформления по выбранным тарифам
		for (Entry<String, List<ServiceItem>> entry : groupedServices.entrySet()) {
			TripIdModel idModel = getTripIdModel(entry.getValue().get(0));
			try {
				DataItem type = client.getTypeInfo(idModel, entry.getKey());
				checkVacant(type);
				List<Tariff> tariffs = search.getTariffs(idModel.asString());
				Tariff tariff = findTariff(tariffs, entry.getKey());
				Price price = search.createPrice(tariff);
				for (ServiceItem serviceItem : entry.getValue()) {
					ServiceIdModel serviceIdModel = createServiceId(request, serviceItem, type);
					orderId.getIds().add(serviceIdModel);
					serviceItem.setId(serviceIdModel.asString());
					serviceItem.setPrice(price);
					resultItems.add(serviceItem);
				}
			} catch (ResponseError e) {
				addServicesWithError(resultItems, entry.getValue(), e);
			}
			
		}
		response.setOrderId(orderId.asString());
		response.setServices(resultItems);
		return response;
	}
	
	private Map<String, List<ServiceItem>> groupeByTariff(OrderRequest request) {
		return request.getServices().stream().collect(
				Collectors.groupingBy(s -> getServiceTariffId(s), Collectors.toList()));
	}
	
	private TripIdModel getTripIdModel(ServiceItem serviceItem) {
		return new TripIdModel().create(serviceItem.getSegment().getId());
	}
	
	private void addServicesWithError(List<ServiceItem> resultItems, List<ServiceItem> requestItems, ResponseError e) {
		for (ServiceItem item : requestItems) {
			item.setError(new RestError(e.getMessage()));
			resultItems.add(item);
		}
	}
	
	private void checkVacant(DataItem dataType) throws ResponseError {
		if (!dataType.getData().getAttributes().isVacant()) {
			throw new ResponseError("Tariff " + dataType.getData().getId() + " is disabled.");
		}
	}
	
	private ServiceIdModel createServiceId(OrderRequest request, ServiceItem serviceItem, DataItem type) {
		TripIdModel idModel = getTripIdModel(serviceItem);
		return new ServiceIdModel(idModel, request.getCustomers().get(serviceItem.getCustomer().getId()),
				getServiceTariffId(serviceItem), type.getData().getAttributes().getTotalPrice());
	}
	
	private Tariff findTariff(List<Tariff> tariffs, String tariffId) throws ResponseError {
		for (Tariff tariff : tariffs) {
			if (Objects.equals(tariffId, tariff.getId())) {
				return tariff;
			}
		}
		throw new ResponseError("Tariff " + tariffId + " not finded.");
	}
	
	private String getServiceTariffId(ServiceItem serviceItem) {
		return serviceItem.getPrice().getTariff().getId();
	}

	@Override
	public OrderResponse addServicesResponse(OrderRequest request) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public OrderResponse removeServicesResponse(OrderRequest request) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public OrderResponse updateCustomersResponse(OrderRequest request) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public OrderResponse getResponse(String orderId) {
		// TODO
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public OrderResponse getServiceResponse(String serviceId) {
		// TODO
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public OrderResponse bookingResponse(String orderId) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public OrderResponse confirmResponse(String orderId) {
		OrderResponse response = new OrderResponse();
		OrderIdModel orderIdModel = new OrderIdModel().create(orderId);
		OrderIdModel newOrderIdModel = new OrderIdModel();
		List<ServiceItem> resultItems = new ArrayList<>(orderIdModel.getIds().size());
		
		// выкупаем заказы и формируем ответ
		for (ServiceIdModel serviceIdModel : orderIdModel.getIds()) {
			try {
				DataItem order = client.confirm(serviceIdModel);
				ServiceItem serviceItem = addServiceItem(resultItems, serviceIdModel, true, null);
				updateServiceItem(serviceItem, order);
				ServiceIdModel newServiceIdModel = updateAndReturnNewServiceId(order, serviceItem);
				newOrderIdModel.getIds().add(newServiceIdModel);
			} catch (ResponseError e) {
				addServiceItem(resultItems, serviceIdModel, false, new RestError(e.getMessage()));
			}
		}
		response.setOrderId(orderId);
		response.setNewOrderId(newOrderIdModel.asString());
		response.setServices(resultItems);
		return response;
	}
	
	private ServiceItem addServiceItem(List<ServiceItem> resultItems, ServiceIdModel idModel, boolean confirmed,
			RestError error) {
		ServiceItem serviceItem = new ServiceItem();
		serviceItem.setId(idModel.asString());
		serviceItem.setConfirmed(confirmed);
		serviceItem.setError(error);
		resultItems.add(serviceItem);
		return serviceItem;
	}
	
	private ServiceIdModel updateAndReturnNewServiceId(DataItem order, ServiceItem serviceItem) {
		ServiceIdModel newServiceIdModel = new ServiceIdModel();
		newServiceIdModel.setId(order.getData().getId());
		serviceItem.setNewId(newServiceIdModel.asString());
		return newServiceIdModel;
	}
	
	private void updateServiceItem(ServiceItem serviceItem, DataItem order) {
		serviceItem.setNumber(order.getData().getAttributes().getMarketingCarrierBookingNumber());
	}

	@Override
	public OrderResponse cancelResponse(String orderId) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public OrderResponse prepareReturnServicesResponse(OrderRequest request) {
		return createOperationResponse(request, (resultItems, serviceIdModel) -> {
			DataItem booking = getBookingIfStatus(serviceIdModel, Data.BOOKINGS_TYPE);
			DataItem cancelInfo = client.getConditions(serviceIdModel.getId());
			checkIfCancellationAllowed(cancelInfo);
			ServiceItem serviceItem = addServiceItem(resultItems, serviceIdModel, true, null);
			int refundAmount = getRefundAmount(booking, cancelInfo);
			addReturnPrice(serviceItem, refundAmount);
		});
	}
	
	private void checkIfCancellationAllowed(DataItem cancelInfo) throws ResponseError {
		if (!cancelInfo.getData().getAttributes().isAllowed()) {
			throw new ResponseError("Return is not allowed");
		}
	}
	
	private int getRefundAmount(DataItem booking, DataItem cancelInfo) {
		return booking.getData().getAttributes().getTotalPrice() - cancelInfo.getData().getAttributes().getFee();
	}
	
	private void addReturnPrice(ServiceItem serviceItem, int refund) {
		Price price = new Price();
		price.setAmount(new BigDecimal(refund).multiply(new BigDecimal("0.01")));
		price.setCurrency(Currency.EUR);
		serviceItem.setPrice(price);
	}

	@Override
	public OrderResponse returnServicesResponse(OrderRequest request) {
		return createOperationResponse(request, (resultItems, serviceIdModel) -> {
			getBookingIfStatus(serviceIdModel, Data.BOOKINGS_TYPE);
			DataItem cancelInfo = client.cancel(serviceIdModel.getId());
			ServiceItem serviceItem = addServiceItem(resultItems, serviceIdModel, true, null);
			addReturnPrice(serviceItem, cancelInfo.getData().getAttributes().getTotalRefund());
		});
	}

	@Override
	public OrderResponse getPdfDocumentsResponse(OrderRequest request) {
		return createOperationResponse(request, (resultItems, serviceIdModel) -> {
			getBookingIfStatus(serviceIdModel, Data.BOOKINGS_TYPE);
			String base64 = client.getTickets(serviceIdModel.getId());
			ServiceItem serviceItem = addServiceItem(resultItems, serviceIdModel, true, null);
			addServiceDocument(serviceItem, base64);
		});
	}
	
	private List<ServiceIdModel> getSelectedServices(OrderRequest request) {
		if (request.getServices() != null
				&& !request.getServices().isEmpty()) {
			return request.getServices().stream().map(s -> new ServiceIdModel().create( s.getId())).collect(Collectors.toList());
		} else {
			return new OrderIdModel().create(request.getOrderId()).getIds();
		}
	}
	
	private void addServiceDocument(ServiceItem serviceItem, String base64) {
		Document document = new Document();
		document.setType(DocumentType.TICKET);
		document.setBase64(base64);
		serviceItem.setDocuments(Collections.singletonList(document));
	}
	
	private DataItem getBookingIfStatus(ServiceIdModel serviceIdModel, String status) throws ResponseError {
		DataItem booking = client.getBooking(serviceIdModel.getId());
		if (!status.equals(booking.getData().getType())) {
			throw new ResponseError("Service is not booked");
		}
		return booking;
	}
	
	private OrderResponse createOperationResponse(OrderRequest request, OperationService service) {
		OrderResponse response = new OrderResponse();
		List<ServiceIdModel> idModels = getSelectedServices(request);
		List<ServiceItem> resultItems = new ArrayList<>(idModels.size());
		for (ServiceIdModel serviceIdModel : idModels) {
			try {
				service.addOperationServiceItem(resultItems, serviceIdModel);
			} catch (ResponseError e) {
				addServiceItem(resultItems, serviceIdModel, false, new RestError(e.getMessage()));
			}
		}
		response.setOrderId(request.getOrderId());
		response.setServices(resultItems);
		return response;
	}
	
	private interface OperationService {
		
		public void addOperationServiceItem(List<ServiceItem> resultItems, ServiceIdModel serviceIdModel) throws ResponseError;
		
	}

}
