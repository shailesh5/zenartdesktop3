package com.jio.asp.gstr1.v31.serviceTest;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.MessageSource;

import com.jio.asp.gstr1.v31.dao.AspSuppliesDaoV31;
import com.jio.asp.gstr1.v31.service.AspSuppliesSumServiceV31Impl;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;


public class Gstr1JioGstL2TestCase {

	@Tested
	AspSuppliesSumServiceV31Impl aspSuppliesSumServiceV31;
	
	@Injectable
	private MessageSource mockeMessageSourceV31;
	
	@Injectable
	private AspSuppliesDaoV31 mockSummaryDao;
	
	Map<String,Object> expectedMap = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testPositiveL2(){
		
		Map<String, String> headerMap = prepareHeaders();
		Map<String, String> inputMap = prepareInputs();
		
		Map<String,Object> expectedMap = new HashMap<String,Object>();
		expectedMap.put("gstin", "wqee87wq98e798wq7");
		
		new Expectations() {
			{
				mockSummaryDao.retrieveSummaryData((Map<String, String> ) any);
				result = expectedMap;
			}
		};
		
		Map<String, Object> respMap = aspSuppliesSumServiceV31.processGstr1InvoiceDataL2(headerMap, inputMap);
		
		Map successData = (HashMap)respMap.get("data");
		assertEquals(successData.get("gstin"), "wqee87wq98e798wq7");
	}
	
	@Test
	public void testNegativeL2(){
		
		Map<String, String> headerMap = prepareHeaders();
		Map<String, String> inputMap = prepareInputs();
		
		Map<String,Object> expectedMap = null;
		
		new Expectations() {
			{
				mockSummaryDao.retrieveSummaryData((Map<String, String> ) any);
				result = expectedMap;
			}
		};
		
		Map<String, Object> respMap = aspSuppliesSumServiceV31.processGstr1InvoiceDataL2(headerMap, inputMap);
		
		Map errorData = (HashMap)respMap.get("data");
		assertEquals(errorData.get("message"), "No data found for the given criteria");
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
		
		return headerMap;
	}
	
	private Map<String,String> prepareInputs(){
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("gstin", "1234");
		inputMap.put("fp", "072017");
		inputMap.put("section", "b2b");
		inputMap.put("level", "L2");
		
		return inputMap;
	}
	
}
