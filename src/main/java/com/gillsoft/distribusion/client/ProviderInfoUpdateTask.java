package com.gillsoft.distribusion.client;

import com.gillsoft.cache.AbstractUpdateTask;
import com.gillsoft.concurrent.SerializablePoolType;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.ContextProvider;

public class ProviderInfoUpdateTask extends AbstractUpdateTask {

	private static final long serialVersionUID = 901349633618899245L;
	private static final String POOL_NAME = "PROVIDER_INFO_POOL";
	private static final int POOL_SIZE = 100;
	private static final SerializablePoolType poolType = new SerializablePoolType(POOL_SIZE, POOL_NAME);
	
	private String carrierId;

	public ProviderInfoUpdateTask(String carrierId) {
		this.carrierId = carrierId;
	}

	@Override
	public void run() {
		RestClient client = ContextProvider.getBean(RestClient.class);
		try {
			DataItem item = client.getProviderInfo(carrierId);
			writeObjectIgnoreAge(client.getCache(), RestClient.getProviderInfoCacheKey(carrierId), item,
					Config.getCacheProviderInfoUpdateDelay());
		} catch (ResponseError e) {
			
			// ошибку тоже кладем в кэш
			writeObject(client.getCache(), RestClient.getProviderInfoCacheKey(carrierId), e,
					Config.getCacheErrorTimeToLive(), 0, false, true, poolType);
		}
	}

}
