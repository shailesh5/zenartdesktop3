package com.jio.asp.gstr1.v30.service;

import java.util.List;
import java.util.Map;

public interface Gstr1ProducerService {

	String invokeConverterApi(String flatPayLoad, Map<String, String> headerMap,String ackNo);
	Map<String, String> saveInvoiceV4(Map<String, Object> requestBody, Map<String, String> header,String ackNo);
	List<Map<String, Object>> convertInputToFlatJsonV4(Map<String, Object> requestBody, String action, String ackNo,
			Map<String, String> reqHeaderMap);
	
	
}
