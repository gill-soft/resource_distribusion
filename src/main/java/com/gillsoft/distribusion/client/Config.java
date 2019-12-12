package com.gillsoft.distribusion.client;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class Config {
	
	private static Properties properties;
	
	static {
		try {
			Resource resource = new ClassPathResource("resource.properties");
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getUrl() {
		return properties.getProperty("url");
	}
	
	public static String getPartnerNumber() {
		return properties.getProperty("partner.number");
	}
	
	public static String getApiKey() {
		return properties.getProperty("api.key");
	}
	
	public static String getBookingStreet() {
		return properties.getProperty("booking.street");
	}
	
	public static String getBookingZipCode() {
		return properties.getProperty("booking.zipCode");
	}
	
	public static String getBookingEmail() {
		return properties.getProperty("booking.email");
	}
	
	public static String getBookingCity() {
		return properties.getProperty("booking.city");
	}
	
	public static int getRequestTimeout() {
		return Integer.valueOf(properties.getProperty("request.timeout"));
	}
	
	public static int getSearchRequestTimeout() {
		return Integer.valueOf(properties.getProperty("request.search.timeout"));
	}
	
	public static long getCacheTripTimeToLive() {
		return Long.valueOf(properties.getProperty("cache.trip.time.to.live"));
	}
	
	public static long getCacheTripUpdateDelay() {
		return Long.valueOf(properties.getProperty("cache.trip.update.delay"));
	}
	
	public static long getCacheErrorTimeToLive() {
		return Long.valueOf(properties.getProperty("cache.error.time.to.live"));
	}
	
	public static long getCacheErrorUpdateDelay() {
		return Long.valueOf(properties.getProperty("cache.error.update.delay"));
	}
	
	public static long getCacheStationsUpdateDelay() {
		return Long.valueOf(properties.getProperty("cache.stations.update.delay"));
	}
	
	public static long getCacheProviderInfoUpdateDelay() {
		return Long.valueOf(properties.getProperty("cache.provider.info.update.delay"));
	}
	
	public static long getCacheTypeInfoTimeToLive() {
		return Long.valueOf(properties.getProperty("cache.type.info.time.to.live"));
	}
	
	public static long getCacheTypeInfoUpdateDelay() {
		return Long.valueOf(properties.getProperty("cache.type.info.update.delay"));
	}
	
}
