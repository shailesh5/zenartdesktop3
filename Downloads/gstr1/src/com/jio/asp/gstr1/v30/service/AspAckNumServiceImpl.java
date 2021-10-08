package com.jio.asp.gstr1.v30.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jio.asp.gstr1.v30.exception.RestExceptionHandler;

/**
 * This class is used for generating unique acknowledgement number.
 * 
 * @author Rohit1.Soni
 *
 */
@Service
public class AspAckNumServiceImpl implements AspAckNumService {

	@Autowired
	private MessageSource gstnResource;
	@Autowired
	private RestTemplate restTemplate;

	Logger log = LoggerFactory.getLogger(AspAckNumServiceImpl.class);

	/**
	 * Method takes input of api which is consuming it and provides the unique
	 * acknowledge number to the calling api. To generate the ack number this
	 * method connects to acknowledge api deployed as service on the given url.
	 * This url is read from application property file.
	 * 
	 * @param fromApi,
	 *            name of the api which is trying to consume the ack number.
	 * @return String acknowledge number
	 */
	@Override
	public String generateAckNumber(String fromApi) {
		log.debug("generateAckNumber Method: START");
		log.debug("generateAckNumber Method: Generating Acknowledge No for API : {}", fromApi);
		String ackUrl=gstnResource.getMessage("url.acknowledgement_number", null, LocaleContextHolder.getLocale());
		SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
		int readTimeOut = Integer
				.parseInt(gstnResource.getMessage("rest-read-timeout", null, LocaleContextHolder.getLocale()));
		int connectionTimeOut = Integer
				.parseInt(gstnResource.getMessage("rest-connection-timeout", null, LocaleContextHolder.getLocale()));
		rf.setReadTimeout(readTimeOut);
		rf.setConnectTimeout(connectionTimeOut);
		restTemplate.setErrorHandler(new RestExceptionHandler());
		String ackNumber = restTemplate.getForObject(ackUrl, String.class);
		log.debug("generateAckNumber Method: END");
		return ackNumber;
	}

}
