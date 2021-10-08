/**
 * 
 */
package com.jio.asp.gstr1.v30.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.exception.AspException;

/**
 * @author 
 *
 */
public class CommonUtil {
	
	
	public static final Logger log = LoggerFactory.getLogger(CommonUtil.class);
	
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
	 * @param messageSource, Message resource which stores all the error messages.
	 */
	public static void validateEmptyString(String value, String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSource) {
		if (StringUtils.isBlank(value)) {
			Map<String, Object> excObj = new HashMap<>();
			String msg=messageSource.getMessage(messageKey, param, LocaleContextHolder.getLocale());
			excObj.put(Gstr1Constants.ERROR_CODE, errorCode);
			excObj.put(Gstr1Constants.ERROR_DESC,msg);
			excObj.put(Gstr1Constants.ERROR_GRP, errorGrp);
			excObj.put(Gstr1Constants.ERROR_HTTP_CODE, status);
			log.error(msg);
			throw new AspException(msg, null, false, false,
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
	 * @param messageSource, Message resource which stores all the error messages.
	 */
	public static void validateEmpty(Object value, String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSource) {
		if (value==null) {
			Map<String, Object> excObj = new HashMap<>();
			String msg=messageSource.getMessage(messageKey, param, LocaleContextHolder.getLocale());
			excObj.put(Gstr1Constants.ERROR_CODE, errorCode);
			excObj.put(Gstr1Constants.ERROR_DESC,msg);
			excObj.put(Gstr1Constants.ERROR_GRP, errorGrp);
			excObj.put(Gstr1Constants.ERROR_HTTP_CODE, status);
			log.error(msg);
			throw new AspException(msg, null, false, false,
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
	 * @param messageSource, Message resource which stores all the error messages.
	 */
	public static void validateLength(String value, int minLength,int maxLength, String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSource) {
		int length=value.length();
		if (length<minLength || length>maxLength) {
			Map<String, Object> excObj = new HashMap<>();
			String msg=messageSource.getMessage(messageKey, param, LocaleContextHolder.getLocale());
			excObj.put(Gstr1Constants.ERROR_CODE, errorCode);
			excObj.put(Gstr1Constants.ERROR_DESC,msg);
			excObj.put(Gstr1Constants.ERROR_GRP, errorGrp);
			excObj.put(Gstr1Constants.ERROR_HTTP_CODE, status);
			log.error(msg);
			throw new AspException(msg, null, false, false,
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
	 * @param messageSource, Message resource which stores all the error messages.
	 */
	public static void validateInteger(String value, String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSource) {
		String regex = "\\d+";
		if (value==null || !value.matches(regex)) {
			Map<String, Object> excObj = new HashMap<>();
			String msg=messageSource.getMessage(messageKey, param, LocaleContextHolder.getLocale());
			excObj.put(Gstr1Constants.ERROR_CODE, errorCode);
			excObj.put(Gstr1Constants.ERROR_DESC,msg);
			excObj.put(Gstr1Constants.ERROR_GRP, errorGrp);
			excObj.put(Gstr1Constants.ERROR_HTTP_CODE, status);
			log.error(msg);
			throw new AspException(msg, null, false, false,
					excObj);
		}
	}

	/**
	 * This method is generic method to throw AspException.
	 * 
	 * @param errorCode
	 *            error code to be given back in response
	 * @param errorGrp group to which this error belongs like, Summary, Save invoice, file gstr etc
	 * @param messageKey, error key which is stored in errorMessage.properties file.
	 * @param status, httpstatus code which needs to be added in the response.
	 * @param param
	 *            Parameter to be replaced in error message incase if it has
	 *            dynamic parameter.
	 * @param messageSource, Message resource which stores all the error messages.
	 * @param e exception object if present should be passed.
	 */
	public static void throwException(String errorCode,String errorGrp, String messageKey, Object[] param, HttpStatus status,
			MessageSource messageSource, Throwable e){
		Map<String, Object> excObj = new HashMap<>();
		excObj.put(Gstr1Constants.ERROR_CODE, errorCode);
		String msg = messageSource.getMessage(messageKey, param, LocaleContextHolder.getLocale());
		excObj.put(Gstr1Constants.ERROR_DESC, msg);
		excObj.put(Gstr1Constants.ERROR_GRP, errorGrp);
		excObj.put(Gstr1Constants.ERROR_HTTP_CODE, status);
		throw new AspException(msg, e, false, false,
				excObj);
	}
	


}
