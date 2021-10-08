package com.jio.asp.gstr1.v31.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.exception.AspExceptionV31;
import com.jio.asp.gstr1.v31.exception.GspExceptionV31;
import com.jio.asp.gstr1.v31.exception.RestExceptionHandlerV31;
import com.jio.asp.gstr1.v31.util.AESEncryptionV31;
import com.jio.asp.gstr1.v31.util.BodyV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.gstr1.v31.util.HmacGeneratorV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;

@Service
public class GSPServiceV31Impl implements GSPServiceV31 {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private MessageSource gstnResource;

	@Autowired
	private MessageSource messageSourceV31;

	private static List<String> filterList;

	static {
		filterList = new ArrayList<>();
		filterList.add(Gstr1ConstantsV31.HEADER_CLIENT_ID);
		filterList.add(Gstr1ConstantsV31.HEADER_SECRET);
	}

	Logger log = LoggerFactory.getLogger(GSPServiceV31Impl.class);

	/**
	 * this method is used to call gsp url, this is a generic method which will
	 * be used for all the calls. for all the method types like POST, GET, PUT
	 * etc.
	 * 
	 * @param url,
	 *            url to be called
	 * @param gspHeaders,
	 *            gsp header required
	 * @param method
	 *            http method, get, post, put
	 * @param body,
	 *            body object will be needed if caller wants to pass body in the
	 *            request. mainly it will needed in method type POST and PUT.
	 *            e.g. code, If the type of object to be passed in entity is
	 *            String BodyV31<String> body =new BodyV31<>();
	 *            body.setRequestBody(<value of body to be passed>);
	 * @return Response map containing results.
	 */
	private <T> Map<String, Object> callGsp(String url, Map<String, String> gspHeaders, HttpMethod method,
			BodyV31<T> body) {
		log.debug("callGsp method : START");
		HttpHeaders headers = new HttpHeaders();
		headers.setAll(gspHeaders);
		HttpEntity<T> entity;
		if ((HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method)) && body != null) {
			entity = new HttpEntity<>(body.getRequestBody(), headers);
		} else {
			entity = new HttpEntity<>(headers);
		}

		SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();

		int readTimeOut = Integer
				.parseInt(gstnResource.getMessage("gstn-read-timeout", null, LocaleContextHolder.getLocale()));
		int connectionTimeOut = Integer
				.parseInt(gstnResource.getMessage("gstn-connection-timeout", null, LocaleContextHolder.getLocale()));

		rf.setReadTimeout(readTimeOut);
		rf.setConnectTimeout(connectionTimeOut);
		log.debug("callGsp method : just before calling the GSP for GSTN data");
		log.info("callGsp method : just before calling the GSP for GSTN data");

