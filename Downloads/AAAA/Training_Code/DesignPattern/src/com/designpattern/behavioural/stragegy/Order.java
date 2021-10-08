package com.designpattern.behavioural.stragegy;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Order {
	
	private String id;
	private LocalDate date;
	private Map<String, Double> items = new HashMap<>();
	private double total;
	
	public Order(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	public LocalDate getDate() {
		return date;
	}
	public Map<String, Double> getItems() {
		return items;
	}
	public double getTotal() {
		return total;
	}
	
	public void addItems(String name, double price) {
		items.put(name, price);
		total += price;
	}
	
	

}
