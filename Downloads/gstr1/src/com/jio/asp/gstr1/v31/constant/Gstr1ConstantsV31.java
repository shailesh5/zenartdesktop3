package com.jio.asp.gstr1.v31.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface Gstr1ConstantsV31 {

	public static final String API_VERSION = "v3.1";
	
	public static final String API_NM = "GSTR1";
	public static final String API_SAVE = "/gstr1/" + API_VERSION + "/Supplies";
	public static final String API_SAVE_GSTN = "/gstr1/" + API_VERSION + "/GSTN";
	public static final String API_UPDATE = "GSTR1_Update";
	public static final String API_DEL = "GSTR1_Delete";
	public static final String API_PARK = "GSTR1_Park";
	public static final String API_L2 = "GSTR1_L2";

	
	//Type of business in gstr1 start
	public static final String TYPE_B2B = "b2b";
	public static final String TYPE_B2BA = "b2ba";
	public static final String TYPE_B2CL = "b2cl";
	public static final String TYPE_B2CLA = "b2cla";
	public static final String TYPE_B2CS = "b2cs";
	public static final String TYPE_AMEND = "amend";
	public static final String TYPE_B2CSA = "b2csa";
	public static final String TYPE_CDNR = "cdnr";
	public static final String TYPE_CDNRA = "cdnra";
	public static final String TYPE_CDNUR = "cdnur";
	public static final String TYPE_CDNURA = "cdnura";
	public static final String TYPE_EXP = "exp";
	public static final String TYPE_EXPA = "expa";
	public static final String TYPE_AT = "at";
	public static final String TYPE_ATA = "ata";
	public static final String TYPE_NIL = "nil";
	public static final String TYPE_HSN = "hsn";
	public static final String TYPE_TXPD = "txpd";
	public static final String TYPE_TXPDA = "txpda";
	public static final String TYPE_DOCS = "doc_issue";
	public static final String TYPE_CDNR_CREDIT = "C";
	public static final String TYPE_CDNR_DEBIT = "D";
	public static final String TYPE_CDNUR_CREDIT = "C";
	public static final String TYPE_CDNUR_DEBIT = "D";
	//Type of business in gstr1 end
	
	
	public static final String TYPE_FILE = "RETFILE";
	public static final String TYPE_STATUS = "RETSTATUS";
	public static final String TYPE_SUBMIT = "RETSUBMIT";
	public static final String TYPE_SAVE = "RETSAVE";
	public static final String TYPE_ALL = "all";
	
	public static final String SUMMARY_TYPE_L0 = "L0";

	public static final String GST = "gst";
	public static final String SYNC = "sync";

	public static final int GSTN_LENGTH = 15;
	public static final int ASP_CLIENT_ID_LENGTH = 15;
	public static final int ASP_CLIENT_SEC_LENGTH = 36;
	public static final int LOCATION_LENGTH = 20;
	public static final int TRANSACTION_LENGTH = 32;
	public static final int SOURCE_DEVICE_LENGTH = 36;
	public static final int IP_USER_LENGTH = 36;
	public static final int STATE_CODE_LENGTH = 2;
	public static final int STATE_COUNT_LENGTH = 37;
	public static final int APP_CODE_LENGTH = 36;
	public static final int MIN_LENGTH = 0;
	public static final int RET_PER_LENGTH = 6;

	public static final int READ_TIMEOUT = 10 * 1000;
	public static final int CONNECTION_TIMEOUT = 10 * 1000;
	
	
	public static final String ERROR_CODE = "error_code";
	public static final String ERROR_DESC = "error_desc";
	public static final String ERROR_GRP = "error_grp";
	public static final String ERROR_HTTP_CODE = "http_code";
	
	public static final String INPUT_SECTION = "section";
	public static final String CODE = "code";
	public static final String TO_DATE = "toDate";
	public static final String FROM_DATE = "fromDate";
	public static final String INPUT_SEK = "sek";
	public static final String INPUT_APP_KEY = "appkey";
	public static final String INPUT_DATA = "data";
	public static final String INPUT_GSTN = "gstin";
	public static final String INPUT_REK = "rek";
	public static final String INPUT_RETMONTH = "retMnth";
	public static final String INPUT_RETYEAR = "retYear";
	public static final String INPUT_FILTER = "filter";
	public static final String INPUT_CNPTY_GSTN = "ctin";
	public static final String INPUT_OFFSET = "offset";
	public static final String INPUT_LIMIT = "limit";
	public static final String INPUT_RETPERIOD = "retPeriod";
	public static final String INPUT_FRM_TIME = "from_time";
	public static final String INPUT_SIGN = "sign";
	public static final String INPUT_ST = "st";
	public static final String INPUT_SID = "sid";
	public static final String INPUT_HMAC = "hmac";
	public static final String INPUT_TRANSACTION_ID = "trans_id";
	public static final String INPUT_REFERENCE_ID = "reference_id";
	public static final String SUBMIT_REFERENCE_ID = "reference_id";
	public static final String FILE_ACK_NUM = "ack_num";

	public static final String GSTN_SAVED_TOKEN = "GstnSaved";
	public static final String GSTN_SUBMIT_TOKEN = "GstnSubmitted";

	public static final String JSON_REF_IDS = "refids";
	public static final String ERROR = "error";
	public static final String ERROR_MESSAGE = "message";
	public static final String ERROR_CD_STRING = "error_cd";

	public static final String GSTN_GSTR1_URL = "url.gsp.gstr1.baseurl";
	public static final String GSTN_BULK_DATA_FILE_URL = "url.gsp.bulk.dat.baseurl";
	public static final String GSTN_BULK_DATA_URL_LIST = "url.gsp.gstr1.urllist";
	
	public static final String INPUT_GSTN_ACTION_REQ = "action_required";
	public static final String INPUT_GSTN_ACTION = "action";
	public static final String INPUT_GSTN_RET_PER = "ret_period";
	public static final String INPUT_GSTN_CTIN = "ctin";
	public static final String INPUT_GSTN_FRM_TIME = "from_time";
	public static final String URL_AMP = "&";
	public static final String URL_EQUAL = "=";
	public static final String INPUT_FP = "fp";
	public static final String INPUT_GT = "gt";
	public static final String INPUT_CUR_GT = "cur_gt";
	public static final String INPUT_SNAME = "sname";
	public static final String INPUT_TYPE = "type";
	public static final String INPUT_ACK_NO="ackNo";
	public static final String INPUT_POS = "pos";
	public static final String INPUT_LEVEL = "level";
	public static final String INPUT_STATUS = "status";
	public static final String INPUT_OIDT = "oidt";
	public static final String INPUT_IDT = "idt";
	
	public static final String CONTROL_JSON_CLIENT_ID = "clientId";
	public static final String CONTROL_JSON_STATE = "status";
	public static final String CONTROL_JSON_GSTN_STATE = "gst_status";
	public static final String CONTROL_JSON_TRANSID = "transid";
	public static final String CONTROL_JSON_TYPE = "type";
	public static final String CONTROL_JSON_LEVEL = "level";
	public static final String CONTROL_JSON_ITIME = "itime";
	public static final String CONTROL_JSON_UTIME = "utime";
	public static final String CONTROL_JSON_ID = "_id";
	public static final String CONTROL_JSON_SEC = "control";
	public static final String CONTROL_JSON_GSTN_SEC = "gstn";
	public static final String CONTROL_JSON_HDR_SEC = "header";
	public static final String CONTROL_JSON_CUST_SEC = "custom";
	public static final String CONTROL_JSON_RECORDS_SEC = "records";
	public static final String CONTROL_JSON_VERSION = "ver";
	public static final String CONTROL_JSON_ETIME = "etime";
	public static final String CONTROL_JSON_GST_STATUS = "gst_status";
	public static final String CONTROL_JSON_SYNC_STATUS = "sync_status";
	public static final String GSTN_ACTION_REQ_VAL = "Y";

	public static final String HEADER_USER_NAME = "username";
	public static final String HEADER_AUTH_TOKEN = "auth-token";
	public static final String HEADER_STATE_CODE = "state-cd";
	public static final String HEADER_TXN = "txn";
	public static final String HEADER_DEVICE_STRING = "device-string";
	public static final String HEADER_CLIENT_ID = "aspclient-id";
	public static final String HEADER_SECRET = "asp-clientsecretkey";
	public static final String HEADER_SRC_DEV = "source-device";
	public static final String HEADER_LOC = "location";
	public static final String HEADER_APP_CD = "app-code";
	public static final String HEADER_IP = "ip-usr";
	public static final String HEADER_GSTIN = "gstin";

	public static final String CLIENT_STATUS_ACTIVE = "A";

	public static final String SYSTEM_ASP = "asp";
	public static final String SYSTEM_GSTN = "gstn";

	public static final String INPUT_SORT_ORDER = "sort_order";
	public static final String INPUT_SORT_BY = "sort";

	public static final String GSTN_GSTR1A_URL = "url.gsp.gstr1a.baseurl";

	// Error Exception Group
	public static final String AUTH_EXCEPTION = "Auth_Exception";
	public static final String GENERIC = "GENERIC";
	public static final String GSP_ERROR_GRP_VAL = "GSP";
	public static final String ASP_GSTR1 = "ASP_GSTR1";
	public static final String ASP_GSTR1_GRP = "ASP_GSTR1_GET";
	public static final String ASP_GSTR1A_SAVE_GRP = "ASP_GSTR1_SAVE";
	public static final String ASP_GRP = "ASP";
	public static final String ASP_GSTR1_RETURN_STATUS_GRP="ASP_GSTR1_Return_Status";
	public static final String ASP_GSTR1_SUBMIT_GRP = "ASP_GSTR1_Submit";
	public static final String ASP_GSTR1_FILE_GRP = "ASP_GSTR1_File";
	public static final String ASP_GSTR1_SAVE_TO_GSTN = "ASP_GSTR1_Save_To_GSTN";
	public static final String ASP_HEADER_EXCEPTION = "Asp_Header";
	public static final String GSTN_HEADER_EXCEPTION = "Gstn_Header";
	public static final String ASP_GSTR1_SUPPLIES_STATUS_GRP="ASP_GSTR1_SUP_STATUS";
	public static final String L0_SUMMARY ="l0_L2";
	public static final String ASP_L2_GET ="ASP_L2_GET";
	public static final String ASP_SYNC ="ASP_SYNC";
	
	

	// error code for Authenticate
	public static final String GSTN_DATE_FORMAT = "dd-MM-yyyy";
	public static final String HEADER_DATE_FORMAT = "dd/MM/yyyy hh:mm:ss a";

	public static final String INVOICE_STATE_NEW = "New";
	public static final String INVOICE_STATE_DEL = "Deleted";
	public static final String INVOICE_STATE_L2_DEL = "DEL";
	public static final String INVOICE_STATE_L2_PARK = "PRK";
	public static final String INVOICE_STATE_PARK = "Parked";
	public static final String INVOICE_STATE_SAVED = "Saved";
	public static final String INVOICE_STATE_SUB = "Submitted";
	public static final String INVOICE_STATE_FILE = "Filed";
	public static final String INVOICE_STATE_GSTN_FAILED = "GstnFailed";
	
	public static final String INVOICE_INPUT_STATE_DEL = "D";
	public static final String INVOICE_INPUT_STATE_ADD = "A";
	public static final String INVOICE_INPUT_STATE_APPEND = "A";
	public static final String INVOICE_INPUT_STATE_PARK = "P";

	public static final String JSON_IRT = "irt";
	public static final String JSON_CRT = "crt";
	public static final String JSON_SRT = "srt";
	public static final String JSON_NUM = "num";
	public static final String JSON_RT = "rt";
	public static final String JSON_FLAG = "flag";
	public static final String JSON_STATE = "state";
	public static final String JSON_RATES = "rates";
	public static final String JSON_ACTION = "action";
	public static final String JSON_STID = "stid";
	
	public static final String JSON_TTL_RCRD = "ttl_record";
	public static final String JSON_RCRD = "record";

	public static final String EX_ITM_PRFX = "_ITEM";
	public static final String EX_INV_PRFX = "_INV";


	public static final String GSTR1_RESP_SUCCESS_ST_CD = "1";
	public static final String GSTR1_RESP_FAILED_ST_CD = "0";
	public static final String GSTR1_RESP_ACKNO = "ackNo";
	public static final String GSTR1_RESP_STATUS_CD = "status_cd";

	public static final String CONTROL_REC_PR_STATUS = "Processing";
	public static final String CONTROL_REC_FAIL_STATUS = "Failed";
	public static final String CONTROL_REC_SUC_STATUS = "Success";
	public static final String CONTROL_REC_ACKNO = "ackNo";
	public static final String CONTROL_REC_USRACT = "t_ty";
	public static final String CONTROL_REC_STATUS = "status";
	public static final String CONTROL_REC_RCRDCOUNT = "tcnt";
	public static final String CONTROL_REC_SUCCCNT = "scnt";
	public static final String CONTROL_REC_ERRCNT = "ecnt";
	public static final String CONTROL_REC_WARNCNT = "wcnt";
	public static final String CONTROL_REC_STRTTIME = "stime";
	public static final String CONTROL_REC_UPDTTIME = "utime";
	public static final String CONTROL_REC_TIMEOUT = "timeout";
	public static final String CONTROL_REC_GSTIN = "gstin";
	public static final String CONTROL_REC_RET_PERIOD = "fp";
	public static final String CONTROL_REC_CLIENT_ID = "clientid";
	public static final String CONTROL_REC_ID = "_id";
	public static final String CONTROL_WARN_REPORT = "warningReport";
	public static final String ERROR_REPORT = "errorReport";
	public static final String ERROR_RESPONSE = "response";

	public static final String HSN_SC = "hsn_sc";
	public static final String HSN_DESC = "desc";
	public static final String HSN_UQC = "uqc";
	public static final String INPUT_NT_NUM = "nt_num";
	public static final String INPUT_NT_DT = "nt_dt";
	public static final String INPUT_ONT_NUM = "ont_num";
	public static final String INPUT_INUM = "inum";
	public static final String INPUT_OINUM = "oinum";
	public static final String DOC_NUM = "doc_num";
	public static final String DOC_TYPE = "doc_typ";
	public static final String DOC_DET = "doc_det";
	public static final String DOCS = "docs";
	public static final String INPUT_ITMS = "itms";
	public static final String INPUT_INV = "inv";
	
	public static final String GSTR1_B2CS_DATE = "date";
	public static final String KEY_APPENDMENT_A = "A";
	public static final String GSTR1_HASH = "#";

	// JSON Key generation constants
	public static final String MONGO_KEY_SEP = ":";
	public static final String TYPE_MONGO_KEY_1 = (TYPE_B2B + GSTR1_HASH + TYPE_AMEND + GSTR1_HASH + TYPE_B2CL
			+ GSTR1_HASH + TYPE_EXP + GSTR1_HASH  );
	public static final String TYPE_MONGO_KEY_2 = (TYPE_CDNR + GSTR1_HASH + TYPE_CDNUR+ GSTR1_HASH);
	public static final String TYPE_MONGO_KEY_3 = (TYPE_B2CS + GSTR1_HASH + TYPE_B2CSA + GSTR1_HASH);
	public static final String TYPE_MONGO_KEY_4 = (TYPE_HSN + GSTR1_HASH);
	public static final String TYPE_MONGO_KEY_5 = (TYPE_AT + GSTR1_HASH + TYPE_ATA + GSTR1_HASH + TYPE_TXPD
			+ GSTR1_HASH + TYPE_TXPDA + GSTR1_HASH);
	public static final String TYPE_MONGO_KEY_6 = (TYPE_DOCS + GSTR1_HASH);
	public static final String TYPE_MONGO_KEY_7 = (TYPE_NIL + GSTR1_HASH);
	public static final String TYPE_MONGO_KEY_8 = (TYPE_EXPA + GSTR1_HASH + TYPE_B2BA + GSTR1_HASH + TYPE_B2CLA + GSTR1_HASH);
	public static final String TYPE_MONGO_KEY_9 = (TYPE_CDNRA + GSTR1_HASH + TYPE_CDNURA + GSTR1_HASH);

	public static final String TYPE_MONGO_KEY_CDNR_CDNUR = "CDN";

	// MONGO COLLECTION NAMES starts
	// public static final String GSTR1_COL = "gstr1";
	// public static final String GSTR1_SUPPLIES_STATUS= "api_transactions";
	// Mongo Collection names ends

	// level constants start
	public static final String LEVEL_INV = "inv";
	public static final String LEVEL_DAILY = "daily";
	public static final String LEVEL_MONTHLY = "monthly";
	// level constants end

	// GSP Headers
	public static final String GSP_HEADER_CONENT_TYPE = "Content-Type";
	public static final String GSP_HEADER_ASP_ID = "asp-id";
	public static final String GSP_HEADER_ASP_LICENSE_KEY = "asp-licensekey";
	public static final String GSP_HEADER_DEVICE_STRING = "asp-devicestring";
	public static final String GSP_HEADER_API_VER = "api-ver";

	// JSON Response Attribute names START
	public static final String ASP_API_RESP_DATA_ATTR = "data";
	public static final String ASP_API_RESP_META_ATTR = "meta";
	public static final String ASP_API_RESP_RCRD_ATTR = "record";
	public static final String ASP_API_RESP_WARN_ATTR = "warning";
	// JSON Response Attribute names END

	// For GSTN Response
	public static final String RESP_STATUS_CODE = "status_cd";
	public static final String RESP_SUCCESS_CODE = "1";
	public static final String INPUT_ACTION = "action";
	public static final String SUMMARY_TYPE_L2 = "L2";
	

	public static final String JSON_CLIENT_ID = "aspclient_id";
	public static final String JSON_CLIENT_KEY = "asp_secretkey";
	public static final String JSON_USER_STATUS = "status";
	public static final String JSON_GSTIN = "gstin";
	public static final String JSON_WL = "wl_enabled";
	public static final String CURSOR = "cursor";
	public static final String RESULT_GSTN = "result.gstin";
	public static final String RESULT_FP = "result.fp";
	public static final String RESULT = "result";
	public static final long HRS_IN_MS = 60000 * 60;
	public static final Object ASP_GSTR1_GSTN_GET = "GSTIN SUMMARY";
	public static final String GSTN_REDIS_EXP_TIME = "app.config.redis.expireTime";
//	Supplies L2 where default condition for getting data which is not equals to  DEFAULT_L2_GET_WQ_PARAM will get in query ;
	public static final String TYPE_DOCS_L2 = "DOCISS";
	public static final String CONTROL_JSON_CUST_SEC_L2_List = "itms";
	public static final String L2_SUMMARY = "l2_summary";
	public static final String STORE_ID_IN_DATA = "gstin";

	public static final String L0_ACTION = "RETSUM";

	public static final String RESP_TTL_VAL = "ttl_val";
	public static final String RESP_INV_TAX_VAL = "ttl_txval";
	public static final String RESP_INV_ADV_VAL = "ttl_adv";
	public static final String RESP_INV_DIFF_VAL = "ttl_diff";
	public static final String RESP_INV_TAX_LIAB = "ttl_tax_liab";
	public static final String RESP_INV_IGST = "ttl_igst";
	public static final String RESP_INV_CGST = "ttl_cgst";
	public static final String RESP_INV_SGST = "ttl_sgst";
	public static final String RESP_TTL_COUNT = "ttl_count";

	public static final String INV_NILSUP_TYP = "sply_ty";
	public static final String INV_NILSUP_AMT = "nil_amt";
	public static final String INV_EXPT_AMT = "expt_amt";
	public static final String INV_NGSUP_AMT = "ngsup_amt";
	public static final String INV_DOC_ISSUED = "totnum";
	public static final String INV_DOC_CANCELLED = "cancel";
	public static final String INV_NET_DOC_ISSUED = "net_issue";
	public static final String RESP_TTL_TXVAL = "ttl_txval";

	public static final String API_NAME = "gstr1";

	public static final String INPUT_STRUCTURE_TYP = "structure";

	public static final String INPUT_STRUCTURE_VAL_DFLT = "1";

	public static final String HEADER_GSTIN_DB = "header.gstin";
	public static final String HEADER_FP_DB = "header.fp";

	public static final String CONTROL_REFERENCEID = "control.referenceId";
	public static final String CONTROL_STATUS = "control.status";
	public static final String CONTROL_UPLOADTIME = "control.uploadtime";
	public static final String CONTROL_UPLOADBY = "control.uploadby";
	public static final String CONTROL_TYPE = "control.type";
	public static final String STATUS_UPLOAD = "GstnUpload";
	public static final String CONTROL_GSTN_STATUS = "control.gst_status";

	public static final String STATUS_SUBMIT_UPLOAD = "GstnSubmitted";
	public static final String STATUS_FILE_UPLOAD = "GstnFiled";

	public static final String GSTN_JSON_STATUS_CD = "status_cd";

	public static final String GSTN_STATUS_CD_P = "P";
	public static final String GSTN_STATUS_CD_E = "ER";
	public static final String GSTN_STATUS_CD_PE = "PE";
	public static final String GSTN_STATUS_CD_IP = "IP";
	public static final String GSTN_STATUS_CD_REC = "REC";

	public static final String STATUS_SAVED = "GstnSaved";

	public static final String ACKNUMBER = "ackNumber";
	public static final String REFIDS = "refids";
	public static final String ERRORVAL = "error";

	public static final String GSTN_JSON_ERROR_REPORT = "error_report";
	public static final int STATE_COUNT_LENGTH_97 = 97;

	public static final String INPUT_FLUSH = "flush";

	public static final String RESP_SUCCESS_CODE_BULK = "2";

	public static final String RESP_L2_KEY_TOKEN = "token";

	public static final String FILE_URLS_ACTION = "file.url.action";

	public static final String TOKEN = "token";

	public static final Object FILE_LOAD_URL = "getFileUrl";

	public static final String BULK_CLIENT_ID = "client-id";

	public static final String BULK_CLIENT_KEY = "client-secret";
	
	public static final String INVID ="inv_id";
	
	public static final String API_NUM ="gstr1";
	
	


	public static final String RESP_ERROR_CODE_RET13508 = "RET13508";

	public static final String GSTN_JSON_ERROR_CD = "error_cd";
	public static final String GSTN_JSON_ERROR_MSG = "error_msg";

	public static final String DOT = ".";
	public static final String ASPSTATUS = "asp_status";
	public static final String ASP_STATUS_INIT = "Initiated";
	public static final String ASP_STATUS_COMPLETE = "Completed";

	public static final String ASPDATA = "asp_data";
	public static final String REFID = "ref_id";
	public static final String JSON_ASPSTATUS = ASPDATA + DOT + ASPSTATUS;
	public static final String JSON_ASPUTIME = ASPDATA + DOT + CONTROL_REC_UPDTTIME;
	public static final String JSON_ASPETIME = ASPDATA + DOT + CONTROL_JSON_ETIME;

	public static final String ASPREQUEST = "ASPREQUEST";
	public static final String ASPHEADER = "ASPHEADER";
	public static final String ASPPAYLOAD = "ASPPAYLOAD";

	public static final List<String> invSecList = Arrays.asList(Gstr1ConstantsV31.TYPE_B2B, Gstr1ConstantsV31.TYPE_B2BA,Gstr1ConstantsV31.TYPE_B2CL,
			Gstr1ConstantsV31.TYPE_B2CLA,Gstr1ConstantsV31.TYPE_CDNR, Gstr1ConstantsV31.TYPE_CDNRA, Gstr1ConstantsV31.TYPE_CDNUR, Gstr1ConstantsV31.TYPE_CDNURA,
			Gstr1ConstantsV31.TYPE_EXP, Gstr1ConstantsV31.TYPE_EXPA);
	
	public static final Object GSTN_EST_BULK = "est";
	
	public static final List<String> aggSecList = Arrays.asList(Gstr1ConstantsV31.TYPE_B2CS, Gstr1ConstantsV31.TYPE_B2CSA, Gstr1ConstantsV31.TYPE_AT,
			Gstr1ConstantsV31.TYPE_ATA, Gstr1ConstantsV31.TYPE_TXPD, Gstr1ConstantsV31.TYPE_TXPDA, Gstr1ConstantsV31.TYPE_NIL, Gstr1ConstantsV31.TYPE_DOCS,
			Gstr1ConstantsV31.TYPE_HSN);
	
	public static final List<String> typeList = Arrays.asList(Gstr1ConstantsV31.REPORT_TYPE_NEW, Gstr1ConstantsV31.REPORT_TYPE_INVALID, Gstr1ConstantsV31.REPORT_TYPE_VALID);
	
	public static final List<String> secReportList = Arrays.asList(Gstr1ConstantsV31.TYPE_B2B, Gstr1ConstantsV31.TYPE_B2BA, Gstr1ConstantsV31.TYPE_CDNR
			, Gstr1ConstantsV31.TYPE_CDNRA);
	
	public static final String ACK_URL = "url.acknowledgement_number-v3.1";

	public static final String CONVERT_URL = "url.json_converter-v3.1";

	public static final String  REST_READ_TIMEOUT = "rest-read-timeout";

	public static final String REST_CONNECTION_TIMEOUT = "rest-connection-timeout";
	
	public static final String INPUT_STATUS_ASP_L2 = "jio_status";

	public static final String INPUT_STATUS_GST_L2 = "jst_status";

	public static final String JSON_TYPE_SEC = "type";

	public static final String RESP_ERROR_CODE_RTN_25 = "RTN_25";
	
	public static final String GSTIN_HEADER = "header.gstin";
	public static final String FP_HEADER = "header.fp";
	public static final String TYPE_HEADER = "header.type";
	public static final String SEC = "sec";

	public static final String CONTROL_VERSION = "control.ver";
	public static final String CONTROL_SYNC_STATUS = "control.sync_status";

	public static  Map<String, String> FILTER_STATUS_MAP() 
	{
		Map<String,String> map = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		map.put("gst:all", SYNC_STATUS_ALL);
		map.put("gst:GstnSaved", STATUS_SAVED);
		map.put("gst:GstnUpload", STATUS_UPLOAD);
		map.put("gst:GstnSubmitted", STATUS_SUBMIT_UPLOAD);
		map.put("gst:New", INVOICE_STATE_NEW);
		map.put("gst:GstnFiled", STATUS_FILE_UPLOAD);
		map.put("gst:GstnFailed", INVOICE_STATE_GSTN_FAILED);
		map.put("sync:all", SYNC_STATUS_ALL);
		map.put("sync:matched", SYNC_STATUS_MATCHED);
		map.put("sync:mismatched", SYNC_STATUS_MISMATCHED);
		map.put("sync:appended", SYNC_STATUS_APPENDED);
		map.put("sync:closed", SYNC_STATUS_CLOSED);

		return Collections.unmodifiableMap(map);

	}
	
	public static  Map<String, String> SYNC_AGG_SECTION_MAP() 
	{
		Map<String,String> map = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		map.put(TYPE_B2CS, TYPE_B2CS);
		map.put(TYPE_AT, TYPE_AT);
		map.put(TYPE_TXPD, TYPE_TXPD);
		map.put(TYPE_HSN, TYPE_HSN);
		map.put(TYPE_B2CSA, TYPE_B2CSA);
		map.put(TYPE_ATA, TYPE_ATA);
		map.put(TYPE_TXPDA, TYPE_TXPDA);
		return Collections.unmodifiableMap(map);

	}
	
	public static  Map<String,String> FILTER_META_MAP() 
	{
		Map<String,String> map = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		map.put(SYNC, CONTROL_SYNC_STATUS);
		map.put(GST, CONTROL_GSTN_STATUS);
		
		return Collections.unmodifiableMap(map);
    }
	
	public static final int MIN_TRANSACTION_LENGTH = 26;
	public static final int MAX_TRANSACTION_LENGTH = 32;
	
	public static final String TYPE_AMENDMENTS = "A";
	
	
	public static final String INPUT_ACTION_GETSYNC = "getsync";
	public static final String INPUT_ACTION_FILTER = "filter";
	public static final String FILTER_ACTION_ALL = "sync:all";
	public static final String FILTER_ACTION_MATCHED = "sync:matched";
	public static final String FILTER_ACTION_MISMATCHED = "sync:mismatched";
	public static final String FILTER_ACTION_CLOSED = "sync:closed";
	public static final String FILTER_ACTION_APPENDED = "sync:appended";
	
	public static final String SYNC_STATUS_ALL = "all";
	public static final String SYNC_STATUS_MATCHED = "matched";
	public static final String SYNC_STATUS_MISMATCHED = "mismatched";
	public static final String SYNC_STATUS_APPENDED = "appended";
	public static final String SYNC_STATUS_CLOSED = "closed";
	
	
	public static final String SYNC_FILTER="syncFilter";
	
	public static final String SYNC_DATA_COLLECTION_NAME = "sync.gstr1L2GstnData";
	public static final String SYNC_CONTROL_COLLECTION_NAME = "sync.gstr1L2Control"; 
	public static final String SUPPLIES_COLLECTION_NAME="gstr1.col";
	public static final String SYNC_STATUS_ADDED  = "Added"; 
	public static final String HEADER_INUM_DB  = "gstn.inum";

	public static final String FLUSH_STATUS = "flush";
	public static final String SYNC_ACTION = "dosync";
	public static final String ASP_SYNC_EXCEPTION="SYNC EXCEPTION";
	public static final String INPUT_SECTION_AGG ="aggregate";

	public static final String SYNC_SECTION_AGGREGATE = "aggregate";

	public static final String CONTROL_GST_STATUS = "control.gst_status";
	public static final String CONTROL_UTIME="control.utime";
	public static final String FLUSH_STATUS_TRUE = "true";

	public static final String FLUSH_STATUS_FALSE = "false";
	
	public static final String REPORT_TYPE_NEW = "new";
	public static final String REPORT_TYPE_INVALID = "invalid";
	public static final String REPORT_TYPE_VALID = "valid";
	
	public static final String RESPONSE_STATUS_CD = "status_cd";
	public static final String RESPONSE_STATUS_MSG = "status_msg";
	public static final String RESPONSE_ERR_SYS = "err_sys";
	public static final String RESPONSE_ERR_GRP = "err_grp";
	public static final String RESPONSE_DEV_MSG = "dev_msg";
	public static final String RESPONSE_USR_MSG = "usr_msg";
	public static final String RESPONSE_USR_ACT = "usr_act";
	public static final String RESPONSE_DETAILS = "details";
	//NEW ERROE GROUP
	public static final String JIOGST_L0 = "JIOGST_L0";
	public static final String JIOGST_L2 = "JIOGST_L2";
	public static final String GSTN_L0 = "GSTN_L0";
	public static final String GSTN_L2 = "GSTN_L2";
	public static final String JIOGST_L0_L2 = "JIOGST_L0_L2";
	public static final String GSTN_L0_L2 = "GSTN_L0_L2";
	public static final String ASP_GET = "ASP_GET";
	public static final String JIOGST_SAVE = "JIOGST_SAVE";
	public static final String JIOGST_RET = "JIOGST_RET";
	public static final String JIOGST_SYNC = "JIOGST_SYNC";
	public static final String GSTN_RET = "GSTN_RET";


	public static final String GSTN_SAVE = "GSTN_SAVE";

	public static final String PROCESSING = "Processing";

	public static final String RESP_ERROR_CODE_RTN_24 = "RTN_24";
	
	
}
