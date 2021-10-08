/**
 * 
 */
package com.jio.asp.gstr1.v30.service;

import java.util.Map;

/**
 * @author Amit1.Dwivedi
 *
 */
public interface AspSuppliesSumService {

	Map<String, String> validateApiInput(Map<String, String> inputMap);

	Map<String, Object> processGstr1InvoiceDataL2(Map<String, String> headers, Map<String, String> data);

}
