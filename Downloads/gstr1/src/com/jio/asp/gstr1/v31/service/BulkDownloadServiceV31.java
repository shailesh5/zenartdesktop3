/**
 * 
 */
package com.jio.asp.gstr1.v31.service;

import java.util.Map;

/**
 * @author Amit1.Dwivedi
 *
 */
public interface BulkDownloadServiceV31 {



	Map<String, Object> bulkDataDecription(String data, String ek);

	Map<String, String> getUrlList(Map<String, String> data, Map<String, String> headerData, String token);

	Map<String, String> getBulkDataByUrl(Map<String, String> data, Map<String, String> headerData,
			Map<String, Object> url);

}
