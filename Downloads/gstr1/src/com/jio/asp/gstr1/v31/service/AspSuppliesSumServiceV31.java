/**
 * 
 */
package com.jio.asp.gstr1.v31.service;

import java.util.Map;

/**
 * @author Amit1.Dwivedi
 *
 */
public interface AspSuppliesSumServiceV31 {

	Map<String, String> validateApiInput(Map<String, String> inputMap);
	
	public void validateFilterInput(Map<String, String> filter);

	Map<String, Object> processGstr1InvoiceDataL2(Map<String, String> headers, Map<String, String> data);
	
	Map<String, Object> processReportDataL2(Map<String, String> headers, Map<String, String> data);

}
