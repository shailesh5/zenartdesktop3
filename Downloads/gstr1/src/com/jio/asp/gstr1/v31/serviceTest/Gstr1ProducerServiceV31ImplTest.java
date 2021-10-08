package com.jio.asp.gstr1.v31.serviceTest;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.asp.gstr1.v31.service.AspAckNumServiceV31;
import com.jio.asp.gstr1.v31.service.AspSuppliesStatusServiceV31;
import com.jio.asp.gstr1.v31.service.AspSuppliesSumServiceV31;
import com.jio.asp.gstr1.v31.service.AspUpdateStatusService;
import com.jio.asp.gstr1.v31.service.Gstr1ProducerServiceV31;
import com.jio.asp.gstr1.v31.service.Gstr1ProducerServiceV31Impl;
import com.jio.asp.gstr1.v31.service.Gstr1SummaryServiceV31;
import com.jio.asp.gstr1.v31.service.KafkaProducerV31;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

public class Gstr1ProducerServiceV31ImplTest {
	
	@Injectable
	private KafkaProducerV31 kafkaService;
	
	@Injectable
	private MessageSource gstnResource;
	@Injectable
	private RestTemplate restTemplate;
	@Injectable
	private MessageSource messageSourceV31;
	@Injectable
	private AspUpdateStatusService mockpUdateStatusService;
		
	@Autowired
	private Gstr1ProducerServiceV31 prodService;
	
	@Injectable
	private AspAckNumServiceV31 ackService;
	@Injectable
	private Gstr1SummaryServiceV31 gstr1SummaryServiceV31;
	@Injectable
	private AspSuppliesStatusServiceV31 statusService;
	@Injectable
	private AspSuppliesSumServiceV31 dasboardService;
	
	@Tested
	Gstr1ProducerServiceV31Impl Gstr1ProducerServiceV31ImplMok;
	 Map<String, Object> payload ;
	@Test
	public final void testInvokeConverterApi() {
		
		String flatPayload = "{ \"gstin\": \"27GSPMH0781G1ZK\", \"fp\": \"082017\", \"expa\": [ { \"flag\":\"P\", \"exp_typ\": \"WPAY\", \"oinum\": \"71542\", \"oidt\": \"16-06-2017\", \"inum\": \"81542\", \"idt\": \"16-07-2017\", \"val\": 995048.36, \"sbpcode\": \"12346\", \"sbnum\": 84298, \"sbdt\": \"17-07-2017\", \"action\": \"R\", \"itms\": [ { \"txval\": 15000, \"rt\": 18, \"iamt\": 2700, \"csamt\": 0.5, \"custom\": { \"ds\": \"test_ds\", \"bgrp\": \"test_bgrp\", \"bloc\": \"test_bloc\", \"bid\": \"test_bid\" } } ] } ] }";
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("gstin", "27GSPMH0781G1ZK");
		headerMap.put("Content-Type", "application/json");
		headerMap.put("state-cd", "27");
		headerMap.put("ip-usr", "127.0.0.1");
		headerMap.put("txn", "ROHI24237825566JBDJBJHBSJBFJBJFB");
		headerMap.put("location", "Mumbai");
		headerMap.put("source-device", "Android");
		headerMap.put("device-string", "Device1");
		headerMap.put("aspclient-id", "USRML9IODPV5F9SAO22");
		headerMap.put("asp-clientsecretkey", "b3c99a68-43e5-4198-9680-0405394b9f64");
		headerMap.put("cntr", "false");
		headerMap.put("username", "Reliance.MH.1");
		headerMap.put("fp", "082017");
		String ackNo = "TXN0000000000000034692";
		
		String jsonString = Gstr1ProducerServiceV31ImplMok.invokeConverterApi(flatPayload, headerMap, ackNo);
		
		org.junit.Assert.assertNotNull(jsonString);
		
		//fail("Not yet implemented"); // TODO
	}
	
