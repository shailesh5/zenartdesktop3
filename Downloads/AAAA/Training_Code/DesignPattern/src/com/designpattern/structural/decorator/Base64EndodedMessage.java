package com.designpattern.structural.decorator;

public class Base64EndodedMessage implements Message{

	private Message msg;
	
	public Base64EndodedMessage(Message msg) {
		this.msg = msg;
	}
	@Override
	public String getContent() {
		return msg.getContent().replaceAll(" ", "****");
	}

}
