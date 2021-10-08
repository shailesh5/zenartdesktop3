package com.jio.asp.gstr1.v30.controller;

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

import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.exception.AspException;
import com.jio.asp.gstr1.v30.service.AspGstr1FileService;
import com.jio.asp.gstr1.v30.service.AspGstr1ReturnStatusService;
import com.jio.asp.gstr1.v30.service.AspGstr1SubmitService;
import com.jio.asp.gstr1.v30.service.AspLoggingService;
import com.jio.asp.gstr1.v30.service.GSPService;
import com.jio.asp.gstr1.v30.service.GstnSummaryService;
import com.jio.asp.gstr1.v30.service.SaveSuppliesToGstnService;
import com.jio.asp.gstr1.v30.util.CommonUtil;
import com.jio.asp.gstr1.v30.util.Validation;

@RestController
@RequestMapping(value = "/v3")
public class Gstr1GstnControllerV3 {
	private static final Logger log = LoggerFactory.getLogger(Gstr1GstnControllerV3.class);

	@Autowired
	private SaveSuppliesToGstnService saveSuppliesToGstnService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	AspGstr1FileService aspfileservice;
	@Autowired
	GSPService gspService;
	@Autowired
	AspGstr1SubmitService aspsubmitservice;
	@Autowired
	AspGstr1ReturnStatusService statusService;
	@Autowired
	private AspLoggingService aspLoggingService;
	@Autowired
	private GstnSummaryService gstnSummaryService;

