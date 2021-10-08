package com.jio.asp.gstr1.v30.service;

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

import com.jio.asp.gstr1.v30.dao.AspMongoDao;

@Service
public class AspLoggingServiceImpl implements AspLoggingService {

	private static final Logger log = LoggerFactory.getLogger(AspLoggingServiceImpl.class);

	@Autowired
	private AspMongoDao mongoDao;
	@Autowired
	private MessageSource gstnResource;

	@Override
	@Async(value = "taskExecutor")
	public void generateControlLog(Map<String, Object> controlDataMap) {
		log.debug("generateControlLog Method: START");
		String gstr1StatusCol=gstnResource.getMessage("trans.status.col", null, LocaleContextHolder.getLocale());
		if (controlDataMap != null) {
			JSONObject jsonHeader = new JSONObject(controlDataMap);
			log.info("START ********************* ackNo : {} ", controlDataMap.get("ackNo"));
			Iterator<String> itr = controlDataMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
			//	log.info("{}: {} ", key, controlDataMap.get(key));
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
		String fileStatusCol=gstnResource.getMessage("file.trans.status.col", null, LocaleContextHolder.getLocale());
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
	public void updateControlLog(Map<String, Object> controlDataMap,Map<String, Number> incRowMap,String id) {
		log.debug("updateControlLog Method: START");
		String gstr1StatusCol=gstnResource.getMessage("trans.status.col", null, LocaleContextHolder.getLocale());
		if ((controlDataMap != null || incRowMap!=null) && StringUtils.isNotBlank(id)) {
			mongoDao.updateInMongo(controlDataMap, gstr1StatusCol,id,incRowMap);
		}
		log.debug("updateControlLog Method: END");
	}

}
