package com.jio.asp.gstr1.v30.service;

import java.util.Map;

public interface AspGstr1SubmitService {

	Map<String, Object> validateApiInput(Map<String, Object> request);
	
	Map<String, Object> processGstr1Data(Map<String, String> headerData, Map<String,Object> request);
	
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
	
	void saveTransactionDetails(Map<String, String> headerData,Map<String,Object> response);
}

