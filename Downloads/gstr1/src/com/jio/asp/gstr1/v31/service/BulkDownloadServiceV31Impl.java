/**
 * 
 */
package com.jio.asp.gstr1.v31.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.jio.asp.gstr1.v30.util.AESEncryption;
import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.exception.GspExceptionV31;
import com.jio.asp.gstr1.v31.exception.RestExceptionHandlerV31;
import com.jio.asp.gstr1.v31.util.BodyV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;
import com.jio.asp.validation.ErrorCodes;

/**
 * @author Amit1.Dwivedi
 *
 */
@Service
public class BulkDownloadServiceV31Impl implements BulkDownloadServiceV31 {

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
	@Autowired
	Environment evn;

	private static List<String> filterList;
	public static final Logger log = LoggerFactory.getLogger(BulkDownloadServiceV31Impl.class);
	static {
		filterList = new ArrayList<>();
		filterList.add(Gstr1ConstantsV31.HEADER_CLIENT_ID);
		filterList.add(Gstr1ConstantsV31.HEADER_SECRET);
	}

	/**
	 * common method to prepare the header required for GSP call.
	 * 
	 * @param headerData,
	 *            header data received
	 * @return header map
	 */
	private Map<String, String> prepareHeader(Map<String, String> reqHeaderMap, String action, String token) {
		log.debug("prepareHeader method : START");
		Map<String, String> gspHeaders = new HashMap<>();

		Iterator<Map.Entry<String, String>> it = reqHeaderMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pair = it.next();
			if (!filterList.contains(pair.getKey())) {
				gspHeaders.put(pair.getKey(), reqHeaderMap.get(pair.getKey()));
			}
		}
		gspHeaders.putAll(reqHeaderMap);

		String apiVer = gstnResource.getMessage(Gstr1ConstantsV31.GSP_HEADER_API_VER, null,
				LocaleContextHolder.getLocale());
		String aspId = gstnResource.getMessage(Gstr1ConstantsV31.GSP_HEADER_ASP_ID, null,
				LocaleContextHolder.getLocale());
		String aspLicKey = gstnResource.getMessage(Gstr1ConstantsV31.GSP_HEADER_ASP_LICENSE_KEY, null,
				LocaleContextHolder.getLocale());
		gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_API_VER, apiVer);
		gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_ASP_LICENSE_KEY, aspLicKey);
		gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_ASP_ID, aspId);
		gspHeaders.put(Gstr1ConstantsV31.GSP_HEADER_DEVICE_STRING, reqHeaderMap.get(Gstr1ConstantsV31.HEADER_SRC_DEV));
		gspHeaders.put(Gstr1ConstantsV31.TOKEN, token);
		gspHeaders.put(Gstr1ConstantsV31.INPUT_GSTN_ACTION, action);

		log.debug("prepareHeader method : END");
		return gspHeaders;
	}

	/**
	 * this method is used to call gsp url, this is a generic method which will
	 * be used for all the calls. for all the method types like POST, GET, PUT
	 * etc.
	 * 
	 * @param url,
	 *            url to be called
	 * @param gspHeaders,
	 *            gsp header required
	 * @param method
	 *            http method, get, post, put
	 * @param body,
	 *            body object will be needed if caller wants to pass body in the
	 *            request. mainly it will needed in method type POST and PUT.
	 *            e.g. code, If the type of object to be passed in entity is
	 *            String BodyV31<String> body =new BodyV31<>();
	 *            body.setRequestBody(<value of body to be passed>);
	 * @return Response map containing results.
	 */
	private <T> Map<String, Object> callGsp(String url, Map<String, String> gspHeaders, HttpMethod method,
			BodyV31<T> body) {
		log.debug("callGsp method : START");
		HttpHeaders headers = new HttpHeaders();
		headers.setAll(gspHeaders);
		HttpEntity<T> entity;
		if ((HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method)) && body != null) {
			entity = new HttpEntity<>(body.getRequestBody(), headers);
		} else {
			entity = new HttpEntity<>(headers);
		}

		SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();

		int readTimeOut = Integer
				.parseInt(gstnResource.getMessage("gstn-read-timeout", null, LocaleContextHolder.getLocale()));
		int connectionTimeOut = Integer
				.parseInt(gstnResource.getMessage("gstn-connection-timeout", null, LocaleContextHolder.getLocale()));

		rf.setReadTimeout(readTimeOut);
		rf.setConnectTimeout(connectionTimeOut);
		log.debug("callGsp method : just before calling the GSP for GSTN data");
		log.info("callGsp method : just before calling the GSP for GSTN data");

		restTemplate.setErrorHandler(new RestExceptionHandlerV31());
		ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {
		};
		ResponseEntity<Map<String, Object>> main = restTemplate.exchange(url, method, entity, typeRef);
		Map<String, Object> responseMap = null;
		if (main != null && main.getStatusCode() == HttpStatus.OK && main.hasBody()) {
			responseMap = main.getBody();
		}
		log.debug("callGsp method : END");
		return responseMap;
	}

	@Override
	public Map<String, String> getUrlList(Map<String, String> data, Map<String, String> headerData, String token) {
		log.debug("initiateGspCallForL3 method : START");
		Map<String, String> responseMap = null;
		log.debug("initiateGspCallForL3 method : inserting the metadata log in the database");
		log.debug("initiateGspCallForL3 method : success in inserting metadata log in the database");
		log.info("initiateGspCallForL3 method : success in inserting metadata log in the database");

		String gspGstr1Baseurl = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_BULK_DATA_URL_LIST, null,
				LocaleContextHolder.getLocale());
		String action = evn.getProperty(Gstr1ConstantsV31.FILE_URLS_ACTION);
		StringBuilder baseUrl = new StringBuilder(gspGstr1Baseurl).append("?action=").append(action)
				.append("&ret_period=").append(data.get("fp")).append("&gstin=").append(data.get("gstin"))
				.append("&token=").append(token);
		// preparing gsp header data needed for GSP call.
		Map<String, String> gspHeaders = prepareHeader(headerData, Gstr1ConstantsV31.FILE_URLS_ACTION, token);
		Map<String, Object> tempMap = callGsp(baseUrl.toString(), gspHeaders, HttpMethod.GET, null);

		log.debug("getB2bData method : converting GSTN data to map of string");
		log.info("getB2bData method : converting GSTN data to map of string");
		if (tempMap != null && tempMap.containsKey(Gstr1ConstantsV31.RESP_STATUS_CODE)
				&& (Gstr1ConstantsV31.RESP_SUCCESS_CODE.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE)))
				|| (Gstr1ConstantsV31.RESP_SUCCESS_CODE_BULK.equals(tempMap.get(Gstr1ConstantsV31.RESP_STATUS_CODE)))) {
			responseMap = tempMap.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
					.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
		} else if (tempMap != null
				&& ((Gstr1ConstantsV31.RESP_ERROR_CODE_RET13508.equals(tempMap.get(Gstr1ConstantsV31.ERROR_CD_STRING)))
						|| (Gstr1ConstantsV31.RESP_ERROR_CODE_RTN_25
								.equals(tempMap.get(Gstr1ConstantsV31.ERROR_CD_STRING))))  ) {
			log.debug("getL2 method : Error response received from GSTN, processing it");
			Map<String, String> respMap = new HashMap<>();
			respMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP006);
			respMap.put(Gstr1ConstantsV31.RESPONSE_DEV_MSG, (String) tempMap.get("message") + ". Please try one more time ");
			respMap.put(Gstr1ConstantsV31.RESPONSE_USR_MSG, (String) tempMap.get("message") + ". Please try one more time ");
			respMap.put(Gstr1ConstantsV31.RESPONSE_USR_ACT, (String) tempMap.get("message") + ". Please try one more time ");
