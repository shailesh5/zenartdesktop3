/**
 * 
 */
package com.jio.asp.gstr1.v30.service;

import java.util.Map;

/**
 * @author amit1.dwivedi
 *
 */
public interface GstnSummaryService {
	Map<String, String> validateApiInput(Map<String, String> inputMap);

	Map<String, Object> processGstr1InvoiceDataL2(Map<String, String> headers, Map<String, String> data);

	Map<String, String> validateApiInputL2(Map<String, String> inputMap);

	Map<String, Object> processGstr1InvoiceDataL0(Map<String, String> reqHeaderMap, Map<String, String> data);

}
