package com.jio.asp.gstr1.v31.service;

import java.util.List;
import java.util.Map;

public interface Gstr1ProducerServiceV31 {

	String invokeConverterApi(String flatPayLoad, Map<String, String> headerMap, String ackNo);

	Map<String, String> saveInvoice(Map<String, Object> requestBody, Map<String, String> header, String ackNo);

	List<Map<String, Object>> convertInputToFlatJson(Map<String, Object> requestBody, String action, String ackNo,
			Map<String, String> reqHeaderMap);

	String getFY(String fp);

	String getYear(String oidt);
}
