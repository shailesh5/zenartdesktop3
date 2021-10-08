package com.jio.asp.gstr1.v31.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.cms.CMSException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.exception.AspExceptionV31;
import com.jio.asp.gstr1.v31.exception.GspExceptionV31;
import com.jio.asp.gstr1.v31.util.AESEncryptionV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.gstr1.v31.util.Hash256Util;
import com.jio.asp.gstr1.v31.util.HmacGeneratorV31;
import com.jio.asp.gstr1.v31.util.SignDataVerificationUtil;

@Service
public class AspGstr1FileServiceV31Impl implements AspGstr1FileServiceV31 {

	@Autowired
	private GSPServiceV31 gspService;

	@Autowired
	private MessageSource gstnResource;

	@Autowired
	private MessageSource messageSourceV31;

	@Autowired
	private AspMongoDaoV31 aspMongoDaoV31;



	@Autowired
	private AspAckNumServiceV31 ackService;
	Logger log = LoggerFactory.getLogger(AspGstr1FileServiceV31Impl.class);

	@Override
	public Map<String, Object> processGstr1Data(Map<String, String> headerData, Map<String, Object> requestData,
			Map<String, String> allRequestParams) {

		Map<String, Object> gspresponse = new HashMap<>();
		Map<String, Object> response = new HashMap<>();
		Map<String, Object> response1 = new HashMap<>();
		Map<String, Object> data = new HashMap<>();
		Map<String, Object> request = new HashMap<>();
		List<Object> refIdList = new ArrayList<Object>();
		String ack_num="";

		Map<String, Object> controlMap = new HashMap<>();

		String gstr1GstnCol = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());

		log.info("POST API - FILE GSTR1");

		request = validateApiInput(requestData);
		log.info("File/Submit GSTR1 API call - BodyV31 ValidationV31 Successfull - STEP2");

		data = encryptRequest(headerData, request);

		log.info("File/Submit GSTR1 API call - Incoming request data encrypted successfully - STEP4");

		gspresponse = gspService.postGstr1(headerData, data);

		String ackNumber = ackService.generateAckNumber(Gstr1ConstantsV31.API_NM);
		Map<String, Object> controlStatusMap = new HashMap<>();
		if (gspresponse != null && gspresponse.containsKey(Gstr1ConstantsV31.RESP_STATUS_CODE)
				&& Gstr1ConstantsV31.RESP_SUCCESS_CODE.equals(gspresponse.get(Gstr1ConstantsV31.RESP_STATUS_CODE))) {
			// geeting reference_id
			response = decryptResponse(headerData, gspresponse);

			ack_num = String.valueOf(response.get(Gstr1ConstantsV31.FILE_ACK_NUM));
			//controlMap.put(Gstr1ConstantsV31.FILE_ACK_NUM, ack_num);

			if ((response.containsKey(Gstr1ConstantsV31.FILE_ACK_NUM))
				&& (StringUtils.isNotBlank(String.valueOf(response.get(Gstr1ConstantsV31.FILE_ACK_NUM))))) {
				
				//updating status in DB
				updateFileStatusInDb(gstr1GstnCol, allRequestParams, ack_num);
				
				
//
//				// String referenceId = getRefIdFromResponse(response);
//
//				Map<String, String> gspresponse1 = gspService.getGstr1Status(headerData, allRequestParams,
//						String.valueOf(response.get(Gstr1ConstantsV31.FILE_ACK_NUM)));
//				response1 = gspService.decryptResponse(headerData, gspresponse1);
//				System.out.println("GSTR1FILE:response1" + response1);
//				log.info("GSTR1FILE:response1" + response1);
//
//				String status = String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD));
//				// update submit in db
//				if (String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD))
//						.equalsIgnoreCase(Gstr1ConstantsV31.GSTN_STATUS_CD_P)) {
//					updateFileStatusInDb(gstr1GstnCol, allRequestParams, ack_num);
//
//					// //by RS
//					// updateStatusService.updateGstinMasterData(MapUtils.getMap(requestData,Gstr1ConstantsV31.INPUT_DATA),
//					// headerData, null);
//					// //add
//					controlMap.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD,
//							String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD)));
//					refIdList.add(controlMap);
//				}
//
//				else if (Gstr1ConstantsV31.GSTN_STATUS_CD_E.equals(status)
//						|| Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equals(status)) {
//					Map<String, Object> errMap = null;
//					if (response1.containsKey(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT)
//							&& !Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equals(status)) {
//						errMap = (Map<String, Object>) response1.remove(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
//						errMap.put("error_msg", "Issue occurred while File data to GSTN, please Redo The File Again");
//					} else {
//						errMap = new HashMap<>();
//						errMap.put("error_msg",
//								"Issue occurred while Filing the data to GSTN, please Redo The File  Again");
//						errMap.put("error_cd", "RETIPERROR");
//					}
//
//					controlMap.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD,
//							String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD)));
//					controlMap.putAll(errMap);
//					refIdList.add(controlMap);
//				}
		
					
		}
//			String gstin = String.valueOf(allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN));
//			String fp = String.valueOf(allRequestParams.get(Gstr1ConstantsV31.INPUT_FP));
//			createSaveGstnStatusData(refIdList, ackNumber, Gstr1ConstantsV31.API_NUM, Gstr1ConstantsV31.TYPE_FILE,
//					gstin, fp);

		} else {
			generateException(gspresponse);
		}

