package com.jio.asp.gstr1.v31.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;

@Service
public class AspUpdateStatusServiceImpl implements AspUpdateStatusService {
	@Autowired
	private AspMongoDaoV31 mongoDao;
	@Autowired
	private MessageSource messageSourceV31;
	@Autowired
	private Environment env;

	@Override
	public boolean processGstinMasterData(Map<String, Object> requestBody, Map<String, String> reqHeaderMap,
			String ackNo) {
		boolean status = false;
		String fp = MapUtils.getString(requestBody, Gstr1ConstantsV31.INPUT_FP);
		String gstin = MapUtils.getString(requestBody, Gstr1ConstantsV31.JSON_GSTIN);
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID, gstin);
		List<Map<String, Object>> data = mongoDao.getMongoData(queryMap, env.getProperty("gstinMaster.col"));
		if (CollectionUtils.isEmpty(data)) {
			Map<String, Object> gstinMaster = new HashMap<>();
			gstinMaster.put(Gstr1ConstantsV31.CONTROL_JSON_ID, gstin);
			gstinMaster.put(Gstr1ConstantsV31.JSON_GSTIN, gstin);
			gstinMaster.put(Gstr1ConstantsV31.API_NAME, "");
			JSONObject jsonHeader = new JSONObject(gstinMaster);
			mongoDao.saveInMongo(jsonHeader, env.getProperty("gstinMaster.col"));
			status = true;
		} else {
			for (Map<String, Object> map : data) {
				if (map.containsKey(Gstr1ConstantsV31.API_NAME)) {
					String savedFp = MapUtils.getString(map, Gstr1ConstantsV31.API_NAME);
					if (StringUtils.isBlank(savedFp)) {
						status = true;
					} else {
						if (savedFp.length() == 6 && fp.length() == 6) {
							String mn = savedFp.substring(0, 2);
							String yr = savedFp.substring(2, 6);
							String savedFp1 = yr + mn;
							String mn1 = fp.substring(0, 2);
							String yr1 = fp.substring(2, 6);
							String inputFp = yr1 + mn1;
							if (inputFp != null && savedFp1 != null) {
								if (inputFp.compareTo(savedFp1) <= 0) {
									status = false;
								} else {
									status = true;
								}
							} else {
								CommonUtilV31.throwException(ErrorCodesV31.ASP524,
										Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP, ErrorCodesV31.ASP524, null,
										HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, null);
							}
						} else {
							CommonUtilV31.throwException(ErrorCodesV31.ASP524, Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP,
									ErrorCodesV31.ASP524, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, null);
						}
					}
				} else {
					Map<String, Object> gstinMaster = new HashMap<>();
					gstinMaster.put(Gstr1ConstantsV31.CONTROL_JSON_ID, gstin);
					gstinMaster.put(Gstr1ConstantsV31.API_NAME, "");
					mongoDao.updateInMongo(gstinMaster, env.getProperty("gstinMaster.col"), gstin, null);
					status = true;
				}
				break;
			}

		}
		return status;
	}

	@Override
	public boolean updateGstinMasterData(Map<String, Object> requestBody, Map<String, String> reqHeaderMap,
			String ackNo) {
		boolean status = false;
		String fp = MapUtils.getString(requestBody, Gstr1ConstantsV31.INPUT_FP,
				MapUtils.getString(requestBody, Gstr1ConstantsV31.INPUT_GSTN_RET_PER,null));
		String gstin = MapUtils.getString(requestBody, Gstr1ConstantsV31.JSON_GSTIN);
		if (MapUtils.isEmpty(requestBody) || StringUtils.isBlank(fp) || StringUtils.isBlank(gstin)) {
			CommonUtilV31.throwException(ErrorCodesV31.ASP524,
					Gstr1ConstantsV31.ASP_GSTR1A_SAVE_GRP, ErrorCodesV31.ASP524, null,
					HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, null);
		} else {
			Map<String, Object> gstinMaster = new HashMap<>();
			gstinMaster.put(Gstr1ConstantsV31.CONTROL_JSON_ID, gstin);
			gstinMaster.put(Gstr1ConstantsV31.JSON_GSTIN, gstin);
			gstinMaster.put(Gstr1ConstantsV31.API_NAME, fp);
			mongoDao.updateInMongo(gstinMaster, env.getProperty("gstinMaster.col"), gstin, null);
			status = true;
		}
		return status;
	}
}
