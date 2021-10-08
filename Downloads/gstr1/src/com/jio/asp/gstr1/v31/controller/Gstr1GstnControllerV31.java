package com.jio.asp.gstr1.v31.controller;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.asp.gstr1.v30.service.AspAckNumService;
import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.exception.AspExceptionV31;
import com.jio.asp.gstr1.v31.service.AspGstr1FileServiceV31;
import com.jio.asp.gstr1.v31.service.AspGstr1ReturnStatusServiceV31;
import com.jio.asp.gstr1.v31.service.AspGstr1SubmitServiceV31;
import com.jio.asp.gstr1.v31.service.AspLoggingServiceV31;
import com.jio.asp.gstr1.v31.service.GstnL2ServiceV31;
import com.jio.asp.gstr1.v31.service.GstnSummaryServiceV31;
import com.jio.asp.gstr1.v31.service.SaveSuppliesToGstnServiceV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.gstr1.v31.util.ValidationV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.AspException;
import com.jio.asp.validation.CommonUtil;
import com.jio.asp.validation.HeaderValidation;

@RestController
@RequestMapping(value = "/v3.1")
public class Gstr1GstnControllerV31 {
	private static final Logger log = LoggerFactory.getLogger(Gstr1GstnControllerV31.class);

	@Autowired
	private SaveSuppliesToGstnServiceV31 saveSuppliesToGstnServiceV31;
	@Autowired
	private MessageSource messageSourceV31;

	@Autowired
	private AspGstr1ReturnStatusServiceV31 statusService;

	@Autowired
	private AspGstr1FileServiceV31 aspfileservice;

	@Autowired
	AspGstr1SubmitServiceV31 aspsubmitservice;

	@Autowired
	private AspLoggingServiceV31 aspLoggingServiceV31;
	@Autowired
	private GstnSummaryServiceV31 gstnSummaryServiceV31;
	@Autowired
	private AspAckNumService ackService;
	
	@Autowired
	GstnL2ServiceV31 gstnL2ServiceV31;
	

	@RequestMapping(value = "/GSTN", method = RequestMethod.PUT, produces = "application/json")
	public ResponseEntity<Object> saveSuppliesToGstnInBatches(@RequestHeader Map<String, String> reqHeaderMap,
			HttpServletRequest request, @RequestParam Map<String, String> allRequestParams,
			@RequestBody String requestBody) {

		long t1 = System.currentTimeMillis();
		log.debug("saveSuppliesToGstn method: START");
		//ValidationV31.gstnApiHeaderValidation(reqHeaderMap,messageSourceV31);
		HeaderValidation.aspApiHeaderValidation(reqHeaderMap, "01", HttpStatus.BAD_REQUEST);
		ValidationV31.apiRequestParamsValidateForSaveGstn(allRequestParams, messageSourceV31);
		String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_GSTIN, "");
		String paramsGstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, "");
		ValidationV31.apiHeaderAndParamsGstinValidation(hdrGstin, paramsGstin, messageSourceV31);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> payload = null;
		try {
			payload = mapper.readValue(requestBody, new TypeReference<Map<String, String>>() {
			});
			validateInputPayload(payload);
		} catch (IOException e) {
			CommonUtilV31.throwException(ErrorCodesV31.ASP103, Gstr1ConstantsV31.ASP_GSTR1_SAVE_TO_GSTN,
					ErrorCodesV31.ASP103, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, e);
		}
		ResponseEntity<Object> entity = null;
