/**
 * 
 */
package com.jio.asp.gstr1.common.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Rohit1.Soni
 *
 */
@Repository
public class CommonDaoImpl implements CommonDao {

	private static Logger log = LoggerFactory.getLogger(CommonDaoImpl.class);
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<Map<String, Object>> getMongoData(Map<String, Object> object, String collection) {
		log.debug("saveInMongo method: START");
		List<Map<String, Object>> result = null;
		if (object != null) {
			Query query = new Query();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				Object obj = MapUtils.getObject(object, key, "");
				query.addCriteria(Criteria.where(key).is(obj));
			}
			Map<String, Object> map = new HashMap<>();
			result = (List<Map<String, Object>>) mongoTemplate.find(query, map.getClass(), collection);
			log.debug("saveInMongo method: After inserting in mongo database");
		} else {
			log.debug("saveInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("saveInMongo method: END");
		return result;
	}

	@Override
	public void saveInMongo(Object object, String collection) {
		log.debug("saveInMongo method: START");
		if (object != null) {
			mongoTemplate.save(object.toString(), collection);
			log.debug("saveInMongo method: After inserting in mongo database");
		} else {
			log.debug("saveInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("saveInMongo method: END");
	}

}
