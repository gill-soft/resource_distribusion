package com.gillsoft.distribusion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.abstract_rest_service.AbstractLocalityService;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.distribusion.client.Data;
import com.gillsoft.distribusion.client.DataItems;
import com.gillsoft.distribusion.client.RestClient;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.request.LocalityRequest;

@RestController
public class LocalityServiceController extends AbstractLocalityService {
	
	public static List<Locality> all;
	
	@Autowired
	private RestClient client;

	@Override
	public List<Locality> getAllResponse(LocalityRequest request) {
		createLocalities();
		return all;
	}

	@Override
	public Map<String, List<String>> getBindingResponse(LocalityRequest request) {
		return null;
	}

	@Override
	public List<Locality> getUsedResponse(LocalityRequest request) {
		createLocalities();
		return all;
	}
	
	@Scheduled(initialDelay = 60000, fixedDelay = 900000)
	public void createLocalities() {
		if (LocalityServiceController.all == null) {
			synchronized (LocalityServiceController.class) {
				if (LocalityServiceController.all == null) {
					boolean cacheError = true;
					do {
						try {
							DataItems dataItems = client.getCachedStations();
							if (dataItems != null) {
								List<Locality> all = createLocalities(dataItems);
								LocalityServiceController.all = all;
							}
							cacheError = false;
						} catch (IOCacheException e) {
							try {
								TimeUnit.MILLISECONDS.sleep(100);
							} catch (InterruptedException ie) {
							}
						}
					} while (cacheError);
				}
			}
		}
	}
	
	private List<Locality> createLocalities(DataItems dataItems) {
		List<Locality> all = new CopyOnWriteArrayList<>();
		Map<String, Locality> cities = createCitiesMap(dataItems);
		for (Data item : dataItems.getData()) {
			if (Objects.equals("stations", item.getType())) {
				Locality locality = createLocality(item);
				if (isPresentCity(item)) {
					locality.setParent(cities.get(item.getRelationships().getCity().getData().getId()));
				}
				all.add(locality);
			}
		}
		return all;
	}
	
	private Map<String, Locality> createCitiesMap(DataItems dataItems) {
		Map<String, Locality> cities = new HashMap<>();
		for (Data item : dataItems.getIncluded()) {
			if (Objects.equals("cities", item.getType())) {
				cities.put(item.getId(), createLocality(item));
			}
		}
		return cities;
	}
	
	private boolean isPresentCity(Data item) {
		return item.getRelationships() != null
				&& item.getRelationships().getCity() != null
				&& item.getRelationships().getCity().getData() != null;
	}
	
	private Locality createLocality(Data item) {
		Locality locality = new Locality();
		locality.setId(item.getId());
		locality.setName(Lang.EN, item.getAttributes().getName());
		locality.setAddress(Lang.EN, getAddress(item));
		locality.setCode(item.getAttributes().getCode());
		locality.setLatitude(item.getAttributes().getLatitude());
		locality.setLongitude(item.getAttributes().getLongitude());
		locality.setTimezone(item.getAttributes().getTimeZone());
		return locality;
	}
	
	private String getAddress(Data item) {
		String address = null;
		if (item.getAttributes().getStreetAndNumber() != null
				&& !item.getAttributes().getStreetAndNumber().isEmpty()) {
			address = item.getAttributes().getStreetAndNumber();
		}
		if (item.getAttributes().getDescription() != null
				&& !item.getAttributes().getDescription().isEmpty()) {
			if (address == null) {
				address = item.getAttributes().getDescription();
			} else {
				address += ". " + item.getAttributes().getDescription();
			}
		}
		return address;
	}
	
	public static Locality getLocality(String id) {
		if (all == null) {
			return null;
		}
		for (Locality locality : all) {
			if (Objects.equals(id, locality.getId())) {
				return locality;
			}
		}
		return null;
	}

}
