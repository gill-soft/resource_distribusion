package com.gillsoft.distribusion.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.logging.SimpleRequestResponseLoggingInterceptor;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Customer;
import com.gillsoft.model.Lang;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.RestTemplateUtil;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RestClient {
	
	private static Logger LOGGER = LogManager.getLogger(RestClient.class);
	
	private static final String STATIONS = "/stations";
	private static final String CONNECTIONS_FIND = "/connections/find";
	private static final String MARKETING_CARRIERS = "/marketing_carriers";
	private static final String VACANCY_SHOW = "/connections/vacancy";
	private static final String ORDERS = "/bookings/create";
	private static final String BOOKINGS_SHOW = "/bookings";
	private static final String TICKETS = "/bookings/{0}/tickets";
	private static final String CANCEL_INFO = "/cancellations/conditions";
	private static final String CANCEL = "/cancellations/create";
	
	public static final String STATIONS_CACHE_KEY = "en.stations";
	public static final String CONNECTIONS_CACHE_KEY = "connections.";
	public static final String PROVIDER_INFO_CACHE_KEY = "provider.info.";
	public static final String TYPE_INFO_CACHE_KEY = "type.info.";
	
	public static final String FULL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";
	
	public static final FastDateFormat fullDateFormat = FastDateFormat.getInstance(FULL_DATE_FORMAT);
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	private HttpHeaders apiKeyHeader;
	
	private RestTemplate template;
	
	// для запросов поиска с меньшим таймаутом
	private RestTemplate searchTemplate;
	
	public RestClient() {
		template = createNewPoolingTemplate(Config.getRequestTimeout());
		searchTemplate = createNewPoolingTemplate(Config.getSearchRequestTimeout());
		apiKeyHeader = createApiKeyHeader();
	}
	
	public RestTemplate createNewPoolingTemplate(int requestTimeout) {
		RestTemplate template = new RestTemplate(new BufferingClientHttpRequestFactory(
				RestTemplateUtil.createPoolingFactory(Config.getUrl(), 300, requestTimeout, true, true)));
		template.setInterceptors(Collections.singletonList(
				new SimpleRequestResponseLoggingInterceptor()));
		return template;
	}
	
	private HttpHeaders createApiKeyHeader() {
		HttpHeaders apiKeyHeader = new HttpHeaders();
		apiKeyHeader.add("Api-Key", Config.getApiKey());
		return apiKeyHeader;
	}
	
	public DataItems getCachedStations() throws IOCacheException {
		try {
			return getCachedObject(STATIONS_CACHE_KEY, new StationsUpdateTask());
		} catch (ResponseError e) {
			return null;
		}
	}
	
	public DataItems getStations() throws ResponseError {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("locale", Lang.EN.name().toLowerCase());
		return sendRequest(template, STATIONS, HttpMethod.GET, params, new ParameterizedTypeReference<DataItems>() {});
	}
	
	public DataItems getCachedTrips(String dispatchId, String arrivalId, Date date) throws IOCacheException, ResponseError {
		return getCachedObject(getConnectionsCacheKey(date, dispatchId, arrivalId),
				new TripsUpdateTask(dispatchId, arrivalId, date));
	}
	
	public DataItems getTrips(String dispatchId, String arrivalId, Date date) throws ResponseError {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("departure_stations[]", dispatchId);
		params.add("arrival_stations[]", arrivalId);
		params.add("departure_date", StringUtil.dateFormat.format(date));
		params.add("pax", "1");
		params.add("locale", Lang.EN.name().toLowerCase());
		params.add("currency", Currency.EUR.name());
		return sendRequest(searchTemplate, CONNECTIONS_FIND, HttpMethod.GET, params,
				new ParameterizedTypeReference<DataItems>() {});
	}
	
	public DataItem getCachedProviderInfo(String carrierId) throws IOCacheException, ResponseError {
		return getCachedObject(getProviderInfoCacheKey(carrierId), new ProviderInfoUpdateTask(carrierId));
	}
	
	public DataItem getProviderInfo(String carrierId) throws ResponseError {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("locale", Lang.EN.name().toLowerCase());
		params.add("currency", Currency.EUR.name());
		return sendRequest(searchTemplate, MARKETING_CARRIERS + "/" + carrierId, HttpMethod.GET, params,
				new ParameterizedTypeReference<DataItem>() {});
	}
	
	public DataItem getCachedTypeInfo(TripIdModel idModel, String typeId) throws IOCacheException, ResponseError {
		return getCachedObject(getTypeInfoCacheKey(idModel, typeId), new TypeInfoUpdateTask(idModel, typeId));
	}
	
	public DataItem getTypeInfo(TripIdModel idModel, String typeId) throws ResponseError {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("departure_station", idModel.getFrom());
		params.add("arrival_station", idModel.getTo());
		params.add("marketing_carrier", idModel.getCarrier());
		params.add("currency", Currency.EUR.name());
		params.add("departure_time", fullDateFormat.format(idModel.getDeparture()));
		params.add("arrival_time", fullDateFormat.format(idModel.getArrival()));
		params.add("passengers[][type]", typeId);
		params.add("passengers[][pax]", "1");
		return sendRequest(searchTemplate, VACANCY_SHOW, HttpMethod.GET, params, new ParameterizedTypeReference<DataItem>() {});
	}
	
	public DataItem getBooking(String bookingId) throws ResponseError {
		return sendRequest(template, BOOKINGS_SHOW + "/" + bookingId, HttpMethod.GET, null, new ParameterizedTypeReference<DataItem>() {});
	}

	public DataItem confirm(ServiceIdModel serviceIdModel) throws ResponseError {
		CreateBookingRequest request = createBookingRequest(serviceIdModel);
		request.setPassengers(createPassengers(serviceIdModel));
		return sendRequest(template, ORDERS, HttpMethod.POST, request, null, new ParameterizedTypeReference<DataItem>() {});
	}
	
	private CreateBookingRequest createBookingRequest(ServiceIdModel serviceIdModel) {
		CreateBookingRequest request = createDefaultBookingRequest();
		Customer customer = serviceIdModel.getCustomer();
		TripIdModel idModel = serviceIdModel.getIdModel();
		request.setPhone(customer.getPhone());
		request.setFirstName(customer.getName());
		request.setLastName(customer.getSurname());
		request.setCarrier(idModel.getCarrier());
		request.setDepartureStation(idModel.getFrom());
		request.setArrivalStation(idModel.getTo());
		request.setDeparture(fullDateFormat.format(idModel.getDeparture()));
		request.setArrival(fullDateFormat.format(idModel.getArrival()));
		request.setTotalPrice(serviceIdModel.getPrice());
		return request;
	}
	
	private CreateBookingRequest createDefaultBookingRequest() {
		CreateBookingRequest request = new CreateBookingRequest();
		request.setStreetAndNumber(Config.getBookingStreet());
		request.setZipCode(Config.getBookingZipCode());
		request.setCity(Config.getBookingCity());
		request.setEmail(Config.getBookingEmail());
		request.setTitle("mr");
		request.setTermsAccepted(true);
		request.setLocale(Lang.EN.name().toLowerCase());
		request.setCurrency(Currency.EUR.name());
		request.setExecutePayment(false);
		request.setSendCustomerEmail(false);
		request.setPartnerNumber(Config.getPartnerNumber());
		request.setPaymentMethod("demand_note");
		request.setPax(1);
		return request;
	}
	
	private List<Passenger> createPassengers(ServiceIdModel serviceIdModel) {
		Passenger passenger = new Passenger();
		Customer customer = serviceIdModel.getCustomer();
		passenger.setFirstName(customer.getName());
		passenger.setLastName(customer.getSurname());
		passenger.setType(serviceIdModel.getTypeId());
		return Collections.singletonList(passenger);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getCachedObject(String key, Runnable task) throws IOCacheException, ResponseError {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, key);
		params.put(RedisMemoryCache.UPDATE_TASK, task);
		Object cached = cache.read(params);
		if (cached == null) {
			return null;
		}
		if (cached instanceof ResponseError) {
			throw (ResponseError) cached;
		}
		return (T) cached;
	}
	
	private <T extends ErrorResponse> T sendRequest(RestTemplate template, String uriMethod, HttpMethod httpMethod,
			MultiValueMap<String, String> params,  ParameterizedTypeReference<T> typeReference) throws ResponseError {
		return sendRequest(template, uriMethod, httpMethod, null, params, typeReference);
	}
	
	private <T extends ErrorResponse> T sendRequest(RestTemplate template, String uriMethod, HttpMethod httpMethod, Object request,
			MultiValueMap<String, String> params,  ParameterizedTypeReference<T> typeReference) throws ResponseError {
		URI uri = UriComponentsBuilder.fromUriString(Config.getUrl() + uriMethod).queryParams(params).build().toUri();
		RequestEntity<Object> requestEntity = new RequestEntity<>(request, apiKeyHeader, httpMethod, uri);
		try {
			ResponseEntity<T> response = template.exchange(requestEntity, typeReference);
			T body = response.getBody();
			checkErrors(body);
			return body;
		} catch (RestClientException e) {
			LOGGER.error("Send request error", e);
			throw new ResponseError(e.getMessage());
		}
	}
	
	private <T extends ErrorResponse> void checkErrors(T response) throws ResponseError {
		List<Error> errors = response.getErrors();
		if (errors != null
				&& !errors.isEmpty()) {
			StringBuilder errorMsg = new StringBuilder();
			for (Error error : errors) {
				errorMsg.append(error.getCode()).append(": ").append(error.getTitle()).append("\r\n");
			}
			throw new ResponseError(errorMsg.toString());
		}
	}
	
	public String getTickets(String bookingId) throws ResponseError {
		URI uri = UriComponentsBuilder.fromUriString(Config.getUrl()
				+ MessageFormat.format(TICKETS, bookingId)).build().toUri();
		RequestEntity<Object> requestEntity = new RequestEntity<>(null, apiKeyHeader, HttpMethod.GET, uri);
		ResponseEntity<Resource> response = template.exchange(requestEntity, Resource.class);
		if (response.getStatusCode().is2xxSuccessful()) { 
			try {
				InputStream in = response.getBody().getInputStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buffer = new byte[256];
				while (in.read(buffer) != -1) {
					out.write(buffer);
				}
				checkErrors(out.toByteArray());
				return StringUtil.toBase64(out.toByteArray());
			} catch (IOException e) {
				throw new ResponseError(e.getMessage());
			}
		} else {
			throw new ResponseError("Get ticket " + bookingId + " error: " + response.getStatusCodeValue());
		}
	}
	
	private void checkErrors(byte[] bytes) throws ResponseError {
		ObjectReader reader = new ObjectMapper().readerFor(new TypeReference<ErrorResponse>() { });
		try {
			ErrorResponse response = reader.readValue(bytes);
			checkErrors(response);
		} catch (IOException e) {
		}
	}

	public CacheHandler getCache() {
		return cache;
	}
	
	public static String getConnectionsCacheKey(Date date, String from, String to) {
		return CONNECTIONS_CACHE_KEY + String.join(";",
				String.valueOf(DateUtils.truncate(date, Calendar.DATE).getTime()), from, to);
	}
	
	public static String getProviderInfoCacheKey(String carrierId) {
		return CONNECTIONS_CACHE_KEY + carrierId;
	}
	
	public static String getTypeInfoCacheKey(TripIdModel idModel, String typeId) {
		return TYPE_INFO_CACHE_KEY + String.join(";",
				typeId, idModel.getCarrier(), idModel.getFrom(), idModel.getTo(),
				String.valueOf(idModel.getDeparture().getTime()),
				String.valueOf(idModel.getArrival().getTime()));
	}

}
