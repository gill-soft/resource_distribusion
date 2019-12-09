package com.gillsoft.distribusion.client;

import java.util.HashMap;
import java.util.Map;

import com.gillsoft.cache.AbstractUpdateTask;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.ContextProvider;

public class StationsUpdateTask extends AbstractUpdateTask {
	
	private static final long serialVersionUID = -5623023690858406188L;

	@Override
	public void run() {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, RestClient.STATIONS_CACHE_KEY);
		params.put(RedisMemoryCache.IGNORE_AGE, true);
		params.put(RedisMemoryCache.UPDATE_DELAY, Config.getCacheStationsUpdateDelay());
		
		RestClient client = ContextProvider.getBean(RestClient.class);
		try {
			DataItems stations = null;
			try {
				stations = client.getStations();
			} catch (ResponseError e) {
			}
			if (stations == null) {
				stations = (DataItems) client.getCache().read(params);
			}
			params.put(RedisMemoryCache.UPDATE_TASK, this);
			client.getCache().write(stations, params);
		} catch (IOCacheException e) {
		}
	}

}
