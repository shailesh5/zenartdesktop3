/**
 * 
 */
package com.jio.asp.gstr1.v31.service;

import java.util.Map;

/**
 * @author amit1.dwivedi
 *
 */
public interface GstnSummaryServiceV31 {
	Map<String, String> validateApiInput(Map<String, String> inputMap,String grpName);

	Map<String, Object> processGstr1InvoiceDataL2(Map<String, String> headers, Map<String, String> data);

	Map<String, String> validateApiInputL2(Map<String, String> allRequestParams,String grpName);

	Map<String, Object> processGstr1InvoiceDataL0(Map<String, String> reqHeaderMap, Map<String, String> data);

}
