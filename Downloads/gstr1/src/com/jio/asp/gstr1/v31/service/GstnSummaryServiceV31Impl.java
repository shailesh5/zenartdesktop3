/**
 * 
 */
package com.jio.asp.gstr1.v31.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.exception.GspExceptionV31;
import com.jio.asp.gstr1.v31.util.AESEncryptionV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;

/**
 * @author amit1.dwivedi
 *
 */
@Service
public class GstnSummaryServiceV31Impl implements GstnSummaryServiceV31 {

	@Autowired
	private MessageSource messageSourceV31;
	@Autowired
	private MessageSource gstnResource;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	RedisCacheServiceV31 redisCacheService;
	@Autowired
	private GSPServiceV31 gspService;

	public static final Logger log = LoggerFactory.getLogger(GstnSummaryServiceV31Impl.class);

	@Override
	public Map<String, String> validateApiInput(Map<String, String> inputMap,String grpName) {
		CommonUtil.validateEmptyString(MapUtils.getString(inputMap,Gstr1ConstantsV31.HEADER_GSTIN,""),
				ErrorCodesV31.ASP011031, grpName, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_FP,""),
				ErrorCodesV31.ASP011311, grpName, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_LEVEL,""),
				ErrorCodesV31.ASP011181, grpName, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
		log.debug("validateApiInput method : END");
		return inputMap;
	}
	
	@Override
	public Map<String, String> validateApiInputL2(Map<String, String> inputMap,String grpName) {
		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_SECTION,""),
				ErrorCodesV31.ASP011179, grpName, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
		return inputMap;
	}
	

	@Override
	public Map<String, Object> processGstr1InvoiceDataL2(Map<String, String> headers, Map<String, String> data) {

		log.debug("processGstr1SummaryDataL2 method : START");
		Map<String, Object> responseMap = new HashMap<>();
		String redishKey = data.get(Gstr1ConstantsV31.INPUT_GSTN) + data.get(Gstr1ConstantsV31.INPUT_SECTION)
				+ data.get(Gstr1ConstantsV31.INPUT_FP)+Gstr1ConstantsV31.API_NAME;
		data.put(Gstr1ConstantsV31.INPUT_ACK_NO, redishKey);
		boolean result = redisCacheService.chekcKey(data.get(Gstr1ConstantsV31.INPUT_ACK_NO));
		log.info("Summary L2 redis key generated for query ", redishKey);

		// asp call, making call to database to get the ASP data
		log.info("processGstr1InvoiceDataL2 Method - ASP Data retrival going to start - STEP4");
		if (!result) {
			Map<String, Object> gstr1Summary = getGstr1SummaryInvoiceDataL2(headers, data);
			log.info("GSTN-Summary API call - GSP Data retrival complete - STEP3");
			String key = prepareGspDataForCashR(gstr1Summary, headers, data);
			log.info("GSTN-Summary API call - GSP Data parsing and preparation complete - STEP4");
		}
		// if (data.get(Gstr1ConstantsV31.INPUT_ACK_NO) == null) {
		//
		// data.put(Gstr1ConstantsV31.INPUT_ACK_NO, key);
		// }

		log.info("GSTN-Summary API call - GSP Data parsing and preparation complete - STEP4");
		// business logic step and response preparation step
		responseMap = prepareAndFormatResponseL2(data);

		log.info("GSTN-Summary API call - GSP data response preparation complete - STEP5");

		log.debug("processGstnSummaryL3 method : END");
		return responseMap;
	}

	private Map<String, Object> prepareAndFormatResponseL2(Map<String, String> data) {
		Map<String, Object> responseMap = new HashMap<>();
		Map<Object, Object> dataMap = getAspInvoiceDataL2(data);
		if (MapUtils.isNotEmpty(dataMap)) {
			log.info("processGstr1InvoiceDataL2 Method - ASP Data parsing and preparation complete - STEP6");
			// business logic step and response preparation step
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
			log.info("processGstr1InvoiceDataL2 Method - Preparing the API response - STEP7");
		} else {
			dataMap = new HashMap<>();
			responseMap = new HashMap<>();
			dataMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, "No data found for the given criteria");
			dataMap.put(Gstr1ConstantsV31.RESP_STATUS_CODE, Gstr1ConstantsV31.ERROR);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			responseMap.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, data);
		}
		log.debug("processGstr1SummaryDataL2 method : END");
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
	public Map<Object, Object> getAspInvoiceDataL2(Map<String, String> data) {
		log.debug("getAspInvoiceDataL2 method : START");
		int limit;
		int offset;
		try {
			limit = Integer.parseInt(data.get(Gstr1ConstantsV31.INPUT_LIMIT));
		} catch (NumberFormatException e) {
			limit = 10;
		}
		try {
			offset = Integer.parseInt(data.get(Gstr1ConstantsV31.INPUT_OFFSET));
		} catch (NumberFormatException e) {
			offset = 0;
		}
		Set<Object> hk = new LinkedHashSet<>();
		for (int i = offset; i < limit+offset; i++)
			hk.add(i + "");
		Map<Object, Object> map = redisCacheService.getPagedData(data, hk);
		data.remove(Gstr1ConstantsV31.INPUT_ACK_NO);
		log.debug("getAspInvoiceDataL2 method : END");
		return map;
	}

	private Map<String, Object> getGstr1SummaryInvoiceDataL2(Map<String, String> headerData, Map<String, String> data) {
		log.debug("getGstr1InvoiceData method : START");
		log.debug("getGstr1InvoiceData method : before calling GSP for gstn data");

		// making call to gstr1 thru GSP.
		Map<String, String> sectionMap = gspService.getL2(data, headerData);

		// parsing GSP data to POJO
		Map<String, Object> gstr1Summary = parseGspData(sectionMap, headerData);

		log.debug("getGstr1InvoiceData method : END");
		return gstr1Summary;
	}

	/**
	 * Method takes the data from getGstr1InvoiceData method and creates java
	 * bean mapping for each of the items. This method will decrypt the data
	 * using appkey --> sek --> rek --> data
	 * 
	 * @param gspData
	 *            Map containing GSP data
	 * @param data
	 *            user input data
	 * @return Map containing parsed GSP data
	 */
	private Map<String, Object> parseGspData(Map<String, String> gspData, Map<String, String> data) {
		log.debug("parseGspData method : START");
		Map<String, Object> gstr1Summary = new HashMap<String, Object>();
		if (gspData.containsKey(Gstr1ConstantsV31.INPUT_REK) && gspData.containsKey(Gstr1ConstantsV31.INPUT_DATA)) {
			String respRek = gspData.get(Gstr1ConstantsV31.INPUT_REK);
			String respData = gspData.get(Gstr1ConstantsV31.INPUT_DATA);
			byte[] authEK;
			try {
				authEK = AESEncryptionV31.decrypt((String) data.get(Gstr1ConstantsV31.INPUT_SEK),
						AESEncryptionV31.decodeBase64StringTOByte((String) data.get(Gstr1ConstantsV31.INPUT_APP_KEY)));

				log.debug("parseGspData method : Successfully Parsed SEK");

				byte[] apiEK = AESEncryptionV31.decrypt(respRek, authEK);
				log.debug("parseGspData method : Successfully Parsed REK");

				String jsonData = new String(AESEncryptionV31
						.decodeBase64StringTOByte(new String(AESEncryptionV31.decrypt(respData, apiEK))));
				log.debug("parseGspData method : Successfully Parsed GSP JSon Response");
				ObjectMapper mapper = new ObjectMapper();
				// mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
				// true);
				// for Stubs only remove once hit Gstn
				jsonData = new String(AESEncryptionV31
						.decodeBase64StringTOByte(new String(AESEncryptionV31.decrypt(respData, apiEK))));
			//	Gson gson = new Gson();

				//Map<String, Object> responseMap = gson.fromJson(jsonData, Map.class);
				//if (!responseMap.isEmpty())
					//return responseMap;

				// fro Stub only
				gstr1Summary = mapper.readValue(jsonData, new TypeReference<Map<Object, Object>>() {
				});

				//System.out.println(gstr1Summary);
				log.debug("parseGspData method : after converting response from GSP for gstn data");

			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException e) {
				String errorMsg = messageSourceV31.getMessage("GSTR1_PARSE_ASP201", null, LocaleContextHolder.getLocale());
				Map<String, Object> excObj = new HashMap<>();
				excObj.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSTR1_PARSE_GSP_ERROR);
				excObj.put(Gstr1ConstantsV31.ERROR_DESC, errorMsg);
				// excObj.put(Gstr1ConstantsV31.ERROR_GRP,
				// Gstr1ConstantsV31.ASP_GSTR1_SUMMARY_GRP);
				excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
				log.error("parseGspData method : Error Occured while parsing the GSP response, error occurred. {}", e);
				throw new GspExceptionV31(errorMsg, e, false, false, excObj);
			} catch (Exception e) {
				String errorMsg = messageSourceV31.getMessage("GSTR1_PARSE_ASP202", null, LocaleContextHolder.getLocale());
				Map<String, Object> excObj = new HashMap<>();
				excObj.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSTR1_PARSE_GSP_GEN_ERROR);
				excObj.put(Gstr1ConstantsV31.ERROR_DESC, errorMsg);
				// excObj.put(Gstr1ConstantsV31.ERROR_GRP,
				// Gstr1ConstantsV31.ASP_GSTR1_SUMMARY_GRP);
				excObj.put(Gstr1ConstantsV31.ERROR_HTTP_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
				log.error(
						"parseGspData method : Unknown Error Occured while parsing the GSP response, error occurred. {}",
						e);
				throw new GspExceptionV31(errorMsg, e, false, false, excObj);
			}
		}
		log.debug("parseGspData method : END");
		return gstr1Summary;
	}

	private String prepareGspDataForCashR(Map<String, Object> gstr1Summary, Map<String, String> headers,
			Map<String, String> data) {
		Map<String, Object> responseMap = new HashMap<>();
		String section = (String) data.get(Gstr1ConstantsV31.INPUT_SECTION);
		if (Gstr1ConstantsV31.TYPE_NIL.equals(section)) {
			List<Object> invList = new ArrayList<>();
			Map<Object, Object> b2bData = (Map<Object, Object>) gstr1Summary.get(section);
			if (b2bData.containsKey("inv")) {
				Map<Object, Object> dataMap = new HashMap<>();
				invList = (List<Object>) b2bData.get("inv");
				for (int i = 0; i < invList.size(); i++) {

					dataMap = (Map<Object, Object>) invList.get(i);

				}
				responseMap.put("0", dataMap);
			}

		} else if (Gstr1ConstantsV31.TYPE_B2CS.equals(section) || Gstr1ConstantsV31.TYPE_B2CSA.equals(section)) {
			Map<String, Object> invMap = new HashMap<>();
			List<Object> b2bData = (List<Object>) gstr1Summary.get(section);
			for (int i = 0; i < b2bData.size(); i++) {
				invMap = (Map<String, Object>) b2bData.get(i);
			}
			responseMap.put("0", invMap);
		} else if (Gstr1ConstantsV31.TYPE_AT.equals(section) || Gstr1ConstantsV31.TYPE_ATA.equals(section)
				|| Gstr1ConstantsV31.TYPE_CDNUR.equals(section)|| Gstr1ConstantsV31.TYPE_CDNR.equals(section)) {
			List<Object> invList = new ArrayList<>();
			List<Object> b2bData = (List<Object>) gstr1Summary.get(section);
			for (int i = 0; i < b2bData.size(); i++) {
				Map<String, Object> invMap = (Map<String, Object>) b2bData.get(i);

				responseMap.put(String.valueOf(i), invMap);
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

		else if (StringUtils.containsIgnoreCase(section, Gstr1ConstantsV31.TYPE_HSN)) {
			List<Object> invList = new ArrayList<>();
			Map<Object, Object> b2bData = (Map<Object, Object>) gstr1Summary.get(Gstr1ConstantsV31.TYPE_HSN);
			if (b2bData.containsKey("data")) {
				Map<Object, Object> dataMap = new HashMap<>();
				invList = (List<Object>) b2bData.get("data");
				for (int i = 0; i < invList.size(); i++) {
					dataMap = (Map<Object, Object>) invList.get(i);
					responseMap.put(String.valueOf(i), invList);
				}
			}

		}

		else if (Gstr1ConstantsV31.TYPE_DOCS_L2.equals(section)) {
			List<Object> invList = new ArrayList<>();
			Map<Object, Object> b2bData = (Map<Object, Object>) gstr1Summary.get(Gstr1ConstantsV31.TYPE_DOCS);
			String chksum = (String) b2bData.get("chksum");
			if (b2bData.containsKey("doc_det")) {
				Map<Object, Object> dataMap = new HashMap<>();
				invList = (List<Object>) b2bData.get("doc_det");
				for (int i = 0; i < invList.size(); i++) {
					dataMap = (Map<Object, Object>) invList.get(i);
					dataMap.put("chksum", chksum);
					responseMap.put(String.valueOf(i), dataMap);
				}
			}

		} else {

			List<Object> b2bData = (List<Object>) gstr1Summary.get(section);
			int hk = 0;
			for (int i = 0; i < b2bData.size(); i++) {

				Map<String, Object> invMap = (Map<String, Object>) b2bData.get(i);
				if (invMap.containsKey("inv")) {
					List<Object> invList = new ArrayList<>();
					invList = (List<Object>) invMap.get("inv");
					String ctin = (String) invMap.get("ctin");
					String cfs = (String) invMap.get("cfs");

					for (int j = 0; j < invList.size(); j++) {
						List l2 = new ArrayList<>();
						Map invInnerMap = (Map) invList.get(j);
						invInnerMap.put("ctin", ctin);
						invInnerMap.put("cfs", cfs);
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
		String key = redisCacheService.saveAll(responseMap, data);
		return key;

	}

	@Override
	public Map<String, Object> processGstr1InvoiceDataL0(Map<String, String> headers, Map<String, String> data) {

		log.debug("processGstr1SummaryDataL0 method : START");
		Map<String, Object> responseMap = new HashMap<>();
			Map<String, Object> gstr1Summary = getGstr1SummaryInvoiceDataL0(headers, data);
			log.info("GSTN-Summary API call - GSP Data retrival complete - STEP3");
			log.info("GSTN-Summary API call - GSP Data retrival complete - STEP3");
				responseMap = prepareGspDataForComparison(gstr1Summary, data);
				log.info("GSTN-Summary API call - GSP Data parsing and preparation complete - STEP4");
				// business logic step and response preparation step
				// responseMap = prepareAndFormatResponseL0(gspData, inputMap);
				log.info("GSTN-Summary API call - GSP data response preparation complete - STEP5");
			log.debug("processGstr1InvoiceData method : END");
		return responseMap;
	}

	private Map<String, Object> getGstr1SummaryInvoiceDataL0(Map<String, String> headers, Map<String, String> data) {
		log.debug("getGstr1InvoiceData method : START");
		log.debug("getGstr1InvoiceData method : before calling GSP for gstn data");

		// making call to gstr1 thru GSP.
		Map<String, String> sectionMap = gspService.getL0(data, headers);

		// parsing GSP data to POJO
		Map<String, Object> gstr1Summary = parseGspData(sectionMap, headers);

		log.debug("getGstr1InvoiceData method : END");
		return gstr1Summary;
	}
	
	/**
	 * Method takes GSP data and creates comparison set, which is used later
	 * stage to compare against ASP data.
	 * 
	 * @param gspBeanData
	 *            parsed GSP data
	 * @param data
	 *            user input data
	 * @return Map containing data like inv count, tax liability, tax value,
	 *         total invoice values
	 */
	private Map<String, Object> prepareGspDataForComparison(Map<String, Object> gspBeanData, Map<String, String> data) {
		log.debug("prepareGspDataForComparison method : START");
		Map<String, Object> responseMap = new HashMap<>();
		List<Map<String, Object>> sectionList = new ArrayList<>();
		sectionList.add(gspBeanData);
		boolean dataPresent = false;
		if (gspBeanData != null) {
			log.debug("prepareGspDataForComparison method : GSP data received is not null");
			List<Object> gstr1SummaryMainList = (List<Object>) gspBeanData.get("sec_sum");
//			if (!gstr1SummaryMainList.isEmpty()) {
//				dataPresent = true;
//				for(int i=0;i<gstr1SummaryMainList.size();i++){
//					Map<String, Object>gstr1SecSummary= (Map<String, Object>) gstr1SummaryMainList.get(i);
////					for (Map.Entry<String, Object> sectionItem : gstr1SecSummary.entrySet()) {
//						Map<String, Object> gstnMap = new HashMap<>();
//						if (!gstr1SecSummary.isEmpty()) {
//							Map<String, Object> gstnSectionMap;
//							log.debug(
//									"prepareGspDataForComparison method : section item from GSP is not null, so preparing the comparison data");
//							log.info(
//									"prepareGspDataForComparison method : section item from GSP is not null, so preparing the comparison data");
//							String type = (String) gstr1SecSummary.get("sec_nm");
//							if(type.equalsIgnoreCase(Gstr1ConstantsV31.TYPE_NIL)){
//								gstnMap.put(Gstr1ConstantsV31.RESP_TTL_COUNT, gstr1SecSummary.getOrDefault("ttl_rec",0.0));
//								gstnMap.put(Gstr1ConstantsV31.INV_NILSUP_AMT, gstr1SecSummary.getOrDefault("ttl_nilsup_amt",0.0));
//								gstnMap.put(Gstr1ConstantsV31.INV_EXPT_AMT,gstr1SecSummary.getOrDefault("ttl_expt_amt",0.0));
//								gstnMap.put(Gstr1ConstantsV31.INV_NGSUP_AMT, gstr1SecSummary.getOrDefault("ttl_ngsup_amt",0.0));
//								
//							}else if(type.equalsIgnoreCase(Gstr1ConstantsV31.TYPE_DOCS)){
//								gstnMap.put(Gstr1ConstantsV31.RESP_TTL_COUNT, gstr1SecSummary.getOrDefault("ttl_rec",0.0));
//								gstnMap.put(Gstr1ConstantsV31.INV_DOC_ISSUED, gstr1SecSummary.getOrDefault("ttl_doc_issued",0.0));
//								gstnMap.put(Gstr1ConstantsV31.INV_DOC_CANCELLED,gstr1SecSummary.getOrDefault("ttl_doc_cancelled",0.0));
//								gstnMap.put(Gstr1ConstantsV31.INV_NET_DOC_ISSUED, gstr1SecSummary.getOrDefault("net_doc_issued",0.0));
//								
//							}else
//							{
//							gstnMap.put(Gstr1ConstantsV31.RESP_TTL_COUNT, gstr1SecSummary.getOrDefault("ttl_rec",0.0));
//							gstnMap.put(Gstr1ConstantsV31.RESP_TTL_VAL, gstr1SecSummary.getOrDefault("ttl_val",0.0));
//							gstnMap.put(Gstr1ConstantsV31.RESP_TTL_TXVAL,gstr1SecSummary.getOrDefault("ttl_tax",0.0));
//							gstnMap.put(Gstr1ConstantsV31.RESP_INV_IGST, gstr1SecSummary.getOrDefault("ttl_igst",0.0));
//							gstnMap.put(Gstr1ConstantsV31.RESP_INV_SGST,gstr1SecSummary.getOrDefault("ttl_sgst",0.0));
//							gstnMap.put(Gstr1ConstantsV31.RESP_INV_CGST, gstr1SecSummary.getOrDefault("ttl_cgst",0.0));
//						
//							}
//							gstnMap.put(Gstr1ConstantsV31.RESP_SECT_NM,type.toLowerCase());
//							if(!type.endsWith("A")){
//								sectionList.add(gstnMap);
//							}
//							
//						}
//					}
//				}
				
//			}
		}		
			
		responseMap = formatOutputList(data, sectionList);
		return responseMap;
	}

	private Map formatOutputList(Map<String, String> allParams, List<Map<String, Object>> sectionList) {

		Map<String, Object> map = new HashMap();
		List<Object> ls = new ArrayList<>();
		if(sectionList != null && sectionList.size() > 0){
				ls.addAll(sectionList);
		
		if (ls.size() > 0) {
			map.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, allParams);
			map.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, ls);
		} else {
			Map<String, String> dataMap = new HashMap<>();
			dataMap.put(Gstr1ConstantsV31.CONTROL_REC_STATUS, Gstr1ConstantsV31.ERROR);
			dataMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, "No Data Available for given criteria");
			map.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, allParams);
			map.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
		}
		}else{
			Map<String, String> dataMap = new HashMap<>();
			dataMap.put(Gstr1ConstantsV31.CONTROL_REC_STATUS, Gstr1ConstantsV31.ERROR);
			dataMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, "No Data Available for given criteria");
			map.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, allParams);
			map.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			
		}
		return map;
	}





}
