package com.designpattern.creational.singleton;

public class LazySingleton {
	
	private LazySingleton() {
		
	}
	private static volatile LazySingleton INSTANCE = null;
	
	public static LazySingleton getInstance() {
		
		if(INSTANCE == null) {
			synchronized(LazySingleton.class) {
				if(INSTANCE == null) {
					INSTANCE = new LazySingleton();
				}
			}
		}
		return INSTANCE;
	}
	
	

}
