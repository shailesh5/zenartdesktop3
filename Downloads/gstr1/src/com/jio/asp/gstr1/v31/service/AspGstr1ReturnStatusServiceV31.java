package com.jio.asp.gstr1.v31.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface AspGstr1ReturnStatusServiceV31 {

	Map<String, Object> validateApiInput(String gstin,String trans_id);
	Map<String, Object> processGstr1Data(Map<String, String> headerData,Map<String, String> request);
	//Map<String, Object> decryptResponse(Map<String, String> headerData, Map<String, Object> gspresponse);

	/**
	 * Method does header data validation like client details from the asp
	 * database.
	 * 
	 * @param headerData
	 *            this object contains request header data, which is needed to
	 *            do GSP call and user validation
	 */
	void validateHeaderInput(Map<String, String> headerData);
	void validateApiInput(Map<String, String> params);
	int UpdateDbForGstnResponse(Map<String, Object> response, String referenceId, Map<String, String> params,
			String toSuccessStatus, String fromStatus);
	void submitFileIpUpdate(Map<String, String> headerData, Map<String, String> params);
	

}
