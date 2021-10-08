package com.jio.asp.gstr1.v31.util;

/**
 * This class will be used to post the body content to rest api call 
 * this will be used in GSPServiceV31Impl, callGsp method.
 * Based on the parameter set system will assign the entity type 
 * and make call to GSP.
 * 
 * @author Rohit1.Soni
 *
 * @param <T>
 */
public class BodyV31<T> {
	
	
	private T requestBody;

	/**
	 * @return the requestBody
	 */
	public T getRequestBody() {
		return requestBody;
	}

	/**
	 * @param requestBody the requestBody to set
	 */
	public void setRequestBody(T requestBody) {
		this.requestBody = requestBody;
	}

}
