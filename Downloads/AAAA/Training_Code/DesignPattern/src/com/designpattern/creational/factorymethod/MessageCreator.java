package com.designpattern.creational.factorymethod;

public abstract class MessageCreator {
	
	public Message getMessage() {
		Message msg = createMessage();
		msg.encrypt();
		msg.addDefaultHeaders();
		return msg;
		
	}

	public abstract Message createMessage();
	

}
