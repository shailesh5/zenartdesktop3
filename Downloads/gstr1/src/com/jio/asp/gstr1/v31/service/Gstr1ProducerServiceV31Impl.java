package com.jio.asp.gstr1.v31.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.exception.AspExceptionV31;
import com.jio.asp.gstr1.v31.exception.RestExceptionHandlerV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;
import com.jio.asp.validation.ErrorCodes;
import com.mongodb.BasicDBObject;

@Service
public class Gstr1ProducerServiceV31Impl implements Gstr1ProducerServiceV31 {
	private static final Logger log = LoggerFactory.getLogger(Gstr1ProducerServiceV31Impl.class);
	@Autowired
	private KafkaProducerV31 kafkaService;
	@Autowired
	private AspLoggingServiceV31 aspLoggingServiceV31;
	@Autowired
	private MessageSource gstnResource;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private MessageSource messageSourceV31;
	@Autowired
	private AspUpdateStatusService updateStatusService;
	@Autowired
	private AspMongoDaoV31 aspMongoDao;
	private static List<String> type1List, type2List, amendmentNonAggreTypeList;

	/**
	 * Method prepares exclusion list for the attribute which needs be added to
	 * final aggregated invoice output.
	 * non aggregated sections
	 * @return List of excluded attributes.
	 */
	static {
		log.debug("prepareType1List method: START");
		type1List = new ArrayList<>();
		type1List.add(Gstr1ConstantsV31.TYPE_B2B);
		type1List.add(Gstr1ConstantsV31.TYPE_B2CL);
		type1List.add(Gstr1ConstantsV31.TYPE_EXP);
		type1List.add(Gstr1ConstantsV31.TYPE_CDNR);
		type1List.add(Gstr1ConstantsV31.TYPE_CDNUR);
		log.debug("prepareType1List method: END");
	}
	
	static {
		log.debug("prepareType1List method: START");
		amendmentNonAggreTypeList = new ArrayList<>();
		amendmentNonAggreTypeList.add(Gstr1ConstantsV31.TYPE_B2BA);
		amendmentNonAggreTypeList.add(Gstr1ConstantsV31.TYPE_B2CLA);
		amendmentNonAggreTypeList.add(Gstr1ConstantsV31.TYPE_EXPA);
		amendmentNonAggreTypeList.add(Gstr1ConstantsV31.TYPE_CDNRA);
		amendmentNonAggreTypeList.add(Gstr1ConstantsV31.TYPE_CDNURA);
		log.debug("prepareType1List method: END");
	}

	/**
	 * Method prepares exclusion list for the attribute which needs be added to
	 * final aggregated invoice output.
	 * 
	 * @return List of excluded attributes.
	 */
	static {
		log.debug("prepareType2List method: START");
		type2List = new ArrayList<>();
		type2List.add(Gstr1ConstantsV31.TYPE_B2CS);
		type2List.add(Gstr1ConstantsV31.TYPE_B2CSA);
		type2List.add(Gstr1ConstantsV31.TYPE_NIL);
		type2List.add(Gstr1ConstantsV31.TYPE_HSN);
		type2List.add(Gstr1ConstantsV31.TYPE_AT);
		type2List.add(Gstr1ConstantsV31.TYPE_ATA);
		type2List.add(Gstr1ConstantsV31.TYPE_TXPD);
		type2List.add(Gstr1ConstantsV31.TYPE_TXPDA);
		type2List.add(Gstr1ConstantsV31.TYPE_DOCS);
		log.debug("prepareType2List method: END");
	}