//			respMap.put(Gstr1ConstantsV31.RESPONSE_ERR_GRP, (String) Gstr1ConstantsV31.ASP_GSTR1_GSTN_GET);
//			throw new GspExceptionV31("Error in Gsp Data", null, false, false, respMap);
			
		return respMap;
		}
		
		
		else if((Gstr1ConstantsV31.RESP_ERROR_CODE_RTN_24
								.equals(tempMap.get(Gstr1ConstantsV31.ERROR_CD_STRING))) ||(Gstr1ConstantsV31.RESP_ERROR_CODE_RTN_25
										.equals(tempMap.get(Gstr1ConstantsV31.ERROR_CD_STRING)))){
			log.debug("getL2 method : Error response received from GSTN, processing it");
			Map<String, String> respMap = new HashMap<>();
//			respMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP003);
//			respMap.put(Gstr1ConstantsV31.ERROR_DESC, (String) tempMap.get("message"));
//			respMap.put(Gstr1ConstantsV31.ERROR_GRP, (String) Gstr1ConstantsV31.ASP_GSTR1_GSTN_GET);
			
			respMap.put(Gstr1ConstantsV31.RESPONSE_STATUS_CD, (String) Gstr1ConstantsV31.PROCESSING);
			respMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, (String) tempMap.get("message"));
			
//			throw new GspExceptionV31("Error in Gsp Data", null, false, false, respMap);
			return respMap;
		}else{
			log.debug("getL2 method : Error response received from GSTN, processing it");
			Map<String, String> respMap = new HashMap<>();
			respMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP006);
			respMap.put(Gstr1ConstantsV31.RESPONSE_DEV_MSG, (String) tempMap.get("message") + ". Please try one more time ");
			respMap.put(Gstr1ConstantsV31.RESPONSE_USR_MSG, (String) tempMap.get("message") + ". Please try one more time ");
			respMap.put(Gstr1ConstantsV31.RESPONSE_USR_ACT, (String) tempMap.get("message") + ". Please try one more time ");
