package com.jio.asp.gstr1.v30.service;

import java.util.Map;

public interface SaveSuppliesToGstnService {

	String processSuppliesData (Map<String, String> allRequestParams, Map<String, String> reqHeaderMap);
}
