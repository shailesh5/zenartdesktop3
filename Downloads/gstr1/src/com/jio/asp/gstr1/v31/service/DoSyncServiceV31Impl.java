package com.jio.asp.gstr1.v31.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.gstr1.v31.dao.AspMongoDaoV31;
import com.jio.asp.gstr1.v31.dao.AspSuppliesDaoV31;
import com.jio.asp.gstr1.v31.exception.GspExceptionV31;
import com.jio.asp.gstr1.v31.util.CommonUtilV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * @author Mayur.Waghchaure
 * @author Ninad.Bambardekar
 * @author Shariquea.Ansari
 */

@Component
public class DoSyncServiceV31Impl implements DoSyncServiceV31 {
	private static final Logger log = LoggerFactory.getLogger(DoSyncServiceV31Impl.class);

	@Autowired
	private MessageSource messageSourceV31;
	@Autowired
	private GstnSummaryServiceV31 gstnSummaryServiceV31;
	@Autowired
	GstnL2ServiceV31 gstnL2ServiceV31;
	@Autowired
	private AspSuppliesDaoV31 summaryDao;
	@Autowired
	private MessageSource gstnResource;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private AspMongoDaoV31 AspMongoDao; 
	@Autowired
	private Gstr1SummaryServiceV31 gstr1SummaryServiceV31; 
	

	public static Object uTime = null;
	
	public static long sysCurTime=System.currentTimeMillis();

    public static Map<String,String> gstStatusMap = new HashMap<String,String>();
	
	@Override
    public Map<String,Object> getDataFromGstn(Map<String, String> requestHeaders, Map<String, String> requestParams, String level, String flushStatus) {

		log.debug("getDataFromGstn method : START");
		log.info("getDataFromGstn method : START");
		
           Map<String,Object> responseMap = new HashMap<>();
           Map<String,String> requestParameterMap = new HashMap<>();
           Map<String,Object> gstnDataStatus = new HashMap<>();
           String gstin = requestParams.get(Gstr1ConstantsV31.JSON_GSTIN);
           String fp = requestParams.get(Gstr1ConstantsV31.INPUT_FP);
           String section = requestParams.get(Gstr1ConstantsV31.INPUT_SECTION);
           String collectionName = gstnResource.getMessage(Gstr1ConstantsV31.SYNC_DATA_COLLECTION_NAME, null, LocaleContextHolder.getLocale());

           if(Gstr1ConstantsV31.SUMMARY_TYPE_L2.equalsIgnoreCase(level))
           {
                  requestParameterMap.put(Gstr1ConstantsV31.INPUT_LEVEL, level);
                  requestParameterMap.put(Gstr1ConstantsV31.INPUT_SECTION,requestParams.get(Gstr1ConstantsV31.INPUT_SECTION));
                  requestParameterMap.put(Gstr1ConstantsV31.INPUT_FP,fp);
                  requestParameterMap.put(Gstr1ConstantsV31.JSON_GSTIN,gstin);
                  requestParameterMap.put(Gstr1ConstantsV31.INPUT_FLUSH, flushStatus);
                  requestParameterMap.put(Gstr1ConstantsV31.SYNC_FILTER,Gstr1ConstantsV31.SYNC_FILTER);
                  
                  try
			{
				gstnDataStatus = gstnL2ServiceV31.processL2(requestHeaders, requestParameterMap);
			} catch (GspExceptionV31 ex) {
				Map<String, Object> exObject;
				exObject = ex.getExcObj();
				if(!"No Invoices found for the provided Inputs".equals(exObject.get(Gstr1ConstantsV31.ERROR_DESC)))
				{
					throw ex;
				}
			}
                  if(gstnDataStatus.containsKey(Gstr1ConstantsV31.ERROR_GRP))
      			{
      				responseMap = gstnDataStatus;
      				return responseMap;
      			}
      			else
      			{
      				responseMap = AspMongoDao.getSyncGstnL2DataFromDb(collectionName, gstin, section, fp);
      			} 

           }
           if (Gstr1ConstantsV31.SUMMARY_TYPE_L0.equalsIgnoreCase(level))
           {
                  requestParameterMap.put(Gstr1ConstantsV31.INPUT_LEVEL,level);
                  requestParameterMap.put(Gstr1ConstantsV31.INPUT_FP,fp);
                  requestParameterMap.put(Gstr1ConstantsV31.JSON_GSTIN,gstin);

                  responseMap = getGstnL0(requestHeaders, requestParameterMap,flushStatus);
           }
           log.debug("getDataFromGstn method : END");
   		log.info("getDataFromGstn method : END");
           return responseMap;
    }      
    



