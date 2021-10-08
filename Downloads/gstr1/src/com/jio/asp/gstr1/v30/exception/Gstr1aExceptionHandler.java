/**
 * 
 */
package com.jio.asp.gstr1.v30.exception;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;

/**
 * @author Rohit1.Soni
 *
 */
@ControllerAdvice
public class Gstr1aExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(Gstr1aExceptionHandler.class);
	@Autowired
	private MessageSource messageSource;

	/**
	 * Exception handler for summary page. Any exception which occurs in
	 * downstream code will come to this method and this method generates
	 * appropriate output for the client.
	 * 
	 * @param request
	 *            Http request object
	 * @param ex
	 *            exception object
	 * @return responseentity containing respose json.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> exceptionHandler(HttpServletRequest request, Exception ex) {
		log.error("Requested URL=" + request.getRequestURL());
		ResponseEntity<Map<String, Object>> entity;
		Map<String, Object> exObject;
		exObject = new HashMap<>();
		exObject.put(Gstr1Constants.ERROR_CODE, ErrorCodes.GENERIC_STATUS_CODE);
		exObject.put(Gstr1Constants.ERROR_DESC, convertExceptionClass(ex));
		exObject.put(Gstr1Constants.ERROR_GRP, Gstr1Constants.GENERIC);
		log.error("exceptionHandler method : Connection error occurred.{}", ex);
		entity = new ResponseEntity<>(exObject, HttpStatus.GATEWAY_TIMEOUT);
		return entity;
	}

	/**
	 * method make the string for generic error block.
	 * 
	 * @param ex, exception object
	 * @return String containing error message
	 */
	private String convertExceptionClass(Exception ex) {
		String name = "Unknown";
		if (ex != null && ex.getClass() != null) {
			name = messageSource.getMessage("IDENTIFIED_ERROR", new Object[] { ex.getClass().getSimpleName() },
					LocaleContextHolder.getLocale());
		}
		if (StringUtils.isEmpty(name) || StringUtils.equals("Unknown", name)) {
			name = messageSource.getMessage("UNIDENTIFIED_ERROR", null, LocaleContextHolder.getLocale());
		}
		return name;
	}

}
