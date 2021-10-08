package com.jio.asp.gstr1.v31.service;

import java.util.List;
import java.util.Map;

public interface AspGstr1SubmitServiceV31 {

	Map<String, Object> validateApiInput(Map<String, Object> request);

	//Map<String, Object> processGstr1Data(Map<String, String> headerData, Map<String, Object> request);
	
	Map<String, Object> processGstr1Data(Map<String, String> headerData, Map<String, Object> request,Map<String,String> allRequestParams);

	Map<String, Object> decryptResponse(Map<String, String> headerData, Map<String, Object> gspresponse);

	Map<String, Object> encryptRequest(Map<String, String> headerData, Map<String, Object> request);

	/**
	 * Method does header data validation like client details from the asp
	 * database.
	 * 
	 * @param headerData
	 *            this object contains request header data, which is needed to
	 *            do GSP call and user validation
	 */
	void validateHeaderInput(Map<String, String> headerData);

	void saveTransactionDetails(Map<String, String> headerData, Map<String, Object> response);

//	void updateSubmitStatusInDb(String collection, Map<String, Object> map, Map<String, String> allRequestParams,
//			Map<String, String> reqHeaderMap, String reference_id);

	void createSaveGstnStatusData(List<Object> refIdsRespone, String ackNumber, String type, String action,
			String gstin, String fp);

	void updateSubmitStatusInDb(String collection, Map<String, String> allRequestParams, String reference_id);
}