//			respMap.put(Gstr1ConstantsV31.RESPONSE_ERR_GRP, (String) Gstr1ConstantsV31.ASP_GSTR1_GSTN_GET);
//			throw new GspExceptionV31("Error in Gsp Data", null, false, false, respMap);
			
		return respMap;
		}
		
		log.debug("getB2bData method : END");

		return responseMap;

	}

	@Override
	public Map<String, String> getBulkDataByUrl(Map<String, String> data, Map<String, String> headerData,
			Map<String, Object> url) {
		log.debug("initiateGspCallForL3 method : START");
		Map<String, String> responseMap = null;
		log.debug("initiateGspCallForL3 method : inserting the metadata log in the database");
		log.debug("initiateGspCallForL3 method : success in inserting metadata log in the database");
		log.info("initiateGspCallForL3 method : success in inserting metadata log in the database");
		String section = (String) data.get(Gstr1ConstantsV31.INPUT_SECTION);

		String gspGstr1Baseurl = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_BULK_DATA_FILE_URL, null,
				LocaleContextHolder.getLocale());
		StringBuilder baseUrl = new StringBuilder(gspGstr1Baseurl).append(Gstr1ConstantsV31.FILE_LOAD_URL);
		// preparing gsp header data needed for GSP call.
		
		Map<String, String> gspHeaders = prepareHeaderForBulk(headerData, Gstr1ConstantsV31.FILE_URLS_ACTION);
		
		// to set body
				JSONObject jsonObject2 = new JSONObject(url);
				BodyV31<String> body = new BodyV31<>();
				body.setRequestBody(jsonObject2.toString());
		
		
		Map<String, Object> tempMap = callGsp(baseUrl.toString(), gspHeaders, HttpMethod.POST, body);

		log.debug("getB2bData method : converting GSTN data to map of string");
		log.info("getB2bData method : converting GSTN data to map of string");
		if (tempMap != null ) {
			responseMap = tempMap.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
					.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
		} else {
			log.debug("getL2 method : Error response received from GSTN, processing it");
			Map<String, Object> respMap = new HashMap<>();
//			respMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP003);
//			respMap.put(Gstr1ConstantsV31.ERROR_DESC, (String) tempMap.get("message"));
//			respMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_GSTN_GET);
			
		//	respMap.put(Gstr1ConstantsV31.RESPONSE_DEV_MSG, (String) tempMap.get("message"));
			respMap.put(Gstr1ConstantsV31.RESPONSE_STATUS_CD, (String) Gstr1ConstantsV31.PROCESSING);
			respMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, (String) tempMap.get("message"));
			
		//	throw new GspExceptionV31("Error in Gsp Data", null, false, false, respMap);

		}
		log.debug("getB2bData method : END");

		return responseMap;

	}

	private Map<String, String> prepareHeaderForBulk(Map<String, String> reqHeaderMap, String fileUrlsAction) {
		log.debug("prepareHeader method : START");
		Map<String, String> gspHeaders = new HashMap<>();

		Iterator<Map.Entry<String, String>> it = reqHeaderMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pair = it.next();
			if (!filterList.contains(pair.getKey())) {
				gspHeaders.put(pair.getKey(), reqHeaderMap.get(pair.getKey()));
			}
		}
		gspHeaders.putAll(reqHeaderMap);

//		gspHeaders.put(Gstr1ConstantsV31.BULK_CLIENT_ID, "GSPCRQL12SZW89T");
//		gspHeaders.put(Gstr1ConstantsV31.BULK_CLIENT_KEY, "e05f879f-78aa-4dca-84bc-b594f61e6f16");
		gspHeaders.put(Gstr1ConstantsV31.BULK_CLIENT_ID, evn.getProperty(Gstr1ConstantsV31.BULK_CLIENT_ID));
		gspHeaders.put(Gstr1ConstantsV31.BULK_CLIENT_KEY,evn.getProperty(Gstr1ConstantsV31.BULK_CLIENT_KEY));

		log.debug("prepareHeader method : END");
		return gspHeaders;
	}

	@Override
	public Map<String, Object> bulkDataDecription(String data, String ek) {

		byte[] decodeEK = null;
		try {
			decodeEK = AESEncryption.decodeBase64StringTOByte(ek);
		} catch (Exception e) {
			Map<String, Object> respMap = new HashMap<>();
			respMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP004);
			respMap.put(Gstr1ConstantsV31.ERROR_DESC, "BULK FILE DOWNLOAD DECRIPTION KEY ERROR");
			respMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_GSTN_GET);
			throw new GspExceptionV31("Error in Gsp Data", null, false, false, respMap);
		}

		String jsonData = null;
		try {
			jsonData = new String(
					AESEncryption.decodeBase64StringTOByte(new String(AESEncryption.decrypt(data, decodeEK))));
		} catch (Exception e) {
			Map<String, Object> respMap = new HashMap<>();
			respMap.put(Gstr1ConstantsV31.ERROR_CODE, ErrorCodesV31.GSP005);
			respMap.put(Gstr1ConstantsV31.ERROR_DESC, "BULK FILE DOWNLOAD DECRIPTION DATA ERROR");
			respMap.put(Gstr1ConstantsV31.ERROR_GRP, Gstr1ConstantsV31.ASP_GSTR1_GSTN_GET);
			throw new GspExceptionV31("Error in Gsp Data", null, false, false, respMap);
		}

		Gson gson = new Gson();

		Map<String, Object> response = gson.fromJson(jsonData, Map.class);

		return response;

	}

}
