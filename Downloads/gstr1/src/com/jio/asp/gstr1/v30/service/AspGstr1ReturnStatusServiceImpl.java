package com.jio.asp.gstr1.v30.service;

import java.util.HashMap;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.exception.AspException;
import com.jio.asp.gstr1.v30.exception.GspException;
import com.jio.asp.gstr1.v30.util.AESEncryption;
import com.jio.asp.gstr1.v30.util.CommonUtil;
import com.jio.asp.gstr1.v30.util.HmacGenerator;

@Service
public class AspGstr1ReturnStatusServiceImpl implements AspGstr1ReturnStatusService {

	@Autowired
	AspLoggingService aspl;
	
	@Autowired
	GSPService gspService;
	
	@Autowired
	private MessageSource gstnResource;
			
	@Autowired
	private MessageSource messageSource;

	
	Logger log = LoggerFactory.getLogger(AspGstr1ReturnStatusServiceImpl.class);

	@Override
	public Map<String, Object> processGstr1Data(Map<String, String> headerData,Map<String, String> params) {
		Map<String, String> gspresponse = new HashMap<>();
		Map<String, Object> response = new HashMap<>();
		log.info("GSTR1 Return Status API call -  start - STEP1");
		String retPeriod = params.get(Gstr1Constants.INPUT_FP);
		headerData.put(Gstr1Constants.INPUT_GSTN_RET_PER, retPeriod);		
		gspresponse = gspService.getGstr1Status(headerData,params);		
		response = gspService.decryptResponse(headerData, gspresponse);
		log.info("GSTR1 Return Status API call - Returning response to client - STEP6");
		return response;
	}

	

	@Override
	public Map<String, Object> validateApiInput(String gstin, String trans_id) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * This method is for handling and parsing the exception 
	 * generated while calling GSP system. At present it handles three conditions
	 * if response is null
	 * if error json has status_cd and error object
	 * if error json is only containing error object
	 * @param map takes response map from gsp call
	 */
	/*private void generateException(Map<String, Object> map) {
		Map<String, Object> excObj = new HashMap<>();
		log.debug("generateException method : START");
		if (map==null) {
			String msg=messageSource.getMessage("GSP_NULL_DATA", null, LocaleContextHolder.getLocale());
			excObj.put(Gstr1Constants.ERROR_CODE, ErrorCodes.GSP_NULL_RESPONSE);
			excObj.put(Gstr1Constants.ERROR_DESC, msg);
			excObj.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.GSP_ERROR_GRP_VAL);
			log.error("generateException method : Error Occured in GSP call null data received, error occurred. {}", msg);
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
	}*/


	/*public Map<String, Object> decryptResponse(Map<String, String> headerData, Map<String, Object> gspresponse) {

		byte[] SessionKeyInBytes = null;
		byte[] rekbytes = null;
		Map<String, Object> responseMap = new HashMap<>();
		Map<String, Object> dataMap = new HashMap<>();

		try {

			log.info("GSTR1 Return Status API call - Decrypting data received from GSP - STEP4");
			// Decryption of Session Key
			SessionKeyInBytes = AESEncryption.decrypt(headerData.get(Gstr1Constants.INPUT_SEK),
					AESEncryption.decodeBase64StringTOByte(headerData.get(Gstr1Constants.INPUT_APP_KEY)));

			// Decryption of rek
			rekbytes = AESEncryption.decrypt(gspresponse.get(Gstr1Constants.INPUT_REK).toString(), SessionKeyInBytes);

			CommonUtil.validateEmptyString(MapUtils.getString(gspresponse, Gstr1Constants.INPUT_DATA),
					ErrorCodes.GSP_NULL_RESPONSE, Gstr1Constants.ASP_GSTR1_RETURN_STATUS_GRP, "GSTR1_GSTN_BODY_EMPTY", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
			
			CommonUtil.validateEmptyString(MapUtils.getString(gspresponse, Gstr1Constants.INPUT_REK),
					ErrorCodes.GSP_NULL_RESPONSE, Gstr1Constants.ASP_GSTR1_RETURN_STATUS_GRP, "GSTR1_GSTN_BODY_EMPTY", null,
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
//				responseMap.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GSTR1_STATUS_GRP);
//				throw new AspException("Invalid data received data from GSP", null, false, false, responseMap);
//			}
			
			log.info("GSTR1 Return Status API call - Successful decryption of data from GSP - STEP5");
			
			if (gspresponse.containsKey(Gstr1Constants.INPUT_SIGN)) {
				
				dataMap.put(Gstr1Constants.INPUT_DATA, responseMap);
				
				CommonUtil.validateEmptyString(MapUtils.getString(gspresponse, Gstr1Constants.INPUT_SIGN),
						ErrorCodes.GSP_NULL_RESPONSE, Gstr1Constants.ASP_GSTR1_RETURN_STATUS_GRP, "GSTR1_GSTN_BODY_EMPTY", null,
						HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
				
				dataMap.put(Gstr1Constants.INPUT_SIGN, gspresponse.get(Gstr1Constants.INPUT_SIGN));
				return dataMap;
			}
		} catch (Exception e) {
			responseMap.put(Gstr1Constants.ERROR_CODE, ErrorCodes.DATA_DECRYPT_ERROR);
			responseMap.put(Gstr1Constants.ERROR_DESC, "Error in decryption of data from GSP");
			responseMap.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GSTR1_RETURN_STATUS_GRP);
			throw new AspException("Error in decryption of data from GSP", null, false, false, responseMap);
		}
		return responseMap;
	}*/

	
	
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
				ErrorCodes.GSTR1_SUM_EMPTY, Gstr1Constants.ASP_GSTR1_RETURN_STATUS_GRP, "GSTR1_HDR_SEK_ASP105", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		
		CommonUtil.validateEmptyString(MapUtils.getString(headerData, Gstr1Constants.INPUT_APP_KEY),
				ErrorCodes.GSTR1_SUM_TABLE_TYPES_EMTY, Gstr1Constants.ASP_GSTR1_RETURN_STATUS_GRP, "GSTR1_HDR_APP_KEY_ASP106",
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		log.debug("validateHeaderInput method : END");
	}

	/**
	 * Method does the api input validation. Data which is needed for request
	 * processing and quering from database and GSP
	 * 
	 * @param inputMap
	 *            this object contains user input parameter for searching of the
	 *            user data.
	 * @return Map, which contains error information or processed data
	 */
	@Override
	public void validateApiInput(Map<String,String> params) {
		
		log.debug("validateApiInput method : START");
		Map<String, Object> excObj = new HashMap<>();

		CommonUtil.validateEmptyString(params.get(Gstr1Constants.INPUT_REFERENCE_ID), ErrorCodes.TRANSACTION_MISSING, Gstr1Constants.ASP_GSTR1_RETURN_STATUS_GRP,
					"GSTR1_BODY_VAL_ASP307", null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(params.get(Gstr1Constants.INPUT_GSTN), ErrorCodes.GSTIN_MISSING, Gstr1Constants.ASP_GSTR1_RETURN_STATUS_GRP,
				"GSTR1_BODY_VAL_ASP306", null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		
		CommonUtil.validateEmptyString(params.get(Gstr1Constants.INPUT_FP), ErrorCodes.RET_PERIOD_MISSING, Gstr1Constants.ASP_GSTR1_RETURN_STATUS_GRP,
				"GSTR1_BODY_VAL_ASP308", null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);


		log.debug("validateApiInput method : END");

	}
}
