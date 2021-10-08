package com.jio.asp.gstr1.v31.serviceTest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.exception.AspExceptionV31;
import com.jio.asp.gstr1.v31.service.AspLoggingServiceV31;
import com.jio.asp.gstr1.v31.service.AspUpdateStatusService;
import com.jio.asp.gstr1.v31.service.Gstr1ProducerServiceV31Impl;
import com.jio.asp.gstr1.v31.service.KafkaProducerV31;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;

public class Gstr1SaveJioGstTestCase {
	
	@Tested
	private Gstr1ProducerServiceV31Impl saveService;
	
	@Injectable
	private MessageSource messageSourceV31;
	
	@Injectable
	private MessageSource gstnResource;
	
	@Injectable
	private AspMongoDaoV31 aspMongoDao;
	
	@Injectable
	private AspUpdateStatusService updateStatusService;
	
	@Injectable
	private AspLoggingServiceV31 aspLoggingServiceV31;
	
	@Injectable
	private KafkaProducerV31 kafkaService;
	
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
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test(expected=AspExceptionV31.class)
    public void testValidateSubmitStatus(){
           Set<String> uniqueMongoIds=new HashSet<>();
           uniqueMongoIds.add("26AAACR5055K1Z9:2017:26113000003156");
           uniqueMongoIds.add("26AAACR5055K1Z9:2017:CDN:2611D001000098");
           
           List<Map<String, Object>> flatJson=new ArrayList<>();
           Object []param=new Object[2];
           param[0]=uniqueMongoIds;
           param[1]=flatJson;
           
           String collection="supplies";
           List<Map<String, Object>> idList=prepareValidateSubmitStatusData();
           
           new Expectations() {
                  {
                        gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
                        result="supplies";

                        aspMongoDao.getMongoData((Map<String, Object>) any, (String[]) any ,collection);times=1;
                        result = idList;
                  }
           };
           
            Deencapsulation.invoke(saveService, "validatePayloadStatus", param);
    }

	@Test
	public void testSaveSuppliesPositive()
	{
		Map<String, String> headerMap = prepareHeaders();
		Map<String, Object> requestPayload = prepareinpute();
        String ackNo="TXN0000000000000034692";
        new NonStrictExpectations() {
			{
				updateStatusService.processGstinMasterData(requestPayload, headerMap, ackNo);
				result = true;
				
				aspLoggingServiceV31.generateControlLog((Map<String, Object>) any);
				
				gstnResource.getMessage("gstr1-kafka-topic-v3.1", null,LocaleContextHolder.getLocale());
				result = "gstr1-kafka-topic-v3.1";
				
				kafkaService.postKafkaMsg((List<Map<String,Object>>) any, ackNo, (String )any);
			}
		};
		
		Map<String, String> respMap = saveService.saveInvoice(requestPayload, headerMap,ackNo);
		assertEquals(respMap.get(Gstr1ConstantsV31.GSTR1_RESP_ACKNO), ackNo);
	}
	
	@Test(expected=AspExceptionV31.class)
	public void testSaveSuppliesNegative()
	{
		Map<String, String> headerMap = prepareHeaders();
		Map<String, Object> requestPayload = prepareinpute();
        String ackNo="TXN0000000000000034692";
      
        new NonStrictExpectations() {
			{
				updateStatusService.processGstinMasterData(requestPayload, headerMap, ackNo);
				result = false;
			}
		};
		
		saveService.saveInvoice(requestPayload, headerMap,ackNo);
	}

	private Map<String,Object> prepareinpute()
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("fp", "072017");
		data.put("sname", "ABC&Co");
		data.put("gt", "1234567.89");
		data.put("gstin", "27GSPMH0782G1ZJ");
		data.put("curr_gt", "1232");
		
		List<Map<String,Object>> b2b = new ArrayList<Map<String,Object>>();
		Map<String,Object> customMap = new HashMap<String,Object>();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		customMap.put("ds", "test_ds");
		customMap.put("bgrp", "test_bgrp");
		customMap.put("bloc", "test_bloc");
		customMap.put("bid", "test_bid");
		
		dataMap.put("custom", customMap);
		
		dataMap.put("ctin", "27GSPMH0782G1ZJ");
		dataMap.put("inv_typ", "R");
		dataMap.put("etin", "");
		dataMap.put("inum", "26113000003156");
		dataMap.put("idt", "14-07-2017");
		dataMap.put("val", "20000.01");
		dataMap.put("pos", "");
		dataMap.put("rchrg", "Y");
		dataMap.put("rt", "12");
		dataMap.put("txval", "15000.23");
		dataMap.put("irt", "0");
		dataMap.put("iamt", "1267");
		dataMap.put("crt", "0");
		dataMap.put("camt", "0");
		dataMap.put("srt", "0");
		dataMap.put("samt", "0");
		dataMap.put("csamt", "172.5");
		dataMap.put("action", "R");
		
		b2b.add(dataMap);
		data.put("b2b", b2b);
		return data;
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

	private List<Map<String, Object>> prepareValidateSubmitStatusData(){
	    Map<String, Object> b2bMap = new HashMap<String, Object>();
	    Map<String, Object> cdnrMap = new HashMap<String, Object>();
	    Map<String, Object> b2bControlMap = new HashMap<String, Object>();
	    Map<String, Object> cdnrControlMap = new HashMap<String, Object>();
	    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	    
	    b2bControlMap.put("type","b2b");
	    b2bMap.put("control", b2bControlMap);
	    b2bMap.put("_id", "26AAACR5055K1Z9:2017:26113000003156");
	    
	    cdnrControlMap.put("type","cdnr");
	    cdnrMap.put("control", cdnrControlMap);
	    cdnrMap.put("_id", "26AAACR5055K1Z9:2017:CDN:2611D001000098");
	    
	    list.add(b2bMap);
	    list.add(cdnrMap);
	    
	    return list;
	}
}