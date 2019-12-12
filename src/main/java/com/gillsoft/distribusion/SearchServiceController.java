package com.gillsoft.distribusion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.abstract_rest_service.SimpleAbstractTripSearchService;
import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.distribusion.client.Data;
import com.gillsoft.distribusion.client.DataItem;
import com.gillsoft.distribusion.client.DataItems;
import com.gillsoft.distribusion.client.RestClient;
import com.gillsoft.distribusion.client.TripIdModel;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Price;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.RestError;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Route;
import com.gillsoft.model.Seat;
import com.gillsoft.model.SeatsScheme;
import com.gillsoft.model.Segment;
import com.gillsoft.model.SimpleTripSearchPackage;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.Trip;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.util.RestTemplateUtil;
import com.gillsoft.util.StringUtil;

@RestController
public class SearchServiceController extends SimpleAbstractTripSearchService<SimpleTripSearchPackage<DataItems>> {
	
	@Autowired
	private RestClient client;
	
	@Autowired
	@Qualifier("RedisMemoryCache")
	private CacheHandler cache;

	@Override
	public TripSearchResponse initSearchResponse(TripSearchRequest request) {
		return simpleInitSearchResponse(cache, request);
	}
	
	@Override
	public void addInitSearchCallables(List<Callable<SimpleTripSearchPackage<DataItems>>> callables, TripSearchRequest request) {
		callables.add(() -> {
			SimpleTripSearchPackage<DataItems> searchPackage = new SimpleTripSearchPackage<>();
			searchPackage.setRequest(request);
			addConnectionsToCache(searchPackage);
			return searchPackage;
		});
	}
	
	private void addConnectionsToCache(SimpleTripSearchPackage<DataItems> searchPackage) {
		searchPackage.setInProgress(false);
		try {
			TripSearchRequest request = searchPackage.getRequest();
			DataItems dataItems = client.getCachedTrips(request.getLocalityPairs().get(0)[0],
						request.getLocalityPairs().get(0)[1], request.getDates().get(0));
			searchPackage.setSearchResult(dataItems);
		} catch (IOCacheException e) {
			searchPackage.setInProgress(true);
		} catch (ResponseError e) {
			searchPackage.setException(e);
		}
	}
		
	@Override
	public TripSearchResponse getSearchResultResponse(String searchId) {
		return simpleGetSearchResponse(cache, searchId);
	}
	
	@Override
	public void addNextGetSearchCallablesAndResult(List<Callable<SimpleTripSearchPackage<DataItems>>> callables,
			Map<String, Vehicle> vehicles, Map<String, Locality> localities, Map<String, Organisation> organisations,
			Map<String, Segment> segments, List<TripContainer> containers,
			SimpleTripSearchPackage<DataItems> result) {
		if (!result.isInProgress()) {
			addResult(localities, organisations, segments, containers, result);
		} else {
			addInitSearchCallables(callables, result.getRequest());
		}
	}
	
	private void addResult(Map<String, Locality> localities, Map<String, Organisation> organisations,
			Map<String, Segment> segments, List<TripContainer> containers,
			SimpleTripSearchPackage<DataItems> result) {
		TripContainer container = new TripContainer();
		container.setRequest(result.getRequest());
		if (result.getSearchResult() != null) {
			List<Trip> trips = new ArrayList<>();
			Map<String, Data> included = createIncludedMap(result.getSearchResult().getIncluded());
			for (Data connection : result.getSearchResult().getData()) {
				if (connection.isTripAvailable()) {
					initializeProviderInfo(connection);
					Trip trip = new Trip();
					trip.setId(createAndAddSegment(localities, organisations, segments, connection, included));
					trips.add(trip);
				}
			}
			container.setTrips(trips);
		}
		if (result.getException() != null) {
			container.setError(new RestError(result.getException().getMessage()));
		}
		containers.add(container);
	}
	
	private void initializeProviderInfo(Data connection) {
		getProviderInfo(connection);
	}
	
	private DataItem getProviderInfo(Data connection) {
		String carrierId = connection.getRelationships().getMarketingCarrier().getData().getId();
		return getProviderInfo(carrierId);
	}
	
	private DataItem getProviderInfo(String carrierId) {
		try {
			return client.getCachedProviderInfo(carrierId);
		} catch (IOCacheException e) {
		} catch (ResponseError e) {
		}
		return null;
	}
	
