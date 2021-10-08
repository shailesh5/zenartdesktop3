package com.designpattern.creational.singleton;

public class EagerSingleton {
	
	private static final EagerSingleton INSTANCE = new EagerSingleton();
	
	private EagerSingleton() {}
	
	public static EagerSingleton getInstance() {
		return INSTANCE;
	}
	
	

}