	@Override
	public void validateParams(String gstin, String fp, String sectionType, String flush) {
		// TODO Auto-generated method stub
		CommonUtilV31.validateEmptyString(gstin, ErrorCodesV31.ASP504, Gstr1ConstantsV31.L0_SUMMARY, "ASP504", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		CommonUtilV31.validateEmptyString(fp, ErrorCodesV31.ASP504, Gstr1ConstantsV31.L0_SUMMARY, "ASP504", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		CommonUtilV31.validateEmptyString(sectionType, ErrorCodesV31.ASP504, Gstr1ConstantsV31.L0_SUMMARY, "ASP504", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		
		CommonUtilV31.validateEmptyString(flush, ErrorCodesV31.ASP504, Gstr1ConstantsV31.L0_SUMMARY, "ASP504", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
	}

	@Override
	public Map<String,Object> getL2DataFromJioGST(Map<String, String> headers, Map<String, String> data) {

		Map<String,Object> jioDataMap = null;
		log.debug("getL2DataFromJioGST method : START");
		log.info("getL2DataFromJioGST method : START ::   ackno{} :",sysCurTime);
		// asp call, making call to database to get the ASP data
		log.info("getL2DataFromJioGST Method - ASP Data retrival going to start - STEP4  ::   ackno :",sysCurTime);
		jioDataMap = getAspInvoiceDataL2(data);
		log.debug("getL2DataFromJioGST method : END");

		return jioDataMap;
	} 

	/**
	 * Method based on the user input for api call, will query database and
	 * retrieve the related information.
	 * 
	 * @param inputMap
	 *            user input map
	 * @return Map containing asp data per section.
	 */
	public Map<String,Object> getAspInvoiceDataL2(Map<String, String> inputMap) {
		
		log.debug("getAspInvoiceDataL2 method : START");
		log.info("getAspInvoiceDataL2 method : START :: ackno{}",sysCurTime);
		Map<String,Object> respMap = summaryDao.retrieveComparisonData(inputMap);
		log.info("getAspInvoiceDataL2 method : END :: ackno{}",sysCurTime);
		log.debug("getAspInvoiceDataL2 method : END");
		return respMap;
	}
	
	@Override
	public Map<String,Object> compareL0(Map<String,Object> jioGstMap, Map<String,Object> gstnMap,
			Map<String, String> requestParam,Map<String, String> header)
	{
		
		Map<String,Object> response = new HashMap<>();
		List<Map<String, Object>> sectionList=new ArrayList<>();
		Map<String,String> recoData = new HashMap<>();
		Iterator<String> gstnKey= gstnMap.keySet().iterator();
		
		while (gstnKey.hasNext()) {
			String section = gstnKey.next();
			Map<String,Object> sectionMap = new LinkedHashMap();
			Map<String,Object> emptyMap= new HashMap();
			if(!jioGstMap.containsKey(section)){
				recoData.put(section, Gstr1ConstantsV31.SYNC_STATUS_APPENDED);
				Map<String, Object> gstn=(Map<String, Object>)gstnMap.get(section);
				sectionMap.put("sec_nm", section);
				sectionMap.put("gstn", gstn);
				sectionMap.put("jiogst", emptyMap);
				sectionList.add(sectionMap);
			}else{
				Map<String, Object> jioGst=(Map<String, Object>)jioGstMap.get(section);
				Map<String, Object> gstn=(Map<String, Object>)gstnMap.get(section);
				sectionMap.put("sec_nm", section);
				sectionMap.put("gstn", gstn);
				sectionMap.put("jiogst", jioGst);
				sectionList.add(sectionMap);
				Iterator<String> secIter=gstn.keySet().iterator();
				boolean mismatched=false;
				boolean closed=true;
				while (secIter.hasNext()) {
					
					String attribute = secIter.next();
					Object jioGstVal=jioGst.get(attribute);
					Object gstnVal=gstn.get(attribute);
					if(jioGstVal instanceof Number && gstnVal instanceof Number && jioGstVal!=null && gstnVal!=null){
						BigDecimal jioGstAmt=new BigDecimal(((Number)jioGstVal).toString());
						BigDecimal gstnAmt=new BigDecimal(((Number)gstnVal).toString());
						if(jioGstAmt.compareTo(gstnAmt)!=0){
							recoData.put(section, Gstr1ConstantsV31.SYNC_STATUS_MISMATCHED);
							mismatched=true;
						}
						
						if(gstnAmt.compareTo(BigDecimal.ZERO)!=0 && closed){
							closed=false;
						}
					}
				}
				if(!mismatched && !closed){
					recoData.put(section, Gstr1ConstantsV31.SYNC_STATUS_MATCHED);
				}
				if(closed){
					recoData.put(section, Gstr1ConstantsV31.SYNC_STATUS_CLOSED);
				}
			}
		}
		if(CollectionUtils.isNotEmpty(sectionList)){
			response.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, sectionList);
			
			log.debug("************doSync method : Sync Status JioGST DB Updation Start");
	        log.info("************doSync method : Sync Status JioGST DB Updation Start");
			updateSuppliesDataForSync(header,recoData,requestParam.get(Gstr1ConstantsV31.JSON_GSTIN),
					requestParam.get(Gstr1ConstantsV31.INPUT_FP),requestParam.get(Gstr1ConstantsV31.INPUT_SECTION));
			log.debug("************doSync method : Sync Status JioGST DB Updation Start");
	        log.info("************doSync method : Sync Status JioGST DB Updation Start");
		}else{
			Map<String, String> noDataMap=new HashMap<>();
			noDataMap.put(Gstr1ConstantsV31.ASP_API_RESP_DATA_ATTR, "No data for the given criteria");
			noDataMap.put(Gstr1ConstantsV31.RESP_STATUS_CODE, "error");
		}
		response.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, requestParam);
		return response;
	}
	
	public Map<String,String> compareL2(Map<String,Object> jioGstMap, Map<String,Object> gstnMap)
    {
		log.debug("compareL2 method START");		
		   Map<String,String> response = new HashMap<>();
		   Map<String,Object> jioGstComparisonMap = new HashMap<>();
           Iterator<String> I = null;
           gstStatusMap.clear();
          
           //logic to seperate unwanted data from jioGstMap
           for(Entry<String, Object> entry : jioGstMap.entrySet())
           {
        	   String id = entry.getKey();
    		   Map invoiceData =  (Map) entry.getValue();
    		   gstStatusMap.put(id, (String) invoiceData.get("gst_status"));
    		   invoiceData.remove("gst_status");
    		   jioGstComparisonMap.put(id, invoiceData);
           }
           
           MapDifference<String,Object> diff = Maps.difference(jioGstComparisonMap, gstnMap);

           Set<String> onlyInjioGST = diff.entriesOnlyOnLeft().keySet();
           Set<String> onlyInGSTN = diff.entriesOnlyOnRight().keySet();
           Set<String> matchedInvoiceSet = diff.entriesInCommon().keySet();
           Set<String> misMatchedInvoiceSet = diff.entriesDiffering().keySet();

           //======== Matched Invoice Bucket ==========
           I = matchedInvoiceSet.iterator();
           while(I.hasNext())
           {
                  response.put(I.next(),Gstr1ConstantsV31.SYNC_STATUS_MATCHED);
           }

           //======== MisMatched Invoice Bucket==========
           I = misMatchedInvoiceSet.iterator();
           while(I.hasNext())
           {
                  response.put(I.next(),Gstr1ConstantsV31.SYNC_STATUS_MISMATCHED);
           }

           //======== Closed Invoice Bucket ==========
           I = onlyInjioGST.iterator();
           while(I.hasNext())
           {
                  response.put(I.next(),Gstr1ConstantsV31.SYNC_STATUS_CLOSED);
           }

           //======== Added Invoice Bucket==========
           I = onlyInGSTN.iterator();
           while(I.hasNext())
           {
                  response.put(I.next(),Gstr1ConstantsV31.SYNC_STATUS_APPENDED);
           }
           log.debug("compareL2 method END");
           return response;
    }

    
	@Override
    public void updateSuppliesDataForSync(Map<String, String> headers,Map<String,String> comparedData, String gstin, String fp, String section)
    {             
	log.debug("updateSuppliesDataForSync method START");	
	long t1 = System.currentTimeMillis();
    String suppliesCollection = gstnResource.getMessage(Gstr1ConstantsV31.SUPPLIES_COLLECTION_NAME, null, LocaleContextHolder.getLocale());
    DB db = (DB) mongoTemplate.getDb();
    DBCollection suppliesDbCollection = db.getCollection(suppliesCollection);
    BasicDBObject searchQuery = new BasicDBObject();
    BasicDBObject updateFields = new BasicDBObject();
    uTime = new Timestamp(System.currentTimeMillis());
    List<Map<String, Object>> appendedInvoiceList = new ArrayList<>();

    if(Gstr1ConstantsV31.SYNC_SECTION_AGGREGATE.equals(section))
    {
    	 for(Map.Entry<String, String> entry : comparedData.entrySet())
    	 {
    		   String sectionType = entry.getKey();
    		   String syncStatus =  entry.getValue();
   
    		   searchQuery.put(Gstr1ConstantsV31.HEADER_GSTIN_DB, gstin);
	           searchQuery.put(Gstr1ConstantsV31.CONTROL_TYPE, sectionType);
	           searchQuery.put(Gstr1ConstantsV31.HEADER_FP_DB, fp);
	           updateFields.put(Gstr1ConstantsV31.CONTROL_SYNC_STATUS, syncStatus);
	           updateFields.put(Gstr1ConstantsV31.CONTROL_GST_STATUS,Gstr1ConstantsV31.STATUS_SUBMIT_UPLOAD);
	           updateFields.put(Gstr1ConstantsV31.CONTROL_UTIME, uTime.toString()); 
	           suppliesDbCollection.updateMulti(searchQuery, new BasicDBObject("$set",updateFields));
    	   }
    }
    else
    {
           for (Map.Entry<String, String> entry : comparedData.entrySet()) {

                  String id = entry.getKey();
                  String syncStatus = entry.getValue();
                  updateFields.clear();
                  
                  searchQuery.put(Gstr1ConstantsV31.HEADER_GSTIN_DB, gstin);
                  searchQuery.put(Gstr1ConstantsV31.CONTROL_TYPE, section);
                  searchQuery.put(Gstr1ConstantsV31.HEADER_FP_DB, fp);
                  searchQuery.put(Gstr1ConstantsV31.CONTROL_JSON_ID, id);
                  updateFields.put(Gstr1ConstantsV31.CONTROL_SYNC_STATUS, syncStatus);
                  updateFields.put(Gstr1ConstantsV31.CONTROL_UTIME, uTime.toString()); 

                  if(!Gstr1ConstantsV31.STATUS_FILE_UPLOAD.equalsIgnoreCase(gstStatusMap.get(id)))
                  {
                	  updateFields.put(Gstr1ConstantsV31.CONTROL_GST_STATUS,Gstr1ConstantsV31.STATUS_SUBMIT_UPLOAD);
                  }
                  if(Gstr1ConstantsV31.SYNC_STATUS_APPENDED.equals(entry.getValue()))
                  {
                        Map<String, Object> jioGSTStructureInvoice = new HashMap<>();
                        
                        log.debug("updateSuppliesDataForSync: Converting GSTN Data to JioGST Structure START");
                        log.info("updateSuppliesDataForSync: Converting GSTN Data to JioGST Structure START");
                        jioGSTStructureInvoice = convertToJioGSTStructure(headers,id,gstin,section,fp,uTime.toString());
                        log.debug("updateSuppliesDataForSync: Converting GSTN Data to JioGST Structure END");
                        log.info("updateSuppliesDataForSync: Converting GSTN Data to JioGST Structure END");
                        
                        appendedInvoiceList.add(jioGSTStructureInvoice);
                  }
                  else
                  {
                        suppliesDbCollection.update(searchQuery, new BasicDBObject("$set",updateFields));
                  }
           }
           log.debug("updateSuppliesDataForSync: Saving Batch Data to JioGST DB START");
           log.info("updateSuppliesDataForSync: Saving Batch Data to JioGST DB START");
           AspMongoDao.saveBatchDataInMongo(appendedInvoiceList, suppliesCollection);
           log.debug("updateSuppliesDataForSync: Saving Batch Data to JioGST DB END");
           log.info("updateSuppliesDataForSync: Saving Batch Data to JioGST DB END");
    }
    long t2 = System.currentTimeMillis();
    log.info("SaveBatchInMongo TAT"+(t2-t1));

    }

	@Override
	public Map<String, Object> convertMapJioGstL0(Map<String, Object> jioGstMap) {
		log.debug("convertMapJioGstL0 method START");
		log.info("convertMapJioGstL0 method START");
		
		Map<String,Object> responseMap = new TreeMap<>();
		List<Map<String, Object>> dataList =  (List<Map<String, Object>>) jioGstMap.get(Gstr1ConstantsV31.INPUT_DATA);
		
		for(Map<String,Object> result : dataList)
		{
			Map<String,Object> sectionMap = new HashMap<>();
			String section = result.get("sec_nm").toString().toLowerCase();
			if(Gstr1ConstantsV31.SYNC_AGG_SECTION_MAP().containsKey(section))
			{
				if(Gstr1ConstantsV31.SYNC_AGG_SECTION_MAP().get(section).equalsIgnoreCase(section))
				{
					sectionMap.put("ttl_igst", result.get("ttl_igst"));
					sectionMap.put("ttl_cgst", result.get("ttl_cgst"));
					sectionMap.put("ttl_sgst", result.get("ttl_sgst"));
					sectionMap.put("ttl_cess", result.get("ttl_cess"));
					sectionMap.put("ttl_val", result.get("ttl_val"));
					sectionMap.put("ttl_txval", result.get("ttl_txval"));
					responseMap.put(section, sectionMap);	
				}
			}
			
			else if(Gstr1ConstantsV31.TYPE_NIL.equalsIgnoreCase(section))
			{
				sectionMap.put("ttl_nilsup_amt", result.get("nil_amt"));
				sectionMap.put("ttl_expt_amt", result.get("expt_amt"));
				sectionMap.put("ttl_ngsup_amt", result.get("ngsup_amt"));
				responseMap.put(section, sectionMap);	
			}
			else if(Gstr1ConstantsV31.TYPE_DOCS.equalsIgnoreCase(section))
			{
				sectionMap.put("ttl_doc_issued", result.get("totnum"));
				sectionMap.put("net_doc_issued", result.get("net_issue"));
				sectionMap.put("ttl_doc_cancelled", result.get("cancel"));
				responseMap.put(section, sectionMap);	
			}
		}
		log.debug("convertMapJioGstL0 method END");
		log.info("convertMapJioGstL0 method END");
		return responseMap;
	}


	@Override
	public Map<String, Object> convertMapGstnL0(Map<String, Object> gstnMap) {
		Map<String,Object> responseMap = new TreeMap<>();
		List<Map<String, Object>> dataList =  (List<Map<String, Object>>) gstnMap.get(Gstr1ConstantsV31.INPUT_DATA);
		
		for(Map<String,Object> result : dataList)
		{
			List<Map<String, Object>> sectionList =  (List<Map<String, Object>>) result.get("sec_sum");
			for(Map<String,Object> result1 : sectionList)
			{
				Map<String,Object> sectionMap = new HashMap<>();
				String section = result1.get("sec_nm").toString().toLowerCase();
				
				if(Gstr1ConstantsV31.SYNC_AGG_SECTION_MAP().containsKey(section))
				{
					if(Gstr1ConstantsV31.SYNC_AGG_SECTION_MAP().get(section).equalsIgnoreCase(section))
					{
						sectionMap.put("ttl_igst", result1.get("ttl_igst"));
						sectionMap.put("ttl_cgst", result1.get("ttl_cgst"));
						sectionMap.put("ttl_sgst", result1.get("ttl_sgst"));
						sectionMap.put("ttl_cess", result1.get("ttl_cess"));
						sectionMap.put("ttl_val", result1.get("ttl_val"));
						sectionMap.put("ttl_txval", result1.get("ttl_val"));
						responseMap.put(section, sectionMap);	
					}
				}
				else if(Gstr1ConstantsV31.TYPE_NIL.equalsIgnoreCase(section))
				{
					sectionMap.put("ttl_nilsup_amt", result1.get("ttl_nilsup_amt"));
					sectionMap.put("ttl_expt_amt", result1.get("ttl_expt_amt"));
					sectionMap.put("ttl_ngsup_amt",result1.get("ttl_ngsup_amt"));
					responseMap.put(section, sectionMap);	
				}
				else if(Gstr1ConstantsV31.TYPE_DOCS.equalsIgnoreCase(section))
				{
					sectionMap.put("ttl_doc_issued", result1.get("ttl_doc_issued"));
					sectionMap.put("net_doc_issued", result1.get("net_doc_issued"));
					sectionMap.put("ttl_doc_cancelled", result1.get("ttl_doc_cancelled"));
					responseMap.put(section, sectionMap);	
				}
			}
			
			}
		
		return responseMap;
	}
	
	
	private Map<String, Object> getGstnL0(Map<String, String> headers, Map<String, String> requestParameterMap, String flush) {

        Map<String,Object> response = new HashMap<>();
        Map<String,Object> dataCollectionMap = new HashMap<>();
        Map<String,Object> controlCollectionMap = new HashMap<>();
        Map<String,Object> controlHeaderMap = new HashMap<>();
        String gstin = requestParameterMap.get(Gstr1ConstantsV31.JSON_GSTIN);
        String fp = requestParameterMap.get(Gstr1ConstantsV31.INPUT_FP);

        String dataCollectionName = gstnResource.getMessage(Gstr1ConstantsV31.SYNC_DATA_COLLECTION_NAME, null, LocaleContextHolder.getLocale());
        String controlCollectionName = gstnResource.getMessage(Gstr1ConstantsV31.SYNC_CONTROL_COLLECTION_NAME, null, LocaleContextHolder.getLocale());

        String mongoL0Key = "GSTR1:"+gstin+":aggregate:"+fp;

        if("true".equals(flush))
        {
               long ttlCount = AspMongoDao.getCount(mongoL0Key, controlCollectionName); 

               if(ttlCount>0)
               {
                     AspMongoDao.deleteL2Key(mongoL0Key, dataCollectionName, controlCollectionName);
               }

               dataCollectionMap = formatGstnL0DataForMongo(mongoL0Key, gstin, fp, headers, requestParameterMap);

               controlHeaderMap.put("updateTime",new Timestamp(System.currentTimeMillis()));
               controlHeaderMap.put("type", Gstr1ConstantsV31.SYNC_SECTION_AGGREGATE);
               controlHeaderMap.put("status", "success");
               controlCollectionMap.put("_id", mongoL0Key);
               controlCollectionMap.put("header", controlHeaderMap);

               AspMongoDao.saveInMongo(new JSONObject(controlCollectionMap), controlCollectionName);
               AspMongoDao.saveInMongo(new JSONObject(dataCollectionMap), dataCollectionName);
               response = AspMongoDao.getSyncGstnL0DataFromDb(dataCollectionName, gstin, fp);

        }
        else
        {
               Query query = new Query();
               query.addCriteria(Criteria.where(Gstr1ConstantsV31.CONTROL_REC_ID).is(mongoL0Key));
               long ttlCount = AspMongoDao.getCount(mongoL0Key, controlCollectionName); 
               if(ttlCount>0)
               {
                     response = AspMongoDao.getSyncGstnL0DataFromDb(dataCollectionName, gstin, fp);
               }
               else
               {
                     dataCollectionMap = formatGstnL0DataForMongo(mongoL0Key, gstin, fp, headers, requestParameterMap);

                     controlHeaderMap.put("updateTime",new Timestamp(System.currentTimeMillis()));
                     controlHeaderMap.put("type", Gstr1ConstantsV31.SYNC_SECTION_AGGREGATE);
                     controlHeaderMap.put("status", "success");
                     controlCollectionMap.put("_id", mongoL0Key);
                     controlCollectionMap.put("header", controlHeaderMap);

                     AspMongoDao.saveInMongo(new JSONObject(controlCollectionMap), controlCollectionName);
                     AspMongoDao.saveInMongo(new JSONObject(dataCollectionMap), dataCollectionName);
                     response = AspMongoDao.getSyncGstnL0DataFromDb(dataCollectionName, gstin, fp);
               }
        }

        return response;
 }


	private Map<String,Object> convertToJioGSTStructure(Map<String,String> headers,String id,String gstin,String section,String fp,String uTime)
    {
           Map<String,Object> responseMap = new HashMap<String,Object>();
           Map<String,Object> controlMap = new HashMap<String,Object>();
           Map<String,Object> headerMap = new HashMap<String,Object>();
           Map<String,Object> gstnMap = new HashMap<String,Object>();
           Map<String,Object> dataMap = new HashMap<String,Object>();
           Map<String,Object> customMap = new HashMap<String,Object>();
           String gstnCollection = gstnResource.getMessage(Gstr1ConstantsV31.SYNC_DATA_COLLECTION_NAME, null, LocaleContextHolder.getLocale());

           headerMap.put(Gstr1ConstantsV31.INPUT_FP, fp);
           headerMap.put(Gstr1ConstantsV31.HEADER_GSTIN, gstin);

           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_VERSION, Gstr1ConstantsV31.API_VERSION);
           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_CLIENT_ID,headers.get(Gstr1ConstantsV31.HEADER_CLIENT_ID));
           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_LEVEL, identifyLevel(section));
           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_UTIME,uTime);
           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_TRANSID,headers.get(Gstr1ConstantsV31.HEADER_TXN));
           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_GSTN_STATE, Gstr1ConstantsV31.STATUS_SUBMIT_UPLOAD);
           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_ITIME,uTime);
           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID,id);
           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_TYPE,section);
           controlMap.put(Gstr1ConstantsV31.CONTROL_JSON_STATE,Gstr1ConstantsV31.INVOICE_STATE_NEW);
           controlMap.put("sync_status",Gstr1ConstantsV31.SYNC_STATUS_APPENDED);

           customMap.put("bloc", "");
           customMap.put("bid", "");
           customMap.put("bgrp", "");
           customMap.put("ds", "");

           String inum;
           if(Gstr1ConstantsV31.TYPE_CDNR.equals(section) || Gstr1ConstantsV31.TYPE_B2B.equals(section) || Gstr1ConstantsV31.TYPE_EXP.equals(section) || Gstr1ConstantsV31.TYPE_B2CL.equals(section) || Gstr1ConstantsV31.TYPE_CDNUR.equals(section))
           {
               inum = id.substring(id.lastIndexOf(":")+1,id.length());      	   
           }
           else
           {
        	   inum = id.substring(id.substring(0, id.lastIndexOf(":")).lastIndexOf(":") + 1,id.lastIndexOf(":"));
           }
  
           dataMap = AspMongoDao.getSyncGstnInvoiceData(gstnCollection, gstin, section, fp, inum);

           gstnMap = (Map<String, Object>) dataMap.get("gstn");
           if(Gstr1ConstantsV31.TYPE_CDNR.equals(section) || Gstr1ConstantsV31.TYPE_CDNRA.equalsIgnoreCase(section))
           {
                  String ctin = (String) dataMap.get("ctin");
                  gstnMap.put("ctin", ctin);
           }

           responseMap.put(Gstr1ConstantsV31.CONTROL_JSON_ID,id);
           responseMap.put(Gstr1ConstantsV31.CONTROL_JSON_GSTN_SEC,gstnMap);
           responseMap.put(Gstr1ConstantsV31.CONTROL_JSON_HDR_SEC,headerMap);
           responseMap.put(Gstr1ConstantsV31.CONTROL_JSON_SEC,controlMap);
           responseMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC, customMap);

           return responseMap;
    }



