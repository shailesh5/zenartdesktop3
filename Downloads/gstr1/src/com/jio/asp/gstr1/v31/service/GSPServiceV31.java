package com.jio.asp.gstr1.v31.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface GSPServiceV31 {

	/**
	 * Call to GSP system, using resttemplate.
	 * 
	 * @param gspHeaderData,
	 *            GSP header data
	 * @param url,
	 *            end point url which needs to be called
	 * @return response map
	 */
	Map<String, String>  getGstr1Status(Map<String, String> headerData,Map<String, String> allRequestParams, String referenceId);

	/**
	 * Call to GSP system, using resttemplate.
	 * 
	 * @param headerData,
	 *            GSP header data
	 * 
	 * @param requestData
	 *            Data should be send to GSP
	 * 
	 * @param url,
	 *            end point url which needs to be called
	 * @return response map
	 */
	Map<String, Object> postGstr1(Map<String, String> headerData, Map<String, Object> requestData);

	Map<String, String> saveGstr1(Map<String, String> headerData, String payLoad);

	Map<String, String> getGstr1Temp(Map<String, String> headerData, String gstin, String refId, String retPeriod);
	
	public Map<String, Object> decryptResponse(Map<String, String> headerData, Map<String, String> gspresponse);


	Map<String, String> getL2(Map<String, String> data, Map<String, String> headerData);

	Map<String, String> getL0(Map<String, String> data, Map<String, String> headers);


	Map<String, String> getBulkUrl(Map<String, String> data, Map<String, String> headerData, String token);

}
