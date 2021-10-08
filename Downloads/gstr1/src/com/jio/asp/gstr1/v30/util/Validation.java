package com.jio.asp.gstr1.v30.util;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import com.jio.asp.gstr1.v30.constant.ErrorCodes;
import com.jio.asp.gstr1.v30.constant.Gstr1Constants;
import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;

public class Validation {

	/**
	 * Common validation for Asp api header data. This method should be used
	 * only for those api which are specific to ASP. They should be going to
	 * call GSP or GSTN for any data. As ASP api will have limited data, this
	 * method should be only used for ASP API.
	 * 
	 * @param headerData,
	 *            header data to be validated.
	 * @param messageSource,
	 *            message resource to read the error message.
	 */
	public static void aspApiHeaderValidation(Map<String, String> headerData, MessageSource messageSource) {

		CommonUtil.validateEmpty(headerData, ErrorCodes.HEADER_DATA_NULL_ASP026, Gstr1Constants.ASP_HEADER_EXCEPTION,
				"GSTR1_SUM_ASP026", null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		String aspLocation = headerData.get("location");
		String ipUsr = headerData.get("ip-usr");
		String stateCode = headerData.get("state-cd");
		String txn = headerData.get("txn");
		String sourceDevice = headerData.get("source-device");
		String deviceString = headerData.get("device-string");
		String appCode = headerData.get("app-code");

		CommonUtil.validateEmptyString(txn, ErrorCodes.ASP011, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP011,
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

//		CommonUtil.validateLength(txn, Gstr1Constants.TRANSACTION_LENGTH, Gstr1Constants.TRANSACTION_LENGTH,
//				ErrorCodes.ASP012, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP012,
//				new Object[] { Gstr1Constants.TRANSACTION_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(appCode, ErrorCodes.ASP009, Gstr1Constants.ASP_HEADER_EXCEPTION,
				ErrorCodes.ASP009, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		CommonUtil.validateEmptyString(deviceString, ErrorCodes.ASP018, Gstr1Constants.ASP_HEADER_EXCEPTION,
				ErrorCodes.ASP018, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(aspLocation, ErrorCodes.ASP007, Gstr1Constants.ASP_HEADER_EXCEPTION,
				ErrorCodes.ASP007, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		CommonUtil.validateLength(aspLocation, Gstr1Constants.MIN_LENGTH, Gstr1Constants.LOCATION_LENGTH,
				ErrorCodes.ASP008, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP008,
				new Object[] { Gstr1Constants.LOCATION_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(sourceDevice, ErrorCodes.ASP019, Gstr1Constants.ASP_HEADER_EXCEPTION,
				ErrorCodes.ASP019, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		CommonUtil.validateLength(sourceDevice, Gstr1Constants.MIN_LENGTH, Gstr1Constants.SOURCE_DEVICE_LENGTH,
				ErrorCodes.ASP020, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP020,
				new Object[] { Gstr1Constants.SOURCE_DEVICE_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(ipUsr, ErrorCodes.ASP013, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP013,
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		CommonUtil.validateLength(ipUsr, Gstr1Constants.MIN_LENGTH, Gstr1Constants.IP_USER_LENGTH, ErrorCodes.ASP014,
				Gstr1Constants.ASP_HEADER_EXCEPTION, "ASP014", new Object[] { Gstr1Constants.IP_USER_LENGTH },
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(stateCode, ErrorCodes.ASP015, Gstr1Constants.ASP_HEADER_EXCEPTION,
				ErrorCodes.ASP015, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		CommonUtil.validateLength(stateCode, Gstr1Constants.STATE_CODE_LENGTH, Gstr1Constants.STATE_CODE_LENGTH,
				ErrorCodes.ASP016, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP016,
				new Object[] { Gstr1Constants.STATE_CODE_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		if (Integer.parseInt(stateCode) > Gstr1Constants.STATE_COUNT_LENGTH
				|| Integer.parseInt(stateCode) < Gstr1Constants.MIN_LENGTH) {
			if(Integer.parseInt(stateCode) != Gstr1Constants.STATE_COUNT_LENGTH_97){
			CommonUtil.throwException(ErrorCodes.ASP017, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP017,
					new Object[] { Gstr1Constants.STATE_COUNT_LENGTH, Gstr1Constants.MIN_LENGTH,Gstr1Constants.STATE_COUNT_LENGTH_97 },
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource, null);
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
	 * @param messageSource,
	 *            message resource to read the error message.
	 */
	public static void gstnApiHeaderValidation(Map<String, String> headerData, MessageSource messageSource) {
		// String aspLocation = headerData.get("location");
		// String sourceDevice = headerData.get("source-device");
		// String aspAppcode =headerData.get("app-code");
		CommonUtil.validateEmpty(headerData, ErrorCodes.HEADER_DATA_NULL_ASP026, Gstr1Constants.ASP_HEADER_EXCEPTION,
				"GSTR1_SUM_ASP026", null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		String stateCode = headerData.get(Gstr1Constants.HEADER_STATE_CODE);
		String txn = headerData.get(Gstr1Constants.HEADER_TXN);
		String ipUsr = headerData.get(Gstr1Constants.HEADER_IP);
		String deviceString = headerData.get(Gstr1Constants.HEADER_DEVICE_STRING);

		CommonUtil.validateEmptyString(MapUtils.getString(headerData, Gstr1Constants.INPUT_SEK),
				ErrorCodes.GSTR1_SUM_EMPTY, Gstr1Constants.ASP_HEADER_EXCEPTION, "GSTR1_HDR_SEK_ASP105", null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		CommonUtil.validateEmptyString(MapUtils.getString(headerData, Gstr1Constants.INPUT_APP_KEY),
				ErrorCodes.GSTR1_SUM_TABLE_TYPES_EMTY, Gstr1Constants.ASP_HEADER_EXCEPTION, "GSTR1_HDR_APP_KEY_ASP106",
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(MapUtils.getString(headerData, Gstr1Constants.HEADER_USER_NAME),
				ErrorCodes.ASP021, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP021, null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		CommonUtil.validateEmptyString(MapUtils.getString(headerData, Gstr1Constants.HEADER_AUTH_TOKEN),
				ErrorCodes.ASP022, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP022, null,
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(txn, ErrorCodes.ASP011, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP011,
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

/*		CommonUtil.validateLength(txn, Gstr1Constants.TRANSACTION_LENGTH, Gstr1Constants.TRANSACTION_LENGTH,
				ErrorCodes.ASP012, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP012,
				new Object[] { Gstr1Constants.TRANSACTION_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);*/

		CommonUtil.validateEmptyString(deviceString, ErrorCodes.ASP018, Gstr1Constants.ASP_HEADER_EXCEPTION,
				ErrorCodes.ASP018, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		//
		// CommonUtil.validateEmptyString(aspLocation, ErrorCodes.ASP007,
		// Gstr1Constants.ASP_HEADER_EXCEPTION,
		// ErrorCodes.ASP007, null, HttpStatus.INTERNAL_SERVER_ERROR,
		// messageSource);
		// CommonUtil.validateLength(aspLocation, Gstr1Constants.MIN_LENGTH,
		// Gstr1Constants.LOCATION_LENGTH, ErrorCodes.ASP008,
		// Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP008, new Object[]
		// { Gstr1Constants.LOCATION_LENGTH },
		// HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		// CommonUtil.validateEmptyString(sourceDevice, ErrorCodes.ASP019,
		// Gstr1Constants.ASP_HEADER_EXCEPTION,
		// ErrorCodes.ASP019, null, HttpStatus.INTERNAL_SERVER_ERROR,
		// messageSource);
		// CommonUtil.validateLength(sourceDevice, Gstr1Constants.MIN_LENGTH,
		// Gstr1Constants.SOURCE_DEVICE_LENGTH,
		// ErrorCodes.ASP020, Gstr1Constants.ASP_HEADER_EXCEPTION,
		// ErrorCodes.ASP020,
		// new Object[] { Gstr1Constants.SOURCE_DEVICE_LENGTH },
		// HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		// CommonUtil.validateEmptyString(aspAppcode, ErrorCodes.ASP009,
		// Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP009,
		// null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		// CommonUtil.validateLength(aspAppcode, Gstr1Constants.MIN_LENGTH,
		// Gstr1Constants.APP_CODE_LENGTH, ErrorCodes.ASP010,
		// Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP010, new Object[]
		// { Gstr1Constants.IP_USER_LENGTH },
		// HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(ipUsr, ErrorCodes.ASP013, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP013,
				null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		CommonUtil.validateLength(ipUsr, Gstr1Constants.MIN_LENGTH, Gstr1Constants.IP_USER_LENGTH, ErrorCodes.ASP014,
				Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP014, new Object[] { Gstr1Constants.IP_USER_LENGTH },
				HttpStatus.INTERNAL_SERVER_ERROR, messageSource);

		CommonUtil.validateEmptyString(stateCode, ErrorCodes.ASP015, Gstr1Constants.ASP_HEADER_EXCEPTION,
				ErrorCodes.ASP015, null, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		CommonUtil.validateLength(stateCode, Gstr1Constants.STATE_CODE_LENGTH, Gstr1Constants.STATE_CODE_LENGTH,
				ErrorCodes.ASP016, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP016,
				new Object[] { Gstr1Constants.STATE_CODE_LENGTH }, HttpStatus.INTERNAL_SERVER_ERROR, messageSource);
		if (Integer.parseInt(stateCode) > Gstr1Constants.STATE_COUNT_LENGTH
				|| Integer.parseInt(stateCode) < Gstr1Constants.MIN_LENGTH) {
			if(Integer.parseInt(stateCode) != Gstr1Constants.STATE_COUNT_LENGTH_97){
			CommonUtil.throwException(ErrorCodes.ASP017, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP017,
					new Object[] { Gstr1Constants.STATE_COUNT_LENGTH, Gstr1Constants.MIN_LENGTH,Gstr1Constants.STATE_COUNT_LENGTH_97 },
					HttpStatus.INTERNAL_SERVER_ERROR, messageSource, null);
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
	 * @param messageSource,
	 *            message resource to read the error message.
	 */
	public static void apiHeaderAndPayloadGstinValidation(String hdrGstin, String payloadGstin,
			MessageSource messageSource) {
		if (!hdrGstin.equalsIgnoreCase(payloadGstin)) {
			CommonUtil.throwException(ErrorCodes.ASP043, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP043,
					new Object[] { hdrGstin, payloadGstin }, HttpStatus.INTERNAL_SERVER_ERROR, messageSource, null);
		}

	}

	public static void apiHeaderAndParamsGstinValidation(String hdrGstin, String paramGstin,
			MessageSource messageSource) {
		if (!hdrGstin.equalsIgnoreCase(paramGstin)) {
			CommonUtil.throwException(ErrorCodes.ASP044, Gstr1Constants.ASP_HEADER_EXCEPTION, ErrorCodes.ASP044,
					new Object[] { hdrGstin, paramGstin }, HttpStatus.INTERNAL_SERVER_ERROR, messageSource, null);

		}

	}

}