private Map<String,Object> formatGstnL0DataForMongo(String mongoL0Key,String gstin, String fp, Map<String,String> headers, Map<String,String> requestParameterMap)
 {
        Map<String,Object> gstnData = new HashMap<>();
        Map<String,Object> dataMap = new HashMap<>();
        Map<String,Object> controlMap = new HashMap<>();
        Map<String,Object> headerMap = new HashMap<>();
        
        gstnData = gstnSummaryServiceV31.processGstr1InvoiceDataL0(headers, requestParameterMap);
        dataMap.put("_id", mongoL0Key);
        dataMap.put("gstn", gstnData);
        controlMap.put("type", Gstr1ConstantsV31.SYNC_SECTION_AGGREGATE);
        controlMap.put("utime", new Timestamp(System.currentTimeMillis()));
        headerMap.put(Gstr1ConstantsV31.INPUT_GSTN, gstin);
        headerMap.put(Gstr1ConstantsV31.INPUT_FP, fp);
        
        dataMap.put("_id", mongoL0Key);
        dataMap.put("gstn", gstnData);
        dataMap.put("control", controlMap);
        dataMap.put("header", headerMap);
        
        return dataMap;
 }

@Override
public Map<String,Object> doSync(Map<String, String> requestHeaders, Map<String, String> requestParams)
{
	log.debug("doSync method START");
	log.info("doSync method START");
       Map<String, Object> resJioGST = null;
       Map <String, Object>resGSTN = null;
       Map<String, Object> convertJioGSTL0 = null;
       Map<String, Object> convertGSTNL0 = null; 
       Map<String,Object> syncBuckets=null;

       String gstin = MapUtils.getString(requestParams, Gstr1ConstantsV31.INPUT_GSTN, null);
       String fp = MapUtils.getString(requestParams, Gstr1ConstantsV31.INPUT_FP, null);
       String sectionType = MapUtils.getString(requestParams, Gstr1ConstantsV31.INPUT_SECTION, null);
       String flushStatus = MapUtils.getString(requestParams, Gstr1ConstantsV31.FLUSH_STATUS,Gstr1ConstantsV31.FLUSH_STATUS_TRUE);
       
       if(Gstr1ConstantsV31.SYNC_SECTION_AGGREGATE.equals(sectionType))
       {
    	   	  log.debug("Calling L0 Data from JIOGst DB");
    	      log.info("Calling L0 Data from JIOGst DB");
              resJioGST = gstr1SummaryServiceV31.processGstr1SummaryL0(requestParams);
              convertJioGSTL0=convertMapJioGstL0(resJioGST); 

              log.debug("Calling L0 Data from GSTN");
              log.info("Calling L0 Data from GSTN");
              resGSTN = getDataFromGstn(requestHeaders,requestParams,Gstr1ConstantsV31.SUMMARY_TYPE_L0,flushStatus);
              convertGSTNL0=convertMapGstnL0(resGSTN); 
              
              log.debug("************doSync method : comparison JioGST L0 and GSTN L0 Data Start");
              log.info("************doSync method : comparison JioGST L0 and GSTN Data L0 Start");
              syncBuckets = compareL0(convertJioGSTL0,convertGSTNL0,requestParams,requestHeaders);
              log.debug("************doSync method : comparison JioGST L0 and GSTN L0 Data and Sync Bucket Creation End");
              log.info("************doSync method : comparison JioGST L0 and GSTN L0 Data and Sync Bucket Creation End");
       }
       
       else if( Gstr1ConstantsV31.TYPE_B2CL.equalsIgnoreCase(sectionType)||
    		    Gstr1ConstantsV31.TYPE_B2CLA.equalsIgnoreCase(sectionType)||
   		        Gstr1ConstantsV31.TYPE_EXP.equalsIgnoreCase(sectionType)||
				Gstr1ConstantsV31.TYPE_EXPA.equalsIgnoreCase(sectionType)||
				Gstr1ConstantsV31.TYPE_B2B.equalsIgnoreCase(sectionType) ||
				Gstr1ConstantsV31.TYPE_B2BA.equalsIgnoreCase(sectionType) ||
				Gstr1ConstantsV31.TYPE_CDNR.equalsIgnoreCase(sectionType) ||
				Gstr1ConstantsV31.TYPE_CDNRA.equalsIgnoreCase(sectionType) ||
				Gstr1ConstantsV31.TYPE_CDNUR.equalsIgnoreCase(sectionType) ||
				Gstr1ConstantsV31.TYPE_CDNURA.equalsIgnoreCase(sectionType)
      			)
       {
    	   
    	   log.debug("Calling L2 Data from JIOGst DB");
 	       log.info("Calling L2 Data from JIOGst DB");
           resJioGST = getL2DataFromJioGST(requestHeaders, requestParams);
           
           log.debug("Calling L2 Data from GSTN");
           log.info("Calling L2 Data from GSTN");
           resGSTN = getDataFromGstn(requestHeaders,requestParams,Gstr1ConstantsV31.SUMMARY_TYPE_L2,flushStatus);
           
           if(resGSTN.containsKey(Gstr1ConstantsV31.ERROR_GRP))
			{
				return resGSTN;
			} 
           
           log.debug("************doSync method : comparison JioGST and GSTN Data Start");
           log.info("************doSync method : comparison JioGST and GSTN Data Start");
           Map<String,String> syncStatusMap = compareL2(resJioGST,resGSTN);
           log.debug("************doSync method : comparison JioGST and GSTN Data End");
           log.info("************doSync method : comparison JioGST and GSTN Data End");
           
           log.debug("************doSync method : Sync Buckets Creation Start");
           log.info("************doSync method : Sync Buckets Creation Start");           
           syncBuckets = getSyncL2Buckets(syncStatusMap,gstin,fp,sectionType,flushStatus);
           log.debug("************doSync method : Sync Buckets Creation End");
           log.info("************doSync method : Sync Buckets Creation End");
           
           log.debug("************doSync method : Sync Status JioGST DB Updation Start");
           log.info("************doSync method : Sync Status JioGST DB Updation Start");
           updateSuppliesDataForSync(requestHeaders,syncStatusMap,gstin,fp,sectionType);
           log.debug("************doSync method : Sync Status JioGST DB Updation End");
           log.info("************doSync method : Sync Status JioGST DB Updation End");
           
       }
       else{
    	   
    	   CommonUtilV31.throwException(ErrorCodesV31.ASP705, Gstr1ConstantsV31.ASP_SYNC, ErrorCodesV31.ASP705,
					null, HttpStatus.OK, messageSourceV31, null);
    	   
       }

       log.debug("************doSync method : Output creation success");
       log.info("************doSync method : Output creation success");
       log.debug("doSync method END");
       log.info("doSync method END");

       requestParams.put(Gstr1ConstantsV31.CONTROL_JSON_UTIME,uTime.toString());
       syncBuckets.put(Gstr1ConstantsV31.ASP_API_RESP_META_ATTR, requestParams);
       
       return syncBuckets;
}

