/**
 * 
 */
package com.jio.asp.gstr1.v30.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v30.service.KafkaProducerImpl;

/**
 * @author Rohit1.Soni
 *
 */
@Repository
public class AspMongoDaoImpl implements AspMongoDao {

	private static final Logger log = LoggerFactory.getLogger(KafkaProducerImpl.class);

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private MessageSource gstnResource;

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

	@Override
	public void updateInMongo(Map<String, Object> object, String collection, String id, Map<String, Number> incObject) {
		log.debug("updateInMongo method: START");
		Update update = null;
		if (object != null ) {
			update=new Update();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				update.set(key, object.get(key));
			}
		}
		if(incObject!=null){
			update=(update==null)? new Update():update;
			Iterator<String> incItr = incObject.keySet().iterator();
			while (incItr.hasNext()) {
				String key = (String) incItr.next();
				update.inc(key, incObject.get(key));
			}
		}
		if(update!=null){
			mongoTemplate.upsert(new Query(Criteria.where("_id").is(id)), update, collection);
			log.debug("updateInMongo method: After updating in mongo database");
		} else {
			log.debug("updateInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("updateInMongo method: END");
	}
	

	@Override
	public List<Map<String, Object>> getMongoData(Map<String, Object> object, String collection) {
		log.debug("saveInMongo method: START");
		List<Map<String, Object>> result= null;
		if (object != null) {
			Query query = new Query();
			Iterator<String> itr= object.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				Object obj=MapUtils.getObject(object,key,"");
				query.addCriteria(Criteria.where(key).is(obj));
			}
			Map<String, Object> map=new HashMap<>();
			result=(List<Map<String, Object>>) mongoTemplate.find(query,map.getClass(), collection);
			log.debug("saveInMongo method: After inserting in mongo database");
		} else {
			log.debug("saveInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("saveInMongo method: END");
		return result;
	}

public List<Map> getSuppliesDataL0 (Map<String, String> allRequestParams, String collection){
		
	log.debug("getSuppliesDataL0 method: START");

	String gstin = allRequestParams.get(Gstr1Constants.INPUT_GSTN);
	String fp = allRequestParams.get(Gstr1Constants.INPUT_FP);
	String level = allRequestParams.get(Gstr1Constants.INPUT_LEVEL);

	List<Map> result = null;
	Query query = new Query();
	query.addCriteria(Criteria.where("gstin").is(gstin).and("fp").is(fp).and("action").is(level));

	result = mongoTemplate.find(query, Map.class, collection);

	log.debug("getSuppliesDataL0 method: After inserting in mongo database");
	log.debug("getSuppliesDataL0 method: END");
	return result;
	}

   public List<Map> getDataForGstn (Map<String, String> allRequestParams, String collection){
	
	log.debug("getDataForGstn method: START");
	log.info("getDataForGstn method: START");
	String gstin = allRequestParams.get(Gstr1Constants.INPUT_GSTN);
	String fp = allRequestParams.get(Gstr1Constants.INPUT_FP);
	
	Map result = null;
	List<Map> mapList = new ArrayList<>();
	Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where(Gstr1Constants.RESULT_GSTN).is(gstin).and(Gstr1Constants.RESULT_FP).is(fp)))
            .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
	AggregationResults<Map> resultAgg = mongoTemplate.aggregate(aggregation, collection, Map.class);
	if(resultAgg != null){
	result = resultAgg.getRawResults().toMap();
	Map map = (Map) result.get(Gstr1Constants.CURSOR);
	Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
	while (entries.hasNext()) {
	    Map.Entry<String, Object> entry = entries.next();
	    Object obj = entry.getValue();
	    if(obj instanceof List<?>){
	    	List<Map> listM = (List<Map>) obj;
	    	mapList.addAll(listM);
	    	
	    }

	}
	}
	
	log.debug("getDataForGstn method: After inserting in mongo database");
	log.info("getDataForGstn method: END");
	return mapList;
	}
	
}
