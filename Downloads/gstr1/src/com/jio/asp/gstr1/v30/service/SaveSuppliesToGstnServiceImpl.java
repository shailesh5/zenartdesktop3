package com.jio.asp.gstr1.v30.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.dao.AspMongoDao;
import com.jio.asp.gstr1.v30.exception.AspException;
import com.jio.asp.gstr1.v30.exception.RestExceptionHandler;
import com.jio.asp.gstr1.v30.util.AESEncryption;
import com.jio.asp.gstr1.v30.util.Body;
import com.jio.asp.gstr1.v30.util.CommonUtil;
import com.jio.asp.gstr1.v30.util.HmacGenerator;

@Service
public class SaveSuppliesToGstnServiceImpl implements SaveSuppliesToGstnService {

	private static final Logger log = LoggerFactory.getLogger(SaveSuppliesToGstnServiceImpl.class);
	@Autowired
	private AspMongoDao aspMongoDao;
	@Autowired
	private MessageSource gstnResource;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private GSPService gspService;

	/* (non-Javadoc)
	 * @see com.jio.asp.gstr1.v30.service.SaveSuppliesToGstnService#processSuppliesData(java.util.Map, java.util.Map)
	 */
	@Override
	public String processSuppliesData(Map<String, String> allRequestParams, Map<String, String> reqHeaderMap) {
		log.debug("processSuppliesData method : START");
		log.info("processSuppliesData method : START");

		String decryptedData1 = "";
		String gstr1GstnCol = gstnResource.getMessage("gstr1gstn.col", null, LocaleContextHolder.getLocale());
		List<Map> listMap = aspMongoDao.getDataForGstn(allRequestParams, gstr1GstnCol);
		Map finalMap = convertInGstnFormat(allRequestParams, listMap);
		log.debug("processSuppliesData method : finalMap");
		log.info("processSuppliesData method : finalMap");
		String fp = allRequestParams.get(Gstr1Constants.INPUT_FP);
		reqHeaderMap.put(Gstr1Constants.INPUT_GSTN_RET_PER, fp);
		Map<String, Object> encryptedData = encryptData(reqHeaderMap, finalMap);
		JSONObject jsonObject = new JSONObject(encryptedData);
		log.debug("Recieved encryptedData");
		log.debug("Before calling saveGstr1");		
		Map<String, String> responseMap = gspService.saveGstr1(reqHeaderMap, jsonObject.toString());		
		log.debug("After calling saveGstr1");
		decryptedData1 = parseGspData(responseMap, reqHeaderMap);		
		log.info("After getting the decryptedData1");		
		log.debug("processSuppliesData method : END");
		return decryptedData1;
	}

	private Map<String, String> prepareGstnHeader(Map<String, String> reqHeaderMap,
			Map<String, String> allRequestParams, String action) {

		Map<String, String> gstnHeaders = new HashMap<>();
		String apiVer = gstnResource.getMessage(Gstr1Constants.GSP_HEADER_API_VER, null,
				LocaleContextHolder.getLocale());
		String aspId = gstnResource.getMessage(Gstr1Constants.GSP_HEADER_ASP_ID, null, LocaleContextHolder.getLocale());
		String aspLicKey = gstnResource.getMessage(Gstr1Constants.GSP_HEADER_ASP_LICENSE_KEY, null,
				LocaleContextHolder.getLocale());

		gstnHeaders.put(Gstr1Constants.GSP_HEADER_CONENT_TYPE, "application/json");
		gstnHeaders.put(Gstr1Constants.HEADER_STATE_CODE, reqHeaderMap.get(Gstr1Constants.HEADER_STATE_CODE));
		gstnHeaders.put(Gstr1Constants.HEADER_IP, reqHeaderMap.get(Gstr1Constants.HEADER_IP));
		gstnHeaders.put(Gstr1Constants.HEADER_TXN, reqHeaderMap.get(Gstr1Constants.HEADER_TXN));
		gstnHeaders.put(Gstr1Constants.GSP_HEADER_API_VER, apiVer);
		gstnHeaders.put(Gstr1Constants.INPUT_GSTN, reqHeaderMap.get(Gstr1Constants.INPUT_GSTN));
		gstnHeaders.put(Gstr1Constants.INPUT_GSTN_RET_PER, allRequestParams.get(Gstr1Constants.INPUT_FP));
		gstnHeaders.put(Gstr1Constants.GSP_HEADER_DEVICE_STRING, reqHeaderMap.get(Gstr1Constants.HEADER_SRC_DEV));

		gstnHeaders.put(Gstr1Constants.HEADER_USER_NAME, reqHeaderMap.get(Gstr1Constants.HEADER_USER_NAME));
		gstnHeaders.put(Gstr1Constants.HEADER_AUTH_TOKEN, reqHeaderMap.get(Gstr1Constants.HEADER_AUTH_TOKEN));
		gstnHeaders.put(Gstr1Constants.INPUT_SEK, reqHeaderMap.get(Gstr1Constants.INPUT_SEK));
		gstnHeaders.put(Gstr1Constants.INPUT_APP_KEY, reqHeaderMap.get(Gstr1Constants.INPUT_APP_KEY));
		gstnHeaders.put(Gstr1Constants.GSP_HEADER_ASP_LICENSE_KEY, aspLicKey);
		gstnHeaders.put(Gstr1Constants.GSP_HEADER_ASP_ID, aspId);

		gstnHeaders.put(Gstr1Constants.INPUT_GSTN_ACTION, action);

		log.debug("preparegstnHeader {} method : END", gstnHeaders);

		return gstnHeaders;

	}

