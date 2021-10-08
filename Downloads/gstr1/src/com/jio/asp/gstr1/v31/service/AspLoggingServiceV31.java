package com.jio.asp.gstr1.v31.service;

import java.util.Map;

public interface AspLoggingServiceV31 {

	void generateControlLog(Map<String, Object> controlDataMap);

	void generateFileControlLog(Map<String, Object> controlDataMap);

	void updateControlLog(Map<String, Object> controlDataMap, Map<String, Number> incRowMap, String id);

	void updateControlLogSync(Map<String, Object> controlDataMap, Map<String, Number> incRowMap, String id,
			String collection);

	void generateControlLogSync(Map<String, Object> controlDataMap, String collection);
}
