package com.jio.asp.gstr1.v31.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jio.asp.gstr1.v30.exception.AspException;
import com.jio.asp.gstr1.v30.exception.GspException;
import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.exception.AspExceptionV31;
import com.jio.asp.gstr1.v31.exception.GspExceptionV31;
import com.jio.asp.gstr1.v31.util.AESEncryptionV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.gstr1.v31.util.HmacGeneratorV31;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@Service
public class SaveSuppliesToGstnServiceV31Impl implements SaveSuppliesToGstnServiceV31 {

	private static final Logger log = LoggerFactory.getLogger(SaveSuppliesToGstnServiceV31Impl.class);
	@Autowired
	private AspMongoDaoV31 aspMongoDaoV31;
	@Autowired
	private MessageSource gstnResource;
	@Autowired
	private MessageSource messageSourceV31;
	@Autowired
	private GSPServiceV31 gspService;
	@Autowired
	private AspAckNumServiceV31 ackService;
	@Autowired
	private AspGstr1ReturnStatusServiceV31 retStatusService;
	@Autowired
	private AspLoggingServiceV31 aspLoggingServiceV31;

	private Gson gson = new Gson();
	private ObjectMapper mapper = new ObjectMapper();

	private static List<String> filterList;
	// static final List<String> invSecList;
	// static final List<String> aggSecList;
	static final List<String> statusList;

	static {
		filterList = new ArrayList<>();
		filterList.add("rt");

		/*
		 * invSecList = new ArrayList<>();
		 * invSecList.add(Gstr1ConstantsV31.TYPE_B2B);
		 * invSecList.add(Gstr1ConstantsV31.TYPE_B2CL);
		 * invSecList.add(Gstr1ConstantsV31.TYPE_CDNR);
		 * invSecList.add(Gstr1ConstantsV31.TYPE_CDNUR);
		 * invSecList.add(Gstr1ConstantsV31.TYPE_EXP);
		 * 
		 * aggSecList = new ArrayList<>();
		 * aggSecList.add(Gstr1ConstantsV31.TYPE_B2CS);
		 * aggSecList.add(Gstr1ConstantsV31.TYPE_AT);
		 * aggSecList.add(Gstr1ConstantsV31.TYPE_TXPD);
		 * aggSecList.add(Gstr1ConstantsV31.TYPE_NIL);
		 * aggSecList.add(Gstr1ConstantsV31.TYPE_DOCS);
		 * aggSecList.add(Gstr1ConstantsV31.TYPE_HSN);
		 */
		statusList = new ArrayList<>();
		statusList.add(Gstr1ConstantsV31.INVOICE_STATE_NEW);
		statusList.add(Gstr1ConstantsV31.STATUS_UPLOAD);
	}

