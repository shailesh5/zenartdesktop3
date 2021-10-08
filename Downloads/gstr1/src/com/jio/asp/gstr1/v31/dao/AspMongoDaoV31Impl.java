/**
 * 
 */
package com.jio.asp.gstr1.v31.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.jongo.Aggregate;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.deser.DataFormatReaders.Match;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.mongodb.AggregationOptions;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.WriteResult;

/**
 * @author Rohit1.Soni
 *
 */
@Repository
public class AspMongoDaoV31Impl implements AspMongoDaoV31 {

	private static final Logger log = LoggerFactory.getLogger(AspMongoDaoV31Impl.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private MongoTemplate mongoGstnTemplate;

	@Autowired
	private Environment environment;

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

	public void updateInMongo(Map<String, Object> object, String collection, String id, Map<String, Number> incObject) {
		log.debug("updateInMongo method: START");
		Update update = null;
		if (object != null) {
			update = new Update();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				update.set(key, object.get(key));
			}
		}
		if (incObject != null) {
			update = (update == null) ? new Update() : update;
			Iterator<String> incItr = incObject.keySet().iterator();
			while (incItr.hasNext()) {
				String key = (String) incItr.next();
				update.inc(key, incObject.get(key));
			}
		}
		if (update != null) {
			mongoTemplate.upsert(new Query(Criteria.where("_id").is(id)), update, collection);
			log.debug("updateInMongo method: After updating in mongo database");
		} else {
			log.debug("updateInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("updateInMongo method: END");
	}

	@Override
	public void updateStatusInMongo(Map<String, Object> object, String collection,
			Map<String, String> allRequestParams) {
		log.debug("updateInMongo method: START");
		Update update = null;
		if (object != null) {
			update = new Update();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				update.set(key, object.get(key));
			}
		}

		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);

		if (update != null) {
			mongoTemplate.updateMulti(new Query(Criteria.where(Gstr1ConstantsV31.HEADER_GSTIN_DB).is(gstin)
					.and(Gstr1ConstantsV31.HEADER_FP_DB).is(fp).and(Gstr1ConstantsV31.CONTROL_STATUS).is("New")),
					update, collection);
			log.debug("updateInMongo method: After updating in mongo database");
		} else {
			log.debug("updateInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("updateInMongo method: END");
	}
	
	//by MJ
	@Override
	public void updateSubmitStatusInMongo(Map<String, Object> object, String collection,
			Map<String, String> allRequestParams) {
		log.debug("updateInMongo method: START");
		Update update = null;
		if (object != null) {
			update = new Update();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				update.set(key, object.get(key));
			}
		}

		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);

		if (update != null) {
			mongoTemplate.updateMulti(new Query(Criteria.where(Gstr1ConstantsV31.HEADER_GSTIN_DB).is(gstin)
					.and(Gstr1ConstantsV31.HEADER_FP_DB).is(fp).and(Gstr1ConstantsV31.CONTROL_GSTN_STATUS).is(Gstr1ConstantsV31.GSTN_SAVED_TOKEN)),
					update, collection);
			log.debug("updateInMongo method: After updating in mongo database");
		} else {
			log.debug("updateInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("updateInMongo method: END");
	}
	@Override
	public void updateFileStatusInMongo(Map<String, Object> object, String collection,
			Map<String, String> allRequestParams) {
		log.debug("updateInMongo method: START");
		Update update = null;
		if (object != null) {
			update = new Update();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				update.set(key, object.get(key));
			}
		}

		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);

		if (update != null) {
			mongoTemplate.updateMulti(new Query(Criteria.where(Gstr1ConstantsV31.HEADER_GSTIN_DB).is(gstin)
					.and(Gstr1ConstantsV31.HEADER_FP_DB).is(fp).and(Gstr1ConstantsV31.CONTROL_GSTN_STATUS).is(Gstr1ConstantsV31.GSTN_SUBMIT_TOKEN)),
					update, collection);
			log.debug("updateInMongo method: After updating in mongo database");
		} else {
			log.debug("updateInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("updateInMongo method: END");
	}
	
	

	@Override
	public void updateStatusInMongoInBatches(Map<String, Object> object, String collection, String inputKey,
			List<String> idList, String status) {
		log.debug("updateInMongo method: START");
		Update update = null;
		if (object != null) {
			update = new Update();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				update.set(key, object.get(key));
			}
		}

		if (update != null) {
			Query q=new Query(Criteria.where(inputKey).in(idList).and(Gstr1ConstantsV31.CONTROL_STATUS).is(status));
			mongoTemplate.updateMulti(
					q,
					update, collection);
			log.debug("updateInMongo method: After updating in mongo database");
		} else {
			log.debug("updateInMongo method: null Object was passed so no save was	 performed!!!!");
		}
		log.debug("updateInMongo method: END");
	}

	// new Code/ROhit
	@Override
	public void updateStatusInMongoInBatches(Map<String, Object> object, String collection,
			Map<String, Object> whereMap) {
		log.debug("updateInMongo method: START");
		Update update = null;
		if (object != null) {
			update = new Update();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				update.set(key, object.get(key));
			}
		}

		Iterator<String> itr = whereMap.keySet().iterator();
		Query query = new Query();
		while (itr.hasNext()) {
			String key = itr.next();
			Object value = whereMap.get(key);
			if (value instanceof List<?>) {
				List<String> l=((List)value);
				query.addCriteria(Criteria.where(key).in(l));
			} else {
				query.addCriteria(Criteria.where(key).is(value));
			}
		}
		if (update != null) {
			mongoTemplate.updateMulti(query, update, collection);
			log.debug("updateInMongo method: After updating in mongo database");
		} else {
			log.debug("updateInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("updateInMongo method: END");
	}

	// new Code/ROhit
	@Override
	public void updateMongoWithPush(Map<String, Object> object, String collection,
			Map<String, Object> whereMap) {
		log.debug("updateInMongo method: START");
		Update update = null;
		if (object != null) {
			update = new Update();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				if(object.get(key) instanceof BasicDBObject){
					update.set(key,object.get(key));
				}else if(object.get(key) instanceof BasicDBList){
					BasicDBList list=(BasicDBList)object.get(key);
					update.pushAll(key,list.toArray());
				}else{
					update.set(key, object.get(key));
				}
			}
		}

		Iterator<String> itr = whereMap.keySet().iterator();
		Query query = new Query();
		while (itr.hasNext()) {
			String key = itr.next();
			Object value = whereMap.get(key);
			if (value instanceof List<?>) {
				List<String> l=((List)value);
				query.addCriteria(Criteria.where(key).in(l));
			} else {
				query.addCriteria(Criteria.where(key).is(value));
			}
		}
		if (update != null) {
			mongoTemplate.updateMulti(query, update, collection);
			log.debug("updateInMongo method: After updating in mongo database");
		} else {
			log.debug("updateInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("updateInMongo method: END");
	}
	
	
	@Override
	public void deleteMongoWithPush(List<String> object, String collection,
			Map<String, Object> whereMap) {
		log.debug("updateInMongo method: START");
		Update update = null;
		if (object != null) {
			update = new Update();
			for (String key : object) {
				update.unset(key);
			}
		}
		Iterator<String> itr = whereMap.keySet().iterator();
		Query query = new Query();
		while (itr.hasNext()) {
			String key = itr.next();
			Object value = whereMap.get(key);
			if (value instanceof List<?>) {
				List<String> l=((List)value);
				query.addCriteria(Criteria.where(key).in(l));
			} else {
				query.addCriteria(Criteria.where(key).is(value));
			}
		}
		if (update != null) {
			mongoTemplate.updateMulti(query, update, collection);
			log.debug("updateInMongo method: After updating in mongo database");
		} else {
			log.debug("updateInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("updateInMongo method: END");
	}
	@Override
	public List<Map<String, Object>> getMongoData(Map<String, Object> object, String collection) {
		log.debug("getMongoData method: START");
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
			log.debug("getMongoData method: After inserting in mongo database");
		} else {
			log.debug("getMongoData method: null Object was passed so no save was performed!!!!");
		}
		log.debug("getMongoData method: END");
		return result;
	}
	
	//new code - Prachi	
	@Override
	public void deleteRecords(Map<String, Object> object, String collection){
		log.info("Start deleteRecords ");
		if (object != null) {
			Query query = new Query();
			Iterator<String> itr = object.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				Object obj = MapUtils.getObject(object, key, "");
				query.addCriteria(Criteria.where(key).is(obj));
			}
			//Map<String, Object> result = new HashMap<>();
			mongoTemplate.remove(query,collection);
			log.info("End deleteRecords");
		}
		
	}

	@Override
	public List<Map> getSuppliesDataL0V1(Map<String, String> allRequestParams, String collection) {

		log.debug("getSuppliesDataL0 method: START");

		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);

		Jongo jongo = new Jongo((DB) mongoGstnTemplate.getDb());
		MongoCollection supplies = jongo.getCollection(collection);
		
		String match1 = "{\"$match\": { \"$or\": [{\"header.gstin\": #} ] } }";
		String match2 = "{\"$match\": { \"header.fp\": # } }";
		String match3 = "{\"$match\": { \"control.status\": # } }";
		String match4 = "{\"$match\": {\"control.ver\": {$in: [\"v3.1\"] } } }";
		String group1="{ \"$group\": {\"_id\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"action\": \"L0\", \"sec\": \"$control.type\"},\"GstnId\": {\"$first\": { \"$concat\": [\"$header.gstin\", \":\", \"L0\", \":\", \"$control.type\", \":\", \"$header.fp\"] } },\"sec\": {\"$first\": \"$control.type\"},\"ntty\": {\"$first\" : {\"$cond\": [{ \"$eq\": [ \"$gstn.ntty\", \"C\" ]},\"C\", \"D\"	]}},\"ivaltot\":   {\"$sum\": \"$gstn.val\" } , \"count\": {\"$sum\": 1},\"section\": {\"$push\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"hsn\"] },{ \"$eq\": [ \"$control.type\",\"nil\"] },{ \"$eq\": [ \"$control.type\",\"b2cs\"] }] },\"then\": \"$gstn\",\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$control.type\",\"doc_issue\"] },\"then\": \"$gstn.docs\",\"else\": \"$gstn.itms\"}}}} }}}";
		String unwind1 = "{ \"$unwind\": \"$section\"}";
		String unwind2= "{ \"$unwind\": \"$section\"}";
		String unwind3 = "{ \"$unwind\": \"$section\"}";
		String group2 = "{ \"$group\": {\"_id\": \"$_id\",\"gstIn\": {\"$first\": \"$GstnId\"},\"gstIn1\": {\"$first\": { \"$concat\": [\"$_id.gstn\", \":\", \"L0\", \":\", \"$_id.sec\", \":\", \"$_id.fp\", \":\", \"$ntty\"] } },\"ivaltot\": {\"$first\": \"$ivaltot\" },\"sec\": {\"$first\": \"$sec\"},\"ntty\": {\"$first\": \"$ntty\"},\"txtot\": { \"$sum\": {\"$cond\": {\"if\": {\"$or\": [{\"$eq\": [\"$_id.sec\", \"hsn\"]}, {\"$eq\": [\"$_id.sec\",\"nil\"]},{\"$eq\": [\"$_id.sec\",\"b2cs\"]}, {\"$eq\": [ \"$_id.sec\",\"exp\"]}] },   \"then\": \"$section.txval\",\"else\": {\"$cond\": {   \"if\": {\"$or\": [{ \"$eq\": [\"$_id.sec\", \"at\"]},{ \"$eq\": [\"$_id.sec\", \"txpd\"]} ] }, \"then\": \"$section.ad_amt\",   \"else\": \"$section.itm_det.txval\" } } }}},   \"itot\": { \"$sum\": {\"$cond\": [ {\"$or\" : [ 	{ \"$eq\": [ \"$_id.sec\", \"hsn\"] }, { \"$eq\": [ \"$_id.sec\",\"nil\"] },{ \"$eq\": [ \"$_id.sec\",\"at\"] },{ \"$eq\": [ \"$_id.sec\",\"b2cs\"] },  { \"$eq\": [ \"$_id.sec\",\"txpd\"] }, { \"$eq\": [ \"$_id.sec\",\"exp\"] } ] }, 		\"$section.iamt\", \"$section.itm_det.iamt\"]}},\"ctot\": { \"$sum\": {\"$cond\": [ {\"$or\" : [ 	{ \"$eq\": [ \"$_id.sec\", \"hsn\"] }, { \"$eq\": [ \"$_id.sec\",\"nil\"] },		{ \"$eq\": [ \"$_id.sec\",\"at\"] },{ \"$eq\": [ \"$_id.sec\",\"b2cs\"] },  { \"$eq\": [ \"$_id.sec\",\"txpd\"] }, { \"$eq\": [ \"$_id.sec\",\"exp\"] } ] }, 		\"$section.camt\", \"$section.itm_det.camt\"]}},   \"stot\": { \"$sum\": {\"$cond\": [ {\"$or\" : [ 	{ \"$eq\": [ \"$_id.sec\", \"hsn\"] }, { \"$eq\": [ \"$_id.sec\",\"nil\"] },		{ \"$eq\": [ \"$_id.sec\",\"at\"] },{ \"$eq\": [ \"$_id.sec\",\"b2cs\"] },  { \"$eq\": [ \"$_id.sec\",\"txpd\"] }, { \"$eq\": [ \"$_id.sec\",\"exp\"] } ] }, 		\"$section.samt\", \"$section.itm_det.samt\"]}},   \"cstot\": { \"$sum\": {\"$cond\": [ {\"$or\" : [ 	{ \"$eq\": [ \"$_id.sec\", \"hsn\"] }, { \"$eq\": [ \"$_id.sec\",\"nil\"] },{ \"$eq\": [ \"$_id.sec\",\"at\"] },{ \"$eq\": [ \"$_id.sec\",\"b2cs\"] },  { \"$eq\": [ \"$_id.sec\",\"txpd\"] }, { \"$eq\": [ \"$_id.sec\",\"exp\"] } ] }, 		\"$section.csamt\", \"$section.itm_det.csamt\"]}},   \"expt_amt\":   {\"$sum\": \"$section.expt_amt\" } ,\"nil_amt\":   {\"$sum\": \"$section.nil_amt\" } ,\"ngsup_amt\":   {\"$sum\": \"$section.ngsup_amt\" } ,   \"sply_ty\":   {\"$first\": \"$section.sply_ty\" } ,   \"cancel\": { \"$sum\": \"$section.cancel\" } ,   \"num\": { \"$sum\": \"$section.num\" } ,   \"totnum\": { \"$sum\": \"$section.totnum\" } ,   \"net_issue\": { \"$sum\": \"$section.net_issue\" } ,\"cnt\": { \"$first\": \"$count\" }  } }";
		String group3 = "{ \"$group\": {\"_id\": \"$gstIn\",\"_id1\": {\"$first\": { \"$cond\": [{\"$or\" : [ 	{ \"$eq\": [ \"$_id.sec\", \"cdnr\"] },{ \"$eq\": [ \"$_id.sec\",\"cdnur\"] }	] },\"$gstIn1\",   \"$gstIn\"] } },\"gstin\":{ \"$first\": \"$_id.gstn\" },\"sec\":{ \"$first\": \"$sec\" },\"action\": {\"$first\": \"L0\"},\"fp\": { \"$first\" : \"$_id.fp\" },\"sections\": { \"$push\": {  \"$cond\": [  {\"$or\" : [ { \"$eq\": [ \"$_id.sec\", \"cdnr\"] },		{ \"$eq\": [ \"$_id.sec\",\"cdnur\"] }] },{\"sec_nm\": \"$_id.sec\",\"ntty\": \"$ntty\",\"ttl_igst\": \"$itot\",\"ttl_cgst\": \"$ctot\",\"ttl_sgst\": \"$stot\",\"ttl_cess\": \"$cstot\",\"ttl_val\": \"$ivaltot\",\"ttl_count\": \"$cnt\",\"ttl_txval\": \"$txtot\" } ,{\"sec_nm\": \"$_id.sec\",\"ttl_igst\": \"$itot\",\"ttl_cgst\": \"$ctot\",\"ttl_sgst\": \"$stot\",\"ttl_cess\": \"$cstot\",\"ttl_val\": \"$ivaltot\",\"ttl_count\": \"$cnt\",\"ttl_txval\": \"$txtot\" }] } },\"nil_sections\": { \"$push\": { \"sply_ty\": \"$sply_ty\",\"sec_nm\": \"$_id.sec\",\"expt_amt\": \"$expt_amt\",\"nil_amt\": \"$nil_amt\",\"ngsup_amt\": \"$ngsup_amt\"}},   \"doc_sections\": { \"$push\": { \"sec_nm\": \"$_id.sec\",\"ttl_count\": \"$count_all\",\"cancel\": \"$cancel\",\"num\": \"$num\",\"totnum\": \"$totnum\",\"net_issue\": \"$net_issue\"}}}}";
		String group4 = "{ \"$group\": {\"_id\": \"$_id1\", \"gstin\":{ \"$first\": \"$gstin\" },\"action\": {\"$first\": \"L0\"},\"fp\": { \"$first\" : \"$fp\" },\"sections\": {\"$first\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$sec\", \"nil\"] }] },\"then\": \"$nil_sections\",\"else\": {  \"$cond\": {  \"if\": { \"$eq\": [ \"$sec\", \"doc_issue\"] },   \"then\":\"$doc_sections\",\"else\": \"$sections\"  }}}}}}}";
		
		List<Map> mapList = new ArrayList<>();
		
		long t1 = System.currentTimeMillis();
		Aggregate.ResultsIterator<Map> resultAgg = supplies.aggregate(match1, gstin).and(match2, fp).and(match3,Gstr1ConstantsV31.INVOICE_STATE_NEW).and(match4)
				.and(group1).and(unwind1).and(unwind2).and(unwind3).and(group2).and(group3).and(group4).as(Map.class);
		long t2 = System.currentTimeMillis();

		log.info("getSuppliesDataL0 TAT {}", (t2 - t1));

		if (resultAgg != null) {
			Iterator<Map> it = resultAgg.iterator();
			while (it.hasNext()) {
				mapList.add(it.next());
			}
		}

		log.debug("getSuppliesDataL0 method: After inserting in mongo database");
		log.debug("getSuppliesDataL0 method: END");
		return mapList;
	}	
	
	@Override
	public List<Map> getSuppliesDataL0(Map<String, String> allRequestParams, String collection) {

		log.debug("getSuppliesDataL0 method: START");

		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);

		Jongo jongo = new Jongo((DB) mongoGstnTemplate.getDb());
		MongoCollection supplies = jongo.getCollection(collection);
		
		AggregationOptions options = AggregationOptions.builder().allowDiskUse(true).build();
		
		String match1 = "{\"$match\": { \"$or\": [{\"header.gstin\": #} ] } }";
		String match2 = "{\"$match\": { \"header.fp\": # } }";
		String match3 = "{\"$match\": { \"control.status\": {$ne:  #  } } }";
		String match4 = "{\"$match\": {\"control.ver\": {$in: [\"v3.1\"] } } }";
		String group1=  "{ \"$group\": {\"_id\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"action\": \"L0\", \"sec\": \"$control.type\",\"gst_status\": \"$control.gst_status\"},\"GstnId\": {\"$first\": { \"$concat\": [\"$header.gstin\", \":\", \"L0\", \":\", \"$control.type\", \":\", \"$header.fp\"] } }, \"sec\": {\"$first\": \"$control.type\"},\"ntty\": {\"$first\" : {\"$cond\": [{ \"$eq\": [ \"$gstn.ntty\", \"C\"]},\"C\",\"D\"]}}, \"ivaltot\":   {\"$sum\": \"$gstn.val\" } , \"count\": {\"$sum\": 1}, \"gst_status1\": {\"$first\": \"$control.gst_status\"}, \"section\": {\"$push\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"hsn\"] },{ \"$eq\": [ \"$control.type\",\"nil\"] },{ \"$eq\": [ \"$control.type\",\"b2cs\"] },{ \"$eq\": [ \"$control.type\",\"b2csa\"] }] }, \"then\": \"$gstn\",\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$control.type\",\"doc_issue\"] },\"then\": \"$gstn.docs\",\"else\": \"$gstn.itms\"}}}}}}}";
		String unwind1 = "{ \"$unwind\": \"$section\"}";
		String unwind2= "{ \"$unwind\": \"$section\"}";
		String unwind3 = "{ \"$unwind\": \"$section\"}";
		String group2 = "{ \"$group\": {\"_id\": \"$_id\",\"gstIn\": {\"$first\": \"$GstnId\"}, \"gstIn1\": {\"$first\": { \"$concat\": [\"$_id.gstn\", \":\", \"L0\", \":\", \"$_id.sec\", \":\", \"$_id.fp\", \":\", \"$ntty\"] } },\"ival_hsn\": {\"$sum\": \"$section.val\"}, \"ivaltot\": {\"$first\": \"$ivaltot\" },\"sec\": {\"$first\": \"$sec\"},\"ntty\": {\"$first\": \"$ntty\"},\"count_all\": {\"$sum\": \"$count\"},\"cnt\": { \"$first\": \"$count\" }, \"new_cnt\": { \"$first\": {\"$cond\": { \"if\": { \"$eq\": [ \"$gst_status1\", \"New\"] } ,  \"then\": \"$count\",\"else\": 0 }}},\"GstnFailed_cnt\": { \"$first\": {\"$cond\": { \"if\": { \"$eq\": [ \"$gst_status1\", \"GstnFailed\"] } ,\"then\": \"$count\",\"else\":  0 }   }},\"GstnSaved_cnt\": { \"$first\": {\"$cond\": { \"if\": { \"$eq\": [ \"$gst_status1\", \"GstnSaved\"] } , \"then\": \"$count\",\"else\":  0 }   }},\"GstnSubmitted_cnt\": { \"$first\": {\"$cond\": { \"if\": { \"$eq\": [ \"$gst_status1\", \"GstnSubmitted\"] } ,\"then\": \"$count\",\"else\":  0 }   }}, \"GstnUpload_cnt\": { \"$first\": {\"$cond\": { \"if\": { \"$eq\": [ \"$gst_status1\", \"GstnUpload\"] } , \"then\": \"$count\",\"else\":  0}}}, \"GstnFiled_cnt\": { \"$first\": {\"$cond\": { \"if\": { \"$eq\": [ \"$gst_status1\", \"GstnFiled\"] } , \"then\": \"$count\",\"else\":  0}}},  \"txtot\": { \"$sum\": {\"$cond\": { \"if\": {  \"$or\": [ {\"$eq\": [\"$_id.sec\", \"hsn\"]}, {\"$eq\": [\"$_id.sec\",\"nil\"]},  {\"$eq\": [\"$_id.sec\",\"b2cs\"]},  {\"$eq\": [\"$_id.sec\",\"b2csa\"]},  {\"$eq\": [ \"$_id.sec\",\"exp\"]}, {\"$eq\": [ \"$_id.sec\",\"expa\"]}] }, \"then\": \"$section.txval\", \"else\": {\"$cond\": {   \"if\": {  \"$or\": [{ \"$eq\": [\"$_id.sec\", \"at\"]},  { \"$eq\": [\"$_id.sec\", \"ata\"]},   { \"$eq\": [\"$_id.sec\", \"txpd\"]}, { \"$eq\": [\"$_id.sec\", \"txpda\"]} ] }, \"then\": \"$section.ad_amt\", \"else\": \"$section.itm_det.txval\" } } }}},  \"itot\": { \"$sum\": {\"$cond\": [ { \"$or\" : [   { \"$eq\": [ \"$_id.sec\", \"hsn\"] },  { \"$eq\": [ \"$_id.sec\",\"nil\"] },   { \"$eq\": [ \"$_id.sec\",\"at\"] }, { \"$eq\": [ \"$_id.sec\",\"ata\"] },  { \"$eq\": [ \"$_id.sec\",\"b2cs\"] },  { \"$eq\": [ \"$_id.sec\",\"b2csa\"] },  { \"$eq\": [ \"$_id.sec\",\"txpd\"] },  { \"$eq\": [ \"$_id.sec\",\"txpda\"] }, { \"$eq\": [ \"$_id.sec\",\"exp\"] }, { \"$eq\": [ \"$_id.sec\",\"expa\"] } ] }, \"$section.iamt\",  \"$section.itm_det.iamt\"]}}, \"ctot\": { \"$sum\": {\"$cond\": [ {  \"$or\" : [ { \"$eq\": [ \"$_id.sec\", \"hsn\"] }, { \"$eq\": [ \"$_id.sec\",\"nil\"] },  { \"$eq\": [ \"$_id.sec\",\"at\"] },{ \"$eq\": [ \"$_id.sec\",\"ata\"] }, { \"$eq\": [ \"$_id.sec\",\"b2cs\"] }, { \"$eq\": [ \"$_id.sec\",\"b2csa\"] }, { \"$eq\": [ \"$_id.sec\",\"txpd\"] }, { \"$eq\": [ \"$_id.sec\",\"txpda\"] },{ \"$eq\": [ \"$_id.sec\",\"exp\"] }, { \"$eq\": [ \"$_id.sec\",\"expa\"] } ] }, \"$section.camt\", \"$section.itm_det.camt\"]}}, \"stot\": { \"$sum\": {\"$cond\": [ { \"$or\" : [   { \"$eq\": [ \"$_id.sec\", \"hsn\"] }, { \"$eq\": [ \"$_id.sec\",\"nil\"] },                                             { \"$eq\": [ \"$_id.sec\",\"at\"] }, { \"$eq\": [ \"$_id.sec\",\"ata\"] }, { \"$eq\": [ \"$_id.sec\",\"b2cs\"] }, { \"$eq\": [ \"$_id.sec\",\"b2csa\"] }, { \"$eq\": [ \"$_id.sec\",\"txpd\"] }, { \"$eq\": [ \"$_id.sec\",\"txpda\"] }, { \"$eq\": [ \"$_id.sec\",\"exp\"] }, { \"$eq\": [ \"$_id.sec\",\"exp\"] } ] }, \"$section.samt\", \"$section.itm_det.samt\"]}}, \"cstot\": { \"$sum\": {\"$cond\": [ { \"$or\" : [{ \"$eq\": [ \"$_id.sec\", \"hsn\"] }, { \"$eq\": [ \"$_id.sec\",\"nil\"] }, { \"$eq\": [ \"$_id.sec\",\"at\"] }, { \"$eq\": [ \"$_id.sec\",\"ata\"] }, { \"$eq\": [ \"$_id.sec\",\"b2cs\"] }, { \"$eq\": [ \"$_id.sec\",\"b2csa\"] }, { \"$eq\": [ \"$_id.sec\",\"txpd\"] },  { \"$eq\": [ \"$_id.sec\",\"txpda\"] }, { \"$eq\": [ \"$_id.sec\",\"exp\"] },  { \"$eq\": [ \"$_id.sec\",\"expa\"] } ] }, \"$section.csamt\", \"$section.itm_det.csamt\"]}}, \"expt_amt\":   {\"$sum\": \"$section.expt_amt\" } ,\"nil_amt\":   {\"$sum\": \"$section.nil_amt\" } ,\"ngsup_amt\":   {\"$sum\": \"$section.ngsup_amt\" } , \"sply_ty\": {\"$first\": \"$section.sply_ty\" } ,  \"cancel\": { \"$sum\": \"$section.cancel\" } ,  \"num\": { \"$sum\": \"$section.num\" } , \"totnum\": { \"$sum\": \"$section.totnum\" } , \"net_issue\": { \"$sum\": \"$section.net_issue\" } } }";
		String group3 = "{ \"$group\": {\"_id\": {\"gstn\": \"$_id.gstn\", \"fp\": \"$_id.fp\" , \"sec\": \"$_id.sec\"} , \"_id1\": {\"$first\": { \"$cond\": [ { \"$or\" : [   { \"$eq\": [ \"$_id.sec\", \"cdnr\"] }, { \"$eq\": [ \"$_id.sec\", \"cdnra\"] }, { \"$eq\": [ \"$_id.sec\",\"cdnur\"] }, { \"$eq\": [ \"$_id.sec\",\"cdnura\"] }] },\"$gstIn1\",\"$gstIn\"] } },  \"gstIn\": {\"$first\": \"$gstIn\"}, \"gstIn1\": {\"$first\": \"$gstIn1\" },\"ival_hsn\": {\"$sum\":\"$ival_hsn\"}, \"ivaltot\": {\"$first\": \"$ivaltot\"}, \"gstin\":{ \"$first\": \"$_id.gstn\" },\"action\": {\"$first\": \"L0\"},\"sec\":{ \"$first\": \"$sec\" },\"fp\": { \"$first\" : \"$_id.fp\" },\"ntty\": {\"$first\": \"$ntty\"},\"count_all\": {\"$sum\": \"$cnt\"},\"txtot\": {\"$sum\": \"$txtot\"}, \"itot\" : {\"$sum\": \"$itot\"},\"ctot\" : {\"$sum\": \"$ctot\"},\"stot\" : {\"$sum\": \"$stot\"},\"cstot\" : {\"$sum\": \"$cstot\"},\"expt_amt\" : {\"$sum\": \"$expt_amt\"},\"nil_amt\" : {\"$sum\": \"$nil_amt\"},\"ngsup_amt\" : {\"$sum\": \"$ngsup_amt\"}, \"sply_ty\" : {\"$first\": \"$sply_ty\"},\"cancel\" : {\"$sum\": \"$cancel\"}, \"num\" : {\"$sum\": \"$num\"}, \"totnum\" : {\"$sum\": \"$totnum\"},  \"net_issue\" : {\"$sum\": \"$net_issue\"}, \"new_cnt\": { \"$sum\": {\"$cond\": [ {\"$or\" : [   { \"$eq\": [ \"$_id.gst_status\", \"New\"] } ] }, \"$new_cnt\", 0 ]}},  \"GstnFailed_cnt\": { \"$sum\": {\"$cond\": [ {\"$or\" : [   { \"$eq\": [ \"$_id.gst_status\", \"GstnFailed\"] } ] }, \"$GstnFailed_cnt\", 0 ]}}, \"GstnSaved_cnt\": { \"$sum\": {\"$cond\": [ {\"$or\" : [   { \"$eq\": [ \"$_id.gst_status\", \"GstnSaved\"] } ] }, \"$GstnSaved_cnt\", 0 ]}}, \"GstnSubmitted_cnt\": { \"$sum\": {\"$cond\": [ {\"$or\" : [   { \"$eq\": [ \"$_id.gst_status\", \"GstnSubmitted\"] } ] },   \"$GstnSubmitted_cnt\", 0 ]}}, \"GstnUpload_cnt\": { \"$sum\": {\"$cond\": [ {\"$or\" : [   { \"$eq\": [ \"$_id.gst_status\", \"GstnUpload\"] } ] }, \"$GstnUpload_cnt\", 0 ]}}, \"GstnFiled_cnt\": { \"$sum\": {\"$cond\": [ {\"$or\" : [   { \"$eq\": [ \"$_id.gst_status\", \"GstnFiled\"] } ] }, \"$GstnFiled_cnt\", 0 ]}}   }}";
		String group4 = "{ \"$group\": {\"_id\": \"$gstIn\",\"_id1\": {\"$first\":  \"$_id1\" },   \"gstin\":{ \"$first\": \"$gstin\" },   \"sec\":{ \"$first\": \"$sec\" },  \"action\": {\"$first\": \"L0\"},   \"fp\": { \"$first\" : \"$_id.fp\" },   \"sections\": { \"$push\": {   \"$cond\": [   {\"$or\" : [  { \"$eq\": [ \"$_id.sec\", \"cdnr\"] },{ \"$eq\": [ \"$_id.sec\", \"cdnra\"] },{ \"$eq\": [ \"$_id.sec\",\"cdnur\"] },{ \"$eq\": [ \"$_id.sec\",\"cdnura\"] }] },{ \"sec_nm\": \"$_id.sec\", \"ntty\": \"$ntty\",\"ttl_igst\": \"$itot\", \"ttl_cgst\": \"$ctot\",\"ttl_sgst\": \"$stot\",\"ttl_cess\": \"$cstot\",\"ttl_val\": \"$ivaltot\",\"ttl_count\": \"$count_all\",\"ttl_txval\": \"$txtot\",\"New_cnt\" :  \"$new_cnt\" ,\"GstnFailed_cnt\" : \"$GstnFailed_cnt\", \"GstnSaved_cnt\" : \"$GstnSaved_cnt\",\"GstnSubmitted_cnt\" : \"$GstnSubmitted_cnt\",\"GstnUpload_cnt\" : \"$GstnUpload_cnt\", \"GstnFiled_cnt\" : \"$GstnFiled_cnt\"} ,{   \"sec_nm\": \"$_id.sec\",\"ttl_igst\": \"$itot\",\"ttl_cgst\": \"$ctot\",\"ttl_sgst\": \"$stot\",  \"ttl_cess\": \"$cstot\", \"ttl_val\": {\"$cond\": [ {\"$or\" : [{ \"$eq\": [ \"$_id.sec\", \"hsn\"] } ] },\"$ival_hsn\", \"$ivaltot\"]}, \"ttl_count\": \"$count_all\", \"ttl_txval\": \"$txtot\", \"New_cnt\" :  \"$new_cnt\", \"GstnFailed_cnt\" : \"$GstnFailed_cnt\", \"GstnSaved_cnt\" : \"$GstnSaved_cnt\", \"GstnSubmitted_cnt\" : \"$GstnSubmitted_cnt\", \"GstnUpload_cnt\" : \"$GstnUpload_cnt\",  \"GstnFiled_cnt\" : \"$GstnFiled_cnt\"}   ]} }, \"nil_sections\": { \"$push\": {\"sply_ty\": \"$sply_ty\", \"sec_nm\": \"$_id.sec\", \"expt_amt\": \"$expt_amt\", \"nil_amt\": \"$nil_amt\",  \"ngsup_amt\": \"$ngsup_amt\",  \"New_cnt\" :  \"$new_cnt\" , \"GstnFailed_cnt\" : \"$GstnFailed_cnt\",   \"GstnSaved_cnt\" : \"$GstnSaved_cnt\", \"GstnSubmitted_cnt\" : \"$GstnSubmitted_cnt\", \"GstnUpload_cnt\" : \"$GstnUpload_cnt\", \"GstnFiled_cnt\" : \"$GstnFiled_cnt\"   }}, \"doc_sections\": { \"$push\": {  \"sec_nm\": \"$_id.sec\", \"ttl_rec\": \"$count_all\", \"cancel\": \"$cancel\", \"num\": \"$num\",  \"totnum\": \"$totnum\", \"net_issue\": \"$net_issue\", \"New_cnt\" :  \"$new_cnt\" ,  \"GstnFailed_cnt\" : \"$GstnFailed_cnt\",  \"GstnSaved_cnt\" : \"$GstnSaved_cnt\", \"GstnSubmitted_cnt\" : \"$GstnSubmitted_cnt\",  \"GstnUpload_cnt\" : \"$GstnUpload_cnt\", \"GstnFiled_cnt\" : \"$GstnFiled_cnt\"   }},}}";
		String group5 = "{ \"$group\": {\"_id\": \"$_id1\",\"gstin\":{ \"$first\": \"$gstin\" }, \"action\": {\"$first\": \"L0\"},   \"fp\": { \"$first\" : \"$fp\" },\"sections\": { \"$first\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$sec\", \"nil\"] }] }, \"then\": \"$nil_sections\", \"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"doc_issue\"] },  \"then\": \"$doc_sections\", \"else\": \"$sections\"}}}}}}}";
		
		List<Map> mapList = new ArrayList<>();
		
		long t1 = System.currentTimeMillis();
		Aggregate.ResultsIterator<Map> resultAgg = supplies.aggregate(match1, gstin).and(match2, fp).and(match3,Gstr1ConstantsV31.INVOICE_STATE_DEL)
				.and(match4).and(group1).and(unwind1).and(unwind2).and(unwind3).and(group2).and(group3).and(group4).and(group5)
				.options(options).as(Map.class);
		long t2 = System.currentTimeMillis();

		log.info("getSuppliesDataL0 TAT {}", (t2 - t1));

		if (resultAgg != null) {
			Iterator<Map> it = resultAgg.iterator();
			while (it.hasNext()) {
				mapList.add(it.next());
			}
		}

		log.debug("getSuppliesDataL0 method: After inserting in mongo database");
		log.debug("getSuppliesDataL0 method: END");
		return mapList;
	}

	@Override
	public List<Map<String, Object>> getDataForGstn(Map<String, String> allRequestParams, String collection) {

		log.debug("getDataForGstn method: START");
		log.info("getDataForGstn method: START");
		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);
		List<Map<String, Object>> mapList = new ArrayList<>();

		Jongo jongo = new Jongo((DB) mongoGstnTemplate.getDb());
		MongoCollection supplies = jongo.getCollection(collection);

		// Queries wrt new structural changes

		String project1 = "{\"$project\": { \"result\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$sec\", \"b2b\"] }] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2b\": \"$b2b\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cl\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cl\": \"$b2cl\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cs\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cs\": \"$b2cs\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"exp\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"exp\": \"$exp\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"nil\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"nil\": \"$nil\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"cdnr\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnr\": \"$cdnr\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"cdnur\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnur\": \"$cdnur\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"doc_issue\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"doc_issue\": \"$doc_issue\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"txpd\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"txpd\": \"$txpd\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"at\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"at\": \"$at\"},\"else\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"hsn\": \"$hsn\"}}}}}}}}}}}}}}}}}}}}}}}";
		String project2 = "{\"$project\": {\"gstn.itms.custom\":0, \"gstn.custom\":0, \"gstn.docs.custom\":0}}";
		String project3 = "{\"$project\": {\"b2cs.date\":0, \"b2cs.stid\":0, \"b2cs.action\":0, \"nil_inv.action\": 0, \"nil_inv.stid\": 0}}";
		String match0 = "{\"$match\": { $or: [{ \"header.gstin\":# }] } }";
		String match1 = "{\"$match\": { $or: [{ \"header.fp\":# }] } }";
		String match2 = "{\"$match\":{\"$or\":[{\"control.status\":\"New\"}]}}";
		String match3 = "{\"$match\": { \"$or\": [{ \"control.type\": \"b2b\" }, { \"control.type\": \"b2cl\" }, { \"control.type\": \"b2cs\" } , { \"control.type\": \"exp\"},{ \"control.type\": \"cdnr\" }, { \"control.type\": \"cdnur\" }, { \"control.type\": \"doc_issue\" } , { \"control.type\": \"nil\" },{ \"control.type\": \"hsn\" }, { \"control.type\": \"at\" }, { \"control.type\": \"txpd\" } ] } }";
		String unwind = "{\"$unwind\": \"$gstn\"}";
		String group1 = "{ \"$group\": {\"_id\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2b\"] },{ \"$eq\": [ \"$control.type\", \"cdnr\"] }] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"ctin\": \"$gstn.ctin\"},\"else\": {\"$cond\": {    \"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2cl\"] }] },    \"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"pos\": \"$gstn.pos\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$control.type\", \"exp\"] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"exp_typ\": \"$gstn.exp_typ\"},\"else\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\"}   }}}}}},\"gstin_con\": {\"$first\": { \"$concat\": [\"$header.gstin\", \":\", \"$control.type\", \":\", \"$header.fp\"] } },    \"gstin\": {\"$first\": \"$header.gstin\" },    \"fp\": {\"$first\": \"$header.fp\"},\"sec\": {\"$first\": \"$control.type\"},\"cur_gt\": {\"$first\": \"$header.curr_gt\"},\"gt\": {\"$first\": \"$header.gt\"}, \"cdnur\": { \"$push\": {\"typ\": \"$gstn.typ\",    \"ntty\": \"$gstn.ntty\",    \"nt_num\":  \"$gstn.nt_num\",    \"nt_dt\":  \"$gstn.nt_dt\",\"p_gst\": \"$gstn.p_gst\",\"rsn\": \"$gstn.rsn\",    \"inum\":  \"$gstn.inum\",    \"idt\": \"$gstn.idt\" ,    \"val\":  \"$gstn.val\" ,\"itms\": \"$gstn.itms\"} }, \"doc_det\": {\"$push\": {\"doc_num\" : \"$gstn.doc_num\",\"docs\" : \"$gstn.docs\"} } ,\"nil_inv\": {\"$push\": \"$gstn\"} ,\"exp_typ\": {\"$first\": \"$gstn.exp_typ\"}, \"ctin\": {\"$first\": \"$gstn.ctin\"}, \"pos\": {\"$first\": \"$gstn.pos\"}, \"b2cs\": {\"$push\": \"$gstn\"}, \"inv\": { \"$push\": {\"etin\": \"$gstn.etin\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"pos\" : \"$gstn.pos\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",    \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"b2cl_inv\": { \"$push\": {\"etin\": \"$gstn.etin\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",    \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"nt\": { \"$push\": {\"ntty\" : \"$gstn.ntty\",\"nt_num\": \"$gstn.nt_num\",    \"nt_dt\" : \"$gstn.nt_dt\",    \"p_gst\" : \"$gstn.p_gst\",    \"rsn\" : \"$gstn.rsn\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"itms\": \"$gstn.itms\"    } },    \"txpd\": { \"$push\": {\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\",    \"itms\": \"$gstn.itms\"    } },    \"at\": { \"$push\": {\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\",    \"itms\": \"$gstn.itms\"    } }, \"data\": { \"$push\": {\"num\": \"$gstn.num\",    \"hsn_sc\": \"$gstn.hsn_sc\",    \"desc\": \"$gstn.desc\",    \"uqc\": \"$gstn.uqc\",    \"qty\": \"$gstn.qty\",    \"val\": \"$gstn.val\",    \"txval\": \"$gstn.txval\",    \"iamt\": \"$gstn.iamt\",    \"csamt\": \"$gstn.csamt\"    } }    }}";
		String group2 = "{ \"$group\": { \"_id\": \"$gstin_con\",    \"sec\": {\"$first\": \"$_id.sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"},    \"cur_gt\": {\"$first\": \"$cur_gt\"},\"gt\":  {\"$first\": \"$gt\"},    \"b2b\": { \"$push\": {    \"ctin\": \"$ctin\",\"inv\" : \"$inv\"} },\"b2cs\":  {\"$first\": \"$b2cs\"},    \"b2cl\": { \"$push\": {    \"pos\": \"$pos\",\"inv\" : \"$b2cl_inv\"} },    \"exp\": { \"$push\": {    \"exp_typ\": \"$exp_typ\",\"inv\" : \"$inv\"} },\"nil\": {\"$first\": {\"inv\": \"$nil_inv\"} } ,    \"doc_issue\": {\"$first\": {\"doc_det\": \"$doc_det\"} } ,\"cdnr\": { \"$push\": {    \"ctin\": \"$ctin\",\"nt\" : \"$nt\"} },\"cdnur\": {\"$first\": \"$cdnur\"},\"txpd\": { \"$first\": \"$txpd\"},\"at\": { \"$first\": \"$at\"},    \"hsn\": { \"$first\": { \"data\": \"$data\"} }     }    }";
		String group3 = "{ \"$group\": { \"_id\": \"$_id\",    \"sec\": {\"$first\": \"$sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"}, \"cur_gt\": {\"$first\": \"$cur_gt\"},   \"gt\":  {\"$first\": \"$gt\"},    \"b2b\": {\"$first\": \"$b2b\"},    \"b2cs\": {\"$first\": \"$b2cs\"},    \"b2cl\": {\"$first\": \"$b2cl\"},    \"exp\": {\"$first\": \"$exp\"},    \"nil\": {\"$first\": \"$nil\"},    \"doc_issue\": {\"$first\": \"$doc_issue\"},    \"cdnr\": {\"$first\": \"$cdnr\"},    \"cdnur\": {\"$first\": \"$cdnur\"},    \"txpd\": {\"$first\": \"$txpd\"},    \"at\": {\"$first\": \"$at\"},    \"hsn\": { \"$first\": \"$hsn\"}    }    }";

		long t1 = System.currentTimeMillis();

		Aggregate.ResultsIterator<Map> resultAgg = supplies.aggregate(match0, gstin).and(match1, fp).and(match2)
				.and(match3).and(project2).and(unwind).and(group1).and(project3).and(group2).and(group3).and(project1)
				.as(Map.class);
		long t2 = System.currentTimeMillis();

		log.info("SaveToGstnGetData TAT {}", (t2 - t1));

		if (resultAgg != null) {
			Iterator<Map> it = resultAgg.iterator();
			while (it.hasNext()) {
				mapList.add(it.next());
			}
		}

		log.debug("getDataForGstn method: After inserting in mongo database");
		log.info("getDataForGstn method: END");

		return mapList;

	}

	@Override
	public List<Map<String, Object>> getDataForGstnInBatches(Map<String, String> allRequestParams, long offset,
			long pageSize, String collection, boolean queryType,String section) {

		log.debug("getDataForGstn method: START");
		log.info("getDataForGstn method: START");
		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);
		// Map<Integer,Object> batchList = new HashMap<Integer,Object>();
		List<Map<String, Object>> mapList = new ArrayList<>();
		Jongo jongo = new Jongo((DB) mongoGstnTemplate.getDb());
		MongoCollection supplies = jongo.getCollection(collection);
		String project1 = "{\"$project\": { \"result\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$sec\", \"b2b\"] }] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2b\": \"$b2b\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cl\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cl\": \"$b2cl\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cs\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cs\": \"$b2cs\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"exp\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"exp\": \"$exp\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"nil\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"nil\": \"$nil\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"cdnr\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnr\": \"$cdnr\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"cdnur\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnur\": \"$cdnur\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"doc_issue\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"doc_issue\": \"$doc_issue\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"txpd\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"txpd\": \"$txpd\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"at\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"at\": \"$at\"},\"else\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"hsn\": \"$hsn\"}}}}}}}}}}}}}}}}}}}}}}}";
		String project2 = "{\"$project\": {\"gstn.itms.custom\":0, \"gstn.custom\":0, \"gstn.docs.custom\":0}}";
		String project3 = "{\"$project\": {\"b2cs.date\":0, \"b2cs.stid\":0, \"b2cs.action\":0, \"nil_inv.action\": 0, \"nil_inv.stid\": 0}}";
		String match0 = "{\"$match\": { $or: [{ \"header.gstin\":# }] } }";
		String match1 = "{\"$match\": { $or: [{ \"header.fp\":# }] } }";
		String match2 = "{\"$match\":{\"$or\":[{\"control.gst_status\":\"New\"},{\"control.gst_status\":\"GstnUpload\"}]}}";
		String match3 = "{\"$match\": {\"control.ver\": {$in: [\"v3.1\"] } } }";
		String skip = "{\"$skip\": # }";
		String limit = "{\"$limit\": # }";

		String invLvlMatch = "{\"$match\": { \"$or\": [{ \"control.type\": \"b2b\" }, { \"control.type\": \"b2cl\" },  { \"control.type\": \"exp\"},{ \"control.type\": \"cdnr\" }, { \"control.type\": \"cdnur\" } ] } }";
