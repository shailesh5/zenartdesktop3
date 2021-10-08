package com.jio.asp.gstr1.v31.service;

import java.util.List;
import java.util.Map;

public interface AspGstr1FileServiceV31 {

	Map<String, Object> validateApiInput(Map<String, Object> request);

	Map<String, Object> processGstr1Data(Map<String, String> headerData, Map<String, Object> request ,Map<String,String> allRequestParams);

	Map<String, Object> decryptResponse(Map<String, String> headerData, Map<String, Object> gspresponse);

	Map<String, Object> encryptRequest(Map<String, String> headerData, Map<String, Object> request);

	void updateFileStatusInDb(String collection, Map<String, String> allRequestParams, String reference_id);

	void createSaveGstnStatusData(List<Object> refIdsRespone, String ackNumber, String type, String action,
			String gstin, String fp);
}
