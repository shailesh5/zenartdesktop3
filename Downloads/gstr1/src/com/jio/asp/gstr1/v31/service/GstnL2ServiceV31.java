/**
 * 
 */
package com.jio.asp.gstr1.v31.service;

import java.util.Map;

/**
 * @author Amit1.Dwivedi
 *
 */
public interface GstnL2ServiceV31 {

	Map<String, Object> processL2(Map<String, String> reqHeaderMap, Map<String, String> data);

}