//		String aggSecMatch = "{\"$match\": { \"$or\": [{ \"control.type\": \"b2cs\" } , { \"control.type\": \"doc_issue\" } , { \"control.type\": \"nil\" },{ \"control.type\": \"hsn\" }, { \"control.type\": \"at\" }, { \"control.type\": \"txpd\" } ] } }";

		String aggSecMatch = "{\"$match\": { $or: [{ \"control.type\":# }] } }";
		
		String unwind = "{\"$unwind\": \"$gstn\"}";
		String group1 = "{ \"$group\": {\"_id\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2b\"] },{ \"$eq\": [ \"$control.type\", \"cdnr\"] }] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"ctin\": \"$gstn.ctin\"},\"else\": {\"$cond\": {    \"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2cl\"] }] },    \"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"pos\": \"$gstn.pos\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$control.type\", \"exp\"] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"exp_typ\": \"$gstn.exp_typ\"},\"else\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\"}   }}}}}},\"gstin_con\": {\"$first\": { \"$concat\": [\"$header.gstin\", \":\", \"$control.type\", \":\", \"$header.fp\"] } },    \"gstin\": {\"$first\": \"$header.gstin\" },    \"fp\": {\"$first\": \"$header.fp\"},\"sec\": {\"$first\": \"$control.type\"},\"cur_gt\": {\"$first\": \"$header.curr_gt\"},\"gt\": {\"$first\": \"$header.gt\"}, \"cdnur\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"typ\": \"$gstn.typ\",    \"ntty\": \"$gstn.ntty\",    \"nt_num\":  \"$gstn.nt_num\",    \"nt_dt\":  \"$gstn.nt_dt\",\"p_gst\": \"$gstn.p_gst\",\"rsn\": \"$gstn.rsn\",    \"inum\":  \"$gstn.inum\",    \"idt\": \"$gstn.idt\" ,    \"val\":  \"$gstn.val\" ,\"itms\": \"$gstn.itms\"} }, \"doc_det\": {\"$push\": {\"inv_id\": \"$_id\", \"doc_num\" : \"$gstn.doc_num\",\"docs\" : \"$gstn.docs\"} } ,\"nil_inv\":{\"$push\": {\"inv_id\": \"$_id\",\"expt_amt\" : \"$gstn.expt_amt\",\"nil_amt\" : \"$gstn.nil_amt\",\"ngsup_amt\" : \"$gstn.ngsup_amt\",\"action\" : \"$gstn.action\",\"sply_ty\" : \"$gstn.sply_ty\"}} ,\"exp_typ\": {\"$first\": \"$gstn.exp_typ\"}, \"ctin\": {\"$first\": \"$gstn.ctin\"}, \"pos\": {\"$first\": \"$gstn.pos\"}, \"b2cs\": {\"$push\": { \"inv_id\": \"$_id\",\"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"sply_ty\": \"$gstn.sply_ty\",\"rt\": \"$gstn.rt\",\"typ\": \"$gstn.typ\",\"etin\": \"$gstn.etin\",\"pos\": \"$gstn.pos\",\"txval\": \"$gstn.txval\",\"camt\": \"$gstn.camt\",\"samt\": \"$gstn.samt\",\"iamt\": \"$gstn.iamt\",\"csamt\": \"$gstn.csamt\"}} , \"inv\": { \"$push\": { \"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"etin\": \"$gstn.etin\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"pos\" : \"$gstn.pos\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",    \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"b2cl_inv\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"etin\": \"$gstn.etin\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",    \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"nt\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"ntty\" : \"$gstn.ntty\",\"nt_num\": \"$gstn.nt_num\",    \"nt_dt\" : \"$gstn.nt_dt\",    \"p_gst\" : \"$gstn.p_gst\",    \"rsn\" : \"$gstn.rsn\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"itms\": \"$gstn.itms\"    } },    \"txpd\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\",    \"itms\": \"$gstn.itms\"    } },    \"at\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\",    \"itms\": \"$gstn.itms\"    } }, \"data\": { \"$push\": {\"inv_id\": \"$_id\",\"num\": \"$gstn.num\",    \"hsn_sc\": \"$gstn.hsn_sc\",    \"desc\": \"$gstn.desc\",    \"uqc\": \"$gstn.uqc\",    \"qty\": \"$gstn.qty\",    \"val\": \"$gstn.val\",    \"txval\": \"$gstn.txval\", \"camt\": \"$gstn.camt\",\"samt\": \"$gstn.samt\",\"iamt\": \"$gstn.iamt\",    \"csamt\": \"$gstn.csamt\"    } }    }}";
		String group2 = "{ \"$group\": { \"_id\": \"$gstin_con\",    \"sec\": {\"$first\": \"$_id.sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"},    \"cur_gt\": {\"$first\": \"$cur_gt\"},\"gt\":  {\"$first\": \"$gt\"},    \"b2b\": { \"$push\": {    \"ctin\": \"$ctin\",\"inv\" : \"$inv\"} },\"b2cs\":  {\"$first\": \"$b2cs\"},    \"b2cl\": { \"$push\": {    \"pos\": \"$pos\",\"inv\" : \"$b2cl_inv\"} },    \"exp\": { \"$push\": {  \"exp_typ\": \"$exp_typ\",\"inv\" : \"$inv\"} },\"nil\": {\"$first\": {\"inv\": \"$nil_inv\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} } ,    \"doc_issue\": {\"$first\": {\"doc_det\": \"$doc_det\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} } ,\"cdnr\": { \"$push\": {    \"ctin\": \"$ctin\",\"nt\" : \"$nt\"} },\"cdnur\": {\"$first\": \"$cdnur\"},\"txpd\": { \"$first\": \"$txpd\"},\"at\": { \"$first\": \"$at\"},    \"hsn\": { \"$first\": { \"data\": \"$data\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} }     }    }";
		String group3 = "{ \"$group\": { \"_id\": \"$_id\", \"inv_id\": {\"$first\": \"$inv_id\"},   \"sec\": {\"$first\": \"$sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"}, \"cur_gt\": {\"$first\": \"$cur_gt\"},   \"gt\":  {\"$first\": \"$gt\"},    \"b2b\": {\"$first\": \"$b2b\"},    \"b2cs\": {\"$first\": \"$b2cs\"},    \"b2cl\": {\"$first\": \"$b2cl\"},    \"exp\": {\"$first\": \"$exp\"},    \"nil\": {\"$first\": \"$nil\"},    \"doc_issue\": {\"$first\": \"$doc_issue\"},    \"cdnr\": {\"$first\": \"$cdnr\"},    \"cdnur\": {\"$first\": \"$cdnur\"},    \"txpd\": {\"$first\": \"$txpd\"},    \"at\": {\"$first\": \"$at\"},    \"hsn\": { \"$first\": \"$hsn\"}    }    }";

		
		long t1 = System.currentTimeMillis();
		Aggregate.ResultsIterator<Map> resultAgg = null;
		if (queryType) {
			resultAgg = supplies.aggregate(match0, gstin).and(match1, fp).and(match2).and(match3)
					.and(invLvlMatch).and(skip, offset).and(limit, pageSize)
					.and(project2).and(unwind).and(group1).and(project3)
					.and(group2).and(group3).and(project1).as(Map.class);
		} else {
			resultAgg = supplies.aggregate(match0, gstin).and(match1, fp).and(match3).and(aggSecMatch,section).and(project2)
					.and(unwind).and(group1).and(project3).and(group2).and(group3).and(project1).as(Map.class);
//			System.out.println(supplies.toString());
		}

		long t2 = System.currentTimeMillis();
		log.info("SaveToGstnGetData TAT {}", (t2 - t1));

		if (resultAgg != null) {
			Iterator<Map> it = resultAgg.iterator();
			while (it.hasNext()) {
				mapList.add(it.next());
			}
		}

		log.debug("getDataForGstn method: After inserting in mongo database");
		log.info("getDataForGstn method: END");

		return mapList;

	}
	
	
	/*@Override
	public List<Map<String, Object>> getDataForGstnInBatchesByIds(Map<String, String> allRequestParams,List<String> ids,
					boolean isAggregate,String section,String collection) {

		log.debug("getDataForGstn method: START");
		log.info("getDataForGstn method: START");
		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);
		// Map<Integer,Object> batchList = new HashMap<Integer,Object>();
		List<Map<String, Object>> mapList = new ArrayList<>();
		Jongo jongo = new Jongo(mongoGstnTemplate.getDb());
		MongoCollection supplies = jongo.getCollection(collection);
		String project1 = "{\"$project\": { \"result\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$sec\", \"b2b\"] }] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2b\": \"$b2b\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cl\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cl\": \"$b2cl\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cs\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cs\": \"$b2cs\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"exp\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"exp\": \"$exp\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"nil\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"nil\": \"$nil\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"cdnr\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnr\": \"$cdnr\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"cdnur\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnur\": \"$cdnur\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"doc_issue\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"doc_issue\": \"$doc_issue\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"txpd\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"txpd\": \"$txpd\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"at\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"at\": \"$at\"},\"else\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"hsn\": \"$hsn\"}}}}}}}}}}}}}}}}}}}}}}}";
		String project2 = "{\"$project\": {\"gstn.itms.custom\":0, \"gstn.custom\":0, \"gstn.docs.custom\":0}}";
		String project3 = "{\"$project\": {\"b2cs.date\":0, \"b2cs.stid\":0, \"b2cs.action\":0, \"nil_inv.action\": 0, \"nil_inv.stid\": 0}}";
		String match0 = "{\"$match\": { $or: [{ \"header.gstin\":# }] } }";
		String match1 = "{\"$match\": { $or: [{ \"header.fp\":# }] } }";
		String match2 = "{\"$match\":{\"$or\":[{\"control.gst_status\":\"New\"},{\"control.gst_status\":\"GstnUpload\"}]}}";
		String match3 = "{\"$match\": {\"control.ver\": {$in: [\"v3.1\"] } } }";
		String match4 = "{\"$match\": {\"_id\": {$in: # } } }";
		String secMatch = "{\"$match\": { $or: [{ \"control.type\":# }] } }";
		String unwind = "{\"$unwind\": \"$gstn\"}";
		String group1 = "{ \"$group\": {\"_id\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2b\"] },{ \"$eq\": [ \"$control.type\", \"cdnr\"] }] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"ctin\": \"$gstn.ctin\"},\"else\": {\"$cond\": {    \"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2cl\"] }] },    \"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"pos\": \"$gstn.pos\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$control.type\", \"exp\"] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"exp_typ\": \"$gstn.exp_typ\"},\"else\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\"}   }}}}}},\"gstin_con\": {\"$first\": { \"$concat\": [\"$header.gstin\", \":\", \"$control.type\", \":\", \"$header.fp\"] } },    \"gstin\": {\"$first\": \"$header.gstin\" },    \"fp\": {\"$first\": \"$header.fp\"},\"sec\": {\"$first\": \"$control.type\"},\"cur_gt\": {\"$first\": \"$header.curr_gt\"},\"gt\": {\"$first\": \"$header.gt\"}, \"cdnur\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"typ\": \"$gstn.typ\",    \"ntty\": \"$gstn.ntty\",    \"nt_num\":  \"$gstn.nt_num\",    \"nt_dt\":  \"$gstn.nt_dt\",\"p_gst\": \"$gstn.p_gst\",\"rsn\": \"$gstn.rsn\",    \"inum\":  \"$gstn.inum\",    \"idt\": \"$gstn.idt\" ,    \"val\":  \"$gstn.val\" ,\"itms\": \"$gstn.itms\"} }, \"doc_det\": {\"$push\": {\"inv_id\": \"$_id\", \"doc_num\" : \"$gstn.doc_num\",\"docs\" : \"$gstn.docs\"} } ,\"nil_inv\":{\"$push\": {\"inv_id\": \"$_id\",\"expt_amt\" : \"$gstn.expt_amt\",\"nil_amt\" : \"$gstn.nil_amt\",\"ngsup_amt\" : \"$gstn.ngsup_amt\",\"action\" : \"$gstn.action\",\"sply_ty\" : \"$gstn.sply_ty\"}} ,\"exp_typ\": {\"$first\": \"$gstn.exp_typ\"}, \"ctin\": {\"$first\": \"$gstn.ctin\"}, \"pos\": {\"$first\": \"$gstn.pos\"}, \"b2cs\": {\"$push\": { \"inv_id\": \"$_id\",\"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"sply_ty\": \"$gstn.sply_ty\",\"rt\": \"$gstn.rt\",\"typ\": \"$gstn.typ\",\"etin\": \"$gstn.etin\",\"pos\": \"$gstn.pos\",\"txval\": \"$gstn.txval\",\"camt\": \"$gstn.camt\",\"samt\": \"$gstn.samt\",\"iamt\": \"$gstn.iamt\",\"csamt\": \"$gstn.csamt\"}} , \"inv\": { \"$push\": { \"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"etin\": \"$gstn.etin\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"pos\" : \"$gstn.pos\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",    \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"b2cl_inv\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"etin\": \"$gstn.etin\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",    \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"nt\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"ntty\" : \"$gstn.ntty\",\"nt_num\": \"$gstn.nt_num\",    \"nt_dt\" : \"$gstn.nt_dt\",    \"p_gst\" : \"$gstn.p_gst\",    \"rsn\" : \"$gstn.rsn\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"itms\": \"$gstn.itms\"    } },    \"txpd\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\",    \"itms\": \"$gstn.itms\"    } },    \"at\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\",    \"itms\": \"$gstn.itms\"    } }, \"data\": { \"$push\": {\"inv_id\": \"$_id\",\"num\": \"$gstn.num\",    \"hsn_sc\": \"$gstn.hsn_sc\",    \"desc\": \"$gstn.desc\",    \"uqc\": \"$gstn.uqc\",    \"qty\": \"$gstn.qty\",    \"val\": \"$gstn.val\",    \"txval\": \"$gstn.txval\", \"camt\": \"$gstn.camt\",\"samt\": \"$gstn.samt\",\"iamt\": \"$gstn.iamt\",    \"csamt\": \"$gstn.csamt\"    } }    }}";
		String group2 = "{ \"$group\": { \"_id\": \"$gstin_con\",    \"sec\": {\"$first\": \"$_id.sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"},    \"cur_gt\": {\"$first\": \"$cur_gt\"},\"gt\":  {\"$first\": \"$gt\"},    \"b2b\": { \"$push\": {    \"ctin\": \"$ctin\",\"inv\" : \"$inv\"} },\"b2cs\":  {\"$first\": \"$b2cs\"},    \"b2cl\": { \"$push\": {    \"pos\": \"$pos\",\"inv\" : \"$b2cl_inv\"} },    \"exp\": { \"$push\": {  \"exp_typ\": \"$exp_typ\",\"inv\" : \"$inv\"} },\"nil\": {\"$first\": {\"inv\": \"$nil_inv\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} } ,    \"doc_issue\": {\"$first\": {\"doc_det\": \"$doc_det\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} } ,\"cdnr\": { \"$push\": {    \"ctin\": \"$ctin\",\"nt\" : \"$nt\"} },\"cdnur\": {\"$first\": \"$cdnur\"},\"txpd\": { \"$first\": \"$txpd\"},\"at\": { \"$first\": \"$at\"},    \"hsn\": { \"$first\": { \"data\": \"$data\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} }     }    }";
		String group3 = "{ \"$group\": { \"_id\": \"$_id\", \"inv_id\": {\"$first\": \"$inv_id\"},   \"sec\": {\"$first\": \"$sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"}, \"cur_gt\": {\"$first\": \"$cur_gt\"},   \"gt\":  {\"$first\": \"$gt\"},    \"b2b\": {\"$first\": \"$b2b\"},    \"b2cs\": {\"$first\": \"$b2cs\"},    \"b2cl\": {\"$first\": \"$b2cl\"},    \"exp\": {\"$first\": \"$exp\"},    \"nil\": {\"$first\": \"$nil\"},    \"doc_issue\": {\"$first\": \"$doc_issue\"},    \"cdnr\": {\"$first\": \"$cdnr\"},    \"cdnur\": {\"$first\": \"$cdnur\"},    \"txpd\": {\"$first\": \"$txpd\"},    \"at\": {\"$first\": \"$at\"},    \"hsn\": { \"$first\": \"$hsn\"}    }    }";

		
		long t1 = System.currentTimeMillis();
		Aggregate.ResultsIterator<Map> resultAgg = null;
		if (!isAggregate) {
			resultAgg = supplies.aggregate(match0, gstin).and(match1, fp).and(match2).and(match3)
					.and(match4, ids).and(secMatch,section).and(project2).and(unwind).and(group1).and(project3)
					.and(group2).and(group3).and(project1).as(Map.class);
		} else {
			resultAgg = supplies.aggregate(match0, gstin).and(match1, fp).and(match3).and(secMatch,section)
					.and(match4, ids).and(project2).and(unwind).and(group1).and(project3).and(group2).and(group3).and(project1).as(Map.class);
//			System.out.println(supplies.toString());
		}

		long t2 = System.currentTimeMillis();
		log.info("SaveToGstnGetData TAT {}", (t2 - t1));

		if (resultAgg != null) {
			Iterator<Map> it = resultAgg.iterator();
			while (it.hasNext()) {
				mapList.add(it.next());
			}
		}

		log.debug("getDataForGstn method: After inserting in mongo database");
		log.info("getDataForGstn method: END");

		return mapList;

	}*/
	
	@Override
	public List<Map<String, Object>> getDataForGstnInBatchesByIds(Map<String, String> allRequestParams,List<String> ids,
					boolean isAggregate,String section,String collection) {

		log.debug("getDataForGstn method: START");
		log.info("getDataForGstn method: START");
		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);
		// Map<Integer,Object> batchList = new HashMap<Integer,Object>();
		List<Map<String, Object>> mapList = new ArrayList<>();
		Jongo jongo = new Jongo((DB) mongoGstnTemplate.getDb());
		MongoCollection supplies = jongo.getCollection(collection);
		
		AggregationOptions options = AggregationOptions.builder().allowDiskUse(true).build();

		String project2 = "{\"$project\": {\"gstn.itms.custom\":0, \"gstn.custom\":0, \"gstn.docs.custom\":0}}";
		
		String match0 = "{\"$match\": { $or: [{ \"header.gstin\":# }] } }";
		String match1 = "{\"$match\": { $or: [{ \"header.fp\":# }] } }";
		String match2 = "{\"$match\":{\"$or\":[{\"control.gst_status\":\"New\"},{\"control.gst_status\":\"GstnUpload\"}]}}";
		String match3 = "{\"$match\": {\"control.ver\": {$in: [\"v3.1\"] } } }";
		String match4 = "{\"$match\": {\"_id\": {$in: # } } }";
		String secMatch = "{\"$match\": { $or: [{ \"control.type\":# }] } }";
		String unwind = "{\"$unwind\": \"$gstn\"}";
		
	/*	String group1 = "{ \"$group\": {\"_id\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2b\"] },{ \"$eq\": [ \"$control.type\", \"cdnr\"] }] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"ctin\": \"$gstn.ctin\"},\"else\": {\"$cond\": {    \"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2cl\"] }] },    \"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"pos\": \"$gstn.pos\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$control.type\", \"exp\"] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"exp_typ\": \"$gstn.exp_typ\"},\"else\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\"}   }}}}}},\"gstin_con\": {\"$first\": { \"$concat\": [\"$header.gstin\", \":\", \"$control.type\", \":\", \"$header.fp\"] } },    \"gstin\": {\"$first\": \"$header.gstin\" },    \"fp\": {\"$first\": \"$header.fp\"},\"sec\": {\"$first\": \"$control.type\"},\"cur_gt\": {\"$first\": \"$header.curr_gt\"},\"gt\": {\"$first\": \"$header.gt\"}, \"cdnur\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"typ\": \"$gstn.typ\",    \"ntty\": \"$gstn.ntty\",    \"nt_num\":  \"$gstn.nt_num\",    \"nt_dt\":  \"$gstn.nt_dt\",\"p_gst\": \"$gstn.p_gst\",\"rsn\": \"$gstn.rsn\",    \"inum\":  \"$gstn.inum\",    \"idt\": \"$gstn.idt\" ,    \"val\":  \"$gstn.val\" ,\"itms\": \"$gstn.itms\"} }, \"doc_det\": {\"$push\": {\"inv_id\": \"$_id\", \"doc_num\" : \"$gstn.doc_num\",\"docs\" : \"$gstn.docs\"} } ,\"nil_inv\":{\"$push\": {\"inv_id\": \"$_id\",\"expt_amt\" : \"$gstn.expt_amt\",\"nil_amt\" : \"$gstn.nil_amt\",\"ngsup_amt\" : \"$gstn.ngsup_amt\",\"action\" : \"$gstn.action\",\"sply_ty\" : \"$gstn.sply_ty\"}} ,\"exp_typ\": {\"$first\": \"$gstn.exp_typ\"}, \"ctin\": {\"$first\": \"$gstn.ctin\"}, \"pos\": {\"$first\": \"$gstn.pos\"}, \"b2cs\": {\"$push\": { \"inv_id\": \"$_id\",\"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"sply_ty\": \"$gstn.sply_ty\",\"rt\": \"$gstn.rt\",\"typ\": \"$gstn.typ\",\"etin\": \"$gstn.etin\",\"pos\": \"$gstn.pos\",\"txval\": \"$gstn.txval\",\"camt\": \"$gstn.camt\",\"samt\": \"$gstn.samt\",\"iamt\": \"$gstn.iamt\",\"csamt\": \"$gstn.csamt\"}} , \"inv\": { \"$push\": { \"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"etin\": \"$gstn.etin\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"pos\" : \"$gstn.pos\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",    \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"b2cl_inv\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"etin\": \"$gstn.etin\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",    \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"nt\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"ntty\" : \"$gstn.ntty\",\"nt_num\": \"$gstn.nt_num\",    \"nt_dt\" : \"$gstn.nt_dt\",    \"p_gst\" : \"$gstn.p_gst\",    \"rsn\" : \"$gstn.rsn\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"itms\": \"$gstn.itms\"    } },    \"txpd\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\",    \"itms\": \"$gstn.itms\"    } },    \"at\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\",    \"itms\": \"$gstn.itms\"    } }, \"data\": { \"$push\": {\"inv_id\": \"$_id\",\"num\": \"$gstn.num\",    \"hsn_sc\": \"$gstn.hsn_sc\",    \"desc\": \"$gstn.desc\",    \"uqc\": \"$gstn.uqc\",    \"qty\": \"$gstn.qty\",    \"val\": \"$gstn.val\",    \"txval\": \"$gstn.txval\", \"camt\": \"$gstn.camt\",\"samt\": \"$gstn.samt\",\"iamt\": \"$gstn.iamt\",    \"csamt\": \"$gstn.csamt\"    } }    }}";
		String project3 = "{\"$project\": {\"b2cs.date\":0, \"b2cs.stid\":0, \"b2cs.action\":0, \"nil_inv.action\": 0, \"nil_inv.stid\": 0}}";
		String group2 = "{ \"$group\": { \"_id\": \"$gstin_con\",    \"sec\": {\"$first\": \"$_id.sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"},    \"cur_gt\": {\"$first\": \"$cur_gt\"},\"gt\":  {\"$first\": \"$gt\"},    \"b2b\": { \"$push\": {    \"ctin\": \"$ctin\",\"inv\" : \"$inv\"} },\"b2cs\":  {\"$first\": \"$b2cs\"},    \"b2cl\": { \"$push\": {    \"pos\": \"$pos\",\"inv\" : \"$b2cl_inv\"} },    \"exp\": { \"$push\": {  \"exp_typ\": \"$exp_typ\",\"inv\" : \"$inv\"} },\"nil\": {\"$first\": {\"inv\": \"$nil_inv\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} } ,    \"doc_issue\": {\"$first\": {\"doc_det\": \"$doc_det\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} } ,\"cdnr\": { \"$push\": {    \"ctin\": \"$ctin\",\"nt\" : \"$nt\"} },\"cdnur\": {\"$first\": \"$cdnur\"},\"txpd\": { \"$first\": \"$txpd\"},\"at\": { \"$first\": \"$at\"},    \"hsn\": { \"$first\": { \"data\": \"$data\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} }     }    }";
		String group3 = "{ \"$group\": { \"_id\": \"$_id\", \"inv_id\": {\"$first\": \"$inv_id\"},   \"sec\": {\"$first\": \"$sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"}, \"cur_gt\": {\"$first\": \"$cur_gt\"},   \"gt\":  {\"$first\": \"$gt\"},    \"b2b\": {\"$first\": \"$b2b\"},    \"b2cs\": {\"$first\": \"$b2cs\"},    \"b2cl\": {\"$first\": \"$b2cl\"},    \"exp\": {\"$first\": \"$exp\"},    \"nil\": {\"$first\": \"$nil\"},    \"doc_issue\": {\"$first\": \"$doc_issue\"},    \"cdnr\": {\"$first\": \"$cdnr\"},    \"cdnur\": {\"$first\": \"$cdnur\"},    \"txpd\": {\"$first\": \"$txpd\"},    \"at\": {\"$first\": \"$at\"},    \"hsn\": { \"$first\": \"$hsn\"}    }    }";
		String project1 = "{\"$project\": { \"result\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$sec\", \"b2b\"] }] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2b\": \"$b2b\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cl\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cl\": \"$b2cl\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cs\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cs\": \"$b2cs\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"exp\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"exp\": \"$exp\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"nil\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"nil\": \"$nil\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"cdnr\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnr\": \"$cdnr\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"cdnur\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnur\": \"$cdnur\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"doc_issue\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"doc_issue\": \"$doc_issue\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"txpd\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"txpd\": \"$txpd\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"at\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"at\": \"$at\"},\"else\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"hsn\": \"$hsn\"}}}}}}}}}}}}}}}}}}}}}}}";
*/		
		//Db queries modified for implementing amendments -- Prachi
		
		String group1 = "{ \"$group\": {\"_id\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2b\"] },{ \"$eq\": [ \"$control.type\", \"b2ba\"] },{ \"$eq\": [ \"$control.type\", \"cdnr\"] },{ \"$eq\": [ \"$control.type\", \"cdnra\"] }] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"ctin\": \"$gstn.ctin\"},\"else\": {\"$cond\": {    \"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"b2cl\"] }, { \"$eq\": [ \"$control.type\", \"b2cla\"] }] },    \"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"pos\": \"$gstn.pos\"},\"else\": {\"$cond\": {    \"if\": {\"$or\" : [{ \"$eq\": [ \"$control.type\", \"exp\"] },{ \"$eq\": [ \"$control.type\", \"expa\"] }] },\"then\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\", \"exp_typ\": \"$gstn.exp_typ\"},\"else\": {\"gstn\": \"$header.gstin\", \"fp\": \"$header.fp\" , \"sec\": \"$control.type\"}   }}}}}},\"gstin_con\": {\"$first\": { \"$concat\": [\"$header.gstin\", \":\", \"$control.type\", \":\", \"$header.fp\"] } },    \"gstin\": {\"$first\": \"$header.gstin\" },    \"fp\": {\"$first\": \"$header.fp\"},\"sec\": {\"$first\": \"$control.type\"},\"cur_gt\": {\"$first\": \"$header.curr_gt\"},\"gt\": {\"$first\": \"$header.gt\"}, \"cdnur\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"typ\": \"$gstn.typ\",    \"ntty\": \"$gstn.ntty\",    \"ont_num\":  \"$gstn.ont_num\", \"ont_dt\":  \"$gstn.ont_dt\", \"nt_num\":  \"$gstn.nt_num\",    \"nt_dt\":  \"$gstn.nt_dt\",\"p_gst\": \"$gstn.p_gst\",\"rsn\": \"$gstn.rsn\",    \"inum\":  \"$gstn.inum\",    \"idt\": \"$gstn.idt\" ,    \"val\":  \"$gstn.val\" ,\"itms\": \"$gstn.itms\"} }, \"doc_det\": {\"$push\": {\"inv_id\": \"$_id\", \"doc_num\" : \"$gstn.doc_num\",\"docs\" : \"$gstn.docs\"} } ,\"nil_inv\":{\"$push\": {\"inv_id\": \"$_id\",\"expt_amt\" : \"$gstn.expt_amt\",\"nil_amt\" : \"$gstn.nil_amt\",\"ngsup_amt\" : \"$gstn.ngsup_amt\",\"action\" : \"$gstn.action\",\"sply_ty\" : \"$gstn.sply_ty\"}} ,\"exp_typ\": {\"$first\": \"$gstn.exp_typ\"}, \"ctin\": {\"$first\": \"$gstn.ctin\"}, \"pos\": {\"$first\": \"$gstn.pos\"}, \"b2cs\": {\"$push\": { \"inv_id\": \"$_id\",\"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"omon\": \"$gstn.omon\",\"sply_ty\": \"$gstn.sply_ty\",\"rt\": \"$gstn.rt\",\"typ\": \"$gstn.typ\",\"etin\": \"$gstn.etin\",\"pos\": \"$gstn.pos\",\"opos\": \"$gstn.opos\",\"txval\": \"$gstn.txval\",\"camt\": \"$gstn.camt\",\"samt\": \"$gstn.samt\",\"iamt\": \"$gstn.iamt\",\"csamt\": \"$gstn.csamt\"}} , \"inv\": { \"$push\": { \"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"etin\": \"$gstn.etin\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"pos\" : \"$gstn.pos\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",   \"oinum\": \"$gstn.oinum\",\"oidt\": \"$gstn.oidt\", \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"b2cl_inv\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"etin\": \"$gstn.etin\",    \"oinum\" : \"$gstn.oinum\", \"oidt\" : \"$gstn.oidt\",\"inum\" : \"$gstn.inum\",   \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"rchrg\" : \"$gstn.rchrg\",    \"inv_typ\" : \"$gstn.inv_typ\",    \"sbpcode\": \"$gstn.sbpcode\",    \"sbnum\": \"$gstn.sbnum\",    \"sbdt\": \"$gstn.sbdt\",    \"itms\": \"$gstn.itms\"} }, \"nt\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"ntty\" : \"$gstn.ntty\",\"ont_num\": \"$gstn.ont_num\", \"ont_dt\": \"$gstn.ont_dt\",\"nt_num\": \"$gstn.nt_num\",   \"nt_dt\" : \"$gstn.nt_dt\",    \"p_gst\" : \"$gstn.p_gst\",    \"rsn\" : \"$gstn.rsn\",    \"inum\" : \"$gstn.inum\",    \"idt\" : \"$gstn.idt\",    \"val\" : \"$gstn.val\",    \"itms\": \"$gstn.itms\"    } },    \"txpd\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\",   \"omon\": \"$gstn.omon\",  \"itms\": \"$gstn.itms\"    } },    \"at\": { \"$push\": {\"inv_id\": \"$_id\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\",\"pos\" : \"$gstn.pos\",\"sply_ty\": \"$gstn.sply_ty\", \"omon\": \"$gstn.omon\",    \"itms\": \"$gstn.itms\"    } }, \"data\": { \"$push\": {\"inv_id\": \"$_id\",\"num\": \"$gstn.num\",    \"hsn_sc\": \"$gstn.hsn_sc\",    \"desc\": \"$gstn.desc\",    \"uqc\": \"$gstn.uqc\",    \"qty\": \"$gstn.qty\",    \"val\": \"$gstn.val\",    \"txval\": \"$gstn.txval\", \"camt\": \"$gstn.camt\",\"samt\": \"$gstn.samt\",\"iamt\": \"$gstn.iamt\",    \"csamt\": \"$gstn.csamt\"    } }    }}";
		String project3 = "{\"$project\": {\"b2cs.date\":0, \"b2cs.stid\":0, \"b2cs.action\":0, \"nil_inv.action\": 0, \"nil_inv.stid\": 0}}";
		String group2 = "{ \"$group\": { \"_id\": \"$gstin_con\",    \"sec\": {\"$first\": \"$_id.sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"},    \"cur_gt\": {\"$first\": \"$cur_gt\"},\"gt\":  {\"$first\": \"$gt\"},    \"b2b\": { \"$push\": {    \"ctin\": \"$ctin\",\"inv\" : \"$inv\"} }, \"b2ba\": { \"$push\": {    \"ctin\": \"$ctin\",\"inv\" : \"$inv\"} },\"b2cs\":  {\"$first\": \"$b2cs\"}, \"b2csa\":  {\"$first\": \"$b2cs\"},    \"b2cl\": { \"$push\": {    \"pos\": \"$pos\",\"inv\" : \"$b2cl_inv\"} },\"b2cla\": { \"$push\": {    \"pos\": \"$pos\",\"inv\" : \"$b2cl_inv\"} },    \"exp\": { \"$push\": {  \"exp_typ\": \"$exp_typ\",\"inv\" : \"$inv\"} },\"expa\": { \"$push\": {  \"exp_typ\": \"$exp_typ\",\"inv\" : \"$inv\"} }, \"nil\": {\"$first\": {\"inv\": \"$nil_inv\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} } ,    \"doc_issue\": {\"$first\": {\"doc_det\": \"$doc_det\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} } ,\"cdnr\": { \"$push\": {    \"ctin\": \"$ctin\",\"nt\" : \"$nt\"} },\"cdnra\": { \"$push\": {    \"ctin\": \"$ctin\",\"nt\" : \"$nt\"} },\"cdnur\": {\"$first\": \"$cdnur\"},\"cdnura\": {\"$first\": \"$cdnur\"},\"txpd\": { \"$first\": \"$txpd\"},\"txpda\": { \"$first\": \"$txpd\"},\"at\": { \"$first\": \"$at\"},\"ata\": { \"$first\": \"$at\"},    \"hsn\": { \"$first\": { \"data\": \"$data\", \"chksum\": \"$gstn.chksum\",\"flag\": \"$gstn.flag\"} }     }    }";
		String group3 = "{ \"$group\": { \"_id\": \"$_id\", \"inv_id\": {\"$first\": \"$inv_id\"},   \"sec\": {\"$first\": \"$sec\"},    \"gstin\": {\"$first\": \"$gstin\"},  \"fp\": {\"$first\": \"$fp\"}, \"cur_gt\": {\"$first\": \"$cur_gt\"},   \"gt\":  {\"$first\": \"$gt\"},    \"b2b\": {\"$first\": \"$b2b\"},\"b2ba\": {\"$first\": \"$b2ba\"},    \"b2cs\": {\"$first\": \"$b2cs\"}, \"b2csa\": {\"$first\": \"$b2csa\"},   \"b2cl\": {\"$first\": \"$b2cl\"}, \"b2cla\": {\"$first\": \"$b2cla\"},    \"exp\": {\"$first\": \"$exp\"},\"expa\": {\"$first\": \"$expa\"},    \"nil\": {\"$first\": \"$nil\"},    \"doc_issue\": {\"$first\": \"$doc_issue\"},    \"cdnr\": {\"$first\": \"$cdnr\"}, \"cdnra\": {\"$first\": \"$cdnra\"},   \"cdnur\": {\"$first\": \"$cdnur\"}, \"cdnura\": {\"$first\": \"$cdnura\"},   \"txpd\": {\"$first\": \"$txpd\"},\"txpda\": {\"$first\": \"$txpda\"},    \"at\": {\"$first\": \"$at\"},\"ata\": {\"$first\": \"$ata\"},    \"hsn\": { \"$first\": \"$hsn\"}}}";
		String project1 = "{\"$project\": { \"result\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$sec\", \"b2b\"] }] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2b\": \"$b2b\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cl\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cl\": \"$b2cl\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cs\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cs\": \"$b2cs\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"exp\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"exp\": \"$exp\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"nil\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"nil\": \"$nil\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"cdnr\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnr\": \"$cdnr\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"cdnur\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnur\": \"$cdnur\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"doc_issue\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"doc_issue\": \"$doc_issue\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"txpd\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"txpd\": \"$txpd\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"at\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"at\": \"$at\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"expa\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"expa\": \"$expa\"},\"else\": {\"$cond\": {\"if\": {\"$or\" : [{ \"$eq\": [ \"$sec\", \"b2ba\"] }] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2ba\": \"$b2ba\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"cdnra\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnra\": \"$cdnra\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2cla\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2cla\": \"$b2cla\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"cdnura\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"cdnura\": \"$cdnura\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"b2csa\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"b2csa\": \"$b2csa\"},\"else\": {\"$cond\": {\"if\": { \"$eq\": [ \"$sec\", \"ata\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"ata\": \"$ata\"},\"else\": {\"$cond\": {    \"if\": { \"$eq\": [ \"$sec\", \"txpda\"] },\"then\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"txpda\": \"$txpda\"},\"else\": {\"gstin\": \"$gstin\", \"fp\": \"$fp\" , \"cur_gt\": \"$cur_gt\", \"gt\": \"$gt\", \"hsn\": \"$hsn\"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}";
		
		long t1 = System.currentTimeMillis();
		Aggregate.ResultsIterator<Map> resultAgg = null;
		if (!isAggregate) {
			resultAgg = supplies.aggregate(match0, gstin).and(match1, fp).and(match2).and(match3)
					.and(match4, ids).and(secMatch,section).and(project2).and(unwind).and(group1).and(project3)
					.and(group2).and(group3).and(project1).options(options).as(Map.class);
		} else {
			resultAgg = supplies.aggregate(match0, gstin).and(match1, fp).and(match3).and(secMatch,section)
					.and(match4, ids).and(project2).and(unwind).and(group1).and(project3).and(group2).and(group3).and(project1).options(options).as(Map.class);
//			System.out.println(supplies.toString());
		}

		long t2 = System.currentTimeMillis();
		log.info("SaveToGstnGetData TAT {}", (t2 - t1));

		if (resultAgg != null) {
			Iterator<Map> it = resultAgg.iterator();
			while (it.hasNext()) {
				mapList.add(it.next());
			}
		}

		log.debug("getDataForGstn method: After inserting in mongo database");
		log.info("getDataForGstn method: END");

		return mapList;

	}


	@Override
	public long getRecordsCount(Map<String, String> allRequestParams, String collection, List<String> sectionList) {
		String gstin = allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
		String fp = allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);

		long totalRecords = 0;
		Query query = new Query();
		query.addCriteria(Criteria.where(Gstr1ConstantsV31.HEADER_GSTIN_DB).is(gstin)
				.and(Gstr1ConstantsV31.HEADER_FP_DB).is(fp).and(Gstr1ConstantsV31.CONTROL_GSTN_STATUS)
				.in(Gstr1ConstantsV31.INVOICE_STATE_NEW,Gstr1ConstantsV31.STATUS_UPLOAD).and(Gstr1ConstantsV31.CONTROL_TYPE).in(sectionList));

		totalRecords = mongoTemplate.count(query, collection);
		/*
		 * List<Map> result = mongoTemplate.find(query, Map.class, collection);
		 * System.out.println("**************number of records**************" +
		 * totalRecords);
		 * System.out.println("**************result**************" + result);
		 */
		log.info("number of records read from db" + totalRecords);

		return totalRecords;

	}
	
	@Override
	public Map<String,List<String>>  getIdsForSaveToGstn(Map<String, Object> matchMap, List<String> fieldNames, String collection,
				String gstin) {
		log.debug("updateInMongo method: END");
		List<String> type = (List<String>) MapUtils.getObject(matchMap, "control.type", null);
		Criteria crt = new Criteria("header.gstin").is(gstin);
		Iterator<String> itr = matchMap.keySet().iterator();
		List<Criteria> cList=new ArrayList<>();
		while (itr.hasNext()) {
			String key = itr.next();
			Object value = matchMap.get(key);
			if (value instanceof List<?> && CollectionUtils.isNotEmpty(type)) {
				List<String> l=((List<String>)value);
				cList.add(Criteria.where(key).in(l));
			} else {
				if(value!=null){
					cList.add(Criteria.where(key).in(value));
				}
			}
		}
		crt.andOperator(cList.toArray(new Criteria[cList.size()]));
		Map<String, List<String>> idsMap = null;
		GroupOperation groupBy = Aggregation.group("control.type").push("control._id").as("recId");
		MatchOperation match = Aggregation.match(crt);
		Aggregation aggregation = Aggregation.newAggregation(match, groupBy);
		AggregationResults<Map> idsList = mongoTemplate.aggregate(aggregation, collection, Map.class);
		if (idsList != null && idsList.getMappedResults().size() > 0) {
			idsMap = new HashMap<>();
			for (Map map : idsList) {
				idsMap.put(MapUtils.getString(map, "_id"), (List<String>) map.get("recId"));
			}
		}
		log.debug("updateInMongo method: END");
		return idsMap;
	}
	/*
	 * 
	 * @Override public Map<String, Object> getL2Data(Map<String, String>
	 * allRequestParams, String collection) {
	 * 
	 * log.debug("retrieveSummaryData L2 method: START"); List<Object> l2 =
	 * null; Map<String, Object> respMap = null; DBCursor cursor = null; try {
	 * DBCollection monogCollection = mongoTemplate.getCollection(collection);
	 * BasicDBObject whereQuery = new BasicDBObject();
	 * 
	 * ReadPreference rp = null; String fp = (String)
	 * allRequestParams.get(Gstr1ConstantsV31.INPUT_FP); String gstin = (String)
	 * allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN); String section =
	 * (String) allRequestParams.get(Gstr1ConstantsV31.INPUT_SECTION); respMap =
	 * new HashMap<>(); int limit = 0; int skip = 0; try { limit =
	 * Integer.parseInt((String)
	 * allRequestParams.get(Gstr1ConstantsV31.INPUT_LIMIT)); skip =
	 * Integer.parseInt((String)
	 * allRequestParams.get(Gstr1ConstantsV31.INPUT_OFFSET)); int max_records =
	 * Integer.parseInt(environment.getProperty("max_records_fetched")); if
	 * ((limit - skip) > max_records) { log.info(
	 * "retrieveSummaryData L2 Method:Setting the max limit of L2 query reqlimit:{},maxLimit:{} "
	 * , limit, max_records); log.debug(
	 * "retrieveSummaryData L2 Method:Setting the max limit of L2 query reqlimit:{},maxLimit:{} "
	 * , limit, max_records); limit = max_records;
	 * respMap.put(Gstr1ConstantsV31.ASP_API_RESP_WARN_ATTR, "Only " +
	 * max_records + " records could be displayed at a time"); } } catch
	 * (Exception e) { limit = 20; skip = 0; } // log.info(
	 * "retrieveSummaryData L2 Method:Query for Criteria fp:{}, gstin:{}, section:{},limit:{},offset:{} "
	 * , // fp, gstin, section, limit, skip); // log.debug( //
	 * "retrieveSummaryData L2 Method:Query for Criteria fp:{}, gstin:{}, section:{},limit:{},offset:{} "
	 * , // fp, gstin, section, limit, skip);
	 * 
	 * whereQuery.put("header.gstin", gstin); whereQuery.put("header.type",
	 * section); whereQuery.put("header.fp", fp);
	 * 
	 * int ttlCount = monogCollection.find(whereQuery).count(); // cursor = //
	 * monogCollection.find(whereQuery).limit(limit).skip(skip).sort(new //
	 * BasicDBObject(sort, order)); cursor =
	 * monogCollection.find(whereQuery).limit(limit).skip(skip).
	 * setReadPreference(rp.nearest()); log.debug(
	 * "retrieveSummaryData L2 Method:After getting result from database");
	 * DBObject headerObj = null; l2 = new ArrayList<>(); while
	 * (cursor.hasNext()) { DBObject obj = cursor.next(); headerObj = (DBObject)
	 * obj.removeField(Gstr1ConstantsV31.CONTROL_JSON_HDR_SEC); DBObject gstnObj
	 * = (DBObject) obj.removeField(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC);
	 * DBObject controlObj = (DBObject)
	 * obj.removeField(Gstr1ConstantsV31.CONTROL_JSON_SEC); // String dbStatus =
	 * (String) // controlObj.get(Gstr1ConstantsV31.CONTROL_JSON_STATE);
	 * 
	 * obj.removeField(Gstr1ConstantsV31.CONTROL_REC_ID); if (gstnObj instanceof
	 * BasicDBObject) { // gstnObj.put(Gstr1ConstantsV31.CONTROL_JSON_STATE, //
	 * dbStatus); l2.add(gstnObj);
	 * 
	 * } else if (gstnObj instanceof BasicDBList) { Map<String, Object> itemMap
	 * = new HashMap<>(); Iterator<String> key = gstnObj.keySet().iterator();
	 * DBObject dbObject = null; List<Object> innerList = new ArrayList<>();
	 * while (key.hasNext()) {
	 * 
	 * dbObject = (DBObject) gstnObj.get(key.next()); //
	 * dbObject.put(Gstr1ConstantsV31.CONTROL_JSON_STATE, // dbStatus);
	 * innerList.add(dbObject);
	 * 
	 * }
	 * 
	 * // if (!(allRequestParams.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) ==
	 * null) // && (allRequestParams.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).
	 * equalsIgnoreCase("2"))) { // itemMap.put("id", (String)
	 * dbObject.get("stid")); // itemMap.put("rates", innerList); // } else //
	 * itemMap.put((String) dbObject.get("stid"), innerList); //
	 * l2.add(itemMap); // } else { l2.add(gstnObj); }
	 * 
	 * } if (CollectionUtils.isNotEmpty(l2)) { headerObj.removeField("sname");
	 * headerObj.removeField("gt"); headerObj.removeField("curr_gt");
	 * respMap.putAll(headerObj.toMap());
	 * respMap.put(Gstr1ConstantsV31.ASP_API_RESP_RCRD_ATTR, l2);
	 * respMap.put(Gstr1ConstantsV31.JSON_TTL_RCRD, ttlCount); } else { respMap
	 * = null; } log.info(
	 * "retrieveSummaryData L2 Method:After forming the response ");
	 * 
	 * } finally { if (cursor != null) { cursor.close(); } } log.debug(
	 * "retrieveSummaryData L2 method: END"); return respMap; }
	 * 
	 */

	@Override
	public Map<String, Object> getL2Data(Map<String, String> allRequestParams, String collection) {

		log.debug("retrieveSummaryData L2 method: START");
		List<Object> l2 = null;
		Map<String, Object> respMap = null;
		try {

			ReadPreference rp = null;
			String fp = (String) allRequestParams.get(Gstr1ConstantsV31.INPUT_FP);
			String gstin = (String) allRequestParams.get(Gstr1ConstantsV31.INPUT_GSTN);
			String section = (String) allRequestParams.get(Gstr1ConstantsV31.INPUT_SECTION);
			respMap = new HashMap<>();
			int limit = 0;
			int skip = 0;
		
			try {
				limit = Integer.parseInt((String) allRequestParams.get(Gstr1ConstantsV31.INPUT_LIMIT));
				skip = Integer.parseInt((String) allRequestParams.get(Gstr1ConstantsV31.INPUT_OFFSET));
				int max_records = Integer.parseInt(environment.getProperty("max_records_fetched"));
				if ((limit - skip) > max_records) {
					log.info("retrieveSummaryData L2 Method:Setting the max limit of L2 query reqlimit:{},maxLimit:{} ",
							limit, max_records);
					log.debug(
							"retrieveSummaryData L2 Method:Setting the max limit of L2 query reqlimit:{},maxLimit:{} ",
							limit, max_records);
					limit = max_records;
					respMap.put(Gstr1ConstantsV31.ASP_API_RESP_WARN_ATTR,
							"Only " + max_records + " records could be displayed at a time");
				}
			} catch (Exception e) {
				limit = 20;
				skip = 0;
			}
		
			// log.info("retrieveSummaryData L2 Method:Query for Criteria fp:{},
			// gstin:{}, section:{},limit:{},offset:{} ",
			// fp, gstin, section, limit, skip);
			// log.debug(
			// "retrieveSummaryData L2 Method:Query for Criteria fp:{},
			// gstin:{}, section:{},limit:{},offset:{} ",
			// fp, gstin, section, limit, skip);

			Query query = new Query();
			query.addCriteria(Criteria.where(Gstr1ConstantsV31.GSTIN_HEADER).is(gstin).and(Gstr1ConstantsV31.FP_HEADER)
					.is(fp).and(Gstr1ConstantsV31.TYPE_HEADER).is(section));
			log.debug("retrieveSummaryData L2 Method:FIRST QUERY START");
			long ttlCount = mongoTemplate.count(query, Object.class, collection);
			log.debug("retrieveSummaryData L2 Method:FIRST QUERY START");
			if (ttlCount > skip) {
				log.debug("retrieveSummaryData L2 Method:FIRST QUERY END");
				if(!allRequestParams.containsKey(Gstr1ConstantsV31.SYNC_FILTER))
				{
				
				query.skip(skip);
				query.limit(limit);
				}
				log.debug("retrieveSummaryData L2 Method:SECOND QUERY START");
				List<Map<String, Object>> list2 = (List) mongoTemplate.find(query, Object.class, collection);
				log.debug("retrieveSummaryData L2 Method:SECOND QUERY END");
				log.debug("retrieveSummaryData L2 Method:After getting result from database");
				Map<String, Object> headerObj = null;
				l2 = new ArrayList<>();
				for (int i = 0; i < list2.size(); i++) {
					Map<String, Object> obj = list2.get(i);
					headerObj = (Map<String, Object>) obj.remove(Gstr1ConstantsV31.CONTROL_JSON_HDR_SEC);
					Object gstnObj = obj.remove(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC);
					Map<String, Object> controlObj = (Map<String, Object>) obj
							.remove(Gstr1ConstantsV31.CONTROL_JSON_SEC);

					obj.remove(Gstr1ConstantsV31.CONTROL_REC_ID);
					if (gstnObj instanceof Map<?, ?>) {
						l2.add(gstnObj);

					} else if (gstnObj instanceof List) {
						Map<String, Object> itemMap = new HashMap<>();
						List list1 = (List) gstnObj;
						Map<Object, Object> dbObject = null;
						List<Object> innerList = new ArrayList<>();
						for (int i1 = 0; i1 < list1.size(); i1++) {
							dbObject = (Map<Object, Object>) list1.get(i1);
							innerList.add(dbObject);
						}
						l2.add(gstnObj);
					}

				}
				if (CollectionUtils.isNotEmpty(l2)) {
					headerObj.remove("sname");
					headerObj.remove("gt");
					headerObj.remove("curr_gt");
					respMap.putAll(headerObj);
					respMap.put(Gstr1ConstantsV31.ASP_API_RESP_RCRD_ATTR, l2);
					respMap.put(Gstr1ConstantsV31.JSON_TTL_RCRD, ttlCount);

				} else {
					respMap = null;
				}
				log.info("retrieveSummaryData L2 Method:After forming the response ");
			} else {
				respMap = null;
			}
		} finally {
		}

		log.debug("retrieveSummaryData L2 method: END");
		return respMap;
	}
	
	
	
	
		@Override
	public boolean getKeyStatus(Map<String, String> data, String collectionControl) {
		DBCollection monogCollection = (DBCollection) mongoTemplate.getCollection(collectionControl);
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("_id", data.get(Gstr1ConstantsV31.INPUT_ACK_NO));
		DBCursor cursor = monogCollection.find(whereQuery);
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				DBObject headerObj = (DBObject) obj.removeField(Gstr1ConstantsV31.CONTROL_JSON_HDR_SEC);
				String status = (String) headerObj.get("header.status");
				if ("true".equalsIgnoreCase(status)) {
					return true;
				}

			} 
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	@Override
	public void deleteL2Key(String mongoL2Key, String collection, String collectionControl) {
		DBCollection monogCollection = (DBCollection) mongoTemplate.getCollection(collectionControl);
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("_id",mongoL2Key);
		WriteResult cursor = null;
		try {
			  cursor = monogCollection.remove(whereQuery);
			monogCollection = (DBCollection) mongoTemplate.getCollection(collection);
			whereQuery.put("_id",new BasicDBObject("$regex",""+ mongoL2Key+".*"));
			cursor = monogCollection.remove(whereQuery);
			
		} finally {
			if (cursor != null) {
			
			}
		}


}

	@Override
	public List<Map<String, Object>> getMongoData(Map<String, Object> whrObj, String[] fields, String collection) {
		log.debug("getMongoData with Fields method: START");
		List<Map<String, Object>> result = null;
		if (whrObj != null) {
			Query query = new Query();
			Iterator<String> itr = whrObj.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				Object obj = MapUtils.getObject(whrObj, key, "");
				if(obj instanceof Set<?>){
					Set<String> l=((Set<String>)obj);
					query.addCriteria(Criteria.where(key).in(l));
				}else if (obj instanceof Object[]){
					Object []l=((Object[])obj);
					query.addCriteria(Criteria.where(key).in(l));
				}else if (obj instanceof List<?> ){
					List<String> l=((List<String>)obj);
					query.addCriteria(Criteria.where(key).in(l));
				}else{
					query.addCriteria(Criteria.where(key).is(obj));					
				}
			}
			if(fields!=null && fields.length>0){
				for (String field: fields) {
					query.fields().include(field);
				}
			}
			Map<String, Object> map = new HashMap<>();
			result = (List<Map<String, Object>>) mongoTemplate.find(query, map.getClass(), collection);
			log.debug("getMongoData with Fields method: After inserting in mongo database");
		} else {
			log.debug("getMongoData with Fields method: null Object was passed so no save was performed!!!!");
		}
		log.debug("getMongoData with Fields method: END");
		return result;
	}
	
	public Map<String, Object> getSyncGstnL2DataFromDb(String collectionName, String gstin, String section, String fp) {

        String fy = fp.substring(2);
        DB db = (DB) mongoTemplate.getDb();
        DBCollection collectiond = db.getCollection(collectionName);

        DBObject groupFields = new BasicDBObject();
        DBObject matchFields = new BasicDBObject();
        DBObject projectFields = new BasicDBObject();
        DBObject unwind1 = null;
        DBObject unwind2 = null;
        BasicDBList concat = new BasicDBList();

        matchFields.put("header.gstin", gstin);
        matchFields.put("header.type", section);
        matchFields.put("header.fp", fp);

        if (Gstr1ConstantsV31.TYPE_B2B.equals(section)) {
               concat.add("$header.gstin");
               concat.add(":");
               concat.add(fy);
               concat.add(":");
               concat.add("$gstn.inum");

               unwind1 = new BasicDBObject("$unwind", "$gstn");
               unwind2 = new BasicDBObject("$unwind", "$gstn.itms");

               groupFields.put("inum", new BasicDBObject("$first", "$gstn.inum"));
               groupFields.put("idt", new BasicDBObject("$first", "$gstn.idt"));
               groupFields.put("txval", new BasicDBObject("$sum", "$gstn.itms.itm_det.txval"));
               groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.iamt"));
               groupFields.put("camt", new BasicDBObject("$sum", "$gstn.itms.itm_det.camt"));
               groupFields.put("samt", new BasicDBObject("$sum", "$gstn.itms.itm_det.samt"));
               groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.csamt"));
               groupFields.put("ctin", new BasicDBObject("$first", "$gstn.ctin"));

               projectFields.put("ctin", "$ctin");
        } else {
               if (Gstr1ConstantsV31.TYPE_B2BA.equals(section)) {
                     concat.add("$header.gstin");
                     concat.add(":");
                     concat.add(fy);
                     concat.add(":");
                     concat.add("$gstn.inum");
                     concat.add(":");
                     concat.add("A");

                     unwind1 = new BasicDBObject("$unwind", "$gstn");
                     unwind2 = new BasicDBObject("$unwind", "$gstn.itms");

                     groupFields.put("inum", new BasicDBObject("$first", "$gstn.inum"));
                     groupFields.put("idt", new BasicDBObject("$first", "$gstn.idt"));
                     groupFields.put("txval", new BasicDBObject("$sum", "$gstn.itms.itm_det.txval"));
                     groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.iamt"));
                     groupFields.put("camt", new BasicDBObject("$sum", "$gstn.itms.itm_det.camt"));
                     groupFields.put("samt", new BasicDBObject("$sum", "$gstn.itms.itm_det.samt"));
                     groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.csamt"));
                     groupFields.put("ctin", new BasicDBObject("$first", "$gstn.ctin"));

                     projectFields.put("ctin", "$ctin");
               } else {
                     if (Gstr1ConstantsV31.TYPE_EXP.equals(section)) {
                            concat.add("$header.gstin");
                            concat.add(":");
                            concat.add(fy);
                            concat.add(":");
                            concat.add("$gstn.inum");

                            unwind1 = new BasicDBObject("$unwind", "$gstn");
                            unwind2 = new BasicDBObject("$unwind", "$gstn.itms");

                            groupFields.put("inum", new BasicDBObject("$first", "$gstn.inum"));
                            groupFields.put("idt", new BasicDBObject("$first", "$gstn.idt"));
                            groupFields.put("txval", new BasicDBObject("$sum", "$gstn.itms.txval"));
                            groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.itms.iamt"));
                            groupFields.put("camt", new BasicDBObject("$sum", "$gstn.itms.camt"));
                            groupFields.put("samt", new BasicDBObject("$sum", "$gstn.itms.samt"));
                            groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.itms.csamt"));
                     } else {
                            if (Gstr1ConstantsV31.TYPE_EXPA.equals(section)) {
                                   concat.add("$header.gstin");
                                   concat.add(":");
                                   concat.add(fy);
                                   concat.add(":");
                                   concat.add("$gstn.inum");
                                   concat.add(":");
                                   concat.add("A");

                                   unwind1 = new BasicDBObject("$unwind", "$gstn");
                                   unwind2 = new BasicDBObject("$unwind", "$gstn.itms");

                                   groupFields.put("inum", new BasicDBObject("$first", "$gstn.inum"));
                                   groupFields.put("idt", new BasicDBObject("$first", "$gstn.idt"));
                                   groupFields.put("txval", new BasicDBObject("$sum", "$gstn.itms.txval"));
                                   groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.itms.iamt"));
                                   groupFields.put("camt", new BasicDBObject("$sum", "$gstn.itms.camt"));
                                   groupFields.put("samt", new BasicDBObject("$sum", "$gstn.itms.samt"));
                                   groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.itms.csamt"));
                            } else {
                                   if (Gstr1ConstantsV31.TYPE_B2CL.equals(section)) {
                                          concat.add("$header.gstin");
                                          concat.add(":");
                                          concat.add(fy);
                                          concat.add(":");
                                          concat.add("$gstn.inum");

                                          unwind1 = new BasicDBObject("$unwind", "$gstn");
                                          unwind2 = new BasicDBObject("$unwind", "$gstn.itms");

                                          groupFields.put("inum", new BasicDBObject("$first", "$gstn.inum"));
                                          groupFields.put("idt", new BasicDBObject("$first", "$gstn.idt"));
                                          groupFields.put("txval", new BasicDBObject("$sum", "$gstn.itms.itm_det.txval"));
                                          groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.iamt"));
                                          groupFields.put("camt", new BasicDBObject("$sum", "$gstn.itms.itm_det.camt"));
                                          groupFields.put("samt", new BasicDBObject("$sum", "$gstn.itms.itm_det.samt"));
                                          groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.csamt"));
                                   } else {
                                          if (Gstr1ConstantsV31.TYPE_B2CLA.equals(section)) {
                                                 concat.add("$header.gstin");
                                                 concat.add(":");
                                                 concat.add(fy);
                                                 concat.add(":");
                                                 concat.add("$gstn.inum");
                                                 concat.add(":");
                                                 concat.add("A");

                                                 unwind1 = new BasicDBObject("$unwind", "$gstn");
                                                 unwind2 = new BasicDBObject("$unwind", "$gstn.itms");

                                                 groupFields.put("inum", new BasicDBObject("$first", "$gstn.inum"));
                                                 groupFields.put("idt", new BasicDBObject("$first", "$gstn.idt"));
                                                 groupFields.put("txval", new BasicDBObject("$sum", "$gstn.itms.itm_det.txval"));
                                                 groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.iamt"));
                                                 groupFields.put("camt", new BasicDBObject("$sum", "$gstn.itms.itm_det.camt"));
                                                 groupFields.put("samt", new BasicDBObject("$sum", "$gstn.itms.itm_det.samt"));
                                                 groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.csamt"));
                                          } else {
                                                 if (Gstr1ConstantsV31.TYPE_CDNR.equals(section)) {
                                                        concat.add("$header.gstin");
                                                        concat.add(":");
                                                        concat.add(fy);
                                                        concat.add(":");
                                                        concat.add("CDN");
                                                        concat.add(":");
                                                        concat.add("$gstn.nt.nt_num");

                                                        unwind1 = new BasicDBObject("$unwind", "$gstn.nt");
                                                        unwind2 = new BasicDBObject("$unwind", "$gstn.nt.itms");

                                                        groupFields.put("nt_num", new BasicDBObject("$first", "$gstn.nt.nt_num"));
                                                        groupFields.put("nt_dt", new BasicDBObject("$first", "$gstn.nt.nt_dt"));
                                                        groupFields.put("ctin", new BasicDBObject("$first", "$gstn.ctin"));
                                                        groupFields.put("txval", new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.txval"));
                                                        groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.iamt"));
                                                        groupFields.put("camt", new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.camt"));
                                                        groupFields.put("samt", new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.samt"));
                                                        groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.csamt"));

                                                        projectFields.put("ctin", "$ctin");
                                                        projectFields.put("nt_num", "$nt_num");
                                                        projectFields.put("nt_dt", "$nt_dt");

                                                 } else {
                                                        if (Gstr1ConstantsV31.TYPE_CDNRA.equals(section)) {
                                                               concat.add("$header.gstin");
                                                               concat.add(":");
                                                               concat.add(fy);
                                                               concat.add(":");
                                                               concat.add("CDN");
                                                               concat.add(":");
                                                               concat.add("$gstn.nt.nt_num");
                                                               concat.add(":");
                                                               concat.add("A");

                                                               unwind1 = new BasicDBObject("$unwind", "$gstn.nt");
                                                               unwind2 = new BasicDBObject("$unwind", "$gstn.nt.itms");

                                                               groupFields.put("nt_num", new BasicDBObject("$first", "$gstn.nt.nt_num"));
                                                               groupFields.put("nt_dt", new BasicDBObject("$first", "$gstn.nt.nt_dt"));
                                                               groupFields.put("ctin", new BasicDBObject("$first", "$gstn.ctin"));
                                                               groupFields.put("txval",
                                                                            new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.txval"));
                                                               groupFields.put("iamt",
                                                                            new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.iamt"));
                                                               groupFields.put("camt",
                                                                            new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.camt"));
                                                               groupFields.put("samt",
                                                                            new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.samt"));
                                                               groupFields.put("csamt",
                                                                            new BasicDBObject("$sum", "$gstn.nt.itms.itm_det.csamt"));

                                                               projectFields.put("ctin", "$ctin");
                                                               projectFields.put("nt_num", "$nt_num");
                                                               projectFields.put("nt_dt", "$nt_dt");
                                                        } else {
                                                               if (Gstr1ConstantsV31.TYPE_CDNUR.equals(section)) {
                                                                      concat.add("$header.gstin");
                                                                     concat.add(":");
                                                                      concat.add(fy);
                                                                     concat.add(":");
                                                                     concat.add("CDN");
                                                                     concat.add(":");
                                                                      concat.add("$gstn.nt_num");

                                                                     unwind1 = new BasicDBObject("$unwind", "$gstn.itms");

                                                                     groupFields.put("nt_num", new BasicDBObject("$first", "$gstn.nt_num"));
                                                                     groupFields.put("nt_dt", new BasicDBObject("$first", "$gstn.nt_dt"));
                                                                     groupFields.put("txval",
                                                                                   new BasicDBObject("$sum", "$gstn.itms.itm_det.txval"));
                                                                     groupFields.put("iamt",
                                                                                   new BasicDBObject("$sum", "$gstn.itms.itm_det.iamt"));
                                                                     groupFields.put("camt",
                                                                                   new BasicDBObject("$sum", "$gstn.itms.itm_det.camt"));
                                                                     groupFields.put("samt",
                                                                                   new BasicDBObject("$sum", "$gstn.itms.itm_det.samt"));
                                                                     groupFields.put("csamt",
                                                                                   new BasicDBObject("$sum", "$gstn.itms.itm_det.csamt"));

                                                                      projectFields.put("nt_num", "$nt_num");
                                                                      projectFields.put("nt_dt", "$nt_dt");
                                                               } else {
                                                                     if (Gstr1ConstantsV31.TYPE_CDNURA.equals(section)) {
                                                                            concat.add("$header.gstin");
                                                                            concat.add(":");
                                                                            concat.add(fy);
                                                                            concat.add(":");
                                                                            concat.add("CDN");
                                                                            concat.add(":");
                                                                            concat.add("$gstn.nt_num");
                                                                            concat.add(":");
                                                                            concat.add("A");

                                                                            unwind1 = new BasicDBObject("$unwind", "$gstn.itms");

                                                                            groupFields.put("nt_num", new BasicDBObject("$first", "$gstn.nt_num"));
                                                                            groupFields.put("nt_dt", new BasicDBObject("$first", "$gstn.nt_dt"));
                                                                            groupFields.put("txval",
                                                                                          new BasicDBObject("$sum", "$gstn.itms.itm_det.txval"));
                                                                            groupFields.put("iamt",
                                                                                          new BasicDBObject("$sum", "$gstn.itms.itm_det.iamt"));
                                                                            groupFields.put("camt",
                                                                                          new BasicDBObject("$sum", "$gstn.itms.itm_det.camt"));
                                                                            groupFields.put("samt",
                                                                                          new BasicDBObject("$sum", "$gstn.itms.itm_det.samt"));
                                                                            groupFields.put("csamt",
                                                                                          new BasicDBObject("$sum", "$gstn.itms.itm_det.csamt"));

                                                                            projectFields.put("nt_num", "$nt_num");
                                                                            projectFields.put("nt_dt", "$nt_dt");
                                                                     }
                                                               }
                                                        }
                                                 }
                                          }
                                   }
                            }
                     }
               }
        }

        groupFields.put("_id", "$_id");
        groupFields.put("_id_concat", new BasicDBObject("$first", new BasicDBObject("$concat", concat)));

        projectFields.put("_id", "$_id_concat");
        projectFields.put("inum", "$inum");
        projectFields.put("idt", "$idt");
        projectFields.put("txval", "$txval");
        projectFields.put("iamt", "$iamt");
        projectFields.put("camt", "$camt");
        projectFields.put("samt", "$samt");
        projectFields.put("csamt", "$csamt");

        DBObject group = new BasicDBObject("$group", groupFields);

        DBObject match = new BasicDBObject("$match", matchFields);
        DBObject project = new BasicDBObject("$project", projectFields);
        AggregationOutput output = null;
        if (Gstr1ConstantsV31.TYPE_CDNUR.equals(section) || Gstr1ConstantsV31.TYPE_CDNURA.equals(section)) {
               output = collectiond.aggregate(match, unwind1, group, project);
        } else {
               output = collectiond.aggregate(match, unwind1, unwind2, group, project);
        }

        Map<String, Object> invoiceListMap = new HashMap<>();
        for (DBObject res : output.results()) {
               Map<String, Object> invoiceData = new HashMap<>();
               String id = (String) res.get("_id");

               if (Gstr1ConstantsV31.TYPE_CDNR.equals(section) || Gstr1ConstantsV31.TYPE_CDNRA.equals(section)
                            || Gstr1ConstantsV31.TYPE_CDNUR.equals(section) || Gstr1ConstantsV31.TYPE_CDNURA.equals(section)) {
                     invoiceData.put("nt_num", res.get("nt_num"));
                     invoiceData.put("nt_dt", res.get("nt_dt"));
               } else {
                     invoiceData.put("inum", res.get("inum"));
                     invoiceData.put("idt", res.get("idt"));
               }
               invoiceData.put("txval", res.get("txval"));
               invoiceData.put("iamt", res.get("iamt"));
               invoiceData.put("samt", res.get("samt"));
               invoiceData.put("camt", res.get("camt"));
               invoiceData.put("csamt", res.get("csamt"));
               if (Gstr1ConstantsV31.TYPE_B2B.equals(section) || Gstr1ConstantsV31.TYPE_CDNR.equals(section)
                            || Gstr1ConstantsV31.TYPE_CDNRA.equals(section) || Gstr1ConstantsV31.TYPE_B2BA.equals(section)) {
                     invoiceData.put("ctin", res.get("ctin"));
               }
               invoiceListMap.put(id, invoiceData);
        }

        return invoiceListMap;
 }

	
	
	@Override
    public Map<String, Object> getSyncGstnL0DataFromDb(String collectionName, String gstin, String fp) {
           
           Map<String,Object> gstnL0Map = new HashMap<>();
           DB db = (DB) mongoTemplate.getDb();
        DBCollection collectiond = db.getCollection(collectionName);
        
        DBObject groupFields = new BasicDBObject();
        DBObject matchFields = new BasicDBObject();
           
        matchFields.put("header.gstin", gstin);
        matchFields.put("control.type", Gstr1ConstantsV31.SYNC_SECTION_AGGREGATE);
        matchFields.put("header.fp", fp);
        
        groupFields.put("_id", "$_id");
        groupFields.put("data", new BasicDBObject("$first","$gstn.data"));
        
        DBObject group = new BasicDBObject("$group", groupFields);
        DBObject match = new BasicDBObject("$match", matchFields);
        
        AggregationOutput output = collectiond.aggregate(match,group);
           
        for (DBObject res : output.results()) {
           gstnL0Map = (Map<String, Object>) res;
        }
        
           return gstnL0Map;
    }

	@Override
    public Map<String, Object> getSyncGstnInvoiceData(String collectionName, String gstin, String section, String fp, String inum) {

           DB db = (DB) mongoTemplate.getDb();
           DBCollection collectiond = db.getCollection(collectionName);

           DBObject groupFields = new BasicDBObject();
           DBObject matchFields = new BasicDBObject();
           DBObject unwind = null; 
           DBObject unwind1 = null;
           AggregationOutput output = null;
           DBObject group = null;
           DBObject match = null;

           matchFields.put("header.gstin", gstin);
           matchFields.put("header.type", section);
           matchFields.put("header.fp", fp);
           groupFields.put("_id", "$_id");

           if(Gstr1ConstantsV31.TYPE_EXP.equals(section) || 
        		   Gstr1ConstantsV31.TYPE_B2B.equals(section) ||
        		   Gstr1ConstantsV31.TYPE_B2BA.equals(section) || 
        		   Gstr1ConstantsV31.TYPE_B2CL.equals(section) || 
        		   Gstr1ConstantsV31.TYPE_EXPA.equals(section) || 
        		   Gstr1ConstantsV31.TYPE_B2CLA.equals(section))
           {
                  matchFields.put("gstn.inum", inum);

                  groupFields.put("gstn", new BasicDBObject("$first","$gstn"));

                  unwind = new BasicDBObject("$unwind","$gstn");

                  group = new BasicDBObject("$group", groupFields);
                  match = new BasicDBObject("$match", matchFields);

                  output = collectiond.aggregate(match,unwind,group);
           }
           else
           {
                  if(Gstr1ConstantsV31.TYPE_CDNR.equals(section) || Gstr1ConstantsV31.TYPE_CDNRA.equals(section))
                  {
                        matchFields.put("gstn.nt.nt_num", inum);

                        groupFields.put("gstn", new BasicDBObject("$first","$gstn.nt"));
                        groupFields.put("ctin",new BasicDBObject("$first","$gstn.ctin"));

                        unwind = new BasicDBObject("$unwind","$gstn");
                        unwind1 = new BasicDBObject("$unwind","$gstn.nt");


                        group = new BasicDBObject("$group", groupFields);
                        match = new BasicDBObject("$match", matchFields);

                        output = collectiond.aggregate(match,unwind,unwind1,group);
                  }
                  else
                  {
                        if(Gstr1ConstantsV31.TYPE_CDNUR.equals(section) || Gstr1ConstantsV31.TYPE_CDNURA.equals(section))
                        {
                               matchFields.put("gstn.nt_num", inum);

                               groupFields.put("gstn", new BasicDBObject("$first","$gstn"));

                               group = new BasicDBObject("$group", groupFields);
                               match = new BasicDBObject("$match", matchFields);

                               output = collectiond.aggregate(match,group);
                        }
                  }
           }



           Map<String,Object> dataMap = new HashMap<>();
           for (DBObject res : output.results()) {
                  if(Gstr1ConstantsV31.TYPE_CDNR.equals(section))
                  {
                        dataMap.put("ctin", res.get("ctin"));
                  }
                  dataMap.put("gstn", res.get("gstn"));
           }

           return dataMap;
    }

	
	@Override
	public void saveBatchDataInMongo(List object, String collection) {
		log.debug("saveBatchDataInMongo method: START");
		if (!object.isEmpty()) {
			DBCollection table=(DBCollection) mongoTemplate.getDb().getCollection(collection);
			BulkWriteOperation bulkWriteOperation = table.initializeOrderedBulkOperation();
			  for (Object object2 : object)
			  {
				Map<String, Object> map=(Map<String, Object>)object2;
				DBObject obj=new BasicDBObject(map);
			    bulkWriteOperation.find(BasicDBObjectBuilder.start("_id", map.get("_id")).get())//
			        .upsert()//
			        .replaceOne(obj);
			  }

			  bulkWriteOperation.execute();
			
			
			//mongoTemplate.insert(object, collection);
			log.debug("saveBatchDataInMongo method: After inserting in mongo database");
		} else {
			log.debug("saveBatchDataInMongo method: null Object was passed so no save was performed!!!!");
		}
		log.debug("saveBatchDataInMongo method: END");
	}
	
    @Override
    public long getCount(String mongoL0Key, String collectionName) {
           
           Query query = new Query();
           query.addCriteria(Criteria.where(Gstr1ConstantsV31.CONTROL_REC_ID).is(mongoL0Key));
           long ttlCount = mongoTemplate.count(query, Object.class, collectionName);
           
           return ttlCount;
    }
	
    @Override
	public List<Map<String, Object>> getMongoData(List<Map<String, Object>> whrObj, String[] fields, String collection, Map<String, Object> whrObj1) {
		List<Map<String, Object>> result = null;
		if(whrObj != null && whrObj1 != null){
		log.debug("getMongoData with Fields method: START");
		Query query = new Query();
		Criteria crt = new Criteria();
		List<Criteria> criteriaList=new ArrayList<>();
		for (Map<String, Object> map : whrObj) {
			Iterator<String> keyItr=map.keySet().iterator();
			Criteria crt1 = new Criteria();
			while (keyItr.hasNext()) {
				String key = keyItr.next();
				crt1= crt1.and(key).is(map.get(key));
			}
			criteriaList.add(crt1);
 
			 
		}
		
		crt.orOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
		query.addCriteria(crt);
		
		//System.out.println("query "+query);
		Iterator<String> itr = whrObj1.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				Object obj = MapUtils.getObject(whrObj1, key, "");
				if(obj instanceof Set<?>){
					Set<String> l=((Set<String>)obj);
					query.addCriteria(Criteria.where(key).in(l));
				}else if (obj instanceof Object[]){
					Object []l=((Object[])obj);
					query.addCriteria(Criteria.where(key).in(l));
				}else if (obj instanceof List<?> ){
					List<String> l=((List<String>)obj);
					query.addCriteria(Criteria.where(key).in(l));
				}else{
					query.addCriteria(Criteria.where(key).is(obj));					
				}
			}
			if(fields!=null && fields.length>0){
				for (String field: fields) {
					query.fields().include(field);
				}
			}
			Map<String, Object> map = new HashMap<>();
			result = (List<Map<String, Object>>) mongoTemplate.find(query, map.getClass(), collection);
			log.debug("getMongoData with Fields method: After inserting in mongo database");
		} else {
			log.debug("getMongoData with Fields method: null Object was passed so no save was performed!!!!");
		}
		log.debug("getMongoData with Fields method: END");
		return result;
	}


}