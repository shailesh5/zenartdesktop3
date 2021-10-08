package com.jio.asp.gstr1.common.intercept;

public class CommonConstants {
	public static final String HEADER_CLIENT_ID = "aspclient-id";
	public static final String HEADER_SECRET = "asp-clientsecretkey";
	public static final String HEADER_IP = "ip-usr";
	public static final String HEADER_GSTIN = "gstin";
	public static final String HEADER_SEK = "sek";
	public static final String HEADER_APP_KEY = "appkey";
	public static final String HEADER_AUTH_TOKEN = "auth-token";
	
	public static final String ACCESS_LOG_URL="url";
	public static final String ACCESS_LOG_STATUS="status";
	public static final String ACCESS_LOG_METHOD="method";
	
	public static final String ACCESS_LOG_STATUS_SUC="Completed";
	public static final String ACCESS_LOG_STATUS_FAIL="Failed";
	public static final String ACCESS_LOG_STATUS_INIT="Initiated";
	
	public static final String HEADER_DATE_FORMAT = "dd/MM/yyyy hh:mm:ss a";
	
	public static final String MONGO_REC_ID = "_id";
	public static final String JSON_CLIENT_ID = "aspclient_id";
	public static final String JSON_CLIENT_KEY = "asp_secretkey";
	public static final String JSON_USER_STATUS = "status";
	public static final String JSON_GSTIN = "gstin";
	public static final String JSON_WL = "wl_enabled";
	public static final String JSON_UDATE = "udate";
	public static final String JSON_IDATE = "idate";

	
	public static final String CLIENT_STATUS_ACTIVE = "A";
	
	public static final String ASP_HEADER_EXCEPTION = "Asp_Header";
	public static final String AUTH_EXCEPTION = "Auth_Exception";

	public static final String ASP003 = "ASP003";
	public static final String ASP004 = "ASP004";
	public static final String ASP005 = "ASP005";
	public static final String ASP501="ASP501";
	public static final String ASP006 = "ASP006";
	public static final String ASP034 = "ASP034";
	public static final String ASP035 = "ASP035";
	public static final String ASP013 = "ASP013";
	
	public static final String ACCESS_LOG_KAFKA_TOPIC="AccessLog";
	
	
	public static final String ERROR_CODE = "error_code";
	public static final String ERROR_DESC = "error_desc";
	public static final String ERROR_GRP = "error_grp";
	public static final String ERROR_HTTP_CODE = "http_code";
}
