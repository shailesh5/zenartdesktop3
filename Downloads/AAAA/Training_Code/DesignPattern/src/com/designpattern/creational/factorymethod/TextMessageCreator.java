package com.designpattern.creational.factorymethod;

public class TextMessageCreator extends MessageCreator{

	@Override
	public Message createMessage() {
		return new TextMessage();
	}

}