	private Map convertInGstnFormat(Map<String, String> allRequestParams, List<Map> listMap) {

		Map<String, Object> map = new HashMap();
		List<Object> ls = new ArrayList<>();
		if (listMap != null && listMap.size() > 0) {

			map.put(Gstr1Constants.INPUT_GSTN, allRequestParams.get(Gstr1Constants.INPUT_GSTN));
			map.put(Gstr1Constants.INPUT_FP, allRequestParams.get(Gstr1Constants.INPUT_FP));
			map.put(Gstr1Constants.INPUT_GT, new Double(allRequestParams.get(Gstr1Constants.INPUT_GT)));
			map.put(Gstr1Constants.INPUT_CUR_GT, new Double(allRequestParams.get(Gstr1Constants.INPUT_CUR_GT)));

			List<Map<String, Object>> sectionB2bAll = new ArrayList<>();
			for (Map m : listMap) {
				Map<String, Object> result = (Map<String, Object>) m.get(Gstr1Constants.RESULT);
				if (result != null) {

					List<Map<String, Object>> sectionB2b = (List<Map<String, Object>>) result
							.get(Gstr1Constants.TYPE_B2B);
					if (sectionB2b != null && sectionB2b.size() > 0)
						map.put(Gstr1Constants.TYPE_B2B, sectionB2b);
					List<Map<String, Object>> sectionB2cl = (List<Map<String, Object>>) result
							.get(Gstr1Constants.TYPE_B2CL);
					if (sectionB2cl != null && sectionB2cl.size() > 0)
						map.put(Gstr1Constants.TYPE_B2CL, sectionB2cl);
					List<Map<String, Object>> sectionCdnr = (List<Map<String, Object>>) result
							.get(Gstr1Constants.TYPE_CDNR);
					if (sectionCdnr != null && sectionCdnr.size() > 0)
						map.put(Gstr1Constants.TYPE_CDNR, sectionCdnr);
					List<Map<String, Object>> sectionB2cs = (List<Map<String, Object>>) result
							.get(Gstr1Constants.TYPE_B2CS);
					if (sectionB2cs != null && sectionB2cs.size() > 0)
						map.put(Gstr1Constants.TYPE_B2CS, sectionB2cs);
					List<Map<String, Object>> sectionExp = (List<Map<String, Object>>) result
							.get(Gstr1Constants.TYPE_EXP);
					if (sectionExp != null && sectionExp.size() > 0)
						map.put(Gstr1Constants.TYPE_EXP, sectionExp);
					Map<String, Object> sectionHsn = (Map<String, Object>) result.get(Gstr1Constants.TYPE_HSN);
					if (sectionHsn != null && sectionHsn.size() > 0)
						map.put(Gstr1Constants.TYPE_HSN, sectionHsn);
					Map<String, Object> sectionNil = (Map<String, Object>) result.get(Gstr1Constants.TYPE_NIL);
					if (sectionNil != null)
						map.put(Gstr1Constants.TYPE_NIL, sectionNil);
					List<Map<String, Object>> sectionTxpd = (List<Map<String, Object>>) result
							.get(Gstr1Constants.TYPE_TXPD);
					if (sectionTxpd != null && sectionTxpd.size() > 0)
						map.put(Gstr1Constants.TYPE_TXPD, sectionTxpd);
					List<Map<String, Object>> sectionAt = (List<Map<String, Object>>) result
							.get(Gstr1Constants.TYPE_AT);
					if (sectionAt != null && sectionAt.size() > 0)
						map.put(Gstr1Constants.TYPE_AT, sectionAt);
					Map<String, Object> sectionDocs = (Map<String, Object>) result.get(Gstr1Constants.TYPE_DOCS);
					if (sectionDocs != null)
						map.put(Gstr1Constants.TYPE_DOCS, sectionDocs);
					List<Map<String, Object>> sectionCdnur = (List<Map<String, Object>>) result
							.get(Gstr1Constants.TYPE_CDNUR);
					if (sectionCdnur != null && sectionCdnur.size() > 0)
						map.put(Gstr1Constants.TYPE_CDNUR, sectionCdnur);

				} 

			}
		}else {
			CommonUtil.throwException(ErrorCodes.ASP105, Gstr1Constants.ASP_GSTR1_SAVE_TO_GSTN,
					ErrorCodes.ASP105, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource, null);
		}


		return map;
	}

