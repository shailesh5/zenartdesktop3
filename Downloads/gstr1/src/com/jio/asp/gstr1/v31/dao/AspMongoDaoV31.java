/**
 * 
 */
package com.jio.asp.gstr1.v31.dao;

import java.util.List;
import java.util.Map;


/**
 * @author Rohit1.Soni
 *
 */
public interface AspMongoDaoV31 {


	void saveInMongo(Object headerMap,String collection);
	
	/*List<String> getAllReferIds(String ackNumber, String collection);*/

	void updateInMongo(Map<String, Object> object, String collection, String id, Map<String, Number> incObject);
	
	public void updateStatusInMongo(Map<String, Object> object, String collection,
			Map<String, String> allRequestParams);
	
	void updateStatusInMongoInBatches(Map<String, Object> object, String collection, String inputKey,  List<String> idList, String status);

	List<Map<String, Object>> getMongoData(Map<String, Object> object, String collection);

	List<Map> getSuppliesDataL0 (Map<String, String> allRequestParams, String collection);
	
	List<Map> getSuppliesDataL0V1 (Map<String, String> allRequestParams, String collection);
	
	List<Map<String, Object>> getDataForGstn (Map<String, String> allRequestParams, String collection);
	
//	long getRecordsCount (Map<String, String> allRequestParams, String collection);
	
//	List<Map<String,Object>> getDataForGstnInBatches (Map<String, String> allRequestParams, long offset, long pageSize, String collection);
//
//	List<Map<String, Object>> getDataForGstnInBatches(Map<String, String> allRequestParams, long offset, long pageSize,
//			String collection, boolean queryType);

	long getRecordsCount(Map<String, String> allRequestParams, String collection, List<String> sectionList);

	void updateStatusInMongoInBatches(Map<String, Object> object, String collection, Map<String, Object> whereMap);

	Map<String, Object> getL2Data(Map<String, String> allRequestParams, String collection);

	boolean getKeyStatus(Map<String, String> data, String collectionControl);

	void deleteL2Key(String mongoL2Key, String collection, String collectionControl);
	
	//by MJ
	public void updateSubmitStatusInMongo(Map<String, Object> object, String collection,
			Map<String, String> allRequestParams);
	public void updateFileStatusInMongo(Map<String, Object> object, String collection,
			Map<String, String> allRequestParams);

	void updateMongoWithPush(Map<String, Object> object, String collection, Map<String, Object> whereMap);
	
	void deleteRecords(Map<String, Object> object, String collection);

	void deleteMongoWithPush(List<String> object, String collection, Map<String, Object> whereMap);

	List<Map<String, Object>> getDataForGstnInBatches(Map<String, String> allRequestParams, long offset, long pageSize,
			String collection, boolean queryType, String section);

	List<Map<String, Object>> getDataForGstnInBatchesByIds(Map<String, String> allRequestParams, List<String> ids,
			boolean isAggregate, String section, String collection);

	Map<String, List<String>> getIdsForSaveToGstn(Map<String, Object> matchMap, List<String> fieldNames,
			String collection, String gstin);
	
	List<Map<String, Object>> getMongoData(Map<String, Object> whrObj, String[] fields, String collection);
	
	List<Map<String, Object>> getMongoData(List<Map<String, Object>> whrObj, String[] fields, String collection, Map<String, Object> whrObj1);
	 public long getCount(String mongoL0Key, String collectionName);

	Map<String, Object> getSyncGstnL0DataFromDb(String collectionName, String gstin, String fp);

	Map<String, Object> getSyncGstnL2DataFromDb(String collectionName, String gstin, String section, String fp);

	Map<String, Object> getSyncGstnInvoiceData(String collectionName, String gstin, String section, String fp,
			String inum);

	void saveBatchDataInMongo(List<Map<String, Object>> dataList, String collection);
}
