/**
 * 
 */
package com.jio.asp.gstr1.v30.service;

import java.util.Map;

/**
 * @author Rohit1.Soni
 *
 */
public interface AspSuppliesStatusService {

	/**
	 * method is the initiation point for supplies status query. This method
	 * takes care of calling all the necessary utility functions to gather and
	 * format the data into required output.
	 * 
	 * @param inputMap,
	 *            this is the input which has been sent from client.
	 * @return String containing the json format of the data returned from
	 *         database.
	 */
	String getSuppliesStatus(Map<String, String> inputMap);

	/**
	 * Method does the api input validation. Data which is needed for request
	 * processing and quering from database and GSP
	 * 
	 * @param inputMap
	 *            this object contains user input parameter for searching of the
	 *            user data.
	 * @return Map, which contains error information or processed data
	 */
	void validateApiInput(Map<String, String> inputMap);

}