	@Override
	public String processSuppliesDataInBatches(Map<String, String> allRequestParams, Map<String, String> reqHeaderMap,
			Map<String, String> reqHeaderBody) {
		log.debug("processSuppliesData method : START");
		log.info("processSuppliesData method : START");
		String ackNumber = ackService.generateAckNumber(Gstr1ConstantsV31.API_NM);
		List<Object> decryptedDataList = new ArrayList<>();
		float pageSize = Float
				.parseFloat(gstnResource.getMessage("save_to_gstn_pageSize", null, LocaleContextHolder.getLocale()));
		String gstr1GstnCol = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
		// int totalRecords = (int)
		// aspMongoDaoV31.getRecordsCount(allRequestParams, gstr1GstnCol);
		List<String> secList = new ArrayList<>();
		secList.add(Gstr1ConstantsV31.TYPE_B2B);
		secList.add(Gstr1ConstantsV31.TYPE_B2CL);
		secList.add(Gstr1ConstantsV31.TYPE_CDNR);
		secList.add(Gstr1ConstantsV31.TYPE_CDNUR);
		secList.add(Gstr1ConstantsV31.TYPE_EXP);
		Map<String, String> aggSecMap = new HashMap<>();
		aggSecMap.put("1", Gstr1ConstantsV31.TYPE_B2CS);
		aggSecMap.put("2", Gstr1ConstantsV31.TYPE_AT);
		aggSecMap.put("3", Gstr1ConstantsV31.TYPE_TXPD);
		aggSecMap.put("4", Gstr1ConstantsV31.TYPE_NIL);
		aggSecMap.put("5", Gstr1ConstantsV31.TYPE_DOCS);
		aggSecMap.put("6", Gstr1ConstantsV31.TYPE_HSN);

		int totalRecords = (int) aspMongoDaoV31.getRecordsCount(allRequestParams, gstr1GstnCol, secList);
		Map<String, String> gspresponse = new HashMap<>();
		Map<String, Object> response = new HashMap<>();
		int totalNumberOfPages = (int) getBatchSize(allRequestParams, pageSize, totalRecords);
		// increasing total number of pages by 1 for checking for aggregate
		int agCnt = aggSecMap.size();
		// section data
		totalNumberOfPages = totalNumberOfPages + agCnt;
		boolean dataPresent = false;
		int aggIdx = 1;
		for (int i = 0; i < totalNumberOfPages; i++) {
			// long offset = i > 0 ? (i * pageSize) : 0;
			boolean isAggregate = false;
			List<Map<String, Object>> mapList = null;
			if (i < (totalNumberOfPages - agCnt)) {
				long offset = 0;
				int remRcrd = (int) ((int) totalRecords - (pageSize * i));
				long limit = (long) pageSize;
				if (remRcrd < pageSize) {
					limit = remRcrd;
				}
				mapList = aspMongoDaoV31.getDataForGstnInBatches(allRequestParams, offset, limit, gstr1GstnCol, true,
						null);
			} else {
				isAggregate = true;
				String sec = aggSecMap.get("" + aggIdx);
				mapList = aspMongoDaoV31.getDataForGstnInBatches(allRequestParams, 0, -1, gstr1GstnCol, false, sec);
				aggIdx++;
			}
			if (mapList != null && mapList.size() > 0) {
				dataPresent = true;
				List<String> idList = new ArrayList<>();
				Map<String, Object> finalMap = convertInGstnFormatInBatches(allRequestParams, mapList, reqHeaderBody,
						idList);
				log.debug("processSuppliesData method : finalMap");
				log.info("processSuppliesData method : finalMap");
				String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);
				reqHeaderMap.put(Gstr1ConstantsV31.INPUT_GSTN_RET_PER, fp);
				Map<String, Object> encryptedData = encryptData(reqHeaderMap, finalMap);
				JSONObject jsonObject = new JSONObject(encryptedData);
				log.debug("Recieved encryptedData");
				String url = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_GSTR1_URL, null,
						LocaleContextHolder.getLocale());
				log.debug("Before calling saveGstr1");
				// try {
				Map<String, String> responseMap = gspService.saveGstr1(reqHeaderMap, jsonObject.toString());
				String decryptedData = parseGspData(responseMap, reqHeaderMap);
				log.info("After getting the decryptedData1");
				String referenceId = getRefIdFromResponse(gstr1GstnCol, decryptedData, reqHeaderMap, idList);
				// decryptedDataList.add(decryptedData);
				log.info(
						"After getting the response from GSTN for **********BATCH NO::: {} **********Reference Id********** {}",
						i, referenceId);
				/*System.out.println("After getting the response from GSTN for **********BATCH NO::: " + i
						+ " **********Reference Id********** " + referenceId);
				System.out.println("After getting the response from GSTN id List ********** " + idList);*/
				Map<String, Object> controlStatusMap = new HashMap<>();
				controlStatusMap.put(Gstr1ConstantsV31.INPUT_REFERENCE_ID, referenceId);
				decryptedDataList.add(controlStatusMap);
				updateStatusInDbInBatches(gstr1GstnCol, decryptedData, reqHeaderMap, idList, isAggregate);
				// changes done for implementing Ret status call along with
				// the api. START

				gspresponse = gspService.getGstr1Status(reqHeaderMap, allRequestParams, referenceId);
				response = gspService.decryptResponse(reqHeaderMap, gspresponse);
				String status = MapUtils.getString(response, Gstr1ConstantsV31.GSTN_JSON_STATUS_CD);
				retStatusService.UpdateDbForGstnResponse(response, referenceId, allRequestParams,
						Gstr1ConstantsV31.STATUS_SAVED, Gstr1ConstantsV31.STATUS_UPLOAD);
				if (Gstr1ConstantsV31.GSTN_STATUS_CD_E.equals(status)) {
					Map<String, Object> errMap = (Map<String, Object>) response
							.remove(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
					finalMap.putAll(errMap);
					Map<String, Object> map = new HashMap<>();
					List<Map<String, Object>> errPayloadList = new ArrayList<>();
					errPayloadList.add(finalMap);
					map.put(Gstr1ConstantsV31.ERRORVAL, errPayloadList);
					response.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT, map);
					controlStatusMap.putAll(response);
				} else if (Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equals(status)
						|| Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equals(status)) {
					Map<String, Object> errMap = new HashMap<>();
					errMap.put("error_msg",
							"Uploaded records are still under processing by GSTN.Please check back after few minutes. "
									+ "If this status persist after few hours please redo the process once's more");
					errMap.put("error_cd", "RETIPERROR");
					finalMap.putAll(errMap);
					Map<String, Object> map = new HashMap<>();
					List<Map<String, Object>> errPayloadList = new ArrayList<>();
					errPayloadList.add(finalMap);
					map.put(Gstr1ConstantsV31.ERRORVAL, errPayloadList);
					response.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT, map);
					controlStatusMap.putAll(response);
				} else {
					controlStatusMap.putAll(response);
				}
				// changes done for implementing Ret status call along with
				// the api. END
				log.debug("processSuppliesData method : END");
				// } catch (Exception x) {
				// Map<String, Object> errorMap=new HashMap<>();
				// errorMap.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD, "E");
				// errorMap.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD, "GSTN did
				// not respond correctly for some request, please retry save to
				// GSTN");
				// }
			}

		}
		if (!dataPresent) {
			CommonUtilV31.throwException(ErrorCodesV31.ASP522, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP,
					ErrorCodesV31.ASP522, null, HttpStatus.OK, messageSourceV31, null);
		}
		createSaveGstnStatusData(decryptedDataList, ackNumber, allRequestParams);

		return ackNumber;
	}

	@Override
	public void initiateSavetoGstn(Map<String, String> reqMap, Map<String, String> headerMap, String ackNo,
			Map<String, String> payload) {
		log.debug("initiateSavetoGstn method : START AckNo:{}", ackNo);
		log.info("initiateSavetoGstn method : START AckNo:{}", ackNo);
		Map<String, Object> controlMap = new HashMap<>();
		controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, ackNo);
		controlMap.put(Gstr1ConstantsV31.ACKNUMBER, ackNo);
		controlMap.put("action", Gstr1ConstantsV31.TYPE_SAVE);
		controlMap.put("type", Gstr1ConstantsV31.API_NAME);
		controlMap.put(Gstr1ConstantsV31.INPUT_FP, reqMap.get(Gstr1ConstantsV31.INPUT_FP));
		controlMap.put(Gstr1ConstantsV31.INPUT_GSTN, reqMap.get(Gstr1ConstantsV31.INPUT_GSTN));
		reqMap.put(Gstr1ConstantsV31.ASPSTATUS, Gstr1ConstantsV31.ASP_STATUS_INIT);
		SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
		Date date = new Date();
		reqMap.put(Gstr1ConstantsV31.CONTROL_REC_STRTTIME, sdf.format(date));
		reqMap.put(Gstr1ConstantsV31.CONTROL_REC_USRACT, Gstr1ConstantsV31.API_SAVE_GSTN);
		reqMap.put(Gstr1ConstantsV31.CONTROL_JSON_VERSION, Gstr1ConstantsV31.API_VERSION.toString());
		reqMap.put(Gstr1ConstantsV31.CONTROL_REC_CLIENT_ID,
				MapUtils.getString(headerMap, Gstr1ConstantsV31.HEADER_CLIENT_ID));
		reqMap.put(Gstr1ConstantsV31.HEADER_USER_NAME,
				MapUtils.getString(headerMap, Gstr1ConstantsV31.HEADER_USER_NAME));
		reqMap.put(Gstr1ConstantsV31.HEADER_TXN, MapUtils.getString(headerMap, Gstr1ConstantsV31.HEADER_TXN));
		controlMap.put(Gstr1ConstantsV31.ASPDATA, reqMap);
		log.debug("initiateSavetoGstn method : Before writing the control log information AckNo:{}", ackNo);
		String coll = gstnResource.getMessage("gstnRefidMap.col", null, LocaleContextHolder.getLocale());
		aspLoggingServiceV31.generateControlLogSync(controlMap, coll);
		log.debug("initiateSavetoGstn method : After writing the control log information AckNo:{}", ackNo);
		log.debug("initiateSavetoGstn method : END AckNo:{}", ackNo);
		log.info("initiateSavetoGstn method : END AckNo:{}", ackNo);
	}

	private boolean isAggregate(String secName) {
		if (Gstr1ConstantsV31.aggSecList.contains(secName.toLowerCase())) {
			return true;
		}
		return false;
	}

	private boolean isInvoiceLevel(String secName) {
		if (Gstr1ConstantsV31.invSecList.contains(secName.toLowerCase())) {
			return true;
		}
		return false;
	}

	private Map<String, List<String>> segregateAggSecAndInvSec(String secName) {
		Map<String, List<String>> sectionMap = new HashMap<>();
		List<String> aggList = new ArrayList<>();
		List<String> invList = new ArrayList<>();
		if (isAggregate(secName)) {
			aggList.add(secName);
			sectionMap.put("AggSec", aggList);
		} else if (isInvoiceLevel(secName)) {
			invList.add(secName);
			sectionMap.put("InvSec", invList);
		}
		return sectionMap;
	}

	private Map<String, List<String>> retrieveSavetoGstnIds(String gstin, String fp, String gstr1GstnCol,
			Map<String, String> allRequestParams) {
		log.debug("retrieveSavetoGstnIds method : START");

		Map<String, Object> matchMap = new HashMap<>();
		Map<String, List<String>> idMap = null;
		matchMap.put("header.fp", fp);
		String sectionName = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_SECTION,
				Gstr1ConstantsV31.TYPE_ALL);
		if (!Gstr1ConstantsV31.TYPE_ALL.equals(sectionName)) {
			log.info("retrieveSavetoGstnIds method : Specific section was passed ");
			String secName = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_SECTION, "");
			Map<String, List<String>> sectionMap = segregateAggSecAndInvSec(secName);
			// getting all the ids for sections.
			if (sectionMap.containsKey("AggSec")) {
				matchMap.remove("control.gst_status");
				List<String> l = sectionMap.get("AggSec");
				matchMap.put("control.type", l);
				idMap = aspMongoDaoV31.getIdsForSaveToGstn(matchMap, null, gstr1GstnCol, gstin);
			} else if (sectionMap.containsKey("InvSec")) {
				matchMap.put("control.gst_status", statusList);
				List<String> l = sectionMap.get("InvSec");
				matchMap.put("control.type", l);
				if (idMap == null) {
					idMap = new HashMap<>();
				}
				Map<String, List<String>> temp = aspMongoDaoV31.getIdsForSaveToGstn(matchMap, null, gstr1GstnCol,
						gstin);
				if (temp != null) {
					idMap.putAll(temp);
				}
			}

		} else {
			log.info("retrieveSavetoGstnIds method : All as section was passed ");
			// getting ids for all the sections, in one call all aggregates and
			// in one call invoice level data
			matchMap.put("control.type", Gstr1ConstantsV31.invSecList);
			matchMap.put("control.gst_status", statusList);
			idMap = aspMongoDaoV31.getIdsForSaveToGstn(matchMap, null, gstr1GstnCol, gstin);
			if (idMap == null) {
				idMap = new HashMap<>();
			}
			// getting aggregates without any status.
			matchMap.put("control.type", Gstr1ConstantsV31.aggSecList);
			matchMap.remove("control.gst_status");
			Map<String, List<String>> temp = aspMongoDaoV31.getIdsForSaveToGstn(matchMap, null, gstr1GstnCol, gstin);
			if (temp != null) {
				idMap.putAll(temp);
			}
		}
		log.info("retrieveSavetoGstnIds method : ****************Returning Back after getting all the ids ");
		log.debug("retrieveSavetoGstnIds method : END");
		return idMap;
	}

	@Override
	public String saveSuppliesDataInGstn(Map<String, String> allRequestParams, Map<String, String> reqHeaderMap,
			Map<String, String> reqHeaderBody, String ackNumber) {
		log.debug("saveSuppliesDataInGstn method : START");
		String gstr1GstnCol = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
		String gstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, "");
		String fp = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_FP, "");

		Map<String, List<String>> idMap = retrieveSavetoGstnIds(gstin, fp, gstr1GstnCol, allRequestParams);

		Map<String, String> gspresponse = new HashMap<>();
		Map<String, Object> response = new HashMap<>();
		if (MapUtils.isNotEmpty(idMap)) {
			log.info("**************** Id values are no Null, Processing................");
			Iterator<String> idsIter = idMap.keySet().iterator();
			int pageSize = Integer
					.parseInt(gstnResource.getMessage("save_to_gstn_pageSize", null, LocaleContextHolder.getLocale()));
			log.info("****************Processing for Page SIZE {}", pageSize);
			boolean dataPresent = false;
			int batchNo = 0;
			while (idsIter.hasNext()) {
				String key = idsIter.next();
				boolean isAggregate = isAggregate(key);
				List<String> idLists = idMap.get(key);
				if (CollectionUtils.isNotEmpty(idLists)) {
					int totalRecords = idLists.size();
					int totalNumberOfPages = 1;
					if (!isAggregate) {
						totalNumberOfPages = (int) getBatchSize(allRequestParams, pageSize, totalRecords);
					}
					log.info("****************TOTAL RECORDS {} for Type {}", totalRecords, key);
					log.info("****************TOTAL Number of Pages {} for Type {}", totalNumberOfPages, key);

					for (int i = 0; i < totalNumberOfPages; i++) {
						Map<String, Object> controlStatusMap = new HashMap<>();
						int batchSize = 0;
						Map<String, Object> finalMap = null;
						String referenceId = null;
						try {
							List<Map<String, Object>> mapList = null;
							List<String> ids = null;
							if (isAggregate) {
								ids = idLists;
							} else {
								int offset = (pageSize * i);
								int remRcrd = (totalRecords - (pageSize * i));
								int limit = offset + pageSize;
								if (remRcrd < pageSize) {
									limit = offset + remRcrd;
								}
								log.info("****************OFFSET:::: {} and LIMIT:::: {} for Type {}", offset, limit,
										key);
								ids = idLists.subList(offset, limit);
							}
							batchSize = ids.size();
							mapList = aspMongoDaoV31.getDataForGstnInBatchesByIds(allRequestParams, ids, isAggregate,
									key, gstr1GstnCol);
							if (mapList != null && mapList.size() > 0) {
								log.info("****************AFTER GETTING DATA FOR IDS");
								dataPresent = true;
								List<String> idList = new ArrayList<>();
								finalMap = convertInGstnFormatInBatches(allRequestParams, mapList, reqHeaderBody,
										idList);
								log.debug("processSuppliesData method : finalMap");
								log.info("processSuppliesData method : finalMap");
								reqHeaderMap.put(Gstr1ConstantsV31.INPUT_GSTN_RET_PER, fp);
								Map<String, Object> encryptedData = encryptData(reqHeaderMap, finalMap);
								JSONObject jsonObject = new JSONObject(encryptedData);
								log.debug("Recieved encryptedData");
								log.debug("Before calling saveGstr1");
								// try {
								Map<String, String> responseMap = gspService.saveGstr1(reqHeaderMap,
										jsonObject.toString());
								String decryptedData = parseGspData(responseMap, reqHeaderMap);
								log.info("After getting the decryptedData");
								referenceId = getRefIdFromResponse(gstr1GstnCol, decryptedData, reqHeaderMap, idList);
								// decryptedDataList.add(decryptedData);
								log.info(
										"After getting the response from GSTN for **********BATCH NO::: {} **********Reference Id********** {}",
										batchNo, referenceId);
								updateStatusInDbInBatches(gstr1GstnCol, decryptedData, reqHeaderMap, idList,
										isAggregate);

								gspresponse = gspService.getGstr1Status(reqHeaderMap, allRequestParams, referenceId);
								response = gspService.decryptResponse(reqHeaderMap, gspresponse);
								String status = MapUtils.getString(response, Gstr1ConstantsV31.GSTN_JSON_STATUS_CD);
								int peCount = retStatusService.UpdateDbForGstnResponse(response, referenceId,
										allRequestParams, Gstr1ConstantsV31.STATUS_SAVED,
										Gstr1ConstantsV31.STATUS_UPLOAD);
								log.info(" **********BATCH NO::: {} **********Partial Error Count********** {}",
										batchNo, peCount);
								controlStatusMap = processRetStatusErrorCode(status, response, finalMap, isAggregate,
										batchSize, peCount);

								controlStatusMap.put(Gstr1ConstantsV31.INPUT_REFERENCE_ID, referenceId);
								controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_RCRDCOUNT, batchSize);
								controlStatusMap.put(Gstr1ConstantsV31.INPUT_SECTION, key);

							} // data map if ends
								// increment batchnumber
						} catch (Exception ex) {
							log.info("Exception occured while saving to GSTN for BATCH NO::: {}", batchNo);
							log.error("Exception occurred while doing save to GSTN for  {}", ex);
							controlStatusMap = handleExceptionFlow(ex, referenceId, controlStatusMap, finalMap,
									isAggregate, batchSize, key);
						}
						log.info("****************GOING FOR UPDATE OF BATCH IN CONTROL DATA for Batch No :::: {}",
								batchNo);
						Map<String, Object> errorReportMap = new HashMap<>();
						if (controlStatusMap.containsKey(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT)
								&& controlStatusMap.get(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT) != null) {
							if (controlStatusMap.get(Gstr1ConstantsV31.INPUT_REFERENCE_ID) == null) {
								String uuid = UUID.randomUUID().toString();
								log.debug("Reference ID is null");
								controlStatusMap.put(Gstr1ConstantsV31.INPUT_REFERENCE_ID, uuid);
							}
							errorReportMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID,
									String.valueOf(controlStatusMap.get(Gstr1ConstantsV31.INPUT_REFERENCE_ID)));
							errorReportMap.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT,
									controlStatusMap.get(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT));
							controlStatusMap.remove(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
							/*
							 * Map<String, Object> object1 = new HashMap<>();
							 * BasicDBList list1 = new BasicDBList();
							 * BasicDBObject pushObject1 = new BasicDBObject();
							 * pushObject1.putAll(errorReportMap);
							 * list1.add(pushObject1);
							 * object1.put(Gstr1ConstantsV31.REFIDS, list1);
							 */
							JSONObject jsonObject = new JSONObject(errorReportMap);
							aspMongoDaoV31.saveInMongo(jsonObject, "gstn_refid_map");
						}
						Map<String, Object> object = new HashMap<>();
						BasicDBList list = new BasicDBList();
						BasicDBObject pushObject = new BasicDBObject();
						pushObject.putAll(controlStatusMap);
						list.add(pushObject);
						object.put(Gstr1ConstantsV31.REFIDS, list);
						updateSaveGstnStatusData(object, ackNumber);

						log.info(
								"****************AFTER SUCCESSFULL UPDATE OF BATCH IN CONTROL DATA for Batch No :::: {}",
								batchNo);
						batchNo++;
					} // number loop ends
				} //
			} // section while loop ends
			log.info("****************MARKING THE SAVE GSTN AS COMPLETE IN CONTROL DATA ");
			Map<String, Object> controlStatusMap = new HashMap<>();
			SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
			Date date = new Date();
			controlStatusMap.put(Gstr1ConstantsV31.JSON_ASPETIME, sdf.format(date));
			controlStatusMap.put(Gstr1ConstantsV31.JSON_ASPSTATUS, Gstr1ConstantsV31.ASP_STATUS_COMPLETE);
			updateSaveGstnStatusData(controlStatusMap, ackNumber);
			log.info("****************SUCCESSFULL IN MARKING THE SAVE GSTN AS COMPLETE IN CONTROL DATA ");
		} else {
			log.info("****************NO RECORD FOUND IN FOR GIVEN CRITERIA ");
			CommonUtilV31.throwException(ErrorCodesV31.ASP522, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP,
					ErrorCodesV31.ASP522, null, HttpStatus.OK, messageSourceV31, null);
		}
		log.debug("processSuppliesData method : END");
		return ackNumber;
	}

	private Map<String, Object> handleExceptionFlow(Exception ex, String referenceId,
			Map<String, Object> controlStatusMap, Map<String, Object> finalMap, boolean isAggregate, int batchSize,
			String key) {
		log.debug("handleExceptionFlow method : START");
		Map<String, Object> errResponse = new HashMap<>();
		Map<String, Object> errMap = new HashMap<>();
		if (ex instanceof AspExceptionV31 || ex instanceof AspException || ex instanceof GspExceptionV31
				|| ex instanceof GspException) {
			Map<String, Object> exObj = ((AspExceptionV31) ex).getExcObj();
			errMap.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_MSG, MapUtils.getString(exObj, Gstr1ConstantsV31.ERROR_DESC));
			errMap.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_CD, MapUtils.getString(exObj, Gstr1ConstantsV31.ERROR_CODE));
		} else {
			errMap.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_MSG,
					"Due to processing error while Saving to GSTN, record might not have been saved "
							+ "in GSTN. If record is not found in GSTN then please repeat the save again.");
			errMap.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_CD, ErrorCodesV31.RETUKNERROR);
		}
		log.debug("handleExceptionFlow method : EXCEPTION PROCESSING");

		if (StringUtils.isBlank(referenceId)) {
			errResponse.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD, Gstr1ConstantsV31.GSTN_STATUS_CD_E);
		} else {
			errResponse.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD, Gstr1ConstantsV31.GSTN_STATUS_CD_IP);
		}
		errResponse.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT, errMap);
		finalMap = (MapUtils.isNotEmpty(finalMap)) ? finalMap : new HashMap<>();
		controlStatusMap = processRetStatusErrorCode(Gstr1ConstantsV31.GSTN_STATUS_CD_E, errResponse, finalMap,
				isAggregate, batchSize, 0);
		controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_RCRDCOUNT, batchSize);
		controlStatusMap.put(Gstr1ConstantsV31.INPUT_SECTION, key);
		controlStatusMap.put(Gstr1ConstantsV31.INPUT_REFERENCE_ID, referenceId);
		log.debug("handleExceptionFlow method : END");
		return controlStatusMap;
	}

	private Map<String, Object> processRetStatusErrorCode(String status, Map<String, Object> response,
			Map<String, Object> finalMap, boolean isAggregate, int batchSize, int peCount) {
		log.debug("processRetStatusErrorCode method : START");
		Map<String, Object> controlStatusMap = new HashMap<>();
		if (Gstr1ConstantsV31.GSTN_STATUS_CD_E.equals(status)) {
			log.info("processRetStatusErrorCode method : RECEIVED ERROR STATUS FOR BATCH");
			Map<String, Object> errMap = (Map<String, Object>) response
					.remove(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
			finalMap.putAll(errMap);
			Map<String, Object> map = new HashMap<>();
			List<Map<String, Object>> errPayloadList = new ArrayList<>();
			errPayloadList.add(finalMap);
			map.put(Gstr1ConstantsV31.ERRORVAL, errPayloadList);
			response.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT, map);
			controlStatusMap.putAll(response);
			controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_ERRCNT, batchSize);
			controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_SUCCCNT, 0);
		} else if (Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equals(status)
				|| Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equals(status)) {
			log.info("processRetStatusErrorCode method : RECEIVED IP and REC STATUS FOR BATCH");
			Map<String, Object> errMap = new HashMap<>();
			errMap.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_MSG,
					"Uploaded records are still under processing by GSTN.Please check back after few minutes. "
							+ "If this status persist after few hours please redo the process once's more");
			errMap.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_CD, ErrorCodesV31.RETIPERROR);
			finalMap.putAll(errMap);
			Map<String, Object> map = new HashMap<>();
			List<Map<String, Object>> errPayloadList = new ArrayList<>();
			errPayloadList.add(finalMap);
			map.put(Gstr1ConstantsV31.ERRORVAL, errPayloadList);
			response.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT, map);
			controlStatusMap.putAll(response);
			controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_ERRCNT, 0);
			controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_SUCCCNT, 0);
		} else {
			if (Gstr1ConstantsV31.GSTN_STATUS_CD_P.equals(status)) {
				log.info("processRetStatusErrorCode method : RECEIVED PROCESSED STATUS FOR BATCH");
				controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_ERRCNT, 0);
				controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_SUCCCNT, batchSize);
			} else if (Gstr1ConstantsV31.GSTN_STATUS_CD_PE.equals(status)) {
				log.info("processRetStatusErrorCode method : RECEIVED PARTIAL ERROR STATUS FOR BATCH");
				if (isAggregate) {
					controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_ERRCNT, batchSize);
					controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_SUCCCNT, 0);
				} else {
					int sucCount = batchSize - peCount;
					controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_ERRCNT, peCount);
					controlStatusMap.put(Gstr1ConstantsV31.CONTROL_REC_SUCCCNT, sucCount);
				}
			}
			controlStatusMap.putAll(response);
		}
		log.debug("processRetStatusErrorCode method : END");
		return controlStatusMap;
	}

	private float getBatchSize(Map<String, String> allRequestParams, float pageSize, int totalRecords) {
		float totalNumberOfPages = 0;

		float n = totalRecords % pageSize;
		float m = totalRecords / pageSize;
		if (m > 1) {
			totalNumberOfPages = totalRecords / pageSize;
			if (n > 0) {
				totalNumberOfPages = totalNumberOfPages + 1;

			}
		} else if ((m <= 1) && (m > 0)) {
			totalNumberOfPages = 1;
		}
		// System.out.println("**************number of pages**************" +
		// totalNumberOfPages);
		log.info("number of pages" + totalNumberOfPages);
		return totalNumberOfPages;
	}

	private void updateSaveGstnStatusData(Map<String, Object> refIdsRespone, String ackNumber) {
		String coll = gstnResource.getMessage("gstnRefidMap.col", null, LocaleContextHolder.getLocale());

		Map<String, Object> whereMap = new HashMap<>();
		whereMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, ackNumber);
		aspMongoDaoV31.updateMongoWithPush(refIdsRespone, coll, whereMap);
	}

	private void createSaveGstnStatusData(List<Object> refIdsRespone, String ackNumber,
			Map<String, String> allRequestParams) {
		Map<String, Object> object = new HashMap<>();
		Map<String, Object> outputMap = new HashMap<>();
		object.put(Gstr1ConstantsV31.CONTROL_JSON_ID, ackNumber);
		object.put(Gstr1ConstantsV31.ACKNUMBER, ackNumber);
		object.put(Gstr1ConstantsV31.REFIDS, refIdsRespone);
		object.put("action", Gstr1ConstantsV31.TYPE_SAVE);
		object.put("type", Gstr1ConstantsV31.API_NAME);
		object.put(Gstr1ConstantsV31.INPUT_FP, allRequestParams.get(Gstr1ConstantsV31.INPUT_FP));
		object.put(Gstr1ConstantsV31.INPUT_GSTN, allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN));
		String coll = gstnResource.getMessage("gstnRefidMap.col", null, LocaleContextHolder.getLocale());
		aspMongoDaoV31.saveInMongo(new JSONObject(object), coll);
		outputMap.put(Gstr1ConstantsV31.INPUT_REFERENCE_ID, ackNumber);
	}

	private String getRefIdFromResponse(String collection, String decryptedData, Map<String, String> reqHeaderMap,
			List<String> idList) {

		// update the JioGst database
		Map<String, String> responsePayload = null;
		String refId = null;
		try {
			responsePayload = mapper.readValue(decryptedData, new TypeReference<Map<String, String>>() {
			});
		} catch (IOException e) {
			CommonUtilV31.throwException(ErrorCodesV31.ASP103, Gstr1ConstantsV31.ASP_GSTR1_SAVE_TO_GSTN,
					ErrorCodesV31.ASP103, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, e);
		}
		if (responsePayload.containsKey(Gstr1ConstantsV31.INPUT_REFERENCE_ID)) {
			refId = responsePayload.get(Gstr1ConstantsV31.INPUT_REFERENCE_ID);
		}
		return refId;
	}

	private void updateStatusInDbInBatches(String collection, String decryptedData, Map<String, String> reqHeaderMap,
			List<String> idList, boolean isAggregate) {

		// update the JioGst database
		Map<String, String> responsePayload = null;

		try {
			responsePayload = mapper.readValue(decryptedData, new TypeReference<Map<String, String>>() {
			});
		} catch (IOException e) {
			CommonUtilV31.throwException(ErrorCodesV31.ASP103, Gstr1ConstantsV31.ASP_GSTR1_SAVE_TO_GSTN,
					ErrorCodesV31.ASP103, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, e);
		}
		Map<String, Object> object = new HashMap<String, Object>();
		if (responsePayload.containsKey(Gstr1ConstantsV31.INPUT_REFERENCE_ID)) {
			object.put(Gstr1ConstantsV31.CONTROL_REFERENCEID,
					responsePayload.get(Gstr1ConstantsV31.INPUT_REFERENCE_ID));
			object.put(Gstr1ConstantsV31.CONTROL_GSTN_STATUS, Gstr1ConstantsV31.STATUS_UPLOAD);
			SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
			Date date = new Date();
			object.put(Gstr1ConstantsV31.CONTROL_UPLOADTIME, sdf.format(date));
			object.put(Gstr1ConstantsV31.CONTROL_UPLOADBY, reqHeaderMap.get(Gstr1ConstantsV31.HEADER_USER_NAME));
			// new Code/ROhit
			Map<String, Object> whereMap = new HashMap<>();
			whereMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, idList);
			List<String> statusList = new ArrayList<>();
			if (!isAggregate) {
				statusList.add(Gstr1ConstantsV31.INVOICE_STATE_NEW);
				statusList.add(Gstr1ConstantsV31.STATUS_UPLOAD);
				whereMap.put(Gstr1ConstantsV31.CONTROL_GSTN_STATUS, statusList);
			}
			aspMongoDaoV31.updateStatusInMongoInBatches(object, collection, whereMap);
		}

	}

	private Map convertInGstnFormatInBatches(Map<String, String> allRequestParams, List<Map<String, Object>> listMap,
			Map<String, String> requestBody, List<String> idList) {

		Map<String, Object> map = new HashMap<>();
		if (listMap != null && listMap.size() > 0) {
			String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
			String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);
			map.put(Gstr1ConstantsV31.INPUT_GSTN, allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN));
			map.put(Gstr1ConstantsV31.INPUT_FP, allRequestParams.get(Gstr1ConstantsV31.INPUT_FP));
			map.put(Gstr1ConstantsV31.INPUT_GT, new Double(requestBody.get(Gstr1ConstantsV31.INPUT_GT)));
			map.put(Gstr1ConstantsV31.INPUT_CUR_GT, new Double(requestBody.get(Gstr1ConstantsV31.INPUT_CUR_GT)));

			for (Map m : listMap) {
				Map<String, Object> result = (Map<String, Object>) m.get(Gstr1ConstantsV31.RESULT);
				if (result != null) {

					List<Map<String, Object>> sectionB2b = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_B2B);
					if (sectionB2b != null && sectionB2b.size() > 0) {
						map.put(Gstr1ConstantsV31.TYPE_B2B, sectionB2b);
						fetchInvIds(sectionB2b, idList);
					}
					List<Map<String, Object>> sectionB2ba = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_B2BA);
					if (sectionB2ba != null && sectionB2ba.size() > 0) {
						map.put(Gstr1ConstantsV31.TYPE_B2BA, sectionB2ba);
						fetchInvIds(sectionB2ba, idList);
					}
					List<Map<String, Object>> sectionB2cl = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_B2CL);
					if (sectionB2cl != null && sectionB2cl.size() > 0) {
						map.put(Gstr1ConstantsV31.TYPE_B2CL, sectionB2cl);
						fetchInvIds(sectionB2cl, idList);
					}
					List<Map<String, Object>> sectionB2cla = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_B2CLA);
					if (sectionB2cla != null && sectionB2cla.size() > 0) {
						map.put(Gstr1ConstantsV31.TYPE_B2CLA, sectionB2cla);
						fetchInvIds(sectionB2cla, idList);
					}
					List<Map<String, Object>> sectionCdnr = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_CDNR);
					if (sectionCdnr != null && sectionCdnr.size() > 0) {
						map.put(Gstr1ConstantsV31.TYPE_CDNR, sectionCdnr);
						fetchInvIds(sectionCdnr, idList);
					}
					List<Map<String, Object>> sectionCdnra = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_CDNRA);
					if (sectionCdnra != null && sectionCdnra.size() > 0) {
						map.put(Gstr1ConstantsV31.TYPE_CDNRA, sectionCdnra);
						fetchInvIds(sectionCdnra, idList);
					}
					List<Map<String, Object>> sectionB2cs = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_B2CS);
					if (sectionB2cs != null && sectionB2cs.size() > 0) {
						List<Map<String, Object>> aggregatedSectionB2cs = aggregateDataForGstn(sectionB2cs, gstin, fp,
								Gstr1ConstantsV31.TYPE_B2CS);
						map.put(Gstr1ConstantsV31.TYPE_B2CS, aggregatedSectionB2cs);
						fetchInvIds(sectionB2cs, idList);
					}
					List<Map<String, Object>> sectionB2csa = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_B2CSA);
					if (sectionB2csa != null && sectionB2csa.size() > 0) {
						List<Map<String, Object>> aggregatedSectionB2csa = aggregateDataForGstn(sectionB2csa, gstin, fp,
								Gstr1ConstantsV31.TYPE_B2CSA);
						map.put(Gstr1ConstantsV31.TYPE_B2CSA, aggregatedSectionB2csa);
						fetchInvIds(sectionB2csa, idList);
					}
					List<Map<String, Object>> sectionExp = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_EXP);
					if (sectionExp != null && sectionExp.size() > 0) {
						List<Map<String, Object>> sectionData = filterNullData(sectionExp);
						map.put(Gstr1ConstantsV31.TYPE_EXP, sectionData);
						fetchInvIds(sectionExp, idList);
					}
					List<Map<String, Object>> sectionExpa = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_EXPA);
					if (sectionExpa != null && sectionExpa.size() > 0) {
						List<Map<String, Object>> sectionData = filterNullData(sectionExpa);
						map.put(Gstr1ConstantsV31.TYPE_EXPA, sectionData);
						fetchInvIds(sectionExpa, idList);
					}
					Map<String, Object> sectionHsn = (Map<String, Object>) result.get(Gstr1ConstantsV31.TYPE_HSN);
					if (sectionHsn != null && sectionHsn.size() > 0) {
						List<Map<String, Object>> dataHsn = (List<Map<String, Object>>) sectionHsn
								.get(Gstr1ConstantsV31.INPUT_DATA);
						if (dataHsn != null && dataHsn.size() > 0) {
							List<Map<String, Object>> sectionData = filterNullData(dataHsn);
							List<Map<String, Object>> aggregatedSectionHsn = aggregateDataForGstn(sectionData, gstin,
									fp, Gstr1ConstantsV31.TYPE_HSN);
							List<Map<String, Object>> newListHsn = insertNumForHsnAndDocIssued(aggregatedSectionHsn);
							Map<String, Object> newMap = new HashMap<>();
							newMap.put(Gstr1ConstantsV31.INPUT_DATA, newListHsn);
							map.put(Gstr1ConstantsV31.TYPE_HSN, newMap);
							fetchInvIds(dataHsn, idList);
						}
					}
					Map<String, Object> sectionNil = (Map<String, Object>) result.get(Gstr1ConstantsV31.TYPE_NIL);
					if (sectionNil != null) {
						List<Map<String, Object>> nilList = (List<Map<String, Object>>) sectionNil
								.get(Gstr1ConstantsV31.INPUT_INV);
						List<Map<String, Object>> aggregatedSectionNil = aggregateDataForGstn(nilList, gstin, fp,
								Gstr1ConstantsV31.TYPE_NIL);
						Map<String, Object> nilMap = new HashMap<>();
						nilMap.put(Gstr1ConstantsV31.INPUT_INV, aggregatedSectionNil);
						map.put(Gstr1ConstantsV31.TYPE_NIL, nilMap);
						fetchInvIds(nilList, idList);
					}
					List<Map<String, Object>> sectionTxpd = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_TXPD);
					if (sectionTxpd != null && sectionTxpd.size() > 0) {
						List<Map<String, Object>> aggregatedSectionTXPD = aggregateDataForGstnAT(sectionTxpd, gstin, fp,
								Gstr1ConstantsV31.TYPE_TXPD);
						map.put(Gstr1ConstantsV31.TYPE_TXPD, aggregatedSectionTXPD);
						fetchInvIds(sectionTxpd, idList);
					}
					List<Map<String, Object>> sectionTxpda = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_TXPDA);
					if (sectionTxpda != null && sectionTxpda.size() > 0) {
						List<Map<String, Object>> aggregatedSectionTXPDa = aggregateDataForGstnAT(sectionTxpda, gstin,
								fp, Gstr1ConstantsV31.TYPE_TXPDA);
						map.put(Gstr1ConstantsV31.TYPE_TXPDA, aggregatedSectionTXPDa);
						fetchInvIds(sectionTxpda, idList);
					}
					List<Map<String, Object>> sectionAt = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_AT);
					if (sectionAt != null && sectionAt.size() > 0) {
						List<Map<String, Object>> aggregatedSectionAT = aggregateDataForGstnAT(sectionAt, gstin, fp,
								Gstr1ConstantsV31.TYPE_AT);
						map.put(Gstr1ConstantsV31.TYPE_AT, aggregatedSectionAT);
						fetchInvIds(sectionAt, idList);
					}
					List<Map<String, Object>> sectionAta = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_ATA);
					if (sectionAta != null && sectionAta.size() > 0) {
						List<Map<String, Object>> aggregatedSectionATa = aggregateDataForGstnAT(sectionAta, gstin, fp,
								Gstr1ConstantsV31.TYPE_ATA);
						map.put(Gstr1ConstantsV31.TYPE_ATA, aggregatedSectionATa);
						fetchInvIds(sectionAta, idList);
					}
					Map<String, Object> sectionDocs = (Map<String, Object>) result.get(Gstr1ConstantsV31.TYPE_DOCS);
					if (sectionDocs != null) {
						List<Map<String, Object>> docDetails = (List<Map<String, Object>>) sectionDocs
								.get(Gstr1ConstantsV31.DOC_DET);
						if (docDetails != null && docDetails.size() > 0) {
							for (Map docDetailMap : docDetails) {
								if (docDetailMap != null) {
									List<Map<String, Object>> lineItems = (List<Map<String, Object>>) docDetailMap
											.get(Gstr1ConstantsV31.DOCS);
									if (lineItems != null && lineItems.size() > 0) {
										List<Map<String, Object>> newListLineItems = insertNumForHsnAndDocIssued(
												lineItems);
										docDetailMap.put(Gstr1ConstantsV31.DOCS, newListLineItems);

									}
								}
							}
							fetchInvIds(docDetails, idList);
						}
						map.put(Gstr1ConstantsV31.TYPE_DOCS, sectionDocs);

					}
					List<Map<String, Object>> sectionCdnur = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_CDNUR);
					if (sectionCdnur != null && sectionCdnur.size() > 0) {
						map.put(Gstr1ConstantsV31.TYPE_CDNUR, sectionCdnur);
						fetchInvIds(sectionCdnur, idList);
					}
					List<Map<String, Object>> sectionCdnura = (List<Map<String, Object>>) result
							.get(Gstr1ConstantsV31.TYPE_CDNURA);
					if (sectionCdnura != null && sectionCdnura.size() > 0) {
						map.put(Gstr1ConstantsV31.TYPE_CDNURA, sectionCdnura);
						fetchInvIds(sectionCdnura, idList);
					}

				}

			}
		} else {
			CommonUtilV31.throwException(ErrorCodesV31.ASP105, Gstr1ConstantsV31.ASP_GSTR1_SAVE_TO_GSTN,
					ErrorCodesV31.ASP105, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, null);
		}

		return map;
	}

	// filter null and empty values for exp and hsn as a temporary fix
	private List<Map<String, Object>> filterNullData(List<Map<String, Object>> sectionData) {
		for (Map<String, Object> data : sectionData) {
			if (data.containsKey(Gstr1ConstantsV31.INPUT_INV)) {
				List<Map<String, Object>> invList = (List<Map<String, Object>>) data.get(Gstr1ConstantsV31.INPUT_INV);
				if (invList != null && invList.size() > 0) {
					for (Map<String, Object> record : invList) {
						if (record.containsKey("sbdt") || record.containsKey("sbpcode")
								|| record.containsKey("sbnum")) {
							if ((record.get("sbpcode") == null)
									|| (StringUtils.isBlank(String.valueOf(record.get("sbpcode"))))) {
								record.remove("sbpcode");
							}
							if ((record.get("sbdt") == null)
									|| (StringUtils.isBlank(String.valueOf(record.get("sbdt"))))) {
								record.remove("sbdt");
							}
							if ((record.get("sbnum") == null)
									|| (StringUtils.isBlank(String.valueOf(record.get("sbnum"))))) {
								record.remove("sbnum");
							}
						}
					}

				}
			} else {
				// filter for hsn
				if (data.containsKey(Gstr1ConstantsV31.HSN_SC) || data.containsKey(Gstr1ConstantsV31.HSN_DESC)) {
					if ((data.get(Gstr1ConstantsV31.HSN_SC) == null)
							|| (StringUtils.isBlank(String.valueOf(data.get(Gstr1ConstantsV31.HSN_SC))))) {
						data.remove(Gstr1ConstantsV31.HSN_SC);
					}
					if ((data.get(Gstr1ConstantsV31.HSN_DESC) == null)
							|| (StringUtils.isBlank(String.valueOf(data.get(Gstr1ConstantsV31.HSN_DESC))))) {
						data.remove(Gstr1ConstantsV31.HSN_DESC);
					}
				}
			}

		}
		return sectionData;

	}

	@SuppressWarnings("unchecked")
	private void fetchInvIds(List<Map<String, Object>> sectionData, List<String> listMap) {
		for (Map<String, Object> data : sectionData) {
			List<Map<String, Object>> invList = new ArrayList<Map<String, Object>>();
			if (data.containsKey(Gstr1ConstantsV31.INPUT_INV)) {
				invList = (List<Map<String, Object>>) data.get(Gstr1ConstantsV31.INPUT_INV);
			} else if (data.containsKey("nt")) {
				invList = (List<Map<String, Object>>) data.get("nt");
			} else if (data.containsKey(Gstr1ConstantsV31.INVID)) {
				listMap.add(data.get(Gstr1ConstantsV31.INVID).toString());
				data.remove(Gstr1ConstantsV31.INVID);
			}
			if (invList != null && invList.size() > 0) {
				for (Map<String, Object> record : invList) {
					if (record.get(Gstr1ConstantsV31.INVID) != null) {
						listMap.add(record.get(Gstr1ConstantsV31.INVID).toString());
						record.remove(Gstr1ConstantsV31.INVID);
					}
				}

			}

		}

	}

	private List<Map<String, Object>> insertNumForHsnAndDocIssued(List<Map<String, Object>> dataMapList) {
		int counter = 0;
		for (Map<String, Object> dataMap : dataMapList) {
			if (dataMap != null && dataMap.size() > 0) {
				dataMap.put(Gstr1ConstantsV31.JSON_NUM, ++counter);
			}
		}
		return dataMapList;
	}

	private List<Map<String, Object>> aggregateDataForGstn(List<Map<String, Object>> sectionData, String gstin,
			String fp, String section) {
		List<Map<String, Object>> newList = new ArrayList<>();
		Map<String, Object> newMap = new HashMap<>();

		for (Map<String, Object> data : sectionData) {
			if (data != null) {

				String key = gstin + ":" + fp + ":" + section + ":";

				if (Gstr1ConstantsV31.TYPE_NIL.equalsIgnoreCase(section)) {
					key = key + data.get(Gstr1ConstantsV31.INV_NILSUP_TYP);
				} else if (Gstr1ConstantsV31.TYPE_HSN.equalsIgnoreCase(section)) {
					if (data.containsKey(Gstr1ConstantsV31.HSN_SC)) {
						key = key + data.get(Gstr1ConstantsV31.HSN_SC) + ":";
					} else if (data.containsKey(Gstr1ConstantsV31.HSN_DESC)) {
						key = key + data.get(Gstr1ConstantsV31.HSN_DESC) + ":";
					}
					key = key + data.get(Gstr1ConstantsV31.HSN_UQC);
				} else {
					key = key + data.get(Gstr1ConstantsV31.INPUT_POS) + ":" + data.get(Gstr1ConstantsV31.JSON_RT);
				}

				if (newMap.containsKey(key)) {
					Map<String, Object> record = (Map<String, Object>) newMap.get(key);

					Iterator<String> itr = record.keySet().iterator();

					while (itr.hasNext()) {
						String recKey = itr.next();
						if (record.get(recKey) instanceof Number && !filterList.contains(recKey)) {
							Number num = (Number) data.get(recKey);
							Number num1 = (Number) record.get(recKey);
							BigDecimal c = new BigDecimal(num.toString()).add(new BigDecimal(num1.toString()));
							c = c.setScale(2, BigDecimal.ROUND_HALF_UP);
							record.put(recKey, c.doubleValue());
						}
					}
					newMap.put(key, record);
				} else {
					newMap.put(key, data);
				}

			}
		}
		Iterator<String> itr = newMap.keySet().iterator();

		while (itr.hasNext()) {
			String key = itr.next();
			newList.add((Map<String, Object>) newMap.get(key));
		}

		return newList;
	}

	private List<Map<String, Object>> aggregateDataForGstnAT(List<Map<String, Object>> sectionData, String gstin,
			String fp, String section) {
		List<Map<String, Object>> newList = new ArrayList<>();
		Map<String, Object> newMap = new HashMap<>();
		Map<String, Object> parentMap = new HashMap<>();
		for (Map<String, Object> data : sectionData) {
			if (data != null) {
				String pKey = gstin + ":" + fp + ":" + section + ":" + data.get(Gstr1ConstantsV31.INPUT_POS);

				List<Map<String, Object>> newListItems = (List<Map<String, Object>>) data
						.get(Gstr1ConstantsV31.INPUT_ITMS);
				for (Map<String, Object> items : newListItems) {

					String key = gstin + ":" + fp + ":" + section + ":" + data.get(Gstr1ConstantsV31.INPUT_POS) + "#:#"
							+ items.get(Gstr1ConstantsV31.JSON_RT);

					if (newMap.containsKey(key)) {
						Map<String, Object> record = (Map<String, Object>) newMap.get(key);

						Iterator<String> itr = record.keySet().iterator();

						while (itr.hasNext()) {
							String recKey = itr.next();

							if (record.get(recKey) instanceof Number && !filterList.contains(recKey)) {
								Number num = (Number) items.get(recKey);
								Number num1 = (Number) record.get(recKey);
								BigDecimal c = new BigDecimal(num.toString()).add(new BigDecimal(num1.toString()));
								c = c.setScale(2, BigDecimal.ROUND_HALF_UP);
								record.put(recKey, c.doubleValue());
							}
						}
						newMap.put(key, record);
					} else {
						newMap.put(key, items);
					}
				}
				if (!parentMap.containsKey(pKey)) {
					data.remove("itms");
					parentMap.put(pKey, data);
				}
			}
		}
		Iterator<String> itr = newMap.keySet().iterator();

		while (itr.hasNext()) {
			String key = itr.next();
			String pKey = key.substring(0, key.indexOf("#:#"));
			List<Map<String, Object>> list = null;
			Map<String, Object> pMap = (Map<String, Object>) parentMap.get(pKey);
			if (pMap.containsKey("itms")) {
				list = (List<Map<String, Object>>) pMap.get("itms");
				list.add((Map<String, Object>) newMap.get(key));
				pMap.put("itms", list);
			} else {
				list = new ArrayList<>();
				list.add((Map<String, Object>) newMap.get(key));
				pMap.put("itms", list);
			}
		}

		Iterator<String> itr1 = parentMap.keySet().iterator();

		while (itr1.hasNext()) {
			String key = itr1.next();
			newList.add((Map<String, Object>) parentMap.get(key));
		}
		return newList;
	}

	private Map<String, Object> encryptData(Map<String, String> headerData, Map<String, Object> data) {

		byte[] SessionKeyInBytes = null;
		Map<String, Object> responseMap = new HashMap<>();

		try {

			log.info("EncryptRequest API call - Encrypting data received from client");
			// Decryption of Session Key
			String session_key = headerData.get(Gstr1ConstantsV31.INPUT_SEK);
			String app_key = headerData.get(Gstr1ConstantsV31.INPUT_APP_KEY);
			byte[] barray = AESEncryptionV31.decodeBase64StringTOByte(app_key);
			SessionKeyInBytes = AESEncryptionV31.decrypt(session_key, barray);
			String jsonData = gson.toJson(data);
			String baseData = AESEncryptionV31.encodeBase64String(jsonData.getBytes());
			log.debug("=======================jsonData===============");
			// Encryption of data from client
			String encryptedData = AESEncryptionV31.encryptEK(baseData.getBytes(), SessionKeyInBytes);
			// Generation of HMAC
			String hmac = HmacGeneratorV31.getHmac(baseData, SessionKeyInBytes);
			responseMap.put(Gstr1ConstantsV31.INPUT_DATA, encryptedData);
			responseMap.put(Gstr1ConstantsV31.INPUT_HMAC, hmac);
			responseMap.put(Gstr1ConstantsV31.INPUT_GSTN_ACTION, Gstr1ConstantsV31.TYPE_SAVE);

		} catch (Exception e) {
			responseMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.DATA_ENCRYPT_ERROR);
			responseMap.put(Gstr1ConstantsV31.ERROR_DESC, "Error in encryption	of data from client");
			responseMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_SAVE_TO_GSTN);
			throw new AspExceptionV31("Error in encryption of data from client", null, false, false, responseMap);
		}
		return responseMap;
	}

	/**
	 * Method takes the data from getGstr1InvoiceData method and creates java
	 * bean mapping for each of the items. This method will decrypt the data
	 * using appkey --> sek --> rek --> data
	 * 
	 * @param gspData
	 *            Map containing GSP data
	 * @param inputMap
	 *            user input data
	 * @return Map containing parsed GSP data
	 */
	private String parseGspData(Map<String, String> gspData, Map<String, String> inputMap) {
		log.debug("parseGspData method : START");
		String jsonData = null;
		if (gspData.containsKey(Gstr1ConstantsV31.INPUT_REK) && gspData.containsKey(Gstr1ConstantsV31.INPUT_DATA)) {
			String respRek = MapUtils.getString(gspData, Gstr1ConstantsV31.INPUT_REK);
			String respData = MapUtils.getString(gspData, Gstr1ConstantsV31.INPUT_DATA);
			byte[] authEK;
			try {
				authEK = AESEncryptionV31.decrypt((String) inputMap.get(Gstr1ConstantsV31.INPUT_SEK), AESEncryptionV31
						.decodeBase64StringTOByte((String) inputMap.get(Gstr1ConstantsV31.INPUT_APP_KEY)));

				log.debug("parseGspData method : Successfully Parsed SEK");

				byte[] apiEK = AESEncryptionV31.decrypt(respRek, authEK);
				log.debug("parseGspData method : Successfully Parsed REK");

				jsonData = new String(AESEncryptionV31
						.decodeBase64StringTOByte(new String(AESEncryptionV31.decrypt(respData, apiEK))));
				log.debug("parseGspData method : Successfully Parsed GSP JSon Response");
				log.debug("parseGspData method : after converting response from GSP for gstn data");

			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException e) {
				/*
				 * String errorMsg =
				 * gstnResource.getMessage("GSTR1_PARSE_ASP201", null,
				 * LocaleContextHolder.getLocale()); Map<String, Object> excObj
				 * = new HashMap<>(); excObj.put(Gstr1ConstantsV31.ERROR_CODE,
				 * ErrorCodesV31.GSTR1_PARSE_GSP_ERROR);
				 * excObj.put(Gstr1ConstantsV31.ERROR_DESC, errorMsg);
				 * excObj.put(Gstr1ConstantsV31.ERROR_GRP,
				 * Gstr1ConstantsV31.ASP_GSTR1_SUMMARY_GRP);
				 * excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE,
				 * HttpStatus.INTERNAL_SERVER_ERROR);
				 */
				log.error("parseGspData method : Error Occured while parsing the GSP response, error occurred. {}", e);
				// throw new GspExceptionV31(errorMsg, e, false, false, excObj);
			} catch (Exception e) {
				/*
				 * String errorMsg =
				 * gstnResource.getMessage("GSTR1_PARSE_ASP202", null,
				 * LocaleContextHolder.getLocale()); Map<String, Object> excObj
				 * = new HashMap<>(); excObj.put(Gstr1ConstantsV31.ERROR_CODE,
				 * ErrorCodesV31.GSTR1_PARSE_GSP_GEN_ERROR);
				 * excObj.put(Gstr1ConstantsV31.ERROR_DESC, errorMsg);
				 * excObj.put(Gstr1ConstantsV31.ERROR_GRP,
				 * Gstr1ConstantsV31.ASP_GSTR1_SUMMARY_GRP);
				 * excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE,
				 * HttpStatus.INTERNAL_SERVER_ERROR);
				 */
				log.error(
						"parseGspData method : Unknown Error Occured while parsing the GSP response, error occurred. {}",
						e);
				// throw new GspExceptionV31(errorMsg, e, false, false, excObj);
			}
		}
		log.debug("parseGspData method : END");
		return jsonData;
	}

}