	private Map<String, Data> createIncludedMap(List<Data> included) {
		Map<String, Data> includes = new HashMap<>();
		for (Data data : included) {
			if (Objects.equals("stations", data.getType())) {
				includes.put("station_" + data.getId(), data);
			} else if (Objects.equals("cities", data.getType())) {
				includes.put("city_" + data.getId(), data);
			} else if (Objects.equals("marketing_carriers", data.getType())) {
				includes.put("marketing_carrier_" + data.getId(), data);
			} else if (Objects.equals("passenger_types", data.getType())) {
				includes.put("passenger_type_" + data.getId(), data);
			} else if (Objects.equals("segments", data.getType())) {
				includes.put("segment_" + data.getId(), data);
			} else if (Objects.equals("operating_carriers", data.getType())) {
				includes.put("operating_carrier_" + data.getId(), data);
			}
		}
		return includes;
	}
	
	private String createAndAddSegment(Map<String, Locality> localities, Map<String, Organisation> organisations,
			Map<String, Segment> segments, Data connection, Map<String, Data> included) {
		Segment segment = new Segment();
		Data carrier = getCarrier(included, connection);
		segment.setNumber(carrier.getId());
		segment.setCarrier(createAndAddOrganisation(organisations, carrier));
		segment.setDepartureDate(connection.getAttributes().getDeparture());
		segment.setArrivalDate(connection.getAttributes().getArrival());
		segment.setDeparture(createAndAddLocality(localities, connection.getRelationships().getDeparture().getData().getId()));
		segment.setArrival(createAndAddLocality(localities, connection.getRelationships().getArrival().getData().getId()));
		segment.setPrice(createPrice(included, connection));
		
		TripIdModel idModel = new TripIdModel(connection);
		initializeTariffs(idModel, carrier);
		String id = idModel.asString();
		segments.put(new TripIdModel(connection).asString(), segment);
		return id;
	}
	
	private Data getCarrier(Map<String, Data> included, Data connection) {
		String carrierId = connection.getRelationships().getMarketingCarrier().getData().getId();
		return included.get("marketing_carrier_" + carrierId);
	}
	
	private Price createPrice(Map<String, Data> included, Data connection) {
		Tariff tariff = createAdultTariff(included, connection);
		return createPrice(tariff);
	}
	
	public Price createPrice(Tariff tariff) {
		Price price = new Price();
		price.setCurrency(Currency.EUR);
		price.setAmount(tariff.getValue());
		price.setTariff(tariff);
		return price;
	}
	
	private Tariff createAdultTariff(Map<String, Data> included, Data connection) {
		Tariff tariff = new Tariff();
		tariff.setId("0");
		tariff.setName(Lang.EN, "Adult");
		setAdultTariffParams(tariff, included, connection);
		return tariff;
	}
	
	private void setAdultTariffParams(Tariff tariff, Map<String, Data> included, Data connection) {
		tariff.setValue(convert(connection.getAttributes().getCheapestTotalAdultPrice()));
		Data carrier = getCarrier(included, connection);
		for (Data type : carrier.getRelationships().getPassengerTypes().getData()) {
			Data dataType = included.get("passenger_type_" + type.getId());
			if (dataType.isAdultPassengerType()) {
				setTariffParams(tariff, dataType);
				tariff.setReturnConditions(createReturnConditions(connection));
				break;
			}
		}
	}
	
	private void setTariffParams(Tariff tariff, Data dataType) {
		tariff.setId(dataType.getAttributes().getCode());
		tariff.setMaxAge(dataType.getAttributes().getMaxAge());
		tariff.setMinAge(dataType.getAttributes().getMinAge());
		tariff.setName(Lang.EN, dataType.getAttributes().getName());
		tariff.setDescription(Lang.EN, dataType.getAttributes().getDescription());
	}
	
	private BigDecimal convert(int value) {
		// value в центах
		return new BigDecimal(value).multiply(new BigDecimal("0.01"));
	}
	
	private List<ReturnCondition> createReturnConditions(Data connection) {
		DataItem carrierData = getProviderInfo(connection);
		return createReturnConditions(carrierData);
	}
	
	private List<ReturnCondition> createReturnConditions(String carrierId) {
		DataItem carrierData = getProviderInfo(carrierId);
		return createReturnConditions(carrierData);
	}
	
	private List<ReturnCondition> createReturnConditions(DataItem carrier) {
		if (carrier != null) {
			ReturnCondition condition = new ReturnCondition();
			condition.setDescription(Lang.EN, carrier.getData().getAttributes().getTerms());
			return Collections.singletonList(condition);
		}
		return null;
	}
	
