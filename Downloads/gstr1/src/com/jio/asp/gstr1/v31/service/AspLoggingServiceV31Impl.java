package com.jio.asp.gstr1.v31.service;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;

@Service
public class AspLoggingServiceV31Impl implements AspLoggingServiceV31 {

	private static final Logger log = LoggerFactory.getLogger(AspLoggingServiceV31Impl.class);

	@Autowired
	private AspMongoDaoV31 mongoDao;
	@Autowired
	private MessageSource gstnResource;

	@Override
	@Async(value = "taskExecutor")
	public void generateControlLog(Map<String, Object> controlDataMap) {
		log.debug("generateControlLog Method: START");
		String gstr1StatusCol = gstnResource.getMessage("trans.status.col", null, LocaleContextHolder.getLocale());
		if (controlDataMap != null) {
			JSONObject jsonHeader = new JSONObject(controlDataMap);
			log.info("START ********************* ackNo : {} ", controlDataMap.get("ackNo"));
			Iterator<String> itr = controlDataMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				log.info("{}: {} ", key, controlDataMap.get(key));
			}
			log.info("END ********************* ackNo : {} ", controlDataMap.get("ackNo"));
			mongoDao.saveInMongo(jsonHeader, gstr1StatusCol);
		}
		log.debug("generateControlLog Method: END");
	}

	@Override
	@Async(value = "taskExecutor")
	public void generateFileControlLog(Map<String, Object> controlDataMap) {
		log.debug("generateControlLog Method: START");
		String fileStatusCol = gstnResource.getMessage("file.trans.status.col", null, LocaleContextHolder.getLocale());
		if (controlDataMap != null) {
			JSONObject jsonHeader = new JSONObject(controlDataMap);
			Iterator<String> itr = controlDataMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				log.info("{}: {} ", key, controlDataMap.get(key));
			}
			mongoDao.saveInMongo(jsonHeader, fileStatusCol);
		}
		log.debug("generateControlLog Method: END");
	}

	@Override
	public void updateControlLog(Map<String, Object> controlDataMap, Map<String, Number> incRowMap, String id) {
		log.debug("updateControlLog Method: START");
		String gstr1StatusCol = gstnResource.getMessage("trans.status.col", null, LocaleContextHolder.getLocale());
		if ((controlDataMap != null || incRowMap != null) && StringUtils.isNotBlank(id)) {
			mongoDao.updateInMongo(controlDataMap, gstr1StatusCol, id, incRowMap);
		}
		log.debug("updateControlLog Method: END");
	}

	// Async Impl save to GSTN, RohitS
	@Override
	public void generateControlLogSync(Map<String, Object> controlDataMap, String collection) {
		log.debug("generateControlLog - SAVE to GSTN Method: START");
		if (controlDataMap != null) {
			JSONObject jsonHeader = new JSONObject(controlDataMap);
			log.info("********************* ackNo : {} ", controlDataMap.get("ackNo"));
			mongoDao.saveInMongo(jsonHeader, collection);
		}
		log.debug("generateControlLog - SAVE to GSTN Method: END");
	}

	// Async Impl save to GSTN, RohitS
	@Override
	public void updateControlLogSync(Map<String, Object> controlDataMap, Map<String, Number> incRowMap, String id,
			String collection) {
		log.debug("updateControlLog - SAVE to GSTN Method: START");
		if ((controlDataMap != null || incRowMap != null) && StringUtils.isNotBlank(id)) {
			mongoDao.updateInMongo(controlDataMap, collection, id, incRowMap);
		}
		log.debug("updateControlLog - SAVE to GSTN Method: END");
	}

}
