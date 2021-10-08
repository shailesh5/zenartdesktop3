package com.designpattern.creational.singleton;

public class LazySingletonIODM {
	
	private LazySingletonIODM() {
		System.out.println("Constructor Called !!!");
	}
	
	private static class LazySingletonHolder{
		static LazySingletonIODM INSTANCE = new LazySingletonIODM();
	}
	
	public static LazySingletonIODM getInstance() {
		return LazySingletonHolder.INSTANCE;
	}

}