@Override
public Map<String,Object> getSyncL2Buckets(Map <String,String>syncStatusMap,String gstin, String fp, String section,String flushStatus)
{
	log.debug("getSyncL2Buckets method START");
       int matchCount = 0;
       int misMatchCount = 0;
       int appendedCount = 0;
       int closedCount = 0;
       Map <String,Object> responseMap = new HashMap<>();
       Map <String,Object> syncResponse = new HashMap<>();
       Map <String,Object> metaDataMap = new HashMap<>();
       Map <String,Object> matchBucketMap = new HashMap<>();
       Map <String,Object> misMatchBucketMap = new HashMap<>();
       Map <String,Object> appendedBucketMap = new HashMap<>();
       Map <String,Object> closedBucketMap = new HashMap<>();
       List<Map <String,Object>> bucketList = new ArrayList<Map <String,Object>>();
       List<Map <String,Object>> syncList = new ArrayList<Map <String,Object>>();

       for (Map.Entry<String, String> entry : syncStatusMap.entrySet()) {

              if(Gstr1ConstantsV31.SYNC_STATUS_MATCHED.equals(entry.getValue()))
              {
                    matchCount = matchCount + 1;
              }
              else if(Gstr1ConstantsV31.SYNC_STATUS_MISMATCHED.equals(entry.getValue()))
              {
                    misMatchCount = misMatchCount + 1;
              }
              else if(Gstr1ConstantsV31.SYNC_STATUS_APPENDED.equals(entry.getValue()))
              {
                    appendedCount = appendedCount + 1;
              }
              else 
              {
                    closedCount = closedCount + 1;
              }
       }

       matchBucketMap.put("ty", "0");
       matchBucketMap.put("label", Gstr1ConstantsV31.SYNC_STATUS_MATCHED);
       matchBucketMap.put("count",matchCount);

       misMatchBucketMap.put("ty", "1");
       misMatchBucketMap.put("label", Gstr1ConstantsV31.SYNC_STATUS_MISMATCHED);
       misMatchBucketMap.put("count",misMatchCount);

       closedBucketMap.put("ty", "2");
       closedBucketMap.put("label", Gstr1ConstantsV31.SYNC_STATUS_CLOSED);
       closedBucketMap.put("count",closedCount);

       appendedBucketMap.put("ty", "3");
       appendedBucketMap.put("label", Gstr1ConstantsV31.SYNC_STATUS_APPENDED);
       appendedBucketMap.put("count",appendedCount);

       bucketList.add(matchBucketMap);
       bucketList.add(misMatchBucketMap);
       bucketList.add(closedBucketMap);
       bucketList.add(appendedBucketMap);

       syncResponse.put("sec_nm",section);
       syncResponse.put("sync",bucketList);

       syncList.add(syncResponse);

       responseMap.put(Gstr1ConstantsV31.INPUT_DATA,syncList);
		log.debug("getSyncL2Buckets method END");
    
       return responseMap;
}

