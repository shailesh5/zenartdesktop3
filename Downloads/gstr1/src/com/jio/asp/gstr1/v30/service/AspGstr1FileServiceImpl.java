package com.jio.asp.gstr1.v30.service;

import java.util.HashMap;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.exception.AspException;
import com.jio.asp.gstr1.v30.exception.GspException;
import com.jio.asp.gstr1.v30.util.AESEncryption;
import com.jio.asp.gstr1.v30.util.CommonUtil;
import com.jio.asp.gstr1.v30.util.HmacGenerator;

@Service
public class AspGstr1FileServiceImpl implements AspGstr1FileService {

	@Autowired
	private GSPService gspService;

	@Autowired
	private MessageSource gstnResource;

	@Autowired
	private MessageSource messageSource;

	Logger log = LoggerFactory.getLogger(AspGstr1FileServiceImpl.class);

	@Override
	public Map<String, Object> processGstr1Data(Map<String, String> headerData, Map<String, Object> requestData) {

		Map<String, Object> gspresponse = new HashMap<>();
		Map<String, Object> response = new HashMap<>();
		Map<String, Object> data = new HashMap<>();
		Map<String, Object> request = new HashMap<>();

		log.info("POST API - FILE GSTR1");

		request = validateApiInput(requestData);
		log.info("FileGSTR1 API call - Body Validation Successfull - STEP2");

		data = encryptRequest(headerData, request);
		log.info("FileGSTR1 API call - Incoming request data encrypted successfully - STEP4");

		gspresponse = gspService.postGstr1(headerData, data);

		if (gspresponse != null && gspresponse.containsKey(Gstr1Constants.RESP_STATUS_CODE)
				&& Gstr1Constants.RESP_SUCCESS_CODE.equals(gspresponse.get(Gstr1Constants.RESP_STATUS_CODE))) {

			response = decryptResponse(headerData, gspresponse);

		} else {
			generateException(gspresponse);
		}

		return response;
	}

