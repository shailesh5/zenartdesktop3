package com.designpattern.creational.singleton;

public class EagerClient {
	
	public static void main(String[] args) {
		EagerSingleton instance = EagerSingleton.getInstance();
		EagerSingleton instance1 = EagerSingleton.getInstance();
		
		System.out.println(instance == instance1);
	}

}
