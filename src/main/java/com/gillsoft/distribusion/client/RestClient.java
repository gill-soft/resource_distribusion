package com.gillsoft.distribusion.client;

import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
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

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.logging.SimpleRequestResponseLoggingInterceptor;
import com.gillsoft.model.Currency;
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
	private static final String BOOKINGS = "/bookings/create";
	private static final String BOOKINGS_SHOW = "/bookings";
	private static final String TICKETS = "/tickets";
	private static final String CANCEL_INFO = "/cancellations/conditions";
	private static final String CANCEL = "/cancellations/create";
	
	public static final String STATIONS_CACHE_KEY = "en.stations";
	public static final String CONNECTIONS_CACHE_KEY = "connections.";
	public static final String PROVIDER_INFO_CACHE_KEY = "provider.info.";
	
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
	
	public DataItems getCachedTrips(String dispatchId, String arrivalId, Date date) throws IOCacheException {
		try {
			return getCachedObject(getConnectionsCacheKey(date, dispatchId, arrivalId),
					new TripsUpdateTask(dispatchId, arrivalId, date));
		} catch (ResponseError e) {
			return null;
		}
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
	
	public DataItem getCachedProviderInfo(String carrierId) throws IOCacheException {
		try {
			return getCachedObject(getProviderInfoCacheKey(carrierId), new ProviderInfoUpdateTask(carrierId));
		} catch (ResponseError e) {
			return null;
		}
	}
	
	public DataItem getProviderInfo(String carrierId) throws ResponseError {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("locale", Lang.EN.name().toLowerCase());
		params.add("currency", Currency.EUR.name());
		return sendRequest(searchTemplate, MARKETING_CARRIERS + "/" + carrierId, HttpMethod.GET, params,
				new ParameterizedTypeReference<DataItem>() {});
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
			checkErrors(response);
			return response.getBody();
		} catch (RestClientException e) {
			LOGGER.error("Send request error", e);
			throw new ResponseError(e.getMessage());
		}
	}
	
	private <T extends ErrorResponse> void checkErrors(ResponseEntity<T> response) throws ResponseError {
		List<Error> errors = response.getBody().getErrors();
		if (errors != null
				&& !errors.isEmpty()) {
			StringBuilder errorMsg = new StringBuilder();
			for (Error error : errors) {
				errorMsg.append(error.getCode()).append(": ").append(error.getTitle()).append("\r\n");
			}
			throw new ResponseError(errorMsg.toString());
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

}
