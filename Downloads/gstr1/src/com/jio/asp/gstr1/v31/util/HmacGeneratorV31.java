package com.jio.asp.gstr1.v31.util;

import java.security.InvalidKeyException;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class HmacGeneratorV31 {

	public static String getHmac(String data, byte[] SessionKeyInBytes)
			throws NoSuchAlgorithmException, InvalidKeyException {

		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(SessionKeyInBytes, "HmacSHA256");
		sha256_HMAC.init(secret_key);

		String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(data.getBytes()));
		return hash;
	}
}
