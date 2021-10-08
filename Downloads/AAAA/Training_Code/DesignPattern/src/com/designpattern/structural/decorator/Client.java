package com.designpattern.structural.decorator;

public class Client {
	
	public static void main(String[] args) {
		
		Message message = new TextMessage("This is Decorator Design Pattern !!!");
		Message msg1 = new HtmlEncodedMessage(message);
		System.out.println(msg1.getContent());

		
		Message msg2 = new Base64EndodedMessage(message);
		System.out.println(msg2.getContent());
		
	}

}
