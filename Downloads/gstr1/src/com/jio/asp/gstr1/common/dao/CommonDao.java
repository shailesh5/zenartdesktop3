package com.jio.asp.gstr1.common.dao;

import java.util.List;
import java.util.Map;

public interface CommonDao {

	List<Map<String, Object>> getMongoData(Map<String, Object> object, String collection);

	void saveInMongo(Object object, String collection);
}
