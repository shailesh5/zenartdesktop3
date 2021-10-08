/**
 * 
 */
package com.jio.asp.gstr1.v30.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.dao.AspMongoDao;
import com.jio.asp.gstr1.v30.util.CommonUtil;

/**
 * @author Rohit1.Soni
 *
 */
@Service
public class AspSuppliesStatusServiceImpl implements AspSuppliesStatusService {

	@Autowired
	private MessageSource messageSource;
	@Autowired
	private AspMongoDao mongoDao;
	@Autowired
	private MessageSource gstnResource;

	private static final Logger log = LoggerFactory.getLogger(AspSuppliesStatusServiceImpl.class);

	/**
	 * method is the initiation point for supplies status query. This method
	 * takes care of calling all the necessary utility functions to gather and
	 * format the data into required output.
	 * 
	 * @param inputMap,
	 *            this is the input which has been sent from client.
	 * @return String containing the json format of the data returned from
	 *         database.
	 */
	@Override
	public String getSuppliesStatus(Map<String, String> inputMap) {
		log.debug("getSuppliesStatus method START");
		String ackNo = inputMap.get(Gstr1Constants.INPUT_ACK_NO);
		String gstin = inputMap.get(Gstr1Constants.INPUT_GSTN);
		String gstr1StatusCol=gstnResource.getMessage("trans.status.col", null, LocaleContextHolder.getLocale());
		Map<String, Object>  responseMap = new HashMap<>();
		log.debug("************SuppliesStatus method : retriving the data from ASP database, arn_no:{}", ackNo);
		log.info("************SuppliesStatus method : retriving the data from ASP database, arn_no:{}", ackNo);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(Gstr1Constants.CONTROL_REC_ID, ackNo);
		paramMap.put(Gstr1Constants.CONTROL_REC_GSTIN, gstin);
		List<Map<String, Object>> statusData = mongoDao.getMongoData(paramMap, gstr1StatusCol);
		log.debug("************SuppliesStatus method : after getting data from database, arn_no:{}", ackNo);
		if (statusData != null) {
			Optional<Map<String, Object>> optional = statusData.stream().findFirst();
			if (optional.isPresent()) {
				Map<String, Object> map = optional.get();
				int recrdCount = MapUtils.getInteger(map, Gstr1Constants.CONTROL_REC_RCRDCOUNT, 0).intValue();
				int succsCount = MapUtils.getInteger(map, Gstr1Constants.CONTROL_REC_SUCCCNT, 0).intValue();
				int errCount = MapUtils.getInteger(map, Gstr1Constants.CONTROL_REC_ERRCNT, 0).intValue();
				int warnCount = MapUtils.getInteger(map, Gstr1Constants.CONTROL_REC_WARNCNT, 0).intValue();
				String status = Gstr1Constants.CONTROL_REC_FAIL_STATUS;
				if (recrdCount == succsCount) {
					status = Gstr1Constants.CONTROL_REC_SUC_STATUS;
				} else if (recrdCount > (succsCount + errCount)) {
					status = Gstr1Constants.CONTROL_REC_PR_STATUS;
				} else if (recrdCount > (succsCount + errCount) && errCount > 0) {
					status = Gstr1Constants.CONTROL_REC_FAIL_STATUS;
				}
				map.remove(Gstr1Constants.CONTROL_REC_ID);
				paramMap.put(Gstr1Constants.CONTROL_JSON_STATE, status);
				mongoDao.updateInMongo(paramMap, gstr1StatusCol, ackNo, null);
				map.put(Gstr1Constants.CONTROL_REC_STATUS, status);
				responseMap.put(Gstr1Constants.ASP_API_RESP_DATA_ATTR, map);
				responseMap.put(Gstr1Constants.ASP_API_RESP_META_ATTR,inputMap);
			} else {
				Map<String, String> dataMap=new HashMap<>();
				dataMap.put(Gstr1Constants.CONTROL_REC_STATUS,Gstr1Constants.ERROR);
				dataMap.put(Gstr1Constants.ERROR_MESSAGE, "No Data Available for given criteria");
				responseMap.put(Gstr1Constants.ASP_API_RESP_DATA_ATTR, dataMap);
				responseMap.put(Gstr1Constants.ASP_API_RESP_META_ATTR,inputMap);
			}
		}else{
			Map<String, String> dataMap=new HashMap<>();
			dataMap.put(Gstr1Constants.CONTROL_REC_STATUS,Gstr1Constants.ERROR);
			dataMap.put(Gstr1Constants.ERROR_MESSAGE, "No Data Available for given criteria");
			responseMap.put(Gstr1Constants.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1Constants.ASP_API_RESP_META_ATTR,inputMap);
		}

		log.debug("************SuppliesStatus method : converting tree view to json and forming the output, arn_no:{}",
				ackNo);
		log.info("************SuppliesStatus method : converting tree view to json and forming the output, arn_no:{}",
				ackNo);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		String statusResp = null;
		try {
			statusResp = mapper.writeValueAsString(responseMap);
		} catch (JsonProcessingException e) {
			CommonUtil.throwException(ErrorCodes.ASP104, Gstr1Constants.ASP_GSTR1_SUPPLIES_STATUS_GRP,
					ErrorCodes.ASP104, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource, e);
		}

		log.debug("************SuppliesStatus method : Output creation success, arn_no:{}", ackNo);
		log.info("************SuppliesStatus method : Output creation success, arn_no:{}", ackNo);
		log.debug("getSuppliesStatus method END");

		return statusResp;
	}

	/**
	 * Method does the api input validation. Data which is needed for request
	 * processing and querying from database
	 * 
	 * @param inputMap
	 *            this object contains user input parameter for searching of the
	 *            user data.
	 * @return Map, which contains error information or processed data
	 */
	@Override
	public void validateApiInput(Map<String, String> inputMap) {
		log.debug("validateApiInput method : START");

		CommonUtil.validateEmpty(inputMap, ErrorCodes.ASP504, Gstr1Constants.ASP_GSTR1_SUPPLIES_STATUS_GRP,
				ErrorCodes.ASP504, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1Constants.INPUT_GSTN),
				ErrorCodes.ASP501, Gstr1Constants.ASP_GSTR1_SUPPLIES_STATUS_GRP,ErrorCodes.ASP501, null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1Constants.INPUT_ACK_NO),
				ErrorCodes.ASP521, Gstr1Constants.ASP_GSTR1_SUPPLIES_STATUS_GRP, ErrorCodes.ASP521, null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		log.debug("validateApiInput method : END");
	}

}
