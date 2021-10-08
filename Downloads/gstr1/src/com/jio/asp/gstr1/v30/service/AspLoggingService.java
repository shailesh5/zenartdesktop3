package com.jio.asp.gstr1.v30.service;

import java.util.Map;

public interface AspLoggingService {

	void generateControlLog(Map<String, Object> controlDataMap);
	
	void generateFileControlLog(Map<String, Object> controlDataMap);

	void updateControlLog(Map<String, Object> controlDataMap, Map<String, Number> incRowMap, String id);
}
