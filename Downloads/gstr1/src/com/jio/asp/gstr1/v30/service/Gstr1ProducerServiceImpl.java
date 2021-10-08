package com.jio.asp.gstr1.v30.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.exception.AspException;
import com.jio.asp.gstr1.v30.exception.RestExceptionHandler;
import com.jio.asp.gstr1.v30.util.CommonUtil;

@Service
public class Gstr1ProducerServiceImpl implements Gstr1ProducerService {
	private static final Logger log = LoggerFactory.getLogger(Gstr1ProducerServiceImpl.class);
	@Autowired
	private KafkaProducer kafkaService;
	@Autowired
	private AspLoggingService aspLoggingService;
	@Autowired
	private MessageSource gstnResource;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private MessageSource messageSource;

	private static List<String> type1List, type2List;

	/**
	 * Method prepares exclusion list for the attribute which needs be added to
	 * final aggregated invoice output.
	 * 
	 * @return List of excluded attributes.
	 */
	static {
		log.debug("prepareType1List method: START");
		type1List = new ArrayList<>();
		type1List.add(Gstr1Constants.TYPE_B2B);
		type1List.add(Gstr1Constants.TYPE_B2BA);
		type1List.add(Gstr1Constants.TYPE_B2CL);
		type1List.add(Gstr1Constants.TYPE_B2CLA);
		type1List.add(Gstr1Constants.TYPE_EXP);
		type1List.add(Gstr1Constants.TYPE_EXPA);
		type1List.add(Gstr1Constants.TYPE_CDNR);
		type1List.add(Gstr1Constants.TYPE_CDNRA);
		type1List.add(Gstr1Constants.TYPE_CDNUR);
		type1List.add(Gstr1Constants.TYPE_HSN);
		type1List.add(Gstr1Constants.TYPE_AT);
		type1List.add(Gstr1Constants.TYPE_ATA);
		type1List.add(Gstr1Constants.TYPE_TXPD);
		type1List.add(Gstr1Constants.TYPE_DOCS);
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
		type2List.add(Gstr1Constants.TYPE_B2CS);
		type2List.add(Gstr1Constants.TYPE_NIL);
		// type1List.add(Gstr1Constants.TYPE_B2CSA);
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
			String convertUrl = gstnResource.getMessage("url.json_converter", null, LocaleContextHolder.getLocale());
			HttpEntity<String> entity = new HttpEntity<String>(flatPayLoad, getUidHeader);
			SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
			int readTimeOut = Integer
					.parseInt(gstnResource.getMessage("rest-read-timeout", null, LocaleContextHolder.getLocale()));
			int connectionTimeOut = Integer.parseInt(
					gstnResource.getMessage("rest-connection-timeout", null, LocaleContextHolder.getLocale()));
			rf.setReadTimeout(readTimeOut);
			rf.setConnectTimeout(connectionTimeOut);
			restTemplate.setErrorHandler(new RestExceptionHandler());
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
					if (jsonNode!=null && jsonNode.has(Gstr1Constants.ERROR_REPORT)) {
						JsonNode errorReport = jsonNode.get(Gstr1Constants.ERROR_REPORT);
						if(jsonNode.has(Gstr1Constants.CONTROL_WARN_REPORT)){
							JsonNode warnReport = jsonNode.get(Gstr1Constants.CONTROL_WARN_REPORT);
							excObj.put(Gstr1Constants.CONTROL_WARN_REPORT, warnReport);
						}
						excObj.put(Gstr1Constants.ERROR_REPORT, errorReport);
						excObj.put(Gstr1Constants.ERROR_DESC, ErrorCodes.ASP103);
						excObj.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.ASP_GSTR1A_SAVE_GRP);
						excObj.put(Gstr1Constants.ERROR_HTTP_CODE, HttpStatus.BAD_REQUEST);
						throw new AspException("Error during Payload Validation", null, false, false, excObj);
					} else {
						log.error("Exception occurred while generating convertor output");
						CommonUtil.throwException(ErrorCodes.ASP103, Gstr1Constants.ASP_GSTR1A_SAVE_GRP,
								ErrorCodes.ASP103, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource, null);
					}
				} catch (IOException e) {
					log.error("Exception occurred in while parsing {}", e);
					CommonUtil.throwException(ErrorCodes.ASP103, Gstr1Constants.ASP_GSTR1A_SAVE_GRP, ErrorCodes.ASP103,
							null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource, null);
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
	public Map<String, String> saveInvoiceV4(Map<String, Object> requestBody, Map<String, String> reqHeaderMap,
			String ackNo) {
		log.debug("saveInvoice method: START");
		log.info("saveInvoice method:Starting the processing of request with ackno: {}", ackNo);
		log.debug("saveInvoice method: requestBody received for ackno: {}", ackNo);
		log.debug("saveInvoice method: formatting request payload json for ackno: {}", ackNo);
		List<Map<String, Object>> flatJson = convertInputToFlatJsonV4(requestBody, Gstr1Constants.INVOICE_STATE_NEW,
				ackNo, reqHeaderMap);
		log.info("saveInvoice method: after parsing the json to per invoice structure ");

		log.debug("saveInvoice method: after formatting request payload json for ackno: {}", ackNo);
		log.debug("saveInvoice method: converted json for ackNo {}", ackNo);
		Map<String, Object> controlDataMap = new HashMap<>();
		controlDataMap.put(Gstr1Constants.CONTROL_REC_ID, ackNo);
		controlDataMap.put(Gstr1Constants.CONTROL_REC_ACKNO, ackNo);
		controlDataMap.put(Gstr1Constants.CONTROL_REC_CLIENT_ID,
				MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_CLIENT_ID));
		controlDataMap.put(Gstr1Constants.CONTROL_REC_USRACT, Gstr1Constants.API_SAVE);
		controlDataMap.put(Gstr1Constants.CONTROL_REC_STATUS, Gstr1Constants.CONTROL_REC_PR_STATUS);
		controlDataMap.put(Gstr1Constants.CONTROL_REC_RCRDCOUNT, flatJson.size());
		controlDataMap.put(Gstr1Constants.CONTROL_REC_SUCCCNT, 0);
		controlDataMap.put(Gstr1Constants.CONTROL_REC_ERRCNT, 0);
		controlDataMap.put(Gstr1Constants.CONTROL_REC_WARNCNT, 0);
		controlDataMap.put(Gstr1Constants.CONTROL_REC_GSTIN,
				MapUtils.getString(requestBody, Gstr1Constants.INPUT_GSTN));
		SimpleDateFormat sdf = new SimpleDateFormat(Gstr1Constants.HEADER_DATE_FORMAT);
		Date date = new Date();
		controlDataMap.put(Gstr1Constants.CONTROL_REC_STRTTIME, sdf.format(date));
		controlDataMap.put(Gstr1Constants.CONTROL_REC_UPDTTIME, "");
		controlDataMap.put(Gstr1Constants.CONTROL_REC_TIMEOUT, "");
		controlDataMap.put(Gstr1Constants.CONTROL_WARN_REPORT, MapUtils.getObject(requestBody, Gstr1Constants.CONTROL_WARN_REPORT,null));
		aspLoggingService.generateControlLog(controlDataMap);
		log.debug("saveInvoice method: before pushing to kafka for ackno: {}", ackNo);
		log.info("saveInvoice method: spawning a thread for pushing to kafka for ackno: {}", ackNo);
		String gstr1Topic = gstnResource.getMessage("gstr1-kafka-topic", null, LocaleContextHolder.getLocale());
		kafkaService.postKafkaMsg(flatJson, ackNo, gstr1Topic);
		log.info("saveInvoice method: Continuing with response after spawning kafka thread for ackno: {}", ackNo);
		log.debug("saveInvoice method: after pushing to kafka for ackno: {}", ackNo);

		Map<String, String> respMap = new HashMap<>();
		respMap.put(Gstr1Constants.GSTR1_RESP_ACKNO, ackNo);
		respMap.put(Gstr1Constants.GSTR1_RESP_STATUS_CD, Gstr1Constants.GSTR1_RESP_SUCCESS_ST_CD);

		log.debug("saveInvoice method: generating response resp: {}", respMap);
		log.info("saveInvoice method: generating response resp: {}", respMap);
		log.debug("saveInvoice method: END");
		return respMap;
	}

	@Override
	public List<Map<String, Object>> convertInputToFlatJsonV4(Map<String, Object> requestBody, String action,
			String ackNo, Map<String, String> reqHeaderMap) {
		log.debug("convertInputToFlatJson method: START for ackNo: {}", ackNo);
		Iterator<String> it = requestBody.keySet().iterator();
		Map<String, Object> headerDataMap = null;
		List<Map<String, Object>> flatJson = new ArrayList<>();
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
			if (type1List.contains(type)) {
				flatJson.addAll(processSectionV4(jsonObj, headerDataMap, action, type, ackNo, reqHeaderMap));
			} else if (type2List.contains(type)) {
				flatJson.addAll(processB2csSectionV4(jsonObj, headerDataMap, action, type, ackNo, reqHeaderMap));
			}
		}
		log.debug("convertInputToFlatJson method: END for ackNo: {}", ackNo);
		return flatJson;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> processSectionV4(Object jsonObj, Map<String, Object> headerMap, String action,
			String type, String ackNo, Map<String, String> reqHeaderMap) {
		log.debug("processSectionV4 method: START for ackNo {}", ackNo);
		SimpleDateFormat sdf = new SimpleDateFormat(Gstr1Constants.HEADER_DATE_FORMAT);
		List<Map<String, Object>> flatJson = new ArrayList<>();
		// retrieving the exclude list based on type.

		if (jsonObj instanceof List<?>) {
			List<Object> inputSecList = (List<Object>) jsonObj;
			log.debug("processSectionV4 method: start of loop for ackNo {}", ackNo);
			for (Object inpSecObj : inputSecList) {
				Map<String, Object> inputSecMap = (Map<String, Object>) inpSecObj;
				log.debug("processSectionV4 method: create control map start for ackNo {}", ackNo);
				Map<String, Object> controlMap = createControlMap(inputSecMap, headerMap, reqHeaderMap, ackNo, type,
						sdf);
				log.debug("processSectionV4 method: create control map end for ackNo {}", ackNo);
				String mongoId = createDocObjectId(controlMap, inputSecMap, headerMap, reqHeaderMap);
				log.debug("processSectionV4 method: create object id end for ackNo {}", ackNo);
				controlMap.put(Gstr1Constants.CONTROL_JSON_ID, mongoId);
				Map<String, Object> invoiceMap = new HashMap<>();
				invoiceMap.put(Gstr1Constants.CONTROL_JSON_CUST_SEC,
						MapUtils.getObject(inputSecMap, Gstr1Constants.CONTROL_JSON_CUST_SEC));
				inputSecMap.remove(Gstr1Constants.CONTROL_JSON_CUST_SEC);
				invoiceMap.put(Gstr1Constants.CONTROL_JSON_SEC, controlMap);
				invoiceMap.put(Gstr1Constants.CONTROL_JSON_GSTN_SEC, inputSecMap);
				invoiceMap.put(Gstr1Constants.CONTROL_JSON_HDR_SEC, headerMap);
				flatJson.add(invoiceMap);
			}
			log.debug("processSectionV4 method: end of loop for ackNo {}", ackNo);
		}
		log.debug("processSectionV4 method: END for ackNo {}", ackNo);
		return flatJson;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> processB2csSectionV4(Object jsonObj, Map<String, Object> headerMap, String action,
			String type, String ackNo, Map<String, String> reqHeaderMap) {
		log.debug("processB2csSectionV4 method: START for ackNo {}", ackNo);
		SimpleDateFormat sdf = new SimpleDateFormat(Gstr1Constants.HEADER_DATE_FORMAT);
		List<Map<String, Object>> flatJson = new ArrayList<>();
		// retrieving the exclude list based on type.

		if (jsonObj instanceof List<?>) {
			List<Object> inputSecList = (List<Object>) jsonObj;
			List<Object> b2csList = new ArrayList<>();
			Map<String, Object> controlMap = null;
			Map<String, Object> invoiceMap = new HashMap<>();
			Map<String, Object> customDataMap = new HashMap<>();
			for (Object inpSecObj : inputSecList) {
				Map<String, Object> inputSecMap = (Map<String, Object>) inpSecObj;
				customDataMap.putAll((Map<String, Object>) inputSecMap.get(Gstr1Constants.CONTROL_JSON_CUST_SEC));
				inputSecMap.remove(Gstr1Constants.CONTROL_JSON_CUST_SEC);
				controlMap = createControlMap(inputSecMap, headerMap, reqHeaderMap, ackNo, type, sdf);
				String mongoId = createDocObjectId(controlMap, inputSecMap, headerMap, reqHeaderMap);
				controlMap.put(Gstr1Constants.CONTROL_JSON_ID, mongoId);
				b2csList.add(inputSecMap);
			}
			invoiceMap.put(Gstr1Constants.CONTROL_JSON_SEC, controlMap);
			invoiceMap.put(Gstr1Constants.CONTROL_JSON_CUST_SEC, customDataMap);
			invoiceMap.put(Gstr1Constants.CONTROL_JSON_GSTN_SEC, b2csList);
			invoiceMap.put(Gstr1Constants.CONTROL_JSON_HDR_SEC, headerMap);
			flatJson.add(invoiceMap);
		}
		log.info("processB2csSectionV4 method: after processing b2cs section data for ackNo {}", ackNo);
		log.debug("processB2csSectionV4 method: END for ackNo {}", ackNo);
		return flatJson;
	}

	private Map<String, Object> createControlMap(Map<String, Object> inputSecMap, Map<String, Object> headerMap,
			Map<String, String> reqHeaderMap, String ackNo, String type, SimpleDateFormat sdf) {
		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(Gstr1Constants.CONTROL_JSON_CLIENT_ID,
				MapUtils.getString(reqHeaderMap, Gstr1Constants.HEADER_CLIENT_ID));
		String state = Gstr1Constants.INVOICE_STATE_NEW;
		if (!Gstr1Constants.TYPE_B2CS.equals(type)) {
			if (MapUtils.getString(inputSecMap, Gstr1Constants.JSON_ACTION, "")
					.equalsIgnoreCase(Gstr1Constants.INVOICE_INPUT_STATE_DEL)) {
				state = Gstr1Constants.INVOICE_STATE_DEL;
			} else if (MapUtils.getString(inputSecMap, Gstr1Constants.JSON_ACTION, "")
					.equalsIgnoreCase(Gstr1Constants.INVOICE_INPUT_STATE_PARK)) {
				state = Gstr1Constants.INVOICE_STATE_PARK;
			}
		}
		controlMap.put(Gstr1Constants.CONTROL_JSON_STATE, state);
		controlMap.put(Gstr1Constants.CONTROL_JSON_TRANSID, ackNo);
		controlMap.put(Gstr1Constants.CONTROL_JSON_TYPE, type);
		controlMap.put(Gstr1Constants.CONTROL_JSON_LEVEL, identifyLevel(type));
		String dt = sdf.format(new Date());
		controlMap.put(Gstr1Constants.CONTROL_JSON_ITIME, dt);
		controlMap.put(Gstr1Constants.CONTROL_JSON_UTIME, dt);
		return controlMap;
	}

	private String createDocObjectId(Map<String, Object> controlMap, Map<String, Object> inputSecMap,
			Map<String, Object> headerMap, Map<String, String> reqHeaderMap) {
		String id = null;
		String type = MapUtils.getString(controlMap, Gstr1Constants.CONTROL_JSON_TYPE);
		StringBuilder sb = new StringBuilder();
		String fp = MapUtils.getString(headerMap, Gstr1Constants.INPUT_FP);
		String fy = getFY(fp);
		// creating id for b2b, b2cl,exp, amendments.
		if (Gstr1Constants.TYPE_MONGO_KEY_1.indexOf(type + Gstr1Constants.GSTR1_HASH) != -1) {
			id = sb.append(MapUtils.getString(headerMap, Gstr1Constants.INPUT_GSTN))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(fy).append(Gstr1Constants.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1Constants.INPUT_INUM)).toString();
		} else if (Gstr1Constants.TYPE_MONGO_KEY_2.indexOf(type + Gstr1Constants.GSTR1_HASH) != -1) {
			// creating id for cdn,cdna.
			id = sb.append(MapUtils.getString(headerMap, Gstr1Constants.INPUT_GSTN))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(fy).append(Gstr1Constants.MONGO_KEY_SEP)
					.append(Gstr1Constants.TYPE_MONGO_KEY_CDNR_CDNUR).append(Gstr1Constants.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1Constants.INPUT_NT_NUM)).toString();
		} else if (Gstr1Constants.TYPE_MONGO_KEY_3.indexOf(type + Gstr1Constants.GSTR1_HASH) != -1) {
			// creating id for b2cs,b2csa.
			id = sb.append(MapUtils.getString(controlMap, Gstr1Constants.CONTROL_JSON_CLIENT_ID))
					.append(Gstr1Constants.MONGO_KEY_SEP)
					.append(MapUtils.getString(headerMap, Gstr1Constants.INPUT_GSTN))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(fy).append(Gstr1Constants.MONGO_KEY_SEP).append(type)
					.append(Gstr1Constants.MONGO_KEY_SEP).append(fp).toString();
		} else if (Gstr1Constants.TYPE_MONGO_KEY_4.indexOf(type + Gstr1Constants.GSTR1_HASH) != -1) {
			// creating id for HSN.
			id = sb.append(MapUtils.getString(controlMap, Gstr1Constants.CONTROL_JSON_CLIENT_ID))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(MapUtils.getString(headerMap, Gstr1Constants.INPUT_GSTN))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(fp).append(Gstr1Constants.MONGO_KEY_SEP).append(type)
					.append(Gstr1Constants.MONGO_KEY_SEP).append(MapUtils.getString(inputSecMap, Gstr1Constants.HSN_SC))
					.toString();
		} else if (Gstr1Constants.TYPE_MONGO_KEY_5.indexOf(type + Gstr1Constants.GSTR1_HASH) != -1) {
			// creating id for AT,ATA, TXPD
			id = sb.append(MapUtils.getString(controlMap, Gstr1Constants.CONTROL_JSON_CLIENT_ID))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(MapUtils.getString(headerMap, Gstr1Constants.INPUT_GSTN))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(fp)
					.append(Gstr1Constants.MONGO_KEY_SEP).append(type).append(Gstr1Constants.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1Constants.INPUT_POS)).toString();
		} else if (Gstr1Constants.TYPE_MONGO_KEY_6.indexOf(type + Gstr1Constants.GSTR1_HASH) != -1) {
			// creating id for docs_issue
			id = sb.append(MapUtils.getString(controlMap, Gstr1Constants.CONTROL_JSON_CLIENT_ID))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(MapUtils.getString(headerMap, Gstr1Constants.INPUT_GSTN))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(fp)
					.append(Gstr1Constants.MONGO_KEY_SEP).append(type).append(Gstr1Constants.MONGO_KEY_SEP)
					.append(MapUtils.getString(inputSecMap, Gstr1Constants.DOC_TYPE)).toString();
		} else if (Gstr1Constants.TYPE_MONGO_KEY_7.indexOf(type + Gstr1Constants.GSTR1_HASH) != -1) {
			// creating id for nil
			id = sb.append(MapUtils.getString(controlMap, Gstr1Constants.CONTROL_JSON_CLIENT_ID))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(MapUtils.getString(headerMap, Gstr1Constants.INPUT_GSTN))
					.append(Gstr1Constants.MONGO_KEY_SEP).append(fp)
					.append(Gstr1Constants.MONGO_KEY_SEP).append(type).toString();
		}
		return id;
	}

	private String identifyLevel(String type) {
		if (Gstr1Constants.TYPE_B2B.equals(type) || Gstr1Constants.TYPE_CDNR.equals(type)
				|| Gstr1Constants.TYPE_B2CL.equals(type) || Gstr1Constants.TYPE_EXP.equals(type)
				|| Gstr1Constants.TYPE_CDNUR.equals(type)) {
			return Gstr1Constants.LEVEL_INV;
		} else {
			return Gstr1Constants.LEVEL_MONTHLY;
		}
	}

	private String getFY(String fp) {
		String fy = fp;
		if (StringUtils.isNotBlank(fp) && fp.length() == 6) {
			fy = fp.substring(2);
		}
		return fy;
	}

	}