	@Override
	public String invokeConverterApi(String flatPayLoad, Map<String, String> headerMap, String ackNo) {
		log.debug("invokeConvertorApi method: START  for ackNo {}", ackNo);
		String jsonString = flatPayLoad;
		String convertCallFlag = MapUtils.getString(headerMap, "cntr", "true");
		if ("true".equals(convertCallFlag)) {
			HttpHeaders getUidHeader = new HttpHeaders();
			getUidHeader.setContentType(MediaType.APPLICATION_JSON);
			String convertUrl = gstnResource.getMessage(Gstr1ConstantsV31.CONVERT_URL, null,
					LocaleContextHolder.getLocale());
			HttpEntity<String> entity = new HttpEntity<String>(flatPayLoad, getUidHeader);
			SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
			int readTimeOut = Integer.parseInt(gstnResource.getMessage(Gstr1ConstantsV31.REST_READ_TIMEOUT, null,
					LocaleContextHolder.getLocale()));
			int connectionTimeOut = Integer.parseInt(gstnResource.getMessage(Gstr1ConstantsV31.REST_CONNECTION_TIMEOUT,
					null, LocaleContextHolder.getLocale()));
			rf.setReadTimeout(readTimeOut);
			rf.setConnectTimeout(connectionTimeOut);
			restTemplate.setErrorHandler(new RestExceptionHandlerV31());
			ResponseEntity<String> respEntity = restTemplate.exchange(convertUrl, HttpMethod.POST, entity,
					String.class);
			log.debug("invokeConvertorApi method: response received from convertor  for ackNo {}", ackNo);
			if (respEntity != null && respEntity.getStatusCode() == HttpStatus.OK
					&& StringUtils.isNotBlank(respEntity.getBody())) {
				jsonString = respEntity.getBody();
				log.debug("invokeConvertorApi method: successfull response from convertor  for ackNo {}", ackNo);
			} else {
				jsonString = respEntity.getBody();
				ObjectMapper mapper = new ObjectMapper();
				JsonNode jsonNode;
				try {
					jsonNode = mapper.readTree(jsonString);
					Map<String, Object> excObj = new HashMap<>();
					if (jsonNode != null && jsonNode.has(Gstr1ConstantsV31.ERROR_RESPONSE)) {
						JsonNode errorReport = jsonNode.get(Gstr1ConstantsV31.ERROR_RESPONSE);
						if (jsonNode.has(Gstr1ConstantsV31.CONTROL_WARN_REPORT)) {
							JsonNode warnReport = jsonNode.get(Gstr1ConstantsV31.CONTROL_WARN_REPORT);
							excObj.put(Gstr1ConstantsV31.CONTROL_WARN_REPORT, warnReport);
						}
//						excObj.put(Gstr1ConstantsV31.ERROR_REPORT, errorReport);
//						excObj.put(Gstr1ConstantsV31.ERROR_DESC, ErrorCodesV31.ASP103);
//						excObj.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP);
//						excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE, HttpStatus.BAD_REQUEST);
						 Map<String, Object> map =  mapper.convertValue(errorReport, Map.class);
						 map.put(Gstr1ConstantsV31.ERROR_HTTP_CODE, HttpStatus.BAD_REQUEST);
						throw new AspExceptionV31("Error during Payload ValidationV31", null, false, false, map);
					} else {
						log.error("Exception occurred while generating convertor output");
						CommonUtil.throwException(ErrorCodesV31.ASP011022, Gstr1ConstantsV31.JIOGST_SAVE, null
								, HttpStatus.INTERNAL_SERVER_ERROR, null, AspConstants.FORM_CODE, null);
					}
				} catch (IOException e) {
					log.error("Exception occurred in while parsing {}", e);
					CommonUtil.throwException(ErrorCodesV31.ASP011022, Gstr1ConstantsV31.JIOGST_SAVE, null
							, HttpStatus.INTERNAL_SERVER_ERROR, null, AspConstants.FORM_CODE, null);
				}
			}
		}
		log.debug("invokeConvertorApi method: END  for ackNo {}", ackNo);
		return jsonString;
	}

	private Map<String, Object> prepareHeaderData(String key, Object value, Map<String, Object> headerMap) {
		headerMap = (headerMap == null) ? new HashMap<>() : headerMap;
		if (value instanceof String || value instanceof Number) {
			headerMap.put(key, value);
		}
		return headerMap;
	}

