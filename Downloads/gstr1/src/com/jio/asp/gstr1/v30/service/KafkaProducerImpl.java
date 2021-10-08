package com.jio.asp.gstr1.v30.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
@Service
public class KafkaProducerImpl implements KafkaProducer {
	private static final Logger log = LoggerFactory.getLogger(KafkaProducerImpl.class);

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	@Autowired
	private AspLoggingService logService;
	
	
	@Override
	@Async(value="taskExecutor")
	public void postKafkaMsg(List<Map<String, Object>> flatJson, String ackNo,String topicName) {
		log.debug("postKafkaMsg: pushing the messages START");
		log.info("postKafkaMsg: ###Starting Message posting in Kafka for ackNo: {}",ackNo);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> controlDataMap=new HashMap<>();
		Map<String, Number> incMap=new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat(Gstr1Constants.HEADER_DATE_FORMAT);
		for (Map<String, Object> map : flatJson) {
			String json = null;
			try {
				json = mapper.writeValueAsString(map);
				ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, json);
				future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
					@Override
					public void onSuccess(SendResult<String, String> result) {
						log.debug("postKafkaMsg: Message push success for ackNo: {}",ackNo);
					}
					@Override
					public void onFailure(Throwable ex) {
						log.debug("postKafkaMsg: Message push failed for ackNo: {}",ackNo);
						Date date = new Date();
						controlDataMap.put(Gstr1Constants.CONTROL_REC_UPDTTIME,  sdf.format(date));
						incMap.put(Gstr1Constants.CONTROL_REC_ERRCNT,1);
						logService.updateControlLog(controlDataMap,incMap,ackNo);
						controlDataMap.clear();
						incMap.clear();
					}
				});
				log.debug("postKafkaMsg: After calling send method, thread id {}",Thread.currentThread().getId());
			} catch (Exception e) {
				Date date = new Date();
				controlDataMap.put(Gstr1Constants.CONTROL_REC_UPDTTIME,  sdf.format(date));
				incMap.put(Gstr1Constants.CONTROL_REC_ERRCNT,1);
				logService.updateControlLog(controlDataMap,incMap,ackNo);
				controlDataMap.clear();
				incMap.clear();
				log.error("postKafkaMsg: Exception unable to process the json passed json is :{}",json);
				log.error("postKafkaMsg: Exception unable to process the json, exception {}",e);
			}
		}
		log.info("postKafkaMsg: ###Ending Message posting in Kafka for ackNo: {}",ackNo);
		log.debug("postKafkaMsg: pushing the messages END");
	}
}
