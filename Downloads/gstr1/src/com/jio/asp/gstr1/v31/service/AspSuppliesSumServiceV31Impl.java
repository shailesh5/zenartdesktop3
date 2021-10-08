/**
 * 
 */
package com.jio.asp.gstr1.v31.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspSuppliesDaoV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;

/**
 * @author Amit1.Dwivedi
 *
 */
@Service
public class AspSuppliesSumServiceV31Impl implements AspSuppliesSumServiceV31 {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jio.asp.gstr1.v31.controller.AspSuppliesSumService#
	 * validateHeaderInput( java.util.Map)
	 */

	@Autowired
	AspLoggingServiceV31 aspl;

	@Autowired
	private AspSuppliesDaoV31 summaryDao;

	@Autowired
	private MessageSource messageSourceV31;
	
	@Autowired
	private MessageSource gstnResource;
	
	public static long sysCurTime=System.currentTimeMillis();

	public static final Logger log = LoggerFactory.getLogger(AspSuppliesSumServiceV31Impl.class);

	@Override
	public Map<String, String> validateApiInput(Map<String, String> inputMap) {
		CommonUtil.validateEmptyString(MapUtils.getString(inputMap,Gstr1ConstantsV31.HEADER_GSTIN,""),
				ErrorCodesV31.ASP011031, Gstr1ConstantsV31.JIOGST_L2, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_FP,""),
				ErrorCodesV31.ASP011311, Gstr1ConstantsV31.JIOGST_L2, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_SECTION,""),
				ErrorCodesV31.ASP011179, Gstr1ConstantsV31.JIOGST_L2, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_LEVEL,""),
				ErrorCodesV31.ASP011181, Gstr1ConstantsV31.JIOGST_L2, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
		log.debug("validateApiInput method : END");

		return inputMap;
	}

	@Override
	public Map<String, Object> processGstr1InvoiceDataL2(Map<String, String> headers, Map<String, String> data) {
		log.debug("processGstr1SummaryDataL2 method : START");
	
		log.info("processGstr1SummaryDataL2 method : START ::   ackno{} :",sysCurTime);
		Map<String, Object> responseMap = new HashMap<>();
		// asp call, making call to database to get the ASP data
		log.info("processGstr1InvoiceDataL2 Method - ASP Data retrival going to start - STEP4  ::   ackno :",sysCurTime);
		Map<String, Object> dataMap = getAspInvoiceDataL2(data);
		if (MapUtils.isNotEmpty(dataMap)) {
			log.info("processGstr1InvoiceDataL2 Method - ASP Data parsing and preparation complete - STEP6 ::   ackno :",sysCurTime);
			// business logic step and response preparation step
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
			log.info("processGstr1InvoiceDataL2 Method - Preparing the API response - STEP7  ::   ackno{} :",sysCurTime);
		} else {
			dataMap = new HashMap<>();
			responseMap = new HashMap<>();
			dataMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, "No data found for the given criteria");
			dataMap.put(Gstr1ConstantsV31.RESP_STATUS_CODE, Gstr1ConstantsV31.ERROR);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
		}
		log.debug("processGstr1SummaryDataL0 method : END");
		return responseMap;
	}

	/**
	 * Method based on the user input for api call, will query database and
	 * retrieve the related information.
	 * 
	 * @param inputMap
	 *            user input map
	 * @return Map containing asp data per section.
	 */
	public Map<String, Object> getAspInvoiceDataL2(Map<String, String> inputMap) {
		
		log.debug("getAspInvoiceDataL2 method : START");
		log.info("getAspInvoiceDataL2 method : START :: ackno{}",sysCurTime);
		Map<String, Object> respMap = summaryDao.retrieveSummaryData(inputMap);
		log.info("getAspInvoiceDataL2 method : END :: ackno{}",sysCurTime);
		log.debug("getAspInvoiceDataL2 method : END");
		return respMap;
	}

	@Override
	public void validateFilterInput(Map<String, String> allRequestParams) {
		String filters = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_ACTION_FILTER, null);
		
		
		if(filters == null){
			return;
		}
		filters=filters.toLowerCase();
		List<String>userFilterList = null;

		if(filters.contains(",")){
			userFilterList = Arrays.asList(filters.split(","));
		}else{
			userFilterList = Arrays.asList(filters);
		}

		for(String filter : userFilterList){
			if(!Gstr1ConstantsV31.FILTER_STATUS_MAP().containsKey(filter)){
				
				 CommonUtil.throwException(ErrorCodesV31.ASP010102, Gstr1ConstantsV31.JIOGST_SYNC, null
							, HttpStatus.OK, null, AspConstants.FORM_CODE, null);
			}
		}
	}

	@Override
	public Map<String, Object> processReportDataL2(Map<String, String> headers, Map<String, String> data) {
		log.debug("processReportDataL2 method : START");
		
		log.info("processReportDataL2 method : START ::   ackno{} :",sysCurTime);
		Map<String, Object> responseMap = new HashMap<>();
		// asp call, making call to database to get the ASP data
		log.info("processReportDataL2 Method - ASP Data retrival going to start - STEP4  ::   ackno :",sysCurTime);
		Map<String, Object> dataMap = getReportDataL2(data);
		if (MapUtils.isNotEmpty(dataMap)) {
			log.info("processReportDataL2 Method - ASP Data parsing and preparation complete - STEP6 ::   ackno :",sysCurTime);
			// business logic step and response preparation step
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
			log.info("processReportDataL2 Method - Preparing the API response - STEP7  ::   ackno{} :",sysCurTime);
		} else {
			dataMap = new HashMap<>();
			responseMap = new HashMap<>();
			dataMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, "No data found for the given criteria");
			dataMap.put(Gstr1ConstantsV31.RESP_STATUS_CODE, Gstr1ConstantsV31.ERROR);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
		}
		log.debug("processReportDataL2 method : END");
		return responseMap;
	}
	
public Map<String, Object> getReportDataL2(Map<String, String> inputMap) {
		
		log.debug("getReportDataL2 method : START");
		log.info("getReportDataL2 method : START :: ackno{}",sysCurTime);
				
		Map<String, Object> respMap = summaryDao.retrieveReportSummaryData(inputMap);
		log.info("getReportDataL2 method : END :: ackno{}",sysCurTime);
		log.debug("getReportDataL2 method : END");
		return respMap;
	}
}
