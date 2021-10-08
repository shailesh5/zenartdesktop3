package com.designpattern.behavioural.stragegy;

import java.util.LinkedList;

public class PrintService {
	
	OrderPrinter orderPrinter;
	
	public PrintService(OrderPrinter printer) {
		orderPrinter = printer;
	}
	
	public void printOrders(LinkedList<Order> orders) {
		orderPrinter.print(orders);
	}
}