//		String resp1 = saveSuppliesToGstnServiceV31.processSuppliesDataInBatches(allRequestParams, reqHeaderMap,
//				payload);
		String ackNumber = ackService.generateAckNumber(Gstr1ConstantsV31.API_NM);
		saveSuppliesToGstnServiceV31.initiateSavetoGstn(allRequestParams, reqHeaderMap, ackNumber, payload);
		String resp = saveSuppliesToGstnServiceV31.saveSuppliesDataInGstn(allRequestParams, reqHeaderMap,
				payload,ackNumber);
		Map<String, String> respMap=new HashMap<>();
		respMap.put("ref_id", resp);
		entity = new ResponseEntity<>(respMap, HttpStatus.OK);
		String ip = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_CLIENT_ID);
		log.info(
				"Successfully completed the request for API : saveSuppliesToGstn, SOURCE_IP: {}, CLIENT_ID: {}, STATUS: Completed",
				ip, clientID);
		long t2 = System.currentTimeMillis();
		log.info("saveSuppliesToGstn TAT {}", (t2 - t1));
		return entity;
	}

	private void validateInputPayload(Map<String, String> payload) {

		CommonUtilV31.validateEmptyString(payload.get(Gstr1ConstantsV31.INPUT_GT), ErrorCodesV31.ASP504,
				Gstr1ConstantsV31.ASP_GSTR1_SAVE_TO_GSTN, "ASP504", null, HttpStatus.INTERNAL_SERVER_ERROR,
				messageSourceV31);

		CommonUtilV31.validateEmptyString(payload.get(Gstr1ConstantsV31.INPUT_CUR_GT), ErrorCodesV31.ASP504,
				Gstr1ConstantsV31.ASP_GSTR1_SAVE_TO_GSTN, "ASP504", null, HttpStatus.INTERNAL_SERVER_ERROR,
				messageSourceV31);

		CommonUtilV31.validateEmptyString(payload.get(Gstr1ConstantsV31.INPUT_SNAME), ErrorCodesV31.ASP504,
				Gstr1ConstantsV31.ASP_GSTR1_SAVE_TO_GSTN, "ASP504", null, HttpStatus.INTERNAL_SERVER_ERROR,
				messageSourceV31);

	}

	// filegstr1 code
	/**
	 * @param requestMap
	 * @param headers
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/GSTN", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> postGSTR(@RequestBody Map<String, Object> requestMap,
			@RequestHeader Map<String, String> headers, HttpServletRequest request,
			@RequestParam Map<String, String> allRequestParams) throws Exception {
		long t1 = System.currentTimeMillis();
		log.debug("postGSTR method: START");
		log.info("*********************************GSTR1-POST API call STARTS*************************************");
		Map<String, Object> responseMap = new HashMap<>();

	
		ValidationV31.gstnApiHeaderValidation(headers, messageSourceV31);
		String hdrGstin = MapUtils.getString(headers, Gstr1ConstantsV31.HEADER_GSTIN, "");
		String paramsGstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, "");
		ValidationV31.apiHeaderAndParamsGstinValidation(hdrGstin, paramsGstin, messageSourceV31);
		//String payloadGstin= MapUtils.getString(MapUtils.getMap(requestMap, "data"),"gstin");
		
		//ValidationV31.apiHeaderAndPayloadGstinValidation(hdrGstin,payloadGstin, messageSourceV31);
		log.info("GSTR1-POST API call - ValidationV31 Success of Header data - STEP1");

		CommonUtilV31.validateEmptyString(MapUtils.getString(requestMap, Gstr1ConstantsV31.INPUT_GSTN_ACTION),
				ErrorCodesV31.ACTION_MISSING, Gstr1ConstantsV31.ASP_GRP, "GSTR1_BODY_VAL_ASP303", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		if (!Gstr1ConstantsV31.TYPE_FILE.equals(requestMap.get(Gstr1ConstantsV31.INPUT_GSTN_ACTION))
				&& !Gstr1ConstantsV31.TYPE_SUBMIT.equals(requestMap.get(Gstr1ConstantsV31.INPUT_GSTN_ACTION))) {
			responseMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.INVALID_ACTION);
			responseMap.put(Gstr1ConstantsV31.ERROR_DESC, "Invalid action");
			responseMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GRP);
			log.error("GSTR1-POST API call - INvalid action value sent");
			throw new AspExceptionV31("Invalid action", null, false, false, responseMap);
		}

		Map<String, Object> controlDataMap = new HashMap<>();

		controlDataMap.put(Gstr1ConstantsV31.HEADER_GSTIN, headers.get(Gstr1ConstantsV31.HEADER_GSTIN));
		controlDataMap.put(Gstr1ConstantsV31.HEADER_SRC_DEV, headers.get(Gstr1ConstantsV31.HEADER_SRC_DEV));
		controlDataMap.put(Gstr1ConstantsV31.HEADER_DEVICE_STRING, headers.get(Gstr1ConstantsV31.HEADER_DEVICE_STRING));
		controlDataMap.put(Gstr1ConstantsV31.HEADER_LOC, headers.get(Gstr1ConstantsV31.HEADER_LOC));
		controlDataMap.put(Gstr1ConstantsV31.HEADER_APP_CD, headers.get(Gstr1ConstantsV31.HEADER_APP_CD));
		controlDataMap.put(Gstr1ConstantsV31.HEADER_TXN, headers.get(Gstr1ConstantsV31.HEADER_TXN));
		controlDataMap.put(Gstr1ConstantsV31.HEADER_IP, headers.get(Gstr1ConstantsV31.HEADER_IP));
		controlDataMap.put(Gstr1ConstantsV31.HEADER_STATE_CODE, headers.get(Gstr1ConstantsV31.HEADER_STATE_CODE));
		controlDataMap.put(Gstr1ConstantsV31.HEADER_CLIENT_ID, headers.get(Gstr1ConstantsV31.HEADER_CLIENT_ID));
		controlDataMap.put(Gstr1ConstantsV31.HEADER_USER_NAME, headers.get(Gstr1ConstantsV31.HEADER_USER_NAME));
		controlDataMap.put(Gstr1ConstantsV31.INPUT_GSTN_ACTION, headers.get(Gstr1ConstantsV31.INPUT_GSTN_ACTION));

		aspLoggingServiceV31.generateFileControlLog(controlDataMap);
		String action=null;
		// ------------------------------ GSTR1 File
		// ------------------------------------------------------------------------------//
		if (Gstr1ConstantsV31.TYPE_FILE.equals(requestMap.get(Gstr1ConstantsV31.INPUT_GSTN_ACTION))){
			action ="Gstn File Gstr1 ";
			responseMap = aspfileservice.processGstr1Data(headers, requestMap,allRequestParams);
			log.debug("GSTR1-POST API call - FILE action called");
		}
		// ------------------------------ GSTR1 Submit
		// ------------------------------------------------------------------------------//
		else if (Gstr1ConstantsV31.TYPE_SUBMIT.equals(requestMap.get(Gstr1ConstantsV31.INPUT_GSTN_ACTION))){
			action ="Gstn Submit Gstr1 ";
			responseMap = aspsubmitservice.processGstr1Data(headers, requestMap,allRequestParams);
			log.debug("GSTR1-POST API call - SUBMIT action called");
		}

		log.debug("GSTR1-POST API call - Returning the response - STEP9");
		String ip = MapUtils.getString(headers, Gstr1ConstantsV31.HEADER_IP);
		String clientID = MapUtils.getString(headers, Gstr1ConstantsV31.HEADER_CLIENT_ID);
		log.info(
				"Successfully completed the request for API : {}, SOURCE_IP: {}, CLIENT_ID: {}, STATUS: Completed",
				action,ip, clientID);
		long t2 = System.currentTimeMillis();
		log.info("{} TAT {}",action, (t2 - t1));
		log.debug("******************************GSTR1-POST API call ENDS****************************************");
		return responseMap;
	}

	/**
	 * Exception handler for summary page. Any exception which occurs in
	 * downstream code will come to this method and this method generates
	 * appropriate output for the client.
	 * 
	 * @param request
	 *            Http request object
	 * @param ex
	 *            exception object
	 * @return responseentity containing response json.
	 */
	@ExceptionHandler({AspException.class, AspExceptionV31.class, SocketTimeoutException.class, ResourceAccessException.class })
	public ResponseEntity<Map<String, Object>> exceptionHandler(HttpServletRequest request, Exception ex) {
		log.error("Requested URL=" + request.getRequestURL());
		ResponseEntity<Map<String, Object>> entity = null;
		Map<String, Object> exObject;

		if (ex instanceof AspExceptionV31) {
			exObject = ((AspExceptionV31) ex).getExcObj();
			HttpStatus status = (HttpStatus) MapUtils.getObject(exObject, Gstr1ConstantsV31.ERROR_HTTP_CODE,
					HttpStatus.INTERNAL_SERVER_ERROR);
			entity = new ResponseEntity<>(exObject, status);
			exObject.remove(Gstr1ConstantsV31.ERROR_HTTP_CODE);
			log.error("exceptionHandler method : AspExceptionV31 error occurred.{}", ex);
		} 
		else if ( ex instanceof AspException) {
			exObject = ((AspException) ex).getExcObj();
//			HttpStatus status = (HttpStatus) MapUtils.getObject(exObject, Gstr1ConstantsV31.ERROR_HTTP_CODE,
//					HttpStatus.INTERNAL_SERVER_ERROR);
			HttpStatus status = (HttpStatus) exObject.get(AspConstants.ERROR_HTTP_CODE);
			if(status==null){
				status=HttpStatus.INTERNAL_SERVER_ERROR;
			}
			entity = new ResponseEntity<>(exObject, status);
			exObject.remove(Gstr1ConstantsV31.ERROR_HTTP_CODE);
			log.error("exceptionHandler method : AspException error occurred.{}", ex);
		}
		else if (ex instanceof SocketTimeoutException || ex instanceof ResourceAccessException) {
			exObject = new HashMap<>();
			exObject.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP_CONNECTION_ERROR);
			exObject.put(Gstr1ConstantsV31.ERROR_DESC, "Connection Error: Unable to connect to destination");
			exObject.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1);
			log.error("exceptionHandler method : Connection error occurred.{}", ex);
			entity = new ResponseEntity<>(exObject, HttpStatus.GATEWAY_TIMEOUT);
		}  else {
			exObject = new HashMap<>();
			exObject.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSTR1_PARSE_GSP_GEN_ERROR);
			exObject.put(Gstr1ConstantsV31.ERROR_DESC, "Unable to process request, some generic error occurred");
			exObject.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GRP);
			log.error("exceptionHandler method : Generic error occurred.{}", ex);
			entity = new ResponseEntity<>(exObject, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return entity;
	}

	@RequestMapping(value = "/GSTN", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getSupplies(@RequestHeader Map<String, String> reqHeaderMap,
			@RequestParam Map<String, String> allRequestParams, HttpServletResponse response) {

		// getSummaryData for L0
		long t1 = System.currentTimeMillis();
		log.debug("getSummaryL2 method START");
		//ValidationV31.gstnApiHeaderValidation(reqHeaderMap, messageSourceV31);
		HeaderValidation.gstnApiHeaderValidation(reqHeaderMap, "01", HttpStatus.BAD_REQUEST);
		String level = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_LEVEL, null);
		String gstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, null);
		String fp = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_FP, null);
		String apiName = "Invalid";
		ResponseEntity<Object> entity = null;
		String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_GSTIN, "");
		ValidationV31.apiHeaderAndParamsGstinValidation(hdrGstin, gstin, messageSourceV31);
		if (Gstr1ConstantsV31.TYPE_STATUS.equals(level)) {
			apiName = "Return Status";
			log.info("GSTR1 Return Status (after SaveToGSTN) API call -  start - STEP1");
			Map<String, Object> responseMap = new HashMap<>();
			statusService.validateApiInput(allRequestParams);
			responseMap = statusService.processGstr1Data(reqHeaderMap, allRequestParams);
			entity = new ResponseEntity<>(responseMap, HttpStatus.OK);
			log.info("GSTR1 Return Status (after SaveToGSTN) API call - Returning response to client - STEP6");
		} else if (Gstr1ConstantsV31.SUMMARY_TYPE_L2.equalsIgnoreCase(level)) {
			apiName = "Supplies Summary L2";
			Map<String, String> data = gstnSummaryServiceV31.validateApiInput(allRequestParams,Gstr1ConstantsV31.GSTN_L2);
			  data = gstnSummaryServiceV31.validateApiInputL2(allRequestParams,Gstr1ConstantsV31.GSTN_L2);
			// aspSectionL3Service.validateApiInputForL3Specific(inputMap);
			log.info("Summary API call - L2 ValidationV31 Success of Input data - STEP3");
			Map<String, Object> responseMap = null;
//			responseMap = gstnSummaryServiceV31.processGstr1InvoiceDataL2(reqHeaderMap, data);
			responseMap = gstnL2ServiceV31.processL2(reqHeaderMap, data);
			entity = new ResponseEntity<>(responseMap, HttpStatus.OK);
		} else if (Gstr1ConstantsV31.SUMMARY_TYPE_L0.equalsIgnoreCase(level)) {
			apiName = "Supplies Summary L0";
			Map<String, String> data = gstnSummaryServiceV31.validateApiInput(allRequestParams,Gstr1ConstantsV31.GSTN_L0);
			log.info("Summary API call - L0 Validation Success of Input data - STEP3");
			Map<String, Object> responseMap = null;
			responseMap = gstnSummaryServiceV31.processGstr1InvoiceDataL0(reqHeaderMap, data);
			entity = new ResponseEntity<>(responseMap, HttpStatus.OK);
		} else if(level !=null) {
			
			
			CommonUtil.throwException(ErrorCodesV31.ASP011185, Gstr1ConstantsV31.GSTN_L0_L2, null, HttpStatus.BAD_REQUEST,
					null, AspConstants.FORM_CODE);
			 
		}else{

			
			CommonUtil.validateEmptyString(MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_LEVEL,""),
					ErrorCodesV31.ASP011181, Gstr1ConstantsV31.GSTN_L0_L2, null,
					HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
		}
		log.info("******************************Summary API call ENDS****************************************");
		String ip = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_CLIENT_ID);
		log.info("Successfully completed the request for API : {}, SOURCE_IP: {}, CLIENT_ID: {}, STATUS: Completed",
				apiName, ip, clientID);
		long t2 = System.currentTimeMillis();
		log.info("TAT {}", (t2 - t1));
		return entity;

	}

	@RequestMapping(value = "/GSTN/L2", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getSuppliesBulk(@RequestHeader Map<String, String> reqHeaderMap,
			@RequestParam Map<String, String> allRequestParams, HttpServletResponse response) {

		// getSummaryData for L0
		long t1 = System.currentTimeMillis();
		log.debug("getSummaryL2 method START");
		//ValidationV31.gstnApiHeaderValidation(reqHeaderMap, messageSourceV31);
		HeaderValidation.gstnApiHeaderValidation(reqHeaderMap, "01", HttpStatus.BAD_REQUEST);
		String level = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_LEVEL, null);
		String gstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, null);
		String fp = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_FP, null);
		String apiName = "Invalid";
		ResponseEntity<Object> entity = null;
		String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_GSTIN, "");
		ValidationV31.apiHeaderAndParamsGstinValidation(hdrGstin, gstin, messageSourceV31);

		apiName = "Supplies Summary L2";
		Map<String, String> data = gstnSummaryServiceV31.validateApiInput(allRequestParams,Gstr1ConstantsV31.GSTN_L2);
		data = gstnSummaryServiceV31.validateApiInputL2(allRequestParams,Gstr1ConstantsV31.GSTN_L2);
		// aspSectionL3Service.validateApiInputForL3Specific(inputMap);
		log.info("Summary API call - L2 ValidationV31 Success of Input data - STEP3");
		Map<String, Object> responseMap = null;
		responseMap = gstnL2ServiceV31.processL2(reqHeaderMap, data);
		entity = new ResponseEntity<>(responseMap, HttpStatus.OK);

		String ip = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_CLIENT_ID);
		log.info("Successfully completed the request for API : {}, SOURCE_IP: {}, CLIENT_ID: {}, STATUS: Completed",
				apiName, ip, clientID);
		long t2 = System.currentTimeMillis();
		log.info("TAT {}", (t2 - t1));
		return entity;

		
	}

}
