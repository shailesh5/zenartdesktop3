package com.jio.asp.gstr1.v31.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;

@Service
public class Gstr1SummaryServiceV31Impl implements Gstr1SummaryServiceV31 {

	private static final Logger log = LoggerFactory.getLogger(Gstr1SummaryServiceV31Impl.class);

	@Autowired
	private AspMongoDaoV31 aspMongoDaoV31;

	@Autowired
	private MessageSource messageSourceV31;
	
	@Autowired
	private MessageSource gstnResource;
	private static List<String> filterList;
	static {
		filterList = new ArrayList<>();
		filterList.add("ttl_count");
		filterList.add("GstnNew_cnt");
		filterList.add("GstnFailed_cnt");
		filterList.add("GstnSaved_cnt");
		filterList.add("GstnSubmitted_cnt");
		filterList.add("GstnUpload_cnt");
		filterList.add("GstnFiled_cnt");
	}

	@Override
	public Map processGstr1SummaryL0(Map<String, String> allRequestParams) {
		log.debug("processGstr1SummaryDataL0 method : START");
		String gstr1GstnCol = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
		List<Map> listMap = aspMongoDaoV31.getSuppliesDataL0(allRequestParams, gstr1GstnCol);
		Map finalMap = formatOutputList(allRequestParams, listMap);
		log.debug("processGstr1SummaryDataL0 method : END");
		return finalMap;
	}

	private Map formatOutputList(Map<String, String> allParams, List<Map> allMapList) {

		Map<String, Object> map = new HashMap();
		List<Object> ls = new ArrayList<>();
		if (allMapList != null && allMapList.size() > 0) {
			for (Map m : allMapList) {
				List<Map<String, Object>> sectionAll = (List<Map<String, Object>>) m.get("sections");
				if (sectionAll != null) {
					roundOffFn(sectionAll);
					ls.addAll(sectionAll);
				}
			}

			if (ls.size() > 0) {
				map.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, allParams);
				map.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, ls);
			} else {
				Map<String, String> dataMap = new HashMap<>();
				dataMap.put(Gstr1ConstantsV31.CONTROL_REC_STATUS, Gstr1ConstantsV31.ERROR);
				dataMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, "No Data Available for given criteria");
				map.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, allParams);
				map.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);
			}
		} else {
			Map<String, String> dataMap = new HashMap<>();
			dataMap.put(Gstr1ConstantsV31.CONTROL_REC_STATUS, Gstr1ConstantsV31.ERROR);
			dataMap.put(Gstr1ConstantsV31.ERROR_MESSAGE, "No Data Available for given criteria");
			map.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, allParams);
			map.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, dataMap);

		}
		return map;
	}
	
	private void roundOffFn(List<Map<String, Object>> sectionAll) {
		for (Map<String, Object> m : sectionAll) {
			Iterator<String> itr = m.keySet().iterator();
			while (itr.hasNext()) {
				String recKey = itr.next();
				if (m.get(recKey) instanceof Number && !filterList.contains(recKey)) {
					BigDecimal c = new BigDecimal(String.valueOf(m.get(recKey)));
					if(!(m.get(recKey) instanceof Integer)){
						c = c.setScale(2, BigDecimal.ROUND_HALF_UP);
					}
					m.put(recKey, c);
				}
			}
		}

	}
	
	public void validateParams(Map<String, String> inputMap) {

		
		CommonUtil.validateEmptyString(MapUtils.getString(inputMap,Gstr1ConstantsV31.HEADER_GSTIN,""),
				ErrorCodesV31.ASP011031, Gstr1ConstantsV31.JIOGST_L0, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		CommonUtil.validateEmptyString(MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_FP,""),
				ErrorCodesV31.ASP011311, Gstr1ConstantsV31.JIOGST_L0, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

		}


}
