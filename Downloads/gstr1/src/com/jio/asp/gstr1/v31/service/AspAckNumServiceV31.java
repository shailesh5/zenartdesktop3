package com.jio.asp.gstr1.v31.service;

/**
 * This class is used for generating unique acknowledgement number.
 * 
 * @author Rohit1.Soni
 *
 */
public interface AspAckNumServiceV31 {

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
	String generateAckNumber(String fromApi);

}
