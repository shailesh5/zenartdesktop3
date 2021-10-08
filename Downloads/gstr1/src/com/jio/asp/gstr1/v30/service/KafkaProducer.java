package com.jio.asp.gstr1.v30.service;

import java.util.List;
import java.util.Map;

public interface KafkaProducer {

	void postKafkaMsg(List<Map<String, Object>> flatJson, String ackNo,String topicName);

}
