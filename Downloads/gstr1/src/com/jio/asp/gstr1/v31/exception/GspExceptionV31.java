/**
 * 
 */
package com.jio.asp.gstr1.v31.exception;

import java.util.Map;

/**
 * @author Rohit1.Soni
 *
 */
public class GspExceptionV31 extends AspExceptionV31 {
	/**
	 * 
	 */
	private static final long serialVersionUID = 886530303715571393L;

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 * @param excObj
	 */
	public GspExceptionV31(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
			Map<String, Object> excObj) {
		super(message, cause, enableSuppression, writableStackTrace,excObj);
	}

}
