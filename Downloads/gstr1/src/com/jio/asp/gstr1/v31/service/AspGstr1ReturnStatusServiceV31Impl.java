package com.jio.asp.gstr1.v31.service;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.attribute.HashPrintJobAttributeSet;

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

import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;
import com.jio.asp.validation.ErrorCodes;
import com.mongodb.BasicDBObject;

@Service
public class AspGstr1ReturnStatusServiceV31Impl implements AspGstr1ReturnStatusServiceV31 {

	@Autowired
	AspLoggingServiceV31 aspl;

	@Autowired
	GSPServiceV31 gspService;

	@Autowired
	private MessageSource gstnResource;

	@Autowired
	private MessageSource messageSourceV31;

	@Autowired
	private AspMongoDaoV31 aspMongoDaoV31;

	@Autowired
	private Gstr1ProducerServiceV31 gstr1ProducerServiceV31;

	@Autowired
	private AspGstr1SubmitServiceV31 aspGstr1SubmitServiceV31;

	@Autowired
	private AspGstr1FileServiceV31 aspGstr1FileServiceV31;

	@Autowired
	private AspUpdateStatusService updateStatusService;
	Logger log = LoggerFactory.getLogger(AspGstr1ReturnStatusServiceV31Impl.class);

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> processGstr1Data(Map<String, String> headerData, Map<String, String> params) {
		Map<String, Object> response = new HashMap<>();
		log.info("GSTR1 Return Status API call -  start - STEP1");
		String retPeriod = params.get(Gstr1ConstantsV31.INPUT_FP);
		headerData.put(Gstr1ConstantsV31.INPUT_GSTN_RET_PER, retPeriod);

		// submitFileIpUpdate method
		submitFileIpUpdate(headerData, params);
		handleGstnUnProcessedStatus(headerData, params);
		List<Map<String, Object>> referIdMap = getAllReferIds(params);
		if (referIdMap != null && referIdMap.size() > 0) {
			for (Map<String, Object> referId : referIdMap) {
				List<Map<String, Object>> referenceIdList = (List<Map<String, Object>>) referId
						.get(Gstr1ConstantsV31.JSON_REF_IDS);
				if (referenceIdList != null && referenceIdList.size() > 0) {
					for (Map<String, Object> referenceId : referenceIdList) {
						if (response.containsKey(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD)) {
							String respst = MapUtils.getString(response, Gstr1ConstantsV31.GSTN_JSON_STATUS_CD);
							String st = MapUtils.getString(referenceId, Gstr1ConstantsV31.GSTN_JSON_STATUS_CD);
							if ((Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equals(st)
									|| Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equals(st))) {
								response.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD,
										Gstr1ConstantsV31.GSTN_STATUS_CD_IP);
							} else if (!(Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equals(respst)
									|| Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equals(respst))
									&& Gstr1ConstantsV31.GSTN_STATUS_CD_PE.equals(st)) {
								response.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD,
										Gstr1ConstantsV31.GSTN_STATUS_CD_PE);
							} else if (!(Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equals(respst)
									|| Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equals(respst))
									&& !Gstr1ConstantsV31.GSTN_STATUS_CD_PE.equals(respst)
									&& Gstr1ConstantsV31.GSTN_STATUS_CD_E.equals(st)) {
								response.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD, Gstr1ConstantsV31.GSTN_STATUS_CD_E);
							}

						} else {
							String st = MapUtils.getString(referenceId, Gstr1ConstantsV31.GSTN_JSON_STATUS_CD);
							response.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD, st);
						}

						String rf = MapUtils.getString(referenceId, Gstr1ConstantsV31.INPUT_REFERENCE_ID);						
						Map<String, Object> errMap = getErrorReport(rf);
						if (errMap != null) {
							if (response.containsKey(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT)) {
								Map<String, Object> sectionMap = errMap;
								/*MapUtils.getMap(referenceId,
										Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT, null);*/
								Map<String, Object> responseSecMap = MapUtils.getMap(response,
										Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT, null);
								Iterator<String> itr = sectionMap.keySet().iterator();
								while (itr.hasNext()) {
									String key = itr.next();
									if (responseSecMap.containsKey(key)) {
										List<Map<String, Object>> respSecList = (List<Map<String, Object>>) MapUtils
												.getObject(responseSecMap, key, new ArrayList<>());
										List<Map<String, Object>> secList = (List<Map<String, Object>>) MapUtils
												.getObject(sectionMap, key, new ArrayList<>());
										respSecList.addAll(respSecList.size(), secList);
									} else {
										responseSecMap.put(key, sectionMap.get(key));
									}
								}
							} else {
								response.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT, errMap);
							}
						}
					}
				}
			}
		} else {
			Map<String, String> dataMap = new HashMap<>();
			dataMap.put(Gstr1ConstantsV31.CONTROL_REC_STATUS, Gstr1ConstantsV31.ERROR);
			dataMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, "No Data Available for given criteria");
			response.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, params);
			response.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
		}

		/*
		 * if (referIdMap != null && referIdMap.size() > 0) { for (Map<String,
		 * Object> referId : referIdMap) { List<String> referenceIdList =
		 * (List<String>) referId.get(Gstr1ConstantsV31.INPUT_REFERENCE_ID); if
		 * (referenceIdList != null && referenceIdList.size() > 0) { for (String
		 * referenceId : referenceIdList) { gspresponse =
		 * gspService.getGstr1Status(headerData, params, referenceId); response
		 * = gspService.decryptResponse(headerData, gspresponse);
		 * UpdateDbForGstnResponse(response, referenceId, params); log.
		 * info("GSTR1 Return Status API call - Returning response to client - STEP6"
		 * ); } } } } else { // TODO }
		 */
		if(!response.containsValue("P") &&response.containsKey(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT) ){
			Map<String, Object> err=new HashMap<>();
			Map<String, Object> errMsgMap=new HashMap<>();
			Map<String, Object> errReport= (Map<String, Object>) response.get(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
			if(!errReport.containsKey(Gstr1ConstantsV31.ERROR)){
				err.putAll(errReport);
				errMsgMap.put(Gstr1ConstantsV31.RESPONSE_DEV_MSG,  err.get("error_cd")+":"+err.get("error_msg"));
				errMsgMap.put(Gstr1ConstantsV31.RESPONSE_USR_MSG, err.get("error_msg"));
				errMsgMap.put(Gstr1ConstantsV31.RESPONSE_USR_ACT, err.get("error_msg"));
			}else{
			List<Map<String, Object>> errorList= (List<Map<String, Object>>) errReport.get(Gstr1ConstantsV31.ERROR);
		
			for (Iterator iterator = errorList.iterator(); iterator.hasNext();) {
				Map<String, Object> map = (Map<String, Object>) iterator.next();
				 List<Object> temp= new ArrayList<>();
				 Map<String, Object> tempMap= new HashMap<>();
				 Map<String, Object> tempStr= new HashMap<>();
				 String tempKey = null;
				 for (Entry<String, Object> entry : map.entrySet()) {
				
			          if(entry.getValue() instanceof List<?>){
			        	//  tempMap.putAll( (Map<? extends String, ? extends Object>) entry.getValue());
			        	  tempMap.put(entry.getKey(), entry.getValue());
			        	  tempKey=entry.getKey();
			          }else {
			        	  tempStr.put(entry.getKey(),  entry.getValue());
					}
			          
			    }
				if(tempMap.get(tempKey) instanceof List<?> ){
					List<?>temSectMap=(List<?>) tempMap.get(tempKey);
					for(int i =0;i<temSectMap.size();i++){
						Map  tem=  (Map) temSectMap.get(i);
						tem.putAll(tempStr);
						
					}
					
				}
				
				err.putAll(map);
				
				errMsgMap.put(Gstr1ConstantsV31.RESPONSE_DEV_MSG,  err.get("error_cd")+":"+err.get("error_msg"));
				errMsgMap.put(Gstr1ConstantsV31.RESPONSE_USR_MSG, err.get("error_msg"));
				errMsgMap.put(Gstr1ConstantsV31.RESPONSE_USR_ACT, err.get("error_msg"));
			}
			}
			
			
		
			Object[] param = {err};
			CommonUtil.throwException(ErrorCodesV31.GSP010200, Gstr1ConstantsV31.GSTN_L2, param, HttpStatus.OK,
					null, AspConstants.FORM_CODE,errMsgMap);
		}
		
		return response;
	}

	@Override
	public int UpdateDbForGstnResponse(Map<String, Object> response, String referenceId, Map<String, String> params,
			String toSuccessStatus, String fromStatus) {
		String status_cd = response.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD).toString();
		int errCount = 0;
		if (Gstr1ConstantsV31.GSTN_STATUS_CD_P.equals(status_cd)) {
			// udating remaining rcord to status as success, assuming everything
			// went well in uploads.
			updateDbToGstnSaved(referenceId, toSuccessStatus, fromStatus);
			log.info("UpdateDbForGstnResponse >>>>>>>>>> PROCESSED STATUS, AFTER MAKING ALL RECORD SUCCESS FOR REF ID");
		} else if ((Gstr1ConstantsV31.GSTN_STATUS_CD_PE).equals(status_cd)) {

			List<Object> inums = filterInvoicesWithErrors(response, params, referenceId);
			if (CollectionUtils.isNotEmpty(inums)) {
				errCount = inums.size();
				updateDbToNew(inums, params);
			}
			log.info(
					"UpdateDbForGstnResponse >>>>>>>>>> PARTIAL ERROR RECEIVED, UPDATED THE IDENTIFIED FAILED RECORD STATUS");
			// udating remaining rcord to status as success, assuming everything
			// went well in uploads.
			updateDbToGstnSaved(referenceId, toSuccessStatus, fromStatus);
			log.info(
					"UpdateDbForGstnResponse >>>>>>>>>> PARTIAL ERROR RECEIVED, UPDATED THE ONLY SUCCESS RECORD STATUS");
		} else if ((Gstr1ConstantsV31.GSTN_STATUS_CD_E).equals(status_cd)) {
			SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
			Date date = new Date();
			Map<String, Object> object = new HashMap<String, Object>();
			object.put(Gstr1ConstantsV31.CONTROL_GSTN_STATUS, Gstr1ConstantsV31.INVOICE_STATE_GSTN_FAILED);
			object.put(Gstr1ConstantsV31.CONTROL_UPLOADTIME, sdf.format(date));
			String gstr1GstnCol = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
			Map<String, Object> whereMap = new HashMap<>();
			whereMap.put(Gstr1ConstantsV31.CONTROL_REFERENCEID, referenceId);
			aspMongoDaoV31.updateStatusInMongoInBatches(object, gstr1GstnCol, whereMap);
			log.info(
					"UpdateDbForGstnResponse >>>>>>>>>> COMPLETE ERROR RECEIVED, UPDATED THE ALL RECORD STATUS TO FAILED");
		} else {
			log.info(
					"UpdateDbForGstnResponse >>>>>>>>>> NONE OF THE 3 CONDITION MATCHED***NO UPDATE***, STATUS RECIEVED IS {} ",
					status_cd);
		}
		return errCount;
	}

	@SuppressWarnings("unchecked")
	private List<Object> filterInvoicesWithErrors(Map<String, Object> response, Map<String, String> params,
			String referenceId) {
		Map<String, Object> errorReport = (Map<String, Object>) response.get(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
		List<Object> inums = new ArrayList<>();
		String gstin = params.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = params.get(Gstr1ConstantsV31.INPUT_FP);
		String fy = gstr1ProducerServiceV31.getFY(fp);
		Map<String, Object> keyMap = new HashMap<>();
		if (errorReport != null) {
			for (Map.Entry<String, Object> pair : errorReport.entrySet()) {
				List<Map<String, Object>> ctinList = (List<Map<String, Object>>) pair.getValue();
				for (Map<String, Object> ctinData : ctinList) {
					if (ctinList != null && ctinList.size() > 0) {
						if ((Gstr1ConstantsV31.TYPE_B2B.equalsIgnoreCase(pair.getKey()))) {
							List<Map<String, Object>> invList = (List<Map<String, Object>>) ctinData
									.get(Gstr1ConstantsV31.INPUT_INV);
							if (invList != null && invList.size() > 0) {
								for (Map<String, Object> inv : invList) {
									String inum = MapUtils.getString(inv, Gstr1ConstantsV31.INPUT_INUM, "");
									String key = gstin + ":" + fy + ":" + inum;
									inums.add(key);
								}
							}
						} else if ((Gstr1ConstantsV31.TYPE_B2BA.equalsIgnoreCase(pair.getKey()))) {
							List<Map<String, Object>> invList = (List<Map<String, Object>>) ctinData
									.get(Gstr1ConstantsV31.INPUT_INV);
							if (invList != null && invList.size() > 0) {
								for (Map<String, Object> inv : invList) {
									String inum = MapUtils.getString(inv, Gstr1ConstantsV31.INPUT_INUM, "");
									String key = gstin + ":" + fy + ":" + inum + ":" + Gstr1ConstantsV31.TYPE_AMENDMENTS;
									inums.add(key);
								}
							}
						} else if ((Gstr1ConstantsV31.TYPE_B2CL.equalsIgnoreCase(pair.getKey()))) {
							String inum = MapUtils.getString(ctinData, Gstr1ConstantsV31.INPUT_INUM, null);
							if (inum != null) {
								String key = gstin + ":" + fy + ":" + inum;
								inums.add(key);
							} else {
								List<Map<String, Object>> invList = (List<Map<String, Object>>) ctinData
										.get(Gstr1ConstantsV31.INPUT_INV);
								if (invList != null && invList.size() > 0) {
									for (Map<String, Object> inv : invList) {
										inum = MapUtils.getString(inv, Gstr1ConstantsV31.INPUT_INUM, "");
										String key = gstin + ":" + fy + ":" + inum;
										inums.add(key);
									}
								}
							}

						} else if ((Gstr1ConstantsV31.TYPE_B2CLA.equalsIgnoreCase(pair.getKey()))) {
							String inum = MapUtils.getString(ctinData, Gstr1ConstantsV31.INPUT_INUM, null);
							if (inum != null) {
								String key = gstin + ":" + fy + ":" + inum + ":" + Gstr1ConstantsV31.TYPE_AMENDMENTS;
								inums.add(key);
							} else {
								List<Map<String, Object>> invList = (List<Map<String, Object>>) ctinData
										.get(Gstr1ConstantsV31.INPUT_INV);
								if (invList != null && invList.size() > 0) {
									for (Map<String, Object> inv : invList) {
										inum = MapUtils.getString(inv, Gstr1ConstantsV31.INPUT_INUM, "");
										String key = gstin + ":" + fy + ":" + inum + ":" + Gstr1ConstantsV31.TYPE_AMENDMENTS;
										inums.add(key);
									}
								}
							}

						} else if ((Gstr1ConstantsV31.TYPE_CDNR.equalsIgnoreCase(pair.getKey()))) {
							List<Map<String, Object>> invList = (List<Map<String, Object>>) ctinData.get("nt");
							if (invList != null && invList.size() > 0) {
								for (Map<String, Object> inv : invList) {
									String ntnum = MapUtils.getString(inv, "nt_num", "");
									String key = gstin + ":" + fy + ":" + Gstr1ConstantsV31.TYPE_MONGO_KEY_CDNR_CDNUR
											+ ":" + ntnum;
									inums.add(key);
								}
							}
						} else if ((Gstr1ConstantsV31.TYPE_CDNRA.equalsIgnoreCase(pair.getKey()))) {
							List<Map<String, Object>> invList = (List<Map<String, Object>>) ctinData.get("nt");
							if (invList != null && invList.size() > 0) {
								for (Map<String, Object> inv : invList) {
									String ntnum = MapUtils.getString(inv, "nt_num", "");
									String key = gstin + ":" + fy + ":" + Gstr1ConstantsV31.TYPE_MONGO_KEY_CDNR_CDNUR
											+ ":" + ntnum + ":" + Gstr1ConstantsV31.TYPE_AMENDMENTS;
									inums.add(key);
								}
							}
						} else if (Gstr1ConstantsV31.TYPE_CDNUR.equalsIgnoreCase(pair.getKey())) {
							String ntNum = (String) ctinData.get("nt_num");
							if (StringUtils.isNotBlank(ntNum)) {
								String key = gstin + ":" + fy + ":" + Gstr1ConstantsV31.TYPE_MONGO_KEY_CDNR_CDNUR + ":"
										+ ntNum;
								inums.add(key);
							}
						} else if (Gstr1ConstantsV31.TYPE_CDNURA.equalsIgnoreCase(pair.getKey())) {
							String ntNum = (String) ctinData.get("nt_num");
							if (StringUtils.isNotBlank(ntNum)) {
								String key = gstin + ":" + fy + ":" + Gstr1ConstantsV31.TYPE_MONGO_KEY_CDNR_CDNUR + ":"
										+ ntNum + ":" + Gstr1ConstantsV31.TYPE_AMENDMENTS;
								inums.add(key);
							}
						} else if ((Gstr1ConstantsV31.TYPE_EXP.equalsIgnoreCase(pair.getKey()))) {
							List<Map<String, Object>> invList = (List<Map<String, Object>>) ctinData
									.get(Gstr1ConstantsV31.INPUT_INV);
							if (invList != null && invList.size() > 0) {
								for (Map<String, Object> inv : invList) {
									String inum = MapUtils.getString(inv, Gstr1ConstantsV31.INPUT_INUM, "");
									String key = gstin + ":" + fy + ":" + inum;
									inums.add(key);
								}
							}
						} else if ((Gstr1ConstantsV31.TYPE_EXPA.equalsIgnoreCase(pair.getKey()))) {
							List<Map<String, Object>> invList = (List<Map<String, Object>>) ctinData
									.get(Gstr1ConstantsV31.INPUT_INV);
							if (invList != null && invList.size() > 0) {
								for (Map<String, Object> inv : invList) {
									String inum = MapUtils.getString(inv, Gstr1ConstantsV31.INPUT_INUM, "");
									String key = gstin + ":" + fy + ":" + inum + ":" + Gstr1ConstantsV31.TYPE_AMENDMENTS;
									inums.add(key);
								}
							}else{
								String inum = MapUtils.getString(ctinData, Gstr1ConstantsV31.INPUT_INUM, "");
								String key = gstin + ":" + fy + ":" + inum + ":" + Gstr1ConstantsV31.TYPE_AMENDMENTS;
								inums.add(key);
								
							}
						} else if ((Gstr1ConstantsV31.TYPE_B2CS.equalsIgnoreCase(pair.getKey()))) {
							prepareAggSectionUpdateKey(keyMap, gstin, fp, referenceId, Gstr1ConstantsV31.TYPE_B2CS);
						} else if ((Gstr1ConstantsV31.TYPE_B2CSA.equalsIgnoreCase(pair.getKey()))) {
							prepareAggSectionUpdateKey(keyMap, gstin, fp, referenceId, Gstr1ConstantsV31.TYPE_B2CSA);
						} else if ((Gstr1ConstantsV31.TYPE_AT.equalsIgnoreCase(pair.getKey()))) {
							prepareAggSectionUpdateKey(keyMap, gstin, fp, referenceId, Gstr1ConstantsV31.TYPE_AT);
						} else if ((Gstr1ConstantsV31.TYPE_ATA.equalsIgnoreCase(pair.getKey()))) {
							prepareAggSectionUpdateKey(keyMap, gstin, fp, referenceId, Gstr1ConstantsV31.TYPE_ATA);
						} else if ((Gstr1ConstantsV31.TYPE_TXPD.equalsIgnoreCase(pair.getKey()))) {
							prepareAggSectionUpdateKey(keyMap, gstin, fp, referenceId, Gstr1ConstantsV31.TYPE_TXPD);
						} else if ((Gstr1ConstantsV31.TYPE_TXPDA.equalsIgnoreCase(pair.getKey()))) {
							prepareAggSectionUpdateKey(keyMap, gstin, fp, referenceId, Gstr1ConstantsV31.TYPE_TXPDA);
						} else if ((Gstr1ConstantsV31.TYPE_DOCS.equalsIgnoreCase(pair.getKey()))) {
							prepareAggSectionUpdateKey(keyMap, gstin, fp, referenceId, Gstr1ConstantsV31.TYPE_DOCS);
						} else if ((Gstr1ConstantsV31.TYPE_NIL.equalsIgnoreCase(pair.getKey()))) {
							prepareAggSectionUpdateKey(keyMap, gstin, fp, referenceId, Gstr1ConstantsV31.TYPE_NIL);
						} else if ((Gstr1ConstantsV31.TYPE_HSN.equalsIgnoreCase(pair.getKey()))
								|| "hsnsum".equals(pair.getKey())) {
							prepareAggSectionUpdateKey(keyMap, gstin, fp, referenceId, Gstr1ConstantsV31.TYPE_HSN);
						}

					}
				}
			}
			if (!keyMap.isEmpty()) {
				inums.add(keyMap);
			}
		}

		return inums;
	}
	
	private Map<String,Object> getErrorReport(String referenceId){
		Map<String,Object> object = new HashMap<>();
		Map<String,Object> errmap = null;
		object.put(Gstr1ConstantsV31.CONTROL_JSON_ID, referenceId);
		
		List<Map<String,Object>> result = aspMongoDaoV31.getMongoData(object, "gstn_refid_map");
		if(result != null && result.size() > 0){
			Map<String,Object> map = result.get(0);
			if(map.containsKey(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT)){
				errmap = (Map<String,Object>)map.get(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
				
			}
		}
		
		return errmap;
	}

	private Map<String, Object> prepareAggSectionUpdateKey(Map<String, Object> keyMap, String gstin, String fp,
			String referenceId, String type) {

		keyMap.put("header.gstin", gstin);
		keyMap.put("header.fp", fp);
		if (keyMap.containsKey("control.type")) {
			List<String> typeList = (List) keyMap.get("control.type");
			typeList.add(type);
			keyMap.put("control.type", typeList);
		} else {
			List<String> typeList = new ArrayList<>();
			typeList.add(type);
			keyMap.put("control.type", typeList);
		}

		keyMap.put("control.referenceId", referenceId);
		return keyMap;
	}

	private void updateDbToNew(List<Object> idList, Map<String, String> params) {
		if (idList != null && idList.size() > 0) {
			// B2b, B2cl, exp
			Map<String, Object> object = new HashMap<String, Object>();
			object.put(Gstr1ConstantsV31.CONTROL_GSTN_STATUS, Gstr1ConstantsV31.INVOICE_STATE_GSTN_FAILED);
			// object.put(Gstr1ConstantsV31.CONTROL_STATUS,
			// Gstr1ConstantsV31.INVOICE_STATE_NEW);
			SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
			Date date = new Date();
			object.put(Gstr1ConstantsV31.CONTROL_UPLOADTIME, sdf.format(date));

			String gstr1GstnCol = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
			//
			// String gstin = params.get(Gstr1ConstantsV31.INPUT_GSTN);
			// String fp = params.get(Gstr1ConstantsV31.INPUT_FP);
			// List<String> idList = new ArrayList<String>();
			// for (String inum : inums) {
			// idList.add(gstin + ":" + gstr1ProducerServiceV31.getFY(fp) + ":"
			// + inum);
			// }

			// aspMongoDaoV31.updateStatusInMongoInBatches(object, gstr1GstnCol,
			// Gstr1ConstantsV31.CONTROL_JSON_ID, idList,
			// Gstr1ConstantsV31.STATUS_UPLOAD);
			// new Code/ROhit
			Map<String, Object> whereMap = new HashMap<>();
			if (idList.get(0) instanceof Map<?, ?>) {
				Map<String, Object> aggKeyMap = (Map<String, Object>) idList.get(0);
				whereMap.putAll(aggKeyMap);
			} else {
				whereMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, idList);
			}
			// whereMap.put(Gstr1ConstantsV31.CONTROL_STATUS,
			// Gstr1ConstantsV31.STATUS_UPLOAD);
			whereMap.put(Gstr1ConstantsV31.CONTROL_GSTN_STATUS, Gstr1ConstantsV31.STATUS_UPLOAD);
			aspMongoDaoV31.updateStatusInMongoInBatches(object, gstr1GstnCol, whereMap);
		}

	}

	private void updateDbToGstnSaved(String referenceId, String toStatus, String fromStatus) {
		Map<String, Object> object = new HashMap<String, Object>();
		object.put(Gstr1ConstantsV31.CONTROL_GSTN_STATUS, toStatus);
		SimpleDateFormat sdf = new SimpleDateFormat(Gstr1ConstantsV31.HEADER_DATE_FORMAT);
		Date date = new Date();
		object.put(Gstr1ConstantsV31.CONTROL_UPLOADTIME, sdf.format(date));
		// object.put(Gstr1ConstantsV31.CONTROL_UPLOADBY,
		// reqHeaderMap.get(Gstr1ConstantsV31.HEADER_USER_NAME));
		String gstr1GstnCol = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
		List<String> idList = new ArrayList<String>();
		idList.add(referenceId);
		// aspMongoDaoV31.updateStatusInMongoInBatches(object, gstr1GstnCol,
		// Gstr1ConstantsV31.CONTROL_REFERENCEID, idList,
		// Gstr1ConstantsV31.STATUS_UPLOAD);
		// new Code/ROhit
		Map<String, Object> whereMap = new HashMap<>();
		whereMap.put(Gstr1ConstantsV31.CONTROL_REFERENCEID, idList);
		whereMap.put(Gstr1ConstantsV31.CONTROL_GSTN_STATUS, fromStatus);
		aspMongoDaoV31.updateStatusInMongoInBatches(object, gstr1GstnCol, whereMap);
	}

	private List<Map<String, Object>> getAllReferIds(Map<String, String> params) {
		Map<String, Object> object = new HashMap<>();
		object.put(Gstr1ConstantsV31.CONTROL_JSON_ID, params.get(Gstr1ConstantsV31.INPUT_REFERENCE_ID));
		List<Map<String, Object>> referIdMap = aspMongoDaoV31.getMongoData(object, "gstn_refid_map");
		return referIdMap;

	}

	/**
	 * Method does header data validation like client details from the asp
	 * database.
	 * 
	 * @param headerData
	 *            this object contains request header data, which is needed to
	 *            do GSP call and user validation
	 */
	public void validateHeaderInput(Map<String, String> headerData) {
		log.debug("validateHeaderInput method : START");
		Map<String, Object> excObj = new HashMap<>();

		CommonUtilV31.validateEmptyString(MapUtils.getString(headerData, Gstr1ConstantsV31.INPUT_SEK),
				ErrorCodesV31.GSTR1_SUM_EMPTY, Gstr1ConstantsV31.ASP_GSTR1_RETURN_STATUS_GRP, "GSTR1_HDR_SEK_ASP105",
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		CommonUtilV31.validateEmptyString(MapUtils.getString(headerData, Gstr1ConstantsV31.INPUT_APP_KEY),
				ErrorCodesV31.GSTR1_SUM_TABLE_TYPES_EMTY, Gstr1ConstantsV31.ASP_GSTR1_RETURN_STATUS_GRP,
				"GSTR1_HDR_APP_KEY_ASP106", null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		log.debug("validateHeaderInput method : END");
	}

	/**
	 * Method does the api input validation. Data which is needed for request
	 * processing and quering from database and GSP
	 * 
	 * @param inputMap
	 *            this object contains user input parameter for searching of the
	 *            user data.
	 * @return Map, which contains error information or processed data
	 */
	@Override
	public void validateApiInput(Map<String, String> params) {

		log.debug("validateApiInput method : START");
		Map<String, Object> excObj = new HashMap<>();
		
		CommonUtil.validateEmptyString(MapUtils.getString(params,Gstr1ConstantsV31.INPUT_REFERENCE_ID,""),
				ErrorCodesV31.ASP010103, Gstr1ConstantsV31.GSTN_RET, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
		
		CommonUtil.validateEmptyString(MapUtils.getString(params,Gstr1ConstantsV31.HEADER_GSTIN,""),
				ErrorCodesV31.ASP011031, Gstr1ConstantsV31.GSTN_RET, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		CommonUtil.validateEmptyString(MapUtils.getString(params, Gstr1ConstantsV31.INPUT_FP,""),
				ErrorCodesV31.ASP011311, Gstr1ConstantsV31.GSTN_RET, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		log.debug("validateApiInput method : END");

	}

	@Override
	public Map<String, Object> validateApiInput(String gstin, String trans_id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void createSaveGstnStatusData(List<Object> refIdsRespone, String ackNumber, String type, String action,
			String gstin, String fp) {
		// List<Map<String, Object>> dataList = new ArrayList<>();
		Map<String, Object> object = new HashMap<>();
		object.put("_id", ackNumber);
		object.put("ackNumber", ackNumber);
		object.put("refids", refIdsRespone);
		object.put("type", type);
		object.put("action", action);
		object.put("fp", fp);
		object.put("gstin", gstin);

		aspMongoDaoV31.saveInMongo(new JSONObject(object), "gstn_refid_map");

	}

	// public void updateGstnStatusData(List<Object> refIdsRespone, String
	// ackNumber, String type,
	// String action,String gstin,String fp,int idx) {
	public void updateGstnStatusData(Map<String, Object> object, String collection, Map<String, Object> whereMap) {

		// aspMongoDaoV31.updateInMongo(object, "gstn_refid_map", ackNumber,
		// null);

		aspMongoDaoV31.updateMongoWithPush(object, collection, whereMap);

	}
	
	private void deleteErrorReport(String referenceId,String collection){
		Map<String,Object> object = new HashMap<>();
		object.put(Gstr1ConstantsV31.CONTROL_JSON_ID, referenceId);
		aspMongoDaoV31.deleteRecords(object, collection);
		
	}

	@Override
	public void submitFileIpUpdate(Map<String, String> headerData, Map<String, String> params) {

		List<Map<String, Object>> referIdMap = getAllReferIds(params);
		Map<String, Object> gspresponse = new HashMap<>();
		String action = "";
		String fp = "";
		String gstin = "";
		String st = "";
		String reference_id = "";
		String type = "";
		String ret_period = headerData.get(Gstr1ConstantsV31.INPUT_GSTN_RET_PER);
		Map<String, Object> response1 = new HashMap<>();
		List<Object> refIdList = new ArrayList<Object>();
		Map<String, Object> requestBody = new HashMap<>();
		String gstr1GstnCol = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
		Map<String, Object> requestData = new HashMap<>();
		Map<String, String> requestParam = new HashMap<>();

		Map<String, Object> controlMap = new HashMap<>();
		String ackNumber = "";

		if (referIdMap != null && referIdMap.size() > 0) {
			Map<String, Object> refMap = (Map<String, Object>) referIdMap.get(0);

			action = MapUtils.getString(refMap, Gstr1ConstantsV31.INPUT_GSTN_ACTION);
			fp = MapUtils.getString(refMap, Gstr1ConstantsV31.INPUT_FP);
			gstin = MapUtils.getString(refMap, Gstr1ConstantsV31.INPUT_GSTN);
			ackNumber = MapUtils.getString(refMap, Gstr1ConstantsV31.ACKNUMBER);
			type = MapUtils.getString(refMap, Gstr1ConstantsV31.INPUT_TYPE);

			requestData.put("fp", fp);
			requestData.put("gstin", gstin);
			requestData.put(Gstr1ConstantsV31.INPUT_GSTN_RET_PER, ret_period);

			requestParam.put("fp", fp);
			requestParam.put("gstin", gstin);

			List<Map<String, Object>> referenceIdList = (List<Map<String, Object>>) referIdMap.get(0)
					.get(Gstr1ConstantsV31.JSON_REF_IDS);
			if (referenceIdList != null && referenceIdList.size() > 0) {
				Map<String, Object> referenceIdMap = referenceIdList.get(0);

				st = MapUtils.getString(referenceIdMap, Gstr1ConstantsV31.GSTN_JSON_STATUS_CD);
				reference_id = MapUtils.getString(referenceIdMap, Gstr1ConstantsV31.INPUT_REFERENCE_ID);
				if ((Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equalsIgnoreCase(st)
						|| Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equalsIgnoreCase(st))
						&& Gstr1ConstantsV31.TYPE_SUBMIT.equals(action)) {

					Map<String, String> gspresponse1 = gspService.getGstr1Status(headerData, params, reference_id);
					response1 = gspService.decryptResponse(headerData, gspresponse1);
					String status = String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD));
					// update submit in db
					if (String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD))
							.equalsIgnoreCase(Gstr1ConstantsV31.GSTN_STATUS_CD_P)) {

						aspGstr1SubmitServiceV31.updateSubmitStatusInDb(gstr1GstnCol, requestParam, reference_id);

						// by RS
						updateStatusService.updateGstinMasterData(requestData, headerData, null);

						// add

						// take inside this if
						controlMap.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD,
								String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD)));
						refIdList.add(controlMap);
					}
					// for other then P
					else if (Gstr1ConstantsV31.GSTN_STATUS_CD_E.equals(status)
							|| Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equals(status)) {
						Map<String, Object> errMap = null;
						if (response1.containsKey(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT)
								&& !Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equals(status)) {
							errMap = (Map<String, Object>) response1.remove(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
							errMap.put("error_msg",
									"Issue occurred while Submitting data to GSTN, please Redo The Submit Again");
						}
						// don't required
						else {
							errMap = new HashMap<>();
							errMap.put("error_msg",
									"Issue occurred while Submitting data to GSTN, please Redo The Submit Status Again");
							errMap.put("error_cd", "RETRECERROR");
						}

						controlMap.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD,
								String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD)));
						controlMap.putAll(errMap);
						refIdList.add(controlMap);
					}

					// need to update
					aspGstr1SubmitServiceV31.createSaveGstnStatusData(refIdList, ackNumber, type, action, gstin, fp);

				}

				// for file
				else if ((Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equalsIgnoreCase(st)
						|| Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equalsIgnoreCase(st))
						&& Gstr1ConstantsV31.TYPE_FILE.equals(action)) {

					Map<String, String> gspresponse1 = gspService.getGstr1Status(headerData, params, reference_id);
					response1 = gspService.decryptResponse(headerData, gspresponse1);
					String status = String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD));
					// update submit in db
					if (String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD))
							.equalsIgnoreCase(Gstr1ConstantsV31.GSTN_STATUS_CD_P)) {

						aspGstr1FileServiceV31.updateFileStatusInDb(gstr1GstnCol, requestParam, reference_id);

						// take inside this if
						controlMap.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD,
								String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD)));
						refIdList.add(controlMap);
					}
					// for other then P
					else if (Gstr1ConstantsV31.GSTN_STATUS_CD_E.equals(status)
							|| Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equals(status)) {
						Map<String, Object> errMap = null;
						if (response1.containsKey(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT)
								&& !Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equals(status)) {
							errMap = (Map<String, Object>) response1.remove(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
							errMap.put("error_msg",
									"Issue occurred while Filing data to GSTN, please Redo The File Again");
						} else {
							errMap = new HashMap<>();
							errMap.put("error_msg",
									"Issue occurred while Filing data to GSTN, please Redo The File Again");
							errMap.put("error_cd", "RETRECERROR");
						}

						controlMap.put(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD,
								String.valueOf(response1.get(Gstr1ConstantsV31.GSTN_JSON_STATUS_CD)));
						controlMap.putAll(errMap);
						refIdList.add(controlMap);
					}
					// need to update
					aspGstr1FileServiceV31.createSaveGstnStatusData(refIdList, ackNumber, type, action, gstin, fp);

				}

				// // for RETSAVE
				// else if
				// ((Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equalsIgnoreCase(st)||
				// Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equalsIgnoreCase(st))
				// && Gstr1ConstantsV31.TYPE_SAVE.equals(action)) {
				// handleGstnUnProcessedStatus(headerData, params,referIdMap);
				// }
			}

		}

	}

	public void handleGstnUnProcessedStatus(Map<String, String> headerData, Map<String, String> params) {
		List<Map<String, Object>> referIdMap = getAllReferIds(params);
		String action = "";
		String fp = "";
		String gstin = "";
		String st = "";
		String reference_id = "";
		String type = "";
		String ret_period = headerData.get(Gstr1ConstantsV31.INPUT_GSTN_RET_PER);
		Map<String, Object> response1 = new HashMap<>();
		Map<String, Object> requestData = new HashMap<>();
		Map<String, String> requestParam = new HashMap<>();

		String ackNumber = "";

		if (referIdMap != null && referIdMap.size() > 0) {
			Map<String, Object> refMap = (Map<String, Object>) referIdMap.get(0);

			action = MapUtils.getString(refMap, Gstr1ConstantsV31.INPUT_GSTN_ACTION);
			fp = MapUtils.getString(refMap, Gstr1ConstantsV31.INPUT_FP);
			gstin = MapUtils.getString(refMap, Gstr1ConstantsV31.INPUT_GSTN);
			ackNumber = MapUtils.getString(refMap, Gstr1ConstantsV31.ACKNUMBER);
			type = MapUtils.getString(refMap, Gstr1ConstantsV31.INPUT_TYPE);

			requestData.put("fp", fp);
			requestData.put("gstin", gstin);
			requestData.put(Gstr1ConstantsV31.INPUT_GSTN_RET_PER, ret_period);

			requestParam.put("fp", fp);
			requestParam.put("gstin", gstin);
			if (Gstr1ConstantsV31.TYPE_SAVE.equals(action)) {
				List<Map<String, Object>> referenceIdList = (List<Map<String, Object>>) referIdMap.get(0)
						.get(Gstr1ConstantsV31.JSON_REF_IDS);
				if (referenceIdList != null && referenceIdList.size() > 0) {
					int idx = 0;
					for (Map<String, Object> referenceIdMaps : referenceIdList) {
						st = MapUtils.getString(referenceIdMaps, Gstr1ConstantsV31.GSTN_JSON_STATUS_CD);
						reference_id = MapUtils.getString(referenceIdMaps, Gstr1ConstantsV31.INPUT_REFERENCE_ID);
						int totalCount = MapUtils.getIntValue(referenceIdMaps, Gstr1ConstantsV31.CONTROL_REC_RCRDCOUNT,
								0);
						if ((Gstr1ConstantsV31.GSTN_STATUS_CD_IP.equalsIgnoreCase(st)
								|| Gstr1ConstantsV31.GSTN_STATUS_CD_REC.equalsIgnoreCase(st))) {

							Map<String, String> gspresponse1 = gspService.getGstr1Status(headerData, params,
									reference_id);
							response1 = gspService.decryptResponse(headerData, gspresponse1);
							String status = MapUtils.getString(response1, Gstr1ConstantsV31.GSTN_JSON_STATUS_CD);
							String fieldPrefix = Gstr1ConstantsV31.JSON_REF_IDS + Gstr1ConstantsV31.DOT + idx
									+ Gstr1ConstantsV31.DOT;
							if (Gstr1ConstantsV31.GSTN_STATUS_CD_P.equalsIgnoreCase(status)) {
								UpdateDbForGstnResponse(response1, reference_id, requestParam,
										Gstr1ConstantsV31.STATUS_SAVED, Gstr1ConstantsV31.STATUS_UPLOAD);
								Map<String, Object> object = new HashMap<>();
								// object.put("refids." + idx + ".status_cd",
								// status);
								// object.put("refids." + idx + ".scnt",
								// totalCount);
								// object.put("refids." + idx + ".ecnt", 0);
								// object.put("refids." + idx + ".error_report",
								// null);
								object.put(fieldPrefix + "status_cd", status);
								object.put(fieldPrefix + "scnt", totalCount);
								object.put(fieldPrefix + "ecnt", 0);
								/*object.put(fieldPrefix + "error_report", null);*/
								Map<String, Object> whereMap = new HashMap<>();
								whereMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, ackNumber);
								updateGstnStatusData(object, "gstn_refid_map", whereMap);
								
								//deleting the error report for the reference id -- Prachi
								deleteErrorReport(reference_id,"gstn_refid_map");
								
							}
							// for other then P
							else if (Gstr1ConstantsV31.GSTN_STATUS_CD_E.equals(status)) {
								// up method.
								UpdateDbForGstnResponse(response1, reference_id, requestParam,
										Gstr1ConstantsV31.STATUS_SAVED, Gstr1ConstantsV31.STATUS_UPLOAD);
								Map<String, Object> errorReport = MapUtils.getMap(response1,
										Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
								Map<String, Object> object = new HashMap<>();
								// object.put("refids." + idx + ".status_cd",
								// status);
								// object.put("refids." + idx + ".scnt", 0);
								// object.put("refids." + idx + ".ecnt",
								// totalCount);
								// object.put("refids." + idx +
								// ".error_report.error.0.error_msg",
								// MapUtils.getString(errorReport,
								// Gstr1ConstantsV31.GSTN_JSON_ERROR_MSG));
								// object.put("refids." + idx +
								// ".error_report.error.0.error_cd",
								// MapUtils.getString(errorReport,
								// Gstr1ConstantsV31.GSTN_JSON_ERROR_CD));
								object.put(fieldPrefix + "status_cd", status);
								object.put(fieldPrefix + "scnt", 0);
								object.put(fieldPrefix + "ecnt", totalCount);
								Map<String, Object> whereMap = new HashMap<>();
								whereMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, ackNumber);
								updateGstnStatusData(object, "gstn_refid_map", whereMap);
								
								//Updating the error report for the reference id -- Prachi
								Map<String, Object> object1 = new HashMap<>();
								object1.put("error_report.error.0.error_msg",
										MapUtils.getString(errorReport, Gstr1ConstantsV31.GSTN_JSON_ERROR_MSG));
								object1.put("error_report.error.0.error_cd",
										MapUtils.getString(errorReport, Gstr1ConstantsV31.GSTN_JSON_ERROR_CD));
								Map<String, Object> whereMap1 = new HashMap<>();
								whereMap1.put(Gstr1ConstantsV31.CONTROL_JSON_ID, reference_id);
								updateGstnStatusData(object1, "gstn_refid_map", whereMap1);
								
							} else if (Gstr1ConstantsV31.GSTN_STATUS_CD_PE.equals(status)) {
								int peCnt = UpdateDbForGstnResponse(response1, reference_id, requestParam,
										Gstr1ConstantsV31.STATUS_SAVED, Gstr1ConstantsV31.STATUS_UPLOAD);
								
								//Resetting error report for the reference id -- Prachi
								
/*								Map<String, Object> whereMap = new HashMap<>();
								whereMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, reference_id);
								//whereMap.put(fieldPrefix + "reference_id", reference_id);

								String errorReportKey = fieldPrefix + "error_report";
								List<String> unsetKeyList = new ArrayList<>();
								unsetKeyList.add(errorReportKey);
								aspMongoDaoV31.deleteMongoWithPush(unsetKeyList, "gstn_refid_map", whereMap);*/
								
								deleteErrorReport(reference_id, "gstn_refid_map");

								Map<String, Object> errorReport = MapUtils.getMap(response1,
										Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT);
								
								Map<String, Object> object = new HashMap<>();
								BasicDBObject whereQuery = new BasicDBObject();
								Map<String, Object> whereMap = new HashMap<>();
								whereMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, ackNumber);
								whereMap.put(fieldPrefix + "reference_id", reference_id);
								object.put(fieldPrefix + "status_cd", status);
								object.put(fieldPrefix + "scnt", (totalCount - peCnt));
								object.put(fieldPrefix + "ecnt", peCnt);
								updateGstnStatusData(object, "gstn_refid_map", whereMap);
								
								Map<String, Object> errorReportMap = new HashMap<>();
								if(errorReport!=null && MapUtils.isNotEmpty(errorReport)){
									errorReportMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID,reference_id);
									errorReportMap.put(Gstr1ConstantsV31.GSTN_JSON_ERROR_REPORT, errorReport);
									JSONObject jsonObject = new JSONObject(errorReportMap);
									aspMongoDaoV31.saveInMongo(jsonObject, "gstn_refid_map");

								}else{
									CommonUtilV31.throwException(ErrorCodesV31.ASP525, Gstr1ConstantsV31.ASP_GSTR1_RETURN_STATUS_GRP,
											ErrorCodesV31.ASP525, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, null);
								}

							}

						}
						idx++;
					}
				}
			}
		}
	}

}
