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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.exception.AspExceptionV31;
import com.jio.asp.gstr1.v31.service.AspAckNumServiceV31;
import com.jio.asp.gstr1.v31.service.AspSuppliesStatusServiceV31;
import com.jio.asp.gstr1.v31.service.AspSuppliesSumServiceV31;
import com.jio.asp.gstr1.v31.service.DoSyncServiceV31Impl;
import com.jio.asp.gstr1.v31.service.Gstr1ProducerServiceV31;
import com.jio.asp.gstr1.v31.service.Gstr1SummaryServiceV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.gstr1.v31.util.ValidationV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.AspException;
import com.jio.asp.validation.CommonUtil;
import com.jio.asp.validation.ErrorCodes;
import com.jio.asp.validation.HeaderValidation;
 

@RestController
@RequestMapping(value = "/v3.1")
public class Gstr1AspControllerV31 {
	String L3 = "L3";
	private static final Logger log = LoggerFactory.getLogger(Gstr1AspControllerV31.class);

	@Autowired
	private Gstr1ProducerServiceV31 prodService;
	@Autowired
	private MessageSource messageSourceV31;
	@Autowired
	private AspAckNumServiceV31 ackService;
	@Autowired
	private Gstr1SummaryServiceV31 gstr1SummaryServiceV31;
	@Autowired
	private AspSuppliesStatusServiceV31 statusService;
	@Autowired
	private AspSuppliesSumServiceV31 dasboardService;
	@Autowired
	private DoSyncServiceV31Impl syncService;
	
	
	
	  