public String identifyLevel(String type) {
	if (Gstr1ConstantsV31.TYPE_B2B.equals(type) || Gstr1ConstantsV31.TYPE_CDNR.equals(type)
			|| Gstr1ConstantsV31.TYPE_B2CL.equals(type) || Gstr1ConstantsV31.TYPE_EXP.equals(type)
			|| Gstr1ConstantsV31.TYPE_CDNUR.equals(type)|| Gstr1ConstantsV31.TYPE_EXPA.equals(type)
			|| Gstr1ConstantsV31.TYPE_B2BA.equals(type) || Gstr1ConstantsV31.TYPE_B2CLA.equals(type)
			|| Gstr1ConstantsV31.TYPE_CDNRA.equals(type) || Gstr1ConstantsV31.TYPE_CDNURA.equals(type)) {
		return Gstr1ConstantsV31.LEVEL_INV;
	} else {
		return Gstr1ConstantsV31.LEVEL_MONTHLY;
	}
} 

@Override
public void validateParams(Map<String, String> allRequestParams) {

       String gstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, null);
       String fp = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_FP, null);
       String sectionType = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_SECTION, null);
       String flushStatus = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.FLUSH_STATUS,"true");
       String action = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_ACTION,null);

       CommonUtil.validateEmptyString(MapUtils.getString(allRequestParams,Gstr1ConstantsV31.HEADER_GSTIN,""),
				ErrorCodesV31.ASP011031, Gstr1ConstantsV31.JIOGST_SYNC, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

       CommonUtil.validateEmptyString(MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_FP,""),
				ErrorCodesV31.ASP011311, Gstr1ConstantsV31.JIOGST_SYNC, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);

       CommonUtil.validateEmptyString(MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_SECTION,""),
				ErrorCodesV31.ASP011179, Gstr1ConstantsV31.JIOGST_SYNC, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
       
       CommonUtil.validateEmptyString(MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_ACTION,""),
				ErrorCodesV31.ASP010101, Gstr1ConstantsV31.JIOGST_SYNC, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
       
       CommonUtil.validateEmptyString(MapUtils.getString(allRequestParams, Gstr1ConstantsV31.FLUSH_STATUS,""),
				ErrorCodesV31.ASP010100, Gstr1ConstantsV31.JIOGST_SYNC, null,
				HttpStatus.BAD_REQUEST, AspConstants.FORM_CODE);
       
       if(!(Gstr1ConstantsV31.FLUSH_STATUS_TRUE.equalsIgnoreCase(flushStatus) || Gstr1ConstantsV31.FLUSH_STATUS_FALSE.equalsIgnoreCase(flushStatus)))
       {
    	   CommonUtil.throwException(ErrorCodesV31.ASP010100, Gstr1ConstantsV31.JIOGST_SYNC, null
					, HttpStatus.OK, null, AspConstants.FORM_CODE, null);
    	  
       }
       
       sectionType = sectionType.toLowerCase();
       allRequestParams.put(Gstr1ConstantsV31.INPUT_SECTION, sectionType); 

}



	
}
