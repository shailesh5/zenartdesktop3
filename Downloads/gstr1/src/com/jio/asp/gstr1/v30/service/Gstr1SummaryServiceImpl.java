package com.jio.asp.gstr1.v30.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.dao.AspMongoDao;
import com.jio.asp.gstr1.v30.util.CommonUtil;

@Service
public class Gstr1SummaryServiceImpl implements Gstr1SummaryService {

	private static final Logger log = LoggerFactory.getLogger(Gstr1SummaryServiceImpl.class);

	@Autowired
	private AspMongoDao aspMongoDao;

	@Autowired
	private MessageSource messageSource;

	@Override
	public Map processGstr1SummaryL0(Map<String, String> allRequestParams) {
		log.debug("processGstr1SummaryDataL0 method : START");
		List<Map> listMap = aspMongoDao.getSuppliesDataL0(allRequestParams, Gstr1Constants.L0_COLLECTION_GSTR1);
 		Map finalMap = formatOutputList(allRequestParams, listMap);
		log.debug("processGstr1SummaryDataL0 method : END");
		return finalMap;
	}

	private Map formatOutputList(Map<String, String> allParams, List<Map> allMapList) {

		Map<String, Object> map = new HashMap();
		List<Object> ls = new ArrayList<>();
		if(allMapList != null && allMapList.size() > 0){
		for (Map m : allMapList) {
			List<Map<String, Object>> sectionAll = (List<Map<String, Object>>) m.get("sections");
			if (sectionAll != null) {
				ls.addAll(sectionAll);
			}
		}
		
		if (ls.size() > 0) {
			map.put(Gstr1Constants.ASP_API_RESP_META_ATTR, allParams);
			map.put(Gstr1Constants.ASP_API_RESP_DATA_ATTR, ls);
		} else {
			Map<String, String> dataMap = new HashMap<>();
			dataMap.put(Gstr1Constants.CONTROL_REC_STATUS, Gstr1Constants.ERROR);
			dataMap.put(Gstr1Constants.ERROR_MESSAGE, "No Data Available for given criteria");
			map.put(Gstr1Constants.ASP_API_RESP_META_ATTR, allParams);
			map.put(Gstr1Constants.ASP_API_RESP_DATA_ATTR, dataMap);
		}
		}else{
			Map<String, String> dataMap = new HashMap<>();
			dataMap.put(Gstr1Constants.CONTROL_REC_STATUS, Gstr1Constants.ERROR);
			dataMap.put(Gstr1Constants.ERROR_MESSAGE, "No Data Available for given criteria");
			map.put(Gstr1Constants.ASP_API_RESP_META_ATTR, allParams);
			map.put(Gstr1Constants.ASP_API_RESP_DATA_ATTR, dataMap);
			
		}
		return map;
	}



}