		// saveTransactionDetails(headerData, response);

		Map<String, Object> ackMap = new HashMap<>();
		ackMap.put(Gstr1ConstantsV31.FILE_ACK_NUM, ack_num);
		return ackMap;
	}

	@Override
	public void createSaveGstnStatusData(List<Object> refIdsRespone, String ackNumber, String type, String action,
			String gstin, String fp) {
		// List<Map<String, Object>> dataList = new ArrayList<>();
		Map<String, Object> object = new HashMap<>();
		Map<String, Object> outputMap = new HashMap<>();
		object.put("_id", ackNumber);
		object.put("ackNumber", ackNumber);
		object.put("refids", refIdsRespone);
		object.put("type", type);
		object.put("action", action);
		object.put("fp", fp);
		object.put("gstin", gstin);

		aspMongoDaoV31.saveInMongo(new JSONObject(object), "gstn_refid_map");

		// outputMap.put(Gstr1ConstantsV31.INPUT_REFERENCE_ID, ackNumber);
		// String ackNumberOutput = gson.toJson(outputMap);
		// return ackNumberOutput;

	}

	@Override
	public void updateFileStatusInDb(String collection, Map<String, String> allRequestParams, String reference_id) {

		// update the JioGst database
		Map<String, Object> object = new HashMap<String, Object>();
		// if (map.containsKey(Gstr1ConstantsV31.GSTN_STATUS_CD_P)) {
		object.put(Gstr1ConstantsV31.CONTROL_GSTN_STATUS, Gstr1ConstantsV31.STATUS_FILE_UPLOAD);
		SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
		Date date = new Date();
		object.put(Gstr1ConstantsV31.CONTROL_UPLOADTIME, sdf.format(date));
		object.put(Gstr1ConstantsV31.CONTROL_REFERENCEID, reference_id);
		aspMongoDaoV31.updateFileStatusInMongo(object, collection, allRequestParams);

	}

	@Override
	public Map<String, Object> validateApiInput(Map<String, Object> request) {

		CommonUtilV31.validateEmptyString(MapUtils.getString(request, Gstr1ConstantsV31.INPUT_DATA),
				ErrorCodesV31.ASP103, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP, "GSTR1_BODY_VAL_ASP303", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		// CommonUtilV31.validateEmptyString(MapUtils.getString(request,
		// Gstr1ConstantsV31.INPUT_GSTN_GSTIN),
		// ErrorCodesV31.ASP103, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP,
		// "GSTR1_BODY_VAL_ASP303", null,
		// HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		if (request.containsKey(Gstr1ConstantsV31.INPUT_SIGN) || request.containsKey(Gstr1ConstantsV31.INPUT_ST)
				|| request.containsKey(Gstr1ConstantsV31.INPUT_SID)) {
			CommonUtilV31.validateEmptyString(MapUtils.getString(request, Gstr1ConstantsV31.INPUT_SIGN),
					ErrorCodesV31.ASP103, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP, "GSTR1_BODY_VAL_ASP303", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
			CommonUtilV31.validateEmptyString(MapUtils.getString(request, Gstr1ConstantsV31.INPUT_ST),
					ErrorCodesV31.ASP103, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP, "GSTR1_BODY_VAL_ASP303", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
			CommonUtilV31.validateEmptyString(MapUtils.getString(request, Gstr1ConstantsV31.INPUT_SID),
					ErrorCodesV31.ASP103, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP, "GSTR1_BODY_VAL_ASP303", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
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
			SessionKeyInBytes = AESEncryptionV31.decrypt(headerData.get(Gstr1ConstantsV31.INPUT_SEK),
					AESEncryptionV31.decodeBase64StringTOByte(headerData.get(Gstr1ConstantsV31.INPUT_APP_KEY)));

			// Decryption of rek
			rekbytes = AESEncryptionV31.decrypt(gspresponse.get(Gstr1ConstantsV31.INPUT_REK).toString(),
					SessionKeyInBytes);

			CommonUtilV31.validateEmptyString(MapUtils.getString(gspresponse, Gstr1ConstantsV31.INPUT_DATA),
					ErrorCodesV31.GSP_NULL_RESPONSE, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP, "GSTR1_GSTN_BODY_EMPTY",
					null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

			CommonUtilV31.validateEmptyString(MapUtils.getString(gspresponse, Gstr1ConstantsV31.INPUT_REK),
					ErrorCodesV31.GSP_NULL_RESPONSE, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP, "GSTR1_GSTN_BODY_EMPTY",
					null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

			// Decryption of data from gsp
			String jsonData = new String(AESEncryptionV31.decodeBase64StringTOByte(new String(
					AESEncryptionV31.decrypt(gspresponse.get(Gstr1ConstantsV31.INPUT_DATA).toString(), rekbytes))));
			Gson gson = new Gson();

			responseMap = gson.fromJson(jsonData, Map.class);

			String jsonBody = gson.toJson(responseMap);
			String hmac = HmacGeneratorV31.getHmac(jsonBody, rekbytes);

			// if(!hmac.equals(gspresponse.get(Gstr1ConstantsV31.INPUT_HMAC).toString()))
			// {
			// responseMap.put(Gstr1ConstantsV31.ERROR_CODE,
			// ErrorCodesV31.HMAC_MISMATCH_ERROR);
			// responseMap.put(Gstr1ConstantsV31.ERROR_DESC, "Invalid data
			// received data from GSP");
			// responseMap.put(Gstr1ConstantsV31.ERROR_GRP,
			// Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP);
			// throw new AspExceptionV31("Invalid data received data from GSP",
			// null, false, false, responseMap);
			// }

			log.info("FileGSTR1 API call - Successful decryption of data from GSP - STEP8");

			if (gspresponse.containsKey(Gstr1ConstantsV31.INPUT_SIGN)) {

				dataMap.put(Gstr1ConstantsV31.INPUT_DATA, responseMap);

				CommonUtilV31.validateEmptyString(MapUtils.getString(gspresponse, Gstr1ConstantsV31.INPUT_SIGN),
						ErrorCodesV31.GSP_NULL_RESPONSE, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP, "GSTR1_GSTN_BODY_EMPTY",
						null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

				dataMap.put(Gstr1ConstantsV31.INPUT_SIGN, gspresponse.get(Gstr1ConstantsV31.INPUT_SIGN));
				return dataMap;
			}
		} catch (Exception e) {
			responseMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.DATA_ENCRYPT_ERROR);
			responseMap.put(Gstr1ConstantsV31.ERROR_DESC, "Error in decryption of data from GSP");
			responseMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP);
			throw new AspExceptionV31("Error in decryption of data from GSP", null, false, false, responseMap);
		}
		return responseMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> encryptRequest(Map<String, String> headerData, Map<String, Object> request) {

		byte[] SessionKeyInBytes = null;
		Map<String, Object> responseMap = new HashMap<>();
		Map<String, Object> data = new HashMap<>();
		Gson gson = new Gson();

		try {

			log.info("FileGSTR1 API call - Encrypting data received from client - STEP3");
			// Decryption of Session Key
			SessionKeyInBytes = AESEncryptionV31.decrypt(headerData.get(Gstr1ConstantsV31.INPUT_SEK),
					AESEncryptionV31.decodeBase64StringTOByte(headerData.get(Gstr1ConstantsV31.INPUT_APP_KEY)));

			if (request.containsKey(Gstr1ConstantsV31.INPUT_SIGN)) {
			
				if("DSC".equalsIgnoreCase(String.valueOf(request.get(Gstr1ConstantsV31.INPUT_ST))))
				{
			
					//System.out.println("Gstr1 FILE:Request Body: " + request);

					// add hash and sign validator
					
					String hash = Hash256Util.hash(String.valueOf(request.get(Gstr1ConstantsV31.INPUT_DATA)));

					
					boolean signedDataValue = SignDataVerificationUtil.verifySign(hash, (String.valueOf(request.get(Gstr1ConstantsV31.INPUT_SIGN))));
					
					//signedDataValue = true;
					
					if (signedDataValue == true) {

						String encryptedData = AESEncryptionV31.encryptEK(
								((String) request.get(Gstr1ConstantsV31.INPUT_DATA)).getBytes(), SessionKeyInBytes);

						// Encryption of data from client
						// String jsonData = gson.toJson(data);

						// String baseData =
						// AESEncryptionV31.encodeBase64String(jsonData.getBytes());
						log.debug("=======================jsonData===============");

						// String encryptedData =
						// AESEncryptionV31.encryptEK((String.valueOf(request.get(Gstr1ConstantsV31.INPUT_DATA))).getBytes(),
						// SessionKeyInBytes);

						responseMap.put(Gstr1ConstantsV31.INPUT_SIGN, request.get(Gstr1ConstantsV31.INPUT_SIGN));
						responseMap.put(Gstr1ConstantsV31.INPUT_ST, request.get(Gstr1ConstantsV31.INPUT_ST));
						responseMap.put(Gstr1ConstantsV31.INPUT_SID, request.get(Gstr1ConstantsV31.INPUT_SID));
						responseMap.put(Gstr1ConstantsV31.INPUT_DATA, encryptedData);

					} else {
						responseMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.ASP701);
						responseMap.put(Gstr1ConstantsV31.ERROR_DESC, "Sign Data Validation Failed");
						responseMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP);
						throw new AspExceptionV31("Sign Data Validation Failed", null, false, false, responseMap);
						
					}
			
			}
				
				
				else{//for ESIGN
				String encryptedData = AESEncryptionV31.encryptEK(
						((String) request.get(Gstr1ConstantsV31.INPUT_DATA)).getBytes(), SessionKeyInBytes);

	
				log.debug("=======================jsonData===============");

			
				responseMap.put(Gstr1ConstantsV31.INPUT_SIGN, request.get(Gstr1ConstantsV31.INPUT_SIGN));
				responseMap.put(Gstr1ConstantsV31.INPUT_ST, request.get(Gstr1ConstantsV31.INPUT_ST));
				responseMap.put(Gstr1ConstantsV31.INPUT_SID, request.get(Gstr1ConstantsV31.INPUT_SID));
				responseMap.put(Gstr1ConstantsV31.INPUT_DATA, encryptedData);
				}
			
				
			
			
			} else {

				data = (Map<String, Object>) request.get(Gstr1ConstantsV31.INPUT_DATA);
				String jsonData = gson.toJson(data);

				// new method:start
				String baseData = AESEncryptionV31.encodeBase64String(jsonData.getBytes());
				log.debug("=======================jsonData===============");
				// Encryption of data from client
				String encryptedData = AESEncryptionV31.encryptEK(baseData.getBytes(), SessionKeyInBytes);
				String hmac = HmacGeneratorV31.getHmac(baseData, SessionKeyInBytes);

				// new method end

				/*
				 * // Encryption of data from client String encryptedData =
				 * AESEncryptionV31.encryptEK(jsonData.getBytes(),
				 * SessionKeyInBytes);
				 * 
				 * 
				 * //Generation of HMAC String hmac =
				 * HmacGeneratorV31.getHmac(request.toString(),
				 * SessionKeyInBytes);
				 */
				responseMap.put(Gstr1ConstantsV31.INPUT_DATA, encryptedData);
				responseMap.put(Gstr1ConstantsV31.INPUT_HMAC, hmac);
			}

			responseMap.put(Gstr1ConstantsV31.INPUT_GSTN_ACTION, Gstr1ConstantsV31.TYPE_FILE);

		} catch (Exception e) {
			
			if(e instanceof AspExceptionV31)
			{
				responseMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.ASP701);
				responseMap.put(Gstr1ConstantsV31.ERROR_DESC, "Sign Data Validation Failed");
				responseMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP);
				throw new AspExceptionV31("Sign Data Validation Failed", null, false, false, responseMap);

				
			}else if(e instanceof CMSException){
				responseMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.ASP702);
				responseMap.put(Gstr1ConstantsV31.ERROR_DESC, "Error in Signing data");
				responseMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP);
				throw new AspExceptionV31("Error in Signing data", null, false, false, responseMap);
			}else{
			
			responseMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.DATA_ENCRYPT_ERROR);
			responseMap.put(Gstr1ConstantsV31.ERROR_DESC, "Error in encryption of data from client");
			responseMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_FILE_GRP);
			throw new AspExceptionV31("Error in encryption of data from client", null, false, false, responseMap);
			}
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

}
