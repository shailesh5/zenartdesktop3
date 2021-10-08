/**
 * 
 */
package com.jio.asp.gstr1.v31.exception;

import java.util.Map;

/**
 * @author Rohit1.Soni
 *
 */
public class AspExceptionV31 extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6125976525557460700L;

	private Map<String, Object> excObj;
	
	public AspExceptionV31(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
			Map<String, Object> excObj) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.excObj=excObj;
	}
	

	public Map<String, Object> getExcObj() {
		return excObj;
	}

	public void setExcObj(Map<String, Object> excObj) {
		this.excObj = excObj;
	}
	
	
}
