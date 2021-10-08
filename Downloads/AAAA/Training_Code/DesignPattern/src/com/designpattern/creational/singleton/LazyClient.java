package com.designpattern.creational.singleton;

public class LazyClient {
	
	public static void main(String[] args) {
		
		LazySingleton instance = LazySingleton.getInstance();
		LazySingleton instance1 = LazySingleton.getInstance();
		
		System.out.println(instance == instance1);
		
		LazySingletonIODM lazyInstance1 = LazySingletonIODM.getInstance();
		LazySingletonIODM lazyInstance2 = LazySingletonIODM.getInstance();
		LazySingletonIODM lazyInstance3 = LazySingletonIODM.getInstance();
		
		System.out.println(lazyInstance1 == lazyInstance2);
		System.out.println(lazyInstance1 == lazyInstance3);
	}

}