		restTemplate.setErrorHandler(new RestExceptionHandlerV31());
		ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {
		};
		ResponseEntity<Map<String, Object>> main = restTemplate.exchange(url, method, entity, typeRef);
		Map<String, Object> responseMap = null;
		if (main != null && main.getStatusCode() == HttpStatus.OK && main.hasBody()) {
			responseMap = main.getBody();
		}
		log.debug("callGsp method : END");
		return responseMap;
	}

	/**
	 * common method to prepare the header required for GSP call.
	 * 
	 * @param headerData,
	 *            header data received
	 * @return header map
	 */
	/*
	 * private Map<String, String> prepareHeader(Map<String, String> headerData,
	 * String action) { log.debug("prepareHeader method : START"); String apiVer
	 * = gstnResource.getMessage(Gstr1ConstantsV31.GSP_HEADER_API_VER, null,
	 * LocaleContextHolder.getLocale()); String aspId =
	 * gstnResource.getMessage(Gstr1ConstantsV31.GSP_HEADER_ASP_ID, null,
	 * LocaleContextHolder.getLocale()); String aspLicKey =
	 * gstnResource.getMessage(Gstr1ConstantsV31.GSP_HEADER_ASP_LICENSE_KEY,
	 * null, LocaleContextHolder.getLocale()); Map<String, String> gspHeaders =
	 * new HashMap<>();
	 * 
	 * gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_CONENT_TYPE,
	 * "application/json"); gspHeaders.put(Gstr1ConstantsV31.HEADER_USER_NAME,
	 * headerData.get(Gstr1ConstantsV31.HEADER_USER_NAME));
	 * gspHeaders.put(Gstr1ConstantsV31.HEADER_STATE_CODE,
	 * headerData.get(Gstr1ConstantsV31.HEADER_STATE_CODE));
	 * gspHeaders.put(Gstr1ConstantsV31.HEADER_AUTH_TOKEN,
	 * headerData.get(Gstr1ConstantsV31.HEADER_AUTH_TOKEN));
	 * gspHeaders.put(Gstr1ConstantsV31.HEADER_IP,
	 * headerData.get(Gstr1ConstantsV31.HEADER_IP));
	 * gspHeaders.put(Gstr1ConstantsV31.HEADER_TXN,
	 * headerData.get(Gstr1ConstantsV31.HEADER_TXN));
	 * gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_ASP_ID, aspId);
	 * gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_ASP_LICENSE_KEY, aspLicKey);
	 * gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_DEVICE_STRING,
	 * headerData.get(Gstr1ConstantsV31.HEADER_SRC_DEV));
	 * gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_API_VER, apiVer);
	 * gspHeaders.put(Gstr1ConstantsV31.INPUT_GSTN_ACTION, action);
	 * gspHeaders.put(Gstr1ConstantsV31.INPUT_GSTN,
	 * headerData.get(Gstr1ConstantsV31.INPUT_GSTN));
	 * gspHeaders.put(Gstr1ConstantsV31.INPUT_GSTN_RET_PER,
	 * headerData.get(Gstr1ConstantsV31.INPUT_GSTN_RET_PER));
	 * 
	 * log.debug("prepareHeader method : END"); return gspHeaders; }
	 */

	@Override
	public Map<String, String> getGstr1Status(Map<String, String> headerData, Map<String, String> allRequestParams, String referenceId) {

		String gspGstr1Baseurl = gstnResource.getMessage("url.gsp.gstr1.retstatusurl", null,
				LocaleContextHolder.getLocale());
		Map<String, String> responseMap=null;
		log.info("GSTR1 Return Status API call - Preparing URL for GSP - STEP2");

		StringBuilder baseUrl = new StringBuilder(gspGstr1Baseurl).append("?action=").append(Gstr1ConstantsV31.TYPE_STATUS)
				.append("&gstin=").append(allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN))
				.append("&ret_period=").append(headerData.get(Gstr1ConstantsV31.INPUT_GSTN_RET_PER))
				.append("&ref_id=").append(referenceId);

		// preparing gsp header data needed for GSP call.
		log.info("GSTR1 Return Status API call -  Preparing Header data for GSP - STEP3");
		Map<String, String> gspHeaders = prepareHeader(headerData, Gstr1ConstantsV31.TYPE_STATUS);

		Map<String, Object> tempMap = callGsp(baseUrl.toString(), gspHeaders, HttpMethod.GET, null);
		
		if (tempMap != null && tempMap.containsKey(Gstr1ConstantsV31.RESP_STATUS_CODE)
				&& Gstr1ConstantsV31.RESP_SUCCESS_CODE.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE))) {
			responseMap = tempMap.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
					.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
		} else {
			log.debug("getGstr1Temp method : Error response received from GSTN, processing it");
			generateException(tempMap);
		}

		return responseMap;
	}
	
	public Map<String, Object> decryptResponse(Map<String, String> headerData, Map<String, String> gspresponse) {

		byte[] SessionKeyInBytes = null;
		byte[] rekbytes = null;
		Map<String, Object> responseMap = new HashMap<>();
		Map<String, Object> dataMap = new HashMap<>();

		try {

			log.info("GSTR1 Return Status API call - Decrypting data received from GSP - STEP4");
			// Decryption of Session Key
			SessionKeyInBytes = AESEncryptionV31.decrypt(headerData.get(Gstr1ConstantsV31.INPUT_SEK),
					AESEncryptionV31.decodeBase64StringTOByte(headerData.get(Gstr1ConstantsV31.INPUT_APP_KEY)));

			// Decryption of rek
			rekbytes = AESEncryptionV31.decrypt(gspresponse.get(Gstr1ConstantsV31.INPUT_REK).toString(), SessionKeyInBytes);

			CommonUtilV31.validateEmptyString(MapUtils.getString(gspresponse, Gstr1ConstantsV31.INPUT_DATA),
					ErrorCodesV31.GSP_NULL_RESPONSE, Gstr1ConstantsV31.ASP_GSTR1_RETURN_STATUS_GRP, "GSTR1_GSTN_BODY_EMPTY", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
			
			CommonUtilV31.validateEmptyString(MapUtils.getString(gspresponse, Gstr1ConstantsV31.INPUT_REK),
					ErrorCodesV31.GSP_NULL_RESPONSE, Gstr1ConstantsV31.ASP_GSTR1_RETURN_STATUS_GRP, "GSTR1_GSTN_BODY_EMPTY", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
			
			// Decryption of data from gsp
			String jsonData = new String(AESEncryptionV31.decodeBase64StringTOByte(
					new String(AESEncryptionV31.decrypt(gspresponse.get(Gstr1ConstantsV31.INPUT_DATA).toString(), rekbytes))));
			Gson gson = new Gson();

			responseMap = gson.fromJson(jsonData, Map.class);
			
			String jsonBody = gson.toJson(responseMap);
			String hmac = HmacGeneratorV31.getHmac(jsonBody, rekbytes);
			
//			if(!hmac.equals(gspresponse.get(Gstr1Constants.INPUT_HMAC).toString()))
//			{
//				responseMap.put(Gstr1Constants.ERROR_CODE, ErrorCodes.HMAC_MISMATCH_ERROR);
//				responseMap.put(Gstr1Constants.ERROR_DESC, "Invalid data received data from GSP");
//				responseMap.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GSTR1_STATUS_GRP);
//				throw new AspException("Invalid data received data from GSP", null, false, false, responseMap);
//			}
			
			log.info("GSTR1 Return Status API call - Successful decryption of data from GSP - STEP5");
			
			if (gspresponse.containsKey(Gstr1ConstantsV31.INPUT_SIGN)) {
				
				dataMap.put(Gstr1ConstantsV31.INPUT_DATA, responseMap);
				
				CommonUtilV31.validateEmptyString(MapUtils.getString(gspresponse, Gstr1ConstantsV31.INPUT_SIGN),
						ErrorCodesV31.GSP_NULL_RESPONSE, Gstr1ConstantsV31.ASP_GSTR1_RETURN_STATUS_GRP, "GSTR1_GSTN_BODY_EMPTY", null,
						HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
				
				dataMap.put(Gstr1ConstantsV31.INPUT_SIGN, gspresponse.get(Gstr1ConstantsV31.INPUT_SIGN));
				return dataMap;
			}
		} catch (Exception e) {
			responseMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.DATA_DECRYPT_ERROR);
			responseMap.put(Gstr1ConstantsV31.ERROR_DESC, "Error in decryption of data from GSP");
			responseMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_RETURN_STATUS_GRP);
			throw new AspExceptionV31("Error in decryption of data from GSP", null, false, false, responseMap);
		}
		return responseMap;
	}
	
	@Override
	public Map<String, String> getGstr1Temp(Map<String, String> headerData, String gstin, String refId,
			String retPeriod) {

		String gspGstr1Baseurl = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_GSTR1_URL, null,
				LocaleContextHolder.getLocale());
		Map<String, String> responseMap = null;
		log.info("GSTR1 Return Status API call - Preparing URL for GSP - STEP2");

		StringBuilder baseUrl = new StringBuilder(gspGstr1Baseurl).append("?action=")
				.append(Gstr1ConstantsV31.TYPE_STATUS).append("&gstin=").append(gstin).append("&ref_id=").append(refId)
				.append("&ret_period=").append(retPeriod);

		// preparing gsp header data needed for GSP call.
		log.info("GSTR1 Return Status API call -  Preparing Header data for GSP - STEP3");
		Map<String, String> gspHeaders = prepareHeader(headerData, Gstr1ConstantsV31.TYPE_STATUS);
		gspHeaders.put(Gstr1ConstantsV31.INPUT_GSTN_RET_PER, retPeriod);

		Map<String, Object> tempMap = callGsp(baseUrl.toString(), gspHeaders, HttpMethod.GET, null);
		log.debug("getGstr1Temp method : converting GSTN data to map of string");
		log.info("getGstr1Temp method : converting GSTN data to map of string");
		if (tempMap != null && tempMap.containsKey(Gstr1ConstantsV31.RESP_STATUS_CODE)
				&& Gstr1ConstantsV31.RESP_SUCCESS_CODE.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE))) {
			responseMap = tempMap.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
					.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
		} else {
			log.debug("getGstr1Temp method : Error response received from GSTN, processing it");
			generateException(tempMap);
		}
		log.debug("getGstr1Temp method : END");
		return responseMap;
	}

	@Override
	public Map<String, Object> postGstr1(Map<String, String> headerData, Map<String, Object> requestData) {

		log.info(requestData.get(Gstr1ConstantsV31.INPUT_GSTN_ACTION)
				+ " API call - Preparing Header data and making call to GSP - STEP5");
		String gspGstr1Baseurl = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_GSTR1_URL, null,
				LocaleContextHolder.getLocale());

		// preparing gsp header data needed for GSP call.
		Map<String, String> gspHeaders = prepareHeader(headerData,
				requestData.get(Gstr1ConstantsV31.INPUT_GSTN_ACTION).toString());

		// to set body
		JSONObject jsonObject2 = new JSONObject(requestData);
		BodyV31<String> body = new BodyV31<>();
		body.setRequestBody(jsonObject2.toString());

		Map<String, Object> tempMap = callGsp(gspGstr1Baseurl.toString(), gspHeaders, HttpMethod.POST, body);

		log.info(requestData.get(Gstr1ConstantsV31.INPUT_GSTN_ACTION)
				+ " API call - Data received from GSP Successfully - STEP6");

		return tempMap;
	}

	/**
	 * Call to GSP system, using resttemplate.
	 * 
	 * @param gspHeaderData,
	 *            GSP header data
	 * @param url,
	 *            end point url which needs to be called
	 * @return response map
	 */
	@Override
	public Map<String, String> saveGstr1(Map<String, String> headerData, String payLoad) {
		log.debug("saveGstr1 method : START");
		Map<String, String> responseMap = null;
		String gspGstr1Baseurl = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_GSTR1_URL, null,
				LocaleContextHolder.getLocale());
		// log.debug("====================headerData================="+headerData);
		// preparing gsp header data needed for GSP call.

		Map<String, String> gspHeaders = prepareHeader(headerData, Gstr1ConstantsV31.TYPE_SAVE);
		// log.debug("====================gspHeaders================="+gspHeaders);
		BodyV31<String> body = new BodyV31<>();
		body.setRequestBody(payLoad);
		Map<String, Object> tempMap = callGsp(gspGstr1Baseurl.toString(), gspHeaders, HttpMethod.PUT, body);
		log.debug("saveGstr1 method : converting GSTN data to map of string");
		log.info("saveGstr1 method : converting GSTN data to map of string");
		if (tempMap != null && tempMap.containsKey(Gstr1ConstantsV31.RESP_STATUS_CODE)
				&& Gstr1ConstantsV31.RESP_SUCCESS_CODE.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE))) {
			responseMap = tempMap.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
					.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
		} else {
			log.debug("saveGstr1 method : Error response received from GSTN, processing it");
			generateException(tempMap);
		}
		log.debug("saveGstr1 method : END");
		return responseMap;
	}

	/**
	 * common method to prepare the header required for GSP call.
	 * 
	 * @param headerData,
	 *            header data received
	 * @return header map
	 */
	private Map<String, String> prepareHeader(Map<String, String> reqHeaderMap, String action) {
		log.debug("prepareHeader method : START");
		Map<String, String> gspHeaders = new HashMap<>();

		Iterator<Map.Entry<String, String>> it = reqHeaderMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pair = it.next();
			if (!filterList.contains(pair.getKey())) {
				gspHeaders.put(pair.getKey(), reqHeaderMap.get(pair.getKey()));
			}
		}
		gspHeaders.putAll(reqHeaderMap);

		String apiVer = gstnResource.getMessage(Gstr1ConstantsV31.GSP_HEADER_API_VER, null,
				LocaleContextHolder.getLocale());
		String aspId = gstnResource.getMessage(Gstr1ConstantsV31.GSP_HEADER_ASP_ID, null,
				LocaleContextHolder.getLocale());
		String aspLicKey = gstnResource.getMessage(Gstr1ConstantsV31.GSP_HEADER_ASP_LICENSE_KEY, null,
				LocaleContextHolder.getLocale());
		gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_API_VER, apiVer);
		gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_ASP_LICENSE_KEY, aspLicKey);
		gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_ASP_ID, aspId);
		gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_DEVICE_STRING, reqHeaderMap.get(Gstr1ConstantsV31.HEADER_SRC_DEV));
		// gspHeaders.put(Gstr1ConstantsV31.INPUT_GSTN_RET_PER,
		// reqHeaderMap.get(Gstr1ConstantsV31.INPUT_FP));
		gspHeaders.put(Gstr1ConstantsV31.INPUT_GSTN_ACTION, action);

		/*
		 * gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_CONENT_TYPE,
		 * "application/json");
		 * gspHeaders.put(Gstr1ConstantsV31.HEADER_STATE_CODE,
		 * reqHeaderMap.get(Gstr1ConstantsV31.HEADER_STATE_CODE));
		 * gspHeaders.put(Gstr1ConstantsV31.HEADER_IP,
		 * reqHeaderMap.get(Gstr1ConstantsV31.HEADER_IP));
		 * gspHeaders.put(Gstr1ConstantsV31.HEADER_TXN,
		 * reqHeaderMap.get(Gstr1ConstantsV31.HEADER_TXN));
		 * 
		 * gspHeaders.put(Gstr1ConstantsV31.INPUT_GSTN,
		 * reqHeaderMap.get(Gstr1ConstantsV31.INPUT_GSTN));
		 * gspHeaders.put(Gstr1ConstantsV31.HEADER_USER_NAME,
		 * reqHeaderMap.get(Gstr1ConstantsV31.HEADER_USER_NAME));
		 * gspHeaders.put(Gstr1ConstantsV31.HEADER_AUTH_TOKEN,
		 * reqHeaderMap.get(Gstr1ConstantsV31.HEADER_AUTH_TOKEN));
		 * gspHeaders.put(Gstr1ConstantsV31.INPUT_SEK,
		 * reqHeaderMap.get(Gstr1ConstantsV31.INPUT_SEK));
		 * gspHeaders.put(Gstr1ConstantsV31.INPUT_APP_KEY,
		 * reqHeaderMap.get(Gstr1ConstantsV31.INPUT_APP_KEY));
		 */

		log.debug("prepareHeader method : END");
		return gspHeaders;
	}

	/**
	 * This method is for handling and parsing the exception generated while
	 * calling GSP system. At present it handles three conditions if response is
	 * null if error json has status_cd and error object if error json is only
	 * containing error object
	 * 
	 * @param map
	 *            takes response map from gsp call
	 */
	private void generateException(Map<String, Object> map) {
		Map<String, Object> excObj = new HashMap<>();
		log.debug("generateException method : START");
		if (map == null) {
			String msg = messageSourceV31.getMessage("GSP_NULL_DATA", null, LocaleContextHolder.getLocale());
			excObj.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP_NULL_RESPONSE);
			excObj.put(Gstr1ConstantsV31.ERROR_DESC, msg);
			excObj.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.GSP_ERROR_GRP_VAL);
			log.error("generateException method : Error Occured in GSP call null data received, error occurred. {}",
					msg);
			log.debug("generateException Error Occured in GSP call null data received method : END");
			throw new GspExceptionV31(msg, null, false, false, excObj);
		} else if (map.containsKey(Gstr1ConstantsV31.ERROR)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> respExcObj = (Map<String, Object>) map.get(Gstr1ConstantsV31.ERROR);
			String errorMsg = String.valueOf(respExcObj.get(Gstr1ConstantsV31.ERROR_MESSAGE));
			excObj.put(Gstr1ConstantsV31.ERROR_CODE, String.valueOf(respExcObj.get(Gstr1ConstantsV31.ERROR_CD_STRING)));
			excObj.put(Gstr1ConstantsV31.ERROR_DESC, errorMsg);
			excObj.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.GSP_ERROR_GRP_VAL);
			log.error("generateException method : Error Occured in GSP call , error occurred. {}", errorMsg);
			log.debug("generateException method : END");
			throw new GspExceptionV31(errorMsg, null, false, false, excObj);
		} else if (map.containsKey(Gstr1ConstantsV31.ERROR_MESSAGE)) {
			String errorMsg = String.valueOf(map.get(Gstr1ConstantsV31.ERROR_MESSAGE));
			excObj.put(Gstr1ConstantsV31.ERROR_CODE, String.valueOf(map.get(Gstr1ConstantsV31.ERROR_CD_STRING)));
			excObj.put(Gstr1ConstantsV31.ERROR_DESC, errorMsg);
			excObj.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.GSP_ERROR_GRP_VAL);
			log.error("generateException method : Error Occured in GSP call , error occurred. {}", errorMsg);
			log.debug("generateException method : END");
			throw new GspExceptionV31(errorMsg, null, false, false, excObj);
		}

		log.debug("generateException method : END");
	}

	@Override
	public Map<String, String> getL2(Map<String, String> data, Map<String, String> headerData) {
		log.debug("initiateGspCallForL3 method : START");
		Map<String, String> responseMap = null;
		log.debug("initiateGspCallForL3 method : inserting the metadata log in the database");
		log.debug("initiateGspCallForL3 method : success in inserting metadata log in the database");
		log.info("initiateGspCallForL3 method : success in inserting metadata log in the database");
		String section = (String) data.get(Gstr1ConstantsV31.INPUT_SECTION);

		String gspGstr1Baseurl = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_GSTR1_URL, null,
				LocaleContextHolder.getLocale());
		StringBuilder baseUrl = new StringBuilder(gspGstr1Baseurl).append("?action=").append(section.toUpperCase())
				.append("&ret_period=").append(data.get("fp")).append("&gstin=").append(data.get("gstin"));
		// preparing gsp header data needed for GSP call.
		Map<String, String> gspHeaders = prepareHeader(headerData, section.toUpperCase());
		Map<String, Object> tempMap = callGsp(baseUrl.toString(), gspHeaders, HttpMethod.GET, null);
		
		log.debug("getB2bData method : converting GSTN data to map of string");
		log.info("getB2bData method : converting GSTN data to map of string");
		if (tempMap != null && tempMap.containsKey(Gstr1ConstantsV31.RESP_STATUS_CODE)
				&& (Gstr1ConstantsV31.RESP_SUCCESS_CODE.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE)))
				|| (Gstr1ConstantsV31.RESP_SUCCESS_CODE_BULK.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE)))) {
			responseMap = tempMap.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
					.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
		} else {
			log.debug("getL2 method : Error response received from GSTN, processing it");
			
			Map<String, Object> respMap = new HashMap<>();
			respMap.put(Gstr1ConstantsV31.RESPONSE_DEV_MSG,  (tempMap.get("error_cd")+":"+tempMap.get("message")));
			respMap.put(Gstr1ConstantsV31.RESPONSE_USR_ACT,  tempMap.get("message"));
			respMap.put(Gstr1ConstantsV31.RESPONSE_USR_MSG, tempMap.get("message"));
			CommonUtil.throwException(ErrorCodesV31.GSP010200, Gstr1ConstantsV31.GSTN_L2, null, HttpStatus.OK,
					null, AspConstants.FORM_CODE,respMap);
			
		}
		log.debug("getB2bData method : END");

		return responseMap;

	}

	@Override
	public Map<String, String> getL0(Map<String, String> data, Map<String, String> headers) {

		log.debug("initiateGspCallForL0 method : START");
		Map<String, String> responseMap = null;
		log.debug("initiateGspCallForL0 method : inserting the metadata log in the database");
		log.debug("initiateGspCallForL0 method : success in inserting metadata log in the database");
		log.info("initiateGspCallForL0 method : success in inserting metadata log in the database");
		String action =  Gstr1ConstantsV31.L0_ACTION;

		String gspGstr1Baseurl = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_GSTR1_URL, null,
				LocaleContextHolder.getLocale());
		StringBuilder baseUrl = new StringBuilder(gspGstr1Baseurl).append("?action=").append(action)
				.append("&ret_period=").append(data.get("fp")).append("&gstin=").append(data.get("gstin"));
		// preparing gsp header data needed for GSP call.
		Map<String, String> gspHeaders = prepareHeader(headers, action.toUpperCase());
		Map<String, Object> tempMap = callGsp(baseUrl.toString(), gspHeaders, HttpMethod.GET, null);

		log.debug("getB2bData method : converting GSTN data to map of string");
		log.info("getB2bData method : converting GSTN data to map of string");
		if (tempMap != null && tempMap.containsKey(Gstr1ConstantsV31.RESP_STATUS_CODE)
				&& Gstr1ConstantsV31.RESP_SUCCESS_CODE.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE))) {
			responseMap = tempMap.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
					.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
		} else {
			log.debug("getL2 method : Error response received from GSTN, processing it");
			Map<String,Object> respMap = new HashMap<>();
			respMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP001);
			respMap.put(Gstr1ConstantsV31.ERROR_DESC,(String) tempMap.get("message") );
			respMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_GSTN_GET );
			throw new GspExceptionV31("Error in Gsp Data",null, false, false, respMap);
			
		}
		log.debug("getB2bData method : END");

		return responseMap;

	}

	
	
	@Override
	public Map<String, String> getBulkUrl(Map<String, String> data, Map<String, String> headerData,String token) {
		log.debug("initiateGspCallForL3 method : START");
		Map<String, String> responseMap = null;
		log.debug("initiateGspCallForL3 method : inserting the metadata log in the database");
		log.debug("initiateGspCallForL3 method : success in inserting metadata log in the database");
		log.info("initiateGspCallForL3 method : success in inserting metadata log in the database");
		String section = (String) data.get(Gstr1ConstantsV31.INPUT_SECTION);

		String gspGstr1Baseurl = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_GSTR1_URL, null,
				LocaleContextHolder.getLocale());
		StringBuilder baseUrl = new StringBuilder(gspGstr1Baseurl).append("?action=").append(Gstr1ConstantsV31.FILE_URLS_ACTION)
				.append("&ret_period=").append(data.get("fp")).append("&gstin=").append(data.get("gstin")).append("&token=").append(token);
		// preparing gsp header data needed for GSP call.
		Map<String, String> gspHeaders = prepareHeader(headerData, section.toUpperCase());
		Map<String, Object> tempMap = callGsp(baseUrl.toString(), gspHeaders, HttpMethod.GET, null);

		log.debug("getB2bData method : converting GSTN data to map of string");
		log.info("getB2bData method : converting GSTN data to map of string");
		if (tempMap != null && tempMap.containsKey(Gstr1ConstantsV31.RESP_STATUS_CODE)
				&& (Gstr1ConstantsV31.RESP_SUCCESS_CODE.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE)))
				|| (Gstr1ConstantsV31.RESP_SUCCESS_CODE_BULK.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE)))) {
			responseMap = tempMap.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
					.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
		} else {
			log.debug("getL2 method : Error response received from GSTN, processing it");
			Map<String, Object> respMap = new HashMap<>();
			respMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP003);
			respMap.put(Gstr1ConstantsV31.ERROR_DESC, (String) tempMap.get("message"));
			respMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_GSTN_GET);
			throw new GspExceptionV31("Error in Gsp Data", null, false, false, respMap);

		}
		log.debug("getB2bData method : END");

		return responseMap;

	}

}
