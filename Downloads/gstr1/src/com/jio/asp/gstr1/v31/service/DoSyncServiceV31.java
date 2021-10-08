package com.jio.asp.gstr1.v31.service;

import java.util.Map;

public interface DoSyncServiceV31 {

	public Map<String, Object> getDataFromGstn(Map<String, String> requestHeaders, Map<String, String> requestParams, String level, String flushStatus); 
	
	public void validateParams(String gstin, String fp, String sectionType,String flush);

	public Map<String,Object> getL2DataFromJioGST(Map<String, String> headers, Map<String, String> data);
	
	public Map<String,Object> convertMapJioGstL0(Map<String,Object> jioGstMap);
	
	public Map<String,Object> convertMapGstnL0(Map<String,Object> gstnMap);

	Map<String, Object> compareL0(Map<String, Object> jioGstMap, Map<String, Object> gstnMap,
			Map<String, String> requestParam,Map<String, String> header);

	void updateSuppliesDataForSync(Map<String, String> headers, Map<String, String> comparedData, String gstin,
			String fp, String section);

	void validateParams(Map<String, String> allRequestParams);

	Map<String, Object> doSync(Map<String, String> requestHeaders, Map<String, String> requestParams);

	Map<String, Object> getSyncL2Buckets(Map<String, String> syncStatusMap, String gstin, String fp, String section,
			String flushStatus);


	
	}
