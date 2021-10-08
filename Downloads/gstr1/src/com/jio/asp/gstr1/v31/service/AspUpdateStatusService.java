package com.jio.asp.gstr1.v31.service;

import java.util.Map;

public interface AspUpdateStatusService {

	boolean processGstinMasterData(Map<String, Object> requestBody, Map<String, String> reqHeaderMap, String ackNo);

	boolean updateGstinMasterData(Map<String, Object> requestBody, Map<String, String> reqHeaderMap, String ackNo);

}
