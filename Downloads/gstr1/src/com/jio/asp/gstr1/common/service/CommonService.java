package com.jio.asp.gstr1.common.service;

import java.util.List;
import java.util.Map;

public interface CommonService {
	/**
	 * this method validates api clients against the database. this is to allow
	 * only valid end client to consume asp services.
	 * 
	 * @param clientId,
	 *            client id sent by user
	 * @param clientSec,
	 *            client sec sent in request
	 * @param clientIp,
	 *            ip address of the user.
	 */
	Map<String, Object> authenticateAspClient(String clientId, String clientSec, String clientIp, String gstin);

	void authorizeAspClient(String clientId, String clientIp, String gstin, Map<String, Object> clientData);

	void commonKafkaProducer(Map<String, Object> jsonMap, String requestId, String topicName);
}
