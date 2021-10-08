/**
 * 
 */
package com.jio.asp.gstr1.v30.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.service.KafkaProducerImpl;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;

/**
 * @author Amit1.Dwivedi
 *
 */
@Repository
public class AspSuppliesDaoImpl implements AspSuppliesDao {

	private static final Logger log = LoggerFactory.getLogger(KafkaProducerImpl.class);

	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private MessageSource gstnResource;
	@Autowired
	private Environment environment;

	@Override
	public Map<String, Object> retrieveSummaryData(Map<String, String> inputMap) {
		log.debug("retrieveSummaryData L2 method: START");
		String gstr1Col = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
		List<Object> l2 = null;
		Map<String, Object> respMap = null;
		DBCursor cursor = null;
		try {
			if (mongoTemplate.collectionExists(gstr1Col)) {
				DBCollection monogCollection = mongoTemplate.getCollection(gstr1Col);
				BasicDBObject whereQuery = new BasicDBObject();

				ReadPreference rp = null;
				String fp = (String) inputMap.get(Gstr1Constants.INPUT_FP);
				String gstin = (String) inputMap.get(Gstr1Constants.INPUT_GSTN);
				String section = (String) inputMap.get(Gstr1Constants.INPUT_SECTION);
				String status = (String) inputMap.get(Gstr1Constants.INPUT_STATUS);
				if (status == null || status.isEmpty()) {
					status = "";
				}
				respMap = new HashMap<>();
				/*
				 * String sort = (String)
				 * inputMap.get(Gstr1Constants.INPUT_SORT_BY); int order; try {
				 * order = Integer.parseInt((String)
				 * inputMap.get(Gstr1Constants.INPUT_SORT_ORDER)); } catch
				 * (NumberFormatException e1) { order = 1; } if (sort == null ||
				 * sort.isEmpty()) { sort = "gstn"; }
				 */

				int limit = 0;
				int skip = 0;
				try {
					limit = Integer.parseInt((String) inputMap.get(Gstr1Constants.INPUT_LIMIT));
					skip = Integer.parseInt((String) inputMap.get(Gstr1Constants.INPUT_OFFSET));
					int max_records = Integer.parseInt(environment.getProperty("max_records_fetched"));
					if ((limit - skip) > max_records) {
						log.info(
								"retrieveSummaryData L2 Method:Setting the max limit of L2 query reqlimit:{},maxLimit:{} ",
								limit, max_records);
						log.debug(
								"retrieveSummaryData L2 Method:Setting the max limit of L2 query reqlimit:{},maxLimit:{} ",
								limit, max_records);
						limit = max_records;
						respMap.put(Gstr1Constants.ASP_API_RESP_WARN_ATTR,
								"Only " + max_records + " records could be displayed at a time");
					}
				} catch (Exception e) {
					limit = 20;
					skip = 0;
				}
				log.info(
						"retrieveSummaryData L2 Method:Query for Criteria fp:{}, gstin:{}, section:{},limit:{},offset:{} ",
						fp, gstin, section, limit, skip);
				log.debug(
						"retrieveSummaryData L2 Method:Query for Criteria fp:{}, gstin:{}, section:{},limit:{},offset:{} ",
						fp, gstin, section, limit, skip);

				whereQuery.put("header.gstin", gstin);
				whereQuery.put("control.type", section.toLowerCase());
				if (Gstr1Constants.INVOICE_STATE_L2_PARK.equals(status)) {
					whereQuery.put("control.status", Gstr1Constants.INVOICE_STATE_PARK);
					List<String> fpList = new ArrayList<>();
					String fMonth = fp.substring(0, 2);
					String fYear = fp.substring(2, 6);
					int fM = Integer.parseInt(fMonth);
					int fY = Integer.parseInt(fYear);

					if (fM < 3) {
						int previousYear = 0;
						previousYear = previousYear - 1;
						for (int i = 4; i == 12; i++) {
							fpList.add("0" + i + String.valueOf(previousYear));
						}
						for (int j = 1; j <= fM; j++) {
							fpList.add("0" + j + fYear);
						}
					} else {
						for (int j = 4; j <= fM; j++) {
							fpList.add("0" + j + fYear);
						}

					}
					whereQuery.put("header.fp", new BasicDBObject("$in", fpList));
				} else {

					whereQuery.put("header.fp", fp);
					whereQuery.put("control.status", new BasicDBObject("$ne", Gstr1Constants.INVOICE_STATE_DEL));
				}
				int ttlCount = monogCollection.find(whereQuery).count();
				// cursor =
				// monogCollection.find(whereQuery).limit(limit).skip(skip).sort(new
				// BasicDBObject(sort, order));
				cursor = monogCollection.find(whereQuery).limit(limit).skip(skip).setReadPreference(rp.nearest());
				log.debug("retrieveSummaryData L2 Method:After getting result from database");
				DBObject headerObj = null;
				l2 = new ArrayList<>();
				while (cursor.hasNext()) {
					DBObject obj = cursor.next();
					headerObj = (DBObject) obj.removeField(Gstr1Constants.CONTROL_JSON_HDR_SEC);
					DBObject gstnObj = (DBObject) obj.removeField(Gstr1Constants.CONTROL_JSON_GSTN_SEC);
					DBObject controlObj = (DBObject) obj.removeField(Gstr1Constants.CONTROL_JSON_SEC);
					String dbStatus = (String) controlObj.get("status");
					
					obj.removeField(Gstr1Constants.CONTROL_REC_ID);
					if (gstnObj instanceof BasicDBObject) {
						gstnObj.put(Gstr1Constants.CONTROL_JSON_CUST_SEC,
								obj.get(Gstr1Constants.CONTROL_JSON_CUST_SEC));
						// if need uniform structure uncomment this section
						// if(!gstnObj.containsKey(Gstr1Constants.CONTROL_JSON_CUST_SEC_L2_List)){
						// Map<String , Object> itemMap=new HashMap<>();
						// List<Object> innerL2 = new ArrayList<>();
						// innerL2.add(gstnObj);
						// itemMap.put(Gstr1Constants.CONTROL_JSON_CUST_SEC_L2_List,
						// innerL2);
						// l2.add(itemMap);
						// }
						// else
						gstnObj.put("status", dbStatus);
						l2.add(gstnObj);

					} else if (gstnObj instanceof BasicDBList) {
						List<Object> innerL2 = new ArrayList<>();
						DBObject customObj = new BasicDBObject();
						customObj.put(Gstr1Constants.CONTROL_JSON_CUST_SEC,
								obj.get(Gstr1Constants.CONTROL_JSON_CUST_SEC));
						((BasicDBList) gstnObj).add(customObj);
						innerL2.addAll(((BasicDBList) gstnObj));
						// change regarding new structure
						Map<String, Object> itemMap = new HashMap<>();
						itemMap.put(Gstr1Constants.CONTROL_JSON_CUST_SEC_L2_List, innerL2);
						itemMap.put("status", dbStatus);
						l2.add(itemMap);
						// l2.add(innerL2);
					}

				}
				if (CollectionUtils.isNotEmpty(l2)) {
					respMap.putAll(headerObj.toMap());
					respMap.put(Gstr1Constants.ASP_API_RESP_RCRD_ATTR, l2);
					respMap.put(Gstr1Constants.JSON_TTL_RCRD, ttlCount);
				} else {
					respMap = null;
				}
				log.info("retrieveSummaryData L2 Method:After forming the response ");
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		log.debug("retrieveSummaryData L2 method: END");
		return respMap;
	}

}
