package com.jio.asp.gstr1.v31.service;

import java.util.List;
import java.util.Map;

public interface KafkaProducerV31 {

	void postKafkaMsg(List<Map<String, Object>> flatJson, String ackNo,String topicName);

}
