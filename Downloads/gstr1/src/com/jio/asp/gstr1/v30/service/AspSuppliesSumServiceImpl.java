/**
 * 
 */
package com.jio.asp.gstr1.v30.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.dao.AspSuppliesDao;
import com.jio.asp.gstr1.v30.util.CommonUtil;

/**
 * @author Amit1.Dwivedi
 *
 */
@Service
public class AspSuppliesSumServiceImpl implements AspSuppliesSumService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jio.asp.gstr1.v30.controller.AspSuppliesSumService#validateHeaderInput(
	 * java.util.Map)
	 */

	@Autowired
	AspLoggingService aspl;

	@Autowired
	private AspSuppliesDao summaryDao;

	@Autowired
	private MessageSource messageSource;

	public static final Logger log = LoggerFactory.getLogger(AspSuppliesSumServiceImpl.class);

	@Override
	public Map<String, String> validateApiInput(Map<String, String> inputMap) {
		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1Constants.INPUT_GSTN), ErrorCodes.ASP504, Gstr1Constants.L2_SUMMARY, ErrorCodes.ASP504,
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		
		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1Constants.INPUT_FP), ErrorCodes.ASP504, Gstr1Constants.L2_SUMMARY, ErrorCodes.ASP504,
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		
		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1Constants.INPUT_SECTION), ErrorCodes.ASP504, Gstr1Constants.L2_SUMMARY, ErrorCodes.ASP504,
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		
		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1Constants.INPUT_LEVEL), ErrorCodes.ASP504, Gstr1Constants.L2_SUMMARY, ErrorCodes.ASP504,
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		log.debug("validateApiInput method : END");
		
		return inputMap;
	}

	@Override
	public Map<String, Object> processGstr1InvoiceDataL2(Map<String, String> headers, Map<String, String> data) {
		log.debug("processGstr1SummaryDataL0 method : START");
		Map<String, Object> responseMap = new HashMap<>();
		// asp call, making call to database to get the ASP data
		log.info("processGstr1InvoiceDataL2 Method - ASP Data retrival going to start - STEP4");
		Map<String, Object> dataMap = getAspInvoiceDataL2(data);
		if (MapUtils.isNotEmpty(dataMap)) {
			log.info("processGstr1InvoiceDataL2 Method - ASP Data parsing and preparation complete - STEP6");
			// business logic step and response preparation step
			responseMap.put(Gstr1Constants.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1Constants.ASP_API_RESP_META_ATTR, data);
			log.info("processGstr1InvoiceDataL2 Method - Preparing the API response - STEP7");
		} else {
			dataMap = new HashMap<>();
			responseMap = new HashMap<>();
			dataMap.put(Gstr1Constants.ERROR_MESSAGE, "No data found for the given criteria");
			dataMap.put(Gstr1Constants.RESP_STATUS_CODE, Gstr1Constants.ERROR);
			responseMap.put(Gstr1Constants.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1Constants.ASP_API_RESP_META_ATTR, data);
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
		Map<String, Object> respMap = summaryDao.retrieveSummaryData(inputMap);
		log.debug("getAspInvoiceDataL2 method : END");
		return respMap;
	}

}
