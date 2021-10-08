package com.jio.asp.gstr1.v31.service;

import java.util.Map;


public interface Gstr1SummaryServiceV31 {	

	public Map processGstr1SummaryL0(Map<String, String> allRequestParams);

	public void validateParams(Map<String, String> allRequestParams);

}
