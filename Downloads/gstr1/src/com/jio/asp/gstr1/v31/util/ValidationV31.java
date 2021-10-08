package com.jio.asp.gstr1.v31.util;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import com.jio.asp.gstr1.v31.constant.ErrorCodesV31;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;
import com.jio.asp.validation.AspConstants;
import com.jio.asp.validation.CommonUtil;
import com.jio.asp.validation.ErrorCodes;

public class ValidationV31 {

	/**
	 * Common validation for Asp api header data. This method should be used
	 * only for those api which are specific to ASP. They should be going to
	 * call GSP or GSTN for any data. As ASP api will have limited data, this
	 * method should be only used for ASP API.
	 * 
	 * @param headerData,
	 *            header data to be validated.
	 * @param messageSourceV31,
	 *            message resource to read the error message.
	 */
	public static void aspApiHeaderValidation(Map<String, String> headerData, MessageSource messageSourceV31) {
		 Map<String, String> newMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		 newMap.putAll(headerData);
		CommonUtilV31.validateEmpty(headerData, ErrorCodesV31.HEADER_DATA_NULL_ASP026,
				Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, "GSTR1_SUM_ASP026", null, HttpStatus.INTERNAL_SERVER_ERROR,
				messageSourceV31);
//		String aspLocation = headerData.get("location");
		String aspLocation = newMap.get("location");
		String ipUsr = headerData.get("ip-usr");
		String stateCode = headerData.get("state-cd");
		String txn = headerData.get("txn");
		String sourceDevice = headerData.get("source-device");
		String deviceString = headerData.get("device-string");
		String appCode = headerData.get("app-code");

		CommonUtilV31.validateEmptyString(txn, ErrorCodesV31.ASP011, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP011, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		//Validation Length 26 to 32 
		CommonUtilV31.validateLength(txn, Gstr1ConstantsV31.MIN_TRANSACTION_LENGTH, Gstr1ConstantsV31.MAX_TRANSACTION_LENGTH,
				ErrorCodesV31.ASP012, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, ErrorCodesV31.ASP012,
				new Object[] {Gstr1ConstantsV31.MIN_TRANSACTION_LENGTH , Gstr1ConstantsV31.MAX_TRANSACTION_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);


		CommonUtilV31.validateEmptyString(appCode, ErrorCodesV31.ASP009, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP009, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		CommonUtilV31.validateEmptyString(deviceString, ErrorCodesV31.ASP018, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP018, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		CommonUtilV31.validateEmptyString(aspLocation, ErrorCodesV31.ASP007, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP007, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		CommonUtilV31.validateLength(aspLocation, Gstr1ConstantsV31.MIN_LENGTH, Gstr1ConstantsV31.LOCATION_LENGTH,
				ErrorCodesV31.ASP008, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, ErrorCodesV31.ASP008,
				new Object[] { Gstr1ConstantsV31.LOCATION_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		CommonUtilV31.validateEmptyString(sourceDevice, ErrorCodesV31.ASP019, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP019, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		CommonUtilV31.validateLength(sourceDevice, Gstr1ConstantsV31.MIN_LENGTH, Gstr1ConstantsV31.SOURCE_DEVICE_LENGTH,
				ErrorCodesV31.ASP020, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, ErrorCodesV31.ASP020,
				new Object[] { Gstr1ConstantsV31.SOURCE_DEVICE_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR,
				messageSourceV31);

		CommonUtilV31.validateEmptyString(ipUsr, ErrorCodesV31.ASP013, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP013, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		CommonUtilV31.validateLength(ipUsr, Gstr1ConstantsV31.MIN_LENGTH, Gstr1ConstantsV31.IP_USER_LENGTH,
				ErrorCodesV31.ASP014, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, "ASP014",
				new Object[] { Gstr1ConstantsV31.IP_USER_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		CommonUtilV31.validateEmptyString(stateCode, ErrorCodesV31.ASP015, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP015, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		CommonUtilV31.validateLength(stateCode, Gstr1ConstantsV31.STATE_CODE_LENGTH,
				Gstr1ConstantsV31.STATE_CODE_LENGTH, ErrorCodesV31.ASP016, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP016, new Object[] { Gstr1ConstantsV31.STATE_CODE_LENGTH },
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		if (Integer.parseInt(stateCode) > Gstr1ConstantsV31.STATE_COUNT_LENGTH
				|| Integer.parseInt(stateCode) < Gstr1ConstantsV31.MIN_LENGTH) {
			if(Integer.parseInt(stateCode) != Gstr1ConstantsV31.STATE_COUNT_LENGTH_97){
			CommonUtilV31.throwException(ErrorCodesV31.ASP017, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
					ErrorCodesV31.ASP017,
					new Object[] { Gstr1ConstantsV31.STATE_COUNT_LENGTH, Gstr1ConstantsV31.MIN_LENGTH, Gstr1ConstantsV31.STATE_COUNT_LENGTH_97},
					HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, null);
			}
		}
	}

	/**
	 * Common validation for Asp api header data. This method should be used
	 * only for those api which are specific to ASP. They should be going to
	 * call GSP or GSTN for any data. As ASP api will have limited data, this
	 * method should be only used for ASP API.
	 * 
	 * @param headerData,
	 *            header data to be validated.
	 * @param messageSourceV31,
	 *            message resource to read the error message.
	 */
	public static void gstnApiHeaderValidation(Map<String, String> headerData, MessageSource messageSourceV31) {
		
		
		CommonUtilV31.validateEmpty(headerData, ErrorCodesV31.HEADER_DATA_NULL_ASP026,
				Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, "GSTR1_SUM_ASP026", null, HttpStatus.INTERNAL_SERVER_ERROR,
				messageSourceV31);

		String stateCode = headerData.get(Gstr1ConstantsV31.HEADER_STATE_CODE);
		String txn = headerData.get(Gstr1ConstantsV31.HEADER_TXN);
		String ipUsr = headerData.get(Gstr1ConstantsV31.HEADER_IP);
		String deviceString = headerData.get(Gstr1ConstantsV31.HEADER_DEVICE_STRING);

		CommonUtilV31.validateEmptyString(txn, ErrorCodesV31.ASP011, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP011, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		
		CommonUtilV31.validateLength(txn, Gstr1ConstantsV31.MIN_TRANSACTION_LENGTH, Gstr1ConstantsV31.MAX_TRANSACTION_LENGTH,
				ErrorCodesV31.ASP012, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, ErrorCodesV31.ASP012,
				new Object[] {Gstr1ConstantsV31.MIN_TRANSACTION_LENGTH , Gstr1ConstantsV31.MAX_TRANSACTION_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
	
		CommonUtilV31.validateEmptyString(MapUtils.getString(headerData, Gstr1ConstantsV31.INPUT_SEK),
				ErrorCodesV31.GSTR1_SUM_EMPTY, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, "GSTR1_HDR_SEK_ASP105", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		CommonUtilV31.validateEmptyString(MapUtils.getString(headerData, Gstr1ConstantsV31.INPUT_APP_KEY),
				ErrorCodesV31.GSTR1_SUM_TABLE_TYPES_EMTY, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				"GSTR1_HDR_APP_KEY_ASP106", null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		CommonUtilV31.validateEmptyString(MapUtils.getString(headerData, Gstr1ConstantsV31.HEADER_USER_NAME),
				ErrorCodesV31.ASP021, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, ErrorCodesV31.ASP021, null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		CommonUtilV31.validateEmptyString(MapUtils.getString(headerData, Gstr1ConstantsV31.HEADER_AUTH_TOKEN),
				ErrorCodesV31.ASP022, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, ErrorCodesV31.ASP022, null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);


		CommonUtilV31.validateEmptyString(deviceString, ErrorCodesV31.ASP018, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP018, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		CommonUtilV31.validateEmptyString(ipUsr, ErrorCodesV31.ASP013, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP013, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		CommonUtilV31.validateLength(ipUsr, Gstr1ConstantsV31.MIN_LENGTH, Gstr1ConstantsV31.IP_USER_LENGTH,
				ErrorCodesV31.ASP014, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION, ErrorCodesV31.ASP014,
				new Object[] { Gstr1ConstantsV31.IP_USER_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);

		CommonUtilV31.validateEmptyString(stateCode, ErrorCodesV31.ASP015, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP015, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		CommonUtilV31.validateLength(stateCode, Gstr1ConstantsV31.STATE_CODE_LENGTH,
				Gstr1ConstantsV31.STATE_CODE_LENGTH, ErrorCodesV31.ASP016, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
				ErrorCodesV31.ASP016, new Object[] { Gstr1ConstantsV31.STATE_CODE_LENGTH },
				HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		if (Integer.parseInt(stateCode) > Gstr1ConstantsV31.STATE_COUNT_LENGTH
				|| Integer.parseInt(stateCode) < Gstr1ConstantsV31.MIN_LENGTH) {
			if(Integer.parseInt(stateCode) != Gstr1ConstantsV31.STATE_COUNT_LENGTH_97)
			{
			
			
			CommonUtilV31.throwException(ErrorCodesV31.ASP017, Gstr1ConstantsV31.ASP_HEADER_EXCEPTION,
					ErrorCodesV31.ASP017,
					new Object[] { Gstr1ConstantsV31.STATE_COUNT_LENGTH, Gstr1ConstantsV31.MIN_LENGTH,Gstr1ConstantsV31.STATE_COUNT_LENGTH_97 },
					HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31, null);
		}
		}	
	}

	/**
	 * Common validation for Asp api header data and gstin in payload.
	 * 
	 * @param hdrGstin,
	 *            header gstin to be validated.
	 * @param payloadGstin,
	 *            payload gstin to be validated.
	 * @param messageSourceV31,
	 *            message resource to read the error message.
	 */
	public static void apiHeaderAndPayloadGstinValidation(String hdrGstin, String payloadGstin,
			MessageSource messageSourceV31) {
		if (!hdrGstin.equalsIgnoreCase(payloadGstin)) {
			CommonUtil.throwException(ErrorCodes.ASP1021, AspConstants.JIOGST_HEADER, null, HttpStatus.BAD_REQUEST,
					null, AspConstants.FORM_CODE);
		}

	}

	public static void apiHeaderAndParamsGstinValidation(String hdrGstin, String paramGstin,
			MessageSource messageSourceV31) {
		if (!hdrGstin.equalsIgnoreCase(paramGstin)) {
			CommonUtil.throwException(ErrorCodes.ASP1018, AspConstants.JIOGST_HEADER, null, HttpStatus.BAD_REQUEST,
					null, AspConstants.FORM_CODE);

		}

	}

	public static void apiRequestParamsValidateForSaveGstn(Map<String, String> allRequestParams,
			MessageSource messageSourceV31) {

		String gstin = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_GSTN, "");
		String fp = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_FP, "");
		String section = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_SECTION, "");
		if(StringUtils.isBlank(section)){
			allRequestParams.put( Gstr1ConstantsV31.INPUT_SECTION, Gstr1ConstantsV31.TYPE_ALL);
		}
		
	 
		CommonUtil.validateEmptyString(gstin, ErrorCodes.ASP1802, Gstr1ConstantsV31.GSTN_SAVE, null, HttpStatus.BAD_REQUEST,
				"01");

		
 
	
		CommonUtil.validateEmptyString(fp, ErrorCodes.ASP1803, Gstr1ConstantsV31.GSTN_SAVE, null, HttpStatus.BAD_REQUEST,
				"01");

		
		if (StringUtils.isNotBlank(section) && (!Gstr1ConstantsV31.TYPE_ALL.equalsIgnoreCase(section)
				&& !Gstr1ConstantsV31.aggSecList.contains(section) && !Gstr1ConstantsV31.invSecList.contains(section))) {
		 
			CommonUtil.throwException(ErrorCodes.ASP1801, Gstr1ConstantsV31.GSTN_SAVE, null, HttpStatus.BAD_REQUEST,
					null, AspConstants.FORM_CODE);
			
		}

	}
	
	public static void apiRequestParamsValidateForL2Report(Map<String, String> allRequestParams,
			MessageSource messageSourceV31) {

		String type = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_TYPE, "");
		String section = MapUtils.getString(allRequestParams, Gstr1ConstantsV31.INPUT_SECTION, "");
		if(StringUtils.isBlank(section)){
			allRequestParams.put( Gstr1ConstantsV31.INPUT_SECTION, Gstr1ConstantsV31.TYPE_ALL);
		}
		
		CommonUtilV31.validateEmptyString(type, ErrorCodesV31.ASP528, Gstr1ConstantsV31.ASP_GSTR1_GRP,
				ErrorCodesV31.ASP528, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSourceV31);
		
		if (StringUtils.isNotBlank(type)
				&& !Gstr1ConstantsV31.typeList.contains(type.toLowerCase())) {
			CommonUtilV31.throwException(ErrorCodesV31.ASP529, Gstr1ConstantsV31.ASP_GSTR1_GRP,
					ErrorCodesV31.ASP529, null, HttpStatus.INTERNAL_SERVER_ERROR,
					messageSourceV31, null);
		}
		
		if (StringUtils.isNotBlank(section) && (!Gstr1ConstantsV31.TYPE_ALL.equalsIgnoreCase(section)
				&& !Gstr1ConstantsV31.secReportList.contains(section))) {
			CommonUtilV31.throwException(ErrorCodesV31.ASP530, Gstr1ConstantsV31.ASP_GSTR1_GRP,
					ErrorCodesV31.ASP530, null, HttpStatus.INTERNAL_SERVER_ERROR,
					messageSourceV31, null);
		}

	}

}