	@RequestMapping(value = "/GSTN", method = RequestMethod.PUT, produces = "application/json")
	public ResponseEntity<Object> saveSuppliesToGstn(@RequestHeader Map<String, String> reqHeaderMap,
			HttpServletRequest request, @RequestParam Map<String, String> allRequestParams) {

		long t1 = System.currentTimeMillis();
		log.debug("saveSuppliesToGstn method: START");
		Validation.gstnApiHeaderValidation(reqHeaderMap, messageSource);
		String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_GSTIN, "");
		String paramsGstin = MapUtils.getString(allRequestParams, Gstr1Constants.INPUT_GSTN, "");
		Validation.apiHeaderAndParamsGstinValidation(hdrGstin, paramsGstin, messageSource);
		validateInputParams(allRequestParams);
        ResponseEntity<Object> entity = null;
		String resp1 = saveSuppliesToGstnService.processSuppliesData(allRequestParams, reqHeaderMap);
		entity = new ResponseEntity<>(resp1, HttpStatus.OK);
		String ip = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_CLIENT_ID);
		log.info(
				"Successfully completed the request for API : saveSuppliesToGstn, SOURCE_IP: {}, CLIENT_ID: {}, STATUS: Completed",
				ip, clientID);
		long t2 = System.currentTimeMillis();
		log.info("TAT {}", (t2 - t1));
		return entity;
	}
	
	private void validateInputParams(Map<String,String> allRequestParams){
		
		CommonUtil.validateEmptyString(allRequestParams.get(Gstr1Constants.INPUT_GT), ErrorCodes.ASP504, Gstr1Constants.ASP_GSTR1_SAVE_TO_GSTN, "ASP504", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(allRequestParams.get(Gstr1Constants.INPUT_CUR_GT), ErrorCodes.ASP504, Gstr1Constants.ASP_GSTR1_SAVE_TO_GSTN, "ASP504", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		
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
			@RequestHeader Map<String, String> headers, HttpServletRequest request) throws Exception {

		Map<String, Object> responseMap = new HashMap<>();

		log.info("*********************************GSTR1-POST API call STARTS*************************************");

		// aspfileservice.validateHeaderInput(headers);
		// to add new header validation

		// Validation.aspApiHeaderValidation(requestMap, messageSource);

		log.info("GSTR1-POST API call - Validation Success of Header data - STEP1");

		CommonUtil.validateEmptyString(MapUtils.getString(requestMap, Gstr1Constants.INPUT_GSTN_ACTION),
				ErrorCodes.ACTION_MISSING, Gstr1Constants.ASP_GRP, "GSTR1_BODY_VAL_ASP303", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		if (!Gstr1Constants.TYPE_FILE.equals(requestMap.get(Gstr1Constants.INPUT_GSTN_ACTION))
				&& !Gstr1Constants.TYPE_SUBMIT.equals(requestMap.get(Gstr1Constants.INPUT_GSTN_ACTION))) {
			responseMap.put(Gstr1Constants.ERROR_CODE, ErrorCodes.INVALID_ACTION);
			responseMap.put(Gstr1Constants.ERROR_DESC, "Invalid action");
			responseMap.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GRP);
			throw new AspException("Invalid action", null, false, false, responseMap);
		}

		Map<String, Object> controlDataMap = new HashMap<>();

		controlDataMap.put(Gstr1Constants.HEADER_GSTIN, headers.get(Gstr1Constants.HEADER_GSTIN));
		controlDataMap.put(Gstr1Constants.HEADER_SRC_DEV, headers.get(Gstr1Constants.HEADER_SRC_DEV));
		controlDataMap.put(Gstr1Constants.HEADER_DEVICE_STRING, headers.get(Gstr1Constants.HEADER_DEVICE_STRING));
		controlDataMap.put(Gstr1Constants.HEADER_LOC, headers.get(Gstr1Constants.HEADER_LOC));
		controlDataMap.put(Gstr1Constants.HEADER_APP_CD, headers.get(Gstr1Constants.HEADER_APP_CD));
		controlDataMap.put(Gstr1Constants.HEADER_TXN, headers.get(Gstr1Constants.HEADER_TXN));
		controlDataMap.put(Gstr1Constants.HEADER_IP, headers.get(Gstr1Constants.HEADER_IP));
		controlDataMap.put(Gstr1Constants.HEADER_STATE_CODE, headers.get(Gstr1Constants.HEADER_STATE_CODE));
		controlDataMap.put(Gstr1Constants.HEADER_CLIENT_ID, headers.get(Gstr1Constants.HEADER_CLIENT_ID));
		controlDataMap.put(Gstr1Constants.HEADER_USER_NAME, headers.get(Gstr1Constants.HEADER_USER_NAME));
		controlDataMap.put(Gstr1Constants.INPUT_GSTN_ACTION, headers.get(Gstr1Constants.INPUT_GSTN_ACTION));

		aspLoggingService.generateFileControlLog(controlDataMap);

		// ------------------------------ GSTR1 File
		// ------------------------------------------------------------------------------//
		if (Gstr1Constants.TYPE_FILE.equals(requestMap.get(Gstr1Constants.INPUT_GSTN_ACTION)))
			responseMap = aspfileservice.processGstr1Data(headers, requestMap);

		// ------------------------------ GSTR1 Submit
		// ------------------------------------------------------------------------------//
		if (Gstr1Constants.TYPE_SUBMIT.equals(requestMap.get(Gstr1Constants.INPUT_GSTN_ACTION)))
			responseMap = aspsubmitservice.processGstr1Data(headers, requestMap);

		log.info("GSTR1-POST API call - Returning the response - STEP9");
		log.info("******************************GSTR1-POST API call ENDS****************************************");

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
	@ExceptionHandler({ AspException.class, SocketTimeoutException.class, ResourceAccessException.class })
	public ResponseEntity<Map<String, Object>> exceptionHandler(HttpServletRequest request, Exception ex) {
		log.error("Requested URL=" + request.getRequestURL());
		ResponseEntity<Map<String, Object>> entity = null;
		Map<String, Object> exObject;

		if (ex instanceof AspException) {
			exObject = ((AspException) ex).getExcObj();
			HttpStatus status = (HttpStatus) MapUtils.getObject(exObject, Gstr1Constants.ERROR_HTTP_CODE,
					HttpStatus.INTERNAL_SERVER_ERROR);
			entity = new ResponseEntity<>(exObject, status);
			exObject.remove(Gstr1Constants.ERROR_HTTP_CODE);
			log.error("exceptionHandler method : AspException error occurred.{}", ex);
		} else if (ex instanceof SocketTimeoutException || ex instanceof ResourceAccessException) {
			exObject = new HashMap<>();
			exObject.put(Gstr1Constants.ERROR_CODE, ErrorCodes.GSP_CONNECTION_ERROR);
			exObject.put(Gstr1Constants.ERROR_DESC, "Connection Error: Unable to connect to destination");
			exObject.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GSTR1_GRP);
			log.error("exceptionHandler method : Connection error occurred.{}", ex);
			entity = new ResponseEntity<>(exObject, HttpStatus.GATEWAY_TIMEOUT);
		} else if (ex instanceof SocketTimeoutException || ex instanceof ResourceAccessException) {
			exObject = new HashMap<>();
			exObject.put(Gstr1Constants.ERROR_CODE, ErrorCodes.GSP_CONNECTION_ERROR);
			exObject.put(Gstr1Constants.ERROR_DESC, "Connection Error: Unable to connect to destination");
			exObject.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GRP);
			log.error("exceptionHandler method : Connection error occurred.{}", ex);
			entity = new ResponseEntity<>(exObject, HttpStatus.GATEWAY_TIMEOUT);
		} else {
			exObject = new HashMap<>();
			exObject.put(Gstr1Constants.ERROR_CODE, ErrorCodes.GSTR1_PARSE_GSP_GEN_ERROR);
			exObject.put(Gstr1Constants.ERROR_DESC, "Unable to process request, some generic error occurred");
			exObject.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GRP);
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
		Validation.gstnApiHeaderValidation(reqHeaderMap, messageSource);
		String level = MapUtils.getString(allRequestParams, Gstr1Constants.INPUT_LEVEL, null);
		String gstin = MapUtils.getString(allRequestParams, Gstr1Constants.INPUT_GSTN, null);
		String fp = MapUtils.getString(allRequestParams, Gstr1Constants.INPUT_FP, null);
		String apiName = "Gstin Summary L2";
		ResponseEntity<Object> entity = null;
		String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_GSTIN, "");
		Validation.apiHeaderAndParamsGstinValidation(hdrGstin, gstin, messageSource);
		if (Gstr1Constants.TYPE_STATUS.equals(level)) {
			apiName = "Return Status";
			log.info("GSTR1 Return Status API call -  start - STEP1");
			Map<String, Object> responseMap = new HashMap<>();
			statusService.validateApiInput(allRequestParams);
			responseMap = statusService.processGstr1Data(reqHeaderMap, allRequestParams);
			entity = new ResponseEntity<>(responseMap, HttpStatus.OK);
			log.info("GSTR1 Return Status API call - Returning response to client - STEP6");
		} else if (Gstr1Constants.SUMMARY_TYPE_L2.equalsIgnoreCase(level)) {
			apiName = "Supplies Summary L2";
			Map<String, String> data = gstnSummaryService.validateApiInput(allRequestParams);
			  data = gstnSummaryService.validateApiInputL2(allRequestParams);
			// aspSectionL3Service.validateApiInputForL3Specific(inputMap);
			log.info("Summary API call - L2 Validation Success of Input data - STEP3");
			Map<String, Object> responseMap = null;
			responseMap = gstnSummaryService.processGstr1InvoiceDataL2(reqHeaderMap, data);
			entity = new ResponseEntity<>(responseMap, HttpStatus.OK);
		} else if (Gstr1Constants.SUMMARY_TYPE_L0.equalsIgnoreCase(level)) {
			Map<String, String> data = gstnSummaryService.validateApiInput(allRequestParams);
			log.info("Summary API call - L0 Validation Success of Input data - STEP3");
			Map<String, Object> responseMap = null;
			responseMap = gstnSummaryService.processGstr1InvoiceDataL0(reqHeaderMap, data);
			entity = new ResponseEntity<>(responseMap, HttpStatus.OK);
		} else {
			CommonUtil.validateEmptyString(level, ErrorCodes.ASP504, Gstr1Constants.ASP_GSTR1_GRP, "ASP504", null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		}

		log.info("******************************Summary API call ENDS****************************************");
		String ip = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_CLIENT_ID);
		log.info("Successfully completed the request for API : {}, SOURCE_IP: {}, CLIENT_ID: {}, STATUS: Completed",
				apiName, ip, clientID);
		long t2 = System.currentTimeMillis();
		log.info("TAT {}", (t2 - t1));
		return entity;

	}

	/**
	 * Method converts request data to map for further usage in business logic.
	 * Any new parameter which is getting added in the request uri this code
	 * needs to be modified.
	 * 
	 * @param request
	 *            http request
	 * @return Input map containing request data
	 */
	private Map<String, Object> parseApiInput(HttpServletRequest request) {
		Map<String, Object> inputMap = new HashMap<>();

		inputMap.put(Gstr1Constants.INPUT_ACTION, request.getParameter(Gstr1Constants.INPUT_ACTION));

		inputMap.put(Gstr1Constants.INPUT_FILTER, request.getParameter(Gstr1Constants.INPUT_FILTER));

		String section = request.getParameter(Gstr1Constants.INPUT_SECTION);

		inputMap.put(Gstr1Constants.INPUT_SECTION, section);
		inputMap.put(Gstr1Constants.INPUT_GSTN, request.getParameter(Gstr1Constants.INPUT_GSTN));
		String retMonth = request.getParameter(Gstr1Constants.INPUT_RETMONTH);
		String retYear = request.getParameter(Gstr1Constants.INPUT_RETYEAR);
		inputMap.put(Gstr1Constants.INPUT_RETPERIOD, retMonth + retYear);
		inputMap.put(Gstr1Constants.INPUT_RETMONTH, retMonth);
		inputMap.put(Gstr1Constants.INPUT_RETYEAR, retYear);

		inputMap.put(Gstr1Constants.INPUT_OFFSET, request.getParameter(Gstr1Constants.INPUT_OFFSET));
		inputMap.put(Gstr1Constants.INPUT_LIMIT, request.getParameter(Gstr1Constants.INPUT_LIMIT));

		return inputMap;
	}

}