	private <T> Map<String, Object> callGstnApi(Map<String, String> headers, HttpMethod method, Body<T> body,
			String url) {
		log.debug("callGstnApi method : START");

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAll(headers);
		HttpEntity<T> entity;
		if (HttpMethod.PUT.equals(method) && body != null) {
			entity = new HttpEntity<>(body.getRequestBody(), httpHeaders);
		} else {
			entity = new HttpEntity<>(httpHeaders);
		}

		SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();

		int readTimeOut = Integer
				.parseInt(gstnResource.getMessage("gstn-read-timeout", null, LocaleContextHolder.getLocale()));
		int connectionTimeOut = Integer
				.parseInt(gstnResource.getMessage("gstn-connection-timeout", null, LocaleContextHolder.getLocale()));

		rf.setReadTimeout(readTimeOut);
		rf.setConnectTimeout(connectionTimeOut);
		log.debug("callGstnApi method : just before calling the GSTN api");
		log.info("callGstnApi method : just before calling the GSTN api");

		restTemplate.setErrorHandler(new RestExceptionHandler());
		ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {
		};
		ResponseEntity<Map<String, Object>> main = restTemplate.exchange(url, method, entity, typeRef);
		Map<String, Object> responseMap = null;
		if (main != null && main.getStatusCode() == HttpStatus.OK && main.hasBody()) {
			responseMap = main.getBody();
		}
		log.debug("callGstnApi method : END");
		return responseMap;
	}

