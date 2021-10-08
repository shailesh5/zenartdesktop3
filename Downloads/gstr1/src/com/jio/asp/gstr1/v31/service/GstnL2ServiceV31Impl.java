/**
 * 
 */
package com.jio.asp.gstr1.v31.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.web.client.RestTemplate;

import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.exception.GspExceptionV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;

/**
 * @author Amit1.Dwivedi
 *
 */
@Service
public class GstnL2ServiceV31Impl implements GstnL2ServiceV31 {

	@Autowired
	private MessageSource messageSourceV31;
	@Autowired
	private MessageSource gstnResource;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private GSPServiceV31 gspService;

	@Autowired
	private AspMongoDaoV31 AspMongoDao;

	@Autowired
	BulkDownloadServiceV31 bulkDownloadServiceV31;

	public static final Logger log = LoggerFactory.getLogger(GstnL2ServiceV31Impl.class);

	@Override
	public Map<String, Object> processL2(Map<String, String> reqHeaderMap, Map<String, String> data) {

		log.debug("processL2 method : START");
		long sysCurTime=System.currentTimeMillis();
		log.info("processL2 method : START :: ackno{} :",sysCurTime);
		String collection = null;
		String collectionControl = null;
		
		Map<String, Object> syncResponse = new HashMap<>();
		// Chnaging the collection name if sync call is done
		if(data.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
		{
		collection = gstnResource.getMessage(Gstr1ConstantsV31.SYNC_DATA_COLLECTION_NAME, null, LocaleContextHolder.getLocale());
		collectionControl = gstnResource.getMessage(Gstr1ConstantsV31.SYNC_CONTROL_COLLECTION_NAME, null, LocaleContextHolder.getLocale());
		}
		else
		{
		collection = gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
		collectionControl = gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
		}
		Map<String, Object> responseMap = new HashMap<>();
		String mongoL2Key = Gstr1ConstantsV31.API_NM + ":" + data.get(Gstr1ConstantsV31.INPUT_GSTN) + ":"
				+ data.get(Gstr1ConstantsV31.INPUT_SECTION) + ":" + data.get(Gstr1ConstantsV31.INPUT_FP);
		data.put(Gstr1ConstantsV31.INPUT_ACK_NO, mongoL2Key);
		Map<String, Object> dataMap = null;
		if (data.get(Gstr1ConstantsV31.INPUT_FLUSH) != null
				&& data.get(Gstr1ConstantsV31.INPUT_FLUSH).equals("false")) {

			// boolean result = AspMongoDao.getKeyStatus(data,
			// collectionControl);

			Map<String, Object> object = new HashMap<>();
			object.put("_id", data.get(Gstr1ConstantsV31.INPUT_ACK_NO));
			List<Map<String, Object>> keyData = AspMongoDao.getMongoData(object, collectionControl);
			if (keyData != null && keyData.size() > 0) {
				Map<String, Object> keyValue = keyData.get(0);
				Map<String, Object> keyHeader = (Map<String, Object>) keyValue.get("header");

				if ("success".equalsIgnoreCase((String) keyHeader.get("status"))) {
					// sending data from db if sync call is not done
					if(data.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
					{
						syncResponse.put("result","success"); 
						dataMap = responseMap;
					}
					else
					{
						dataMap = getL2DataFromDB(data, collection);
					}
				} else {

					data.put("token", (String) keyHeader.get("token"));

					Map<String, Object> urlList = getUrlList(reqHeaderMap, data);
					if (urlList.containsKey("error_code")) {
						if (ErrorCodesV31.GSP006.equalsIgnoreCase((String) urlList.get(Gstr1ConstantsV31.ERROR_CODE))) {
							deleteL2DataKey(mongoL2Key, collection, collectionControl);
						}
						return urlList;
					}
					data.remove("token");
					Map<String, Object> l2Data = getBulkData(reqHeaderMap, data, urlList);
					saveL2DataInMongo(l2Data, collection, mongoL2Key, data);
					// sending data from db if sync call is not done
					if(data.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
					{
						syncResponse.put("result","success"); 
						dataMap = responseMap;
					}
					else
					{
						dataMap = getL2DataFromDB(data, collection);
					}
				}

			} else {
				Map<String, Object> l2KeyHeader = new HashMap<>();
				l2KeyHeader.put("type", "L2");
				l2KeyHeader.put("status", "new");
				l2KeyHeader.put("count", "0");
				deleteL2DataKey(mongoL2Key, collection, collectionControl);
				// createUpdateL2Key(data, l2KeyHeader);
				Map<String, Object> result = getL2DataFromGsp(reqHeaderMap, data);
				if (result.containsKey(Gstr1ConstantsV31.ERROR_GRP)){
					result.remove(Gstr1ConstantsV31.ERROR_GRP);
					data.remove(Gstr1ConstantsV31.INPUT_ACK_NO);
					responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, result);
					responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
					return responseMap;
				}
				// sending data from db if sync call is not done
				if(data.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
				{
					syncResponse.put("result","success"); 
					dataMap = responseMap;
				}
				else
				{
					dataMap = getL2DataFromDB(data, collection);
				}
			}

		} else {
			Map<String, Object> object = new HashMap<>();
			object.put("_id", data.get(Gstr1ConstantsV31.INPUT_ACK_NO));
			List<Map<String, Object>> keyData = AspMongoDao.getMongoData(object, collectionControl);
			if (keyData != null && keyData.size() > 0) {
				Map<String, Object> keyValue = keyData.get(0);
				Map<String, Object> keyHeader = (Map<String, Object>) keyValue.get("header");
				if ("token".equalsIgnoreCase((String) keyHeader.get("status"))) {
					data.put("token", (String) keyHeader.get("token"));

					Map<String, Object> urlList = getUrlList(reqHeaderMap, data);
					if (urlList.containsKey("data")) {
						
						return urlList;
					}else if (ErrorCodesV31.GSP006.equalsIgnoreCase((String) urlList.get(Gstr1ConstantsV31.ERROR_CODE))) {
						deleteL2DataKey(mongoL2Key, collection, collectionControl);
						urlList.remove(Gstr1ConstantsV31.ERROR_CODE);
						
						if(data.containsKey(Gstr1ConstantsV31.INPUT_ACTION) && data.containsValue(Gstr1ConstantsV31.SYNC_ACTION) ){
							CommonUtil.throwException(ErrorCodesV31.GSP010200, Gstr1ConstantsV31.JIOGST_SYNC, null, HttpStatus.OK,
									null, AspConstants.FORM_CODE,urlList);
						}
						CommonUtil.throwException(ErrorCodesV31.GSP010200, Gstr1ConstantsV31.GSTN_L2, null, HttpStatus.OK,
								null, AspConstants.FORM_CODE,urlList);
					}
					data.remove("token");
					Map<String, Object> l2Data = getBulkData(reqHeaderMap, data, urlList);
					saveL2DataInMongo(l2Data, collection, mongoL2Key, data);
					// sending data from db if sync call is not done
					if(data.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
					{
						syncResponse.put("result","success"); 
						dataMap = responseMap;
					}
					else
					{
						dataMap = getL2DataFromDB(data, collection);
					}
				} else {
					Map<String, Object> l2KeyHeader = new HashMap<>();
					l2KeyHeader.put("type", "L2");
					l2KeyHeader.put("status", "new");
					l2KeyHeader.put("count", "0");
					deleteL2DataKey(mongoL2Key, collection, collectionControl);
					// createUpdateL2Key(data, l2KeyHeader);
					Map<String, Object> result = getL2DataFromGsp(reqHeaderMap, data);
					if (result.containsKey(Gstr1ConstantsV31.ERROR_CODE)){
						
						result.remove(Gstr1ConstantsV31.ERROR_GRP);
					data.remove(Gstr1ConstantsV31.INPUT_ACK_NO);
					responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, result);
					responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
					return responseMap;
					} 
					// sending data from db if sync call is not done
					if(data.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
					{
						syncResponse.put("result","success"); 
						dataMap = responseMap;
					}
					else
					{
						dataMap = getL2DataFromDB(data, collection);
					}
				}
			} else {
				Map<String, Object> l2KeyHeader = new HashMap<>();
				l2KeyHeader.put("type", "L2");
				l2KeyHeader.put("status", "new");
				l2KeyHeader.put("count", "0");
				deleteL2DataKey(mongoL2Key, collection, collectionControl);
				// createUpdateL2Key(data, l2KeyHeader);
				Map<String, Object> result = getL2DataFromGsp(reqHeaderMap, data);
				if (result.containsKey(Gstr1ConstantsV31.ERROR_GRP)){
					result.remove(Gstr1ConstantsV31.ERROR_GRP);
					data.remove(Gstr1ConstantsV31.INPUT_ACK_NO);
					responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, result);
					responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
					return responseMap;
				}
				// sending data from db if sync call is not done
				if(data.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
				{
					syncResponse.put("result","success"); 
					dataMap = responseMap;
				}
				else
				{
					dataMap = getL2DataFromDB(data, collection);
				}
			}
		}

		if (MapUtils.isNotEmpty(dataMap)) {
			log.info("processGstr1InvoiceDataL2 Method - ASP Data parsing and preparation complete - STEP6");
			// business logic step and response preparation step
			data.remove(Gstr1ConstantsV31.INPUT_ACK_NO);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
			log.info("processGstr1InvoiceDataL2 Method - Preparing the API response - STEP7");
		} else {
			dataMap = new HashMap<>();
			responseMap = new HashMap<>();
			dataMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, "No data found for the given criteria");
			dataMap.put(Gstr1ConstantsV31.RESP_STATUS_CODE, Gstr1ConstantsV31.ERROR);
			data.remove(Gstr1ConstantsV31.INPUT_ACK_NO);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
		}

		log.info("Summary L2 redis key generated for query ", mongoL2Key);
		return responseMap;
	}

	private void deleteL2DataKey(String mongoL2Key, String collection, String collectionControl) {
		AspMongoDao.deleteL2Key(mongoL2Key, collection, collectionControl);
	}

	/**
	 * Method based on the user input for api call, will query database and
	 * retrieve the related information.
	 * 
	 * @param inputMap
	 *            user input map
	 * @return Map containing asp data per section.
	 */
	public Map<String, Object> getL2DataFromDB(Map<String, String> inputMap, String collection) {
		log.debug("getAspInvoiceDataL2 method : START");
		Map<String, Object> respMap = (Map<String, Object>) AspMongoDao.getL2Data(inputMap, collection);
		Map<String, Object> respMap2 = getPagedData(respMap, inputMap);
		log.debug("getAspInvoiceDataL2 method : END");
		return respMap2;
	}

	private void saveL2DataInMongo(Map<String, Object> l2Data, String gstr1l2DataCol, String mongoL2Key,
			Map<String, String> data) {
		log.info("saveL2DataInMongo method : START");
		Set<String> keySet = l2Data.keySet();
		Map<String, Object> header = new HashMap<>();
		header.put("fp", data.get(Gstr1ConstantsV31.INPUT_FP));
		header.put("type", data.get(Gstr1ConstantsV31.INPUT_SECTION));
		header.put("gstin", data.get(Gstr1ConstantsV31.INPUT_GSTN));
		header.put("utime", new Timestamp(System.currentTimeMillis()));

		int keySize = keySet.size();
		Iterator itr = keySet.iterator();
		Map<String, Object> response = prepareGspData(l2Data, data);
		// while (itr.hasNext()) {
		// int j = 0;
		// String type = (String) itr.next();
		// int conut;
		// if (l2Data.get(type ) instanceof List<?> ) {
		// List<Object> l2Value = (List<Object>) l2Data.get(type);
		// for (int i = 0; i < l2Value.size(); i++) {
		//
		// j = j + i;
		// Map<String, Object> l2DataIN = new HashMap<>();
		// l2DataIN.put("_id", mongoL2Key + ":" + j);
		// l2DataIN.put("gstn", l2Value.get(i));
		// l2DataIN.put("header", header);
		//
		// AspMongoDao.saveInMongo(new JSONObject(l2DataIN), gstr1l2DataCol);
		// }
		// conut=l2Value.size();
		// } else {
		// Map<String , Object> l2Value = (Map<String, Object>)
		// l2Data.get(type);
		// for (int i = 0; i < l2Value.size(); i++) {
		// j = j + i;
		//
		// Map<String, Object> l2DataIN = new HashMap<>();
		// l2DataIN.put("_id", mongoL2Key + ":" + j);
		// l2DataIN.put("gstn", l2Value);
		// l2DataIN.put("header", header);
		//
		// AspMongoDao.saveInMongo(new JSONObject(l2DataIN), gstr1l2DataCol);
		// }
		// conut=l2Value.size();
		// }
		//
		//
		// }

		for (int i = 0; i < response.size(); i++) {
			Map<String, Object> l2DataIN = new HashMap<>();
			l2DataIN.put("_id", mongoL2Key + ":" + i);
			l2DataIN.put("gstn", response.get("" + i));
			l2DataIN.put("header", header);
			AspMongoDao.saveInMongo(new JSONObject(l2DataIN), gstr1l2DataCol);
		}
		// response.forEach((k,v)->System.out.println("Key : " + k + " Value : "
		// + v));
		// response.forEach((k,v)->AspMongoDao.saveInMongo(new JSONObject(v),
		// gstr1l2DataCol));
		Map<String, Object> l2KeyHeader = new HashMap<>();
		l2KeyHeader.put("type", "L2");
		l2KeyHeader.put("status", "success");
		l2KeyHeader.put("count", response.size());
		l2KeyHeader.put("token", data.get("token"));
		l2KeyHeader.put("updateTime", new Timestamp(System.currentTimeMillis()));

		createUpdateL2Key(data, l2KeyHeader);
		log.info("saveL2DataInMongo method : END");
	}

	private void createUpdateL2Key(Map<String, String> data, Map<String, Object> l2KeyHeader) {
		String collection = null;
		
		if(data.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
			collection = gstnResource.getMessage(Gstr1ConstantsV31.SYNC_CONTROL_COLLECTION_NAME, null, LocaleContextHolder.getLocale());
		else
			collection = gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
		
		Map<String, Object> respMap = new HashMap<>();
		respMap.put("_id", data.get(Gstr1ConstantsV31.INPUT_ACK_NO));
		respMap.put("header", l2KeyHeader);
		AspMongoDao.saveInMongo(new JSONObject(respMap), collection);
	}

	private Map<String, Object> getL2DataFromGsp(Map<String, String> headerData, Map<String, String> data) {
		log.debug("getGstr1InvoiceData method : START");
		log.debug("getGstr1InvoiceData method : before calling GSP for gstn data");
		String gstr1L2DataCol = null;
		
		if(data.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
			gstr1L2DataCol = gstnResource.getMessage(Gstr1ConstantsV31.SYNC_DATA_COLLECTION_NAME, null, LocaleContextHolder.getLocale());
		else
			gstr1L2DataCol = gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
		// making call to gstr1 thru GSP.
		Map<String, String> gspresponse = gspService.getL2(data, headerData);
		// parsing GSP data to POJO
		Map<String, Object> decrypedtL2Data = gspService.decryptResponse(headerData, gspresponse);

		if (decrypedtL2Data.containsKey(Gstr1ConstantsV31.RESP_L2_KEY_TOKEN)) {
			log.debug("getL2 method : Error response received from GSTN, processing it");
			Map<String, Object> l2KeyHeader = new HashMap<>();
			l2KeyHeader.put("type", "L2");
			l2KeyHeader.put("status", "token");
			l2KeyHeader.put("updateTime", new Timestamp(System.currentTimeMillis()));
			l2KeyHeader.put("count", "0");
			l2KeyHeader.put("token", decrypedtL2Data.get(Gstr1ConstantsV31.RESP_L2_KEY_TOKEN));
			createUpdateL2Key(data, l2KeyHeader);

			// error message
			String est = (String) decrypedtL2Data.get(Gstr1ConstantsV31.GSTN_EST_BULK);
			Map<String, Object> respMap = new HashMap<>();
			respMap.put(Gstr1ConstantsV31.RESP_STATUS_CODE, Gstr1ConstantsV31.PROCESSING);
			respMap.put(Gstr1ConstantsV31.ERROR_MESSAGE,
					"Due to bulk download, gstn will be take time to prepare response, kindly visit after some time(estimated time :"
							+ est + " minutes)");
			respMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_GSTN_GET);
			// throw new GspExceptionV31("Error in Gsp Data", null, false,
			// false, respMap);
			return respMap;

		} else {
			saveL2DataInMongo(decrypedtL2Data, gstr1L2DataCol, data.get(Gstr1ConstantsV31.INPUT_ACK_NO), data);
		}
		log.debug("getGstr1InvoiceData method : END");
		return decrypedtL2Data;
	}

	public Map<String, Object> getUrlList(Map<String, String> headers, Map<String, String> data) {

		Map<String, String> gspresponse = bulkDownloadServiceV31.getUrlList(data, headers, data.get("token"));
		// parsing GSP data to POJO
		Map<String, Object> responseMap = new HashMap<>();
		if (gspresponse.containsValue(Gstr1ConstantsV31.PROCESSING)) {
			
			
//			responseMap.putAll(gspresponse);
			data.remove(Gstr1ConstantsV31.INPUT_ACK_NO);
			data.remove(Gstr1ConstantsV31.TOKEN);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, gspresponse);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
			
			return responseMap;

		} else if(gspresponse.containsKey(Gstr1ConstantsV31.RESPONSE_DEV_MSG)){
			responseMap.putAll(gspresponse);
			return responseMap;
		}
		
		Map<String, Object> decryptedUrls = gspService.decryptResponse(headers, gspresponse);

		return decryptedUrls;
	}

	public Map<String, Object> getBulkData(Map<String, String> headers, Map<String, String> data,
			Map<String, Object> urlList) {
		Map<String, Object> response = new HashMap<>();

		List<Object> urlListData = (List<Object>) urlList.get("urls");
		String ek = (String) urlList.get("ek");
		for (int i = 0; i < urlListData.size(); i++) {

			Map<String, Object> urlMap = (Map<String, Object>) urlListData.get(i);
			urlMap.put("url", urlMap.get("ul"));
			urlMap.remove("ul");
			Map<String, String> gspresponse = bulkDownloadServiceV31.getBulkDataByUrl(data, headers, urlMap);
			// parsing GSP data to POJO

			Map<String, Object> decrypedtL2Data = bulkDownloadServiceV31.bulkDataDecription(gspresponse.get("data"),
					ek);
			if (response.containsKey(data.get(Gstr1ConstantsV31.INPUT_SECTION))) {

				response.put(data.get(Gstr1ConstantsV31.INPUT_SECTION) + i,
						decrypedtL2Data.get(data.get(Gstr1ConstantsV31.INPUT_SECTION)));

			} else {

				response.put(data.get(Gstr1ConstantsV31.INPUT_SECTION) + i,
						decrypedtL2Data.get(data.get(Gstr1ConstantsV31.INPUT_SECTION)));
			}
		}

		return response;

	}

	private Map<String, Object> prepareGspData(Map<String, Object> gstr1Summary, Map<String, String> data) {
		Map<String, Object> responseMap = new HashMap<>();
		String section = (String) data.get(Gstr1ConstantsV31.INPUT_SECTION);
		if (Gstr1ConstantsV31.TYPE_NIL.equals(section)) {
			List<Object> invList = new ArrayList<>();
			Map<Object, Object> b2bData = (Map<Object, Object>) gstr1Summary.get(section);
			String flag = (String) b2bData.get("flag");
			String chksum = (String) b2bData.get("chksum");
			if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
					&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("3"))) {

				responseMap.put("0", b2bData);

				// }

			} else {

				if (b2bData.containsKey("inv")) {
					Map<Object, Object> dataMap = new HashMap<>();
					invList = (List<Object>) b2bData.get("inv");
					// for (int i = 0; i < invList.size(); i++) {

					// dataMap = (Map<Object, Object>) invList.get(i);
					// responseMap.put("" + i, dataMap);
					responseMap.put("0", invList);

					// }

				}

			}
		} else if (Gstr1ConstantsV31.TYPE_B2CS.equals(section) || Gstr1ConstantsV31.TYPE_B2CSA.equals(section)) {
			Map<String, Object> invMap = new HashMap<>();
			List<Object> b2bData = (List<Object>) gstr1Summary.get(section);
			for (int i = 0; i < b2bData.size(); i++) {
				invMap = (Map<String, Object>) b2bData.get(i);
				responseMap.put("" + i, invMap);
			}

		} else if (Gstr1ConstantsV31.TYPE_AT.equals(section) || Gstr1ConstantsV31.TYPE_ATA.equals(section)
				|| Gstr1ConstantsV31.TYPE_CDNUR.equals(section)|| Gstr1ConstantsV31.TYPE_CDNURA.equals(section)) {
			List<Object> invList = new ArrayList<>();
			List<Object> b2bData = (List<Object>) gstr1Summary.get(section);
			for (int i = 0; i < b2bData.size(); i++) {
				Map<String, Object> invMap = (Map<String, Object>) b2bData.get(i);

				responseMap.put(String.valueOf(i), invMap);
			}

		}

		else if (Gstr1ConstantsV31.TYPE_CDNR.equals(section)|| Gstr1ConstantsV31.TYPE_CDNRA.equals(section)) {
			List<Object> invList = new ArrayList<>();
			List<Object> b2bData = (List<Object>) gstr1Summary.get(section);
			int k = 0;
			for (int i = 0; i < b2bData.size(); i++) {
				Map<String, Object> invMap = (Map<String, Object>) b2bData.get(i);
				List<Object> ntList = (List<Object>) invMap.get("nt");
				for (int j = 0; j < ntList.size(); j++) {
					responseMap.put(String.valueOf(k), invMap);
					k++;
				}
			}

		}

		else if (Gstr1ConstantsV31.TYPE_TXPD.equals(section + "d")) {
			List<Object> invList = new ArrayList<>();
			List<Object> b2bData = (List<Object>) gstr1Summary.get(Gstr1ConstantsV31.TYPE_TXPD);
			for (int i = 0; i < b2bData.size(); i++) {
				Map<String, Object> invMap = (Map<String, Object>) b2bData.get(i);

				responseMap.put(String.valueOf(i), invMap);
			}

		}
		
		else if (Gstr1ConstantsV31.TYPE_TXPDA.equals(section.replace('a', 'd') + "a")) {
			List<Object> invList = new ArrayList<>();
			List<Object> b2bData = (List<Object>) gstr1Summary.get(Gstr1ConstantsV31.TYPE_TXPDA);
			for (int i = 0; i < b2bData.size(); i++) {
				Map<String, Object> invMap = (Map<String, Object>) b2bData.get(i);

				responseMap.put(String.valueOf(i), invMap);
			}

		}

		else if (StringUtils.containsIgnoreCase(section, Gstr1ConstantsV31.TYPE_HSN)) {
			if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
					&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("3"))) {

				List<Object> invList = new ArrayList<>();
				Map<Object, Object> b2bData = (Map<Object, Object>) gstr1Summary.get(Gstr1ConstantsV31.TYPE_HSN);
				String chksum = (String) b2bData.get("chksum");
				String flag = (String) b2bData.get("flag");
				if (b2bData.containsKey("data")) {

					invList = (List<Object>) b2bData.get("data");
					for (int i = 0; i < invList.size(); i++) {
						Map<Object, Object> dataMap = new HashMap<>();
						Map<Object, Object> dataMapKey = new HashMap<>();
						dataMapKey = (Map<Object, Object>) invList.get(i);
						List<Object> dataList = new ArrayList<>();
						dataList.add(dataMapKey);
						dataMap.put("chksum", chksum);
						dataMap.put("flag", flag);
						dataMap.put("data", dataList);
						responseMap.put(String.valueOf(i), dataMap);
					}
				}

			}

			else {

				List<Object> invList = new ArrayList<>();
				Map<Object, Object> b2bData = (Map<Object, Object>) gstr1Summary.get(Gstr1ConstantsV31.TYPE_HSN);
				String chksum = (String) b2bData.get("chksum");
				String flag = (String) b2bData.get("flag");
				if (b2bData.containsKey("data")) {

					invList = (List<Object>) b2bData.get("data");
					for (int i = 0; i < invList.size(); i++) {
						Map<Object, Object> dataMap = new HashMap<>();
						dataMap = (Map<Object, Object>) invList.get(i);
						dataMap.put("chksum", chksum);
						dataMap.put("flag", flag);
						responseMap.put(String.valueOf(i), dataMap);
					}
				}

			}
		} else if (Gstr1ConstantsV31.TYPE_DOCS_L2.equalsIgnoreCase(section)) {
			if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
					&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("3"))) {

				List<Object> invList = new ArrayList<>();
				Map<Object, Object> b2bData = (Map<Object, Object>) gstr1Summary.get(Gstr1ConstantsV31.TYPE_DOCS);
				String chksum = (String) b2bData.get("chksum");
				String flag = (String) b2bData.get("flag");

				if (b2bData.containsKey("doc_det")) {

					invList = (List<Object>) b2bData.get("doc_det");
					for (int i = 0; i < invList.size(); i++) {
						Map<Object, Object> dataMap = new HashMap<>();
						List<Object> docsList = new ArrayList<>();
						Map<Object, Object> dataMapDocs = new HashMap<>();
						dataMapDocs = (Map<Object, Object>) invList.get(i);
						docsList.add(dataMapDocs);
						dataMap.put("chksum", chksum);
						dataMap.put("flag", flag);
						dataMap.put("doc_det", docsList);
						responseMap.put(String.valueOf(i), dataMap);
					}
				}
			}

			else {
				List<Object> invList = new ArrayList<>();
				Map<Object, Object> b2bData = (Map<Object, Object>) gstr1Summary.get(Gstr1ConstantsV31.TYPE_DOCS);
				String chksum = (String) b2bData.get("chksum");
				String flag = (String) b2bData.get("flag");

				if (b2bData.containsKey("doc_det")) {
					Map<Object, Object> dataMap = new HashMap<>();
					invList = (List<Object>) b2bData.get("doc_det");
					for (int i = 0; i < invList.size(); i++) {
						dataMap = (Map<Object, Object>) invList.get(i);
						dataMap.put("chksum", chksum);
						dataMap.put("flag", flag);
						responseMap.put(String.valueOf(i), dataMap);
					}
				}
			}
		}

		else {
			List<Object> b2bData = new ArrayList<>();
			if (gstr1Summary.size() == 1) {
				List<Object> dataList = (List<Object>) gstr1Summary.get(section);
				b2bData.addAll(dataList);
			} else {
				for (int k = 0; k < gstr1Summary.size(); k++) {
					List<Object> dataList = (List<Object>) gstr1Summary.get(section + k);
					b2bData.addAll(dataList);
				}
			}

			// List<Object> b2bData = (List<Object>) gstr1Summary.get(section);
			int hk = 0;
			for (int i = 0; i < b2bData.size(); i++) {

				Map<String, Object> invMap = (Map<String, Object>) b2bData.get(i);
				if (invMap.containsKey("inv")) {
					List<Object> invList = new ArrayList<>();
					invList = (List<Object>) invMap.get("inv");
					String ctin = (String) invMap.get("ctin");
					String cfs = (String) invMap.get("cfs");
					String exp_typ = null;
					String pos = null;
					if(Gstr1ConstantsV31.TYPE_EXP.equalsIgnoreCase(section)){
						  exp_typ = (String) invMap.get("exp_typ");
					}
					else
						  pos = (String) invMap.get("pos");
					
					for (int j = 0; j < invList.size(); j++) {
						List l2 = new ArrayList<>();
						Map invInnerMap = (Map) invList.get(j);
						invInnerMap.put("ctin", ctin);
						invInnerMap.put("cfs", cfs);
						if (Gstr1ConstantsV31.TYPE_EXP.equalsIgnoreCase(section)) {
							invInnerMap.put("exp_typ", exp_typ);
						} else {
							invInnerMap.put("pos", pos);
						}
//						invInnerMap.put("pos", pos);
						l2.add(invInnerMap);
						responseMap.put(String.valueOf(hk), l2);
						hk++;

					}

				} else if (invMap.containsKey("nt")) {
					List<Object> invList = new ArrayList<>();
					invList = (List<Object>) invMap.get("nt");
					responseMap.put(String.valueOf(i), invList);
				}

			}
		}
		return responseMap;

	}

	public Map<String, Object> getPagedData(Map<String, Object> respMap, Map<String, String> data) {
		if(respMap==null){
			Map<String, Object> responseMap = new HashMap<>();
			return responseMap;
		}

		if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
				&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("3"))) {
			List<Object> list = (List<Object>) respMap.get(Gstr1ConstantsV31.ASP_API_RESP_RCRD_ATTR);
			List<Object> recordsList = new ArrayList<>();
			String stId = (String) data.get(Gstr1ConstantsV31.STORE_ID_IN_DATA);
			if (Gstr1ConstantsV31.TYPE_NIL.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				for (int i = 0; i < list.size(); i++) {
					List<Object> innerList = new ArrayList<>();
					// Map<Object, Object> innerMap = new HashMap<>();
					// innerList = (List<Object>) list.get(i);
					// innerMap.put("inv", innerList);
					recordsList.add(list.get(i));
				}

			}

			else if (Gstr1ConstantsV31.TYPE_B2CS.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_B2CSA.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				for (int i = 0; i < list.size(); i++) {
					Map<Object, Object> innerMap = new HashMap<>();
					innerMap = (Map<Object, Object>) list.get(i);
					List<Object> l2 = new ArrayList<>();
					// l2.add(innerMap);
					recordsList.add(innerMap);
				}

			} else if (Gstr1ConstantsV31.TYPE_AT.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_ATA.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				for (int i = 0; i < list.size(); i++) {
					Map<Object, Object> innerMap = new HashMap<>();
					innerMap = (Map<Object, Object>) list.get(i);
					recordsList.add(innerMap);

				}

			} else if (Gstr1ConstantsV31.TYPE_CDNR
					.equalsIgnoreCase((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_CDNRA
					.equalsIgnoreCase((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
						&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {

					for (int i = 0; i < list.size(); i++) {
						List<Object> cdnList = new ArrayList<>();
						cdnList.add(list.get(i));
						recordsList.add(cdnList);
					}

				} else {

					int count = 0;
					Map<String, Object> invMap = null;
					for (int i = 0; i < list.size(); i++) {
						Map<Object, Object> innerMap = new HashMap<>();
						innerMap = (Map<Object, Object>) list.get(i);
						List<Object> listOfNode = (List<Object>) innerMap.get("nt");
						innerMap.remove("nt");
						invMap = new HashMap<>();
						for (int j = 0; j < listOfNode.size(); j++) {
							Map<Object, Object> ntMap = (Map<Object, Object>) listOfNode.get(j);
							ntMap.putAll(innerMap);
							List<Object> ntList = new ArrayList<>();
							ntList.add(ntMap);
							invMap.put("nt", ntList);
						}

						recordsList.add(invMap);
					}

					respMap.put(Gstr1ConstantsV31.JSON_TTL_RCRD, list.size());
				}
			}

			else if (Gstr1ConstantsV31.TYPE_CDNUR.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_CDNURA.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				recordsList.addAll(list);

			} else if (Gstr1ConstantsV31.TYPE_TXPD.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION) + "d")
					|| Gstr1ConstantsV31.TYPE_DOCS_L2.equalsIgnoreCase(data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_TXPDA.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION).replace('a', 'd') + "a")) {
				for (int i = 0; i < list.size(); i++) {
					Map<Object, Object> innerMap = new HashMap<>();
					List<Object> l2 = new ArrayList<>();
					innerMap = (Map<Object, Object>) list.get(i);
					// Map<String, List<Object>> itemMap = new HashMap<>();
					// itemMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC_L2_List,
					// l2);

					Map<String, Object> itemMap = new HashMap<>();
					if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
							&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
						itemMap.put("id", stId);
						itemMap.put("rates", l2);
					} else {
						itemMap.put(stId, l2);
					}
					// itemMap.put(stId, l2);
					recordsList.add(innerMap);
				}

			}
			// else if (Gstr1ConstantsV31.TYPE_TXPD.equals((String)
			// data.get(Gstr1ConstantsV31.INPUT_SECTION) + "d")
			// ||
			// Gstr1ConstantsV31.TYPE_DOCS_L2.equalsIgnoreCase(data.get(Gstr1ConstantsV31.INPUT_SECTION)))
			// {
			// for (int i = 0; i < list.size(); i++) {
			// Map<Object, Object> inerList = new HashMap<>();
			// inerList = (Map<Object, Object>) list.get(i);
			// recordsList.add(inerList);
			// }
			//
			// }

			else if ((Gstr1ConstantsV31.TYPE_HSN + "SUM")
					.equalsIgnoreCase((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
						&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("3"))) {

					int count = 0;
					for (int i = 0; i < list.size(); i++) {
						Map<String, Object> innerMap = new HashMap<>();
						innerMap = (Map<String, Object>) list.get(i);
						recordsList.add(innerMap);
					}

				} else {

					int count = 0;
					for (int i = 0; i < list.size(); i++) {
						List<Object> innerMap = new ArrayList<>();
						innerMap = (List<Object>) list.get(i);
						List<Object> l2 = new ArrayList<>();
						l2.add(innerMap);

						Map<String, Object> itemMap = new HashMap<>();
						if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
								&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
							itemMap.put("id", stId);
							itemMap.put("rates", l2);
						} else {
							itemMap.put(stId, l2);
						}
						// itemMap.put(stId, l2);
						recordsList.add(itemMap);
						// recordsList.add(l2);
					}

				}
			} else

			{

				for (int i = 0; i < list.size(); i++) {
					List<Object> inerList = (List<Object>) list.get(i);
					List<Object> invList = new ArrayList<>();
					for (int j = 0; j < inerList.size(); j++) {
						Map<Object, Object> recMap = new HashMap<>();
						recMap = (Map<Object, Object>) inerList.get(j);
						invList.add(recMap);
					}
					Map<String, Object> invMap = new HashMap<>();
					invMap.put("inv", invList);
					recordsList.add(invMap);
				}
			}

			Map<String, Object> result = new HashMap<>();
			result.put("record", recordsList);
			result.put("ttl_record", respMap.get(Gstr1ConstantsV31.JSON_TTL_RCRD));
			result.put("gstin", data.get(Gstr1ConstantsV31.INPUT_GSTN));
			result.put("fp", data.get(Gstr1ConstantsV31.INPUT_FP));
			result.put("uTime", respMap.get("utime"));
			result.put(Gstr1ConstantsV31.JSON_TYPE_SEC,data.get(Gstr1ConstantsV31.INPUT_SECTION) );
			// result.put("sname", "");
			// result.put("gt", "");
			return result;
		} else {

			List<Object> list = (List<Object>) respMap.get(Gstr1ConstantsV31.ASP_API_RESP_RCRD_ATTR);
			List<Object> recordsList = new ArrayList<>();
			String stId = (String) data.get(Gstr1ConstantsV31.STORE_ID_IN_DATA);
			if (Gstr1ConstantsV31.TYPE_NIL.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				for (int i = 0; i < list.size(); i++) {
					List<Object> innerList = new ArrayList<>();
					Map<Object, Object> innerMap = new HashMap<>();

					innerList = (List<Object>) list.get(i);
					List<Object> l2 = new ArrayList<>();
					l2.add(innerList);
					// change regarding new structure
					// Map<String, List<Object>> itemMap = new HashMap<>();
					// itemMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC_L2_List,
					// l2);

					Map<String, Object> itemMap = new HashMap<>();
					if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
							&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
						itemMap.put("id", stId);
						itemMap.put("rates", l2);
					} else {
						itemMap.put(stId, l2);
					}
					// itemMap.put(stId, l2);
					recordsList.add(itemMap);
					// recordsList.add(l2);
				}

			}

			else if (Gstr1ConstantsV31.TYPE_B2CS.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_B2CSA.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				for (int i = 0; i < list.size(); i++) {
					Map<Object, Object> innerMap = new HashMap<>();
					innerMap = (Map<Object, Object>) list.get(i);
					List<Object> l2 = new ArrayList<>();
					l2.add(innerMap);
					// change regarding new structure
					Map<String, Object> itemMap = new HashMap<>();

					// itemMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC_L2_List,
					// l2);
					if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
							&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
						itemMap.put("id", stId);
						itemMap.put("rates", l2);
					} else {
						itemMap.put(stId, l2);
					}

					recordsList.add(itemMap);
					// recordsList.add(l2);
				}

			} else if (Gstr1ConstantsV31.TYPE_AT.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_ATA.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				for (int i = 0; i < list.size(); i++) {
					Map<Object, Object> innerMap = new HashMap<>();
					innerMap = (Map<Object, Object>) list.get(i);
					List<Object> l2 = new ArrayList<>();
					l2.add(innerMap);
					// Map<String, List<Object>> itemMap = new HashMap<>();
					// itemMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC_L2_List,
					// l2);
					Map<String, Object> itemMap = new HashMap<>();
					if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
							&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
						itemMap.put("id", stId);
						itemMap.put("rates", l2);
					} else {
						itemMap.put(stId, l2);
					}
					// itemMap.put(stId, l2);
					recordsList.add(itemMap);

				}

			} else if (Gstr1ConstantsV31.TYPE_CDNR
					.equalsIgnoreCase((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_CDNRA
					.equalsIgnoreCase((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
						&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {

					for (int i = 0; i < list.size(); i++) {
						List<Object> cdnList = new ArrayList<>();
						cdnList.add(list.get(i));
						recordsList.add(cdnList);
					}

				} else {

					int count = 0;
					Map<String, Object> invMap = null;
					for (int i = 0; i < list.size(); i++) {
						Map<Object, Object> innerMap = new HashMap<>();
						innerMap = (Map<Object, Object>) list.get(i);
						List<Object> listOfNode = (List<Object>) innerMap.get("nt");
						innerMap.remove("nt");
						// List<Object> invList = new ArrayList<>();
						// count = count + listOfNode.size();
						// for (Iterator iterator = listOfNode.iterator();
						// iterator.hasNext();) {
						// Map<Object, Object> object = (Map<Object, Object>)
						// iterator.next();
						// object.putAll(innerMap);
						// recordsList.add(object);
						// invList.add(innerMap);
						// }
						// // recordsList.add(innerMap);
						// // recordsList.add(innerMap);
						invMap = new HashMap<>();
						//
						// invMap.put("nt", invList);
						// recordsList.add(invMap);
						for (int j = 0; j < listOfNode.size(); j++) {
							Map<Object, Object> ntMap = (Map<Object, Object>) listOfNode.get(j);
							ntMap.putAll(innerMap);
							List<Object> ntList = new ArrayList<>();
							ntList.add(ntMap);
							// listOfNode.add(ntMap);
							invMap.put("nt", ntList);
						}

						recordsList.add(invMap);
					}

					respMap.put(Gstr1ConstantsV31.JSON_TTL_RCRD, list.size());
				}
			}

			else if (Gstr1ConstantsV31.TYPE_CDNUR.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_CDNURA.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {

				if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
						&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
					recordsList.addAll(list);
				} else {
					int count = 0;
					for (int i = 0; i < list.size(); i++) {
						Map<Object, Object> innerMap = new HashMap<>();
						innerMap = (Map<Object, Object>) list.get(i);
						// List<Object> listOfNode = (List<Object>)
						// innerMap.get("nt");
						// innerMap.remove("nt");
						// count = count + listOfNode.size();
						// for (Iterator iterator = listOfNode.iterator();
						// iterator.hasNext();) {
						// Map<Object, Object> object = (Map<Object, Object>)
						// iterator.next();
						// object.putAll(innerMap);
						List<Object> invList = new ArrayList<>();
						// recordsList.add(innerMap);
						Map<String, Object> invMap = new HashMap<>();
						invList.add(innerMap);
						invMap.put("nt", invList);
						recordsList.add(invMap);
						// }

						// recordsList.add(innerMap);
						count = i + 1;
					}

					respMap.put(Gstr1ConstantsV31.JSON_TTL_RCRD, count);
				}
			} else if (Gstr1ConstantsV31.TYPE_TXPD.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION) + "d")
					|| Gstr1ConstantsV31.TYPE_DOCS_L2.equalsIgnoreCase(data.get(Gstr1ConstantsV31.INPUT_SECTION))||Gstr1ConstantsV31.TYPE_TXPDA.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION).replace('a', 'd') + "a")) {
				for (int i = 0; i < list.size(); i++) {
					Map<Object, Object> innerMap = new HashMap<>();
					List<Object> l2 = new ArrayList<>();
					innerMap = (Map<Object, Object>) list.get(i);
					// Map<String, List<Object>> itemMap = new HashMap<>();
					// itemMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC_L2_List,
					// l2);

					Map<String, Object> itemMap = new HashMap<>();
					if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
							&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
						itemMap.put("id", stId);
						itemMap.put("rates", l2);
					} else {
						itemMap.put(stId, l2);
					}
					// itemMap.put(stId, l2);
					recordsList.add(innerMap);
				}

			}
			// else if (Gstr1ConstantsV31.TYPE_TXPD.equals((String)
			// data.get(Gstr1ConstantsV31.INPUT_SECTION) + "d")
			// ||
			// Gstr1ConstantsV31.TYPE_DOCS_L2.equalsIgnoreCase(data.get(Gstr1ConstantsV31.INPUT_SECTION)))
			// {
			// for (int i = 0; i < list.size(); i++) {
			// Map<Object, Object> inerList = new HashMap<>();
			// inerList = (Map<Object, Object>) list.get(i);
			// recordsList.add(inerList);
			// }
			//
			// }

			else if ((Gstr1ConstantsV31.TYPE_HSN + "SUM")
					.equalsIgnoreCase((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
				int count = 0;
				for (int i = 0; i < list.size(); i++) {
					Map<Object, Object> innerMap = new HashMap<>();
					innerMap = (Map<Object, Object>) list.get(i);
					List<Object> l2 = new ArrayList<>();
					l2.add(innerMap);

					Map<String, Object> itemMap = new HashMap<>();
					if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
							&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
						itemMap.put("id", stId);
						itemMap.put("rates", l2);
					} else {
						itemMap.put(stId, l2);
					}
					// itemMap.put(stId, l2);
					recordsList.add(itemMap);
					// recordsList.add(l2);
				}

			} else

			{

				if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
						&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
					recordsList.addAll(list);
				} else {

					for (int i = 0; i < list.size(); i++) {
						List<Object> inerList = (List<Object>) list.get(i);
						List<Object> invList = new ArrayList<>();
						for (int j = 0; j < inerList.size(); j++) {
							Map<Object, Object> recMap = new HashMap<>();
							recMap = (Map<Object, Object>) inerList.get(j);
							// recordsList.add(recMap);
							invList.add(recMap);
							// recordsList.add(recMap);

						}
						Map<String, Object> invMap = new HashMap<>();
						invMap.put("inv", invList);
						recordsList.add(invMap);
					}
				}

			}
			Map<String, Object> result = new HashMap<>();
			result.put("record", recordsList);
			result.put("ttl_record", respMap.get(Gstr1ConstantsV31.JSON_TTL_RCRD));
			result.put("gstin", data.get(Gstr1ConstantsV31.INPUT_GSTN));
			result.put("fp", data.get(Gstr1ConstantsV31.INPUT_FP));
			result.put("uTime", respMap.get("utime"));
			result.put(Gstr1ConstantsV31.JSON_TYPE_SEC,data.get(Gstr1ConstantsV31.INPUT_SECTION) );
			// result.put("sname", "");
			// result.put("gt", "");
			return result;
		}
	}
}
