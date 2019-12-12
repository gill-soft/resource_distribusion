package com.gillsoft.distribusion.client;

import com.gillsoft.cache.AbstractUpdateTask;
import com.gillsoft.concurrent.SerializablePoolType;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.ContextProvider;

public class TypeInfoUpdateTask extends AbstractUpdateTask {

	private static final long serialVersionUID = 5683410506110177402L;
	
	private static final String POOL_NAME = "TYPE_INFO_POOL";
	private static final int POOL_SIZE = 100;
	private static final SerializablePoolType poolType = new SerializablePoolType(POOL_SIZE, POOL_NAME);
	
	private TripIdModel idModel;
	private String typeId;

	public TypeInfoUpdateTask(TripIdModel idModel, String typeId) {
		this.idModel = idModel;
		this.typeId = typeId;
	}

	@Override
	public void run() {
		RestClient client = ContextProvider.getBean(RestClient.class);
		try {
			DataItem item = client.getTypeInfo(idModel, typeId);
			writeObject(client.getCache(), RestClient.getTypeInfoCacheKey(idModel, typeId), item,
					getTimeToLive(), Config.getCacheTypeInfoUpdateDelay(), false, false, poolType);
		} catch (ResponseError e) {
			
			// ошибку тоже кладем в кэш
			writeObject(client.getCache(), RestClient.getTypeInfoCacheKey(idModel, typeId), e,
					Config.getCacheErrorTimeToLive(), Config.getCacheErrorUpdateDelay(), false, false, poolType);
		}
	}
	
	// время жизни до момента отправления
	private long getTimeToLive() {
		if (Config.getCacheTypeInfoTimeToLive() != 0) {
			return Config.getCacheTypeInfoTimeToLive();
		}
		if (idModel.getDeparture() == null
				|| idModel.getDeparture().getTime() < System.currentTimeMillis()) {
			return Config.getCacheErrorTimeToLive();
		}
		return idModel.getDeparture().getTime() - System.currentTimeMillis();
	}

}
