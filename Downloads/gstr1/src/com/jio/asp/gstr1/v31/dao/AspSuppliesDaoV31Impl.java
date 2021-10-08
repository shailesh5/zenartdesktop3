/**
 * 
 */
package com.jio.asp.gstr1.v31.dao;


import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.lookup;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.service.AspSuppliesSumServiceV31Impl;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference; 

/**
 * @author Amit1.Dwivedi
 *
 */
@Repository
public class AspSuppliesDaoV31Impl implements AspSuppliesDaoV31 {

	private static final Logger log = LoggerFactory.getLogger(AspSuppliesDaoV31Impl.class);

	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private MessageSource gstnResource;
		
	@Autowired
	private Environment environment;

	@Override
	public Map<String, Object> retrieveSummaryData(Map<String, String> inputMap) {
		log.debug("retrieveSummaryData L2 method: START");
		log.debug("retrieveSummaryData L2 method: START :: ackno{} :",AspSuppliesSumServiceV31Impl.sysCurTime);
		String gstr1Col = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
		List<Object> l2 = null;
		Map<String, Object> respMap = null;
		try {

			ReadPreference rp = null;
			
			String fp = (String) inputMap.get(Gstr1ConstantsV31.INPUT_FP);
			String gstin = (String) inputMap.get(Gstr1ConstantsV31.INPUT_GSTN);
			String section = (String) inputMap.get(Gstr1ConstantsV31.INPUT_SECTION);
			String status = (String) inputMap.get(Gstr1ConstantsV31.INPUT_STATUS);
			if (status == null || status.isEmpty()) {
				status = "";
			}

			respMap = new HashMap<>();
			/*
			 * String sort = (String)
			 * inputMap.get(Gstr1ConstantsV31.INPUT_SORT_BY); int order; try {
			 * order = Integer.parseInt((String)
			 * inputMap.get(Gstr1ConstantsV31.INPUT_SORT_ORDER)); } catch
			 * (NumberFormatException e1) { order = 1; } if (sort == null ||
			 * sort.isEmpty()) { sort = "gstn"; }
			 */

			int limit = 0;
			int skip = 0;
			try {
				limit = Integer.parseInt((String) inputMap.get(Gstr1ConstantsV31.INPUT_LIMIT));
				skip = Integer.parseInt((String) inputMap.get(Gstr1ConstantsV31.INPUT_OFFSET));
				int max_records = Integer.parseInt(environment.getProperty("max_records_fetched"));
				if ((limit - skip) > max_records) {
					log.info("retrieveSummaryData L2 Method:Setting the max limit of L2 query reqlimit:{},maxLimit:{} ackno{}",
							limit, max_records, AspSuppliesSumServiceV31Impl.sysCurTime);
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
			// log.info(
			// "retrieveSummaryData L2 Method:Query for Criteria fp:{},
			// gstin:{}, section:{},limit:{},offset:{} ",
			// fp, section, limit, skip);
			// log.debug(
			// "retrieveSummaryData L2 Method:Query for Criteria fp:{},
			// gstin:{}, section:{},limit:{},offset:{} ",
			// fp, section, limit, skip);

			Query query = new Query();
			query.addCriteria(Criteria.where(Gstr1ConstantsV31.GSTIN_HEADER).is(gstin).and(Gstr1ConstantsV31.FP_HEADER)
					.is(fp).and(Gstr1ConstantsV31.CONTROL_TYPE).is(section.toLowerCase()).and(Gstr1ConstantsV31.CONTROL_VERSION).is("v3.1"));

			
			/**
			 * Add filters in criteria query if present in request param
			 */
			//if(!filter.isEmpty()){	
			query = addFilterCriteria(query,inputMap);
			//}
			
			if (Gstr1ConstantsV31.INVOICE_STATE_L2_PARK.equals(status)) {
				query.addCriteria(
						Criteria.where(Gstr1ConstantsV31.CONTROL_STATUS).is(Gstr1ConstantsV31.INVOICE_STATE_PARK));
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
				query.addCriteria(Criteria.where(Gstr1ConstantsV31.FP_HEADER).in(fpList));

			} else {

				query.addCriteria(Criteria.where(Gstr1ConstantsV31.FP_HEADER).is(fp));
				query.addCriteria(
						Criteria.where(Gstr1ConstantsV31.CONTROL_STATUS).ne(Gstr1ConstantsV31.INVOICE_STATE_DEL));
			}
			
			

			log.debug("retrieveSummaryData L2 Method:FIRST QUERY START");
			long ttlCount = mongoTemplate.count(query, Object.class, gstr1Col);
			log.debug("retrieveSummaryData L2 Method:FIRST QUERY END");
			query.skip(skip);
			query.limit(limit);
			log.debug("retrieveSummaryData L2 Method:SECOND QUERY START");
			List<Map<String, Object>> list2 = (List) mongoTemplate.find(query, Object.class, gstr1Col);
			log.debug("retrieveSummaryData L2 Method:SECOND QUERY END");
			log.debug("retrieveSummaryData L2 Method:After getting result from database");
			Map<String, Object> headerObj = null;
			l2 = new ArrayList<>();
			log.debug("retrieveSummaryData L2 Method:READING DATA FROM LIST :: START");
			for (int i = 0; i < list2.size(); i++) {
				Map<String, Object> obj = list2.get(i);
				headerObj = (Map<String, Object>) obj.get(Gstr1ConstantsV31.CONTROL_JSON_HDR_SEC);
				Object gstnObj = obj.get(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC);
				Map<String, Object> controlObj = (Map<String, Object>) obj.get(Gstr1ConstantsV31.CONTROL_JSON_SEC);
				String dbStatus = (String) controlObj.get(Gstr1ConstantsV31.CONTROL_JSON_STATE);
				String gstnStatus = (String) controlObj.get(Gstr1ConstantsV31.CONTROL_JSON_GST_STATUS);
				if(gstnStatus==null || gstnStatus.isEmpty()){
					gstnStatus="";
				}
				String syncStatus = (String) controlObj.get(Gstr1ConstantsV31.CONTROL_JSON_SYNC_STATUS);
				if(syncStatus==null || syncStatus.isEmpty()){
					syncStatus="";
				}

				// obj.removeField(Gstr1ConstantsV31.CONTROL_REC_ID);
				if (gstnObj instanceof Map<?, ?>) {
					Map<String, Object> gstnObj1 = (Map<String, Object>) gstnObj;
					gstnObj1.put(Gstr1ConstantsV31.CONTROL_JSON_STATE, dbStatus);
					gstnObj1.put(Gstr1ConstantsV31.CONTROL_JSON_GST_STATUS, gstnStatus);
					gstnObj1.put(Gstr1ConstantsV31.CONTROL_JSON_SYNC_STATUS, syncStatus);
					l2.add(gstnObj1);

				} else if (gstnObj instanceof List) {

					Map<String, Object> itemMap = new HashMap<>();
					List list1 = (List) gstnObj;
					Map<Object, Object> dbObject = null;

					List<Object> innerList = new ArrayList<>();
					for (int i1 = 0; i1 < list1.size(); i1++) {
						dbObject = (Map<Object, Object>) list1.get(i1);
						dbObject.put(Gstr1ConstantsV31.CONTROL_JSON_STATE, dbStatus);
						dbObject.put(Gstr1ConstantsV31.CONTROL_JSON_GST_STATUS, gstnStatus);
						dbObject.put(Gstr1ConstantsV31.CONTROL_JSON_SYNC_STATUS, syncStatus);
						innerList.add(dbObject);

					}

					if (!(inputMap.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
							&& (inputMap.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
						itemMap.put("id", (String) dbObject.get("stid"));
						itemMap.put("rates", innerList);
					} else
						itemMap.put((String) dbObject.get("stid"), innerList);
					l2.add(itemMap);
				}

			}
			
			log.debug("retrieveSummaryData L2 Method:READING DATA FROM LIST :: END");

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
			log.info("retrieveSummaryData L2 Method:After forming the response:: ackno{} :",AspSuppliesSumServiceV31Impl.sysCurTime);
		} finally {
		}
		log.debug("retrieveSummaryData L2 method: END");
		//respMap.put(Gstr1ConstantsV31.CONTROL_JSON_UTIME, utime);
		return respMap;
	}
	
	
	@Override
    public Map<String,Object> retrieveComparisonData(Map<String, String> inputMap) {
           
           String gstin = MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_GSTN, null);
           String fp = MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_FP, null);
           String sectionType = MapUtils.getString(inputMap, Gstr1ConstantsV31.INPUT_SECTION, null);

           String collection = gstnResource.getMessage(Gstr1ConstantsV31.SUPPLIES_COLLECTION_NAME, null, LocaleContextHolder.getLocale());
           DB db = (DB) mongoTemplate.getDb();
           DBCollection collectiond = db.getCollection(collection);

           DBObject groupFields = new BasicDBObject();
           DBObject matchFields = new BasicDBObject();
           DBObject projectFields = new BasicDBObject();

           matchFields.put(Gstr1ConstantsV31.HEADER_GSTIN_DB, gstin);
           matchFields.put(Gstr1ConstantsV31.CONTROL_TYPE, sectionType);
           matchFields.put(Gstr1ConstantsV31.HEADER_FP_DB, fp);
           matchFields.put(Gstr1ConstantsV31.CONTROL_STATUS, new BasicDBObject("$ne", Gstr1ConstantsV31.INVOICE_STATE_DEL));
           
           groupFields.put(Gstr1ConstantsV31.CONTROL_JSON_GSTN_STATE, new BasicDBObject("$first","$control.gst_status"));
           projectFields.put(Gstr1ConstantsV31.CONTROL_JSON_GSTN_STATE, "$gst_status");
           
           if(Gstr1ConstantsV31.TYPE_B2B.equals(sectionType))
           {
        	   groupFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, new BasicDBObject("$first","$gstn.ctin"));
        	   groupFields.put(Gstr1ConstantsV31.INPUT_INUM, new BasicDBObject("$first","$gstn.inum"));
        	   groupFields.put(Gstr1ConstantsV31.INPUT_IDT, new BasicDBObject("$first","$gstn.idt"));
        	   projectFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, "$ctin");
        	   projectFields.put(Gstr1ConstantsV31.INPUT_INUM, "$inum");
        	   projectFields.put(Gstr1ConstantsV31.INPUT_IDT, "$idt");
           }
           
           if(Gstr1ConstantsV31.TYPE_B2BA.equals(sectionType))
           {
        	   groupFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, new BasicDBObject("$first","$gstn.ctin"));
               groupFields.put(Gstr1ConstantsV31.INPUT_INUM, new BasicDBObject("$first","$gstn.inum"));
               groupFields.put(Gstr1ConstantsV31.INPUT_IDT, new BasicDBObject("$first","$gstn.idt"));
               projectFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, "$ctin");
               projectFields.put(Gstr1ConstantsV31.INPUT_INUM, "$inum");
               projectFields.put(Gstr1ConstantsV31.INPUT_IDT, "$idt");
           }
           
           if(Gstr1ConstantsV31.TYPE_CDNR.equals(sectionType))
           {
        	   groupFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, new BasicDBObject("$first","$gstn.ctin"));
               groupFields.put(Gstr1ConstantsV31.INPUT_NT_NUM, new BasicDBObject("$first","$gstn.nt_num"));
               groupFields.put(Gstr1ConstantsV31.INPUT_NT_DT, new BasicDBObject("$first","$gstn.nt_dt"));
               projectFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, "$ctin");
               projectFields.put(Gstr1ConstantsV31.INPUT_NT_NUM, "$nt_num");
               projectFields.put(Gstr1ConstantsV31.INPUT_NT_DT, "$nt_dt");
           }
           
           if(Gstr1ConstantsV31.TYPE_CDNRA.equals(sectionType))
           {
        	      groupFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, new BasicDBObject("$first","$gstn.ctin"));
                  groupFields.put(Gstr1ConstantsV31.INPUT_NT_NUM, new BasicDBObject("$first","$gstn.nt_num"));
                  groupFields.put(Gstr1ConstantsV31.INPUT_NT_DT, new BasicDBObject("$first","$gstn.nt_dt"));
                  projectFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, "$ctin");
                  projectFields.put(Gstr1ConstantsV31.INPUT_NT_NUM, "$nt_num");
                  projectFields.put(Gstr1ConstantsV31.INPUT_NT_DT, "$nt_dt");
           }
           
           if(Gstr1ConstantsV31.TYPE_CDNUR.equals(sectionType))
           {
                  //groupFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, new BasicDBObject("$first","$gstn.ctin"));
                  groupFields.put(Gstr1ConstantsV31.INPUT_NT_NUM, new BasicDBObject("$first","$gstn.nt_num"));
                  groupFields.put(Gstr1ConstantsV31.INPUT_NT_DT, new BasicDBObject("$first","$gstn.nt_dt"));
                  projectFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, "$ctin");
                  projectFields.put(Gstr1ConstantsV31.INPUT_NT_NUM, "$nt_num");
                  projectFields.put(Gstr1ConstantsV31.INPUT_NT_DT, "$nt_dt");
           }
           
           if( Gstr1ConstantsV31.TYPE_CDNURA.equals(sectionType))
           {
                  //groupFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, new BasicDBObject("$first","$gstn.ctin"));
                  groupFields.put(Gstr1ConstantsV31.INPUT_NT_NUM, new BasicDBObject("$first","$gstn.nt_num"));
                  groupFields.put(Gstr1ConstantsV31.INPUT_NT_DT, new BasicDBObject("$first","$gstn.nt_dt"));
                  projectFields.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, "$ctin");
                  projectFields.put(Gstr1ConstantsV31.INPUT_NT_NUM, "$nt_num");
                  projectFields.put(Gstr1ConstantsV31.INPUT_NT_DT, "$nt_dt");
           }
           
           
           if(Gstr1ConstantsV31.TYPE_EXP.equals(sectionType))
           {
        	   groupFields.put(Gstr1ConstantsV31.INPUT_INUM, new BasicDBObject("$first","$gstn.inum"));
        	   groupFields.put(Gstr1ConstantsV31.INPUT_IDT, new BasicDBObject("$first","$gstn.idt"));
        	   projectFields.put(Gstr1ConstantsV31.INPUT_INUM, "$inum");
        	   projectFields.put(Gstr1ConstantsV31.INPUT_IDT, "$idt");

        	   groupFields.put("txval", new BasicDBObject("$sum", "$gstn.itms.txval"));
        	   groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.itms.iamt"));
        	   groupFields.put("camt", new BasicDBObject("$sum", "$gstn.itms.camt"));
        	   groupFields.put("samt", new BasicDBObject("$sum", "$gstn.itms.samt"));
        	   groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.itms.csamt"));
           }       
           
           if(Gstr1ConstantsV31.TYPE_EXPA.equalsIgnoreCase(sectionType))
           {
        	   groupFields.put(Gstr1ConstantsV31.INPUT_INUM, new BasicDBObject("$first","$gstn.inum"));
        	   groupFields.put(Gstr1ConstantsV31.INPUT_IDT, new BasicDBObject("$first","$gstn.idt"));
        	   projectFields.put(Gstr1ConstantsV31.INPUT_INUM, "$inum");
        	   projectFields.put(Gstr1ConstantsV31.INPUT_IDT, "$idt");

        	   groupFields.put("txval", new BasicDBObject("$sum", "$gstn.itms.txval"));
        	   groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.itms.iamt"));
        	   groupFields.put("camt", new BasicDBObject("$sum", "$gstn.itms.camt"));
        	   groupFields.put("samt", new BasicDBObject("$sum", "$gstn.itms.samt"));
        	   groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.itms.csamt"));
           }
    
           if( Gstr1ConstantsV31.TYPE_B2CL.equals(sectionType))
           {
                  groupFields.put(Gstr1ConstantsV31.INPUT_INUM, new BasicDBObject("$first","$gstn.inum"));
                  groupFields.put(Gstr1ConstantsV31.INPUT_IDT, new BasicDBObject("$first","$gstn.idt"));
                  projectFields.put(Gstr1ConstantsV31.INPUT_INUM, "$inum");
                  projectFields.put(Gstr1ConstantsV31.INPUT_IDT, "$idt");
           }
           
           if( Gstr1ConstantsV31.TYPE_B2CLA.equals(sectionType))
           {
                  groupFields.put(Gstr1ConstantsV31.INPUT_INUM, new BasicDBObject("$first","$gstn.inum"));
                  groupFields.put(Gstr1ConstantsV31.INPUT_IDT, new BasicDBObject("$first","$gstn.idt"));
                  projectFields.put(Gstr1ConstantsV31.INPUT_INUM, "$inum");
                  projectFields.put(Gstr1ConstantsV31.INPUT_IDT, "$idt");
           }       
           
           if( !(Gstr1ConstantsV31.TYPE_EXP.equalsIgnoreCase(sectionType) || Gstr1ConstantsV31.TYPE_EXPA.equalsIgnoreCase(sectionType)))
           {
        	   groupFields.put("txval", new BasicDBObject("$sum", "$gstn.itms.itm_det.txval"));
        	   groupFields.put("iamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.iamt"));
        	   groupFields.put("camt", new BasicDBObject("$sum", "$gstn.itms.itm_det.camt"));
        	   groupFields.put("samt", new BasicDBObject("$sum", "$gstn.itms.itm_det.samt"));
        	   groupFields.put("csamt", new BasicDBObject("$sum", "$gstn.itms.itm_det.csamt"));
           }
           
           groupFields.put(Gstr1ConstantsV31.CONTROL_JSON_ID, "$_id");

           projectFields.put(Gstr1ConstantsV31.CONTROL_JSON_ID, "$_id");
           projectFields.put("txval", "$txval");
           projectFields.put("iamt", "$iamt");
           projectFields.put("camt", "$camt");
           projectFields.put("samt", "$samt");
           projectFields.put("csamt", "$csamt");

           DBObject group = new BasicDBObject("$group", groupFields);
           DBObject unwind1 = new BasicDBObject("$unwind","$gstn.itms");
           DBObject match = new BasicDBObject("$match", matchFields);
           DBObject project = new BasicDBObject("$project", projectFields);

           AggregationOutput output = collectiond.aggregate(match,unwind1,group,project);

           Map<String,Object> invoiceMap = new HashMap<>();
           
           for (DBObject res : output.results()) {
                  Map<String,Object> invoiceData = new HashMap<>();
                  
                  invoiceData.put("txval", res.get("txval"));
                  invoiceData.put("iamt", res.get("iamt"));
                  invoiceData.put("samt", res.get("samt"));
                  invoiceData.put("camt", res.get("camt"));
                  invoiceData.put("csamt", res.get("csamt"));
                  invoiceData.put("gst_status", res.get("gst_status"));
                  
                  if(Gstr1ConstantsV31.TYPE_B2B.equals(sectionType) || Gstr1ConstantsV31.TYPE_B2BA.equals(sectionType)  || Gstr1ConstantsV31.TYPE_CDNR.equals(sectionType) || Gstr1ConstantsV31.TYPE_CDNRA.equals(sectionType))
                  {
                        invoiceData.put(Gstr1ConstantsV31.INPUT_CNPTY_GSTN, res.get(Gstr1ConstantsV31.INPUT_CNPTY_GSTN));
                  }
                  
                  if(Gstr1ConstantsV31.TYPE_CDNR.equals(sectionType) || Gstr1ConstantsV31.TYPE_CDNRA.equals(sectionType) || Gstr1ConstantsV31.TYPE_CDNUR.equals(sectionType) || Gstr1ConstantsV31.TYPE_CDNURA.equals(sectionType))
                  {
                        invoiceData.put(Gstr1ConstantsV31.INPUT_NT_DT, res.get(Gstr1ConstantsV31.INPUT_NT_DT));
                        invoiceData.put(Gstr1ConstantsV31.INPUT_NT_NUM, res.get(Gstr1ConstantsV31.INPUT_NT_NUM));
                        
                  }
                  if(Gstr1ConstantsV31.TYPE_EXP.equals(sectionType) || Gstr1ConstantsV31.TYPE_EXPA.equalsIgnoreCase(sectionType) || Gstr1ConstantsV31.TYPE_B2CL.equals(sectionType) || Gstr1ConstantsV31.TYPE_B2B.equals(sectionType) || Gstr1ConstantsV31.TYPE_B2CLA.equals(sectionType))
                  {
                        invoiceData.put(Gstr1ConstantsV31.INPUT_INUM, res.get(Gstr1ConstantsV31.INPUT_INUM));
                        invoiceData.put(Gstr1ConstantsV31.INPUT_IDT, res.get(Gstr1ConstantsV31.INPUT_IDT));
                  }
                  
                  invoiceMap.put((String) res.get(Gstr1ConstantsV31.CONTROL_JSON_ID),invoiceData);
           }
        
           log.debug("retrieveComparisonData method : END");
   		log.info("retrieveComparisonData method : END");
           return invoiceMap;
    }

	/**
	 * Method is responsible to add filters in criteria query only if present in request param.
	 * @param query
	 * @param inputMap
	 * @return
	 */
	Query addFilterCriteria(Query query,Map<String, String> inputMap){
        log.info("addFilterCriteria method: START");
        String filter = (String) inputMap.get(Gstr1ConstantsV31.INPUT_FILTER);
        if(filter != null){
               if(filter.contains(",")){
                     log.debug("Multiple filters found:  START spliting");
                     String filters [] = filter.split(","); 
                     log.debug("Multiple filters found:  END spliting : Total filters : " + filters.length);
                     addFilterToQuery(query,filters);
               }else{
                     addFilterToQuery(query,new String[]{filter});
               }
        }
        
        log.info("addFilterCriteria method: END");
        return query;
 }
 
	private void addFilterToQuery(Query query,String filters[]){
        Map<String,List<String>> filterMap = new HashMap<String,List<String>>();
        String filterKey = null;
        String filterValue = null;
        log.debug("Seprate out filters based on the filter type :  START");
        for(int i=0; i< filters.length;i++){
              String filter = filters[i];
              String filterPair[] = filter.split(":");
              filterKey = filterPair [0];
              filterValue = Gstr1ConstantsV31.FILTER_STATUS_MAP().get(filter);
              
              if(filterMap.containsKey(filterKey)){
                    filterMap.get(filterKey).add(filterValue);
              }else{
                    List<String> list = new ArrayList<String>();
                    list.add(filterValue);
                    filterMap.put(filterKey,list);
              }
        }
        log.debug("Seprate out filters based on the filter type :  END");
        
        String constantKey = null;
        
        log.debug("Add critria for filter :  START");
        for (Map.Entry<String,List<String>> keyValue : filterMap.entrySet()) {
              
              constantKey =  Gstr1ConstantsV31.FILTER_META_MAP().get(keyValue.getKey());
                                
              for(String filterVal : keyValue.getValue()){
                    if(!Gstr1ConstantsV31.TYPE_ALL.equalsIgnoreCase(filterVal)){
                          query.addCriteria(Criteria.where(constantKey)
                                      .in(keyValue.getValue()));
                    }
                    break;
              }
        }
        
        log.debug("Add critria for filter :  END");
  }
	int limit1 = 0;
	int skip1 = 0;

	@Override
	public Map<String, Object> retrieveReportSummaryData(Map<String, String> inputMap) {
		log.debug("retrieveReportSummaryData L2 method: START");

		String gstr1GstnCol = gstnResource.getMessage("gstr1.col", null, LocaleContextHolder.getLocale());
		String gstinDetailsCol = gstnResource.getMessage("gstinDetails.col", null, LocaleContextHolder.getLocale());
		
		Map<String, Object> respMap = null;
		try {

			ReadPreference rp = null;
			
			String fp = (String) inputMap.get(Gstr1ConstantsV31.INPUT_FP);
			String gstin = (String) inputMap.get(Gstr1ConstantsV31.INPUT_GSTN);
			String section = (String) inputMap.get(Gstr1ConstantsV31.INPUT_SECTION);
			Object type = getType(inputMap.get(Gstr1ConstantsV31.INPUT_TYPE));
			
			respMap = new HashMap<>();
			
			
			try {
				limit1 = Integer.parseInt((String) inputMap.get(Gstr1ConstantsV31.INPUT_LIMIT));
				skip1 = Integer.parseInt((String) inputMap.get(Gstr1ConstantsV31.INPUT_OFFSET));
				int max_records = Integer.parseInt(environment.getProperty("max_records_fetched"));
				if ((limit1 - skip1) > max_records) {
					log.info("retrieveReportSummaryData L2 Method:Setting the max limit of L2 query reqlimit:{},maxLimit:{} ackno{}",
							limit1, max_records, AspSuppliesSumServiceV31Impl.sysCurTime);
					log.debug(
							"retrieveReportSummaryData L2 Method:Setting the max limit of L2 query reqlimit:{},maxLimit:{} ",
							limit1, max_records);
					limit1 = max_records;
					respMap.put(Gstr1ConstantsV31.ASP_API_RESP_WARN_ATTR,
							"Only " + max_records + " records could be displayed at a time");
				}
			} catch (Exception e) {
				limit1 = 20;
				skip1 = 0;
			}
			
			

			AggregationExpression operater = new AggregationExpression() {
				
				@Override
				public DBObject toDbObject(AggregationOperationContext arg0) {
					return new BasicDBObject("$ifNull",Arrays.<Object> asList("$gstn.ctin","$gstn.etin"));
				}
			};
			
			AggregationExpression groupPush = new AggregationExpression() {
				
				@Override
				public DBObject toDbObject(AggregationOperationContext arg0) {
					DBObject condExpression = new BasicDBObject();
					condExpression.put("gstn", "$gstn");
					condExpression.put("status", "$status");
					condExpression.put("gst_status", "$gst_status");
					condExpression.put("sync_status", "$sync_status");
					return condExpression;
				}
			};
			
	
			Aggregation aggregation = Aggregation.newAggregation(
					match(new Criteria(Gstr1ConstantsV31.GSTIN_HEADER).is(gstin).
							and(Gstr1ConstantsV31.FP_HEADER).is(fp).
							and(Gstr1ConstantsV31.CONTROL_TYPE).is(section.toLowerCase())),
					project()
					 .andExpression("_id").as("_id")
					 .andExpression("header.gstin").as("gstin")
					 .andExpression("header.fp").as("fp")
					 .andExpression("control.status").as("status")
					 .andExpression("control.gst_status").as("gst_status")
					 .andExpression("control.sync_status").as("sync_status")
					 .andExpression("gstn").as("gstn")
					 .and(operater).as("tin"),
					 lookup(gstinDetailsCol, "tin", "_id", "docs"),
					 match(new Criteria("docs.sts").is(type)),
					 //project().andExclude("docs").andExclude("tin"),
                     group("gstin","fp")
					 .first("gstin").as("gstin")
					 .first("fp").as("fp")
					 .count().as("ttl_record")
					 .push(groupPush).as("record"),
					 limit(limit1),
					 skip(skip1)
					
					).withOptions(Aggregation.newAggregationOptions().explain(false).
	                        allowDiskUse(true).build());
			
			log.debug("retrieveReportSummaryData L2 Method:FIRST QUERY START");
						
			AggregationResults<Map> idsList = mongoTemplate.aggregate(aggregation, gstr1GstnCol, Map.class);
			log.debug("retrieveReportSummaryData L2 Method:FIRST QUERY END");
			log.debug("retrieveReportSummaryData L2 Method:After getting result from database");
			log.debug("retrieveSummaryData L2 Method:READING DATA FROM LIST :: START");
			
			if (idsList != null && idsList.getMappedResults().size() > 0) {
				for (Map map : idsList) {
					
					respMap.put(Gstr1ConstantsV31.INPUT_GSTN, map.get(Gstr1ConstantsV31.INPUT_GSTN));
					respMap.put(Gstr1ConstantsV31.INPUT_FP, map.get(Gstr1ConstantsV31.INPUT_FP));
					respMap.put(Gstr1ConstantsV31.JSON_TTL_RCRD, map.get(Gstr1ConstantsV31.JSON_TTL_RCRD));
					
					List<Map> listRecord = (List<Map>) map.get(Gstr1ConstantsV31.ASP_API_RESP_RCRD_ATTR);
					for(Map map1 : listRecord){
						Map<String, Object> gstnMap=MapUtils.getMap(map1, "gstn");
						gstnMap.put("status", MapUtils.getString(map1,"status",""));
						gstnMap.put("gst_status", MapUtils.getString(map1,"gst_status",""));
						gstnMap.put("sync_status", MapUtils.getString(map1,"sync_status",""));
						map1.remove("status");
						map1.remove("gst_status");
						map1.remove("sync_status");
						
						map1.putAll(gstnMap);
						map1.remove("gstn");
						
					}
					respMap.put(Gstr1ConstantsV31.ASP_API_RESP_RCRD_ATTR,map.get(Gstr1ConstantsV31.ASP_API_RESP_RCRD_ATTR));
				}
			}
						
			log.debug("retrieveSummaryData L2 Method:READING DATA FROM LIST :: END");

			log.info("retrieveSummaryData L2 Method:After forming the response:: ackno{} :",AspSuppliesSumServiceV31Impl.sysCurTime);
		} finally {
		}
				
		log.debug("retrieveReportSummaryData L2 method: After inserting in mongo database");
		log.debug("retrieveReportSummaryData L2 method: END");
		return respMap;
	}     
	
	private Object getType(String t){
		if("invalid".equalsIgnoreCase(t)){
			return new BasicDBObject("$in",Arrays.<Object> asList("Invalid", "Inactive"));
		}else if("valid".equalsIgnoreCase(t)){
			return new BasicDBObject("$in",Arrays.<Object> asList("Active", "Provisional", "Active pending Verification"));
		}else if("new".equalsIgnoreCase(t)){
			return new BasicDBObject("$in",Arrays.<Object> asList("NotFound"));
		}else{
			return null;
		}
	}
}
