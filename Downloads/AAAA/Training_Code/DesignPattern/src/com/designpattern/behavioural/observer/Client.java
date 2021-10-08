package com.designpattern.behavioural.observer;

public class Client {
	public static void main(String[] args) {
		Order order = new Order("100");
		
		PriceObserver observer = new PriceObserver();
		order.attach(observer);
		
		order.addItem(250);
		
		System.out.println(order);
		
	}

}