	@Test
	public final void testInvokeConverterApiCallConverter() {
		
		String flatPayload = "{ \"gstin\": \"27GSPMH0781G1ZK\", \"fp\": \"082017\", \"expa\": [ { \"flag\":\"P\", \"exp_typ\": \"WPAY\", \"oinum\": \"71542\", \"oidt\": \"16-06-2017\", \"inum\": \"81542\", \"idt\": \"16-07-2017\", \"val\": 995048.36, \"sbpcode\": \"12346\", \"sbnum\": 84298, \"sbdt\": \"17-07-2017\", \"action\": \"R\", \"itms\": [ { \"txval\": 15000, \"rt\": 18, \"iamt\": 2700, \"csamt\": 0.5, \"custom\": { \"ds\": \"test_ds\", \"bgrp\": \"test_bgrp\", \"bloc\": \"test_bloc\", \"bid\": \"test_bid\" } } ] } ] }";
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("gstin", "27GSPMH0781G1ZK");
		headerMap.put("Content-Type", "application/json");
		headerMap.put("state-cd", "27");
		headerMap.put("ip-usr", "127.0.0.1");
		headerMap.put("txn", "ROHI24237825566JBDJBJHBSJBFJBJFB");
		headerMap.put("location", "Mumbai");
		headerMap.put("source-device", "Android");
		headerMap.put("device-string", "Device1");
		headerMap.put("aspclient-id", "USRML9IODPV5F9SAO22");
		headerMap.put("asp-clientsecretkey", "b3c99a68-43e5-4198-9680-0405394b9f64");
		headerMap.put("cntr", "true");
		headerMap.put("username", "Reliance.MH.1");
		headerMap.put("fp", "082017");
		String ackNo = "TXN0000000000000034692";
		
		String jsonString = Gstr1ProducerServiceV31ImplMok.invokeConverterApi(flatPayload, headerMap, ackNo);
		
		org.junit.Assert.assertNotNull(jsonString);
		
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testSaveInvoice() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
		String jsonString = "{ \"gstin\": \"27GSPMH0781G1ZK\", \"fp\": \"082017\", \"expa\": [ { \"flag\":\"P\", \"exp_typ\": \"WPAY\", \"oinum\": \"71542\", \"oidt\": \"16-06-2017\", \"inum\": \"81542\", \"idt\": \"16-07-2017\", \"val\": 995048.36, \"sbpcode\": \"12346\", \"sbnum\": 84298, \"sbdt\": \"17-07-2017\", \"action\": \"R\", \"itms\": [ { \"txval\": 15000, \"rt\": 18, \"iamt\": 2700, \"csamt\": 0.5, \"custom\": { \"ds\": \"test_ds\", \"bgrp\": \"test_bgrp\", \"bloc\": \"test_bloc\", \"bid\": \"test_bid\" } } ] } ] }";
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("gstin", "27GSPMH0781G1ZK");
		headerMap.put("Content-Type", "application/json");
		headerMap.put("state-cd", "27");
		headerMap.put("ip-usr", "127.0.0.1");
		headerMap.put("txn", "ROHI24237825566JBDJBJHBSJBFJBJFB");
		headerMap.put("location", "Mumbai");
		headerMap.put("source-device", "Android");
		headerMap.put("device-string", "Device1");
		headerMap.put("aspclient-id", "USRML9IODPV5F9SAO22");
		headerMap.put("asp-clientsecretkey", "b3c99a68-43e5-4198-9680-0405394b9f64");
		headerMap.put("cntr", "false");
		headerMap.put("username", "Reliance.MH.1");
		headerMap.put("fp", "082017");
		String ackNo = "TXN0000000000000034692";
		try {
			payload = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
			});
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Expectations() {
			{
				mockpUdateStatusService.processGstinMasterData(payload, headerMap, ackNo);
				result=true;
			}
		};
		
		Map<String, String> result=Gstr1ProducerServiceV31ImplMok.saveInvoice(payload, headerMap, ackNo);
		
		Assert.assertNotNull(result);
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testConvertInputToFlatJson() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetFY() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testValidatePayloadStatus() {
		fail("Not yet implemented"); // TODO
	}

}