	@Override
	public Map<String, Object> validateApiInput(Map<String, Object> request) {

		CommonUtil.validateEmptyString(MapUtils.getString(request, Gstr1Constants.INPUT_DATA),
				ErrorCodes.ASP103, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_BODY_VAL_ASP303", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
//		CommonUtil.validateEmptyString(MapUtils.getString(request, Gstr1Constants.INPUT_GSTN_GSTIN),
//				ErrorCodes.ASP103, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_BODY_VAL_ASP303", null,
//				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		if (request.containsKey(Gstr1Constants.INPUT_SIGN) || request.containsKey(Gstr1Constants.INPUT_ST)
				|| request.containsKey(Gstr1Constants.INPUT_SID)) {
			CommonUtil.validateEmptyString(MapUtils.getString(request, Gstr1Constants.INPUT_SIGN),
					ErrorCodes.ASP103, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_BODY_VAL_ASP303", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
			CommonUtil.validateEmptyString(MapUtils.getString(request, Gstr1Constants.INPUT_ST),
					ErrorCodes.ASP103, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_BODY_VAL_ASP303", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
			CommonUtil.validateEmptyString(MapUtils.getString(request, Gstr1Constants.INPUT_SID),
					ErrorCodes.ASP103, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_BODY_VAL_ASP303", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		}

		return request;
	}

	@Override
	public Map<String, Object> decryptResponse(Map<String, String> headerData, Map<String, Object> gspresponse) {

		byte[] SessionKeyInBytes = null;
		byte[] rekbytes = null;
		Map<String, Object> responseMap = new HashMap<>();
		Map<String, Object> dataMap = new HashMap<>();

		try {

			log.info("FileGSTR1 API call - Decrypting data received from GSP - STEP7");
			// Decryption of Session Key
			SessionKeyInBytes = AESEncryption.decrypt(headerData.get(Gstr1Constants.INPUT_SEK),
					AESEncryption.decodeBase64StringTOByte(headerData.get(Gstr1Constants.INPUT_APP_KEY)));

			// Decryption of rek
			rekbytes = AESEncryption.decrypt(gspresponse.get(Gstr1Constants.INPUT_REK).toString(), SessionKeyInBytes);

			CommonUtil.validateEmptyString(MapUtils.getString(gspresponse, Gstr1Constants.INPUT_DATA),
					ErrorCodes.GSP_NULL_RESPONSE, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_GSTN_BODY_EMPTY", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
			
			CommonUtil.validateEmptyString(MapUtils.getString(gspresponse, Gstr1Constants.INPUT_REK),
					ErrorCodes.GSP_NULL_RESPONSE, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_GSTN_BODY_EMPTY", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
			
			// Decryption of data from gsp
			String jsonData = new String(AESEncryption.decodeBase64StringTOByte(
					new String(AESEncryption.decrypt(gspresponse.get(Gstr1Constants.INPUT_DATA).toString(), rekbytes))));
			Gson gson = new Gson();
			
			responseMap = gson.fromJson(jsonData, Map.class);
			
			String jsonBody = gson.toJson(responseMap);
			String hmac = HmacGenerator.getHmac(jsonBody, rekbytes);
			
//			if(!hmac.equals(gspresponse.get(Gstr1Constants.INPUT_HMAC).toString()))
//			{
//				responseMap.put(Gstr1Constants.ERROR_CODE, ErrorCodes.HMAC_MISMATCH_ERROR);
//				responseMap.put(Gstr1Constants.ERROR_DESC, "Invalid data received data from GSP");
//				responseMap.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GSTR1_FILE_GRP);
//				throw new AspException("Invalid data received data from GSP", null, false, false, responseMap);
//			}
			
			log.info("FileGSTR1 API call - Successful decryption of data from GSP - STEP8");
			
			if (gspresponse.containsKey(Gstr1Constants.INPUT_SIGN)) {
				
				dataMap.put(Gstr1Constants.INPUT_DATA, responseMap);
				
				CommonUtil.validateEmptyString(MapUtils.getString(gspresponse, Gstr1Constants.INPUT_SIGN),
						ErrorCodes.GSP_NULL_RESPONSE, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_GSTN_BODY_EMPTY", null,
						HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
				
				dataMap.put(Gstr1Constants.INPUT_SIGN, gspresponse.get(Gstr1Constants.INPUT_SIGN));
				return dataMap;
			}
		} catch (Exception e) {
			responseMap.put(Gstr1Constants.ERROR_CODE, ErrorCodes.DATA_ENCRYPT_ERROR);
			responseMap.put(Gstr1Constants.ERROR_DESC, "Error in decryption of data from GSP");
			responseMap.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GSTR1_FILE_GRP);
			throw new AspException("Error in decryption of data from GSP", null, false, false, responseMap);
		}
		return responseMap;
	}

	/**
	 * Method does header data validation like client details from the asp
	 * database.
	 * 
	 * @param headerData
	 *            this object contains request header data, which is needed to
	 *            do GSP call and user validation
	 */
	@Override
	public void validateHeaderInput(Map<String, String> headerData) {
		log.debug("validateHeaderInput method : START");
		Map<String, Object> excObj = new HashMap<>();

		CommonUtil.validateEmptyString(MapUtils.getString(headerData, Gstr1Constants.INPUT_SEK),
				ErrorCodes.GSTR1_SUM_EMPTY, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_HDR_SEK_ASP105", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		
		CommonUtil.validateEmptyString(MapUtils.getString(headerData, Gstr1Constants.INPUT_APP_KEY),
				ErrorCodes.GSTR1_SUM_TABLE_TYPES_EMTY, Gstr1Constants.ASP_GSTR1_FILE_GRP, "GSTR1_HDR_APP_KEY_ASP106",
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		log.debug("validateHeaderInput method : END");
	}

	@Override
	public Map<String, Object> encryptRequest(Map<String, String> headerData, Map<String, Object> request) {

		byte[] SessionKeyInBytes = null;
		Map<String, Object> responseMap = new HashMap<>();
		Map<String, Object> data = new HashMap<>();
		Gson gson = new Gson();

		try {

			log.info("FileGSTR1 API call - Encrypting data received from client - STEP3");
			// Decryption of Session Key
			SessionKeyInBytes = AESEncryption.decrypt(headerData.get(Gstr1Constants.INPUT_SEK),
					AESEncryption.decodeBase64StringTOByte(headerData.get(Gstr1Constants.INPUT_APP_KEY)));

			if (request.containsKey(Gstr1Constants.INPUT_SIGN)) {
				data = (Map<String, Object>) request.get(Gstr1Constants.INPUT_DATA);
				// Encryption of data from client
				String jsonData = gson.toJson(data);
				ObjectMapper json = new ObjectMapper();
				
				jsonData = json.writeValueAsString(data);
				String encryptedData = AESEncryption.encryptEK(jsonData.getBytes(), SessionKeyInBytes);
				
				responseMap.put(Gstr1Constants.INPUT_SIGN, request.get(Gstr1Constants.INPUT_SIGN));
				responseMap.put(Gstr1Constants.INPUT_ST, request.get(Gstr1Constants.INPUT_ST));
				responseMap.put(Gstr1Constants.INPUT_SID, request.get(Gstr1Constants.INPUT_SID));
				
				responseMap.put(Gstr1Constants.INPUT_DATA, encryptedData);

			} else {
				
				data = (Map<String, Object>) request.get(Gstr1Constants.INPUT_DATA);
				String jsonData = gson.toJson(data);

				// Encryption of data from client
				String encryptedData = AESEncryption.encryptEK(jsonData.getBytes(), SessionKeyInBytes);
				
				//Generation of HMAC
				String hmac = HmacGenerator.getHmac(request.toString(), SessionKeyInBytes);

				responseMap.put(Gstr1Constants.INPUT_DATA, encryptedData);
				responseMap.put(Gstr1Constants.INPUT_HMAC, hmac);
			}

			responseMap.put(Gstr1Constants.INPUT_GSTN_ACTION, Gstr1Constants.TYPE_FILE);
			
		} catch (Exception e) {
			responseMap.put(Gstr1Constants.ERROR_CODE, ErrorCodes.DATA_ENCRYPT_ERROR);
			responseMap.put(Gstr1Constants.ERROR_DESC, "Error in encryption of data from client");
			responseMap.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GSTR1_FILE_GRP);
			throw new AspException("Error in encryption of data from client", null, false, false, responseMap);
		}
		return responseMap;
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
			String msg = messageSource.getMessage("GSP_NULL_DATA", null, LocaleContextHolder.getLocale());
			excObj.put(Gstr1Constants.ERROR_CODE, ErrorCodes.GSP_NULL_RESPONSE);
			excObj.put(Gstr1Constants.ERROR_DESC, msg);
			excObj.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.GSP_ERROR_GRP_VAL);
			log.error("generateException method : Error Occured in GSP call null data received, error occurred. {}",
					msg);
			log.debug("generateException Error Occured in GSP call null data received method : END");
			throw new GspException(msg, null, false, false, excObj);
		} else if (map.containsKey(Gstr1Constants.ERROR)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> respExcObj = (Map<String, Object>) map.get(Gstr1Constants.ERROR);
			String errorMsg = String.valueOf(respExcObj.get(Gstr1Constants.ERROR_MESSAGE));
			excObj.put(Gstr1Constants.ERROR_CODE, String.valueOf(respExcObj.get(Gstr1Constants.ERROR_CD_STRING)));
			excObj.put(Gstr1Constants.ERROR_DESC, errorMsg);
			excObj.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.GSP_ERROR_GRP_VAL);
			log.error("generateException method : Error Occured in GSP call , error occurred. {}", errorMsg);
			log.debug("generateException method : END");
			throw new GspException(errorMsg, null, false, false, excObj);
		} else if (map.containsKey(Gstr1Constants.ERROR_MESSAGE)) {
			String errorMsg = String.valueOf(map.get(Gstr1Constants.ERROR_MESSAGE));
			excObj.put(Gstr1Constants.ERROR_CODE, String.valueOf(map.get(Gstr1Constants.ERROR_CD_STRING)));
			excObj.put(Gstr1Constants.ERROR_DESC, errorMsg);
			excObj.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.GSP_ERROR_GRP_VAL);
			log.error("generateException method : Error Occured in GSP call , error occurred. {}", errorMsg);
			log.debug("generateException method : END");
			throw new GspException(errorMsg, null, false, false, excObj);
		}

		log.debug("generateException method : END");
	}

}
