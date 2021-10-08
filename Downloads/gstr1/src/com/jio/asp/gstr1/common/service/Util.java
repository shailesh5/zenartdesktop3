/**
 * 
 */
package com.jio.asp.gstr1.common.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import com.jio.asp.gstr1.common.intercept.CommonConstants;
import com.jio.asp.gstr1.v31.exception.AspExceptionV31;

/**
 * @author 
 *
 */
public class Util {
	
	
	public static final Logger log = LoggerFactory.getLogger(Util.class);
	
	/**
	 * This method validates Empty string. Incase if input string is passed
	 * blank then this method will throw Asp Exception, with wrapped Error
	 * Response object.
	 * 
	 * @param value
	 *            string to be validated for empty
	 * @param errorCode
	 *            error code to be given back in response
	 * @param errorGrp group to which this error belongs like, Summary, Save invoice, file gstr etc
	 * @param messageKey, error key which is stored in errorMessage.properties file.
	 * @param status, httpstatus code which needs to be added in the response.
	 * @param param
	 *            Parameter to be replaced in error message incase if it has
	 *            dynamic parameter.
	 * @param messageSourceV31, Message resource which stores all the error messages.
	 */
	public static void validateEmptyString(String value, String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSourceV31) {
		if (StringUtils.isBlank(value)) {
			Map<String, Object> excObj = new HashMap<>();
			String msg=messageSourceV31.getMessage(messageKey, param, LocaleContextHolder.getLocale());
			excObj.put(CommonConstants.ERROR_CODE, errorCode);
			excObj.put(CommonConstants.ERROR_DESC,msg);
			excObj.put(CommonConstants.ERROR_GRP, errorGrp);
			excObj.put(CommonConstants.ERROR_HTTP_CODE, status);
			log.error(msg);
			throw new AspExceptionV31(msg, null, false, false,
					excObj);
		}
	}
	
	/**
	 * This method validates Empty string. Incase if input string is passed
	 * blank then this method will throw Asp Exception, with wrapped Error
	 * Response object.
	 * 
	 * @param value
	 *            string to be validated for empty
	 * @param errorCode
	 *            error code to be given back in response
	 * @param errorGrp group to which this error belongs like, Summary, Save invoice, file gstr etc
	 * @param messageKey, error key which is stored in errorMessage.properties file.
	 * @param status, httpstatus code which needs to be added in the response.
	 * @param param
	 *            Parameter to be replaced in error message incase if it has
	 *            dynamic parameter.
	 * @param messageSourceV31, Message resource which stores all the error messages.
	 */
	public static void validateEmpty(Object value, String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSourceV31) {
		if (value==null) {
			Map<String, Object> excObj = new HashMap<>();
			String msg=messageSourceV31.getMessage(messageKey, param, LocaleContextHolder.getLocale());
			excObj.put(CommonConstants.ERROR_CODE, errorCode);
			excObj.put(CommonConstants.ERROR_DESC,msg);
			excObj.put(CommonConstants.ERROR_GRP, errorGrp);
			excObj.put(CommonConstants.ERROR_HTTP_CODE, status);
			log.error(msg);
			throw new AspExceptionV31(msg, null, false, false,
					excObj);
		}
	}
	
	/**
	 * This method validates Empty string. Incase if input string is passed
	 * blank then this method will throw Asp Exception, with wrapped Error
	 * Response object.
	 * 
	 * @param value
	 *            string to be validated for empty
	 * @param minLength, min length allowed
	 * @param maxLength, max length allowed
	 * @param errorCode
	 *            error code to be given back in response
	 * @param errorGrp group to which this error belongs like, Summary, Save invoice, file gstr etc
	 * @param messageKey, error key which is stored in errorMessage.properties file.
	 * @param status, httpstatus code which needs to be added in the response.
	 * @param param
	 *            Parameter to be replaced in error message incase if it has
	 *            dynamic parameter.
	 * @param messageSourceV31, Message resource which stores all the error messages.
	 */
	public static void validateLength(String value, int minLength,int maxLength, String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSourceV31) {
		int length=value.length();
		if (length<minLength || length>maxLength) {
			Map<String, Object> excObj = new HashMap<>();
			String msg=messageSourceV31.getMessage(messageKey, param, LocaleContextHolder.getLocale());
			excObj.put(CommonConstants.ERROR_CODE, errorCode);
			excObj.put(CommonConstants.ERROR_DESC,msg);
			excObj.put(CommonConstants.ERROR_GRP, errorGrp);
			excObj.put(CommonConstants.ERROR_HTTP_CODE, status);
			log.error(msg);
			throw new AspExceptionV31(msg, null, false, false,
					excObj);
		}
	}

	
	/**
	 * This method validates Empty string. Incase if input string is passed
	 * blank then this method will throw Asp Exception, with wrapped Error
	 * Response object.
	 * 
	 * @param value
	 *            string to be validated for empty
	 * @param errorCode
	 *            error code to be given back in response
	 * @param errorGrp group to which this error belongs like, Summary, Save invoice, file gstr etc
	 * @param messageKey, error key which is stored in errorMessage.properties file.
	 * @param status, httpstatus code which needs to be added in the response.
	 * @param param
	 *            Parameter to be replaced in error message incase if it has
	 *            dynamic parameter.
	 * @param messageSourceV31, Message resource which stores all the error messages.
	 */
	public static void validateInteger(String value, String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSourceV31) {
		String regex = "\\d+";
		if (value==null || !value.matches(regex)) {
			Map<String, Object> excObj = new HashMap<>();
			String msg=messageSourceV31.getMessage(messageKey, param, LocaleContextHolder.getLocale());
			excObj.put(CommonConstants.ERROR_CODE, errorCode);
			excObj.put(CommonConstants.ERROR_DESC,msg);
			excObj.put(CommonConstants.ERROR_GRP, errorGrp);
			excObj.put(CommonConstants.ERROR_HTTP_CODE, status);
			log.error(msg);
			throw new AspExceptionV31(msg, null, false, false,
					excObj);
		}
	}

	/**
	 * This method is generic method to throw AspExceptionV31.
	 * 
	 * @param errorCode
	 *            error code to be given back in response
	 * @param errorGrp group to which this error belongs like, Summary, Save invoice, file gstr etc
	 * @param messageKey, error key which is stored in errorMessage.properties file.
	 * @param status, httpstatus code which needs to be added in the response.
	 * @param param
	 *            Parameter to be replaced in error message incase if it has
	 *            dynamic parameter.
	 * @param messageSourceV31, Message resource which stores all the error messages.
	 * @param e exception object if present should be passed.
	 */
	public static void throwException(String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSourceV31, Throwable e){
		Map<String, Object> excObj = new HashMap<>();
		excObj.put(CommonConstants.ERROR_CODE, errorCode);
		String msg = messageSourceV31.getMessage(messageKey, param, LocaleContextHolder.getLocale());
		excObj.put(CommonConstants.ERROR_DESC, msg);
		excObj.put(CommonConstants.ERROR_GRP, errorGrp);
		excObj.put(CommonConstants.ERROR_HTTP_CODE, status);
		throw new AspExceptionV31(msg, e, false, false,
				excObj);
	}
	


}