	public Organisation createAndAddOrganisation(Map<String, Organisation> organisations, Data carrier) {
		String key = StringUtil.md5(carrier.getAttributes().getTradeName());
		Organisation organisation = organisations.get(key);
		if (organisation == null) {
			organisation = new Organisation();
			organisation.setTradeMark(carrier.getAttributes().getTradeName());
			organisation.setName(Lang.UA, carrier.getAttributes().getLegalName());
			organisations.put(key, organisation);
		}
		return new Organisation(key);
	}
	
	private Locality createAndAddLocality(Map<String, Locality> localities, String id) {
		String key = String.valueOf(id);
		Locality fromDict = LocalityServiceController.getLocality(key);
		if (fromDict == null) {
			return null;
		}
		String fromDictId = fromDict.getId();
		try {
			fromDict = fromDict.clone();
			fromDict.setId(null);
		} catch (CloneNotSupportedException e) {
		}
		Locality locality = localities.get(fromDictId);
		if (locality == null) {
			localities.put(fromDictId, fromDict);
		}
		return new Locality(fromDictId);
	}
	
	private void initializeTariffs(TripIdModel idModel, Data carrier) {
		for (Data dataType : carrier.getRelationships().getPassengerTypes().getData()) {
			getTypeInfo(idModel, dataType.getId());
		}
	}
	
	private DataItem getTypeInfo(TripIdModel idModel, String typeId) {
		try {
			return client.getCachedTypeInfo(idModel, typeId);
		} catch (IOCacheException e) {
		} catch (ResponseError e) {
		}
		return null;
	}

	@Override
	public Route getRouteResponse(String tripId) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public SeatsScheme getSeatsSchemeResponse(String tripId) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public List<Seat> getSeatsResponse(String tripId) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public List<Tariff> getTariffsResponse(String tripId) {
		TripIdModel idModel = new TripIdModel().create(tripId);
		DataItem carrier = getProviderInfo(idModel.getCarrier());
		if (carrier != null) {
			Map<String, Data> included = createIncludedMap(carrier.getIncluded());
			List<Tariff> tariffs = createTariffs(carrier, included);
			for (Iterator<Tariff> iterator = tariffs.iterator(); iterator.hasNext();) {
				Tariff tariff = iterator.next();
				if (!setTariffAmount(idModel, tariff)) {
					iterator.remove();
				}
			}
			return tariffs;
		} else {
			return null;
		}
	}
	
	private List<Tariff> createTariffs(DataItem carrier, Map<String, Data> included) {
		if (carrier != null) {
			List<Tariff> tariffs = new ArrayList<>();
			for (Data type : carrier.getData().getRelationships().getPassengerTypes().getData()) {
				Data dataType = included.get("passenger_type_" + type.getId());
				Tariff tariff = createTariff(dataType);
				tariff.setReturnConditions(createReturnConditions(carrier));
				tariffs.add(tariff);
			}
			return tariffs;
		}
		return null;
	}
	
	private Tariff createTariff(Data dataType) {
		Tariff tariff = new Tariff();
		setTariffParams(tariff, dataType);
		return tariff;
	}
	
	private boolean setTariffAmount(TripIdModel idModel, Tariff tariff) {
		DataItem typeInfo = getTypeInfo(idModel, tariff.getId());
		if (typeInfo != null
				&& typeInfo.getData().getAttributes().isVacant()) {
			tariff.setValue(convert(typeInfo.getData().getAttributes().getTotalPrice()));
			return true;
		}
		return false;
	}
	
	@Override
	public List<RequiredField> getRequiredFieldsResponse(String tripId) {
		List<RequiredField> requiredFields = new ArrayList<>();
		requiredFields.add(RequiredField.NAME);
		requiredFields.add(RequiredField.SURNAME);
		requiredFields.add(RequiredField.PHONE);
		requiredFields.add(RequiredField.EMAIL);
		requiredFields.add(RequiredField.TARIFF);
		return requiredFields;
	}

	@Override
	public List<Seat> updateSeatsResponse(String tripId, List<Seat> seats) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

	@Override
	public List<ReturnCondition> getConditionsResponse(String tripId, String tariffId) {
		TripIdModel idModel = new TripIdModel().create(tripId);
		return createReturnConditions(idModel.getCarrier());
	}

	@Override
	public List<Document> getDocumentsResponse(String tripId) {
		throw RestTemplateUtil.createUnavailableMethod();
	}

}
