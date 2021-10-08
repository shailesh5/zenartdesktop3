package com.designpattern.creational.factorymethod;

public class Client {
	
	public static void main(String[] args) {
		
		printMessage(new JsonMessageCreator());
		printMessage(new TextMessageCreator());
		
	}

	private static void printMessage(MessageCreator messageCreator) {
		System.out.println(messageCreator.createMessage());
		
	}

}