	@RequestMapping(value = "/Supplies", method = RequestMethod.PUT, produces = "application/json")
	public Map<String, String> saveSupplies(@RequestHeader Map<String, String> reqHeaderMap, HttpServletRequest request,
			@RequestBody String requestBody, @RequestParam Map<String, String> allRequestParams) {

		long t1 = System.currentTimeMillis();
		log.debug("saveSupplies method: START");
//		ValidationV31.aspApiHeaderValidation(reqHeaderMap, messageSourceV31); 
		HeaderValidation.aspApiHeaderValidation(reqHeaderMap,"01",HttpStatus.BAD_REQUEST);
		
		String ackNo = ackService.generateAckNumber(Gstr1ConstantsV31.API_NM);
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
		mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
		Map<String, Object> payload = null;
		try {
			log.debug("saveSupplies method: processing convertor api json to Map object for ackNo: {}", ackNo);
			payload = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
			});
			String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_GSTIN, "");
			String payloadGstin = MapUtils.getString(payload, Gstr1ConstantsV31.INPUT_GSTN, "");
			String paramsGstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, "");
			ValidationV31.apiHeaderAndPayloadGstinValidation(hdrGstin, payloadGstin, messageSourceV31);
			ValidationV31.apiHeaderAndParamsGstinValidation(hdrGstin, paramsGstin, messageSourceV31);
			log.debug("saveSupplies method: Sucess in convertor api json to Map object for ackNo: {}", ackNo);
			log.info("saveSupplies method: sucessfully parsed the json for ackNo: {}", ackNo);
		} catch (IOException e) {
		 
			CommonUtil.throwException(ErrorCodes.ASP1022, AspConstants.JIOGST_SAVE, null, HttpStatus.BAD_REQUEST,
					null, AspConstants.FORM_CODE);
		}
		log.debug("saveSupplies method: Starting save opertaion for ackNo: {}", ackNo);
		Map<String, String> resp1 = prodService.saveInvoice(payload, reqHeaderMap, ackNo);
		log.debug("saveSupplies method: Processing done!!! for ackNo: {}", ackNo);
		log.debug("saveSupplies method: END  for ackNo: {}", ackNo);
		String ip = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_CLIENT_ID);
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
	//	ValidationV31.aspApiHeaderValidation(reqHeaderMap, messageSourceV31);
		HeaderValidation.aspApiHeaderValidation(reqHeaderMap, "01", HttpStatus.BAD_REQUEST);
		String level = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_LEVEL, null);
		String gstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, null);
		String fp = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_FP, null);
		String apiName = "Supplies Summary L0";
		ResponseEntity<Object> entity = null;
		String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_GSTIN, "");
		ValidationV31.apiHeaderAndParamsGstinValidation(hdrGstin, gstin, messageSourceV31);
		if (Gstr1ConstantsV31.TYPE_STATUS.equals(level)) {
			apiName = "Supplies Status";
			statusService.validateApiInput(allRequestParams);
			log.debug("getSupplies method: Starting get STATUS opertaion for ackNo: {}");
			String statusData = statusService.getSuppliesStatus(allRequestParams);
			entity = new ResponseEntity<>(statusData, HttpStatus.OK);
			log.debug("getSupplies method: Starting  get STATUS opertaion for ackNo: {}");
			log.debug("getSupplies method: END  get STATUS for ackNo: {}");
		} else if (Gstr1ConstantsV31.SUMMARY_TYPE_L2.equalsIgnoreCase(level)) {
			
			//******************* SYNC FUNCTIONALITY ********************//
			
				apiName = "Supplies Summary L2";
				Map<String, String> data = dasboardService.validateApiInput(allRequestParams);
				
				dasboardService.validateFilterInput(allRequestParams);
				// aspSectionL3Service.validateApiInputForL3Specific(inputMap);
				log.info("Summary API call - L3 ValidationV31 Success of Input data - STEP3");
				Map<String, Object> responseMap = null;
				responseMap = dasboardService.processGstr1InvoiceDataL2(reqHeaderMap, data);
				entity = new ResponseEntity<>(responseMap, HttpStatus.OK);
			
		}

		else if  (Gstr1ConstantsV31.SUMMARY_TYPE_L0.equalsIgnoreCase(level)){
			gstr1SummaryServiceV31.validateParams(allRequestParams);

			Map resp = gstr1SummaryServiceV31.processGstr1SummaryL0(allRequestParams);
			entity = new ResponseEntity<>(resp, HttpStatus.OK);
			log.info("Summary API call - Returning the response - STEP9");
		} else {
			CommonUtil.validateEmptyString(MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_LEVEL,""),
					ErrorCodesV31.ASP011181, Gstr1ConstantsV31.JIOGST_L0_L2, null,
					HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

			 
		}

		log.info("******************************getSupplies API call ENDS****************************************");
		String ip = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_CLIENT_ID);
		log.info("Successfully completed the request for API : {}, SOURCE_IP: {}, CLIENT_ID: {}, STATUS: Completed",
				apiName, ip, clientID);
		long t2 = System.currentTimeMillis();
		log.info("TAT {}", (t2 - t1));
		return entity;
	}
	
	
	
	
	@RequestMapping(value = "/Supplies", method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> syncData(@RequestHeader Map<String, String> headers,
                  @RequestParam Map<String, String> allRequestParams) {

		   String apiName = "DO SYNC";
           long t1 = System.currentTimeMillis();
           log.debug("doSync method START");
           ResponseEntity<Object> entity = null;
           Map<String,Object> response =null;
   		   HeaderValidation.aspApiHeaderValidation(headers, "01", HttpStatus.BAD_REQUEST);

          // ValidationV31.aspApiHeaderValidation(headers, messageSourceV31);
           syncService.validateParams(allRequestParams);                  
           log.info("DO SYNC API call - Validation Success of Input data");
           response = syncService.doSync(headers, allRequestParams);

           long t2 = System.currentTimeMillis();
           log.info("TAT {}", (t2 - t1));
           entity = new ResponseEntity<>(response, HttpStatus.OK); 
           log.info("Successfully completed the request for API : {}, STATUS: Completed ",apiName);
           return entity;
    }
	
	
	@RequestMapping(value = "/report", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getGSTINReport(@RequestHeader Map<String, String> reqHeaderMap,
			@RequestParam Map<String, String> allRequestParams, HttpServletResponse response) {
		// getSummaryData for L2
		long t1 = System.currentTimeMillis();
		log.debug("getReportSummaryL2 method START");
		ValidationV31.apiRequestParamsValidateForL2Report(allRequestParams, messageSourceV31);
		   HeaderValidation.aspApiHeaderValidation(reqHeaderMap, "01", HttpStatus.BAD_REQUEST);
		//ValidationV31.aspApiHeaderValidation(reqHeaderMap, messageSourceV31);
		String level = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_LEVEL, null);
		String gstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, null);
		String fp = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_FP, null);
		String apiName = "Report Summary L2";
		ResponseEntity<Object> entity = null;
		String hdrGstin = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_GSTIN, "");
		ValidationV31.apiHeaderAndParamsGstinValidation(hdrGstin, gstin, messageSourceV31);
		if (Gstr1ConstantsV31.SUMMARY_TYPE_L2.equalsIgnoreCase(level)) {
			apiName = "Report Summary L2";
			Map<String, String> data = dasboardService.validateApiInput(allRequestParams);
			
			dasboardService.validateFilterInput(allRequestParams);
			
			log.info("Report API call - L3 ValidationV31 Success of Input data - STEP3");
			Map<String, Object> responseMap = null;
			responseMap = dasboardService.processReportDataL2(reqHeaderMap, data);
			entity = new ResponseEntity<>(responseMap, HttpStatus.OK);
		}
		log.info("******************************getSupplies API call ENDS****************************************");
		String ip = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_IP);
		String clientID = MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_CLIENT_ID);
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
		} else if ( ex instanceof AspException) {
			exObject = ((AspException) ex).getExcObj();
//			HttpStatus status = (HttpStatus) MapUtils.getObject(exObject, Gstr1ConstantsV31.ERROR_HTTP_CODE,
//					HttpStatus.INTERNAL_SERVER_ERROR);
			HttpStatus status = (HttpStatus) exObject.get(AspConstants.ERROR_HTTP_CODE);
			entity = new ResponseEntity<>(exObject, status);
			exObject.remove(Gstr1ConstantsV31.ERROR_HTTP_CODE);
			log.error("exceptionHandler method : AspExceptionV31 error occurred.{}", ex);
		}else if (ex instanceof SocketTimeoutException || ex instanceof ResourceAccessException) {
			exObject = new HashMap<>();
			exObject.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP_CONNECTION_ERROR);
			exObject.put(Gstr1ConstantsV31.ERROR_DESC, "Connection Error: Unable to connect to destination");
			exObject.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1);
			log.error("exceptionHandler method : Connection error occurred.{}", ex);
			entity = new ResponseEntity<>(exObject, HttpStatus.GATEWAY_TIMEOUT);
		}
		return entity;
	}

	
}
