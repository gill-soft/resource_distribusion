package com.gillsoft.distribusion.client;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.gillsoft.cache.AbstractUpdateTask;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.ContextProvider;

public class TripsUpdateTask extends AbstractUpdateTask {

	private static final long serialVersionUID = 4112113158534958584L;
	
	private String from;
	private String to;
	private Date date;

	public TripsUpdateTask(String from, String to, Date date) {
		super();
		this.from = from;
		this.to = to;
		this.date = DateUtils.truncate(date, Calendar.DATE);
	}

	@Override
	public void run() {
		RestClient client = ContextProvider.getBean(RestClient.class);
		try {
			DataItems items = client.getTrips(from, to, date);
			writeObject(client.getCache(), RestClient.getConnectionsCacheKey(date, from, to), items,
					getTimeToLive(items), Config.getCacheTripUpdateDelay());
		} catch (ResponseError e) {
			
			// ошибку тоже кладем в кэш
			writeObject(client.getCache(), RestClient.getConnectionsCacheKey(date, from, to), e,
					Config.getCacheErrorTimeToLive(), Config.getCacheErrorUpdateDelay());
		}
	}
	
	// время жизни до момента самого позднего отправления
	private long getTimeToLive(DataItems items) {
		if (Config.getCacheTripTimeToLive() != 0) {
			return Config.getCacheTripTimeToLive();
		}
		long max = 0;
		for (Data item : items.getData()) {
			if (item.isTripAvailable()
					&& item.getAttributes().getDeparture().getTime() > max) {
				max = item.getAttributes().getDeparture().getTime();
			}
		}
		if (max == 0
				|| max < System.currentTimeMillis()) {
			return Config.getCacheErrorTimeToLive();
		}
		return max - System.currentTimeMillis();
	}
	
}