	@Override
	public Map<String, String> saveInvoice(Map<String, Object> requestBody, Map<String, String> reqHeaderMap,
			String ackNo) {
		log.debug("saveInvoice method: START");
		log.info("saveInvoice method:Starting the processing of request with ackno: {}", ackNo);
		log.debug("saveInvoice method: requestBody received for ackno: {}", ackNo);
		log.debug("saveInvoice method: formatting request payload json for ackno: {}", ackNo);
		List<Map<String, Object>> flatJson = convertInputToFlatJson(requestBody, Gstr1ConstantsV31.INVOICE_STATE_NEW,
				ackNo, reqHeaderMap);
		log.info("saveInvoice method: after parsing the json to per invoice structure ");
		Map<String, String> respMap = new HashMap<>();
		log.debug("saveInvoice method: after formatting request payload json for ackno: {}", ackNo);
		log.debug("saveInvoice method: converted json for ackNo {}", ackNo);
		boolean status = updateStatusService.processGstinMasterData(requestBody, reqHeaderMap, ackNo);
		
		if (status) {
			Map<String, Object> transLogMap = new HashMap<>();
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_ID, ackNo);
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_ACKNO, ackNo);
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_CLIENT_ID,
					MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_CLIENT_ID));
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_USRACT, Gstr1ConstantsV31.API_SAVE);
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_STATUS, Gstr1ConstantsV31.CONTROL_REC_PR_STATUS);
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_RCRDCOUNT, flatJson.size());
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_SUCCCNT, 0);
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_ERRCNT, 0);
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_WARNCNT, 0);
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_GSTIN,
					MapUtils.getString(requestBody, Gstr1ConstantsV31.INPUT_GSTN));
			SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
			Date date = new Date();
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_STRTTIME, sdf.format(date));
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_UPDTTIME, "");
			transLogMap.put(Gstr1ConstantsV31.CONTROL_REC_TIMEOUT, "");
			transLogMap.put(Gstr1ConstantsV31.CONTROL_WARN_REPORT,
					MapUtils.getObject(requestBody, Gstr1ConstantsV31.CONTROL_WARN_REPORT, null));
			aspLoggingServiceV31.generateControlLog(transLogMap);
			log.debug("saveInvoice method: before pushing to kafka for ackno: {}", ackNo);
			log.info("saveInvoice method: spawning a thread for pushing to kafka for ackno: {}", ackNo);
			String gstr1Topic = gstnResource.getMessage("gstr1-kafka-topic-v3.1", null,
					LocaleContextHolder.getLocale());
			kafkaService.postKafkaMsg(flatJson, ackNo, gstr1Topic);
			log.info("saveInvoice method: Continuing with response after spawning kafka thread for ackno: {}", ackNo);
			log.debug("saveInvoice method: after pushing to kafka for ackno: {}", ackNo);

			
			respMap.put(Gstr1ConstantsV31.GSTR1_RESP_ACKNO, ackNo);
			respMap.put(Gstr1ConstantsV31.GSTR1_RESP_STATUS_CD, Gstr1ConstantsV31.GSTR1_RESP_SUCCESS_ST_CD);
		} else {
			log.info("saveInvoice method: Financial period issue, gstr1 already submitted for given FP for ackno: {}", ackNo);
			log.debug("saveInvoice method: Financial period issue, gstr1 already submitted for given FP for ackno: {}", ackNo);
			String fp = MapUtils.getString(requestBody, Gstr1ConstantsV31.INPUT_FP);
			String gstin = MapUtils.getString(requestBody, Gstr1ConstantsV31.JSON_GSTIN);

//			CommonUtilV31.throwException(ErrorCodesV31.ASP523, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP,
//					ErrorCodesV31.ASP523, new Object[] { Gstr1ConstantsV31.API_NAME, fp, gstin }, HttpStatus.OK, messageSourceV31, null);
			
			CommonUtil.throwException(ErrorCodes.ASP1800, AspConstants.JIOGST_SAVE, null, HttpStatus.BAD_REQUEST,
					null, AspConstants.FORM_CODE);
			
		}
		log.debug("saveInvoice method: generating response resp: {}", respMap);
		log.info("saveInvoice method: generating response resp: {}", respMap);
		log.debug("saveInvoice method: END");
		return respMap;
	}

	@Override
	public List<Map<String, Object>> convertInputToFlatJson(Map<String, Object> requestBody, String action,
			String ackNo, Map<String, String> reqHeaderMap) {
		log.debug("convertInputToFlatJson method: START for ackNo: {}", ackNo);
		Iterator<String> it = requestBody.keySet().iterator();
		Map<String, Object> headerDataMap = null;
		List<Map<String, Object>> flatJson = new ArrayList<>();
		Set<String> uniqueMongoIds = new HashSet<>();
		List<Map<String, Object>> amdentmentsQueryData = new ArrayList<>();
		Set<String> amdentmentsCheckSubmitFile = new HashSet<>();
		while (it.hasNext()) {
			log.debug("convertInputToFlatJson method: looping and processing section wise payload for ackNo: {}",
					ackNo);
			// looping to main map
			String type = it.next();
			log.debug("convertInputToFlatJson method: processing for section {}", type);
			log.info("convertInputToFlatJson method: processing for section {}", type);
			Object jsonObj = requestBody.get(type);
			headerDataMap = prepareHeaderData(type, jsonObj, headerDataMap);
			log.debug("convertInputToFlatJson method: processing for section {} with following payload for ackNo: {}",
					type, ackNo);
			if (type1List.contains(type) || amendmentNonAggreTypeList.contains(type)) {
				flatJson.addAll(
						processSection(jsonObj, headerDataMap, action, type, ackNo, reqHeaderMap, uniqueMongoIds, amdentmentsQueryData, amdentmentsCheckSubmitFile));
				
			} else if (type2List.contains(type)) {
				flatJson.addAll(processAggSection(jsonObj, headerDataMap, action, type, ackNo, reqHeaderMap));
			}
		}
		validatePayloadStatus(uniqueMongoIds, flatJson);
		//validatePayloadforAmendmentSubmitFile(amdentmentsCheckSubmitFile, flatJson);
		//validatePayloadforAmendments(amdentmentsQueryData, flatJson);
		log.debug("convertInputToFlatJson method: END for ackNo: {}", ackNo);
		return flatJson;
	}

	private void validatePayloadforAmendmentSubmitFile(Set<String> amdentmentsCheckSubmitFile,
			List<Map<String, Object>> flatJson) {
		
		if (CollectionUtils.isNotEmpty(amdentmentsCheckSubmitFile)) {
			Map<String, Object> whrObj = new HashMap<>();
			String[] status = new String[2];
			status[0] = Gstr1ConstantsV31.STATUS_SUBMIT_UPLOAD;
			status[1] = Gstr1ConstantsV31.STATUS_FILE_UPLOAD;
			whrObj.put(Gstr1ConstantsV31.CONTROL_JSON_ID, amdentmentsCheckSubmitFile);
			whrObj.put("control." + Gstr1ConstantsV31.CONTROL_JSON_GSTN_STATE, status);
			String[] fields = new String[1];
			fields[0] = "control.type";
			// ASP526
			String supCollection = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
			Map<String, Object> errorMap = null;
			List<Map<String, Object>> list = aspMongoDao.getMongoData(whrObj, fields, supCollection);
			
			
			if (!CollectionUtils.isNotEmpty(list)) {
				errorMap = new HashMap<>();
				Map<String, List<Map<String, Object>>> secMap = new HashMap<>();				
				Map<String, String> map = formatSavedIdDataAmendments(flatJson);
				errorMap.put("errorReport", secMap);
				for (Map<String, Object> map2 : flatJson) {

					Map<String, Object> controlSecMap = (Map<String, Object>) map2
							.get(Gstr1ConstantsV31.CONTROL_JSON_SEC);
					String id = MapUtils.getString(controlSecMap, "_id");
					String section = map.get(id);
					if (map.containsKey(id)) {
						Map<String, Object> inputSecMap = (Map<String, Object>) map2
								.get(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC);

						if (secMap.containsKey(section)) {
							List<Map<String, Object>> itemList = secMap.get(section);
							Map<String, Object> itemMap = populateMap(inputSecMap, "The Original Invoice no " + inputSecMap.get(Gstr1ConstantsV31.INPUT_OINUM) +" is not Submitted/Filed to GSTN.");
							itemList.add(itemMap);
							secMap.put(section, itemList);
						} else {
							List<Map<String, Object>> itemList = new ArrayList<>();
							Map<String, Object> itemMap = populateMap(inputSecMap, "The Original Invoice no " + inputSecMap.get(Gstr1ConstantsV31.INPUT_OINUM) +" is not Submitted/Filed to GSTN.");
							itemList.add(itemMap);
							secMap.put(section, itemList);
						}
					}
				}
			} 

			if (MapUtils.isNotEmpty(errorMap)) {
				Map<String, Object> excObj = new HashMap<>();
				excObj.put(Gstr1ConstantsV31.ERROR_REPORT, errorMap);
				excObj.put(Gstr1ConstantsV31.ERROR_DESC, ErrorCodesV31.ASP103);
				excObj.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP);
				excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE, HttpStatus.BAD_REQUEST);
				throw new AspExceptionV31("Error during Payload ValidationV31", null, false, false, excObj);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> processSection(Object jsonObj, Map<String, Object> headerMap, String action,
			String type, String ackNo, Map<String, String> reqHeaderMap, Set<String> uniqueMongoIds, 
			List<Map<String, Object>> amdentmentsQueryData, Set<String> amdentmentsCheckSubmitFile) {
		log.debug("processSection method: START for ackNo {}", ackNo);
		SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
		List<Map<String, Object>> flatJson = new ArrayList<>();
		// retrieving the exclude list based on type.
		if (jsonObj instanceof List<?>) {
			List<Object> inputSecList = (List<Object>) jsonObj;
			log.debug("processSection method: start of loop for ackNo {}", ackNo);
			for (Object inpSecObj : inputSecList) {
				Map<String, Object> inputSecMap = (Map<String, Object>) inpSecObj;
				log.debug("processSection method: create control map start for ackNo {}", ackNo);
				Map<String, Object> controlMap = createControlMap(inputSecMap, headerMap, reqHeaderMap, ackNo, type,
						sdf);
				log.debug("processSection method: create control map end for ackNo {}", ackNo);
				String mongoId = createDocObjectId(controlMap, inputSecMap, headerMap, reqHeaderMap);
				uniqueMongoIds.add(mongoId);
				if (amendmentNonAggreTypeList.contains(type)) {
					String mongoId1 = createNonAggregateDocObjectIdwithCurrentYear(controlMap, inputSecMap, headerMap, reqHeaderMap);
					Map<String, Object> individualQueryData = new HashMap<>();
					individualQueryData.put(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC+"."+Gstr1ConstantsV31.INPUT_INUM, inputSecMap.get(Gstr1ConstantsV31.INPUT_OINUM));
					individualQueryData.put(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC+"."+Gstr1ConstantsV31.INPUT_IDT, inputSecMap.get(Gstr1ConstantsV31.INPUT_OIDT));
					individualQueryData.put(Gstr1ConstantsV31.CONTROL_JSON_HDR_SEC+"."+Gstr1ConstantsV31.HEADER_GSTIN, headerMap.get(Gstr1ConstantsV31.HEADER_GSTIN));
					String stringSection = controlMap.get(Gstr1ConstantsV31.CONTROL_JSON_TYPE).toString().substring(0, controlMap.get(Gstr1ConstantsV31.CONTROL_JSON_TYPE).toString().length()-1);
					individualQueryData.put(Gstr1ConstantsV31.CONTROL_JSON_SEC+"."+Gstr1ConstantsV31.CONTROL_JSON_TYPE, stringSection);
					amdentmentsQueryData.add(individualQueryData);
					
					amdentmentsCheckSubmitFile.add(mongoId1);
				}
				log.debug("processSection method: create object id end for ackNo {}", ackNo);
				controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, mongoId);
				Map<String, Object> invoiceMap = new HashMap<>();
				invoiceMap.put(Gstr1ConstantsV31.CONTROL_JSON_SEC, controlMap);
				invoiceMap.put(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC, inputSecMap);
				invoiceMap.put(Gstr1ConstantsV31.CONTROL_JSON_HDR_SEC, headerMap);
				flatJson.add(invoiceMap);
			}
			log.debug("processSection method: end of loop for ackNo {}", ackNo);
		}
		log.debug("processSection method: END for ackNo {}", ackNo);
		return flatJson;
	}

	/**
	 * method is used for all the aggregate sections data in payload. Looping on
	 * the type data which comes from convertor. Parses "records" section out of
	 * the payload for each index in the payload. After that it adds that record
	 * array to invoice map which will go in mongo as single record.
	 * 
	 * @param jsonObj
	 * @param headerMap
	 * @param action
	 * @param type
	 * @param ackNo
	 * @param reqHeaderMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> processAggSection(Object jsonObj, Map<String, Object> headerMap, String action,
			String type, String ackNo, Map<String, String> reqHeaderMap) {
		log.debug("processB2csSection method: START for ackNo {}", ackNo);
		SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
		List<Map<String, Object>> flatJson = new ArrayList<>();
		// retrieving the exclude list based on type.

		if (jsonObj instanceof List<?>) {
			List<Object> inputSecList = (List<Object>) jsonObj;

			Map<String, Object> controlMap = null;

			for (Object inpSecObj : inputSecList) {
				Map<String, Object> invoiceMap = new HashMap<>();
				Map<String, Object> inputSecMap = (Map<String, Object>) inpSecObj;
				controlMap = createControlMap(inputSecMap, headerMap, reqHeaderMap, ackNo, type, sdf);
				String mongoId = createDocObjectId(controlMap, inputSecMap, headerMap, reqHeaderMap);
				controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, mongoId);
				invoiceMap.put(Gstr1ConstantsV31.CONTROL_JSON_SEC, controlMap);
				invoiceMap.put(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC,
						MapUtils.getObject(inputSecMap, Gstr1ConstantsV31.CONTROL_JSON_RECORDS_SEC));
				invoiceMap.put(Gstr1ConstantsV31.CONTROL_JSON_HDR_SEC, headerMap);
				flatJson.add(invoiceMap);
			}

		}
		log.info("processB2csSection method: after processing b2cs section data for ackNo {}", ackNo);
		log.debug("processB2csSection method: END for ackNo {}", ackNo);
		return flatJson;
	}

	/**
	 * creates Control map for the mongo database.
	 * 
	 * @param inputSecMap
	 * @param headerMap
	 * @param reqHeaderMap
	 * @param ackNo
	 * @param type
	 * @param sdf
	 * @return
	 */
	private Map<String, Object> createControlMap(Map<String, Object> inputSecMap, Map<String, Object> headerMap,
			Map<String, String> reqHeaderMap, String ackNo, String type, SimpleDateFormat sdf) {
		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_CLIENT_ID,
				MapUtils.getString(reqHeaderMap, Gstr1ConstantsV31.HEADER_CLIENT_ID));
		String state = Gstr1ConstantsV31.INVOICE_STATE_NEW;
		if (!Gstr1ConstantsV31.TYPE_B2CS.equals(type)) {
			if (MapUtils.getString(inputSecMap, Gstr1ConstantsV31.JSON_ACTION, "")
					.equalsIgnoreCase(Gstr1ConstantsV31.INVOICE_INPUT_STATE_DEL)) {
				state = Gstr1ConstantsV31.INVOICE_STATE_DEL;
			} else if (MapUtils.getString(inputSecMap, Gstr1ConstantsV31.JSON_ACTION, "")
					.equalsIgnoreCase(Gstr1ConstantsV31.INVOICE_INPUT_STATE_PARK)) {
				state = Gstr1ConstantsV31.INVOICE_STATE_PARK;
			}
		}
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_GSTN_STATE, Gstr1ConstantsV31.INVOICE_STATE_NEW);
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_STATE, state);
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_TRANSID, ackNo);
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_TYPE, type);
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_LEVEL, identifyLevel(type));
		String dt = sdf.format(new Date());
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_ITIME, dt);
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_UTIME, dt);
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_VERSION, Gstr1ConstantsV31.API_VERSION);
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_SYNC_STATUS, "");
		return controlMap;
	}

	/**
	 * Method creates Unique JSON Id which will be stored in _id value. This
	 * method generates keys for each type list which is defined.
	 * 
	 * @param controlMap
	 *            Control Map
	 * @param inputSecMap
	 *            User INput map
	 * @param headerMap
	 *            Header Map
	 * @param reqHeaderMap
	 *            Request Body Header map
	 * @return String containing key for specific type
	 */
	private String createDocObjectId(Map<String, Object> controlMap, Map<String, Object> inputSecMap,
			Map<String, Object> headerMap, Map<String, String> reqHeaderMap) {
		String id = null;
		String type = MapUtils.getString(controlMap, Gstr1ConstantsV31.CONTROL_JSON_TYPE);
		StringBuilder sb = new StringBuilder();
		String fp = MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_FP);
		String fy = getFY(fp);
		// creating id for b2b, b2cl,exp, amendments.
		if (Gstr1ConstantsV31.TYPE_MONGO_KEY_1.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			id = sb.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fy).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.INPUT_INUM)).toString();
		} else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_2.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for cdn,cdna.
			id = sb.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fy).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.TYPE_MONGO_KEY_CDNR_CDNUR).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.INPUT_NT_NUM)).toString();
		} else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_3.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for b2cs,b2csa.
			id = sb.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.JSON_STID))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fp).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(type).toString();
		} else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_4.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for HSN.
			id = sb.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.JSON_STID))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fp).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(type).toString();
		} else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_5.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for AT,ATA, TXPD, TXPDA
			id = sb.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.JSON_STID))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fp).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(type).toString();
		} else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_6.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for docs_issue
			id = sb.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.JSON_STID))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fp).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(type).toString();
		} else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_7.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for nil
			id = sb.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.JSON_STID))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fp).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(type).toString();
		} else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_8.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for expa, B2ba, B2cla
			id = sb.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fy).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.INPUT_INUM)).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.KEY_APPENDMENT_A).toString();
		}else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_9.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for cdnra.
			id = sb.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fy).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.TYPE_MONGO_KEY_CDNR_CDNUR).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.INPUT_NT_NUM)).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.KEY_APPENDMENT_A).toString();
		}
		return id;
	}

	/**
	 * Identifies the level for the type, at present it can be only inv or
	 * monthly
	 * 
	 * @param type
	 *            Section name
	 * @return identified level
	 */
	private String identifyLevel(String type) {
		if (Gstr1ConstantsV31.TYPE_B2B.equals(type) || Gstr1ConstantsV31.TYPE_CDNR.equals(type)
				|| Gstr1ConstantsV31.TYPE_B2CL.equals(type) || Gstr1ConstantsV31.TYPE_EXP.equals(type)
				|| Gstr1ConstantsV31.TYPE_CDNUR.equals(type)|| Gstr1ConstantsV31.TYPE_EXPA.equals(type)
				|| Gstr1ConstantsV31.TYPE_B2BA.equals(type) || Gstr1ConstantsV31.TYPE_B2CLA.equals(type)
				|| Gstr1ConstantsV31.TYPE_CDNRA.equals(type) || Gstr1ConstantsV31.TYPE_CDNURA.equals(type)) {
			return Gstr1ConstantsV31.LEVEL_INV;
		} else {
			return Gstr1ConstantsV31.LEVEL_MONTHLY;
		}
	}

	/**
	 * parses FY from the fp which is input to request.
	 * 
	 * @param fp
	 *            financial period of payload
	 * @return Financial Year, 2017 etc
	 */
	@Override
	public String getFY(String fp) {
		String fy = fp;
		if (StringUtils.isNotBlank(fp) && fp.length() == 6) {
			fy = fp.substring(2);
		}
		return fy;
	}

	public void validatePayloadStatus(Set<String> uniqueMongoIds, List<Map<String, Object>> flatJson) {
		if (CollectionUtils.isNotEmpty(uniqueMongoIds)) {
			Map<String, Object> whrObj = new HashMap<>();
			String[] status = new String[2];
			status[0] = Gstr1ConstantsV31.STATUS_SUBMIT_UPLOAD;
			status[1] = Gstr1ConstantsV31.STATUS_FILE_UPLOAD;
			whrObj.put(Gstr1ConstantsV31.CONTROL_JSON_ID, uniqueMongoIds);
			whrObj.put("control." + Gstr1ConstantsV31.CONTROL_JSON_GSTN_STATE, status);
			String[] fields = new String[1];
			fields[0] = "control.type";
			// ASP526
			String supCollection = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
			Map<String, Object> errorMap = null;
			List<Map<String, Object>> list = aspMongoDao.getMongoData(whrObj, fields, supCollection);
			if (CollectionUtils.isNotEmpty(list)) {
				errorMap = new HashMap<>();
				Map<String, List<Map<String, Object>>> secMap = new HashMap<>();
				Map<String, String> map = formatSavedIdData(list);
				errorMap.put("errorReport", secMap);
				for (Map<String, Object> map2 : flatJson) {

				
					Map<String, Object> controlSecMap = (Map<String, Object>) map2
							.get(Gstr1ConstantsV31.CONTROL_JSON_SEC);
					String id = MapUtils.getString(controlSecMap, "_id");
					String section = map.get(id);
					if (map.containsKey(id)) {
						Map<String, Object> inputSecMap = (Map<String, Object>) map2
								.get(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC);

						if (secMap.containsKey(section)) {
							List<Map<String, Object>> itemList = secMap.get(section);
							Map<String, Object> itemMap = populateMap(inputSecMap, "This Invoice no is already Submitted/Filed to GSTN. You can't upload the same Invoice again");
							itemList.add(itemMap);
							secMap.put(section, itemList);
						} else {
							List<Map<String, Object>> itemList = new ArrayList<>();
							Map<String, Object> itemMap = populateMap(inputSecMap, "This Invoice no is already Submitted/Filed to GSTN. You can't upload the same Invoice again");
							itemList.add(itemMap);
							secMap.put(section, itemList);
						}
					}
				}
			}

			if (MapUtils.isNotEmpty(errorMap)) {
				Map<String, Object> excObj = new HashMap<>();
				excObj.put(Gstr1ConstantsV31.ERROR_REPORT, errorMap);
				excObj.put(Gstr1ConstantsV31.ERROR_DESC, ErrorCodesV31.ASP103);
				excObj.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP);
				excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE, HttpStatus.BAD_REQUEST);
				throw new AspExceptionV31("Error during Payload ValidationV31", null, false, false, excObj);
			}
		}
	}

	private Map<String, Object> populateMap(Map<String, Object> inputSecMap, String isSubmitCheck){
		String invNum = MapUtils.getString(inputSecMap, "inum");
		String invDt = MapUtils.getString(inputSecMap, "idt");
		String ntNum = MapUtils.getString(inputSecMap, "nt_num");
		String pos = MapUtils.getString(inputSecMap, "pos");
		String ctin = MapUtils.getString(inputSecMap, "ctin");
		Map<String, Object> itemMap = new HashMap<>();
		itemMap.put("error_msg",isSubmitCheck);
		itemMap.put("inv_num", invNum);
		itemMap.put("date", invDt);
		itemMap.put("nt_num", ntNum);
		itemMap.put("pos", pos);
		itemMap.put("ctin", ctin);

		return itemMap;
	}
	
	private Map<String, String> formatSavedIdData(List<Map<String, Object>> list) {
		Map<String, String> idMap = new HashMap<>();
		for (Map<String, Object> map : list) {
			String section = (String) ((Map) map.get("control")).get("type");
			String id = (String) map.get("_id");
			idMap.put(id, section);
			
		}
		return idMap;
	}
	
	private Map<String, String> formatSavedIdDataAmendments(List<Map<String, Object>> list) {
		Map<String, String> idMap = new HashMap<>();
		for (Map<String, Object> map : list) {
			String section = (String) ((Map) map.get("control")).get("type");
			String id = null; 
			
			if(amendmentNonAggreTypeList.contains(section)){
				id = (String) ((Map) map.get("control")).get("_id");
				idMap.put(id, section);
			}
		}
		return idMap;
	}
	
	/* Extract the yyyy from "dd-mm-yyyy" format
	 * */
	@Override
	public String getYear(String oidt) {
		String year = "";
		
	    if (oidt == null) {
			Map<String, Object> excObj = new HashMap<>();
			excObj.put(Gstr1ConstantsV31.ERROR_REPORT, "null date");
			excObj.put(Gstr1ConstantsV31.ERROR_DESC, ErrorCodesV31.ASP103);
			excObj.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP);
			excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE, HttpStatus.BAD_REQUEST);
			throw new AspExceptionV31("Error during Payload ValidationV31", null, false, false, excObj);
	    }

	    // match only a 4 digit year
	    Pattern yearPat = Pattern.compile("^.*([\\d]{4}).*$");

	   // obtain a matcher, and then see if we have the expected value
	    Matcher match = yearPat.matcher(oidt);
	    if (match.matches() && match.groupCount() == 1) {
	        year = match.group(1);
	    }

	    return year;
	}

	private String createNonAggregateDocObjectId(Map<String, Object> controlMap, Map<String, Object> inputSecMap,
			Map<String, Object> headerMap, Map<String, String> reqHeaderMap) {
		String id = null;
		String type = MapUtils.getString(controlMap, Gstr1ConstantsV31.CONTROL_JSON_TYPE);
		StringBuilder sb = new StringBuilder();
		String fp = MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_FP);
		String fy = String.valueOf((Integer.parseInt(getFY(fp)) -1));
		
		if (Gstr1ConstantsV31.TYPE_MONGO_KEY_8.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for expa, B2ba, B2cla
			id = sb.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fy).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.INPUT_OINUM)).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.KEY_APPENDMENT_A)
					.toString();
		}else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_9.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for cdnra.
			id = sb.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fy).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.TYPE_MONGO_KEY_CDNR_CDNUR).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.INPUT_ONT_NUM)).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.KEY_APPENDMENT_A)
					.toString();
		}
		return id;
	}
	
	private String createNonAggregateDocObjectIdwithCurrentYear(Map<String, Object> controlMap, Map<String, Object> inputSecMap,
			Map<String, Object> headerMap, Map<String, String> reqHeaderMap) {
		String id = null;
		String type = MapUtils.getString(controlMap, Gstr1ConstantsV31.CONTROL_JSON_TYPE);
		StringBuilder sb = new StringBuilder();
		String fp = MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_FP);
		String fy =getFY(fp);
		
		if (Gstr1ConstantsV31.TYPE_MONGO_KEY_8.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for expa, B2ba, B2cla
			id = sb.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fy).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.INPUT_OINUM)).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.KEY_APPENDMENT_A).toString();
		}else if (Gstr1ConstantsV31.TYPE_MONGO_KEY_9.indexOf(type + Gstr1ConstantsV31.GSTR1_HASH) != -1) {
			// creating id for cdnra.
			id = sb.append(MapUtils.getString(headerMap, Gstr1ConstantsV31.INPUT_GSTN))
					.append(Gstr1ConstantsV31.MONGO_KEY_SEP).append(fy).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.TYPE_MONGO_KEY_CDNR_CDNUR).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1ConstantsV31.INPUT_ONT_NUM)).append(Gstr1ConstantsV31.MONGO_KEY_SEP)
					.append(Gstr1ConstantsV31.KEY_APPENDMENT_A).toString();
		}
		return id;
	}
	
	public void validatePayloadforAmendments(List<Map<String, Object>> uniqueMongoIds, List<Map<String, Object>> flatJson) {
		if (CollectionUtils.isNotEmpty(uniqueMongoIds)) {
			Map<String, Object> whrObj = new HashMap<>();
			String[] status = new String[2];
			status[0] = Gstr1ConstantsV31.STATUS_SUBMIT_UPLOAD;
			status[1] = Gstr1ConstantsV31.STATUS_FILE_UPLOAD;
			whrObj.put("control." + Gstr1ConstantsV31.CONTROL_JSON_GSTN_STATE, status);
			String[] fields = new String[2];
			fields[0] = "gstn";
			fields[1] = "control";
			// ASP526
			String supCollection = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
			Map<String, Object> errorMap = null;
			List<Map<String, Object>> list = aspMongoDao.getMongoData(uniqueMongoIds, fields, supCollection, whrObj);
			
			if (CollectionUtils.isNotEmpty(list) && CollectionUtils.isNotEmpty(flatJson)) {
				
				for (Map<String, Object> map2 : flatJson) {
					
					Map<String, Object> controlSecMap = (Map<String, Object>) map2
							.get(Gstr1ConstantsV31.CONTROL_JSON_SEC);
					
					String section = (String) controlSecMap
							.get(Gstr1ConstantsV31.CONTROL_JSON_TYPE);
									
					if (amendmentNonAggreTypeList.contains(section)) {
						
						Map<String, Object> sendGSTNMap = (Map<String, Object>) map2
								.get(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC);
						
						String oinum = MapUtils.getString(sendGSTNMap, "oinum");
					
						for(Map<String, Object> dataDBGSTN : list){
							
								Map<String, Object> dataDBGSTN1 = (Map<String, Object>) dataDBGSTN
										.get(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC);
								
								Map<String, Object> dbControlSecMap = (Map<String, Object>) dataDBGSTN
										.get(Gstr1ConstantsV31.CONTROL_JSON_SEC);
								
								String dbSection = (String) dbControlSecMap
										.get(Gstr1ConstantsV31.CONTROL_JSON_TYPE);
								
								String dbinum = MapUtils.getString(dataDBGSTN1, "inum");
								
								
								if (StringUtils.isNotEmpty(section) && StringUtils.isNotEmpty(dbSection) && 
										!oinum.equalsIgnoreCase(dbinum)) {
									errorMap = new HashMap<>();
									Map<String, List<Map<String, Object>>> secMap = new HashMap<>();
									
									errorMap.put("errorReport", secMap);
									if (secMap.containsKey(section)) {
										List<Map<String, Object>> itemList = secMap.get(section);
										Map<String, Object> itemMap = populateMap(sendGSTNMap, "");
										itemList.add(itemMap);
										secMap.put(section, itemList);
									} else {
										List<Map<String, Object>> itemList = new ArrayList<>();
										Map<String, Object> itemMap = populateMap(sendGSTNMap, "");
										itemList.add(itemMap);
										secMap.put(section, itemList);
									}
								}
						}
					}
				}
			}

			if (MapUtils.isNotEmpty(errorMap)) {
				Map<String, Object> excObj = new HashMap<>();
				excObj.put(Gstr1ConstantsV31.ERROR_REPORT, errorMap);
				excObj.put(Gstr1ConstantsV31.ERROR_DESC, ErrorCodesV31.ASP103);
				excObj.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP);
				excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE, HttpStatus.BAD_REQUEST);
				throw new AspExceptionV31("Error during Payload ValidationV31", null, false, false, excObj);
			}
		}
	}
	
	public static boolean compareDates(String psDate1, String psDate2){
        SimpleDateFormat dateFormat = new SimpleDateFormat ("dd-MM-yyyy");
        Date date1 = null;
        Date date2 = null;
		try {
			date1 = dateFormat.parse(psDate1);
			date2 = dateFormat.parse(psDate2);
		} catch (ParseException e) {
			Map<String, Object> excObj = new HashMap<>();
			excObj.put(Gstr1ConstantsV31.ERROR_REPORT, "Issue with the date format");
			excObj.put(Gstr1ConstantsV31.ERROR_DESC, ErrorCodesV31.ASP103);
			excObj.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP);
			excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE, HttpStatus.BAD_REQUEST);
			throw new AspExceptionV31("Error during Payload ValidationV31", null, false, false, excObj);
		}
       
        if(date2.compareTo(date1) != 0) {
            return true;
        } else {
            return false;
        }
    }
}
