/**
 * 
 */
package com.jio.asp.gstr1.v31.dao;

import java.util.Map;

/**
 * @author Amit1.Dwivedi
 *
 */
public interface AspSuppliesDaoV31 {

	Map<String, Object> retrieveSummaryData(Map<String, String> inputMap);
	Map<String, Object> retrieveComparisonData(Map<String, String> inputMap);
	Map<String, Object> retrieveReportSummaryData(Map<String, String> inputMap);
}
