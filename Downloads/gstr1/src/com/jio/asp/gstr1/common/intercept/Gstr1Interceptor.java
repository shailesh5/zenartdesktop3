package com.jio.asp.gstr1.common.intercept;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.jio.asp.gstr1.common.service.CommonService;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;

/**
 * @author Rohit1.Soni
 *
 */
public class Gstr1Interceptor extends HandlerInterceptorAdapter {

	private static final Logger log = LoggerFactory.getLogger(Gstr1Interceptor.class);

	private static List<String> filterHdrList;

	static {
		filterHdrList = new ArrayList<>();
		filterHdrList.add(CommonConstants.HEADER_SECRET);
		filterHdrList.add(CommonConstants.HEADER_SEK);
		filterHdrList.add(CommonConstants.HEADER_APP_KEY);
		filterHdrList.add(CommonConstants.HEADER_AUTH_TOKEN);
		filterHdrList.add(Gstr1Constants.HEADER_IP_USER);
	}

	@Autowired
	private CommonService commonService;

	@Autowired
	MessageSource messageSource;
	


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// System.out.println("inside pre handle ");
		log.debug("Gstr1Interceptor: Pre-handle - ENTRY");
		log.info("Gstr1Interceptor: Pre-handle - ENTRY");
		String uri = request.getRequestURI();
		String context = request.getContextPath();
		// below if condition will be called for all the url other than ping.
		// Below condition checks for client auth.
		if (!uri.equals(context + "/ping")) {
			String requestId = UUID.randomUUID().toString();
			request.setAttribute("RequestId", requestId);
			String clientId = request.getHeader(CommonConstants.HEADER_CLIENT_ID);
			String clientSec = request.getHeader(CommonConstants.HEADER_SECRET);
			String clientIp = request.getHeader(Gstr1Constants.HEADER_IP_USER);
			String gstin = request.getHeader(CommonConstants.HEADER_GSTIN);
			Map<String, Object> userData = commonService.authenticateAspClient(clientId, clientSec, clientIp, gstin);
			commonService.authorizeAspClient(clientId, clientIp, gstin, userData);
			logApiAccess(requestId, true, request);
		}
		log.debug("Gstr1Interceptor: Pre-handle - EXIT");
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		log.debug("Gstr1Interceptor: Post-handle - EXIT " + request.getAttribute("RequestId"));
		// System.out.println("inside post
		// handle"+request.getAttribute("RequestId"));
		if(request.getAttribute("RequestId") != null){
			logApiAccess(String.valueOf(request.getAttribute("RequestId")), false, request);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		log.debug("Gstr1Interceptor: afterCompletion - EXIT " + request.getAttribute("RequestId"));

		// System.out.println("inside afterCompletion
		// "+request.getAttribute("RequestId"));
	}

	private boolean logApiAccess(String requestId,boolean isInsert, HttpServletRequest request){
		Map<String, Object> additionalLogMap=new HashMap<>();
		String url=request.getRequestURI();
		String method=request.getMethod();
		additionalLogMap.put(CommonConstants.ACCESS_LOG_URL, url);
		additionalLogMap.put(CommonConstants.ACCESS_LOG_METHOD, method);
		additionalLogMap.put(CommonConstants.MONGO_REC_ID, requestId);
		if(!isInsert){
			additionalLogMap.put(CommonConstants.JSON_UDATE, LocalDateTime.now().toString() );
			additionalLogMap.put(CommonConstants.ACCESS_LOG_STATUS, CommonConstants.ACCESS_LOG_STATUS_SUC);
		}else{
			Map<String, Object> hdrMap=getHeadersInfo( request);
			additionalLogMap.put(CommonConstants.ACCESS_LOG_STATUS, CommonConstants.ACCESS_LOG_STATUS_INIT);
			additionalLogMap.put(CommonConstants.JSON_IDATE,LocalDateTime.now().toString());
			additionalLogMap.putAll(hdrMap);
			
		}
		commonService.commonKafkaProducer(additionalLogMap, requestId, CommonConstants.ACCESS_LOG_KAFKA_TOPIC);
		
		return true;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 * In filter, modified the header ip-usr and assigned a constant value to it.
	 * Introduced new header ip-user and assigned ip-usr's value to it.
	 * Added condition here :-
	 * 1) To check if header is ip-usr then take value of ip-user which is ip address sent by user and log it.
	 * 2) Ignore newly introduced header ip-user for logging
	 */
	private Map<String, Object> getHeadersInfo(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			if (!filterHdrList.contains(key)) {
				String value = null;
				if(Gstr1Constants.HEADER_IP_USR.equalsIgnoreCase(key)){
					value = request.getHeader(Gstr1Constants.HEADER_IP_USER);
				}else{
					value = request.getHeader(key);
				}
				
				map.put(key, value);
			}
		}
		return map;
	}

}
