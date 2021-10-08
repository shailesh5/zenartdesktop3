package com.jio.asp.gstr1.v31.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Hash256Util {

	public static String hash(String dataToHash) {

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		md.update(dataToHash.getBytes());

		byte byteData[] = md.digest();

		// convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

	
	//Public method
//	public static void main(String args[]) {
//
//		Scanner sc = new Scanner(System.in);
//		String hash = sc.next();
//		System.out.println("--------------------");
//		System.out.println("Enter the data to hashed");
//		String Hashed = hash(hash);
//		System.out.println("Hashed: " + Hashed);
//		System.out.println("--------------------");
//
//	}

}
