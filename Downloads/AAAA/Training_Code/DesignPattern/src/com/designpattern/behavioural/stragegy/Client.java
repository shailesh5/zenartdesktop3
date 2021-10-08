package com.designpattern.behavioural.stragegy;

import java.util.LinkedList;

public class Client {
	
	private static LinkedList<Order> orders = new LinkedList<>();
	
	public static void main(String[] args) {
		createOrders();
		
		PrintService service = new PrintService(new SummaryPrinter());
		service.printOrders(orders);
		
	}

	private static void createOrders() {
		
		Order order = new Order("100");
		order.addItems("Cake", 300);
		order.addItems("Cookie", 20);
		orders.add(order);
		
		order = new Order("200");
		order.addItems("Cola", 90);
		order.addItems("Pepsi", 100);
		orders.add(order);
		
		order = new Order("300");
		order.addItems("Chips", 10);
		order.addItems("Samosa", 20);
		orders.add(order);
		
		
		
	}

}
