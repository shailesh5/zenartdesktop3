package com.jio.asp.gstr1.v30.controller;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.exception.AspException;
import com.jio.asp.gstr1.v30.service.AspAckNumService;
import com.jio.asp.gstr1.v30.service.AspSuppliesStatusService;
import com.jio.asp.gstr1.v30.service.AspSuppliesSumService;
import com.jio.asp.gstr1.v30.service.Gstr1ProducerService;
import com.jio.asp.gstr1.v30.service.Gstr1SummaryService;
import com.jio.asp.gstr1.v30.util.CommonUtil;
import com.jio.asp.gstr1.v30.util.Validation;

@RestController
@RequestMapping(value = "/v3")
public class Gstr1AspControllerV3 {
	private static final Logger log = LoggerFactory.getLogger(Gstr1AspControllerV3.class);

	@Autowired
	private Gstr1ProducerService prodService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private AspAckNumService ackService;
	@Autowired
	private Gstr1SummaryService gstr1SummaryService;
	@Autowired
	private AspSuppliesStatusService statusService;
	@Autowired
	private AspSuppliesSumService dasboardService;

	@RequestMapping(value = "/Supplies", method = RequestMethod.PUT, produces = "application/json")
	public Map<String, String> saveSupplies(@RequestHeader Map<String, String> reqHeaderMap, HttpServletRequest request,
			@RequestBody String requestBody, @RequestParam Map<String, String> allRequestParams) {

		long t1 = System.currentTimeMillis();
		log.debug("saveSupplies method: START");
		Validation.aspApiHeaderValidation(reqHeaderMap, messageSource);
		String ackNo = ackService.generateAckNumber(Gstr1Constants.API_NM);
		log.debug("saveSupplies method: calling convertor api for processing the json for ackNo: {}", ackNo);
		long t1_1 = System.currentTimeMillis();
		String jsonString = prodService.invokeConverterApi(requestBody, reqHeaderMap, ackNo);
		long t1_2 = System.currentTimeMillis();
		log.info("Trans Id {} Converter:TAT {}", ackNo, (t1_2 - t1_1));
		log.debug("saveSupplies method: response receivd from convertor api after processing the json for ackNo: {}",
				ackNo);
		log.info("saveSupplies method: response receivd from convertor api after processing the json for ackNo: {}",
				ackNo);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> payload = null;
		try {
			log.debug("saveSupplies method: processing convertor api json to Map object for ackNo: {}", ackNo);
			payload = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
			});
			String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_GSTIN, "");
			String payloadGstin = MapUtils.getString(payload, Gstr1Constants.INPUT_GSTN, "");
			String paramsGstin = MapUtils.getString(allRequestParams, Gstr1Constants.INPUT_GSTN, "");
			Validation.apiHeaderAndPayloadGstinValidation(hdrGstin, payloadGstin, messageSource);
			Validation.apiHeaderAndParamsGstinValidation(hdrGstin, paramsGstin, messageSource);
			log.debug("saveSupplies method: Sucess in convertor api json to Map object for ackNo: {}", ackNo);
			log.info("saveSupplies method: sucessfully parsed the json for ackNo: {}", ackNo);
		} catch (IOException e) {
			CommonUtil.throwException(ErrorCodes.ASP103, Gstr1Constants.ASP_GSTR1A_SAVE_GRP, ErrorCodes.ASP103, null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource, e);
		}
		log.debug("saveSupplies method: Starting save opertaion for ackNo: {}", ackNo);
		Map<String, String> resp1 = prodService.saveInvoiceV4(payload, reqHeaderMap, ackNo);
		log.debug("saveSupplies method: Processing done!!! for ackNo: {}", ackNo);
		log.debug("saveSupplies method: END  for ackNo: {}", ackNo);
		String ip = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_CLIENT_ID);
		log.info(
				"Successfully completed the request for API : SaveSupplies, SOURCE_IP: {}, CLIENT_ID: {}, STATUS: Completed",
				ip, clientID);
		long t2 = System.currentTimeMillis();
		log.info("Trans Id {} TAT {}", ackNo, (t2 - t1));
		return resp1;
	}

	@RequestMapping(value = "/Supplies", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getSupplies(@RequestHeader Map<String, String> reqHeaderMap,
			@RequestParam Map<String, String> allRequestParams, HttpServletResponse response) {
		// getSummaryData for L0
		long t1 = System.currentTimeMillis();
		log.debug("getSummaryL0 method START");
		Validation.aspApiHeaderValidation(reqHeaderMap, messageSource);
		String level = MapUtils.getString(allRequestParams, Gstr1Constants.INPUT_LEVEL, null);
		String gstin = MapUtils.getString(allRequestParams, Gstr1Constants.INPUT_GSTN, null);
		String fp = MapUtils.getString(allRequestParams, Gstr1Constants.INPUT_FP, null);
		String status = MapUtils.getString(allRequestParams, Gstr1Constants.INPUT_STATUS, null);
		String apiName = "Supplies Summary L0";
		ResponseEntity<Object> entity = null;
		String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_GSTIN, "");
		Validation.apiHeaderAndParamsGstinValidation(hdrGstin, gstin, messageSource);
		if (Gstr1Constants.TYPE_STATUS.equals(level)) {
			apiName = "Supplies Status";
			statusService.validateApiInput(allRequestParams);
			log.debug("getSupplies method: Starting get STATUS opertaion for ackNo: {}");
			String statusData = statusService.getSuppliesStatus(allRequestParams);
			entity = new ResponseEntity<>(statusData, HttpStatus.OK);
			log.debug("getSupplies method: Starting  get STATUS opertaion for ackNo: {}");
			log.debug("getSupplies method: END  get STATUS for ackNo: {}");
		} else if (Gstr1Constants.SUMMARY_TYPE_L2.equalsIgnoreCase(level)) {
			apiName = "Supplies Summary L2";
			Map<String, String> data = dasboardService.validateApiInput(allRequestParams);
			// aspSectionL3Service.validateApiInputForL3Specific(inputMap);
			log.info("Summary API call - L3 Validation Success of Input data - STEP3");
			Map<String, Object> responseMap = null;
			responseMap = dasboardService.processGstr1InvoiceDataL2(reqHeaderMap, data);
			entity = new ResponseEntity<>(responseMap, HttpStatus.OK);
		} else {
			validateParams(gstin, fp, level);
			Map resp = gstr1SummaryService.processGstr1SummaryL0(allRequestParams);
			entity = new ResponseEntity<>(resp, HttpStatus.OK);
			log.info("Summary API call - Returning the response - STEP9");
		}

		log.info("******************************getSupplies API call ENDS****************************************");
		String ip = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_CLIENT_ID);
		log.info("Successfully completed the request for API : {}, SOURCE_IP: {}, CLIENT_ID: {}, STATUS: Completed",
				apiName, ip, clientID);
		long t2 = System.currentTimeMillis();
		log.info("TAT {}", (t2 - t1));
		return entity;
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
		}
		return entity;
	}

	private void validateParams(String gstin, String fp, String level) {

		CommonUtil.validateEmptyString(gstin, ErrorCodes.ASP504, Gstr1Constants.L0_SUMMARY, "ASP504", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(fp, ErrorCodes.ASP504, Gstr1Constants.L0_SUMMARY, "ASP504", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(level, ErrorCodes.ASP504, Gstr1Constants.L0_SUMMARY, "ASP504", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

	}

}
