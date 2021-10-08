package com.jio.asp.gstr1.v31.service;

import java.util.Map;

public interface SaveSuppliesToGstnServiceV31 {

	public String processSuppliesDataInBatches(Map<String, String> allRequestParams, Map<String, String> reqHeaderMap,
			Map<String, String> reqHeaderBody);

	void initiateSavetoGstn(Map<String, String> reqMap, Map<String, String> headerMap, String ackNo,
			Map<String, String> payload);

	String saveSuppliesDataInGstn(Map<String, String> allRequestParams, Map<String, String> reqHeaderMap,
			Map<String, String> reqHeaderBody, String ackNumber);
}
