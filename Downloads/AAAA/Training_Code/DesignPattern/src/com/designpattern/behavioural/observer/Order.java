package com.designpattern.behavioural.observer;

import java.util.ArrayList;
import java.util.List;

public class Order {
	
	private String id;
	
	//cost of item
	private double itemCost;
	
	//number of item
	private int quantity;
	
	private double discount;
	
	private double shippingCost;
	

	private List<OrderObserver> observers = new ArrayList<OrderObserver>();
	
	public void attach(OrderObserver orderObserver) {
		observers.add(orderObserver);
	}
	
	public void detach(OrderObserver orderObserver) {
		observers.remove(orderObserver);
	}
	
	public void addItem(double price) {
		itemCost += price;
		quantity++;
		observers.forEach(o->o.updated(this));
	}
	
	public Order(String id){
		this.id = id;
	}

	public double getItemCost() {
		return itemCost;
	}

	public int getQuantity() {
		return quantity;
	}
	
	public double getTotal() {
		return itemCost - discount + shippingCost;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public double getShippingCost() {
		return shippingCost;
	}

	public void setShippingCost(double shippingCost) {
		this.shippingCost = shippingCost;
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", itemCost=" + itemCost + ", quantity=" + quantity + ", discount=" + discount
				+ ", shippingCost=" + shippingCost + ", observers=" + observers + "]";
	}

	
}
