/**
 * 
 */
package com.jio.asp.gstr1.common.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.asp.gstr1.common.dao.CommonDao;
import com.jio.asp.gstr1.common.intercept.CommonConstants;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;
import com.jio.asp.validation.ErrorCodes;

/**
 * @author Rohit1.Soni
 *
 */
@Service
public class CommonServiceImpl implements CommonService {
	@Autowired
	private CommonDao commonDao;
	@Autowired
	private Environment env;
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private MessageSource cmmnMessageSource;

	private static Logger log = LoggerFactory.getLogger(CommonServiceImpl.class);

	@Override
	public Map<String, Object> authenticateAspClient(String clientId, String clientSec, String clientIp, String gstin) {
		log.debug("authenticateAspClient method: START");
		long start = System.currentTimeMillis();
		CommonUtil.validateEmptyString(clientId, ErrorCodes.ASP1001, AspConstants.JIOGST_HEADER, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
		CommonUtil.validateEmptyString(clientSec, ErrorCodes.ASP1002, AspConstants.JIOGST_HEADER, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
		CommonUtil.validateEmptyString(gstin, ErrorCodes.ASP1020, AspConstants.JIOGST_HEADER, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
		CommonUtil.validateEmptyString(clientId, ErrorCodes.ASP1007, AspConstants.JIOGST_HEADER, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		String ipAddr = StringUtils.isNotEmpty(clientIp) ? clientIp : "noippassed";
		Map<String, Object> filterMap = new HashMap<>();
		Map<String, Object> aspClientDetails = null;
		filterMap.put(CommonConstants.MONGO_REC_ID, clientId);
		List<Map<String, Object>> userData = commonDao.getMongoData(filterMap, env.getProperty("client-details.col"));
		if (CollectionUtils.isEmpty(userData)) {
			log.error("Invalid client data was entered, from the ip address {} , asp client used was {}", ipAddr,
					clientId);
			log.info("Invalid client data was entered, from the ip address {} , asp client used was {}", ipAddr,
					clientId);
			CommonUtil.throwException(ErrorCodes.ASP1016, AspConstants.JIOGST_HEADER, null, HttpStatus.UNAUTHORIZED,
					null, AspConstants.FORM_CODE);

		} else {
			aspClientDetails = userData.stream().findFirst().orElse(null);
			if (aspClientDetails != null) {
				if (!clientSec.equals(MapUtils.getString(aspClientDetails, CommonConstants.JSON_CLIENT_KEY))) {
					log.error("Invalid secret key was entered, from the ip address {} , asp client used was {}", ipAddr,
							clientId);
					log.info("Invalid secret key  was entered, from the ip address {} , asp client used was {}", ipAddr,
							clientId);
					CommonUtil.throwException(ErrorCodes.ASP1016, AspConstants.JIOGST_HEADER, null,
							HttpStatus.UNAUTHORIZED, null, AspConstants.FORM_CODE);
				} else if (!CommonConstants.CLIENT_STATUS_ACTIVE
						.equals(MapUtils.getString(aspClientDetails, CommonConstants.JSON_USER_STATUS))) {
					log.error(
							"Invalid client data was entered, account is inactive, from the ip address {} , asp client used was {}",
							ipAddr, clientId);
					log.info(
							"Invalid client data was entered, account is inactive, from the ip address {} , asp client used was {}",
							ipAddr, clientId);
					CommonUtil.throwException(ErrorCodes.ASP1016, AspConstants.JIOGST_HEADER, null,
							HttpStatus.UNAUTHORIZED, null, AspConstants.FORM_CODE);
				}
			} else {
				log.error("Invalid client data was entered, from the ip address {} , asp client used was {}", ipAddr,
						clientId);
				log.info("Invalid client data was entered, from the ip address {} , asp client used was {}", ipAddr,
						clientId);
				CommonUtil.throwException(ErrorCodes.ASP1016, AspConstants.JIOGST_HEADER, null, HttpStatus.UNAUTHORIZED,
						null, AspConstants.FORM_CODE);
			}
		}
		long end = System.currentTimeMillis();
		log.debug("authenticateAspClient method: Total Time: {}", (end - start));
		log.debug("authenticateAspClient method: END");
		return aspClientDetails;
	}

	@Override
	public void authorizeAspClient(String clientId, String clientIp, String gstin, Map<String, Object> clientData) {
		log.debug("authorizeAspClient method: START");
		long start = System.currentTimeMillis();
		CommonUtil.validateEmptyString(gstin, ErrorCodes.ASP1020, AspConstants.JIOGST_HEADER, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
		String ipAddr = StringUtils.isNotEmpty(clientIp) ? clientIp : "noippassed";

		List<String> gstinList = (List<String>) clientData.get(CommonConstants.JSON_GSTIN);
		boolean wlEnabled = MapUtils.getBooleanValue(clientData, CommonConstants.JSON_WL, false);
		if (!wlEnabled
				&& (CollectionUtils.isEmpty(gstinList) || !gstinList.stream().anyMatch(gstin::equalsIgnoreCase))) {
			log.error(
					"authorizeAspClient method: Invalid gstin data was entered, gstin is not registered list of gstins, from the ip address {} , asp client used was {}",
					ipAddr, clientId);
			log.info(
					"authorizeAspClient method: Invalid gstin data was entered, gstin is not registered list of gstins, from the ip address {} , asp client used was {}",
					ipAddr, clientId);
			CommonUtil.throwException(ErrorCodes.ASP1017, AspConstants.JIOGST_HEADER, null, HttpStatus.UNAUTHORIZED,
					null, AspConstants.FORM_CODE);
		}
		long end = System.currentTimeMillis();
		log.debug("authorizeAspClient method: Total Time: {}", (end - start));
		log.debug("authorizeAspClient method: END");
	}

	@Override
	@Async(value = "taskExecutor")
	public void commonKafkaProducer(Map<String, Object> jsonMap, String requestId, String topicName) {
		log.debug("commonKafkaProducer: pushing the messages START");
		log.info("commonKafkaProducer: ###Starting Message posting in Kafka for requestId: {}", requestId);
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			json = mapper.writeValueAsString(jsonMap);
			ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, json);
			future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
				@Override
				public void onSuccess(SendResult<String, String> result) {
					log.debug("commonKafkaProducer: Message push success for requestId: {}", requestId);
				}

				@Override
				public void onFailure(Throwable ex) {
					log.debug("commonKafkaProducer: Message push failed for requestId: {}", requestId);
				}
			});
			log.debug("commonKafkaProducer: After calling send method, thread id {}", Thread.currentThread().getId());
		} catch (Exception e) {
			log.error("commonKafkaProducer: Exception unable to process the json passed json is :{}", json);
			log.error("commonKafkaProducer: Exception unable to process the json, exception {}", e);
		}
		log.info("commonKafkaProducer: ###Ending Message posting in Kafka for requestId: {}", requestId);
		log.debug("commonKafkaProducer: pushing the messages END");
	}
}
