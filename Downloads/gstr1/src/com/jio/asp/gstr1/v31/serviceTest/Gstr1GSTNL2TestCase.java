package com.jio.asp.gstr1.v31.serviceTest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.service.BulkDownloadServiceV31;
import com.jio.asp.gstr1.v31.service.GSPServiceV31;
import com.jio.asp.gstr1.v31.service.GSPServiceV31Impl;
import com.jio.asp.gstr1.v31.service.GstnL2ServiceV31Impl;

import mockit.Expectations;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;

public class Gstr1GSTNL2TestCase {

	@Tested
	GstnL2ServiceV31Impl gstnL2ServiceV31Impl;

	@Tested
	private GSPServiceV31Impl gSPServiceImplV31;

	@Injectable
	private GSPServiceV31 mockGspService;

	@Injectable
	private AspMongoDaoV31 mockAspMongoDao;

	@Injectable
	private MessageSource gstnResource;

	@Injectable
	private BulkDownloadServiceV31 mockBulkDownloadServiceV31;

	/**
	 * 
	 */
	@Test
	public void testFlushFalseWithData() {

		Map<String, String> headerMap = prepareHeaders();
		Map<String, String> inputMap = prepareInputs();

		String mongoL2Key = Gstr1ConstantsV31.API_NM + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_GSTN) + ":"
				+ inputMap.get(Gstr1ConstantsV31.INPUT_SECTION) + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_FP);
		
		String collectionControl = "gstr1L2Control.col";
		String gstnControl = "gstr1L2GstnData.col";

		Map<String,Object> expectedMap = new HashMap<String,Object>();
		expectedMap.put("gstin", "wqee87wq98e798wq7");

		Map<String, Object> expectedMapForMongo =  prepareDbResponse();

		Map<String, Object> object = new HashMap<>();
		object.put("_id", mongoL2Key);

		List<Map<String, Object>> keyData = new ArrayList<Map<String, Object>>();
		Map<String, Object> keyValue = new HashMap<String, Object>();
		Map<String, Object> keyHeader = new HashMap<String, Object>();;
		keyHeader.put("status", "success");
		keyValue.put("header", keyHeader);
		keyData.add(keyValue);
		
		new NonStrictExpectations() {
			{
				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2Control.col";

				mockAspMongoDao.getMongoData(object, collectionControl);
				result = keyData;

				mockAspMongoDao.getL2Data(inputMap, gstnControl);
				result = expectedMapForMongo;
			}
		};

		Map<String, Object> respMap = gstnL2ServiceV31Impl.processL2(headerMap, inputMap);

		Map successData = (HashMap)respMap.get("data");
		assertEquals(successData.get("gstin"), "27GSPMH0782G1ZJ");
	}

	@Test
	public void testFlushFalseWithNoDataTokenResponse() {

		Map<String, String> headerData = prepareHeaders();
		Map<String, String> inputMap = prepareInputs();
		Map<String,String> gspresponse = prepareEncryptGSTNResponseForToken();
		Map<String,Object> gstnResponse = prepareGSTNResponse();
		Map<String, Object> dycrptDataToken = new HashMap<String, Object>();

		dycrptDataToken.put("token","token");
		String mongoL2Key = Gstr1ConstantsV31.API_NM + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_GSTN) + ":"
				+ inputMap.get(Gstr1ConstantsV31.INPUT_SECTION) + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_FP);

		Map<String, Object> object = new HashMap<>();
		object.put("_id", mongoL2Key);
		String collectionControl = "gstr1L2Control.col";
		String gstnControl = "gstr1L2GstnData.col";

		List<Map<String, Object>> keyData = new ArrayList<Map<String, Object>>();

		Map<String, Object> expectedMapForMongo =  prepareDbResponse();

		new NonStrictExpectations() {
			{
				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2Control.col";

				mockAspMongoDao.getMongoData(object, collectionControl);
				result = null;

				mockAspMongoDao.deleteL2Key(mongoL2Key, gstnControl, collectionControl);

				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				mockGspService.getL2(inputMap,headerData);
				result = gspresponse;

				mockGspService.decryptResponse(headerData,gspresponse);
				result=dycrptDataToken;

				mockAspMongoDao.saveInMongo(new JSONObject(gstnResponse), gstnControl);

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2Control.col";

				mockAspMongoDao.saveInMongo(new JSONObject(gstnResponse), gstnControl);

				mockAspMongoDao.getL2Data(inputMap, gstnControl);
				result = expectedMapForMongo;
			}
		};

		Map<String, Object> respMap = gstnL2ServiceV31Impl.processL2(headerData, inputMap);

		assertEquals(respMap.get(Gstr1ConstantsV31.ERROR_CODE), ErrorCodesV31.GSP002);
	}

	@Test
	public void testFlushFalseWithNoDataL2Response() {

		Map<String, String> headerMap = prepareHeaders();
		Map<String, String> inputMap = prepareInputs();
		Map<String,String> gstnResponseForData = prepareEncryptGSTNResponseForData();
		Map<String,Object> gstnResponseForDataObj = prepareGSTNResponseForGSTNData();
		
		String mongoL2Key = Gstr1ConstantsV31.API_NM + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_GSTN) + ":"
				+ inputMap.get(Gstr1ConstantsV31.INPUT_SECTION) + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_FP);

		Map<String, Object> object = new HashMap<>();
		object.put("_id", mongoL2Key);
		String collectionControl = "gstr1L2Control.col";
		String gstnControl = "gstr1L2GstnData.col";

		List<Map<String, Object>> keyData = new ArrayList<Map<String, Object>>();

		Map<String,Object> expectedMap = new HashMap<String,Object>();
		expectedMap.put("gstin", "wqee87wq98e798wq7");

		Map<String, Object> expectedMapForMongo =  prepareDbResponse();

		new Expectations() {
			{
				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2Control.col";

				mockAspMongoDao.getMongoData(object, collectionControl);
				result = null;

				mockAspMongoDao.deleteL2Key(mongoL2Key, gstnControl, collectionControl);

				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				mockGspService.getL2(inputMap, headerMap);
				result = gstnResponseForData;

				mockGspService.decryptResponse(headerMap,gstnResponseForData);
				result=gstnResponseForDataObj;

				mockAspMongoDao.saveInMongo((JSONObject) any, gstnControl);

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				mockAspMongoDao.saveInMongo((JSONObject) any, gstnControl);

				mockAspMongoDao.getL2Data(inputMap, gstnControl);
				result = expectedMapForMongo;
			}
		};

		Map<String, Object> respMap = gstnL2ServiceV31Impl.processL2(headerMap, inputMap);
		
		Map successData = (HashMap)respMap.get("data");
		assertEquals(successData.get("record"), successData.get("record"));
	}

	@Test
	public void testFlushTrueWithTokenInDB() {

		Map<String, String> headerMap = prepareHeaders();
		Map<String, String> inputMap = prepareInputsWithToken();
		Map<String,String> gstnResponseForData = prepareEncryptGSTNResponseForData();
		Map<String,Object> gstnResponseForDataObj = prepareGSTNResponseForGSTNData();
		
		Map<String,Object> urlList = getUrlList();
		String mongoL2Key = Gstr1ConstantsV31.API_NM + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_GSTN) + ":"
				+ inputMap.get(Gstr1ConstantsV31.INPUT_SECTION) + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_FP);

		Map<String, Object> object = new HashMap<>();
		object.put("_id", mongoL2Key);
		String collectionControl = "gstr1L2Control.col";
		String gstnControl = "gstr1L2GstnData.col";

		Map<String,Object> expectedMap = new HashMap<String,Object>();
		expectedMap.put("gstin", "wqee87wq98e798wq7");

		List<Map<String, Object>> keyData = new ArrayList<Map<String, Object>>();
		Map<String, Object> keyValue = new HashMap<String, Object>();
		Map<String, Object> keyHeader = new HashMap<String, Object>();;
		keyHeader.put("status", "token");
		keyHeader.put("token", "token");
		keyValue.put("header", keyHeader);
		keyData.add(keyValue);

		Map<String, Object> expectedMapForMongo =  prepareDbResponse();

		new Expectations() {
			{
				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2Control.col";

				mockAspMongoDao.getMongoData(object, collectionControl);
				result = keyData;

				mockBulkDownloadServiceV31.getUrlList(inputMap, headerMap, "token");
				result = gstnResponseForData;

				mockGspService.decryptResponse(headerMap, gstnResponseForData);
				result = urlList;

				mockBulkDownloadServiceV31.getBulkDataByUrl(inputMap, headerMap,(Map<String, Object>) any);
				result = gstnResponseForData;

				mockBulkDownloadServiceV31.bulkDataDecription((String) any,(String) any);
				result = gstnResponseForDataObj;

				mockBulkDownloadServiceV31.getBulkDataByUrl(inputMap, headerMap,(Map<String, Object>) any);
				result = gstnResponseForData;

				mockBulkDownloadServiceV31.bulkDataDecription((String) any,(String) any);
				result = gstnResponseForDataObj;

				mockAspMongoDao.saveInMongo(any, gstnControl);

				mockAspMongoDao.saveInMongo(any, gstnControl);

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				mockAspMongoDao.saveInMongo(any, gstnControl);

				mockAspMongoDao.getL2Data(inputMap, gstnControl);
				result = expectedMapForMongo;
			}
		};

		Map<String, Object> respMap = gstnL2ServiceV31Impl.processL2(headerMap, inputMap);

		Map successData = (HashMap)respMap.get("data");
		assertEquals(successData.get("record"), successData.get("record"));
	}

	@Test
	public void testFlushTrueWithoutData() {

		Map<String, String> headerMap = prepareHeaders();
		Map<String, String> inputMap = prepareInputsWithToken();
		Map<String,String> gstnResponseForData = prepareEncryptGSTNResponseForData();
		Map<String,Object> gstnResponseForDataObj = prepareGSTNResponseForGSTNData();
		
		Map<String,Object> urlList = getUrlList();
		String mongoL2Key = Gstr1ConstantsV31.API_NM + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_GSTN) + ":"
				+ inputMap.get(Gstr1ConstantsV31.INPUT_SECTION) + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_FP);

		Map<String, Object> object = new HashMap<>();
		object.put("_id", mongoL2Key);
		String collectionControl = "gstr1L2Control.col";
		String gstnControl = "gstr1L2GstnData.col";

		Map<String,Object> expectedMap = new HashMap<String,Object>();
		expectedMap.put("gstin", "wqee87wq98e798wq7");

		List<Map<String, Object>> keyData = new ArrayList<Map<String, Object>>();

		Map<String, Object> expectedMapForMongo =  prepareDbResponse();

		new Expectations() {
			{
				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2Control.col";

				mockAspMongoDao.getMongoData(object, collectionControl);
				result = keyData;

				mockAspMongoDao.deleteL2Key(mongoL2Key, gstnControl, collectionControl);

				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				mockGspService.getL2(inputMap, headerMap);
				result = gstnResponseForData;

				mockGspService.decryptResponse(headerMap,gstnResponseForData);
				result=gstnResponseForDataObj;

				mockAspMongoDao.saveInMongo((JSONObject) any, gstnControl);

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				mockAspMongoDao.saveInMongo((JSONObject) any, gstnControl);

				mockAspMongoDao.getL2Data(inputMap, gstnControl);
				result = expectedMapForMongo;
			}
		};

		Map<String, Object> respMap = gstnL2ServiceV31Impl.processL2(headerMap, inputMap);

		Map successData = (HashMap)respMap.get("data");
		assertEquals(successData.get("record"), successData.get("record"));
	}

	@Test
	public void testFlushTrueWithoutTokenWithData() {

		Map<String, String> headerMap = prepareHeaders();
		Map<String, String> inputMap = prepareInputsWithToken();
		Map<String,String> gstnResponseForData = prepareEncryptGSTNResponseForData();
		Map<String,Object> gstnResponseForDataObj = prepareGSTNResponseForGSTNData();
		
		Map<String,Object> urlList = getUrlList();
		String mongoL2Key = Gstr1ConstantsV31.API_NM + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_GSTN) + ":"
				+ inputMap.get(Gstr1ConstantsV31.INPUT_SECTION) + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_FP);

		Map<String, Object> object = new HashMap<>();
		object.put("_id", mongoL2Key);
		String collectionControl = "gstr1L2Control.col";
		String gstnControl = "gstr1L2GstnData.col";

		Map<String,Object> expectedMap = new HashMap<String,Object>();
		expectedMap.put("gstin", "wqee87wq98e798wq7");

		List<Map<String, Object>> keyData = new ArrayList<Map<String, Object>>();
		Map<String, Object> keyValue = new HashMap<String, Object>();
		Map<String, Object> keyHeader = new HashMap<String, Object>();;
		keyHeader.put("status", "success");
		keyValue.put("header", keyHeader);
		keyData.add(keyValue);

		Map<String, Object> expectedMapForMongo =  prepareDbResponse();

		new Expectations() {
			{
				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2Control.col";

				mockAspMongoDao.getMongoData(object, collectionControl);
				result = keyData;

				mockAspMongoDao.deleteL2Key(mongoL2Key, gstnControl, collectionControl);

				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				mockGspService.getL2(inputMap, headerMap);
				result = gstnResponseForData;

				mockGspService.decryptResponse(headerMap,gstnResponseForData);
				result=gstnResponseForDataObj;

				mockAspMongoDao.saveInMongo((JSONObject) any, gstnControl);

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				mockAspMongoDao.saveInMongo((JSONObject) any, gstnControl);

				mockAspMongoDao.getL2Data(inputMap, gstnControl);
				result = expectedMapForMongo;
			}
		};

		Map<String, Object> respMap = gstnL2ServiceV31Impl.processL2(headerMap, inputMap);

		Map successData = (HashMap)respMap.get("data");
		assertEquals(successData.get("record"), successData.get("record"));
	}

	@Test
	public void testFlushTrueWithoutTokenDataTokenInResponse() {

		Map<String, String> headerMap = prepareHeaders();
		Map<String, String> inputMap = prepareInputsWithToken();
		Map<String,String> gstnResponseForData = prepareEncryptGSTNResponseForData();
		Map<String,Object> gstnResponseForDataObj = prepareGSTNResponseForGSTNData();

		Map<String,Object> urlList = getUrlList();
		String mongoL2Key = Gstr1ConstantsV31.API_NM + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_GSTN) + ":"
				+ inputMap.get(Gstr1ConstantsV31.INPUT_SECTION) + ":" + inputMap.get(Gstr1ConstantsV31.INPUT_FP);

		Map<String, Object> dycrptDataToken = new HashMap<String, Object>();
		dycrptDataToken.put("token","token");
		dycrptDataToken.put("est","est");
		Map<String, Object> object = new HashMap<>();
		object.put("_id", mongoL2Key);
		String collectionControl = "gstr1L2Control.col";
		String gstnControl = "gstr1L2GstnData.col";

		Map<String,Object> expectedMap = new HashMap<String,Object>();
		expectedMap.put("gstin", "wqee87wq98e798wq7");

		List<Map<String, Object>> keyData = new ArrayList<Map<String, Object>>();
		Map<String, Object> keyValue = new HashMap<String, Object>();
		Map<String, Object> keyHeader = new HashMap<String, Object>();;
		keyHeader.put("status", "success");
		keyValue.put("header", keyHeader);
		keyData.add(keyValue);

		Map<String, Object> expectedMapForMongo =  prepareDbResponse();

		new Expectations() {
			{
				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2Control.col";

				mockAspMongoDao.getMongoData(object, collectionControl);
				result = keyData;

				mockAspMongoDao.deleteL2Key(mongoL2Key, gstnControl, collectionControl);

				gstnResource.getMessage("gstr1L2GstnData.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2GstnData.col";

				mockGspService.getL2(inputMap, headerMap);
				result = gstnResponseForData;

				mockGspService.decryptResponse(headerMap,gstnResponseForData);
				result=dycrptDataToken;

				gstnResource.getMessage("gstr1L2Control.col", null, LocaleContextHolder.getLocale());
				result="gstr1L2Control.col";

				mockAspMongoDao.saveInMongo((JSONObject) any, (String) any);
			}
		};


		Map<String, Object> respMap = gstnL2ServiceV31Impl.processL2(headerMap, inputMap);

		assertEquals(respMap.get(Gstr1ConstantsV31.ERROR_CODE), ErrorCodesV31.GSP002);
	}

	private Map<String,String> prepareHeaders(){
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("gstin", "1234");
		headerMap.put("Content-Type", "application/json");
		headerMap.put("api-ver", "0.1");
		headerMap.put("state-cd", "33");
		headerMap.put("ip-usr", "127.0.0.1");
		headerMap.put("txn", "LAPN24235325555JBDJBJHBSJBFJBJFBJBFD");
		headerMap.put("app-code", "01");
		headerMap.put("location", "Mumbai");
		headerMap.put("source-device", "Android");
		headerMap.put("device-string", "Device1");
		headerMap.put("aspclient-id", "USRML9IODPV5F9SAO22");
		headerMap.put("asp-clientsecretkey", "b3c99a68-43e5-4198-9680-0405394b9f64");
		headerMap.put("sek", "f9WKUgtKB8HS9ms/2W4VFwQah/dCuf+xvCbArQmPWhjCLZoHgCzjLkmKOJnuCeeQ");
		headerMap.put("appkey", "CWvnPSa5nX7V5DipSerBu/ave/PLjSe6iKRC1iQYess=");
		headerMap.put("auth-token", "1115f0410ff74fbaa507ac79ba38bf74");
		return headerMap;
	}

	private Map<String,String> prepareInputs(){
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("gstin", "27GSPMH0782G1ZJ");
		inputMap.put("fp", "072017");
		inputMap.put("section", "b2b");
		inputMap.put("level", "L2");
		inputMap.put("flush", "false");

		return inputMap;
	}

	private Map<String,String> prepareInputsWithToken(){
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("gstin", "27GSPMH0782G1ZJ");
		inputMap.put("fp", "072017");
		inputMap.put("section", "b2b");
		inputMap.put("level", "L2");
		inputMap.put("flush", "true");
		inputMap.put("token", "ssd8s98d");

		return inputMap;
	}
	
	private Map<String, Object> prepareDbResponse(){

		Map<String, Object> data = new HashMap<String, Object>();

		data.put("fp", "072017");
		data.put("ttl_record", "19");
		data.put("type", "b2b");
		data.put("gstin", "27GSPMH0782G1ZJ");
		data.put("utime", "2017-10-27 19:15:14.492");

		List<List<Map<String,Object>>> record = new ArrayList<List<Map<String,Object>>>();
		List<Map<String,Object>> dataLsit = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> itms = new ArrayList<Map<String,Object>>();
		Map<String,String> itm_det_data = new HashMap<String,String>();

		Map<String,Object> dataMap = new HashMap<String,Object>();
		Map<String,Object> num = new HashMap<String,Object>();
		Map<String,Object> itm_det = new HashMap<String,Object>();


		itm_det_data.put("csamt", "100");
		itm_det_data.put("rt", "28");
		itm_det_data.put("txval", "10000");
		itm_det_data.put("iamt", "10000");
		itm_det.put("itm_det", itm_det_data);
		num.put("num", "1");

		itms.add(num);
		itms.add(itm_det);

		dataMap.put("val", "2000");
		dataMap.put("cfs", "N");
		dataMap.put("inv_typ", "R");
		dataMap.put("flag", "U");
		dataMap.put("updby", "S");
		dataMap.put("idt", "05-07-2017");
		dataMap.put("ctin", "04AAACR5055K1ZF");
		dataMap.put("rchrg", "N");
		dataMap.put("inum", "VEN103");
		dataMap.put("cflag", "N");
		dataMap.put("chksum", "993c231ac20b04c609f20411984a83609aba2f7be28d9aa63696a48e66215a03");
		dataMap.put("itms", itms);

		dataLsit.add(dataMap);
		record.add(dataLsit);
		data.put("record",record);
		
		return data;
	}

	private Map<String,String> prepareEncryptGSTNResponseForToken(){

		Map<String,String> itm_det_data = new HashMap<String,String>();
		itm_det_data.put("data", "arNWuN6fsO2wDR7LVnmtMLAYptFco7qNacwH2nRJAIXza5yLlBZBFReIKcOMhVHHq9j8q6gj3fn5XKsjTdGihCSeHEiNr5xb7nNGFNZ5BFg=");
		itm_det_data.put("status_cd", "2");
		itm_det_data.put("hmac", "yQdoq0Mfw1kpKsJFpzHgrjdxZ0ZXAdPYEXvGD0QfrYY=");
		itm_det_data.put("rek", "ki6J2JVCPIgxzvthjfQDb3hf2DFwLfnEVr2uykr/94ZxSfiohY+t7Eo3M2Xh9i8t");

		return itm_det_data;
	}

	private Map<String,String> prepareEncryptGSTNResponseForData(){

		Map<String,String> itm_det_data = new HashMap<String,String>();
		itm_det_data.put("data", "lJNsjPRT6h11ZseLDpsYRvRwXHVMi+q0YEwIsaWvuXww4zrkSI5PaAm1Qpp/A0skIbdlJSD3VxdIhKcNGIPKhDSh+gI9AilG8wi31lqwRGB1Cdz78YeptXqCFDvEpSX63tG9raX/ssoEvf4V2cD9FuDTDsQ4weCw89ZcAfPCt5ItqgDUVHbKbQlF/iZ1vvnEQmf/eS98rKM64pAP95Llb8a3KSEiLudBkSTPg15+m12BaP3/jRVj6sdgPs4UjIC4ok2ZTv6fRbWuJnF3/g/C3ABr2oMcVFyNTF3q6K60DN3kicJjq3WmC9Fk95nG28v4aFjSFvPchbbjXYSNiAvr16WkEo3CQnrAlB7mE7bWwF8s23oTIvXLxzcaZ2tIf+BY2uLtP73TvCTIoOINYz8ITBOf9oSQbfO2DZF96NxPCkXLktK4pWzIje5QcMyEfD4RrRTx3+Sd7zk2yM91Zr0aJV58wl15yCfL4CwpmnqqJCbU4r0d8gY5YBYVHSX6KXzJqCDc5N1NQi+ZktJlyH8S00Yl7OG5BF2wrgyAYOO/P5K4aC44Tc6zgKL9zOqfNSRO3A4wAmfTmU5q4X3OZVTsIg==");
		itm_det_data.put("status_cd", "1");
		itm_det_data.put("hmac", "tDDxnnzV5Ca6VzCWEdxdO9OeGXV5WLKFgba8qH+dsKI=");
		itm_det_data.put("rek", "REI8JuiDDPN+asl+APYpKuXFF08Hgv5YbLN6GLfMnZbUl/hofTI/LE/e0Zq2rcjC");

		return itm_det_data;
	}

	private Map<String,Object> prepareGSTNResponse(){
		Map<String,Object> section = new HashMap<String,Object>();
		Map<String,Object> b2b = new HashMap<String,Object>();
		List<Map<String,Object>> inv = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> item = new ArrayList<Map<String,Object>>();
		
		Map<String,String> itm_det_data = new HashMap<String,String>();
		itm_det_data.put("csamt", "100");
		itm_det_data.put("rt", "5");
		itm_det_data.put("txval", "1000");
		itm_det_data.put("iamt", "50");

		Map<String,Object> itm_Obj = new HashMap<String,Object>();
		itm_Obj.put("num", "1");
		itm_Obj.put("itm_det", itm_det_data);

		item.add(itm_Obj);

		Map<String,Object> data_Obj = new HashMap<String,Object>();
		data_Obj.put("val", "1000");
		data_Obj.put("itms", item);
		data_Obj.put("inv_typ", "R");
		data_Obj.put("flag", "U");
		data_Obj.put("updby", "S");
		data_Obj.put("pos", "27");
		data_Obj.put("idt", "01-06-2017");
		data_Obj.put("rchrg", "N");
		data_Obj.put("cflag", "N");
		data_Obj.put("inum", "SAP0000000001");
		data_Obj.put("chksum", "00fd705162db0a9204f6189913fb8369a3355289d4fe1c09bb15ef5e46e2ba9f");

		inv.add(data_Obj);
		b2b.put("b2b",inv);
		List<Object> list = new ArrayList<Object>();
		list.add(b2b);

		section.put("section0",list);
		return section;
	}

	private Map<String,Object> prepareGSTNResponseForGSTNData(){
		Map<String,Object> b2b = new HashMap<String,Object>();
		Map<String,Object> inv = new HashMap<String,Object>();
		List<Map<String,Object>> item = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> lsit = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> invLsit = new ArrayList<Map<String,Object>>();
		Map<String,String> itm_det_data = new HashMap<String,String>();
		itm_det_data.put("csamt", "100");
		itm_det_data.put("rt", "5");
		itm_det_data.put("txval", "1000");
		itm_det_data.put("iamt", "50");

		Map<String,Object> itm_Obj = new HashMap<String,Object>();
		itm_Obj.put("num", "1");
		itm_Obj.put("itm_det", itm_det_data);

		item.add(itm_Obj);

		Map<String,Object> data_Obj = new HashMap<String,Object>();
		data_Obj.put("val", "1000");
		data_Obj.put("itms", item);
		data_Obj.put("inv_typ", "R");
		data_Obj.put("flag", "U");
		data_Obj.put("updby", "S");
		data_Obj.put("pos", "27");
		data_Obj.put("idt", "01-06-2017");
		data_Obj.put("rchrg", "N");
		data_Obj.put("cflag", "N");
		data_Obj.put("inum", "SAP0000000001");
		data_Obj.put("chksum", "00fd705162db0a9204f6189913fb8369a3355289d4fe1c09bb15ef5e46e2ba9f");
		invLsit.add(data_Obj);
		inv.put("inv",invLsit);
		lsit.add(inv);


		b2b.put("b2b",lsit);

		return b2b;
	}

	private Map<String,Object> getUrlList(){

		Map<String,Object> urlList = new HashMap<String,Object>();
		Map<String, Object> urls = new HashMap<String, Object>();
		Map<String, Object> urls1 = new HashMap<String, Object>();
		List<Object> list = new ArrayList<Object>();

		urls.put("ul", "http://loclahost://8080/gstr1");
		urls1.put("ul", "http://loclahost://8080/gstr2");
		list.add(urls);
		list.add(urls1);
		urlList.put("ek","ek");
		urlList.put("urls",list);

		return urlList;
	}
}
