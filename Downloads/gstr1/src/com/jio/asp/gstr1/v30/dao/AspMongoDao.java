/**
 * 
 */
package com.jio.asp.gstr1.v30.dao;

import java.util.List;
import java.util.Map;


/**
 * @author Rohit1.Soni
 *
 */
public interface AspMongoDao {


	void saveInMongo(Object headerMap,String collection);

	void updateInMongo(Map<String, Object> object, String collection, String id, Map<String, Number> incObject);

	List<Map<String, Object>> getMongoData(Map<String, Object> object, String collection);

	List<Map> getSuppliesDataL0 (Map<String, String> allRequestParams, String collection);
	
	public List<Map> getDataForGstn (Map<String, String> allRequestParams, String collection);
}