	private Map<String, Object> encryptData(Map<String, String> headerData, Map<String, Object> data) {

		byte[] SessionKeyInBytes = null;
		Map<String, Object> responseMap = new HashMap<>();
		Gson gson = new Gson();

		try {

			log.info("EncryptRequest API call - Encrypting data received from client");
			// Decryption of Session Key
			String session_key = headerData.get(Gstr1Constants.INPUT_SEK);
			String app_key = headerData.get(Gstr1Constants.INPUT_APP_KEY);
			byte[] barray = AESEncryption.decodeBase64StringTOByte(app_key);
			SessionKeyInBytes = AESEncryption.decrypt(session_key, barray);
			String jsonData = gson.toJson(data);
			String baseData = AESEncryption.encodeBase64String(jsonData.getBytes());
			log.debug("=======================jsonData===============");
			// Encryption of data from client
			String encryptedData = AESEncryption.encryptEK(baseData.getBytes(), SessionKeyInBytes);
			// Generation of HMAC
			String hmac = HmacGenerator.getHmac(baseData, SessionKeyInBytes);
			responseMap.put(Gstr1Constants.INPUT_DATA, encryptedData);
			responseMap.put(Gstr1Constants.INPUT_HMAC, hmac);
			responseMap.put(Gstr1Constants.INPUT_GSTN_ACTION, Gstr1Constants.TYPE_SAVE);

		} catch (Exception e) {
			responseMap.put(Gstr1Constants.ERROR_CODE, ErrorCodes.DATA_ENCRYPT_ERROR);
			responseMap.put(Gstr1Constants.ERROR_DESC, "Error in encryption	of data from client");
			responseMap.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GSTR1_SAVE_TO_GSTN);
			throw new AspException("Error in encryption of data from client", null, false, false, responseMap);
		}
		return responseMap;
	}

	/**
	 * Method takes the data from getGstr1InvoiceData method and creates java
	 * bean mapping for each of the items. This method will decrypt the data
	 * using appkey --> sek --> rek --> data
	 * 
	 * @param gspData
	 *            Map containing GSP data
	 * @param inputMap
	 *            user input data
	 * @return Map containing parsed GSP data
	 */
	private String parseGspData(Map<String, String> gspData, Map<String, String> inputMap) {
		log.debug("parseGspData method : START");
		String jsonData = null;
		if (gspData.containsKey(Gstr1Constants.INPUT_REK) && gspData.containsKey(Gstr1Constants.INPUT_DATA)) {
			String respRek = MapUtils.getString(gspData, Gstr1Constants.INPUT_REK);
			String respData = MapUtils.getString(gspData, Gstr1Constants.INPUT_DATA);
			byte[] authEK;
			try {
				authEK = AESEncryption.decrypt((String) inputMap.get(Gstr1Constants.INPUT_SEK),
						AESEncryption.decodeBase64StringTOByte((String) inputMap.get(Gstr1Constants.INPUT_APP_KEY)));

				log.debug("parseGspData method : Successfully Parsed SEK");

				byte[] apiEK = AESEncryption.decrypt(respRek, authEK);
				log.debug("parseGspData method : Successfully Parsed REK");

				jsonData = new String(
						AESEncryption.decodeBase64StringTOByte(new String(AESEncryption.decrypt(respData, apiEK))));
				log.debug("parseGspData method : Successfully Parsed GSP JSon Response");
				ObjectMapper mapper = new ObjectMapper();
				log.debug("parseGspData method : after converting response from GSP for gstn data");

			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException e) {
				/*
				 * String errorMsg =
				 * gstnResource.getMessage("GSTR1_PARSE_ASP201", null,
				 * LocaleContextHolder.getLocale()); Map<String, Object> excObj
				 * = new HashMap<>(); excObj.put(Gstr1Constants.ERROR_CODE,
				 * ErrorCodes.GSTR1_PARSE_GSP_ERROR);
				 * excObj.put(Gstr1Constants.ERROR_DESC, errorMsg);
				 * excObj.put(Gstr1Constants.ERROR_GRP,
				 * Gstr1Constants.ASP_GSTR1_SUMMARY_GRP);
				 * excObj.put(Gstr1Constants.ERROR_HTTP_CODE,
				 * HttpStatus.INTERNAL_SERVER_ERROR);
				 */
				log.error("parseGspData method : Error Occured while parsing the GSP response, error occurred. {}", e);
				// throw new GspException(errorMsg, e, false, false, excObj);
			} catch (Exception e) {
				/*
				 * String errorMsg =
				 * gstnResource.getMessage("GSTR1_PARSE_ASP202", null,
				 * LocaleContextHolder.getLocale()); Map<String, Object> excObj
				 * = new HashMap<>(); excObj.put(Gstr1Constants.ERROR_CODE,
				 * ErrorCodes.GSTR1_PARSE_GSP_GEN_ERROR);
				 * excObj.put(Gstr1Constants.ERROR_DESC, errorMsg);
				 * excObj.put(Gstr1Constants.ERROR_GRP,
				 * Gstr1Constants.ASP_GSTR1_SUMMARY_GRP);
				 * excObj.put(Gstr1Constants.ERROR_HTTP_CODE,
				 * HttpStatus.INTERNAL_SERVER_ERROR);
				 */
				log.error(
						"parseGspData method : Unknown Error Occured while parsing the GSP response, error occurred. {}",
						e);
				// throw new GspException(errorMsg, e, false, false, excObj);
			}
		}
		log.debug("parseGspData method : END");
		return jsonData;
	}

}
